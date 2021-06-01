package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.comparator.TmComparator;
import com.gof.dao.CorpCrdGrdPdDao;
import com.gof.dao.TransitionMatrixDao;
import com.gof.entity.CorpCrdGrdPd;
import com.gof.entity.CorpCumPd;
import com.gof.entity.TransitionMatrix;
import com.gof.enums.ECreditGrade;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;
import com.gof.util.MathUtil;

/**
 *  <p> 기업 신용등급별 부도율 산출 모형 
 *  <p> 신용평가사에서 제시하는 전이행렬 (신용등급간 이동 확률)을 이용하여 1년 부도율, 누적 부도율, 선도부도율을 산출함.
 *  
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class Job51_CorporatePd {
	private final static Logger logger = LoggerFactory.getLogger(Job51_CorporatePd.class);

	public static void writeCorpPd(String bssd, Properties prop) {
		

		Session session = HibernateUtil.getSessionFactory(prop).openSession();
		session.beginTransaction();

		for (CorpCrdGrdPd aa : getCorpPd(bssd)) {
			// logger.info("CorpCrdGrdPd is inserted : {}", aa.toString());
			session.saveOrUpdate(aa);
		}

		session.getTransaction().commit();
		logger.info("CorpCrdGrdPd is inserted");

	}

	public static List<CorpCrdGrdPd> getCorpPd(String bssd) {
		List<CorpCrdGrdPd> rstList = new ArrayList<CorpCrdGrdPd>();
		CorpCrdGrdPd temp;

		List<TransitionMatrix> rst = TransitionMatrixDao.getDefaultRate(bssd);

		Map<String, Double> volMap = getVolMap(bssd);

		for (TransitionMatrix aa : rst) {
			temp = new CorpCrdGrdPd();

			temp.setBaseYymm(bssd);
			temp.setCrdEvalAgncyCd("01");
			temp.setCrdGrdCd(ECreditGrade.getECreditGrade(aa.getFromGrade()).getLegacyCode());
			temp.setPd(aa.getTmRate());
			temp.setVol(volMap.getOrDefault(temp.getCrdGrdCd(), 0.0));
			// temp.setLastModifiedBy("JOB_52");
			temp.setLastModifiedBy(aa.getFromGrade());
			temp.setLastUpdateDate(LocalDateTime.now());

			rstList.add(temp);

		}
		logger.info("Job51( Corporate PD Calculation) creates  {} results.  They are inserted into EAS_CORP_CRD_GRD_PD Table", rstList.size());
		rstList.stream().forEach(s->logger.debug("Corporate PD Result : {}", s.toString()));
		
		return rstList;
	}
	public static void calcCorpCumPd(String bssd, Properties prop) {
		Session session = HibernateUtil.getSessionFactory(prop).openSession();
		session.beginTransaction();

		List<TransitionMatrix> rst = TransitionMatrixDao.getTM(bssd);
		rst.sort(new TmComparator());

		double[][] tm = new double[14][15];
		List<ECreditGrade> gradeList = new ArrayList<ECreditGrade>();
		int n = 0;
		int m = 0;

		for (TransitionMatrix aa : rst) {
			logger.info("Sorted : {},{},{}", aa.getFromGradeEnum().getAlias(), aa.getToGradeEnum().getAlias(),
					aa.getTmRate());
			tm[m][n] = aa.getTmRate();
			n = n + 1;
			if (n == 15) {
				m = m + 1;
				n = 0;
				gradeList.add(aa.getFromGradeEnum());
			}
		}

		List<CorpCumPd> rstList = new ArrayList<CorpCumPd>();
		CorpCumPd temp;
		double[][] fwdPd = getFwdPd(101, tm);
		int k = 0;
		double cumPd[] = new double[gradeList.size()];
		double tempfwdPd = 0.0;

		for (int i = 0; i < 100; i++) {
			k = 0;
			for (ECreditGrade aa : gradeList) {
				tempfwdPd = fwdPd[i][k];
				cumPd[k] = tempfwdPd + (1 - tempfwdPd) * cumPd[k];

				temp = new CorpCumPd();

				temp.setBaseYymm(bssd);
				temp.setAgencyCode("01");
				temp.setGradeCode(aa.getLegacyCode());
				temp.setMatCd("M" + String.format("%04d", (i + 1) * 12));
				temp.setCumPd(cumPd[k]);
				temp.setFwdPd(tempfwdPd);
				temp.setVol(0.01);
				temp.setLastModifiedBy("JOB_31");
				temp.setLastUpdated(LocalDateTime.now());

				k = k + 1;
				rstList.add(temp);
			}
		}

		for (CorpCumPd aa : rstList) {
			session.saveOrUpdate(aa);
		}

//		session.getTransaction().commit();
		logger.info("CorpCumPd is inserted");
//		session.close();

//		HiberUtil.shutdown();
	}
	public static void writeCorpCumPd(String bssd, Properties prop) {
		Session session = HibernateUtil.getSessionFactory(prop).openSession();
		session.beginTransaction();

		for (CorpCumPd aa : getCorpCumPd(bssd)) {
			session.saveOrUpdate(aa);
		}

		session.getTransaction().commit();
		logger.info("CorpCumPd is inserted");

	}

//	14,15 배열은 사용자가 입력하는 TM 의 신용등급 갯수임. row 는 정상 신용등급, column 은 정상 신용등급 + 부도등급으로 구성됨
	public static List<CorpCumPd> getCorpCumPd(String bssd) {
		List<CorpCumPd> rstList = new ArrayList<CorpCumPd>();
		CorpCumPd temp;

		List<TransitionMatrix> rst = TransitionMatrixDao.getTM(bssd);
		rst.sort(new TmComparator());

		double[][] tm = new double[14][15];
		List<ECreditGrade> gradeList = new ArrayList<ECreditGrade>();
		
		int n = 0;
		int m = 0;
		for (TransitionMatrix aa : rst) {
//			logger.info("Corp Cum PD : {},{},{}", aa.getFromGradeEnum().getAlias(), aa.getToGradeEnum().getAlias(),aa.getTmRate());
			tm[m][n] = aa.getTmRate();
			n = n + 1;
			if (n == 15) {
				m = m + 1;
				n = 0;
				gradeList.add(aa.getFromGradeEnum());
			}
		}

		double[][] fwdPd = getFwdPd(101, tm);
		int k = 0;
		double cumPd[] = new double[gradeList.size()];
		double tempfwdPd = 0.0;

		for (int i = 0; i < 100; i++) {
			k = 0;
			for (ECreditGrade aa : gradeList) {
				tempfwdPd = fwdPd[i][k];
				cumPd[k] = tempfwdPd + (1 - tempfwdPd) * cumPd[k];		//신용등급이 index 임. cumPd =0 에서 부터 fwd PD 가 적용되는 누적 PD =선도 PD=1년 PD 임. 

				temp = new CorpCumPd();

				temp.setBaseYymm(bssd);
				temp.setAgencyCode("01");
				temp.setGradeCode(aa.getLegacyCode());
				temp.setMatCd("M" + String.format("%04d", (i + 1) * 12));
				temp.setCumPd(cumPd[k]);
				temp.setFwdPd(tempfwdPd);
				temp.setVol(0.0);
				temp.setLastModifiedBy("ESG_51");
				temp.setLastUpdated(LocalDateTime.now());

				k = k + 1;
				rstList.add(temp);
			}
		}
		
		logger.info("Job51( Corporate Cumulative PD Calculation) creates  {} results.  They are inserted into EAS_CORP_CRD_GRD_CUM_PD Table", rstList.size());
		rstList.stream().forEach(s->logger.debug("Corporate Cumulative PD Result : {}", s.toString()));
		
		return rstList;
	}
	
	private static Map<String, Double> getVolMap(String bssd) {
		List<CorpCrdGrdPd> gradeList = CorpCrdGrdPdDao.getPrecedingCorpPd(bssd, -36);
		Map<String, List<CorpCrdGrdPd>> rstMap = gradeList.stream()
				.collect(Collectors.groupingBy(s -> s.getCrdGrdCd(), Collectors.toList()));
		return FinUtils.getVolMap(rstMap);
	}

//	선도부도율 산출 기간과 현재 TM 을 입력하여 선도 부도율을 산출함.  t-1 ~ t 년 사이의 선도 부도율은 t-1 index 에 저장됨
//	0~1 년 선도부도율= 현재 1년 부도율과 동일함. 1~2년 선도 부도율은 row index 1 에 저장됨.
//	신용등급은 순차적으로 column index 에 저장됨
	
	private static double[][] getFwdPd(int yearNum, double[][] tm) {

		double[][] fwdPd = new double[yearNum][tm.length];
		double[][] rstMatrix;
		double temp = 0.0;

		double[][] tempMatrix;

		tempMatrix = tm;
		rstMatrix = tm;
		
		// 	선도 부도율 Matrix 는 row 는 선도부도율 시점, column 은 선도부도율임.
		
		// 현재 시점의 선도부도율은 1년 부도율과 동일함. 15번째 column 이 현재 부도율임.
		for (int j = 0; j < tm.length; j++) {
			fwdPd[0][j] = tm[j][tm[0].length - 1];
		}
 		
		for (int i = 1; i < yearNum - 1; i++) {
			// TM 을 좌측에 반복곱하여 선도  부도율을 산출함. N년동안 부도가 발생하지 않은경우 향후 1년 동안 부도율을 의미함.
			rstMatrix = MathUtil.multi(tm, tempMatrix);				
			for (int j = 0; j < tm.length; j++) {
				fwdPd[i][j] = rstMatrix[j][tm[0].length - 1];		
			}
			tempMatrix = rstMatrix;
		}
		// logger.info("tm size : {},{}", tm.length, tm[0].length);
		return fwdPd;
	}

}

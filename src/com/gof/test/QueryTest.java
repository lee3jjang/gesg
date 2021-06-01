package com.gof.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.BottomupDcntDao;
import com.gof.dao.DaoUtil;
import com.gof.dao.DiscRateDao;
import com.gof.dao.DiscRateStatsDao;
import com.gof.dao.IrCurveHisDao;
import com.gof.dao.SmithWilsonDao;
import com.gof.entity.BottomupDcnt;
import com.gof.entity.DiscRateHis;
import com.gof.entity.DiscRateStats;
import com.gof.entity.EsgMeta;
import com.gof.entity.EsgMst;
import com.gof.entity.InvestManageCostUd;
import com.gof.entity.IrCurveHis;
import com.gof.entity.IrSce;
import com.gof.entity.SmithWilsonParam;
import com.gof.enums.EBoolean;
import com.gof.model.SmithWilsonModel;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;
import com.gof.util.ParamUtil;

public class QueryTest {
	private final static Logger logger = LoggerFactory.getLogger("DAO");
	private static Session session = HibernateUtil.getSessionFactory().openSession();
//	private static Session session = HibernateUtil.getSessionFactory().getCurrentSession();
	public static void main(String[] args) {
		String bssd ="201712";
		Map<String, Double> rstMap = getAllDriverCurveMapByMatCd(bssd, 0, "M0003", true);
		
		for( int i =-12; i< 1200; i++) {
			String st = FinUtils.addMonth(bssd, i);
			logger.info("zzz : {}, {}",st,  rstMap.get(st));
		}
//		rstMap.entrySet().stream().forEachOrdered(s -> logger.info("zzz : {}, {}", s.getKey(), s.getValue()));
		
	}
	
	private static void aaa(String bssd) {
		List<DiscRateHis> discRateHis = DiscRateDao.getDiscRateHis(bssd, -36);
		
		List<DiscRateHis> filteredList   = discRateHis.stream()
								.filter(s -> s.getIntRateCd().equals("2305"))
								.collect(Collectors.toList());
		
		filteredList.stream().forEachOrdered(s -> logger.info("zzz : {}", s));
		

		DiscRateStatsDao.getUserDiscRateAdj(bssd).forEach(s -> logger.info("zzzaaa :{}", s.getApplAdjRate()));
	}
	
	private static Map<String, Double> getAllDriverCurveMapByMatCd(String bssd,  int monthNum, String matCd, boolean isRiskFree){
		
		Map<String, Double> rstMap = new HashMap<String,  Double>();
		
		Map<String, Double> matCdRateMap = new HashMap<String,  Double>();
		
		matCdRateMap = getDriverCurveMap(bssd, isRiskFree);
		
//		미래시점의 금리 (forward 로 산출함)
//		rstMap = FinUtils.getForwardRateByMaturityZZ(bssd, matCdRateMap, matCd); 
		
//		과거 만기별 금리를 일자로 정렬
		for(int k= monthNum; k < 1; k++) {
			String prevBssd = FinUtils.addMonth(bssd, k);
			matCdRateMap = getDriverCurveMap(prevBssd, isRiskFree);
			rstMap.put(prevBssd, matCdRateMap.getOrDefault(matCd, new Double(0.0)));
		}
		return rstMap;
	}
	
	private static Map<String, Double> getDriverCurveMap(String bssd,  boolean isRiskFree){
		if(isRiskFree) {
			List<IrCurveHis> curveList = IrCurveHisDao.getIrCurveHis( bssd, "A100");		
				
			List<SmithWilsonParam> swParamList =SmithWilsonDao.getParamList();
			Map<String, SmithWilsonParam> swParamMap = swParamList.stream().collect(Collectors.toMap(s->s.getCurCd(), Function.identity()));
				 
			double ufr =  swParamMap.get("KRW").getUfr();
			double ufrt =  swParamMap.get("KRW").getUfrT();
				
			SmithWilsonModel swModel = new SmithWilsonModel(curveList, ufr, ufrt);
				
			return swModel.getSmithWilsionResult(false).stream().collect(Collectors.toMap(s->s.getMatCd(), s->s.getSpotAnnual()));
		}
		else {
				List<BottomupDcnt> dcntRateList = BottomupDcntDao.getTermStructure(bssd, "RF_KRW_BU");
				
				return dcntRateList.stream().collect(Collectors.toMap(s->s.getMatCd(), s-> s.getRiskAdjRfRate()))	;
		}
	}
}

package com.gof.process;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.log4j.Level;
import org.hibernate.Session;
import org.hibernate.transform.ToListResultTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.comparator.DcntSceComparator;
import com.gof.csvmapper.IrCurveHisMapper;
import com.gof.dao.BizDcntRateDao;
import com.gof.dao.BottomupDcntDao;
import com.gof.dao.DaoUtil;
import com.gof.dao.DiscRateDao;
import com.gof.dao.DiscRateStatsDao;
import com.gof.dao.EsgMstDao;
import com.gof.dao.EsgParamDao;
import com.gof.dao.InflationDao;
import com.gof.dao.IrCurveHisDao;
import com.gof.dao.LiqPremDao;
import com.gof.dao.SmithWilsonDao;
import com.gof.dao.SwaptionVolDao;
import com.gof.entity.BizDiscFwdRateSce;
import com.gof.entity.BizDiscRate;
import com.gof.entity.BizDiscRateStat;
import com.gof.entity.BizDiscountRate;
import com.gof.entity.BizDiscountRateSce;
import com.gof.entity.BizDiscountRateUd;
import com.gof.entity.BizEsgParam;
import com.gof.entity.BizInflation;
import com.gof.entity.BizLiqPremium;
import com.gof.entity.BottomupDcnt;
import com.gof.entity.DcntSce;
import com.gof.entity.DiscRate;
import com.gof.entity.DiscRateSce;
import com.gof.entity.DiscRateStats;
import com.gof.entity.EsgMst;
import com.gof.entity.HisDiscRate;
import com.gof.entity.IrCurve;
import com.gof.entity.IrCurveHis;
import com.gof.entity.IrSce;
import com.gof.entity.LogTable;
import com.gof.entity.ParamCalcHis;
import com.gof.entity.SmithWilsonParam;
import com.gof.entity.SmithWilsonParamHis;
import com.gof.entity.SwaptionVol;
import com.gof.enums.EBaseMatCd;
import com.gof.enums.EBoolean;
import com.gof.enums.ERunArgument;
import com.gof.model.SmithWilsonModel;
import com.gof.util.FileUtils;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;
import com.gof.util.LoggingOutputStream;
import com.gof.util.ParamUtil;

/**
 * ESG 의 Main 클래스. Process 패키지에 등록된 작업의 구동 여부, 구동 순서를 관리함.
 * 
 * @author takion77@gofconsulting.co.kr
 * @version 1.0
 *
 */
public class MainBackup {
	private final static Logger logger = LoggerFactory.getLogger(MainBackup.class);
	private static Map<ERunArgument, String> argMap = new HashMap<>();
	private static String output;
	private static int batchNum = 10;
	private static double dnsErrorTolerance = 0.00001;
	
	private static double kicsVolAdjust = 0.0;
	
//	private static double kicsUsdVolAdjust = 0.0032;
//	private static double kicsKrwVolAdjust = 0.0032;
	
	private static String kicsVolAdjustString ;
	private static Map<String, Double> kicsVolAdjustMap = new HashMap<String, Double>();

	private static double hwErrorTolerance = 0.00000001;
	private static double hw2ErrorTolerance = 0.00000001;
	private static String irSceGenSmithWilsonApply = "Y";
	
	private static long cnt = 0;
	private static long totalSize = 0;

	private static String paramGroup ;
	private static String jobString;
	private static String irSceCurrencyString;
	private static String irTenorString;
	private static String lqFittingModel;
	
	private static String bssd;
	private static int poolSize;
	private static ExecutorService exe ;
	private static Session session;
	private static int flushSize = 100000; 
//	private static int flushSize = 50;
	
	private static List<IrCurve> rfCurveList = new ArrayList<IrCurve>();
	private static List<IrCurve> bottomUpList = new ArrayList<IrCurve>();
	private static List<IrCurve> kicsList = new ArrayList<IrCurve>(); 
	private static List<String> jobList = new ArrayList<String>();
	private static Set<String> irSceCurrency = new HashSet<>();
	private static Set<String> irCurveTenor  = new HashSet<>();
	
	public static void main(String[] args) {
		System.setErr(new PrintStream(new LoggingOutputStream(org.apache.log4j.Logger.getLogger("outLog"), Level.ERROR), true));
		
// ******************************************************************Run Argument &  Common Data Setting ********************************
		init(args);		// 매개변수 설정 & 통화별 무위험 금리 곡선 ID ( spot 과 discount curve (bottomup/topdown/KICS) 으로 구분됨)
// ******************************************************************Pre Validation ********************************
		job1();			// 사전 데이터 Validation

// ******************************************************************ESG Parameter Job ********************************
		job11Async();	// Job11 : 매개변수 추정  : Vasicek, CIR, HULL AND WHITE, Hull and White 2 Factor
//		job11();		// Job11 : 매개변수 추정  : Vasicek, CIR, HULL AND WHITE, Hull and White 2 Factor
//		job12();		// job12 : KICS 의  Local Calibration 을 통한 parameter 산출 
		job13();		// job12 : 적용 매개변수 산출 : HULL AND WHITE 매개변수는 감독원 매개변수 적용 요건 반영 ( alpha : 전기간 동일, vol : 구간별 추정, 미관측기간 : 평균치 적용)
		
// ******************************************************************ESG Simulation Job ********************************
		job14();		// job14 : HULL WHITE 시나리오 생성
		job14FileAndDB();// job14 : HULL WHITE 시나리오 생성 DB 작업
		job15DBOnly();  // job15 : HULL WHITE 시나리오 생성 DB 작업
		job18();		// job18 : DNS 충격 시나리오 생성 TODO : 테이블 생성 & 저장 로직
		
// ****************************************************************** Bottom Up Job *********************************************************
		job20();		// Job 20 : 유동성 프리미엄 산출 :
		job21();		// Job 21 : 목적별 적용 유동성 프리미엄 산출 :
		
		job22();		// Job 22 : Smith Wilson 보간법 적용, 유동성 프리미엄 반영한 조정무위험 금리 곡선 생성
		job221();		// Job 222 : Smith Wilson 보간법 적용, 유동성 프리미엄 반영한 조정무위험 금리 곡선 생성==> Biz Apply
		
		job23();		// Job 23 : Bottom Up 시나리오 생성 : File Write Job complete!!!! 통화별 시나리오를 별도의 파일에 100 개씩 10번 쓴다
		job23DB();		// job 23 : BottomUp 시나리오생성 DB 작업 
		job24DBOnly();  // job 24 : BottomUp 시나리오생성 DB 작업
		job24New();  	// job 24 : BottomUp 시나리오생성 DB 작업
		
		job25();		// Job 25 : KICS 금리 기긴구조 산출 : 무위험 금리 + 변동성 조정
		job251();		// Job 25 : KICS 금리 기긴구조 산출 : 무위험 금리 + 변동성 조정  or User Input ==> Biz Apply DCNT
		job26();		// Job 26 : KICS 금리 시나리오
		job26DB();		// Job 27 : KICS 금리 시나리오 DB 작업
		job27DBOnly();
		job27New();
		
		job28();		// job 28 TopDown CashFlow 산출 할인율  TODO : CF 확인
		job29();		// job 29 TopDown CashFlow 산출 할인율
		job291();		// job 29 TopDown CashFlow 산출 할인율
		job292();		// job 29 TopDown CashFlow 산출 할인율
		job293();		// job 29 TopDown CashFlow 산출 할인율
		job294();		// job 29 TopDown CashFlow 산출 할인율
		

// ****************************************************************** 공시이율 **********************************************************
		
		job31();		// job31 : 내부모형 기준의 자산운용수익률, 외부금리, 공시이율  통계분석 결과 산출
		job32();		// job32 : 산출한 통계모형 또는 사용자가 입력한 통계모형 중 적용할 통계모형을 결정하며, 현재 공시이율 수준 Fitting 작업을 수행함. 
		
		job33();		// Job33 : 감독원 기준의 통계모형 또는 사용자가 입력한 통계모형 중 적용할 통계모형을 결정함. 현재 공시이율 수준 Fitting 작업은 미수행함. 
		job34();		// Job34 : Spread 모형  또는 사용자가 입력한 통계모형 중 적용할 통계모형을 결정함. 현재 공시이율 수준 Fitting 작업을 수행함. (Kics 모형 & Fitting 과 동일함.)
		job35();		// Job35 : 공시이율 추정치 생성 IFRS 모형(Job32 와 Pair 임)  
		job36();		// Job35 : 공시이율 추정치 생성 KICS 모형(Job33 과 Pair 임) 
		job37();		// Job35 : 공시이율 추정치 생성 Spread 모형( Job34 와 Pair 임) 

		job38();			// Job38 : 공시이율 시나리오 생성  :Deprecated 2019.07.01
//		job39Async();		// Job38 : 공시이율 시나리오 생성
		job39Async_All();   // Job39 : 공시이율 시나리오 생성 DB 작업 : Deprecated 2019.07.01       
		
// ****************************************************************** RC Job ********************************
		job41();		// Job 41 : 전년동월 대비 CPI 지수의 변동으로 Inflation 지수를 산출함
		job51();		// Job 51 : transiton Matrix 에서 누적 부도율을 산출하는 Job 임.
		job52();		// Job 52 : 감독원 소매 부도율 & 증감율 정보에서 소매 부도율, 누적/선도 부도율을 산출하는 Job 임
		job53();		// Job 53 : 감독원 부도시 손실률
		
// ****************************************************************** 목적별 데이터 이관 ********************************
		job61();		// Job 61 : IFRS 17 적용 할인율 데이터 이관
		job63();		// Job 61 : IFRS 17 적용 자산 RC 데이터 이관
		
		job71();		// Job 71 : KIcS 적용 할인율 데이터 이관
		job73();		// Job 72 : KIcS 적용 자산 RC 데이터 이관

// ****************************************************************** Other Job ********************************
		test();
// *********************************************************** End Job ********************************
//		HibernateUtil.shutdown();
		exe.shutdown();
	}

	private static void init(String[] args) {
		
		for (String aa : args) {
			logger.info("Input Arguments : {}", aa);
			for (ERunArgument bb : ERunArgument.values()) {
				if (aa.split("=")[0].toLowerCase().contains(bb.name())) {
					argMap.put(bb, aa.split("=")[1]);
					break;
				}
			}
		}
		
		bssd = argMap.get(ERunArgument.time).replace("-", "").replace("/", "").substring(0, 6);
		Properties properties = new Properties();
		try {
			FileInputStream fis = new FileInputStream(argMap.get(ERunArgument.properties));
			properties.load(new BufferedInputStream(fis));

		} catch (Exception e) {
			logger.warn("Error in Properties Loading : {}", e);
		}

		session = HibernateUtil.getSessionFactory(properties).openSession();
//		Session From DB
//		session = HibernateUtil.getSessionFactory().getCurrentSession();
//		session = HibernateUtil.getSessionFactory().openSession();
		logger.info("Session Info : {}", session.getProperties());
		
		paramGroup = properties.getOrDefault("paramGroup", "BASE").toString();
		
//		Map<String, String> argumentMap = EsgMstDao.getEsgParam(paramGroup).stream().collect(Collectors.toMap(s->s.getParamKey(), s->s.getParamValue()));
		Map<String, String> argumentMap = ParamUtil.getParamList(paramGroup).stream().collect(Collectors.toMap(s->s.getParamKey(), s->s.getParamValue()));
		
//		batchNum, dnsErrorTolerance, dnsVolAdjust, hwErrorTolerance, hw2ErrorTolerance, IrSceCurrency, outputDir 
		output 			  			= argumentMap.getOrDefault("outputDir", properties.get("outputDir").toString()) ;
		lqFittingModel    			= argumentMap.getOrDefault("lqFittingModel", properties.getOrDefault("lqFittingModel", "POLY_FITTING").toString()) ;
//		lqFittingModel    			= argumentMap.getOrDefault("lqFittingModel", properties.getOrDefault("lqFittingModel", "SW_FITTING").toString()) ;
		irSceGenSmithWilsonApply	= argumentMap.getOrDefault("irSceGenSmithWilsonApply", properties.getOrDefault("irSceGenSmithWilsonApply", "N").toString()) ;
		irSceCurrencyString  		= argumentMap.getOrDefault("IrSceCurrency", properties.getOrDefault("IrSceCurrency", "KRW").toString());
		irTenorString		  		= argumentMap.getOrDefault("IrCurveTenor ", properties.getOrDefault("IrCurveTenor", "M0003,M0006,M0009,M0012,M0024,M0036,M0060,M0084,M0120,M0240").toString());
//		irTenorString		  		= argumentMap.getOrDefault("IrCurveTenor ", properties.getOrDefault("IrCurveTenor", "3,6,9,12,24,36,60,84,120,0240").toString());
		
		
		batchNum 		  = Integer.parseInt(argumentMap.getOrDefault("batchNum", properties.getOrDefault("batchNum", "10").toString()));
		dnsErrorTolerance = Double.parseDouble(argumentMap.getOrDefault("dnsErrorTolerance", properties.getOrDefault("dnsErrorTolerance", "0.00001").toString()));
		
//		kicsVolAdjust     = Double.parseDouble(argumentMap.getOrDefault("kicsVolAdjust", properties.getOrDefault("kicsVolAdjust", "0.0032").toString()));
		
		
		hwErrorTolerance  = Double.parseDouble(argumentMap.getOrDefault("hwErrorTolerance", properties.getOrDefault("hwErrorTolerance", "0.0001").toString()));
		hw2ErrorTolerance = Double.parseDouble(argumentMap.getOrDefault("hw2ErrorTolerance", properties.getOrDefault("hw2ErrorTolerance", "0.0001").toString()));
		
		kicsVolAdjustString = argumentMap.getOrDefault("kicsVolAdjustString", properties.getOrDefault("kicsVolAdjustString", "0.0032").toString());
		
		jobString 			 = properties.get("job").toString();
		
		for(Map.Entry<String, String> entry : argumentMap.entrySet()) {
			if(entry.getKey().contains("JOB")) {
				jobString = entry.getValue();
			}
		}
		//Add for stepwise run !!!! 20190402
		if(properties.containsKey("job_batch")) {
			jobString 			 = properties.get("job_batch").toString();
		}
		
		jobList 	 = Arrays.stream(jobString .split(",")).map(s -> s.trim()).collect(Collectors.toList());
		irCurveTenor = Arrays.stream(irTenorString.split(",")).map(s -> s.trim()).collect(Collectors.toSet());
		irSceCurrency= Arrays.stream(irSceCurrencyString.split(",")).map(s -> s.trim()).collect(Collectors.toSet());
		
		String[] tempString = irSceCurrencyString.split(",");
		String[] tempValue  = kicsVolAdjustString.split(",");
		
		for(int i=0; i<tempString.length; i++) {
			kicsVolAdjustMap.put(tempString[i], Double.valueOf(tempValue[i]));
		}
		
		kicsVolAdjust = kicsVolAdjustMap.get("KRW");
		
		
		logger.info("Prop :{}, {} ", bssd, properties);
		argMap.entrySet().stream().forEach(s -> logger.info("Effective Arguments Input : {},{}", s.getKey(), s.getValue()));
		argumentMap.entrySet().forEach(s ->logger.info("Effective Arguments in DB : {},{}", s.getKey(), s.getValue()));
		
		jobList.stream().forEach(s -> logger.info("Job List : {}", s));
		
		rfCurveList = IrCurveHisDao.getIrCurveByCrdGrdCd("000").stream().filter(s -> irSceCurrency.contains(s.getCurCd())).collect(Collectors.toList());
		bottomUpList = IrCurveHisDao.getBottomUpIrCurve().stream().filter(s -> irSceCurrency.contains(s.getCurCd())).collect(Collectors.toList());
		kicsList     = IrCurveHisDao.getIrCurveByGenMethod("5").stream().filter(s -> irSceCurrency.contains(s.getCurCd())).collect(Collectors.toList());	//5 : KICS
		
		
//		Hibernate Context flush Size
		flushSize 	 = Integer.parseInt(argumentMap.getOrDefault("flushSize", properties.getOrDefault("flushSize", "100000").toString()));
		
//		병렬처리 서비스 생성 
		int maxThreadNum = Integer.parseInt(argumentMap.getOrDefault("maxThreadNum", properties.getOrDefault("maxThreadNum", "5").toString()));
//		poolSize = Math.min(maxThreadNum, Runtime.getRuntime().availableProcessors()) -1 ;
		poolSize = maxThreadNum;
//		poolSize = 1;
		logger.info("Number of Thread to Run in case of parallel process : {}" , poolSize);
		
		exe = Executors.newFixedThreadPool(poolSize, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			}
		});
		
		smithWilsonSetup();
	}

	private static void smithWilsonSetup() {
		session.beginTransaction();
		List<SmithWilsonParamHis> smList = SmithWilsonDao.getParamHisList(bssd);
		
		List<SmithWilsonParam> smRst = new ArrayList<SmithWilsonParam>(); 
		SmithWilsonParam temp;
		for(SmithWilsonParamHis aa : smList) {
			temp = new SmithWilsonParam();
			temp.setCurCd(aa.getCurCd());
			temp.setIrCurveDv(aa.getIrCurveDv());
			temp.setLlp(aa.getLlp());
			temp.setUfr(aa.getUfr());
			temp.setUfrT(aa.getUfrT());
			temp.setLastModifiedBy("ESG");
			temp.setLastUpdateDate(LocalDateTime.now());
			smRst.add(temp);
		}
		
		logger.info("SmithWilson Parameter Update : {},{}", smList.size(), smRst.size());
		for(SmithWilsonParam bb : smRst) {
			
			session.saveOrUpdate(bb);
		}
		session.getTransaction().commit();
	}
	
	private static void job1() {
		if (jobList.contains("1")) {
			logger.info("Job 1 : Validation start !!!");
			session.beginTransaction();

			// Job1_PreValidation.validateIrCurve(bssd);

			Job1_PreValidation.validateSwaptionVol(bssd);
			Job1_PreValidation.validateUsedEsgModel();
			
			Job1_PreValidation.validateBottomUpIrCurve();
			Job1_PreValidation.validDisclosureRateHis(bssd);
			Job1_PreValidation.validDiscRateCumAssetYield(bssd);
			Job1_PreValidation.validDiscRateAssetYield(bssd);
			Job1_PreValidation.validExternalIntRate(bssd);
			
			
			Job1_PreValidation.validTransitionMatrix(bssd);
			Job1_PreValidation.validTransitionMatrixSumEqualOne(bssd);
			session.getTransaction().commit();

			logger.info("Job 1 : Validation  is Completed !!!");
		}		
	}
	private static void job11Async() {
		if (jobList.contains("11")) {
			LocalDateTime startTime = LocalDateTime.now();
			logger.info("Job 11 : Parameter calculation start at {} !!!", startTime);
			session.beginTransaction();
			LogTable logTable = getLogTable("BADA1001JM", startTime,bssd);     
			try {
				List<IrCurveHis> curveRst = IrCurveHisDao.getKTBIrCurveHis(bssd);
				
				List<IrCurveHis> curveMatRst = IrCurveHisDao.getIrCurveHisByMaturityHis(bssd, -100, "A100", "M0003");
				Map<String, String> eom = IrCurveHisDao.getEomMap(bssd, "A100");
				List<IrCurveHis> eomCurveMatRst = curveMatRst.stream().filter(s->eom.containsKey(s)).collect(Collectors.toList());
				
				List<SwaptionVol> volRst = SwaptionVolDao.getSwaptionVol(bssd);
				
				volRst.forEach(s -> logger.info("Swaption Vol : {}", s));
				logger.info("Swpation vol Size : {},{}", volRst.size());
				
				List<SmithWilsonParam> swParam = DaoUtil.getEntities(SmithWilsonParam.class, new HashMap<String, Object>());
				List<SmithWilsonParam> swParamList = swParam.stream().filter(s->s.getCurCd().equals("KRW")).collect(Collectors.toList());
	
				double ufr = swParamList.isEmpty()? 0.045: swParamList.get(0).getUfr();
				double ufrt = swParamList.isEmpty()? 60: swParamList.get(0).getUfrT();
				
				
				CompletableFuture<List<ParamCalcHis>> 	vasicekFuture = CompletableFuture.supplyAsync(()-> Job11_EsgParameter.createVasicekParamCalcHisAsync(bssd, curveRst, ufr, ufrt,hwErrorTolerance), exe);
				CompletableFuture<List<ParamCalcHis>> 	cirkFuture = CompletableFuture.supplyAsync(()-> Job11_EsgParameter.createCirParamCalcHisAsync(bssd, curveRst, ufr, ufrt, hwErrorTolerance), exe);
				CompletableFuture<List<ParamCalcHis>> 	hwFuture = CompletableFuture.supplyAsync(()-> Job11_EsgParameter.createHwParamCalcHisAsync(bssd, curveRst, volRst, ufr, ufrt, hwErrorTolerance), exe);
				CompletableFuture<List<ParamCalcHis>> 	hwKicsFuture = CompletableFuture.supplyAsync(()-> Job11_EsgParameter.createHwKicsParamCalcHisAsync(bssd, curveRst, volRst, ufr, ufrt, hwErrorTolerance), exe);
				CompletableFuture<List<ParamCalcHis>> 	hw2Future = CompletableFuture.supplyAsync(()-> Job11_EsgParameter.createHw2FactorParamCalcHisAsync(bssd, curveRst, volRst, ufr, ufrt, hwErrorTolerance), exe);
				
				List<CompletableFuture<List<ParamCalcHis>>> futureList = new ArrayList<CompletableFuture<List<ParamCalcHis>>>();
				futureList.add(cirkFuture);
				futureList.add(vasicekFuture);
				futureList.add(hwFuture);
				futureList.add(hwKicsFuture);
				futureList.add(hw2Future);
				
				List<ParamCalcHis> rst = futureList.stream().map(CompletableFuture::join).flatMap(s ->s.stream()).collect(Collectors.toList());
				
				rst.stream().forEach(s ->session.saveOrUpdate(s));
				
				session.flush();
			} catch (Exception e) {
				logTable.updateFail(e);  
				logger.info("Erro : {}", e);
			}
			session.saveOrUpdate(logTable);
			session.getTransaction().commit();

			logger.info("Job 11 : Parameter calculation  is Completed !!!");
		}
	}
	
	private static void job13() {
		if (jobList.contains("13")) {
			LocalDateTime startTime = LocalDateTime.now();
			logger.info("Job 13(Applied Param Calculation) START at {}!!!", startTime);
			LogTable logTable = getLogTable("BADA1001JM", startTime,bssd);     
			session.beginTransaction();
			List<BizEsgParam> rst = new ArrayList<BizEsgParam>();
			try {
				session.createQuery("delete BizEsgParam a where a.baseYymm=:param").setParameter("param", bssd).executeUpdate();
				
				session.createQuery("delete HisEsgParam a where a.baseYymm=:param and a.seq=:seq")
								.setParameter("param", bssd).setParameter("seq", 0).executeUpdate();
				
				int  maxSeq = EsgParamDao.getMaxSeq(bssd);
				
				rst = Job13_BizEsgParameter.createBizAppliedParameter(bssd);
				rst.stream().forEach(s -> session.save(s));
				
				rst.stream().map(s -> s.convertToHisEsgParam(maxSeq+1)).forEach(s -> session.save(s));
				rst.stream().map(s -> s.convertToHisEsgParam(0)).forEach(s -> session.save(s));
				
			} catch (Exception e) {
				logTable.updateFail(e);         
				logger.info("Erro : {}", e);
			}
			session.saveOrUpdate(logTable);
			session.getTransaction().commit();
			logger.info("Job 13(Applied Param Calculation) is completed!!!");
		}	
	}
	
	private static void job14() {
		if (jobList.contains("141")) {
			LocalDateTime startTime = LocalDateTime.now();
			logger.info("Job 14(IR Scenario) START at {}!!!",startTime);
			for(IrCurve aa : rfCurveList) {	
				logger.info("Ir Scenario for {} : {}", aa.getCurCd(), aa.getIrCurveNm());
				Path irScePath = Paths.get(output + "IrSce_" + aa.getCurCd() + "_" + bssd + ".csv");
				
				FileUtils.reset(irScePath);
				
//				사전 프로세스 - 데이터 준비
				List<IrCurveHis> irCurveHisList = IrCurveHisDao.getIrCurveHis(bssd, aa.getIrCurveId());
//				List<EsgMst> esgMstList = EsgMstDao.getEsgMstWithBizAppliedParam(bssd, EBoolean.Y);
				List<EsgMst> esgMstList = EsgMstDao.getEsgMst(EBoolean.Y);
				EsgMst tempEsgMst       = esgMstList.get(0);
				List<BizEsgParam> bizParamHis = EsgParamDao.getBizEsgParam(bssd, "I", tempEsgMst.getIrModelId());
				
				Stream<SmithWilsonParam> swStream        = DaoUtil.getEntityStream(SmithWilsonParam.class, new HashMap<>());
				Map<String, SmithWilsonParam> swParamMap = swStream.collect(Collectors.toMap(s->s.getCurCd(), Function.identity()));
				double ufr  =  swParamMap.get(aa.getCurCd()).getUfr();
				double ufrt =  swParamMap.get(aa.getCurCd()).getUfrT();
				
				double shortRate = FinUtils.spanFullBucket(bssd, irCurveHisList).stream().filter(s ->s.getMatCd().equals("M0001")).map(s -> s.getIntRate()).findFirst().orElse(0.01);
				String irCurveId = irCurveHisList.get(0).getIrCurveId();
//				사전 프로세스 끝

//				통화별로 산출 결과를 생성하기 위한 병렬처리 
				List<CompletableFuture<List<IrSce>>> 	sceJobFutures =	
						IntStream.range(0,batchNum)
								.mapToObj(sceNo-> CompletableFuture.supplyAsync(() 
													->  Job15_EsgScenarioAsync.createEsgScenarioAsync(bssd, irCurveId, irCurveHisList, bizParamHis, tempEsgMst, ufr, ufrt, shortRate, sceNo), exe))
								.collect(Collectors.toList());
					
//				병렬처리 결과 집계
				List<IrSce> rst = sceJobFutures.stream().map(CompletableFuture::join)
														.flatMap(s ->s.stream())
														.collect(Collectors.toList());
				
//				금리 시나리오 파일 결과 Write
				try (Writer writer = new BufferedWriter(new FileWriter(irScePath.toFile(), true));			
					) 
				{
					for(IrSce zz : rst) {
						writer.write(zz.toString());
					}
				} catch (Exception e) {
					logger.error("Error in Ir Scenario result writing : {}", e);
				}
			}
			logger.info("Job 14(IR Scenario) is Completed !!!");
		}		
	}
	
	private static void job14FileAndDB() {
		if (jobList.contains("14")) {
			LocalDateTime startTime = LocalDateTime.now();
			LogTable logTable = getLogTable("BADA1001JM", startTime,bssd);   
			logger.info("Job 15(Hull and White Scenario DB) START at {}!!!", startTime);
			session.beginTransaction();
			try {
				session.createQuery("delete IrSce a where a.baseDate=:param")
						.setParameter("param", bssd)
						.executeUpdate();
				
				for(IrCurve aa : rfCurveList) {	
					logger.info("Ir Scenario for {} : {}", aa.getCurCd(), aa.getIrCurveNm());
					Path irScePath = Paths.get(output + "IrSce_" + aa.getCurCd() + "_" + bssd + ".csv");
					
					FileUtils.reset(irScePath);
					
	//				사전 프로세스 - 데이터 준비
					List<IrCurveHis> irCurveHisList = IrCurveHisDao.getIrCurveHis(bssd, aa.getIrCurveId());
					List<EsgMst> esgMstList = EsgMstDao.getEsgMst(EBoolean.Y);
					EsgMst tempEsgMst       = esgMstList.get(0);
					List<BizEsgParam> bizParamHis = EsgParamDao.getBizEsgParam(bssd, "I", tempEsgMst.getIrModelId());				
					
					
					Stream<SmithWilsonParam> swStream        = DaoUtil.getEntityStream(SmithWilsonParam.class, new HashMap<>());
					Map<String, SmithWilsonParam> swParamMap = swStream.collect(Collectors.toMap(s->s.getCurCd(), Function.identity()));
					double ufr  =  swParamMap.get(aa.getCurCd()).getUfr();
					double ufrt =  swParamMap.get(aa.getCurCd()).getUfrT();
					
					double shortRate = FinUtils.spanFullBucket(bssd, irCurveHisList).stream().filter(s ->s.getMatCd().equals("M0001")).map(s -> s.getIntRate()).findFirst().orElse(0.01);
					String irCurveId = irCurveHisList.get(0).getIrCurveId();
	//				사전 프로세스 끝
	
	//				batchNum =2;
	//				통화별로 산출 결과를 생성하기 위한 병렬처리 
					List<CompletableFuture<List<IrSce>>> 	sceJobFutures =	
							IntStream.rangeClosed(1,batchNum)
									.mapToObj(sceNo-> CompletableFuture.supplyAsync(() 
														->  Job15_EsgScenarioAsync.createEsgScenarioAsync(bssd, irCurveId, irCurveHisList, bizParamHis, tempEsgMst, ufr, ufrt, shortRate, sceNo), exe))
									.collect(Collectors.toList());
						
	//				병렬처리 결과 집계
					List<IrSce> rst = sceJobFutures.stream().map(CompletableFuture::join)
															.flatMap(s ->s.stream())
															.collect(Collectors.toList());
					
					
	//				금리 시나리오 결과 파일 Write
					try (Writer writer = new BufferedWriter(new FileWriter(irScePath.toFile(), true));			
						) 
					{
						for(IrSce zz : rst) {
							writer.write(zz.toString());
						}
					} catch (Exception e) {
						logger.error("Error in Ir Scenario result writing : {}", e);
					}
					
					int sceCnt = 1;
	//				금리 시나리오 결과 DB Write
					for (IrSce bb :rst) {
						session.save(bb);
						if (sceCnt % 50 == 0) {
							session.flush();
							session.clear();
						}
						if (sceCnt % flushSize == 0) {
							logger.info("IR Scenario for {}  is processed {}/{} in Job 15 {}", aa.getCurCd(),sceCnt, batchNum * 100 * 1200);
						}
						sceCnt = sceCnt + 1;
					}
				}
			} catch (Exception e) {
				logTable.updateFail(e);
				logger.info("Erro : {}", e);
			}
			session.saveOrUpdate(logTable);
			session.getTransaction().commit();
			logger.info("Job 15(Hull and White Scenario DB) is Completed !!!");
		}
	}
	
	private static void job15DBOnly() {
		if (jobList.contains("15")) {
			LocalDateTime startTime = LocalDateTime.now();
			LogTable logTable = getLogTable("BADA1001JM", startTime,bssd);   
			logger.info("Job 15(Hull and White Scenario DB) START at {}!!!", startTime);
			session.beginTransaction();
			try {
				session.createQuery("delete IrSce a where a.baseDate=:param")
						.setParameter("param", bssd)
						.executeUpdate();
				
				for(IrCurve aa : rfCurveList) {	
					logger.info("Ir Scenario for {} : {}", aa.getCurCd(), aa.getIrCurveNm());
					
	//				사전 프로세스 - 데이터 준비
					List<IrCurveHis> irCurveHisList = IrCurveHisDao.getIrCurveHis(bssd, aa.getIrCurveId());
					List<EsgMst> esgMstList = EsgMstDao.getEsgMst(EBoolean.Y);
					EsgMst tempEsgMst       = esgMstList.get(0);
					List<BizEsgParam> bizParamHis = EsgParamDao.getBizEsgParam(bssd, "I", tempEsgMst.getIrModelId());				
					
					
					Stream<SmithWilsonParam> swStream        = DaoUtil.getEntityStream(SmithWilsonParam.class, new HashMap<>());
					Map<String, SmithWilsonParam> swParamMap = swStream.collect(Collectors.toMap(s->s.getCurCd(), Function.identity()));
					double ufr  =  swParamMap.get(aa.getCurCd()).getUfr();
					double ufrt =  swParamMap.get(aa.getCurCd()).getUfrT();
					
					double shortRate = FinUtils.spanFullBucket(bssd, irCurveHisList).stream().filter(s ->s.getMatCd().equals("M0001")).map(s -> s.getIntRate()).findFirst().orElse(0.01);
					String irCurveId = irCurveHisList.get(0).getIrCurveId();
	//				사전 프로세스 끝
	
	//				batchNum =2;
	//				통화별로 산출 결과를 생성하기 위한 병렬처리 
					List<CompletableFuture<List<IrSce>>> 	sceJobFutures =	
							IntStream.rangeClosed(1,batchNum)
									.mapToObj(sceNo-> CompletableFuture.supplyAsync(() 
														->  Job15_EsgScenarioAsync.createEsgScenarioAsync(bssd, irCurveId, irCurveHisList, bizParamHis, tempEsgMst, ufr, ufrt, shortRate, sceNo), exe))
									.collect(Collectors.toList());
						
	//				병렬처리 결과 집계
					List<IrSce> rst = sceJobFutures.stream().map(CompletableFuture::join)
															.flatMap(s ->s.stream())
															.collect(Collectors.toList());
					logger.info("aaa : {}", rst.size());
					int sceCnt = 1;
					for (IrSce bb :rst) {
						session.save(bb);
						if (sceCnt % 50 == 0) {
							session.flush();
							session.clear();
						}
						if (sceCnt % flushSize == 0) {
							logger.info("IR Scenario for {}  is processed {}/{} in Job 15 {}", aa.getCurCd(),sceCnt, rst.size());
						}
						sceCnt = sceCnt + 1;
					}
				}
			} catch (Exception e) {
				logTable.updateFail(e);
				logger.info("Erro : {}", e);
			}
			session.saveOrUpdate(logTable);
			session.getTransaction().commit();
			logger.info("Job 15(Hull and White Scenario DB) is Completed !!!");
		}
	}
	
	private static void job18() {
		if (jobList.contains("18")) {
			LocalDateTime startTime = LocalDateTime.now();                              
			LogTable logTable = getLogTable("BADA1001JM", startTime,bssd);    

			logger.info("Job 18(DNS Shock Scenario) START at {}!!!", startTime);
			session.beginTransaction();
			try {
				Job18_DnsScenario.createDnaShockScenario(bssd, "KRW", "A100", 1, dnsErrorTolerance, kicsVolAdjust).forEach(s -> session.saveOrUpdate(s));
			} catch (Exception e) {
				logTable.updateFail(e);  
				logger.info("Erro : {}", e);
			}
			
			session.saveOrUpdate(logTable); 
			session.getTransaction().commit();
			logger.info("Job 18(DNS Shcok Scenario) is Completed !!!");
	
		}
	}
	
//	20210420  수정
	private static void job20() {
		if (jobList.contains("20")) {
			LocalDateTime startTime = LocalDateTime.now();                              
//			LogTable logTable = getLogTable("BADA1002JM", startTime,bssd);    
			logger.info("Job 20(Liquid Premium) Start at {} !!! ", startTime);
			session.beginTransaction();
			try {
				
				int deleteNum = session.createQuery("delete LiqPremium a where a.baseYymm=:param").setParameter("param", bssd).executeUpdate();
				logger.info("Job 20(Liquid Premium) delete {} rows in the db", deleteNum);
				
				String lqModelId = ParamUtil.getParamMap().getOrDefault("lqModelId", "COVERED_BOND_KDB");
				
//				Job20_LiquidPremium.createLiquidPremium(bssd, lqFittingModel).stream().forEach(s -> session.saveOrUpdate(s));
				Job20_LiquidPremium_Obs.createLiquidPremium(bssd, lqModelId, bottomUpList).stream().forEach(s -> session.saveOrUpdate(s));
				
			} catch (Exception e) {
//				logTable.updateFail(e); 
//				logTable.setErrLogCn1(e.getMessage());
			}
//			session.saveOrUpdate(logTable.updateExecTime(LocalDateTime.now())); 
			session.getTransaction().commit();
			logger.info("Job 20(Liquid Premium) is Completed !!!");
		}
	}
	
//	20210420  수정
	private static void job21() {
		if (jobList.contains("21")) {
			LocalDateTime startTime = LocalDateTime.now();                              
			LogTable logTable = getLogTable("BADA1002JM", startTime,bssd);
			logger.info("Job 21(Biz Applied Liquid Premium) Start at {} !!!", startTime);
			session.beginTransaction();
			List<BizLiqPremium> rst = new ArrayList<BizLiqPremium>();
			try {
				int deleteNum = session.createQuery("delete BizLiqPremium a where a.baseYymm=:param and a.applyBizDv =:param2")
								.setParameter("param", bssd)
								.setParameter("param2", "I")
								.executeUpdate();
				logger.info("Job 21(Biz Liquid Premium) delete {} rows in the db", deleteNum);
				
				for(IrCurve aa : bottomUpList) {
					String lqModelId = ParamUtil.getParamMap().getOrDefault("lqModelId", "COVERED_BOND_KDB");

					rst = Job21_BizLiquidPremium.createBizLiqPremium(bssd, aa, lqModelId, irCurveTenor);
					
					rst.stream().forEach(s -> session.saveOrUpdate(s));
//					rst.stream().forEach(s -> logger.info("aaaa : {}, {},{}", s.getMatCd(), s.getApplyLiqPrem(), s.getLiqPrem()));
					
					int  maxSeq = LiqPremDao.getMaxSeq(bssd, "I");
					rst.stream().map(s -> s.convertToHisLiqPremium(0)).forEach(s -> session.saveOrUpdate(s));
					rst.stream().map(s -> s.convertToHisLiqPremium(maxSeq+1)).forEach(s -> session.save(s));
				}

				
			} catch (Exception e) {
				logTable.updateFail(e);     
			}
			session.saveOrUpdate(logTable); 
			session.getTransaction().commit();
			logger.info("Job 21(Biz Applied Liquid Premium) is Completed !!!");
		}
	}
	
//	20210420  수정
	private static void job22() {
		if (jobList.contains("22")) {
			LocalDateTime startTime = LocalDateTime.now();                              
			LogTable logTable = getLogTable("BADA1002JM", startTime,bssd);
			logger.info("Job 22(BottomUp Curve) Start at {}!!!", startTime);
			session.beginTransaction();
			try {
				List<String> bottomupIdList = bottomUpList.stream().map(s->s.getIrCurveId()).collect(Collectors.toList());
				
				int deleteNum = session.createQuery("delete BottomupDcnt a where a.baseYymm=:param and a.irCurveId IN ( :param2 ) ")
							.setParameter("param", bssd)
							.setParameterList("param2", bottomupIdList)
							.executeUpdate();
//				
				logger.info("Job 22(BottomUp Curve) delete {} rows for {} in the db", deleteNum, bottomupIdList);
				
				bottomUpList.forEach(s-> logger.info("Job 22(BottomUp Curve) : {}",s.getIrCurveId())) ;
				
//				Job22_BottomUp.createBottomUpCurve(bssd, bottomUpList).stream().forEach(s -> session.save(s));
				Job22_BottomUp.createBottomUpCurveNew(bssd, "I", bottomUpList, irCurveTenor).stream().forEach(s -> session.saveOrUpdate(s));
				
//				Job221_IFRSBizApplyDcnt.getBizDcntRate(bssd).stream().forEach(s -> session.saveOrUpdate(s));
				
			} catch (Exception e) {
				logTable.updateFail(e);
				logger.error("zzz : {}", e);
			}
			
			session.saveOrUpdate(logTable); 
			session.getTransaction().commit();
			logger.info("Job 22(BottomUp Curve) is Completed !!!");
		}
	}
//	202105  추가 : 작업 순서 조정 ( 61==> 221)
	private static void job221() {
		if (jobList.contains("221")) {
			LocalDateTime startTime = LocalDateTime.now();
			LogTable logTable = getLogTable("BADA1003JM", startTime,bssd);
			
			logger.info("Job 221(IFRS Applied Discount Rate ETL) start !!!");
			session.beginTransaction();
			try {
//				Job221_IFRSBizApplyDcnt.getBizDcntRate(bssd).stream().forEach(s -> session.saveOrUpdate(s));
				Job221_IFRSBizApplyDcnt.getBizDcntRate(bssd, bottomUpList).stream().forEach(s -> session.saveOrUpdate(s));
			} catch (Exception e) {
				logTable.updateFail(e);
				logger.info("Error : {}", e);
			}
			session.saveOrUpdate(logTable);	
			session.getTransaction().commit();
			logger.info("Job 221(IFRS Applied Discount Rate ETL) Results are committed !!!");
		}	
	}
	
	private static void job23() {
		if (jobList.contains("231")) {
			List<DcntSce> sceRst = new ArrayList<DcntSce>();

//			SmithWilson parameter 호출
			Stream<SmithWilsonParam> swStream        = DaoUtil.getEntityStream(SmithWilsonParam.class, new HashMap<>());
			Map<String, SmithWilsonParam> swParamMap = swStream.collect(Collectors.toMap(s->s.getCurCd(), Function.identity()));

//			적용할 유동성 프리미엄 호출 
			String lqModelId = ParamUtil.getParamMap().getOrDefault("lqModelId", "COVERED_BOND_KDB");
//			List<BizLiqPremium> lpRst = Job21_BizLiquidPremium.createBizLiqPremium(bssd, lqModelId, irCurveTenor);
			List<BizLiqPremium> lpRst = LiqPremDao.getBizLiquidPremium(bssd, "I");
			
			logger.info("Load Liquidity Premium. Loaded Data Size :{}.", lpRst.size());
			
			for(IrCurve aa : rfCurveList) {		
				
				Path irScePath = Paths.get(output + "IrSce_" + aa.getCurCd()+ "_" + bssd + ".csv");
				Path dcntScePath = Paths.get(output + "DcntSce_" + aa.getCurCd() + "_" + bssd + ".csv");
				Path bizDcntScePath = Paths.get(output + "BizDcntSce_" + aa.getCurCd() + "_" + bssd + ".csv");
				
				logger.info("Read Risk Free Ir Scanrio File for  {} : {}", aa.getCurCd(), irScePath.getFileName());
				logger.info("Result File for Discount Scenario for {} is {}", aa.getCurCd(), dcntScePath);

				double ufr  =  swParamMap.get(aa.getCurCd()).getUfr();
				double ufrt =  swParamMap.get(aa.getCurCd()).getUfrT();
				
				int sceCnt = 0;
				
//				금리시나리오 File Reading
				try (Stream<IrCurveHis> rStream = IrCurveHisMapper.readEntityNoHeader(irScePath)) {

					logger.info("Current Thread 1:  {},{},{}", Thread.currentThread().getId(), Thread.currentThread().getName(), irSceGenSmithWilsonApply);
					FileUtils.reset(dcntScePath);
					FileUtils.reset(bizDcntScePath);
					
					List<CompletableFuture<List<DcntSce>>> sceJobFutures ;
					
//					금리 시나리오의 적용 방법에 따라 발산형과 수렴형으로 분기 (Y : 수렴형으로 smith wilson 의 장기금리로 수렴함)
					if(irSceGenSmithWilsonApply.equals("Y")) {
						Map<String, List<IrCurveHis>> sceMap = rStream.filter(s -> s.isBaseTerm()).collect(Collectors.groupingBy(s -> s.getSceNo(), Collectors.toList()));
						sceJobFutures =
								sceMap.entrySet().stream()
								.map(entry -> CompletableFuture.supplyAsync(() 
												->  Job23_BottomUpScenario.createBottomUpScenarioSmithWilson(bssd,entry.getKey(), "RF_" + aa.getCurCd() + "_BU", entry.getValue(), lpRst, ufr, ufrt), exe))
								.collect(Collectors.toList());
					}
					else {
						Map<String, List<IrCurveHis>> sceMap = rStream.collect(Collectors.groupingBy(s -> s.getSceNo(), Collectors.toList()));
						 sceJobFutures =
								sceMap.entrySet().stream()
								.map(entry -> CompletableFuture.supplyAsync(() ->  Job23_BottomUpScenario.createBottomUpScenario(bssd,entry.getKey(), "RF_" + aa.getCurCd() + "_BU", entry.getValue(), lpRst), exe))
								.collect(Collectors.toList());
					}
					
//					금리 시나리오 파일 단위로 병렬처리된 결과 집계 
					List<DcntSce> rst = sceJobFutures.stream().map(CompletableFuture::join)
															  .flatMap(s ->s.stream())
															  .collect(Collectors.toList());
					rst.sort(new DcntSceComparator());
					logger.info("DcntSce : {}", rst.size());
					
//					할인율 시나리오 결과 파일 작성
					try (Writer writer = new BufferedWriter(new FileWriter(dcntScePath.toFile(), true));
							Writer bizWriter = new BufferedWriter(new FileWriter(bizDcntScePath.toFile(), true));	) 
					{
						for(DcntSce zz : rst) {
							writer.write(zz.toString());
							bizWriter.write(zz.toStringWithBizDv(",", "I"));
						}
						
					} catch (Exception e) {
							logger.error("Error in DcntSce result writing : {}", e);
					}

				} catch (Exception e) {
					logger.info("Current Thread 3:  {},{}", Thread.currentThread().getId(),Thread.currentThread().getName());
					logger.info("Error in Bottom Up Scenario : {}", e);
				}
			}
			
			logger.info("Job 23(Bottom Up Scenario) is Completed !!!");
		}		
	}

	private static void job23DB() {
		if (jobList.contains("23")) {
			LocalDateTime startTime = LocalDateTime.now();                            
			LogTable logTable = getLogTable("BADA1002JM", startTime,bssd);  

			logger.info("Job 23(Bottom Up Scenario DB ) Start at {} !!!", startTime);
			session.beginTransaction();
			try {
				session.createQuery("delete DcntSce a where a.baseYymm=:param and a.irCurveId like :param2")
						.setParameter("param", bssd)
						.setParameter("param2", "%BU")
						.executeUpdate();
				
				session.createQuery("delete BizDiscountRateSce a where a.baseYymm=:param and a.applyBizDv =:param2")
						.setParameter("param", bssd)
						.setParameter("param2", "I")
						.executeUpdate();
				
	//			List<DcntSce> sceRst = new ArrayList<DcntSce>();
				
	//			SmithWilson parameter 호출
				Stream<SmithWilsonParam> swStream        = DaoUtil.getEntityStream(SmithWilsonParam.class, new HashMap<>());
				Map<String, SmithWilsonParam> swParamMap = swStream.collect(Collectors.toMap(s->s.getCurCd(), Function.identity()));
				
	//			적용할 유동성 프리미엄 호출 
//				String lqModelId = ParamUtil.getParamMap().getOrDefault("lqModelId", "COVERED_BOND_KDB");
//				List<BizLiqPremium> lpRst = Job21_BizLiquidPremium.createBizLiqPremium(bssd, lqModelId, irCurveTenor);
				
				List<BizLiqPremium> lpRst = LiqPremDao.getBizLiquidPremium(bssd, "I");
				logger.info("Load Liquidity Premium. Loaded Data Size :{}.", lpRst.size());
//				lpRst.forEach(s -> logger.info("aaa : {},{},{}", s.getMatCd(), s.getApplyLiqPrem(), s.getLiqPrem()));
				
				
				for(IrCurve aa : rfCurveList) {		
					
					Path irScePath = Paths.get(output + "IrSce_" + aa.getCurCd()+ "_" + bssd + ".csv");
					Path dcntScePath = Paths.get(output + "DcntSce_" + aa.getCurCd() + "_" + bssd + ".csv");
					Path bizDcntScePath = Paths.get(output + "BizDcntSce_" + aa.getCurCd() + "_" + bssd + ".csv");
					
					logger.info("Read Risk Free Ir Scanrio File for  {} : {}", aa.getCurCd(), irScePath.getFileName());
					logger.info("Result File for Discount Scenario for {} is {}", aa.getCurCd(), dcntScePath);
	
					double ufr  =  swParamMap.get(aa.getCurCd()).getUfr();
					double ufrt =  swParamMap.get(aa.getCurCd()).getUfrT();
					
					int sceCnt = 1;
					
	//				금리시나리오 File Reading
					try (Stream<IrCurveHis> rStream = IrCurveHisMapper.readEntityNoHeader(irScePath)) {
	
						logger.info("Current Thread 1:  {},{},{}", Thread.currentThread().getId(), Thread.currentThread().getName(), irSceGenSmithWilsonApply);
						FileUtils.reset(dcntScePath);
						FileUtils.reset(bizDcntScePath);
						
						List<CompletableFuture<List<DcntSce>>> sceJobFutures ;
						
	//					금리 시나리오의 적용 방법에 따라 발산형과 수렴형으로 분기 (Y : 수렴형으로 smith wilson 의 장기금리로 수렴함)
						irSceGenSmithWilsonApply ="N";
						if(irSceGenSmithWilsonApply.equals("Y")) {
							Map<String, List<IrCurveHis>> sceMap = rStream.filter(s -> s.isBaseTerm()).collect(Collectors.groupingBy(s -> s.getSceNo(), Collectors.toList()));
							sceJobFutures =
									sceMap.entrySet().stream()
									.map(entry -> CompletableFuture.supplyAsync(() 
//												->  Job23_BottomUpScenario.createBottomUpScenarioSmithWilson(bssd,entry.getKey(), "RF_" + aa.getCurCd() + "_BU", entry.getValue(), lpRst, ufr, ufrt), exe))
												->  Job23_BottomUpScenario.createBottomUpScenarioAddLp(bssd,entry.getKey(), "RF_" + aa.getCurCd() + "_BU", entry.getValue(), lpRst, ufr, ufrt), exe))
									.collect(Collectors.toList());
						}
						else {
							Map<String, List<IrCurveHis>> sceMap = rStream.collect(Collectors.groupingBy(s -> s.getSceNo(), Collectors.toList()));
							 sceJobFutures =
									sceMap.entrySet().stream()
									.map(entry -> CompletableFuture.supplyAsync(() ->  Job23_BottomUpScenario.createBottomUpScenario(bssd,entry.getKey(), "RF_" + aa.getCurCd() + "_BU", entry.getValue(), lpRst), exe))
									.collect(Collectors.toList());
						}
						
	//					금리 시나리오 파일 단위로 병렬처리된 결과 집계 
						List<DcntSce> rst = sceJobFutures.stream().map(CompletableFuture::join)
																  .flatMap(s ->s.stream())
																  .collect(Collectors.toList());
						rst.sort(new DcntSceComparator());
						logger.info("DcntSce for {}  : {}", aa.getIrCurveId(), rst.size());
						
						
	//					할인율 시나리오 결과 파일 작성
						try (Writer writer = new BufferedWriter(new FileWriter(dcntScePath.toFile(), true));
								Writer bizWriter = new BufferedWriter(new FileWriter(bizDcntScePath.toFile(), true));	) 
						{
							for(DcntSce zz : rst) {
								writer.write(zz.toString());
								bizWriter.write(zz.toStringWithBizDv(",", "I"));
							}
							
						} catch (Exception e) {
								logger.error("Error in DcntSce result writing : {}", e);
								throw e;
						}
						
	//					할인율 시나리오 DB Write
						for (DcntSce bb : rst) {
							session.save(bb);
							if (sceCnt % 50 == 0) {
								session.flush();
								session.clear();
								
							}
							if (sceCnt % flushSize == 0) {
								logger.info("Discount Scenario for {}  is processed {}/{} in Job 24 {}", aa.getCurCd(),sceCnt, batchNum * 100 * 1200);
							}
							sceCnt = sceCnt + 1;
						}
						
						sceCnt = 1;
						List<BizDiscountRateSce> bizSCe = rst.stream().map(s -> s.convertToBizDcntSce("I")).collect(Collectors.toList()); 
						for(BizDiscountRateSce cc : bizSCe) {
							session.save(cc);
							if (sceCnt % 50 == 0) {
								session.flush();
								session.clear();
							}
							if (sceCnt % flushSize == 0) {
								logger.info("Biz Discount Scenario for {}  is processed {}/{} in Job 24 {}", aa.getCurCd(),sceCnt, batchNum * 100 * 1200);
							}
							sceCnt = sceCnt + 1;
						}
	
					} catch (Exception e) {
						logger.info("Error in Bottom Up Scenario : {}", e);
						throw e;
					}
				}
			} catch (Exception e) {
				logTable.updateFail(e);
			}
			session.saveOrUpdate(logTable);
			session.getTransaction().commit();
			logger.info("Job 24(Bottom Up Scenario DB) is Completed !!!");
		}
	}
	
	private static void job24DBOnly() {
		if (jobList.contains("241")) {
			LocalDateTime startTime = LocalDateTime.now();                            
			LogTable logTable = getLogTable("BADA1002JM", startTime,bssd);  

			logger.info("Job 24(Bottom Up Scenario DB Only ) Start at {} !!!", startTime);
			session.beginTransaction();
			try {
				session.createQuery("delete DcntSce a where a.baseYymm=:param and a.irCurveId like :param2")
						.setParameter("param", bssd)
						.setParameter("param2", "%BU")
						.executeUpdate();
				
				session.createQuery("delete BizDiscountRateSce a where a.baseYymm=:param and a.applyBizDv =:param2")
						.setParameter("param", bssd)
						.setParameter("param2", "I")
						.executeUpdate();
				
	//			List<DcntSce> sceRst = new ArrayList<DcntSce>();
				
	//			SmithWilson parameter 호출
				Stream<SmithWilsonParam> swStream        = DaoUtil.getEntityStream(SmithWilsonParam.class, new HashMap<>());
				Map<String, SmithWilsonParam> swParamMap = swStream.collect(Collectors.toMap(s->s.getCurCd(), Function.identity()));
				
	//			적용할 유동성 프리미엄 호출 
//				String lqModelId = ParamUtil.getParamMap().getOrDefault("lqModelId", "COVERED_BOND_KDB");
//				List<BizLiqPremium> lpRst = Job21_BizLiquidPremium.createBizLiqPremium(bssd, lqModelId, irCurveTenor);
				List<BizLiqPremium> lpRst = LiqPremDao.getBizLiquidPremium(bssd, "I");
				
				logger.info("Load Liquidity Premium. Loaded Data Size :{}.", lpRst.size());
//				lpRst.forEach(s -> logger.info("aaa : {},{},{}", s.getMatCd(), s.getApplyLiqPrem(), s.getLiqPrem()));
				
				
				for(IrCurve aa : rfCurveList) {		
//					logger.info("sceMap : {},{}", aa.getIrCurveId());
					double ufr  =  swParamMap.get(aa.getCurCd()).getUfr();
					double ufrt =  swParamMap.get(aa.getCurCd()).getUfrT();
					
					int sceCnt = 1;

					logger.info("Current Thread 1:  {},{},{}", Thread.currentThread().getId(), Thread.currentThread().getName(), irSceGenSmithWilsonApply);
					List<CompletableFuture<List<DcntSce>>> sceJobFutures ;
					
					Stream<IrCurveHis> rStream =IrCurveHisDao.getIrCurveSce(bssd, aa.getIrCurveId()).map(s->s.convertToIrCurveHis());
					
	//				금리 시나리오의 적용 방법에 따라 발산형과 수렴형으로 분기 (Y : 수렴형으로 smith wilson 의 장기금리로 수렴함)
					irSceGenSmithWilsonApply ="N";
					if(irSceGenSmithWilsonApply.equals("Y")) {
						Map<String, List<IrCurveHis>> sceMap = rStream.filter(s -> s.isBaseTerm()).collect(Collectors.groupingBy(s -> s.getSceNo(), Collectors.toList()));
						
						sceJobFutures =
							sceMap.entrySet().stream()
								.map(entry -> CompletableFuture.supplyAsync(() 
//												->  Job23_BottomUpScenario.createBottomUpScenarioSmithWilson(bssd,entry.getKey(), "RF_" + aa.getCurCd() + "_BU", entry.getValue(), lpRst, ufr, ufrt), exe))
												->  Job23_BottomUpScenario.createBottomUpScenarioAddLp(bssd,entry.getKey(), "RF_" + aa.getCurCd() + "_BU", entry.getValue(), lpRst, ufr, ufrt), exe))
								.collect(Collectors.toList());
					}
					else {
						Map<String, List<IrCurveHis>> sceMap = rStream.collect(Collectors.groupingBy(s -> s.getSceNo(), Collectors.toList()));
						 sceJobFutures =
								sceMap.entrySet().stream()
								.map(entry -> CompletableFuture.supplyAsync(() ->  Job23_BottomUpScenario.createBottomUpScenario(bssd,entry.getKey(), "RF_" + aa.getCurCd() + "_BU", entry.getValue(), lpRst), exe))
								.collect(Collectors.toList());
					}
						
//					금리 시나리오 파일 단위로 병렬처리된 결과 집계 
					List<DcntSce> rst = sceJobFutures.stream().map(CompletableFuture::join)
																  .flatMap(s ->s.stream())
																  .collect(Collectors.toList());
//					rst.sort(new DcntSceComparator());
					logger.info("DcntSce for {}  : {}", aa.getIrCurveId(), rst.size());
						
						
//					할인율 시나리오 DB Write
						for (DcntSce bb : rst) {
							session.save(bb);
							if (sceCnt % 50 == 0) {
								session.flush();
								session.clear();
								
							}
							if (sceCnt % flushSize == 0) {
								logger.info("Discount Scenario for {}  is processed {}/{} in Job 24 {}", aa.getCurCd(),sceCnt, batchNum * 100 * 1200);
							}
							sceCnt = sceCnt + 1;
						}
						
						sceCnt = 1;
						List<BizDiscountRateSce> bizSCe = rst.stream().map(s -> s.convertToBizDcntSce("I")).collect(Collectors.toList()); 
						for(BizDiscountRateSce cc : bizSCe) {
							session.save(cc);
							if (sceCnt % 50 == 0) {
								session.flush();
								session.clear();
							}
							if (sceCnt % flushSize == 0) {
								logger.info("Biz Discount Scenario for {}  is processed {}/{} in Job 24 {}", aa.getCurCd(),sceCnt, batchNum * 100 * 1200);
							}
							sceCnt = sceCnt + 1;
						}
				}	
			} catch (Exception e) {
				logTable.updateFail(e);
				logger.info("Erro : {}", e);
			}
			session.saveOrUpdate(logTable);
			session.getTransaction().commit();
			logger.info("Job 24(Bottom Up Scenario DB) is Completed !!!");
		}
	}
	
//	20210420  수정
	private static void job24New() {
		if (jobList.contains("24")) {
			LocalDateTime startTime = LocalDateTime.now();                            
			LogTable logTable = getLogTable("BADA1002JM", startTime,bssd);  

			logger.info("Job 24(Bottom Up Scenario DB Only ) Start at {} !!!", startTime);
			session.beginTransaction();
			try {
				String bizDv="I";
				
				session.createQuery("delete DcntSce a where a.baseYymm=:param and a.irCurveId like :param2")
						.setParameter("param", bssd)
						.setParameter("param2", "%BU")
						.executeUpdate();
				
				session.createQuery("delete BizDiscountRateSce a where a.baseYymm=:param and a.applyBizDv =:param2")
						.setParameter("param", bssd)
						.setParameter("param2", "I")
						.executeUpdate();
				
	//			SmithWilson parameter 호출 및 통화별 매개변수 정리
				Stream<SmithWilsonParam> swStream        = DaoUtil.getEntityStream(SmithWilsonParam.class, new HashMap<>());
				Map<String, SmithWilsonParam> swParamMap = swStream.collect(Collectors.toMap(s->s.getCurCd(), Function.identity()));
				
				
				for(IrCurve aa : bottomUpList) {	
					logger.info("Ir Scenario for {} : {}", aa.getCurCd(), aa.getIrCurveNm());
					
	//				사전 프로세스 - 데이터 준비
					List<EsgMst> esgMstList = EsgMstDao.getEsgMst(EBoolean.Y);
					EsgMst tempEsgMst       = esgMstList.get(0);
					
					List<BizEsgParam> bizParamHis = EsgParamDao.getBizEsgParam(bssd, bizDv, tempEsgMst.getIrModelId());
					
					List<BizDiscountRate> bottomUpList = BizDcntRateDao.getTermStructure(bssd, aa.getIrCurveId());
					
					List<IrCurveHis> irCurveHisList = bottomUpList.stream().map(s-> s.convert()).collect(Collectors.toList());
					
					List<IrCurveHis> irCurveHisBaseList = bottomUpList.stream().filter(s->irCurveTenor.contains(s.getMatCd()))
																	   			.map(s-> s.convert()).collect(Collectors.toList());
					
					
					double ufr  =  swParamMap.get(aa.getCurCd()).getUfr();
					double ufrt =  swParamMap.get(aa.getCurCd()).getUfrT();
					
//					double shortRate = FinUtils.spanFullBucket(bssd, irCurveHisBaseList).stream().filter(s ->s.getMatCd().equals("M0001")).map(s -> s.getIntRate()).findFirst().orElse(0.01);
//					BIZ APPLY DCNT  : M0001~M1200 
					double shortRate = irCurveHisList.stream().filter(s ->s.getMatCd().equals("M0001")).map(s -> s.getIntRate()).findFirst().orElse(0.01);
					String irCurveId = irCurveHisList.get(0).getIrCurveId();
					
					
	//				사전 프로세스 끝
					
	//				batchNum =2;
	//				통화별로 산출 결과를 생성하기 위한 병렬처리 
					List<CompletableFuture<List<DcntSce>>> 	sceJobFutures =	
							IntStream.rangeClosed(1,batchNum)
									.mapToObj(sceNo-> CompletableFuture.supplyAsync(() 
														->  Job15_EsgScenarioAsync.createDcntScenarioAsync(bssd, irCurveId, irCurveHisBaseList, bizParamHis, tempEsgMst, ufr, ufrt, shortRate, sceNo), exe))
									.collect(Collectors.toList());
						
	//				병렬처리 결과 집계
					List<DcntSce> rst = sceJobFutures.stream().map(CompletableFuture::join)
															.flatMap(s ->s.stream())
															.collect(Collectors.toList());
					
					int sceCnt = 1;
	//				금리 시나리오 결과 DB Write
					
					for (DcntSce bb : rst) {
						session.save(bb);
						if (sceCnt % 50 == 0) {
							session.flush();
							session.clear();
							
						}
						if (sceCnt % flushSize == 0) {
							logger.info("Discount Scenario for {}  is processed {}/{} in Job 24 {}", aa.getCurCd(),sceCnt, batchNum * 100 * 1200);
						}
						sceCnt = sceCnt + 1;
					}
					
					sceCnt = 1;
					String currSceNo="1";

					List<BizDiscountRateSce> bizSce = new ArrayList<BizDiscountRateSce>();
					
					irSceGenSmithWilsonApply ="N";
					if(irSceGenSmithWilsonApply.equals("Y")) {
						
						List<CompletableFuture<List<DcntSce>>> 	anotherFutures =	
								IntStream.rangeClosed(1,batchNum)
										.mapToObj(sceNo-> CompletableFuture.supplyAsync(() 
															->  Job15_EsgScenarioAsync.createDcntScenarioAsync(bssd, irCurveId, irCurveHisBaseList, bizParamHis, tempEsgMst, ufr, ufrt, shortRate, sceNo), exe))
										.collect(Collectors.toList());
						
						List<DcntSce> anotherRst = anotherFutures.stream().map(CompletableFuture::join)
															.flatMap(s ->s.stream())
															.collect(Collectors.toList());
						
						bizSce = anotherRst.stream().map(s -> s.convertToBizDcntSce(bizDv)).collect(Collectors.toList());
					}	
					else if(irSceGenSmithWilsonApply.equals("YY")) {
						Map<String, List<DcntSce>> rstMap = rst.stream().filter(s->irCurveTenor.contains(s.getMatCd()))
																.collect(Collectors.groupingBy(DcntSce::getSceNo, Collectors.toList()));
						for(int i=1; i<=batchNum * 100; i++) {
//						for(int i=1; i<=10; i++) {
							currSceNo = String.valueOf(i);
							logger.info("aaaa : {},{}", currSceNo, rstMap.get(currSceNo).size());
							SmithWilsonModel sw = new SmithWilsonModel(bssd, aa.getIrCurveId(), currSceNo, rstMap.get(currSceNo));
							bizSce.addAll(sw.convertToIrCurveSce(true, bizDv));
						}
					}
					else {
						bizSce = rst.stream().map(s -> s.convertToBizDcntSce(bizDv)).collect(Collectors.toList()); 
					}
					
					for(BizDiscountRateSce cc : bizSce) {
						session.save(cc);
						if (sceCnt % 50 == 0) {
							session.flush();
							session.clear();
						}
						if (sceCnt % flushSize == 0) {
							logger.info("Biz Discount Scenario for {}  is processed {}/{} in Job 24 {}", aa.getCurCd(),sceCnt, batchNum * 100 * 1200);
						}
						sceCnt = sceCnt + 1;
					}
				}
			} catch (Exception e) {
				logTable.updateFail(e);
				logger.info("Erro : {}", e);
			}
			session.saveOrUpdate(logTable);
			session.getTransaction().commit();
			logger.info("Job 24(Hull and White Scenario DB) is Completed !!!");
		}
	}
	
//	20210420  수정
	private static void job25() {
		if (jobList.contains("25")) {
			LocalDateTime startTime = LocalDateTime.now();                             
			LogTable logTable = getLogTable("BADA1002JM", startTime,bssd);   

			logger.info("Job 25(KICS IR Curve) Start at {}!!!", startTime);
			session.beginTransaction();
			try {
				List<String> kicsIdList = kicsList.stream().map(s->s.getIrCurveId()).collect(Collectors.toList());
				
				int deleteNum = session.createQuery("delete BottomupDcnt a where a.baseYymm=:param and a.irCurveId IN ( :param2 ) ")
							.setParameter("param", bssd)
							.setParameterList("param2", kicsIdList)
							.executeUpdate();
//				
				logger.info("Job 25(BottomUp Curve) delete {} rows for {} in the db", deleteNum, kicsIdList);
				
				kicsList.forEach(s-> logger.info("Job 25(BottomUp Curve) : {}",s.getIrCurveId())) ;
				
//				session.createQuery("delete BottomupDcnt a where a.baseYymm=:param").setParameter("param", bssd).executeUpdate();
				Job25_KicsTermStructure.createKicsTermStructure(bssd, kicsList, kicsVolAdjustMap).stream().forEach(s -> session.saveOrUpdate(s));
				
//				Job251_KicsBizApplyDcnt.getBizDcntRateKics(bssd).stream().forEach(s -> session.saveOrUpdate(s));
				
			} catch (Exception e) {
				// TODO: handle exception
				logTable.updateFail(e); 
			}
					
//			session.saveOrUpdate(logTable);  
			session.getTransaction().commit();
			logger.info("Job 25(KICS IR Curve) is Completed !!!");
		}
	}
	
//	202105  추가 : 작업 순서 조정 ( 71==> 251)
	private static void job251() {
		if (jobList.contains("251")) {
			LocalDateTime startTime = LocalDateTime.now();
			LogTable logTable = getLogTable("BADA1003JM", startTime,bssd);
			logger.info("Job 251(KICS Applied Discount Rate) start !!!");
			session.beginTransaction();
			try {
				
//				Job251_KicsBizApplyDcnt.getBizDcntRateKics(bssd).stream().forEach(s -> session.saveOrUpdate(s));
				Job251_KicsBizApplyDcnt.getBizDcntRateKics(bssd, kicsList).stream().forEach(s -> session.saveOrUpdate(s));
			} catch (Exception e) {
				logTable.updateFail(e);
				logger.info("Error : {}", e);
			}
			session.saveOrUpdate(logTable);	
			session.getTransaction().commit();
			logger.info("Job 251(KICS Applied Discount Rate ETL) Results are committed !!!");
		}
	}
	
	private static void job26() {
		if (jobList.contains("261")) {
			logger.info("Job 26(KICS IR Scenario) Start !!!");
//			List<IrCurve> kicsList = IrCurveHisDao.getIrCurveByGenMethod("5");				//5 : KICS
			
			List<SmithWilsonParam> swParam = DaoUtil.getEntities(SmithWilsonParam.class, new HashMap<String, Object>());
			Map<String, SmithWilsonParam> swParamMap = swParam.stream().collect(Collectors.toMap(s ->s.getCurCd(), Function.identity()));
			
			double ufr  = swParamMap.containsKey("KRW") ? swParamMap.get("KRW").getUfr() : 0.045 ;
			double ufrt = swParamMap.containsKey("KRW") ? swParamMap.get("KRW").getUfrT(): 60  ;
			
			for(IrCurve aa : kicsList) {		
				Path irScePath = Paths.get(output + "IrSce_" + aa.getCurCd()+ "_" + bssd + ".csv");
				Path kicsScePath = Paths.get(output + "KicsSce_" + aa.getCurCd() + "_" + bssd + ".csv");
				Path bizKicsScePath = Paths.get(output + "BizKicsSce_" + aa.getCurCd() + "_" + bssd + ".csv");
	
				logger.info("Read Risk Free Ir Scanrio File for  {} : {}", aa.getCurCd(), irScePath.getFileName());
				logger.info("Result File for KICS IR Scanrio for {} is {}", aa.getCurCd(), kicsScePath);
	
				int sceCnt = 0;
				double volAdj = kicsVolAdjustMap.getOrDefault(aa.getCurCd(), 0.0);
				try (Stream<IrCurveHis> rStream = IrCurveHisMapper.readEntityNoHeader(irScePath)) {
	
					FileUtils.reset(kicsScePath);
					FileUtils.reset(bizKicsScePath);
					
					List<CompletableFuture<List<DcntSce>>> sceJobFutures ;
						
					if(irSceGenSmithWilsonApply.equals("Y")) {
						Map<String, List<IrCurveHis>> sceMap = rStream.filter(s -> s.isBaseTerm()).collect(Collectors.groupingBy(s -> s.getSceNo(), Collectors.toList()));
							sceJobFutures =
								sceMap.entrySet().stream()
								.map(entry -> CompletableFuture.supplyAsync(() ->  
												Job26_KicsTsScenario.createKicsTsSmithWilson(bssd,entry.getKey(), "RF_" + aa.getCurCd() + "_KICS", entry.getValue(), volAdj, ufr, ufrt), exe))
								.collect(Collectors.toList());
					}
					else {
						Map<String, List<IrCurveHis>> sceMap = rStream.collect(Collectors.groupingBy(s -> s.getSceNo(), Collectors.toList()));
						 sceJobFutures =
								sceMap.entrySet().stream()
								.map(entry -> CompletableFuture.supplyAsync(() ->  
												Job26_KicsTsScenario.createKicsTsScenario(bssd,entry.getKey(), "RF_" + aa.getCurCd() + "_KICS", entry.getValue(), volAdj), exe))
								.collect(Collectors.toList());
					}
						
					List<DcntSce> rst = sceJobFutures.stream().map(CompletableFuture::join).flatMap(s ->s.stream()).collect(Collectors.toList());
					rst.sort(new DcntSceComparator());
					logger.info("DcntSce for KICS : {}", rst.size());
						
					try (Writer writer = new BufferedWriter(new FileWriter(kicsScePath.toFile(), true));
							Writer bizWriter = new BufferedWriter(new FileWriter(bizKicsScePath.toFile(), true));
						) 
					{
						for(DcntSce zz : rst) {
							writer.write(zz.toString());
							bizWriter.write(zz.toStringWithBizDv(",", "K"));
						}
					} catch (Exception e) {
						logger.error("Error in DcntSce result writing : {}", e);
					}
	
//					sceCnt = sceCnt + 1;
//					if (sceCnt % 100 == 0) {
//						logger.info("KICS IR Scenario for {}  is processed {}/{} in Job 26", aa.getCurCd(),sceCnt, batchNum * 100);
//					}
				} catch (Exception e) {
					logger.info("Error in KICS IR Scenario : {}", e);
				}
			}
			logger.info("Job 26(KICS TS Scenario) is Completed !!!");
		}
	}
	
	private static void job26DB() {
		if (jobList.contains("26")) {
			LocalDateTime startTime = LocalDateTime.now();                           
			LogTable logTable = getLogTable("BADA1002JM", startTime,bssd); 

			logger.info("Job 27(KICS IR Scenario DB) Start at {}!!!", startTime);
			session.beginTransaction();
			try {
				session.createQuery("delete DcntSce a where a.baseYymm=:param and a.irCurveId like :param2")
						.setParameter("param", bssd)
						.setParameter("param2", "%KICS")
						.executeUpdate();
		
				session.createQuery("delete BizDiscountRateSce a where a.baseYymm=:param and a.applyBizDv =:param2")
						.setParameter("param", bssd)
						.setParameter("param2", "K")
						.executeUpdate();
				
				List<SmithWilsonParam> swParam = DaoUtil.getEntities(SmithWilsonParam.class, new HashMap<String, Object>());
				Map<String, SmithWilsonParam> swParamMap = swParam.stream().collect(Collectors.toMap(s ->s.getCurCd(), Function.identity()));
				
				double ufr  = swParamMap.containsKey("KRW") ? swParamMap.get("KRW").getUfr() : 0.045 ;
				double ufrt = swParamMap.containsKey("KRW") ? swParamMap.get("KRW").getUfrT(): 60  ;
				
				for(IrCurve aa : kicsList) {		
					Path irScePath = Paths.get(output + "IrSce_" + aa.getCurCd()+ "_" + bssd + ".csv");
					Path kicsScePath = Paths.get(output + "KicsSce_" + aa.getCurCd() + "_" + bssd + ".csv");
					Path bizKicsScePath = Paths.get(output + "BizKicsSce_" + aa.getCurCd() + "_" + bssd + ".csv");
		
					logger.info("Read Risk Free Ir Scanrio File for  {} : {}", aa.getCurCd(), irScePath.getFileName());
					logger.info("Result File for KICS IR Scanrio for {} is {}", aa.getCurCd(), kicsScePath);
		
					int sceCnt = 1;
					double volAdj= kicsVolAdjustMap.getOrDefault(aa.getCurCd(), 0.0);
					try (Stream<IrCurveHis> rStream = IrCurveHisMapper.readEntityNoHeader(irScePath)) {
		
						FileUtils.reset(kicsScePath);
						FileUtils.reset(bizKicsScePath);
						
						List<CompletableFuture<List<DcntSce>>> sceJobFutures ;
						
						if(irSceGenSmithWilsonApply.equals("Y")) {
							Map<String, List<IrCurveHis>> sceMap = rStream.filter(s -> s.isBaseTerm()).collect(Collectors.groupingBy(s -> s.getSceNo(), Collectors.toList()));
								sceJobFutures =
									sceMap.entrySet().stream()
									.map(entry -> CompletableFuture.supplyAsync(() ->  
													Job26_KicsTsScenario.createKicsTsSmithWilson(bssd,entry.getKey(), "RF_" + aa.getCurCd() + "_KICS", entry.getValue(), volAdj, ufr, ufrt), exe))
									.collect(Collectors.toList());
						}
						else {
							Map<String, List<IrCurveHis>> sceMap = rStream.collect(Collectors.groupingBy(s -> s.getSceNo(), Collectors.toList()));
							 sceJobFutures =
									sceMap.entrySet().stream()
									.map(entry -> CompletableFuture.supplyAsync(() ->  
													Job26_KicsTsScenario.createKicsTsScenario(bssd,entry.getKey(), "RF_" + aa.getCurCd() + "_KICS", entry.getValue(), volAdj), exe))
									.collect(Collectors.toList());
						}
							
						List<DcntSce> rst = sceJobFutures.stream().map(CompletableFuture::join).flatMap(s ->s.stream()).collect(Collectors.toList());
						rst.sort(new DcntSceComparator());
						logger.info("DcntSce for Kics : {}", rst.size());
							
	//					KICS 할인율 시나리오 파일 Write
						try (Writer writer = new BufferedWriter(new FileWriter(kicsScePath.toFile(), true));
								Writer bizWriter = new BufferedWriter(new FileWriter(bizKicsScePath.toFile(), true));
							) 
						{
							for(DcntSce zz : rst) {
								writer.write(zz.toString());
								bizWriter.write(zz.toStringWithBizDv(",", "K"));
							}
						} catch (Exception e) {
							logger.error("Error in DcntSce result writing : {}", e);
							throw e;
						}
						
	//					할인율 시나리오 DB Write
						sceCnt = 1;
						for (DcntSce bb :rst) {
							session.save(bb);
							if (sceCnt % 50 == 0) {
								session.flush();
								session.clear();
							}
							if (sceCnt % flushSize == 0) {
								logger.info("KICS Discount Scenario for {}  is processed {}/{} in Job 27 {}", aa.getCurCd(),sceCnt, batchNum * 100 * 1200);
							}
							sceCnt = sceCnt + 1;
						}
	
						sceCnt = 1;
						List<BizDiscountRateSce> bizSCe = rst.stream().map(s -> s.convertToBizDcntSce("K")).collect(Collectors.toList()); 
	//					List<BizDiscountRateSce> bizSCe = rst.stream().map(s -> s.convertToBizDcntSce("Z")).collect(Collectors.toList());
//						List<BizDiscountRateSce> bizSCe = rst.stream().map(s -> s.convertToBizDcntSce("X")).collect(Collectors.toList());
						for(BizDiscountRateSce cc : bizSCe) {

							session.save(cc);
							if (sceCnt % 50 == 0) {
								session.flush();
								session.clear();
							}
							if (sceCnt % flushSize == 0) {
								logger.info("Biz KICS Discount Scenario for {}  is processed {}/{} in Job 27 {}", aa.getCurCd(),sceCnt, batchNum * 100 * 1200);
							}
							sceCnt = sceCnt + 1;
						}
						
					} catch (Exception e) {
						logger.info("Error in KICS IR Scenario : {}", e);
						throw e;
					}
				}
				
			} catch (Exception e) {
				logger.error("DB Error : {} ", e);
				logTable.updateFail(e);
			}
			session.saveOrUpdate(logTable);  
			session.getTransaction().commit();
			
			logger.info("Job 27(KICS TS Scenario DB) is Completed !!!");
		}
	}
	
	private static void job27DBOnly() {
		if (jobList.contains("271")) {
			LocalDateTime startTime = LocalDateTime.now();                           
			LogTable logTable = getLogTable("BADA1002JM", startTime,bssd); 

			logger.info("Job 27(KICS IR Scenario DB) Start at {}!!!", startTime);
			session.beginTransaction();
			try {
				session.createQuery("delete DcntSce a where a.baseYymm=:param and a.irCurveId like :param2")
						.setParameter("param", bssd)
						.setParameter("param2", "%KICS")
						.executeUpdate();
		
				session.createQuery("delete BizDiscountRateSce a where a.baseYymm=:param and a.applyBizDv =:param2")
						.setParameter("param", bssd)
						.setParameter("param2", "K")
						.executeUpdate();
				
				List<SmithWilsonParam> swParam = DaoUtil.getEntities(SmithWilsonParam.class, new HashMap<String, Object>());
				Map<String, SmithWilsonParam> swParamMap = swParam.stream().collect(Collectors.toMap(s ->s.getCurCd(), Function.identity()));
				
				double ufr  = swParamMap.containsKey("KRW") ? swParamMap.get("KRW").getUfr() : 0.045 ;
				double ufrt = swParamMap.containsKey("KRW") ? swParamMap.get("KRW").getUfrT(): 60  ;
//				kicsList.forEach(s -> logger.info("kicsList : {}", s.getIrCurveId()));
				
				for(IrCurve aa : rfCurveList) {		
					double volAdj = kicsVolAdjustMap.getOrDefault(aa.getCurCd(), 0.0);
					int sceCnt = 1;
//					logger.info("size : {}", IrCurveHisDao.getIrCurveSce(bssd, aa.getIrCurveId()).count());
					Stream<IrCurveHis> rStream = IrCurveHisDao.getIrCurveSce(bssd, aa.getIrCurveId()).map(s ->s.convertToIrCurveHis());

					List<CompletableFuture<List<DcntSce>>> sceJobFutures ;
					
					if(irSceGenSmithWilsonApply.equals("Y")) {
						Map<String, List<IrCurveHis>> sceMap = rStream.filter(s -> s.isBaseTerm()).collect(Collectors.groupingBy(s -> s.getSceNo(), Collectors.toList()));
							sceJobFutures =
								sceMap.entrySet().stream()
								.map(entry -> CompletableFuture.supplyAsync(() ->  
												Job26_KicsTsScenario.createKicsTsSmithWilson(bssd,entry.getKey(), "RF_" + aa.getCurCd() + "_KICS", entry.getValue(), volAdj, ufr, ufrt), exe))
								.collect(Collectors.toList());
					}
					else {
						Map<String, List<IrCurveHis>> sceMap = rStream.collect(Collectors.groupingBy(s -> s.getSceNo(), Collectors.toList()));
						 sceJobFutures =
								sceMap.entrySet().stream()
								.map(entry -> CompletableFuture.supplyAsync(() ->  
												Job26_KicsTsScenario.createKicsTsScenario(bssd,entry.getKey(), "RF_" + aa.getCurCd() + "_KICS", entry.getValue(), volAdj), exe))
								.collect(Collectors.toList());
					}
						
					List<DcntSce> rst = sceJobFutures.stream().map(CompletableFuture::join).flatMap(s ->s.stream()).collect(Collectors.toList());
//					rst.sort(new DcntSceComparator());
					logger.info("DcntSce for Kics : {}", rst.size());
						
//					할인율 시나리오 DB Write
					sceCnt = 1;
					for (DcntSce bb :rst) {
						session.save(bb);
						if (sceCnt % 50 == 0) {
							session.flush();
							session.clear();
						}
						if (sceCnt % flushSize == 0) {
							logger.info("KICS Discount Scenario for {}  is processed {}/{} in Job 27 {}", aa.getCurCd(),sceCnt, rst.size());
						}
						sceCnt = sceCnt + 1;
					}

					sceCnt = 1;
					List<BizDiscountRateSce> bizSCe = rst.stream().map(s -> s.convertToBizDcntSce("K")).collect(Collectors.toList()); 
					for(BizDiscountRateSce cc : bizSCe) {
						session.save(cc);
						if (sceCnt % 50 == 0) {
							session.flush();
							session.clear();
						}
						if (sceCnt % flushSize == 0) {
							logger.info("Biz KICS Discount Scenario for {}  is processed {}/{} in Job 27 {}", aa.getCurCd(),sceCnt, rst.size());
						}
						sceCnt = sceCnt + 1;
					}
				}
				
			} catch (Exception e) {
				logger.error("DB Error : {} ", e);
				logTable.updateFail(e);
			}
			session.saveOrUpdate(logTable);  
			session.getTransaction().commit();
			
			logger.info("Job 27(KICS TS Scenario DB) is Completed !!!");
		}
	}
	
//	20210420  수정
	private static void job27New() {
		if (jobList.contains("27")) {
			LocalDateTime startTime = LocalDateTime.now();                            
			LogTable logTable = getLogTable("BADA1002JM", startTime,bssd);  

			logger.info("Job 27(Bottom Up Scenario DB Only ) Start at {} !!!", startTime);
			session.beginTransaction();
			try {
				session.createQuery("delete DcntSce a where a.baseYymm=:param and a.irCurveId like :param2")
						.setParameter("param", bssd)
						.setParameter("param2", "%KICS")
						.executeUpdate();
				
				session.createQuery("delete BizDiscountRateSce a where a.baseYymm=:param and a.applyBizDv =:param2")
						.setParameter("param", bssd)
						.setParameter("param2", "K")
						.executeUpdate();
				
	//			SmithWilson parameter 호출 및 통화별 매개변수 정리
				String bizDv ="K";
				Stream<SmithWilsonParam> swStream        = DaoUtil.getEntityStream(SmithWilsonParam.class, new HashMap<>());
				Map<String, SmithWilsonParam> swParamMap = swStream.collect(Collectors.toMap(s->s.getCurCd(), Function.identity()));
				
				
				for(IrCurve aa : kicsList) {	
					logger.info("Ir Scenario for {} : {}", aa.getCurCd(), aa.getIrCurveNm());
					
	//				사전 프로세스 - 데이터 준비
					List<EsgMst> esgMstList = EsgMstDao.getEsgMst(EBoolean.Y);
					EsgMst tempEsgMst       = esgMstList.get(0);

//					double volAdj = kicsVolAdjustMap.getOrDefault(aa.getCurCd(), 0.0);
					
					List<BizEsgParam> bizParamHis = EsgParamDao.getBizEsgParam(bssd, bizDv, tempEsgMst.getIrModelId());
					
					List<BizDiscountRate> bottomUpList = BizDcntRateDao.getTermStructure(bssd, aa.getIrCurveId());
					
					List<IrCurveHis> irCurveHisList = bottomUpList.stream().map(s-> s.convert()).collect(Collectors.toList());
					
					List<IrCurveHis> irCurveHisBaseList = bottomUpList.stream()
																	.filter(s->irCurveTenor.contains(s.getMatCd()))
																	.map(s-> s.convert()).collect(Collectors.toList());
					
					
					double ufr  =  swParamMap.get(aa.getCurCd()).getUfr();
					double ufrt =  swParamMap.get(aa.getCurCd()).getUfrT();
					
//					double shortRate = FinUtils.spanFullBucket(bssd, irCurveHisBaseList).stream().filter(s ->s.getMatCd().equals("M0001")).map(s -> s.getIntRate()).findFirst().orElse(0.01);
//					BIZ APPLY DCNT  : M0001~M1200 
					double shortRate = irCurveHisList.stream().filter(s ->s.getMatCd().equals("M0001")).map(s -> s.getIntRate()).findFirst().orElse(0.01);
					String irCurveId = irCurveHisList.get(0).getIrCurveId();
					
					
	//				사전 프로세스 끝
					
	//				batchNum =2;
	//				통화별로 산출 결과를 생성하기 위한 병렬처리 
					List<CompletableFuture<List<DcntSce>>> 	sceJobFutures =	
							IntStream.rangeClosed(1,batchNum)
									.mapToObj(sceNo-> CompletableFuture.supplyAsync(() 
														->  Job15_EsgScenarioAsync.createDcntScenarioAsync(bssd, irCurveId, irCurveHisBaseList, bizParamHis, tempEsgMst, ufr, ufrt, shortRate, sceNo), exe))
									.collect(Collectors.toList());
						
	//				병렬처리 결과 집계
					List<DcntSce> rst = sceJobFutures.stream().map(CompletableFuture::join)
															.flatMap(s ->s.stream())
															.collect(Collectors.toList());
					
					int sceCnt = 1;
	//				금리 시나리오 결과 DB Write
					
					for (DcntSce bb : rst) {
//						logger.info("zzz : {}", bb.toString("#"));
						session.save(bb);
						if (sceCnt % 50 == 0) {
							session.flush();
							session.clear();
							
						}
						if (sceCnt % flushSize == 0) {
							logger.info("Discount Scenario for {}  is processed {}/{} in Job 24 {}", aa.getCurCd(),sceCnt, batchNum * 100 * 1200);
						}
						sceCnt = sceCnt + 1;
					}
					
					sceCnt = 1;
					String currSceNo="1";

					List<BizDiscountRateSce> bizSce = new ArrayList<BizDiscountRateSce>();
					
					irSceGenSmithWilsonApply ="N";
					if(irSceGenSmithWilsonApply.equals("Y")) {
						
						List<CompletableFuture<List<DcntSce>>> 	anotherFutures =	
								IntStream.rangeClosed(1,batchNum)
										.mapToObj(sceNo-> CompletableFuture.supplyAsync(() 
															->  Job15_EsgScenarioAsync.createDcntScenarioAsync(bssd, irCurveId, irCurveHisBaseList, bizParamHis, tempEsgMst, ufr, ufrt, shortRate, sceNo), exe))
										.collect(Collectors.toList());
						
						List<DcntSce> anotherRst = anotherFutures.stream().map(CompletableFuture::join)
															.flatMap(s ->s.stream())
															.collect(Collectors.toList());
						
						bizSce = anotherRst.stream().map(s -> s.convertToBizDcntSce(bizDv)).collect(Collectors.toList());
					}	
					else if(irSceGenSmithWilsonApply.equals("YY")) {
						Map<String, List<DcntSce>> rstMap = rst.stream().filter(s->irCurveTenor.contains(s.getMatCd()))
																.collect(Collectors.groupingBy(DcntSce::getSceNo, Collectors.toList()));
						for(int i=1; i<=batchNum * 100; i++) {
//						for(int i=1; i<=10; i++) {
							currSceNo = String.valueOf(i);
							logger.info("aaaa : {},{}", currSceNo, rstMap.get(currSceNo).size());
							SmithWilsonModel sw = new SmithWilsonModel(bssd, aa.getIrCurveId(), currSceNo, rstMap.get(currSceNo));
							bizSce.addAll(sw.convertToIrCurveSce(true, bizDv));
						}
					}
					else {
						bizSce = rst.stream().map(s -> s.convertToBizDcntSce(bizDv)).collect(Collectors.toList()); 
					}
					
					for(BizDiscountRateSce cc : bizSce) {
						session.save(cc);
						if (sceCnt % 50 == 0) {
							session.flush();
							session.clear();
						}
						if (sceCnt % flushSize == 0) {
							logger.info("Biz Discount Scenario for {}  is processed {}/{} in Job 24 {}", aa.getCurCd(),sceCnt, batchNum * 100 * 1200);
						}
						sceCnt = sceCnt + 1;
					}
				}
			} catch (Exception e) {
				logTable.updateFail(e);
				logger.info("Erro : {}", e);
			}
			session.saveOrUpdate(logTable);
			session.getTransaction().commit();
			logger.info("Job 27(Hull and White Scenario DB) is Completed !!!");
		}
	}

	
	private static void job28() {
		if (jobList.contains("28")) {
			LocalDateTime startTime = LocalDateTime.now();                            
			LogTable logTable = getLogTable("BADA1002JM", startTime,bssd);  

			logger.info("Job 28(TopDown ) Start !!!");
			
//			String bfBssd = FinUtils.addMonth(bssd, -1);
			String bfBssd = bssd;
			
//			자산 CF 추출
			session.beginTransaction();
			try {
				session.createQuery("delete AssetCf a where a.baseYymm=:param").setParameter("param", bfBssd).executeUpdate();
				Job28_TopDownCashFlow.createAssetCashFlow(bfBssd).forEach(s -> session.save(s));
				session.getTransaction().commit();
				
//			부채 CF 추출
				session.beginTransaction();
				session.createQuery("delete LiabCf a where a.baseYymm=:param").setParameter("param", bfBssd).executeUpdate();
				Job28_TopDownCashFlow.createLiabilityCashFlow(bfBssd).forEach(s -> session.save(s));
				
			} catch (Exception e) {
				// TODO: handle exception                                        
				logTable.updateFail(e);   
			}
			session.saveOrUpdate(logTable);         
			session.getTransaction().commit();

			logger.info("Job 28(TopDownCashFlow) is Completed !!!");
		}
	}
	private static void job291() {
		if (jobList.contains("291")) {
//			String bfBssd = FinUtils.addMonth(bssd, -1);
			LocalDateTime startTime = LocalDateTime.now();                            
			LogTable logTable = getLogTable("BADA1002JM", startTime,bssd);  
			String bfBssd = bssd;
			
//			현금흐름 매칭 조정
			session.beginTransaction();
			try {
				session.createQuery("delete CashFlowMatchAdj a where a.baseYymm=:param").setParameter("param", bfBssd).executeUpdate();
				Job29_TopDown.calcCashFlowMatchRatio(bfBssd).forEach(s -> session.save(s));
			} catch (Exception e) {
				// TODO: handle exception
				logTable.updateFail(e);   
			}
			session.saveOrUpdate(logTable);
			session.getTransaction().commit();
		}
	}
	private static void job292() {
		if (jobList.contains("292")) {
//			String bfBssd = FinUtils.addMonth(bssd, -1);
			String bfBssd = bssd;
//			신용위험 조정
			session.beginTransaction();
			Job29_TopDown.updateCreditAdjustment(bfBssd).forEach(s -> session.saveOrUpdate(s)); 
			session.getTransaction().commit();
		}
	}	
	private static void job293() {
		if (jobList.contains("293")) {
//			String bfBssd = FinUtils.addMonth(bssd, -1);
			String bfBssd = bssd;
//			자산군 수익률 산출
			session.beginTransaction();
			session.createQuery("delete AssetClassYield a where a.baseYymm=:param").setParameter("param", bssd).executeUpdate();
			Job29_TopDown.createAssetClassYield(bssd).forEach(s -> session.save(s));
			session.getTransaction().commit();
		}
	}	
	private static void job294() {
		if (jobList.contains("294")) {
//			String bfBssd = FinUtils.addMonth(bssd, -1);
			String bfBssd = bssd;
			
//			포트폴리오 수익률 산출 
			session.beginTransaction();
			session.createQuery("delete RefPortYield a where a.baseYymm=:param").setParameter("param", bssd).executeUpdate();
			Job29_TopDown.createRefPortfolioYield(bssd).forEach(s -> session.save(s));
			session.getTransaction().commit();
			 
//			TopDown 할인율 산출
			session.beginTransaction();
			session.createQuery("delete TopDownDcnt a where a.baseYymm=:param").setParameter("param", bssd).executeUpdate();
			Job29_TopDown.createTopDown(bssd).forEach(s -> session.save(s));
			session.getTransaction().commit();
		}
	}	
	private static void job29() {
		if (jobList.contains("29")) {
			LocalDateTime startTime = LocalDateTime.now();                            
			LogTable logTable = getLogTable("BADA1002JM", startTime,bssd);  
			logger.info("Job 29(TopDown ) Start !!!");
			
//			현금흐름 매칭 조정
			session.beginTransaction();
			try {
				session.createQuery("delete CashFlowMatchAdj a where a.baseYymm=:param").setParameter("param", bssd).executeUpdate();
				Job29_TopDown.calcCashFlowMatchRatio(bssd).forEach(s -> session.save(s));
				session.getTransaction().commit();
				
//			신용위험 조정
				session.beginTransaction();
				Job29_TopDown.updateCreditAdjustment(bssd).forEach(s -> session.saveOrUpdate(s)); 
				session.getTransaction().commit();
				
//			 자산군 수익률 산출
				session.beginTransaction();
				session.createQuery("delete AssetClassYield a where a.baseYymm=:param").setParameter("param", bssd).executeUpdate();
				Job29_TopDown.createAssetClassYield(bssd).forEach(s -> session.save(s));
				session.getTransaction().commit();
				
//			포트폴리오 수익률 산출 
				session.beginTransaction();
				session.createQuery("delete RefPortYield a where a.baseYymm=:param").setParameter("param", bssd).executeUpdate();
				Job29_TopDown.createRefPortfolioYield(bssd).forEach(s -> session.save(s));
				session.getTransaction().commit();
				
//			TopDown 할인율 산출
				session.beginTransaction();
				session.createQuery("delete TopDownDcnt a where a.baseYymm=:param").setParameter("param", bssd).executeUpdate();
				Job29_TopDown.createTopDown(bssd).forEach(s -> session.save(s));
				
			} catch (Exception e) {
				// TODO: handle exception                                        
				logTable.updateFail(e);   

			}
			session.saveOrUpdate(logTable);         
			session.getTransaction().commit();
			
			logger.info("Job 29(TopDown) is Completed !!!");
		}
	}
	
	private static void job31() {
		if (jobList.contains("31")) {
			LocalDateTime startTime = LocalDateTime.now();                                     
			LogTable logTable = getLogTable("BADA1004JM", startTime,bssd);           

			logger.info("Job 31(DiscRate Stat Analysis) Start !!!");
			session.beginTransaction();
			try {
				session.createQuery("delete DiscRateStats a where a.applStYymm=:param and a.discRateCalcTyp=:param2")
								.setParameter("param", bssd).setParameter("param2", "I").executeUpdate();
//				Job31_DiscRateStatInternal.getBaseDiscRateStat(bssd).forEach(s->session.save(s));
				for(DiscRateStats aa : Job31_DiscRateStatInternal.getBaseDiscRateStat(bssd)) {
					try {
						session.save(aa);
					} catch (Exception e) {
						logger.error("error :  {}", aa);
						// TODO: handle exception
					}
				}
				
			} catch (Exception e) {
				logTable.updateFail(e);      
				logger.error("Erro : {}", e);
			}
//			session.saveOrUpdate(logTable);            
			session.getTransaction().commit();
//			try {
//				session.saveOrUpdate(logTable);            
//			} catch (Exception e) {
//				logger.error("LogTable Error : {}", e);
//			}
			logger.info("Job 31(DiscRate Stat Analysis) is Completed !!!");
		}	
		
	}
	
	private static void job32() {
		if (jobList.contains("32")) {
			LocalDateTime startTime = LocalDateTime.now();                                     
			LogTable logTable = getLogTable("BADA1004JM", startTime,bssd);     
			logger.info("Job 32(Biz Applied DiscRate Stats) Start !!! ");
			session.beginTransaction();
			try {
				int  maxSeq = DiscRateStatsDao.getMaxSeq(bssd, "I");
				session.createQuery("delete BizDiscRateStat a where a.baseYymm=:param and a.applyBizDv=:param2")
					.setParameter("param", bssd).setParameter("param2", "I").executeUpdate();
				
				session.createQuery("delete HisDiscRateStat a where a.baseYymm=:param and a.applyBizDv=:param2 and a.seq = :seq")
					.setParameter("param", bssd).setParameter("param2", "I").setParameter("seq", 0).executeUpdate();
				
				List<BizDiscRateStat> bizRst = Job32_BizDiscRateStatInternal.getBaseDiscRateStat(bssd);
				
				
				bizRst.stream().forEach(s->session.save(s));
				
				bizRst.stream().map( s-> s.convertToHisDiscRateStst(maxSeq+1)).forEach(s->session.save(s));
				bizRst.stream().map( s-> s.convertToHisDiscRateStst(0)).forEach(s->session.save(s));
				
				logger.info("His Disc Rate Stat Seq : {},{}", maxSeq, bizRst.size());
				
			} catch (Exception e) {
				logTable.updateFail(e);     
				logger.info("Erro : {}", e);
			}

			session.saveOrUpdate(logTable);  
			session.getTransaction().commit();
//			try {
//				session.saveOrUpdate(logTable);            
//			} catch (Exception e) {
//				logger.error("LogTable Error : {}", e);
//			}
			logger.info("Job 32(Applied DiscRate Stats ) is completed !!! ");
		}
	}
	
	
	private static void job33() {
		if (jobList.contains("33")) {
			LocalDateTime startTime = LocalDateTime.now();                                     
			LogTable logTable = getLogTable("BADA1004JM", startTime,bssd); 
			logger.info("Job 33(Biz Applied DiscRate Stat for Kics) Start !!! ");
			session.beginTransaction();
			try {
				int  maxSeq = DiscRateStatsDao.getMaxSeq(bssd, "K");
				session.createQuery("delete DiscRateStats a where a.applStYymm=:param and a.discRateCalcTyp=:param2")
				.setParameter("param", bssd).setParameter("param2", "K").executeUpdate();
				
				session.createQuery("delete BizDiscRateStat a where a.baseYymm=:param and a.applyBizDv=:param2")
				.setParameter("param", bssd).setParameter("param2", "K").executeUpdate();
				
				session.createQuery("delete HisDiscRateStat a where a.baseYymm=:param and a.applyBizDv=:param2 and a.seq = :seq")
				.setParameter("param", bssd).setParameter("param2", "K").setParameter("seq", 0).executeUpdate();
				
				// List<BizDiscRateStat> rst =  Job33_BizDiscRateStatKics.getBaseDiscRateStat(bssd);
				// 20200529 :Add
				List<BizDiscRateStat> rst =  Job33_BizDiscRateStatKics.getBaseDiscRateStat1(bssd);
				
//				rst.forEach(s->logger.info("zzz : {}", s.toString()));
				rst.forEach(s->session.save(s));
				
				
				rst.stream().map(s->s.convertToDiscRateStst()).forEach(s->session.save(s));
				rst.stream().map(s->s.convertToHisDiscRateStst(0)).forEach(s->session.save(s));
				rst.stream().map(s->s.convertToHisDiscRateStst(maxSeq +1)).forEach(s->session.save(s));
								
			} catch (Exception e) {
				logTable.updateFail(e);      
				logger.info("Erro : {}", e);
			}
			session.saveOrUpdate(logTable);            
			session.getTransaction().commit();
			logger.info("Job 33(Biz Applied DiscRate Stats for Kics) is completed !!! ");
		}
	}
	
	private static void job34() {
		if (jobList.contains("34")) {
			LocalDateTime startTime = LocalDateTime.now();                                     
			LogTable logTable = getLogTable("BADA1004JM", startTime,bssd); 
			logger.info("Job 34(Biz Applied DiscRate Stat for Spread Model) Start !!! ");
			session.beginTransaction();
			try {
				int  maxSeq = DiscRateStatsDao.getMaxSeq(bssd, "S");
				
				session.createQuery("delete DiscRateStats a where a.applStYymm=:param and a.discRateCalcTyp=:param2")
				.setParameter("param", bssd).setParameter("param2", "S").executeUpdate();
				
				session.createQuery("delete BizDiscRateStat a where a.baseYymm=:param and a.applyBizDv=:param2")
				.setParameter("param", bssd).setParameter("param2", "S").executeUpdate();
				
				session.createQuery("delete HisDiscRateStat a where a.baseYymm=:param and a.applyBizDv=:param2 and a.seq = :seq")
				.setParameter("param", bssd).setParameter("param2", "S").setParameter("seq", 0).executeUpdate();
				
				List<BizDiscRateStat> rst =  Job34_BizDiscRateStatSpread.getBaseDiscRateStat(bssd);
				
				rst.forEach(s->session.save(s));
				
				rst.stream().map(s->s.convertToDiscRateStst()).forEach(s->session.save(s));
				
				rst.stream().map(s->s.convertToHisDiscRateStst(0)).forEach(s->session.save(s));
				rst.stream().map(s->s.convertToHisDiscRateStst(maxSeq +1)).forEach(s->session.save(s));
//				rst.stream().map(s->s.convertToHisDiscRateStst(maxSeq +1)).forEach(s->logger.info("zdfasdf : {}", s.toString()));
				
			} catch (Exception e) {
				logTable.updateFail(e);      
				logger.info("Erro : {}", e);
			}

			session.saveOrUpdate(logTable);            
			session.getTransaction().commit();
//			try {
//				session.saveOrUpdate(logTable);            
//			} catch (Exception e) {
//				logger.error("LogTable Error : {}", e);
//			}
			logger.info("Job 34(Biz Applied DiscRate Stats for Kics) is completed !!! ");
		}

	}

	private static void job35() {
		if (jobList.contains("35")) {
			forwardRateSetupForDiscRate(bssd, "I", "RF_KRW_BU");
			
			LocalDateTime startTime = LocalDateTime.now();                                     
			LogTable logTable = getLogTable("BADA1004JM", startTime,bssd); 
			logger.info("Job 35(DiscRate with IFRS Method) Start !!! ");
			session.beginTransaction();
			try {
				session.createQuery("delete DiscRate a where a.baseYymm=:param and a.discRateCalcTyp =:param2"  )
				.setParameter("param", bssd)
				.setParameter("param2", "I")
				.executeUpdate();
				
				session.createQuery("delete HisDiscRate a where a.baseYymm=:param and a.applyBizDv=:param2 and a.seq =:seq")
				.setParameter("param", bssd)
				.setParameter("param2", "I")
				.setParameter("seq", 0)
				.executeUpdate();
				
				List<DiscRate> discRateList = Job35_DiscRateIfrsAsync.getDiscRateAsync_Alt(bssd, exe);
				cnt = 1;
				totalSize = discRateList.size();
				
				for (DiscRate aa : discRateList) {
					session.save(aa);
					if (cnt % 50 == 0) {
						session.flush();
						session.clear();
					}
					if (cnt % flushSize == 0) {
						logger.info("Disc Rate IFRS are Flushed :  {} / {}", cnt, totalSize);
					}
					cnt = cnt + 1;
				}
				
				List<HisDiscRate> hisDiscRateList = discRateList.stream().map(s->s.convertToHisDiscRate(0)).collect(Collectors.toList());
				cnt = 1;
				totalSize = hisDiscRateList.size();
				for (HisDiscRate aa : hisDiscRateList) {
					session.save(aa);
					if (cnt % 50 == 0) {
						session.flush();
						session.clear();
					}
					if (cnt % flushSize == 0) {
						logger.info("His Disc Rate at zeroseq IFRS are Flushed :  {} / {}", cnt, totalSize);
					}
					cnt = cnt + 1;
				}
				
				
				int  maxSeq = DiscRateDao.getMaxSeq(bssd, "I");
				logger.info("Max SEq : {}", maxSeq);
				List<HisDiscRate> hisDiscRateList1 = discRateList.stream().map(s->s.convertToHisDiscRate(maxSeq+1)).collect(Collectors.toList());
				cnt = 1;
				totalSize = hisDiscRateList1.size();
				for (HisDiscRate aa : hisDiscRateList1) {
					session.save(aa);
					if (cnt % 50 == 0) {
						session.flush();
						session.clear();
					}
					if (cnt % flushSize == 0) {
						logger.info("His Biz Disc Rate at maxseq IFRS are Flushed :  {} / {}", cnt, totalSize);
					}
					cnt = cnt + 1;
				}
				
			} catch (Exception e) {
				logTable.updateFail(e);      
				logger.info("Error : {}", e);
			}
			session.saveOrUpdate(logTable);            
			session.getTransaction().commit();
//			try {
//				session.saveOrUpdate(logTable);            
//			} catch (Exception e) {
//				logger.error("LogTable Error : {}", e);
//			}
			logger.info("Job 35(DiscRate with IFRS Method ) is completed !!! ");

		}
	}

	
	private static void job36() {
		if (jobList.contains("36")) {
			forwardRateSetupForDiscRate(bssd, "K", "RF_KRW_KICS");
			
			LocalDateTime startTime = LocalDateTime.now();                                     
			LogTable logTable = getLogTable("BADA1004JM", startTime, bssd); 
			logger.info("Job 36(DiscRate with KICS Method) Start !!! ");
			
			session.beginTransaction();
			try {
				
				session.createQuery("delete DiscRate a where a.baseYymm=:param and a.discRateCalcTyp =:param2")
				.setParameter("param", bssd)
				.setParameter("param2", "K")
				.executeUpdate();
				
				session.createQuery("delete HisDiscRate a where a.baseYymm=:param and a.applyBizDv=:param2 and a.seq =:seq")
				.setParameter("param", bssd)
				.setParameter("param2", "K")
				.setParameter("seq", 0)
				.executeUpdate();
				
				List<DiscRate> discRateList = Job36_DiscRateKicsAsync.getDiscRateAsync_Alt(bssd, exe);
				cnt = 1;
				totalSize = discRateList.size();
				
				for (DiscRate aa : discRateList) {
					session.save(aa);
					if (cnt % 50 == 0) {
						session.flush();
						session.clear();
					}
					if (cnt % flushSize == 0) {
						logger.info("Disc Rate KICS are Flushed :  {} / {}", cnt, totalSize);
					}
					cnt = cnt + 1;
				}
				
				List<HisDiscRate> hisDiscRateList = discRateList.stream().map(s->s.convertToHisDiscRate(0)).collect(Collectors.toList());
				cnt = 1;
				totalSize = hisDiscRateList.size();
				for (HisDiscRate aa : hisDiscRateList) {
					session.save(aa);
					if (cnt % 50 == 0) {
						session.flush();
						session.clear();
					}
					if (cnt % flushSize == 0) {
						logger.info("His Disc Rate at zeroseq IFRS are Flushed :  {} / {}", cnt, totalSize);
					}
					cnt = cnt + 1;
				}
				
				
				int  maxSeq = DiscRateDao.getMaxSeq(bssd, "K");
				List<HisDiscRate> hisDiscRateList1 = discRateList.stream().map(s->s.convertToHisDiscRate(maxSeq+1)).collect(Collectors.toList());
				cnt = 1;
				totalSize = hisDiscRateList1.size();
				for (HisDiscRate aa : hisDiscRateList1) {
					session.save(aa);
					if (cnt % 50 == 0) {
						session.flush();
						session.clear();
					}
					if (cnt % flushSize == 0) {
						logger.info("His Biz Disc Rate at maxseq IFRS are Flushed :  {} / {}", cnt, totalSize);
					}
					cnt = cnt + 1;
				}
				
			} catch (Exception e) {
				logTable.updateFail(e);   
				logger.info("Error : {}", e);
			}
			session.saveOrUpdate(logTable);        
			session.getTransaction().commit();
//			try {
//				session.saveOrUpdate(logTable);            
//			} catch (Exception e) {
//				logger.error("LogTable Error : {}", e);
//			}
			logger.info("Job 36(DiscRate with KICS Method ) is completed !!! ");

		}
	}

	private static void job37() {
		if (jobList.contains("37")) {
			forwardRateSetupForDiscRate(bssd, "S", "RF_KRW_KICS");
			
			LocalDateTime startTime = LocalDateTime.now();                                     
			LogTable logTable = getLogTable("BADA1004JM", startTime,bssd); 
			logger.info("Job 37(DiscRate with Spread Method) Start !!! ");
			
			session.beginTransaction();
			try {
				int  maxSeq = DiscRateDao.getMaxSeq(bssd, "S");
				
				session.createQuery("delete DiscRate a where a.baseYymm=:param and a.discRateCalcTyp =:param2")
				.setParameter("param", bssd)
				.setParameter("param2", "S")
				.executeUpdate();
				
				session.createQuery("delete HisDiscRate a where a.baseYymm=:param and a.applyBizDv=:param2 and a.seq =:seq")
				.setParameter("param", bssd)
				.setParameter("param2", "S")
				.setParameter("seq", 0)
				.executeUpdate();
				
				List<DiscRate> discRateList = Job37_DiscRateSpreadAsync.getDiscRateAsync_Alt(bssd, exe);
				cnt = 1;
				totalSize = discRateList.size();
				
				for (DiscRate aa : discRateList) {
					session.save(aa);
					if (cnt % 50 == 0) {
						session.flush();
						session.clear();
					}
					if (cnt % flushSize == 0) {
						logger.info("Disc Rate for Spread are Flushed :  {} / {}", cnt, totalSize);
					}
					cnt = cnt + 1;
				}
				
				
				List<HisDiscRate> hisDiscRateList = discRateList.stream().map(s->s.convertToHisDiscRate(0)).collect(Collectors.toList());
				cnt = 1;
				totalSize = hisDiscRateList.size();
				for (HisDiscRate aa : hisDiscRateList) {
					session.save(aa);
					if (cnt % 50 == 0) {
						session.flush();
						session.clear();
					}
					if (cnt % flushSize == 0) {
						logger.info("His Disc Rate at zeroseq IFRS are Flushed :  {} / {}", cnt, totalSize);
					}
					cnt = cnt + 1;
				}
				

				List<HisDiscRate> hisDiscRateList1 = discRateList.stream().map(s->s.convertToHisDiscRate(maxSeq+1)).collect(Collectors.toList());
				cnt = 1;
				totalSize = hisDiscRateList1.size();
				for (HisDiscRate aa : hisDiscRateList1) {
					session.save(aa);
					if (cnt % 50 == 0) {
						session.flush();
						session.clear();
					}
					if (cnt % flushSize == 0) {
						logger.info("His Biz Disc Rate at maxseq IFRS are Flushed :  {} / {}", cnt, totalSize);
					}
					cnt = cnt + 1;
				}
				
			} catch (Exception e) {
				logTable.updateFail(e);  
				logger.info("Error : {}", e);
			}
			
			session.saveOrUpdate(logTable);   
			session.getTransaction().commit();
//			try {
//				session.saveOrUpdate(logTable);            
//			} catch (Exception e) {
//				logger.error("LogTable Error : {}", e);
//			}
			logger.info("Job 37(DiscRate with Spread Method ) is completed !!! ");

		}
	}
	private static void job38() {
		// Job 38 : 공시이율 시나리오 생성 : 금리 시나리오 결과를 이용하여 각 시나리오를 금리 커브로 간주하여 공시이율 추정치를 산출하는 로직을 반복 수행하여 공시이율 시나리오를 산출함.
		
		if (jobList.contains("38")) {
			logger.info("Job 38(DiscRate Scenario)  Start!!!  It takes about {} minutes", batchNum * 15 );
			Path irScePath = Paths.get(output + "IrSce_KRW_" + bssd + ".csv");
			
			Job38_DiscRateScenario.writeDiscRateScenario(bssd, batchNum, irScePath, output);
			
			logger.info("Job 38(DiscRate Scenario)  is completed !!! ");
		}
	}
	
	private static void job39Async() {
		// Job 38 : 공시이율 시나리오 생성 : 금리 시나리오 결과를 이용하여 각 시나리오를 금리 커브로 간주하여 공시이율 추정치를 산출하는 로직을 반복 수행하여 공시이율 시나리오를 산출함.
		
		if (jobList.contains("39")) {
			LocalDateTime startTime = LocalDateTime.now();                                     
			LogTable logTable = getLogTable("BADA1004JM", startTime,bssd); 
			logger.info("Job 39(DiscRate Scenario for DB)  Start!!!  It takes about {} minutes", batchNum * 15 );
			session.beginTransaction();
			try {
				session.createQuery("delete DiscRateSce a where a.baseYymm=:param").setParameter("param", bssd).executeUpdate();
				
				List<DiscRateSce> rst = new ArrayList<DiscRateSce>();
				
				String isRiskFreeString    = ParamUtil.getParamMap().getOrDefault("discIntenalDriverIsRiskFree", "N");
				boolean	isRiskFree = isRiskFreeString.equals("Y");
				String discRateSceBizDv = ParamUtil.getParamMap().getOrDefault("discRateSceBizDv", "I");
				
				Map<String, Double> pastCurvetMap = new HashMap<String,  Double>();
				Map<String, Double> matCdRateMap = new HashMap<String,  Double>();
				
				for(EBaseMatCd aa : EBaseMatCd.values()) {
//				만기별 과거 금리를 기준년월로 정렬
					for(int k= -36; k < 1; k++) {
						String prevBssd = FinUtils.addMonth(bssd, k);
						matCdRateMap = getDriverCurveMap(prevBssd, isRiskFree);
						pastCurvetMap.put(prevBssd, matCdRateMap.getOrDefault(aa.name(), new Double(0.0)));
					}
				}
				logger.info("Past Curve History for DiscRate Estimation : {}", pastCurvetMap.size() );
				cnt =0;
				for(int i=0 ; i< batchNum*100 ; i++) {
//			for(int i=0 ; i< 10 ; i++) {	
					rst = Job39_DiscRateScenarioAsync.getDiscRateScenarioAsync(bssd, discRateSceBizDv, String.valueOf(i+1), pastCurvetMap, exe);
//				rst = Job38_DiscRateScenarioAsync.getDiscRateScenarioAsync(bssd, String.valueOf(i+1), exe);
					for (DiscRateSce aa : rst) {
						session.save(aa);
						if (cnt % 50 == 0) {
							session.flush();
							session.clear();
						}
					}
					logger.info("Disc Rate Scenario for batchNum are Flushed :  {} / {}", i+1 , batchNum *100);
				}
			} catch (Exception e) {
				logTable.updateFail(e);      
			}
			
			session.saveOrUpdate(logTable);   
			session.getTransaction().commit();
			logger.info("Job 39(DiscRate Scenario)  is completed !!! ");
		}
	}
	
	
	private static void job39Async_All() {
		if (jobList.contains("39")) {
			LocalDateTime startTime = LocalDateTime.now();                                     
			LogTable logTable = getLogTable("BADA1004JM", startTime,bssd); 
			logger.info("Job 39(DiscRate Scenario for DB)  Start!!!  It takes about {} minutes", batchNum * 15 );
			session.beginTransaction();
			
			List<DiscRateSce> rst = new ArrayList<DiscRateSce>();
			try {
				session.createQuery("delete DiscRateSce a where a.baseYymm=:param").setParameter("param", bssd).executeUpdate();
				
				cnt =0;
//				for(int i=0 ; i< batchNum*100 ; i++) {
				for(int i=0 ; i< 10 ; i++) {	
					rst = calcDiscRateSce("I", String.valueOf(i));
					rst.addAll(calcDiscRateSce("S", String.valueOf(i)));
					rst.addAll(calcDiscRateSce("K", String.valueOf(i)));

					for (DiscRateSce aa : rst) {
						session.save(aa);
						if (cnt % 50 == 0) {
							session.flush();
							session.clear();
						}
					}
					logger.info("Disc Rate Scenario for batchNum are Flushed :  {} / {}", i+1 , batchNum *100);
				}
			} catch (Exception e) {
				logTable.updateFail(e);      
				logger.info("Error : {}", e);
			}
			
			session.saveOrUpdate(logTable);   
			session.getTransaction().commit();
			logger.info("Job 39(DiscRate Scenario)  is completed !!! ");
		}
	}
	
	private static void job39_IFRS() {
		if (jobList.contains("39")) {
			forwardRateSetupForDiscRate(bssd, "I", "RF_KRW_BU");
			
			LocalDateTime startTime = LocalDateTime.now();                                     
			LogTable logTable = getLogTable("BADA1004JM", startTime,bssd); 
			logger.info("Job 35(DiscRateSce with IFRS Method) Start !!! ");
			session.beginTransaction();
			try {
				session.createQuery("delete DiscRateSce a where a.baseYymm=:param and a.discRateCalcTyp =:param2")
						.setParameter("param", bssd)
						.setParameter("param2", "I")
						.executeUpdate();
				
				List<DiscRateSce> rst = new ArrayList<DiscRateSce>();
				
				for(int i=0 ; i<= batchNum*100 ; i++) {
					cnt =1;
					rst = Job35_DiscRateIfrsAsync.getDiscRateAsync_Alt(bssd, String.valueOf(i), exe);
					for (DiscRateSce aa : rst) {
						session.save(aa);
						if (cnt % 50 == 0) {
							session.flush();
							session.clear();
						}
						if (cnt % flushSize == 0) {
							logger.info("Biz Disc Rate IFRS are Flushed :  {} / {}", cnt++, rst.size());
						}
						cnt++;
					}
					logger.info("Disc Rate Scenario for batchNum are Flushed :  {} / {}", i, batchNum*100);
				}
				
			} catch (Exception e) {
				logTable.updateFail(e);     
				logger.info("Error : {}", e);
			}
			session.saveOrUpdate(logTable);            
			session.getTransaction().commit();
			logger.info("Job 39(DiscRateSce with IFRS Method ) is completed !!! ");
	
		}
	}

	private static void job39_KICS() {
		if (jobList.contains("39")) {
			forwardRateSetupForDiscRate(bssd, "K", "RF_KRW_KICS");
	
			LocalDateTime startTime = LocalDateTime.now();                                     
			LogTable logTable = getLogTable("BADA1004JM", startTime,bssd); 
			logger.info("Job 39(DiscRateSce with KICS Method) Start !!! ");
			
			session.beginTransaction();
			try {
				session.createQuery("delete DiscRateSce a where a.baseYymm=:param and a.discRateCalcTyp =:param2")
						.setParameter("param", bssd)
						.setParameter("param2", "K")
						.executeUpdate();
				
				List<DiscRateSce> rst = new ArrayList<DiscRateSce>();
				
				for(int i=0 ; i<= batchNum*100 ; i++) {
					cnt =1;
					rst = Job36_DiscRateKicsAsync.getDiscRateAsync_Alt(bssd, String.valueOf(i), exe);
					for (DiscRateSce aa : rst) {
						session.save(aa);
						if (cnt % 50 == 0) {
							session.flush();
							session.clear();
						}
						if (cnt % flushSize == 0) {
							logger.info("Disc Rate Scenario are Flushed :  {} / {}", cnt++, rst.size());
						}
						cnt++;
					}
					logger.info("Disc Rate Scenario for batchNum are Flushed :  {} / {}", i, batchNum*100);
				}
				
			} catch (Exception e) {
				logTable.updateFail(e);     
				logger.info("Error : {}", e);
			}
			session.saveOrUpdate(logTable);            
			session.getTransaction().commit();
			logger.info("Job 39(DiscRateSce with KICS Method ) is completed !!! ");
	
		}
	}

	private static void job39_SPREAD() {
		if (jobList.contains("39")) {
			forwardRateSetupForDiscRate(bssd, "S", "RF_KRW_KICS");
	
			LocalDateTime startTime = LocalDateTime.now();                                     
			LogTable logTable = getLogTable("BADA1004JM", startTime,bssd); 
			logger.info("Job 39(DiscRateSce with SPREAD Method) Start !!! ");
			
			
			session.beginTransaction();
			try {
				
				session.createQuery("delete DiscRateSce a where a.baseYymm=:param and a.discRateCalcTyp =:param2")
						.setParameter("param", bssd)
						.setParameter("param2", "S")
						.executeUpdate();
				
				List<DiscRateSce> rst = new ArrayList<DiscRateSce>();
				
				for(int i=0 ; i<= batchNum*100 ; i++) {
					cnt =1;
					rst = Job37_DiscRateSpreadAsync.getDiscRateAsync_Alt(bssd, String.valueOf(i), exe);
					
					for (DiscRateSce aa : rst) {
						session.save(aa);
						if (cnt % 50 == 0) {
							session.flush();
							session.clear();
						}
						if (cnt % flushSize == 0) {
							logger.info("Disc Rate Scenario are Flushed :  {} / {}", cnt++, rst.size());
						}
						cnt++;
					}
					logger.info("Disc Rate Scenario for batchNum are Flushed :  {} / {}", i, batchNum*100);
				}
				
			} catch (Exception e) {
				logTable.updateFail(e);      
				logger.info("Error : {}", e);
			}
			session.saveOrUpdate(logTable);            
			session.getTransaction().commit();
			logger.info("Job 39(DiscRateSce with SPREAD Method ) is completed !!! ");
	
		}
	}

	private static void job41() {
		if (jobList.contains("41")) {
			LocalDateTime startTime = LocalDateTime.now();
			LogTable logTable = getLogTable("BADA1005JM", startTime,bssd);
			logger.info("Job 41(Inflation) Start at {}", startTime);
			session.beginTransaction();
			
			try {
//				Job41_Inflation.getInflationMA(bssd).stream().forEach(s -> session.saveOrUpdate(s));
				Job41_Inflation.getInflationRate(bssd).stream().forEach(s -> session.saveOrUpdate(s));
				
			} catch (Exception e) {
				logTable.updateFail(e);
				logger.info("Error : {}", e);
			}
			session.saveOrUpdate(logTable);
			session.getTransaction().commit();
			logger.info("Job 41(Inflation) is Completed!!!");
		}
	}
	
	private static void job51() {
		cnt = 0;
		if (jobList.contains("51")) {
			LocalDateTime startTime = LocalDateTime.now();
			LogTable logTable = getLogTable("BADA1005JM", startTime,bssd);
			logger.info("Job 51(Corporate PD) Start!!!");
			
			session.beginTransaction();
			try {
				session.createQuery("delete CorpCumPd a where a.baseYymm=:param").setParameter("param", bssd).executeUpdate();
				session.createQuery("delete CorpCrdGrdPd a where a.baseYymm=:param").setParameter("param", bssd).executeUpdate();
				
				Job51_CorporatePd.getCorpPd(bssd).stream().forEach(s -> session.save(s));
				Job51_CorporatePd.getCorpCumPd(bssd).stream().forEach(s -> session.save(s));
			} catch (Exception e) {
				logTable.updateFail(e);
				logger.info("Error : {}", e);
			}
			session.saveOrUpdate(logTable);
			session.getTransaction().commit();
			logger.info("Job 51(Corporate PD & cumulative Pd) is Completed!!!");
		}

		
	}
//	BADA1005JM
	private static void job52() {

		if (jobList.contains("52")) {
			LocalDateTime startTime = LocalDateTime.now();
			LogTable logTable = getLogTable("BADA1005JM", startTime,bssd);
			logger.info("Job 52(Individual PD) Start!!!");
			session.beginTransaction();
			try {
				session.createQuery("delete IndiCrdGrdCumPd a where a.baseYymm=:param").setParameter("param", bssd).executeUpdate();
				session.createQuery("delete IndiCrdGrdPd a where a.baseYymm=:param").setParameter("param", bssd).executeUpdate();
				
				Job52_IndividualPd.getIndividualPd(bssd).stream().forEach(s -> session.save(s));
				Job52_IndividualPd.getIndividualCumPd(bssd).stream().forEach(s -> session.save(s));
			} catch (Exception e) {
				logTable.updateFail(e);
				logger.info("Error : {}", e);
			}
			session.saveOrUpdate(logTable);
			session.getTransaction().commit();
			logger.info("Job 52 (Individual pd and culmulative pd) is Completed !!!");
		}

				
	}
//	BADA1005JM
	private static void job53() {
		if (jobList.contains("53")) {
			LocalDateTime startTime = LocalDateTime.now();
			LogTable logTable = getLogTable("BADA1005JM", startTime,bssd);
			logger.info("Job 53(Segment LGD) Start!!!");
			session.beginTransaction();
			try {
				session.createQuery("delete SegLgd a where a.baseYymm=:param").setParameter("param", bssd).executeUpdate();
				Job53_SegLgd.getSegLgd(bssd).stream().forEach(s -> session.save(s));
			} catch (Exception e) {
				logTable.updateFail(e);
				logger.info("Error : {}", e);
			}
			session.saveOrUpdate(logTable);
			session.getTransaction().commit();
			logger.info("Job 53(Segment LGD) is Completed !!!");
		}
	}
//	BADA1003JM
	private static void job61() {
		if (jobList.contains("61")) {
			LocalDateTime startTime = LocalDateTime.now();
			LogTable logTable = getLogTable("BADA1003JM", startTime,bssd);
			
			logger.info("Job 61(IFRS Applied Discount Rate ETL) start !!!");
			session.beginTransaction();
			try {
//				Job61_IFRSBizApplyDcnt.getBizDcntRate(bssd).stream().forEach(s -> session.saveOrUpdate(s));
				Job221_IFRSBizApplyDcnt.getBizDcntRate(bssd, bottomUpList).stream().forEach(s -> session.saveOrUpdate(s));
				
			} catch (Exception e) {
				logTable.updateFail(e);
				logger.info("Error : {}", e);
			}
			session.saveOrUpdate(logTable);	
			session.getTransaction().commit();
			logger.info("Job 61(IFRS Applied Discount Rate ETL) Results are committed !!!");
		}	
	}
	
//	BADA1006JM
	private static void job63() {
		if (jobList.contains("63")) {
			LocalDateTime startTime = LocalDateTime.now();
			LogTable logTable = getLogTable("BADA1006JM", startTime,bssd);
			logger.info("Job 63(IFRS Applied RC ETL) start !!!");
			session.beginTransaction();
			try {
				session.createQuery("delete BizInflation a where a.baseYymm=:param and a.applyBizDv=:biz").setParameter("param", bssd).setParameter("biz", "I").executeUpdate();
				
				Job63_IFRSBizApplyRC.getBizCrdSpread(bssd).stream().forEach(s ->session.saveOrUpdate(s));
				Job63_IFRSBizApplyRC.getBizCorpPdFromCumPd(bssd).stream().forEach(s ->session.saveOrUpdate(s));
				Job63_IFRSBizApplyRC.getBizSegLgd(bssd).stream().forEach(s->session.saveOrUpdate(s));
				
//				Job63_IFRSBizApplyRC.getBizInflationMA(bssd).stream().forEach(s ->session.saveOrUpdate(s));
				List<BizInflation> infRst = Job63_IFRSBizApplyRC.getBizInflationMA(bssd);
				
				int  infMaxSeq = InflationDao.getMaxSeq(bssd, "I");
				
				infRst.stream().map(s -> s.convertToHisInflation(0)).forEach(s -> session.saveOrUpdate(s));
				infRst.stream().map(s -> s.convertToHisInflation(infMaxSeq+1)).forEach(s -> session.save(s));
				
			} catch (Exception e) {
				logTable.updateFail(e);
				logger.info("Error : {}", e);
			}
			
			
			
			session.saveOrUpdate(logTable);	
			session.getTransaction().commit();
			logger.info("Job 63(IFRS Applied RC Data ETL) is Completed !!!");
		}	
	}
//	BADA1003JM
	private static void job71() {
		if (jobList.contains("71")) {
			LocalDateTime startTime = LocalDateTime.now();
			LogTable logTable = getLogTable("BADA1003JM", startTime,bssd);
			logger.info("Job 71(KICS Applied Discount Rate) start !!!");
			session.beginTransaction();
			try {
				Job71_KicsBizApplyDcnt.getBizDcntRateKics(bssd).stream().forEach(s -> session.saveOrUpdate(s));
			} catch (Exception e) {
				logTable.updateFail(e);
				logger.info("Error : {}", e);
			}
			session.saveOrUpdate(logTable);	
			session.getTransaction().commit();
			logger.info("Job 71(KICS Applied Discount Rate ETL) Results are committed !!!");
		}
	}
	
//	BADA1006JM
	private static void job73() {
		if (jobList.contains("73")) {
			LocalDateTime startTime = LocalDateTime.now();
			LogTable logTable = getLogTable("BADA1006JM", startTime,bssd);
			logger.info("Job 72(KICS Applied Disc Rate Data ETL) start !!!");
			session.beginTransaction();
			try {
				
				session.createQuery("delete  BizInflation a where a.baseYymm=:param and a.applyBizDv=:biz").setParameter("param", bssd).setParameter("biz", "K").executeUpdate();
				
				Job73_KicsBizApplyRC.getBizCrdSpread(bssd).stream().forEach(s ->session.saveOrUpdate(s));
				Job73_KicsBizApplyRC.getBizCorpPdFromCumPd(bssd).stream().forEach(s ->session.saveOrUpdate(s));
				Job73_KicsBizApplyRC.getBizIndiPdFromCumPd(bssd).stream().forEach(s ->session.saveOrUpdate(s));
				Job73_KicsBizApplyRC.getBizSegLgd(bssd).stream().forEach(s->session.saveOrUpdate(s));
				Job73_KicsBizApplyRC.getBizSegPrepay(bssd).stream().forEach(s -> session.saveOrUpdate(s));
				
//				Job73_KicsBizApplyRC.getBizInflationMA(bssd).stream().forEach(s -> session.saveOrUpdate(s));
				List<BizInflation> infRst = Job73_KicsBizApplyRC.getBizInflationMA(bssd);
				
				int  infMaxSeq = InflationDao.getMaxSeq(bssd, "K");
				
				infRst.stream().map(s -> s.convertToHisInflation(0)).forEach(s -> session.saveOrUpdate(s));
				infRst.stream().map(s -> s.convertToHisInflation(infMaxSeq+1)).forEach(s -> session.save(s));
				
			} catch (Exception e) {
				logTable.updateFail(e);
				logger.info("Error : {}", e);
			}
			session.saveOrUpdate(logTable);	
			session.getTransaction().commit();
			logger.info("Job 73(KICS Applied Disc Rate  ETL) Results are committed !!!");
		}
	}
	
	private static List<DiscRateSce> calcDiscRateSce(String bizDv, String sceNo) {
		if(bizDv.equals("I")) {
			return Job35_DiscRateIfrsAsync.getDiscRateAsync_Alt(bssd, bizDv, "RF_KRW_BU", sceNo, exe);	
		}	else {
			return Job35_DiscRateIfrsAsync.getDiscRateAsync_Alt(bssd, bizDv, "RF_KRW_KICS", sceNo, exe);	
		}
	}
	
	private static void forwardRateSetupForDiscRate(String bssd, String bizDv, String irCurveId) {
		LocalDateTime startTime = LocalDateTime.now();                                     
		LogTable logTable = getLogTable("BADA1004JM", startTime,bssd); 
		logger.info("Pre-Setup for DiscRate and DiscRate Scenario under BizDv {} Start !!!  ", bizDv);
		session.beginTransaction();
		try {
			session.createQuery("delete BizDiscFwdRateSce a where a.baseYymm=:param and a.applyBizDv =:bizDv")
			.setParameter("param", bssd)
			.setParameter("bizDv", bizDv)
			.executeUpdate();
			
			List<BizDiscFwdRateSce> fwdRateBase = Job35_DiscRateFwdGen.getDiscFwdRateAsync(bssd, bizDv, irCurveId, exe);
			
			logger.info("Forward Rate size  : {}", fwdRateBase.size());
			cnt = 1;
			totalSize = fwdRateBase.size();
			
			for (BizDiscFwdRateSce aa : fwdRateBase) {
				session.save(aa);
				if (cnt % 50 == 0) {
					session.flush();
					session.clear();
				}
				if (cnt % flushSize == 0) {
					logger.info("Forward Rate for Disc Rate of BizDv {} are Flushed :  {} / {}", bizDv, cnt, totalSize);
				}
				cnt = cnt + 1;
			}
			
			List<BizDiscountRate> pastIntRateAll = BizDcntRateDao.getTimeSeries(bssd, bizDv, irCurveId, -36);
			for(int i =0; i< batchNum* 100; i++) {
				List<BizDiscFwdRateSce> fwdRateSce = Job35_DiscRateFwdGen.getDiscFwdRateAsync(bssd, bizDv, String.valueOf(i+1), pastIntRateAll, exe);
				cnt = 1;
				totalSize = fwdRateSce.size();
				
				for (BizDiscFwdRateSce aa : fwdRateSce) {
					session.save(aa);
					if (cnt % 50 == 0) {
						session.flush();
						session.clear();
					}
					if (cnt % flushSize == 0) {
						logger.info("Forward Rate Scenario for Disc Rate are Flushed :  {} / {}", cnt, totalSize);
					}
					cnt = cnt + 1;
				}
			
			}
		} catch (Exception e) {
			logTable.updateFail(e);      
		}
		session.saveOrUpdate(logTable);            
		session.getTransaction().commit();
//		try {
//			session.saveOrUpdate(logTable);            
//		} catch (Exception e) {
//			logger.error("LogTable Error : {}", e);
//		}
	}
	
//	private static void job36_Before() {
//		LocalDateTime startTime = LocalDateTime.now();                                     
//		LogTable logTable = getLogTable("BADA1004JM", startTime,bssd); 
//		logger.info("Job 36(DiscRate with KICS Method) Start !!! ");
//		session.beginTransaction();
//		try {
//			session.createQuery("delete BizDiscFwdRateSce a where a.baseYymm=:param and a.applyBizDv=:param2")
//			.setParameter("param", bssd)
//			.setParameter("param2", "K")
//			.executeUpdate();
//			
//			List<BizDiscFwdRateSce> fwdRateSCe = Job35_DiscRateFwdGen.getDiscFwdRateAsync(bssd, "K", exe);
//			
//			cnt = 1;
//			totalSize = fwdRateSCe.size();
//			
//			for (BizDiscFwdRateSce aa : fwdRateSCe) {
//				session.save(aa);
//				if (cnt % 50 == 0) {
//					session.flush();
//					session.clear();
//				}
//				if (cnt % flushSize == 0) {
//					logger.info("Disc Rate KICS are Flushed :  {} / {}", cnt, totalSize);
//				}
//				cnt = cnt + 1;
//			}
//		} catch (Exception e) {
//			logTable.updateFail(e);      
//		}
//		session.saveOrUpdate(logTable);            
//		session.getTransaction().commit();
//	}
//
//	
//
//	private static void job37_Before() {
//		LocalDateTime startTime = LocalDateTime.now();                                     
//		LogTable logTable = getLogTable("BADA1004JM", startTime,bssd); 
//		logger.info("Job 37(DiscRate with Spread Method) Start !!! ");
//		session.beginTransaction();
//		try {
//			session.createQuery("delete BizDiscFwdRateSce a where a.baseYymm=:param and a.applyBizDv=:param2")
//			.setParameter("param", bssd)
//			.setParameter("param2", "S")
//			.executeUpdate();
//			
//			List<BizDiscFwdRateSce> fwdRateSCe = Job35_DiscRateFwdGen.getDiscFwdRateAsync(bssd, "S", exe);
//			
//			cnt = 1;
//			totalSize = fwdRateSCe.size();
//			
//			for (BizDiscFwdRateSce aa : fwdRateSCe) {
//				session.save(aa);
//				if (cnt % 50 == 0) {
//					session.flush();
//					session.clear();
//				}
//				if (cnt % flushSize == 0) {
//					logger.info("Disc Rate Spread are Flushed :  {} / {}", cnt, totalSize);
//				}
//				cnt = cnt + 1;
//			}
//		} catch (Exception e) {
//			logTable.updateFail(e);      
//		}
//		session.saveOrUpdate(logTable);            
//		session.getTransaction().commit();
//	
//	}

	

	private static void test() {
		if (jobList.contains("99")) {
//			logger.info("aaa : {}", Job38_AccreteRateScenarioAsync.getPastCurveMap(bssd, 0, true));
//			
//			logger.info("aaa : {}", Job38_AccreteRateScenarioAsync.getPastCurveMap(bssd, 0, false));
//			List<BottomupDcnt> dcntRateList  = new ArrayList<>();
//			logger.info("aaa : {}", Job38_AccreteRateScenarioAsync.getFullCurveMap(bssd, "1", dcntRateList ,  true));
			
//			logger.info("aaa : {}", Job33_AccreteRateInternalNew.getPastCurveMap(bssd, -2, true));
//			
//			logger.info("aaa : {}", Job33_AccreteRateInternalNew.getPastCurveMap(bssd, -2, true).size());
			
//			logger.info("aaa : {}", Job33_AccreteRateInternalNew.aaa());
//			 Job33_AccreteRateInternalNew.getAssetYieldStat(bssd);
			 
			session.beginTransaction();
//			session.createQuery("delete DiscRateStats a where a.applStYymm=:param and a.discRateCalcTyp=:param2")
//								.setParameter("param", bssd).setParameter("param2", "I").executeUpdate();
//			 Job31_DiscRateStatInternal.getBaseDiscRateStat(bssd).forEach(s->session.save(s));
			
//			session.createQuery("delete BizDiscRateStat a where a.baseYymm=:param").setParameter("param", bssd).executeUpdate();
			
//			session.createQuery("delete BizDiscRateStat a where a.baseYymm=:param and a.applyBizDv=:param2")
//							.setParameter("param", bssd).setParameter("param2", "I").executeUpdate();
//			Job32_BizDiscRateStatInternal.getBaseDiscRateStat(bssd).forEach(s->session.save(s));
//			 
////			 session.createQuery("delete BizDiscRateStat a where a.baseYymm=:param and a.applyBizDv=:param2")
////			 			.setParameter("param", bssd).setParameter("param2", "K").executeUpdate();
//			 Job32_BizDiscRateStatKics.getBaseDiscRateStat(bssd).forEach(s->session.save(s));
//			 
////			 session.createQuery("delete BizDiscRateStat a where a.baseYymm=:param and a.applyBizDv=:param2")
////			 			.setParameter("param", bssd).setParameter("param2", "S").executeUpdate();
//			 Job32_BizDiscRateStatSpread.getBaseDiscRateStat(bssd).forEach(s->session.save(s));
			
			
//			 session.createQuery("delete DiscRate a where a.baseYymm=:param and a.discRateCalcTyp=:param2")
//			 			.setParameter("param", bssd).setParameter("param2", "I").executeUpdate();
			 
//			 session.createQuery("delete DiscRate a where a.baseYymm=:param " )
//	 			.setParameter("param", bssd)
//	 			.executeUpdate();
//			 for (DiscRate aa : Job36_DiscRateAsync.getDiscRateAsync(bssd, exe)) {
////				 for (DiscRate aa : Job36_DiscRateAsync.getDiscRate(bssd)) {	 
//					session.save(aa);
//					if (cnt % 50 == 0) {
//						session.flush();
//						session.clear();
//					}
//					if (cnt % flushSize == 0) {
//						logger.info("Disc Rate  Flush :  {} / {}", cnt, totalSize);
//					}
//					cnt = cnt + 1;
//				}
			 
			 session.createQuery("delete BizDiscRate a where a.baseYymm=:param " )
	 			.setParameter("param", bssd)
	 			.executeUpdate();

			 for (BizDiscRate aa : Job36_DiscRateAsyncDeprecate.getBizDiscRateAsync(bssd, exe)) {
					session.save(aa);
					if (cnt % 50 == 0) {
						session.flush();
						session.clear();
					}
					if (cnt % flushSize == 0) {
						logger.info("Disc Rate  Flush :  {} / {}", cnt, totalSize);
					}
					cnt = cnt + 1;
				}
			session.getTransaction().commit();
			
			
			
		}
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
	
	private static LogTable getLogTable(String projDtlId, LocalDateTime startTime, String bssd) {
		LogTable rst = new LogTable();
		
		rst.setPrjtNm(projDtlId);
		rst.setPrjtPtlNo("01");
		rst.setPrjtPtlStrDttm(startTime);
		
		rst.setBatTypNm("ESG");
		rst.setBzDvnNm("esg");
//		rst.setPrjtPtlFinDttm(LocalDateTime.now());
//		long execTime = Duration.between(startTime, rst.getPrjtPtlFinDttm()).getSeconds();
//		rst.setPrjtPtlExctTime(String.valueOf(execTime));

		rst.setPrjtExctStatNm("SUCCESS");
		rst.setPrjtPtlOprRslNm("SUCCESS");
		rst.setBsePotmDvcdVal("M");
		rst.setBsePotmVal(FinUtils.toEndOfMonth(bssd+"01"));
		rst.setFnalAmdrEmpno("ESG");
		rst.setFrstIpmnEmpno("ESG");
		rst.setFrstInptDttm(startTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
//		rst.setFnalUpdtDttm(rst.getPrjtPtlFinDttm().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
		
		
		return rst;
		
	}
	
	private static void temp() {
		if (jobList.contains("100")) {
			forwardRateSetupForDiscRate(bssd, "K", "RF_KRW_KICS");
		}
	}
}

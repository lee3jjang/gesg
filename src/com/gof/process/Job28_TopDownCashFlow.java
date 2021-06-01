package com.gof.process;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.TopDownDao;
import com.gof.entity.AssetCf;
import com.gof.entity.LiabCf;

/**
 *  <p> IFRS 17 의 TopDown 산출을 위한 기초 데이터인 자산 /부채 현금흐름 추출         
 *  <p> 경제적 가정의 TopDown 할인율 산출이 자산/부채 현금흐름 산출과정 보다 선행되어야 하고, 경제적 가정이 실행되는 시점에는 직전월의 자산 현금흐름 정보만 존재함.
 *  <p> 따라서 직전월 기준으로 현금흐름을 추출하며, 
 *  <p> TopDown 할인율을 산출시 적용할 CF 매칭 조정율은 직전월 기준의 현금흐름으로 산출함.
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class Job28_TopDownCashFlow {
	
	private final static Logger logger = LoggerFactory.getLogger("TopDown");

	/** 
	*  개별 자산의 현금흐름을 잔존만기별 현금흐름 만기별로 그룹핑하여 적재함
	*  잔존만기, 현금흐름 만기코드는 분기 기준임.
	*  @param bssd 	   기준년월
	*  @return        만기별 자산 현금흐름   
	*/ 
	public static List<AssetCf> createAssetCashFlow(String bssd) {
		List<AssetCf> rst  = new ArrayList<AssetCf>();
		
		rst = TopDownDao.getRawAssetCashFlow(bssd, 3);
		return rst;
	}
	
	/** 
	*  개별 보험부채의  현금흐름을  만기별로 그룹핑하여 적재함.
	*  현금흐름 만기코드는 분기기준임.    
	*  @param bssd 	   기준년월
	*  @return        만기별 부채 현금흐름   
	*/ 
	public static List<LiabCf> createLiabilityCashFlow(String bssd) {
		List<LiabCf> rst  = new ArrayList<LiabCf>();
		
		rst = TopDownDao.getRawLiabilityCashFlow(bssd,3);
		return rst;
	}

}

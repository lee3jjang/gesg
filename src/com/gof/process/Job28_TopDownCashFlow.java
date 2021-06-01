package com.gof.process;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.TopDownDao;
import com.gof.entity.AssetCf;
import com.gof.entity.LiabCf;

/**
 *  <p> IFRS 17 �� TopDown ������ ���� ���� �������� �ڻ� /��ä �����帧 ����         
 *  <p> ������ ������ TopDown ������ ������ �ڻ�/��ä �����帧 ������� ���� ����Ǿ�� �ϰ�, ������ ������ ����Ǵ� �������� �������� �ڻ� �����帧 ������ ������.
 *  <p> ���� ������ �������� �����帧�� �����ϸ�, 
 *  <p> TopDown �������� ����� ������ CF ��Ī �������� ������ ������ �����帧���� ������.
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class Job28_TopDownCashFlow {
	
	private final static Logger logger = LoggerFactory.getLogger("TopDown");

	/** 
	*  ���� �ڻ��� �����帧�� �������⺰ �����帧 ���⺰�� �׷����Ͽ� ������
	*  ��������, �����帧 �����ڵ�� �б� ������.
	*  @param bssd 	   ���س��
	*  @return        ���⺰ �ڻ� �����帧   
	*/ 
	public static List<AssetCf> createAssetCashFlow(String bssd) {
		List<AssetCf> rst  = new ArrayList<AssetCf>();
		
		rst = TopDownDao.getRawAssetCashFlow(bssd, 3);
		return rst;
	}
	
	/** 
	*  ���� �����ä��  �����帧��  ���⺰�� �׷����Ͽ� ������.
	*  �����帧 �����ڵ�� �б������.    
	*  @param bssd 	   ���س��
	*  @return        ���⺰ ��ä �����帧   
	*/ 
	public static List<LiabCf> createLiabilityCashFlow(String bssd) {
		List<LiabCf> rst  = new ArrayList<LiabCf>();
		
		rst = TopDownDao.getRawLiabilityCashFlow(bssd,3);
		return rst;
	}

}

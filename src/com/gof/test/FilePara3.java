package com.gof.test;

import java.util.List;
import java.util.concurrent.Callable;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.entity.IrCurveHis;
import com.gof.util.HibernateUtil;

public class FilePara3 implements Callable<Double>{
	private final static Logger logger = LoggerFactory.getLogger("DAO");
//	private static Session session = HibernateUtil.getSessionFactory().getCurrentSession();
//	private static Session session = HibernateUtil.getSessionFactory().openSession();
	
	private String str;
	private IrCurveHis irCurve;
	
	
	

	public FilePara3(IrCurveHis irCurve) {
		super();
		this.irCurve = irCurve;
	}

	public String getStr() {
		return str;
	}
	public void setStr(String str) {
		this.str = str;
	}

	
	public IrCurveHis getIrCurve() {
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
			logger.info("Error  : {},{}", e);
		}
		return irCurve;
	}

	public void setIrCurve(IrCurveHis irCurve) {
		this.irCurve = irCurve;
	}

	@Override
	public Double call() throws Exception {
	
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
			logger.info("Error  : {},{}", e);
		}


		return irCurve.getIntRate(); 
	}
}

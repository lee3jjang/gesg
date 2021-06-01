package com.gof.test;

import java.util.List;
import java.util.concurrent.Callable;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.entity.IrCurveHis;
import com.gof.util.HibernateUtil;

public class ReporterNormal {
	private final static Logger logger = LoggerFactory.getLogger("DAO");
//	private static Session session = HibernateUtil.getSessionFactory().getCurrentSession();
//	private static Session session = HibernateUtil.getSessionFactory().openSession();
	private double number;
	
	
	

	public ReporterNormal(double number) {
		super();
		this.number = number;
	}
	
	public ReporterNormal(Double number) {
		super();
		this.number = number;
	}

//	@Override
//	public void run() {
//		try {
//			Thread.sleep(2000);
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//		
//		System.out.println("aaa" + number +"_" + Thread.currentThread().getName());
//		
//	}
	
	public double getNumber() {
		System.out.println("Reporter" + number +"_" + Thread.currentThread().getName());
		try {
			if(number==4.0) {
				logger.info("Sleep");
				Thread.sleep((int)number * 1000);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return number;
	}

	public void setNumber(double number) {
		this.number = number;
	}

}

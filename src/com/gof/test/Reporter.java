package com.gof.test;

import java.util.List;
import java.util.concurrent.Callable;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.entity.IrCurveHis;
import com.gof.util.HibernateUtil;

public class Reporter implements Callable<Double>{
	private final static Logger logger = LoggerFactory.getLogger("DAO");
//	private static Session session = HibernateUtil.getSessionFactory().getCurrentSession();
//	private static SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
//	private static Session session = sessionFactory.getCurrentSession();
//	private static Session session = HibernateUtil.getSessionFactory().openSession();
	private Session session = HibernateUtil.getSessionFactory().openSession();
//	private Session session = HibernateUtil.getSessionFactory().getCurrentSession();
	private double number;
	
	
	

	public Reporter(double number) {
		super();
		this.number = number;
	}
	
	public Reporter(Double number) {
		super();
		this.number = number;
	}

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

	@Override
	public Double call() throws Exception {
//		session.joinTransaction();
		session.beginTransaction();
		
		logger.info("session : {},{}", session.isOpen(), session.toString() );
		List<IrCurveHis> irCuve = getIrCurveHis("20171229", "A100");
		logger.info("Size  : {},{}", number, irCuve.size());
		try {
			Thread.sleep((int)number * 1000);
		} catch (Exception e) {
			logger.info("Error  : {},{}", e);
		}
		System.out.println("Reporter" + number +"_" + Thread.currentThread().getName());
		session.getTransaction().commit();
		return number;
	}

	private List<IrCurveHis> getIrCurveHis(String bssd, String irCurveId){
		String query = "select a from IrCurveHis a "
				+ "		where 1=1 "
				+ "		and a.irCurveId =:irCurveId "
				+ "		and a.baseDate  = :bssd	"
				+ "     order by a.matCd"
				;
		
		List<IrCurveHis> curveRst =  session.createQuery(query, IrCurveHis.class)
				.setParameter("irCurveId", irCurveId)
				.setParameter("bssd", bssd)
				.getResultList();
		return curveRst;
	}
	
}

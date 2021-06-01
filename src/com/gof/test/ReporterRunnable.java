package com.gof.test;

import java.util.List;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.entity.IrCurveHis;
import com.gof.util.HibernateUtil;

public class ReporterRunnable implements Runnable{
	private final static Logger logger = LoggerFactory.getLogger("DAO");
	private static Session session = HibernateUtil.getSessionFactory().getCurrentSession();
	
	private double number;
	
	
	

	public ReporterRunnable(double number) {
		super();
		this.number = number;
	}

	@Override
	public void run() {
		
		List<IrCurveHis> irCuve = getIrCurveHis("20171229", "A100");
		logger.info("Size  : {}", irCuve.size());
		try {
			Thread.sleep(2000);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		System.out.println("runnable" + number +"_" + Thread.currentThread().getName() +"_"+ irCuve.size());
		
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

package com.gof.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.DaoUtil;
import com.gof.dao.IrCurveHisDao;
import com.gof.entity.EsgMeta;
import com.gof.entity.EsgMst;
import com.gof.entity.InvestManageCostUd;
import com.gof.entity.IrCurveHis;
import com.gof.entity.IrSce;
import com.gof.enums.EBoolean;
import com.gof.util.HibernateUtil;
import com.gof.util.ParamUtil;
import com.gof.util.ScriptUtil;

public class ScriptTest {
	private final static Logger logger = LoggerFactory.getLogger("DAO");
	private static Session session = HibernateUtil.getSessionFactory().openSession();
//	private static Session session = HibernateUtil.getSessionFactory().getCurrentSession();
	public static void main(String[] args) {
		String bssd ="201912";
		
//		ScriptUtil.getScriptContentsSet().forEach(s ->logger.info("aa : {}", s));
		ScriptUtil.getScriptContents().forEach(s ->logger.info("aa : {}", s));

	}
	
	
}

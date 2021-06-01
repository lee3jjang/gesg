package com.gof.process;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.metamodel.PluralAttribute.CollectionType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.IrCurveHisShockDao;
import com.gof.entity.IrCurveHis;
import com.gof.entity.IrCurveWeek;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;


/**
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class Job19_AfnsScenarioSetup {	
	private final static Logger log = LoggerFactory.getLogger("Job");
	
	public static List<IrCurveWeek> setupIrCurveWeek(String bssd, String stBssd, String irCurveId, List<String> tenorList, DayOfWeek dayOfWeek){	
		List<IrCurveWeek> rstList = new ArrayList<IrCurveWeek>();
		
		List<IrCurveHis> curveHisList  = IrCurveHisShockDao.getIrCurveListTermStructureForShock(bssd, stBssd, irCurveId, tenorList);
		
		if(curveHisList.size()==0) {
			log.warn("IR Curve History of {} Data is not found at from {} to {}", irCurveId, stBssd, bssd);
			return null;
		}
		
		WeekFields wf = WeekFields.of(Locale.KOREA);
		List<String> dayOfWeekSet = new ArrayList<String>();
		
		Set<LocalDate> dateSet = curveHisList.stream()
											.map(s-> LocalDate.parse(s.getBaseDate(), DateTimeFormatter.BASIC_ISO_DATE))
//											.sorted(Comparator.reverseOrder())
											.collect(Collectors.toSet());
		
		List<LocalDate> dateList = dateSet.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
		
		boolean needToInsert = true;
		int currWeekOfYear  = 0;
		for( LocalDate aa : dateList) {
			log.info("date1 :  {},{}",aa.get(wf.weekOfWeekBasedYear()),  aa.format(DateTimeFormatter.BASIC_ISO_DATE));
			if(currWeekOfYear!= aa.get(wf.weekOfWeekBasedYear())) {
				needToInsert = true;
			}
			
			if(aa.getDayOfWeek().equals(dayOfWeek)) {
				dayOfWeekSet.add(aa.format(DateTimeFormatter.BASIC_ISO_DATE));
				currWeekOfYear = aa.get(wf.weekOfWeekBasedYear());
				needToInsert= false;
			}
			else if( needToInsert && currWeekOfYear != aa.get(wf.weekOfYear())){
				dayOfWeekSet.add(aa.format(DateTimeFormatter.BASIC_ISO_DATE));
				currWeekOfYear = aa.get(wf.weekOfWeekBasedYear());
				needToInsert= false;
			}
			else {
				
			}
		}
		dayOfWeekSet.stream().sorted().forEach(s-> log.info("qqqq : {}", s));
		
		return curveHisList.stream().filter(s-> dayOfWeekSet.contains(s.getBaseDate()))
								    .map(s-> s.convertToWeek())
								    .collect(Collectors.toList());
		
	}
	
//	public static List<IrCurveWeek> setupIrCurveWeek(String bssd, String stBssd, String irCurveId, List<String> tenorList, DayOfWeek dayOfWeek){	
//		List<IrCurveWeek> rstList = new ArrayList<IrCurveWeek>();
//		
//		List<IrCurveHis> curveHisList  = IrCurveHisShockDao.getIrCurveListTermStructureForShock(bssd, stBssd, irCurveId, tenorList);
//		
//		if(curveHisList.size()==0) {
//			log.warn("IR Curve History of {} Data is not found at from {} to {}", irCurveId, stBssd, bssd);
//			return null;
//		}
//		
//		LocalDate stDate = LocalDate.parse(stBssd+"01", DateTimeFormatter.BASIC_ISO_DATE);
//		LocalDate endDate = LocalDate.parse(bssd+"31", DateTimeFormatter.BASIC_ISO_DATE);
//		LocalDate tempDate= stDate;
//		List<LocalDate> fullDateList = new ArrayList<LocalDate>();
//		
//		while(tempDate.isBefore(endDate) ) {
//			if(tempDate.getDayOfWeek().equals(dayOfWeek)) {
//				fullDateList.add(tempDate);
//			}
//			tempDate = tempDate.plusDays(1);
//		}
//		fullDateList.forEach(s-> log.info("aaa : {}", s.toString()));
//		WeekFields wf = WeekFields.of(Locale.KOREA);
//		
//		Set<LocalDate> dateSet = curveHisList.stream()
//											.map(s-> LocalDate.parse(s.getBaseDate(), DateTimeFormatter.BASIC_ISO_DATE))
//											.sorted(Comparator.reverseOrder())
//											.collect(Collectors.toSet());
//		
//		List<LocalDate> dateList = dateSet.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
//									
////		Map<String, LocalDate> dateMap = curveHisList.stream()
////											.collect(Collectors.toMap(IrCurveHis::getBaseDate, s-> LocalDate.parse(s.getBaseDate(), DateTimeFormatter.BASIC_ISO_DATE), (s,u)->s));
//						
//		Map<String, List<LocalDate>> weekMap = dateList.stream().collect(Collectors.groupingBy(s-> s.getYear() +"_" + s.get(wf.weekOfYear()), Collectors.toList()));
//				
//		Set<String> dayOfWeekSet = new HashSet<String>();
//		for(Map.Entry<String, List<LocalDate>> entry : weekMap.entrySet()) {
//			log.info("date :  {},{}", entry.getKey(), entry.getValue());
////			log.info("date :  {},{}", entry.getKey(), entry.getValue().toString(), entry.getValue().format(DateTimeFormatter.BASIC_ISO_DATE));
//		}
//		
//		boolean needToCheck = true;
//		int currWeekOfYear  = 0;
//		for( LocalDate aa : dateList) {
//			log.info("date1 :  {},{}", aa.toString(), aa.format(DateTimeFormatter.BASIC_ISO_DATE));
//			if(currWeekOfYear!= aa.get(wf.weekOfYear())) {
//				needToCheck = true;
//			}
//			
//			if(aa.getDayOfWeek().equals(dayOfWeek)) {
//				dayOfWeekSet.add(aa.format(DateTimeFormatter.BASIC_ISO_DATE));
//				currWeekOfYear = aa.get(wf.weekOfYear());
//				needToCheck= false;
//			}
//			else if( needToCheck && currWeekOfYear != aa.get(wf.weekOfYear())){
//				dayOfWeekSet.add(aa.format(DateTimeFormatter.BASIC_ISO_DATE));
//				currWeekOfYear = aa.get(wf.weekOfYear());
//				needToCheck= false;
//			}
//			else {
//				
//			}
////			currWeekOfYear = aa.get(wf.weekOfYear());
//		}
//		 dayOfWeekSet.stream().sorted().forEach(s-> log.info("qqqq : {}", s));
//		
//		return curveHisList.stream().filter(s-> dayOfWeekSet.contains(s.getBaseDate()))
//								    .map(s-> s.convertToWeek(dayOfWeek))
//								    .collect(Collectors.toList());
//		
//	}
}

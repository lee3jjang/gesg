package com.gof.test;

import java.io.BufferedWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.hibernate.Session;
import org.renjin.gnur.api.RStartup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.DiscRateStatsDao;
import com.gof.entity.InvestManageCostUd;
import com.gof.util.FileUtils;
import com.gof.util.HibernateUtil;

public class Test5 {
	private final static Logger logger = LoggerFactory.getLogger("DAO");

	public static void main(String[] args) {
		
		String bssd = "20131227";
		String bssd1 = "20131231";
		String bssd2 = "20140101";
		WeekFields aa = WeekFields.of(Locale.KOREA);
		
		LocalDate weekend = LocalDate.parse(bssd, DateTimeFormatter.BASIC_ISO_DATE);
		LocalDate weekend1 = LocalDate.parse(bssd1, DateTimeFormatter.BASIC_ISO_DATE);
		LocalDate weekend2 = LocalDate.parse(bssd2, DateTimeFormatter.BASIC_ISO_DATE);
		
		logger.info("aaaa0 :  {},{},{},{},{},{}", weekend.getEra(), weekend.get(aa.weekOfWeekBasedYear()));
		logger.info("aaaa1 :  {},{},{},{},{},{}", weekend1.getEra(), weekend1.get(aa.weekOfWeekBasedYear()));
		logger.info("aaaa1 :  {},{},{},{},{},{}", weekend2.getEra(), weekend2.get(aa.weekOfWeekBasedYear()));
		
		
		logger.info("aaaa0 :  {},{},{},{},{},{}", weekend.getEra(), weekend.get(aa.weekOfYear()));
		logger.info("aaaa1 :  {},{},{},{},{},{}", weekend1.getEra(), weekend1.get(aa.weekOfYear()));
		logger.info("aaaa1 :  {},{},{},{},{},{}", weekend2.getEra(), weekend2.get(aa.weekOfYear()));
		
		logger.info("aaaa :  {},{},{},{},{},{}", weekend.getEra(), weekend.toEpochDay(), weekend.toString());
		logger.info("aaaa :  {},{},{},{},{},{}", weekend.getEra(), weekend.getDayOfYear());
		logger.info("aaaa :  {},{},{},{},{},{}", weekend.getEra(), weekend.get(aa.weekBasedYear()));
		logger.info("aaaa :  {},{},{},{},{},{}", weekend.getEra(), weekend.get(aa.weekBasedYear()));
		
		logger.info("aaaa1 :  {},{},{},{},{},{}", weekend1.getEra(), weekend1.toEpochDay(), weekend.toString());
		logger.info("aaaa1 :  {},{},{},{},{},{}", weekend1.getEra(), weekend1.getDayOfYear());
		logger.info("aaaa1 :  {},{},{},{},{},{}", weekend1.getEra(), weekend1.get(aa.weekBasedYear()));
		logger.info("aaaa1 :  {},{},{},{},{},{}", weekend1.getEra(), weekend1.get(aa.weekBasedYear()));
		
		logger.info("aaaa2 :  {},{},{},{},{},{}", weekend2.getEra(), weekend2.toEpochDay(), weekend.toString());
		logger.info("aaaa2 :  {},{},{},{},{},{}", weekend2.getEra(), weekend2.getDayOfYear());
		logger.info("aaaa1 :  {},{},{},{},{},{}", weekend2.getEra(), weekend2.get(aa.weekBasedYear()));
		logger.info("aaaa1 :  {},{},{},{},{},{}", weekend2.getEra(), weekend2.get(aa.weekBasedYear()));

		logger.info("aaaa1 :  {},{},{},{},{},{}", DayOfWeek.FRIDAY.toString(), weekend.getYear());
		
	}
	
	
	
	
	
}

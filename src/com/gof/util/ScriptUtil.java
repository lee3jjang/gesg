package com.gof.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.DaoUtil;
import com.gof.entity.EsgScript;
import com.gof.enums.EBoolean;
import com.gof.process.Main;

public class ScriptUtil {
	
	private final static Logger logger = LoggerFactory.getLogger("ScriptUtil");
	
	private static List<String> scriptContents = new ArrayList<>();
	private static Set<String> scriptContentsSet = new HashSet<>();
	private static Map<String, String> scriptMap = new HashMap<String, String>();
	
//	Default Script Read  ==>  DB Script ==> File Script 순서로 update 됨. 동일한 function 명은 최종적으로 update 된 것이 적용됨.
/*	static {
		final String path = "resources";
		File jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		
		String tempScript ;
		
		if(jarFile.isFile()) {
			try {
				JarFile jar = new JarFile(jarFile);
				Enumeration<JarEntry> entries = jar.entries();
				while(entries.hasMoreElements()) {
					final String name = entries.nextElement().getName();
					if(name.startsWith(path +"/")) {
						InputStream dirIns = Main.class.getClassLoader().getResourceAsStream(name);
						BufferedReader dirReader = new BufferedReader(new InputStreamReader(dirIns));
						
						Stream<String> dirStream = dirReader.lines();
						tempScript = dirStream.filter(s -> !s.trim().startsWith("#")).filter(s -> s.length() > 0).collect(Collectors.joining("\n"));
						
						scriptMap.put(name.replace("resources/", "").replace(".R", ""), tempScript);          //TODO : 스크립트 정리되면 주석 풀기
					}
				}
				jar.close();
			} catch (Exception e) {
				logger.error("ScriptUtil file error :  {}", e);
			}
		}
		else {
			try {
				Path dir = Paths.get(Main.class.getClassLoader().getResource("resources").toURI());
				for (Path zz : Files.walk(dir).filter(Files::isRegularFile).collect(Collectors.toList())) {	
						Stream<String> stream = Files.lines(zz);
						tempScript = stream.filter(s -> !s.trim().startsWith("#")).filter(s -> s.length() > 0).collect(Collectors.joining("\n"));
//						scriptMap.put(zz.toFile().getName().replace("resources/", "").replace(".R", ""), tempScript);
						
//						scriptMap.entrySet().stream().forEach(s -> logger.info("script : {},{}", s.getKey(), s.getValue()));
						logger.info("Scrip File load");
				}
			} catch (Exception e) {
				logger.error("ScriptUtil file error :  {}", e);
			}
		}
		
		
//		DB Script Read 		
		Session session = HibernateUtil.getSessionFactory().openSession();
	    session.beginTransaction();
	    
	    Map<String, Object> param = new HashMap<String, Object>();
	    param.put("useYn", EBoolean.Y);
	    param.put("scriptType", "02");
	    
    	List<EsgScript> scriptList = DaoUtil.getEntities(EsgScript.class, param);
    	for(EsgScript kk : scriptList) {
    		scriptMap.put(kk.getScriptId(), kk.getScriptContent());
    	}
		
    	
		scriptContents = scriptMap.values().stream().collect(Collectors.toList());
//		logger.info("script : {}",scriptContents);
	}*/

	public static List<String> getScriptContents(String dir) {
		String tempScript ;
		try (Stream<Path> stream = Files.walk(Paths.get(dir))){
			for(Path aa: stream.filter(s -> !s.toFile().isDirectory()).filter(s -> s.getFileName().toString().endsWith(".R")).collect(Collectors.toList())) {
				tempScript = Files.lines(aa).filter(s -> !s.trim().startsWith("#")).filter(s -> !s.startsWith("source")).collect(Collectors.joining("\n"));
				scriptMap.put(aa.toFile().getName().replace(".R", ""), tempScript);
			}
		} catch (Exception e) {
			logger.info("Error : {}", e);
		}
		logger.info("File Script created" );
		scriptContents = scriptMap.values().stream().collect(Collectors.toList());
		
		return scriptContents;
	}
	
	public static List<String> getScriptContents() {
		if(scriptContents.size()==0) {
//			return getScriptContents();
			load();
		}
		return scriptContents;
	}
	
	public static Set<String> getScriptContentsSet() {
		if(scriptContentsSet.size()==0) {
//			return getScriptContents();
			load();
		}
		return scriptContentsSet;
	}
	public static Map<String, String> getScriptMap() {
		return scriptMap;
	}
	
	
	 private static void load() {
			final String path = "resources";
			File jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath());
			
			String tempScript ;
			
			if(jarFile.isFile()) {
				try {
					JarFile jar = new JarFile(jarFile);
					Enumeration<JarEntry> entries = jar.entries();
					while(entries.hasMoreElements()) {
						final String name = entries.nextElement().getName();
						if(name.startsWith(path +"/")) {
							InputStream dirIns = Main.class.getClassLoader().getResourceAsStream(name);
							BufferedReader dirReader = new BufferedReader(new InputStreamReader(dirIns));
							
							Stream<String> dirStream = dirReader.lines();
							tempScript = dirStream.filter(s -> !s.trim().startsWith("#")).filter(s -> s.length() > 0).collect(Collectors.joining("\n"));
							
							scriptMap.put(name.replace("resources/", "").replace(".R", ""), tempScript);          //TODO : 스크립트 정리되면 주석 풀기
						}
					}
					jar.close();
				} catch (Exception e) {
					logger.error("ScriptUtil file error :  {}", e);
				}
			}
			else {
				try {
					Path dir = Paths.get(Main.class.getClassLoader().getResource("resources").toURI());
					for (Path zz : Files.walk(dir).filter(Files::isRegularFile).collect(Collectors.toList())) {	
							Stream<String> stream = Files.lines(zz);
							tempScript = stream.filter(s -> !s.trim().startsWith("#")).filter(s -> s.length() > 0).collect(Collectors.joining("\n"));
							scriptMap.put(zz.toFile().getName().replace("resources/", "").replace(".R", ""), tempScript);
							
//							scriptMap.entrySet().stream().forEach(s -> logger.info("script : {},{}", s.getKey(), s.getValue()));
//							logger.info("Scrip File load : {}", tempScript);
					}
				} catch (Exception e) {
					logger.error("ScriptUtil file error :  {}", e);
				}
			}
			
			
//			DB Script Read 		
			/*Session session = HibernateUtil.getSessionFactory().openSession();
		    session.beginTransaction();
		    
		    Map<String, Object> param = new HashMap<String, Object>();
		    param.put("useYn", EBoolean.Y);
		    param.put("scriptType", "02");
		    
	    	List<EsgScript> scriptList = DaoUtil.getEntities(EsgScript.class, param);
	    	for(EsgScript kk : scriptList) {
	    		scriptMap.put(kk.getScriptId(), kk.getScriptContent());
	    	}*/
			
	    	
//	    	scriptContents = scriptMap.entrySet().stream().filter(s->s.getKey().startsWith("Hw")).map(s ->s.getValue()).collect(Collectors.toList());
			scriptContents = scriptMap.values().stream().collect(Collectors.toList());
			scriptContentsSet =scriptMap.values().stream().collect(Collectors.toSet());
			
			
//			logger.info("script : {}",scriptContents);
		}
}

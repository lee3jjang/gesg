package com.gof.csvmapper;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.gof.entity.IrCurveHis;
import com.gof.entity.SmithWilsonResult;

import de.bytefish.jtinycsvparser.CsvParser;
import de.bytefish.jtinycsvparser.CsvParserOptions;
import de.bytefish.jtinycsvparser.builder.IObjectCreator;
import de.bytefish.jtinycsvparser.mapping.CsvMapping;
import de.bytefish.jtinycsvparser.mapping.CsvMappingResult;

/**
 *  <p> HullWhite 모형 등의 금리모형으로 산출한 시나리오는 대용량의 데이터로써 파일 인터페이스도 요구됨.(시나리오 1,000 개 * 만기Bucket 1200 개 * 통화별 금리곡선 ID (4~5개))         
 *  <p> 파일에 저장된 금리 시나리오를 금리 이력( {@link IrCurveHis})로 변환하여 입수하는 기능을 수행함. </p>
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class SmithWilsonResultMapper extends CsvMapping<SmithWilsonResult>{

	    public SmithWilsonResultMapper(IObjectCreator creator) {
	        super(creator);
//	        BASE_DATE, IR_MODEL_ID, MAT_CD, SCE_NO, IR_CURVE_ID, INT_RATE, VOL, LAST_MODIFIEID_BY, LAST_UPDATE
	        
//	        mapProperty(0, String.class, SmithWilsonResult::setBaseDate);
	        mapProperty(2, String.class, SmithWilsonResult::setMatCd);
	        mapProperty(3, String.class, SmithWilsonResult::setSceNo);
	        mapProperty(4, String.class, SmithWilsonResult::setIrCurveId);
	        mapProperty(5, Double.class, SmithWilsonResult::setSpotAnnual);
	        
	    }
	    
		public static Stream<CsvMappingResult<SmithWilsonResult>> read(Path filePath) throws Exception{
	    	return read(filePath, ",", StandardCharsets.UTF_8);
	    }
	    
	    public static Stream<CsvMappingResult<SmithWilsonResult>> read(Path filePath, String delimeter) throws Exception{
	    	return read(filePath, delimeter, StandardCharsets.UTF_8);
	    }
	    
	    public static Stream<CsvMappingResult<SmithWilsonResult>> read(Path filePath, String delimeter, Charset charSet) throws Exception{
	    	return read(filePath, new CsvParserOptions(true, delimeter,false), charSet);
	    	
	    } 
	    public static Stream<CsvMappingResult<SmithWilsonResult>> read(Path filePath, CsvParserOptions options, Charset charSet) throws Exception{
	    	SmithWilsonResultMapper 	  	  mapper = new SmithWilsonResultMapper(() ->new SmithWilsonResult());
	    	CsvParser<SmithWilsonResult>   parser = new CsvParser<SmithWilsonResult>(options, mapper);
	    	return parser.readFromFile(filePath, charSet);
	    }

	    
	    /**
	     * <p> 파일에 저장된 금리 시나리오를  {@link SmithWilsonResult} 로 변환하여 추출함. 
	     * <p> 이때 Java 8 의 Stream 형식으로 리턴하여 대용량의 금리 시나리오의 처리시 메모리 문제를 사전에 방지함.  
	     *  
	     * @param filePath 파일경로
	     * @throws Exception 파일 경로 등의 오류시 발생함.
	     * @return  {@link SmithWilsonResult} 
	    */
	    
	    public static Stream<SmithWilsonResult> readEntity(Path filePath) throws Exception{
    		return SmithWilsonResultMapper.read(filePath).map(s -> s.getResult());
    	}
	    
	    
	    /**
	     * <p> 파일에 저장된 금리 시나리오를  (시나리오번호,금리기간구조)의 조합으로 변환하여 추출함. 
	     *  
	     * @param filePath 파일경로
	     * @throws Exception 파일 경로 등의 오류시 발생함.
	     * @return  key 가 시나리오 번호이고 value 가 금리 기간구조 ( 만기 Bucket : M0001 ~ M1200) 인 Map 으로 변환하여 데이터 추출함. 
	    */
	    public static Map<String, List<SmithWilsonResult>> readGroupBySceNo(Path filePath) throws Exception{
    		return SmithWilsonResultMapper.read(filePath).map(s -> s.getResult())
    							   .collect(Collectors.groupingBy(s -> s.getSceNo(), Collectors.toList()))
			    					 ;
    	}
}


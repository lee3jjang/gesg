package com.gof.csvmapper;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.gof.entity.IrCurveHis;

import de.bytefish.jtinycsvparser.CsvParser;
import de.bytefish.jtinycsvparser.CsvParserOptions;
import de.bytefish.jtinycsvparser.builder.IObjectCreator;
import de.bytefish.jtinycsvparser.mapping.CsvMapping;
import de.bytefish.jtinycsvparser.mapping.CsvMappingResult;

/**
 *  <p> HullWhite ���� ���� �ݸ��������� ������ �ó������� ��뷮�� �����ͷν� ���� �������̽��� �䱸��.(�ó����� 1,000 �� * ����Bucket 1200 �� * ��ȭ�� �ݸ�� ID (4~5��))         
 *  <p> ���Ͽ� ����� �ݸ� �ó������� �ݸ� �̷�( {@link IrCurveHis})�� ��ȯ�Ͽ� �Լ��ϴ� ����� ������. </p>
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class IrCurveHisMapper extends CsvMapping<IrCurveHis>{

	    public IrCurveHisMapper(IObjectCreator creator) {
	        super(creator);
//	        BASE_DATE, IR_MODEL_ID, MAT_CD, SCE_NO, IR_CURVE_ID, INT_RATE, VOL, LAST_MODIFIEID_BY, LAST_UPDATE
	        
	        mapProperty(0, String.class, IrCurveHis::setBaseDate);
	        mapProperty(2, String.class, IrCurveHis::setMatCd);
	        mapProperty(3, String.class, IrCurveHis::setSceNo);
	        mapProperty(4, String.class, IrCurveHis::setIrCurveId);
	        mapProperty(5, Double.class, IrCurveHis::setIntRate);
	        
	    }
	    
		public static Stream<CsvMappingResult<IrCurveHis>> read(Path filePath) throws Exception{
	    	return read(filePath, ",", StandardCharsets.UTF_8);
	    }
	    
	    public static Stream<CsvMappingResult<IrCurveHis>> read(Path filePath, String delimeter) throws Exception{
	    	return read(filePath, delimeter, StandardCharsets.UTF_8);
	    }
	    
	    public static Stream<CsvMappingResult<IrCurveHis>> read(Path filePath, String delimeter, Charset charSet) throws Exception{
	    	return read(filePath, new CsvParserOptions(true, delimeter,false), charSet);
	    	
	    } 
	    public static Stream<CsvMappingResult<IrCurveHis>> read(Path filePath, CsvParserOptions options, Charset charSet) throws Exception{
	    	IrCurveHisMapper 	  	  mapper = new IrCurveHisMapper(() ->new IrCurveHis());
	    	CsvParser<IrCurveHis>   parser = new CsvParser<IrCurveHis>(options, mapper);
	    	return parser.readFromFile(filePath, charSet);
	    }

	    
	    /**
	     * <p> ���Ͽ� ����� �ݸ� �ó�������  {@link IrCurveHis} �� ��ȯ�Ͽ� ������. 
	     * <p> �̶� Java 8 �� Stream �������� �����Ͽ� ��뷮�� �ݸ� �ó������� ó���� �޸� ������ ������ ������.  
	     *  
	     * @param filePath ���ϰ��
	     * @throws Exception ���� ��� ���� ������ �߻���.
	     * @return  {@link IrCurveHis} 
	    */
	    
	    public static Stream<IrCurveHis> readEntity(Path filePath) throws Exception{
    		return IrCurveHisMapper.read(filePath).map(s -> s.getResult());
    	}
	    
	    
	    public static Stream<IrCurveHis> readEntityNoHeader(Path filePath) throws Exception{
    		CsvParserOptions options = new CsvParserOptions(false, ",");
	    	return IrCurveHisMapper.read(filePath, options, StandardCharsets.UTF_8).map(s -> s.getResult());
    	}
	    /**
	     * <p> ���Ͽ� ����� �ݸ� �ó�������  (�ó�������ȣ,�ݸ��Ⱓ����)�� �������� ��ȯ�Ͽ� ������. 
	     *  
	     * @param filePath ���ϰ��
	     * @throws Exception ���� ��� ���� ������ �߻���.
	     * @return  key �� �ó����� ��ȣ�̰� value �� �ݸ� �Ⱓ���� ( ���� Bucket : M0001 ~ M1200) �� Map ���� ��ȯ�Ͽ� ������ ������. 
	    */
	    public static Map<String, List<IrCurveHis>> readGroupBySceNo(Path filePath) throws Exception{
    		return IrCurveHisMapper.read(filePath).map(s -> s.getResult())
    							   .collect(Collectors.groupingBy(s -> s.getSceNo(), Collectors.toList()))
			    					 ;
    	}
}


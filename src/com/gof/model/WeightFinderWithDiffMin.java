package com.gof.model;

import org.apache.commons.lang3.ArrayUtils;
import org.ejml.simple.SimpleMatrix;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.core.variable.RealVariable;
import org.moeaframework.problem.AbstractProblem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeightFinderWithDiffMin extends AbstractProblem{
	private final static Logger logger = LoggerFactory.getLogger("WeightFinder");
	private SimpleMatrix wMatrix ;
	private SimpleMatrix bMatrix ;

	private double minBound =0.5;
	private double maxBound =10.0;
	private double maxAssetIncrease =2.0;
		
	public WeightFinderWithDiffMin(int numberOfVariables,  SimpleMatrix assetMatrix, SimpleMatrix liabMatrix) {
		super(numberOfVariables, 1, 1);
		
		this.wMatrix = assetMatrix;
		this.bMatrix = liabMatrix;
				
	}
	
	public WeightFinderWithDiffMin(int numberOfVariables,  SimpleMatrix assetMatrix, SimpleMatrix liabMatrix, double maxAssetIncrease, double minBound, double maxBound) {
		super(numberOfVariables, 1, 1);
		
		this.wMatrix = assetMatrix;
		this.bMatrix = liabMatrix;
		
		this.maxAssetIncrease = maxAssetIncrease;
		this.minBound = minBound;
		this.maxBound = maxBound;
		
		logger.info("Matrix Asset & liability Sum: {},{}",  assetMatrix.elementSum(), liabMatrix.elementSum());
	}
	
	@Override
	public Solution newSolution() {
		Solution solution = new Solution(numberOfVariables, numberOfObjectives, numberOfConstraints);
		
		for(int i =0; i<numberOfVariables; i++) {
			solution.setVariable(i,  new RealVariable(minBound, maxBound));
//			if(i<= 80 ) {
//				solution.setVariable(i,  new RealVariable(minBound, maxBound));
//			}
//			else {
//				solution.setVariable(i,  new RealVariable(1.0,1.0));
//			}
		}

		return solution;
	}
	 
	
	@Override
	public void evaluate(Solution solution) {
		double[] x = EncodingUtils.getReal(solution);
		
		SimpleMatrix xMatrix  = new SimpleMatrix(numberOfVariables, 1, true, x);
		
//		Matrix(자산 현금흐름 * 비중 - 부채현금흐름)  
		SimpleMatrix adjAssetCfMatrix = wMatrix.mult(xMatrix);
		SimpleMatrix diffCfMatrix  = wMatrix.mult(xMatrix).minus(bMatrix);
		
		
		
		double sum =0.0;
		double totalLiabCf = bMatrix.elementSum();
		double totaAssetCf = wMatrix.elementSum();
		
		double cfRatio  = adjAssetCfMatrix.elementSum() / wMatrix.elementSum() - maxAssetIncrease ;
		
//		현재 운용 자산 규모 총액의  증가규모를 제약조건으로  각 만기의 현금흐름 차이  절대값을  최소화로 하는 최적화 함수로 설정하여  잔존만기별 투자비중 조정치를 산출   
		for( int k =0; k < diffCfMatrix.numRows(); k++) {
			sum = sum + Math.abs(diffCfMatrix.get(k,0));
		}

		solution.setConstraint(0, cfRatio <= 0.0 ? 0.0: cfRatio);
		
		solution.setObjective(0, Math.abs(sum));
//		solution.setObjective(1, adjAssetCfMatrix.elementSum());
		
	}

}

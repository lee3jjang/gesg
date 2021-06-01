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
		
//		Matrix(�ڻ� �����帧 * ���� - ��ä�����帧)  
		SimpleMatrix adjAssetCfMatrix = wMatrix.mult(xMatrix);
		SimpleMatrix diffCfMatrix  = wMatrix.mult(xMatrix).minus(bMatrix);
		
		
		
		double sum =0.0;
		double totalLiabCf = bMatrix.elementSum();
		double totaAssetCf = wMatrix.elementSum();
		
		double cfRatio  = adjAssetCfMatrix.elementSum() / wMatrix.elementSum() - maxAssetIncrease ;
		
//		���� ��� �ڻ� �Ը� �Ѿ���  �����Ը� ������������  �� ������ �����帧 ����  ���밪��  �ּ�ȭ�� �ϴ� ����ȭ �Լ��� �����Ͽ�  �������⺰ ���ں��� ����ġ�� ����   
		for( int k =0; k < diffCfMatrix.numRows(); k++) {
			sum = sum + Math.abs(diffCfMatrix.get(k,0));
		}

		solution.setConstraint(0, cfRatio <= 0.0 ? 0.0: cfRatio);
		
		solution.setObjective(0, Math.abs(sum));
//		solution.setObjective(1, adjAssetCfMatrix.elementSum());
		
	}

}

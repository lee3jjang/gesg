package com.gof.model;

import org.apache.commons.lang3.ArrayUtils;
import org.ejml.simple.SimpleMatrix;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.core.variable.RealVariable;
import org.moeaframework.problem.AbstractProblem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeightFinder extends AbstractProblem{
	private final static Logger logger = LoggerFactory.getLogger("WeightFinder");
	private double[][] weight;
	private double[] b;
	private SimpleMatrix wMatrix ;
	private SimpleMatrix bMatrix ;
	
	public WeightFinder(int numberOfVariables, int numberOfObjectives, int numberOfConstraints) {
		super(numberOfVariables, numberOfObjectives, numberOfConstraints);

	}

	public WeightFinder(int numberOfVariables, int numberOfObjectives, int numberOfConstraints, double[][] weight, double[] b) {
		super(numberOfVariables, numberOfObjectives, numberOfConstraints);
		this.weight = weight;
		this.b = b;

		this.wMatrix = new SimpleMatrix(weight);
		this.bMatrix  = new SimpleMatrix(numberOfVariables, 1, true, b);
		
				
	}
	
	public WeightFinder(int numberOfVariables, int numberOfObjectives, int numberOfConstraints, SimpleMatrix assetMatrix, SimpleMatrix liabMatrix) {
		super(numberOfVariables, numberOfObjectives, numberOfConstraints);
		
		this.wMatrix = assetMatrix;
		this.bMatrix = liabMatrix;
				
	}
	
	@Override
	public Solution newSolution() {
		Solution solution = new Solution(numberOfVariables, numberOfObjectives);
		
		for(int i =0; i<numberOfVariables; i++) {
			solution.setVariable(i,  new RealVariable(-13500.0, 13500.0));
//			if(i < numberOfObjectives) {
//				solution.setVariable(i,  new RealVariable(-13500.0, 13500.0));
//			}else {
//				solution.setVariable(i,  new RealVariable(1.0, 1.0));
//			}
			
		}
		return solution;
	}
	 
	
	@Override
	public void evaluate(Solution solution) {
		double[] x = EncodingUtils.getReal(solution);
		
//		double[] identity = new double[numberOfVariables-numberOfObjectives];
//		for(int k =0; k< numberOfVariables-numberOfObjectives; k++) {
//			identity[k] = 1.0;
//		}
//		
//		double[] xx= ArrayUtils.addAll(x, identity);
		
		SimpleMatrix xMatrix  = new SimpleMatrix(numberOfVariables, 1, true, x);
		
//		logger.info("matrix : {},{}", wMatrix.numCols(), xMatrix.numRows() );
		
		SimpleMatrix cc = wMatrix.mult(xMatrix).minus(bMatrix);
		
//		SimpleMatrix cc = wMatrix.mult(xMatrix).elementDiv(bMatrix);
		
		
		for( int k =0; k < numberOfObjectives; k++) {
			solution.setObjective(k, Math.abs(cc.get(k,0)));
			
		}
		
//		System.out.println(wMatrix);
//		System.out.println(bMatrix);
//		System.out.println(xMatrix);
//		System.out.println(wMatrix.mult(xMatrix));
//		System.out.println(cc);
		
//		f1 = Math.abs(cc.get(0,0));
//		f2 = Math.abs(cc.get(1,0));
		
//		f1 = f1 + weight[0][0]* x[0] + weight[0][1]* x[1] - b[0];  
//		f2 = f2 + weight[1][0]* x[0] + weight[1][1]* x[1] - b[1];
		
//		solution.setObjective(0, Math.abs(f1));
//		solution.setObjective(1, Math.abs(f2));
		
	}

}

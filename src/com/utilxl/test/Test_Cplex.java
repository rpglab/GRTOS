package com.utilxl.test;

//import mpi.MPI;
import ilog.concert.*;
import ilog.cplex.*;

/**
 * 
 * Initialized on Nov.3rd, 2014. 
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class Test_Cplex {
	
	
	
	
//	Internally, they are all represented as IloRange objects
//	with appropriate choices of bounds, which is why all these methods return IloRange objects
	public static void main(String args[])
	{
//		MPI.Init(args);
//		System.out.println("test here.. \n");
//		if ( args.length != 1 || args[0].charAt(0) != '-' ) {return;}
//		System.out.println("test here 2.. \n");

//		example 1.
		try {
			IloCplex testCplex= new IloCplex();
			boolean isProblemIP = true;
			IloNumVarType type = IloNumVarType.Float;
			if (isProblemIP == true)
			{
				type = IloNumVarType.Int;
			}
			IloNumVar[] vars = testCplex.numVarArray(2, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, type);
			

//			IloNumExpr expr = testCplex.sum(testCplex.prod(0.0, vars[0]), testCplex.prod(1.0, vars[1]));
			IloLinearNumExpr expr = testCplex.linearNumExpr();  // an extension to linearNumExpr.
			expr.addTerm(0, vars[0]);  // addTerm method is expecting a coefficient and a variable not an Expr.
			expr.addTerm(1, vars[1]);
			
			testCplex.addMaximize(expr); 
//			testCplex.addMinimize(expr);   // == the following two lines.
//			IloObjective obj = testCplex.minimize(expr);
//			testCplex.add(obj);
			
//			addLe(a,b) : a<=b
//		    addEq(a,b) : a = b
			testCplex.addLe(testCplex.sum(testCplex.negative(vars[0]), vars[1]), 1);
			testCplex.addLe(testCplex.sum(testCplex.prod(3, vars[0]), testCplex.prod(2, vars[1])), 12);
			testCplex.addLe(testCplex.sum(testCplex.prod(2, vars[0]), testCplex.prod(3, vars[1])), 12);

			testCplex.solve();
//			testCplex.solveRelaxed();
			testCplex.getStatus();
			testCplex.getCplexStatus();
			
			double objval = testCplex.getObjValue();
			double[] xval = testCplex.getValues(vars); 
			System.out.println("\nObjetive value: "+objval);
			System.out.println("x is :"+xval[0]);
			System.out.println("y is :"+xval[1]);
			System.out.println(" Example source: http://en.wikipedia.org/wiki/Integer_programming ");
			System.out.println("***  The first problem is done here..  ***\n");
			
//			calling cplex.end to free the memory used by the model object.
			testCplex.end();
			
		} catch (IloException e) {
            System.out.println("Concert exception caught: " + e);
			e.printStackTrace();
		}
		

//		example 2:
//		Maximize x1 + 2x2 + 3x3 
//		subject to -x1 + x2 + x3 <= 20 
//		x1 - 3x2 + x3 <= 30 
//		with these bounds 0 <= x1 <= 40 
//						  0 <= x2 
//					      0 <= x3
		try {
			 IloCplex cplex = new IloCplex(); 
			 
			 double[] lb = {0.0, 0.0, 0.0}; 
			 double[] ub = {40.0, Double.MAX_VALUE, Double.MAX_VALUE}; 
			 IloNumVar[] x = cplex.numVarArray(3, lb, ub); 
			 double[] objvals = {1.0, 2.0, 3.0}; 
			 
//			 IloCplex.scalProd, that forms a scalar product using an array
//			 of objective coefficients times the array of variables
			 cplex.addMaximize(cplex.scalProd(x, objvals)); 
			 
			 cplex.addLe(cplex.sum(cplex.prod(-1.0, x[0]), 
			 cplex.prod( 1.0, x[1]), 
			 cplex.prod( 1.0, x[2])), 20.0); 
			 
//			 cplex.addLe(cplex.sum(cplex.prod( 1.0, x[0]), 
//			 cplex.prod(-3.0, x[1]), 
//			 cplex.prod( 1.0, x[2])), 30.0); 
			 
			 IloNumExpr tmp = cplex.sum(cplex.prod( 1.0, x[0]),  // these four lines are equivalent to the above three lines
					 cplex.prod(-3.0, x[1]));
			 tmp = cplex.sum(tmp, cplex.prod( 1.0, x[2]));
			 cplex.addLe(tmp, 30.0); 
			 			 
//			 cplex.setOut(null);
			 if ( cplex.solve() ) 
			 {
				 cplex.output().println("Solution status = " + cplex.getStatus()); 
				 cplex.output().println("Solution value = " + cplex.getObjValue());
				 double[] val = cplex.getValues(x); 
				 int ncols = cplex.getNcols(); 
				 for (int j = 0; j < ncols; ++j) 
				     cplex.output().println("Column: " + j + " Value = " + val[j]); 
				 
//				 Write model to file
//				 cplex.exportModel("lpex1.lp");

				 // testing, fail to pass compiler yet.
//				 cplex.output().WriteLine("Solution value = " + cplex.getObjValue());
//				 double[] x1 = cplex.getValues(x[0]);
//				 double[] dj = cplex.getReducedCosts(x1[0]);
//				 double[] pi = cplex.getDuals(rng[0]);
//				 double[] slack = cplex.getSlacks(rng[0]);
			 }
			 cplex.end();
		} catch (IloException e) {
			System.err.println("Concert exception '" + e + "' caught"); 
		}
		
				
//		MPI.Finalize();

	}

}

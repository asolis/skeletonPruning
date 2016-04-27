/**************************************************************************************************
 **************************************************************************************************

 BSD 3-Clause License (https://www.tldrlegal.com/l/bsd3)

 Copyright (c) 2012 Andrés Solís Montero <http://www.solism.ca>, All rights reserved.


 Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.
 3. Neither the name of the copyright holder nor the names of its contributors
 may be used to endorse or promote products derived from this software
 without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

      
If you find this code useful in your research/software, please consider citing the following publication:

Andrés Solís Montero and Jochen Lang. "Skeleton pruning by contour approximation and the 
integer medial axis transform". Computers & Graphics, Elsevier, 2012. 

 (http://www.sciencedirect.com/science/article/pii/S0097849312000684)
 
**************************************************************************************************
**************************************************************************************************/
package util.CurveFitting;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;

public class LeastSquareLine extends Interpolation {

		
		public LeastSquareLine(ArrayList<Point2D> pts_, LinkedList<Integer> index) throws CurveCreationException{
			setData(pts_, index);
		}
		protected void compute(){
			//no need for approximation right now
			
//			this.cP = new Point2D[(N()-1)];
//			for (int i = 0; i < N()-1; i ++){
//				cP[2*i]     = A(getIndex(i),getIndex(i+1));
//			}	
		}	

		// Least square estimation of a ( y  = ax + b)
		// A = (sum(y)*sum(x^2)-sum(x)sum(x*y))/ (n*sum(x^2)-sum(x)^2);
		// B = (n*sum(x*y)-sum(x)*sum(y))/(n*sum(x^2)-sum(x)^2);
		private Point2D A(int init,int end){
			
			double A = sumY(init,end)*sumXSq(init,end);
				   A -= sumX(init,end)*sumXY(init,end);
			double den= (end-init+1)*sumXSq(init,end)-Math.pow(sumX(init,end), 2.0);
				   A /= den;
			double B = (end-init+1)*sumXY(init,end);
				   B -= sumX(init,end)*sumY(init,end);
				   B /= den;	   
			 
			return new Point2D.Double(A,B);		
		}
	
		private double sumX(int init,int end){
			int sum =0;
			for (int i = init; i <= end; i++){
				sum+=points.get(i).getX();
			}
			return sum;
		}
		private double sumXY(int init,int end){
			int sum =0;
			for (int i = init; i <= end; i++){
				sum+=points.get(i).getX()*points.get(i).getY();
			}
			return sum;
		}
		private double sumY(int init,int end){
			int sum =0;
			for (int i = init; i <= end; i++){
				sum+=points.get(i).getY();
			}
			return sum;
		}
		private double sumXSq(int init,int end){
			int sum =0;
			for (int i = init; i <= end; i++){
				sum+=Math.pow(points.get(i).getX(),2.0);
			}
			return sum;
		}
		private double sumYSq(int init,int end){
			int sum =0;
			for (int i = init; i <= end; i++){
				sum+=Math.pow(points.get(i).getY(),2.0);
			}
			return sum;
		}

		
		public Line2D getLineAt(int i){
			if (i < 0 || i >= N() - 1 ) throw 
			new IndexOutOfBoundsException(
					 String.format("Interpolation Class: cannot " +
					 		       "retrieve line with index : %d" , i));
			Line2D.Double line = new Line2D.Double(
														  get(i).getX(),
														  get(i).getY(),
														  get(i+1).getX(), 
									                      get(i+1).getY());
			return line;
		}
		
		public ArrayList<Shape> getCurves(){
	    	  ArrayList<Shape> s = new ArrayList<Shape>();
	    	
	    	  for (int i = 0; i < N()-1; i++)
	    	  {        	
	    		  Line2D.Double line = new Line2D.Double(get(i).getX(),
	    				  								  get(i).getY(),
	    				                                  get(i+1).getX(), 
	    				                                  get(i+1).getY());
	    		  s.add(line);
	    	  }	    	  
	            
	          return s;
	    }
		
}

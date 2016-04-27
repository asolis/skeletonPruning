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

package util.Graphics;

import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import util.CubicRoots;
import util.Tuple;

public class LineCurveIntersection {	
	public LineCurveIntersection(){
		
	}
	
	public static Tuple.Tuple2<Point2D.Double, java.lang.Double>[] intersection (CubicCurve2D _c, Line2D _l) {
		return intersection(_c, _l, true);
	}
	
	/**
	 * Gets intersection points for a curve.
	 * @param _c Cubic curve
	 * @param _l Line
	 * @return Intersection points and corresponding T values.
	 */
	public static Tuple.Tuple2<Point2D.Double, java.lang.Double>[] intersection (CubicCurve2D _c, Line2D _l, boolean constrainSolution){
		@SuppressWarnings("unchecked") // java cannot instantiate generic arrays
		Tuple.Tuple2<Point2D.Double, java.lang.Double>[] result = new Tuple.Tuple2[3];
		
		double[] sol =  new double [3];
		double [] lineEq = new double [3]; // [A B C]
		double [] cX = new double [4];     // [ax bx cx dx];
		double [] cY = new double [4];     // [ay by cy dy];
		double [] l_c_eq = new double[4];  // Line-curve equation;
		//double [] sol = new double [3];
		
		//lineEq = lineEquation(_l);
		lineEq = lineEquation(_l);
		cX = curveX(_c); //_c
		cY = curveY(_c); // _c
		l_c_eq = line_Curve(cX,cY,lineEq);
		int n = CubicRoots.SolveCubic (l_c_eq ,sol);
		//Point2D [] p = new Point2D [n];
		//setSolution(sol);
		
		for (int i =0; i<n; i++){
		    double s_s = sol[i]*sol[i];
			double s_c = sol[i] * s_s;
			
			if((sol[i]>=0 && sol[i]<=1) || !constrainSolution){
				double x = (cX[0]*(s_c))+ (cX[1]*s_s) + (cX[2]*sol[i]) + cX[3];
				double y = (cY[0]*(s_c))+ (cY[1]*s_s) + (cY[2]*sol[i]) + cY[3];
			    result[i] = Tuple.create(new Point2D.Double(x,y), sol[i]);
			}else{
				result[i] = null;
			}		    
		}
		return result;
		
	}
	
	public static double[] lineEquation (Line2D L){
		
		double A = L.getY2()-L.getY1();
		double B = - (L.getX2()-L.getX1());
		double C = (L.getY1()*L.getX2()) -(L.getX1()*L.getY2());
		
		double [] le = new double[3];
		le[0] = A;
		le[1] = B;
		le[2] = C;
		
		return le;
	}
	
	private static double [] curveX(CubicCurve2D c){
		double [] x = new double[4];
		
		double ax = (c.getX2()-c.getX1())+ 3*(c.getCtrlX1()-c.getCtrlX2()); 
		double bx = (3*(c.getCtrlX2()-c.getCtrlX1())) - (3*(c.getCtrlX1()-c.getX1()));
		double cx = 3*(c.getCtrlX1()-c.getX1());
		double dx = c.getX1();
		
		x[0] = ax;
		x[1] = bx;
		x[2] = cx;
		x[3] = dx;
		
		return x;
	}
	
	private static double [] curveY(CubicCurve2D c){
		double [] y = new double[4];
		
		double ay = (c.getY2()-c.getY1())+ 3*(c.getCtrlY1()-c.getCtrlY2()); 
		double by = (3*(c.getCtrlY2()-c.getCtrlY1())) - (3*(c.getCtrlY1()-c.getY1()));
		double cy = 3*(c.getCtrlY1()-c.getY1());
		double dy = c.getY1();
		
		y[0] = ay;
		y[1] = by;
		y[2] = cy;
		y[3] = dy;
		
		return y;
	}

    private static double []  line_Curve (double [] cx, double [] cy, double [] line){
    	
    	double[] lc = new double[4];
    	
    	double l3 = (line[0]*cx[0]) + (line[1]*cy[0]);
    	double l2 = (line[0]*cx[1]) + (line[1]*cy[1]);
    	double l1 = (line[0]*cx[2]) + (line[1]*cy[2]);
    	double l0 = line[2] + (line[0]*cx[3]) + (line[1]*cy[3]);
    	
    	lc[0] = l0;
    	lc[1] = l1;
    	lc[2] = l2;
    	lc[3] = l3;
    	
    	return lc;
    }
    
    public static double rounding(double n){
     int factor = 10000;
     int sr = (int)(n*factor+0.5);
     double rounded = (double) sr/factor;
    	
     return rounded;	
    	
    }
    
    /**TEST**/
	//public static void main(String[] args){
		//Line2D L = new Line2D.Double(408, 173, 379, 166);
		//CubicCurve2D c = new CubicCurve2D.Double(386.2882,192.0153,389.6457,177.3417,393.6871,159.8703,
		//		                                 398.4125,139.6008);
				
		//Point2D [] n = new LineCurveIntersection().intersection(c,L);
		//for(int i=0; i<n.length; i++){
		// System.out.print(n[i].getX()+ ","+ n[i].getY());
		//} 
	//}
}


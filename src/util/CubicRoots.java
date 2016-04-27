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
package util;

import java.awt.geom.Point2D;
import java.util.ArrayList;

/** Functions to find cubic roots.
 * Taken from "Graphic Gems", Academic Press,1990
 * "Cubic and Quadric Roots"
 * @author Jochen Schwarze
 *
 *The coefficients are passed like this:
 *       c[0]+c[1]*x+c[2]*x^2+c[3]*x^3 = 0
 */

public class CubicRoots {

	 private static final double EQN_EPS = 1e-9;
	 private static final double PI = Math.PI;
	 protected static double [] c = new double[4];  //Coefficients
	 protected static double [] s = new double[3];  //Solutions
	 
	private CubicRoots(){
		
	}
	
	
    private static boolean IsZero(double x){
		return ((x > -EQN_EPS) && (x < EQN_EPS));
	}
	
	public static int SolveCubic(double _c[],double s[]){
		int num;
		double sub;
		double A,B,C ;
		double sq_A,p,q;
		double cb_p,D;
		
		// normal form: x^3 + AX^2 + Bx + C = 0
		
		A = _c[2]/_c[3];
		B = _c[1]/_c[3];
		C = _c[0]/_c[3];
		
		// Substitute x = y -A/3 to eliminate quadratic term:
		// x^3 + px + q = 0
		
		sq_A = A*A;
		p    = (1.0/3)* (-1.0/3 * sq_A + B);
		q    = (1.0/2)* (2.0/27 * A * sq_A - 1.0/3 * A * B + C);
		   
		// Use Cardano's formula
		   
		   cb_p = p*p*p;
		   D    = (q*q) + cb_p;
		   
		   if (IsZero(D)){
			   if(IsZero(q)){
				   s[0] = 0;
				   num = 1;
			   }else{
				   double u = Math.cbrt(-q);
				   s[0] = 2*u;
				   s[1] = -u;
				   num =2;
			   }
		   }
		   else if (D < 0){   //Casus irreducibilis: three real solutions
			   double phi = 1.0/3 * Math.acos(-q/Math.sqrt(-cb_p));
			   double t   = 2 * Math.sqrt(-p);
			   
			   s[0] =  t * Math.cos(phi);
			   s[1] = -t * Math.cos(phi + Math.PI/3);
			   s[2] = -t * Math.cos(phi - Math.PI/3);
			   num = 3;
		   
		   } else {    //one real solution
			   
			   double sqrt_D =  Math.sqrt(D);
			   double u      =  Math.cbrt(sqrt_D - q);
			   double v      = -Math.cbrt(sqrt_D + q);
			   s[0] = u+v;
			   num =1;
			   
			  }
		   
		   /*Resubstitute*/
		   sub = 1.0/3 * A;
		   for(int i =0; i < num; ++i){
			   s[i] -= sub;
		   }
	    return num;
	
	}
	
	/**TEST**/
	public static void main(String[] args){
		c[0] = -53.1115;
		c[1] = 67.1571;
		c[2] = 30.1011;
		c[3] = -.0004;
		
		int n = SolveCubic(c,s);
	}


}

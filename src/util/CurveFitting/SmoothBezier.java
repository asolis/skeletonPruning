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

 **************************************************************************************************
 **************************************************************************************************/
package util.CurveFitting;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;


public class SmoothBezier extends Interpolation
{
		//First derivative and second derivative are equals.
		public SmoothBezier(){
			cP = new Point2D[0];
			points = new ArrayList<Point2D>();
			index  = new LinkedList<Integer>();
		}
		
		public SmoothBezier(ArrayList<Point2D> points,
						    LinkedList<Integer> index) throws CurveCreationException
		{			
			setData(points, index);
		}
		
		protected void compute() {				
			int n = N()-1;
			if (n == 1)
			{ 
				cP  = new Point2D[2];
				// 3P1 = 2P0 + P3
				cP[0] = new Point2D.Double(
						                (2 * get(0).getX() + get(1).getX()) / 3,
						                (2 * get(0).getY() + get(1).getY()) / 3);
				// P2 = 2P1 ñ P0
				cP[1] = new Point2D.Double(
						                 (2 * cP[0].getX() - get(0).getX()),
						                 (2 * cP[0].getY() - get(0).getY()));
				return;
			}

			cP = new Point2D[2*n];
			cP[0] = new Point2D.Double(get(0).getX()+2*get(1).getX(),
									   get(0).getY()+2*get(1).getY());
			for (int i = 1; i < n-1 ; ++i ){
				cP[2*i]= new Point2D.Double(4 * get(i).getX() + 2 * get(i+1).getX(),
											4 * get(i).getY() + 2 * get(i+1).getY());
			}
			cP[2*(n-1)] = new Point2D.Double((8 * get(n-1).getX() + get(n).getX()) / 2.0,
											 (8 * get(n-1).getY() + get(n).getY()) / 2.0 );
			
			//Compute first right end points
			getControlPoints(cP);
			for (int i = 0; i < n; ++i)
			{
				if (i < n - 1)
					cP[2*i+1] = new Point2D.Double(2 * get(i+1).getX() - cP[2*(i + 1)].getX(),
											       2 * get(i+1).getY() - cP[2*(i + 1)].getY());
				else
					cP[2*i+1] = new Point2D.Double((get(n).getX() + cP[2*(n - 1)].getX()) / 2,
												   (get(n).getY() + cP[2*(n - 1)].getY()) / 2);
			}
			
		}
		
		private void getControlPoints(Point2D[] data){
			int n        = data.length/2;
			double[] tmp = new double[n];
			double     b = 2.0;
			data[0].setLocation(data[0].getX() / b,
					            data[0].getY() / b);
			for (int i = 1; i < n; i++){
				tmp[i]    = 1 / b;
				b         = ( i < n-1 ? 4.0 : 3.5) - tmp[i];
				data[2*i].setLocation( (data[2*i].getX() - data[2*(i-1)].getX())/b,
									   (data[2*i].getY() - data[2*(i-1)].getY())/b);
			}
			for (int i = 1; i < n; i ++){
				
				data[2*(n-i-1)].setLocation(data[2*(n-i-1)].getX() - tmp[n-i] * data[2*(n-i)].getX(),
											data[2*(n-i-1)].getY() - tmp[n-i] * data[2*(n-i)].getY());
			}
		}

		
}


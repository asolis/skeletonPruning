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
        /**
         * Interpolates given points by a bezier curve. The first
         * and the last two points are interpolated by a quadratic
         * bezier curve; the other points by a cubic bezier curve.
         * 
         * Let p a list of given points and b the calculated bezier points,
         * then one get the whole curve by: (This is not like this anymore after Andres S. Montero modifications).
         * 
         * sharedPath.moveTo(p[0])
         * sharedPath.quadTo(b[0].x, b[0].getY(), p[1].x, p[1].getY());
         * 
         * for(int i = 2; i < p.length - 1; i++ ) {
         *    Point b0 = b[2*i-3];
         *	  Point b1 = b[2*i-2];
         *	  sharedPath.curveTo(b0.x, b0.getY(), b1.x, b1.getY(), p[i].x, p[i].getY());
         * }
         * 
         * sharedPath.quadTo(b[b.length-1].x, b[b.length-1].getY(), p[n - 1].x, p[n - 1].getY());
         * 
         * @author krueger
         * @Modified by Andres Solis Montero 
         * 			   Changed data structures used in the algorithm
         * 			   Added CubicCurve2D, QuadCurve2D for displaying
         * 			   Added knots indexes (for efficiently curve fitting).
         * 			   Added missing special cases when there is only two points
         * 			   All the beziers are represented as CubicCurve
         * 			   Changed the interface to retrieve the curves.
         * 			   Moved the code from the constructor to compute(), 
         * 					this way every time we change the indexes the curve is interpolated again
         * 			   Removed all the temp variables inside the cycle, so the space in memory is 
         * 			   		allocated only once.
         */
package util.CurveFitting;


       
import java.awt.Point;
import java.awt.geom.Point2D;

import java.util.ArrayList;
import java.util.LinkedList;



        public class Bezier extends Interpolation {

            private static final float AP = 0.5f;
           
    		public Bezier(){
    			cP = new Point2D[0];
    			points = new ArrayList<Point2D>();
    			index  = new LinkedList<Integer>();
    		}
            
            public Bezier(ArrayList<Point2D> pts, LinkedList<Integer> idxs) throws CurveCreationException {
            		setData(pts, idxs);
            }

            protected void compute(){
            	 int n = N();
            	 if (n == 2) {
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
                 
                 cP = new Point2D[2 * n -2];
                 double paX, paY;
                 double pbX = get(0).getX();
                 double pbY = get(0).getY();
                 double pcX = get(1).getX();
                 double pcY = get(1).getY();
                 double p2X = 0, p2Y=0, 
                 		abX, abY,
                 		acX, acY,
                 		lac,
                 		proj,
                 		apX, apY,
                 		p1X, p1Y,
                 		cbX, cbY;
                 
                 for (int i = 1; i < n - 1; i++) {
                     paX = pbX;
                     paY = pbY;
                     pbX = pcX;
                     pbY = pcY;
                     pcX = get(i+1).getX();
                     pcY = get(i+1).getY();
                     abX = pbX - paX;
                     abY = pbY - paY;
                     acX = pcX - paX;
                     acY = pcY - paY;
                     lac = Math.sqrt(acX * acX + acY * acY);
                     acX = acX / lac;
                     acY = acY / lac;

                     proj = abX * acX + abY * acY;
                     proj = proj < 0 ? -proj : proj;
                     apX = proj * acX;
                     apY = proj * acY;

                     p1X = pbX - AP * apX;
                     p1Y = pbY - AP * apY;
                     if (i == 1)
                     	cP[0] = new Point((int) p1X, (int) p1Y);
                     cP[2*i-1] = new Point((int) p1X, (int) p1Y);

                     acX = -acX;
                     acY = -acY;
                     cbX = pbX - pcX;
                     cbY = pbY - pcY;
                     proj = cbX * acX + cbY * acY;
                     proj = proj < 0 ? -proj : proj;
                     apX = proj * acX;
                     apY = proj * acY;

                     p2X = pbX - AP * apX;
                     p2Y = pbY - AP * apY;
                     cP[2*i] = new Point((int) p2X, (int) p2Y);
                 }
                 cP[cP.length-1]= new Point((int) p2X, (int) p2Y);
            }

	       

        }

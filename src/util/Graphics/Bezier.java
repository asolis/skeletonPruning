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
package util.Graphics;
       

        import java.awt.Color;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;

        /**
         * Interpolates given points by a bezier curve. The first
         * and the last two points are interpolated by a quadratic
         * bezier curve; the other points by a cubic bezier curve.
         * 
         * Let p a list of given points and b the calculated bezier points,
         * then one get the whole curve by:
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
         * Modified by Andres Solis Montero
         */
        public class Bezier {

            private static final float AP = 0.5f;
            private ArrayList<Point2D> bPoints;
            private ArrayList<Point2D> oPoints;

            /**
             * Creates a new Bezier curve.
             * @param points
             */
            public Bezier(ArrayList<Point2D> points) {
                int n = points.size();
                if (n < 3) {
                    // Cannot create bezier with less than 3 points
                    return;
                }
                oPoints = points;
                bPoints = new ArrayList<Point2D>(2 * (n - 2));
                double paX, paY;
                double pbX = points.get(0).getX();
                double pbY = points.get(0).getY();
                double pcX = points.get(1).getX();
                double pcY = points.get(1).getY();
                for (int i = 0; i < n - 2; i++) {
                    paX = pbX;
                    paY = pbY;
                    pbX = pcX;
                    pbY = pcY;
                    pcX = points.get(i+2).getX();
                    pcY = points.get(i+2).getY();
                    double abX = pbX - paX;
                    double abY = pbY - paY;
                    double acX = pcX - paX;
                    double acY = pcY - paY;
                    double lac = Math.sqrt(acX * acX + acY * acY);
                    acX = acX / lac;
                    acY = acY / lac;

                    double proj = abX * acX + abY * acY;
                    proj = proj < 0 ? -proj : proj;
                    double apX = proj * acX;
                    double apY = proj * acY;

                    double p1X = pbX - AP * apX;
                    double p1Y = pbY - AP * apY;
                    bPoints.add(2*i,new Point((int) p1X, (int) p1Y) );

                    acX = -acX;
                    acY = -acY;
                    double cbX = pbX - pcX;
                    double cbY = pbY - pcY;
                    proj = cbX * acX + cbY * acY;
                    proj = proj < 0 ? -proj : proj;
                    apX = proj * acX;
                    apY = proj * acY;

                    double p2X = pbX - AP * apX;
                    double p2Y = pbY - AP * apY;
                    bPoints.add(2*i+1, new Point((int) p2X, (int) p2Y));
                }
            }

            /**
             * Returns the calculated bezier points.
             * @return the calculated bezier points
             */
            public ArrayList<Point2D> getPoints() {
                return bPoints;
            }
            
            
            public ArrayList<Shape> getCurves(){
            	  ArrayList<Shape> s = new ArrayList<Shape>();
            	  QuadCurve2D.Double  f = new QuadCurve2D.Double(oPoints.get(0).getX(),
            			  										 oPoints.get(0).getY(),
            			  										 bPoints.get(0).getX(),
            			  										 bPoints.get(0).getY(),
            			  										 oPoints.get(1).getX(),
            			  										 oPoints.get(1).getY());
            	  s.add(f);
            	  
            	  for (int i = 2; i < oPoints.size() - 1; i++){
                	  java.awt.Point b0 = (java.awt.Point) bPoints.get(2*i-3);
                	  java.awt.Point b1 = (java.awt.Point) bPoints.get(2*i-2);
            		  CubicCurve2D.Double cubic = new CubicCurve2D.Double(oPoints.get(i-1).getX(),
            				  											  oPoints.get(i -1).getY(),
            				  											  b0.x, 
            				                                              b0.getY(),
            				                                              b1.x, 
            				                                              b1.getY(), 
            				                                              oPoints.get(i).getX(), 
            				                                              oPoints.get(i).getY());
            		  s.add(cubic);
            		  }
            	  
            	  QuadCurve2D.Double  fn = new QuadCurve2D.Double(oPoints.get(oPoints.size()-2).getX(),
            			  										 oPoints.get(oPoints.size()-2).getY(),
            			  										 bPoints.get(bPoints.size()-1).getX(),							                                        
            			                                          bPoints.get(bPoints.size()-1).getY(),
							  								 	  oPoints.get(oPoints.size()-1).getX(),
            			  										  oPoints.get(oPoints.size()-1).getY()
            			  										  );
            	 
                     s.add(fn);
                     
                     return s;
            }
            

            /**
             * Returns the number of bezier points.
             * @return number of bezier points
             */
            public int getPointCount() {
                return bPoints.size();
            }

            /**
             * Returns the bezier points at position i.
             * @param i
             * @return the bezier point at position i
             */
            public Point2D getPoint(int i) {
                return bPoints.get(i);
            }

        }

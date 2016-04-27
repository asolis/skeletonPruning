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
  /***
    * Calculates the nearest point on a straight line or on a cubic bezier curve.
    *
    * Calculations for the Bezier parts come from:
   * "Solving the Nearest Point-on-Curve Problem" and "A Bezier Curve-Based Root-Finder"
   * by Philip J. Schneider from "Graphics Gems", Academic Press, 1990.
   *
   * @author Mark Donszelmann
   * @Modified: Andres Solis Montero. (Fixed Problem with JavaNullPointerExceptions when computing closest point on Curve).
   * @version $Id: NearestPoint.java 3161 2012-06-08 22:20:19Z cedmu020 $
   */ 
package util.CurveFitting;

   // Copyright 2004, FreeHEP.
 
   import java.awt.geom.*;

import util.Tuple;
import util.Tuple.Tuple2;
import util.Tuple.Tuple3;
   
 
  public class NearestPoint {
      
      private static final int MAXDEPTH = 64;                                 // Maximum depth for recursion
      private static final double EPSILON  = 1.0 * Math.pow(2, -MAXDEPTH-1);  // Flatness control value
      private static final int DEGREE = 3;                                    // Cubic Bezier curve
      private static final int W_DEGREE = 5;                                  // Degree of eqn to find roots of

      public NearestPoint() {
      }
  
      /***
       * Returns the nearest point (pn) on line p1 - p2 nearest to point pa.
       *
       * @param p1 start point of line
       * @param p2 end point of line
       * @param pa arbitrary point
       * @param pn nearest point (return param)
       * @return distance squared between pa and nearest point (pn)
       */ 
      public static double onLine(Point2D p1, Point2D p2, Point2D pa, Point2D pn) {
    	  if ( pn == null )
    		  pn = new Point2D.Double();
          double dx = p2.getX() - p1.getX();
          double dy = p2.getY() - p1.getY();
          double dsq = dx*dx + dy*dy;
          if (dsq == 0) {
              pn.setLocation(p1);
          } else {
              double u = ((pa.getX()-p1.getX())*dx + (pa.getY()-p1.getY())*dy)/dsq;
              if (u <= 0) {
                  pn.setLocation(p1);
              } else if (u >= 1) {
                  pn.setLocation(p2);
              } else {
                  pn.setLocation(p1.getX() + u*dx, p1.getY() + u*dy);
              }
          }
          return pn.distanceSq(pa);
      }
      
      /**
       * Gets the nearest point on the curve
       * @param c Curve
       * @param pa Arbitrary point to test
       * @param pn The nearest point (return value)
       * @return Distance squared (u), T-value at curve point (v)
       */
      public static Tuple2<Double, Double> onCurve(CubicCurve2D c, Point2D pa, Point2D pn) {
          double[] tCandidate = new double[W_DEGREE];     // Possible roots
          Point2D[] v = {
              c.getP1(), c.getCtrlP1(), c.getCtrlP2(), c.getP2()
          };
  
          // Convert problem to 5th-degree Bezier form
          Point2D[] w = convertToBezierForm(v, pa);
  
          // Find all possible roots of 5th-degree equation
          int nSolutions = findRoots(w, W_DEGREE, tCandidate, 0);
  
          // Compare distances of P5 to all candidates, and to t=0, and t=1
          // Check distance to beginning of curve, where t = 0
          double minDistance = pa.distanceSq(c.getP1());
          double t = 0.0;
  
          // Find distances for candidate points
          for (int i = 0; i < nSolutions; i++) {
              Point2D p = bezier(v, DEGREE, tCandidate[i], null, null);
              double distance = pa.distanceSq(p);
              if (distance < minDistance) {
                  minDistance = distance;
                  t = tCandidate[i];
              }
          }
  
          // Finally, look at distance to end point, where t = 1.0
          double distance = pa.distanceSq(c.getP2());
          if (distance < minDistance) {
              minDistance = distance;
              t = 1.0;
          }
  
  
          //  Return the point on the curve at parameter value t
         pn.setLocation(bezier(v, DEGREE, t, null, null));
         return Tuple.create(minDistance, t);
      }
      
      /**
       * Gets the nearest point on the curve
       * @param c Curve
       * @param pa Arbitrary point to test
       * @return Distance squared (u), nearest point on curve (v), T-value at curve point (x)
       */
      public static Tuple3<Double, Point2D, Double> onCurve(CubicCurve2D c, Point2D pa) {
    	  Point2D pn = new Point2D.Double();

    	  Tuple2<Double, Double> res = onCurve(c, pa, pn);
 
    	  return Tuple.create(res.u, pn, res.v);
      }
      
      /***
       * Return the nearest point (pn) on cubic bezier curve c nearest to point pa.
       *
       * @param c cubice curve
       * @param pa arbitrary point
       * @param pn nearest point found (return param)
       * @return distance squared between pa and nearest point (pn)
       */    
      //public static double onCurve(CubicCurve2D c, Point2D pa, Point2D pn) {   
    	//  return onCurveT(c, pa, pn).u;
      //}
     
     /***
      *  FindRoots :
      *  Given a 5th-degree equation in Bernstein-Bezier form, find
      *  all of the roots in the interval [0, 1].  Return the number
      *  of roots found.
      */
     private static int findRoots(Point2D[] w, int degree, double[] t, int depth) {  
 
         switch (crossingCount(w, degree)) {
             case 0 : { // No solutions here
                 return 0;   
             }
             case 1 : { // Unique solution
                 // Stop recursion when the tree is deep enough
                 // if deep enough, return 1 solution at midpoint
                 if (depth >= MAXDEPTH) {
                     t[0] = (w[0].getX() + w[W_DEGREE].getX()) / 2.0;
                     return 1;
                 }
                 if (controlPolygonFlatEnough(w, degree)) {
                     t[0] = computeXIntercept(w, degree);
                     return 1;
                 }
                 break;
             }
         }
 
         // Otherwise, solve recursively after
         // subdividing control polygon
         Point2D[] left = new Point2D.Double[W_DEGREE+1];    // New left and right
         Point2D[] right = new Point2D.Double[W_DEGREE+1];   // control polygons
         double[] leftT = new double[W_DEGREE+1];            // Solutions from kids
         double[] rightT = new double[W_DEGREE+1];
         
         bezier(w, degree, 0.5, left, right);
         int leftCount  = findRoots(left,  degree, leftT, depth+1);
         int rightCount = findRoots(right, degree, rightT, depth+1);
     
         // Gather solutions together
         for (int i = 0; i < leftCount; i++) {
             t[i] = leftT[i];
         }
         for (int i = 0; i < rightCount; i++) {
             t[i+leftCount] = rightT[i];
         }
     
         // Send back total number of solutions  */
         return leftCount+rightCount;
     }
 
     private static final double[][] cubicZ = {  
         /* Precomputed "z" for cubics   */
         {1.0, 0.6, 0.3, 0.1},
         {0.4, 0.6, 0.6, 0.4},
         {0.1, 0.3, 0.6, 1.0},
     };
 
     /***
      *  ConvertToBezierForm :
      *      Given a point and a Bezier curve, generate a 5th-degree
      *      Bezier-format equation whose solution finds the point on the
      *      curve nearest the user-defined point.
      */
     private static Point2D[] convertToBezierForm(Point2D[] v, Point2D pa) {
 
         Point2D[]   c = new Point2D.Double[DEGREE+1];   // v(i) - pa
         Point2D[]   d = new Point2D.Double[DEGREE];     // v(i+1) - v(i)
         double[][]  cdTable = new double[3][4];         // Dot product of c, d
         Point2D[]   w = new Point2D.Double[W_DEGREE+1]; // Ctl pts of 5th-degree curve
 
         // Determine the c's -- these are vectors created by subtracting
         // point pa from each of the control points
         for (int i = 0; i <= DEGREE; i++) {
             c[i]  = new Point2D.Double(v[i].getX() - pa.getX(), v[i].getY() - pa.getY());
         }
 
         // Determine the d's -- these are vectors created by subtracting
         // each control point from the next
         double s = 3;
         for (int i = 0; i <= DEGREE - 1; i++) {       
             d[i]  = new Point2D.Double(s * (v[i+1].getX() - v[i].getX()), s * (v[i+1].getY() - v[i].getY()));
         }
         
         // Create the c,d table -- this is a table of dot products of the
         // c's and d's                          */
         for (int row = 0; row <= DEGREE - 1; row++) {
             for (int column = 0; column <= DEGREE; column++) {
                 cdTable[row][column] = (d[row].getX() * c[column].getX()) + (d[row].getY() * c[column].getY());
             }
         }
 
         // Now, apply the z's to the dot products, on the skew diagonal
         // Also, set up the x-values, making these "points"
         for (int i = 0; i <= W_DEGREE; i++) {
             w[i] = new Point2D.Double((double)(i) / W_DEGREE, 0.0);
         }
 
         int n = DEGREE;
         int m = DEGREE-1;
         for (int k = 0; k <= n + m; k++) {
             int lb = Math.max(0, k - m);
             int ub = Math.min(k, n);
             for (int i = lb; i <= ub; i++) {
                 int j = k - i;
                 w[i+j].setLocation(w[i+j].getX(), w[i+j].getY() + cdTable[j][i] * cubicZ[j][i]);
             }
         }
 
         return w;
     }
 
     /***
      * CrossingCount :
      *  Count the number of times a Bezier control polygon 
      *  crosses the 0-axis. This number is >= the number of roots.
      *
      */
     private static int crossingCount(Point2D[] v, int degree) {
         int nCrossings = 0;
         int sign = v[0].getY() < 0 ? -1 : 1;
         int oldSign = sign;
         for (int i = 1; i <= degree; i++) {
             sign = v[i].getY() < 0 ? -1 : 1;
             if (sign != oldSign) nCrossings++;
             oldSign = sign;
         }
         return nCrossings;
     }
     
     
     
     /*
      *  ControlPolygonFlatEnough :
      *  Check if the control polygon of a Bezier curve is flat enough
      *  for recursive subdivision to bottom out.
      *
      */
     private static boolean controlPolygonFlatEnough(Point2D[] v, int degree) {
 
         // Find the  perpendicular distance
         // from each interior control point to
         // line connecting v[0] and v[degree]
     
         // Derive the implicit equation for line connecting first
         // and last control points
         double a = v[0].getY() - v[degree].getY();
         double b = v[degree].getX() - v[0].getX();
         double c = v[0].getX() * v[degree].getY() - v[degree].getX() * v[0].getY();
     
         double abSquared = (a * a) + (b * b);
         double[] distance = new double[degree+1];      // Distances from pts to line
     
         for (int i = 1; i < degree; i++) {
         // Compute distance from each of the points to that line
             distance[i] = a * v[i].getX() + b * v[i].getY() + c;
             if (distance[i] > 0.0) {
                 distance[i] = (distance[i] * distance[i]) / abSquared;
             }
             if (distance[i] < 0.0) {
                 distance[i] = -((distance[i] * distance[i]) / abSquared);
             }
         }
     
     
         // Find the largest distance
         double maxDistanceAbove = 0.0;
         double maxDistanceBelow = 0.0;
         for (int i = 1; i < degree; i++) {
             if (distance[i] < 0.0) {
                 maxDistanceBelow = Math.min(maxDistanceBelow, distance[i]);
             }
             if (distance[i] > 0.0) {
                 maxDistanceAbove = Math.max(maxDistanceAbove, distance[i]);
             }
         }
     
         // Implicit equation for zero line
         double a1 = 0.0;
         double b1 = 1.0;
         double c1 = 0.0;
     
         // Implicit equation for "above" line
         double a2 = a;
         double b2 = b;
         double c2 = c + maxDistanceAbove;
     
         double det = a1 * b2 - a2 * b1;
         double dInv = 1.0/det;
         
         double intercept1 = (b1 * c2 - b2 * c1) * dInv;
     
         //  Implicit equation for "below" line
         a2 = a;
         b2 = b;
         c2 = c + maxDistanceBelow;
         
         det = a1 * b2 - a2 * b1;
         dInv = 1.0/det;
         
         double intercept2 = (b1 * c2 - b2 * c1) * dInv;
     
         // Compute intercepts of bounding box
         double leftIntercept = Math.min(intercept1, intercept2);
         double rightIntercept = Math.max(intercept1, intercept2);
     
         double error = 0.5 * (rightIntercept-leftIntercept);    
         
         return error < EPSILON;
     }
     
     
     
     /*
      *  ComputeXIntercept :
      *  Compute intersection of chord from first control point to last
      *      with 0-axis.
      * 
      */
     private static double computeXIntercept(Point2D[] v, int degree) {
     
         double XNM = v[degree].getX() - v[0].getX();
         double YNM = v[degree].getY() - v[0].getY();
         double XMK = v[0].getX();
         double YMK = v[0].getY();
     
         double detInv = - 1.0/YNM;
     
         return (XNM*YMK - YNM*XMK) * detInv;
     }
     
     
 
     private static Point2D bezier(Point2D[] c, int degree, double t, Point2D[] left, Point2D[] right) {
         // FIXME WIRED-252, move outside the method and make static
         Point2D[][] p = new Point2D.Double[W_DEGREE+1][W_DEGREE+1];
         
         /* Copy control points  */
         for (int j=0; j <= degree; j++) {
             p[0][j]= new Point2D.Double(c[j].getX(),c[j].getY());
         }
             
         /* Triangle computation */
         for (int i = 1; i <= degree; i++) {  
             for (int j = 0 ; j <= degree - i; j++) {
                 p[i][j] = new Point2D.Double(
                     (1.0 - t) * p[i-1][j].getX() + t * p[i-1][j+1].getX(),
                     (1.0 - t) * p[i-1][j].getY() + t * p[i-1][j+1].getY()
                 );
             }
         }
         
         if (left != null) {
             for (int j = 0; j <= degree; j++) {
                 left[j]  = p[j][0];
             }
         }
         
         if (right != null) {
             for (int j = 0; j <= degree; j++) {
                 right[j] = p[degree-j][j];
             }
         }
         
         return p[degree][0];
     }
     
     /***
      * Test for onCurve
      */
     public static void main(String[] args) {
         Point2D pn = new Point2D.Double();
         CubicCurve2D c = new CubicCurve2D.Double(0, 0, 1, 2, 3, 3, 4, 2);
         Point2D pa = new Point2D.Double(3.5, 2.0);
         double distSq = NearestPoint.onCurve(c, pa, pn).u;
         //double t = get_t();
         System.out.println("Point On Curve is "+pn + " distSq=" + distSq);
     }
 }

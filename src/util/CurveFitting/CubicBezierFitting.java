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
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;


public class CubicBezierFitting {

	double THRESHOLD = 25;
	double PIXEL_STEPS = 15;
	public Bezier curve; 
	
	private LinkedList<Integer> idxs = new LinkedList<Integer>();
	private LinkedList<Integer> knots = new LinkedList<Integer>();
	private ArrayList<Point2D>  points ;
	
	
	public ArrayList<Shape> shape = new ArrayList<Shape>();
	public CubicBezierFitting(ArrayList<Point2D> pts) throws Exception{
		
		//This algorithm could take a little bit of time for big inputs 
		//It's just trying to make the best fit with the lest knots points
		//If performance is not good we can remove first step 3, then step 2. 
		// Of course the three steps all together give a better output.
		
		knots.add(0); knots.add(pts.size()-1);
		points = pts;
		curve  = new Bezier(points,knots);				
		// index of the point with max distance to the bezier curve
		int index = maxIndex(points, 0, points.size()-1, curve.getCurveAt(0));
		if (index != -1) idxs.add(index);
		
		//first step of the algorithm.
		while (!idxs.isEmpty()){
			
			int j  = curve.AddIndex(idxs.poll());
				
			
			index = maxIndex(points,knots.get(j-1),knots.get(j),curve.getCurveAt(j-1));
			if (index != -1) idxs.add(index);
			
		
			index = maxIndex(points,knots.get(j),knots.get(j+1),curve.getCurveAt(j));

			if (index != -1)idxs.add(index);
			
		}
		
		int aKnotsS1 = knots.size();
		
		//Step 2: minimising total error 
		for (int j = 0; j < knots.size()-1; j++){
			int t = maxIndex(points,knots.get(j),knots.get(j+1),curve.getCurveAt(j));
			if (t!= -1)	curve.AddIndex(t);			
		}
		
		int aKnotsS2 = knots.size();
		
		//Step 3: simplifying output (less points)
		for (int j =1; j < knots.size()-1; j++){
			index = knots.get(j);
			curve.RemoveIndex(knots.get(j));
			if (check()){
				j--;
			}else {
				curve.AddIndex(index);
			}
		}
		int aKnotsS3 = knots.size();
		
		//System.out.println(String.format("Stats: tPoints:%d kS1:%d kS2:%d kS3:%d", points.size(),aKnotsS1,aKnotsS2,aKnotsS3));
	}
	
	

	private boolean check() throws Exception{
		
		for (int j = 0; j < knots.size()-1; j++){
			if (maxIndex(points,knots.get(j),knots.get(j+1),curve.getCurveAt(j))!=-1) return false;
		}
		return true;
	}
	
	//max sq distance index between cubic bezier and data. 
	private int maxIndex(ArrayList<Point2D> p,
						 int init,
						 int end, 
						 CubicCurve2D curve) throws Exception{
		if (init > end)              throw new Exception("Bad Indices (init > end)");
		int index = -1;
		if (end-init <= PIXEL_STEPS) return index;
		double max=0;
		Line2D line = null;
		for (int i = init+1; i < end; i++){
			Point2D pn = new Point2D.Double();
			double sqDis = NearestPoint.onCurve(curve,p.get(i),pn ).u;
			if (sqDis > max){
				max = sqDis;
				line  = new Line2D.Double(p.get(i).getX(),p.get(i).getY(),pn.getX(),pn.getY());
				index = i;
			}
		}
		//this line is just for debug purposes
		if (max>THRESHOLD) shape.add(line);
		return (max > THRESHOLD)?index: -1;
	}
}

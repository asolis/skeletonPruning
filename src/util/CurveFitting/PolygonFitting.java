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
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;

public class PolygonFitting extends Fitting {
	
	public PolygonFitting(double threshold){
		THRESHOLD = threshold;
	}
	
	public PolygonFitting() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public ArrayList<Shape> fitCurve(ArrayList<Point2D> pts) {
		idxs   = new LinkedList<Integer>();
		knots  = new LinkedList<Integer>();
		knots.add(0); knots.add(pts.size()-1);
		points = pts;
		try {
			curve  = new LeastSquareLine(points,knots);
		} catch (CurveCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
		int index = maxIndex(points, 0, points.size()-1, curve.getLineAt(0));
		if (index != -1) idxs.add(index);
		
		while (!idxs.isEmpty()){
			
			int j  = curve.AddIndex(idxs.poll());
				
			index = maxIndex(points,knots.get(j-1),knots.get(j),curve.getLineAt(j-1));
			if (index != -1) idxs.add(index);
			
			index = maxIndex(points,knots.get(j),knots.get(j+1),curve.getLineAt(j));

			if (index != -1)idxs.add(index);
			
		}
		
		removeUnnecessaryPoints();
		return curve.getCurves();
	}
	
	private boolean check() {
		
		for (int j = 0; j < knots.size()-1; j++){
			if (maxIndex(points,knots.get(j),knots.get(j+1),curve.getLineAt(j))!=-1) return false;
		}
		return true;
	}
	protected void removeUnnecessaryPoints() {
		int index = 0;
		for (int j =1; j < knots.size()-1; j++){
			index = knots.get(j);
			curve.RemoveIndex(knots.get(j));
			if (check()){
				j--;
			}else {
				curve.AddIndex(index);
			}
		}
	}
	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return "Polygon Fitting";
	}
}
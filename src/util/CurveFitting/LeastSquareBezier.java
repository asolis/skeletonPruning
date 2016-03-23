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

import java.awt.Shape;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;

public class LeastSquareBezier extends Interpolation {

	
	boolean dynamic = true;
//	boolean[][]  dp;
	double[]  _a1;
	double[]  _a2;
	double[] _a12;
	
	
//	
//	Point2D[][]  _c1;
//	Point2D[][]  _c2;
	
	public LeastSquareBezier(ArrayList<Point2D> pts_, LinkedList<Integer> index) throws CurveCreationException{
		
		setData(pts_, index);
		
	}
	public void setData(ArrayList<Point2D> pts_,
			LinkedList<Integer> idx) throws CurveCreationException {
			this.index = idx;
			this.points = pts_;
			dynamic = true;
			if (dynamic){
				int size = this.points.size();
		    	_a1= new double[size];
				_a2= new double[size];
				_a12= new double[size];
				
//				_c1 = new Point2D[size][size];
//				_c2 = new Point2D[size][size];
			}
			if (points == null)
				throw new CurveCreationException("Empty point set to interpolate");
			if (index != null)
				java.util.Collections.sort(index); 
			if (N() < 2)
				throw new CurveCreationException("Two knots requiered to interpolate");
			this.cP = new Point2D[(pts_.size()-1)*2];
			compute(0);
	
	} 
	
	@Override
	public int AddIndex(int i){
		if (i < 0 ) return -1;			
		if (this.index == null){
			this.index = new LinkedList<Integer>();
			this.index.add(0);
			this.index.add(points.size()-1);
		}
		for (int j = 0; j < index.size(); j++){
			if (index.get(j) == i) return -1;
			if (index.get(j) < i) continue;
			else {
				index.add(j, i);
				compute(j-1);
				compute(j);
				return j;
			}
		}
		return -1;
	}
	public int AddIndex(int i,Point2D p1, Point2D p2, Point2D p3, Point2D p4){
		if (i < 0 ) return -1;			
		if (this.index == null){
			this.index = new LinkedList<Integer>();
			this.index.add(0);
			this.index.add(points.size()-1);
		}
		for (int j = 0; j < index.size(); j++){
			if (index.get(j) == i) return -1;
			if (index.get(j) < i) continue;
			else {
				index.add(j, i);
				cP[2*getIndex(j)]= p1;   
				cP[2*getIndex(j)+1]= p2; 
		
				cP[2*getIndex(j-1)]= p3;   
				cP[2*getIndex(j-1)+1]= p4; 
				
				return j;
			}
		}
		return -1;
	}
	//Remove index from interpolation
	@Override
	public int RemoveIndex(int i){
		if (i < 0 ) return -1;
		if (this.index == null || this.index.size() <3){
			
		}else {
			for (int j = 0; j < index.size(); j ++)
			{
				if (index.get(j)==i) {
					index.remove(j);
					compute(j-1);
					return j;
				}
			}
		}
		return -1;
	}
	
	protected void compute(int first){
		cP[2*getIndex(first)]     = P1(getIndex(first),getIndex(first+1));
		cP[2*getIndex(first) + 1] = P2(getIndex(first),getIndex(first+1)); 
	}
	protected void compute(){
		//this.cP = new Point2D[(N()-1)*2];
//		if (dynamic) initialize();
		for (int i = 0; i < N()-1; i ++){
			cP[2*getIndex(i)]     = P1(getIndex(i),getIndex(i+1));
			cP[2*getIndex(i) + 1] = P2(getIndex(i),getIndex(i+1)); 
		}	
	}	

	// Least square estimation of first control point
	// P1 = (A2*C1 - A12*C2)/ (A1*A2-A12*A12);
	private Point2D P1(int init,int end){
		if (end - init == 1) return new Point2D.Double(points.get(init).getX(),points.get(init).getY()); 
		double a1 = A1(init,end);
		double a2 = A2(init,end);
		double a12= A12(init,end);
		double  den = ( a1*a2 - Math.pow(a12, 2));
		if (den == 0) return new Point2D.Double(points.get(init).getX(),points.get(init).getY());
		Point2D c1 =  C1(init,end);
		Point2D c2 =  C2(init,end);
		double p1x = (a2*c1.getX() - a12*c2.getX())/den;
		double p1y = (a2*c1.getY() - a12*c2.getY())/den;
		 
		return new Point2D.Double(p1x,p1y);		
	}
	// Least square stimation of second control point
	// P2 = (A1*C2 - A12*C1)/ (A1*A2-A12*A12);
	private Point2D P2(int init,int end){
		if (end - init == 1) return new Point2D.Double(points.get(end).getX(),points.get(end).getY()); 
		
		double a1 = A1(init,end);
		double a2 = A2(init,end);
		double a12= A12(init,end);
		double  den = ( a1*a2 - Math.pow(a12, 2));
		if (den == 0) return new Point2D.Double(points.get(end).getX(),points.get(end).getY()); 
		
		Point2D c1 =  C1(init,end);
		Point2D c2 =  C2(init,end);
		double p2x = (a1*c2.getX() - a12*c1.getX())/den;
		double p2y = (a1*c2.getY() - a12*c1.getY())/den;
		 
		return new Point2D.Double(p2x,p2y);		
	}	
	
	private double A1(int init, int end){
		
		double ti = 0;
		double A1 = 0;
		if (dynamic)
		if (_a1[end-init]> 0) {
				return _a1[end-init]*9.0;
		}
		for (int i = 1; i <= (end-init) ; i ++){
			ti = (double)i/(double)(end-init);
			A1 += Math.pow(ti, 2)*Math.pow(1-ti, 4);
		}
		if (dynamic)
			_a1[end-init] = A1;
		return 9*A1;
	}
	private double A2(int init, int end){
		
		double ti = 0;
		double A1 = 0;
		if (dynamic)
			if (_a2[end-init]> 0) {
					return _a2[end-init]*9.0;
			}
		for (int i = 1; i <= (end-init) ; i ++){
			ti = (double)i/(double)(end-init);
			A1 += Math.pow(ti, 4)*Math.pow(1-ti, 2);
		}
		if (dynamic)
			_a2[end-init] = A1;
		return 9*A1;
	}
	private double A12(int init, int end){
		
		double ti = 0;
		double A1 = 0;
		if (dynamic)
			if (_a12[end-init]> 0) {
					return _a12[end-init]*9.0;
			}
		for (int i = 1; i <= (end-init) ; i ++){
			ti = (double)i/(double)(end-init);
			A1 += Math.pow(ti, 3)*Math.pow(1-ti, 3);
		}
		if (dynamic)
			_a12[end-init] = A1;
		return 9*A1;
	}
	private Point2D C1(int init, int end){
//		if (dynamic)
//			if (_c1[init][end]!=null) return _c1[init][end];		
		Point2D P0 = points.get(init);
		Point2D P3 = points.get(end);
		double c1x = 0;
		double c1y = 0;
		double ti  = 0; 

		for (int i = 1; i <= (end-init); i++){
//			if (dynamic)
//				if (_c1[init][init+i]!=null) {
//					c1x += _c1[init][init+i].getX();
//					c1y += _c1[init][init+i].getY();
//					continue;
//				} 
			ti = (double)i/(double)(end-init);
			c1x += 3*ti*Math.pow(1-ti, 2)*(points.get(init+i).getX() - Math.pow(1-ti, 3)*P0.getX()- Math.pow(ti,3)*P3.getX());
			c1y += 3*ti*Math.pow(1-ti, 2)*(points.get(init+i).getY() - Math.pow(1-ti, 3)*P0.getY()- Math.pow(ti,3)*P3.getY());
//			if (dynamic){
//				_c1[init][init+i]=new Point2D.Double(c1x,c1y);
//			}
		}
		return new Point2D.Double(c1x,c1y);
	}
	private Point2D C2(int init, int end){
		
		Point2D P0 = points.get(init);
		Point2D P3 = points.get(end);
		double c1x = 0;
		double c1y = 0;
		double ti  = 0; 
//		if (dynamic)
//			if (_c2[init][end]!=null) return _c2[init][end];
		for (int i = 1; i <= (end-init); i++){
			ti = (double)i/(double)(end-init);
			c1x += 3*Math.pow(ti, 2)*(1-ti)*(points.get(init+i).getX() - Math.pow(1-ti, 3)*P0.getX()- Math.pow(ti,3)*P3.getX());
			c1y += 3*Math.pow(ti, 2)*(1-ti)*(points.get(init+i).getY() - Math.pow(1-ti, 3)*P0.getY()- Math.pow(ti,3)*P3.getY());
//			if (dynamic){
//				_c2[init][init+i]=new Point2D.Double(c1x,c1y);
//			}
		}
		return new Point2D.Double(c1x,c1y);
	}	

	
	
	
	
	public ArrayList<Shape> getCurves(){
  	  ArrayList<Shape> s = new ArrayList<Shape>();
  	
  	  for (int i = 0; i < N()-1; i++)
  	  {        	
  		  CubicCurve2D.Double cubic = new CubicCurve2D.Double(get(i).getX(),
  				  											  get(i).getY(),
  				  											  cP[2*getIndex(i)].getX(),
  															  cP[2*getIndex(i)].getY(),
  															  cP[2*getIndex(i)+1].getX(),
  															  cP[2*getIndex(i)+1].getY(), 
  				                                              get(i+1).getX(), 
  				                                              get(i+1).getY());
  		  s.add(cubic);
  	  }	    	  
          
        return s;
  }
	public CubicCurve2D getCurveAt(int i){
		if (i < 0 || i >= N() - 1 ) throw 
		new IndexOutOfBoundsException(
				 String.format("Interpolation Class: cannot " +
				 		       "retrieve curve with index : %d" , i));
		CubicCurve2D.Double cubic = new CubicCurve2D.Double(
													  get(i).getX(),
													  get(i).getY(),
													  cP[2*getIndex(i)].getX(),
													  cP[2*getIndex(i)].getY(),
													  cP[2*getIndex(i)+1].getX(),
													  cP[2*getIndex(i)+1].getY(), 
								                      get(i+1).getX(), 
								                      get(i+1).getY());
		return cubic;
	}
	
	

}

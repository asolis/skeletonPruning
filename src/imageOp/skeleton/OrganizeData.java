/**************************************************************************************************
 **************************************************************************************************
 
     BSD 3-Clause License (https://www.tldrlegal.com/l/bsd3)
     
     Copyright (c) 2012 Jochen Lang <https://www.site.uottawa.ca/~jlang/>, All rights reserved.
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
package imageOp.skeleton;

import imageOp.skeleton.BoundaryMapping;
import imageOp.skeleton.MapData;

import java.awt.Shape;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;


public class OrganizeData {
  
  protected ArrayList<Shape> bCurves = new ArrayList<Shape>(); //stores the curves 
  protected ArrayList<ArrayList<Point2D>> ftOnBoundary = new ArrayList<ArrayList<Point2D>>(); // stores the ft corresponding to each curve
  protected ArrayList<ArrayList<Point2D>> sPoint = new ArrayList<ArrayList<Point2D>>(); // stores the pixel chain point corresponding to each curve
  protected ArrayList<ArrayList<Point2D>> sCurveP =new ArrayList<ArrayList<Point2D>>();
  protected ArrayList<ArrayList<CubicCurve2D>> skCurve =new ArrayList<ArrayList<CubicCurve2D>>();
  protected ArrayList<ArrayList<Double>> tvalues = new ArrayList<ArrayList<Double>>();
  protected ArrayList<ArrayList<Double>> qvalues = new ArrayList<ArrayList<Double>>();
  protected BoundaryMapping bM;
  
 public OrganizeData(BoundaryMapping bm){
	 this.bM = bm;
	 organizePoints();
 }
 
 public OrganizeData(){
	 
 }
 
 public void organizePoints(){
	    
     ArrayList<Point2D> tempFT = new ArrayList<Point2D>();
     ArrayList<Point2D> tempsP = new ArrayList<Point2D>();
     ArrayList<Point2D> tempSCP = new ArrayList<Point2D>();
     ArrayList<CubicCurve2D> tempskC = new ArrayList<CubicCurve2D>();
     ArrayList<Double> t = new ArrayList<Double>();
     ArrayList<Double> q = new ArrayList<Double>();
     
     bCurves.add(bM.cBound.get(0));
     
     tempFT.add(bM.ftPoints.get(0));
     ftOnBoundary.add(tempFT);
     
     tempsP.add(bM.skPoint.get(0));
     sPoint.add(tempsP);
     
     tempSCP.add(bM.skCurvePoint.get(0));
     sCurveP.add(tempSCP);
     
     tempskC.add(bM.cSkeleton.get(0));
     skCurve.add(tempskC);
     
     t.add(bM.tvalue.get(0));
     tvalues.add(t);
     
     q.add(bM.qvalue.get(0));
     qvalues.add(q);
     int position;
     
     for (int i=1; i<bM.cBound.size(); i++){
	      
	        if(bCurves.contains(bM.cBound.get(i))){
	           position = bCurves.indexOf(bM.cBound.get(i));   
	    	   ftOnBoundary.get(position).add(bM.ftPoints.get(i));
	           sPoint.get(position).add(bM.skPoint.get(i));
	           sCurveP.get(position).add(bM.skCurvePoint.get(i));
	           skCurve.get(position).add(bM.cSkeleton.get(i));
	           tvalues.get(position).add(bM.tvalue.get(i));
	           qvalues.get(position).add(bM.qvalue.get(i));
	        }
	    	else {
	    	   bCurves.add(bM.cBound.get(i));	
	    	   tempFT = new ArrayList<Point2D>();
	    	   tempFT.add(bM.ftPoints.get(i));
	    	   ftOnBoundary.add(tempFT);
	    	   
	    	   tempsP = new ArrayList<Point2D>();
	    	   tempsP.add(bM.skPoint.get(i));
	    	   sPoint.add(tempsP);
	  	       
	    	   tempSCP = new ArrayList<Point2D>();
	    	   tempSCP.add(bM.skCurvePoint.get(i));
	    	   sCurveP.add(tempSCP);
	    	   
	    	   tempskC = new ArrayList<CubicCurve2D>();
	    	   tempskC.add(bM.cSkeleton.get(i));
	    	   skCurve.add(tempskC);
	    	   
 	   	   	   t = new ArrayList<Double>();
 	   	   	   t.add(bM.tvalue.get(i));
 	   	   	   tvalues.add(t);
	    	   
 	   	       q = new ArrayList<Double>();
	   	   	   q.add(bM.qvalue.get(i));
	   	   	   qvalues.add(q);
	    	}
	        
	      	 
	     }
     
                           
         ArrayList<ArrayList<MapData>> md = new ArrayList<ArrayList<MapData>>();
         ArrayList<MapData> tempMD = null;
      
    	   for(int i=0; i<ftOnBoundary.size(); i++){
    		 MapData mapdata = null;
    		 tempMD = new ArrayList<MapData>();
    		 for(int j=0; j<ftOnBoundary.get(i).size();j++){
  		 
    			int id = skCurve.get(i).get(j).hashCode();
    			mapdata = new MapData(sPoint.get(i).get(j),sCurveP.get(i).get(j),id, tvalues.get(i).get(j),
    					   qvalues.get(i).get(j));
    		  	tempMD.add(mapdata);
    		 }
    		md.add(tempMD);
    		
    	}
    	   System.out.println("end for loop");
 }
 
  public ArrayList<Shape> getbCurves(){
	  return bCurves;
  }
  public ArrayList<ArrayList<Double>> gettValues(){
	  return tvalues;
	  
  }
  
  public ArrayList<ArrayList<Double>> getqValues(){
	  return qvalues;
	  
  }

  public ArrayList<ArrayList<CubicCurve2D>> getSkCurve(){
	  return skCurve;
	  
  }

  
}

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
package util;

import imageOp.skeleton.FeatureTransform;
import imageOp.skeleton.PixelChain;
import imageOp.skeleton.Point;
import imageOp.skeleton.Segmentation;
import imageOp.skeleton.SegmentationImage;

import java.util.ArrayList;

public class BPointTest {

	private ArrayList<Point> bpoints = new ArrayList<Point>();
	public Point [][] table;
	private int width;
	protected int [][] ft;
	public int countNulls;
	
	public BPointTest(ArrayList<Point> bp, int[][] ft2, int width){
		this.bpoints = bp;
		this.width = width;
		this.ft = ft2;
		getTable();
		
	}

	private void getTable() {
		table = new Point[bpoints.size()][bpoints.size()];
		countNulls = 0;
		for(int i = 0; i<bpoints.size(); i++){
			int pos1 = bpoints.get(i).x*width + bpoints.get(i).y;
			int dS1 = (new Point(ft[0][pos1],ft[1][pos1])).distanceSq(bpoints.get(i));
			int radio1 = (int) Math.ceil(Math.sqrt(dS1));
			
			for(int j = 0; j<bpoints.size(); j++){
				            
				int pos2 = bpoints.get(j).x*width + bpoints.get(j).y;
				int dS2= (new Point(ft[0][pos2],ft[1][pos2])).distanceSq(bpoints.get(j));
				int radio2 = (int) Math.ceil(Math.sqrt(dS2));
				
				// distance from center to the others branch points			
			    int ppD = (int) Math.sqrt(((bpoints.get(i).x - bpoints.get(j).x)* (bpoints.get(i).x - bpoints.get(j).x)) +
			    		   ((bpoints.get(i).y - bpoints.get(j).y)* (bpoints.get(i).y - bpoints.get(j).y)));
			    
			    if(ppD <= radio1){
			    	table[i][j] = bpoints.get(j);
			    }else
			    	countNulls = countNulls+1;
			    
			    // If table does not contains any null value, it means that the maximal disk of each 
			    // branch points contains all the others branch points, in this case, all the branch points can be
			    // considered as a single one. Thus, we need to check if there is a null value in the table.
			    // This is just for shapes like stars and squares. When table contains several null values, we need 
			    // to treat it in a different way in order to "merged" the corresponding branch points
			    System.out.println();
			}
			
		}
		System.out.println();
	}
	
	
}

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
 
 **************************************************************************************************
 **************************************************************************************************/
package imageOp.skeleton;

import java.util.ArrayList;

public class SkeletonThinner {
  private static final boolean DEBUG = false;

	private byte[] skeleton;
	private ArrayList<Point> skeletonPts;
	private ArrayList<Point> branchPts;
	private ArrayList<Point> endPts;
	private int width;
	private int height;
	
	
	//stats 
	private int droppedPoints = 0;
	
	byte[] A  = new byte[]{ 0, 0, 3,
							0, 1, 1,
							3, 1, 3 };
	
	byte[] B  = new byte[]{ 3, 0, 0,
							1, 1, 0,
							3, 1, 3 };
	
	byte[] C  = new byte[]{ 3, 1, 3,
							1, 1, 0,
							3, 0, 0 };
	
	byte[] D  = new byte[]{ 3, 1, 3,
							0, 1, 1,
							0, 0, 3 };
	
	byte[] E  = new byte[]{ 0, 0, 0,
							3, 1, 3,
							1, 1, 3 };
	
	byte[] F  = new byte[]{ 1, 3, 0,
							1, 1, 0,
							3, 3, 0 };
	
	byte[] G  = new byte[]{ 3, 1, 1,
							3, 1, 3,
							0, 0, 0 };
	
	byte[] H  = new byte[]{ 0, 3, 3,
							0, 1, 1,
							0, 3, 1 };
	//removing noise (isolated points)
	byte[] I  = new byte[]{0, 0, 0,
						   0, 1, 0,
						   0, 0, 0};
	
	byte[] E2  = new byte[]{ 0, 0, 0,
							 3, 1, 3,
							 3, 1, 1 };

	byte[] F2  = new byte[]{ 3, 3, 0,
							 1, 1, 0,
							 1, 3, 0 };

	byte[] G2  = new byte[]{ 1, 1, 3,
							 3, 1, 3,
							 0, 0, 0 };

	byte[] H2  = new byte[]{ 0, 3, 1,
							 0, 1, 1,
							 0, 3, 3 };
	byte[] H3  = new byte[]{ 0, 3, 1,
							 0, 1, 1,
			                 1, 1, 0 };
	
	byte[][] sTemps = new byte[][]{A,B,C,D,E,F,G,H,I, E2,F2,G2,H2,H3};
	
  // Why are B1 to B4  bps?	

	byte[] B1 = new byte[]{3, 1, 3,
			       3, 1, 1,
			       3, 3, 3}; // 1, 3, 3 JL
	byte[] B2 = new byte[]{3, 1, 3,
			       1, 1, 3,
			       3, 3, 3};
	byte[] B3 = new byte[]{3, 3, 3,
			       1, 1, 3,
			       3, 1, 3};
	byte[] B4 = new byte[]{3, 3, 3,
			       3, 1, 1,
			       3, 1, 3};
	
	byte[] B5 = new byte[]{3, 0, 3,
			       0, 1, 0,
			       3, 0, 3};
	byte[] B6 = new byte[]{0, 1, 0,
			       3, 1, 3,
			       1, 0, 1};	
	byte[] B7 = new byte[]{1, 0, 1,
			       3, 1, 3,
			       0, 1, 0};	
	byte[] B8 = new byte[]{0, 3, 1,
			       1, 1, 0,
			       0, 3, 1};	
	byte[] B9 = new byte[]{1, 3, 0,
			       0, 1, 1,
			       1, 3, 0};
  // Added JL
	byte[] B10 = new byte[]{1, 0, 0,
			       0, 1, 1,
			       1, 0, 1};
	byte[] B11 = new byte[]{1, 1, 0,
			       0, 1, 0,
			       1, 0, 1};


  byte[][] bTemps = new byte[][]{B1,B2,B3,B4,B5,B6,B7,B8,B9,B10,B11};

	
	
	
	
	byte[] f = new byte[]{3,1,3,
						  1,0,1,
						  3,1,3};
	byte[][] OR = new byte[][]{f};
	
  private boolean matchTemplates(int row, int col,byte[][] temp){
    int p = row*width+col;
    int[] pos = new int[]{p-width-1, p-width, p-width+1,
			  p-1      , p      , p+1,
			  p+width-1, p+width, p+width+1};		
	
    for (byte[] t: temp){
			
      TEMPLATE:{
	int 	count = 0;
	for (int index: pos){			
	  switch(t[count++]){
	  case 0:
	    if (skeleton[index] == IMA.SKEL) 
	      // was >= but such values don't exist 
	      break TEMPLATE;
	    break;
	  case 1:
	    if (skeleton[index] != IMA.SKEL)
	      break TEMPLATE;
	    break;
	  case 3:
	    break;				
	  }
					
	}
	return true;
      }
		
    }		
    return false;
  }

	private void makeThinner(){
		//This was a quit fix, a better combination of templates must be choseen
		// but so far this  works good.
		for (int row = 1; row < height - 1; row++ ){
			for (int col = 1; col < width -1; col++){
				if (matchTemplates(row, col, OR)&& degree(row,col)>4) {
					
					skeleton[row*width+col]= IMA.SKEL;
				}
			}
		}
		droppedPoints  = 0;
		for (int row = 1; row < height - 1; row++ ){
			for (int col = 1; col < width -1; col++){
				if (skeleton[row*width+col] == IMA.SKEL)
					if (matchTemplates(row, col, sTemps)&& degree(row,col)>2) {
						droppedPoints++;
						skeleton[row*width+col]= IMA.WHITE;
					}
					else skeletonPts.add(new Point(row,col));
			}
		}
		int count = 0;
		ArrayList<Point> remove = new ArrayList<Point>();
		do {
			count = 0;
			for (Point p: skeletonPts){
				if (matchTemplates(p.x,p.y,sTemps)){
					count++;
					droppedPoints++;
					skeleton[p.x*width+p.y]= IMA.WHITE;
					remove.add(p);
				}
			}
			
		}while (count >0);
		skeletonPts.removeAll(remove);
		
	}
	
	private int degree(int row,int col){
		int p = row*width+col;
		int[] pos = new int[]{p-width-1, p-width, p-width+1,
							  p-1      /*, p*/      , p+1,
							  p+width-1, p+width, p+width+1};
		int degree = 0;
		for (int _p : pos){
			if (skeleton[_p] == IMA.SKEL) degree++;
		}
		return degree;
	}
	
	public SkeletonThinner(byte[] skeleton, int width, int height){
		if (skeleton == null) 
			throw new NullPointerException("Skeleton data cannot be empty");
		if (width < 1 || height < 1 ) 
			throw new NullPointerException("Width or Height must be greather than 0");
		this.skeleton = skeleton;
		this.width  = width;
		this.height = height;
		this.skeletonPts = new ArrayList<Point>();
		this.branchPts   = new ArrayList<Point>();
		this.endPts      = new ArrayList<Point>();
		makeThinner();
		correctBranchPoints();
		correctEndPoints();
		if (DEBUG ) {
		  System.out.println(String.format("Dropped Points: %d Final Skell Pts:%d Branch Pts:%d EndPts:%d", 
						   droppedPoints,
						   skeletonPts.size(),
						   branchPts.size(),
						   endPts.size()));
		}
	}
	
	public void correctBranchPoints(){
		branchPts.clear();
		for (Point p: skeletonPts){
					if (degree(p.x, p.y) >2 && matchTemplates(p.x, p.y, bTemps)) 
					branchPts.add(p);
			}
		
	}
	
	public void correctEndPoints(){
		endPts.clear();
		for (Point p: skeletonPts){
			if (isEndPoint(p.x,p.y))
				endPts.add(p);
		}
	}
	final boolean isEndPoint(int row,int col){
		  if ((row==0) || (col==0) || (row==height-1) || (col==width-1)){
		      return true;
		  }
		  int p = row*width+col;
		  int pos[] = {
				  p-1,         //Left
				  p-width-1,   //Upper-left
				  p-width,	 //Top
				  p-width+1,	 //Upper-Right
				  p+1,         //Right
				  p+width+1,   //Bottom-Right
				  p+width,     //Bottom
				  p+width-1	 //Bottom-Left			  
		  };	
		  int count = 0;
		  
		  for (int i = 0; i < pos.length + 5 ; i ++){
			  //if (skeleton[pos[i%pos.length]] == IMA.BLACK) count++;
			  if (skeleton[pos[i%pos.length]] <= IMA.WHITE) count++; else count = 0;
			  if (count >= 6 ) return true;
		  }

		  return false;
	  }
	
	public byte[] getSkeleton()
	{
		return skeleton;
	}
	
	public ArrayList<Point> branchPoints(){
		return branchPts;
	}
	public ArrayList<Point> skeletonPoints(){
		return skeletonPts;
	}
	
	public ArrayList<Point> endPoints(){
		return endPts;
	}
}

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


import java.awt.image.BufferedImage;
import java.util.ArrayList;


/** 
 * Class which given a Integer Medial Axis (IMA) of a grown shape and
 * the original shape, combines the two. This is the shrinking phase
 * of the Scale Axis Transform (SAT).  The SAT is somewhat simplified
 * since no updates feature transform is calculated. See the
 * description of Miklos, Giesen and Pauly, SIGGRAPH 2010.
 @see SAG
 @see IMA
 @see FeatureTransform
*/ 
public class SAT {
  // image size -- for convienence
  protected int width;
  protected int height;

  protected IMA imaData;
  protected SAG sagData;

  ArrayList<Point> skeletonPts;
  ArrayList<Point> endPts;
  ArrayList<Point> branchPts;

  /** Initialize */
  public SAT( SAG _sagData, IMA _imaData ) {
    setInput( _sagData, _imaData );
  }

  public void setInput( SAG _sagData, IMA _imaData ) {
    this.sagData = _sagData; // reference to original data
    this.width = sagData.imaData.ftData.boundary.getWidth();
    this.height = sagData.imaData.ftData.boundary.getHeight();
    this.imaData = _imaData; // reference to grown _imaData
    this.skeletonPts= new ArrayList<Point>(imaData.skeletonPts.size()/2);
    this.branchPts= new ArrayList<Point>(imaData.branchPts.size()/2);
    this.endPts = new ArrayList<Point>(imaData.branchPts.size()/2);
    
    //convienence reference
    boolean boundary[] = sagData.imaData.ftData.boundary.boundary;
    // Prune the skeleton data
    for ( Point sPt : imaData.skeletonPts ) {
      if ( !boundary[width*sPt.x+sPt.y] ) {
	// Add point ref to the pruned list
    	  if (isBoundary(boundary,imaData.skeleton, sPt.x, sPt.y)) this.endPts.add(sPt);
    	  this.skeletonPts.add( sPt ); 
      }
    }
    this.skeletonPts.trimToSize();
   
    for ( Point bPt : imaData.branchPts ) {
      if ( !boundary[width*bPt.x+bPt.y] ) {
	// Add point ref to the pruned list
	this.branchPts.add( bPt ); 
      }
    }
    
    for ( Point bPt : imaData.endPts ) {
        if ( !boundary[width*bPt.x+bPt.y] ) {
        	// Add point ref to the pruned list
        	this.endPts.add( bPt ); 
        }
      }
    branchPts.trimToSize();
    endPts.trimToSize();
    return;
  }
  private boolean isBoundary(boolean boundary[],byte[] skeleton,int row,int col){
	  if ((row==0) || (col==0) || (row==height-1) || (col==width-1)){
	      return true;
	  }
	  int pos = row*width+col;
	
	  int neighboors[] = {
			  pos-1,         //Left
			  pos-width-1,   //Upper-left
			  pos-width,	 //Top
			  pos-width+1,	 //Upper-Right
			  pos+1,         //Right
			  pos+width+1,   //Bottom-Right
			  pos+width,     //Bottom
			  pos+width-1	 //Bottom-Left			  
	  };
	  
	  int count = 0;
	  
	  for (int i = 0; i < neighboors.length + 5 ; i ++){
		  //if (skeleton[neighboors[i%pos.length]] == IMA.BLACK) count++;
		  if (skeleton[neighboors[i%neighboors.length]] <= IMA.WHITE) count++; else count = 0;
		  if (count >= 6 ) return true;
	  }

	  return false;
  }

  /** Return the skeleton as an ArrayList (reference) */
  protected ArrayList<Point> getSkeletonPoints() {
    return this.skeletonPts;
  }

  /** Return the skeleton branch points as an ArrayList */
  protected ArrayList<Point> getBranchPoints() {
    return this.branchPts;
  }
  /** Return the skeleton end points as an ArrayList */
  protected ArrayList<Point> getEndPoints() {
		
		return this.endPts;
  }

  /** Get result as an image */
  public SATImage getSATImage()  {
    SATImage result = 
      new SATImage( this, width, height, BufferedImage.TYPE_INT_RGB);
	    for ( int row=0; row < height; row++ ) {
	      int rowStart = row*width;
	      for ( int col=0; col < width; col++ ) {
				if ( sagData.imaData.ftData.boundary.boundary[rowStart+col] ) {
				  result.setRGB(col, row, imaData.BACK_COLOR);
				} else {
							  if ( imaData.skeleton[rowStart+col] == IMA.WHITE ) {
							    result.setRGB(col, row, imaData.SHAPE_COLOR );
							  } else {
							    result.setRGB(col, row, imaData.SKEL_COLOR );
							  }
				}
	      }
	    }
    
    for (Point p: this.getBranchPoints())
    {    	
    	result.setRGB(p.y, p.x, imaData.BP_COLOR);
    }
    for (Point p: this.getEndPoints()){
    	result.setRGB(p.y, p.x, imaData.EP_COLOR);
    }
    
    return result;
  }

}
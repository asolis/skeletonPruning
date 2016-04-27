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
 * Class which given a feature transform on binary images, calculates
 a skeleton, see the algorithm of Hesselink and Roerdink, PAMI,
 2008. Implements constant pruning.
 @see FeatureTransform
*/ 
public class IMA {
  public static final int DEFAULT_GAMMA = 1;
  public static final byte BLACK = 0, WHITE = 1, SKEL = 2;
  public static final int BP_COLOR = 0x00FF00;
  public static final int EP_COLOR = 0xFF0000;
  public static final int SKEL_COLOR = 0x0000FF;
  public static final int SHAPE_COLOR = 0xFFFFFF;
  public static final int BACK_COLOR = 0x000000;
  // Feature transform
  protected FeatureTransform ftData;
  
  protected float gammaSq;

  /** Result skeleton */
  protected int width;
  protected int height;
  protected byte[] skeleton;
  //protected boolean[] branch;
  protected ArrayList<Point> skeletonPts;
  protected ArrayList<Point> branchPts;
  protected ArrayList<Point> endPts;
  // private Point tmpPt = new Point();

  public IMA(){
	  
  }
  /** Initialize */
  public IMA( FeatureTransform _ftData ) {
    this(_ftData, 1.0f );
  }

  /** Gamma is a float value but truncated to int for no 
      or constant pruning */
  public IMA( FeatureTransform _ftData, float gamma  ) {
    if ( gamma > 1.0f ) gammaSq = gamma*gamma;
    else gammaSq = 1.0f;
    setFT( _ftData );
  }

  public void setFT( FeatureTransform _ftData ) {
    ftData = _ftData; // keep a reference
    // for convienence
    this.width = ftData.boundary.getWidth();
    this.height = ftData.boundary.getHeight();
    this.skeleton = new byte[width*height];
    //this.branch = new boolean[width*height];
    
    // Allocate a growable array with a guessed length of width+height
    this.skeletonPts = new ArrayList<Point>(width+height);
    this.branchPts = new ArrayList<Point>();
    this.endPts = new ArrayList<Point>();
    boolean[] bBoundary = ftData.boundary.getBoundary();
    for ( int pos=0; pos < height*width; pos++ ) {
      if (bBoundary[pos]) {
    	  skeleton[pos] = BLACK;
      } else {
    	  skeleton[pos] = WHITE;
      }
    }
   
	findSkeleton();
   
    SkeletonThinner thin = new SkeletonThinner(skeleton, width, height);
    skeletonPts = thin.skeletonPoints();
    branchPts   = thin.branchPoints();
    endPts      = thin.endPoints();
    
    //findEndPoints();
    skeletonPts.trimToSize();
    branchPts.trimToSize();
    endPts.trimToSize();
   
  }
protected int degree(int row,int col){
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
  /** Compare function; override for different pruning methods */
  protected void compare( int xRow, int xCol, int yRow, int yCol) {
    // feature transform for i and p
    Point xFt = new Point( ftData.ft[0][xRow*width+xCol],
			   ftData.ft[1][xRow*width+xCol] );
    Point yFt = new Point( ftData.ft[0][yRow*width+yCol],
			   ftData.ft[1][yRow*width+yCol] );
    if ( xFt.distanceSq(yFt) > (int) gammaSq ) {
      Point dFt = xFt.sub(yFt); 
      Point lFt = new Point( xFt.x + yFt.x - xRow - yRow, 
			     xFt.y + yFt.y - xCol - yCol );
      int crit = dFt.innerProd( lFt );
      
      
      if ( crit >= 0 ) {
			if  ( skeleton[xRow*width+xCol] != SKEL ) {
			  // Add it only once
			  skeletonPts.add( new Point( xRow, xCol )); 
			  skeleton[xRow*width+xCol] = SKEL;

			}
      }
      if ( crit <= 0 ) {
			if ( skeleton[yRow*width+yCol] != SKEL ) {
			  // Add it only once
			  skeletonPts.add( new Point( yRow, yCol )); 
			  skeleton[yRow*width+yCol] = SKEL;

			}
      }
    }
    return;
  }

  final protected byte[] findSkeleton() {
    for ( int row=0; row < height; row++ ) {
      int rowStart = row*width;
      for ( int col=0; col < width; col++ ) {
	if ( row > 0 && ( skeleton[rowStart+col] == WHITE ||
			  skeleton[rowStart-width+col] == WHITE )) {
		
	  compare( row, col, row-1, col );
	  } 
	if ( col > 0 && ( skeleton[rowStart+col] == WHITE ||
			  skeleton[rowStart+col-1] == WHITE )) {
	  compare( row, col, row, col-1 );
	} 
      }  // end col
    }  // end row
    return this.skeleton;
  }
  /** Get result as jet-mapped image */
  public IMAImage getIMAImage()  {
    // Construct buffered image from result

    IMAImage imaImage = new IMAImage(this, width, height,
    								 BufferedImage.TYPE_INT_RGB);
    

    //skeleton = thinner.getSkeleton();
    for ( int row=0; row < height; row++ ) {
      int rowStart = row*width;
      for ( int col=0; col < width; col++ ) {
    	    byte skelVal = skeleton[rowStart+col]; 
			if ( skelVal == BLACK ) {
			  imaImage.setRGB(col, row, 0);
			} else {
			  if ( skelVal == WHITE ) {
			    imaImage.setRGB(col, row, SHAPE_COLOR );
			  } else {
			    imaImage.setRGB(col, row, SKEL_COLOR );
			  }
			}
      }
    }
    
    //just to check the amount of branch points detected
    //System.out.println(getBranchPoints().size());
    for (Point p: this.getBranchPoints())
    {    	
    	imaImage.setRGB(p.y, p.x, BP_COLOR);
    }
    for (Point p: this.getEndPoints()){
    	imaImage.setRGB(p.y, p.x, EP_COLOR);
    }
    return imaImage;
  }

  public ArrayList<Point> getEndPoints() {	
	return this.endPts;
  }

/** Return the skeleton as an ArrayList */
  public ArrayList<Point> getSkeletonPoints() {
    return this.skeletonPts;
  }


  /** Return the skeleton branch points as an ArrayList */
  public ArrayList<Point> getBranchPoints() {
    return this.branchPts;
  }


 

 

  

}
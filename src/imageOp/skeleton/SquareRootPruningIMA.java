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


/**
 * Implements square-root pruning.
 @see IMImage
*/ 
public class SquareRootPruningIMA extends IMA {

  // No threshold gamma needed
  public SquareRootPruningIMA( FeatureTransform _ftData ) {
    super(_ftData, 1.0f );
  }

  /** Compare function with linear pruning */
  protected void compare( int xRow, int xCol, int yRow, int yCol) {
    // feature transform for i and p
    Point xFt = new Point( ftData.ft[0][xRow*width+xCol],
			   ftData.ft[1][xRow*width+xCol] );
    Point yFt = new Point( ftData.ft[0][yRow*width+yCol],
			   ftData.ft[1][yRow*width+yCol] );
    int distSqXFtYFt = xFt.distanceSq(yFt);
    if ( distSqXFtYFt > 1 ) {
      Point lFt = new Point( xFt.x + yFt.x - xRow - yRow, 
			     xFt.y + yFt.y - xCol - yCol );
      Point dFt = xFt.sub(yFt); 
      Point dPt = new Point( xRow-yRow, xCol-yCol );
      if ( (float) distSqXFtYFt > 2.0f * dPt.innerProd( dFt ) +
	   (float) Math.sqrt(lFt.innerProd(lFt)) + 1.0f ) {
	int crit = dFt.innerProd( lFt );
	if ( crit >= 0 ) {
	  if  ( skeleton[xRow*width+xCol] != SKEL ) {
	    // Add it only once
	    skeletonPts.add( new Point( xRow, xCol )); 
	    skeleton[xRow*width+xCol] = SKEL;
	    // Check if we are at a branch
//	    if (isBranchPoint(xRow,xCol))
//	      branchPts.add( new Point( xRow, xCol )); 
	  }
	}
	if ( crit <= 0 ) {
	  if ( skeleton[yRow*width+yCol] != SKEL ) {
	    // Add it only once
	    skeletonPts.add( new Point( yRow, yCol )); 
	    skeleton[yRow*width+yCol] = SKEL;
	    // Check if we are at a branch
//	    if (isBranchPoint(xRow,xCol))
//	      branchPts.add( new Point( yRow, yCol )); 
	  }
	}
      }
    }
    return;
  }

////Implemented here if there is need to change the threshold formula
////for isBranchPoint computation
////pivot is the candidate for branch point
//protected double Threshold(Point pivot,Point x, Point y,Point xFt, Point yFt){
//	  
//	  //equivalent code to professor solution. 
//	  Point pA = xFt.sub(x);
//	  Point pB = yFt.sub(y);
//	  Point lFt = pA.add(pB); //lFt
//	  
//	  Point dFt = pA.sub(pB); // dFt
//
//	  Point dPt = x.sub(y); //x-y
//	  
//	  
//	   return  2.0f * dPt.innerProd( dFt ) +
//	   (float) Math.sqrt(lFt.innerProd(lFt)) + 1.0f ;
//		  
//}

}
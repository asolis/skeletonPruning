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


import util.JetMap;
import java.awt.image.BufferedImage;


/**
 * Class which calculates a linear time distance transform on binary
 images, see the algorithm of Meijster, Roerdink and Hesselink, 2000.
 The distance metric can be changed between Euclidean, Manhatten and
 Chessboard; default is Euclidean. */ 
public class DistanceTransform {
  Boundary boundary;
  int inf;

  /** Current column pointer */ 
  protected int colR2;
  /** Intermediate row only distance transform */
  protected int[] dtRow;

  /** Result distance transform */
  protected int[] dt;

  /** Initialize */
  public DistanceTransform( Boundary _boundary ) {
    setBoundary( _boundary );
  }

  public void setBoundary( Boundary _boundary ) {
    this.boundary = _boundary;
    // No value can be larger
    this.inf = _boundary.getWidth() + _boundary.getHeight(); 
    processRows();  
    processColumns();
    // dt = dtRow;
  }

  /** Get result as jet-mapped image */
  DTImage getDTImage()  {
    // Construct buffered image from result
    // Maximum element
    int maxElement = 1;
    for ( int el=0; el<boundary.getHeight()*boundary.getWidth(); el++ ) {
      if ( dt[el] > maxElement ) maxElement = dt[el];
    }
    System.err.println( "Maximum: " + maxElement );    
//     int diagonal = (int) Math.sqrt(boundary.getWidth()*boundary.getWidth() +
// 				   boundary.getHeight()*boundary.getHeight());
    int diagonal = boundary.getWidth()*boundary.getWidth() +
      boundary.getHeight()*boundary.getHeight();

    maxElement = Math.min( maxElement, diagonal );
    System.err.println( "JetMap: " + 0 + "..." + maxElement ); 
    JetMap jmap = new JetMap( 0, maxElement ); // maxElement );

    DTImage dtImage = new DTImage(this, boundary.getWidth(), 
				  boundary.getHeight(),
				  BufferedImage.TYPE_INT_RGB);
    for ( int row=0; row < boundary.getHeight(); row++ ) {
      int rowStart = row*boundary.getWidth();
      for ( int col=0; col < boundary.getWidth(); col++ ) {
	int d = dt[rowStart+col];
	if ( d == 0 ) {
	  dtImage.setRGB(col, row, 0);
	} else {
	  if ( d > maxElement || d < 0 )
	    dtImage.setRGB(col, row, 0xFFFFFF );
	  else {
	    dtImage.setRGB(col, row, jmap.get(d));
	  }
	}
      }
    }
    return dtImage;
  }


  /** Distance from xRow to boundary via iRow 
      Override function for different distance metric */
  protected int distance(int xRow, int iRow ) {
    int rowDist = 0;
    try {
      rowDist = dtRow[iRow*boundary.getWidth()+colR2];
    } catch (Exception _e) {
      System.err.println("iRow: " + iRow + " colR2: " + colR2);
      // _e.printStackTrace();
    }

    return (xRow-iRow)*(xRow-iRow) 
      + rowDist*rowDist;
  }
    
  /** Calculates row where iSeg(row) <= uSeg(row)
      Override function for different distance metric */
  protected int separation(int iRow, int uRow ) {
    if ( uRow - iRow == 0 ) return 0;
    int iRowDist = dtRow[iRow*boundary.getWidth()+colR2];
    int uRowDist = dtRow[uRow*boundary.getWidth()+colR2];
    return ((uRow*uRow-iRow*iRow
	     + uRowDist*uRowDist - iRowDist*iRowDist)/
	    (2*(uRow-iRow)));
  }

  // Algorithm in two rounds; round one: along rows
  protected int[] processRows() {
    this.dtRow = new int[boundary.getHeight()*boundary.getWidth()];
    boolean[] bBoundary = boundary.getBoundary();
    for ( int row=0; row<boundary.getHeight(); row++ ) {
      int startRow = row*boundary.getWidth();
      // Count distance from left boundary
      if ( bBoundary[startRow] ) { 
	dtRow[startRow] = 0;
      } else {
	dtRow[startRow] = this.inf;
      }
      for ( int col=1; col<boundary.getWidth(); col++ ) {
	if ( bBoundary[startRow+col] ) {
	  dtRow[startRow+col] = 0;
	} else {
	  dtRow[startRow+col] = 1 + dtRow[startRow+col-1];
	}
      }
      // Update with distance from right boundary
      for ( int col=boundary.getWidth()-2; col>=0; col-- ) {
	if ( dtRow[startRow+col+1] < dtRow[startRow+col] ) {
	  // Will not be more then dtRow[startRow+col]
	  dtRow[startRow+col] = 1+dtRow[startRow+col+1];
	} 
      }
    }
    return this.dtRow;
  }

  // Algorithm in two rounds; round two: along cols
  protected int[] processColumns() {
    this.dt = new int[boundary.getHeight()*boundary.getWidth()];
    int[] seg = new int[boundary.getHeight()+1]; // s
    int[] val = new int[boundary.getHeight()+1]; // t
    int idSeg; // q
    for ( colR2=0; colR2 < boundary.getWidth(); colR2++ ) { 
      idSeg = 0; 
      seg[0] = 0;
      val[0] = 0;
      
      for ( int u=1; u < boundary.getHeight(); u++ ) {
	while ( idSeg >= 0 && 
		distance(val[idSeg],seg[idSeg]) > 
		distance(val[idSeg],u)) {
	  --idSeg;
	}
	if ( idSeg < 0 ) {
	  idSeg = 0;
	  seg[idSeg] = u; // set current index to be first
	} else {
	  int curVal = 1 + separation(seg[idSeg],u);
	  // Check if current segment becomes minimal inside image
	  if ( curVal < boundary.getHeight() ) {
	    // Update for current segment
	    ++idSeg;
	    seg[idSeg] = u;
	    val[idSeg] = curVal;
	  }
	}
      }
      for ( int u=boundary.getHeight()-1; u>=0; u-- ) {
	dt[u*boundary.getWidth()+colR2] = 
	  // (int) Math.sqrt(distance(u,seg[idSeg]));
	  distance(u,seg[idSeg]);
	// Reached next segment?
	if ( u==val[idSeg] ) {
	  --idSeg;
	}
      }
    }
    return dt;
  }

}
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

import util.PseudoRandomMap;
import java.awt.image.BufferedImage;


/**
 * Class which calculates a linear time feature transform on binary
 images, see the algorithm of Hesselink and Roerdink, PAMI, 2008. This
 is basically identical to the distance transform by Meijster et
 al. except that the closest point of the background is stored instead
 of the distance.
*/ 
public class FeatureTransform {

  Boundary boundary;
  int inf;

  /** Current column pointer */ 
  protected int colR2;

  /** Result row feature transform */
  protected int[] ftRow;

  /** Result feature transform */
  // ft[0] is row, ft[1] is col
  protected int[][] ft;

  /** Initialize */
  public FeatureTransform( Boundary _boundary ) {
    setBoundary( _boundary );
  }

  public void setBoundary( Boundary _boundary ) {
    this.boundary = _boundary;
    // No value can be larger
    this.inf = _boundary.getWidth() + _boundary.getHeight(); 
    processRows();  
    processColumns();
    /* // Testing rows only
    this.ft = new int[2][_boundary.getWidth() + _boundary.getHeight()]; 
    ft[0] = ftRow;
    ft[1] = ftRow;
    */
  }

  /**  Return reference to 2D array of FT in row major order 
   * ft[0] is row, ft[1] is col  */
  protected int[][] getFT() {
    return this.ft;
  }


  /** Get result as color-mapped image */
  FTImage getFTImage()  {
    // Construct buffered image from result
    // Maximum element
    int last = boundary.getWidth()+boundary.getHeight();
    System.err.println( "PseudoRandomMap: " + 0 + "..." + last ); 
    PseudoRandomMap pmap = new PseudoRandomMap( 0, last );
    FTImage ftImage = new FTImage(this, boundary.getWidth(), 
				  boundary.getHeight(),
				  BufferedImage.TYPE_INT_RGB);
    for ( int row=0; row < boundary.getHeight(); row++ ) {
	      int rowStart = row*boundary.getWidth();
	      for ( int col=0; col < boundary.getWidth(); col++ ) {
			int d = ft[0][rowStart+col] + ft[1][rowStart+col];
			if ( col == ft[1][rowStart+col] &&
			     row == ft[0][rowStart+col] ) {
				ftImage.setRGB(col, row, 0);
			} 
			else {
			  if ( d > last || d < 0 )
			    ftImage.setRGB(col, row, 0xFFFFFF );
			  else {
				int colour = pmap.get(d,(ft[1][rowStart+col]>=ft[0][rowStart+col]));
			    ftImage.setRGB(col, row, colour);
			  }
			}
	      }
    }
    return ftImage;
  }


  /** Distance from xRow to boundary via iRow 
      Override function for different distance metric */
  protected int distance(int xRow, int iRow ) {
    int rowDist = 0;
    try {
      rowDist = colR2 - ftRow[iRow*boundary.getWidth()+colR2];
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
    int iRowDist = colR2-ftRow[iRow*boundary.getWidth()+colR2];
    int uRowDist = colR2-ftRow[uRow*boundary.getWidth()+colR2];
    return ((uRow*uRow-iRow*iRow
	     + uRowDist*uRowDist - iRowDist*iRowDist)/
	    (2*(uRow-iRow)));
  }

  // Algorithm in two rounds; round one: along rows
  protected int[] processRows() {
    // feature transform array - row only
    this.ftRow = new int[boundary.getWidth()*boundary.getHeight()];
    // temporary column distance array
    int [] rowDist = new int[boundary.getWidth()];
    for ( int row=0; row<boundary.getHeight(); row++ ) {
      int startRow = row*boundary.getWidth();
      // Count distance from right boundary
      if ( boundary.boundary[startRow+boundary.getWidth()-1] ) { 
	rowDist[boundary.getWidth()-1] = 0;
      } else {
	rowDist[boundary.getWidth()-1] = this.inf;
      }
      for ( int col=boundary.getWidth()-2; col>=0; col-- ) {
	if ( boundary.boundary[startRow+col] ) {
	  rowDist[col] = 0;
	} else {
	  rowDist[col] = 1 + rowDist[col+1];
	}
      }
      // column for first pixel is equal distance 
      ftRow[startRow] = rowDist[0];
      // Update with distance from left boundary
      for ( int col=1; col<boundary.getWidth(); col++ ) {
	if ( col - ftRow[startRow+col-1] <= rowDist[col] ) {
	  ftRow[startRow+col] = ftRow[startRow+col-1];
	} else {
	  ftRow[startRow+col] = col+rowDist[col];
	}
      }
    }
    return this.ftRow;
  }

  // Algorithm in two rounds; round two: along cols
  protected int[][] processColumns() {
    this.ft = new int[2][boundary.getWidth()*boundary.getHeight()];
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
	ft[1][u*boundary.getWidth()+colR2] =
	  ftRow[seg[idSeg]*boundary.getWidth()+colR2]; 
	ft[0][u*boundary.getWidth()+colR2] = seg[idSeg];
	// Reached next segment?
	if ( u==val[idSeg] ) {
	  --idSeg;
	}
      }
    }
    return this.ft;
  }

}
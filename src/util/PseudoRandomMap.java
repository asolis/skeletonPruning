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

import java.util.Random;

public class PseudoRandomMap {
  public static final boolean DEBUG = false;

  // Table of RGB values
  int[] table = new int[1024];
  int[] table2 = new int[1024];
  
  int size;
  double minValue;
  double maxValue;
  double scale;

  // step must be 0..7
  public PseudoRandomMap(double _minValue, double _maxValue, int _step) {
    if ( _minValue >= _maxValue ) 
      throw new IllegalArgumentException( "maxValue <= minValue" );
    this.minValue = _minValue;
    this.maxValue = _maxValue; 
    int stepSize = Math.min(1<<_step,64);
    this.size = 1024/stepSize;
    initTable(stepSize);
    initTable2(stepSize);
    this.scale = ((double)size)/(maxValue-minValue);
    System.err.println("PseudoRandomMap: " + this.minValue +  
		       " ... " + this.maxValue +
		       "(scale: " + this.scale + 
		       " and " + this.size + ")");
  }

  public PseudoRandomMap(double _minValue, double _maxValue) {
    // Use default of 64 values -- 2^4 = steps of 16
    this(_minValue, _maxValue,4); 
  }

  protected void initTable(int stepSize) {
    Random generate = new Random();
    // random colors
    for (int pos=0; pos<table.length; ++pos) {
      table[pos] = 0xFF000000 | 
	generate.nextInt(255) << 16 |
	generate.nextInt(255) << 8 |
	generate.nextInt(255);  
    }
  }

  protected void initTable2(int stepSize) {
	    Random generate = new Random();
	    // random colors
	    for (int pos=0; pos<table2.length; ++pos) {
	      table2[pos] = 0xFF000000 | 
		generate.nextInt(255) << 16 |
		generate.nextInt(255) << 8 |
		generate.nextInt(255);  
	    }
 }

  public int get( double val ) {
    if ( DEBUG ) {
      System.err.println(val + " -> " +
			 Math.min((int)(this.scale*(val - this.minValue)),
				  this.size-1));
    }
    return table[Math.min((int)(this.scale*(val - this.minValue)),
			  this.size-1)];
  }
  public int get( double val, boolean bool ) {
	    if ( DEBUG ) {
	      System.err.println(val + " -> " +
				 Math.min((int)(this.scale*(val - this.minValue)),
					  this.size-1));
	    }
	    int temp = (int)(this.scale*(val - this.minValue));
	    return (bool)? table[Math.min(temp,this.size-1)]:
	    			  table2[Math.min(temp,this.size-1)];
	  }
  
  
}
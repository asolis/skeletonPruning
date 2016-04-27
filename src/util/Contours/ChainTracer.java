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

      
If you find this code useful in your research/software, please consider citing the following publication:

Andrés Solís Montero and Jochen Lang. "Skeleton pruning by contour approximation and the 
integer medial axis transform". Computers & Graphics, Elsevier, 2012. 

 (http://www.sciencedirect.com/science/article/pii/S0097849312000684)
 
**************************************************************************************************
**************************************************************************************************/
package util.Contours;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import imageOp.skeleton.IMA;
import imageOp.skeleton.Point;

public class ChainTracer {

	byte[] map;
	int w,h;
	final byte ENDPOINT = 4;
	final byte BPOINT   = 3;
    final byte SPOINT   = 2;
	final byte SHAPE    = 1;
	final byte BACK     = 0;
	
	int ep_count = 0;
	public ChainTracer(byte[] data,int width, int height,ArrayList<Point> ep, ArrayList<Point> bp){
		map = new byte[width*height];
		w = width;
		h = height;
		for (int i = 0; i < map.length; i++){
			map[i] = data[i];
		}
		for (Point p: ep){
			map[p.x*w+p.y] = ENDPOINT;
		}
		for (Point p: bp){
			map[p.x*w+p.y] = BPOINT;
			ep_count++;
		}
	}
	
	private boolean decrement(){
		if (ep_count > 3) {
			ep_count--;
			return true;
		}
		return false;
	}
	
	public void deleteChain(Chain chain){
	  for (Point p: chain.points){
	    map[p.x*w+p.y] = SHAPE;
	  }
		
	  Point last = chain.points.get(chain.points.size()-1);
	  // Huh? JL
	  // if (last.y == 217 && last.x == 135){
	  // int c=3333;
	  // c=3;
	  // }
	  int degree  = degree(last.x,last.y);
	  switch (degree){
	  case 0:
	    map[last.x*w+last.y] = SHAPE;
	    break;
	  case 1:
	    map[last.x*w+last.y] = ENDPOINT;
	    break;
	  case 2:
	    map[last.x*w+last.y] = SPOINT;
	    break;
	  default:
	    map[last.x*w+last.y] = BPOINT;
	    break;
	  }
	}

	public Chain getChainFromEndPoint(Point p){
	  int nextPos = p.x*w+p.y;
	  Chain r = new Chain(nextPos);
	  if (map[nextPos]!= ENDPOINT) return null;
		
	  do{
	    if (map[nextPos] == BPOINT){
	      int br = nextPos/w;
	      int bc = nextPos%w;
	      r.add(new Point(br,bc));
	      //				int dg = degree(br,bc);
	      //				if (dg < 2)
	      //					map[nextPos]=ENDPOINT;
	      //				else if(dg < 3)
	      //					map[nextPos]=SPOINT;
	      break;
				
	    }
	    int row = nextPos/w;
	    int col = nextPos%w;
	    map[nextPos] = SHAPE;
	    r.add( new Point(row,col));
	    nextPos = findNextPoint(row,col);
	  }
	  while (nextPos != -1 );
		
	  return r;
	}
	
	int findNextPoint (int row,int col) { 
		int p = row*w+col;
		int[] pos = new int[]{p-w-1, p-w, p-w+1,
							  p-1      /*, p*/      , p+1,
							  p+w-1, p+w, p+w+1};
		int index = -1;
		for (int i = 0; i < 8; i++ ){
			if (map[pos[i]] == BPOINT){
				return pos[i];
			} else if (map[pos[i]] > SHAPE ){
				index = pos[i];
			}
			
		}
		return index;
	}
	
	private int degree(int row,int col){
		int p = row*w+col;
		int[] pos = new int[]{p-w-1, p-w, p-w+1,
							  p-1      /*, p*/      , p+1,
							  p+w-1, p+w, p+w+1};
		int degree = 0;
		for (int _p : pos){
			if (map[_p] == IMA.SKEL) degree++;
		}
		return degree;
	}
}

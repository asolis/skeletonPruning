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

 **************************************************************************************************
 **************************************************************************************************/
package util.Graphics;

import java.awt.Component;
import java.awt.Shape;

public class CountBlinkingShape extends BlinkingShape implements TemporalDrawableShape{

	private int count = 5;
	
	
	public CountBlinkingShape(Component canvas, Shape shape) {
		super(canvas, shape);
		// TODO Auto-generated constructor stub
	}
	public CountBlinkingShape(Component canvas, Shape shape, int Count) {
		super(canvas, shape);
		this.count = Count;
		// TODO Auto-generated constructor stub
	}
	public void run () {
		    while (blinker != null && (count>0)) {
		    	repaintBounds();
		      
		      try {
		        Thread.sleep(laptime);
		      }
		      catch (InterruptedException e) {
		    	count = 0;
		        break;
		      }
		      visible = !visible;
		      if (visible) count--;
		    }
		    if (count <=0) {
		    	stopAnimation();
		    	visible = false;
		    	repaintBounds();
		    }
	}
	
	@Override
	public boolean Remove() {
		// TODO Auto-generated method stub
		return count<=0;
	}
	
	
	

}

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

import imageOp.ImageOp;
import java.awt.image.BufferedImage;
import java.awt.Component;



/**
 * Interface class for the final (shrink) phase of the Scale Axis Transform
 */
public class ScaleAxisTransformOp extends ScaleAxisGrowthOp implements ImageOp {

	// Attributes
	private BufferedImage image;

	private IntegerMedialAxisOp imaOp;

	private String stringShort = "SAT";

	public ScaleAxisTransformOp(BufferedImage _image) {
		super( _image );
		image = _image;
	}

	//*****************************************************
	// Interface ImageOp
	//*****************************************************
	public void setImage( BufferedImage _image ) throws IllegalArgumentException {
		// Would like to write
		// if ( !(_image instanceof image.getClass()) {
		if ( !image.getClass().getName().equals(_image.getClass().getName())) {
			throw new IllegalArgumentException("Images must be of same RTT");
		}
		image = _image;
		super.setImage( image );
	}

	public String toString() {
		return "Scale Axis Transform";
	}

	// this is appended to the new name of a window (should include toString())  
	public String getParamString() {
		StringBuilder s = new StringBuilder();
		if (!(image instanceof SAGImage )) {
			s.append(super.getParamString());
		}
		s.append(stringShort);
		s.append("(");
		s.append(")");
		return s.toString();
	}

	// returns true if operator overwrites input image
	public boolean inPlace() {
		return false;
	}

	// called before execute, this will ask the user to set some parameters
	public void guiSetup(Component parent, Object source) {
		// threshold for boundary
		try {
			if (!(image instanceof SAGImage )) {
				super.guiSetup( parent, source );
			}
			// Now we need input parameters for the IMA of the grown shape
			BoundaryImage grownShape = new 
					BoundaryImage(null, 1, 1, BufferedImage.TYPE_INT_RGB);
			imaOp = new IntegerMedialAxisOp(grownShape);
			imaOp.guiSetup( parent, source );
		} catch (Exception _e) {
			_e.printStackTrace();
		}
		return;
	}

	public BufferedImage execute() {
		SAGImage sagImg = null;
		if (image instanceof SAGImage ) {
			sagImg = (SAGImage) image;
		} else {
			sagImg = (SAGImage) super.execute();
		}
		// set the grown shape as the input for the second IMA  
		Boundary grownShape = new Boundary( sagImg, 127.5f ); 
		imaOp.setImage(grownShape.getBoundaryImage());
		IMAImage grownIMAimage =  (IMAImage) imaOp.execute();
		SAT satImg = new SAT( sagImg.sagData, grownIMAimage.imaData );
		return satImg.getSATImage();
	}
}
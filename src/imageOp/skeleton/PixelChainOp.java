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

import imageOp.ImageOp;

import java.awt.event.MouseEvent;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.awt.Component;
import java.awt.Shape;

import java.util.ArrayList;
import java.util.LinkedList;

import util.CurveFitting.NearestPoint;
import util.Graphics.Action;
import util.Graphics.ControlPointAction;
import util.Graphics.EndPointAction;


/**
 * Interface class for generating pixel chain for skeleton
 */
public class PixelChainOp extends ScaleAxisTransformOp implements ImageOp {

	// Attributes
	private BufferedImage image;
	protected Point pressedAt;
	protected boolean pressed;
	protected LinkedList<Action> actions;



	// reference to pixelChain class
	protected PixelChain pC = null;


	protected String stringShort      = "PC";

	protected int width;
	protected int height;

	public static final int BOX = 2;
	public PixelChainOp(BufferedImage _image) {
		super( _image );
		image = _image;

		pressedAt = new Point();
		pressed = false;
		actions = new LinkedList<Action>();
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
		return "Pixel Chain";
	}

	// this is appended to the new name of a window (should include toString())  
	public String getParamString() {
		StringBuilder s = new StringBuilder();
		if (!((image instanceof SATImage ) ||
				(image instanceof IMAImage ))) {
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
			if (!((image instanceof SATImage ) ||
					(image instanceof ASMImage )	||
					(image instanceof IMAImage ))) {
				super.guiSetup( parent, source );
			}
		} catch (Exception _e) {
			_e.printStackTrace();
		}
		return;
	}

	private void HanndleLineSegment(Action a, Point releasedAt, boolean DELMODE){
		boolean outside = this.pC.boundary[releasedAt.y*width+releasedAt.x];
		if (outside && DELMODE){
			a.list.remove(a.shape);			    					
		}else {
			Line2D l = (Line2D)a.shape;
			if (a.endpoint ==Action.LEFT){
				l.setLine(new Point2D.Double(releasedAt.x,releasedAt.y), l.getP2());
			} else {
				l.setLine( l.getP1(),new Point2D.Double(releasedAt.x,releasedAt.y));
			}
		}
	}
	private void HandleCubicCurve(Action a, Point releasedAt, boolean DELMODE){

		boolean outside = this.pC.boundary[releasedAt.y*width+releasedAt.x];
		CubicCurve2D s = (CubicCurve2D)a.shape;
		int index = a.list.indexOf(s);
		if (outside && DELMODE){
			if (a.endpoint == Action.LEFT){
				if (index-1 > 0){
					Shape left = a.list.get(index-1);
					if (left instanceof CubicCurve2D){
						CubicCurve2D ls = (CubicCurve2D)left;
						CubicCurve2D _newL = new CubicCurve2D.Double( ls.getX1(),ls.getY1(),ls.getCtrlX1(),ls.getCtrlY1(),s.getCtrlX2(),s.getCtrlY2(),s.getX2(),s.getY2());
						ls.setCurve(_newL);
					}else {
						//it's a QuadCurve!!
						QuadCurve2D ls = (QuadCurve2D)left;
						QuadCurve2D _newL = new QuadCurve2D.Double( ls.getX1(),ls.getY1(),ls.getCtrlX(),ls.getCtrlY(),s.getX2(),s.getY2());
						ls.setCurve(_newL);			    	
					}
				}
				a.list.remove(s);

			} else { //it's a right endpoint
				if (index+1 < a.list.size()){
					Shape right = a.list.get(index+1);
					if (right instanceof CubicCurve2D){
						CubicCurve2D rs = (CubicCurve2D)right;
						CubicCurve2D _newR = new CubicCurve2D.Double( s.getX1(),s.getY1(),s.getCtrlX1(),s.getCtrlY1(),rs.getCtrlX2(),rs.getCtrlY2(),rs.getX2(),rs.getY2());

						rs.setCurve(_newR);
					}else {
						//it's a QuadCurve!!
						QuadCurve2D rs = (QuadCurve2D)right;
						QuadCurve2D _newR = new QuadCurve2D.Double(s.getX1(),s.getY1(),rs.getCtrlX(),rs.getCtrlY(),rs.getX2(),rs.getY2());

						rs.setCurve(_newR);

					}
				}
				a.list.remove(s);
			}

		}
		else {
			if (a.endpoint == Action.LEFT){

				CubicCurve2D _newL = new CubicCurve2D.Double(releasedAt.x, releasedAt.y, s.getCtrlX1(),s.getCtrlY1(),s.getCtrlX2(),s.getCtrlY2(),s.getX2(),s.getY2());
				s.setCurve(_newL);

			} ///NOw to the right
			if (a.endpoint == Action.RIGHT){
				CubicCurve2D _newR = new CubicCurve2D.Double( s.getX1(),s.getY1(),s.getCtrlX1(),s.getCtrlY1(),s.getCtrlX2(),s.getCtrlY2(),releasedAt.x, releasedAt.y);
				s.setCurve(_newR);	
			}//Right finished;
		}


	}
	private void HandleQuadCurve(Action a, Point releasedAt, boolean DELMODE){
		//removing a quadCurve
		boolean outside = this.pC.boundary[releasedAt.y*width+releasedAt.x];
		QuadCurve2D s = (QuadCurve2D)a.shape;
		int index = a.list.indexOf(s);
		if (outside && DELMODE){
			if (a.endpoint == Action.LEFT){
				//there is somebody to the left
				if (index > 0) {
					Shape left = a.list.get(index-1);
					if (left instanceof QuadCurve2D){
						QuadCurve2D ls = (QuadCurve2D)left;
						QuadCurve2D _newL = new QuadCurve2D.Double( ls.getX1(),ls.getY1(),ls.getCtrlX(),ls.getCtrlY(),s.getX2(),s.getY2());
						a.list.remove(s);
						ls.setCurve(_newL);
					}else {
						CubicCurve2D ls = (CubicCurve2D)left;
						QuadCurve2D _newL = new QuadCurve2D.Double( ls.getX1(),ls.getY1(),s.getCtrlX(),s.getCtrlY(),s.getX2(),s.getY2());
						a.list.remove(s);
						a.list.remove(ls);
						a.list.add(index-1, _newL);
					}
				} else {
					if (index < a.list.size()-1){
						Shape right = a.list.get(index+1);			    								
						if (right instanceof QuadCurve2D){
							QuadCurve2D rs = (QuadCurve2D)right;
							QuadCurve2D _newL = new QuadCurve2D.Double( s.getX1(),s.getY1(),rs.getCtrlX(),rs.getCtrlY(),rs.getX2(),rs.getY2());
							rs.setCurve(_newL);
							a.list.remove(s);					    				    	
						} else {
							CubicCurve2D rs = (CubicCurve2D)right;
							QuadCurve2D _newL = new QuadCurve2D.Double( s.getX1(),s.getY1(),rs.getCtrlX2(),rs.getCtrlY2(),rs.getX2(),rs.getY2());a.list.remove(s);
							a.list.add(index, _newL);
							a.list.remove(right);
							a.list.remove(s);
						}
					}

				}
			}
			if (a.endpoint == Action.RIGHT){
				//there is somebody to the r
				if (index <a.list.size()-1) {
					Shape right = a.list.get(index+1);
					if (right instanceof QuadCurve2D ){
						QuadCurve2D rs = (QuadCurve2D)right;
						QuadCurve2D _newL = new QuadCurve2D.Double( s.getX1(),s.getY1(),rs.getCtrlX(),rs.getCtrlY(),rs.getX2(),rs.getY2());
						a.list.remove(s);
						rs.setCurve(_newL);
					} 
					if (right instanceof CubicCurve2D)
					{
						CubicCurve2D rs = (CubicCurve2D)right;
						QuadCurve2D _newL = new QuadCurve2D.Double( s.getX1(),s.getY1(),s.getCtrlX(),s.getCtrlY(),rs.getX2(),rs.getY2());
						a.list.add(index, _newL);
						a.list.remove(rs);
						a.list.remove(s);
					}	
				} else {
					if (index >0) {
						Shape left = a.list.get(index-1);
						if (left instanceof QuadCurve2D) a.list.remove(s);
						else {
							CubicCurve2D ls= (CubicCurve2D)left;
							QuadCurve2D _newL = new QuadCurve2D.Double( ls.getX1(),ls.getY1(),ls.getCtrlX2(),ls.getCtrlY2(),ls.getX2(),ls.getY2());
							a.list.add(index-1, _newL);
							a.list.remove(ls);
							a.list.remove(s);
						}
					} else 
						a.list.remove(s);


				}
			}




		}else {

			if (a.endpoint == Action.LEFT){
				QuadCurve2D _new = new QuadCurve2D.Double( releasedAt.x,releasedAt.y,s.getCtrlX(),s.getCtrlY(),s.getX2(),s.getY2());
				s.setCurve(_new);
			}else {
				QuadCurve2D _new = new QuadCurve2D.Double(s.getX1(),s.getY1(), s.getCtrlX(),s.getCtrlY(),releasedAt.x,releasedAt.y);
				s.setCurve(_new);
			}


		}

	}
	public void HandleControlPoint(Action a, Point releasedAt){

		if (a.shape instanceof QuadCurve2D){
			QuadCurve2D s = (QuadCurve2D)a.shape;
			QuadCurve2D _new = new QuadCurve2D.Double(s.getX1(),s.getY1(),releasedAt.x,releasedAt.y,s.getX2(),s.getY2());
			s.setCurve(_new);

		}
		if (a.shape instanceof CubicCurve2D)
		{
			CubicCurve2D s = (CubicCurve2D)a.shape;
			if (a.endpoint == Action.LEFT){
				CubicCurve2D _new  = new CubicCurve2D.Double(s.getX1(),s.getY1(),releasedAt.x,releasedAt.y,s.getCtrlX2(),s.getCtrlY2(),s.getX2(),s.getY2());
				s.setCurve(_new);
			}
			if (a.endpoint == Action.RIGHT){
				CubicCurve2D _new  = new CubicCurve2D.Double(s.getX1(),s.getY1(),s.getCtrlX1(),s.getCtrlY1(),releasedAt.x,releasedAt.y,s.getX2(),s.getY2());
				s.setCurve(_new);
			}
		}

	}
	@Override
	public void mouseDragged(MouseEvent e){


		if (pressed){


			int x = (int) (e.getX() / ri.scale);
			int y = (int) (e.getY() / ri.scale);
			if (x >= 0 && x < ri.image.getWidth() && y >= 0 && y < ri.image.getHeight()) {


				if (actions.size() > 0){
					Point releasedAt = new Point(x,y);
					//Point move = releasedAt.sub(pressedAt);

					for (Action a: actions){
						if (a instanceof EndPointAction){
							if (a.shape instanceof Line2D)       HanndleLineSegment(a, releasedAt,false);
							if (a.shape instanceof QuadCurve2D)  HandleQuadCurve(a, releasedAt,false);
							if (a.shape instanceof CubicCurve2D) HandleCubicCurve(a, releasedAt,false);//EndPointEnded;

						}
						if (a instanceof ControlPointAction)HandleControlPoint(a, releasedAt);

					}//Processed All the actions
					// remove this line comment if closest point is removed from this method : ri.repaint();
				}//there is at least one action to process	
				else {
					//show closest point 
					this.pC.closestPointToCubicBezier(new Point2D.Double(x,y));
					ri.setClosest(this.pC.pCurve);
					ri.setChain(this.pC.chain);
					ri.setPointInChain(this.pC.pChain);
					ri.setFTPoints(this.pC.ftP);
				}
				ri.repaint();
			}//Processing click 
		}//Left Mouse button

	}
	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {		  		  	
			int x = (int) (e.getX() / ri.scale);
			int y = (int) (e.getY() / ri.scale);
			if (x >= 0 && x < ri.image.getWidth() && y >= 0 && y < ri.image.getHeight()) {
				if (actions.size() > 0){
					Point releasedAt = new Point(x,y);
					//Point move = releasedAt.sub(pressedAt);

					boolean outside = this.pC.boundary[releasedAt.y*width+releasedAt.x];
					//if (outside) System.out.println("OUTSIDE !!!");
					for (Action a: actions){
						if (a instanceof EndPointAction){
							if (a.shape instanceof Line2D)       HanndleLineSegment(a, releasedAt,true);
							if (a.shape instanceof QuadCurve2D)  HandleQuadCurve(a, releasedAt,true);
							if (a.shape instanceof CubicCurve2D) HandleCubicCurve(a, releasedAt,true);//EndPointEnded;

						}
						if (a instanceof ControlPointAction)HandleControlPoint(a, releasedAt);
						if (outside) break;
					}//Processed All the actions

				}//there is at least one action to process

				ri.setPointInChain(null);
				ri.setChain(null);
				ri.setClosest(null);
				ri.setFTPoints(null);
				pressed = false;
				actions.clear();
				ri.repaint();
			}//Processing click 
		}//Left Mouse button

	}//mousedReleased
	@Override
	public void mousePressed(MouseEvent e){
		if (e.getButton() == MouseEvent.BUTTON1) {


			int x = (int) (e.getX() / ri.scale);
			int y = (int) (e.getY() / ri.scale);
			if (x >= 0 && x < ri.image.getWidth() && 
					y >= 0 && y < ri.image.getHeight()) {
				pressed = true;
				pressedAt = new Point(x,y);

				//image.getGraphics().drawRect(20, 90, 59, 100);
				outerloop:
					for (ArrayList<Shape> al: pC.shapes){
						for (Shape s: al){
							if (s instanceof QuadCurve2D){
								QuadCurve2D q2 = (QuadCurve2D)s;

								if (Math.abs(x - q2.getCtrlX()) <=BOX &&
										Math.abs(y - q2.getCtrlY()) <=BOX){
									actions.add(new ControlPointAction(al, q2, pressedAt,Action.NONE));
									if (e.isShiftDown())
										break outerloop;
									// System.out.println("control point of QuadCurve");
								}
								if (Math.abs(x - q2.getX1())    <=BOX  &&
										Math.abs(y - q2.getY1())    <= BOX){
									actions.add(new EndPointAction(al, q2, pressedAt,Action.LEFT));
									//System.out.println("X1 of QuadCurve");
									if (e.isShiftDown())
										break outerloop;
								}
								if (Math.abs(x - q2.getX2())    <=BOX  &&
										Math.abs(y - q2.getY2())    <= BOX){
									actions.add(new EndPointAction(al, q2, pressedAt,Action.RIGHT));
									// System.out.println("X1 of QuadCurve");
									if (e.isShiftDown())
										break outerloop;
								}

							}
							if (s instanceof CubicCurve2D){
								CubicCurve2D c2 = (CubicCurve2D)s;

								if (Math.abs(x - c2.getCtrlX1()) <=BOX &&
										Math.abs(y - c2.getCtrlY1()) <=BOX){
									actions.add(new ControlPointAction(al, c2, pressedAt, Action.LEFT));
									//System.out.println("control point of 1 CubicCurve2D");
									if (e.isShiftDown())
										break outerloop;
								}
								if (Math.abs(x - c2.getCtrlX2()) <=BOX &&
										Math.abs(y - c2.getCtrlY2()) <=BOX){
									actions.add(new ControlPointAction(al, c2, pressedAt,Action.RIGHT));
									// System.out.println("control point 2 of CubicCurve2D");
									if (e.isShiftDown())
										break outerloop;
								}
								if (Math.abs(x - c2.getX1())    <=BOX  &&
										Math.abs(y - c2.getY1())    <= BOX){
									actions.add(new EndPointAction(al, c2, pressedAt,Action.LEFT));
									//System.out.println("X1 of QuadCurve");
									if (e.isShiftDown())
										break outerloop;
								}
								if (Math.abs(x - c2.getX2())    <=BOX  &&
										Math.abs(y - c2.getY2())    <= BOX){
									actions.add(new EndPointAction(al, c2, pressedAt, Action.RIGHT));
									// System.out.println("X2 of QuadCurve");
									if (e.isShiftDown())
										break outerloop;
								}
							}
							if (s instanceof Line2D){
								Line2D l2 = (Line2D) s;
								if (Math.abs(x - l2.getX1())    <=BOX  &&
										Math.abs(y - l2.getY1())    <= BOX){		    					   
									actions.add(new EndPointAction(al, l2, pressedAt,Action.LEFT));
									//System.out.println("X1 of Line");
									if (e.isShiftDown())
										break outerloop;
								}
								if (Math.abs(x - l2.getX2())    <=BOX  &&
										Math.abs(y - l2.getY2())    <= BOX){
									actions.add(new EndPointAction(al, l2, pressedAt,Action.RIGHT));
									//System.out.println("X2 of Line");
									if (e.isShiftDown())
										break outerloop;
								}
							}

						}
					}
			}
		}
	}



	public BufferedImage execute() {
		if ( image instanceof SATImage )
			this.pC = new PixelChain((SATImage)image);
		else if (image instanceof ASMImage) 
			this.pC = new PixelChain((ASMImage)image);
		else if (image instanceof IMAImage) 
			this.pC = new PixelChain((IMAImage)image);
		else 
			this.pC = new PixelChain((SATImage)super.execute());

		width 	 = this.pC.getWidth();
		height 	 = this.pC.getHeight();
		PCImage pcI = this.pC.getChainImage();

		return pcI;
	}
	@Override
	public void setParentComponent(Component parent) {
		super.setParentComponent(parent);
		if ( parent instanceof viewer.ImageWindow){
			ri = ((viewer.ImageWindow)parent).imagePanel;
			ri.setCurves(pC.shapes);
			ri.repaint();
		}
	}
}
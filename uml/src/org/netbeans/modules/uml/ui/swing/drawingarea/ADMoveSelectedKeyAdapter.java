/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uml.ui.swing.drawingarea;


import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Iterator;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
//import com.tomsawyer.util.TSPoint;
import com.tomsawyer.drawing.geometry.TSPoint;
import com.tomsawyer.diagramming.TSMoveControl;
import com.tomsawyer.drawing.*;
import com.tomsawyer.editor.*;
import com.tomsawyer.editor.command.TSEMoveGroupCommand;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.IMessageEdgeDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.SmartDragTool;


/**
 * This is a key adapter that allows the user to move selected objects
 * using Ctrl- arrow keys.
 */
//public class ADMoveSelectedKeyAdapter extends TSEWindowState
public class ADMoveSelectedKeyAdapter extends TSEWindowTool
{
	/**
	 * This constructor creates a new key adapter attached to the specified
	 * graph window.
	 */
	public ADMoveSelectedKeyAdapter(TSEGraphWindow graphWindow)
	{
		this.setGraphWindow(graphWindow);
		this.state = ADMoveSelectedKeyAdapter.DONE;
		this.setXStep(this.getDefaultXStep());
		this.setYStep(this.getDefaultYStep());
	}


        private IMessageEdgeDrawEngine selectedMessage = null;
        private SmartDragTool messageTool = null;
        
	/**
	 * This method initializes the adapter to begin moving the selected
	 * objects.
	 */
	public void initMove()
	{
                TSEGraphManager graphManager = this.getGraphWindow().getGraphManager();
                List selectedNodes = graphManager.selectedNodes();
                List selctedPathNodes = graphManager.selectedPathNodes(true);
                List selectedEdgeLabels = graphManager.selectedEdgeLabels(true);
                List selctedNodeLabels = graphManager.selectedNodeLabels();
                List selectedConnectorLabels = graphManager.selectedConnectorLabels();   
                
                selectedMessage = null;
                messageTool = null;
                moveControl = null;
                
                selectedMessage = getSelectedMessage();
                if(selectedMessage != null)
                {
                    int x = (int)selectedMessage.getEdge().getSourceConnector().getCenterX() + 10;
                    int y = (int)selectedMessage.getEdge().getSourceConnector().getCenterY();
                    
                    System.out.printf("Location (%d, %d)\n", x, y);
                    Point devicePt = getGraphWindow().getTransform().pointToDevice(x, y);
                    MouseEvent event = new MouseEvent(getGraphWindow(), 
                                                      MouseEvent.MOUSE_CLICKED,
                                                      0, // When - Item (don't care)
                                                      0, // modifiers
                                                      (int)devicePt.getX(), 
                                                      (int)devicePt.getY(),
                                                      1, // click count
                                                      false,
                                                      MouseEvent.BUTTON1);
                    selectedMessage.handleLeftMouseButtonPressed(event);    
                    
                    TSEWindowTool currentTool = getGraphWindow().getCurrentTool();
                    if(currentTool instanceof SmartDragTool)
                    {
                        messageTool = (SmartDragTool)currentTool;
                    }
                    
                    this.startPoint = new TSConstPoint(x, y);
                    this.endPoint = new TSPoint(this.startPoint);
                }
                else
                {                 
                    this.moveControl = new TSMoveControl();
                    this.moveControl.init(graphManager.graphs(false),
                                          selectedNodes, 
                                          selctedPathNodes,  
                                          selectedEdgeLabels,  
                                          selctedNodeLabels, 
                                          selectedConnectorLabels);

                    TSDNode node =
                            (TSDNode) this.getGraphWindow().getGraph().
                                    nodes().get(0);

                    // start point can be any point on the graph, which only
                    // serves as a reference.

                    this.startPoint = new TSConstPoint(node.getCenterX(),
                            node.getCenterY());
                    this.endPoint = new TSPoint(this.startPoint);

                    this.moveControl.onStartAt(this.startPoint.getX(),
                            this.startPoint.getY());
                }
                
                // initialize this variable to 0 before any move.
                
                this.lastMove = 0;
                this.lastExtremePoint = new TSPoint();
	}


	/**
	 * This method moves the selected objects in the specified <code>
	 * direction</code>.
	 */
	public void move(int direction)
        {
            if ((this.state == this.DONE) &&
                (this.getGraphWindow().getGraphManager().hasSelected(true)))
            {
                this.initMove();
                this.state = this.MOVING;
            }
            else if (!this.getGraphWindow().getGraphManager().hasSelected(true))
            {
                return;
            }
            
            double x = this.endPoint.getX();
            double y = this.endPoint.getY();
            
            if (direction == this.UP)
            {
                y = this.getAlignedWorldY(this.UP);
            }
            else if (direction == this.DOWN)
            {
                y = this.getAlignedWorldY(this.DOWN);
            }
            else if (direction == this.LEFT)
            {
                x = this.getAlignedWorldX(this.LEFT);
            }
            else if (direction == this.RIGHT)
            {
                x = this.getAlignedWorldX(this.RIGHT);
            }
            
            this.updateGraphInvalidRegions();
            
            if(moveControl != null)
            {
                this.moveControl.onDragTo(x, y);
                this.moveControl.updateGraphsBounds();
            }
            else if(messageTool != null)
            { 
                System.out.printf("New Location (%d,%d)\n", (int)x, (int)y);
                Point devicePt = getGraphWindow().getTransform().pointToDevice(x, y);
                MouseEvent event = new MouseEvent(getGraphWindow().getCanvas(),  
                                                  506, //MouseEvent.MOUSE_CLICKED,
                                                  0, // When - Item (don't care)
                                                  1040, // modifiers
                                                  (int)devicePt.getX(), 
                                                  (int)devicePt.getY(),
                                                  1, // click count
                                                  false,
                                                  MouseEvent.BUTTON1);
                messageTool.onMouseDragged(event);
                messageTool.onMouseReleased(event);
            }
            
            this.updateGraphInvalidRegions();

            if (!this.updateVisibleArea(direction, true))
            {
                this.getGraphWindow().updateInvalidRegions(true);
            }
        }


	/**
	 * This method finalizes this state. The moving of the selected
	 * objects is commited through this method.
	 */
	public void finalizeState()
	{
		if (this.state == this.MOVING)
		{
                    if(moveControl != null)
                    {
			this.moveControl.onCancel();
                    }
                    else if(messageTool != null)
                    {
                        getGraphWindow().switchTool(getGraphWindow().getDefaultTool());
                    }
                        
			this.commitMoving();

			this.state = this.DONE;
			this.lastMove = 0;
			this.lastExtremePoint = null;
		}
	}


	/**
	 * This method returns the state of the move selected operation,
	 * i.e., whether it is in process, or already finished.
	 */
	public int getState()
	{
		return this.state;
	}


	/**
	 * This method registers all rectangular regions affected by the
	 * move with the graph window.
	 */
	void updateGraphInvalidRegions()
	{
            if(moveControl != null)
            {
		// first all dragged edges...

		this.getGraphWindow().addInvalidRegion(
			this.moveControl.draggedEdges());

		// ...and all their labels...

		for (Iterator edgeIter =
			this.moveControl.draggedEdges().iterator();
			edgeIter.hasNext();)
		{
			TSEEdge edge = (TSEEdge) edgeIter.next();

			this.getGraphWindow().addInvalidRegion(edge.labels());
		}

		// ...then dragged nodes...

		this.getGraphWindow().addInvalidRegion(
			this.moveControl.draggedNodes());

		// ...and all their labels...

		for (Iterator nodeIter =
				this.moveControl.draggedNodes().iterator();
			nodeIter.hasNext();)
		{
			TSENode node = (TSENode) nodeIter.next();

			this.getGraphWindow().addInvalidRegion(node.labels());
		}

		// ...then dragged edge labels...

		this.getGraphWindow().addInvalidRegion(this.moveControl.draggedEdgeLabels());

		// ...then dragged node labels...

		this.getGraphWindow().addInvalidRegion(this.moveControl.draggedNodeLabels());

		// ...and finally dragged bends.

		this.getGraphWindow().addInvalidRegion(this.moveControl.draggedPathNodes());
            }
            else if(selectedMessage != null)
            {
                getGraphWindow().addInvalidRegion(selectedMessage.getEdge());
            }
	}


	/**
	 * This method returns the amount by which the selected objects
	 * are moved on the X axis per step.
	 */
	public double getXStep()
	{
		return this.xStep;
	}


	/**
	 * This method sets the amount by which the selected objects
	 * are moved on the X axis per step.
	 */
	public void setXStep(double xStep)
	{
		this.xStep = xStep;
	}


	/**
	 * This method returns the default amount by which the selected
	 * objects are moved on the X axis per step.
	 * @return 1.
	 */
	public double getDefaultXStep()
	{
		return 5;
	}


	/**
	 * This method returns the amount by which the selected objects
	 * are moved on the Y axis per step.
	 */
	public double getYStep()
	{
		return this.yStep;
	}


	/**
	 * This method sets the amount by which the selected objects
	 * are moved on the Y axis per step.
	 */
	public void setYStep(double yStep)
	{
		this.yStep = yStep;
	}


	/**
	 * This method returns the default amount by which the selected
	 * objects are moved on the Y axis per step.
	 * @return 1.
	 */
	public double getDefaultYStep()
	{
		return 5;
	}


	/**
	 * This method returns the X coordinate of the end point after a 
	 * move by one arrow key event. The X coordinate is not aligned
	 * to the grid.
	 */
	public double getNonalignedWorldX(int direction)
	{
		double step;

		if (direction == this.LEFT)
		{
			this.endPoint.setX(this.endPoint.getX() - this.getXStep());
		}
		else if (direction == this.RIGHT)
		{
			this.endPoint.setX(this.endPoint.getX() + this.getXStep());
		}

		return this.endPoint.getX();
	}


	/**
	 * This method returns the Y coordinate of the end point after a 
	 * move by one arrow key event. The Y coordinate is not aligned
	 * to the grid.
	 */
	public double getNonalignedWorldY(int direction)
	{
		double step;

		if (direction == this.UP)
		{
			this.endPoint.setY(this.endPoint.getY() + this.getYStep());
		}
		else if (direction == this.DOWN)
		{
			this.endPoint.setY(this.endPoint.getY() - this.getYStep());
		}

		return this.endPoint.getY();
	}


	/**
	 * This method returns the X coordinate of the end point after a 
	 * move by one arrow key event. The X coordinate is aligned
	 * to the grid.
	 */
	public double getAlignedWorldX(int direction)
	{
		double x = this.getNonalignedWorldX(direction);

		if (this.getGraphWindow().hasGrid())
		{
			// we snap the end point to the closest grid based on the
			// direction.

			if (direction == this.LEFT)
			{
				this.endPoint.setX(this.getGraphWindow().getGrid().
					getLeftNearestGridX(x));

				x = this.endPoint.getX();
			}
			else if (direction == this.RIGHT)
			{
				this.endPoint.setX(this.getGraphWindow().getGrid().
					getRightNearestGridX(x));

				x = this.endPoint.getX();
			}
		}

		return (x);
	}


	/**
	 * This method returns the y coordinate of the end point after a 
	 * move by one arrow key event. The y coordinate is aligned
	 * to the grid.
	 */
	public double getAlignedWorldY(int direction)
	{
		double y = this.getNonalignedWorldY(direction);

		if (this.getGraphWindow().hasGrid())
		{
			// we snap the end point to the closest grid based on the
			// direction.

			if (direction == this.UP)
			{
				this.endPoint.setY(this.getGraphWindow().getGrid().
					getUpperNearestGridY(y));

				y = this.endPoint.getY();
			}
			else if (direction == this.DOWN)
			{
				this.endPoint.setY(this.getGraphWindow().getGrid().
					getLowerNearestGridY(y));

				y = this.endPoint.getY();
			}
		}

		return (y);
	}


	/**
	 * This method updates the visible area of the graph window, if
	 * necessary, in order to ensure that the moved objects are within
	 * it. If the moved objects are outside the visible area, the 
	 * window is automatically scrolled by the amount set by <code>
	 * setScrollingStep</code> in the parent class. The graph window
	 * is then redrawn and repainted if requested.
	 */
	public boolean updateVisibleArea(int direction, boolean redraw)
	{
		// here, we need to find out the farthest graph object in the
		// direction of movement, and update the visible area on that
		// graph object.

		TSConstPoint extremePoint = this.getExtremePoint(direction);

		return super.updateVisibleArea(extremePoint, redraw);

	}


	/**
	 * This method returns the extreme point of selected graph objects
	 * in the direction of movement.
	 */
	public TSConstPoint getExtremePoint(int direction)
	{
		if (this.lastMove != 0 &&
			this.lastMove == direction &&
			this.lastExtremePoint != null)
		{
			return this.lastExtremePoint;
		}

		TSSolidGeometricObject extremeObject = null;
		double extremeValue;

		// first initialize the extremeValue to the infinity value
		// of its opposite direction.

		if ((direction == this.UP) || (direction == this.RIGHT))
		{
			extremeValue = Double.NEGATIVE_INFINITY;
		}
		else
		{
			extremeValue = Double.POSITIVE_INFINITY;
		}

		// this is the owner graph manager.
		
		TSEGraphManager graphManager =
			this.getGraphWindow().getGraphManager();

		for (Iterator nodeIter =
			graphManager.selectedNodes().iterator();
			nodeIter.hasNext();)
		{
			TSDNode node = (TSDNode) nodeIter.next();

			extremeValue = this.adjustExtremeValue(node,
				direction,
				extremeValue,
                                node.getCenterX(),
                                node.getCenterY());
		}

		for (Iterator bendIter =
			graphManager.selectedPathNodes(true).iterator();
			bendIter.hasNext();)
		{
			TSPNode bend = (TSPNode) bendIter.next();

			extremeValue = this.adjustExtremeValue(bend,
				direction,
				extremeValue,
                                bend.getCenterX(),
                                bend.getCenterY());
		}

		for (Iterator labelIter =
			graphManager.selectedEdgeLabels(true).iterator();
			labelIter.hasNext();)
		{
			TSEdgeLabel edgeLabel = (TSEdgeLabel) labelIter.next();

			extremeValue = this.adjustExtremeValue(edgeLabel,
				direction,
				extremeValue,
                                edgeLabel.getCenterX(),
                                edgeLabel.getCenterY());
		}

		for (Iterator labelIter =
			graphManager.selectedNodeLabels().iterator();
			labelIter.hasNext();)
		{
			TSNodeLabel nodeLabel = (TSNodeLabel) labelIter.next();

			extremeValue = this.adjustExtremeValue(nodeLabel,
				direction,
				extremeValue,
                                nodeLabel.getCenterX(),
                                nodeLabel.getCenterY());
		}
                
                for(Object curEdge : graphManager.selectedEdges(true))
		{
			TSEEdge edge = (TSEEdge) curEdge;

			extremeValue = this.adjustExtremeValue(edge,
				direction,
				extremeValue,
                                edge.getRight() + (edge.getWidth() / 2),
                                edge.getTop() - (edge.getHeight() / 2));
		}

		return this.lastExtremePoint;
	}


	/**
	 * This method returns the extreme value changed by the specified 
	 * graph object in the specified direction of movement. The last
	 * extreme point is also adjusted.
	 */
	double adjustExtremeValue(TSGeometricObject object,
		int direction,
		double extremeValue,
                double centerX,
                double centerY)
	{
		if (direction == this.UP)
		{
			if (object.getTop() > extremeValue)
			{
				this.lastExtremePoint.setLocation(centerX,
					object.getTop());
				return object.getTop();
			}
		}
		else if (direction == this.DOWN)
		{
			if (object.getBottom() < extremeValue)
			{
				this.lastExtremePoint.setLocation(centerX,
					object.getBottom());
				return object.getBottom();
			}
		}
		else if (direction == this.LEFT)
		{
			if (object.getLeft() < extremeValue)
			{
				this.lastExtremePoint.setLocation(object.getLeft(),
					centerY);
				return object.getLeft();
			}
		}
		else if (direction == this.RIGHT)
		{
			if (object.getRight() > extremeValue)
			{
				this.lastExtremePoint.setLocation(object.getRight(),
					centerY);
				return object.getRight();
			}
		}

		return extremeValue;
	}


	/**
	 * This method commits the movement of the objects from the start
	 * point to the end point. It changes the position by issuing
	 * a TSEMoveGroupCommand.
	 */
	public void commitMoving()
	{
		TSEGraphManager graphManager =
			this.getGraphWindow().getGraphManager();
/*
		this.getGraphWindow().transmit(new TSEMoveGroupCommand(
			(List) graphManager.graphs(false),
			graphManager.selectedNodes(),
			graphManager.selectedPathNodes(true),
			graphManager.selectedEdgeLabels(true),
			graphManager.selectedNodeLabels(),
			this.startPoint,
			this.endPoint,
			this.getGraphWindow()));
 */
                this.getGraphWindow().transmit(
                    new TSEMoveGroupCommand(
			(List) graphManager.graphs(false),
			graphManager.selectedNodes(),
			graphManager.selectedPathNodes(true),
			graphManager.selectedEdgeLabels(true),
			graphManager.selectedNodeLabels(),
                        graphManager.selectedConnectorLabels(),
			(TSConstPoint) this.startPoint,
			(TSConstPoint) this.endPoint)
                    );
	}

    private IMessageEdgeDrawEngine getSelectedMessage()
    {
        IMessageEdgeDrawEngine retVal = null;
        
        TSEGraphManager graphManager = this.getGraphWindow().getGraphManager();
        
        List edges = graphManager.selectedEdges(true);
        
        // We can only move one message at a time.  Otherwise all of the bumping
        // may start affecting each other.
        if(edges.size() == 1)
        {
            ETEdge edge = (ETEdge)edges.get(0);
            IDrawEngine engine = edge.getEngine();
            
            if(engine instanceof IMessageEdgeDrawEngine)
            {
                retVal = (IMessageEdgeDrawEngine)engine;
            }
        }
        
        return retVal;
    }


// ---------------------------------------------------------------------
// Section: Class variables
// ---------------------------------------------------------------------

	/**
	 * This constant signifies that the move is to the UP
	 * side.
	 */
	public static final int UP = 1;

	/**
	 * This constant signifies that the move is to the DOWN
	 * side.
	 */
	public static final int DOWN = 2;

	/**
	 * This constant signifies that the move is to the LEFT
	 * side.
	 */
	public static final int LEFT = 3;

	/**
	 * This constant signifies that the move is to the RIGHT
	 * side.
	 */
	public static final int RIGHT = 4;

	/**
	 * This constant signifies that the move selected operation is
	 * in process.
	 */
	public static final int MOVING = 10;

	/**
	 * This constant signifies that the move selected operation is
	 * done.
	 */
	public static final int DONE = 20;


// ---------------------------------------------------------------------
// Section: Instance variables
// ---------------------------------------------------------------------

	/**
	 * This variable stores the move control object.
	 */
	TSMoveControl moveControl;

	/**
	 * This variable stores the start point.
	 */
	TSConstPoint startPoint;

	/**
	 * This variable stores the end point.
	 */
	TSPoint endPoint;

	/**
	 * This variable stores the x step.
	 */
	double xStep;

	/**
	 * This variable stores the y step.
	 */
	double yStep;

	/**
	 * This variable is the state variable that signifies whether the
	 * move selected operation is in process or not.
	 */
	int state;

	/**
	 * This variable records the direction of last move.
	 */
	int lastMove;

	/**
	 * This variable records the extreme point in the last move.
	 */
	TSPoint lastExtremePoint;
}

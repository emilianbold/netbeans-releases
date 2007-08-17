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

import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.WeakHashMap;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleAction;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleComponent;
import javax.accessibility.AccessibleSelection;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETClassDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETGraph;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETGraphManager;
import org.netbeans.modules.uml.ui.support.accessibility.AccessibleSelectionParent;
import org.netbeans.modules.uml.ui.support.helpers.ETSmartWaitCursor;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import com.tomsawyer.drawing.TSLabel;
import com.tomsawyer.drawing.command.TSDeleteNodeCommand;
import com.tomsawyer.drawing.geometry.TSConstRect;
import com.tomsawyer.editor.TSEGraph;
import com.tomsawyer.editor.TSEGraphManager;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.editor.TSEHitTesting;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.TSEObjectUI;
import com.tomsawyer.editor.TSEPreferences;
//import com.tomsawyer.editor.TSEWindowState;
import com.tomsawyer.editor.TSEWindowTool;
import com.tomsawyer.editor.event.TSEEventManager;
import com.tomsawyer.editor.event.TSESelectionChangeEvent;
import com.tomsawyer.editor.event.TSESelectionChangeEventData;
import com.tomsawyer.editor.event.TSESelectionChangeListener;
import com.tomsawyer.editor.event.TSEViewportChangeEvent;
import com.tomsawyer.editor.event.TSEViewportChangeListener;
import com.tomsawyer.editor.graphics.TSEGraphics;
import com.tomsawyer.editor.ui.TSEGraphUI;
import com.tomsawyer.graph.TSGraphObject;
import com.tomsawyer.graph.event.TSGraphChangeEvent;
import com.tomsawyer.graph.event.TSGraphChangeListener;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.util.TSObject;
import com.tomsawyer.util.command.TSCommand;
import com.tomsawyer.util.command.TSGroupCommand;
import com.tomsawyer.editor.TSEInnerCanvas;
import java.awt.Image;
import java.awt.image.BufferedImage;

/**
 * This class extends the main toolkit class TSEGraphWindow.
 */
public class ADGraphWindow extends TSEGraphWindow implements ActionListener, Accessible
{
    String status;
    boolean changed;

    String graphFileName;
    protected boolean hasFileName;
    ADMoveSelectedKeyAdapter moveAdapter;
    boolean needsFitInWindow = true;
    IDrawingAreaControl m_drawingAreaCtrl = null;
    public static final int MAX_ZOOM = 5000;
    public static final double MIN_ZOOM = 0.01;
    private boolean allowRedraw = true;
    private boolean bUpdatingScrollBars = false;

    private TSEHitTesting m_hitTesting;
    
	

   protected class ADDrawingPreferences extends com.tomsawyer.editor.TSEPreferences
   {
      ADDrawingPreferences(TSEGraphWindow graphWindow)
      {
         super(graphWindow);
      }

      protected void modifyDefaults()
      {
         super.modifyDefaults();
      }

      public void setDefaults()
      {
         super.setDefaults();
         // this is causing exceptions.
         //setDrawFullUIOnDragging(true);
         //setAutoHidingScrollBars(true);

         //			setValue(DRAW_INVISIBLE, true);
         //			setValue(AUTO_HIDE_SCROLLBARS, true);
      }
   }

   public ADGraphWindow(IDrawingAreaControl drawingArea)
   {
      this(drawingArea, null, true);

   }

   public ADGraphWindow(IDrawingAreaControl drawingArea, ETGraphManager graphMgr)
   {
      this(drawingArea, graphMgr, true);
   }

   public ADGraphWindow(IDrawingAreaControl drawingArea, ETGraphManager graphMgr, boolean withScrollbars)
   {
      this(graphMgr, withScrollbars);    
      
      this.setDrawFullUIOnDragging(false);
      
      //JM: Fix for Bug#6315533 - Provide vicinity feature for connector/relationship/link elements in drawing area  
      this.setHitTolerance(5);            
      m_drawingAreaCtrl = drawingArea;
   }

   public TSEPreferences newPreferences(TSEGraphWindow graphWindow)
   {
      return new ADDrawingPreferences(graphWindow);
   }

   protected ADGraphWindow(ETGraphManager graphManager, boolean withScrollbars)
   {
      super(graphManager, withScrollbars);
      
      //JM: Temp Fix for #6412795 (till we move to the next version of TomSawyer) 
      //- Provide a fix for UML drawing slownes without the sun.java2d.pmoffscreen flag.
      this.setCanvas(new TSEInnerCanvas(this) {
          public Image createImage(int width, int height) {
              return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
          }
      });

      this.moveAdapter = new ADMoveSelectedKeyAdapter(this);

      this.addComponentListener(new ComponentAdapter()
      {
         /**
          * This method makes sure fitInWindow is called
          * when the graph window is resized and auto fit in
          * window is on.
          */
         public void componentResized(ComponentEvent e)
         {
            if (getDrawingArea() != null && getDrawingArea().isAutoFitInWindow())
            {
               ADGraphWindow.this.fitInWindow(true);
            }

            // This is a work-around to get rid of repaint problem
            // of scroll bars when the component is resized.

            if (ADGraphWindow.this.isAutoHidingScrollBars())
            {
               if (ADGraphWindow.this.getHorizontalScrollBar().isVisible())
               {
                  ADGraphWindow.this.getHorizontalScrollBar().revalidate();
               }

               if (ADGraphWindow.this.getVerticalScrollBar().isVisible())
               {
                  ADGraphWindow.this.getVerticalScrollBar().revalidate();
               }
            }
         }
      });

      m_hitTesting = new TSEHitTesting(this);
      setToolTipShown(true);
   }
   
   /**
    * This overide protects our wait cursor from going away during CDFS
    */
   public void setCursorOnCanvas(Cursor cursor)
   {
      if( ! ETSmartWaitCursor.inWaitState() )
      {
         super.setCursorOnCanvas( cursor );
      }
   }
   
   public void setupETDefaultDrawingPreferences()
   {
      ETSystem.out.println("ADGraphWindow setupETDefaultDrawingPreferences");
      getPreferences().setValue(com.tomsawyer.editor.TSEPreferences.RECONNECT_EDGE_SENSITIVITY, 10.0);

      // limit the max zoom level to the current value set in Tomahawk
      setMaxZoomLevel(MAX_ZOOM / 100);

      // limit the min zoom level to the current value set in Tomahawk
      setMinZoomLevel(MIN_ZOOM / 100);

      this.setupCommonLayoutProperties();
   }

   /*
    * Layout properties that all diagrams share.
    */
   protected void setupCommonLayoutProperties()
   {
      // Orthongonal layout will resize nodes by default, tell the layout engine to
      // respect the shape of our nodes.
      setOrthogonalKeepNodeSizes(true);
   }

   /*
    * Sets the OrthogonalKeepNodeSizes layout property, 
    */
   public void setOrthogonalKeepNodeSizes(boolean value)
   {
      TSEGraph graph = this.getGraph();
      if (graph != null)
      {/*
         TSBooleanLayoutProperty property = new TSBooleanLayoutProperty(TSTailorProperties.ORTHOGONAL_KEEP_NODE_SIZES);
         property.setCurrentValue(value);
         graph.setTailorProperty(property);
        */ //JM - to be done
      }
   }

   public void reshape(int x, int y, int width, int height)
   {
      super.reshape(x, y, width, height);
   }

   public IDrawingAreaControl getDrawingArea()
   {
      return m_drawingAreaCtrl;
   }

   public TSEGraphManager newGraphManager()
   {
      return (new ETGraphManager());

   }

   /**
    * This method expands all the selected nodes of the graphs
    * which are owned by the current graph manager.
    */
   public void expandSelected()
   {
   }

   /**
    * This method expands all the nodes of the graphs which are owned
    * by the current graph manager.
    */
   public void expandAll()
   {
   }

   /**
    * This method collapses all the selected nodes of the graphs
    * which are owned by the current graph manager.
    */
   public void collapseSelected()
   {
   }

   /**
    * This method collapses all the nodes of the graphs which are owned
    * by the current graph manager.
    */
   public void collapseAll()
   {
   }

   /**
    * This method hides all selected objects in the current graph
    * and nested graphs.
    */
   public void hideSelected()
   {
   }

   /**
    * This method unhides all hidden objects in the current graph
    * and nested graphs.
    */
   public void unhideAll()
   {
   }

   /**
    * This method folds selected objects into new folders in owner
    * graphs.
    */
   public void foldSelected()
   {
   }

   /**
    * This method unfolds all folders.
    */
   public void unfoldAll()
   {
   }

   /**
    * This method unfolds selected folders.
    */
   public void unfoldSelected()
   {
   }

   /**
    * This method sets the status of this graph window.
    */
   public void setStatus(String status)
   {
      this.status = status;
   }

   /**
    * This method returns the status of this graph window.
    */
   public String getStatus()
   {
      return this.status;
   }

   /**
    * This method sets the flag that stores whether this graph window
    * is displaying a graph that has changed since it was last saved.
    */
   public void setChanged(boolean changed)
   {
      this.changed = changed;
   }

   /**
    * This method returns whether this graph window is displaying a graph
    * that has changed since it was last saved.
    */
   public boolean isChanged()
   {
      return this.changed;
   }

   /**
    * This method sets the absolute pathname of the graph manager being
    * displayed by this graph window.
    */
   public void setGraphFileName(String filename)
   {
      this.graphFileName = filename;
   }

   /**
    * This method gets the absolute pathname of the graph file displayed
    * by this graph window.
    */
   public String getGraphFileName()
   {
      return this.graphFileName;
   }

   /**
    * This method returns the boolean value indicating whether the given
    * file is the same file as this graph window's graph file
    */
   protected boolean ownsSameGraphFile(String file)
   {
      if (this.hasFileName == false)
      {
         return false;
      }
      else if (this.graphFileName == null)
      {
         return (file == null);
      }
      else
      {
         return (this.graphFileName.equalsIgnoreCase(file));
      }
   }

   /**
    * This method moves the selected objects in the specified <code>
    * direction</code>.
    */
   public void move(int direction)
   {
      if (this.moveAdapter != null)
      {
         this.moveAdapter.move(direction);
      }
   }

   /**
    * This method finalizes the movement of selected objects
    */
   public void finalizeMove()
   {
      if (this.moveAdapter != null)
      {
         this.moveAdapter.finalizeState();
      }
   }

   public void actionPerformed(ActionEvent event)
   {
      String action = event.getActionCommand();

      ETSystem.out.println("ADGraphWindow - " + action);
   }

//   public void switchState(TSEWindowState newState)
//   public void switchState(TSEWindowTool newState)
   public void switchTool(TSEWindowTool newState)
   {
      //super.switchState(newState);
       super.switchTool(newState);
   }

   /* (non-Javadoc)
    * @see com.tomsawyer.util.command.TSCommandListener#listen(com.tomsawyer.util.command.TSCommand)
    */
   public int listen(TSCommand cmd)
   {
      if (cmd instanceof TSGroupCommand)
      {
         TSGroupCommand groupCmd = (TSGroupCommand) cmd;
         List cmds = groupCmd.getCommandList();
         for (Iterator iter = cmds.iterator(); iter.hasNext();)
         {
            TSCommand curCommand = (TSCommand) iter.next();
            handleCommandEvent(curCommand);
         }
      }
      else
      {
         handleCommandEvent(cmd);
      }
      
      int retval = 0;
      try
      {
         retval = super.listen(cmd);
      }
      catch( Exception e )
      {
         // Fix J2221:  This code still throws an exception in TS when the message-to-self is involved.
         //             So, we catch the exception here.
         //             The use case comes from SmartDragTool.onMouseReleased() when the user
         //             moves the top of the messagee-to-self.
         // UPDATE:  We should probably figure out why the exception is being thrown in TS.
      }
      
      return retval;
   }

   protected void handleCommandEvent(TSCommand cmd)
   {
      if (cmd instanceof TSDeleteNodeCommand)
      {
         TSDeleteNodeCommand command = (TSDeleteNodeCommand) cmd;

         ETList < IETGraphObject > deletedObject = new ETArrayList < IETGraphObject > ();

         deletedObject.add(TypeConversions.getETGraphObject(command.getNode()));
         getDrawingArea().onGraphEvent(IGraphEventKind.GEK_POST_DELETE, null, null, deletedObject);
      }
   }

   /* (non-Javadoc)
    * @see com.tomsawyer.editor.TSEGraphWindow#deleteSelected()
    */
   public void deleteSelected()
   {
      TSEGraph graph = this.getGraph();
      if (graph != null && graph.hasSelected())
         super.deleteSelected();
   }

   /* (non-Javadoc)
    * @see java.awt.Component#getBackground()
    */
   public Color getBackground()
   {
      TSEGraph graph = this.getGraph();
      if (graph != null && graph.getUI() instanceof TSEGraphUI)
      {
         TSEGraphUI ui = (TSEGraphUI) graph.getUI();
         return ui.getBackgroundColor().getColor();
      }
      return super.getBackground();
   }
   
   

   /*
    *  (non-Javadoc)
    * @see com.tomsawyer.editor.TSEGraphWindow#isAutoHidingScrollBarsByDefault()
    */
   public boolean isAutoHidingScrollBarsByDefault()
   {
      return true;
   }

   /*
    *  (non-Javadoc)
    * @see com.tomsawyer.editor.TSEGraphWindow#updateScrollBarValues()
    */
   public synchronized void updateScrollBarValues()
   {
      // Only update the scroll bars if the autoUpdateBounds are enabled.
      if (getAllowRedraw() && isVisible() && getDrawingArea() != null && getDrawingArea().getGraphWindow() == this)
      {
			bUpdatingScrollBars = true;
         super.updateScrollBarValues();
			bUpdatingScrollBars = false;
      }
   }

   /* (non-Javadoc)
    * @see com.tomsawyer.editor.TSEGraphWindow#fitInWindow(boolean)
    */
   public void fitInWindow(boolean arg0)
   {
      // Only Fit in the window if the bounds are known, otherwise its a useless.
      if (getGraph().isBoundsUpdatingEnabled())
         super.fitInWindow(arg0);
      else
         ETSystem.out.println("Warning: your are calling fitInWindow when the graph bounds are unknown.");
   }

   /* (non-Javadoc)
    * @see com.tomsawyer.editor.TSEGraphWindow#drawGraph()
    */
   public void drawGraph()
   {
      if (getAllowRedraw())
         super.drawGraph();
   }

   /* (non-Javadoc)
    * @see com.tomsawyer.editor.TSEGraphWindow#drawEntireGraph(com.tomsawyer.editor.graphics.TSEGraphics, int, boolean, boolean, int, int)
    */
   public void drawEntireGraph(TSEGraphics arg0, int arg1, boolean arg2, boolean arg3, int arg4, int arg5)
   {
		if (getAllowRedraw())
         super.drawEntireGraph(arg0, arg1, arg2, arg3, arg4, arg5);
   }

   /* (non-Javadoc)
    * @see com.tomsawyer.editor.TSEGraphWindow#drawEntireGraph(com.tomsawyer.editor.graphics.TSEGraphics, int, boolean, boolean)
    */
   public void drawEntireGraph(TSEGraphics arg0, int arg1, boolean arg2, boolean arg3)
   {
		if (getAllowRedraw())
         super.drawEntireGraph(arg0, arg1, arg2, arg3);
   }

   /* (non-Javadoc)
    * @see com.tomsawyer.editor.TSEGraphWindow#drawGraph(boolean)
    */
   public void drawGraph(boolean arg0)
   {
		if (getAllowRedraw())
         super.drawGraph(arg0);
   }

   /* (non-Javadoc)
    * @see com.tomsawyer.editor.TSEGraphWindow#drawGraph(java.awt.Graphics, boolean, boolean, boolean, boolean, boolean)
    */
   public void drawGraph(Graphics arg0, boolean arg1, boolean arg2, boolean arg3, boolean arg4, boolean arg5)
   {
		if (getAllowRedraw())
         super.drawGraph(arg0, arg1, arg2, arg3, arg4, arg5);
   }

   /* (non-Javadoc)
    * @see com.tomsawyer.editor.TSEGraphWindow#drawGraph(java.awt.Graphics, boolean, boolean, boolean)
    */
   public void drawGraph(Graphics arg0, boolean arg1, boolean arg2, boolean arg3)
   {
		if (getAllowRedraw())
         super.drawGraph(arg0, arg1, arg2, arg3);
   }

   /* (non-Javadoc)
    * @see com.tomsawyer.editor.TSEGraphWindow#drawGraph(java.awt.Graphics, boolean, boolean)
    */
   public void drawGraph(Graphics arg0, boolean arg1, boolean arg2)
   {
		if (getAllowRedraw())
         super.drawGraph(arg0, arg1, arg2);
   }

   /* (non-Javadoc)
    * @see com.tomsawyer.editor.TSEGraphWindow#drawGraph(java.awt.Graphics, boolean)
    */
   public void drawGraph(Graphics arg0, boolean arg1)
   {
		if (getAllowRedraw())
      	super.drawGraph(arg0, arg1);
   }

   /* (non-Javadoc)
    * @see com.tomsawyer.editor.TSEGraphWindow#drawGraph(java.awt.Graphics)
    */
   public void drawGraph(Graphics arg0)
   {
		if (getAllowRedraw())
         super.drawGraph(arg0);
   }

   /* (non-Javadoc)
    * @see com.tomsawyer.editor.TSEGraphWindow#drawGraph(com.tomsawyer.util.TSConstRect, boolean, boolean)
    */
   public void drawGraph(TSConstRect arg0, boolean arg1, boolean arg2)
   {
		if (getAllowRedraw())
         super.drawGraph(arg0, arg1, arg2);
   }

   /* (non-Javadoc)
    * @see com.tomsawyer.editor.TSEGraphWindow#drawGraph(com.tomsawyer.util.TSConstRect)
    */
   public void drawGraph(TSConstRect arg0)
   {
		if (getAllowRedraw())
         super.drawGraph(arg0);
   }

   /* (non-Javadoc)
    * @see com.tomsawyer.editor.TSEGraphWindow#updateInvalidRegions()
    */
   public void updateInvalidRegion()
   {
		if (getAllowRedraw())
         super.updateInvalidRegion();
   }

   /* (non-Javadoc)
    * @see com.tomsawyer.editor.TSEGraphWindow#updateInvalidRegions(boolean)
    */
   public synchronized void updateInvalidRegion(boolean arg0)
   {
		if (getAllowRedraw())
         super.updateInvalidRegion(arg0);
   }

	/*
	 * Returns we we can draw on the graph window
	 */
	public boolean getAllowRedraw()
	{
		return allowRedraw && getGraph() != null && getGraph().isBoundsUpdatingEnabled();
	}
	
	/*
	 * Sets if we can draw on the graph window.
	 */
	public void setAllowRedraw(boolean allow)
	{
		allowRedraw = allow;
	}
	
	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.TSEGraphWindow#getMaxZoomLevel()
	 */
	public double getMaxZoomLevel()
	{
		if (m_drawingAreaCtrl != null)
		{
				return m_drawingAreaCtrl.getExtremeZoomValues().getParamTwo().doubleValue();			
		}
		else
			return super.getMaxZoomLevel();		
	}

	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.TSEGraphWindow#getMinZoomLevel()
	 */
	public double getMinZoomLevel()
	{
		if (m_drawingAreaCtrl != null)
		{
			return m_drawingAreaCtrl.getExtremeZoomValues().getParamOne().doubleValue();
		}
		else
			return super.getMinZoomLevel();		
	}

   /* (non-Javadoc)
    * @see java.awt.Component#doLayout()
    */
   public void doLayout()
   {
      try
      {
         if (bUpdatingScrollBars == false)
            super.doLayout();
      }
      catch(Exception e)
      {
         // For some reason TS is throwing an array out of bounds exception.
         // Just check the exception and move on.
      }
   }
   //JM : writing this method for debugging purposes only.. can be deleted later...
   public void setZoomLevel(double zoomLevel, boolean redraw) {
//       Debug.out.println("Old zoom level: " + this.getZoomLevel());       
       super.setZoomLevel(zoomLevel, redraw);
//       Debug.out.println("New zoom level: " + this.getZoomLevel());
   }

   
   
   public String getToolTipText(MouseEvent event) {
        
        TSENode node = m_hitTesting.getNodeAt(
            getNonalignedWorldPoint(event.getPoint()), getGraph(), true);
        
        if (node == null) return null;
        
        TSEObjectUI nodeUI = node.getUI();
        
        if (nodeUI == null) return null;
        if (!(nodeUI instanceof IETGraphObjectUI)) return null;
        
        IDrawEngine engine = ((IETGraphObjectUI) nodeUI).getDrawEngine();
        
        if (engine == null) return null;

        if (engine instanceof ETClassDrawEngine) {
            return ((ETClassDrawEngine) engine).getToolTipText(event);
        }
        
        return null;
    }
   
   //JM: Fix for Bug#6315533 - Provide vicinity feature for connector/relationship/link elements in drawing area   
   public int getHitTolerance() {        
//       Debug.out.println("...............get hit tolerance = "+super.getHitTolerance()+" this = "+this.hashCode());
       return super.getHitTolerance();       
   }

   public void setHitTolerance(int tolerance) {       
       super.setHitTolerance(tolerance);
//       Debug.out.println("...............in set method... hit tolerance = "+super.getHitTolerance()+" this = "+this.hashCode());        
   }
   
   public void scrollBy(int dx,  int dy, boolean redraw) {
//       System.err.println("  ADGraphWindow : scrollBy .. dx = "+dx+" dy = "+dy+" redraw = "+redraw);
       super.scrollBy(dx,dy,redraw);
       drawGraph();
       repaint();
   }


    /////////////
    // Accessible
    /////////////

    AccessibleContext accessibleContext;

    public AccessibleContext getAccessibleContext() {
	if (accessibleContext == null) {
	    accessibleContext = new AccessibleGraphWindow();
	} 
	return accessibleContext;
    }


    public class AccessibleGraphWindow extends AccessibleJComponent implements AccessibleSelection
    {
	
	WeakHashMap<IDrawEngine, AccessibleEngineAndLabelsPanel> panels = new WeakHashMap<IDrawEngine, AccessibleEngineAndLabelsPanel>();

	public AccessibleGraphWindow() {
	    super();
	    registerAsGraphListener();
	}

	public String getAccessibleName(){
	    return getDrawingArea().getName();
	}

	public int getAccessibleChildrenCount() {
	    List<Accessible> children = getAccessibleChildren();
	    if (children != null) {
		return children.size();
	    }
	    return 0;
	}
	
	
	public Accessible getAccessibleChild(int i) {
	    List<Accessible> children = getAccessibleChildren();
	    if (children != null && i < children.size()) {
		return children.get(i);
	    }
	    return null;
	}
	
	
	public AccessibleRole getAccessibleRole() {
	    return AccessibleRole.PANEL;
	}
	
	
	public AccessibleSelection getAccessibleSelection() {
	    return this;
	}


	public AccessibleComponent getAccessibleComponent() {
	    return this;
	}


	////////////////////////////////
	// interface AccessibleComponent
	////////////////////////////////

	public javax.accessibility.Accessible getAccessibleAt(java.awt.Point point) {
	    return null;
	}


	////////////////////////////////
	// interface AccessibleSelection
	////////////////////////////////

	public int getAccessibleSelectionCount() {
	    List<Accessible> selected = getSelectedAccessibleChildren();
	    if (selected != null) {
		return selected.size();
	    }
	    return 0;
	}
	
	public Accessible getAccessibleSelection(int i) {
	    List<Accessible> selected = getSelectedAccessibleChildren();
	    if (selected != null && i < selected.size()) {
		return selected.get(i);
	    }
	    return null;
	}
	
	public boolean isAccessibleChildSelected(int i) {
	    Accessible child = getAccessibleChild(i);	   
	    if (child != null) {
		return isSelected(child);	    
	    }
	    return false;
	}
	
	public void addAccessibleSelection(int i) {
	    Accessible child = getAccessibleChild(i);	    
	    if (child != null) {
		selectChild(child, true, false);
	    }
	}
	
	public void removeAccessibleSelection(int i) {
	    List<Accessible> selected = getSelectedAccessibleChildren();
	    List<Accessible> children = getAccessibleChildren();
	    if (children != null && i < children.size()) {
		//if (isSelected(children.get(i))) {
		    selectChild(children.get(i), false, true); 
		//}
	    }
	}

	public void selectAllAccessibleSelection() {
	    List<Accessible> children = getAccessibleChildren();
	    if (children != null) {
		for(int i = 0; i < children.size(); i++) {
		    selectChild(children.get(i), true, false); 
		}
	    }	    
	}
	
	public void clearAccessibleSelection() {
	    List<Accessible> selected = getSelectedAccessibleChildren();
	    if (selected != null) {
		for(int i = 0; i < selected.size(); i++) {
		    selectChild(selected.get(i), false, true); 
		}
	    }
	}
	

	/////////////////
	// Helper methods
	/////////////////
	
	public void selectChild(Accessible child, boolean select, boolean replaceSelection) {
	    AccessibleSelection childSelection = child.getAccessibleContext().getAccessibleSelection();
	    if (childSelection != null) {
		if (select) {
		    childSelection.selectAllAccessibleSelection();
		} else {
		    childSelection.clearAccessibleSelection();
		}
	    }
	}
	
	public boolean isSelectable(Accessible child) {
	    return true;
	}
	
	/**
	 *  a panel for node/edge with labels is considered selected if the node/engine is selected
	 */
	public boolean isSelected(Accessible child) {
	    if (child instanceof AccessibleEngineAndLabelsPanel) {
		AccessibleEngineAndLabelsPanel panel = (AccessibleEngineAndLabelsPanel)child;
		return panel.isSelected(panel.engine);
	    }
	    return false;
	}


	public List<Accessible> getAccessibleChildren() {
	    ArrayList<Accessible> children = new ArrayList<Accessible>();
	    if (m_drawingAreaCtrl != null) { 
		ETList<TSGraphObject> nodesAndEdges = new ETArrayList<TSGraphObject>();
		nodesAndEdges.addAll(getGraph().edges());
		nodesAndEdges.addAll(getGraph().nodes());
		Iterator<TSGraphObject> iter = nodesAndEdges.iterator();
		while(iter.hasNext()) {
		    TSGraphObject obj = iter.next();		    
		    IDrawEngine eng = TypeConversions.getDrawEngine(obj);
		    if (eng instanceof Accessible) {
			if (eng != null && eng instanceof Accessible) {
			    AccessibleEngineAndLabelsPanel panel = panels.get(eng);
			    if (panel == null) { 				
				panel = new AccessibleEngineAndLabelsPanel((Accessible)eng);
				panel.getAccessibleContext().setAccessibleParent(ADGraphWindow.this);
				panels.put(eng, panel);
			    }
			    children.add(panel);
			}
		    }
		}
	    }
	    return children;
	}


	public List<Accessible> getSelectedAccessibleChildren() {
	    ArrayList<Accessible> selected = new ArrayList<Accessible>();
	    List<Accessible> children = getAccessibleChildren();
	    for(int i = 0; i < children.size(); i++) {
		Accessible child = children.get(i);
		if (isSelected(child)) {
		    selected.add(child);		
		}
	    }
	    return selected;
	}

	
	public Accessible getAccessibleChild(Object obj) {
	    if (obj instanceof TSObject) {
		IDrawEngine eng = TypeConversions.getDrawEngine((TSObject)obj);
		if (eng != null && eng instanceof Accessible) {
		    AccessibleEngineAndLabelsPanel panel = panels.get(eng);
		    if (panel == null) { 				
			panel = new AccessibleEngineAndLabelsPanel((Accessible)eng);
			panel.getAccessibleContext().setAccessibleParent(ADGraphWindow.this);
			panels.put(eng, panel);
		    }
		    return panel;
		}
	    }
	    return null;
	}


	////////////////////////////////
	// Property Change Notifications
	////////////////////////////////

	
	public void registerAsGraphListener() {		 

	    ACS_GraphListener listener = new ACS_GraphListener(); 
	    getGraphManager().getEventManager().addGraphChangeListener(getGraphManager(),listener);  
	    ((TSEEventManager)getGraphManager().getEventManager()).addSelectionChangeListener(getGraphManager(), listener);      
	    ((TSEEventManager)getGraphManager().getEventManager()).addViewportChangeListener(ADGraphWindow.this, listener);
	}

	
	public class ACS_GraphListener implements TSGraphChangeListener, TSESelectionChangeListener, TSEViewportChangeListener {
	    
	    public void graphChanged(TSGraphChangeEvent event) {     
	
		if ((event.getType() == TSGraphChangeEvent.NODE_INSERTED) ||
		    (event.getType() == TSGraphChangeEvent.EDGE_INSERTED)) 
		{ 		    			 
		    Accessible child = getAccessibleChild(event.getSource());
		    firePropertyChange(ACCESSIBLE_CHILD_PROPERTY, null, child);
		} 
		else if ((event.getType() == TSGraphChangeEvent.NODE_REMOVED) ||
			 (event.getType() == TSGraphChangeEvent.NODE_DISCARDED) ||
			 (event.getType() == TSGraphChangeEvent.EDGE_REMOVED) ||
			 (event.getType() == TSGraphChangeEvent.EDGE_DISCARDED)) 
		{ 		    
		    Accessible child = getAccessibleChild(event.getSource());
		    firePropertyChange(ACCESSIBLE_CHILD_PROPERTY, child, null);
		} 
		else if ((event.getType() == TSGraphChangeEvent.NODE_RENAMED) ||
			 (event.getType() == TSGraphChangeEvent.EDGE_RENAMED)) 
		{ 	
		    
		    ;
		}
		    
	    }


	    public void selectionChanged(TSESelectionChangeEvent event) {   
		Object source = event.getSource();
		Object oldValue = null;
		Object newValue = null;
		
		if (event.getData() instanceof TSESelectionChangeEventData) {
		    TSESelectionChangeEventData data = (TSESelectionChangeEventData)event.getData();
		    if (data != null) {
			if (data.isSelected()) {
			    newValue = source; 
			} else if (data.wasSelected()) {
			    oldValue = source;
			}
		    }
		}

		firePropertyChange(ACCESSIBLE_SELECTION_PROPERTY, oldValue, newValue);

		if (source != null) {
		    Accessible child;
		    if (source instanceof TSLabel) {
			child = getAccessibleChild(((TSGraphObject)event.getSource()).getOwner());
		    } else {
			child = getAccessibleChild(event.getSource());
		    }
		    if (child != null) {
			child.getAccessibleContext().firePropertyChange(ACCESSIBLE_SELECTION_PROPERTY, oldValue, newValue);
		    }
		}
		
		
	    }
	    
	    public void viewportChanged(TSEViewportChangeEvent event) {
		;
	    }
	    
	}

	public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
	    //System.out.println("\nFIRED : AccessibleADGraphWindow \n propertyName = "+propertyName+"\n oldValue = "+oldValue+"\n newValue = "+newValue);
	    super.firePropertyChange(propertyName, oldValue, newValue);
	}
	

    }


    ////////////////////////////////////////////////
    // Artificial panel for an engine and its labels
    ////////////////////////////////////////////////

    public class AccessibleEngineAndLabelsPanel extends AccessibleContext
	implements Accessible, AccessibleComponent, AccessibleSelection,
		   AccessibleSelectionParent
    {
	

	public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
	    //System.out.println("\nFIRED : AccessibleEngineAndLabelsPanel \n propertyName = "+propertyName+"\n oldValue = "+oldValue+"\n newValue = "+newValue);
	    super.firePropertyChange(propertyName, oldValue, newValue);
	}

	public AccessibleContext getAccessibleContext() {
	    return this;
	}
	

	public Accessible engine;

	public AccessibleEngineAndLabelsPanel(Accessible eng) {	    
	    this.engine = eng;	    
	}

	public Locale getLocale() {
	    return Locale.US;
	}

	public int getAccessibleIndexInParent() {
	    return 0;
	}

	public AccessibleStateSet getAccessibleStateSet() {
	    return new AccessibleStateSet(new AccessibleState[] {
		AccessibleState.SHOWING,
		AccessibleState.VISIBLE,
		AccessibleState.ENABLED, 		
		AccessibleState.FOCUSABLE,	
		AccessibleState.SELECTABLE	
	    });	    
	}

	public String getAccessibleName(){
	    return engine.getAccessibleContext().getAccessibleName();
	}

	public String getAccessibleDescription(){
	    return engine.getAccessibleContext().getAccessibleDescription();
	}

	public int getAccessibleChildrenCount() {
	    List<Accessible> children = getAccessibleChildren();
	    if (children != null) {
		return children.size();
	    }
	    return 0;
	}
	
	
	public Accessible getAccessibleChild(int i) {
	    List<Accessible> children = getAccessibleChildren();
	    if (children != null && i < children.size()) {
		return children.get(i);
	    }
	    return null;
	}

	
	public AccessibleRole getAccessibleRole() {
	    return AccessibleRole.PANEL;
	}
	
	
	public AccessibleSelection getAccessibleSelection() {
	    return this;
	}

	public AccessibleComponent getAccessibleComponent() {
	    return this;
	}


	////////////////////////////////
	// interface AccessibleComponent
	////////////////////////////////

	public java.awt.Color getBackground() {
	    return null;
	}

	public void setBackground(java.awt.Color color) {
	    ;
	}

	public java.awt.Color getForeground() {
	    return null;
	}

	public void setForeground(java.awt.Color color) {
	    ;
	}

	public java.awt.Cursor getCursor() {
	    return null; //getGraphWindow().getCursor();
	}
	
	public void setCursor(java.awt.Cursor cursor) {
	    ;
	}

	public java.awt.Font getFont() {
	    return null;
	}

	public void setFont(java.awt.Font font) {
	    ;
	}

	public java.awt.FontMetrics getFontMetrics(java.awt.Font font) {
	    return null; //getGraphWindow().getFontMetrics(font);
	}
	public boolean isEnabled() {
	    return true;
	}

	public void setEnabled(boolean enabled) {

	}

	public boolean isVisible() {
	    return true;
	}

	public void setVisible(boolean visible) {
	    ;
	}

	public boolean isShowing() {
	    return true;
	}
	
	public boolean contains(java.awt.Point point) {
            Rectangle r = getBounds();
            return r.contains(point);
	}
	
	public java.awt.Point getLocationOnScreen() {
	    java.awt.Point p = null;
	    List<Accessible> children = getAccessibleChildren();
	    if (children != null) {
		for(int i = 0; i < children.size(); i++) {
		    Accessible child = children.get(i);
		    if (child != null) {
			java.awt.Point pc = child.getAccessibleContext().getAccessibleComponent().getLocationOnScreen();
			if (pc != null) {
			    if (p == null) {
				p = pc;
			    } else {
				if (pc.x < p.x) {
				    p.setLocation(pc.x, p.y);
				}
				if (pc.y < p.y) {
				    p.setLocation(p.x, pc.y);
				}
			    }
			} 
		    }
		}
	    }  
	    return p;
	}
	
	public java.awt.Point getLocation() {
	    java.awt.Point p = null;
	    List<Accessible> children = getAccessibleChildren();
	    if (children != null) {
		for(int i = 0; i < children.size(); i++) {
		    Accessible child = children.get(i);
		    if (child != null) {
			java.awt.Point pc = child.getAccessibleContext().getAccessibleComponent().getLocation();
			if (pc != null) {
			    if (p == null) {
				p = pc;
			    } else {
				if (pc.x < p.x) {
				    p.setLocation(pc.x, p.y);
				}
				if (pc.y < p.y) {
				    p.setLocation(p.x, pc.y);
				}
			    }
			} 
		    }
		}
	    }  
	    return p;
	}
	
	public void setLocation(java.awt.Point point) {
	    ;
	}
	
	public java.awt.Rectangle getBounds() {
	    java.awt.Rectangle bounds = null;
	    List<Accessible> children = getAccessibleChildren();
	    if (children != null) {
		for(int i = 0; i < children.size(); i++) {
		    Accessible child = children.get(i);
		    if (child != null) {
			java.awt.Rectangle bc = child.getAccessibleContext().getAccessibleComponent().getBounds();
			if (bc != null) {
			    if (bounds == null) {
				bounds = bc;
			    } else {
				bounds = bounds.union(bc);
			    }
			}
		    }
		}
	    }  
	    return bounds;
	}
	
	public void setBounds(java.awt.Rectangle bounds) {
	    ;
	}

	public java.awt.Dimension getSize() {
            Rectangle r = getBounds();
            return new Dimension(r.width, r.height);
	}
	
	public void setSize(java.awt.Dimension dim) {
	    ;
	}
	
	public javax.accessibility.Accessible getAccessibleAt(java.awt.Point point) {	    
	    return null;
	}

	public boolean isFocusTraversable() {
	    return true;
	}

	public void requestFocus() {
	    ;
	}

	public void addFocusListener(java.awt.event.FocusListener listener) {
	    ;
	}

	public void removeFocusListener(java.awt.event.FocusListener listener) {
	    ;
	}


	////////////////////////////////
	// interface AccessibleSelection
	////////////////////////////////

	public int getAccessibleSelectionCount() {
	    List<Accessible> selected = getSelectedAccessibleChildren();
	    if (selected != null) {
		return selected.size();
	    }
	    return 0;
	}
	
	public Accessible getAccessibleSelection(int i) {
	    List<Accessible> selected = getSelectedAccessibleChildren();
	    if (selected != null && i < selected.size()) {
		return selected.get(i);
	    }
	    return null;
	}
	
	public boolean isAccessibleChildSelected(int i) {
	    Accessible child = getAccessibleChild(i);	   
	    if (child != null) {
		return isSelected(child);	    
	    }
	    return false;
	}
	
	public void addAccessibleSelection(int i) {
	    Accessible child = getAccessibleChild(i);	    
	    if (child != null) {
		selectChild(child, true, false);
	    }
	}
	
	public void removeAccessibleSelection(int i) {
	    List<Accessible> selected = getSelectedAccessibleChildren();
	    List<Accessible> children = getAccessibleChildren();
	    if (children != null && i < children.size()) {
		//if (isSelected(children.get(i))) {
		    selectChild(children.get(i), false, true); 
		//}
	    }
	}

	public void selectAllAccessibleSelection() {
	    List<Accessible> children = getAccessibleChildren();
	    if (children != null) {
		for(int i = 0; i < children.size(); i++) {
		    selectChild(children.get(i), true, false); 
		}
	    }	    
	}
	
	public void clearAccessibleSelection() {
	    List<Accessible> selected = getSelectedAccessibleChildren();
	    if (selected != null) {
		for(int i = 0; i < selected.size(); i++) {
		    selectChild(selected.get(i), false, true); 
		}
	    }
	}
	

	/////////////////
	// Helper methods
	/////////////////

	public void selectChild(Accessible child, boolean select, boolean replaceSelection) {
	    if (child instanceof IDrawEngine) {
		if (replaceSelection) {
		    deselectAll(false);
		}
		if ( ! select) {
		    ((IDrawEngine)child).selectAllCompartments(false);
		}
		((IDrawEngine)child).getParent().getTSObject().setSelected(select);
		getDrawingArea().onGraphEvent(IGraphEventKind.GEK_POST_SELECT, null, null, null);		
		ETGraph etGraph = getGraph() instanceof ETGraph ? (ETGraph) getGraph() : null;
		getDrawingArea().fireSelectEvent(etGraph != null ? etGraph.getSelectedObjects(false, false) : null);
		getDrawingArea().refresh(true);
	    }

	}
	
	public boolean isSelectable(Accessible child) {
	    return true;
	}
	
	public boolean isSelected(Accessible child) {
	    if (child instanceof IDrawEngine) {
		ETGraph etGraph = getGraph() instanceof ETGraph ? (ETGraph) getGraph() : null;
		if (etGraph != null) { 
		    List<TSGraphObject> selected = etGraph.getSelectedObjects(false, false);
		    if (selected != null 
			&& selected.contains(((IDrawEngine)child).getParent().getTSObject())) 
		    {
			return true;
		    }
		}
	    }
	    return false;
	}


	public List<Accessible> getAccessibleChildren() {
	    ArrayList<Accessible> children = new ArrayList<Accessible>();
	    if (engine != null) {
		engine.getAccessibleContext().setAccessibleParent(this);
		children.add(engine);
		ILabelManager labelMgr = ((IDrawEngine)engine).getLabelManager();	    
		if (labelMgr != null) {
		    for (int lIndx = 0; /* break below */; lIndx++) {
			IETLabel label = null;
			label = labelMgr.getLabelByIndex(lIndx);
			if (label != null) {
			    IDrawEngine eng = label.getEngine();
			    if (eng instanceof Accessible) {
				((Accessible)eng).getAccessibleContext().setAccessibleParent(this);
				children.add((Accessible)eng);
			    }
			} else {
			    break;
			} 
		    }
		}
	    }
	    return children;
	}


	public List<Accessible> getSelectedAccessibleChildren() {
	    ArrayList<Accessible> selected = new ArrayList<Accessible>();
	    List<Accessible> children = getAccessibleChildren();
	    for(int i = 0; i < children.size(); i++) {
		Accessible child = children.get(i);
		if (isSelected(child)) {
		    selected.add(child);		
		}
	    }
	    return selected;
	}


    }


}

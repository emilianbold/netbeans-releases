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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDrawingToolKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.ILayoutKind;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import com.tomsawyer.graph.event.TSGraphChangeEvent;
import com.tomsawyer.graph.event.TSGraphChangeListener;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.editor.TSEWindowTool;
import com.tomsawyer.editor.tool.TSEInteractiveZoomTool;
import com.tomsawyer.editor.tool.TSELinkNavigationTool;
import com.tomsawyer.editor.tool.TSEPanTool;
import com.tomsawyer.editor.tool.TSESelectTool;
import com.tomsawyer.editor.tool.TSEZoomTool;
import com.tomsawyer.util.TSSystem;
import com.tomsawyer.editor.event.TSEViewportChangeListener;
import com.tomsawyer.editor.event.TSESelectionChangeListener;
import com.tomsawyer.editor.event.TSESelectionChangeEvent;
import com.tomsawyer.editor.event.TSEViewportChangeEvent;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.support.Debug;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;

/**
 * This class handles the keyboard and menu actions.
 */
public class ADDrawingAreaActions extends Object implements ActionListener, TSGraphChangeListener, TSESelectionChangeListener, TSEViewportChangeListener
{
    
    private ADDrawingAreaControl m_drawingArea;
    
    private static final String BUNDLE_NAME = "org.netbeans.modules.uml.ui.swing.drawingarea.Bundle"; //$NON-NLS-1$
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
    private boolean processedPreMoveAction = false;
    /**
     * This is the default constructor.
     */
    public ADDrawingAreaActions(ADDrawingAreaControl pDrawingArea)
    {
        this.m_drawingArea = pDrawingArea;
        Debug.out.println(" ADDrawingAreaActions : constructor");
    }
    
    /**
     * This method switches to the "next" state.
     * It cycles through select, pan, zoom, interactive zoom, and link
     * navigation states. It is invoked when the spacebar is pressed.
     */
    public void onNextState()
    {
        TSEWindowTool state = this.getCurrentState();
        
        if ((state instanceof TSESelectTool) && !this.m_drawingArea.isAutoFitInWindow())
        {
            this.onSwitchToPan();
        }
        else if ((state instanceof TSEPanTool) && !this.m_drawingArea.isAutoFitInWindow())
        {
            this.onSwitchToZoom();
        }
        else if ((state instanceof TSEZoomTool) && !this.m_drawingArea.isAutoFitInWindow())
        {
            this.onSwitchToInteractiveZoom();
        }
        else if ((state instanceof TSEInteractiveZoomTool) &&
                (this.m_drawingArea.getGraphManager().numberOfViewableEdges() > 0) &&
                !this.m_drawingArea.isAutoFitInWindow())
        {
            this.onSwitchToLinkNavigation();
        }
        else
        {
            this.onSwitchToSelect();
        }
    }
    
   /*
    * changes the state object.
    */
    protected void switchState(TSEWindowTool state)
    {
        getGraphWindow().switchTool(state);
    }
    
   /*
    * Returns the default state, generally the select state.
    */
    protected TSEWindowTool getDefaultState()
    {
        return getGraphWindow() != null ? getGraphWindow().getDefaultState() : null;
    }
    
   /*
    * Returns the Current state object.
    */
    protected TSEWindowTool getCurrentState()
    {
        return getGraphWindow() != null ? getGraphWindow().getCurrentState() : null;
    }
    
    /**
     * This method sets the select state to be the current state of
     * the graph window.
     */
    public void onSwitchToSelect()
    {
        getControl().enterMode(IDrawingToolKind.DTK_SELECTION);
    }
    
    /**
     * This method sets the marquee zoom state to be the current state
     * of the graph window.
     */
    public void onSwitchToZoom()
    {
        getControl().enterMode(IDrawingToolKind.DTK_ZOOM);
    }
    
    /**
     * This method sets the interactive zoom state to be the current
     * state of the graph window.
     */
    public void onSwitchToInteractiveZoom()
    {
        getControl().enterMode(IDrawingToolKind.DTK_MOUSE_ZOOM);
    }
    
    /**
     * This method sets the pan state to be the current state of
     * the graph window.
     */
    public void onSwitchToPan()
    {
        getControl().enterMode(IDrawingToolKind.DTK_PAN);
    }
    
    /**
     * This method sets the link navigation state to be the current
     * state of the graph window.
     */
    public void onSwitchToLinkNavigation()
    {
        getControl().enterMode(IDrawingToolKind.DTK_EDGENAV_MOUSE);
    }
    
    /**
     * This method aborts whatever action the user is performing.
     */
    public void onAbortAction()
    {
        if (this.m_drawingArea.hasGraphWindow())
        {
            getGraphWindow().cancelAction();
            ADDrawingAreaControl drawingAreaControl = this.m_drawingArea;
            drawingAreaControl.autoFitInWindow = false;
        }
    }
    
    /**
     * This method refreshes the graph painting without changing the
     * scaling or location.
     */
    public void onRefresh()
    {
        getControl().refresh(false);
    }
    
    
    /**
     * This method duplicates the selected objects.
     */
    public void onDuplicateGraph()
    {
        m_drawingArea.duplicate();
    }
    
    /**
     * This method replaces the graph manager of the selected window
     * with a new empty one.
     */
    public void onClearAll()
    {
        m_drawingArea.onClearAll();
        
        // switch to the default state
        this.onSwitchToSelect();
    }
    
    /**
     * This method deletes all selected objects in the selected graph window.
     */
    public void onDeleteSelected()
    {
        m_drawingArea.onDeleteSelected();
    }
    
    /**
     * This method hides all selected objects in the selected graph window.
     */
    public void onHideSelected()
    {
    }
    
    /**
     * This method unhides all hidden objects in the selected graph window.
     */
    public void onUnhideAll()
    {
    }
    
    /**
     * This method folds selected objects in the selected graph window
     * into a new folder.
     */
    public void onFoldSelected()
    {
    }
    
    /**
     * This method unfolds all folders in the main display graph of
     * the selected graph window.
     */
    public void onUnfoldAll()
    {
    }
    
    /**
     * This method unfolds all the selected folders in the main display graph
     * of the selected graph window.
     */
    public void onUnfoldSelected()
    {
    }
    
    /**
     * This method collapses all the selected expanded nodes in
     * the selected graph window.
     */
    public void onCollapseSelected()
    {
    }
    
    /**
     * This method expands all the selected collapsed nodes in
     * the selected graph window.
     */
    public void onExpandSelected()
    {
    }
    
    /**
     * This method scrolls the graph in the specified direction.
     */
    public void onScrollGraph(int direction)
    {
        //Jyothi: Pan only when nothing is selected on the diagram
        ETList <IPresentationElement> graphObjects =  m_drawingArea.getSelected();
        ADGraphWindow graphWindow = getGraphWindow();
        if (graphObjects == null || graphObjects.size() == 0)
        {
            switch (direction)
            {
            case KeyEvent.VK_LEFT :
                graphWindow.scrollBy(-20, 0, true);
                break;
                
            case KeyEvent.VK_RIGHT :
                graphWindow.scrollBy(20, 0, true);
                break;
                
            case KeyEvent.VK_UP :
                graphWindow.scrollBy(0, -20, true);
                break;
                
            case KeyEvent.VK_DOWN :
                graphWindow.scrollBy(0, 20, true);
                break;
            }
        }
    }

    /**
     * This method selects all the visible objects in the selected graph
     * window.
     */
    public void onSelectAll()
    {
        // fires a SELECTION_CHANGE event.
        getGraphWindow().selectAll(true);
    }
    
    /**
     * This method selects all the visible nodes in the selected graph
     * window.
     */
    public void onSelectNodes()
    {
        ADGraphWindow graphWindow = getGraphWindow();
        graphWindow.deselectAll(false);
        graphWindow.selectAllNodes(true);
    }
    
    /**
     * This method selects all the visible edges in the selected graph
     * window.
     */
    public void onSelectEdges()
    {
        ADGraphWindow graphWindow = getGraphWindow();
        graphWindow.deselectAll(false);
        graphWindow.selectAllEdges(true);
    }
    
    /**
     * This method selects all the visible labels in the selected graph
     * window.
     */
    public void onSelectLabels()
    {
        ADGraphWindow graphWindow = getGraphWindow();
        graphWindow.deselectAll(false);
        
        // don't fire events here, we don't want three.
        graphWindow.selectAllEdgeLabels(true);
        graphWindow.selectAllNodeLabels(true);
    }
    
    /**
     * This method reacts to the selected value in the zoom combo box
     * being changed.
     */
    public void onZoomChange()
    {
        String selectedText = this.m_drawingArea.getZoomComboBox().getSelectedItem().toString();
        String zoomStr = RESOURCE_BUNDLE.getString("IDS_ZOOMTOFIT");
        if (selectedText.equals("") || selectedText.equals(zoomStr))
        {
            this.onFitInWindow();
        }
        
        if (selectedText.endsWith("%"))
        {
            selectedText = selectedText.substring(0, selectedText.length() - 1);
        }
        
        try
        {
            double selectedNumber = java.lang.Math.abs(Double.valueOf(selectedText).doubleValue());
            
            double currentZoom = getGraphWindow().getZoomLevel() * 100;
            
            if (currentZoom != selectedNumber)
            {
                this.onZoom(new Double(selectedNumber).toString());
            }
        }
        catch (Exception ignored)
        {
            // do nothing if we get an exception, although one
            // might want to take action here
        }
        
        ADDrawingAreaControl drawingAreaControl = this.m_drawingArea;
        drawingAreaControl.autoFitInWindow = false;
        
    }
    
    /**
     * This method sets the given zoom level. The zoom level
     * is expressed in percent, thus for example passing 134
     * means setting zoom level to 134%.
     */
    public void onZoom(String command)
    {
        if( getGraphWindow() != null )
        {
            double level = 100.0;
            
            try
            {
                String zoomLevel = command.replaceAll(ADDrawingAreaConstants.ZOOM, "");
                level = Math.abs( Double.parseDouble( zoomLevel ));
            }
            catch (NumberFormatException ignored)
            {
            }
            getGraphWindow().setZoomLevel( level / 100.0, true );
        }
    }
    
    /**
     * This method sets the desired grid type on the graph and redraws the
     * graph window
     */
    public void onGridType(String command)
    {
        this.onGridType(command, true);
    }
    
    /**
     * This method sets the desired grid type on the graph and redraws the
     * graph window depending on <code>redraw</code> parameter.
     */
    public void onGridType(String command, boolean redraw)
    {
        int dotIndex = command.lastIndexOf('.');
        
        if (dotIndex > 0)
        {
            String subString = command.substring(dotIndex + 1);
            
            if (subString.equals("none"))
            {
                getGraphWindow().setGrid(null);
            }
            else if (subString.equals("point"))
            {
                getGraphWindow().setGrid(this.m_drawingArea.getPointGrid());
            }
            else if (subString.equals("line"))
            {
                getGraphWindow().setGrid(this.m_drawingArea.getLineGrid());
            }
            
            if (redraw)
            {
                onRefresh();
            }
        }
        
    }
    
    /**
     * This method sets the desired grid size on the graph and redraws
     * the graph window.
     */
    public void onGridSize(String command)
    {
        this.onGridSize(command, true);
    }
    
    /**
     * This method sets the desired grid size on the graph and redraws
     * the graph window depending on <code>redraw</code> parameter.
     */
    public void onGridSize(String command, boolean redraw)
    {
    }
    
    /**
     * This method sets the grid size according to the input given
     * by the user through a popup dialog.
     */
    public void onCustomGridSize()
    {
    }
    
    
    /**
     * This method expands or collapses all the parent nodes,
     * that contain child graphs, depending on the given argument.
     */
    public void onExpandCollapseAll(boolean expand)
    {
    }
    
    public ADDrawingAreaResourceBundle getResources()
    {
        return this.m_drawingArea.getResources();
    }
    
    /**
     * This method sets the zoom level according to the input given
     * by the user through a popup dialog.
     */
    public void onCustomZoom()
    {
        ADGraphWindow graphWindow = getGraphWindow();
        // fetch the current zoom level from the graph window
        double currentZoom = 100 * graphWindow.getZoomLevel();
        currentZoom = java.lang.Math.round(currentZoom * 100.0) / 100.0;
        
        String string = String.valueOf(currentZoom);
        
        if (string.endsWith(".0"))
        {
            string = string.substring(0, string.lastIndexOf(".0"));
        }
        
        // show the input dialog
        String result =
                (String)JOptionPane.showInputDialog(this.m_drawingArea,
                getResources().getStringResource("dialog.zoom.message"),
                getResources().getStringResource("dialog.zoom.title"),
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                string);
        
        // check if the user pressed OK
        
        if (result != null && result.length() > 0)
        {
            try
            {
                // try to convert the user specified zoom to a number
                double newZoomLevel = Double.valueOf(result).doubleValue();
                
                //if out of bounds, throw an exception to be caught below.
                
                if ((newZoomLevel < (graphWindow.getMinZoomLevel() * 100)) || (newZoomLevel > (graphWindow.getMaxZoomLevel() * 100)))
                {
                    throw new Exception();
                }
                
                double zoomFraction = newZoomLevel / 100.0;
                
                // set zoom level if it is different from the old one
                
                if (zoomFraction != currentZoom)
                {
                    graphWindow.setZoomLevel(zoomFraction, true);
                }
                
            }
            catch (Exception e)
            {
                // inform the user of the error
                String message = TSSystem.replace(getResources().getStringResource("dialog.zoomError.message"), ADDrawingAreaConstants.X_PLACEHOLDER, "" + graphWindow.getMinZoomLevel() * 100);
                
                message = TSSystem.replace(message, ADDrawingAreaConstants.Y_PLACEHOLDER, "" + graphWindow.getMaxZoomLevel() * 100);
                
                JOptionPane.showMessageDialog(this.m_drawingArea,
                        message,
                        getResources().getStringResource("dialog.zoomError.title"),
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        
        graphWindow.fastRepaint();
    }
    
    /**
     * This method magnifies the graph by 10%, creating an impression
     * of zooming in.
     */
    public void onZoomIn()
    {
        TSEGraphWindow graphWindow = getGraphWindow();
        
        // do not let the zoom level get larger than the maximum
        
        if (graphWindow.getZoomLevel() < graphWindow.getMaxZoomLevel() / 1.1)
        {
            graphWindow.zoom(0.1, true);
        }
        else
        {
            graphWindow.setZoomLevel(graphWindow.getMaxZoomLevel(), true);
        }
        ADDrawingAreaControl drawingAreaControl = this.m_drawingArea;
        drawingAreaControl.autoFitInWindow = false;
    }
    
    /**
     * This method shrinks the graph by 10%, creating an impression of
     * zooming out. It does not allow the user to zoom out so far that
     * the zoom level is less than the minimum zoom level.
     */
    public void onZoomOut()
    {
        TSEGraphWindow graphWindow = getGraphWindow();
        
        // do not let the zoom level get smaller than the minimum
        
        if (graphWindow.getZoomLevel() > graphWindow.getMinZoomLevel() * 1.1)
        {
            graphWindow.zoom(-0.1, true);
        }
        else
        {
            graphWindow.setZoomLevel(graphWindow.getMaxZoomLevel(), true);
        }
        ADDrawingAreaControl drawingAreaControl = this.m_drawingArea;
        drawingAreaControl.autoFitInWindow = false;
    }
    
    /**
     * This method undoes the last action performed in the selected
     * graph window.
     */
    public void onUndoAction()
    {
    }
    
    /**
     * This method redoes the last undone action performed by the
     * graph window.
     */
    public void onRedoAction()
    {
    }
    
    /**
     * This method clears the undo/redo history of the graph window.
     */
    public void onClearHistory()
    {
        int option =
                JOptionPane.showConfirmDialog(this.m_drawingArea,
                getResources().getStringResource("dialog.clearHistory.message"),
                getResources().getStringResource("dialog.clearHistory.title"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        
        if (option == JOptionPane.YES_OPTION)
        {
            getGraphWindow().clearUndoStack();
            
            // make sure that the state of application buttons is correct
            //			getControl().checkAllButtons();
        }
    }
    
    public void onAutoFitInWindow()
    {
        ADDrawingAreaControl drawingAreaControl = this.m_drawingArea;
        drawingAreaControl.setAutoFitInWindow(!drawingAreaControl.isAutoFitInWindow());
        
        if (drawingAreaControl.isAutoFitInWindow())
        {
            if (drawingAreaControl.hasGraphWindow())
            {
                drawingAreaControl.getGraphWindow().fitInWindow(true);
            }
            
            // remove some key bindings, which are used to zoom and scroll
            
            drawingAreaControl.unregisterKeyCommands(drawingAreaControl);
        }
        else
        {
            // recreate key bindings
            drawingAreaControl.registerKeyCommands(drawingAreaControl);
        }
        
        // disable the move/zoom state of the overview window.
        //Jyothi:
        if (drawingAreaControl.getOverviewWindow() != null)
        {
            //drawingAreaControl.getOverviewWindow().getOverviewComponent().setStateEnabled(!drawingAreaControl.isAutoFitInWindow());
            drawingAreaControl.getOverviewWindow().getOverviewComponent().setToolEnabled(true);
        }
        
        // if the current state is one the states which conflicts with
        // auto fit in window option, switch to select state.
        
        if (drawingAreaControl.hasGraphWindow())
        {
            //         TSEWindowState state = this.getCurrentState();
            TSEWindowTool state = this.getCurrentState();
            
            //         if ((state instanceof TSEPanState) || (state instanceof TSEZoomState) || (state instanceof TSEInteractiveZoomState) || (state instanceof TSELinkNavigationState))
            if ((state instanceof TSEPanTool) || (state instanceof TSEZoomTool) || (state instanceof TSEInteractiveZoomTool) || (state instanceof TSELinkNavigationTool))
            {
                this.onSwitchToSelect();
            }
        }
        
        //this.getZoomComboBox().setEnabled(!this.isAutoFitInWindow());
    }
    
    /**
     * This method fits the graph perfectly into the bounds of the graph window.
     */
    public void onFitInWindow()
    {
        if( getGraphWindow() != null )
        {
            getGraphWindow().fitInWindow(true);
        }
    }
    
    public void onAppExit()
    {
    }
    
    public void onCloseGraph()
    {
    }
    
    public void onRevertGraph()
    {
    }
    
    public void onPrintPreview()
    {
        IDrawingAreaControl control = getControl();
        control.printPreview(control.getName(), false);
    }
    
    protected ADDrawingAreaPrinter m_printer = null;
    
    public ADDrawingAreaPrinter getDrawingAreaPrinter()
    {
        if (m_printer == null)
        {
            m_printer = new ADDrawingAreaPrinter(getGraphWindow(), getResources());
        }
        
        return m_printer;
    }
    
    protected ADGraphWindow getGraphWindow()
    {
        return this.m_drawingArea.getGraphWindow();
    }
    
    public void onPrintSetup()
    {
        //this.getDrawingAreaPrinter().printSetup();
        Debug.out.println(" entered ADDrawingAreaActions : onPrintSetup() ");
        IDrawingAreaControl control = getControl();
        if ( control instanceof ADDrawingAreaControl )
        {
            Debug.out.println(" in if instanceof ...ADDrawingAreaActions : onPrintSetup() ");
            ((ADDrawingAreaControl)control).getDrawingAreaPrinter().printSetup();
        }
        else
        {
            Debug.out.println(" in else of ...ADDrawingAreaActions : onPrintSetup() ");
            this.getDrawingAreaPrinter().printSetup();
        }
    }
    
    public void onPrintGraph()
    {
        IDrawingAreaControl control = getControl();
        control.printGraph( false);
    }
    
    public void onExportAsImage()
    {
        this.m_drawingArea.showImageDialog();
    }
    
    public void onChangeSpacing()
    {
        showNotImplementedMessage();
    }
    
    public void onDiagramSync()
    {
        m_drawingArea.validateDiagram(false,null);
    }
    
    public void onShowFriendly()
    {
        //showNotImplementedMessage();
        ProductHelper.toggleShowAliasedNames();
    }
    
    public void onRelationDiscovery()
    {
        this.m_drawingArea.executeRelationshipDiscovery();
    }
    
    public void onMoveForword()
    {
        this.m_drawingArea.executeStackingCommand(IDrawingAreaControl.SOK_MOVEFORWARD, true);
    }
    
    public void onMoveToFront()
    {
        this.m_drawingArea.executeStackingCommand(IDrawingAreaControl.SOK_MOVETOFRONT, true);
    }
    
    public void onMoveBackward()
    {
        this.m_drawingArea.executeStackingCommand(IDrawingAreaControl.SOK_MOVEBACKWARD, true);
    }
    
    public void onMovetoBack()
    {
        this.m_drawingArea.executeStackingCommand(IDrawingAreaControl.SOK_MOVETOBACK, true);
    }
    
    // The body of this methos is executed only one when the move key stroke (Ctlr-<arrow keys>) is first pressed.
    // The instance variable processedPreMoveAction indicates whether or not the pre-move action has been processed.
    public void onPreMoveObjects()
    {
        if( m_drawingArea != null && !processedPreMoveAction)
        {
            ETList < IETGraphObject > affectedObjects = new ETArrayList < IETGraphObject > ();
            affectedObjects = m_drawingArea.getSelectedNodes();
            if(affectedObjects != null)
            {
                m_drawingArea.onGraphEvent(IGraphEventKind.GEK_PRE_MOVE, null, null, affectedObjects);
            }
            processedPreMoveAction = true;
        }
    }
    
    public void onPostMoveObjects()
    {
        if( m_drawingArea != null)
        {
            ETList < IETGraphObject > affectedObjects = new ETArrayList < IETGraphObject > ();
            affectedObjects = m_drawingArea.getSelectedNodes();
            m_drawingArea.onGraphEvent(IGraphEventKind.GEK_POST_MOVE, null, null, affectedObjects);
            processedPreMoveAction = false;
        }
    }
    
    public void onApplySequenceLayout(String command)
    {
        m_drawingArea.immediatelySetLayoutStyle(ILayoutKind.LK_SEQUENCEDIAGRAM_LAYOUT,false);
    }
    
    private void showNotImplementedMessage()
    {
        JOptionPane.showMessageDialog(
                this.m_drawingArea,
                getResources().getStringResource("dialog.notImplemented.message"),
                getResources().getStringResource("dialog.notImplemented.title"),
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    // ---------------------------------------------------------------------
    // Section: Helper methods for event handling
    // ---------------------------------------------------------------------
    
    /**
     * This method reacts to action events fired by menus and toolbars.
     */
    public void actionPerformed(ActionEvent event)
    {
        Debug.out.println(" ADDrawingAreaActions : actionPerformed");
        String command = event.getActionCommand();
        
        ADGraphWindow graphWindow = getGraphWindow();
        
        if (this.m_drawingArea.hasGraphWindow())
        {
            graphWindow.getCanvas().requestFocus();
        }
        
        // if the command is not a key release,
        // abort whatever the graph window is currently doing.
        
        if (!ADDrawingAreaConstants.MOVE_DONE.equals(command))
        {
            this.onAbortAction();
        }
        
        if (this.m_drawingArea.hasGraphWindow())
        {
            // check to see if the command is a move
            
            if (ADDrawingAreaConstants.MOVE_LEFT.equals(command))
            {
                onPreMoveObjects();
                graphWindow.move(ADMoveSelectedKeyAdapter.LEFT);
                graphWindow.finalizeMove();
            }
            else if (ADDrawingAreaConstants.MOVE_RIGHT.equals(command))
            {
                onPreMoveObjects();
                graphWindow.move(ADMoveSelectedKeyAdapter.RIGHT);
                graphWindow.finalizeMove();
            }
            else if (ADDrawingAreaConstants.MOVE_UP.equals(command))
            {
                onPreMoveObjects();
                graphWindow.move(ADMoveSelectedKeyAdapter.UP);
                graphWindow.finalizeMove();
            }
            else if (ADDrawingAreaConstants.MOVE_DOWN.equals(command))
            {
                onPreMoveObjects();
                graphWindow.move(ADMoveSelectedKeyAdapter.DOWN);
                graphWindow.finalizeMove();
            }
            else
            {
                // if it's not a move, finalize any pending moves.
                graphWindow.finalizeMove();
            }
        }
        
        if (ADDrawingAreaConstants.PRINT_PREVIEW.equals(command))
        {
            this.onPrintPreview();
        }
        else if (ADDrawingAreaConstants.PRINT_GRAPH.equals(command))
        {
            this.onPrintGraph();
        }
        else if (ADDrawingAreaConstants.SAVE_AS_IMAGE.equals(command))
        {
            this.onExportAsImage();
        }
        else if (ADDrawingAreaConstants.SELECT_STATE.equals(command))
        {
            this.onSwitchToSelect();
        }
        else if (ADDrawingAreaConstants.PAN_STATE.equals(command))
        {
            this.onSwitchToPan();
        }
        else if (ADDrawingAreaConstants.CHANGE_SPACING.equals(command))
        {
            this.onChangeSpacing();
        }
        else if (ADDrawingAreaConstants.ZOOM_STATE.equals(command))
        {
            this.onSwitchToZoom();
        }
        else if (ADDrawingAreaConstants.INTERACTIVE_ZOOM_STATE.equals(command))
        {
            this.onSwitchToInteractiveZoom();
        }
        else if (ADDrawingAreaConstants.EDGE_NAVIGATION_STATE.equals(command))
        {
            this.onSwitchToLinkNavigation();
        }
        else if (ADDrawingAreaConstants.OVERVIEW_WINDOW.equals(command))
        {
            // toggles the overview.
            this.m_drawingArea.onShowOverviewWindow();
        }
        else if (ADDrawingAreaConstants.DIAGRAM_SYNC.equals(command))
        {
            this.onDiagramSync();
        }
        else if (ADDrawingAreaConstants.SHOW_FRIENDLY.equals(command))
        {
            this.onShowFriendly();
        }
        else if (ADDrawingAreaConstants.RELATION_DISCOVERY.equals(command))
        {
            this.onRelationDiscovery();
        }
        else if (ADDrawingAreaConstants.ZOOM_AUTO_FIT.equals(command))
        {
            this.onAutoFitInWindow();
        }
        else if (ADDrawingAreaConstants.ZOOM_CHANGE.equals(command))
        {
            this.onZoomChange();
        }
        else if (ADDrawingAreaConstants.ZOOM_IN.equals(command))
        {
            this.onZoomIn();
        }
        else if (ADDrawingAreaConstants.ZOOM_OUT.equals(command))
        {
            this.onZoomOut();
        }
        else if (ADDrawingAreaConstants.MOVE_FORWORD.equals(command))
        {
            this.onMoveForword();
        }
        else if (ADDrawingAreaConstants.MOVE_TO_FRONT.equals(command))
        {
            this.onMoveToFront();
        }
        else if (ADDrawingAreaConstants.MOVE_BACKWARD.equals(command))
        {
            this.onMoveBackward();
        }
        else if (ADDrawingAreaConstants.MOVE_TO_BACK.equals(command))
        {
            this.onMovetoBack();
        }
        else if (command.startsWith(ADDrawingAreaConstants.APPLY_LAYOUT))
        {
            this.m_drawingArea.onApplyLayout(command);
        }
        else if (command.startsWith(ADDrawingAreaConstants.SEQUENCE_LAYOUT))
        {
            this.onApplySequenceLayout(command);
        }
        else if (command.startsWith(ADDrawingAreaConstants.RELAYOUT))
        {
            this.m_drawingArea.onApplyLayout(command);
        }
        else if (command.endsWith(ADDrawingAreaConstants.INCREMENTAL_LAYOUT))
        {
            this.onApplyIncrementalLayout();
        }
        else if (ADDrawingAreaConstants.CLOSE_GRAPH.equals(command))
        {
            this.onCloseGraph();
        }
        else if (ADDrawingAreaConstants.APP_EXIT.equals(command))
        {
            this.onAppExit();
        }
        else if (ADDrawingAreaConstants.CLEAR_ALL.equals(command))
        {
            this.onClearAll();
        }
        else if (ADDrawingAreaConstants.CLEAR_HISTORY.equals(command))
        {
            this.onClearHistory();
        }
        else if (ADDrawingAreaConstants.DELETE_SELECTED.equals(command))
        {
            // The delete should be handled in handleKeyDown (issue 5087018)
            //this.onDeleteSelected();
        }
        else if (ADDrawingAreaConstants.EXPAND_SELECTED.equals(command))
        {
            this.onExpandSelected();
        }
        else if (ADDrawingAreaConstants.COLLAPSE_SELECTED.equals(command))
        {
            this.onCollapseSelected();
        }
        else if (ADDrawingAreaConstants.INCREMENTAL_LAYOUT_AFTER_ACTION.equals(command))
        {
            this.onIncrementalLayoutAfterAction();
        }
        else if (ADDrawingAreaConstants.DUPLICATE_GRAPH.equals(command))
        {
            this.onDuplicateGraph();
        }
        else if (ADDrawingAreaConstants.REVERT_GRAPH.equals(command))
        {
            this.onRevertGraph();
        }
        else if (ADDrawingAreaConstants.SCROLL_LEFT.equals(command))
        {
            this.onScrollGraph(KeyEvent.VK_LEFT);
        }
        else if (ADDrawingAreaConstants.SCROLL_RIGHT.equals(command))
        {
            this.onScrollGraph(KeyEvent.VK_RIGHT);
        }
        else if (ADDrawingAreaConstants.SCROLL_UP.equals(command))
        {
            this.onScrollGraph(KeyEvent.VK_UP);
        }
        else if (ADDrawingAreaConstants.SCROLL_DOWN.equals(command))
        {
            this.onScrollGraph(KeyEvent.VK_DOWN);
        }
        else if (ADDrawingAreaConstants.NEW_GRAPH.equals(command))
        {
            //			this.m_drawingArea.onNewGraph();
        }
        else if (ADDrawingAreaConstants.PALETTE_WINDOW.equals(command))
        {
            //			this.m_drawingArea.onShowPalette();
        }
        else if (ADDrawingAreaConstants.PASTE_GRAPH.equals(command))
        {
            //         this.onPasteGraph();
        }
        else if (ADDrawingAreaConstants.PRINT_SETUP.equals(command))
        {
            //			this.m_drawingArea.onPrintSetup();
        }
        else if (ADDrawingAreaConstants.REDO.equals(command))
        {
            this.onRedoAction();
        }
        else if (ADDrawingAreaConstants.REFRESH_GRAPH.equals(command))
        {
            this.onRefresh();
        }
        else if (ADDrawingAreaConstants.SAVE_GRAPH.equals(command))
        {
            //			this.m_drawingArea.onSaveGraph();
        }
        else if (ADDrawingAreaConstants.SAVE_GRAPH_AS.equals(command))
        {
            //			this.m_drawingArea.onSaveGraphAs();
        }
        else if (ADDrawingAreaConstants.SAVE_GRAPH_AS_IMAGE.equals(command))
        {
            //			this.m_drawingArea.onSaveGraphAsImage();
        }
        else if (ADDrawingAreaConstants.SELECT_ALL.equals(command))
        {
            this.onSelectAll();
        }
        else if (ADDrawingAreaConstants.SELECT_EDGES.equals(command))
        {
            this.onSelectEdges();
        }
        else if (ADDrawingAreaConstants.SELECT_LABELS.equals(command))
        {
            this.onSelectLabels();
        }
        else if (ADDrawingAreaConstants.SELECT_NODES.equals(command))
        {
            this.onSelectNodes();
        }
        else if (ADDrawingAreaConstants.UNDO.equals(command))
        {
            this.onUndoAction();
        }
        else if (ADDrawingAreaConstants.ZOOM_CUSTOM.equals(command))
        {
            this.onCustomZoom();
        }
        else if (ADDrawingAreaConstants.ZOOM_FIT.equals(command))
        {
            this.onFitInWindow();
        }
        else if (ADDrawingAreaConstants.NEXT_STATE.equals(command))
        {
            this.onNextState();
        }
        else if (command.startsWith(ADDrawingAreaConstants.ZOOM))
        {
            this.onZoom(command);
        }
        else if (command.startsWith(ADDrawingAreaConstants.GRID_TYPE))
        {
            this.onGridType(command);
        }
        else if (ADDrawingAreaConstants.GRID_SIZE_CUSTOM.equals(command))
        {
            this.onCustomGridSize();
        }
        else if (command.startsWith(ADDrawingAreaConstants.GRID_SIZE))
        {
            this.onGridSize(command);
            //		} else if (command.startsWith(ADDrawingAreaConstants.SNAP_TO_GRID)) {
            //			this.onSnapToGrid();
        }
        else if (command.startsWith(ADDrawingAreaConstants.EXPAND_ALL))
        {
            this.onExpandCollapseAll(true);
        }
        else if (command.startsWith(ADDrawingAreaConstants.COLLAPSE_ALL))
        {
            this.onExpandCollapseAll(false);
        }
        else if (ADDrawingAreaConstants.LAYOUT_PROPERTIES.equals(command))
        {
            //			this.m_drawingArea.onLayoutProperties();
        }
        else if (ADDrawingAreaConstants.DRAWING_PREFERENCES.equals(command))
        {
            //			this.m_drawingArea.onDrawingPreferences();
        }
        else if (
                (ADDrawingAreaConstants.MOVE_DONE.equals(command))
                || (ADDrawingAreaConstants.MOVE_LEFT.equals(command))
                || (ADDrawingAreaConstants.MOVE_RIGHT.equals(command))
                || (ADDrawingAreaConstants.MOVE_UP.equals(command))
                || (ADDrawingAreaConstants.MOVE_DOWN.equals(command)))
        {
            // do nothing but prevent an "Unhandled command" trace.
        }
        else if (ADDrawingAreaConstants.NODE_RESIZE_TALLER.equals(command) ||
                ADDrawingAreaConstants.NODE_RESIZE_SHORTER.equals(command) ||
                ADDrawingAreaConstants.NODE_RESIZE_WIDER.equals(command) ||
                ADDrawingAreaConstants.NODE_RESIZE_THINNER.equals(command))
        {
            onIncrementalResizeNodes(command);
        }
        else if (ADDrawingAreaConstants.ACTION_ABORT.equals(command))
        {
            onCancel();
        }
        else
        {
            getControl().enterModeFromButton(command);
        }
        
        onPostActionaPerformed();
    }
    
    /**
     * This method sets whether or not invisible objects will be drawn
     * when they are being dragged in the current graph window.
     */
    public void onDrawInvisibleOnDragging()
    {
        TSEGraphWindow graphWindow = getGraphWindow();
        
        graphWindow.setDrawInvisibleOnDragging(!graphWindow.isDrawInvisibleOnDragging());
    }
    
    /**
     * This method performs an incremental layout.
     */
    public void onApplyIncrementalLayout()
    {
        Debug.out.println(" ADDrawingAreaActions : onApplyIncrementalLayout");
        String command = ADDrawingAreaConstants.APPLY_LAYOUT + "." + this.m_drawingArea.getLayoutInputTailor().getLayoutStyle(this.m_drawingArea.getGraph()) + "." + ADDrawingAreaConstants.INCREMENTAL_LAYOUT; // jyothi
        Debug.out.println(" Jyothi -- ADDrawingAreaActions : onApplyIncrementalLayout() -- command = "+command);
        this.m_drawingArea.onApplyLayout(command); //jyothi
    }
    
    /**
     * This method sets the incremental layout after action option.
     */
    public void onIncrementalLayoutAfterAction()
    {
    }
    
     /* jyothi
   public void graphChanged(TSEGraphChangeEvent event)
   {
      if (event.getChangeType() == TSEGraphChangeEvent.GRAPH_CHANGE)
      {
         this.m_drawingArea.setChanged(true);
      }
      
      if (event.getChangeType() == TSEGraphChangeEvent.SELECTION_CHANGE && getEnableSelectionEvents() == true)
      {
         onSelectionChange(event);
      }
      
      if ((event.getChangeType() == TSEGraphChangeEvent.NEW_GRAPH || event.getChangeType() == TSEGraphChangeEvent.GRAPH_CHANGE) && this.m_drawingArea.isAutoFitInWindow())
      {
         event.getSourceWindow().fitInWindow(true);
      }
      
      if (event.getChangeType() == TSEGraphChangeEvent.VIEW_CHANGE)
      {
         onViewChange(event);
      }
   }
      */
    //jyothi
    public void graphChanged(TSGraphChangeEvent event)
    {
        long type = event.getType();
        if (    (type == TSGraphChangeEvent.GRAPH_INSERTED) ||
                (type == TSGraphChangeEvent.GRAPH_REMOVED) ||
                (type == TSGraphChangeEvent.GRAPH_RENAMED) ||
                (type == TSGraphChangeEvent.GRAPH_DISCARDED) ||
                (type == TSGraphChangeEvent.NODE_INSERTED) ||
                (type == TSGraphChangeEvent.NODE_REMOVED) ||
                (type == TSGraphChangeEvent.NODE_RENAMED) ||
                (type == TSGraphChangeEvent.NODE_DISCARDED) ||
                (type == TSGraphChangeEvent.EDGE_INSERTED) ||
                (type == TSGraphChangeEvent.EDGE_REMOVED) ||
                (type == TSGraphChangeEvent.EDGE_RENAMED) ||
                (type == TSGraphChangeEvent.EDGE_DISCARDED) ||
                (type == TSGraphChangeEvent.EDGE_ENDNODE_CHANGED)
                )
        {
            if (m_drawingArea != null)
            {
                this.m_drawingArea.setChanged(true);
            }
        }
        if ((type == TSGraphChangeEvent.GRAPH_INSERTED || type == TSGraphChangeEvent.ANY_CHANGE) && this.m_drawingArea.isAutoFitInWindow())
        {
            getGraphWindow().fitInWindow(true);
        }
    }
    
    public void selectionChanged(TSESelectionChangeEvent event)
    {
        if (event.getType() == TSESelectionChangeEvent.GRAPH_SELECTION_CHANGED && getEnableSelectionEvents() == true)
        {
            onSelectionChange(event);
        }
    }
    
    public void viewportChanged(TSEViewportChangeEvent event)
    {
        if ((event.getType() == TSEViewportChangeEvent.ZOOM) || (event.getType() == TSEViewportChangeEvent.PAN))
        {
            onViewChange(event);
        }
    }
    
    protected boolean m_enableSelectionEvents = true;
    
   /*
    * By default we fire Selection change notifications when we receive Graph Change events from the GET.
    */
    public boolean setEnableSelectionEvents(boolean enable)
    {
        boolean enabled = m_enableSelectionEvents;
        m_enableSelectionEvents = enable;
        return enabled;
    }
    
   /*
    * Get accessor, returns true if selection events are disabled.
    */
    public boolean getEnableSelectionEvents()
    {
        return m_enableSelectionEvents;
    }
    
   /*
    * Called by the graph change notication,
    */
    //public void onSelectionChange(TSEGraphChangeEvent event)
    public void onSelectionChange(TSESelectionChangeEvent event)
    {
        //ETSystem.out.println("ADDrawingAreaActions.onSelectionChange()");
    }
    
   /*
    *  Called by the graph change notication.
    */
    //public void onViewChange(TSEGraphChangeEvent event)
    public void onViewChange(TSEViewportChangeEvent event)
    {
        double zoomLevel = 1.0;
        ADGraphWindow graphWindow = getGraphWindow();
        if (graphWindow != null)
        {
            zoomLevel = graphWindow.getZoomLevel() * 100;
        }
        zoomLevel = java.lang.Math.round(zoomLevel * 100.0) / 100.0;
        
        String string = String.valueOf(zoomLevel);
        
        if (string.endsWith(".0"))
        {
            string = string.substring(0, string.lastIndexOf(".0"));
        }
        
        // Since we are changing the zoom combobox programmatically,
        // we don't want it to fire action events that would result
        // in an infinite loop, so we tell it to not fire events
        // for this call.
        
        ADComboBox zoomComboBox = this.m_drawingArea.getZoomComboBox();
        if (zoomComboBox.getSelectedItem().equals(string + "%"))
            return;
        
        zoomComboBox.setFireEvents(false);
        zoomComboBox.setSelectedItem(string + "%");
        zoomComboBox.setFireEvents(true);
    }
    
   /*
    * Gets call after actions need to call this function it resets the tool state to the default action.
    */
    protected void onPostActionaPerformed()
    {
    }
    
    protected IDrawingAreaControl getControl()
    {
        return this.m_drawingArea;
    }
    
    private void onIncrementalResizeNodes(String command)
    {
        int cmdCode = ADDrawingAreaConstants.NODE_RESIZE_TALLER_CMD;
        
        if (command.equals(ADDrawingAreaConstants.NODE_RESIZE_SHORTER))
            cmdCode = ADDrawingAreaConstants.NODE_RESIZE_SHORTER_CMD;
        
        else if (command.equals(ADDrawingAreaConstants.NODE_RESIZE_WIDER))
            cmdCode = ADDrawingAreaConstants.NODE_RESIZE_WIDER_CMD;
        
        else if (command.equals(ADDrawingAreaConstants.NODE_RESIZE_THINNER))
            cmdCode = ADDrawingAreaConstants.NODE_RESIZE_THINNER_CMD;
        
        m_drawingArea.onHandleResize(cmdCode);
    }
    
    private void onCancel()
    {
        m_drawingArea.onHandleCancel();
    }

    protected void clearDrawingAreaControlRefs() 
    {
	m_drawingArea = null;
    }

}

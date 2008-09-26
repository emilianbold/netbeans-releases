/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.uml.drawingarea.palette.context;

import java.util.List;
import org.netbeans.api.visual.action.WidgetAction.State;
import org.netbeans.api.visual.action.WidgetAction.WidgetMouseEvent;
import org.netbeans.modules.uml.drawingarea.palette.context.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.openide.util.Lookup;

/**
 *
 * @author treyspiva
 */
public class SwingPaletteManager implements ContextPaletteManager
{
    private static final Color FILLCOLOR = Color.WHITE;
    private static final Color DRAWCOLOR = Color.BLACK;
    
    static final int SPACE_FROM_WIDGET = 10;
    
    private ObjectScene scene = null;
    private JComponent decoratorLayer = null;
    private ContextPalette paletteWidget = null;
    private FollowCursorAction followAction = new FollowCursorAction();
    private FollowCursorLeftRightAction followLRAction = new FollowCursorLeftRightAction();
    private boolean followCursor = false;
    
    //if palette is cancelled and reappears it's good to put to the same position (have sense for follow case)
    private Widget cancelledWidget;
    
    public SwingPaletteManager(ObjectScene scene)
    {
        this(scene, null);
        
        if(scene.getView() == null)
        {
            setDecoratorLayer(scene.createView());
        }
        else
        {
            setDecoratorLayer(scene.getView());
        }
    }

    public SwingPaletteManager(ObjectScene scene, JComponent layer)
    {
        setScene(scene);
        setDecoratorLayer(layer);
    }

    ///////////////////////////////////////////////////////////////
    // ContextPaletteManager implementation
    
    /**
     * Changes the palette to represent the select widget.  If more than one 
     * widget is selected, or no widgets are selected then the palette is 
     * removed.
     * 
     * @param scenePoint Specifies the location to place the palette.  If null
     *                   is specified the best location if determined.
     */
    public void selectionChanged(Point scenePoint)
    {
        selectionChanged(scenePoint, false);
    }
    
    /**
     * Changes the palette to represent the select widget.  If more than one 
     * widget is selected, or no widgets are selected then the palette is 
     * removed.
     * 
     * @param scenePoint Specifies the location to place the palette.  If null
     *                   is specified the best location if determined.
     * @param forceShow If true the palette will be show even if more than 
     *                  one widget is selected.
     */
    protected void selectionChanged(Point scenePoint, boolean forceShow)
    {
        // Clear the previous palette
        cancelPalette();
        
        IDiagram diagram = getScene().getLookup().lookup(IDiagram.class);
        if((diagram != null) && (diagram.getReadOnly() == true))
        {
            return;
        }
        
        // The palette need to decorate the first selected node.
        DesignerScene scene = (DesignerScene)getScene();
        List selectedObjects = scene.getOrderedSelection();
        if((selectedObjects.size() == 1) || 
           ((selectedObjects.size() > 1) && (forceShow == true)))
        {
            Widget selectedWidget = getFirstNode(scene, selectedObjects);
            
            cancelledWidget=selectedWidget;
            
            if(selectedWidget != null)
            {
                showPaletteFor(selectedWidget);

                ContextPaletteModel.FOLLOWMODE follow = ContextPaletteModel.FOLLOWMODE.NONE;
                if(paletteWidget != null)
                {
                    follow = paletteWidget.getModel().getFollowMouseMode();
                }

                if(follow != ContextPaletteModel.FOLLOWMODE.NONE)
                {
                    String activeTool = getScene().getActiveTool();
                    WidgetAction.Chain actions = selectedWidget.createActions(activeTool);

                    if(follow==ContextPaletteModel.FOLLOWMODE.VERTICAL_ONLY)
                    {
                        actions.addAction(followAction);
                    }
                    else if(follow==ContextPaletteModel.FOLLOWMODE.VERTICAL_AND_HORIZONTAL)
                    {
                        actions.addAction(followLRAction);
                    }

                    if(scenePoint!=null && paletteWidget != null)
                    {
                        //TBD avoid duplicate code here and in FollowAction
                        Point viewPt = scene.convertSceneToView(scenePoint);
                        viewPt = SwingUtilities.convertPoint(getScene().getView(), viewPt, getDecoratorLayer());

                        // The palette is going to follow the cursor vertical position.
                        // We may want to change where the horizontal position is located.
                        Point newPt = new Point(paletteWidget.getX(), viewPt.y);
                        paletteWidget.setLocation(newPt);
                    }
                }
            }
        }
    }

    public void cancelPalette()
    {
        if(paletteWidget != null)
        {
            getDecoratorLayer().remove(paletteWidget);
            getDecoratorLayer().repaint();
            paletteWidget = null;
            
            if(cancelledWidget!=null)//remove from widget to which actions was assigned
            {
                WidgetAction.Chain actionChain = cancelledWidget.getActions(getScene().getActiveTool());
                if(actionChain != null)
                {
                    actionChain.removeAction(followAction);
                    actionChain.removeAction(followLRAction);
                }
            }
            cancelledWidget=null;
        }
    }

    public ContextPaletteModel getModel()
    {
        ContextPaletteModel retVal = null;
        
        if(paletteWidget != null)
        {
            retVal = paletteWidget.getModel();
        }
        
        return retVal;
    }
    
    /**
     * Request that the context palette recieve input focus.
     */
    public void requestFocus()
    {
        if(paletteWidget != null)
        {
            paletteWidget.requestFocusInWindow(); 
        }
        else
        {
            selectionChanged(null, true);
            if(paletteWidget != null)
            {
                paletteWidget.requestFocusInWindow();
            }
        }
        
        // Need to make sure that the attached Widget is visible.
        if(paletteWidget != null)
        {
            
        }
    }

    ///////////////////////////////////////////////////////////////
    // Helper Methods
    
    protected void showPaletteFor(Widget widget)
    {   
        if(widget != null)
        {
            Lookup lookup = widget.getLookup();
            ContextPaletteModel model = lookup.lookup(ContextPaletteModel.class);
            if(model != null)
            {
                // For some reason when you use tab key, and the new widget is 
                // off the screen the palette is not being removed off the screen
                // correctly, therefore the current paletteWidget gets orphaned.
                //
                // I think that this is because the selectionChanged method is
                // being called twice (however the selectionChanged method also
                // calls cancelPalettte so it should be removed).  So, instead 
                // requiring others to manage the events, the palette manager
                // should make sure that things are cleaned up correctly.
                if(paletteWidget != null)
                {
                    paletteWidget.getParent().remove(paletteWidget);
                }

                paletteWidget = new ContextPalette(model);            
                paletteWidget.revalidate();

                Point location = getPaletteLocation(widget, paletteWidget);

                Dimension size = paletteWidget.getPreferredSize();
                paletteWidget.setBounds(location.x, location.y, size.width, size.height);

                getDecoratorLayer().add(paletteWidget);
                getDecoratorLayer().revalidate();
            }
            else
            {
                // Check if a parent has the a model.
                showPaletteFor(widget.getParentWidget());
            }
        }
    }
    
    /**
     * The preferred side is the right side of the widget.  If there is
     * not enough space, the palette should be on the left side.
     * 
     * @param widget The widget that will be decorated.
     */
    protected Point getPaletteLocation(Widget widget,
                                       ContextPalette palette)
    {
        int xPos = 0;
        int yPos = 0;
        
        if(widget != null)
        {
            Dimension collapsedDim = palette.getPreferredSize();
        
            // The 10 accounts for the top, and bottom values of the empty border.
            int height = collapsedDim.height - 10;
        
        
            Point location = widget.getPreferredLocation();
            if(location != null)
            {
                Widget parentWidget = widget.getParentWidget();
                if (parentWidget != null)
                {
                    location = parentWidget.convertLocalToScene(location);
                }
                Rectangle actual = widget.getClientArea();

                Point viewLocation = scene.convertSceneToView(location);
                Rectangle viewActual = scene.convertSceneToView(actual);

                // Center the palette on the widget.
                int yCenter = viewLocation.y + (viewActual.height / 2);

                xPos = viewLocation.x + viewActual.width + SPACE_FROM_WIDGET;
                yPos = yCenter - (height / 2);

                JComponent view = getScene().getView();

                int expandedWidth = palette.getExpandedWidth();
                Rectangle viewableRec = view.getVisibleRect();
                int rightViewBounds = viewableRec.x + viewableRec.width;
                if(rightViewBounds < (xPos + expandedWidth))
                {
                    xPos = viewLocation.x - SPACE_FROM_WIDGET - collapsedDim.width;
                    palette.setDirection(PaletteDirection.LEFT);
                }
            }
        }
        
        return new Point(xPos, yPos);
    }
    /**
     * The preferred side is the right side of the widget.  If there is
     * not enough space, do nothing becausae jump have no sense
     * 
     * @param widget The widget that will be decorated.
     * #param left if paletter should be on left side (inner)
     */
    protected Point getPaletteLocationLR(Widget widget,
                                       ContextPalette palette,boolean left)
    {
        Dimension collapsedDim = palette.getPreferredSize();
        
        Point location = widget.getPreferredLocation();
        location = widget.getParentWidget().convertLocalToScene(location);
        
        Rectangle actual = widget.getClientArea();

        Point viewLocaton = scene.convertSceneToView(location);
        Rectangle viewActual = scene.convertSceneToView(actual);

        // Center the palette on the widget.
        int xPos = 0;
        if(!left)
        {
            xPos=viewLocaton.x + viewActual.width + SPACE_FROM_WIDGET;
        }
        else
        {
            xPos=viewLocaton.x + SPACE_FROM_WIDGET;//inner left side
        }
        int yPos = viewLocaton.y + (viewActual.height / 2) - (collapsedDim.height / 2);
        
        //JComponent view = getScene().getView();
        
        //int expandedWidth = palette.getExpandedWidth();
        /*if(view.getWidth() < xPos + expandedWidth)
        {
            xPos = viewLocaton.x - SPACE_FROM_WIDGET - collapsedDim.width;
            palette.setDirection(PaletteDirection.LEFT);
        }*/
        
        return new Point(xPos, yPos);
    }
    
    ///////////////////////////////////////////////////////////////
    // Data Access
    
    public JComponent getDecoratorLayer()
    {
        JComponent retVal = decoratorLayer;
        
        if(retVal == null)
        {
            if(scene.getView() == null)
            {
                retVal = scene.createView();
            }
            else
            {
                retVal = scene.getView();
            }
        }
        
        return retVal;
    }

    protected void setDecoratorLayer(JComponent layer)
    {
        decoratorLayer = layer;
    }

    public ObjectScene getScene()
    {
        return scene;
    }

    public void setScene(ObjectScene scene)
    {
        this.scene = scene;
    }

    private Widget getFirstNode(DesignerScene scene, List selectedObjects)
    {
        Widget retVal = null;
        
        for(Object curObject : selectedObjects)
        {
            if(scene.isNode(curObject) == true)
            {
                retVal = scene.findWidget(curObject);
                break;
            }
        }
                
        return retVal;
    }

    public class FollowCursorAction extends WidgetAction.Adapter
    {

        @Override
        public State mouseMoved(Widget widget, WidgetMouseEvent event) 
        {
            if(paletteWidget != null && paletteWidget.isVisible() && widget.getState().isSelected())
            {   
                Point scenePt = widget.convertLocalToScene(event.getPoint());
                Point viewPt = scene.convertSceneToView(scenePt);
                viewPt = SwingUtilities.convertPoint(getScene().getView(), viewPt, getDecoratorLayer());
                
                // The palette is going to follow the cursor vertical position.
                // We may want to change where the horizontal position is located.
                Point newPt = new Point(paletteWidget.getX(), viewPt.y-6);
                paletteWidget.setLocation(newPt);
            }
            
            return State.REJECTED;
        }
        
    }
    public class FollowCursorLeftRightAction extends WidgetAction.Adapter
    {

        @Override
        public State mouseMoved(Widget widget, WidgetMouseEvent event) 
        {
            if(paletteWidget != null && paletteWidget.isVisible() && widget.getState().isSelected())
            {   
                Rectangle bounds = widget.getBounds();
                boolean left = event.getPoint().x < (bounds.x+bounds.width/2);
                
                Point scenePt = widget.convertLocalToScene(event.getPoint());
                Point viewPt = scene.convertSceneToView(scenePt);
                viewPt = SwingUtilities.convertPoint(getScene().getView(), viewPt, getDecoratorLayer());
                
                // The palette is going to follow the cursor vertical position.
                // We may want to change where the horizontal position is located.
                Point newPt = getPaletteLocationLR(widget, paletteWidget,left);
                newPt.y=viewPt.y-6;
                //Point newPt = new Point(paletteWidget.getX(), viewPt.y);
                if(!paletteWidget.getBounds().contains(viewPt))paletteWidget.setLocation(newPt);
            }
            
            return State.REJECTED;
        }
        
    }
}

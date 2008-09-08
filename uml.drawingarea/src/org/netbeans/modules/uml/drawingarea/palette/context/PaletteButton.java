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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.MouseInputAdapter;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.WidgetAction.State;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.drawingarea.view.DesignerTools;

/**
 * The PaletteButton is displayed in the context palette.  
 * @author treyspiva
 */
public class PaletteButton extends ContextPaletteButton
{
    
    private Widget actionTarget = null;
    private ContextPaletteButtonModel model = null;
    private WidgetAction[] actions = null;
    private String currentTool = "";
    private boolean recordedCurrentTool = false; 
    private PaletteDirection direction = PaletteDirection.RIGHT;
    private boolean filler = true;
    private ArrayList < ContextButtonListener > listeners = 
            new ArrayList < ContextButtonListener >();
    
    public PaletteButton(Widget context, 
                         ContextPaletteButtonModel desc)
    {
        this(context, desc, PaletteDirection.RIGHT, true);
    }
    
    public PaletteButton(Widget context, 
                         ContextPaletteButtonModel desc,
                         PaletteDirection direction)
    {
        this(context, desc, direction, true);
    }
    
    public PaletteButton(Widget context, 
                         ContextPaletteButtonModel desc,
                         PaletteDirection direction,
                         boolean addFiller)
    {
        actionTarget = context;
        model = desc;
                
        Scene scene = context.getScene();
        
        WidgetAction[] descriptionActions = desc.createActions(scene);
        actions = new WidgetAction[descriptionActions.length];
        
        for(int index = 0; index < descriptionActions.length; index++)
        {
            WidgetAction action = descriptionActions[index];
            actions[index] = action;
        }
        
        setLayout(new BorderLayout());
        
        filler = addFiller;
        this.direction = direction;
        initializeUI();
        
        setOpaque(false);
        
        ButtonListener listener = new ButtonListener();
        addMouseListener(listener);
        addMouseMotionListener(listener);
        addKeyListener(new ExecuteAction());
    }
    
    protected Border getFocusBorder()
    {
        return BorderFactory.createLineBorder(UIManager.getColor("List.selectionBackground"), 1);
    }
    
    protected Border getNonFocusedBorder()
    {
        return BorderFactory.createEmptyBorder(1, 1, 1, 1);
    }
            
    protected void initializeUI()
    {
        removeAll();
        
        // I wanted to make the PaletteButton derive from JLabel.  However, 
        // the label was always centering the image.  Instead I made the
        // PaletteButton derive from JPanel.  I then add a label to the 
        // panel.  This handles the layout issues correctly.
        JLabel display = new JLabel();
        display.setIcon(new ImageIcon(model.getImage()));
        display.setOpaque(false);
        
        if(direction == PaletteDirection.RIGHT)
        {
            add(display, BorderLayout.WEST);
            display.setHorizontalAlignment(SwingConstants.LEFT);
        }
        else
        {
            add(display, BorderLayout.EAST);
            display.setHorizontalAlignment(SwingConstants.RIGHT);
        }
        
        if(filler == true)
        {
            JPanel fillerPanel = new JPanel();
            fillerPanel.setOpaque(false);
            fillerPanel.setPreferredSize(new Dimension(0, 0));
            
            add(fillerPanel, BorderLayout.CENTER);
        }
        
        setToolTipText(model.getTooltip());
        setBorder(getNonFocusedBorder());
    }
    
    public PaletteDirection getDirection()
    {
        return direction;
    }

    public void setDirection(PaletteDirection direction)
    {
        this.direction = direction;
        
        initializeUI();
        revalidate();
    }
    
    
    protected WidgetAction.WidgetMouseEvent convertToWidgetAction(MouseEvent e)
    {
        WidgetAction.WidgetMouseEvent retVal = 
                    new WidgetAction.WidgetMouseEvent(e.getID(), e);

        Scene scene = actionTarget.getScene();
        Point scenePt = scene.convertViewToScene(e.getPoint());
        retVal.setPoint(new Point(scenePt));
        
        Point location = scene.getLocation ();
        retVal.translatePoint(location.x, location.y);
        return retVal;
    }
    
    protected MouseEvent convertMouseEvent(MouseEvent event)
    {
        return SwingUtilities.convertMouseEvent(this, event, actionTarget.getScene().getView());
    }
    
    ///////////////////////////////////////////////////////////////
    // Button listener methods.
    
    public void addContextButtonListener(ContextButtonListener listener)
    {
        listeners.add(listener);
    }
    
    public void removeContextButtonListener(ContextButtonListener listener)
    {
        listeners.remove(listener);
    }
    
    protected WidgetAction.State fireWidgetActionHandedEvent(WidgetAction.State state)
    {
        if(state.isLockedInChain() == true)
        {
            for(ContextButtonListener listener : listeners)
            {
                listener.actionPerformed(this, true);
            }
        }
        else if(state.isConsumed() == true)
        {
            for(ContextButtonListener listener : listeners)
            {
                listener.actionPerformed(this, true);
            }
        }
        
        return state;
    }
    
    public class ButtonListener extends MouseInputAdapter
    {
        @Override
        public void mousePressed(MouseEvent event)
        {
            Scene scene = actionTarget.getScene();
            
            ArrayList < ButtonWidgetAction > delegateActions = 
                    new ArrayList < ButtonWidgetAction >();
            
            if(actionTarget.getActions(DesignerTools.CONTEXT_PALETTE) == null)
            {
                actionTarget.createActions(DesignerTools.CONTEXT_PALETTE);
            }
            
            WidgetAction.Chain paletteActions = actionTarget.getActions(DesignerTools.CONTEXT_PALETTE);
            for(int index = paletteActions.getActions().size();
                index > 0;
                index--)
            {
                paletteActions.removeAction(index-1);
            }
            
            
            // If the user right clicks while drawing a relationship we will
            // get the tool that we made active.
            if(scene.getActiveTool().equals(DesignerTools.CONTEXT_PALETTE) != true)
            {
                currentTool = scene.getActiveTool();
                recordedCurrentTool = true;
            }
            
            scene.setActiveTool(DesignerTools.CONTEXT_PALETTE);
            for(WidgetAction action : actions)
            {
                ButtonWidgetAction newAction = new ButtonWidgetAction(action);
                actionTarget.getActions(DesignerTools.CONTEXT_PALETTE).addAction(newAction);
                delegateActions.add(newAction);
            }
            
            Rectangle sceneActual = actionTarget.getClientArea();
            
            Point localTargetPt = actionTarget.getLocation();
            Point sceneTargetPnt=actionTarget.getParentWidget().convertLocalToScene(localTargetPt);
            localTargetPt = sceneTargetPnt;
            
            Point eventPoint=event.getPoint();
            
            ContextPaletteModel.FOLLOWMODE mode = model.getPaletteModel().getFollowMouseMode();
            if(mode == ContextPaletteModel.FOLLOWMODE.VERTICAL_AND_HORIZONTAL)
            {
                int x=getParent().getParent().getLocation().x+getParent().getLocation().x+event.getPoint().x;
                boolean left=x<(sceneTargetPnt.x+sceneActual.x+sceneActual.getWidth());//menu inside/left from right border
                localTargetPt.x += left ? sceneActual.x : sceneActual.x+sceneActual.getWidth();//left-rigth jump
            }
            else 
            {
                localTargetPt.x += sceneActual.getWidth() / 2;
            }
            localTargetPt.y += sceneActual.getHeight() / 2;

            Point viewPt = scene.convertSceneToView(localTargetPt);
            
            if(mode != ContextPaletteModel.FOLLOWMODE.NONE)
            {
                viewPt.y=getParent().getParent().getLocation().y+getParent().getLocation().y+event.getPoint().y;//provide correct y if necessary
            }

            
            MouseEvent newEvent = new MouseEvent(event.getComponent(), 
                                                 event.getID(),
                                                 event.getWhen(),
                                                 event.getModifiers(),
                                                 viewPt.x,
                                                 viewPt.y,
                                                 event.getClickCount(),
                                                 event.isPopupTrigger(),
                                                 event.getButton());
            
            scene.getView().dispatchEvent(newEvent);
            
        }

        @Override
        public void mouseDragged(MouseEvent event)
        {
            super.mouseDragged(event);
            
            Scene scene = actionTarget.getScene();
            scene.getView().dispatchEvent(convertMouseEvent(event));
        }

        @Override
        public void mouseReleased(MouseEvent event)
        {
            super.mouseReleased(event);
            
            Scene scene = actionTarget.getScene();
            try
            {
                scene.getView().dispatchEvent(convertMouseEvent(event));
            }
            finally
            {
                //need to be sure to restore tools etc even if something fails in event handling
                if (recordedCurrentTool) 
                {
                    scene.setActiveTool(currentTool);
                    recordedCurrentTool = false;
                }
                removeMouseListener(this);
                removeMouseMotionListener(this);
            }
        }
        
    }
    
    public class ButtonWidgetAction implements WidgetAction
    {
        private WidgetAction delegatedAction = null;
        
        public ButtonWidgetAction(WidgetAction action)
        {
            delegatedAction = action;
        }
        
        public State mouseClicked(Widget widget, WidgetMouseEvent event)
        {
            State state = delegatedAction.mouseClicked(widget, event);
            return fireWidgetActionHandedEvent(state);
        }

        public State mousePressed(Widget widget, WidgetMouseEvent event)
        {
            if(widget!=actionTarget)
            {
                Point tmp=widget.convertLocalToScene(event.getPoint());
                tmp=actionTarget.convertSceneToLocal(tmp);
                event.setPoint(tmp);
            }
            State state = delegatedAction.mousePressed(actionTarget, event);
            actionTarget.getScene().validate();
            return fireWidgetActionHandedEvent(state);
        }

        public State mouseReleased(Widget widget, WidgetMouseEvent event)
        {
            State state =  delegatedAction.mouseReleased(widget, event);
            if (recordedCurrentTool) 
            {
                actionTarget.getScene().setActiveTool(currentTool);
                recordedCurrentTool = false;
            }
            return fireWidgetActionHandedEvent(state);
        }

        public State mouseEntered(Widget widget, WidgetMouseEvent event)
        {
            State state = delegatedAction.mouseEntered(widget, event);
            return fireWidgetActionHandedEvent(state);
        }

        public State mouseExited(Widget widget, WidgetMouseEvent event)
        {
            State state = delegatedAction.mouseExited(widget, event);
            return fireWidgetActionHandedEvent(state);
        }

        public State mouseDragged(Widget widget, WidgetMouseEvent event)
        {
            State state = delegatedAction.mouseDragged(widget, event);
            return fireWidgetActionHandedEvent(state);
        }

        public State mouseMoved(Widget widget, WidgetMouseEvent event)
        {
            State state = delegatedAction.mouseMoved(widget, event);
            return fireWidgetActionHandedEvent(state);
        }

        public State mouseWheelMoved(Widget widget, WidgetMouseWheelEvent event)
        {
            State state = delegatedAction.mouseWheelMoved(widget, event);
            return fireWidgetActionHandedEvent(state);
        }

        public State keyTyped(Widget widget, WidgetKeyEvent event)
        {
            State state = delegatedAction.keyTyped(widget, event);
            return fireWidgetActionHandedEvent(state);
        }

        public State keyPressed(Widget widget, WidgetKeyEvent event)
        {
            State state = delegatedAction.keyPressed(widget, event);
            return fireWidgetActionHandedEvent(state);
        }

        public State keyReleased(Widget widget, WidgetKeyEvent event)
        {
            State state = delegatedAction.keyReleased(widget, event);
            return fireWidgetActionHandedEvent(state);
        }

        public State focusGained(Widget widget, WidgetFocusEvent event)
        {
            State state = delegatedAction.focusGained(widget, event);
            return fireWidgetActionHandedEvent(state);
        }

        public State focusLost(Widget widget, WidgetFocusEvent event)
        {
            State state = delegatedAction.focusLost(widget, event);
            return fireWidgetActionHandedEvent(state);
        }

        public State dragEnter(Widget widget, WidgetDropTargetDragEvent event)
        {
            State state = delegatedAction.dragEnter(widget, event);
            return fireWidgetActionHandedEvent(state);
        }

        public State dragOver(Widget widget, WidgetDropTargetDragEvent event)
        {
            State state = delegatedAction.dragOver(widget, event);
            return fireWidgetActionHandedEvent(state);
        }

        public State dropActionChanged(Widget widget, WidgetDropTargetDragEvent event)
        {
            State state = delegatedAction.dropActionChanged(widget, event);
            return fireWidgetActionHandedEvent(state);
        }

        public State dragExit(Widget widget, WidgetDropTargetEvent event)
        {
            State state = delegatedAction.dragExit(widget, event);
            return fireWidgetActionHandedEvent(state);
        }

        public State drop(Widget widget, WidgetDropTargetDropEvent event)
        {
            State state = delegatedAction.drop(widget, event);
            return fireWidgetActionHandedEvent(state);
        }
        
    }

    public class ExecuteAction extends KeyAdapter
    {

        @Override
        public void keyPressed(KeyEvent e)
        {
            for(WidgetAction action : actions)
            {
                WidgetAction.WidgetKeyEvent keyEvent = new WidgetAction.WidgetKeyEvent((long)e.getID(), e);
                if(action.keyPressed(actionTarget, keyEvent) == State.CONSUMED)
                {
                    e.consume();
                    break;
                }
            }
        }
        
    }
    
}

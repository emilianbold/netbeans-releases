/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.uml.drawingarea.actions;

import java.awt.Point;
import java.awt.event.KeyEvent;
import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.action.MoveStrategy;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Utilities;

/**
 *
 * @author treyspiva
 */
public class MoveNodeKeyAction extends WidgetAction.LockedAdapter
{

    private MoveStrategy strategy;
    private MoveProvider provider;
    private Widget movingWidget = null;
    private Point dragSceneLocation = null;
    private Point originalSceneLocation = null;
    private Point initialWidgetLocation = null;

    public MoveNodeKeyAction(MoveStrategy strategy, MoveProvider provider)
    {
        this.strategy = strategy;
        this.provider = provider;
    }

    protected boolean isLocked()
    {
        return movingWidget != null;
    }

    public State keyPressed (Widget widget, WidgetKeyEvent event)
    {
        if (isLocked())
        {
            return State.createLocked(widget, this);
        }
        
        boolean controlKeyPressed = event.isControlDown();
        if(Utilities.isMac() == true)
        {
            controlKeyPressed = event.isMetaDown();
        }
            
        if((controlKeyPressed == true) && 
          ((event.getKeyCode() == KeyEvent.VK_UP) ||
           (event.getKeyCode() == KeyEvent.VK_DOWN) ||
           (event.getKeyCode() == KeyEvent.VK_LEFT) ||
           (event.getKeyCode() == KeyEvent.VK_RIGHT)))
        {
            movingWidget = widget;
            
            initialWidgetLocation = getWidgetLocation(widget);
            
            originalSceneLocation = provider.getOriginalLocation(widget);
            if (originalSceneLocation == null)
            {
                originalSceneLocation = new Point();
            }
            
            // TODO: I do not think I need the dragSceneLocation any longer
            dragSceneLocation = initialWidgetLocation;
            provider.movementStarted(widget);
            return State.createLocked(widget, this);
        }
        return State.REJECTED;
    }

    public State keyReleased (Widget widget, WidgetKeyEvent event)
    {
        boolean state;
        Point newWidgetLocation = getNewLocation(widget, event);
        
        if (initialWidgetLocation != null && initialWidgetLocation.equals(newWidgetLocation))
        {
            state = true;
        }
        else
        {
            state = move(widget, newWidgetLocation);
        }
        
        if (state)
        {
            movingWidget = null;
            dragSceneLocation = null;
            originalSceneLocation = null;
            initialWidgetLocation = null;
            provider.movementFinished(widget);
        }
        return state ? State.CONSUMED : State.REJECTED;
    }

    private Point getNewLocation (Widget widget, WidgetKeyEvent event)
    {
        Point location = getWidgetLocation(widget);
        if(event.getKeyCode() == KeyEvent.VK_UP)
        {
            location.y -= 10;
        }
        else if(event.getKeyCode() == KeyEvent.VK_DOWN)
        {
            location.y += 10;
        }
        else if(event.getKeyCode() == KeyEvent.VK_LEFT)
        {
            location.x -= 10;
        }
        else if(event.getKeyCode() == KeyEvent.VK_RIGHT)
        {
            location.x += 10;

        }
        
        return location;
    }

    private Point getWidgetLocation(Widget widget)
    {

        Point retVal = widget.getPreferredLocation();
        if (retVal == null)
        {
            retVal = widget.getLocation();
        }

        return retVal;
    }
    
    private boolean move(Widget widget, Point newLocation)
    {
        if (movingWidget != widget)
        {
            return false;
        }
        initialWidgetLocation = null;
        newLocation = widget.getParentWidget().convertLocalToScene(newLocation);
        Point location = new Point(originalSceneLocation.x + newLocation.x - dragSceneLocation.x, originalSceneLocation.y + newLocation.y - dragSceneLocation.y);
        provider.setNewLocation(widget, strategy.locationSuggested(widget, originalSceneLocation, location));
        return true;
    }
}

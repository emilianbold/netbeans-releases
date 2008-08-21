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
import java.util.Set;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.WidgetAction.State;
import org.netbeans.api.visual.action.WidgetAction.WidgetKeyEvent;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.drawingarea.palette.context.ContextPaletteManager;
import org.openide.util.Utilities;

/**
 *
 * @author treyspiva
 */
public class MoveNodeKeyAction extends WidgetAction.Adapter
{

    public MoveNodeKeyAction()
    {
    }

    @Override
    public State keyPressed(Widget widget, WidgetKeyEvent event)
    {
        State retVal = State.REJECTED;
        
        boolean controlKeyPressed = event.isControlDown();
        if(Utilities.isMac() == true)
        {
            controlKeyPressed = event.isMetaDown();
        }
            
        if(controlKeyPressed == true)
        {
//            Point location = widget.getLocation();

            boolean update = false;
            if(event.getKeyCode() == KeyEvent.VK_UP)
            {
//                location.y -= 10;
//                update = true;
                updateSelectedWidgets(widget.getScene(), 0, -10);
            }
            else if(event.getKeyCode() == KeyEvent.VK_DOWN)
            {
//                location.y += 10;
//                update = true;
                updateSelectedWidgets(widget.getScene(), 0, 10);
            }
            else if(event.getKeyCode() == KeyEvent.VK_LEFT)
            {
//                location.x -= 10;
//                update = true;
                updateSelectedWidgets(widget.getScene(), -10, 0);
            }
            else if(event.getKeyCode() == KeyEvent.VK_RIGHT)
            {
//                location.x += 10;
//                update = true;
                updateSelectedWidgets(widget.getScene(), 10, 0);

            }

//            if(update == true)
//            {
//                ContextPaletteManager manager = widget.getScene().getLookup().lookup(ContextPaletteManager.class);
//                if(manager != null)
//                {
//                    manager.cancelPalette();
//                }
//
//                widget.setPreferredLocation(location);
//
//                if(manager != null)
//                {
//                    manager.selectionChanged(null);
//                }
//            }
        }
        
        return retVal;
    }
    
    protected void updateSelectedWidgets(Scene scene , int dx, int dy)
    {
        ContextPaletteManager manager = scene.getLookup().lookup(ContextPaletteManager.class);
        if(manager != null)
        {
            manager.cancelPalette();
        }
        
        if (scene instanceof GraphScene)
        {
            GraphScene gScene = (GraphScene) scene;
            Set selected = gScene.getSelectedObjects();
            
            for(Object curSelected : selected)
            {
                Widget widget = gScene.findWidget(curSelected);
                if(widget != null)
                {
                    Point location = widget.getLocation();
                    location.x += dx;
                    location.y += dy;
                    
                    widget.setPreferredLocation(location);
                }
            }
        }
        
        if(manager != null)
        {
            manager.selectionChanged(null);
        }
    }
}

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

package org.netbeans.modules.uml.diagrams.actions;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.animator.AnimatorEvent;
import org.netbeans.api.visual.animator.AnimatorListener;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.drawingarea.palette.context.ContextPaletteManager;
import org.netbeans.modules.uml.drawingarea.util.Util;

public class RotateAction extends WidgetAction.Adapter
{
    
    @Override
    public State mousePressed(Widget widget, WidgetMouseEvent event) 
    {
        rotateWidget(widget);
        return State.CONSUMED;
    }
    
    @Override
    public State keyPressed(Widget widget, WidgetKeyEvent event)
    {
        State retVal = State.REJECTED;
        
        if(Util.isPaletteExecute(event) == true)
        {
            ContextPaletteManager manager = null;
            if(widget.getScene() != null)
            {
                final Scene scene = widget.getScene();
                scene.getSceneAnimator().getPreferredBoundsAnimator().addAnimatorListener(new AnimatorListener() {

                    public void animatorStarted(AnimatorEvent event)
                    {
                        
                    }

                    public void animatorReset(AnimatorEvent event)
                    {
                        
                    }

                    public void animatorFinished(AnimatorEvent event)
                    {
                        ContextPaletteManager manager = scene.getLookup().lookup(ContextPaletteManager.class);
                        if(manager != null)
                        {
                            manager.selectionChanged(null);
                        }
                    }

                    public void animatorPreTick(AnimatorEvent event)
                    {
                        
                    }

                    public void animatorPostTick(AnimatorEvent event)
                    {
                        
                    }
                });
                
                manager = scene.getLookup().lookup(ContextPaletteManager.class);
                
                if(manager != null)
                {
                    manager.cancelPalette();
                }
            }
            
            rotateWidget(widget);
            
            retVal = State.CONSUMED;
        }
        
        return retVal;
    }

    private void rotateWidget(Widget widget)
    {
        Rectangle bounds = widget.getPreferredBounds();

        Scene scene = widget.getScene();
        widget.setMinimumSize(null);
        Rectangle newBounds = new Rectangle(bounds.x, bounds.y, bounds.height, bounds.width);

        scene.getSceneAnimator().animatePreferredBounds(widget, bounds); // start bound
        scene.getSceneAnimator().animatePreferredBounds(widget, newBounds); // end bounds
        scene.revalidate();
    }
}

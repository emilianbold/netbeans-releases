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

package org.netbeans.modules.uml.diagrams.actions.sqd;

import java.awt.Point;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.LifelineWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.MessagePinWidget;
import org.netbeans.modules.uml.drawingarea.actions.ActionProvider;

/**
 *
 * @author sp153251
 */
public class AdjustAfterBoxChangeProvider implements ActionProvider {

    private LifelineWidget llW;
    private int oldLineY;
    
    /**
     * should be created before actual change, to store previous sizes to be able to find difference
     * (or at least before validation)
     * @param widget
     */
    public AdjustAfterBoxChangeProvider(LifelineWidget widget)
    {
        llW=widget;
        
        Widget lineParent = llW.getLine().getParentWidget();
        
        oldLineY = 0;
        if(lineParent != null)
        {
            oldLineY = lineParent.convertLocalToScene(llW.getLine().getLocation()).y;
        }
    }
    
    public void perfomeAction() {
        int newLineY = 0;
        
        Widget lineParent = llW.getLine().getParentWidget();
        if(lineParent != null)
        {
            newLineY = lineParent.convertLocalToScene(llW.getLine().getLocation()).y;
        }
        
        if((newLineY-oldLineY)!=0)
        {
            if(llW.isCreated())
            {
                MessagePinWidget pin=null;
                for(Widget p:llW.getBox().getChildren())
                {
                    if(p instanceof MessagePinWidget)
                    {
                        pin=(MessagePinWidget) p;
                        break;
                    }
                }
                if(pin!=null)
                {
                    ConnectionWidget message=pin.getConnection(0);
                    //use knowlege it should be horizontal
                    int dy=message.getSourceAnchor().getRelatedSceneLocation().y-message.getTargetAnchor().getRelatedSceneLocation().y;
                    if(dy!=0)
                    {
                        Point llWLoc=llW.getPreferredLocation();
                        llWLoc.y+=dy;
                        llW.setPreferredLocation(llWLoc);
                        newLineY+=dy;
                    }
                }
            }
            ArrangeMoveWithBumping.correctPindOnWidget(llW.getLine(), oldLineY-newLineY);
            //TBD bump upper messages if overlap new size
            //
        }
    }

}

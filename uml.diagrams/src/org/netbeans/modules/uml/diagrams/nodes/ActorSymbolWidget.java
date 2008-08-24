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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.uml.diagrams.nodes;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.uml.drawingarea.view.CustomizableWidget;
import org.netbeans.modules.uml.drawingarea.view.ResourceType;
import org.openide.util.NbBundle;

/**
 * This widget represents only actor symbol and do not represent Actor element
 * @author sp153251
 */
public class ActorSymbolWidget extends CustomizableWidget{
    
    private int height;
    private int width;

    public ActorSymbolWidget(Scene scene, String id, String name) {
        super(scene, id, NbBundle.getMessage(ActorSymbolWidget.class, "LBL_StickFigure"));
        
        setBackground(null);
        setForeground(Color.BLACK);
        
        setCustomizableResourceTypes(new ResourceType[]{ResourceType.BACKGROUND,ResourceType.FOREGROUND});
    }
    
    public ActorSymbolWidget(Scene scene, int height, int width, String id, String name)
    {
        this(scene, id, name);
        this.height = height;
        this.width = width;
    }

    @Override
    protected void paintWidget() {
        Rectangle rec=getClientArea();
        Graphics2D gr = getGraphics ();
        int x_c=rec.x+rec.width/2;
        int r=Math.min(rec.width/2, rec.height/8);//height determine sizes, but need to fit into width also
        Paint paint =null;//(Paint) getResourceTable().getProperty(getID()+".bgcolor");
        if(paint==null)paint=getBackground();
        gr.setPaint(paint);
        gr.fillOval(x_c-r, rec.y, 2*r, 2*r);
        Color color = null;//(Color) getResourceTable().getProperty(getID()+".fgcolor");
        if(color==null)color=getForeground();
        gr.setColor(color);
        gr.drawOval(x_c-r, rec.y, 2*r, 2*r);
        int h_free=rec.height-2*r;
        int hand_width=Math.min(rec.width/2, rec.height/4);//height determine sizes, but need to fit into width also
        gr.drawLine(x_c-hand_width, rec.y+2*r+h_free/8, x_c+hand_width, rec.y+2*r+h_free/8);
        gr.drawLine(x_c, rec.y+2*r, x_c, rec.y+2*r+h_free/2);
        gr.drawLine(x_c, rec.y+2*r+h_free/2, x_c-hand_width, rec.y+2*r+h_free);
        gr.drawLine(x_c, rec.y+2*r+h_free/2, x_c+hand_width, rec.y+2*r+h_free);
    }
    
    @Override
    protected Rectangle calculateClientArea()
    {
        if (getBounds() == null || !isPreferredBoundsSet())
        {
            return new Rectangle( -width/2, -height/2, width, height);
        }
        return super.calculateClientArea();
    }

}

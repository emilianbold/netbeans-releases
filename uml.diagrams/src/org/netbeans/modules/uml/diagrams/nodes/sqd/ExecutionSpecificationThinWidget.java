/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.uml.diagrams.nodes.sqd;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.diagrams.layouts.sqd.ExSpeificationLayout;
import org.netbeans.modules.uml.drawingarea.view.UMLEdgeWidget;

/**
 *
 * @author sp153251
 */
public class ExecutionSpecificationThinWidget extends Widget {

    boolean isWhite=false;
    public final int width=10;
    private final int pin_from_border=10;
    private Color fill;
    
    public ExecutionSpecificationThinWidget(Scene scene) {
        super(scene);
        setLayout(ExSpeificationLayout.getLayout());
    }
    

    /**
     * Calculates a client area for the label.
     * @return the client area
     */
    @Override
    protected Rectangle calculateClientArea () {
        if (getChildren().size()==0)
            return super.calculateClientArea ();
        int y0=Integer.MAX_VALUE,y1=Integer.MIN_VALUE;
        //do not check upper border for now
        MessagePinWidget pin=null;
        for(Widget i:getChildren())
        {
            //
            Point loc=i.getPreferredLocation();
            Rectangle rec=i.getPreferredBounds();
            int height=rec.height;
            int y_lo=loc.y+rec.y;
            int y_hi=y_lo+height;
            if(i instanceof MessagePinWidget)
            {
                pin=(MessagePinWidget) i;
                y_lo-=pin.getMarginBefore();
                y_hi+=pin.getMarginAfter();
            }
            else
            {
                y_lo-=pin_from_border;
                y_hi+=pin_from_border;
            }
            if(y_hi>y1)
            {
                y1=y_hi;
            }
            if(y_lo<y0)y0=y_lo;
        }
        Rectangle ret=new Rectangle(-width/2,y0,width,y1-y0);
        return ret;
    }

    
    /**
     * 
     */
    @Override
    protected void paintWidget () {
        if(fill==null)
        {
            //get color from parent (invert)
            Widget par=getParentWidget();
            if(par instanceof ExecutionSpecificationThinWidget)
            {
                isWhite=! ((ExecutionSpecificationThinWidget)par).isWhite();
            }
             if(isWhite)fill=Color.WHITE;
            else fill=Color.LIGHT_GRAY;
       }
         Graphics2D gr = getGraphics ();
        //check graphics
        AffineTransform transform=gr.getTransform();
        double zoom=Math.sqrt(transform.getScaleX()*transform.getScaleX()+transform.getShearY()*transform.getShearY());
         Rectangle rec=getPreferredBounds();
         Paint foreground=getForeground();
         Color colorF = (Color) foreground;
         if(zoom>0.2)
         {
             //it have no sense to fill rectangle with approximate width of 1-2px
             gr.setColor(fill);
             gr.fillRect(rec.x, rec.y, width, rec.height);
         }
         gr.setColor(colorF);
         gr.drawRect(rec.x, rec.y, width, rec.height);
    }

    
    
    
    public ArrayList<UMLEdgeWidget> getMessages()
    {
        ArrayList<UMLEdgeWidget> ret=new ArrayList<UMLEdgeWidget>();
        for(Widget i:getChildren())
        {
            if(i instanceof MessagePinWidget)
            {
                MessagePinWidget pin=(MessagePinWidget) i;
                if(pin.getNumbetOfConnections()==1)ret.add(pin.getConnection(0));
                else throw new UnsupportedOperationException("Now supported only one to one pin-connection relation");
            }
        }
        return ret;
    }

    public boolean isWhite()
    {
        return isWhite;
    }
}

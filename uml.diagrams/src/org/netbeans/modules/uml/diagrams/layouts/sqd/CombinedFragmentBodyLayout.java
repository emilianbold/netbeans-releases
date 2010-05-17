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

package org.netbeans.modules.uml.diagrams.layouts.sqd;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.InteractionOperatorWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.MessagePinWidget;
import org.netbeans.modules.uml.drawingarea.widgets.ContainerWidget;

/**
 *
 * @author sp153251
 */
public class CombinedFragmentBodyLayout implements Layout {
    private static final Layout baseLayout = LayoutFactory.createAbsoluteLayout();

    public CombinedFragmentBodyLayout()
    {
    }
    
    public void layout(Widget widget) {
        Collection<Widget> children = widget.getChildren ();
        int minWidth=0;
        for (Widget child : children) {
            if (child instanceof InteractionOperatorWidget)
            {
                minWidth=child.getPreferredBounds ().width+10;
                break;
            }
        }
        for (Widget child : children) {
            if (child.isVisible ())
            {
                Rectangle rec=child.getPreferredBounds();
                if(rec!=null && !(child instanceof InteractionOperatorWidget) && !(child instanceof MessagePinWidget))if(rec.width<minWidth)rec.width=minWidth;
                child.resolveBounds (child.getPreferredLocation (), rec);
            }
            else
                child.resolveBounds (null, new Rectangle ());
        }
    }

    public boolean requiresJustification(Widget widget) {
        return true;
    }

    public void justify(Widget widget) {
        baseLayout.justify(widget);
        int width=widget.getClientArea().width;
        int x_left=widget.getClientArea().x;
        int y_top=widget.getClientArea().y;
        int height=widget.getClientArea().height;
        for(Widget i:widget.getChildren())
        {
            if(i instanceof MessagePinWidget)
            {
                Point loc=i.getLocation();
                Rectangle rec=i.getBounds();
                loc.x=loc.x<(x_left+width/2-1) ? x_left : x_left+width;
                i.resolveBounds(loc, rec);
            }
            else if(i  instanceof InteractionOperatorWidget)
            {
                Point loc=i.getLocation();
                Rectangle rec=i.getBounds();
                loc.x=-rec.x;//loc.y=-rec.y;
                loc.y=y_top-rec.y;
                i.resolveBounds(loc, rec);
            }
            else if(i instanceof ContainerWidget)
            {
                i.resolveBounds(new Point(x_left,y_top), new Rectangle(0,0,width,height));
            }
            else 
            {
                //operands container
                i.resolveBounds(new Point(x_left,y_top), new Rectangle(0,0,width,height));
                //widget.setMinimumSize(i.getMinimumSize());
            }
        }
    }

}

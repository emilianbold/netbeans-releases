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

package org.netbeans.modules.uml.diagrams.layouts.sqd;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Widget;

/**
 * center 0 point of children to center line of parent widget
 * from preferred location only 'y' is used now
 * @author sp153251
 */
public class LifelineLineLayout implements Layout {

        private int last_total_height;
        public LifelineLineLayout()
        {
        }
    
        /////////////////////////////////////////////////////////////
        public void layout (Widget widget) {
            LayoutFactory.createAbsoluteLayout().layout(widget);
    }

    public boolean requiresJustification (Widget widget) {
        return true;
    }

    public void justify (Widget widget) {
        Rectangle bounds = widget.getClientArea ();
        int parentX1 = bounds.x;
        int parentX2 = parentX1 + bounds.width;
        
        ArrayList<Widget> children=new ArrayList<Widget>(widget.getChildren());
        int height=20;
        if(children.size()!=0)
        {
            Widget w=children.get(children.size()-1);
           height=10+w.getLocation().y+w.getBounds().y+w.getBounds().height;
        }
        //
        for (int i=0;i<children.size();i++) {
            Widget child=children.get(i);
        Point childLocation = child.getLocation ();
        Rectangle childBounds = child.getBounds ();

        childLocation.x = (parentX1 + parentX2) / 2;

        child.resolveBounds (childLocation, null);
                    //
        }
        if(widget.getBounds().height<height)
        {
            Rectangle rec=widget.getBounds();
            rec.height=height;
            widget.resolveBounds(null, rec);
            Rectangle parRec=widget.getParentWidget().getBounds();
            if((widget.getLocation().y+rec.height)>(parRec.y+parRec.height))
            {
                int dy=(widget.getLocation().y+rec.height)-(parRec.y+parRec.height);
                parRec.height+=dy;
                widget.getParentWidget().resolveBounds(null, parRec);
            }
        }
    }

}

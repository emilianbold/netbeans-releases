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

package org.netbeans.modules.uml.diagrams.layouts.sqd;

import java.awt.Point;
import java.util.Collection;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.LifelineWidget;


/**
 * similar to absolute layout except check lifelines positions
 * @author sp153251
 */
public class SQDLayout implements Layout {

    Layout baseLayout=LayoutFactory.createAbsoluteLayout();
    
    private final int min_spacing=10;

    public void layout(Widget widget) {
        baseLayout.layout(widget);
    }

    public boolean requiresJustification(Widget widget) {
       return true;
    }

    public void justify(Widget widget) {
        baseLayout.justify(widget);
        Collection<Widget> children = widget.getChildren ();
        for(Widget w:children)
        {
            boolean change=false;
            Point loc=w.getLocation();
            if(w instanceof LifelineWidget)
            {
                LifelineWidget l=(LifelineWidget)w;
                //all simple lifelines(not created) should be on top, TBD check fo created
                int y_border_shift=l.getBorder().getInsets().top+l.getBounds().y;
                if(!l.isCreated())
                {
                    if(loc.y!=(60-y_border_shift) && !l.isActorLifeline())
                    {
                        loc.y=60-y_border_shift;//TBD where to store this number, may be it's not necessary to use layout at all (used also in creation when link is drawn)
                        change=true;
                    }
                    else if(loc.y!=(20-y_border_shift) && l.isActorLifeline())
                    {
                       loc.y=20-y_border_shift;//TBD where to store this number, may be it's not necessary to use layout at all (used also in creation when link is drawn)
                        change=true;
                    }
                }
            }
            if(change)w.setPreferredLocation(loc);
        }
    }
}

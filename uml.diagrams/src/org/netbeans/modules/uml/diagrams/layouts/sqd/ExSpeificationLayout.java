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
import java.awt.Rectangle;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.ExecutionSpecificationThinWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.MessagePinWidget;

/**
 * the only purpose of this layout is proper horizontal position of children specifications based on links(messages) direction
 * direction is derived from first message only to simplify logic and perfomance if there no messages but child execution specification directon is the same as for child
 * TBD ??it also shpould keep distance between child execution specification and nearest pin??
 * @author sp153251
 */
public class ExSpeificationLayout  implements Layout {

        private static Layout instance;
        private int last_total_height;
        private boolean justify=false;
        private int level=0;
        public final int shift_x=7;//default ??
        private ExSpeificationLayout()
        {
        }
    
        public static Layout getLayout()
        {
            if(instance==null)instance=new ExSpeificationLayout();
            return instance;
        }
        /////////////////////////////////////////////////////////////
        public void layout (Widget wdg) {
        if(wdg instanceof ExecutionSpecificationThinWidget)
        {
            int width=10;
            int ch_width=Integer.MIN_VALUE;
            ExecutionSpecificationThinWidget widget=(ExecutionSpecificationThinWidget) wdg;

            Collection<Widget> children = widget.getChildren ();
                for (Iterator<Widget> iterator=children.iterator();iterator.hasNext();) {
                    Widget ch=iterator.next();
                    if(ch instanceof ExecutionSpecificationThinWidget)
                    {
                        ExecutionSpecificationThinWidget child=(ExecutionSpecificationThinWidget) ch;
                        Rectangle preferredBounds = child.getPreferredBounds ();
                        Point prefLoc=child.getPreferredLocation();
                        //childs always to the right
                        prefLoc.x=shift_x;
                        if(preferredBounds.width>ch_width)ch_width=preferredBounds.width;
                        child.resolveBounds (prefLoc, preferredBounds);
                        //
                    }
                    else ch.resolveBounds(ch.getPreferredLocation(), ch.getPreferredBounds());
                }
            //wdg.resolveBounds(null, new Rectangle(width,0));
        }
        else LayoutFactory.createAbsoluteLayout().layout(wdg);
    }

    public boolean requiresJustification (Widget widget) {
        return true;
    }

    public void justify (Widget wdg) {

        if(wdg instanceof ExecutionSpecificationThinWidget)
        {
            int y_low=Integer.MAX_VALUE,y_max=Integer.MIN_VALUE;
            for(Widget w:wdg.getChildren())
            {
                int y1=w.getLocation().y+w.getBounds().y;
                int y2=y1+w.getBounds().height;
                if(w instanceof MessagePinWidget)
                {
                    MessagePinWidget pin=(MessagePinWidget) w;
                        y1-=pin.getMarginBefore();//margin before most pins
                    y2+=pin.getMarginAfter();//margin after all pins
                }
                if(y1<y_low)y_low=y1;
                if(y2>y_max)y_max=y2;
            }
            Rectangle rec=wdg.getBounds();
            rec.y=y_low;
            rec.height=y_max-y_low;
            rec.width=10;
            wdg.resolveBounds(wdg.getLocation(), rec);
        }
    }
}

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

package org.netbeans.modules.uml.diagrams.layouts.sqd;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.List;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author sp153251
 */
public class OperandsLayout implements Layout {
    
    private static final Layout baseLayout = LayoutFactory.createAbsoluteLayout();
    private int minOperandAdditionVertivcalMargin = 50;//TBD, move to single place (now exists in CFW too)if't marging from nearest message and bottom of combined fragment
    
    
    public OperandsLayout()
    {
    }


    public void layout(Widget widget) {
        baseLayout.layout(widget);
    }

    public boolean requiresJustification(Widget widget) {
        return true;
    }

    public void justify(Widget widget) {
        baseLayout.justify(widget);
        //height
        int height = minOperandAdditionVertivcalMargin;
        Rectangle rec = widget.getClientArea();
        rec.height=30;
        List<Widget> widgets = widget.getChildren();
        for (int i = 0; i < widgets.size(); i++) {
            if ((widgets.get(i).getPreferredLocation().y + minOperandAdditionVertivcalMargin) > height) {
                height = widgets.get(i).getPreferredLocation().y + minOperandAdditionVertivcalMargin;
            }
            widgets.get(i).resolveBounds(widgets.get(i).getPreferredLocation(), rec);
        }
        //TBD chnge 100 to var
        Dimension minS = null;//combinedFragment.getMinimumSize();
        if (minS == null) {
           //minS = new Dimension(100, 100);
            minS = new Dimension(0, height);
        }
        //widget.setMinimumSize(minS);
        if (height > minS.height) {
            minS.height=height;
            //combinedFragment.setMinimumSize(minS);
        }
    }
}

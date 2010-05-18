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

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.layout;

import java.awt.Point;
import java.awt.Rectangle;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author anjeleevich
 */
public class LeftRightLayout implements Layout {
        
    private int hgap;
    private int maxLeftWidth;
    
    public LeftRightLayout(int hgap) {
        this(hgap, 100000);
    }
    
    
    public LeftRightLayout(int hgap, int maxLeftWidth) {
        this.hgap = hgap;
        this.maxLeftWidth = maxLeftWidth;
    }

    
    public void layout(Widget widget) {
        Widget w1 = widget.getChildren().get(0);
        Widget w2 = widget.getChildren().get(1);

        Rectangle b1 = w1.getPreferredBounds();
        Rectangle b2 = w2.getPreferredBounds();
        
        b1.width = Math.min(b1.width, maxLeftWidth);

        int height = Math.max(b1.height, b2.height);
        int width = b1.width + hgap + b2.width;

        int x1 = -b1.x;
        int x2 = width - b2.width - b2.x;

        int y1 = (height - b1.height) / 2 - b1.y;
        int y2 = (height - b2.height) / 2 - b2.y;

        w1.resolveBounds(new Point(x1, y1), b1);
        w2.resolveBounds(new Point(x2, y2), b2);
    }

    
    public boolean requiresJustification(Widget widget) {
        return true;
    }
    

    public void justify(Widget widget) {
        Widget w1 = widget.getChildren().get(0);
        Widget w2 = widget.getChildren().get(1);

        Rectangle bounds = widget.getClientArea();

        Rectangle b1 = w1.getBounds();
        Rectangle b2 = w2.getBounds();

        int width = bounds.width;
        int height = bounds.height;

        int x1 = bounds.x - b1.x;
        int x2 = bounds.x + width - b2.width - b2.x;

        int y1 = bounds.y + (height - b1.height) / 2 - b1.y;
        int y2 = bounds.y + (height - b2.height) / 2 - b2.y;

        w1.resolveBounds(new Point(x1, y1), b1);
        w2.resolveBounds(new Point(x2, y2), b2);
    }
}    

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
import java.util.List;

import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.widget.Widget;

public class PartnerLinkTypeContentLayout implements Layout {

    private int mGap = 40;
    
    
    //Half Width of Rectangle Widget + RectangleWidget thickness + Border thickness 
    //5                              +   2                       +         3
    private int mHorizontalSpacing = 10;
    
    public PartnerLinkTypeContentLayout(int gap) {
        mGap = gap;
    }
    
    public void justify(Widget widget) {
        Rectangle parentBounds = widget.getClientArea();
        int totalWidth = parentBounds.width;
        
        List<Widget> children = widget.getChildren();
        if (children.size() < 3) return;
        
        Widget firstRole = children.get(0);
        Widget secondRole = children.get(1);
        Widget operationLayer = children.get(2);
        
        Rectangle bounds1 = firstRole.getBounds();
        Rectangle bounds2 = secondRole.getBounds();
        Rectangle bounds3 = operationLayer.getBounds();
        
        int width = Math.max(bounds1.width, bounds2.width);
        
        bounds3.width = totalWidth - width + 2 * mHorizontalSpacing;
        Point point = secondRole.getLocation();
        point.x = totalWidth - bounds2.width;
        operationLayer.resolveBounds(operationLayer.getLocation(), bounds3);
        secondRole.resolveBounds(point, bounds2);
        
        
    }

    public void layout(Widget widget) {
        List<Widget> children = widget.getChildren();
        
        if (children.size() < 3) return;
        
        Widget firstRole = children.get(0);
        Widget secondRole = children.get(1);
        Widget operationLayer = children.get(2);
        
        
        Rectangle bounds1 = firstRole.getPreferredBounds();
        Rectangle bounds2 = secondRole.getPreferredBounds();
        Rectangle bounds3 = operationLayer.getPreferredBounds();
        
        
        int width = Math.max(bounds1.width, bounds2.width);
        int height = Math.max(bounds1.height, bounds2.height);
        
        int totalWidth = Math.max(2 * width + 50, bounds3.width + width - 2 * mHorizontalSpacing); //atleast 50 gap between two roles
        
        bounds3.width = Math.max(totalWidth - width + 2 * mHorizontalSpacing, bounds3.width);  
        
        int realHeight = bounds3.height + mGap;
        
        height = Math.max(height, realHeight);
        
        Rectangle roleBounds = new Rectangle(width, realHeight);
        firstRole.resolveBounds(new Point(), roleBounds);
        secondRole.resolveBounds(new Point(totalWidth - width, 0), roleBounds);
        operationLayer.resolveBounds(new Point(width / 2 - mHorizontalSpacing, mGap), bounds3);
    }

    public boolean requiresJustification(Widget widget) {
        return true;
    }

}

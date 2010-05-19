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

package org.netbeans.modules.compapp.casaeditor.graph.layout;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.graph.*;

/**
 * @author Josh Sandusky
 */
public final class LayoutBindings extends CustomizablePersistLayout {
    
    
    public LayoutBindings() {
        // just use the inherent widget vertical spacing
        setYSpacing(0);
    }
    
    
    public void layout (Widget widget) {
        
        if (widget == null) {
            return;
        }
        
        // First determine the relative Y ordering.
        List<CasaNodeWidget> orderedNodeList = new ArrayList<CasaNodeWidget>();
        for (Widget child : widget.getChildren()) {
            if (child instanceof CasaNodeWidget) {
                orderedNodeList.add((CasaNodeWidget) child);
            }
        }
        Collections.sort(orderedNodeList, new YOrderComparator(
                (CasaModelGraphScene) widget.getScene()));
        
        final int parentWidth  = (int) widget.getBounds().getWidth();
        final int regionTitleOffset = ((CasaRegionWidget) widget).getTitleYOffset();
        
        int nextYStart = isAdjustingForOverlapOnly() ? 
            regionTitleOffset :
            regionTitleOffset + getYSpacing();
        
        for (CasaNodeWidget child : orderedNodeList) {
            int x = parentWidth - child.getBounds().width;
            int y = nextYStart;
            
            if (isAdjustingForOverlapOnly()) {
                y = nextYStart > child.getLocation().y ? nextYStart + getYSpacing() : child.getLocation().y;
                nextYStart = y + child.getEntireBounds().height;
            } else {
                nextYStart += child.getEntireBounds().height + getYSpacing();
            }
            
            moveWidget(child, new Point(x, y), true);
        }
        
        widget.getScene().validate();
    }
}

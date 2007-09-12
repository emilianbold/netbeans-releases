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

package org.netbeans.modules.bpel.design;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;

/**
 *
 * @author aa160298
 */
public class DesignViewLayout implements java.awt.LayoutManager {
    
    
    public void addLayoutComponent(String name, Component comp) {}
    public void removeLayoutComponent(Component comp) {}

    
    public Dimension preferredLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            DesignView designView = (DesignView) parent;
            
            int w = 0;
            int h = 0;

            Pattern rp = designView.getModel().getRootPattern();

            double k = designView.getCorrectedZoom();

            if (rp != null) {
                FBounds bounds = rp.getBounds();
                
                w = (int) Math.round(k * bounds.width 
                        + MARGIN_LEFT + MARGIN_RIGHT);
                h = (int) Math.round(k * bounds.height 
                        + MARGIN_TOP + MARGIN_BOTTOM);
            }
            
            int count = designView.getComponentCount();
            for (int i = 0; i < count; i++) {
                Component c = designView.getComponent(i);
                if (c instanceof NavigationTools) continue;
                
                w = Math.max(w, c.getX() + c.getWidth() + MARGIN_RIGHT);
                h = Math.max(h, c.getY() + c.getHeight() + MARGIN_BOTTOM);
            }
            
            return new Dimension(w, h);
        }
    }

    
    public Dimension minimumLayoutSize(Container parent) {
        return preferredLayoutSize(parent);
    }

    
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            if (parent.getComponentCount() == 0) return;
            Component component = parent.getComponent(0);
            if (!(component instanceof NavigationTools)) return;
            component.setBounds(0, 0, parent.getWidth(), parent.getHeight());
        }
    }
    


    public static int MARGIN_TOP = 32;
    public static int MARGIN_LEFT = 32;
    public static int MARGIN_BOTTOM = 32;
    public static int MARGIN_RIGHT = 32;    
}
 
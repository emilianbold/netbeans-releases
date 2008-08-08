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
/*
 * AutoSizingPanel.java
 *
 * Created on May 27, 2006, 12:40 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe;

import java.awt.Component;
import java.awt.Dimension;

/**
 *
 * @author girix
 */
public class AutoSizingPanel extends ABEBaseDropPanel{
    private static final long serialVersionUID = 7526472295622776147L;

    private boolean horizontalScaling = false;
    private boolean verticalScaling = false;
    private boolean fixedHeight = false;
    private boolean fixedWidth = false;
    private int fixedPanelHeight;
    private int fixedPanelWidth;
    
    private int interComponentSpacing = 0;
    
    public AutoSizingPanel(InstanceUIContext context){
        super(context);
    }
    
    public Dimension getPreferredSize() {
        int width = 0;
        int height = 0;
        for(Component child: this.getComponents()){
            Dimension dim = child.getPreferredSize();
            int curW = dim.width;
            int curH = dim.height;
            if(horizontalScaling)
                width += curW + getInterComponentSpacing();
            else
                width = width < curW ? curW : width;

            if(verticalScaling)
                height += curH + getInterComponentSpacing();
            else
                height = height < curH ? curH : height;
        }
        if(isFixedHeight()){
            height = getFixedPanelHeight();
        }
        if(isFixedWidth()){
            width = getFixedPanelWidth();
        }
        return new Dimension(width, height);
    }
    
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
    
    public boolean isHorizontalScaling() {
        return horizontalScaling;
    }
    
    public void setHorizontalScaling(boolean horizontalScaling) {
        this.horizontalScaling = horizontalScaling;
    }
    
    public boolean isVerticalScaling() {
        return verticalScaling;
    }
    
    public void setVerticalScaling(boolean verticalScaling) {
        this.verticalScaling = verticalScaling;
    }

    public boolean isFixedHeight() {
        return fixedHeight;
    }

    public void setFixedHeight(boolean fixedHeight) {
        this.fixedHeight = fixedHeight;
    }

    public boolean isFixedWidth() {
        return fixedWidth;
    }

    public void setFixedWidth(boolean fixedWidth) {
        this.fixedWidth = fixedWidth;
    }

    public int getFixedPanelHeight() {
        return fixedPanelHeight;
    }

    public void setFixedPanelHeight(int fixedPanelHeight) {
        this.fixedPanelHeight = fixedPanelHeight;
    }

    public int getFixedPanelWidth() {
        return fixedPanelWidth;
    }

    public void setFixedPanelWidth(int fixedPanelWidth) {
        this.fixedPanelWidth = fixedPanelWidth;
    }

    public int getInterComponentSpacing() {
        return interComponentSpacing;
    }

    public void setInterComponentSpacing(int interComponentSpacing) {
        this.interComponentSpacing = interComponentSpacing;
    }

    public void accept(UIVisitor visitor) {
        //noop
    }
    
    
    
}

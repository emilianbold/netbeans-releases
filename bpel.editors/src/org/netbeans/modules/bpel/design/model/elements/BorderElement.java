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

package org.netbeans.modules.bpel.design.model.elements;

import java.awt.Color;
import java.awt.Paint;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.geometry.FInsets;
import org.netbeans.modules.bpel.design.geometry.FShape;

/**
 *
 * @author anjeleevich
 */
public abstract class BorderElement extends VisualElement {
    
    
    private FInsets insets;
    
    
    public BorderElement(FShape shape, FInsets insets) {
        super(shape);
        this.insets = insets;
    }
    
    
    public FInsets getInsets() {
        return insets;
    }
    
    
    public void setClientRectangle(double x, double y, double w, double h) {
        setBounds(x - insets.left, y - insets.top,
                w + insets.left + insets.right, 
                h + insets.top + insets.bottom);
    }
    
    
    public void setClientRectangle(FBounds bounds){
        setClientRectangle(bounds.x, bounds.y, bounds.width, bounds.height);  
    }
    
    
    
    public void setSize(double width, double height) {
        shape = shape.resize(width, height);
    }
    
    
    public void setBounds(double x, double y, double w, double h) {
        shape = shape.reshape(x, y, w, h);
    }
    
    
    public static final Paint STROKE_COLOR = new Color(0xD0D0D0);
}
 
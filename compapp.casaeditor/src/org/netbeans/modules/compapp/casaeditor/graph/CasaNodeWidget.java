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

package org.netbeans.modules.compapp.casaeditor.graph;

import java.awt.Rectangle;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.widget.*;


/**
 * This class represents a node widget in the VMD plug-in.
 * It implements the minimize ability. It allows to add pin widgets into the widget
 * using attachPinWidget method.
 *
 * @author David Kaspar
 */
public abstract class CasaNodeWidget extends ErrableWidget {
    
    protected Widget mContainerWidget;
    
    private boolean mEditable = false;
    private boolean mWSPolicyAttached = false;
    
    public CasaNodeWidget(Scene scene) {
        super(scene);   
    }
           
    protected boolean hasPreferredLocation() {
        return true;
    }
    
    protected int getErrorBadgeDeltaX() {
        return getBounds().width + 5;
    }
    
    protected int getErrorBadgeDeltaY() {
        return -8;
    }
        
    public Rectangle getEntireBounds() {
        return new Rectangle(getLocation(), getBounds().getSize());
    }
    
//    /**
//     * Initialization for the glass layer above the widget.
//     * @param layer the glass layer
//     */
//    public abstract void initializeGlassLayer(LayerWidget layer);
    
    /**
     * Attaches a pin widget to the node widget.
     * @param widget the pin widget
     */
    public abstract void attachPinWidget(CasaPinWidget widget);
    
    /**
     * Sets all node properties at once.
     * @param nodeName  node name
     * @param bindingType  node's binding type, e.x., "http", "soap", "???"
     */
    public abstract void setNodeProperties(String nodeName, String bindingType);
    
    /**
     * Returns an anchor for the given pin anchor.
     * Subclasses may return a proxy anchor or the same anchor passed-in.
     * @param anchor the original pin anchor
     * @return the extended pin anchor
     */
    protected abstract Anchor createAnchorPin(Anchor pinAnchor);
    
    
    public Anchor getPinAnchor(Widget pinMainWidget) {
        Anchor anchor = null;
        if (pinMainWidget != null) {
            assert pinMainWidget instanceof CasaPinWidget;
            anchor = ((CasaPinWidget) pinMainWidget).getAnchor();
            anchor = createAnchorPin(anchor);
        }
        return anchor;
    }
    
    public Widget getContainerWidget() {
        return mContainerWidget;
    }
    
    public boolean isEditable() {
        return mEditable;
    }
    
    public void setEditable(boolean bValue) {
        mEditable = bValue;
    }
    
    public boolean isWSPolicyAttached() {
        return mWSPolicyAttached;
    }
    
    public void setWSPolicyAttached(boolean bValue) {
        mWSPolicyAttached = bValue;
    }
    
    public void readjustBounds() {
        if (getBounds() != null) {
            revalidate();
            getScene().validate();
        }
    }
}

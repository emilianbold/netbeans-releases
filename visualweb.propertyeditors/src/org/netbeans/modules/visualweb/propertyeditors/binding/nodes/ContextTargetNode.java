/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.propertyeditors.binding.nodes;

import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeModel;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.faces.FacesDesignContext;
import org.netbeans.modules.visualweb.propertyeditors.binding.BindingTargetNode;

public class ContextTargetNode extends BindingTargetNode {
    public ContextTargetNode(BindingTargetNode parent, DesignContext context) {
        super(parent);
        this.context = context;
        this.displayText = "<html><b>" + context.getDisplayName() + "</b></html>";  //NOI18N
    }
    protected DesignContext context;
    public DesignContext getDesignContext() {
        return context;
    }
    public boolean lazyLoad() {
        DesignBean[] kids = getDesignContext().getRootContainer().getChildBeans();
        for (int i = 0; kids != null && i < kids.length; i++) {
            super.add(_createTargetNode(this, kids[i], null, kids[i].getInstance()));
        }
        return true;
    }
    protected String displayText;
    public String getDisplayText(boolean enableNode) {
        return displayText;
    }
    public boolean hasDisplayIcon() {
        return getChildCount() < 1;
    }
    Icon displayIcon = UIManager.getIcon("Tree.closedIcon"); // NOI18N
    public Icon getDisplayIcon(boolean enableNode) {
        return displayIcon;
    }
    public boolean isValidBindingTarget() {
        return true;
    }
    public String getBindingExpressionPart() {
        if (context instanceof FacesDesignContext) {
            return ((FacesDesignContext)context).getReferenceName();
        }
        return context.getDisplayName();
    }
    public Class getTargetTypeClass() {
        return null;
    }
}

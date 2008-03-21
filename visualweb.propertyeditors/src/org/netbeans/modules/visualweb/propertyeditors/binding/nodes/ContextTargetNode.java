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
package org.netbeans.modules.visualweb.propertyeditors.binding.nodes;

import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeModel;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.faces.FacesDesignContext;
import org.netbeans.modules.visualweb.propertyeditors.binding.BindingTargetNode;
import com.sun.data.provider.DataProvider;
import javax.sql.RowSet;

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
            // Do not show the data provider in the object binding dialog.
            // We have explicit data provider binding dialog
            // Fix: 128647 Don't check in case of interfaces Ex: java.util.List
            if (kids[i].getInstance() == null){
              super.add(_createTargetNode(this, kids[i], null, kids[i].getInstance()));  
            }else if (!DataProvider.class.isAssignableFrom(kids[i].getInstance().getClass())
                    && !RowSet.class.isAssignableFrom(kids[i].getInstance().getClass())) {
                super.add(_createTargetNode(this, kids[i], null, kids[i].getInstance()));
            }
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

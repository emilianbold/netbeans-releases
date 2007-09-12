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

package org.netbeans.modules.xslt.mapper.model.nodes.actions;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.soa.ui.axinodes.NodeType;
import org.netbeans.modules.soa.ui.axinodes.NodeType.BadgeModificator;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xslt.mapper.model.BranchConstructor;
import org.netbeans.modules.xslt.mapper.model.ModelBridge;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;
import org.netbeans.modules.xslt.model.XslComponent;

/**
 *
 * @author nk160297
 */
public class AddNestedAxiAttribute extends XsltNodeAction {
    
    private static final long serialVersionUID = 1L;
    
    protected Attribute myNewAxiAttribute;
    
    public AddNestedAxiAttribute(XsltMapper xsltMapper, TreeNode node,
            Attribute attribute) {
        super(xsltMapper, node);
        myNewAxiAttribute = attribute;
        postInit();
    }
    
    public String getDisplayName() {
        return myNewAxiAttribute.getName();
    }
    
    public Icon getIcon() {
        Icon icon = new ImageIcon(NodeType.ATTRIBUTE.getImage(BadgeModificator.SINGLE));
        return icon;
    }
    
    public void actionPerformed(ActionEvent e) {
        Object dataObject = myTreeNode.getDataObject();
        if (dataObject != null && dataObject instanceof XslComponent) {
            XslComponent parentComp = (XslComponent)dataObject;
            BranchConstructor.createXslElementOrAttribute(parentComp, myNewAxiAttribute, getMapper());
        }
    }
    
}

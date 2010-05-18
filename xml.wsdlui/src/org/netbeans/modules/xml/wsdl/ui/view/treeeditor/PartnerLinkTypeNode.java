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

package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Class PartnerLinkTypeNode represents a partner link type component.
 *
 * @author  Nathan Fiedler
 */
public class PartnerLinkTypeNode extends ExtensibilityElementNode<PartnerLinkType> {
    /** The partner link type component. */
    private PartnerLinkType partnerLinkType;

    /**
     * Creates a new instance of PartnerLinkTypeNode.
     *
     * @param  wsdlComponent  the WSDL component.
     */
    public PartnerLinkTypeNode(PartnerLinkType wsdlComponent) {
        super(wsdlComponent);
        partnerLinkType = wsdlComponent;
    }

    @Override
    protected List<Node.Property> createAlwaysPresentAttributeProperty() throws Exception {
        ArrayList<Node.Property> list = new ArrayList<Node.Property>();
        Role role = partnerLinkType.getRole1();
        if (role != null) {
            String name = role.getName();
            String dname = NbBundle.getMessage(PartnerLinkTypeNode.class,
                    "ROLE1_NAME_DISPLAYNAME");
            String desc = NbBundle.getMessage(PartnerLinkTypeNode.class,
                    "ROLE_NAME_DESC");
            Node.Property prop = new ReadOnlyProperty("role1", String.class,
                    dname, desc, name);
            list.add(prop);
        }
        role = partnerLinkType.getRole2();
        if (role != null) {
            String name = role.getName();
            String dname = NbBundle.getMessage(PartnerLinkTypeNode.class,
                    "ROLE2_NAME_DISPLAYNAME");
            String desc = NbBundle.getMessage(PartnerLinkTypeNode.class,
                    "ROLE_NAME_DESC");
            Node.Property prop = new ReadOnlyProperty("role2", String.class,
                    dname, desc, name);
            list.add(prop);
        }
        return list;
    }
}

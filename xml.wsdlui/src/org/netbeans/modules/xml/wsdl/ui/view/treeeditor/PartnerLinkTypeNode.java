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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
public class PartnerLinkTypeNode extends ExtensibilityElementNode {
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

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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.common;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.j2ee.sun.dd.api.app.SunApplication;
import org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDSectionNodeView;
import org.netbeans.modules.xml.multiview.SectionNode;


/**
 * @author Peter Williams
 */
public class SecurityRoleMappingView extends DDSectionNodeView {

    private final Set<SecurityRoleMapping> securityRoleCache = new HashSet<SecurityRoleMapping>();

    public SecurityRoleMappingView(SunDescriptorDataObject dataObject) {
        super(dataObject);
        
        if(!(rootDD instanceof SunWebApp || rootDD instanceof SunEjbJar || rootDD instanceof SunApplication)) {
            throw new IllegalArgumentException("Data object is not a root that contains security-role-mappings (" + rootDD + ")");
        }
        
        SectionNode [] children = new SectionNode [] { 
            new SecurityRoleMappingGroupNode(this, rootDD, version)
        };
       
        setChildren(children);
    }
    
    protected void checkChildren() {
//        EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
//        Servlet [] servlets = sunWebApp.getServlet();
//        
//        if (enterpriseBeans != this.enterpriseBeans) {
//            SectionNode rootNode = getRootNode();
//            final Children children = rootNode.getChildren();
//            final Node[] nodes = children.getNodes();
//            for (int i = 0; i < nodes.length; i++) {
//                Node node = nodes[i];
//                if (node instanceof EnterpriseBeansNode) {
//                    children.remove(new Node[]{node});
//                }
//            }
//            if (enterpriseBeans != null) {
//                enterpriseBeansNode = new EnterpriseBeansNode(this, enterpriseBeans);
//                if (rootNode != null) {
//                    rootNode.addChild(enterpriseBeansNode);
//                    rootNode.populateBoxPanel();
//                }
//            }
//            this.enterpriseBeans = enterpriseBeans;
//        }
    }
    
}

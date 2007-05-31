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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview;

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.nodes.Children;

/**
 * @author pfiala
 * @author Peter Williams
 */
public class BaseSectionNode extends SectionNode {

    public static final String ICON_BASE_MISC_NODE =
            "org/netbeans/modules/j2ee/sun/ddloaders/resources/MiscNodeIcon"; // NOI18N
    public static final String ICON_BASE_SERVLET_NODE =
            "org/netbeans/modules/j2ee/sun/ddloaders/resources/ServletIcon"; // NOI18N
    public static final String ICON_BASE_SECURITY_ROLE_MAPPING_NODE =
            "org/netbeans/modules/j2ee/sun/ddloaders/resources/SecurityRoleMappingIcon"; // NOI18N
    public static final String ICON_BASE_SERVICE_REF_NODE =
            "org/netbeans/modules/j2ee/sun/ddloaders/resources/ServiceRefIcon"; // NOI18N
    public static final String ICON_BASE_PORT_INFO_NODE =
            "org/netbeans/modules/j2ee/sun/ddloaders/resources/ServiceRefIcon"; // NOI18N
    public static final String ICON_EJB_GROUP_NODE =
            "org/netbeans/modules/j2ee/sun/ddloaders/resources/EjbGroupIcon"; // NOI18N
    public static final String ICON_EJB_SESSION_NODE =
            "org/netbeans/modules/j2ee/sun/ddloaders/resources/SessionBean"; // NOI18N
    public static final String ICON_EJB_ENTITY_NODE =
            "org/netbeans/modules/j2ee/sun/ddloaders/resources/EntityBean"; // NOI18N
    public static final String ICON_EJB_MDB_NODE =
            "org/netbeans/modules/j2ee/sun/ddloaders/resources/MessageBean"; // NOI18N
            
    protected final ASDDVersion version;
    
    
    public BaseSectionNode(SectionNodeView sectionNodeView, boolean isLeaf, Object key, 
            final ASDDVersion version, String title, String iconBase) {
        super(sectionNodeView, isLeaf ? Children.LEAF : new Children.Array(), key, title, iconBase);
        this.version = version;
        
    }

    public BaseSectionNode(SectionNodeView sectionNodeView, Object key, final ASDDVersion version, String title, String iconBase) {
        this(sectionNodeView, false, key, version, title, iconBase);
    }
    
}

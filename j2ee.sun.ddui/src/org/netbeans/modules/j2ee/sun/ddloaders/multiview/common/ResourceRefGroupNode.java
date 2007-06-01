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

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.client.SunApplicationClient;
import org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.util.NbBundle;


/**
 * @author Peter Williams
 */
public class ResourceRefGroupNode extends NamedBeanGroupNode {

    public ResourceRefGroupNode(SectionNodeView sectionNodeView, CommonDDBean commonDD, ASDDVersion version) {
        super(sectionNodeView, commonDD, ResourceRef.RES_REF_NAME, 
                NbBundle.getMessage(ResourceRefGroupNode.class, "LBL_ResourceRefGroupHeader"), // NOI18N
                ICON_BASE_RESOURCE_REF_NODE, version);
        
        enableAddAction(NbBundle.getMessage(ResourceRefGroupNode.class, "LBL_AddResourceRef")); // NOI18N
    }

    protected SectionNode createNode(CommonDDBean bean) {
        return new ResourceRefNode(getSectionNodeView(), (ResourceRef) bean, version);
    }

    protected CommonDDBean [] getBeansFromModel() {
        ResourceRef [] resourceRefs = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            resourceRefs = ((SunWebApp) commonDD).getResourceRef();
        } else if(commonDD instanceof Ejb) {
            resourceRefs = ((Ejb) commonDD).getResourceRef();
        } else if(commonDD instanceof SunApplicationClient) {
            resourceRefs = ((SunApplicationClient) commonDD).getResourceRef();
        }
        return resourceRefs;
    }

    protected CommonDDBean addNewBean() {
        ResourceRef newResourceRef = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            SunWebApp sunWebApp = (SunWebApp) commonDD;
            newResourceRef = sunWebApp.newResourceRef();
            sunWebApp.addResourceRef(newResourceRef);
        } else if(commonDD instanceof Ejb) {
            Ejb ejb = (Ejb) commonDD;
            newResourceRef = ejb.newResourceRef();
            ejb.addResourceRef(newResourceRef);
        } else if(commonDD instanceof SunApplicationClient) {
            SunApplicationClient sunAppClient = (SunApplicationClient) commonDD;
            newResourceRef = sunAppClient.newResourceRef();
            sunAppClient.addResourceRef(newResourceRef);
        }
        
        newResourceRef.setResRefName("resource_ref" + getNewBeanId()); // NOI18N
        
        return newResourceRef;
    }
    
    protected void removeBean(CommonDDBean bean) {
        ResourceRef resourceRef = (ResourceRef) bean;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            SunWebApp sunWebApp = (SunWebApp) commonDD;
            sunWebApp.removeResourceRef(resourceRef);
        } else if(commonDD instanceof Ejb) {
            Ejb ejb = (Ejb) commonDD;
            ejb.removeResourceRef(resourceRef);
        } else if(commonDD instanceof SunApplicationClient) {
            SunApplicationClient sunAppClient = (SunApplicationClient) commonDD;
            sunAppClient.removeResourceRef(resourceRef);
        }
    }
    
}

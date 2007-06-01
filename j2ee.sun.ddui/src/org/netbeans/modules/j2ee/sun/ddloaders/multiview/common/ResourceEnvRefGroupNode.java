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
import org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.util.NbBundle;


/**
 * @author Peter Williams
 */
public class ResourceEnvRefGroupNode extends NamedBeanGroupNode {

    public ResourceEnvRefGroupNode(SectionNodeView sectionNodeView, CommonDDBean commonDD, ASDDVersion version) {
        super(sectionNodeView, commonDD, ResourceEnvRef.RESOURCE_ENV_REF_NAME, 
                NbBundle.getMessage(ResourceEnvRefGroupNode.class, "LBL_ResourceEnvRefGroupHeader"), // NOI18N
                ICON_BASE_RESOURCE_ENV_REF_NODE, version);
        
        enableAddAction(NbBundle.getMessage(ResourceEnvRefGroupNode.class, "LBL_AddResourceEnvRef")); // NOI18N
    }

    protected SectionNode createNode(CommonDDBean bean) {
        return new ResourceEnvRefNode(getSectionNodeView(), (ResourceEnvRef) bean, version);
    }

    protected CommonDDBean [] getBeansFromModel() {
        ResourceEnvRef [] resourceEnvRefs = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            resourceEnvRefs = ((SunWebApp) commonDD).getResourceEnvRef();
        } else if(commonDD instanceof Ejb) {
            resourceEnvRefs = ((Ejb) commonDD).getResourceEnvRef();
        } else if(commonDD instanceof SunApplicationClient) {
            resourceEnvRefs = ((SunApplicationClient) commonDD).getResourceEnvRef();
        }
        return resourceEnvRefs;
    }

    protected CommonDDBean addNewBean() {
        ResourceEnvRef newResourceEnvRef = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            SunWebApp sunWebApp = (SunWebApp) commonDD;
            newResourceEnvRef = sunWebApp.newResourceEnvRef();
            sunWebApp.addResourceEnvRef(newResourceEnvRef);
        } else if(commonDD instanceof Ejb) {
            Ejb ejb = (Ejb) commonDD;
            newResourceEnvRef = ejb.newResourceEnvRef();
            ejb.addResourceEnvRef(newResourceEnvRef);
        } else if(commonDD instanceof SunApplicationClient) {
            SunApplicationClient sunAppClient = (SunApplicationClient) commonDD;
            newResourceEnvRef = sunAppClient.newResourceEnvRef();
            sunAppClient.addResourceEnvRef(newResourceEnvRef);
        }
        
        newResourceEnvRef.setResourceEnvRefName("resource_env_ref" + getNewBeanId()); // NOI18N
        
        return newResourceEnvRef;
    }
    
    protected void removeBean(CommonDDBean bean) {
        ResourceEnvRef resourceEnvRef = (ResourceEnvRef) bean;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            SunWebApp sunWebApp = (SunWebApp) commonDD;
            sunWebApp.removeResourceEnvRef(resourceEnvRef);
        } else if(commonDD instanceof Ejb) {
            Ejb ejb = (Ejb) commonDD;
            ejb.removeResourceEnvRef(resourceEnvRef);
        } else if(commonDD instanceof SunApplicationClient) {
            SunApplicationClient sunAppClient = (SunApplicationClient) commonDD;
            sunAppClient.removeResourceEnvRef(resourceEnvRef);
        }
    }
    
}

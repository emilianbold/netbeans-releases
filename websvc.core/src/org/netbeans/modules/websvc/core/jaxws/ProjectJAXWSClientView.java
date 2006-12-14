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

package org.netbeans.modules.websvc.core.jaxws;

import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.core.ProjectClientViewProvider;
import org.netbeans.modules.websvc.core.jaxws.nodes.JaxWsClientRootNode;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientViewImpl;
import org.openide.nodes.Node;

/**
 *
 * @author mkuchtiak
 */
public class ProjectJAXWSClientView implements JAXWSClientViewImpl, ProjectClientViewProvider {
    
    /** Creates a new instance of ProjectJAXWSView */
    public ProjectJAXWSClientView() {
    }

    public Node createJAXWSClientView(Project project) {
        if (project != null) {
            JaxWsModel model = (JaxWsModel) project.getLookup().lookup(JaxWsModel.class);
            
            if (model != null) {
                return new JaxWsClientRootNode(model,project.getProjectDirectory());
            }
        }
        return null;
    }
    
    public Node createClientView(Project project) {
        JAXWSClientSupport support = JAXWSClientSupport.getJaxWsClientSupport(project.getProjectDirectory());
        if (support!=null && support.getServiceClients().size()>0) {
            return createJAXWSClientView(project);
        }
        return null;
    }
    
}

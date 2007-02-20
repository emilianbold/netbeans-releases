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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.websvc.core.jaxws.nodes.JaxWsRootNode;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.jaxws.spi.JAXWSViewImpl;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author mkuchtiak
 */
public class ProjectJAXWSView implements JAXWSViewImpl {
    
    /** Creates a new instance of ProjectJAXWSView */
    public ProjectJAXWSView() {
    }

    public Node createJAXWSView(Project project) {
        if (project != null) {
            JaxWsModel model = (JaxWsModel) project.getLookup().lookup(JaxWsModel.class);
            
            if (model != null) {
                Sources sources = (Sources)project.getLookup().lookup(Sources.class);
                if (sources!=null) {
                    SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                    if (groups!=null) {
                        List<FileObject> roots = new ArrayList<FileObject>();
                        for (SourceGroup group: groups) {
                            roots.add(group.getRootFolder());
                        }
                        FileObject[] srcRoots = new FileObject[roots.size()];
                        roots.toArray(srcRoots);
                        return new JaxWsRootNode(project, model,srcRoots);
                    }   
                }
            }
        }
        return null;
    }
    
}

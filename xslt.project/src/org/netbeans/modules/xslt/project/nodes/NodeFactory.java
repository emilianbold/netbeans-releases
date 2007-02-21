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
package org.netbeans.modules.xslt.project.nodes;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Factory used to create Transforamtion nodes
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class NodeFactory {
    
    private NodeFactory() {
    }
   
    /**
     * Used to create XsltTransformationsNode
     * @param xsltMapFile FileObject for xsltmap.xml file
     * @return created XsltTransformationsNode which associated with xsltMap dataObject node
     */
    public static final XsltTransformationsNode createXsltTransformationsNode(FileObject xsltMapFile) {
        XsltTransformationsNode node = null;
        if (xsltMapFile != null) {
            Project project = FileOwnerQuery.getOwner(xsltMapFile);
            if (project != null ) {
                Children tChildren = new TransformationsChildren(project);
                
                DataObject dObj;
                try {
                    dObj = DataObject.find(xsltMapFile);
                    if (dObj != null) {
                        node = new XsltTransformationsNode(dObj, tChildren);
                    }
                } catch (DataObjectNotFoundException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        }
        
        return node;
    }
    
}

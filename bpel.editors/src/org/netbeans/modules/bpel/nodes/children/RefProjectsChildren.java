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
package org.netbeans.modules.bpel.nodes.children;

import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.bpel.nodes.ExternalProjectNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Build childerens' list consists of referenced projects. 
 *
 * @author Nikita Krjukov
 */
public class RefProjectsChildren extends Children.Keys {
    
    private Lookup myLookup;
    
    public RefProjectsChildren(List<Project> refProjList, Lookup lookup) {
        myLookup = lookup;
        setKeys(refProjList);
    }

    protected Node[] createNodes(Object key) {
        if(key instanceof Project) {
            Project refProj = (Project)key;
            ProjectChildren children = new ProjectChildren(refProj, myLookup);
            ExternalProjectNode projNode =
                    new ExternalProjectNode(refProj, children, myLookup);
            //
            return new Node[] {projNode};
        }
        return new Node[0];
    }
    
}

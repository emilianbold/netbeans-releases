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
package org.netbeans.modules.bpel.nodes;

import org.netbeans.api.project.Project;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.support.Utils;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 *
 * @author ads
 */
public class SchemaFileNode extends BpelNode<SchemaModel> {
    
    /**
     * @param name
     */
    public SchemaFileNode(SchemaModel model, Lookup lookup) {
        super(model, lookup);
    }
    
    public SchemaFileNode(SchemaModel model, Children children,  Lookup lookup) {
        super(model, children, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.SCHEMA_FILE;
    }
    
    @Override
    protected String getNameImpl() {
        SchemaModel ref = getReference();
        if (ref == null) {
            return null;
        }
        FileObject fo = ref.getModelSource().getLookup().lookup(FileObject.class);
        if (fo == null || !fo.isValid()) {
            Schema schema = ref.getSchema();
            
            return schema == null ? null : schema.getTargetNamespace();
        }
        Project modelProject = Utils.safeGetProject(ref);
        String relativePath = ResolverUtility.safeGetRelativePath(fo, modelProject);
        return relativePath != null ? relativePath : fo.getPath();
    }
    
}

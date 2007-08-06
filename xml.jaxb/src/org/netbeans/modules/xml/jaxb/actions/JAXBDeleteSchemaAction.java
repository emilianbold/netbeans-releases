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

package org.netbeans.modules.xml.jaxb.actions;

import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.jaxb.cfg.schema.Schema;
import org.netbeans.modules.xml.jaxb.ui.JAXBWizardSchemaNode;
import org.netbeans.modules.xml.jaxb.util.ProjectHelper;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * @author $Author$
 * @author lgao
 */
public class JAXBDeleteSchemaAction extends NodeAction {
    
    public JAXBDeleteSchemaAction() {
    }

    public void performAction(Node[] nodes) {
        JAXBWizardSchemaNode schemaNode = 
                nodes[0].getLookup().lookup(JAXBWizardSchemaNode.class);
        if (schemaNode != null){
            Schema schema = schemaNode.getSchema();
            Project prj = schemaNode.getProject();
            ProjectHelper.deleteSchemaFromModel(prj, schema);
            ProjectHelper.cleanupLocalSchemaDir(prj, schema);
        }        
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public String getName() {
        return NbBundle.getMessage(this.getClass(), "LBL_DeleteSchema");//NOI18N
    }
    
    protected boolean asynchronous() {
        return false;
    }
        
    @Override
    protected boolean enable(Node[] node) {
        return true;
    }
}

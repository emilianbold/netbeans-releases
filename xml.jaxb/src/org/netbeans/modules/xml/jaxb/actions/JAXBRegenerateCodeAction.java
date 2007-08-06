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

import org.netbeans.modules.xml.jaxb.util.ProjectHelper;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.jaxb.util.ProjectHelper;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * @author $Author$
 * @author lgao
 */
public class JAXBRegenerateCodeAction extends NodeAction {
    public JAXBRegenerateCodeAction() {
    }
        
    protected void performAction(Node[] nodes) {
        Node nd = nodes[0];        
        Project project = nd.getLookup().lookup(Project.class);
        
        if ( project != null ){
            ProjectHelper.compileXSDs(project);
        }
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }
    
    public String getName() {
        return NbBundle.getMessage(this.getClass(),
                "LBL_CodeRegenerateActionName"); //NOI18N
    }
    
    @Override
    protected boolean enable(Node[] node) {
        return true;
    }
}
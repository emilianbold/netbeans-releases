/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.bpel.nodes;

import java.awt.Image;
import javax.swing.Action;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.editors.api.nodes.images.FolderIcon;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.nodes.actions.ActionType;
import org.netbeans.modules.bpel.nodes.actions.AddSchemaImportAction;
import org.netbeans.modules.bpel.nodes.actions.AddSchemaImportAddAction;
import org.netbeans.modules.bpel.nodes.actions.AddWsdlImportAction;
import org.netbeans.modules.bpel.nodes.actions.AddWsdlImportAddAction;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Vitaly Bychkov
 */
public class ImportContainerNode extends BpelNode<Process> {
    public ImportContainerNode(Process reference, Children children, Lookup lookup) {
        super(reference, children, lookup);
    }

    public ImportContainerNode(Process reference, Lookup lookup) {
        super(reference, lookup);
    }
    
    public Image getIcon(int type) {
        return FolderIcon.getClosedIcon();
    }

    public Image getOpenedIcon(int type) {
        return FolderIcon.getOpenedIcon();
    }
    
    public NodeType getNodeType() {
        return NodeType.IMPORT_CONTAINER;
    }

    public String getDisplayName() {
        return getNodeType().getDisplayName();
    }

    protected String getImplHtmlDisplayName() {
        return getDisplayName();
    }

    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.ADD_WSDL_IMPORT,
            ActionType.ADD_SCHEMA_IMPORT
        };
    }
    
    @Override
    public Action createAction(ActionType actionType) {
        Action action = null;
        switch (actionType) {
            case ADD_WSDL_IMPORT : 
                action = SystemAction.get(AddWsdlImportAddAction.class);
                break;
            case ADD_SCHEMA_IMPORT:
                action = SystemAction.get(AddSchemaImportAddAction.class);
                break;
            default 
                : action = super.createAction(actionType);
        }
        
        return action;
    }
}

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
package org.netbeans.modules.bpel.nodes.actions;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.nodes.BpelProcessNode;
import org.netbeans.modules.bpel.nodes.ImportContainerNode;
import org.netbeans.modules.bpel.properties.Util;
import org.netbeans.modules.bpel.properties.editors.FormBundle;
import org.netbeans.modules.bpel.properties.importchooser.ImportSchemaCreator;
import org.netbeans.modules.xml.schema.ui.basic.UIUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 28 March 2006
 */
public class AddSchemaImportAction extends BpelNodeAction {
    private static final long serialVersionUID = 1L;
    
    protected String getBundleName() {
        return NbBundle.getMessage(AddSchemaImportAction.class,
                "CTL_AddSchemaImportAction"); // NOI18N
    }
    
    public ActionType getType() {
        return ActionType.ADD_SCHEMA_IMPORT;
    }
    
    public void performAction(Node[] nodeArr) {
        if (nodeArr == null || nodeArr.length == 0) {
            return;
        }
        //
        final Process process;
        if (nodeArr[0] instanceof BpelProcessNode) {
            process = ((BpelProcessNode)nodeArr[0]).getReference();
        } else if (nodeArr[0] instanceof ImportContainerNode) {
            process = ((ImportContainerNode)nodeArr[0]).getReference();
        } else {
            process = null;
        }
        //
        if (process == null) {
            return;
        }
        //
        BpelModel model = process.getBpelModel();
        //
        String dialogTitle = NbBundle.getMessage(
                FormBundle.class, "LBL_AddSchemaImport"); // NOI18N
        ImportSchemaCreator creator = new ImportSchemaCreator(process);
        DialogDescriptor descriptor =
                UIUtilities.getCustomizerDialog(creator, dialogTitle, true);
        descriptor.setValid(false);
        Object result = DialogDisplayer.getDefault().notify(descriptor);
    }
    
    protected void performAction(BpelEntity[] bpelEntities) {
    }
    
    protected boolean enable(BpelEntity[] bpelEntities) {
//        System.out.println("bpelEntities: "+(bpelEntities.length)+"; entity: "+bpelEntities[0]);
        return bpelEntities != null && bpelEntities.length > 0 &&
                super.enable(bpelEntities) && bpelEntities[0] instanceof Process;
    }
}

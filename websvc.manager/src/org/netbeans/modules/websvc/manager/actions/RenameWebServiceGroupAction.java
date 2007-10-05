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

package org.netbeans.modules.websvc.manager.actions;

import org.netbeans.modules.websvc.manager.model.WebServiceGroup;
import org.netbeans.modules.websvc.manager.model.WebServiceListModel;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.actions.RenameAction;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * To rename a web service group
 */
public class RenameWebServiceGroupAction extends RenameAction {
    
    // This method is cut/pasted from RenameAction. It is modified to make sure
    // that the new name has to contain character other than space and also trim
    // off the leading and tailing spaces
    protected void performAction(Node[] activatedNodes) {
        
        Node n = activatedNodes[0]; // we supposed that one node is activated
        
        NotifyDescriptor.InputLine dlg = new NotifyDescriptor.InputLine(
                NbBundle.getMessage(RenameAction.class, "CTL_RenameLabel"),
                NbBundle.getMessage(RenameAction.class, "CTL_RenameTitle"));
        dlg.setInputText(n.getName());
        if (NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(dlg))) {
            String newname = null;
            String oldName = n.getName();
            try {
                newname = dlg.getInputText().trim();
                WebServiceListModel wsListModel = WebServiceListModel.getInstance();
                if (! newname.equals("") && newname.trim().length() != 0 ) {
                    n.setName(dlg.getInputText().trim()); // NOI18N
                    WebServiceGroup wsGroup = wsListModel.getWebServiceGroup(oldName);
                    if (wsGroup != null){
                         wsGroup.setName(newname);
                    }
                }
                
            } catch (IllegalArgumentException e) {
                ErrorManager em = ErrorManager.getDefault();
                ErrorManager.Annotation[] ann = em.findAnnotations(e);
                
                // determine if "printStackTrace"  and  "new annotation" of this exception is needed
                boolean needToAnnotate = true;
                if (ann!=null && ann.length>0) {
                    for (int i=0; i<ann.length; i++) {
                        String glm = ann[i].getLocalizedMessage();
                        if (glm!=null && !glm.equals("")) { // NOI18N
                            needToAnnotate = false;
                        }
                    }
                }
                
                // annotate new localized message only if there is no localized message yet
                if (needToAnnotate) {
                    em.annotate(e, NbBundle.getMessage(RenameAction.class, "MSG_BadFormat", n.getName(), newname));
                }
                
                em.notify(e);
            }
        }
    }
}

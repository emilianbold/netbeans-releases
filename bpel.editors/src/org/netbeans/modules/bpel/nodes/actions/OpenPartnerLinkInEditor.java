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
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.properties.ImportRegistrationHelper;
import org.netbeans.modules.bpel.nodes.PartnerLinkNode;
import org.netbeans.modules.bpel.properties.Util;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.openide.ErrorManager;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 21 April 2006
 */
public class OpenPartnerLinkInEditor extends BpelNodeAction {
    private static final long serialVersionUID = 1L;
    
    protected String getBundleName() {
        return NbBundle.getMessage(OpenPartnerLinkInEditor.class, 
                "CTL_OpenPartnerLinkInEditor"); // NOI18N
    }
    
    public void performAction(Node[] nodes) {
        if (!enable(nodes)) {
            return;
        }
        PartnerLink pl = ((PartnerLinkNode)nodes[0]).getReference();
        if (pl == null) {
            return;
        }
        
        WSDLReference<PartnerLinkType> plt = pl.getPartnerLinkType();
        if (plt == null) {
            return;
        }
        
        WSDLModel wsdlModel = plt.get().getModel();
        FileObject fo = Util.getFileObjectByModel(wsdlModel);
        try {
            DataObject d = DataObject.find(fo);
            LineCookie lc = (LineCookie) d.getCookie(LineCookie.class);
            if (lc == null) {
                return;
            }
            final Line l = lc.getLineSet().getOriginal(1);
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    l.show(Line.SHOW_GOTO);
                }
            });
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace();
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    
    public boolean isChangeAction() {
        return false;
    }
    
    
    public boolean enable(BpelEntity[] entities) {
        if (!super.enable(entities)) return false;
        return (entities[0] instanceof PartnerLink);
    }
//    
//    public boolean enable(Node[] nodes) {
//        return nodes != null
//            && nodes.length == 1
//            && nodes[0] instanceof PartnerLinkNode;
//    }
    
    public ActionType getType() {
        return ActionType.OPEN_IN_EDITOR;
    }
    
    protected void performAction(BpelEntity[] bpelEntities) {
    }
}

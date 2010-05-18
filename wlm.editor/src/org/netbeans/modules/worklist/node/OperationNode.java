/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.node;

import java.awt.Image;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;

/**
 *
 * @author radval
 */
public class OperationNode extends WSDLComponentNode {

     /** Icon for the Ip msg button.    */
    private static Image ICON  = ImageUtilities.loadImage
             ("org/netbeans/modules/worklist/editor/view/resources/requestresponse_operation.png"); // NOI18N
    
    
    public OperationNode(Operation operation) {
        super(operation, Children.LEAF);
        
    }

     @Override
    public Image getIcon(int type) {
        return ICON;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return ICON;
    }

     protected void updateDisplayName() {
        Operation operation = (Operation) getWSDLComponent();
        String name = operation.getName();
        if (name == null) {
//            name = NbBundle.getMessage(DefinitionsNode.class,
//                    "LBL_DefinitionsNode_NoTargetNamespace");
            name = "No TargetNamespace";
        }
        setDisplayName(name);
    }
     
     
}

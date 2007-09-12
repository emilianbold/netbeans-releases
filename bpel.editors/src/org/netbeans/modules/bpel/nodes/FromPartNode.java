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

import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.FromPart;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.nodes.actions.ActionType;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class FromPartNode extends BpelNode<FromPart>{
    
    public FromPartNode(FromPart reference, Lookup lookup) {
        super(reference, lookup);
    }

    public FromPartNode(FromPart reference, Children children, Lookup lookup) {
        super(reference, lookup);
    }

    public NodeType getNodeType() {
        return NodeType.FROM_PART;
    }
    
    protected String getImplHtmlDisplayName() {
        FromPart fromPart = getReference();
        if (fromPart == null) {
            return EMPTY_STRING;
        }
        BpelReference<VariableDeclaration> toVar = fromPart.getToVariable();
        String resultString = toVar == null ? null 
                : toVar.getRefString(); 

        return resultString == null ? EMPTY_STRING 
                : NbBundle.getMessage(FromPartNode.class,"LBL_FROM_PART_NODE", // NOI18N
                resultString);
    }
    
//    protected String getImplShortDescription() {
//        FromPart fromPart = getReference();
//        StringBuffer result = new StringBuffer();
//        BpelReference<VariableDeclaration> toVar = fromPart.getToVariable();
//
//        result.append(toVar == null 
//                ? EMPTY_STRING 
//                : NbBundle.getMessage(
//                    BpelNode.class,
//                    "LBL_ATTRIBUTE_HTML_TEMPLATE", // NOI18N
//                    FromPart.TO_VARIABLE, 
//                    toVar.getRefString()
//                    )
//                ); 
//        
//        return NbBundle.getMessage(BpelNode.class,
//            "LBL_LONG_TOOLTIP_HTML_TEMPLATE", // NOI18N
//            getName(),    
//            result.toString()
//            );
//    }

    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.GO_TO_SOURCE,
            ActionType.SEPARATOR,
            ActionType.REMOVE
        };    
    }
}

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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.nodes.CorrelationPropertyNode;
import org.netbeans.modules.bpel.nodes.CorrelationSetNode;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.openide.ErrorManager;
import org.openide.nodes.Node;

/**
 *
 * @author Vitaly Bychkov
 * @version 19 April 2006
 *
 */
public class DeletePropertyAction extends DeleteAction {
    private static final long serialVersionUID = 1L;
    
    public void performAction(Node[] nodes) {
        if (!enable(nodes)) {
            return;
        }
        //
        CorrelationPropertyNode corrPropNode = ((CorrelationPropertyNode)nodes[0]);
        final CorrelationProperty corrProperty = corrPropNode.getReference();
        if (corrProperty == null) {
            return;
        }
        //
        assert corrPropNode.getParentNode() instanceof CorrelationSetNode;
        CorrelationSetNode corrSetNode = (CorrelationSetNode)nodes[0].getParentNode();
        final CorrelationSet corrSet = corrSetNode.getReference();
        //
        BpelModel model = corrSet.getBpelModel();
        try {
            model.invoke(new Callable<Object>() {
                public Object call() {
                    List<WSDLReference<CorrelationProperty>> oldCorrPropRefList
                            = corrSet.getProperties();
                    assert oldCorrPropRefList != null;
                    List<WSDLReference<CorrelationProperty>> newCorrPropRefList =
                            new ArrayList<WSDLReference<CorrelationProperty>>
                            (oldCorrPropRefList);
                    // remove just first equals element
                    for (WSDLReference<CorrelationProperty> elem : oldCorrPropRefList) {
                        if (elem != null && elem.get().equals(corrProperty)) {
                            newCorrPropRefList.remove(elem);
                            break;
                        }
                    }
                    //
                    corrSet.setProperties(newCorrPropRefList);
                    return null;
                }
            }, this);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    public boolean enable(Node[] nodes) {
        return nodes != null
                && nodes.length == 1
                && nodes[0] instanceof CorrelationPropertyNode
                && nodes[0].getParentNode() instanceof CorrelationSetNode;
    }
}

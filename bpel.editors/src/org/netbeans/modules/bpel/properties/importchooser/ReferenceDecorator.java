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

package org.netbeans.modules.bpel.properties.importchooser;

import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.netbeans.modules.bpel.properties.Util;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.ui.customizer.AbstractReferenceDecorator;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceCreator;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceDataNode;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * The ExternalReferenceDecorator for schema or wsdl components.
 * The actual type is specified as a parameter of constructor.
 *
 * @author nk160297
 */
public class ReferenceDecorator extends AbstractReferenceDecorator {
    /**
     * The myRefCreator that created this decorator.
     */
    private ExternalReferenceCreator myRefCreator;
    private Utilities.DocumentTypesEnum myDocType;
    private Process myProcess;
    // Used to generate unique namespace prefixes.
    private int myPrefixCounter;
    
    /**
     * Creates a new instance of ReferenceDecorator.
     *
     *
     *
     * @param myRefCreator  provides information about the edited component.
     */
    public ReferenceDecorator(Process process, ExternalReferenceCreator creator,
            Utilities.DocumentTypesEnum docType) {
        super();
        myProcess = process;
        myRefCreator = creator;
        myDocType = docType;
    }
    
    public String validate(ExternalReferenceNode node) {
        if (node.hasModel()) {
            // get model of the node selected in the tree view
            Model selectedModel = node.getModel();
            if (selectedModel == null || selectedModel.getState() != State.VALID) {
                // If it is supposed to have a selectedModel, it must not be null.
                return NbBundle.getMessage(ReferenceDecorator.class,
                        "LBL_ReferenceDecorator_InvalidModel");
            }
            //
            // Component selectedModel always has to be the BPEL selectedModel
            Model componentModel = myRefCreator.getComponentModel();
            if (componentModel instanceof BpelModel) {
                BpelModel bpelModel = (BpelModel)componentModel;
                if (ResolverUtility.isModelImported(selectedModel, bpelModel)) {
                    return NbBundle.getMessage(ReferenceDecorator.class,
                            "LBL_ReferenceDecorator_AlreadyRefd");
                }
            }
            //
            String ns = node.getNamespace();
            if (ns == null || ns.length() == 0) {
                return NbBundle.getMessage(ReferenceDecorator.class,
                        "LBL_ReferenceDecorator_NoNamespace");
            }
            //
            String namespace = myRefCreator.getTargetNamespace();
            if (myRefCreator.mustNamespaceDiffer()) {
                // This is an import, which must have no namespace, or a
                // different one than the customized component.
                if (!Utilities.NO_NAME_SPACE.equals(ns) &&
                        namespace.equals(ns)) {
                    return NbBundle.getMessage(ReferenceDecorator.class,
                            "LBL_ReferenceDecorator_SameNamespace");
                }
            } else {
                // This is an include or redefine, which must have no namespace,
                // or the same one as the customized component.
                if (!Utilities.NO_NAME_SPACE.equals(ns) &&
                        !namespace.equals(ns)) {
                    return NbBundle.getMessage(ReferenceDecorator.class,
                            "LBL_ReferenceDecorator_DifferentNamespace");
                }
            }
        }
        //
        return null;
    }
    
    public ExternalReferenceDataNode createExternalReferenceNode(Node original) {
        return myRefCreator.createExternalReferenceNode(original);
    }
    
    public Utilities.DocumentTypesEnum getDocumentType() {
        return myDocType;
    }
    
    public String getHtmlDisplayName(String name, ExternalReferenceNode node) {
        if (validate(node) == null) {
            return name;
        } else {
            return "<s>" + name + "</s>";
        }
    }
    
    public String getNamespace(Model model) {
        return Util.getTargetNamespace(model);
    }

    protected String generatePrefix(Model model) {
        BpelModel bpelModel = myProcess.getBpelModel();
        if (model != null) {
            String namespace = getNamespace(model);
            if (namespace != null && namespace.length() > 0) {
                String oldPrefix =  NameGenerator.
                        getNamespacePrefix(namespace, myProcess);
                if (oldPrefix != null && oldPrefix.length() > 0) {
                    return oldPrefix;
                } else {
                    return NameGenerator.getInstance().generateNamespacePrefix(
                                null, bpelModel, myPrefixCounter++);
                }
            }
        }
        return "";
    }

}

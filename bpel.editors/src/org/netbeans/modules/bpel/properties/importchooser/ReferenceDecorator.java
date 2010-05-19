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

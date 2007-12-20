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

package org.netbeans.modules.xml.wsdl.ui.view;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceCreator;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceDataNode;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceDecorator;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * An import creator.
 *
 * @author  Administrator
 * @author  Nathan Fiedler
 */
public class ImportWSDLCreator extends ExternalReferenceCreator<Definitions> {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;
    private ExternalReferenceDecorator decorator;

    /**
     * Creates a new instance of ImportCustomizer
     *
     * @param  defs  component to contain the import(s).
     */
    public ImportWSDLCreator(Definitions defs) {
        super(defs, null);
    }

    @Override
    public void applyChanges() throws IOException {
        super.applyChanges();
        Definitions def = getModelComponent();
        WSDLModel model = def.getModel();
        List<Node> nodes = getSelectedNodes();
        for (Node node : nodes) {
            Import imp = model.getFactory().createImport();
            // Save the location.
            imp.setLocation(getLocation(node));

            // Save the namespace.
            String namespace = getNamespace(node);
            if (mustNamespaceDiffer()) {
                imp.setNamespace(namespace);
            }

            // Save the prefix.
            if (node instanceof ExternalReferenceDataNode) {
                String prefix = ((ExternalReferenceDataNode) node).getPrefix();
                if (prefix.length() > 0) {
                    // Should not have to cast, but Definitions does not
                    // expose the prefixes API.
                    AbstractDocumentComponent adc =
                            (AbstractDocumentComponent) model.getDefinitions();
                    Map prefixes = adc.getPrefixes();
                    if (!prefixes.containsKey(prefix)) {
                        adc.addPrefix(prefix, namespace);
                    }
                }
            }
            def.addImport(imp);

            // Check whether namespace was added. Temporary fix till import
            // dialog mandates the prefix.
            if (Utility.getNamespacePrefix(imp.getNamespace(), model) == null) {
                //create a prefix for this namespace
                String prefix = NameGenerator.getInstance().generateNamespacePrefix(null, model);
                ((AbstractDocumentComponent) def).addPrefix(prefix, imp.getNamespace());
            }
        }
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(ImportWSDLCreator.class);
    }

    protected String getTargetNamespace(Model model) {
        return ((WSDLModel) model).getDefinitions().getTargetNamespace();
    }

    protected Map<String, String> getPrefixes(Model model) {
        WSDLModel wm = (WSDLModel) model;
        AbstractDocumentComponent def =
                (AbstractDocumentComponent) wm.getDefinitions();
        return def.getPrefixes();
    }

    protected ExternalReferenceDecorator getNodeDecorator() {
        if (decorator == null) {
            decorator = new WSDLReferenceDecorator(this);
        }
        return decorator;
    }

    public boolean mustNamespaceDiffer() {
        return true;
    }

    protected String referenceTypeName() {
        return NbBundle.getMessage(ImportWSDLCreator.class,
                "LBL_ImportCreator_Type");
    }
}

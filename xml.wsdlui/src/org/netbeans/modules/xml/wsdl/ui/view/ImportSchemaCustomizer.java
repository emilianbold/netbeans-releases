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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceCreator;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceDataNode;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceDecorator;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * An import customizer for schema in a WSDL document.
 *
 * @author  Administrator
 * @author  Nathan Fiedler
 */
public class ImportSchemaCustomizer extends ExternalReferenceCreator<Schema> {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;
    private ExternalReferenceDecorator decorator;
    /** The real model from which we get the namespace prefixes. */
    private WSDLModel mModel;

    /**
     * Creates a new instance of ImportCustomizer
     *
     * @param  schema  component to contain the import(s).
     */
    public ImportSchemaCustomizer(Schema schema, WSDLModel model) {
        super(schema, model);
    }

    @Override
    protected void init(Schema component, Model model) {
        // Set the model reference now, before the UI initialization occurs.
        mModel = (WSDLModel) model;
    }

    @Override
    public void applyChanges() throws IOException {
        super.applyChanges();
        Schema schema = getModelComponent();
        List<Node> nodes = getSelectedNodes();
        for (Node node : nodes) {
            Import schemaImport = schema.getModel().getFactory().createImport();
            // Save the location.
            schemaImport.setSchemaLocation(getLocation(node));

            String namespace = getNamespace(node);
            if (mustNamespaceDiffer()) {
                // Save the namespace.
                schemaImport.setNamespace(namespace);
            }

            // Save the prefix.
            if (node instanceof ExternalReferenceDataNode) {
                String prefix = ((ExternalReferenceDataNode) node).getPrefix();
                if (prefix.length() > 0) {
                    if (mModel != null) {
                        AbstractDocumentComponent def =
                                (AbstractDocumentComponent) mModel.getDefinitions();
                        Map prefixes = def.getPrefixes();
                        if (!prefixes.containsKey(prefix)) {
                            def.addPrefix(prefix, namespace);
                        }

                    }
                }
            }
            schema.addExternalReference(schemaImport);
        }
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(ImportSchemaCustomizer.class);
    }

    /**
     * Return the WSDL model that contains the embedded schema model.
     *
     * @return  the containing WSDL model.
     */
    public WSDLModel getModel() {
        return mModel;
    }

    protected String getTargetNamespace(Model model) {
        return ((SchemaModel) model).getSchema().getTargetNamespace();
    }

    protected Map<String, String> getPrefixes(Model model) {
        if (mModel != null) {
            return ((AbstractDocumentComponent) mModel.getDefinitions()).getPrefixes();
        }
        return new HashMap();
    }

    protected ExternalReferenceDecorator getNodeDecorator() {
        if (decorator == null) {
            decorator = new SchemaReferenceDecorator(this);
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

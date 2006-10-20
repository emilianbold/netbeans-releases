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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.ui.view;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceCustomizer;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceDecorator;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * An import customizer for schema in a WSDL document.
 *
 * @author  Administrator
 * @author  Nathan Fiedler
 */
public class ImportSchemaCustomizer extends ExternalReferenceCustomizer<Import> {
    private static final long serialVersionUID = 1L;
    private ExternalReferenceDecorator decorator;
    /** The real model from which we get the namespace prefixes. */
    private WSDLModel mModel;

    /**
     * Creates a new instance of ImportCustomizer
     *
     * @param  _import  component to customize.
     */
    public ImportSchemaCustomizer(Import _import, WSDLModel model) {
        super(_import, model);
    }

    @Override
    protected void init(Import component, Model model) {
        // Set the model reference now, before the UI initialization occurs.
        mModel = (WSDLModel) model;
    }

    @Override
    public void applyChanges() throws IOException {
        super.applyChanges();
        Import _import = getModelComponent();
        if (isLocationChanged()) {
            // Save the location.
            _import.setSchemaLocation(getEditedLocation());
        }

        String namespace = getEditedNamespace();
        if (mustNamespaceDiffer() && isNamespaceChanged()) {
            // Save the namespace.
            _import.setNamespace(namespace);
        }

        // Save the prefix.
        String prefix = getEditedPrefix();
        if (prefix.length() > 0) {
            if (mModel != null) {
                AbstractDocumentComponent def = (AbstractDocumentComponent)mModel.getDefinitions();
                Map prefixes = def.getPrefixes();
                if (!prefixes.containsKey(prefix)) {
                    def.addPrefix(prefix, namespace);
                }
                
            }
        }
    }

    @Override
    protected String getReferenceLocation() {
        Import _import = getModelComponent();
        return _import.getSchemaLocation();
    }

    @Override
    protected String getNamespace() {
        Import _import = getModelComponent();
        return _import.getNamespace();
    }

    @Override
    protected String getPrefix() {
        Import _import = getModelComponent();
        String namespace = _import.getNamespace();
        if (mModel != null) {
            Map<String, String> prefixMap = ((AbstractDocumentComponent) mModel.
                    getDefinitions()).getPrefixes();
            for (Map.Entry<String, String> entry : prefixMap.entrySet()) {
                if (entry.getValue().equals(namespace)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(ImportWSDLCustomizer.class);
    }
    
    
    @Override
    protected String getTargetNamespace(Model model) {
        return ((SchemaModel) model).getSchema().getTargetNamespace();
    }

    @Override
    protected Map<String, String> getPrefixes(Model model) {
        if (mModel != null) {
            return ((AbstractDocumentComponent) mModel.getDefinitions()).getPrefixes();
        }
        return new HashMap();
    }

    @Override
    protected ExternalReferenceDecorator getNodeDecorator() {
        if (decorator == null) {
            decorator = new SchemaReferenceDecorator(this);
        }
        return decorator;
    }

    @Override
    protected String generatePrefix() {
        //prefix is added on the definitions, create unique prefix based on the prefixes at the definitions.
        return NameGenerator.getInstance().generateNamespacePrefix(null, mModel);
    }

    @Override
    public boolean mustNamespaceDiffer() {
        return true;
    }
}

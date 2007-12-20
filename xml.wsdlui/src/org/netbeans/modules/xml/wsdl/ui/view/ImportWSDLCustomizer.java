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
import java.util.Map;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceDecorator;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceCustomizer;
import org.openide.util.HelpCtx;

/**
 * An import customizer.
 *
 * @author  Administrator
 * @author  Nathan Fiedler
 */
public class ImportWSDLCustomizer extends ExternalReferenceCustomizer<Import> {
    private static final long serialVersionUID = 1L;
    private ExternalReferenceDecorator decorator;

    /**
     * Creates a new instance of ImportCustomizer
     *
     * @param  _import  component to customize.
     */
    public ImportWSDLCustomizer(Import _import) {
        super(_import, null);
    }

    @Override
    public void applyChanges() throws IOException {
        super.applyChanges();
        Import _import = getModelComponent();
        if (isLocationChanged()) {
            // Save the file location.
            _import.setLocation(getEditedLocation());
        }

        String namespace = getEditedNamespace();
        if (mustNamespaceDiffer() && isNamespaceChanged()) {
            // Save the namespace.
            _import.setNamespace(namespace);
        }

        WSDLModel model = getModelComponent().getModel();
        if (mustNamespaceDiffer() && isPrefixChanged()) {
            // Save the prefix.
            String prefix = getEditedPrefix();
            if (prefix.length() > 0) {
                // Should not have to cast, but Definitions does not
                // expose the prefixes API.
                AbstractDocumentComponent def =
                        (AbstractDocumentComponent) model.getDefinitions();
                def.addPrefix(prefix, namespace);
            }
        }
    }

    protected String getReferenceLocation() {
        Import _import = getModelComponent();
        return _import.getLocation();
    }

    protected String getNamespace() {
        Import _import = getModelComponent();
        return _import.getNamespace();
    }

    protected String getPrefix() {
        Import _import = getModelComponent();
        String namespace = _import.getNamespace();
        WSDLModel model = getModelComponent().getModel();
        AbstractDocumentComponent def =
                (AbstractDocumentComponent) model.getDefinitions();
        Map<String, String> prefixMap = def.getPrefixes();
        for (Map.Entry<String, String> entry : prefixMap.entrySet()) {
            if (entry.getValue().equals(namespace)) {
                return entry.getKey();
            }
        }
// The alternative to casting to AbstractDocumentComponent is to attempt
// to collect the prefixes from the imports.
//        Collection<Import> imports = model.getDefinitions().getImports();
//        for (Import entry : imports) {
//            String ns = entry.getNamespace();
//            if (ns != null && ns.equals(namespace)) {
//                return entry.getNamespace();
//            }
//        }
        return null;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(ImportWSDLCustomizer.class);
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

    protected String generatePrefix() {
        WSDLModel model = getModelComponent().getModel();
        return NameGenerator.getInstance().generateNamespacePrefix(null, model);
    }

    public boolean mustNamespaceDiffer() {
        return true;
    }
}

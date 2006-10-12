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

package org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer;

import java.io.IOException;
import java.util.Map;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceDecorator;
import org.openide.util.HelpCtx;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceCustomizer;

/**
 * An import customizer.
 *
 * @author  Administrator
 * @author  Nathan Fiedler
 */
public class ImportCustomizer extends ExternalReferenceCustomizer<Import> {
    private static final long serialVersionUID = 1L;
    private ExternalReferenceDecorator decorator;

    /**
     * Creates a new instance of ImportCustomizer
     *
     * @param  _import  component to customize.
     */
    public ImportCustomizer(Import _import) {
        super(_import, null);
    }

    @Override
    public void applyChanges() throws IOException {
        super.applyChanges();
        Import _import = (Import) getModelComponent();
        if (isLocationChanged()) {
            // Save the location.
            _import.setSchemaLocation(getEditedLocation());
        }

        String namespace = getEditedNamespace();
        if (mustNamespaceDiffer() && isNamespaceChanged()) {
            // Save the namespace.
            _import.setNamespace(namespace);
        }

        if (mustNamespaceDiffer() && isPrefixChanged()) {
            // Save the prefix.
            SchemaModel model = (SchemaModel) getModelComponent().getModel();
            String prefix = getEditedPrefix();
            if (prefix.length() > 0) {
                model.getSchema().addPrefix(prefix, namespace);
            }
        }
    }

    protected String getReferenceLocation() {
        Import _import = (Import) getModelComponent();
        return _import.getSchemaLocation();
    }

    protected String getNamespace() {
        Import _import = (Import) getModelComponent();
        return _import.getNamespace();
    }

    protected String getPrefix() {
        Import _import = (Import) getModelComponent();
        String namespace = _import.getNamespace();
        SchemaModel model = (SchemaModel) getModelComponent().getModel();
        Map<String, String> prefixMap = model.getSchema().getPrefixes();
        for (Map.Entry<String, String> entry : prefixMap.entrySet()) {
            if (entry.getValue().equals(namespace)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(ImportCustomizer.class);
    }

    protected String getTargetNamespace(Model model) {
        if (model instanceof SchemaModel) {
            SchemaModel sm = (SchemaModel) model;
            Schema schema = sm.getSchema();
            if (schema != null) {
                return schema.getTargetNamespace();
            }
        }
        return null;
    }

    protected Map<String, String> getPrefixes(Model model) {
        if (model instanceof SchemaModel) {
            SchemaModel sm = (SchemaModel) model;
            Schema schema = sm.getSchema();
            if (schema != null) {
                return schema.getPrefixes();
            }
        }
        return null;
    }

    protected ExternalReferenceDecorator getNodeDecorator() {
        if (decorator == null) {
            decorator = new SchemaReferenceDecorator(this);
        }
        return decorator;
    }

    protected String generatePrefix() {
        return "";
    }

    public boolean mustNamespaceDiffer() {
        return true;
    }
}

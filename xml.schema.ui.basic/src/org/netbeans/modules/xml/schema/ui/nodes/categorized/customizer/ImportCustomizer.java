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

package org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer;

import java.io.IOException;
import java.util.Map;
import org.netbeans.modules.xml.schema.ui.basic.NameGenerator;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceDecorator;
import org.openide.util.HelpCtx;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.ui.ModelCookie;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceCustomizer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

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

// XXX: Ignore whether the prefix changed or not, just use the
//      value, if it is non-null, and save it.
//      Change this back once there is an ImportCreator class that
//      extends ExternalReferenceCreator.
        if (mustNamespaceDiffer()/* && isPrefixChanged()*/) {
            // Save the prefix.
            SchemaModel model = getModelComponent().getModel();
            String prefix = getEditedPrefix();
            if (prefix.length() > 0) {
                // This overwrites any existing value for the same prefix.
                model.getSchema().addPrefix(prefix, namespace);
            }
        }
    }

    protected String getReferenceLocation() {
        Import _import = getModelComponent();
        return _import.getSchemaLocation();
    }

    protected String getNamespace() {
        Import _import = getModelComponent();
        return _import.getNamespace();
    }

    /**
     * Search the prefixes defined in the given model for one that matches
     * the given namespace value.
     *
     * @param  model      the schema model to search.
     * @param  namespace  the namespace to look for.
     * @return  matching prefix, or null if none found.
     */
    private static String findPrefix(SchemaModel model, String namespace) {
        Map<String, String> prefixMap = model.getSchema().getPrefixes();
        for (Map.Entry<String, String> entry : prefixMap.entrySet()) {
            if (entry.getValue().equals(namespace)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Search the prefixes defined in the given model for one that matches
     * the given namespace value.
     *
     * @param  model      the WSDL model to search.
     * @param  namespace  the namespace to look for.
     * @return  matching prefix, or null if none found.
     */
    @SuppressWarnings("unchecked")
    private static String findPrefix(WSDLModel model, String namespace) {
        AbstractDocumentComponent def =
                (AbstractDocumentComponent) model.getDefinitions();
        Map<Object, Object> prefixMap = def.getPrefixes();
        for (Map.Entry<Object, Object> entry : prefixMap.entrySet()) {
            if (entry.getValue().equals(namespace)) {
                return entry.getKey().toString();
            }
        }
        return null;
    }

    protected String getPrefix() {
        Import _import = getModelComponent();
        String namespace = _import.getNamespace();
        SchemaModel model = getModelComponent().getModel();
        String prefix = findPrefix(model, namespace);
        if (prefix != null) {
            return prefix;
        }
        // We may be embedded in another model (e.g. WSDL model), so
        // attempt to get the model that contains this one (the embedded
        // model delegates everything to the parent, so calling
        // getModelSource() will return the parent model source).
        FileObject fobj = (FileObject) model.getModelSource().getLookup().
                lookup(FileObject.class);
        if (fobj != null) {
            try {
                DataObject dobj = DataObject.find(fobj);
                ModelCookie modelCookie = (ModelCookie) dobj.getCookie(
                        ModelCookie.class);
                if (modelCookie != null) {
                    Model model2 = modelCookie.getModel();
                    if (model2 != null && !model.equals(model2)) {
                        if (model2 instanceof SchemaModel) {
                            return findPrefix((SchemaModel) model2, namespace);
                        } else if (model2 instanceof WSDLModel) {
                            return findPrefix((WSDLModel) model2, namespace);
                        }
                    }
                }
            } catch (IOException ioe) {
                // ignore and fall through
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
        SchemaModel model = getModelComponent().getModel();
        return NameGenerator.getInstance().generateNamespacePrefix(null, model);
    }

    public boolean mustNamespaceDiffer() {
        return true;
    }
}

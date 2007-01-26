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
import java.util.List;
import java.util.Map;
import org.netbeans.modules.xml.schema.model.Include;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceCreator;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceDecorator;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;

/**
 * An include creator.
 *
 * @author  Administrator
 * @author  Nathan Fiedler
 */
public class IncludeCreator extends ExternalReferenceCreator<Schema> {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The decorator for this include. */
    private ExternalReferenceDecorator decorator;

    /**
     * Creates a new instance of IncludeCreator
     *
     * @param  schema  component to contain the include(s).
     */
    public IncludeCreator(Schema schema) {
        super(schema, null);
    }

    @Override
    public void applyChanges() throws IOException {
        super.applyChanges();
        Schema schema = getModelComponent();
        SchemaModel model = schema.getModel();
        List<Node> nodes = getSelectedNodes();
        for (Node node : nodes) {
            Include imp = model.getFactory().createInclude();
            // Save the location.
            imp.setSchemaLocation(getLocation(node));
            schema.addExternalReference(imp);
        }
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(IncludeCreator.class);
    }

    protected String getTargetNamespace(Model model) {
        return ((SchemaModel) model).getSchema().getTargetNamespace();
    }

    @SuppressWarnings("unchecked")
    protected Map<String, String> getPrefixes(Model model) {
        SchemaModel sm = (SchemaModel) model;
        AbstractDocumentComponent schema =
                (AbstractDocumentComponent) sm.getSchema();
        return schema.getPrefixes();
    }

    protected ExternalReferenceDecorator getNodeDecorator() {
        if (decorator == null) {
            decorator = new SchemaReferenceDecorator(this);
        }
        return decorator;
    }

    public boolean mustNamespaceDiffer() {
        return false;
    }
}

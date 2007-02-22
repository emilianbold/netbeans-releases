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
import org.netbeans.modules.xml.schema.model.Redefine;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceCustomizer;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceDecorator;
import org.openide.util.HelpCtx;

/**
 * A redefine customizer.
 *
 * @author  Administrator
 * @author  Nathan Fiedler
 */
public class RedefineCustomizer extends ExternalReferenceCustomizer<Redefine> {
    private static final long serialVersionUID = 1L;
    private ExternalReferenceDecorator decorator;

    /**
     * Creates a new instance of RedefineCustomizer.
     *
     * @param  redefine  the component to customize.
     */
    public RedefineCustomizer(Redefine redefine) {
        super(redefine, null);
    }

    @Override
    public void applyChanges() throws IOException {
        super.applyChanges();
        if (isLocationChanged()) {
            Redefine redefine = getModelComponent();
            redefine.setSchemaLocation(getEditedLocation());
        }
    }

    public boolean mustNamespaceDiffer() {
        return false;
    }

    protected String getReferenceLocation() {
        Redefine redefine = getModelComponent();
        return redefine.getSchemaLocation();
    }

    protected String getNamespace() {
        return null;
    }

    protected String getPrefix() {
        return null;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(RedefineCustomizer.class);
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
}

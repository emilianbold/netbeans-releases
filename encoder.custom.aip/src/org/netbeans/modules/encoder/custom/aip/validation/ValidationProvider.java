/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.encoder.custom.aip.validation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.spi.XsdBasedValidator;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.xml.sax.SAXException;

/**
 * A validator for validating custom encoder metadata.
 *
 * @author Jun Xu
 */
public class ValidationProvider extends XsdBasedValidator {
    
    private static final ResourceBundle _bundle =
            ResourceBundle.getBundle("org/netbeans/modules/encoder/custom/aip/validation/Bundle");
    private static Schema _schema;
    
    static {
        try {
            _schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema();
        } catch(SAXException ex) {
            assert false: "Error while creating compiled schema for"; //NOI18N
        }
    }
    
    protected Schema getSchema(Model model) {
        if (! (model instanceof SchemaModel)) {
            return null;
        }
        
        // This will not be used as validate(.....) method is being overridden here.
        // So just return a schema returned by newSchema().
        return _schema;
    }

    public String getName() {
        return _bundle.getString("validator_provider.lbl.provider_name");
    }

    @Override
    protected void validate(Model model, Schema schema, XsdBasedValidator.Handler handler) {
        try {
            new CustomEncoderValidator((SchemaModel) model, handler).validate();
        } catch(Exception ex) {
            handler.logValidationErrors(Validator.ResultType.ERROR, ex.getMessage());
        }
    }

    @Override
    public DocumentModel resolveResource(String systemId, Model model) {
        try {
            CatalogModel cm = (CatalogModel) model.getModelSource().getLookup()
                .lookup(CatalogModel.class);
            ModelSource ms = cm.getModelSource(new URI(systemId));
            if (ms != null) {
                return SchemaModelFactory.getDefault().getModel(ms);
            }
        } catch(URISyntaxException ex) {
            Logger.getLogger(getClass().getName()).log(Level.FINE, "resolveResource", ex); //NOI18N
        } catch(CatalogModelException ex) {
            Logger.getLogger(getClass().getName()).log(Level.FINE, "resolveResource", ex); //NOI18N
        }
        return null;
    }
}

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
package org.netbeans.modules.xslt.tmap.util;

import java.net.URI;
import java.net.URISyntaxException;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.locator.CatalogModelFactory;
import org.netbeans.modules.xslt.tmap.model.api.Import;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;

/**
 *
 * @author Vitaly Bychkov
 */
public class ImportHelper {

    private ImportHelper() {
    }

    /**
     * Returns wsdl model respectively given import <code>imp</code>.
     * @param model TMap OM
     * @param location location uri
     * @return imported WSDL Model
     *
     */
    public static WSDLModel getWsdlModel( TMapModel model, String location) {
        return getWsdlModel(model, location, true );
    }

    /**
     * Returns wsdl model respectively given import <code>imp</code>.
     * @param model TMap OM
     * @param location location uri
     * @param checkWellFormed if true method will return null if model is not valid
     * @return imported WSDL model
     */
    public static WSDLModel getWsdlModel( TMapModel model, String location, 
            boolean checkWellFormed )
    {
        WSDLModel wsdlModel;

        if (location == null) {
            return null;
        }
        try {
            URI uri = new URI(location);
            ModelSource source = CatalogModelFactory.getDefault().getCatalogModel( model.getModelSource()).getModelSource(uri, model.getModelSource());
            wsdlModel = WSDLModelFactory.getDefault().getModel(source);
        } catch (URISyntaxException e) {
            wsdlModel = null;
        } catch (CatalogModelException e) {
            wsdlModel = null;
        }

        if (wsdlModel != null && wsdlModel.getState() == Model.State.NOT_WELL_FORMED 
                && checkWellFormed)
        {
            return null;
        }
        return wsdlModel;
    }

    /**
     * Returns wsdl model respectively given import <code>imp</code>.
     * @param imp import statement in Tmap OM
     * @return Imported WSDL model
     */
    public static WSDLModel getWsdlModel( Import imp ) {
        return getWsdlModel( imp , true );
    }

    /**
     * Returns wsdl model respectively given import <code>imp</code>.
     * @param imp import statement in TMap OM
     * @param checkWellFormed if true method will return null if model is not valid
     * @return Imported WSDL model
     */
    public static WSDLModel getWsdlModel( Import imp , boolean checkWellFormed ) {
        return getWsdlModel( imp.getModel() , imp.getLocation(), checkWellFormed );
    }

}

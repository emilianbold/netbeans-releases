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
package org.netbeans.modules.xslt.tmap.model.impl;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xslt.tmap.model.api.Import;
import org.netbeans.modules.xslt.tmap.model.api.TMapAttributes;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapVisitor;
import org.netbeans.modules.xslt.tmap.model.api.events.VetoException;
import org.w3c.dom.Element;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class ImportImpl extends TMapComponentContainerImpl 
        implements Import 
{
    
    public ImportImpl(TMapModelImpl model) {
        this(model, createNewElement(TMapComponents.IMPORT, model));
    }
    
    public ImportImpl(TMapModelImpl model, Element element) {
        super(model, element);
    }

    public void accept(TMapVisitor visitor) {
        visitor.visit(this);
    }

    public Class<? extends TMapComponent> getComponentType() {
        return Import.class;
    }

    public String getNamespace() {
        return getAttribute(TMapAttributes.NAMESPACE);
    }

    public void setNamespace(String uri) throws VetoException {
        setAttribute(Import.NAMESPACE, TMapAttributes.NAMESPACE, uri);
    }

    public void removeNamespace() {
        setAttributeQuietly(TMapAttributes.NAMESPACE, null);
    }
    
    public String getLocation() {
        return getAttribute(TMapAttributes.LOCATION);
    }

    public void setLocation(String value) throws VetoException {
        setAttribute(Import.LOCATION, TMapAttributes.LOCATION, value);
    }

    public void removeLocation() {
        setAttributeQuietly(TMapAttributes.LOCATION, null);
    }
    
    public WSDLModel getImportModel() throws CatalogModelException {
        ModelSource ms = resolveModel( getLocation() );
        return WSDLModelFactory.getDefault().getModel( ms );
    }
}

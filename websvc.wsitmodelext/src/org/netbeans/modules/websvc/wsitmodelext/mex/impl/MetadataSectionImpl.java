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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitmodelext.mex.impl;

import java.util.Collections;
import org.netbeans.modules.websvc.wsitmodelext.mex.Dialect;
import org.netbeans.modules.websvc.wsitmodelext.mex.Identifier;
import org.netbeans.modules.websvc.wsitmodelext.mex.Location;
import org.netbeans.modules.websvc.wsitmodelext.mex.MetadataReference;
import org.netbeans.modules.websvc.wsitmodelext.mex.MetadataSection;
import org.netbeans.modules.websvc.wsitmodelext.mex.MexQName;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Grebac
 */
public class MetadataSectionImpl extends MexComponentImpl implements MetadataSection {
    
    /**
     * Creates a new instance of MetadataSectionImpl
     */
    public MetadataSectionImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public MetadataSectionImpl(WSDLModel model){
        this(model, createPrefixedElement(MexQName.METADATA.getQName(), model));
    }

    @Override
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }

    public Location getLocation() {      
        return getChild(Location.class);
    }

    public void setLocation(Location loc) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(Location.class, LOCATION_PROPERTY, loc, classes);
    }

    public void removeLocation(Location loc) {
        removeChild(LOCATION_PROPERTY, loc);
    }
        
    public Dialect getDialect() {
        return getChild(Dialect.class);
    }

    public void setDialect(Dialect dialect) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(Dialect.class, DIALECT_PROPERTY, dialect, classes);
    }

    public void removeDialect(Dialect dialect) {
        removeChild(DIALECT_PROPERTY, dialect);
    }

    public Identifier getIdentifier() {
        return getChild(Identifier.class);
    }

    public void setIdentifier(Identifier id) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(Identifier.class, IDENTIFIER_PROPERTY, id, classes);
    }

    public void removeIdentifier(Identifier id) {
        removeChild(IDENTIFIER_PROPERTY, id);
    }

    public MetadataReference getMetadataReference() {
        return getChild(MetadataReference.class);
    }

    public void setMetadataReference(MetadataReference mReference) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(MetadataReference.class, METADATAREFERENCE_PROPERTY, mReference, classes);
    }

    public void removeMetadataReference(MetadataReference mReference) {
        removeChild(METADATAREFERENCE_PROPERTY, mReference);
    }

}

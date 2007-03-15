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

package org.netbeans.modules.websvc.wsitmodelext.addressing.impl;

import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing10Metadata;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing10QName;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Grebac
 */
public class Addressing10MetadataImpl extends Addressing10ComponentImpl implements Addressing10Metadata {
    
    /**
     * Creates a new instance of Addressing10MetadataImpl
     */
    public Addressing10MetadataImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public Addressing10MetadataImpl(WSDLModel model){
        this(model, createPrefixedElement(Addressing10QName.ADDRESSINGMETADATA.getQName(), model));
    }

    public void setAddressing10Metadata(String mdata) {
        setText(ADDRESSING10_METADATA_CONTENT_VALUE_PROPERTY, mdata);
    }

    public String getAddressing10Metadata() {
        return getText();
    }

    @Override
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }
    
}

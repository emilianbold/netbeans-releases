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
package org.netbeans.modules.xml.wsdl.model.extensions.soap.impl;

import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeaderBase;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.AbstractReference;
import org.netbeans.modules.xml.xam.Reference;

/**
 *
 * @author Nam Nguyen
 */
public class PartReference extends AbstractReference<Part> implements Reference<Part> {
    
    public PartReference(Part referenced, AbstractDocumentComponent parent) {
        super(referenced, Part.class, parent);
    }
    
    //used by resolve methods
    public PartReference(AbstractDocumentComponent parent, String ref){
        super(Part.class, parent, ref);
    }
    
    public String getRefString() {
        if (refString == null) {
            refString = getReferenced().getName();
        }
        return refString;
    }
    
    public SOAPBodyImpl getBodyParent() {
        if (getParent() instanceof SOAPBodyImpl) {
            return (SOAPBodyImpl) getParent();
        } else {
            return null;
        }
    }
    
    public SOAPHeaderBaseImpl getHeaderParent() {
        if (getParent() instanceof SOAPHeaderBaseImpl) {
            return (SOAPHeaderBaseImpl) getParent();
        } else {
            return null;
        }
    }
    
    public Part get() {
        if (getReferenced() == null) {
            Message m = null;
            if (getBodyParent() != null) {
                SOAPBody p = getBodyParent();
                if (p.getParent() instanceof BindingInput) {
                    BindingInput bi = (BindingInput)p.getParent();
                    if (bi.getInput() != null) {
                        Input in = bi.getInput().get();
                        if (in != null) {
                            m = in.getMessage().get();
                        }
                    }
                } else if (p.getParent() instanceof BindingOutput) {
                    BindingOutput bo = (BindingOutput)p.getParent();
                    if (bo.getOutput() != null) {
                        Output out = bo.getOutput().get();
                        if (out != null) {
                            m = out.getMessage().get();
                        }
                    }
                }
                
            } else if (getHeaderParent() != null) {
                SOAPHeaderBase header = getHeaderParent();
                if (header.getMessage() != null) {
                    m = header.getMessage().get();
                }
            }
            if (m != null) {
                for (Part part : m.getParts()) {
                    if (part.getName().equals(getRefString())) {
                        setReferenced(part);
                        break;
                    }
                }
            }
        }
        return getReferenced();
    }
}

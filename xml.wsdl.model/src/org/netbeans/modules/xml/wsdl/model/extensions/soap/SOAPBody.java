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

package org.netbeans.modules.xml.wsdl.model.extensions.soap;

import java.util.List;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.Reference;

/**
 *
 * @author rico
 * Represents the body element under the wsdl:input or wsdl:output element for SOAP binding
 */
public interface SOAPBody extends SOAPMessageBase {
    public static final String PARTS_PROPERTY = "parts";
    
    /**
     * @return list of parts correspond to the RPC call parameter list.
     */
    List<Reference<Part>> getPartRefs();
    List<String> getParts();

    /**
     * Set list of references to message parts.
     */
    void setParts(List<String> parts);
    void setPartRefs(List<Reference<Part>> parts);
    
    
    /**
     * Append message part to part list.
     */
    void addPart(String part);
    void addPartRef(Reference<Part> ref);
    
    /**
     * Add message part to part list at specified index.
     */
    void addPart(int index, String part);
    void addPartRef(int index, Reference<Part> ref);
    
    /**
     * Remove given part.
     */
    void removePart(String part);
    void removePartRef(Reference<Part> partRef);
}

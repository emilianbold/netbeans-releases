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

import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author Nam Nguyen
 */
public interface SOAPHeaderBase extends SOAPMessageBase {
    
    public static final String MESSAGE_PROPERTY = "message";
    public static final String PART_PROPERTY = "part";
    
    void setMessage(NamedComponentReference<Message> message);
    NamedComponentReference<Message> getMessage();
    
    void setPart(String part);
    void setPartRef(Reference<Part> part);
    String getPart();
    Reference<Part> getPartRef();
}

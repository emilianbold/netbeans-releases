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
package org.netbeans.modules.compapp.casaeditor.model.jbi.impl;

import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIModel;
import org.netbeans.modules.compapp.casaeditor.model.visitor.JBIVisitor;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Consumer;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;

/**
 *
 * @author jqian
 */
public class ConsumerImpl extends ConnectionEndImpl implements Consumer {
    
    /** Creates a new instance of ConsumerImpl */
    public ConsumerImpl(JBIModel model, Element element) {
        super(model, element);
    }
    
    public ConsumerImpl(JBIModel model) {
        this(model, createElementNS(model, JBIQNames.CONSUMER));
    }

    public void accept(JBIVisitor visitor) {
        visitor.visit(this);
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Consumer: service-name=\"");
        sb.append(getServiceName());
        sb.append("\" endpoint-name=\"");
        sb.append(getEndpointName());
        sb.append("\"]");
        return sb.toString();
    }
}

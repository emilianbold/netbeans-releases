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
package org.netbeans.modules.xml.wsdl.model.extensions.bpel.validation;

import java.util.HashSet;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELExtensibilityComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Documentation;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Query;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;


import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;


/**
 * @author ads
 *
 */
public abstract class ValidationVisitor implements BPELExtensibilityComponent.Visitor {

    /**
     * @return Set with result items. 
     */
    public HashSet<ResultItem> getResultItems() {
        return myResultItems;
    }
    
    protected void init(){
        myResultItems = new HashSet<ResultItem>();
    }
    
    public void visit(CorrelationProperty c) {
        
    }
    
    public void visit(PartnerLinkType c) {
        
    }
    
    public void visit(PropertyAlias c) {
        
    }
    
    public void visit(Role c) {
        
    }
    
    public void visit( Query c ) {
        
    }

    public void visit(Documentation c) {
        
    }
    
    private HashSet<ResultItem> myResultItems;

}

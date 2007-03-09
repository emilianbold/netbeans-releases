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

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIComponent;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIModel;
import org.netbeans.modules.compapp.casaeditor.model.visitor.JBIVisitor;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Identification;
import org.netbeans.modules.compapp.casaeditor.model.jbi.ServiceUnit;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Target;
import org.openide.util.NbBundle;
import org.w3c.dom.Element;

/**
 *
 * @author jqian
 */
public class ServiceUnitImpl extends JBIComponentImpl implements ServiceUnit {
    
    /** Creates a new instance of ServiceUnitImpl */
    public ServiceUnitImpl(JBIModel model, Element element) {
        super(model, element);
    }
    
    public ServiceUnitImpl(JBIModel model) {
        this(model, createElementNS(model, JBIQNames.SERVICE_UNIT));
    }
    
    public void accept(JBIVisitor visitor) {
        visitor.visit(this);
    }
    
    public Identification getIdentification() {
        return getChild(Identification.class);
    }
    
    public void setIdentification(Identification identification) {
        List<Class<? extends JBIComponent>> empty = Collections.emptyList();
        setChild(Identification.class, IDENTIFICATION_PROPERTY, identification, empty);
    }
    
    public Target getTarget() {
        return getChild(Target.class);
    }
    
    public void setTarget(Target target) {
        List<Class<? extends JBIComponent>> empty = Collections.emptyList();
        setChild(Target.class, TARGET_PROPERTY, target, empty);
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        //sb.append("ServiceUnit: [");
        sb.append(NbBundle.getMessage(getClass(), "ServiceUnit"));  // NOI18N
        sb.append(Constants.COLON_STRING);
        sb.append(Constants.SPACE);
        sb.append(Constants.SQUARE_BRACKET_OPEN);
        
        sb.append(getIdentification());

        sb.append(Constants.COMMA);
        sb.append(getTarget());
        sb.append(Constants.SQUARE_BRACKET_CLOSE);
        
        return sb.toString();
    }
    
}

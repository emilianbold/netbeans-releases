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
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIModel;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Consumes;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIComponent;
import org.netbeans.modules.compapp.casaeditor.model.visitor.JBIVisitor;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Provides;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Services;
import org.w3c.dom.Element;

/**
 *
 * @author jqian
 */
public class ServicesImpl extends JBIComponentImpl implements Services {
    
    /** Creates a new instance of ServicesImpl */
    public ServicesImpl(JBIModel model, Element element) {
        super(model, element);
    }
    
    public ServicesImpl(JBIModel model) {
        this(model, createElementNS(model, JBIQNames.SERVICES));
    }
    
    public void accept(JBIVisitor visitor) {
        visitor.visit(this);
    }
    
    public Boolean getBindingComponent() {
        String v = getAttribute(JBIAttributes.BINDING_COMPONENT);
        if (v != null) {
            return Boolean.valueOf(v);
        } else {
            return null;
        }
    }

    public void setBindingComponent(Boolean bindingComponent) {
        super.setAttribute(BINDING_COMPONENT_PROPERTY, 
                JBIAttributes.BINDING_COMPONENT, bindingComponent);
    }

    public List<Provides> getProvidesList() {
        return getChildren(Provides.class);
    }

    public void removeProvides(Provides provides) {
        removeChild(PROVIDES_PROPERTY, provides);
    }
    
    public void addProvides(int index, Provides provides) {
        insertAtIndex(PROVIDES_PROPERTY, provides, index, Provides.class);
    }

    public List<Consumes> getConsumesList() {
        return getChildren(Consumes.class);
    }

    public void removeConsumes(Consumes consumes) {
        removeChild(CONSUMES_PROPERTY, consumes);
    }
    
    public void addConsumes(int index, Consumes consumes) {
        insertAtIndex(CONSUMES_PROPERTY, consumes, index, Consumes.class);
    }
}

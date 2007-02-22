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


package org.netbeans.modules.xml.schema.abe.nodes;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.schema.abe.InstanceUIContext;
import org.openide.ErrorManager;
import org.openide.nodes.Node;


/**
 * This class provides a wrapper which invokes flush on the schema model
 * when setValue has been invoked.
 * @author Chris Webster
 */
public class SchemaModelFlushWrapper extends Node.Property {
    private Node.Property delegate;
    private AXIModel model;
    private boolean readOnly = false;
    AXIComponent axiComponent;
    private InstanceUIContext context;
    
    public SchemaModelFlushWrapper(AXIComponent sc, Node.Property delegate, InstanceUIContext context) {
        super(delegate.getValueType());
        this.context = context;
        model = sc.getModel();
        this.delegate = delegate;
        this.axiComponent = sc;
    } 
    
    @Override
    public void setValue(Object object) throws IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        try {
            context.setUserInducedEventMode(true);
            model.startTransaction();
            delegate.setValue(object);
        } finally {
            model.endTransaction();
        }
    }
    
    @Override
    public void restoreDefaultValue() throws IllegalAccessException,
            InvocationTargetException {
        try {
            model.startTransaction();
            delegate.restoreDefaultValue();
        } finally {
            model.endTransaction();
        }
    }
    
    @Override
    public boolean equals(Object object) {
        return delegate.equals(object);
    }

    @Override
    public void setExpert(boolean expert) {
        delegate.setExpert(expert);
    }

    @Override
    public void setHidden(boolean hidden) {
        delegate.setHidden(hidden);
    }

    @Override
    public void setPreferred(boolean preferred) {
        delegate.setPreferred(preferred);
    }

    @Override
    public void setShortDescription(String text) {
        delegate.setShortDescription(text);
    }

    @Override
    public Object getValue(String attributeName) {
        return delegate.getValue(attributeName);
    }

    @Override
    public void setDisplayName(String displayName) {
        delegate.setDisplayName(displayName);
    }

    @Override
    public void setName(String name) {
        delegate.setName(name);
    }

    @Override
    public void setValue(String attributeName, Object value) {
		delegate.setValue(attributeName, value);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public boolean supportsDefaultValue() {
        return delegate.supportsDefaultValue();
    }

    @Override
    public Object getValue() throws IllegalAccessException, 
    InvocationTargetException {
        return delegate.getValue();
    }

    @Override
    public String getShortDescription() {
        return delegate.getShortDescription();
    }

    @Override
    public java.beans.PropertyEditor getPropertyEditor() {
        return delegate.getPropertyEditor();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public String getHtmlDisplayName() {
        return delegate.getHtmlDisplayName();
    }

    @Override
    public String getDisplayName() {
        return delegate.getDisplayName();
    }
    
    @Override
    public boolean canWrite() {
        if( (axiComponent != null) && (axiComponent.getModel() != null) )
            return !(axiComponent.isReadOnly());
        return true;
    }

    @Override
    public boolean canRead() {
        return delegate.canRead();
    }

    @Override
    public Enumeration<String> attributeNames() {
        return delegate.attributeNames();
    }
   
    @Override
    public Class getValueType() {
        return delegate.getValueType();
    }
   
    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean isDefaultValue() {
        return delegate.isDefaultValue();
    }
    
    @Override
    public boolean isExpert() {
        return delegate.isExpert();
    }

    @Override
    public boolean isHidden() {
        return delegate.isHidden();
    }

    @Override
    public boolean isPreferred() {
        return delegate.isPreferred();
    }
    
}

package org.netbeans.modules.xml.wsdl.ui.api.property;

import java.lang.reflect.InvocationTargetException;

import org.openide.nodes.PropertySupport.ReadOnly;

public class ReadOnlyProperty extends ReadOnly {
    ExtensibilityElementPropertyAdapter adapter;
    
    public ReadOnlyProperty(ExtensibilityElementPropertyAdapter adapter, String name, Class type, String displayName, String shortDescription) {
        super(name, type, displayName, shortDescription);
        this.adapter = adapter;
    }

    @Override
    public Object getValue() throws IllegalAccessException,
            InvocationTargetException {
        return adapter.getValue();
    }

}

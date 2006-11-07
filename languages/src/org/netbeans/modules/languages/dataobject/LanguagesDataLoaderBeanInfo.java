package org.netbeans.modules.languages.dataobject;

import org.openide.loaders.UniFileLoader;

import java.awt.*;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.SimpleBeanInfo;

public class LanguagesDataLoaderBeanInfo extends SimpleBeanInfo {

    public BeanInfo[] getAdditionalBeanInfo() {
        try {
            return new BeanInfo[] {Introspector.getBeanInfo(UniFileLoader.class)};
        } catch (IntrospectionException e) {
            throw new AssertionError(e);
        }
    }
    
    public Image getIcon(int type) {
        return super.getIcon(type); // TODO add a custom icon here

    }

}

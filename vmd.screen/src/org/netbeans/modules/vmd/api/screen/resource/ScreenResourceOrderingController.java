/*
 * ScreenREsourceCategoryOrderingContoller.java
 *
 * Created on 21 marzec 2007, 14:26
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.vmd.api.screen.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;

/**
 *
 * @author Karol Harezlak
 */
public abstract class ScreenResourceOrderingController {
    
    private static ArrayOrderingController arrayOrdering;
    private static DefaultOrderingController defaultOrdering;
    
    public static ScreenResourceOrderingController getArrayOrdering(final String propertyName) {
        if (arrayOrdering == null)
            arrayOrdering = new ArrayOrderingController(propertyName);
        
        return arrayOrdering;
    }
    
    public static ScreenResourceOrderingController getDefaultOrdering() {
        if (defaultOrdering == null)
            defaultOrdering = new DefaultOrderingController();
        
        return defaultOrdering;
    }
    
    public abstract List<ScreenResourceItemPresenter> getOrdered(DesignComponent component, Collection<ScreenResourceItemPresenter> items);
    
    private static class ArrayOrderingController extends ScreenResourceOrderingController {
        
        private String propertyName;
        
        private ArrayOrderingController(String propertyName) {
            this.propertyName = propertyName;
        }
        
        public List<ScreenResourceItemPresenter> getOrdered(DesignComponent component, Collection<ScreenResourceItemPresenter> items) {
            List<ScreenResourceItemPresenter> list = new ArrayList<ScreenResourceItemPresenter>(items.size());
           
            List<PropertyValue> array = new ArrayList<PropertyValue>(component.readProperty(propertyName).getArray());
            for (PropertyValue value : array) {
                DesignComponent commandEventSource = value.getComponent();
                for (ScreenResourceItemPresenter descriptor : items)
                    if (descriptor.getRelatedComponent() == commandEventSource) {
                        list.add(descriptor);
                        break;
                    }
            }
            
            return list;
        }
    }
    
    private static class DefaultOrderingController extends ScreenResourceOrderingController {
        public List<ScreenResourceItemPresenter> getOrdered(DesignComponent component, Collection<ScreenResourceItemPresenter> items) {
            List<ScreenResourceItemPresenter> orderedList = new ArrayList<ScreenResourceItemPresenter>(items);
            Collections.sort(orderedList, new Comparator<ScreenResourceItemPresenter>() {
                public int compare(ScreenResourceItemPresenter item1, ScreenResourceItemPresenter item2) {
                    String name1 = InfoPresenter.getDisplayName(item1.getRelatedComponent());
                    String name2 = InfoPresenter.getDisplayName(item2.getRelatedComponent());
                    if (name1 == null || name2 == null)
                        throw new NullPointerException();
                    return name1.compareTo(name2);
                }
            });
            
            return orderedList;
        }
    }
}

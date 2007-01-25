/*
 * MapperData.java
 *
 * Created on 19 Декабрь 2006 г., 18:41
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xslt.mapper.model;

import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xslt.model.XslModel;

/**
 *
 * @author Alexey
 */
public interface MapperContext<T> {
    
    T getTransformDesc();
    
    XslModel getXSLModel();
    
    AXIComponent getTargetType();
    
    AXIComponent getSourceType();

    void addMapperContextChangeListener(MapperContextChangeListener listener);
    
    void removeMapperContextChangeListener(MapperContextChangeListener listener);
    
}

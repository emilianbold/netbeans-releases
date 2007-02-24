/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

/*
 * ElementData.java
 *
 * Created on August 12, 2005, 5:51 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.uml.propertysupport.customizers;

import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.propertysupport.DefinitionPropertyBuilder;
import java.util.ArrayList;
import java.util.Vector;
import java.util.HashMap;
/**
 *
 * @author thuy
 */
public class ElementData {
    public static String PARAM_NAME = "Name";  //NO I18N
    public static String PARAM_TYPE = "Type";  //NO I18N
    public static String PARAM_DIRECTION = "Direction";  //NO I18N
    public static String PARAM_KIND = "Kind";  //NO I18N
    public static String RANGE_LOWER = "Lower";  //NO I18N
    public static String RANGE_UPPER = "Upper";  //NO I18N
    public static String MULTI_RANGES = "MultiplicityRanges"; //NO I18N
    public static String MULTI_RANGE = "MultiplicityRange";   //NO I18N
    public  static String SEPARATOR = " ";
    public static String EMPTY_STR = new String();
    
    private IPropertyElement element;
    private HashMap <String, IPropertyElement> paramDataMap = new HashMap();
    private DefinitionPropertyBuilder builder = DefinitionPropertyBuilder.instance();
    private Vector multiRanges;
    
    /**
     * Creates a new instance of ElementData
     */
    public ElementData(IPropertyElement element) {
        this(element, true);
    }
    
    public ElementData(IPropertyElement element, boolean load) {
        this.element = element;
        if (element != null) {
            getPropertiesDataMap(element, load);
        }
    }
    
    public IPropertyElement getElement() {
        return element;
    }
    
    public void setElement(IPropertyElement propElement) {
        element = propElement;
    }
    
    public HashMap <String, IPropertyElement> getElementDataMap() {
        return paramDataMap;
    }
    
    public Vector <IPropertyElement> getSubElements() {
        Vector <IPropertyElement> subElements = element.getSubElements();
        return subElements;
    }
    
    public String getName() {
        String value = null;
        IPropertyElement propElem = paramDataMap.get(PARAM_NAME);
        if (propElem != null) {
            value = propElem.getValue();
        }
        return (value != null ? value : EMPTY_STR);
    }
    
    public void setName(String name) {
        IPropertyElement propElem = paramDataMap.get(PARAM_NAME);
        if (propElem != null) {
            propElem.setValue(name);
        }
    }
    
    public String getType() {
        String value = null;
        IPropertyElement propElem = paramDataMap.get(PARAM_TYPE);
        if (propElem != null) {
            value = propElem.getValue();
        }
        return (value != null ? value : EMPTY_STR);
    }
    
    public void setType(String type) {
        IPropertyElement propElem = paramDataMap.get(PARAM_TYPE);
        if (propElem != null) {
            propElem.setValue(type);
        }
    }
    
    public String getDirection() {
        String value = null;
        IPropertyElement propElem = paramDataMap.get(PARAM_DIRECTION);
        if (propElem != null) {
            value = propElem.getValue();
        }
        return (value != null ? value : EMPTY_STR);
    }
    
    
    public void setDirection(String value) {
        IPropertyElement propElem = paramDataMap.get(PARAM_DIRECTION);
        if (propElem != null) {
            propElem.setValue(value);
        }
    }
    
    public String getKind() {
        String value = null;
        IPropertyElement propElem = paramDataMap.get(PARAM_KIND);
        if (propElem != null) {
            value = propElem.getValue();
        }
        return (value != null ? value : EMPTY_STR);
    }
    
    public void setKind(String value) {
        IPropertyElement propElem = paramDataMap.get(PARAM_KIND);
        if (propElem != null) {
            propElem.setValue(value);
        }
    }
    
    public String getLower() {
        String value = null;
        IPropertyElement propElem = paramDataMap.get(RANGE_LOWER);
        if (propElem != null) {
            value = propElem.getValue();
        }
        return (value != null ? value : EMPTY_STR);
    }
    
    
    public void setLower(String value) {
        IPropertyElement propElem = paramDataMap.get(RANGE_LOWER);
        if (propElem != null) {
            propElem.setValue(value);
        }
    }
    
    public String getUpper() {
        String value = null;
        IPropertyElement propElem = paramDataMap.get(RANGE_UPPER);
        if (propElem != null) {
            value = propElem.getValue();
        }
        return (value != null ? value : EMPTY_STR);
    }
    
    public void setUpper(String value) {
        IPropertyElement propElem = paramDataMap.get(RANGE_UPPER);
        if (propElem != null) {
            propElem.setValue(value);
        }
    }
    
    public  IPropertyElement getMultiRangesProp() {
        return paramDataMap.get(MULTI_RANGES);
    }
    
    public IPropertyDefinition getMultiRangesSubDef() {
        // - get PropertyElement of MultiplictyRanges
        // - get PropertyDefinition of MultiplictyRanges
        // - get subdefinition of mutiplictyRanges
        // - load subDefintiion if isOnDemand flag is true
        IPropertyElement propElem = getMultiRangesProp();
        IPropertyDefinition subDef = propElem.getPropertyDefinition().getSubDefinition(MULTI_RANGE);
        if(subDef != null && subDef.isOnDemand()) {
            DefinitionPropertyBuilder builder = DefinitionPropertyBuilder.instance();
            subDef = builder.loadOnDemandDefintion(subDef);
        }
        return subDef;
    }
    
    public void setMultiRanges(Vector multiRangeList) {
        multiRanges = multiRangeList;
    }
    
    public Vector getMultiRanges() {
        return multiRanges;
    }
    
    
    private void getPropertiesDataMap(IPropertyElement paramElem, boolean load) {
        if (paramElem != null) {
            if (load) {
                if(paramElem.getPropertyDefinition().isOnDemand()) {
                    builder.loadOnDemandProperties(paramElem);
                }
            }
            paramDataMap.put(paramElem.getName(), paramElem);
            Vector <IPropertyElement> childElements = paramElem.getSubElements();
            if (childElements != null) {
                for (IPropertyElement ele : childElements){
                    getPropertiesDataMap(ele, load);
                }
            }
        }
    }
    
    public void save() {
        if(element != null) {
            save(element);
        }
    }
    
    protected void save(IPropertyElement element) {
        if(element != null) {
            element.save();
            Vector < IPropertyElement > children = element.getSubElements();
            for(IPropertyElement child : children) {
                save(child);
            }
        }
    }
    
    public void remove() {
        if(element != null) {
            element.remove();
        }
    }
    
    public String toString(){
        return (getType() + SEPARATOR + getName());
    }
}


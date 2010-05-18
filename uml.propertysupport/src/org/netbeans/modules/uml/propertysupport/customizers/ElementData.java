/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */


package org.netbeans.modules.uml.propertysupport.customizers;

import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.propertysupport.DefinitionPropertyBuilder;
import java.util.Vector;
import java.util.HashMap;
import org.netbeans.modules.uml.propertysupport.DefinitionPropertyBuilder.ValidValues;
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
    public static String COLLECTION_TYPE = "collectionType";
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
    
    public String getCollectionType()
    {   
        String value = null;
        IPropertyElement propElem = paramDataMap.get(COLLECTION_TYPE);
        if (propElem != null) {
            value = propElem.getValue();
        }
        return (value != null ? value : EMPTY_STR);
    }
    
    public void setCollectionType(String value) {
        IPropertyElement propElem = paramDataMap.get(COLLECTION_TYPE);
        if (propElem != null) {
            propElem.setValue(value);
        }
    }
    
    public String[] getValidCollectionTypes()
    {
        String[] retVal = new String[0];
        
        IPropertyElement propElem = paramDataMap.get(COLLECTION_TYPE);
        if (propElem != null) {
            DefinitionPropertyBuilder builder = DefinitionPropertyBuilder.instance();
            ValidValues values = builder.retrieveValidValues(propElem.getPropertyDefinition(),
                                                             propElem);
            retVal = values.getValidValues();
        }
        
        return retVal;
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


/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.common.xmlutils;

/**
 *
 * @author  builder
 */
public class ElementAttrInfo {
    
    private String elementName;
    private String attributeName;
    private String attributeValue;

    /** Creates new ElementAttrInfo */
    public ElementAttrInfo(String elementName,String attributeName,String attributeValue) {
        this.elementName=elementName;
        this.attributeName=attributeName;
        this.attributeValue=attributeValue;
    }    
    public String getElementName(){
        return elementName;
    }
    public String getAttributeName(){
        return attributeName;
    }    
    public String getAttributeValue(){
        return attributeValue;
    }    

}

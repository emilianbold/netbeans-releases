/*
 * Utils.java
 * 
 * Created on 31.08.2007, 15:40:45
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.xpath.ext;

import javax.xml.namespace.QName;

/**
 * Utility class.
 * 
 * @author nk160297
 */
public class XPathUtils {

    /**
     * Converts the qName to a String. 
     * Only the prefix and the local part is used. 
     * The namespace URI is ignored. 
     * 
     * This method is intended to print an entity.
     */ 
    public static String qNameObjectToString(QName qName) {
        String prefix = qName.getPrefix();
        if (prefix == null || prefix.length() == 0) {
            return qName.getLocalPart();
        } else {
            return prefix + ":" + qName.getLocalPart();
        }
                
    }
    
    /**
     * Converts the qName to a String. 
     * Only the prefix and the namespace URI is used. 
     * The local part is ignored. 
     * 
     * This method is intended to print a namespace which is held in a QName.
     */ 
    public static String gNameNamespaceToString(QName qName) {
        String prefix = qName.getPrefix();
        String nsUri = qName.getNamespaceURI();
        //
        if (prefix == null || prefix.length() == 0) {
            return "{" + nsUri + "}";
        } else {
            return "{" + nsUri + "}" + prefix;
        }
    }
    
}

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
    
    public static boolean equalsIgnorNsUri(QName qName1, QName qName2) {
        return (qName1.getLocalPart().equals(qName2.getLocalPart())) && 
                (qName1.getPrefix().equals(qName2.getPrefix()));
    }
    
    public static boolean samePredicatesArr(
            XPathPredicateExpression[] predArr1, 
            XPathPredicateExpression[] predArr2) {
        //
        // Compare predicates count
        int counter1 = predArr1 == null ? 0 : predArr1.length;
        int counter2 = predArr2 == null ? 0 : predArr2.length;
        if (counter1 != counter2) {
            return false;
        }
        // Compare predicates one by one
        for (int index = 0; index < counter1; index++) {
            XPathPredicateExpression predicate1 = predArr1[index];
            XPathPredicateExpression predicate2 = predArr2[index];
            String predText1 = predicate1.getExpressionString();
            String predText2 = predicate2.getExpressionString();
            if (!(predText1.equals(predText2))) {
                return false;
            }
        }
        return true;
    }
    
}

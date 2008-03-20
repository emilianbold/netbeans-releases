/*
 * Utils.java
 * 
 * Created on 31.08.2007, 15:40:45
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.xpath.ext;

import java.util.List;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xpath.ext.schema.FindAllChildrenSchemaVisitor;

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
    
    /**
     * Check if the namespace URI is specified for the name. 
     * If the name isn't specified, then try resolve it from the prefix.
     * Returns the corrected name if possible. Otherwise returns old name.
     * @param nsContext
     * @param name
     * @return
     */
    public static QName resolvePrefix(NamespaceContext nsContext, QName name) {
        String nsUri = name.getNamespaceURI();
        if (nsUri == null || nsUri.length() == 0 && nsContext != null) {
            //
            String nsPrefix = name.getPrefix();
            nsUri = nsContext.getNamespaceURI(nsPrefix);
            //
            if (nsUri != null) {
                String localPart = name.getLocalPart();
                QName newName = new QName(nsUri, localPart);
                name = newName;
            }
        }
        //
        return name;
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
    
    /**
     * Determines if a namespace prefix is required for the specified schema component. 
     * @param sComp
     * @return
     */
    public static boolean isPrefixRequired(SchemaComponent sComp) {
        if (sComp instanceof LocalElement) {
            Form form = ((LocalElement)sComp).getFormEffective();
            if (form == Form.QUALIFIED) {
                return true;
            } else {
                return false;
            }
        } else if (sComp instanceof GlobalElement) {
            return true;
        } else if (sComp instanceof LocalAttribute) {
            Form form = ((LocalAttribute)sComp).getFormEffective();
            if (form == Form.QUALIFIED) {
                return true;
            } else {
                return false;
            }
        } else if (sComp instanceof GlobalElement || 
                sComp instanceof ElementReference || 
                sComp instanceof GlobalAttribute) {
            // all global objects have to be with a prefix
            return true;
        }
        //
        assert true : "Unsupported schema component in the BPEL mapper tree!"; // NOI18N
        return false;
    }
    
    /**
     * Checks if the specified Schema component has any subcomponents.
     * @param sComp
     * @return
     */
    public static boolean hasSubcomponents(SchemaComponent sComp) {
        FindAllChildrenSchemaVisitor checker = 
                new FindAllChildrenSchemaVisitor(true, true);
        checker.lookForSubcomponents(sComp);
        List<SchemaComponent> found = checker.getFound();
        return found != null && !found.isEmpty();
    }
}

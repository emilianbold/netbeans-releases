/*
 * PropertyDataFormatter.java
 *
 * Created on April 17, 2007, 4:48 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.uml.propertysupport.customizers;

/**
 *
 * @author treyspiva
 */
public class PropertyDataFormatter
{
    
    /** Creates a new instance of PropertyDataFormatter */
    public PropertyDataFormatter()
    {
    }
    
    public static String translateFullyQualifiedName(String fullQName)
    {
        String retVal = fullQName;
        
        if((fullQName != null) && (fullQName.indexOf("::") > 0))
        {
            int index = fullQName.lastIndexOf("::");
            StringBuffer name = new StringBuffer(fullQName.substring(index + 2));
            name.append(" : ");
            name.append(fullQName.substring(0, index));
            retVal = name.toString();
        }
        
        return retVal;
    }
    
    public static String translateToFullyQualifiedName(String value)
    {
        String retVal = value;
        
        if((value != null) && (value.indexOf(" : ") > 0))
        {
            int index = value.indexOf(" : ");
            String shortName = value.substring(0, index);
            
            StringBuffer fullQName = new StringBuffer();
            fullQName.append(value.substring(index + 3));
            fullQName.append("::");
            fullQName.append(shortName);
            retVal = fullQName.toString();
        }
        
        return retVal;
    }
}

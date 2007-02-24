
/*
 * File       : TestTranslator.java
 * Created on : Nov 3, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.configstringframework;

import java.util.ResourceBundle;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;

/**
 * @author Aztec
 */
public class NewTestTranslator implements ICustomTranslator
{
    private static ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.modules.uml.core.configstringframework.TestBundle");
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.configstringframework.ICustomTranslator#translate(org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition, java.lang.String)
     */
    public String translate(IPropertyDefinition pDef, String sPSK)
    {
        return messages.getString(sPSK); 
    }

}

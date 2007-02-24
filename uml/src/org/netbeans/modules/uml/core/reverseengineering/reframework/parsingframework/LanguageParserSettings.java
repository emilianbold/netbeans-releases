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
 * File       : LanguageParserSettings.java
 * Created on : Oct 27, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;

import java.util.HashMap;

import javax.swing.plaf.SplitPaneUI;

import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class LanguageParserSettings implements ILanguageParserSettings
{
    HashMap<String, String> m_SettingMap = new HashMap<String, String>();
    HashMap<String, ETList<ILanguageMacro>> m_LanguageMacrosMap = new HashMap<String, ETList<ILanguageMacro>>();    

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParserSettings#addMacro(java.lang.String, org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageMacro)
     */
    public void addMacro(String language, ILanguageMacro macro)
    {
        ETList<ILanguageMacro> spLanguageMacros = ensureLanguageMacros(language);
        spLanguageMacros.add(macro);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParserSettings#addSetting(java.lang.String, java.lang.String, java.lang.String)
     */
    public void addSetting(String language, String name, String value)
    {
        m_SettingMap.put(name, value) ;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParserSettings#getAllMacros()
     */
    public ETList<ILanguageMacro> getAllMacros()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParserSettings#getMacro(java.lang.String, java.lang.String)
     */
    public ILanguageMacro getMacro(String language, String name)
    {
        if(language == null || name == null) return null;
        
        ETList<ILanguageMacro> spLanguageMacros = ensureLanguageMacros(language);
        ILanguageMacro spLanguageMacro = null;
        int lCount = spLanguageMacros.size();
        for (int i = 0; i < lCount; i++)
        {
            spLanguageMacro = spLanguageMacros.get(i);
            if (spLanguageMacro != null)
            {
                if (name.equals(spLanguageMacro.getName()))
                    return spLanguageMacro;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParserSettings#getMacros(java.lang.String)
     */
    public ETList<ILanguageMacro> getMacros(String language)
    {
        return ensureLanguageMacros(language);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParserSettings#getSetting(java.lang.String, java.lang.String)
     */
    public String getSetting(String language, String name)
    {
        if(language == null || name == null) return null;
        
        return m_SettingMap.get(name);
    }
    
    private ETList<ILanguageMacro> ensureLanguageMacros(String language)
    {
        if(language == null) return null;
        
        ETList<ILanguageMacro> spLanguageMacros = null;
        if(m_LanguageMacrosMap != null)
        {
            spLanguageMacros = m_LanguageMacrosMap.get(language);
        }
        if( spLanguageMacros == null)
        {
            spLanguageMacros = new ETArrayList<ILanguageMacro>();
            // Add to the macro map.        
            m_LanguageMacrosMap.put(language, spLanguageMacros);           
        }        
        return spLanguageMacros;
    }
}

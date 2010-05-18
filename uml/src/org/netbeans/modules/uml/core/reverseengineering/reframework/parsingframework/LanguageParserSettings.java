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

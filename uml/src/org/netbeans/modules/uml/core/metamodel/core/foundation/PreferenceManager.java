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

package org.netbeans.modules.uml.core.metamodel.core.foundation;

import java.io.IOException;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.openide.util.NbBundle;


public class PreferenceManager implements IPreferenceManager
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IPreferenceManager#getIsAliasingOn()
     */
    public boolean getIsAliasingOn()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IPreferenceManager#setIsAliasingOn(boolean)
     */
    public void setIsAliasingOn(boolean newVal)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IPreferenceManager#getDefaultLanguage(java.lang.String)
     */
    public String getDefaultLanguage(String modeName)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IPreferenceManager#setDefaultLanguage(java.lang.String, java.lang.String)
     */
    public void setDefaultLanguage(String modeName, String newVal)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IPreferenceManager#getDefaultModeName()
     */
    public String getDefaultModeName()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IPreferenceManager#setDefaultModeName(java.lang.String)
     */
    public void setDefaultModeName(String newVal)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IPreferenceManager#getHomeLocation()
     */
    public String getHomeLocation()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IPreferenceManager#unknownClassifier(java.lang.String)
     */
    public String unknownClassifier()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IPreferenceManager#installDefaultModelLibraries(org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage)
     */
    public void installDefaultModelLibraries(IPackage pack, 
                                             ETList<IElement> libs)
    {
        Element elem = retrieveDefaultModelElement();
        if (elem != null)
        {
            String libraries = elem.attributeValue("modelLibraries");
            if (libraries != null && libraries.length() > 0)
            {
                ETList<String> tokens = StringUtilities.splitOnDelimiter(libraries, null);
                
                // TODO: Do the hard work here
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IPreferenceManager#save(java.lang.String)
     */
    public void save(String fileName)
    {
        if (m_PrefDoc != null)
        {
            XMLManip.save(m_PrefDoc, fileName);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IPreferenceManager#getDefaultElementName()
     */
    public String getDefaultElementName()
    {
        //kris richards - "DefaultelementName" pref expunged. Set to "Unnamed"
        return NbBundle.getMessage (PreferenceManager.class, "UNNAMED") ;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IPreferenceManager#setDefaultElementName(java.lang.String)
     */
    public void setDefaultElementName(String newVal)
    {
        setAttribute("Preferences/DefaultProject/Elements/NamedElement",
            "defaultName", newVal);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IPreferenceManager#getDefaultProjectName()
     */
    public String getDefaultProjectName()
    {
        Element elem = retrieveDefaultProjectElement();
        if (elem != null)
            return elem.attribute("name").getValue();
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IPreferenceManager#setDefaultProjectName(java.lang.String)
     */
    public void setDefaultProjectName(String newVal)
    {
        Element elem = retrieveDefaultProjectElement();
        if (elem != null)
            elem.attribute("name").setValue(newVal);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IPreferenceManager#load(java.lang.String)
     */
    public void load(String prefFile)
    {
        if (prefFile == null || prefFile.length() == 0)
            prefFile = retrieveDefaultPreferenceLocation();
        if (prefFile != null && prefFile.length() > 0)
            m_PrefDoc = XMLManip.getDOMDocument(prefFile);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IPreferenceManager#retrieveDefaultModelLibraryNames()
     */
    public ETList<String> retrieveDefaultModelLibraryNames()
    {
        Element elem = retrieveDefaultModelElement();
        if (elem != null)
        {
            String libraries = elem.attributeValue("modelLibraries");
            if (libraries != null && libraries.length() > 0)
            {
                ETList<String> tokens = StringUtilities.splitOnDelimiter(libraries, null);
                
                ETList<String> names =
                    new ETArrayList<String>(tokens.size());
                for (Iterator<String> iter = tokens.iterator(); 
                        iter.hasNext(); )
                {
                    String name = 
                        getAttribute("//*[@id=\"" + iter.next() + "\"]", 
                            "name");
                    if (name != null)
                        names.add(name);
                }
                return names;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IPreferenceManager#getDefaultRoundTripBehavior(java.lang.String, java.lang.String)
     */
    public String getDefaultRoundTripBehavior(
        String sLanguage,
        String sBehaviorType)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IPreferenceManager#setDefaultRoundTripBehavior(java.lang.String, java.lang.String, java.lang.String)
     */
    public void setDefaultRoundTripBehavior(
        String sLanguage,
        String sBehaviorType,
        String sValue)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IPreferenceManager#retrieveDefaultPreferenceLocation()
     */
    public String retrieveDefaultPreferenceLocation()
    {
        if (m_DefaultPreferenceLoc == null 
            || m_DefaultPreferenceLoc.length() == 0)
        {
            ICoreProduct product = ProductRetriever.retrieveProduct();
            IConfigManager man = product != null?
                product.getConfigManager() : new ConfigManager();
            if (man != null)
                m_DefaultPreferenceLoc = man.getPreferenceLocation();
        }
        return m_DefaultPreferenceLoc;
    }
    
    protected Element retrieveDefaultProjectElement()
    {
        return retrieveElement("//DefaultProject");
    }
    
    protected Element retrieveDefaultModelElement()
    {
        String name = getAttribute("//DefaultMode", "name");
        if (name != null && name.length() > 0)
            return retrieveElement("//DefaultModes/Mode[@name=\"" +
                name + "\"]");
        return null;
    }

    protected Element retrieveElement(String xpath)
    {
        if (m_PrefDoc == null)
            load(null);
        if (m_PrefDoc != null)
        {
            Node node = m_PrefDoc.selectSingleNode(xpath);
            if (node instanceof Element)
                return (Element) node;
        }
        return null;
    }
    
    protected String getAttribute(String xpath, String attribute)
    {
        if (m_PrefDoc == null)
            load(null);
        if (m_PrefDoc != null)
        {
            Node node = m_PrefDoc.selectSingleNode(xpath);
            if (node instanceof Element)
            {
                Element elem = (Element) node;
                return elem.attributeValue(attribute);
            }
        }
        return null;
    }
    
    protected void setAttribute(String xpath, String attribute, String value)
    {
        if (m_PrefDoc == null)
            load(null);
        if (m_PrefDoc != null)
        {
            Node node = m_PrefDoc.selectSingleNode(xpath);
            if (node instanceof Element)
            {
                Element elem = (Element) node;
                elem.attribute(attribute).setValue(value);
            }
        }
    }

    private String   m_DefaultPreferenceLoc;
    private Document m_PrefDoc;
}

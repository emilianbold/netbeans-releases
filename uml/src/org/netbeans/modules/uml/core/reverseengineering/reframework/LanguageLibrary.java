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

package org.netbeans.modules.uml.core.reverseengineering.reframework;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 */
public class LanguageLibrary implements ILanguageLibrary
{
    private String   m_IndexName, m_LookupFile;
    private Document m_LookupDocument;
    private Map<String,String> m_Index = new HashMap<String,String>();
    
    /**
     * The LookupLanguageLibrary will use a configuration file to retrieve 
     * the XMI id of the class that is contained in a Describe project
     * 
     * @param name [in] Then name of the class.
     * @param pVal [out] The class data.
     */
    public IREClass findClass(String name)
    {
        IREClass cl = null;
        String fullName = name;
        String shortName = name ;
        // NOTE: I am making the assumption that we are using the Java RT library.
        // In java there are no JDK classes that are not in a package.  Also, since
        // classes in the java.lang package do not have to have an import statement
        // that references the class I have to make sure that I check for classes 
        // in the java.lang package.  To accomplish the java.lang check I am checking
        // all non-scoped classes against the java.lang package.
        //
        // This is very Java specific and I should create a new JavaRT index file that 
        // will take this into account.  Basically the index file must also take into
        // account all global scope packages.
        String id = null;
        
        //check for <*> appended to name
        int index = -1;
        if ((index=name.indexOf("<")) > 0) {
            shortName = name.substring(0,index);
        }
        
        if (shortName.indexOf("::") == -1 && shortName.indexOf(".") == -1) {
            //try java.util.*
            fullName = "java::util::" + shortName;

            if ((id = m_Index.get(fullName)) == null) {
                fullName = "java::lang::" + shortName;

                if ((id = m_Index.get(fullName)) == null) {
                    fullName = resolveFullyQualifiedName(shortName);
                }
            }
        }
            
        if (id == null)
        id = m_Index.get(fullName);
        if (m_LookupDocument != null && id != null)
        {
            //String query = "//*[@xmi.id=\"" + id + "\"]";            
            //Node n = XMLManip.selectSingleNode(m_LookupDocument, query);
            
            Node n = m_LookupDocument.elementByID(id);
            
            if (n != null)
            {
                cl = new REClass();
                cl.setEventData(n);
            }
        }
        return cl;
    }

    /**
     * Retrieves the XML file that is used to lookup the class information.  
     * The XMI Ids must be the same used in the index file.
     * 
     * @param pVal [out] The name of the XML file.
     */
    public String getLookupFile()
    {
        return m_LookupFile;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.ILanguageLibrary#setLookupFile(java.lang.String)
     */
    public void setLookupFile(String newval)
    {
        if (newval == null) return ;
        
        // AZTEC: C++ code did m_IndexName = newval, which is strange
        m_LookupFile = newval;
        m_LookupDocument = XMLManip.getDOMDocumentUseWeakCache(newval);
    }


    /**
     * Retrieves the index file used to map the class names to a XMI ID.
     * 
     * @param pVal [out] Then name of the index file.
     */
    public String getIndex()
    {
        return m_IndexName;
    }

    /**
     * Sets the index file used to map the class names to a XMI ID.
     * 
     * @param pVal [in] Then name of the index file.
     */
    public void setIndex(String newVal)
    {
        m_IndexName = newVal;
        try
        {
            loadIndex(m_IndexName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    protected void loadIndex(String file) 
        throws FileNotFoundException, IOException
    {
        FileReader fr = new FileReader(file);
        BufferedReader bufr = new BufferedReader(fr);
        String line;
        while ((line = bufr.readLine()) != null)
        {
            int pos = line.lastIndexOf(':');
            if (pos != -1)
            {
                String key = line.substring(0, pos).trim();
                String val = line.substring(pos + 1).trim();
                
                m_Index.put(key, val);
            }
        }
        bufr.close();
        fr.close();
    }

    private String resolveFullyQualifiedName(String name) {
        String result = null ;
        String query = "::"+name;
        Iterator<String> iter = m_Index.keySet().iterator() ;
        
        while (iter.hasNext()) {
            String key = iter.next() ;
            if (key.endsWith(query)) {
                result = key ;
                break ;
            }
            
        }
        
        return result ;
    }
}

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
 * File       : TopLevelStateHandler.java
 * Created on : Dec 8, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.dom4j.*;

import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 * @author Aztec
 */
public class TopLevelStateHandler extends StateHandler
{
    String m_Language = null;

    public TopLevelStateHandler(String language)
    {
        m_Language = language;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.ITopLevelStateHandler#createTopLevelNode(java.lang.String)
     */
    public void createTopLevelNode(String nodeName)
    {
        if(nodeName == null) return;
        
        Document pDoc = XMLManip.getDOMDocument();
        if(pDoc != null)
        {

            Node pNewNode = createNode(pDoc, nodeName);

            if(pNewNode != null)
            {
                Element element = (pNewNode instanceof Element)?
                                    (Element)pNewNode : null;
                if(element != null)
                {
                    XMLManip.setAttributeValue(element, 
                                                "language", 
                                                getLanguage());
                }

                setDOMNode(pNewNode);
            }
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.ITopLevelStateHandler#writeDocument(java.lang.String)
     */
    public void writeDocument(String filename)
    {
        try
        {
            String xml = getDOMNode().getDocument().asXML();
            File f = new File(filename);
            f.createNewFile();
            if(f.exists())
            {
                FileWriter fw = new FileWriter(f);
                fw.write(xml);
            }
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.ITopLevelStateHandler#getLanguage()
     */
    public String getLanguage()
    {
        return m_Language;
    }

}

/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.testtools.wizards;

/*
 * XMLDocument.java
 *
 * Created on April 25, 2002, 1:55 PM
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import org.openide.ErrorManager;
import org.openide.loaders.DataObject;
import org.openide.cookies.EditorCookie;
import org.openide.xml.EntityCatalog;

import org.apache.tools.ant.module.api.AntProjectCookie;
import org.netbeans.modules.testtools.XTestProjectSupport;
import org.xml.sax.SAXException;

/**
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
class XMLDocument {
    
    private Document doc;
    private DataObject dob;
    
    private static void fail(Throwable t) {
        ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, t);
    }
    
    XMLDocument(DataObject o) {
        dob=o;
        AntProjectCookie cookie=(AntProjectCookie)o.getCookie(AntProjectCookie.class);
        if (cookie==null) cookie=new XTestProjectSupport(dob.getPrimaryFile());
        doc=cookie.getDocument();
/* 
        if (cookie!=null) {
            doc=cookie.getDocument();
        } else try {
            DocumentBuilder builder=DocumentBuilderFactory.newInstance().newDocumentBuilder();
            builder.setEntityResolver(EntityCatalog.getDefault());
            doc=builder.parse(o.getPrimaryFile().getInputStream());
        } catch (IOException ioe) {
            fail(ioe);
        } catch (SAXException saxe) {
            fail(saxe);
        } catch (ParserConfigurationException pce) {
            fail(pce);
        }
 */
    }
    
    void setProperty(String name, String valueName, Object valueValue) {
        setElement("property", "name", name, valueName, valueValue); // NOI18N
    }
    
    void setElement(String element, String valueName, Object valueValue) {
        setElement(element, null, null, valueName, valueValue);
    }
    
    void setElement(String element, String nameName, String nameValue, String valueName, Object valueValue) {
        if (valueValue==null) return;
        NodeList list = doc.getElementsByTagName(element);
        for (int i=0; i<list.getLength(); i++) try {
            if ((nameName==null)||(nameValue.equals(list.item(i).getAttributes().getNamedItem(nameName).getNodeValue()))) {
                Node n=list.item(i).getAttributes().getNamedItem(valueName);
                if (n!=null)
                    n.setNodeValue(valueValue.toString());
            }
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    String getProperty(String name, String valueName) {
        return getElement("property", "name", name, valueName); // NOI18N
    }
    
    String getElement(String element, String valueName) {
        return getElement(element, null, null, valueName);
    }

    String getElement(String element, String nameName, String nameValue, String valueName) {
        NodeList list = doc.getElementsByTagName(element);
        for (int i=0; i<list.getLength(); i++) try {
            if ((nameName==null)||(nameValue.equals(list.item(i).getAttributes().getNamedItem(nameName).getNodeValue()))) {
                Node n=list.item(i).getAttributes().getNamedItem(valueName);
                if (n!=null)
                    return n.getNodeValue();
                else 
                    return null;
            }
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
        return null;
    }
}

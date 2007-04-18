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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.javaee.sunresources.tool.archive;

import java.util.ArrayList;
import org.openide.util.NbBundle;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.netbeans.modules.compapp.javaee.sunresources.tool.archive.ArchiveConstants.ArchiveType;

/**
 * @author echou
 *
 */
// helper class for parsing application.xml DD
class AppDDHandler extends DefaultHandler {

    private String version;
    private AppModule curModule = null;
    private ArrayList<AppModule> modules = new ArrayList<AppModule> ();
    
    private String curElement = null;  // track current Element node
    
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (ArchiveConstants.TAG_APP_EJB.equals(curElement) ||
                ArchiveConstants.TAG_APP_WEBURI.equals(curElement) ||
                ArchiveConstants.TAG_APP_CLIENT.equals(curElement) ||
                ArchiveConstants.TAG_APP_RAR.equals(curElement)) {
            curModule.path = String.copyValueOf(ch, start, length);
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        curElement = qName;
        
        if (ArchiveConstants.TAG_APPLICATION.equals(qName)) {
            version = attributes.getValue(ArchiveConstants.ATTR_VERSION);
        } else if (ArchiveConstants.TAG_APP_MODULE.equals(qName)) {
            curModule = new AppModule();
        } else if (ArchiveConstants.TAG_APP_EJB.equals(qName)) {
            curModule.type = ArchiveType.EJB;
        } else if (ArchiveConstants.TAG_APP_WEB.equals(qName)) {
            curModule.type = ArchiveType.WAR;
        } else if (ArchiveConstants.TAG_APP_CLIENT.equals(qName)) {
            curModule.type = ArchiveType.CLIENT;
        } else if (ArchiveConstants.TAG_APP_RAR.equals(qName)) {
            curModule.type = ArchiveType.RAR;
        }
    }
    
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (ArchiveConstants.TAG_APP_MODULE.equals(qName)) {
            if (curModule != null) {
                modules.add(curModule);
                curModule = null;
            } else {
                throw new SAXException(
                        NbBundle.getMessage(AppDDHandler.class, "EXC_illegal_state"));
            }
        }
        curElement = null;
    }

    public String getVersion() {
        return version;
    }
    public ArrayList<AppModule> getModules() {
        return modules;
    }
    
    class AppModule {
        ArchiveType type;
        String path;
        
        AppModule() {
            
        }
        
    }
}
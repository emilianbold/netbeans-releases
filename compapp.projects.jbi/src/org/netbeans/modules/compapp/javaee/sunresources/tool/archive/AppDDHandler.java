/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
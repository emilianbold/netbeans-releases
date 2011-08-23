/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.libs.oracle.cloud;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.DefaultHandler;

class WhiteListConfigHandler extends DefaultHandler2 {

    private String configStartElement = null;
    private DefaultHandler handler = null;
    private static HashMap<String, DefaultHandler> handlers = new HashMap<String, DefaultHandler>();
    private List<String> otherFiles;

    public WhiteListConfigHandler(List<String> otherFiles) {
        this.otherFiles = otherFiles;
    }

    public void startDocument()
            throws SAXException {
        registedHandlers();
    }

    public void endDocument()
            throws SAXException {
        Collection<DefaultHandler> hlds = handlers.values();
        for (DefaultHandler hld : hlds) {
            hld.endDocument();
        }
        handlers.clear();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (configStartElement == null) {
            handler = getHandler(qName);
            if (handler != null) {
                configStartElement = qName;
            }
        }
        if (handler != null) {
            handler.startElement(uri, localName, qName, attributes);
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        if (handler != null) {
            handler.characters(ch, start, length);
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (handler != null) {
            handler.endElement(uri, localName, qName);
        }
        if (qName.equals(configStartElement)) {
            configStartElement = null;
            handler = null;
        }
    }

    private DefaultHandler getHandler(String element) {
        return handlers.get(element);
    }

    private void registedHandlers() {
        handlers.put("WhitelistClassMethod", 
            new WhiteListClassHandler(WhiteListClassHandler.Type.Class,
                "WhitelistClassMethod","ClassName", "Method", "MethodName", "Parameters"));
        handlers.put("WhitelistExtendableClass", 
            new WhiteListClassHandler(WhiteListClassHandler.Type.Extendable,
                "WhitelistExtendableClass","ExtendableClassName", "Method", "OverrideMethodName", "Parameters"));
        handlers.put("WhitelistInstantiateableClass", 
            new WhiteListClassHandler(WhiteListClassHandler.Type.Instantiable,
                "WhitelistInstantiateableClass", "InstantiateableClassName", "Constructor", null, "Parameters"));
        handlers.put("ListOfPackageImport", 
            new WhiteListPackageImportHandler(otherFiles));
//        handlers.put("ListOfSystemPackage",
//            new WhiteListSystemPackageHandler());
        handlers.put("WhitelistPackages",
            new WhiteListPackagesHandler());
    }
}

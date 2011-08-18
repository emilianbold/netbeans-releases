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
package org.netbeans.modules.libs.cloud9;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


import javax.xml.parsers.ParserConfigurationException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.spi.whitelist.WhiteListQueryImplementation.WhiteListImplementation;
import org.netbeans.spi.whitelist.support.WhiteListImplementationBuilder;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public final class WhiteListConfigReader {

    public final static String CONFIG_ROOT = "/oracle/hostedcode/common/whitelist/configuration/resource/";
    public final static String CONFIG_FILE = "whitelistconfiguration.xml";

    private static final Logger LOG = Logger.getLogger(WhiteListConfigReader.class.getName());
    
    private static WhiteListImplementationBuilder builder;
    
    synchronized static WhiteListImplementationBuilder getBuilder() {
        if (builder == null) {
            builder = WhiteListImplementationBuilder.create();
        }
        return builder;
    }
    
    private static WhiteListImplementation reader;

    @NbBundle.Messages({
        "TXT_WhiteListName=Oracle Public Cloud"
    })
    public synchronized static WhiteListImplementation getDefault() {
        if (reader == null) {
            
//            // XXX: whitelist files are read in separate thread; negative
//            // side effect is that first code completio will not contain
//            // any whitelisting
//            
//            reader = new WhiteListImplementation() {
//                @Override
//                public boolean canInvoke(ElementHandle<?> element) {
//                    return true;
//                }
//
//                @Override
//                public boolean canOverride(ElementHandle<?> element) {
//                    return true;
//                }
//            };
//            RequestProcessor.getDefault().post(new Runnable() {
//
//                @Override
//                public void run() {
                    try {
                        readXMLs();
                    } catch (ParserConfigurationException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (SAXException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    setRealReader(
                         getBuilder().
                            setDisplayName(Bundle.TXT_WhiteListName()).
                         build());
//                }
//            });
        }
        return reader;
    }
    
    private synchronized static void setRealReader(WhiteListImplementation w) {
        reader = w;
    }
    
    private static void readXMLs() throws ParserConfigurationException, SAXException, IOException {
        long l = System.currentTimeMillis();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        SAXParser saxParser = factory.newSAXParser();
        List<String> otherFiles = new ArrayList<String>();
        WhiteListConfigHandler handler = new WhiteListConfigHandler(otherFiles);
        LOG.log(Level.INFO, "Loading of whitelists:");
        InputStream is = getInputStream(CONFIG_FILE);
        LOG.log(Level.INFO, "   "+CONFIG_FILE);
        readSingleXML(saxParser, is, handler);
        for (String anotherFile : otherFiles) {
            LOG.log(Level.INFO, "   "+anotherFile);
            is = getInputStream(anotherFile);
            readSingleXML(saxParser, is, handler);
        }
        LOG.log(Level.INFO, "   Loading took "+((System.currentTimeMillis()-l))+
                " ms (in "+(otherFiles.size()+1)+" files)");
        LOG.log(Level.INFO, "   "+WhiteListClassHandler.invocableMethodCount+" invocable methods registered");
        LOG.log(Level.INFO, "   "+WhiteListClassHandler.overridableMethodCount+" overridable methods registered");
    }
    
    private static InputStream getInputStream(String fileName) {
        return WhiteListConfigReader.class.getResourceAsStream(CONFIG_ROOT+fileName);
    }
    
    private static void readSingleXML(SAXParser saxParser, InputStream inputStream, WhiteListConfigHandler handler) throws IOException, SAXException {
        try {
            SAXSource source = new SAXSource(new InputSource(inputStream));
            saxParser.parse(inputStream, handler);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

    }

}

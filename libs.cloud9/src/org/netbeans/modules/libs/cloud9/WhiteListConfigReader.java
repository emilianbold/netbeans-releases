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


import javax.xml.parsers.ParserConfigurationException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import org.netbeans.spi.java.source.WhiteListQueryImplementation.WhiteListImplementation;
import org.netbeans.spi.java.source.support.WhiteListImplementationBuilder;
import org.openide.util.Exceptions;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public final class WhiteListConfigReader {

    public final static String CONFIG_RESOURCE = "/oracle/hostedcode/common/whitelist/configuration/resource/whitelistconfiguration.io.xml";

    private static WhiteListImplementationBuilder builder;
    
    synchronized static WhiteListImplementationBuilder getBuilder() {
        if (builder == null) {
            builder = WhiteListImplementationBuilder.create();
        }
        return builder;
    }
    
    private static WhiteListImplementation reader;

    public synchronized static WhiteListImplementation getDefault() {
        if (reader == null) {
            try {
                
                // XXX: read XMLs in separate thread:
                
                
                readXMLs();
            } catch (ParserConfigurationException ex) {
                Exceptions.printStackTrace(ex);
            } catch (SAXException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            reader = getBuilder().build();
        }
        return reader;
    }
    
    public static void readXMLs() throws ParserConfigurationException, SAXException, IOException {
        InputStream inputStream = WhiteListConfigReader.class.getResourceAsStream(CONFIG_RESOURCE);

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);

        SAXParser saxParser = factory.newSAXParser();

        WhiteListConfigHandler handler = new WhiteListConfigHandler();

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

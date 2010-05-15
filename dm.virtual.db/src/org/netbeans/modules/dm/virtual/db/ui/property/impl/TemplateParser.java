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
package org.netbeans.modules.dm.virtual.db.ui.property.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.openide.util.NbBundle;

/**
 * @author Ritesh Adval
 */
public class TemplateParser extends DefaultHandler {

    private static Writer out;
    private TemplateFactory factory;

    public TemplateParser() {
    }

    public TemplateParser(InputStream stream, TemplateFactory fac) {
        this.factory = fac;
        init(stream);
    }

    @Override
    public void characters(char[] buf, int offset, int len) throws SAXException {
        String s = new String(buf, offset, len);
        showData(s);
    }
    // ===========================================================
    // Methods in SAX DocumentHandler
    // ===========================================================

    @Override
    public void endDocument() throws SAXException {
        try {
            newLine();
            out.flush();
        } catch (IOException e) {
            throw new SAXException(NbBundle.getMessage(TemplateParser.class, "MSG_IO_Error"), e);
        }
    }

    // SAX 2.0
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        showData("</" + qName + ">");
        if (factory != null) {
            factory.endElement(uri, localName, qName);
        }
    }

    @Override
    public void startDocument() throws SAXException {
        showData("<?xml version='1.0' encoding='UTF-8'?>");
        newLine();
    }

    // SAX 2.0
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
        showData("<" + qName);
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                showData(" ");
                showData(attrs.getQName(i) + "=\"" + attrs.getValue(i) + "\"");
            }
        }
        showData(">");
        if (factory != null) {
            factory.startElement(uri, localName, qName, attrs);
        }
    }

    private void init(InputStream stream) {
        // Use the default (non-validating) parser
        SAXParserFactory saxpFactory = SAXParserFactory.newInstance();
        try {
            // Set up output stream
            if (Boolean.getBoolean("org.netbeans.modules.dm.virtual.db.ui.property.impl.TemplateParser.showOutput")) {
                out = new OutputStreamWriter(System.out, "UTF8");
            } else {
                out = new StringWriter(512);
            }

            // Parse the input
            SAXParser saxParser = saxpFactory.newSAXParser();
            saxParser.parse(stream, this);
        } catch (Exception t) {
            t.printStackTrace();
        }
    }
    // ===========================================================
    // Helpers Methods
    // ===========================================================

    // Start a new line
    private void newLine() throws SAXException {
        String lineEnd = System.getProperty("line.separator");
        try {
            out.write(lineEnd);
        } catch (IOException e) {
            throw new SAXException(NbBundle.getMessage(TemplateParser.class, "MSG_IO_Error"), e);
        }
    }

    // Wrap I/O exceptions in SAX exceptions, to
    // suit handler signature requirements
    private void showData(String s) throws SAXException {
        try {
            out.write(s);
            out.flush();
        } catch (IOException e) {
            throw new SAXException(NbBundle.getMessage(TemplateParser.class, "MSG_IO_Error"), e);
        }
    }
}
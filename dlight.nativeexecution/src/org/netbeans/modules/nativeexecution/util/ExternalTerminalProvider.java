/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.util;

import org.netbeans.modules.nativeexecution.support.*;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Stack;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 */
public final class ExternalTerminalProvider {

    private static final java.util.logging.Logger log = Logger.getInstance();
    private static HashMap<String, TerminalProfile> hash =
            new HashMap<String, TerminalProfile>();


    static {
        init();
    }

    private ExternalTerminalProvider() {
    }

    public static ExternalTerminal getTerminal(String id) {
        TerminalProfile ti = hash.get(id);

        if (ti == null) {
            throw new IllegalArgumentException("Unsupported terminal type");
        }

        ExternalTerminal result = new ExternalTerminal(ti);

        return result;
    }

    private static void init() {
        FileObject folder = FileUtil.getConfigFile("NativeExecution/ExtTerminalSupport"); //NOI18N
        if (folder != null && folder.isFolder()) {
            FileObject[] files = folder.getChildren();
            for (FileObject file : files) {
                try {
                    readConfiguration(file.getInputStream());
                } catch (FileNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    private static void readConfiguration(InputStream inputStream) {
        final SAXParserFactory spf = SAXParserFactory.newInstance();

        XMLReader xmlReader = null;

        try {
            SAXParser saxParser = spf.newSAXParser();
            xmlReader = saxParser.getXMLReader();
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        SAXHandler handler = new SAXHandler();
        xmlReader.setContentHandler(handler);

        try {
            InputSource source = new InputSource(inputStream);
            xmlReader.parse(source);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static final class SAXHandler extends DefaultHandler {

        private Stack<Context> context = new Stack<Context>();
        private TerminalProfile info;
        private StringBuilder accumulator = new StringBuilder();
        private int version = 1;

        private SAXHandler() {
            this.info = new TerminalProfile();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            accumulator.setLength(0);

            if ("terminaldefinition".equals(qName)) {
                context.push(Context.root);

                String xmlns = attributes.getValue("xmlns"); // NOI18N
                if (xmlns != null) {
                    int lastSlash = xmlns.lastIndexOf('/'); // NOI18N
                    if (lastSlash >= 0 && (lastSlash + 1 < xmlns.length())) {
                        String versionStr = xmlns.substring(lastSlash + 1);
                        if (versionStr.length() > 0) {
                            try {
                                version = Integer.parseInt(versionStr);
                            } catch (NumberFormatException ex) {
                                // skip
                                log.fine("Incorrect version information:" + xmlns); // NOI18N
                            }
                        }
                    } else {
                        log.fine("Incorrect version information:" + xmlns); // NOI18N
                    }
                }
            } else {
                context.push(elementStarted(qName, attributes));
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            accumulator.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            Context cont = context.pop();

            if (cont != Context.root) {
                elementEnded(cont, accumulator.toString());
            } else {
                hash.put(info.getID(), info);
            }
        }

        public Context elementStarted(String name, Attributes attributes) {
            if ("terminal".equals(name)) {
                info.setID(attributes.getValue("id"));
                info.setSupportedPlatforms(attributes.getValue("platforms"));
                return Context.terminal;
            }

            if ("searchpaths".equals(name)) {
                return Context.searchpaths;
            }

            if (context.lastElement() == Context.searchpaths && "path".equals(name)) {
                return Context.searchpath;
            }

            if ("platforms".equals(name)) {
                // TODO
                return Context.terminaldefinition;
            }

            if ("command".equals(name)) {
                info.setCommand(attributes.getValue("stringvalue")); // NOI18N
                return Context.terminaldefinition;
            }

            if ("arguments".equals(name)) {
                return Context.arguments;
            }

            if (context.lastElement() == Context.arguments &&
                    "arg".equals(name)) {
                return Context.argument;
            }

            return Context.unknown;
        }

        private void elementEnded(Context context, String text) {
            switch (context) {
                case argument:
                    info.addArgument(text);
                    break;
                case searchpath:
                    info.addSearchPath(text);
                    break;
            }
        }
    }

    enum Context {

        unknown,
        root,
        terminaldefinition,
        terminal,
        searchpaths,
        searchpath,
        arguments,
        argument
    }
}

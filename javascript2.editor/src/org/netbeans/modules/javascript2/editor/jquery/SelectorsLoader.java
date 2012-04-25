/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.jquery;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.editor.model.DeclarationScope;
import org.netbeans.modules.javascript2.editor.model.Identifier;
import org.netbeans.modules.javascript2.editor.model.JsObject;
import org.netbeans.modules.javascript2.editor.model.impl.IdentifierImpl;
import org.netbeans.modules.javascript2.editor.model.impl.JsFunctionImpl;
import org.netbeans.modules.javascript2.editor.model.impl.JsObjectImpl;
import org.netbeans.modules.javascript2.editor.model.impl.TypeUsageImpl;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class loads all selectors from jquery-api.xml file.
 * 
 * @author Petr Pisl
 */
public class SelectorsLoader extends DefaultHandler {
    public static final String TYPE = "type";   //NOI18N
    public static final String NAME = "name";   //NOI18N
    public static final String SELECTOR = "selector";   //NOI18N
        
    private static final Logger LOGGER = Logger.getLogger(SelectorsLoader.class.getName());
    
    private SelectorsLoader() {
        
    }
    private static List<JQueryCodeCompletion.SelectorItem> result = new ArrayList<JQueryCodeCompletion.SelectorItem>();
    
    public static Collection<JQueryCodeCompletion.SelectorItem> getSelectors(File file) {
        result.clear();
        try {
            long start = System.currentTimeMillis();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            DefaultHandler handler = new SelectorsLoader();
            parser.parse(file, handler);
            long end = System.currentTimeMillis();
            LOGGER.log(Level.FINE, "Loading selectors from API file took {0}ms ",  (end - start)); //NOI18N
         
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ParserConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SAXException ex) {
            Exceptions.printStackTrace(ex);
        }
        return result;
    }
    
    public static String getDocumentation(File file, String selectorName) {
        DocumentationBuilder documentationBuilder = new DocumentationBuilder(file);
        return documentationBuilder.buildForSelector(selectorName);
    }

    public static void addToModel(File apiFile, JsObject jQuery) {
        JQueryModelBuilder propertiesBuilder = new JQueryModelBuilder(apiFile, jQuery);
        propertiesBuilder.addProperties(jQuery);
    }
    
    private boolean inSelector = false;
    
    private enum Tag {
        added, argument, desc, entry, longdesc, note, sample, signature, notinterested;
    }
    private String name;
    private String sample;
    
    private Tag inTag = Tag.notinterested;
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (inSelector){
          if (qName.equals(Tag.sample.name())){
              inTag = Tag.sample;
          }  
        } else if(qName.equals(Tag.entry.name())) {
            String type = attributes.getValue(TYPE); //NOI18N
            if (type.equals(SELECTOR)) {  //NOI18N
                inSelector = true;
                name = attributes.getValue(NAME); //NOI18N
            }
        }
        
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(inSelector && qName.equals(Tag.entry.name())) {
            inSelector = false;
            String template = null;
            if(sample.indexOf('(') > -1) {          //NOI18N
                template = name + "(${cursor})";    //NOI18N
                name = name + "()";                 //NOI18N
            }
            JQueryCodeCompletion.SelectorItem item = new JQueryCodeCompletion.SelectorItem(name, template);
            result.add(item);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        switch (inTag) {
            case sample:
                sample = new String(ch, start, length);
                inTag = Tag.notinterested;
                break;
        }
    }
    
    private static class Argument {
        String name;
        String type;
        String description;

        public Argument(String name, String type, String description) {
            this.name = name;
            this.type = type;
            this.description = description;
        }    
    }
    
    private static class DocumentationBuilder extends DefaultHandler {

        
        private static final String TABLE_STYLE= "style=\"border: 0px; width: 100%;\""; //NOI18N
        private static final String TD_STYLE = "style=\"text-aling:left; border-width: 0px;padding: 1px;padding:3px;\" ";  //NOI18N
        private static final String TD_STYLE_MAX_WIDTH = "style=\"text-aling:left; border-width: 0px;padding: 1px;padding:3px;width:80%;\" ";  //NOI18N
        
        private StringBuilder documentation;
        private boolean inSelector;
        private String selectorName;
        private File file;
        private List<Tag> tagPath;
        
        private String sample;
        private String description;
        private List<String> notes;
        private String exampleDesc;
        private String longDescription;
        private String fromVersion;
        private String argName;
        private String argType;
        private List<Argument> arguments;
        
        public DocumentationBuilder(File file) {
            inSelector = false;
            this.file = file;
            tagPath = new ArrayList<Tag>();
        }
        
        public String buildForSelector(String name) {
            documentation = new StringBuilder();
            try {
                long start = System.currentTimeMillis();
                selectorName = name;
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();
                parser.parse(file, this);
                long end = System.currentTimeMillis();
                LOGGER.log(Level.FINE, "Loading selectors from API file took {0}ms ", (end - start)); //NOI18N

            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ParserConfigurationException ex) {
                Exceptions.printStackTrace(ex);
            } catch (SAXException ex) {
                Exceptions.printStackTrace(ex);
            }
            return documentation.toString();
        }
        
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (inSelector) {

                Tag current;
                try {
                    current = Tag.valueOf(qName);
                } catch (IllegalArgumentException iae) {
                    current = Tag.notinterested;
                }
                tagPath.add(0, current);
                if (current == Tag.argument) {
                    argName = attributes.getValue(NAME);
                    argType = attributes.getValue(TYPE);
                }
            } else if (qName.equals(Tag.entry.name())) {
                String type = attributes.getValue(TYPE);
                if (type.equals(SELECTOR)) {
                    String name = attributes.getValue(NAME);
                    if (name.equals(selectorName)) {
                        inSelector = true;
                        description = "";
                        longDescription = "";
                        sample = "";
                        fromVersion = longDescription = "";
                        notes = new ArrayList<String>();
                        arguments = new ArrayList<Argument>();
                    }
                }
            }

        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (inSelector) {
                if (qName.equals(Tag.entry.name())) {
                    inSelector = false;
                    createHtmlDoc();
                } else if (tagPath.size() > 0) {
                    tagPath.remove(0);

                }
            }
        }

        private void createHtmlDoc() {
            documentation.append("<html>\n");
            documentation.append("<head>\n");

            documentation.append("</head>\n");
            documentation.append("<body style='font-family: Arial; font-size: 11px'>\n");
            documentation.append("<table width='100%'><tr>\n");
            documentation.append("<td style='font-weight: bold; font-size: large'>jQuery('").append(sample).append("')</td>\n");
            documentation.append("<td style='vertical-align: bottom; text-align: right; font-weight: bold;font-size: small'>version added: ").append(fromVersion).append("</td>\n");
            documentation.append("</tr></table>\n");
            documentation.append("<hr/>\n");
            documentation.append("<p style='font-size: 12px'>\n");
            documentation.append("<span style='font-weight: bold'>").append(NbBundle.getMessage(SelectorsLoader.class, "LBL_Description")).append(" </span>\n");
            documentation.append("<span>").append(description).append("</span>\n");
            documentation.append("</p> \n");
            if (!arguments.isEmpty()) {
                documentation.append("<p style='font-size: 12px'>\n");
                documentation.append("<span style='font-weight: bold'>").append(NbBundle.getMessage(SelectorsLoader.class, "LBL_Arguments")).append("</span>\n");
                documentation.append("<table cellspacing=0 " + TABLE_STYLE + ">\n");
                for (Argument arg : arguments) {
                    documentation.append(String.format("<tr><td>&nbsp;</td><td valign=\"top\" %s><nobr>%s</nobr></td><td valign=\"top\" %s><nobr><b>%s</b></nobr></td><td valign=\"top\" %s>%s</td></tr>\n", //NOI18N
                            TD_STYLE, arg.type, TD_STYLE, arg.name, TD_STYLE_MAX_WIDTH, arg.description));
                }
                documentation.append("</table>\n"); //NOI18N
                documentation.append("</p> \n");
            }
            if (!longDescription.isEmpty()) {
                documentation.append("<div style='font-size: small'>").append(longDescription).append("</div>\n");
            }
            if (!notes.isEmpty()) {
                documentation.append("<p style='font-size: 12px'>\n");
                documentation.append("<span style='font-weight: bold'>Additional Notes: </span>\n");
                documentation.append("<ul>\n");
                for (String note : notes) {
                    documentation.append("<li>").append(note).append("</li>\n");
                }
                documentation.append("</ul>\n");
                documentation.append("</p> \n");

            }
            documentation.append("</body>\n");
            documentation.append("</html>\n");
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (inSelector && tagPath.size() > 0) {
                switch (tagPath.get(0)) {
                    case added:
                        fromVersion = new String(ch, start, length);
                        break;
                    case desc:
                        if (tagPath.size() == 1) {
                            description = new String(ch, start, length);
                        } else {
                            if (tagPath.get(1) == Tag.argument) {
                                arguments.add(new Argument(argName, argType, new String(ch, start, length)));
                            }
                        }
                        break;
                    case longdesc:
                        longDescription = new String(ch, start, length);
                        break;
                    case note:
                        notes.add(new String(ch, start, length));
                        break;
                    case sample:
                        sample = new String(ch, start, length);
                        break;
                }
            }
        }
    }
    
    private static class JQueryModelBuilder extends DefaultHandler {
        private final File file;
        private final JsObject jQuery;
        private final List<Tag> tagPath;
        
        
        boolean isMethod;
        boolean isProperty;
        private String name;
        private String returns;
        private String added;
        // Todo the parametrs has to be js objects to assign type
        private final List<Identifier> params;
        
        public JQueryModelBuilder(final File file, final JsObject jQuery) {
            this.file = file;
            this.jQuery = jQuery;
            this.tagPath = new ArrayList();
            this.params  = new ArrayList<Identifier>();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            Tag current;
            try {
                current = Tag.valueOf(qName);
            } catch (IllegalArgumentException iae) {
                current = Tag.notinterested;
            }
            tagPath.add(0, current);

            switch (current) {
                case entry:
                    String type = attributes.getValue(TYPE);
                    if (type.equals("method")) {
                        isMethod = true;
                    } else if (type.equals("property")) {
                        isProperty = true;
                    }
                    if (isMethod || isProperty) {
                        name = attributes.getValue(NAME);
                        returns = attributes.getValue("return");
//                        if (name.indexOf(".") == -1) {
//                            if (type.equals("method")) {
//                                lastFunction = new JsFunctionImpl(null, jQuery, new IdentifierImpl(name, OffsetRange.NONE), Collections.<Identifier>emptyList(), OffsetRange.NONE);
//                                lastFunction.addReturnType(new TypeUsageImpl(returns, -1, true));
//                                jQuery.addProperty(name, lastFunction);
//                            }
//                        }
                    }
                    break;
                case argument : 
                    if (isMethod) {
                        String paramName = attributes.getValue(NAME);
                        String paramType = attributes.getValue(TYPE);
                        boolean isOptional = Boolean.parseBoolean(attributes.getValue("optional"));
                        IdentifierImpl param = new IdentifierImpl(paramName, OffsetRange.NONE);
                        params.add(param);
                    }
                    
                    break;
            }    
        }
        
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (tagPath.size() > 0) {
                Tag current = tagPath.remove(0);
                if (isMethod){
                    switch (current) {
                        case signature:
                            if(name.indexOf('.') == -1) {
                                JsFunctionImpl function = new JsFunctionImpl((DeclarationScope) jQuery, jQuery, new IdentifierImpl(name, OffsetRange.NONE), params, OffsetRange.NONE);
                                function.addReturnType(new TypeUsageImpl(returns, -1, true));
                                jQuery.addProperty(name + "#" + added, function);
                                System.out.println(name + "#" + added);
                                params.clear();
                            }
                            break;
                        case entry:
                            isMethod = false;
                            params.clear();
                            ;
                            break;
                    }
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(tagPath.get(0)) {
                case added:
                    if (tagPath.size() > 1 && tagPath.get(1) == Tag.signature) {
                        added = new String(ch, start, length);
                    }
                    break;
            }
        }
        
        
        private void addProperties(JsObject global) {
            try {
                long start = System.currentTimeMillis();
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();
                parser.parse(file, this);
                long end = System.currentTimeMillis();
                LOGGER.log(Level.FINE, "Collecting properties from jQuery API file took {0}ms ", (end - start)); //NOI18N

            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ParserConfigurationException ex) {
                Exceptions.printStackTrace(ex);
            } catch (SAXException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

}

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
package org.netbeans.modules.javafx2.editor.completion.model;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.modules.javafx2.editor.completion.impl.ContentLocator;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import static org.netbeans.modules.javafx2.editor.completion.model.FxXmlSymbols.*;
import static org.netbeans.modules.javafx2.editor.JavaFXEditorUtils.FXML_FX_NAMESPACE;
import org.netbeans.modules.javafx2.editor.completion.impl.ErrorMark;
import org.openide.util.NbBundle;

import static org.netbeans.modules.javafx2.editor.completion.model.Bundle.*;
import org.netbeans.modules.javafx2.editor.completion.model.FxNode.Kind;
import org.openide.util.Utilities;

/**
 *
 * @author sdedic
 */
public class FxModelBuilder implements ContentHandler, ContentLocator.Receiver {
    /**
     * URL of the current source.
     */
    private URL sourceURL;
    
    /**
     * Stack of Elements, as they are processed.
     */
    private Deque<FxNode> nodeStack = new LinkedList<FxNode>();
    
    /**
     * Supplemental interface, to get offsets & other info from the parser
     */
    private ContentLocator  contentLocator;
   
    /**
     * The created model
     */
    private FxModel     fxModel = new FxModel();
    
    /**
     * The current parsed bean instance.
     */
    private FxInstance<FxInstance>  current;

    /**
     * Collected import declarations.
     */
    private List<ImportDecl>  imports = new ArrayList<ImportDecl>();
    
    /**
     * List of errors found during parsing
     */
    private List<ErrorMark> errors = new ArrayList<ErrorMark>();
    
    private Map<String, Set<String>>    packageContents = new HashMap<String, Set<String>>();
    private Map<String, String>         importedClasses = new HashMap<String, String>();
    
    
    public void setBaseURL(URL sourceURL) {
        this.sourceURL = sourceURL;
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        // no op, we don't need SAX locator.
    }

    @Override
    public void startDocument() throws SAXException {
        fxModel.attach(createElementInfo());
        nodeStack.add(fxModel);
    }
    
    private NodeInfo createElementInfo() {
        return new NodeInfo(contentLocator.getElementOffset()).
                startContent(contentLocator.getEndOffset());
    }

    @Override
    public void endDocument() throws SAXException {
        int end = contentLocator.getElementOffset();
        fxModel.i().endContent(end).endsAt(end);
        fxModel.setImports(imports);
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
    }
    
    @NbBundle.Messages({
        "# {0} - tag name",
        "ERR_tagNotJavaIdentifier=Invalid class name: {0}",
    })
    private void handleClassTag(String localName, Attributes atts) {
        String fxValueContent = null;
        String fxFactoryContent = null;
        String fxId = null;
        int off = contentLocator.getElementOffset() + 1; // the <
        
        for (int i = 0; i < atts.getLength(); i++) {
            String uri = atts.getURI(i);
            if (!FXML_FX_NAMESPACE.equals(uri)) {
                // no special attribute
                continue;
            }
            String name = atts.getLocalName(i);
            if (FX_VALUE.equals(name)) {
                fxValueContent = atts.getValue(i);
            } else if (FX_FACTORY.equals(name)) {
                fxFactoryContent = atts.getValue(i);
            } else if (FX_ID.equals(name)) {
                fxId = atts.getValue(i);
            } else {
                addError(
                    new ErrorMark(
                        contentLocator.getAttributeOffset(atts.getQName(i)), atts.getQName(i).length(),
                        "invalid-property-reserved-name",
                        ERR_invalidReservedPropertyName(name),
                        name
                    )
                );
            }
        }
        
        // first we must check how this class tag is created. 
        FxNewInstance instance = new FxNewInstance(localName, null);
        
        instance = instance.fromValue(fxValueContent).
                usingFactory(fxFactoryContent).withId(fxId);
        
        instance.attach(createElementInfo());
        
        if (!FxXmlSymbols.isQualifiedIdentifier(localName)) {
            // not a java identifier, error
            addError(
                new ErrorMark(
                    off, localName.length(), 
                    "invalid-class-name", 
                    ERR_tagNotJavaIdentifier(localName),
                    localName
            ));
            NodeInfo ni = createElementInfo();
            instance.attach(ni);
            attachChildNode(instance);
            nodeStack.push(instance);
            return;
        }
        
        attachInstance(instance, true);
        processInstanceAttributes(atts);
    }
    
    private static final String EVENT_HANDLER_PREFIX = "on"; // NOI18N
    private static final int EVENT_HANDLER_PREFIX_LEN = 2;
    
    private void processEventHandlerAttribute(String event, String content, int off) {
        EventHandler eh = new EventHandler(event);
        eh.setContent(content);
        
        NodeInfo ni = new NodeInfo(off);
        eh.attach(ni);
        attachChildNode(eh);
        
        current.addEvent(eh);
    }
    
    @NbBundle.Messages({
        "# {0} - attribute name",
        "ERR_lowercasePropertyName=Invalid property name: {0}. Property name, or the last component of a static property name must start with lowercase.",
        "# {0} - attribute name",
        "ERR_invalidReservedPropertyName=Unknown name in FXML reserved namespace: {0}"
    })
    private void processInstanceAttributes(Attributes atts) {
        for (int i = 0; i < atts.getLength(); i++) {
            int off = contentLocator.getAttributeOffset(atts.getQName(i));
            String uri = atts.getURI(i);
            String name = atts.getLocalName(i);
            String qname = atts.getQName(i);
            
            PropertySetter ps = null;
            
            if (qname.startsWith("xmlns")) { // NOI18N
                continue;
            }
            
            if (FXML_FX_NAMESPACE.equals(uri)) {
                if (FX_VALUE.equals(name) || FX_FACTORY.contains(name)) {
                    continue;
                } else {
                    // FIXME - error: unexpected fx: attribute
                }
            }
            
            // if the name begins with "on", it's an event handler.
            if (name.startsWith(EVENT_HANDLER_PREFIX) && name.length() > EVENT_HANDLER_PREFIX_LEN) {
                String en = Character.toLowerCase(name.charAt(EVENT_HANDLER_PREFIX_LEN)) +
                        name.substring(EVENT_HANDLER_PREFIX_LEN + 1);
                processEventHandlerAttribute(en, atts.getValue(i), off);
                continue;
            }
            
            int stProp = FxXmlSymbols.findStaticProperty(name);
            if (stProp == -2) {
                // report error, not a well formed property name.
                addError(
                    new ErrorMark(off, qname.length(),
                        "invalid-property-name",
                        ERR_lowercasePropertyName(name),
                        name
                    )
                );
                ps = new PropertySetter(name);
            } else if (stProp == -1) {
                // this is a normal property
                ps = new PropertySetter(name);
                current.addProperty(ps);
            } else {
                // it is a static property
                StaticProperty s = new StaticProperty(
                        name.substring(0, stProp), 
                        name.substring(stProp + 1)
                );
                current.addStaticProperty(s);
                ps = s;
            }
            if (ps != null) {
                NodeInfo ni = new NodeInfo(off);
                ps.attach(ni);
                ps.addContent(atts.getValue(i));
                attachChildNode(ps);
            }
        }
    }
    
    private int definitions;
    private boolean definitionsFound;
    
    @NbBundle.Messages({
        "# {0} - tag name",
        "ERR_invalidFxElement=Unknown element in fx: namespace: {0}"
    })
    private void handleFxmlElement(String localName, Attributes atts) {
        if (FX_DEFINITIONS.equals(localName)) {
            definitions++;
            
            if (definitionsFound) {
                // error, defs cannot be nested or used more than once. Ignore.
                
            }
        } else if (FX_COPY.equals(localName)) {
            handleFxCopy(atts);
        } else if (FX_REFERENCE.equals(localName)) {
            handleFxReference(atts);
        } else {
            // error, invalid fx: element
            FxNode n = new Dummy(localName);
            NodeInfo ni = createElementInfo();
            n.attach(ni);
            attachChildNode(n);
            nodeStack.push(n);
            addError("invalid-fx-element", ERR_invalidFxElement(localName), localName);
        }
    }
    
    private void handleFxCopy(Attributes atts) {
        handleFxReference(atts, true);        
    }
    
    private void handleFxReference(Attributes atts) {
        handleFxReference(atts, false);
    }
    
    @NbBundle.Messages({
        "# {0} - attribute local name",
        "ERR_unexpectedReferenceAttribute=Unexpected attribute in fx:reference or fx:copy: {0}",
        "ERR_missingReferenceSource=Missing 'source' attribute in fx:reference or fx:copy"
    })
    private void handleFxReference(Attributes atts, boolean copy) {
        String refId = null;
        String id = null;
        
        for (int i = 0; i < atts.getLength(); i++) {
            String ns = atts.getURI(i);
            String name = atts.getLocalName(i);
            if (!FXML_FX_NAMESPACE.equals(ns)) {
                if (!copy) {
                    // error, references do not support normal attributes
                }
            } else {
                if (FX_ID.equals(name) && id == null) {
                    id = atts.getValue(i);
                } else if (FX_ATTR_REFERENCE_SOURCE.equals(name) && refId == null) {
                    refId = atts.getValue(i);
                } else {
                    // error, unexpected attribute
                    int off = contentLocator.getAttributeOffset(atts.getQName(i));
                    addError(
                        new ErrorMark(off, atts.getQName(i).length(),
                            "invalid-reference-attribute",
                            ERR_unexpectedReferenceAttribute(name),
                            name
                        )
                    );
                }
            }
        }
        
        if (refId == null) {
            // error, no source attribute found
            addError(
                    "missing-reference-source",
                    ERR_missingReferenceSource()
            );
        }
        FxObjectBase ref = copy ? new FxInstanceCopy(refId) : new FxReference(refId);
        
        NodeInfo ni = createElementInfo();
        ref.attach(ni);
        
        // is the parent elemetn valid ?
        attachInstance(ref, refId != null);
        
        if (copy) {
            // modifications allowed on fx:copied beans
            processInstanceAttributes(atts);
        }
    }
    
    private void pushInstance(FxObjectBase instance) {
        nodeStack.push(instance);
        if (instance.getKind() == Kind.Instance) {
            current = (FxInstance)instance;
        } else {
            current = null;
        }
    }
    
    /**
     * Checks that the instance is allowed in this context. May even create e.g.
     * default property setter etc. Will return true, if the instance can be attached to the parent.
     */
    @NbBundle.Messages({
        "# {0} - tag name",
        "ERR_moreRootElements=Duplicate root element: {0}",
        "ERR_instanceInMapProperty=Cannot add instances directly to readonly Map",
        "# {0} - parent tag name",
        "ERR_parentNotSupportInstance=Instances cannot be added to the parent {0}"
    })
    private void attachInstance(FxObjectBase instance, boolean valid) {
        String localName = instance.getTagName();
        int off = contentLocator.getElementOffset() + 1;
        
        // check the parent, whether it is appropriate to host such a node:
        FxNode parent = nodeStack.peek();
        
        if (parent.getKind() == Kind.Instance) {
            // pretend we have a default property
            PropertySetter s = new PropertySetter(null).asImplicitDefault();
            NodeInfo ni = new NodeInfo(contentLocator.getElementOffset());
            s.attach(ni);
            attachChildNode(s);
            current.addProperty(s);
            nodeStack.push(s);
            parent = s;
        }
        
        if (parent.getKind() == Kind.Source) {
            FxModel mdl = (FxModel)parent;
            FxObjectBase old = mdl.getRootComponent();
            if (old != null) {
                addError(new ErrorMark(
                    off, contentLocator.getEndOffset() - off,
                    "duplicate-root",
                    ERR_moreRootElements(localName),
                    localName
                ));
            }
            mdl.setRootComponent(instance);
            attachChildNode(instance);
            pushInstance(instance);
        } else if (parent.getKind() == Kind.Property) {
            attachChildNode(instance);
            if (parent instanceof MapProperty) {
                addError(new ErrorMark(
                    off, contentLocator.getEndOffset() - off,
                    "instance-in-map-property",
                    ERR_instanceInMapProperty(),
                    localName
                ));
            } else {
                if (valid) {
                    PropertySetter  setter = (PropertySetter)parent;
                    setter.addValue(instance);
                }
                pushInstance(instance);
            }
        } else {
            if (parent.getKind() != Kind.Error) {
                addError(new ErrorMark(
                    off, contentLocator.getEndOffset() - off,
                    "parent-not-support-instance",
                    ERR_instanceInMapProperty()
                ));
            }
            NodeInfo ni = new NodeInfo(contentLocator.getElementOffset());
            instance.attach(ni);
            attachChildNode(instance);
            pushInstance(instance);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        System.err.println("startElement: " + localName);
        if (FXML_FX_NAMESPACE.equals(uri)) {
            handleFxmlElement(localName, atts);
            return;
        }
        // non-fx namespace, should be either an instance, or a property
        if (FxXmlSymbols.isClassTagName(localName)) {
            handleClassTag(localName, atts);
        } else {
            handlePropertyTag(localName, atts);
        }
    }
    
    private void handleStaticProperty(String className, 
            String propName, Attributes atts, NodeInfo ni) {
        // FIXME - check that attributes are empty
        StaticProperty s = new StaticProperty(
                className,
                propName
        );
        attachProperty(s, ni);
    }
    
    /**
     * Processes instance (non-static) property. As per examples in Guides, instance
     * property element must NOT have any attributes; otherwise it corresponds to
     * an readonly Map element, and the property must be of the Map type.
     * 
     * @param propName
     * @param atts As
     */
    @NbBundle.Messages({
        "# {0} - attribute name",
        "ERR_propertyElementNamespacedAttribute=Property elements may not contain attributes with namespace: {0}"
    })
    private void handleSimpleProperty(String propName, Attributes atts, NodeInfo ni) {
        PropertyValue p;
        
        // no relevant attributes to use, real simple property then
        p = new PropertySetter(propName);
        attachProperty(p, ni);
    }
    
    private void attachProperty(PropertyValue p, NodeInfo ni) {
        p.attach(ni);
        attachChildNode(p);
        current.addProperty(p);
        nodeStack.push(p);
    }
    
    private void handleMapProperty(String propName, Attributes atts, NodeInfo ni) {
        MapProperty mp = new MapProperty(propName);

        for (int i = 0; i < atts.getLength(); i++) {
            String uri = atts.getURI(i);
            if (uri != null) {
                continue;
            }
            mp.addValue(atts.getLocalName(i), atts.getValue(i));
        }
        attachProperty(mp, ni);
    }
    
    @NbBundle.Messages({
        "# {0} - tag name",
        "ERR_invalidPropertyName=Invalid property name: {0}"
    })
    private void handlePropertyTag(String propName, Attributes atts) {
        NodeInfo ni = createElementInfo();
        
        int errorAttrs = 0;
        for (int i = 0; i < atts.getLength(); i++) {
            String uri = atts.getURI(i);
            if (uri != null) {
                String qn = atts.getQName(i);
                errorAttrs++;
                addError(
                    new ErrorMark(
                        contentLocator.getAttributeOffset(qn),
                        qn.length(),
                        "property-namespaced-attribute",
                        ERR_propertyElementNamespacedAttribute(qn),
                        qn
                    )
                );
            }
        }
        
        int stProp = FxXmlSymbols.findStaticProperty(propName);
        switch (stProp) {
            case -1:
                // simple property
                if (!Utilities.isJavaIdentifier(propName)) {
                    addError(new ErrorMark(
                        (int)ni.getStart(), (int)ni.getEnd(),
                        "invalid-property-name",
                        ERR_invalidPropertyName(propName),
                        propName
                    ));
                }
                if (errorAttrs == atts.getLength()) {
                    handleSimpleProperty(propName, atts, ni);
                } else {
                    handleMapProperty(propName, atts, ni);
                }
                break;
                
            case -2:
                // broken name, but must create a node
                PropertySetter ps = new PropertySetter(propName);
                ps.attach(ni);
                // do not add the property to the parent, it's broken beyond repair
                attachChildNode(ps);
                addError(new ErrorMark(
                    (int)ni.getStart(), (int)ni.getEnd(),
                    "invalid-property-name",
                    ERR_invalidPropertyName(propName),
                    propName
                ));
                break;
                
            default:
                // static property, just ignore for now
                handleStaticProperty(propName.substring(0, stProp), 
                        propName.substring(stProp + 1), atts, ni);
                break;
        }
        
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        System.err.println("endElement: " + localName);
        FxNode node = nodeStack.pop();
        node.i().endsAt(contentLocator.getEndOffset()).endContent(contentLocator.getElementOffset());
        if (node instanceof PropertySetter) {
            PropertySetter s = (PropertySetter)node;
            if (s.isImplicit()) {
                // actually the outer element ends
                node = nodeStack.pop();
                // copy the offset information
                node.i().endsAt(contentLocator.getEndOffset()).endContent(contentLocator.getElementOffset());
            }
        }
        String tn = ((FxElement)node).getTagName();
        if (!tn.equals(localName)) {
            throw new IllegalStateException();
        }
        if (node.getKind() == Kind.Instance) {
            current = (FxInstance)node;
        }
        
        // special hack for parent nodes, which are implicit property setters:
        FxNode parentNode = nodeStack.peek();
        if (parentNode instanceof PropertySetter) {
            PropertySetter ps = (PropertySetter)parentNode;
            if (ps.isImplicit() && ps.getContent() == null) {
                ps.i().endsAt(contentLocator.getEndOffset()).endContent(contentLocator.getEndOffset());
            }
        }
    }

    @NbBundle.Messages({
        "ERR_unexpectedCharacters=Unexpected character content"
    })
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        FxNode node = nodeStack.peek();
        switch (node.getKind()) {
            case Instance:
                handleInstanceContent(ch, start, length);
                break;
            case Property:
                handlePropertyContent(ch, start, length);
                break;
                
            default:
                addError(new ErrorMark(
                    contentLocator.getElementOffset(),
                    length,
                    "unexpected-characters",
                    ERR_unexpectedCharacters()
                ));
        }
    }
    
    private void handleInstanceContent(char[] chars, int start, int len) {
        // find among properties as setter, which is marked as implicit. If there's none, create one.
        PropertySetter defaultSetter = null;
        
        for (PropertyValue p : current.getProperties()) {
            if (p instanceof PropertySetter) {
                PropertySetter ps = (PropertySetter)p;
                if (ps.isImplicit()) {
                    defaultSetter = ps;
                }
            }
        }
        
        if (defaultSetter == null) {
            defaultSetter = new PropertySetter(null).asImplicitDefault();
            NodeInfo ni = new NodeInfo(contentLocator.getElementOffset());
            attachProperty(defaultSetter, ni);
        }
        defaultSetter.addContent(String.copyValueOf(chars, start, len));
        defaultSetter.i().endsAt(contentLocator.getEndOffset());
    }
    
    private ErrorMark addError(String errCode, String message, Object... params) {
        int offs = contentLocator.getElementOffset();
        ErrorMark m = new ErrorMark(
            offs,
            contentLocator.getEndOffset() - offs,
            errCode,
            message, 
            params
        );
        addError(m);
        return m;
    }

    private void handlePropertyContent(char[] chars, int start, int len) {
        FxNode node = nodeStack.peek();
        if (!(node instanceof PropertySetter)) {
            addError(
                "unexpected-characters", 
                ERR_unexpectedCharacters()
            );
            return;
        }
        PropertySetter ps = (PropertySetter)node;
        ps.addContent(String.copyValueOf(chars, start, len));
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        // check whether the current node supports content
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        addElementErrors();
        if (FX_IMPORT.equals(target)) {
            handleFxImport(data);
        } else if (FX_LANGUAGE.equals(target)) {
            handleFxLanguage(data);
        } else {
            handleErrorInstruction(target, data);
        }
    }
    
    private NodeInfo createFullNodeInfo() {
        return new NodeInfo(contentLocator.getElementOffset()).
                endsAt(contentLocator.getEndOffset());
    }
    
    /**
     * Processes "import" PI. Checks syntax of the identifier
     * @param data 
     */
    @NbBundle.Messages({
        "ERR_importNotJavaIdentifier=Imported symbol must be a class or package name.",
        "ERR_importInsideElement=Imports must be at top level, not nested in elements",
        "ERR_importFollowsRoot=Import must not follow the root element"
    })
    private void handleFxImport(String data) {
        if (data.endsWith("?")) {
            // recovery from unterminated ?> -- the lexer will report ? as part of PI data.
            data = data.substring(0, data.length() -1);
        }
        int lastDot = data.lastIndexOf('.');
        boolean starImport = false;
        
        if (lastDot != -1 && lastDot < data.length() - 1) {
            if (FX_IMPORT_STAR.equals(data.substring(lastDot + 1))) {
                starImport = true;
                data = data.substring(0, lastDot);
            }
        }
        ImportDecl decl = new ImportDecl(data, starImport);
        decl.attach(createFullNodeInfo());
        attachChildNode(decl);
        
        if (!FxXmlSymbols.isQualifiedIdentifier(data)) {
            addError(
                new ErrorMark(contentLocator.getAttributeOffset(ContentLocator.ATTRIBUTE_DATA), data.length(), 
                    "import-not-java-identifier",
                    ERR_importNotJavaIdentifier(), data)
            );
            return;
        }
        
        // check that ?import is at top level, and does not follow a root element:
        if (!isTopLevel()) {
            int o = contentLocator.getElementOffset();
            addError(
                new ErrorMark(o, contentLocator.getEndOffset() - o, 
                    "import-inside-element",
                    ERR_importInsideElement())
            );
            return;
        }
        
        imports.add(decl);
    }
    
    /**
     * Processes ?include directive
     * 
     * @param include 
     */
    @NbBundle.Messages({
        "ERR_missingIncludeName=Missing include name"
    })
    void handleFxInclude(String include) {
        if (include == null) {
            // must be some text, otherwise = error
            addError(
                new ErrorMark(contentLocator.getAttributeOffset(ContentLocator.ATTRIBUTE_TARGET), FxXmlSymbols.FX_INCLUDE.length(),
                    "missing-included-name",
                    ERR_missingIncludeName())
            );
            return;
        }
    }
    
    @NbBundle.Messages({
        "ERR_missingLanguageName=Language name is missing",
        "ERR_duplicateLanguageDeclaration=Language is already declared",
        "ERR_languageNotTopLevel=Language declaration must precede all elements"
    })
    void handleFxLanguage(String language) {
        if (language == null) {
            addError(
                new ErrorMark(contentLocator.getAttributeOffset(ContentLocator.ATTRIBUTE_TARGET), FxXmlSymbols.FX_LANGUAGE.length(),
                "missing-language-name",
                ERR_missingLanguageName())
            );
            return;
        }
        LanguageDecl decl = new LanguageDecl(language);
        decl.attach(createFullNodeInfo());
        attachChildNode(decl);
        
        int off = contentLocator.getElementOffset();
        
        if (fxModel.getLanguage() != null) {
            // error, language can be specified only once:
            addError(new ErrorMark(
                off, contentLocator.getEndOffset() - off,
                "duplicate-language",
                ERR_duplicateLanguageDeclaration(),
                fxModel.getLanguage()
            ));
            return;
        }
        if (!isTopLevel()) {
            addError(
                new ErrorMark(
                    off, contentLocator.getEndOffset() - off,
                    "language-not-toplevel",
                    ERR_languageNotTopLevel()
                )
            );
            return;
        }
        
        fxModel.setLanguage(decl);
    }
    
    private boolean isTopLevel() {
        return nodeStack.peek() == fxModel;
    }
    
    void addElementErrors() {
        this.errors.addAll(contentLocator.getErrors());
    }
    
    void addError(ErrorMark mark) {
        this.errors.add(mark);
    }
    
    void attachChildNode(FxNode node) {
        FxNode top = nodeStack.peek();
        top.i().addChild(node);
    }
    
    @NbBundle.Messages({
        "# {0} - PI target",
        "ERR_invalidProcessingInstruction=Invalid processing instruction: {0}. Expected 'import', 'include' or 'language'",
        "ERR_missingProcessingInstruction=Missing processing intruction."
    })
    private void handleErrorInstruction(String target, String data) {
        int start = contentLocator.getElementOffset();
        int offset = -1;
        int piOffset = -1;
        
        TokenSequence<XMLTokenId> seq = contentLocator.getTokenSequence();
        
        // lex up to the invalid target:
        seq.move(start);
        boolean found = false;
        while (!found && seq.moveNext()) {
            Token<XMLTokenId> t = seq.token();
            switch (t.id()) {
                case PI_START:
                    piOffset = offset;
                    if (target == null) {
                        found = true;
                    }
                case WS:
                    break;
                    
                default:
                case PI_TARGET:
                    offset = seq.offset();
                    found = true;
                    break;
            }
        }
        ErrorMark mark;
        
        if (target != null) {
            mark = new ErrorMark(offset, seq.token().length(), 
                    "invalid-processing-instruction", 
                    ERR_invalidProcessingInstruction(target),
                    target
            );
        } else {
            mark = new ErrorMark(piOffset, seq.token().length(), 
                    "missing-processing-instruction",
                    ERR_missingProcessingInstruction()
            );
        }
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
    }

    @Override
    public void setContentLocator(ContentLocator l) {
        this.contentLocator = l;
    }
    
    FxModel getModel() {
        return fxModel;
    }
    
    List<ErrorMark> getErrors() {
        return errors;
    }
}

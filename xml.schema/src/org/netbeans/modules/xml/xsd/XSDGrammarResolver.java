/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.xml.xsd;

import java.util.*;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.EntityResolver;


import org.openide.filesystems.FileObject;
import org.netbeans.modules.xml.api.model.*;
import org.netbeans.api.xml.services.UserCatalog;
import org.w3c.dom.Attr;

/**
 * Resolves XSDGrammars and Namespaces
 * @author  Ales Novak
 */
class XSDGrammarResolver implements GrammarQuery {
    
    /** Map<String, Namespace> find ns for a prefix*/
    private final Map prefix2Namespace;
    /** Map<String,Namespace> find ns for na URI*/
    private final Map uri2Namespace;
    /** Default Namespace of top levele Element */
    private Namespace defaultNamespace;
    
    private Node target;
    private FileObject document;
    
    /** Creates new XSDGrammar */
    private XSDGrammarResolver() {
        this.uri2Namespace = new HashMap();
        this.prefix2Namespace = new HashMap();
        this.defaultNamespace = null;
        this.target = null;
        this.document = null;
    }
    
    /** Factory method */
    public static XSDGrammarResolver createResolver(org.netbeans.modules.xml.api.model.GrammarEnvironment ctx) {
        XSDGrammarResolver ret = null;
        
        Enumeration en = ctx.getDocumentChildren();
        while (en.hasMoreElements()) {
            Node next = (Node) en.nextElement();
            // resolve top level
            if (next.getNodeType() == next.ELEMENT_NODE) {
                org.w3c.dom.Element element = (org.w3c.dom.Element) next;
                org.w3c.dom.NamedNodeMap atts = element.getAttributes();
                String eprefix = Namespace.getPrefix(element.getNodeName());
                
                // process namespaces
                for (int i = 0; i < atts.getLength(); i++) {
                    Node attribute = atts.item(i);
                    String name = attribute.getNodeName();
                    if (name.startsWith(Namespace.XMLNS_ATTR)) {
                        String uri = attribute.getNodeValue();
                        String prefix = Namespace.getSufix(name);
                        Namespace ns = new Namespace(uri, prefix);
                        if (ret == null) {
                            ret = new XSDGrammarResolver();
                        }
                        ret.addNamespace(ns);
                        
                        if (prefix == null || prefix.equals(eprefix)) {
                            ret.defaultNamespace = ns;
                        }
                    }
                }
                
                if (ret == null) {
                    continue;
                }
                
                assert ret.defaultNamespace != null;
                
                // find location of schema
                ret.resolveSchemaLocation(element);

                if (ret.defaultNamespace.getSchemaLocation() == null) {
                    // bail out
                    org.openide.ErrorManager.getDefault().log(org.openide.ErrorManager.WARNING, "SCHEMA is null: " + element.getLocalName());
                    // not necessarily a bad thing - namespace URI should suffice
                }
                
                ret.setTarget(next);
                ret.setDocument(ctx.getFileObject());
                System.err.println("create resolver - success");
                return ret;
            } // if Element Node
        } // while
        
        return null;
    }
    
    /** sets location of schema into default namespace */
    private void resolveSchemaLocation(org.w3c.dom.Element element) {
        // find location of schema
        Namespace ns = findNamespaceByURI(Namespace.XSI_NAMESPACE_URI);
        String prefix = ns.getPrefix().concat(":");
        String schema = null;
        Attr attr = element.getAttributeNode(prefix.concat(Namespace.XSI_LOCATION));
        if (attr != null) {
            schema = attr.getValue();
        } else {
            schema = element.getAttribute(prefix.concat(Namespace.XSI_NO_NAMESPACE_LOCATION));
        }
        
        System.err.println("SCHEMA LOC: " + schema);
        defaultNamespace.setSchemaLocation(schema);
    }
    
    private void setDocument(FileObject fileObject) {
        this.document = fileObject;
    }
    
    private XSDGrammar findGrammar(HintContext virtualElementCtx) throws java.io.IOException {
        Namespace ns = findNamespace(virtualElementCtx);
        XSDGrammar grammar = ns.getGrammar();
        if (grammar == null) {
            grammar = createGrammar(ns);
            ns.setGrammar(grammar);
            grammar.setNamespace(ns);
        }
        
        return grammar;
    }
    
    private XSDGrammar createGrammar(Namespace ns) throws java.io.IOException {
	String uri = ns.getSchemaLocation(); // either use schemaLocation or URI of xmlns="URI"
        
        if (uri == null) {
            uri = ns.getURI();
        }
        System.err.println("findSchema: " + uri);
        
        int idx = uri.indexOf(' ');
        if (idx >= 0) {
            uri = uri.substring(idx + 1);
        }
        
        // first try std way
        try {
            UserCatalog catalog = UserCatalog.getDefault();
            if (catalog != null) {
                EntityResolver resolver = catalog.getEntityResolver();
                if (resolver != null) {
                    InputSource inputSource = resolver.resolveEntity(uri, null);
                    if (inputSource != null) {
                        return new XSDParser().parse(inputSource);
                    }
                }
            }
        } catch (org.xml.sax.SAXException e) {
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.EXCEPTION, e);
        }
        
        // try an URL first
        try {
            java.net.URL url = new java.net.URL(uri);
            return new XSDParser().parse(new InputSource(url.openStream()));
        } catch (java.net.MalformedURLException e) { // sort of expected
            // debug only
            // ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "URL not found: " + schema);
        }
        
        // try files
        if (document == null) {
            return null;
        }
        
        FileObject fo = document.getParent().getFileObject(uri);

        if (fo == null) {
            // debug only
            // ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "File not found: " + schema);
            return null;
        }

        return new XSDParser().parse(new InputSource(fo.getInputStream()));
    }
    
    private void addNamespace(Namespace ns) {
        if (ns.getPrefix() != null) {
            prefix2Namespace.put(ns.getPrefix(), ns);
        }
        
        uri2Namespace.put(ns.getURI(), ns);
    }
    
    private Namespace findNamespaceByURI(String uri) {
        return (Namespace) uri2Namespace.get(uri);
    }
    
    private Namespace findNamespace(Node node) {
        // [ TODO] examine node or search through parents up to defaultNamespace
        System.err.println("find namespace");
        System.err.println("PARENT: " + node.getParentNode().getNodeName());
        /*
        node = node.getParentNode();
        System.err.println("findNamespace: " + node);
        System.err.flush();
        try {
            System.err.println("resolved namespace URI: " + node.getNamespaceURI());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.err.flush();
        try {
            System.err.println(" prefix: " + node.getPrefix());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.err.flush();
         */
        return defaultNamespace;
    }
    
    /** not implemented
     * @return true
     */
    public boolean isAllowed(Enumeration en) {
        return true;
    }
    
    public Enumeration queryAttributes(HintContext ownerElementCtx) {
        Thread.dumpStack();
        return Collections.enumeration(new ArrayList());
    }
    
    public GrammarResult queryDefault(HintContext parentNodeCtx) {
        Thread.dumpStack();
        return null;
    }
    
    public Enumeration queryElements(HintContext virtualElementCtx) {
        try {
            XSDGrammar grammar = findGrammar(virtualElementCtx);
            return grammar.queryElements(virtualElementCtx);
        } catch (java.io.IOException e) {
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.EXCEPTION, e);
            return Collections.enumeration(new ArrayList(0));
        }
    }
    
    public Enumeration queryEntities(String prefix) {
        Thread.dumpStack();
        return Collections.enumeration(new ArrayList());
    }
    
    public Enumeration queryNotations(String prefix) {
        Thread.dumpStack();
        return Collections.enumeration(new ArrayList());
    }
    
    public Enumeration queryValues(HintContext virtualTextCtx) {
        Thread.dumpStack();
        return Collections.enumeration(new ArrayList());
    }

    // Legacy methods
    
    /** @return null */
    public java.awt.Component getCustomizer(HintContext nodeCtx) {
        return null;
    }
    
    /** @return null */
    public org.openide.nodes.Node.Property[] getProperties(HintContext nodeCtx) {
        return null;
    }
    
    /** @return false */
    public boolean hasCustomizer(HintContext nodeCtx) {
        return false;
    }
    
    /**
     * Getter for property target.
     * @return Value of property target.
     */
    public org.w3c.dom.Node getTarget() {
        return target;
    }
    
    /**
     * Setter for property target.
     * @param target New value of property target.
     */
    public void setTarget(org.w3c.dom.Node target) {
        this.target = target;
    }
    
}

/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.grammar;

import java.io.File;
import java.io.StringReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.xml.api.model.HintContext;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

/**
 * Helpers for AntGrammarTest.
 * @author Jesse Glick
 */
final class TestUtil {
    
    private TestUtil() {}
    
    private static final InstalledFileLocator FILE_LOCATOR;
    static {
        String antHomeS = System.getProperty("test.ant.home");
        if (antHomeS == null) {
            throw new Error("Tests will not run unless test.ant.home and test.ant.bridge system properties are defined");
        }
        final File antHome = new File(antHomeS);
        final File antBridge = new File(System.getProperty("test.ant.bridge"));
        final File antJar = new File(new File(antHome, "lib"), "ant.jar");
        FILE_LOCATOR = new InstalledFileLocator() {
            public File locate(String name, String module, boolean loc) {
                if (name.equals("ant")) {
                    return antHome;
                } else if (name.equals("ant/nblib/bridge.jar")) {
                    return antBridge;
                } else if (name.equals("ant/nblib")) {
                    return antBridge.getParentFile();
                } else if (name.equals("ant/lib/ant.jar")) {
                    return antJar;
                } else {
                    return null;
                }
            }
        };
        System.setProperty("org.openide.util.Lookup", TestUtil.Lkp.class.getName());
    }
    public static final class Lkp extends ProxyLookup {
        public Lkp() {
            super(new Lookup[] {
                Lookups.fixed(new Object[] {
                    FILE_LOCATOR,
                    Lkp.class.getClassLoader(),
                }),
                Lookups.metaInfServices(Lkp.class.getClassLoader()),
            });
        }
    }
    
    private static HintContext createHintContext(final Node n, final String prefix) {
        Set/*<Class>*/ interfaces = new HashSet();
        findAllInterfaces(n.getClass(), interfaces);
        interfaces.add(HintContext.class);
        class Handler implements InvocationHandler {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getDeclaringClass().equals(HintContext.class)) {
                    assert method.getName().equals("getCurrentPrefix");
                    return prefix;
                } else {
                    return method.invoke(n, args);
                }
            }
        }
        return (HintContext)Proxy.newProxyInstance(TestUtil.class.getClassLoader(),
            (Class[])interfaces.toArray(new Class[interfaces.size()]), new Handler());
    }
    
    static void findAllInterfaces(Class c, Set/*<Class>*/ interfaces) {
        if (c.isInterface()) {
            interfaces.add(c);
        }
        Class s = c.getSuperclass();
        if (s != null) {
            findAllInterfaces(s, interfaces);
        }
        Class[] is = c.getInterfaces();
        for (int i = 0; i < is.length; i++) {
            findAllInterfaces(is[i], interfaces);
        }
    }
    
    /**
     * Create a context for completing some XML.
     * The XML text must be a well-formed document.
     * It must contain exactly one element name, attribute name,
     * attribute value, or text node ending in the string <samp>HERE</samp>.
     * The context will be that node (Element, Attribute, or Text) with
     * the suffix stripped off and the prefix set to the text preceding that suffix.
     */
    public static HintContext createCompletion(String xml) throws Exception {
        Document doc = XMLUtil.parse(new InputSource(new StringReader(xml)), false, true, null, null);
        return findCompletion(doc.getDocumentElement(), doc);
    }
    
    private static HintContext findCompletion(Node n, Document doc) {
        switch (n.getNodeType()) {
            case Node.ELEMENT_NODE:
                Element el = (Element)n;
                String name = el.getTagName();
                if (name.endsWith("HERE")) {
                    String prefix = name.substring(0, name.length() - 4);
                    Node nue = doc.createElementNS(el.getNamespaceURI(), prefix);
                    NodeList nl = el.getChildNodes();
                    while (nl.getLength() > 0) {
                        nue.appendChild(nl.item(0));
                    }
                    el.getParentNode().replaceChild(nue, el);
                    return createHintContext(nue, prefix);
                }
                break;
            case Node.TEXT_NODE:
                Text text = (Text)n;
                String contents = text.getNodeValue();
                if (contents.endsWith("HERE")) {
                    String prefix = contents.substring(0, contents.length() - 4);
                    text.setNodeValue(prefix);
                    return createHintContext(text, prefix);
                }
                break;
            case Node.ATTRIBUTE_NODE:
                Attr attr = (Attr)n;
                name = attr.getName();
                if (name.endsWith("HERE")) {
                    String prefix = name.substring(0, name.length() - 4);
                    Attr nue = doc.createAttributeNS(attr.getNamespaceURI(), prefix);
                    Element owner = attr.getOwnerElement();
                    owner.removeAttributeNode(attr);
                    owner.setAttributeNodeNS(nue);
                    return createHintContext(nue, prefix);
                } else {
                    String value = attr.getNodeValue();
                    if (value.endsWith("HERE")) {
                        String prefix = value.substring(0, value.length() - 4);
                        attr.setNodeValue(prefix);
                        return createHintContext(attr, prefix);
                    }
                }
                break;
            default:
                // ignore
                break;
        }
        // Didn't find it, check children.
        NodeList nl = n.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            HintContext c = findCompletion(nl.item(i), doc);
            if (c != null) {
                return c;
            }
        }
        // Element's attr nodes are listed separately.
        NamedNodeMap nnm = n.getAttributes();
        if (nnm != null) {
            for (int i = 0; i < nnm.getLength(); i++) {
                HintContext c = findCompletion(nnm.item(i), doc);
                if (c != null) {
                    return c;
                }
            }
        }
        // Nope.
        return null;
    }
    
    /**
     * Get a particular element in a test XML document.
     * Pass in a well-formed XML document and an element name to search for.
     * Must be exactly one such.
     */
    public static Element createElementInDocument(String xml, String elementName, String elementNamespace) throws Exception {
        Document doc = XMLUtil.parse(new InputSource(new StringReader(xml)), false, true, null, null);
        NodeList nl = doc.getElementsByTagNameNS(elementNamespace, elementName);
        if (nl.getLength() != 1) {
            throw new IllegalArgumentException("Zero or more than one <" + elementName + ">s in \"" + xml + "\"");
        }
        return (Element)nl.item(0);
    }
    
    /**
     * Given a list of XML nodes returned in GrammarResult's, return a list of their names.
     * For elements, you get the name; for attributes, the name;
     * for text nodes, the value.
     * (No namespaces returned.)
     */
    public static List/*<String>*/ grammarResultValues(Enumeration/*<Node>*/ e) {
        List l = new ArrayList();
        while (e.hasMoreElements()) {
            Object o = e.nextElement();
            String s;
            if (o instanceof Element) {
                s = ((Element)o).getNodeName();
            } else if (o instanceof Attr) {
                s = ((Attr)o).getName();
            } else {
                s = ((Text)o).getData();
            }
            l.add(s);
        }
        return l;
    }
    
}

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
/**
 *        This generated bean class CCollab
 *        matches the schema element 'c:collab'.
 *
 *        Generated on Thu Aug 19 15:45:47 PDT 2004
 *
 *        This class matches the root element of the XML Schema,
 *        and is the root of the bean graph.
 *
 *         c:collab : CCollab
 *                 mdc:config : Config[1,n]
 *                         [attr: version CDATA #IMPLIED  : java.lang.String]
 *                         | mdc:event-notifier-config : EventNotifierConfig
 *                         |         register-event : RegisterEvent[1,n]
 *                         |                 event-class : java.lang.String
 *                         |                 event-name : java.lang.String
 *                         | mdc:event-processor-config : EventProcessorConfig
 *                         |         register-event-handler : RegisterEventHandler[1,n]
 *                         |                 event-name : java.lang.String
 *                         |                 event-handler-info : EventHandlerInfo
 *                         |                         handler-class : java.lang.String
 *                         |                         stateful : boolean
 *
 */
package org.netbeans.modules.collab.channel.filesharing.mdc.configbean;

public class CCollab {
    private java.util.List _MdcConfig = new java.util.ArrayList(); // List<Config>

    public CCollab() {
    }

    // Deep copy
    public CCollab(org.netbeans.modules.collab.channel.filesharing.mdc.configbean.CCollab source) {
        for (java.util.Iterator it = source._MdcConfig.iterator(); it.hasNext();) {
            _MdcConfig.add(
                new org.netbeans.modules.collab.channel.filesharing.mdc.configbean.Config(
                    (org.netbeans.modules.collab.channel.filesharing.mdc.configbean.Config) it.next()
                )
            );
        }
    }

    // This attribute is an array containing at least one element
    public void setMdcConfig(org.netbeans.modules.collab.channel.filesharing.mdc.configbean.Config[] value) {
        if (value == null) {
            value = new Config[0];
        }

        _MdcConfig.clear();

        for (int i = 0; i < value.length; ++i) {
            _MdcConfig.add(value[i]);
        }
    }

    public void setMdcConfig(int index, org.netbeans.modules.collab.channel.filesharing.mdc.configbean.Config value) {
        _MdcConfig.set(index, value);
    }

    public org.netbeans.modules.collab.channel.filesharing.mdc.configbean.Config[] getMdcConfig() {
        Config[] arr = new Config[_MdcConfig.size()];

        return (Config[]) _MdcConfig.toArray(arr);
    }

    public java.util.List fetchMdcConfigList() {
        return _MdcConfig;
    }

    public org.netbeans.modules.collab.channel.filesharing.mdc.configbean.Config getMdcConfig(int index) {
        return (Config) _MdcConfig.get(index);
    }

    // Return the number of mdcConfig
    public int sizeMdcConfig() {
        return _MdcConfig.size();
    }

    public int addMdcConfig(org.netbeans.modules.collab.channel.filesharing.mdc.configbean.Config value) {
        _MdcConfig.add(value);

        return _MdcConfig.size() - 1;
    }

    // Search from the end looking for @param value, and then remove it.
    public int removeMdcConfig(org.netbeans.modules.collab.channel.filesharing.mdc.configbean.Config value) {
        int pos = _MdcConfig.indexOf(value);

        if (pos >= 0) {
            _MdcConfig.remove(pos);
        }

        return pos;
    }

    public void write(java.io.OutputStream out) throws java.io.IOException {
        write(out, null);
    }

    public void write(java.io.OutputStream out, String encoding)
    throws java.io.IOException {
        java.io.Writer w;

        if (encoding == null) {
            encoding = "UTF-8"; // NOI18N
        }

        w = new java.io.BufferedWriter(new java.io.OutputStreamWriter(out, encoding));
        write(w, encoding);
        w.flush();
    }

    // Print this Java Bean to @param out including an XML header.
    // @param encoding is the encoding style that @param out was opened with.
    public void write(java.io.Writer out, String encoding)
    throws java.io.IOException {
        out.write("<?xml version='1.0'"); // NOI18N

        if (encoding != null) {
            out.write(" encoding='" + encoding + "'"); // NOI18N
        }

        out.write(" ?>\n"); // NOI18N
        writeNode(out, "c:collab", ""); // NOI18N
    }

    public void writeNode(java.io.Writer out, String nodeName, String indent)
    throws java.io.IOException {
        out.write(indent);
        out.write("<");
        out.write(nodeName);
        out.write(" xmlns='"); // NOI18N
        out.write("http://sun.com/ns/collab/dev/1_0/mdc"); // NOI18N
        out.write("'"); // NOI18N
        out.write(">\n");

        String nextIndent = indent + "	";

        for (java.util.Iterator it = _MdcConfig.iterator(); it.hasNext();) {
            org.netbeans.modules.collab.channel.filesharing.mdc.configbean.Config element = (org.netbeans.modules.collab.channel.filesharing.mdc.configbean.Config) it.next();

            if (element != null) {
                element.writeNode(out, "mdc:config", nextIndent);
            }
        }

        out.write(indent);
        out.write("</" + nodeName + ">\n");
    }

    public static CCollab read(java.io.InputStream in)
    throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
        return read(new org.xml.sax.InputSource(in), false, null, null);
    }

    // Warning: in readNoEntityResolver character and entity references will
    // not be read from any DTD in the XML source.
    // However, this way is faster since no DTDs are looked up
    // (possibly skipping network access) or parsed.
    public static CCollab readNoEntityResolver(java.io.InputStream in)
    throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
        return read(
            new org.xml.sax.InputSource(in), false,
            new org.xml.sax.EntityResolver() {
                public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) {
                    java.io.ByteArrayInputStream bin = new java.io.ByteArrayInputStream(new byte[0]);

                    return new org.xml.sax.InputSource(bin);
                }
            }, null
        );
    }

    public static CCollab read(
        org.xml.sax.InputSource in, boolean validate, org.xml.sax.EntityResolver er, org.xml.sax.ErrorHandler eh
    ) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setValidating(validate);

        javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();

        if (er != null) {
            db.setEntityResolver(er);
        }

        if (eh != null) {
            db.setErrorHandler(eh);
        }

        org.w3c.dom.Document doc = db.parse(in);

        return read(doc);
    }

    public static CCollab read(org.w3c.dom.Document document) {
        CCollab aCCollab = new CCollab();
        aCCollab.readNode(document.getDocumentElement());

        return aCCollab;
    }

    public void readNode(org.w3c.dom.Node node) {
        org.w3c.dom.NodeList children = node.getChildNodes();

        for (int i = 0, size = children.getLength(); i < size; ++i) {
            org.w3c.dom.Node childNode = children.item(i);
            String childNodeName = ((childNode.getLocalName() == null) ? childNode.getNodeName().intern()
                                                                       : childNode.getLocalName().intern());
            String childNodeValue = "";

            if (childNode.getFirstChild() != null) {
                childNodeValue = childNode.getFirstChild().getNodeValue();
            }

            if (childNodeName == "mdc:config") {
                Config aMdcConfig = new org.netbeans.modules.collab.channel.filesharing.mdc.configbean.Config();
                aMdcConfig.readNode(childNode);
                _MdcConfig.add(aMdcConfig);
            } else {
                // Found extra unrecognized childNode
            }
        }
    }

    // Takes some text to be printed into an XML stream and escapes any
    // characters that might make it invalid XML (like '<').
    public static void writeXML(java.io.Writer out, String msg)
    throws java.io.IOException {
        writeXML(out, msg, true);
    }

    public static void writeXML(java.io.Writer out, String msg, boolean attribute)
    throws java.io.IOException {
        if (msg == null) {
            return;
        }

        int msgLength = msg.length();

        for (int i = 0; i < msgLength; ++i) {
            char c = msg.charAt(i);
            writeXML(out, c, attribute);
        }
    }

    public static void writeXML(java.io.Writer out, char msg, boolean attribute)
    throws java.io.IOException {
        if (msg == '&') {
            out.write("&amp;");
        } else if (msg == '<') {
            out.write("&lt;");
        } else if (msg == '>') {
            out.write("&gt;");
        } else if (attribute && (msg == '"')) {
            out.write("&quot;");
        } else if (attribute && (msg == '\'')) {
            out.write("&apos;");
        } else if (attribute && (msg == '\n')) {
            out.write("&#xA;");
        } else if (attribute && (msg == '\t')) {
            out.write("&#x9;");
        } else {
            out.write(msg);
        }
    }

    public void changePropertyByName(String name, Object value) {
        if (name == null) {
            return;
        }

        name = name.intern();

        if (name == "mdcConfig") {
            addMdcConfig((Config) value);
        } else if (name == "mdcConfig[]") {
            setMdcConfig((Config[]) value);
        } else {
            throw new IllegalArgumentException(name + " is not a valid property name for CCollab");
        }
    }

    public Object fetchPropertyByName(String name) {
        if (name == "mdcConfig[]") {
            return getMdcConfig();
        }

        throw new IllegalArgumentException(name + " is not a valid property name for CCollab");
    }

    // Return an array of all of the properties that are beans and are set.
    public java.lang.Object[] childBeans(boolean recursive) {
        java.util.List children = new java.util.LinkedList();
        childBeans(recursive, children);

        java.lang.Object[] result = new java.lang.Object[children.size()];

        return (java.lang.Object[]) children.toArray(result);
    }

    // Put all child beans into the beans list.
    public void childBeans(boolean recursive, java.util.List beans) {
        for (java.util.Iterator it = _MdcConfig.iterator(); it.hasNext();) {
            org.netbeans.modules.collab.channel.filesharing.mdc.configbean.Config element = (org.netbeans.modules.collab.channel.filesharing.mdc.configbean.Config) it.next();

            if (element != null) {
                if (recursive) {
                    element.childBeans(true, beans);
                }

                beans.add(element);
            }
        }
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof org.netbeans.modules.collab.channel.filesharing.mdc.configbean.CCollab)) {
            return false;
        }

        org.netbeans.modules.collab.channel.filesharing.mdc.configbean.CCollab inst = (org.netbeans.modules.collab.channel.filesharing.mdc.configbean.CCollab) o;

        if (sizeMdcConfig() != inst.sizeMdcConfig()) {
            return false;
        }

        // Compare every element.
        for (
            java.util.Iterator it = _MdcConfig.iterator(), it2 = inst._MdcConfig.iterator();
                it.hasNext() && it2.hasNext();
        ) {
            org.netbeans.modules.collab.channel.filesharing.mdc.configbean.Config element = (org.netbeans.modules.collab.channel.filesharing.mdc.configbean.Config) it.next();
            org.netbeans.modules.collab.channel.filesharing.mdc.configbean.Config element2 = (org.netbeans.modules.collab.channel.filesharing.mdc.configbean.Config) it2.next();

            if (!((element == null) ? (element2 == null) : element.equals(element2))) {
                return false;
            }
        }

        return true;
    }

    public int hashCode() {
        int result = 17;
        result = (37 * result) + ((_MdcConfig == null) ? 0 : _MdcConfig.hashCode());

        return result;
    }
}

/*
                The following schema file has been used for generation:

<?xml version="1.0" encoding="UTF-8"?>
<!--
    Document   : collab_config.xsd
    Created on : Aug 19, 2004, 7:45 AM
    Description:
        Purpose of the document follows.
-->
<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            targetNamespace="http://sun.com/ns/collab/dev/1_0/mdc"
            xmlns:c="http://sun.com/ns/collab/dev/1_0"
            xmlns:mdc="http://sun.com/ns/collab/dev/1_0/mdc"
            elementFormDefault="qaulified">

    <!-- collab element -->
    <xsd:element name="c:collab" type="_collab"/>

    <xsd:complexType name="_collab">
        <xsd:sequence>
            <xsd:choice maxOccurs="1">
                <xsd:element name="mdc:config" type="_config"
                        minOccurs="1" maxOccurs="unbounded"/>
            </xsd:choice>
        </xsd:sequence>
    </xsd:complexType>

    <!-- Schema for config -->
    <xsd:complexType name="_config">
        <xsd:sequence>
            <xsd:choice maxOccurs="1">
                <xsd:element name="mdc:event-notifier-config" type="_event-notifier-config"
                        minOccurs="1" maxOccurs="1"/>
                <xsd:element name="mdc:event-processor-config" type="_event-processor-config"
                        minOccurs="1" maxOccurs="1"/>
            </xsd:choice>
        </xsd:sequence>
        <xsd:attribute name="version" type="xsd:string"/>
    </xsd:complexType>

   <!-- Schema for event-notifier-config -->
    <xsd:complexType name="_event-notifier-config">
        <xsd:sequence>
            <xsd:element name="register-event" type="_register-event"
                    minOccurs="1" maxOccurs="unbounded"/>
        </xsd:sequence>
    </xsd:complexType>

   <!-- Schema for event-processor-config -->
    <xsd:complexType name="_event-processor-config">
        <xsd:sequence>
            <xsd:element name="register-event-handler" type="_register-event-handler"
                    minOccurs="1" maxOccurs="unbounded"/>
        </xsd:sequence>
    </xsd:complexType>

    <!-- Schema for register-event -->
    <xsd:complexType name="_register-event">
        <xsd:sequence>
            <xsd:element name="event-class" type="_class-name"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="event-name" type="_event-name"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <!-- Schema for register-event-handler -->
    <xsd:complexType name="_register-event-handler">
        <xsd:sequence>
            <xsd:element name="event-name" type="_event-name"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="event-handler-info" type="_event-handler-info"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <!-- Schema for _event-handler -->
    <xsd:complexType name="_event-handler-info">
        <xsd:sequence>
            <xsd:element name="handler-class" type="_class-name"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="stateful" type="xsd:boolean"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <!-- Schema for _event-name -->
    <xsd:complexType name="_event-name">
        <xsd:simpleContent>
            <xsd:extension base="xsd:string"/>
        </xsd:simpleContent>
    </xsd:complexType>

    <!-- Schema for _class -->
    <xsd:complexType name="_class-name">
        <xsd:simpleContent>
            <xsd:extension base="xsd:string"/>
        </xsd:simpleContent>
    </xsd:complexType>

</xsd:schema>

*/

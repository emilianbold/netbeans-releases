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
 *        This generated bean class Config
 *        matches the schema element '_config'.
 *
 *        Generated on Thu Aug 19 15:45:47 PDT 2004
 */
package org.netbeans.modules.collab.channel.filesharing.mdc.configbean;

public class Config {
    private java.lang.String _Version;
    private EventNotifierConfig _MdcEventNotifierConfig;
    private EventProcessorConfig _MdcEventProcessorConfig;

    public Config() {
    }

    // Deep copy
    public Config(org.netbeans.modules.collab.channel.filesharing.mdc.configbean.Config source) {
        _Version = source._Version;
        _MdcEventNotifierConfig = new org.netbeans.modules.collab.channel.filesharing.mdc.configbean.EventNotifierConfig(
                source._MdcEventNotifierConfig
            );
        _MdcEventProcessorConfig = new org.netbeans.modules.collab.channel.filesharing.mdc.configbean.EventProcessorConfig(
                source._MdcEventProcessorConfig
            );
    }

    // This attribute is optional
    public void setVersion(java.lang.String value) {
        _Version = value;
    }

    public java.lang.String getVersion() {
        return _Version;
    }

    // This attribute is mandatory
    public void setMdcEventNotifierConfig(
        org.netbeans.modules.collab.channel.filesharing.mdc.configbean.EventNotifierConfig value
    ) {
        _MdcEventNotifierConfig = value;
    }

    public org.netbeans.modules.collab.channel.filesharing.mdc.configbean.EventNotifierConfig getMdcEventNotifierConfig() {
        return _MdcEventNotifierConfig;
    }

    // This attribute is mandatory
    public void setMdcEventProcessorConfig(
        org.netbeans.modules.collab.channel.filesharing.mdc.configbean.EventProcessorConfig value
    ) {
        _MdcEventProcessorConfig = value;
    }

    public org.netbeans.modules.collab.channel.filesharing.mdc.configbean.EventProcessorConfig getMdcEventProcessorConfig() {
        return _MdcEventProcessorConfig;
    }

    public void writeNode(java.io.Writer out, String nodeName, String indent)
    throws java.io.IOException {
        out.write(indent);
        out.write("<");
        out.write(nodeName);

        // version is an attribute
        if (_Version != null) {
            out.write(" version"); // NOI18N
            out.write("='"); // NOI18N
            org.netbeans.modules.collab.channel.filesharing.mdc.configbean.CCollab.writeXML(out, _Version, true);
            out.write("'"); // NOI18N
        }

        out.write(">\n");

        String nextIndent = indent + "	";

        if (_MdcEventNotifierConfig != null) {
            _MdcEventNotifierConfig.writeNode(out, "mdc:event-notifier-config", nextIndent);
        }

        if (_MdcEventProcessorConfig != null) {
            _MdcEventProcessorConfig.writeNode(out, "mdc:event-processor-config", nextIndent);
        }

        out.write(indent);
        out.write("</" + nodeName + ">\n");
    }

    public void readNode(org.w3c.dom.Node node) {
        if (node.hasAttributes()) {
            org.w3c.dom.NamedNodeMap attrs = node.getAttributes();
            org.w3c.dom.Attr attr;
            attr = (org.w3c.dom.Attr) attrs.getNamedItem("version");

            if (attr != null) {
                _Version = attr.getValue();
            }
        }

        org.w3c.dom.NodeList children = node.getChildNodes();

        for (int i = 0, size = children.getLength(); i < size; ++i) {
            org.w3c.dom.Node childNode = children.item(i);
            String childNodeName = ((childNode.getLocalName() == null) ? childNode.getNodeName().intern()
                                                                       : childNode.getLocalName().intern());
            String childNodeValue = "";

            if (childNode.getFirstChild() != null) {
                childNodeValue = childNode.getFirstChild().getNodeValue();
            }

            if (childNodeName == "mdc:event-notifier-config") {
                _MdcEventNotifierConfig = new org.netbeans.modules.collab.channel.filesharing.mdc.configbean.EventNotifierConfig();
                _MdcEventNotifierConfig.readNode(childNode);
            } else if (childNodeName == "mdc:event-processor-config") {
                _MdcEventProcessorConfig = new org.netbeans.modules.collab.channel.filesharing.mdc.configbean.EventProcessorConfig();
                _MdcEventProcessorConfig.readNode(childNode);
            } else {
                // Found extra unrecognized childNode
            }
        }
    }

    public void changePropertyByName(String name, Object value) {
        if (name == null) {
            return;
        }

        name = name.intern();

        if (name == "version") {
            setVersion((java.lang.String) value);
        } else if (name == "mdcEventNotifierConfig") {
            setMdcEventNotifierConfig((EventNotifierConfig) value);
        } else if (name == "mdcEventProcessorConfig") {
            setMdcEventProcessorConfig((EventProcessorConfig) value);
        } else {
            throw new IllegalArgumentException(name + " is not a valid property name for Config");
        }
    }

    public Object fetchPropertyByName(String name) {
        if (name == "version") {
            return getVersion();
        }

        if (name == "mdcEventNotifierConfig") {
            return getMdcEventNotifierConfig();
        }

        if (name == "mdcEventProcessorConfig") {
            return getMdcEventProcessorConfig();
        }

        throw new IllegalArgumentException(name + " is not a valid property name for Config");
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
        if (_MdcEventNotifierConfig != null) {
            if (recursive) {
                _MdcEventNotifierConfig.childBeans(true, beans);
            }

            beans.add(_MdcEventNotifierConfig);
        }

        if (_MdcEventProcessorConfig != null) {
            if (recursive) {
                _MdcEventProcessorConfig.childBeans(true, beans);
            }

            beans.add(_MdcEventProcessorConfig);
        }
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof org.netbeans.modules.collab.channel.filesharing.mdc.configbean.Config)) {
            return false;
        }

        org.netbeans.modules.collab.channel.filesharing.mdc.configbean.Config inst = (org.netbeans.modules.collab.channel.filesharing.mdc.configbean.Config) o;

        if (!((_Version == null) ? (inst._Version == null) : _Version.equals(inst._Version))) {
            return false;
        }

        if (
            !((_MdcEventNotifierConfig == null) ? (inst._MdcEventNotifierConfig == null)
                                                    : _MdcEventNotifierConfig.equals(inst._MdcEventNotifierConfig))
        ) {
            return false;
        }

        if (
            !((_MdcEventProcessorConfig == null) ? (inst._MdcEventProcessorConfig == null)
                                                     : _MdcEventProcessorConfig.equals(inst._MdcEventProcessorConfig))
        ) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result = 17;
        result = (37 * result) + ((_Version == null) ? 0 : _Version.hashCode());
        result = (37 * result) + ((_MdcEventNotifierConfig == null) ? 0 : _MdcEventNotifierConfig.hashCode());
        result = (37 * result) + ((_MdcEventProcessorConfig == null) ? 0 : _MdcEventProcessorConfig.hashCode());

        return result;
    }
}

/*
                The following schema file has been used for generation:

<?xml version="1.0" encoding="UTF-8"?>
<!--
    Document   : collab_config.xsd
    Created on : Aug 19, 2004, 7:45 AM
    Author     : Ayub Khan
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

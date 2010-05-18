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


package org.netbeans.modules.xml.wsdl.ui.common;

/**
 * Implements a fully-qualified name model.
 *
 * @author Enrico Lelina
 * @version 
 */
public class QName {

    /** The XML namespace URI. */
    public static final String XMLNS = "http://www.w3.org/XML/1998/namespace";

    /** The local name. */
    private String mLocalName = null;

    /** The namespace URI. */
    private String mNamespaceURI = null;

    /** The namespace prefix. */
    private String mPrefix = null;

    /**
     * Constructs an empty QName.
     */
    public QName() {
        // nothing to do
    }
    
    /**
     * Constructs a QName given QName string
     * @param localName the local name
     */
    public QName(String qNameStr) {
        QName qName = getQNameFromString(qNameStr);
        this.mNamespaceURI = qName.getNamespaceURI();
        this.mPrefix = qName.getPrefix();
        this.mLocalName = qName.getLocalName();
    }
    
    /**
     * Constructs a QName with the specified namespace URI and local name.
     * @param namespaceURI the namespace URI
     * @param name Either the local name or qualified name.
     */
    public QName(String namespaceURI, String name) {
        this(namespaceURI, null, name);
    }
    
    /**
     * Constructs a QName with the specified namespace URI, namespace prefix
     * and local name.
     * @param namespaceURI the namespace URI
     * @param prefix the namespace prefix
     * @param name Either the local name or qualified name (in which case, prefix must be <code>null</code>).
     */
    public QName(String namespaceURI, String prefix, String name) {
        mNamespaceURI = namespaceURI;
        int colon;
        if ((null == prefix)
                && ((name != null) && ((colon = name.indexOf(':')) != -1))) {
            mPrefix = name.substring(0, colon);
            mLocalName = name.substring(colon + 1);
        } else {
            mPrefix = prefix;
            mLocalName = name;
        }
    }
    
    /**
     * Constructs a QName from the given QName string, e.g., "tns:foo", "foo".
     * @param qNameString the QName string
     * @return a new QName
     */
    public static QName getQNameFromString(String qNameString) {
        QName qName = new QName(QName.getNamespaceURI(qNameString),
                                QName.getPrefix(qNameString),
                                QName.getLocalName(qNameString));
                               
        if(qName.getLocalName() == null) {
            return null;
        }
        
        return qName;
    }
    
    /**
     * Gets the prefix from the given QName string.
     * @param qName the QName string
     * @return the prefix or null if there is no prefix
     */
    public static String getPrefix(String qName) {
        if(qName == null || qName.trim().equals("")) {
            return null;
        }
        
        int index = qName.indexOf('{');
        //if { then we have namespace
        if(index != -1) {
            return null;
        }
        
        index = qName.lastIndexOf(':');
        
        return ((index > 0) ? qName.substring(0, index) : null); 
    }
    
    /**
     * Gets the local name from the given QName string.
     * @param qName the QName string
     * @return the local name
     */
    public static String getLocalName(String qName) {
        if(qName == null || qName.trim().equals("")) {
            return null;
        }
        
        //first check if qName is {namespace}localName
        int    index = qName.lastIndexOf('}');
        
        if(index == -1) {
            index = qName.lastIndexOf(':');
        }
        
        return ((index < 0) ? qName : qName.substring(index + 1));
    }
    
    public static String getNamespaceURI(String qName) {
        if(qName == null || qName.trim().equals("")) {
            return null;
        }
        
        String namespace = null;
        int sIndex = qName.indexOf('{');
        int eIndex = qName.lastIndexOf('}');
        
        if(sIndex != -1 
           && eIndex != -1) {
            namespace = qName.substring(sIndex+1, eIndex);
        }
        
        return namespace;
    }
    
    /**
     * Gets the local name.
     * @return the local name
     */
    public String getLocalName() {
        return mLocalName;
    }
    
    /**
     * Sets the local name.
     * @param localName the new local name
     */
    public void setLocalName(String localName) {
        mLocalName = localName;
    }
    
    /**
     * Gets the namespace URI.
     * @return the namespace URI
     */
    public String getNamespaceURI() {
        return mNamespaceURI;
    }
    
    /**
     * Sets the namespace URI.
     * @param namespaceURI the new namespace URI
     */
    public void setNamespaceURI(String namespaceURI) {
        mNamespaceURI = namespaceURI;
    }
    
    /**
     * Gets the prefix.
     * @return the prefix
     */
    public String getPrefix() {
        return mPrefix;
    }
    
    /**
     * Sets the prefix.
     * @param prefix the new prefix
     */
    public void setPrefix(String prefix) {
        mPrefix = prefix;
    }
    
    /**
     * Returns the string representation of the QName. If the prefix is
     * available, then it will be [prefix]:[localName], for example,
     * tns:foo. If the prefix is not available and there is a namespace URI,
     * then it will be {[namespaceURI]}[localName], for example,
     * {http://schemas.xmlsoap.org/wsdl/}message. If neither the prefix not
     * the namespace URI are present, then it will just be the local name.
     * @return the QName's string representation
     */
    public String toString() {
        String qName = (null == getLocalName()) ? "" : getLocalName();
        
        if (getPrefix() != null) {
            return getPrefix() + ':' + qName;
        } else if (getNamespaceURI() != null) {
            return '{' + getNamespaceURI() + '}' + qName;
        } else {
            return qName;
        }
    }
    
    public boolean equals(Object src) {
        if(!(src instanceof QName)) {
            return false;
        }
        
        QName srcQName = (QName) src;
        return this.toString().equals(srcQName.toString());
        
    }
    
    public int hashCode() {
        return this.toString().hashCode();
    }
}

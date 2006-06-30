/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.schema2beans;

/**
 * @author cliffwd
 * A QName class for dealing with XML Qualified Names; basically,
 * a namespace and a localpart.
 *
 * This class is intended solely for those who for some reason can't
 * use javax.xml.namespace.QName.  See that class for documentation.
 * Remember that prefix is not part of equals or hashCode.
 */
public class QName {
    private String namespaceURI;
    private String localPart;
    private String prefix;

    public QName(String localPart) {
        this("", localPart, "");
    }

    public QName(String namespaceURI, String localPart) {
        this(namespaceURI, localPart, "");
    }

    public QName(String namespaceURI, String localPart, String prefix) {
        if (namespaceURI == null)
            namespaceURI = "";
        this.namespaceURI = namespaceURI;
        if (localPart == null)
            throw new IllegalArgumentException("localPart == null");
        this.localPart = localPart;
        this.prefix = prefix;
    }

    public String getNamespaceURI() {
        return namespaceURI;
    }

    public String getLocalPart() {
        return localPart;
    }

    public String getPrefix() {
        return prefix;
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof QName))
            return false;
        QName q = (QName) o;
        if (!namespaceURI.equals(q.namespaceURI))
            return false;
        if (!localPart.equals(q.localPart))
            return false;
        return true;
    }

    public int hashCode() {
        int result = 17;
        result = 37*result + namespaceURI.hashCode();
        result = 37*result + localPart.hashCode();
        return result;
    }

    public String toString() {
        if ("".equals(namespaceURI))
            return localPart;
        else
            return "{"+namespaceURI+"}"+localPart;
    }

    public static QName valueOf(String asString) {
        int pos = asString.indexOf('}');
        if (pos < 0) {
            return new QName(asString);
        } else {
            String ns = asString.substring(1, pos-1);
            String localPart = asString.substring(pos+1, asString.length());
            return new QName(ns, localPart);
        }
    }
}

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
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

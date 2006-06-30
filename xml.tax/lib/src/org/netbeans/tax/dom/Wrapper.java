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

package org.netbeans.tax.dom;

import org.w3c.dom.*;
import org.netbeans.tax.*;

/**
 *
 * @author  Petr Kuzel
 */
public class Wrapper {


    public static Attr wrap(TreeAttribute attr) {
        return new AttrImpl(attr);
    }

    public static Element wrap(TreeElement element) {
        return new ElementImpl(element);
    }

    public static Text wrap(TreeText text) {
        return new TextImpl(text);
    }

    public static Document wrap(TreeDocumentRoot document) {
        return new DocumentImpl(document);
    }
    
    public static DocumentType wrap(TreeDocumentType documentType) {
        return new DocumentTypeImpl(documentType);
    }
    
    public static Comment wrap(TreeComment comment) {
        return new CommentImpl(comment);
    }
    
    static NodeList wrap(TreeObjectList list) {
        return new NodeListImpl(list);
    }
    
    static NamedNodeMap wrap(TreeNamedObjectMap map) {
        return new NamedNodeMapImpl(map);
    }
    
    public static Node wrap(TreeObject object) {
        if (object == null) return null;
        if (object instanceof TreeAttribute) {
            return wrap((TreeAttribute) object);
        } else if (object instanceof TreeElement) {
            return wrap((TreeElement) object);            
        } else if (object instanceof TreeText) {
            return wrap((TreeText) object);
        } else if (object instanceof TreeDocumentRoot) {
            return wrap((TreeDocumentRoot) object);
        } else if (object instanceof TreeDocumentType) {
            return wrap((TreeDocumentType) object);
        } else if (object instanceof TreeComment) {
            return wrap((TreeComment) object);
        } else {
            throw new RuntimeException("Cannot wrap: " + object.getClass());
        }
    }
}

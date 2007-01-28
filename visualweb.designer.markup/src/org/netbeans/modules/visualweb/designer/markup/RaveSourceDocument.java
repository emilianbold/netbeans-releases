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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.visualweb.designer.markup;

import org.apache.xerces.dom.CoreDocumentImpl;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * Document representing JSP DOM (the source DOM).
 *
 * @author Peter Zavadsky
 */
public class RaveSourceDocument extends AbstractRaveDocument {

    /** Creates a new instance of RaveSourceDocument */
    public RaveSourceDocument() {
    }

    protected Element createDefaultElement(CoreDocumentImpl document, String namespaceURI, String qualifiedName) {
        return new RaveSourceElementImpl(document, namespaceURI, qualifiedName);
    }

    protected Element createDefaultElement(CoreDocumentImpl document, String namespaceURI, String qualifiedName, String localpart) {
        return new RaveSourceElementImpl(document, namespaceURI, qualifiedName, localpart);
    }

    protected Element createStyleElement(CoreDocumentImpl document, String namespaceURI, String qualifiedName) {
        return new SourceStyleElement(document, namespaceURI, qualifiedName);
    }

    protected Element createTableElement(CoreDocumentImpl document, String namespaceURI, String qualifiedName) {
        return new RaveSourceTableElementImpl(document, namespaceURI, qualifiedName);
    }

    protected Element createStylesheetLinkElement(CoreDocumentImpl document, String namespaceURI, String qualifiedName) {
        return new SourceStylesheetLinkElement(document, namespaceURI, qualifiedName);
    }

    protected Text createTextNode(CoreDocumentImpl document, String data) {
        return new RaveSourceTextImpl(document, data);
    }

}

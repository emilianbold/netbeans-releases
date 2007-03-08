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
/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Batik" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation. For more  information on the
 Apache Software Foundation, please see <http://www.apache.org/>.

*/

package org.netbeans.modules.visualweb.designer.markup;

import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.api.designer.cssengine.StyleRefreshable;
import java.net.URL;

import org.apache.batik.css.engine.CSSStyleSheetNode;
import org.apache.batik.css.engine.StyleSheet;
import org.apache.xerces.dom.CoreDocumentImpl;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;

import org.netbeans.modules.visualweb.designer.html.HtmlAttribute;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;

/**
 * This class is derived from org.apache.batik.dom.svg.SVGOMStyleElement,
 * so I've left the Batik copyright on it.
 * This class represents a &lt;style&gt; element in the DOM.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @author Tor Norbye
 */
class RenderedStyleElement extends RaveRenderedElementImpl
implements CSSStyleSheetNode, StyleRefreshable {

    /**
     *
     */
    private static final long serialVersionUID = 4050484529888048693L;

    public RenderedStyleElement(
        CoreDocumentImpl ownerDocument,
        String namespaceURI,
        String qualifiedName)
        throws DOMException {
        // XXX Is this redundant? Should we just hardcode
        // HtmlTag.STYLE.toString() here and not have it passed in?
        super(ownerDocument, namespaceURI, qualifiedName);
    }

    /**
     * The DOM CSS style-sheet.
     */
    protected transient StyleSheet styleSheet;
    
    public void refresh() {
        styleSheet = null;
    }    

    /**
     * The listener used to track the content changes.
     */
    protected transient EventListener domCharacterDataModifiedListener =
        new DOMCharacterDataModifiedListener();

    /**
     * <b>DOM</b>: Implements {@link org.w3c.dom.Node#getLocalName()}.
     */
    public String getLocalName() {
        return HtmlTag.STYLE.name;
    }

//    /** Get the text or comment text children of this element, which
//     * should correspond to style rules.
//     */
//    public static String getStyleText(Element e) {
//        String text = "";
//        Node n = e.getFirstChild();
//        if (n != null) {
//            StringBuffer sb = new StringBuffer();
//            while (n != null) {
//                if (n.getNodeType() == Node.CDATA_SECTION_NODE
//                    // Unlike javascript, where the first line in a comment should be treated
//                    // as a comment, the browsers seem to treat all comment text as style rules
//                    || n.getNodeType() == Node.COMMENT_NODE
//                    
//                    || n.getNodeType() == Node.TEXT_NODE)
//                    // XXX should pick up comments contents too!!
//                    sb.append(n.getNodeValue());
//                n = n.getNextSibling();
//            }
//            text = sb.toString();
//            // Strip out comments?
//        }
//        return text;
//    }

    /**
     * Returns the associated style-sheet.
     */
    public StyleSheet getCSSStyleSheet() {
        if (styleSheet == null) {
            String type = getType();
            if (type.length() == 0 || type.equals("text/css")) {
//                RaveDocument doc = (RaveDocument)getOwnerDocument();
//                CSSEngine e = doc.getCssEngine();
                Document doc = getOwnerDocument();
//                CSSEngine e = CssEngineServiceProvider.getDefault().getCssEngine(doc);
                
                String text = MarkupServiceImpl.getStyleText(this);
                URL burl = MarkupUtilities.getCascadedXMLBase(this);
                if (burl != null) {
                    String media =
                        getAttributeNS(null, HtmlAttribute.MEDIA);
//                    styleSheet = e.parseStyleSheet(text, burl, media, this);
                    styleSheet = CssProvider.getEngineService().parseStyleSheetForDocument(doc, text, burl, media, this);
                    addEventListener(
                        "DOMCharacterDataModified",
                        domCharacterDataModifiedListener,
                        false);
                }
            }
        }
        return styleSheet;
    }

    /**
     * <b>DOM</b>: Implements {@link SVGStyleElement#getXMLspace()}.
     */
    /*
    public String getXMLspace() {
        return XMLSupport.getXMLSpace(this);
    }
    */

    /**
     * <b>DOM</b>: Implements {@link SVGStyleElement#setXMLspace(String)}.
     */
    /*
    public void setXMLspace(String space) throws DOMException {
        setAttributeNS(XMLSupport.XML_NAMESPACE_URI,
                       XMLSupport.XML_SPACE_ATTRIBUTE,
                       space);
    }
    */

    /**
     * <b>DOM</b>: Implements {@link SVGStyleElement#getType()}.
     */
    public String getType() {
        return getAttributeNS(null, HtmlAttribute.TYPE);
    }

    /**
     * <b>DOM</b>: Implements {@link SVGStyleElement#setType(String)}.
     */
    /*
    public void setType(String type) throws DOMException {
        setAttributeNS(null, SVG_TYPE_ATTRIBUTE, type);
    }
    */

    /**
     * The DOMCharacterDataModified listener.
     */
    protected class DOMCharacterDataModifiedListener
        implements EventListener {
        public void handleEvent(Event evt) {
            styleSheet = null;
        }
    }
}

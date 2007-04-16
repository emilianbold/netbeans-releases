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

import org.netbeans.modules.visualweb.api.designerapi.DesignerServiceHack;
import org.netbeans.modules.visualweb.api.insync.InSyncService;
import org.netbeans.modules.visualweb.designer.html.HtmlAttribute;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;
import java.net.URL;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.xerces.dom.CoreDocumentImpl;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sun.rave.designtime.markup.MarkupDesignBean;
import com.sun.rave.designtime.markup.MarkupMouseRegion;

// CVS note: This file used to be called XhtmlElement (same directory)
import org.w3c.dom.UserDataHandler;
// if you need to look at older CVS history

/**
 * XXX Originally in insync.
 * 
 * This class is attempts to merge in Batik specific "StylableElement" interface
 * implementations with Xerces nodes. That way we can continue to use Xerces2 for
 * parsing, and reuse a lot of the batik CSS handling code.
 * This was derived from Batik's org.apache.batik.dom.SVGStylableElement class
 * with a lot of SVG stuff thrown in, and subclassed for Xerces' Element classes
 * instead, so I've left the original license/copyright on the file.
 * However, most of the file here is new (not copied from Batik) and deals with
 * other aspects of our own DOM needs.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @author Tor Norbye
 */
class RaveElement extends org.apache.xerces.dom.ElementNSImpl
implements CSSStylableElement,  CSSEngine.StyleElementLink /*, RaveElement*/ {
    private static final long serialVersionUID = 3546078060169606198L;

    /**
     * The computed style map.
     */
//    protected StyleMap computedStyleMap;

//    private transient CSSStylableElement styleLink;

    // ----------------------------------------------------------------
    // TODO - remember to update copyFrom if you add additional fields!
    // ----------------------------------------------------------------
//    private transient boolean jspx;
    
//    private transient MarkupDesignBean bean;
//    private transient MarkupMouseRegion region;

    // XXX This seems to be a dummy field now.
    /** @todo replace usage with alternate/source/rendered */
//    transient RaveElement source;

//    /** Flag which indicates whether this node lives in the "source" DOM (typically the JSP file)
//     * or the "rendered" DOM (typically the HTML file). I chose to use "render"/"source" terms
//     * instead of HTML/JSP terms since the JSP can itself contain HTML markup which might muddy the
//     * issue. */
//    private transient boolean rendered;
//
//    /** If this is a "rendered" node (see {@link isRendered} then alternate points to its JSP
//     * source node, and if this is a node in the source DOM (the JSP itself), then alternate
//     * will point to the first, top level HTML node rendered from this node.   HTML markup
//     * in the JSP will have a 1-1 mapping to HTML markup in the rendered DOM. For JSF components,
//     * only the top level HTML rendered elements will point back to the source JSP node; children
//     * will be null indicating that they have no direct alternate node.
//     */
//    private transient RaveElement alternate;

    protected RaveElement() {
    }

    protected RaveElement(CoreDocumentImpl ownerDocument, String namespaceURI, String qualifiedName)
        throws DOMException {
        super(ownerDocument, namespaceURI, qualifiedName);
    }

    protected RaveElement(CoreDocumentImpl ownerDocument, String namespaceURI,
        String qualifiedName, String localName) throws DOMException {
        super(ownerDocument, namespaceURI, qualifiedName, localName);
    }

    // XXX Batik
    /**
     * Returns the computed style of this element/pseudo-element.
     */
    public StyleMap getComputedStyleMap(String pseudoElement) {
//        return computedStyleMap;
        return MarkupServiceImpl.getElementStyleMap(this);
    }

    // XXX Batik
    /**
     * Sets the computed style of this element/pseudo-element.
     */
    public void setComputedStyleMap(String pseudoElement, StyleMap sm) {
//        computedStyleMap = sm;
        MarkupServiceImpl.setElementStyleMap(this, sm);
    }

    // XXX Batik
    /**
     * Returns the ID of this element.
     */
    public String getXMLId() {
        return getAttributeNS(null, "id");
    }

    // XXX Batik
    /**
     * Returns the class of this element.
     */
    public String getCSSClass() {
        return getAttributeNS(null, "class");
    }

    // XXX Batik
    /**
     * Returns the CSS base URL of this element.
     */
    public URL getCSSBase() {
//        //String bu = XMLBaseSupport.getCascadedXMLBase(this);
//        URL bu = MarkupUtilities.getCascadedXMLBase(this);
//
//        if (bu == null) {
//            return null;
//        }
//
//        //return new URL(bu);
//        return bu;
        return MarkupUtilities.getCascadedXMLBase(this);
    }

    // XXX Batik
    /**
     * Tells whether this element is an instance of the given pseudo
     * class.
     */
    public boolean isPseudoInstanceOf(String pseudoClass) {
//        if (pseudoClass.equals("first-child")) {
//            Node n = getPreviousSibling();
//
//            while ((n != null) && (n.getNodeType() != ELEMENT_NODE)) {
//                n = n.getPreviousSibling();
//            }
//
//            return n == null;
//        } else if (pseudoClass.equals("link")) {
//            Node n = this;
//            String a = HtmlTag.A.toString();
//
//            while (n != null) {
//                if ((n.getNodeType() == Node.ELEMENT_NODE) && n.getNodeName().equals(a)) {
//                    // Only a link if the href attribute is set!
//                    if (((Element)n).hasAttribute(HtmlAttribute.HREF)) {
//                        return true;
//                    }
//                }
//
//                n = n.getParentNode();
//            }
//
//            return false;
//        }
//
//        return false;
        return MarkupServiceImpl.isElementPseudoInstanceOf(this, pseudoClass);
    }

//    public CSSEngine getEngine() {
////        return ((RaveDocument)getOwnerDocument()).getCssEngine();
//        return CssEngineServiceProvider.getDefault().getCssEngine(getOwnerDocument());
//    }

    // XXX Batik
    public void setStyleParent(CSSStylableElement styleLink) {
//        this.styleLink = styleLink;
        MarkupServiceImpl.setElementStyleParent(this, styleLink);
    }

    // XXX Batik
    public CSSStylableElement getStyleParent() {
//        return styleLink;
        return MarkupServiceImpl.getElementStyleParent(this);
    }

    /** During a clone operation, copy relevant info from the given
     * source element to this target element
     */
//    void copyFrom(RaveElementImpl from) {
    void copyFrom(RaveElement from) {
        // XXX Replacement for previous direct usage.
        // TODO Investigate if this is needed, there may not be
        // the same view for different models.
        DesignerServiceHack.getDefault().copyBoxForElement(from, this);
                
//        this.bean = from.bean;
        InSyncService.getProvider().copyMarkupDesignBeanForElement(from, this);
//        this.source = from.getSourceElement();
//        this.styleLink = from.styleLink;
//        setStyleParent(from.getStyleParent());
        MarkupServiceImpl.setElementStyleParent(this, MarkupServiceImpl.getElementStyleParent(from));
        
//        this.region = from.region;
        InSyncService.getProvider().copyMarkupMouseRegionForElement(from, this);
    }


//    // ------------ Implements DesignBeanElement ------------------------
//    public MarkupDesignBean getDesignBean() {
//        return bean;
//    }
//
//    public void setDesignBean(MarkupDesignBean bean) {
//        this.bean = bean;
//    }

//    public void setMarkupMouseRegion(MarkupMouseRegion region) {
//        this.region = region;
//    }
//
//    public MarkupMouseRegion getMarkupMouseRegion() {
//        return region;
//    }

//    /** Return the source element for this element, if different.
//     * For example, when a div element is a child of a h:form element,
//     * it gets rendered into the h:form document fragment. The source
//     * element for the div will be the div in the jsp.  For elements
//     * in the jsp, getSourceElement just returns itself.
//     * @todo Get rid of this (but first figure out how to replace it
//     *   by getSource in the presence of jsp node duplication)
//     * @deprecated This should go away in favor of getSource() -- which is 
//     *    not yet equivalent. Clean these up.
//     */
//    public RaveElement getSourceElement() {
//        if (source == null) {
//            return this;
//        } else {
//            return source;
//        }
//    }

//    /**
//     * Set the virtual parent
//     */
//    public static void setStyleParent(Element e, Element p) {
//        if (e instanceof CSSStylableElement && p instanceof CSSStylableElement) {
//            CSSEngine.StyleElementLink link = (CSSEngine.StyleElementLink)e;
//            link.setStyleParent((CSSStylableElement)p);
//        }
//    }

//    public boolean isRendered() {
//        return rendered;
//    }
//    public abstract boolean isRendered();

//    public RaveElement getRendered() {
//        if (rendered) {
//            // TODO - should anyone ask for this if they already have HTML ? The code
//            // might be confused so perhaps I should assert on this instead to pinpoint
//            // questionable code...
//            return this;
//        }
//
//        return alternate;
//    }
//
//    public RaveElement getSource() {
//        if (!rendered) {
//            // TODO - should anyone ask for this if they already have JSP ? The code
//            // might be confused so perhaps I should assert on this instead to pinpoint
//            // questionable code...
//            return this;
//        }
//
//        return alternate;
//    }
//
//    public void setSource(RaveElement source) {
//        rendered = true;
//        alternate = source;
//
//        // Make the source have a render reference to this element too, unless
//        // we know it's a nonvisual element that I'll never need a reference from.
//        // (And because some components can render script or style tags at the top
//        // level next to the bean render, it's important not to clobber the render
//        // pointer here.)
//        String name = getTagName();
//
//        // Don't store references to invisible markup (<script>, <style>, <input hidden>)
//        if (alternate != null) {
//            char first = name.charAt(0);
//
//            if (!(((first == 's') &&
//                    (name.equals(HtmlTag.SCRIPT.name) || name.equals(HtmlTag.STYLE.name))) ||
//                    ((first == 'i') && (name.equals(HtmlTag.INPUT.name)) &&
//                    getAttribute(HtmlAttribute.TYPE).equals("hidden")))) { // NOI18N
//                ((RaveElementImpl)alternate).rendered = false;
//                ((RaveElementImpl)alternate).alternate = this;
//            }
//        }
//    }
//
//    public void setRendered(RaveElement rendered) {
//        this.rendered = false;
//        alternate = rendered;
//
//        if (alternate != null) {
//            ((RaveElementImpl)alternate).rendered = true;
//            ((RaveElementImpl)alternate).alternate = this;
//        }
//    }

//    public boolean isJspx() {
////        return jspx;
//    }
//
//    public void setJspx(boolean jspx) {
////        this.jspx = jspx;
//    }

//    public /*RaveRenderNode*/ Node getSourceNode() {
//        return getSource();
//    }
//
//    public /*RaveRenderNode*/ Node getRenderedNode() {
//        return getRendered();
//    }

//    public void markRendered() {
//        this.rendered = true;
//    }

    public String toString() {
        // XXX The first part copied from Object.toString, because it was hidden by the superclass.
        return getClass().getName() + "@" + Integer.toHexString(hashCode()) 
//                + "[" + getTagName() + "], id=" + getAttribute("id") + ", rendered=" + rendered;
                + "[tagName=" + getTagName() + ", id=" + getAttribute("id") + "]"/*+ ", rendered=" + isRendered()*/; // NOI18N
    }
    
}

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
package org.netbeans.modules.visualweb.designer.cssengine;

import org.netbeans.modules.visualweb.spi.designer.cssengine.CssUserAgentInfo;
import org.apache.batik.css.engine.CSSContext;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.SystemColorSupport;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.ParsedURL;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Context used by the CSS parser code in Batik to get user agent specific stuff
 *
 * @author Tor Norbye
 */
public class DesignerContext implements CSSContext {
    private Document document;
    private UserAgent userAgent;
    private XhtmlCssEngine engine;
    private final CssUserAgentInfo userAgentInfo;

    public DesignerContext(Document document, UserAgent userAgent, CssUserAgentInfo userAgentInfo) {
        this.document = document;
        this.userAgent = userAgent;
        this.userAgentInfo = userAgentInfo;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public void setEngine(XhtmlCssEngine engine) {
        this.engine = engine;
    }

    /**
     * Returns the Value corresponding to the given system color.
     */
    public Value getSystemColor(String ident) {
        if (ident.equals(CSSConstants.CSS_LINKCOLOR_VALUE)) {
            return engine.getLinkColor();
        }

        return SystemColorSupport.getSystemColor(ident);
    }

    /**
     * Returns the value corresponding to the default font.
     */
    public Value getDefaultFontFamily() {
        // No cache needed since the default font family is asked only
        // one time on the root element (only if it does not have its
        // own font-family).
// XXX shouldn't this be cached?
//        RaveDocument doc = (RaveDocument)document;
        Document doc = document;
        //CSSStylableElement root = (CSSStylableElement)doc.getDocumentElement();
        CSSStylableElement root = null;
        if (doc != null) {
            root = (CSSStylableElement)doc.getDocumentElement();
        }
        String str = userAgent.getDefaultFontFamily();
        return engine.parsePropertyValue
            (root, CssConstants.CSS_FONT_FAMILY_PROPERTY, str);
    }

    /**
     * Returns a lighter font-weight.
     */
    public float getLighterFontWeight(float f) {
        return userAgent.getLighterFontWeight(f);
    }

    /**
     * Returns a bolder font-weight.
     */
    public float getBolderFontWeight(float f) {
        return userAgent.getBolderFontWeight(f);
    }

    /**
     * Returns the size of a px CSS unit in millimeters.
     */
    public float getPixelUnitToMillimeter() {
        return userAgent.getPixelUnitToMillimeter();
    }

    /**
     * Returns the size of a px CSS unit in millimeters.
     * This will be removed after next release.
     * @see #getPixelUnitToMillimeter()
     */
    public float getPixelToMillimeter() {
        return getPixelUnitToMillimeter();

    }

    /**
     * Returns the medium font size.
     */
    public float getMediumFontSize() {
        return userAgent.getMediumFontSize();
    }

    /**
     * Returns the width of the block which directly contains the
     * given element.
     */
    public float getBlockWidth(Element elt) {
//        return DesignerServiceHack.getDefault().getBlockWidth(elt);
        return userAgentInfo.getBlockWidth(document, elt);
    }

    /**
     * Returns the height of the block which directly contains the
     * given element.
     */
    public float getBlockHeight(Element elt) {
//        return DesignerServiceHack.getDefault().getBlockHeight(elt);
        return userAgentInfo.getBlockHeight(document, elt);
    }

    /**
     * This method throws a SecurityException if the resource
     * found at url and referenced from docURL
     * should not be loaded.
     * 
     * This is a convenience method to call checkLoadExternalResource
     * on the ExternalResourceSecurity strategy returned by 
     * getExternalResourceSecurity.
     *
     * @param resourceURL  url for the script, as defined in
     *        the script's xlink:href attribute. If that
     *        attribute was empty, then this parameter should
     *        be null
     * @param docURL  url for the document into which the 
     *        script was found.
     */
    public void 
        checkLoadExternalResource(ParsedURL resourceURL,
                                  ParsedURL docURL) throws SecurityException {
        //userAgent.checkLoadExternalResource(resourceURL,
        //                                     docURL);
    }

    /** Mark the document as dynamic - this means event listeners etc.
     * will be installed.
     * @see org.apache.batik.css.engine.CSSContext#isDynamic()
     */
    public boolean isDynamic() {
        return true;
    }
    
    public boolean isInteractive() {
        return true;
    }

}

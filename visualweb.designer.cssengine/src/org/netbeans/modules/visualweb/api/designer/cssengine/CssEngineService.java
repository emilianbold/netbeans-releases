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


package org.netbeans.modules.visualweb.api.designer.cssengine;


import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.batik.css.engine.StyleSheet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Interface to the CSS engine services.
 * <p>
 * <b><font color="red"><em>Important note: Do not provide implementation of this interface, use the provider to access it!</em></font></b>
 * </p>
 *
 * @author Peter Zavadsky
 */
public interface CssEngineService {

//    /** XXX Temporary, engine should be hidden. */
//    public XhtmlCssEngine getCssEngine(Document document);
//    /** XXX Temporary, engine should be hidden. */
//    public void setCssEngine(Document document, XhtmlCssEngine engine);

    /** Creates a css engine for specified <code>Document</code> and stores it internally. */
    public void createCssEngineForDocument(Document document, URL url);
    /** Removes the css engine for specified <code>Document</code> if there was one created before. */
    public void removeCssEngineForDocument(Document document);
    /** Reuses the css engine instance for the specified <code>document</code> if there already
     * exist one for the <code>originalDocument</code> instance. */
    public void reuseCssEngineForDocument(Document document, Document originalDocument);
    /** Gets collection of style classes for specific document, if there is corresponding css engine available
     *  otherwise it returns an empty collection. */
    public Collection getCssStyleClassesForDocument(Document document);
    /** Parse the given style declaration and return a map of properties
     * stored in it. The Map will have String keys which correspond to
     * property names, and String values which correspond to CSS
     * raw text for the values.
     * </p>
     * If there is no css engine created, it returns an empty map. */
    public Map getStyleMapFromStringForDocument(Document document, String style);
    /** Given a Map of style properties, serialize the set and compress
     * properties into shorthands, when possible. See styleToMap.
     * <p/>
     * If there is no css engine created, it returns an empty map. */
    public String getStringFromStyleMapForDocument(Document document, Map<String, String> styleMap);
    /** Adding a node which represents style sheet node for specified <code>Document</code>,
     * into the corresponding css engine, if there is one. */
    public void addTransientStyleSheetNodeForDocument(Document document, Node styleSheetNode);
    /** Clears the stylesheet nodes for the specified document, from the corresponding css engine,
     * if there is one. */
    public void clearTransientStyleSheetNodesForDocument(Document document);

    /** Gets the local styles string for a specified <code>Element</code> if there is corresponding css engine.
     * First any style settings in the set parameter array are applied, then any properties
     * pointed to by the remove array are applied.
     * @return The new style string */
    public String getUpdatedLocalStyleValuesForElement(Element element, StyleData[] stylesToSet, StyleData[] stylesToRemove)
    throws Exception;
    /** Sets style attribute in specified <code>Element</code> if there is corresponding css engine. */
    public void setStyleAttributeForElement(Element element, String value);
//    /** XXX Active modification (via the design bean/property).
//     * Updates local style value for specified <code>Element</code>. */
//    public void updateLocalStyleValuesForElement(Element element, StyleData[] stylesToSet, StyleData[] stylesToRemove);
//    /** XXX Active modification (via the design bean/property).
//     * Adds local style value for specified <code>Element</code>. */
//    public void addLocalStyleValueForElement(Element element, int style, String value);
//    /** XXX Active modification (via the design bean/property).
//     * Removes local style value for specified <code>Element</code>. */
//    public void removeLocalStyleValueForElement(Element element, int style);
    
    /** Clears computed styles for specified <code>Element</code> if there is corresponding css engine. */
    public void clearComputedStylesForElement(Element element);
    /** Sets silent error handler for specified <code>Document</code> if there is corresponding css engine. */
    public void setSilentErrorHandlerForDocument(Document document);
    /** Sets null error handler for specified <code>Document</code> if there is corresponding css engine. */
    public void setNullErrorHandlerForDocument(Document document);
//    /** Sets error handler for specified <code>Document</code> if there is corresponding css engine. */
//    public void setErrorHandlerForDocument(Document document, ErrorHandler errorHandler);
    /** Returns true if was inherited, as opposed to referenced directly by some rule.
     * for a specified <code>Element</code> if there is corresponsing css engine. */
    public boolean isInheritedStyleValueForElement(Element element, int propIndex);
    /** Returns true if the value is default,
     * for a specified <code>Element</code> if there is corresponsing css engine. */
    public boolean isDefaultStyleValueForElement(Element element, String pseudo, int propIndex);
    /** Refreshes all styles for specified <code>Document</code> if corresponding css engine exists. */
    public void refreshStylesForDocument(Document document);
    /** If the given property for an element has been computed, remove
     * its computed state so that it gets recomputed for the next
     * getComputedStyle call, for specified <code>Document</code> if corresponding
     * css engine exists.
     * XXX TODO it sounds horrible, revise. */
    public void uncomputeValueForElement(Element element, int propIndex);
    
    public int getXhtmlPropertyIndex(String property);
//    public int getXhtmlShorthandIndex(String property);
    
    /** Creates a 'preview' element. */
    public Element createPreviewElementForDocument(Document document, URL base, String styles);

    // Convenience method
    /** Returns <code>URL</code> of background image relative to the specified base <code>URL</code>. */
    public URL getBackgroundImageUrlForElement(Element element, URL baseUrl);
    
    ////////////////////////////////////////////////////////////////////////////
    // <methods needed to be replaced>
    /** XXX FIXME Get rid of this method, it uses batik classes, which is not acceptable.
     * Gets computed value for specified <code>Element</code> index if the corresponding css engine exists. */
    public CssValue getComputedValueForElement(Element element, int propIndex);
//    /** XXX FIXME Get rid of this method, it uses batik classes, which is not acceptable. */
//    public boolean isMediaMatchingForDocument(Document document, SACMediaList mediaList);
//    /** XXX FIXME Get rid of this method, it uses batik classes, which is not acceptable. */
//    public StyleDeclaration parseStyleDeclarationForElement(Element element, String value);
//    /** XXX FIXME Get rid of this method, it uses batik classes, which is not acceptable. */
//    public StyleSheet parseStyleSheetForDocument(Document document, InputSource inputSource, URL uri, String media, Object location);
    /** XXX FIXME Get rid of this method, it uses batik classes, which is not acceptable. */
    public StyleSheet parseStyleSheetForDocument(Document document, String rules, URL uri, String media, Object location);
    /** XXX FIXME Get rid of this method, it uses batik classes, which is not acceptable. */
    public StyleSheet parseStyleSheetForDocument(Document document, URL uri, String media, Object location);
//    /** XXX FIXME Get rid of this method, it uses batik classes, which is not acceptable. */
//    public ValueManager[] getXhtmlValueManagers();
    // </methods needed to be replaced>
    ////////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////////
    // XXX TODO Move to some other (non engine) interface.
    // <Non engine methods, might be separated>
    
    /** Returns true if the given CSS property for the given element
     * is set by an inline property setting.
     * TODO move to some other non css engine interface */
    public boolean isInlineStyleValue(Element element, int propIndex);
    
    // </Non engine methods, might be separated>
    ////////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////////
    // XXX Some utility methods used probably in DOMInspector only
    // TODO Revise what to do with them
    // <dominspector?>
    public String getAllStylesForElement(Element element);
    public String getAllComputedStylesForElement(Element element);
    public String getAllRulesForElement(Element element);
    // </dominspector?>
    ////////////////////////////////////////////////////////////////////////////
    
    // XXX Debugging only
    public void clearEngineStyleLookupCount();
    public int getEngineStyleLookupCount();
    
    // XXX Stylesheet cache
    public void flushStyleSheetCache();
    
    public String[] getCssIdentifiers(String propertyName);
    public String[] getCssProperties();
    
    public void setStyleParentForElement(Element element, Element styleParent);
    public Element getStyleParentForElement(Element element);
    
    /** Gets url strings. */
    public String[] getStyleResourcesForElement(Element element, String rules, Document doc, URL docUrl, int[] indexesToMatch);
    /** Gets style resources data. */
    public ResourceData[] getStyleResourcesForRules(String rules, Document doc, URL docUrl, URL base, int[] indexesToMatch);
    /** Gets style resources data. */
    public ResourceData[] getStyleResourcesForUrl(URL url, Document doc, URL docUrl, int[] indexesToMatch);
    
    /** Parse the given document, gets the info about syntax errors.
     * @param document The document to be parsed */
    public CssSyntaxErrorInfo[] parseCss(javax.swing.text.Document document);
    
    /** XXX Interface describing css syntax error. 
     * <p>
     * <b><font color="red"><em>Important note: Do not provide implementation of this interface, use the service method to access it!</em></font></b>
     * </p>
     * @see CssEngineService#parseCss */
    public interface CssSyntaxErrorInfo {
        public int getLineNumber();
        public String getLocalizedMessage();
    } // End of CssSyntaxErrorInfo.
}

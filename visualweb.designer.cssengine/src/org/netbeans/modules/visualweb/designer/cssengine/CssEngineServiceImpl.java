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


import com.sun.org.apache.xerces.internal.dom.DocumentImpl;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssEngineService;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssValue;
import org.netbeans.modules.visualweb.api.designer.cssengine.ResourceData;
import org.netbeans.modules.visualweb.api.designer.cssengine.StyleData;
import org.netbeans.modules.visualweb.api.designer.cssengine.XhtmlCss;
import org.netbeans.modules.visualweb.spi.designer.cssengine.CssUserAgentInfo;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.CSSStyleSheetNode;
import org.apache.batik.css.engine.ImportRule;
import org.apache.batik.css.engine.Rule;
import org.apache.batik.css.engine.StyleDeclaration;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.StyleRule;
import org.apache.batik.css.engine.StyleSetting;
import org.apache.batik.css.engine.StyleSheet;
import org.apache.batik.css.engine.StyleSheetCache;
import org.apache.batik.css.engine.value.AbstractValue;
import org.apache.batik.css.engine.value.ComputedValue;
import org.apache.batik.css.engine.value.IdentifierProvider;
import org.apache.batik.css.engine.value.InheritValue;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.URIValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueManager;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.SACMediaList;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.UserDataHandler;


/**
 * Impl of the <code>CssEngineService</code>.
 * XXX Impl code copied from various places before spread in the modules.
 *
 * @author Peter Zavadsky
 */
public final class CssEngineServiceImpl implements CssEngineService {

    private static String[] properties;

    private static final CssEngineServiceImpl instance = new CssEngineServiceImpl();

//    /** Maps <code>Document</code> to XHTML CSS engine. */
//    private final Map<Document, XhtmlCssEngine> document2engine = new WeakHashMap<Document, XhtmlCssEngine>();
    private static final String KEY_CSS_ENGINE = "vwpXhtmlCssEngine"; // NOI18N


    /** Creates a new instance of CssServiceImpl */
    private CssEngineServiceImpl() {
    }

    public static CssEngineService getDefault() {
        return get();
    }

    static CssEngineServiceImpl get() {
        return instance;
    }


    private XhtmlCssEngine getCssEngine(Document document) {
        if (document == null) {
            return null;
        }
//        XhtmlCssEngine ret;
//        synchronized (document2engine) {
//            ret = document2engine.get(document);
//        }
        return (XhtmlCssEngine)document.getUserData(KEY_CSS_ENGINE);
    }

//    public void setCssEngine(Document document, XhtmlCssEngine engine) {
//        synchronized (document2engine) {
//            document2engine.put(document, engine);
//        }
//    }

    public void createCssEngineForDocument(Document document, URL url) {
        if (document == null) {
            return;
        }
        CssUserAgentInfo userAgentInfo = getUserAgentInfo();
        XhtmlCssEngine engine = XhtmlCssEngine.create(document, url, userAgentInfo);
//        synchronized (document2engine) {
//            document2engine.put(document, engine);
//        }
        document.setUserData(KEY_CSS_ENGINE, engine, CssEngineDataHandler.getDefault());
    }

    /*private*/ static CssUserAgentInfo getUserAgentInfo() {
        // XXX FIXME The userAgentInfo might not be correct as singleton, but it should be
        // passed as argument (there might be more user agents available).
        CssUserAgentInfo userAgentInfo = (CssUserAgentInfo)Lookup.getDefault().lookup(CssUserAgentInfo.class);
        if (userAgentInfo == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new NullPointerException("No CssUserAgentInfo available! Using dummy one!")); // NOI18N
            userAgentInfo = new DummyUserAgentInfo();
        }
        return userAgentInfo;
    }

    
    private static class DummyUserAgentInfo implements CssUserAgentInfo {
        public float getBlockWidth(Document document, Element element) {
            return 0F;
        }

        public float getBlockHeight(Document document, Element element) {
            return 0F;
        }

        public int getDefaultFontSize() {
            return 16;
        }

        public String computeFileName(Object location) {
            return location == null ? null : location.toString();
        }

        public int computeLineNumber(Object location, int lineno) {
            return lineno;
        }

        public URL getDocumentUrl(Document document) {
            return null;
        }

        public void displayErrorForLocation(String message, Object location, int lineno, int column) {
        }

        public Element getHtmlBodyForDocument(Document document) {
            return null;
        }

        public DocumentFragment getHtmlDomFragmentForDocument(Document document) {
            return null;
        }
    } // End of DummyBlockSizeProvider.

    
    public void removeCssEngineForDocument(Document document) {
        if (document == null) {
            return;
        }
        
        XhtmlCssEngine engine;
//        synchronized (document2engine) {
//            engine = document2engine.remove(document);
//        }
        engine = (XhtmlCssEngine) document.getUserData(KEY_CSS_ENGINE);
        
        if (engine != null) {
            engine.dispose();
        }
    }
    
    public void reuseCssEngineForDocument(Document document, Document originalDocument) {
        if (document == null || originalDocument == null) {
            return;
        }
//        synchronized (document2engine) {
//            XhtmlCssEngine engine = document2engine.get(originalDocument);
//            document2engine.put(document, engine);
//        }
        XhtmlCssEngine engine = (XhtmlCssEngine)originalDocument.getUserData(KEY_CSS_ENGINE);
        document.setUserData(KEY_CSS_ENGINE, engine, CssEngineDataHandler.getDefault());
    }

    public Collection<String> getCssStyleClassesForDocument(Document document) {
        XhtmlCssEngine engine = getCssEngine(document);
        
        if (engine == null) {
            return Collections.emptySet();
        } else {
            return engine.getStyleClasses();
        }
    }

    public Map<String, String> getStyleMapFromStringForDocument(Document document, String style) {
        XhtmlCssEngine engine = getCssEngine(document);
        
        if (engine == null) {
            return Collections.emptyMap();
        } else {
            return engine.styleToMap(style);
        }
    }

    public String getStringFromStyleMapForDocument(Document document, Map<String, String> styleMap) {
        XhtmlCssEngine engine = getCssEngine(document);
        
        if (engine == null) {
            return ""; // NOI18N
        } else {
            return engine.mapToStyle(styleMap);
        }
    }

    public void addTransientStyleSheetNodeForDocument(Document document, Node styleSheetNode) {
        if (!(styleSheetNode instanceof CSSStyleSheetNode)) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalArgumentException("Node has to be of CSSStyleSheetNode type, node=" + styleSheetNode)); // NOI18N
            return;
        }
        
        XhtmlCssEngine engine = getCssEngine(document);
        
        if (engine != null) {
            engine.addTransientStyleSheetNode((CSSStyleSheetNode)styleSheetNode);
        }
    }

    public void clearTransientStyleSheetNodesForDocument(Document document) {
        XhtmlCssEngine engine = getCssEngine(document);
        
        if (engine != null) {
            engine.clearTransientStyleSheetNodes();
        }
    }
    
//    public void addLocalStyleValueForElement(Element element, int style, String value) {
//        List set = new ArrayList(1);
//        set.add(new StyleData(style, value));
//        updateLocalStyleValuesForElement(element,(StyleData[])set.toArray(new StyleData[set.size()]), null);
//    }
//    
//    public void removeLocalStyleValueForElement(Element element, int style) {
//        List remove = new ArrayList(1);
//        remove.add(new StyleData(style));
//        updateLocalStyleValuesForElement(element, null, (StyleData[])remove.toArray(new StyleData[remove.size()]));
//    }
//    
//    public void updateLocalStyleValuesForElement(Element element, StyleData[] stylesToSet, StyleData[] stylesToRemove) {
//        try {
////            String value = engine.getUpdatedLocalStyleValues(element, set, remove);
//            String value = getUpdatedLocalStyleValuesForElement(element, stylesToSet, stylesToRemove);
//            
////            if (element instanceof RaveElement) {
////                RaveElement raveElement = (RaveElement)element;
//////                DesignBean markupBean = raveElement.getDesignBean();
////                DesignBean markupBean = InSyncService.getProvider().getMarkupDesignBeanForElement(raveElement);
//            if (element != null) {
//                DesignBean markupBean = InSyncService.getProvider().getMarkupDesignBeanForElement(element);
//                if (markupBean != null) {
//                    DesignProperty property = markupBean.getProperty(HtmlAttribute.STYLE);
//                    if (property != null) {
//                        try {
//                            if ((value != null) && (value.length() > 0)) {
//                                property.setValue(value);
//                            } else {
//                                property.unset();
//                            }
//
//                            return;
//                        } catch (Exception ex) {
//                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//                            // For some reason the above throws exceptions
//                            // sometimes, not sure why org.w3c.dom.DOMException:
//                            // NOT_FOUND_ERR: An attempt is made to reference a
//                            // node in a context where it does not exist.  TODO
//                            // - figure out WHY!  For now just swallow since
//                            // there's nothing more we can do about it.
//                            // (Update: It think this may be fixed now)
//                        }
//                    }
//                }
//            }
//
//            // If the above fails (shouldn't)
////            engine.setStyleAttributeValue(element, value);
//            setStyleAttributeForElement(element, value);
//        } catch (Exception e) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
//        }
//    }
    
    public String getUpdatedLocalStyleValuesForElement(Element element,
    StyleData[] stylesToSet, StyleData[] stylesToRemove) throws Exception {
        if (!(element instanceof CSSStylableElement)) {
            return ""; // NOI18N
        }

        Document document = element.getOwnerDocument();
        XhtmlCssEngine engine = getCssEngine(document);
        
        if (engine != null) {
            CSSStylableElement elt = (CSSStylableElement)element;
            return engine.getUpdatedLocalStyleValues(elt,
                    convertToStyleSettings(stylesToSet),
                    convertToStyleSettings(stylesToRemove));
        } else {
            return ""; // NOI18N
        }
    }
    
    private static StyleSetting[] convertToStyleSettings(StyleData[] stylesData) {
        if (stylesData == null) {
            return null;
        }
        
        List<StyleSetting> styleSettings = new ArrayList<StyleSetting>();
        for (int i = 0; i < stylesData.length; i++) {
            StyleData sd = stylesData[i];
            styleSettings.add(new StyleSetting(sd.getIndex(), sd.getValue()));
        }
        return styleSettings.toArray(new StyleSetting[styleSettings.size()]);
    }
    
    public void setStyleAttributeForElement(Element element, String value) {
        if (element == null) {
            return;
        }
        
        Document document = element.getOwnerDocument();
        XhtmlCssEngine engine = getCssEngine(document);
        
        if (engine != null) {
            engine.setStyleAttributeValue(element, value);
        }
    }

    public void clearComputedStylesForElement(Element element) {
        if (element == null) {
            return;
        }
        
        Document document = element.getOwnerDocument();
        XhtmlCssEngine engine = getCssEngine(document);
        
        if (engine != null) {
            engine.clearComputedStyles(element);
        }
    }
    
    public void setSilentErrorHandlerForDocument(Document document) {
        setErrorHandlerForDocument(document, XhtmlCssEngine.SILENT_ERROR_HANDLER);
    }

    public void setNullErrorHandlerForDocument(Document document) {
        setErrorHandlerForDocument(document, null);
    }
    
    private void setErrorHandlerForDocument(Document document, ErrorHandler errorHandler) {
        XhtmlCssEngine engine = getCssEngine(document);
        
        if (engine != null) {
            engine.setErrorHandler(errorHandler);
        }
    }
    
    public boolean isInheritedStyleValueForElement(Element element, int propIndex) {
        if (!(element instanceof CSSStylableElement)) {
            return false;
        }

        Document document = element.getOwnerDocument();
        XhtmlCssEngine engine = getCssEngine(document);
        
        if (engine != null) {
            CSSStylableElement elt = (CSSStylableElement)element;
            return engine.isInheritedValue(elt, propIndex);
        } else {
            return false;
        }
    }

    public boolean isDefaultStyleValueForElement(Element element, String pseudo, int propIndex) {
        if (!(element instanceof CSSStylableElement)) {
            return false;
        }
        
        Document document = element.getOwnerDocument();
        XhtmlCssEngine engine = getCssEngine(document);
        
        if (engine != null) {
            CSSStylableElement elt = (CSSStylableElement)element;
            return engine.isDefaultValue(elt, pseudo, propIndex);
        } else {
            return false;
        }
    }

    public void refreshStylesForDocument(Document document) {
        XhtmlCssEngine engine = getCssEngine(document);
        
        if (engine != null) {
            engine.refreshStyles();
        }
    }


    // <Non engine methods, might be separated>
    // XXX Moved from XhtmlCssEngine.
    public boolean isInlineStyleValue(Element element, int propidx) {
        if (!(element instanceof CSSStylableElement)) {
            return false;
        }
        
        CSSStylableElement elt = (CSSStylableElement)element;
        
        String pseudo = ""; // Pending // NOI18N
        StyleMap sm = elt.getComputedStyleMap(pseudo);

        if (sm == null) {
            return false;
        }

        Value value = sm.getValue(propidx);

        if (value == null) {
            return false;
        }

        return sm.getOrigin(propidx) == StyleMap.INLINE_AUTHOR_ORIGIN;
    }
    // </Non engine methods, might be separated>

    // <dominspector?>
    public String getAllStylesForElement(Element element) {
        if (!(element instanceof CSSStylableElement)) {
            return "";
        }

        Document document = element.getOwnerDocument();        
        XhtmlCssEngine engine = getCssEngine(document);
        
        if (engine != null) {
            CSSStylableElement elt = (CSSStylableElement)element;
            StyleMap map = elt.getComputedStyleMap(null);
            if (map != null) {
                return map.toString(engine);
            }
        }
        
        return ""; // NOI18N
    }

    // XXX Content moved from designer/CssLookup.
    public String getAllComputedStylesForElement(Element element) {
        if (!(element instanceof CSSStylableElement)) {
            return "";
        }
        
        Document document = element.getOwnerDocument();
        XhtmlCssEngine engine = getCssEngine(document);
        
        if (engine == null) {
            return ""; // NOI18N
        }
        
        CSSStylableElement elt = (CSSStylableElement)element;
        StyleMap map = elt.getComputedStyleMap(null);
//        StyleMap cascaded = getCascadedStyleMap(elt);
        StyleMap cascaded = engine.getCascadedStyleMap(elt, null);
        
        StringBuffer sb = new StringBuffer(400);

        for (int i = 0, n = map.getSize(true); i < n; i++) {
            Value v = engine.getComputedStyle((CSSStylableElement)element, null, i);

            ValueManager vm = engine.getValueManagers()[i];
            Value deflt = vm.getDefaultValue();

            if (!v.equals(deflt)) { // XXX Use !.equals instead?
                sb.append("*");
            }

            int pos = sb.length();
            sb.append(engine.getPropertyName(i));
            sb.append(": ");
            sb.append(v);

            sb.append("       ");

            if (cascaded.getOrigin(i) == StyleMap.NON_CSS_ORIGIN) {
                sb.append("(from HTML attribute) ");
            }

            if (cascaded.getValue(i) == InheritValue.INSTANCE) {
                sb.append("(inherited) ");
            }

            if (map.isImportant(i)) {
                sb.append("!important ");
            }

            if (v instanceof AbstractValue) {
                AbstractValue av = (AbstractValue)v;
//                String fullname = ((XhtmlCssEngine)engine).computeFilename(av);
//                String fullname = InSyncService.getProvider().computeFileName(av.getLocation());
                String fullname = engine.computeFileName(av.getLocation());

                if (fullname != null) {
                    String location = null;
                    String filename = fullname.substring(fullname.lastIndexOf('/') + 1);

                    // +1: numbers are generally 1-based
//                    int lineno = ((XhtmlCssEngine)engine).computeLineNumber(av) + 1;
//                    int lineno = InSyncService.getProvider().computeLineNumber(av.getLocation(), av.getLineNumber()) + 1;
                    int lineno = engine.computeLineNumber(av.getLocation(), av.getLineNumber()) + 1;

                    if ((filename.length() == 0) || filename.equals("default.css")) { // NOI18N
                    } else if ((lineno > 0) && (filename.length() > 0)) {
                        location = filename + ':' + Integer.toString(lineno);
                    } else {
                        location = filename;
                    }

                    if (location != null) {
                        int currPos = sb.length();
                        sb.append(' ');

                        while (currPos < (pos + 40)) {
                            sb.append(' ');
                            currPos++;
                        }

                        sb.append(location);
                    }
                }
            }

            sb.append('\n');
        }

        return sb.toString();
    }
    
    public String getAllRulesForElement(Element element) {
        if (element == null) {
            return ""; // NOI18N
        }
        
        Document document = element.getOwnerDocument();
        XhtmlCssEngine engine = getCssEngine(document);
        
        if (engine != null) {
            return engine.getMatchingRules(element, false);
        } else {
            return ""; // NOI18N
        }
    }
    // </dominspector?>

    // XXX
    public void uncomputeValueForElement(Element element, int propIndex) {
        if (!(element instanceof CSSStylableElement)) {
            return;
        }

        Document document = element.getOwnerDocument();
        XhtmlCssEngine engine = getCssEngine(document);
        
        if (engine == null) {
            return;
        }

//        RaveElement xel = (RaveElement)e;
        CSSStylableElement elt = (CSSStylableElement)element;
        StyleMap sm = elt.getComputedStyleMap(null);

        if (sm != null) {
            Value v = sm.getValue(propIndex);

            if (v instanceof ComputedValue) {
                ComputedValue cv = (ComputedValue)v;
                sm.putComputed(propIndex, false);
                sm.putValue(propIndex, cv.getCascadedValue());
            }
        }
    }

    public CssValue getComputedValueForElement(Element element, int propIndex) {
        return getComputedValueImplForElement(element, propIndex);
    }
    
    CssValueImpl getComputedValueImplForElement(Element element, int propIndex) {
        if (!(element instanceof CSSStylableElement)) {
            return null;
        }
        

        Document document = element.getOwnerDocument();
        XhtmlCssEngine engine = getCssEngine(document);
        
        if (engine == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("There is no css engine associated with the element's document," // NOI18N
                    + "\nelement=" + element // NOI18N
                    + "\ndocument=" + document // NOI18N
                    + "\ndocument class=" + (document == null ? null : document.getClass()) // NOI18N
                    + "\ndocument hashCode=" + (document == null ? null : document.hashCode()))); // NOI18N
            return null;
        }

        CSSStylableElement elt = (CSSStylableElement)element;
        Value value = engine.getComputedStyle(elt, null, propIndex);

        return (CssValueImpl)CssValueFactory.createCssValue(value);
    }

    private boolean isMediaMatchingForDocument(Document document, SACMediaList mediaList) {
        XhtmlCssEngine engine = getCssEngine(document);
        
        if (engine == null) {
            return false;
        }

        return engine.mediaMatch(mediaList);
    }
    
    private StyleDeclaration parseStyleDeclarationForElement(Element element, String value) {
        if (!(element instanceof CSSStylableElement)) {
            return new StyleDeclaration();
        }
        
        Document document = element.getOwnerDocument();
        XhtmlCssEngine engine = getCssEngine(document);
        
        if (engine == null) {
            return new StyleDeclaration();
        }

        CSSStylableElement elt = (CSSStylableElement)element;
        return engine.parseStyleDeclaration(elt, value);
    }

    private StyleSheet parseStyleSheetForDocument(Document document, InputSource inputSource, URL uri, String media, Object location) {
        XhtmlCssEngine engine = getCssEngine(document);
        
        if (engine == null) {
            return new StyleSheet();
        }

        return engine.parseStyleSheet(inputSource, uri, media, location);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // <methods needed to be replaced>
    // FIXME get rid of this method (batik return type) api has to be batik independent.
    public StyleSheet parseStyleSheetForDocument(Document document, String rules, URL uri, String media, Object location) {
        XhtmlCssEngine engine = getCssEngine(document);
        
        if (engine == null) {
            return new StyleSheet();
        }

        return engine.parseStyleSheet(rules, uri, media, location);
    }
    
    public StyleSheet parseStyleSheetForDocument(Document document, URL uri, String media, Object location) {
        XhtmlCssEngine engine = getCssEngine(document);
        
        if (engine == null) {
            return new StyleSheet();
        }

        return engine.parseStyleSheet(uri, media, location);
    }
    // </methods needed to be replaced>
    ////////////////////////////////////////////////////////////////////////////
    
    private static ValueManager[] getXhtmlValueManagers() {
        return XhtmlCssEngine.XHTML_VALUE_MANAGERS;
    }

    public int getXhtmlPropertyIndex(String property) {
        return XhtmlCssEngine.getXhtmlPropertyIndex(property);
    }
    
    private static int getXhtmlShorthandIndex(String property) {
        return XhtmlCssEngine.getXhtmlShorthandIndex(property);
    }

    public Element createPreviewElementForDocument(Document document, URL base, String styles) {
        return new PreviewElement(document, base, styles);
    }

    public void clearEngineStyleLookupCount() {
        CSSEngine.styleLookupCount = 0;
    }

    public int getEngineStyleLookupCount() {
        return CSSEngine.styleLookupCount;
    }

    public void flushStyleSheetCache() {
        // XXX
        StyleSheetCache.getInstance().flush();
    }

    public String[] getCssIdentifiers(String propertyName) {
        StringMap map = getIdentifiers(propertyName);

        if (map == null) {
            return new String[0];
        }

        int count = map.size();
        List<String> keys = new ArrayList<String>(count);
        Iterator<String> it = map.keys();

        while (it.hasNext()) {
            String string = it.next();
            keys.add(string);
        }

        keys.add("inherit"); // NOI18N
        Collections.sort(keys);

        return keys.toArray(new String[keys.size()]);
    }

    private StringMap getIdentifiers(String property) {
//        int index = XhtmlCssEngine.getXhtmlPropertyIndex(property);
        int index = getXhtmlPropertyIndex(property);

        if (index == -1) {
//            index = XhtmlCssEngine.getXhtmlShorthandIndex(property);
            index = getXhtmlShorthandIndex(property);

            if (index == -1) {
                return null;
            }

            // XXX TODO! What do we do here?
            return null;
        }

//        ValueManager vm = XhtmlCssEngine.XHTML_VALUE_MANAGERS[index];
        ValueManager vm = getXhtmlValueManagers()[index];

        if (vm instanceof IdentifierProvider) {
            return ((IdentifierProvider)vm).getIdentifierMap();
        }

        return null;
    }

    public String[] getCssProperties() {
        if (properties == null) {
//            ValueManager[] vms = XhtmlCssEngine.XHTML_VALUE_MANAGERS;
            ValueManager[] vms = getXhtmlValueManagers();
            List<String> list = new ArrayList<String>(vms.length);

            for (int i = 0, n = vms.length; i < n; i++) {
                String property = vms[i].getPropertyName();

                if (property.charAt(0) != '-') { // don't include vendor-specific properties
                    list.add(property);
                }
            }

            Collections.sort(list);
            properties = list.toArray(new String[list.size()]);
        }

        return properties;
    }
    
    public void setStyleParentForElement(Element element, Element styleParent) {
        if (element == styleParent) {
            // #6490385 Don't do that for the same instances.
            return;
        }
        if (element instanceof CSSStylableElement
        && styleParent instanceof CSSStylableElement) {
            CSSEngine.StyleElementLink link = (CSSEngine.StyleElementLink)element;
            link.setStyleParent((CSSStylableElement)styleParent);
        }
    }
    
    public Element getStyleParentForElement(Element element) {
        if (element instanceof CSSEngine.StyleElementLink) {
            return ((CSSEngine.StyleElementLink)element).getStyleParent();
        }
        return null;
    }

    public String[] getStyleResourcesForElement(Element element, String rules, Document doc, URL docUrl, int[] indexesToMatch) {
//        URL docUrl = InSyncService.getProvider().getUrl(doc);
        createCssEngineForDocument(doc, docUrl);
        StyleDeclaration sd = parseStyleDeclarationForElement(element, rules);
        return getStyleResourcesFromStyleDeclaration(sd, indexesToMatch);
    }
    
    public ResourceData[] getStyleResourcesForRules(String rules, Document doc, URL docUrl, URL base, int[] indexesToMatch) {
//        URL docUrl = InSyncService.getProvider().getUrl(doc);
        createCssEngineForDocument(doc, docUrl);
        InputSource is = new InputSource(new StringReader(rules));
        StyleSheet ss = parseStyleSheetForDocument(doc, is, base, "all", base); // NOI18N
        return getStyleResourcesFromStyleSheet(doc, ss, indexesToMatch);
    }
    
    public ResourceData[] getStyleResourcesForUrl(URL url, Document doc, URL docUrl, int[] indexesToMatch) {
//        URL docUrl = InSyncService.getProvider().getUrl(doc);
        createCssEngineForDocument(doc, docUrl);
        InputSource is = new InputSource(url.toString());
        StyleSheet ss = parseStyleSheetForDocument(doc, is, url, "all", url); // NOI18N
        return getStyleResourcesFromStyleSheet(doc, ss, indexesToMatch);
    }
    
    /**
     * Gets an array of urlStrings.
     * Adds the rules matching the element/pseudo-element of given style
     * declaration to the list
     */
    private static String[] getStyleResourcesFromStyleDeclaration(StyleDeclaration sd, int[] indexesToMatch) {
        List<String> urlStrings = new ArrayList<String>();
        for (int i = 0, m = sd.size(); i < m; i++) {
            int idx = sd.getIndex(i);

            boolean matches = false;
            for (int j = 0; j < indexesToMatch.length; j++) {
                if (idx == indexesToMatch[j]) {
                    matches = true;
                    break;
                }
            }
            
//            if ((idx == XhtmlCss.BACKGROUND_IMAGE_INDEX) ||
//                    (idx == XhtmlCss.LIST_STYLE_IMAGE_INDEX)) {
            if (matches) {
                // If I support audio: cue-before, cure-after,
                // play-during as well
                Value v = sd.getValue(i);

                if (v instanceof URIValue) {
                    URIValue uv = (URIValue)v;
                    String urlString = uv.getRawCssText();

//                    if (rewrite.get(urlString) == null) {
//                        // Import the image, as newUrl
//                        String projectUrl = copyResource(urlString);
//
//                        if (projectUrl != null) {
//                            rewrite.put(urlString, projectUrl);
//                        }
//                    }
                    if (urlStrings.contains(urlString)) {
                        continue;
                    }
                    
                    urlStrings.add(urlString);
                }
            }
        }
        return urlStrings.toArray(new String[urlStrings.size()]);
    }
    
    /** Gets map of urlString to relPath.
     * Adds the rules matching the element/pseudo-element of given style
     * sheet to the list.
     */
    private ResourceData[] getStyleResourcesFromStyleSheet(Document doc, StyleSheet ss, int[] indexesToMatch) {
//        Map rewrite = new HashMap();
        List<ResourceData> resourceData = new ArrayList<ResourceData>();
        
        int len = ss.getSize();

        for (int i = 0; i < len; i++) {
            Rule r = ss.getRule(i);

            switch (r.getType()) {
            case StyleRule.TYPE:

                StyleRule style = (StyleRule)r;
                StyleDeclaration sd = style.getStyleDeclaration();
//                rewrite.putAll(importStyleResourcesFromStyleDeclaration(depth, sd));
                String[] urlStrings = getStyleResourcesFromStyleDeclaration(sd, indexesToMatch);
//                rewrite.putAll(importStyleResources(urlStrings));
                resourceData.add(new UrlStringsResourceData(urlStrings));
                break;

            /*case MediaRule.TYPE:*/
            case ImportRule.TYPE:

                ImportRule mr = (ImportRule)r;

//                XhtmlCssEngine ces = CssEngineServiceProvider.getDefault().getCssEngine(doc);
//                if (ces.mediaMatch(mr.getMediaList())) { // XXX todo
                if (isMediaMatchingForDocument(doc, mr.getMediaList())) {

                    URL url = mr.getURI();
//                    String parent = new File(url.getPath()).getParent() + "/";
//                    URL oldBase = context.base;
//
//                    try {
//                        context.base =
//                            new URL(url.getProtocol(), url.getHost(), url.getPort(), parent);
//                    } catch (MalformedURLException mfu) {
//                        // XXX shouldn't happen
//                        ErrorManager.getDefault().notify(mfu);
//
//                        return rewrite;
//                    }
//
//                    String urlString = mr.getRelativeUri();
//                    String relPath = handleStyleSheet(depth + 1, urlString, url);
//                    context.base = oldBase;
                    String urlString = mr.getRelativeUri();
//                    String relPath;
//                    try {
//                        relPath = importStyleSheetResource(depth, url, urlString);
//                    } catch (MalformedURLException mfu) {
//                        // XXX shouldn't happen
//                        ErrorManager.getDefault().notify(mfu);
//                        return rewrite;
//                    }
//
//                    if (relPath != null) {
//                        rewrite.put(urlString, relPath);
//                    }
                    resourceData.add(new UrlResourceData(url, urlString));
                }

                break;
            }
        }
        
        return resourceData.toArray(new ResourceData[resourceData.size()]);
    }
    
    public CssSyntaxErrorInfo[] parseCss(javax.swing.text.Document document) {
        if (document == null) {
            throw new IllegalArgumentException("Parameter document may not be null!"); // NOI18N
        }

        Document fakeDocument = new FakeDocument();
        createCssEngineForDocument(fakeDocument, null);
        
        List<CssSyntaxErrorInfo> parseErrors = new ArrayList<CssSyntaxErrorInfo>();
        ErrorHandler handler = new DefaultErrorHandler(parseErrors);
        setErrorHandlerForDocument(fakeDocument, handler);

        String rules;

        try {
            rules = document.getText(0, document.getLength());
        } catch (javax.swing.text.BadLocationException e) {
            ErrorManager.getDefault().notify(e);

            return parseErrors.toArray(new CssSyntaxErrorInfo[parseErrors.size()]);
        }

//        engine.parseStyleSheet(rules, null, "all", null);
        parseStyleSheetForDocument(fakeDocument, rules, null, "all", null); // NOI18N
//        engine.setErrorHandler(null);
        setErrorHandlerForDocument(fakeDocument, null);
        
        return parseErrors.toArray(new CssSyntaxErrorInfo[parseErrors.size()]);
    }

    public URL getBackgroundImageUrlForElement(Element element, URL baseUrl) {
//        Value value = CssLookup.getValue(element, XhtmlCss.BACKGROUND_IMAGE_INDEX);
        CssValue cssValue = getComputedValueForElement(element, XhtmlCss.BACKGROUND_IMAGE_INDEX);
        
//        if (value == CssValueConstants.NONE_VALUE) {
        if (CssProvider.getValueService().isNoneValue(cssValue)) {
            return null;
        }

//        String urlString = value.getStringValue();
        String urlString = cssValue.getStringValue();
        try {
            return new URL(baseUrl, urlString);
        } catch (MalformedURLException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        
        return null;
    }
    
    private static class UrlStringsResourceData implements ResourceData.UrlStringsResourceData {
        private final String[] urlStrings;
        public UrlStringsResourceData(String[] urlStrings) {
            this.urlStrings = urlStrings;
        }
        public String[] getUrlStrings() {
            return urlStrings;
        }
    } // End of UrlStringsResourceData.
    
    private static class UrlResourceData implements ResourceData.UrlResourceData {
        private final URL url;
        private final String urlString;
        public UrlResourceData(URL url, String urlString) {
            this.url = url;
            this.urlString = urlString;
        }
        public URL getUrl() {
            return url;
        }
        public String getUrlString() {
            return urlString;
        }
    } // End of UrlResourceData.
    
    /** XXX Fake document, to be able to create engine.
     * TODO Better is just to impl the <code>Document</code> interface, without dep on xerces. */
    private static class FakeDocument extends DocumentImpl {
    } // End of FakeDocument.
    
    
    // XXX  Add the error type and error code also.
    private static class DefaultErrorHandler implements ErrorHandler {
        private final List<CssSyntaxErrorInfo> parseErrors;
        public DefaultErrorHandler(List<CssSyntaxErrorInfo> parseErrors){
            this.parseErrors = parseErrors;
        }
        public void warning(CSSParseException cpe) {
            //System.out.println("CSS Parse Warning");
            //System.out.println(cpe.getLineNumber() + ": " + cpe.getLocalizedMessage());
            parseErrors.add(new DefaultCssSyntaxErrorInfo(cpe.getLineNumber(), cpe.getLocalizedMessage()));
        }
        public void error(CSSParseException cpe) {
            //System.out.println("CSS Parse Error");
            //System.out.println(cpe.getLineNumber() + ": " + cpe.getLocalizedMessage());
            parseErrors.add(new DefaultCssSyntaxErrorInfo(cpe.getLineNumber(), cpe.getLocalizedMessage()));
        }
        public void fatalError(CSSParseException cpe) {
            //System.out.println("CSS Parse Fatal Error");
            //System.out.println(cpe.getLineNumber() + ": " + cpe.getLocalizedMessage());
            parseErrors.add(new DefaultCssSyntaxErrorInfo(cpe.getLineNumber(), cpe.getLocalizedMessage()));
        }
    } // End of DefaultErrorHandler.

    
    private static class DefaultCssSyntaxErrorInfo implements CssSyntaxErrorInfo {
        private final int lineNumber;
        private final String localizedMessage;

        public DefaultCssSyntaxErrorInfo(int lineNumber, String localizeMessage) {
            this.lineNumber = lineNumber;
            this.localizedMessage = localizeMessage;
        }
        
        public int getLineNumber() {
            return lineNumber;
        }

        public String getLocalizedMessage() {
            return localizedMessage;
        }
    } // End of DefaultCssSyntaxErrorInfo.
    
    
    private static class CssEngineDataHandler implements UserDataHandler {
        private static final CssEngineDataHandler INSTANCE = new CssEngineDataHandler();
        
        public static CssEngineDataHandler getDefault() {
            return INSTANCE;
        }
        
        public void handle(short operation, String key, Object data, Node src, Node dst) {
            // No op.
        }
    } // End of CssEngineDataHandler.
    
}

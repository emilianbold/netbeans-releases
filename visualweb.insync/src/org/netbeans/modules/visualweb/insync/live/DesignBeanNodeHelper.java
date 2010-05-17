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

package org.netbeans.modules.visualweb.insync.live;

import java.beans.PropertyDescriptor;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssComputedValue;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssValue;
import org.netbeans.modules.visualweb.api.designer.cssengine.XhtmlCss;
import org.netbeans.modules.visualweb.api.designer.markup.MarkupService;
import org.netbeans.modules.visualweb.designer.html.HtmlAttribute;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.Position;
import com.sun.rave.designtime.markup.MarkupPosition;
import org.netbeans.modules.visualweb.insync.InSyncServiceProvider;
import org.netbeans.modules.visualweb.insync.UndoEvent;
import org.netbeans.modules.visualweb.insync.Util;
import org.netbeans.modules.visualweb.insync.faces.FacesPageUnit;
import org.netbeans.modules.visualweb.insync.faces.MarkupBean;
import org.netbeans.modules.visualweb.insync.markup.MarkupUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.propertyeditors.StandardUrlPropertyEditor;
import java.awt.Color;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import javax.faces.component.UIViewRoot;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XXX Providing the fake properties for the <code>DesignBeanNode</code>
 * if the one represents the root container of Page design bean context.
 * This is a continuation of hack from the past made in designer using
 * DocumentComp and DocumentCompNode.
 *
 * @author Peter Zavadsky
 * @author Tor Norbye (old code, parts of impl of set/getValue methods)
 */
final class DesignBeanNodeHelper {
    
    /** Creates a new instance of DesignBeanNodeHelper */
    private DesignBeanNodeHelper() {
    }
    
    
    /** XXX Adds additional properties to the specified sheet
     * and for specified <code>DesignBean</code>.
     * @return same sheet with added properties */
    static void addFakePageProperties(Sheet sheet, DesignBean designBean) {
        
        Sheet.Set set = addSetIntoSheet(sheet,
                "Appearance", // NOI18N
                NbBundle.getMessage(DesignBeanNodeHelper.class, "LBL_Appearance"));
        
        boolean isPortlet = isPortlet(designBean);
        boolean isFragment = isFragment(designBean);
        
        if (isPortlet || !isFragment) {
            // Add Background property
            set.put(new BackgroundProperty(designBean));
        }
        if (!isPortlet && !isFragment) {
            // Add Background Image property
            set.put(new BackgroundImageProperty(designBean));
        }
        // Add Page Layout property
        set.put(new LayoutProperty(designBean));
        if (isPortlet || isFragment) {
            // Add Width property
            // TODO
            set.put(new WidthProperty(designBean));
            // Add Height property
            // TODO
            set.put(new HeightProperty(designBean));
        }
        if (!isPortlet && !isFragment) {
            // Add Style sheet property
            set.put(new StyleSheetProperty(designBean));
            // Add Title property
            set.put(new TitleProperty(designBean));
            
            set = addSetIntoSheet(sheet,
                    "Advanced", // NOI18N
                    NbBundle.getMessage(DesignBeanNodeHelper.class, "LBL_Advanced"));
            // Add Response Encoding property
            set.put(new EncodingProperty(designBean));
            // Add Language property
            set.put(new LanguageProperty(designBean));
        }
    }
    
    private static Set addSetIntoSheet(Sheet sheet, String name, String displayName) {
        Sheet.Set set = sheet.get(name);
        if (set == null) {
            set = new Sheet.Set();
            set.setName(name);
            set.setDisplayName(displayName);
            //            //ss.setExpert();
            //            // would like to set default expanded state too...
            //            if (descr != null) {
            //                ss.setShortDescription(descr);
            //            }
            sheet.put(set);
        }
        
        return set;
    }
    
    
    private static class BackgroundProperty extends PropertySupport.ReadWrite {
        private final DesignBean designBean;
        private Color bgColor;
        public BackgroundProperty(DesignBean desingBean) {
            super("background", // NOI18n
                    Color.class,
                    NbBundle.getMessage(DesignBeanNodeHelper.class, "LBL_Background"),
                    NbBundle.getMessage(DesignBeanNodeHelper.class, "DESC_Background"));
            this.designBean = desingBean;
        }
        
        public void setValue(Object value) throws IllegalArgumentException {
            validateValue(value, Color.class);
            
            Color color = (Color)value;
            
            FacesModel facesModel = findFacesModel(designBean);
            if (facesModel == null) {
                return;
            }
            
            Element body = facesModel.getHtmlBody();
            if (body == null) {
                return;
            }
            
            String bga = colorToHex(color); //!CQ or call: colorToRgb(bgcolor);
            String description = null; // TODO XXX old code
            UndoEvent event = facesModel.writeLock(description);
            try {
//                CssLookup.setLocalStyleValue(body, XhtmlCss.BACKGROUND_COLOR_INDEX, bga);
                Util.addLocalStyleValueForElement(body, XhtmlCss.BACKGROUND_COLOR_INDEX, bga);
                bgColor = color;
            } finally {
                facesModel.writeUnlock(event);
            }
            
//            CssLookup.refreshEffectiveStyles(webform.getDom());
            CssProvider.getEngineService().refreshStylesForDocument(facesModel.getJspDom());
            // XXX Should this be here too (or the above?).
            CssProvider.getEngineService().refreshStylesForDocument(facesModel.getHtmlDom());
        }
        
        public Object getValue() {
            if (bgColor != null) {
                return bgColor;
            }
            
            FacesModel facesModel = findFacesModel(designBean);
            if (facesModel == null) {
                bgColor = Color.WHITE;
                return bgColor;
            }
            
            Element body = facesModel.getHtmlBody();
            if (body == null) {
                bgColor = Color.WHITE;
                return bgColor;
            }
            
            if (body != null) {
//                bgColor = CssLookup.getColor(body, XhtmlCss.BACKGROUND_COLOR_INDEX);
                bgColor = CssProvider.getValueService().getColorForElement(body, XhtmlCss.BACKGROUND_COLOR_INDEX);
                if (bgColor == null) {
                    bgColor = Color.WHITE;
                }
            }
            return bgColor;
        }
    } // End of BackgroundProperty.
    
    
    private static class BackgroundImageProperty extends PropertySupport.ReadWrite {
        private final DesignBean designBean;
        private String bgImage;
        
        public BackgroundImageProperty(DesignBean desingBean) {
            super("backgroundImage", // NOI18N
                    String.class,
                    NbBundle.getMessage(DesignBeanNodeHelper.class, "LBL_BackgroundImage"),
                    NbBundle.getMessage(DesignBeanNodeHelper.class, "DESC_BackgroundImage"));
            this.designBean = desingBean;
            DesignContext designContext = designBean.getDesignContext();
            FacesModel facesModel = findFacesModel(designBean);
            FileObject fileObject = facesModel == null ? null : facesModel.getFile();
            if ((designContext != null) || (fileObject != null)) {
                setValue(StandardUrlPropertyEditor.PROPERTY_PROPERTY, this);
                if (designContext == null) {
                    setValue(StandardUrlPropertyEditor.PROPERTY_FORM_FILE, fileObject);
                } else {
                    setValue(StandardUrlPropertyEditor.PROPERTY_LIVE_CONTEXT, designContext);
                }
            }
        }
        
        public PropertyEditor getPropertyEditor() {
            if (this.designBean.getInstance() instanceof UIViewRoot) {
                StandardUrlPropertyEditor editor = new StandardUrlPropertyEditor();
                return editor;
            }
            return super.getPropertyEditor();
        }
        
        public Object getValue() {
            if (bgImage == null) {
                FacesModel facesModel = findFacesModel(designBean);
                if (facesModel == null) {
                    return ""; // NOI18N
                }
                
                Element body = facesModel.getHtmlBody();
                
                if (body != null) {
                    // Prefer to use the body's "background image" property, since
                    // this can properly handle context relative urls etc. (direct CSS properties
                    // can not)
//                    DesignBean bodyBean = ((RaveElement)body).getDesignBean();
                    DesignBean bodyBean = InSyncServiceProvider.get().getMarkupDesignBeanForElement(body);
                    if (bodyBean != null) {
                        DesignProperty background = bodyBean.getProperty("imageURL"); // NOI18N
                        if (background != null) {
                            Object o = background.getValue();
                            if (o != null) {
                                return o.toString();
                            } else {
                                return "";
                            }
                        }
                    }
                    
//                    Value value = CssLookup.getValue(body, XhtmlCss.BACKGROUND_IMAGE_INDEX);
                    CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(body, XhtmlCss.BACKGROUND_IMAGE_INDEX);
                    
//                    if ((value == CssValueConstants.NONE_VALUE) || (value == null)) {
                    if (cssValue == null || CssProvider.getValueService().isNoneValue(cssValue)) {
                        bgImage = ""; // NOI18N
                    } else {
                        bgImage = cssValue.getStringValue();
                    }
                } else {
                    bgImage = ""; // NOI18N
                }
            }
            
            return bgImage;
        }
        
        public void setValue(Object value) throws IllegalArgumentException {
            validateValue(value, String.class);
            String bgImage = (String)value;
            
            FacesModel facesModel = findFacesModel(designBean);
            if (facesModel == null) {
                this.bgImage = null;
                return;
            }
            
            Element body = facesModel.getHtmlBody();
            
            if (body != null) {
                UndoEvent event = null;
                
                try {
                    String description = null; // TODO
                    event = facesModel.writeLock(description);
                    
                    // Prefer to use the body's "background image" property, since
                    // this can properly handle context relative urls etc. (direct CSS properties
                    // can not)
//                    DesignBean bodyBean = ((RaveElement)body).getDesignBean();
                    DesignBean bodyBean = InSyncServiceProvider.get().getMarkupDesignBeanForElement(body);
                    if (bodyBean != null) {
                        DesignProperty background = bodyBean.getProperty("imageURL"); // NOI18N
                        if (background != null) {
                            if ((bgImage != null) && (bgImage.length() > 0)) {
                                background.setValue(bgImage);
                            } else {
                                background.unset();
                            }
                            
                            return;
                        }
                    }
                    
                    if ((bgImage != null) && (bgImage.length() > 0)) {
                        String url = "url(" + bgImage + ")"; // NOI18N
//                        CssLookup.setLocalStyleValue(body, XhtmlCss.BACKGROUND_IMAGE_INDEX, url);
                        Util.addLocalStyleValueForElement(body, XhtmlCss.BACKGROUND_IMAGE_INDEX, url);
                    } else {
//                        CssLookup.removeLocalStyleValue(body, XhtmlCss.BACKGROUND_IMAGE_INDEX);
                        Util.removeLocalStyleValueForElement(body, XhtmlCss.BACKGROUND_IMAGE_INDEX);
                    }
                } finally {
                    facesModel.writeUnlock(event);
                    this.bgImage = bgImage;
                }
            }
        }
        
        public boolean supportsDefaultValue() {
            return true;
        }
        
        public void restoreDefaultValue() {
            setValue(null);
        }
    } // End of BackgroundImageProperty.
    
    
    private static class LayoutProperty extends PropertySupport.ReadWrite {
        private final DesignBean designBean;
        private int layoutMode = -1;
        public LayoutProperty(DesignBean desingBean) {
            super("layout", // NOI18N
                    Integer.class,
                    NbBundle.getMessage(DesignBeanNodeHelper.class, "LBL_Layout"),
                    NbBundle.getMessage(DesignBeanNodeHelper.class, "DESC_Layout"));
            this.designBean = desingBean;
        }
        
        public Object getValue() {
            if (layoutMode < 0) {
                FacesModel facesModel = findFacesModel(designBean);
                if (facesModel == null) {
                    return new Integer(0);
                }
                
                Element body = facesModel.getHtmlBody();
                
                if (body != null) {
//                    Value val = CssLookup.getValue(body, XhtmlCss.RAVELAYOUT_INDEX);
                    CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(body, XhtmlCss.RAVELAYOUT_INDEX);
//                    layoutMode = (val == CssValueConstants.GRID_VALUE) ? 0 : 1;
                    layoutMode = (CssProvider.getValueService().isGridValue(cssValue) ? 0 : 1);
                } else {
                    layoutMode = 1; // flow if grid is not known
                }
            }
            
            return new Integer(layoutMode);
        }
        
        /** XXX */
        public PropertyEditor getPropertyEditor() {
            return new PageLayoutEditor();
        }
        
        public void setValue(Object value) throws IllegalArgumentException {
            validateValue(value, Integer.class);
            Integer intValue = (Integer)value;
            int mode = intValue == null ? 0 : intValue.intValue();
            
            FacesModel facesModel = findFacesModel(designBean);
            if (facesModel == null) {
                return;
            }
            
            Element body = facesModel.getHtmlBody();
            
            if (body != null) {
                UndoEvent event = null;
                
                try {
                    String description = null; // TODO
                    event = facesModel.writeLock(description);
                    
                    if (mode == 1) { // flow
//                        CssLookup.removeLocalStyleValue(body, XhtmlCss.RAVELAYOUT_INDEX);
                        Util.removeLocalStyleValueForElement(body, XhtmlCss.RAVELAYOUT_INDEX);
                        
                        // Ensure that the document has a <br> near the top we
                        // can go back to. This is necessary because the caret can
                        // only be placed in lineboxes, and we need to ensure we have a linebox.
                        // Other HTML editors seem to do this too - Mozilla Composer for example.
//                        if (body instanceof RaveElement) {
                        if (body != null) {
//                            DesignBean bean = ((RaveElement)body).getDesignBean();
                            DesignBean bean = InSyncServiceProvider.get().getMarkupDesignBeanForElement(body);
                            
                            LiveUnit lunit = facesModel.getLiveUnit();
                            // Bugfix:125243
                          /*  if (bean != null) {
                                if ((bean.getChildBeanCount() == 0) ||
                                        !((MarkupDesignBean)bean.getChildBean(0)).getElement()
                                        .getTagName().equals(HtmlTag.BR.name)) {
                                    // Add a br
                                    Element parent = ((MarkupDesignBean)bean).getElement();
                                    Node before = parent.getFirstChild();
//                                    webform.getDocument().createBean(org.netbeans.modules.visualweb.xhtml.Br.class.getName(), parent, before);
                                    createBean(org.netbeans.modules.visualweb.xhtml.Br.class.getName(), parent, before, lunit);
                                }
                            }*/
                            
                            // Add one to form too...
                            FacesPageUnit facesUnit = facesModel.getFacesUnit();
                            
                            if ((facesUnit != null) && (lunit != null)) {
                                MarkupBean formBean = facesUnit.getDefaultParent();
//                                bean = /*FacesSupport*/Util.getDesignBean(formBean.getElement());
                                bean = InSyncServiceProvider.get().getMarkupDesignBeanForElement(formBean.getElement());
                                
                                if (bean != null) {
                                    int n = bean.getChildBeanCount();
                                    
                                    if ((n == 0) ||
                                            !((MarkupDesignBean)bean.getChildBean(n - 1)).getElement()
                                            .getTagName().equals(HtmlTag.BR.name)) {
                                        // Add a br
                                        Element parent = ((MarkupDesignBean)bean).getElement();
//                                        webform.getDocument().createBean(org.netbeans.modules.visualweb.xhtml.Br.class.getName(), parent, null);
                                        createBean(org.netbeans.modules.visualweb.xhtml.Br.class.getName(), parent, null, lunit);
                                    }
                                }
                            }
                        }
                    } else {
                        assert mode == 0; // grid
//                        CssLookup.setLocalStyleValue(body, XhtmlCss.RAVELAYOUT_INDEX,
//                            CssConstants.CSS_GRID_VALUE);
                        Util.addLocalStyleValueForElement(body, XhtmlCss.RAVELAYOUT_INDEX,
                                CssProvider.getValueService().getGridValue());
                    }
                } finally {
                    facesModel.writeUnlock(event);
                }
            }
            
            this.layoutMode = mode;
        }
        
        // XXX Copy from designer/Document.
        private static DesignBean createBean(String className, Node parent, Node before, LiveUnit liveUnit) {
            MarkupPosition pos = new MarkupPosition(parent, before);
            DesignBean parentBean = /*FacesSupport.*/Util.findParentBean(parent);
//            LiveUnit unit = webform.getModel().getLiveUnit();
            DesignBean bean = liveUnit.createBean(className, parentBean, pos);
            
            return bean;
        }
        
    } // End of LauoutProperty.
    
    /**
     * Property editor for editing the "pagelayout" property of a document
     */
    private static class PageLayoutEditor extends PropertyEditorSupport {
        private String[] tags =
                new String[] {
            NbBundle.getMessage(DesignBeanNodeHelper.class, "LBL_GridLayout"), // NOI18N
            NbBundle.getMessage(DesignBeanNodeHelper.class, "LBL_FlowLayout") // NOI18N
        };
        
        public String getJavaInitializationString() {
            return getAsText();
        }
        
        public String getAsText() {
            int val = ((Integer)getValue()).intValue();
            
            return (val == 0) ? tags[0] : tags[1];
        }
        
        public void setAsText(String text) throws java.lang.IllegalArgumentException {
            if (text.equals(tags[0])) {
                setValue(new Integer(0));
            } else if (text.equals(tags[1])) {
                setValue(new Integer(1));
            } else {
                throw new java.lang.IllegalArgumentException(text);
            }
        }
        
        public String[] getTags() {
            return tags;
        }
    } // End of PageLayoutEditor.
    
    
    private static class StyleSheetProperty extends PropertySupport.ReadWrite {
        private final DesignBean designBean;
        private String styleSheet;
        public StyleSheetProperty(DesignBean desingBean) {
            super("styleSheet", // NOI18n
                    String.class,
                    NbBundle.getMessage(DesignBeanNodeHelper.class, "LBL_StyleSheet"),
                    NbBundle.getMessage(DesignBeanNodeHelper.class, "DESC_StyleSheet"));
            this.designBean = desingBean;
            DesignContext designContext = designBean.getDesignContext();
            FacesModel facesModel = findFacesModel(designBean);
            FileObject fileObject = facesModel == null ? null : facesModel.getFile();
            if ((designContext != null) || (fileObject != null)) {
                setValue(StandardUrlPropertyEditor.PROPERTY_PROPERTY, this);
                if (designContext == null) {
                    setValue(StandardUrlPropertyEditor.PROPERTY_FORM_FILE, fileObject);
                } else {
                    setValue(StandardUrlPropertyEditor.PROPERTY_LIVE_CONTEXT, designContext);
                }
            }
        }
        
        public PropertyEditor getPropertyEditor() {
            if (this.designBean.getInstance() instanceof UIViewRoot) {
                StandardUrlPropertyEditor editor = new StandardUrlPropertyEditor();
                return editor;
            }
            return super.getPropertyEditor();
        }
        
        public Object getValue() {
            if (styleSheet == null) {
                styleSheet = getStyleSheetURL();
            }
            
            return styleSheet;
        }
        
        public void setValue(Object value) throws IllegalArgumentException {
            validateValue(value, String.class);
            styleSheet = (String)value;
            
            FacesModel facesModel = findFacesModel(designBean);
            if (facesModel == null) {
                return;
            }
            
            if (styleSheet == null) {
                styleSheet = ""; // NOI18N
            }
            
            String description = null; // TODO
            UndoEvent event = facesModel.writeLock(description);
            try {
                setStyleSheetURL(styleSheet);
                
                // The value shown in the propertysheet is shown by immediately
                // calling getStyleSheet() after setStyleSheet(), and at this point we haven't
                // re-rendered the document (it's delayed by DomSynchronizer) so
                // just use the expected value directly.
                this.styleSheet = styleSheet;
            } finally {
                facesModel.writeUnlock(event);
            }
            
            // Apparently we don't need to refresh the styles, the CSS engine
            // listens to the DOM
            this.styleSheet = styleSheet;
        }
        
        private String getStyleSheetURL() {
            FacesModel facesModel = findFacesModel(designBean);
            if (facesModel == null) {
                return ""; // NOI18N
            }
            
//            RaveDocument document = webform.getDom();
            Document document = facesModel.getJspDom();
//            MarkupUnit markup = webform.getMarkup();
            MarkupUnit markup = facesModel.getMarkupUnit();
//            Element root = document.getRoot();
            Node root = facesModel.getHtmlDomFragment();
            if (root == null) {
                return ""; // NOI18N
            }
            
            // XXX is there a way to get ensureElement to NOT create?
            Element html = markup.findHtmlTag(root);
            
            if (html == null) {
                return "";
            }
            
            Element head = Util.findChild(HtmlTag.HEAD.name, html, false);
            
            if (head == null) {
                return "";
            }
            
            NodeList list = head.getChildNodes();
            int len = list.getLength();
            
            for (int i = 0; i < len; i++) {
                org.w3c.dom.Node child = list.item(i);
                
                if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element element = (Element)child;
                    
                    if (element.getTagName().equals(HtmlTag.LINK.name)) {
//                        RaveElement xlink = (RaveElement)element;
                        
//                        if (xlink.isRendered() && (xlink.getSource() == null)) {
//                        if (MarkupService.isRenderedNode(element) && MarkupService.getSourceNodeForNode(element) == null) {
                        // It is the rendered node (got from #getHtmlDomFragment).
                        if (MarkupService.getSourceNodeForNode(element) == null) {
                            // Don't return "derived" links such as theme links
                            // automatically rendered for themes for example
                            continue;
                        }
                        
                        String url = element.getAttribute(HtmlAttribute.HREF);
                        
                        if (url != null) {
                            return url;
                        }
                    }
                }
            }
            
            return ""; // NOI18N
        }
        
        private void setStyleSheetURL(String url) {
            // See if we have a Braveheart Link component. If so, just change its
            // url.
            boolean add = (url != null) && (url.length() > 0);
            
            FacesModel facesModel = findFacesModel(designBean);
            if (facesModel == null) {
                return;
            }
            
//            MarkupUnit markup = webform.getMarkup();
            MarkupUnit markup = facesModel.getMarkupUnit();
//            RaveDocument document = webform.getDom();
            Document document = facesModel.getJspDom();
//            LiveUnit lu = webform.getModel().getLiveUnit();
            LiveUnit lu = facesModel.getLiveUnit();
            
            DataObject markupDO;
            try {
                markupDO = DataObject.find(facesModel.getMarkupFile());
            } catch (DataObjectNotFoundException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                return;
            }
            
            DesignBean uihead = null;
//            if (DesignerUtils.isBraveheartPage(document)) {
            if (InSyncServiceProvider.get().isBraveheartPage(document)) {
                DesignBean[] heads = lu.getBeansOfType(com.sun.rave.web.ui.component.Head.class);
                
                if ((heads != null) && (heads.length > 0)) {
                    uihead = heads[0];
                }
            } else if (InSyncServiceProvider.get().isWoodstockPage(document)) {
                DesignBean[] heads = lu.getBeansOfType(com.sun.webui.jsf.component.Head.class);
                
                if ((heads != null) && (heads.length > 0)) {
                    uihead = heads[0];
                }
            }
            
            
            if (uihead != null) {
                // Find existing eligible links
                for (int i = 0, n = uihead.getChildBeanCount(); i < n; i++) {
                    DesignBean child = uihead.getChildBean(i);
                    
                    if (child.getInstance() instanceof com.sun.rave.web.ui.component.Link) {
                        // Ensure that this is a stylesheet link
                        String rel = (String)child.getProperty("rel").getValue(); // NOI18N
                        
                        if ((rel != null) && (rel.length() > 0) && !rel.equalsIgnoreCase("stylesheet")) { // NOI18N
                            
                            continue;
                        }
                        
                        // Use this one
                        if (add) {
                            child.getProperty("url").setValue(url); // NOI18N
//                            webform.getActions().refresh(false);
//                            DesignerServiceHack.getDefault().refresh(null, markupDO, false);
//                            DesignerServiceHack.getDefault().refreshDataObject(markupDO, false);
                            fireRefreshNeeded(false);
                            
                            return;
                        } else {
                            lu.deleteBean(child);
//                            webform.getActions().refresh(false);
//                            DesignerServiceHack.getDefault().refresh(null, markupDO, false);
//                            DesignerServiceHack.getDefault().refreshDataObject(markupDO, false);
                            fireRefreshNeeded(false);
                            
                            return;
                        }
                    }
                }
                
                // No stylesheet link exists - add one
//                DesignBean link = lu.createBean("com.sun.rave.web.ui.component.Link", uihead, new Position()); // NOI18N
                
                DesignBean link;
                if (InSyncServiceProvider.get().isBraveheartPage(document)) {
                    link = lu.createBean(com.sun.rave.web.ui.component.Link.class.getName(), uihead, new Position());
                } else if (InSyncServiceProvider.get().isWoodstockPage(document)) {
                    link = lu.createBean(com.sun.webui.jsf.component.Link.class.getName(), uihead, new Position());
                } else {
                    // XXX Log?
                    link = null;
                }
                
                if (link != null) {
                    link.getProperty("rel").setValue("stylesheet"); // NOI18N
                    link.getProperty("type").setValue("text/css"); // NOI18N
                    link.getProperty("url").setValue(url); // NOI18N
//                    webform.getActions().refresh(false);
//                    DesignerServiceHack.getDefault().refresh(null, markupDO, false);
//                    DesignerServiceHack.getDefault().refreshDataObject(markupDO, false);
                    fireRefreshNeeded(false);
                }
            } else {
                Element root = document.getDocumentElement();
                Element html = markup.findHtmlTag(root);
                
                if (html == null) { // We're hosed!!! This shouldn't happen
                    Thread.dumpStack();
                    
                    return;
                }
                
                // Gotta replace with HtmlTag.LINK.name
                Element head = Util.findChild(HtmlTag.HEAD.name, html, true);
                Element link = Util.findChild(HtmlTag.LINK.name, head, false);
                
                // XXX should iterate over ALL the links until you find one
                // that has rel="stylesheet" !!
                if ((url != null) && (url.length() > 0)) {
                    // Add stylesheet reference
                    if (link == null) {
                        // XXX Shouldn't I be using beans2 here?? plus accquiring a writelock?
                        // No stylesheet link exists - add one
                        link = document.createElement(HtmlTag.LINK.name);
                        link.setAttribute(HtmlAttribute.REL, "stylesheet");
                        link.setAttribute(HtmlAttribute.TYPE, "text/css");
                        link.setAttribute(HtmlAttribute.HREF, url);
                        head.appendChild(link);
                    } else {
                        // Existing stylesheet - just change it
                        link.setAttribute(HtmlAttribute.HREF, url);
                    }
                } else if (link != null) {
                    // Remove stylesheet reference
                    head.removeChild(link);
                }
                
//                webform.getActions().refresh(true);
//                DesignerServiceHack.getDefault().refresh(null, markupDO, true);
//                DesignerServiceHack.getDefault().refreshDataObject(markupDO, true);
                fireRefreshNeeded(true);
            }
        }
        
        private void fireRefreshNeeded(boolean deep) {
            FacesModel facesModel = findFacesModel(designBean);
            if (facesModel != null) {
                // XXX FacesModel should be able to fire events itself.
//                facesModel.getDnDSupport().fireRefreshNeeded(deep);
                facesModel.getJsfSupport().refresh(deep);
            }
        }
        
        public boolean supportsDefaultValue() {
            return true;
        }
        
        public void restoreDefaultValue() {
            setValue(null);
        }
    } // End of StyleSheetProperty.
    
    
    private static class TitleProperty extends PropertySupport.ReadWrite {
        private final DesignBean designBean;
        private String title;
        public TitleProperty(DesignBean desingBean) {
            super("title", // NOI18n
                    String.class,
                    NbBundle.getMessage(DesignBeanNodeHelper.class, "LBL_Title"),
                    NbBundle.getMessage(DesignBeanNodeHelper.class, "DESC_Title"));
            this.designBean = desingBean;
        }
        
        public Object getValue() {
            if (title == null) {
                Element titleElem = findTitle();
                
                if (titleElem != null) {
                    title = MarkupUnit.getElementText(titleElem);
                }
                
                if (title == null) {
                    title = ""; // NOI18N
                } else {
                    // Swing labels like to interpret String that start with <html>
                    // as actual HTML to be rendered. That happens for some weird
                    // titles (like the one in 6316218) - so prevent this scenario
                    if (title.startsWith("<html>")) { // NOI18N
                        title = " " + title;
                    }
                }
            }
            
            return title;
        }
        
        public void setValue(Object value) throws IllegalArgumentException {
            validateValue(value, String.class);
            String title = (String)value;
            
            Element head = findHead();
            
            if (head != null) {
//                RaveElement h = (RaveElement)head;
                
//                if (h.isRendered() && (h.getDesignBean() != null)) {
                // It is the rendered node.
//                if (MarkupService.isRenderedNode(head)) {
//                    MarkupDesignBean b = h.getDesignBean();
                    com.sun.rave.designtime.markup.MarkupDesignBean b = InSyncServiceProvider.get().getMarkupDesignBeanForElement(head);
                    if (b != null) {
                        DesignProperty prop = b.getProperty("title"); // NOI18N
                        
                        if (prop != null) {
                            prop.setValue(title);
                            
                            // The value shown in the propertysheet is shown by immediately
                            // calling getTitle() after setTitle(), and at this point we haven't
                            // re-rendered the document (it's delayed by DomSynchronizer) so
                            // just use the expected value directly.
                            this.title = title;
                            
                            return;
                        }
                    }
//                }
            }
            
            FacesModel facesModel = findFacesModel(designBean);
            if (facesModel == null) {
                return;
            }
            Document doc = facesModel.getJspDom();
            String description = null; // TODO
            UndoEvent event = facesModel.writeLock(description);
            try {
                head = MarkupUnit.ensureElement(doc.getDocumentElement(), HtmlTag.HEAD.name, null);
                
                //Element head = MarkupUnit.getFirstDescendantElement(doc.getDocumentElement(), "head");
                Element titleElem = MarkupUnit.ensureElement(head, HtmlTag.TITLE.name, null);
                MarkupUnit.setElementText(titleElem, title);
            } finally {
                facesModel.writeUnlock(event);
            }
            
            this.title = title;
        }
        
        public boolean supportsDefaultValue() {
            return true;
        }
        
        public void restoreDefaultValue() {
            setValue(null);
        }
        
        private Element findHead() {
            FacesModel facesModel = findFacesModel(designBean);
            if (facesModel == null) {
                return null;
            }
            
//            Document doc = facesModel.getMarkupUnit().getSourceDom();
//            Element head =
//                MarkupUnit.getFirstDescendantElement(((RaveDocument)doc).getRoot(), HtmlTag.HEAD.name);
            Node root = facesModel.getHtmlDomFragment();
            if (root == null) {
                return null;
            }
            MarkupUnit markup = facesModel.getMarkupUnit();
            Element html = markup.findHtmlTag(root);
            if (html == null) {
                return null;
            }
            
            return MarkupUnit.getFirstDescendantElement(html, HtmlTag.HEAD.name);
        }
        
        private Element findTitle() {
            Element head = findHead();
            
            if (head != null) {
                Element titleElem = MarkupUnit.getFirstDescendantElement(head, HtmlTag.TITLE.name);
                
                if (titleElem != null) {
                    return titleElem;
                }
            }
            
            return null;
        }
    } // End of TitleProperty.
    
    
    private static class EncodingProperty extends PropertySupport.ReadWrite {
        private final DesignBean designBean;
        public EncodingProperty(DesignBean desingBean) {
            super("encoding", // NOI18n
                    String.class,
                    NbBundle.getMessage(DesignBeanNodeHelper.class, "LBL_Encoding"),
                    NbBundle.getMessage(DesignBeanNodeHelper.class, "DESC_Encoding"));
            this.designBean = desingBean;
        }
        
        public Object getValue() {
            FacesModel facesModel = findFacesModel(designBean);
            if (facesModel == null) {
                return null;
            }
            if (facesModel.getFacesUnit() != null) {
                return facesModel.getFacesUnit().getEncoding();
            } else {
                return null;
            }
        }
        
        public void setValue(Object value) throws IllegalArgumentException {
            validateValue(value, String.class);
            String encoding = (String)value;
            
            FacesModel facesModel = findFacesModel(designBean);
            if (facesModel == null) {
                return;
            }
            if (facesModel.getFacesUnit() != null) {
                String description = null; // TODO
                UndoEvent event = facesModel.writeLock(description);
                try {
                    facesModel.getFacesUnit().setEncoding(encoding);
                } finally {
                    facesModel.writeUnlock(event);
                }
            }
        }
        
        public boolean supportsDefaultValue() {
            return true;
        }
        
        public void restoreDefaultValue() {
            setValue(null);
        }
    } // End of EncodingProperty.
    
    
    private static class LanguageProperty extends PropertySupport.ReadWrite {
        private final DesignBean designBean;
        public LanguageProperty(DesignBean desingBean) {
            super("language", // NOI18n
                    String.class,
                    NbBundle.getMessage(DesignBeanNodeHelper.class, "LBL_Language"),
                    NbBundle.getMessage(DesignBeanNodeHelper.class, "DESC_Language"));
            this.designBean = desingBean;
            
            // EATTODO: Take the setting of property editor out until I can get someone to help me
            // liveProperty not getting set on this editor.  I assume its due to this not being a DesignProperty :)
            // Never the less, I want a solution, maybe I do one where the property editor can work with no liveProperty
            //            p.setPropertyEditorClass(com.sun.jsfcl.std.property.SingleChoiceReferenceDataPropertyEditor.class);
//            setValue(ChooseOneReferenceDataPropertyEditor.REFERENCE_DATA_NAME,
//                ReferenceDataManager.LANGUAGE_CODES);
        }
        
        public Object getValue() {
            FacesModel facesModel = findFacesModel(designBean);
            if (facesModel == null) {
                return ""; // NOI18N
            }
            Document doc = facesModel.getJspDom();
            
            if (InSyncServiceProvider.get().isBraveheartPage(doc)
            || InSyncServiceProvider.get().isWoodstockPage(doc)) {
                DesignProperty property = findLanguageProperty();
                
                if (property != null) {
                    String lang = property.getValueSource();
                    
                    if (lang != null) {
                        return lang;
                    }
                }
                
                // Just return empty string
            } else {
                Element html =
                        MarkupUnit.getFirstDescendantElement(doc.getDocumentElement(),
                        HtmlTag.HTML.name);
                
                if (html != null) {
                    return html.getAttribute(HtmlAttribute.LANG);
                }
            }
            
            return ""; // NOI18N
        }
        
        public void setValue(Object value) throws IllegalArgumentException {
            validateValue(value, String.class);
            String language = (String)value;
            
            FacesModel facesModel = findFacesModel(designBean);
            if (facesModel == null) {
                return;
            }
            Document doc = facesModel.getJspDom();
            if (InSyncServiceProvider.get().isBraveheartPage(doc)
            || InSyncServiceProvider.get().isWoodstockPage(doc)) {
                DesignProperty property = findLanguageProperty();
                
                if (property != null) {
                    property.setValue(language);
                }
            } else {
                if ((language == null) && (facesModel.getFacesUnit() != null)) {
                    language = facesModel.getFacesUnit().getDefaultLanguage();
                }
                
                String description = null; // TODO
                UndoEvent event = facesModel.writeLock(description);
                try {
                    
                    Element html =
                            MarkupUnit.getFirstDescendantElement(doc.getDocumentElement(),
                            HtmlTag.HTML.name);
                    
                    if (html != null) {
                        MarkupUnit pgunit = facesModel.getMarkupUnit();
                        pgunit.ensureAttributeValue(html, HtmlAttribute.LANG, language);
                        pgunit.ensureAttributeValue(html, "xml:lang", language);
                    }
                } finally {
                    facesModel.writeUnlock(event);
                }
            }
        }
        
        public boolean supportsDefaultValue() {
            return true;
        }
        
        public void restoreDefaultValue() {
            setValue(null);
        }
        
        private DesignProperty findLanguageProperty() {
//            assert !webform.isPortlet();
//            assert !webform.isFragment();
            
//            RaveElement body = webform.getBody();
            FacesModel facesModel = findFacesModel(designBean);
            if (facesModel == null) {
                return null; // NOI18N
            }
            
            Element body = facesModel.getHtmlBody();
            
            if (body == null) {
                return null;
            }
            
//            DesignBean bodyBean = body.getDesignBean();
            DesignBean bodyBean = InSyncServiceProvider.get().getMarkupDesignBeanForElement(body);
            if (bodyBean == null) {
                // XXX #137120.
                return null;
            }

            DesignBean parent = bodyBean.getBeanParent();
            
            if (parent != null) {
                // TODO - check if this is really an Html component?
                // It doesn't really matter... if it has a "lang" property we'll
                // be happy with third party containers too!
                DesignProperty property = parent.getProperty("lang"); // NOI18N
                
                return property;
            }
            
            return null;
        }
        
    } // End of LanguageProperty.
    
    
    private static class WidthProperty extends PropertySupport.ReadWrite {
        private final DesignBean designBean;
        public WidthProperty(DesignBean desingBean) {
            super("width", // NOI18N
                    String.class,
                    NbBundle.getMessage(DesignBeanNodeHelper.class, "LBL_Width"),
                    NbBundle.getMessage(DesignBeanNodeHelper.class, "DESC_Width"));
            this.designBean = desingBean;
        }
        
        public Object getValue() {
            return getSize(designBean, XhtmlCss.WIDTH_INDEX);
        }
        
        public void setValue(Object value) throws IllegalArgumentException {
            validateValue(value, String.class);
            String size = (String)value;
            setSize(designBean, XhtmlCss.WIDTH_INDEX, size);
        }
        
        public boolean supportsDefaultValue() {
            return true;
        }
        
        public void restoreDefaultValue() {
            setValue(null);
        }
    } // End of WidthProperty.
    
    
    private static class HeightProperty extends PropertySupport.ReadWrite {
        private final DesignBean designBean;
        public HeightProperty(DesignBean desingBean) {
            super("height", // NOI18N
                    String.class,
                    NbBundle.getMessage(DesignBeanNodeHelper.class, "LBL_Height"),
                    NbBundle.getMessage(DesignBeanNodeHelper.class, "DESC_Height"));
            this.designBean = desingBean;
        }
        
        public Object getValue() {
            return getSize(designBean, XhtmlCss.HEIGHT_INDEX);
        }
        
        public void setValue(Object value) throws IllegalArgumentException {
            validateValue(value, String.class);
            String size = (String)value;
            setSize(designBean, XhtmlCss.HEIGHT_INDEX, size);
        }
        
        public boolean supportsDefaultValue() {
            return true;
        }
        
        public void restoreDefaultValue() {
            setValue(null);
        }
    } // End of HeightProperty.
    
    
    private static void validateValue(Object value, Class type) throws IllegalArgumentException {
        if (value != null && !type.isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException("Value is not of " + type + ", value class=" + value.getClass()); // NOI18N
        }
    }
    
    private static FacesModel findFacesModel(DesignBean designBean) {
        DesignContext designContext = designBean.getDesignContext();
        if (!(designContext instanceof LiveUnit)) {
            return null;
        }
        return ((LiveUnit)designContext).getModel();
    }
    
    private static void setSize(DesignBean designBean, int property, String size) {
        FacesModel facesModel = findFacesModel(designBean);
        if (facesModel == null) {
            return;
        }
        
        Element body = facesModel.getHtmlBody();
        if (body == null) {
            return;
        }
        
        
        UndoEvent undoEvent = facesModel.writeLock(NbBundle.getMessage(DesignBeanNodeHelper.class, "LBL_SetSize")); // NOI18N
        try {
            if (size != null) {
                size = size.trim();
            }
            
//            if ((size == null) || (size.length() == 0) || CssConstants.CSS_AUTO_VALUE.equals(size)) {
            if (size == null || size.length() == 0 || CssProvider.getValueService().isAutoValue(size)) {
//                CssLookup.removeLocalStyleValue(body, property);
                Util.removeLocalStyleValueForElement(body, property);
            } else {
//                if (XhtmlCssEngine.hasNoUnits(size)) {
                if (CssProvider.getValueService().hasNoUnits(size)) {
                    size = size + "px"; // NOI18N
                }
                
//                CssLookup.setLocalStyleValue(body, property, size);
                Util.addLocalStyleValueForElement(body, property, size);
            }
        } finally {
            facesModel.writeUnlock(undoEvent);
        }
    }
    
    private static String getSize(DesignBean designBean, int property) {
        FacesModel facesModel = findFacesModel(designBean);
        if (facesModel == null) {
            return ""; // NOI18N
        }
        
        Element body = facesModel.getHtmlBody();
        if (body == null) {
            return ""; // NOI18N
        }
        
        
        if (body != null) {
//            Value v = CssLookup.getValue(body, property);
            CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(body, property);
            
//            if ((v == null) || (v == CssValueConstants.AUTO_VALUE)) {
            if (cssValue == null || CssProvider.getValueService().isAutoValue(cssValue)) {
                return ""; // NOI18N
            }
            
//            if (v instanceof ComputedValue) {
//                return ((ComputedValue)v).getCascadedValue().getCssText();
//            }
            if (cssValue instanceof CssComputedValue) {
                return ((CssComputedValue)cssValue).getCascadedValue().getCssText();
            }
            
//            return v.toString(); // Do I need to add in the percent?
            return cssValue.getCssText();
        }
        
        return ""; // NOI18N
    }
    
    
    /** XXX Moved from designer/DesignerUtils. */
    private static char[] hexdigits =
    { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    private static String colorToHex(Color c) {
        if (c == null) {
            return null;
        }
        
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        
        return "#" + hexdigits[(r & 0xF0) >> 4] + hexdigits[r & 0x0F] + hexdigits[(g & 0xF0) >> 4] +
                hexdigits[g & 0x0F] + hexdigits[(b & 0xF0) >> 4] + hexdigits[b & 0x0F];
    }
    
    private static boolean isPortlet(DesignBean designBean) {
        DesignContext designContext = designBean.getDesignContext();
        if (designContext instanceof LiveUnit) {
            LiveUnit liveUnit = (LiveUnit)designContext;
            FileObject markupFile = liveUnit.getModel().getMarkupFile();
            if (markupFile != null && "jspf".equals(markupFile.getExt())) { // NOI18N
                return true;
            }
        }
        return false;
    }
    
    private static boolean isFragment(DesignBean designBean) {
        DesignContext designContext = designBean.getDesignContext();
        if (designContext instanceof LiveUnit) {
            LiveUnit liveUnit = (LiveUnit)designContext;
            if (liveUnit.getModel().getFacesModelSet().getFacesContainer().isPortletContainer()) {
                return true;
            }
        }
        return false;
    }
}

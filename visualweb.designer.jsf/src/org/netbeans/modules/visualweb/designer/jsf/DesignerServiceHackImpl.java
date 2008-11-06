/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.visualweb.designer.jsf;


import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import org.netbeans.modules.visualweb.designer.html.HtmlAttribute;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;
import java.lang.reflect.Method;
import javax.faces.component.html.HtmlCommandLink;
import javax.faces.component.html.HtmlOutputLink;
import javax.faces.component.html.HtmlOutputText;
import org.netbeans.modules.visualweb.api.designer.DesignerServiceHackProvider;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssEngineService;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.api.designerapi.DesignerServiceHack;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import java.awt.Container;
import org.netbeans.modules.visualweb.insync.beans.BeansUnit;
import org.netbeans.modules.visualweb.insync.faces.FacesPageUnit;
import org.netbeans.modules.visualweb.insync.live.LiveUnit;
import org.netbeans.modules.visualweb.insync.markup.MarkupUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.visualweb.api.designer.Designer;
import org.netbeans.modules.visualweb.api.designer.Designer.Box;
import org.netbeans.modules.visualweb.api.designer.Designer.ExternalBox;
import org.netbeans.modules.visualweb.api.designer.DomProvider;
import org.netbeans.modules.visualweb.designer.jsf.ui.JsfTopComponent;
import org.netbeans.modules.visualweb.insync.UndoEvent;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;



/**
 * Implementation of the DesignerService API.
 * <p>
 *
 * @todo The css value lookup methods need to do something smarter
 *   for shorthand properties
 * @author  Tor Norbye
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.visualweb.api.designerapi.DesignerServiceHack.class)
public class DesignerServiceHackImpl extends DesignerServiceHack {
//    private static final String[] LENGTH_UNITS =
//        { "%", "em", "ex", "px", "cm", "mm", "in", "pt", "pc" };
//    private static volatile String[] properties;

//    /**
//     * The following mime types are valid mime types for files
//     * that will be considered webforms in the WebAppProject
//     */
//    private static final String[] FORM_MIME_TYPES = new String[] { "text/x-jsp" }; // NOI18N


    public DesignerServiceHackImpl() {
    }

    private FacesPageUnit getFacesUnit(DesignContext context) {
        LiveUnit lu = (LiveUnit)context;

        // Find the model
        BeansUnit bu = lu.getBeansUnit();

        if (!(bu instanceof FacesPageUnit)) {
            return null;
        }

        return (FacesPageUnit)bu;
    }

    public Image getCssPreviewImage(String cssStyle, String[] cssStyleClasses,
        MarkupDesignBean bean, int width, int height) {
        if (bean.getElement() == null) {
            return null;
        }

        FacesPageUnit fu = getFacesUnit(bean.getDesignContext());
        if (fu == null) {
            return null;
        }

        MarkupUnit mu = fu.getPageUnit();
        FileObject fo = mu.getFileObject();
        DataObject dobj = null;

        try {
            dobj = DataObject.find(fo);
        } catch (DataObjectNotFoundException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return null;
        }

//        WebForm webform = DesignerUtils.getWebForm(dobj);
//
//        if (webform == null) {
//            return null;
//        }
//
//        // Phew! On to the preview painting.
//        PageBox pageBox = PageBox.getPageBox(null, webform, webform.getHtmlBody());
//        WindowManager wm = WindowManager.getDefault();
//        Graphics2D g2d = (Graphics2D)wm.getMainWindow().getGraphics();
//
//        return pageBox.paintCssPreview(g2d, cssStyle, bean, width, height);
        
// >>> Moved from designer/PageBox.paintCssPreview >>>
//        if (initialWidth == 0) {
        if (width == 0) {
            // Ensure that we don't force wrapping on components like a composite
            // breadcrumbs by giving it some space to work with.
            width = 600;
        }

        // Distinguish between the bean we're going to -render- and the one we're
        // going to apply the differente properties to
        MarkupDesignBean renderBean = bean;

//        // Handle hyperlinks. We really need to render its surrounding content
//        // to see the CS stylerules for <a> apply
//        if (renderBean.getInstance() instanceof HtmlOutputText) {
//            DesignBean parent = renderBean.getBeanParent();
//
//            if ((parent != null) && (parent.getChildBeanCount() == 1) &&
//                    (parent.getInstance() instanceof HtmlCommandLink ||
//                    parent.getInstance() instanceof HtmlOutputLink)) {
//                renderBean = (MarkupDesignBean)parent;
//            }
//        }
//
//        // Embedded table portions (rowgroups, columns) aren't happy being rendered
//        // without their surrounding table.
//        // It would be better to modify the preview code to actually go and -try- rendering
//        // components and then progressively retry on parents until it succeeds.
//        // But given that the code is freezing today I'm playing it safe
//        if (renderBean.getInstance() instanceof com.sun.rave.web.ui.component.TableColumn
//        || renderBean.getInstance() instanceof com.sun.webui.jsf.component.TableColumn) {
//            if (renderBean.getBeanParent() instanceof MarkupDesignBean) {
//                renderBean = (MarkupDesignBean)renderBean.getBeanParent();
//            } else {
//                return null;
//            }
//        } else if (renderBean.getBeanParent().getInstance() instanceof com.sun.rave.web.ui.component.TableColumn
//        || renderBean.getBeanParent().getInstance() instanceof com.sun.webui.jsf.component.TableColumn) {
//            // We also have to render components that are children of a TableColumn as part of the whole
//            // table as well, because their value binding expressions can involve data providers set up
//            // by the table. This is clearly not a clean solution. See comment above about trying arbitary
//            // rendering instead. This breaks once you nest components in a column inside a container
//            // component for example. Just doing a low risk, 90% fix now right before FCS.
//            if (renderBean.getBeanParent().getBeanParent() instanceof MarkupDesignBean) {
//                renderBean = (MarkupDesignBean)renderBean.getBeanParent().getBeanParent();
//            } else {
//                return null;
//            }
//        }
//
//        // Not else: a TableColumn can be inside a TableRowGroup so keep moving outwards if necessary:
//        if (renderBean.getInstance() instanceof com.sun.rave.web.ui.component.TableRowGroup
//        || renderBean.getInstance() instanceof com.sun.webui.jsf.component.TableRowGroup) {
//            if (renderBean.getBeanParent() instanceof MarkupDesignBean) {
//                renderBean = (MarkupDesignBean)renderBean.getBeanParent();
//            } else {
//                return null;
//            }
//        }
        // XXX Hack, see the impl.
//        renderBean = WebForm.getDomProviderService().adjustRenderBeanHack(renderBean);
        renderBean = adjustRenderBeanHack(renderBean);

        Element e = bean.getElement();
        assert e != null;
        
        // XXX can I shut off errors in output window?
        String oldStyleAttribute = null;
        String oldStyleProperty = null;

        if (e.hasAttribute(HtmlAttribute.STYLE)) {
            oldStyleAttribute = e.getAttribute(HtmlAttribute.STYLE);
        }

//        XhtmlCssEngine engine = webform.getMarkup().getCssEngine();

        DesignContext designContext = bean.getDesignContext();
        if (!(designContext instanceof LiveUnit)) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("Design context should be LiveUnit instance, designContext=" + designContext + ", bean=" + bean)); // NOI18N
            return null;
        }
        FacesModel facesModel = ((LiveUnit)designContext).getModel();
        if (facesModel == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new NullPointerException("FacesModel is null, designContext=" + designContext + ", bean=" + bean)); // NOI18N
            return null;
        }
        UndoEvent writeLock = facesModel.writeLock(NbBundle.getMessage(DesignerServiceHackImpl.class, "LBL_CssPreviewImage")); // NOI18N
        try {
//            engine.setErrorHandler(XhtmlCssEngine.SILENT_ERROR_HANDLER);
//            CssProvider.getEngineService().setSilentErrorHandlerForDocument(webform.getMarkup().getSourceDom());
//            CssProvider.getEngineService().setSilentErrorHandlerForDocument(webform.getMarkup().getRenderedDom());
//            CssProvider.getEngineService().setSilentErrorHandlerForDocument(webform.getHtmlDom());
            CssProvider.getEngineService().setSilentErrorHandlerForDocument(mu.getRenderedDom());
            
//            CssBox.noBoxPersistence = true;

            e.setAttribute(HtmlAttribute.STYLE, cssStyle);

            DesignProperty prop = bean.getProperty("style");

            if (prop != null) {
                oldStyleProperty = (String)prop.getValue();

                try {
                    Method m = prop.getPropertyDescriptor().getWriteMethod();
                    m.invoke(bean.getInstance(), new Object[] { cssStyle });
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }

//            engine.clearComputedStyles(e, "");
//            CssProvider.getEngineService().clearComputedStylesForElement(e); // TEMP
            
            
            // Try to render JSF so I can process the DF before proceeding
            Element element = renderBean.getElement();
            String tagName = element.getTagName();
            HtmlTag tag = HtmlTag.getTag(tagName);

            DocumentFragment df;
            if (tag == null) {
                // Possibly a Jsf component.
                // Use getDocument() rather than doc directly since
                // e.g. jsp includes may point to external documents here,
                // not the document containing the jsp tag itself
                
                // XXX TODO There is not needed webform here.
//                FileObject markupFile = webform.getModel().getMarkupFile();
////                DocumentFragment df = FacesSupport.renderHtml(markupFile, renderBean, !CssBox.noBoxPersistence);
//                DocumentFragment df = InSyncService.getProvider().renderHtml(markupFile, renderBean);
//                df = webform.renderHtmlForMarkupDesignBean(renderBean);
                df = FacesPageUnit.renderHtml((FacesModel)fu.getModel(), renderBean, false);
            } else {
                df = null;
            }
            
//            if (df != null) {
//                stripDesignStyleClasses(df);
//            }

            Element componentRootElement = JsfSupportUtilities.getComponentRootElementForDesignBean(bean);
            
            // XXX Moved from DesignerHackProviderImpl.
            Designer[] designers = JsfForm.getDesignersForDataObject(dobj);
            if (designers.length == 0) {
                return null;
            }
            // XXX Why it is used the main window graphics??
            WindowManager wm = WindowManager.getDefault();
            Graphics2D g2d = (Graphics2D)wm.getMainWindow().getGraphics();
// <<< Moved from designer/PageBox.paintCssPreview
            return DesignerServiceHackProvider.getCssPreviewImage(/*dobj,*/
                    designers[0], g2d,
                    cssStyle, cssStyleClasses,
                    /*bean,*/ componentRootElement, df, element,
                    width, height);
// >>> Moved from designer/PageBox.paintCssPreview
        } finally {
//            CssBox.noBoxPersistence = false;

            if (oldStyleAttribute != null) {
                e.setAttribute(HtmlAttribute.STYLE, oldStyleAttribute);
            } else {
                e.removeAttribute(HtmlAttribute.STYLE);
            }

            DesignProperty prop = bean.getProperty("style");

            if (prop != null) {
                try {
                    Method m = prop.getPropertyDescriptor().getWriteMethod();
                    m.invoke(bean.getInstance(), new Object[] { oldStyleProperty });
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }

//            engine.clearComputedStyles(e, null);
            CssEngineService cssEngineService = CssProvider.getEngineService();
            cssEngineService.clearComputedStylesForElement(e);

            if (renderBean != bean) {
//                engine.clearComputedStyles(renderBean.getElement(), null);
                cssEngineService.clearComputedStylesForElement(renderBean.getElement());
            }

//            engine.setErrorHandler(null);
//            cssEngineService.setNullErrorHandlerForDocument(webform.getMarkup().getSourceDom());
//            cssEngineService.setNullErrorHandlerForDocument(webform.getMarkup().getRenderedDom());
//            cssEngineService.setNullErrorHandlerForDocument(webform.getHtmlDom());
            cssEngineService.setNullErrorHandlerForDocument(mu.getRenderedDom());
            
            facesModel.writeUnlock(writeLock);
        }
// <<< Moved from designer/PageBox.paintCssPreview
    }

    public Image getCssPreviewImage(Map<String, String> properties, URL base, int width, int height) {
//        WindowManager wm = WindowManager.getDefault();
//        Graphics2D g2d = (Graphics2D)wm.getMainWindow().getGraphics();
//
//        return paintCssPreview(g2d, base, properties, width, height);
        return DesignerServiceHackProvider.getCssPreviewImage(properties, base,
                width, height, JsfDesignerPreferences.getInstance().getDefaultFontSize());
    }

    /** Computes a preview image of the specified size for given <code>DataObject</code>.
     * @return the image or <code>null</code> if the specified DataObject is not a webform one. */
    public Image getPageBoxPreviewImage(DataObject dobj, int width, int height) {
        // Copied from navigation/../PageFlowGraph to get rid of dependencies.
        // Obtain a page box for the given page
//        WebForm webform = DesignerUtils.getWebForm(dobj);
//
//        if (webform == null) {
//            return null;
//        }
//
//        webform.getModel().sync();
//
//        if (webform.getModel().isBusted()) {
//            return null;
//        }
//
//        Element body = webform.getHtmlBody();
//
//        if (body == null) {
//            return null;
//        }
//
//        PageBox pageBox = PageBox.getPageBox(null, webform, body);
//
//        return pageBox.createPreviewImage(width, height);
        // XXX Moved from DesignerHackProviderImpl.
//        Designer[] designers = JsfForm.getDesignersForDataObject(dobj);
//        if (designers.length == 0) {
//            return null;
//        }
        JsfForm jsfForm = JsfForm.getJsfForm(dobj);
        if (jsfForm == null) {
            return null;
        }
        // XXX Moved from designer/../DesignerHackProviderImpl.
//        webform.getModel().sync();
        jsfForm.syncModel();
//        if (webform.getModel().isBusted()) {
        if (jsfForm.isModelBusted()) {
            return null;
        }
        Designer[] designers = JsfForm.getDesigners(jsfForm);
        if (designers.length == 0) {
            return null;
        }
        
        return DesignerServiceHackProvider.getPageBoxPreviewImage(/*dobj,*/ designers[0], width, height);
    }

//    public String[] getCssIdentifiers(String propertyName) {
////        StringMap map = getIdentifiers(propertyName);
////
////        if (map == null) {
////            return new String[0];
////        }
////
////        int count = map.size();
////        ArrayList keys = new ArrayList(count);
////        Iterator it = map.keys();
////
////        while (it.hasNext()) {
////            Object o = it.next();
////            keys.add(o);
////        }
////
////        keys.add("inherit");
////        Collections.sort(keys);
////
////        return (String[])keys.toArray(new String[keys.size()]);
//        return CssProvider.getEngineService().getCssIdentifiers(propertyName);
//    }

//    //    public Object[] getCssIdentifierValues(String propertyName) {
//    //        StringMap map = getIdentifiers(propertyName);
//    //        if (map == null) {
//    //            return new Object[0];
//    //        }
//    //        int count = map.size();
//    //        ArrayList values = new ArrayList(count);
//    //        Iterator it = map.values();
//    //        while (it.hasNext()) {
//    //            Object o = it.next();
//    //            values.add(o);
//    //        }
//    //        // TODO -- sort in the same order as the identifier names??
//    //        return (Object[])values.toArray(new Object[values.size()]);
//    //    }
//    private StringMap getIdentifiers(String property) {
////        int index = XhtmlCssEngine.getXhtmlPropertyIndex(property);
//        int index = CssProvider.getEngineService().getXhtmlPropertyIndex(property);
//
//        if (index == -1) {
////            index = XhtmlCssEngine.getXhtmlShorthandIndex(property);
//            index = CssProvider.getEngineService().getXhtmlShorthandIndex(property);
//
//            if (index == -1) {
//                return null;
//            }
//
//            // XXX TODO! What do we do here?
//            return null;
//        }
//
////        ValueManager vm = XhtmlCssEngine.XHTML_VALUE_MANAGERS[index];
//        ValueManager vm = CssProvider.getEngineService().getXhtmlValueManagers()[index];
//
//        if (vm instanceof IdentifierProvider) {
//            return ((IdentifierProvider)vm).getIdentifierMap();
//        }
//
//        return null;
//    }

//    public String[] getCssLengthUnits() {
//        return LENGTH_UNITS;
//    }

//    /**
//     * {@inheritDoc}
//     *
//     * @todo Include properties that I'm not supporting/tracking in the
//     *   designer yet!
//     */
//    public String[] getCssProperties() {
////        if (properties == null) {
//////            ValueManager[] vms = XhtmlCssEngine.XHTML_VALUE_MANAGERS;
////            ValueManager[] vms = CssProvider.getEngineService().getXhtmlValueManagers();
////            ArrayList list = new ArrayList(vms.length);
////
////            for (int i = 0, n = vms.length; i < n; i++) {
////                String property = vms[i].getPropertyName();
////
////                if (property.charAt(0) != '-') { // don't include vendor-specific properties
////                    list.add(property);
////                }
////            }
////
////            Collections.sort(list);
////            properties = (String[])list.toArray(new String[list.size()]);
////        }
////
////        return properties;
//        return CssProvider.getEngineService().getCssProperties();
//    }

//    public CssValue getCssValue(MarkupDesignBean bean, String property) {
////        int index = XhtmlCssEngine.getXhtmlPropertyIndex(property);
//        int index = CssProvider.getEngineService().getXhtmlPropertyIndex(property);
//
//        if (index == -1) {
//            return null;
//        }
//
////        return CssLookup.getValue(bean.getElement(), index);
//        CssLookup.getCssValue(bean.getElement(), index);
//    }

//    public Map convertCssStyleToMap(DesignContext context, String cssStyle) {
//        if(DesignerUtils.DEBUG) {
//            DesignerUtils.debugLog(getClass().getName() + ".convertCssStyleToMap(DesignContext, String)");
//        }
//        if(context == null) {
//            throw(new IllegalArgumentException("Null context."));
//        }
//        if(cssStyle == null) {
//            throw(new IllegalArgumentException("Null style."));
//        }
//        return ((LiveUnit)context).convertCssStyleToMap(cssStyle);
//    }
//
//    public String convertMapToCssStyle(DesignContext context, Map cssStyleMap) {
//        if(DesignerUtils.DEBUG) {
//            DesignerUtils.debugLog(getClass().getName() + ".convertMapToCssStyle(DesignContext, String)");
//        }
//        if(context == null) {
//            throw(new IllegalArgumentException("Null context."));
//        }
//        if(cssStyleMap == null) {
//            throw(new IllegalArgumentException("Null style."));
//        }
//        return ((LiveUnit)context).convertMapToCssStyle(cssStyleMap);
//    }

//    public String[] getHtmlTags() {
//        HtmlTag[] tags = HtmlTag.getTags();
//        ArrayList result = new ArrayList(tags.length);
//
//        for (int i = 0; i < tags.length; i++) {
//            if (tags[i] == null) {
//                break;
//            }
//
//            String name = tags[i].name;
//
//            if (!name.startsWith("jsp:")) { // NOI18N
//                result.add(name);
//            }
//        }
//
//        return (String[])result.toArray(new String[result.size()]);
//    }

//    /**
//     * Show the given line in a particular file.
//     *
//     * @param filename The full path to the file, or null. Exactly one of filename or fileObject
//     *            should be non null.
//     * @param fileObject The FileObject for the file or null. Exactly one of filename or fileObject
//     *            should be non null.
//     * @param lineno The line number
//     * @param openFirst Usually you'll want to pass false. When set to true, this will first open
//     *            the file, then request the given line number; this works around certain bugs for
//     *            some editor types like CSS files.
//     */
//    public void show(String filename, FileObject fileObject, int lineno, int column,
//        boolean openFirst) {
//        assert ((filename != null) && (fileObject == null)) ||
//        ((filename == null) && (fileObject != null));
//
//        if (fileObject != null) {
//            show(fileObject, lineno, column, openFirst);
//        } else {
//            File file = new File(filename);
//            FileObject fo = FileUtil.toFileObject(file);
//
//            if (fo != null) {
//                show(fo, lineno, column, openFirst);
//            }
//        }
//    }
//
//    private static boolean show(FileObject fo, int lineno, int column, boolean openFirst) {
//        throw new RuntimeException("show not yet implemented");
//
//        /*
//        boolean opened = false;
//        DataObject dobj;
//        try {
//            dobj = DataObject.find(fo);
//        }
//        catch (DataObjectNotFoundException ex) {
//            ErrorManager.getDefault().notify(ex);
//            return false;
//        }
//
//        GenericItem item = GenericItem.findItem(dobj);
//        if (item != null) {
//            WebAppProject p = (WebAppProject)item.getProject();
//            if (p != null) {
//                FacesModelSet models = FacesModelSet.getInstance(p);
//                if (models != null) {
//                    FacesModel model = models.getFacesModel(fo);
//                    WebForm wf = WebForm.get(model);
//                    if (wf != null && wf.getDataObject() != null) {
//                        DataObject dobj2 = wf.getDataObject();
//                        dobj = dobj2; ???
//                    }
//                }
//            }
//        }
//
//        if (dobj instanceof JSFDataObject) {
//            EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
//            if (ec instanceof JSFEditorSupport) {
//                JSFEditorSupport jes = (JSFEditorSupport)ec;
//                if ("java".equalsIgnoreCase(fo.getExt())) {
//                   jes.viewJavaSource(-1);
//                } else {
//                    jes.viewJSPSource();
//                }
//                opened = true;
//                // How do we force the line number now? Do we have to or can we rely on the above?
//            }
//        }
//
//        // Try to open doc before showing the line. This SHOULD not be
//        // necessary, except without this the IDE hangs in its attempt
//        // to open the file when the file in question is a CSS file.
//        // Probably a bug in the xml/css module's editorsupport code.
//        // This has the negative effect of first flashing the top
//        // of the file before showing the destination line, so
//        // this operation is made conditional so only clients who
//        // actually need it need to use it.
//        if (openFirst && !opened) {
//            EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
//            if (ec != null) {
//                try {
//                    ec.openDocument(); // ensure that it has been opened - REDUNDANT?
//                    //ec.open();
//                }
//                catch (IOException ex) {
//                    ErrorManager.getDefault().notify(ex);
//                }
//            }
//        }
//
//        LineCookie lc = (LineCookie)dobj.getCookie(LineCookie.class);
//        if (lc != null) {
//            Line.Set ls = lc.getLineSet();
//            if (ls != null) {
//                // -1: convert line numbers to be zero-based
//                Line line = ls.getCurrent(lineno-1);
//                // TODO - pass in a column too?
//                line.show(Line.SHOW_GOTO, column);
//                return true;
//            }
//        }
//
//        return false;
//         */
//    }

    public boolean canDrop(DataFlavor flavor) {
//        // Fish for the designer pane
//        DesignerTopComp dtc = findCurrent();
//
//        if (dtc == null) {
//            return false;
//        }
//
//        // TODO -- additional flavor checking?
//        return true;
//        return DesignerServiceHackProvider.canDrop(flavor);
        // Fish for the designer pane
        JsfTopComponent dtc = findCurrent();

        if (dtc == null) {
            return false;
        }

        // TODO -- additional flavor checking?
        return true;
        
    }

    public void drop(Transferable transferable) {
//        // Fish for the "current" designer pane
//        DesignerTopComp dtc = findCurrent();
//
//        if (dtc == null) {
//            return;
//        }
//
//        DesignerPane pane = dtc.getWebForm().getPane();
//        TransferHandler dth = pane.getTransferHandler();
//
//        // Drop it
//        dth.importData(pane, transferable);
//        DesignerServiceHackProvider.drop(transferable);
        // Fish for the "current" designer pane
        JsfTopComponent dtc = findCurrent();

        if (dtc == null) {
            return;
        }

//        DesignerPane pane = dtc.getWebForm().getPane();
        JComponent pane = dtc.getPane();
        TransferHandler dth = pane.getTransferHandler();

        // Drop it
        dth.importData(pane, transferable);
    }

    /** For temporary use by getCurrentDesigner runnable */
    private static transient TopComponent temptc;
    
    private static TopComponent getCurrentDesigner() {
        if (SwingUtilities.isEventDispatchThread()) {
            return findCurrent();
        } else {
            // FIXME This is incorrect, it can't work.
            // If this can work only in AWT thread,
            // then it should be required on the client to be called only in that
            // thread and not pretend othwerise.
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            temptc = findCurrent();
                        }
                    });

                return temptc;
            } catch (Exception e) {
                ErrorManager.getDefault().notify(e);

                return null;
            } finally {
                temptc = null; // done after return value
            }
        }
    }
    
    /**
     * Attempt to locate the current design view in use; may return
     * null if no designer is found.
     */
    private static JsfTopComponent findCurrent() {
        // Fish for the designer pane
        JsfTopComponent formView = null;

        // Search through workspaces, then modes, then topcomponents
        Set modes = WindowManager.getDefault().getModes();
        Iterator it2 = modes.iterator();

        while (it2.hasNext()) {
            Mode m = (Mode)it2.next();
            TopComponent[] tcs = m.getTopComponents();

            if (tcs != null) {
                for (int j = 0; j < tcs.length; j++) {
                    if (!tcs[j].isShowing()) {
                        continue;
                    }

                    // Go hunting for our DesignerTopComp
                    JsfTopComponent comp = findDesigner(tcs[j], 0);

                    if (comp != null) {
                        if (comp.isShowing()) {
                            return comp;
                        }
                    }
                }
            }

            if (formView != null) {
                break;
            }
        }

        return formView;
    }

    /** Fish for a DesignerPane within a container hierarchy
     */
    private static JsfTopComponent findDesigner(Container c, int depth) {
        if (c == null) {
            return null;
        }

        // Only look slightly into the hiearchy since TopComponents should
        // be near the top
        if (depth == 4) {
            return null;
        }

        depth++;

        int n = c.getComponentCount();

        for (int i = 0; i < n; i++) {
            java.awt.Component child = c.getComponent(i);

            if (child instanceof JsfTopComponent) {
                return (JsfTopComponent)child;
            } else if (child instanceof Container) {
                JsfTopComponent result = findDesigner((Container)child, depth);

                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }
    

//    public void registerTransferable(Transferable transferable) {
////        if(DesignerUtils.DEBUG) {
////            DesignerUtils.debugLog(getClass().getName() + ".registerTransferable(Transferable)");
////        }
////        if(transferable == null) {
////            throw(new IllegalArgumentException("Null transferable."));
////        }
////        DndHandler.setActiveTransferable(transferable);
//        DesignerServiceHackProvider.registerTransferable(transferable);
//    }

//    /** For temporary use by getCurrentDesigner runnable */
//    private transient TopComponent temptc;
//    
//    private TopComponent getCurrentDesigner() {
//        if (SwingUtilities.isEventDispatchThread()) {
//            return findCurrent();
//        } else {
//            // FIXME This is incorrect, it can't work.
//            // If this can work only in AWT thread,
//            // then it should be required on the client to be called only in that
//            // thread and not pretend othwerise.
//            try {
//                SwingUtilities.invokeAndWait(new Runnable() {
//                        public void run() {
//                            temptc = findCurrent();
//                        }
//                    });
//
//                return temptc;
//            } catch (Exception e) {
//                ErrorManager.getDefault().notify(e);
//
//                return null;
//            } finally {
//                temptc = null; // done after return value
//            }
//        }
//    }

    public FileObject getCurrentFile() {
//        DesignerTopComp tc = (DesignerTopComp)getCurrentDesigner();
//
//        if ((tc == null) || (tc.getWebForm().getMarkup() == null)) {
//            return null;
//        }
//
//        return tc.getWebForm().getMarkup().getFileObject();
//        return DesignerServiceHackProvider.getCurrentFile();
        JsfTopComponent tc = (JsfTopComponent)getCurrentDesigner();

//        if ((tc == null) || (tc.getWebForm().getMarkup() == null)) {
        if (tc == null) {
            return null;
        }

//        return tc.getWebForm().getMarkup().getFileObject();
        DataObject jspDataObject = getJspDataObject(tc);
        return jspDataObject == null ? null : jspDataObject.getPrimaryFile();
    }
    
    private static DataObject getJspDataObject(JsfTopComponent jsfTopComponent) {
        return jsfTopComponent.getLookup().lookup(DataObject.class);
    }

    //    public static void testPreview() {
    //        DesignerService ds = DesignerService.getDefault();
    //        HashMap properties = new HashMap();
    //        properties.put("background-color", "red");
    //        URL base = null;
    //        int width = 200;
    //        int height = 200;
    //        BufferedImage img1 = (BufferedImage)ds.getCssPreviewImage(properties, base, width, height);
    //        showScreenshot(img1);
    //
    //        properties = new HashMap();
    //        properties.put("border-color", "blue");
    //        properties.put("border-width", "3px");
    //        properties.put("border-style", "solid");
    //        properties.put("font-size", "24pt");
    //        properties.put("text-decoration", "underline");
    //        base = null;
    //        width = 300;
    //        height = 300;
    //        BufferedImage img2 = (BufferedImage)ds.getCssPreviewImage(properties, base, width, height);
    //        showScreenshot(img2);
    //    
    //    
    //    }
    //    
    //    protected static void showScreenshot(BufferedImage bi) {
    //        try {
    //            File tmp = File.createTempFile("designer", ".png");
    //            tmp.deleteOnExit();
    //            saveImage(bi, tmp);
    //            showScreenshot(tmp);
    //        } catch (java.io.IOException ioe) {
    //            ErrorManager.getDefault().notify(ioe);
    //        }
    //    }
    //    
    //    /** Save the given image to disk */
    //    protected static void saveImage(BufferedImage image, File file) {
    //        try {
    //            if (file.exists()) {
    //                file.delete();
    //            }
    //            ImageIO.write(image, "png", file);
    //        } catch (IOException e) {
    //            System.err.println(e);
    //        }
    //    }
    //
    //    protected static void showScreenshot(File file) {
    //        URL url;
    //        try {
    //            url = new URL("file:" + file.getPath()); // NOI18N
    //        } catch (MalformedURLException e) {
    //            // Can't show URL
    //            ErrorManager.getDefault().notify(e);
    //            return;
    //        }
    //        URLDisplayer.getDefault().showURL(url);
    //    }
//    public void parseCss(javax.swing.text.Document document, Object handler) {
//        if (!(handler instanceof org.w3c.css.sac.ErrorHandler)) {
//            throw new IllegalArgumentException("Handler must be org.w3c.css.sac.ErrorHandler");
//        }
//
//        if (document == null) {
//            throw new IllegalArgumentException("document parameter should not be null!");
//        }
//
//        // Parse document
////        RaveDocument doc = null;
//        // <markup_separation>
////        XhtmlCssEngine engine = XhtmlCssEngine.create(doc, null, null);
//        // ====
////        XhtmlCssEngine engine = XhtmlCssEngine.create(null, null);
////// <moved from engine impl> it doesn't (shoudn't) know about RaveDocument.
//////        if (doc != null) {
//////            doc.setCssEngine(engine);
//////        }
////// </moved from engine impl>
//        Document fakeDocument = new FakeDocument();
//        CssProvider.getEngineService().createCssEngineForDocument(fakeDocument, null);
////        XhtmlCssEngine engine = CssEngineServiceProvider.getDefault().getCssEngine(fakeDocument);
////        
////        // </markup_separation>
////        engine.setErrorHandler((ErrorHandler)handler);
//        CssProvider.getEngineService().setErrorHandlerForDocument(fakeDocument, (ErrorHandler)handler);
//
//        String rules;
//
//        try {
//            rules = document.getText(0, document.getLength());
//        } catch (javax.swing.text.BadLocationException e) {
//            ErrorManager.getDefault().notify(e);
//
//            return;
//        }
//
////        engine.parseStyleSheet(rules, null, "all", null);
//        CssProvider.getEngineService().parseStyleSheetForDocument(fakeDocument, rules, null, "all", null); // NOI18N
////        engine.setErrorHandler(null);
//        CssProvider.getEngineService().setErrorHandlerForDocument(fakeDocument, null);
//    }
    
//    /** XXX Fake document, to be able to create engine.
//     * TODO Better is just to impl the <code>Document</code> interface, without dep on xerces. */
//    private static class FakeDocument extends DocumentImpl {
//    } // End of FakeDocument.

    public Object getTableInfo(MarkupDesignBean bean) {
//        assert bean.getElement() != null;
//
//        CssBox box = CssBox.getBox(bean.getElement());
//
//        if (box instanceof TableBox) {
//            return box;
//        }
//
//        return null;
        Element componentRootElement = JsfSupportUtilities.getComponentRootElementForDesignBean(bean);
        if (componentRootElement == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new NullPointerException("There is no element in markup design bean=" + bean)); // NOI18N
            return null;
        }
//        return DesignerServiceHackProvider.getTableInfo(componentRootElement);
        Designer[] designers = JsfForm.findDesignersForElement(componentRootElement);
        Designer designer = designers.length > 0 ? designers[0] : null;
        if (designer == null) {
            return null;
        }
        Box box = designer.findBoxForComponentRootElement(componentRootElement);
        return  DesignerServiceHackProvider.isTableBox(box) ? box : null;
    }

    public Element getCellElement(Object tableInfo, int row, int column) {
//        assert tableInfo instanceof TableBox;
//
//        TableBox table = (TableBox)tableInfo;
//        CssBox box = table.getCell(row, column);
//
//        if (box == null) {
//            return null;
//        }
//
//        return box.getElement();
        return DesignerServiceHackProvider.getCellElement(tableInfo, row, column);
    }

    public MarkupDesignBean getCellBean(Object tableInfo, int row, int column) {
//        assert tableInfo instanceof TableBox;
//
//        TableBox table = (TableBox)tableInfo;
//        CssBox box = table.getCell(row, column);
//
//        if (box == null) {
//            return null;
//        }
//
//        return box.getDesignBean();
        Element componentRootElement = DesignerServiceHackProvider.getCellComponent(tableInfo, row, column);
        return MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
    }

    public int getColSpan(Object tableInfo, int row, int column) {
//        assert tableInfo instanceof TableBox;
//
//        TableBox table = (TableBox)tableInfo;
//
//        return table.getCellSpan(CssBox.Y_AXIS, row, column);
        return DesignerServiceHackProvider.getColSpan(tableInfo, row, column);
    }

    public int getRowSpan(Object tableInfo, int row, int column) {
//        assert tableInfo instanceof TableBox;
//
//        TableBox table = (TableBox)tableInfo;
//
//        return table.getCellSpan(CssBox.X_AXIS, row, column);
        return DesignerServiceHackProvider.getRowSpan(tableInfo, row, column);
    }

    public int getColumnCount(Object tableInfo) {
//        assert tableInfo instanceof TableBox;
//
//        TableBox table = (TableBox)tableInfo;
//
//        return table.getColumns();
        return DesignerServiceHackProvider.getColumnCount(tableInfo);
    }

    public int getRowCount(Object tableInfo) {
//        assert tableInfo instanceof TableBox;
//
//        TableBox table = (TableBox)tableInfo;
//
//        return table.getRows();
        return DesignerServiceHackProvider.getRowCount(tableInfo);
    }

//    public void removeCssProperty(MarkupDesignBean bean, String property) {
////        int index = XhtmlCssEngine.getXhtmlPropertyIndex(property);
//        int index = CssProvider.getEngineService().getXhtmlPropertyIndex(property);
//
//        if (index != -1) {
//            // TODO -- update the -rendered- element!
////            CssLookup.removeLocalStyleValue(bean.getElement(), index);
//            CssProvider.getEngineService().removeLocalStyleValueForElement(bean.getElement(), index);
//        }
//    }
//
//    public void setCssProperty(MarkupDesignBean bean, String property, String value) {
////        int index = XhtmlCssEngine.getXhtmlPropertyIndex(property);
//        int index = CssProvider.getEngineService().getXhtmlPropertyIndex(property);
//
//        if (index != -1) {
//            // TODO -- update the -rendered- element!
////            CssLookup.setLocalStyleValue(bean.getElement(), index, value);
//            CssProvider.getEngineService().addLocalStyleValueForElement(bean.getElement(), index, value);
//        }
//    }

//    public URL resolveUrl(URL base, Document document, String src) {
//        if (src == null) {
//            src = "";
//        }
//
//        // TODO after Reef: push this into superclass
//        URL reference = null;
//
//        // Relative to the web folder?
//        if (src.startsWith("/")) { // NOI18N
//
//            // What if it's a local file, e.g. /home/tor/foo.jspf?? that wouldn't work at deploy time anyway..
//            try {
//                // <markup_separation>
////                MarkupUnit markup = ((RaveDocument)document).getMarkup();
////                FileObject fo = markup.getFileObject();
//                // ====
//                FileObject fo = InSyncServiceProvider.getProvider().getFileObject(document);
//                // </markup_separation>
//                Project project = FileOwnerQuery.getOwner(fo);
//
//                if (project != null) {
//                    FileObject webroot = JSFProjectUtil.getDocumentRoot(project);
//                    reference = FileUtil.toFile(webroot).toURI().toURL();
//                }
//
//                src = src.substring(1); // strip off leading "/" or URL class will ignore base
//            } catch (Exception ex) {
//                reference = base;
//            }
//        } else {
//            reference = base;
//        }
//
//        try {
//            URL u = new URL(reference, src); // XXX what if it's absolute?
//
//            return u;
//        } catch (MalformedURLException e) {
//            ErrorManager.getDefault().notify(e);
//
//            return null;
//        }
//    }

//    public Element getBody(Document document) {
//        // <markup_separation>
////        MarkupUnit markup = ((RaveDocument)document).getMarkup();
////        DataObject dobj = markup.getDataObject();
//        // ====
//        FileObject fo = InSyncServiceProvider.getProvider().getFileObject(document);
//        DataObject dobj;
//        // XXX Copied form insync.
//        if (fo != null && !fo.isValid()) {
//            dobj = null;
//        } else {
//            try {
//                dobj = DataObject.find(fo);
//            } catch (DataObjectNotFoundException dnfe) {
//                dobj = null;
//            }
//        }
//        // </markup_separation>
//
//        if (WebForm.isWebFormDataObject(dobj)) {
//            WebForm webform = WebForm.getWebFormForDataObject(dobj);
//
//            return webform.getBody();
//        } else {
//            throw new IllegalStateException(
//                "Wrong document parameter in DesignerService.getBody dobj=" + dobj); // NOI18N
//        }
//    }

//    public boolean isWebPage(FileObject fo) {
//        String mime = fo.getMIMEType();
//
//        String[] mimeTypes = FORM_MIME_TYPES;
//
//        for (int i = 0; i < mimeTypes.length; i++) {
//            if (mimeTypes[i].equals(mime)) {
//                return true;
//            }
//        }
//
//        return false;
//    }
//
//    public String[] getMimeTypes() {
//        return FORM_MIME_TYPES;
//    }
//
//    public List getWebPages(Project project, boolean includePages, boolean includeFragments) {
//        ArrayList list = new ArrayList(20);
//        FileObject fobj = JSFProjectUtil.getDocumentRoot(project);
//        addWebPages(list, fobj, includePages, includeFragments);
//
//        return list;
//    }
//
//    private void addWebPages(List list, FileObject folder, boolean includePages,
//        boolean includeFragments) {
//        if(DesignerUtils.DEBUG) {
//            DesignerUtils.debugLog(getClass().getName() + ".addWebPages(List, FileObject, boolean, boolean)");
//        }
//        if(folder == null) {
//            throw(new IllegalArgumentException("Null folder."));
//        }
//        if(list == null) {
//            throw(new IllegalArgumentException("Null list."));
//        }
//
//        FileObject[] children = folder.getChildren();
//
//        for (int i = 0; i < children.length; i++) {
//            FileObject fo = children[i];
//
//            if (fo.isFolder()) {
//                addWebPages(list, fo, includePages, includeFragments);
//            } else {
//                if (isWebPage(fo)) {
//                    boolean isFragment = "jspf".equals(fo.getExt()); // NOI18N
//
//                    if (isFragment) {
//                        if (includeFragments) {
//                            list.add(fo);
//                        }
//                    } else if (includePages) {
//                        list.add(fo);
//                    }
//                }
//            }
//        }
//    }

    // Moved to insync.
//    public boolean isBraveheartPage(Document document) {
//        return DesignerUtils.isBraveheartPage(document);
//    }
//
//    public boolean isBraveheartPage(FileObject fo) {
//        DataObject dobj = null;
//
//        try {
//            dobj = DataObject.find(fo);
//        } catch (DataObjectNotFoundException ex) {
//            return false;
//        }
//
//        WebForm webform = DesignerUtils.getWebForm(dobj, false);
//
//        if (webform == null) {
//            return false;
//        }
//
//        Document dom = webform.getJspDom();
//
//        if (dom != null) {
//            return isBraveheartPage(dom);
//        }
//
//        return false;
//    }

    /** Weak ref to the last task processing. */
    private static WeakReference<RequestProcessor.Task> lastTaskWRef = new WeakReference<RequestProcessor.Task>(null);
    
    public void notifyCssEdited(final DataObject dobj) {
//        DesignerTopComp.setPendingRefreshAll();
//        DesignerServiceHackProvider.notifyCssEdited(dobj);
        if (dobj == null) {
            return;
        }

        // This method is called when editing the css, and this take care not to cause performance
        // issues with processing too many refresh tasks.
        RequestProcessor.Task lastTask = lastTaskWRef.get();
        boolean previousTaskRunning;
        if (lastTask != null && !lastTask.isFinished()) {
            // XXX Cancel only the task which has the same dobj. But how, the RequestProcessor.Task is final.
            // Assuming this omission should be fine, there shouldn't occur sudden edit from different projects.
            previousTaskRunning = !lastTask.cancel();
        } else {
            previousTaskRunning = false;
        }
        int delay = previousTaskRunning ? 500 : 200;
        
        RequestProcessor.Task newTask = RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        Project project = FileOwnerQuery.getOwner(dobj.getPrimaryFile());
                        if (project != null) {
                            JsfForm.refreshDesignersInProject(project);
                        }
                    }
                });
            }
        }, delay);
        lastTaskWRef = new WeakReference<RequestProcessor.Task>(newTask);
    }

//    public void refresh(Project project, DataObject dobj, boolean deep) {
////        if (dobj != null) {
////            DesignerActions.refresh(dobj, deep);
////        } else {
////            DesignerActions.refreshAll(project, deep);
////        }
//        DesignerServiceHackProvider.refresh(project, dobj, deep);
//    }
//    public void refreshDataObject(DataObject dobj, boolean deep) {
////        DesignerServiceHackProvider.refreshDataObject(dobj, deep);
//        RefreshServiceImpl.refreshDataObject(dobj, deep);
//    }
//    public void refreshProject(Project project, boolean deep) {
//        DesignerServiceHackProvider.refreshProject(project, deep);
//    }

//    public void destroyWebFormForFileObject(FileObject fo) {
////        WebForm webform = WebForm.findWebFormForFileObject(fo);
////        if (webform != null) {
////            webform.destroy();
////            WebForm.removeWebFormForFileObject(fo);
////        }
//        DesignerServiceHackProvider.destroyWebFormForFileObject(fo);
//    }

//    public void detachTopComponentForDataObject(DataObject dobj) {
//        if (WebForm.hasWebFormForDataObject(dobj)) {
//            WebForm webform = WebForm.getWebFormForDataObject(dobj);
//            webform.detachTopComponent();
//        }
//    }

//    public MultiViewElement getMultiViewElementForDataObject(DataObject dobj) {
//        if (WebForm.isWebFormDataObject(dobj)) {
//            WebForm webform = WebForm.getWebFormForDataObject(dobj);
//            webform.createTopComponent();
//
//            return webform.getTopComponent();
//        }
//
//        return null;
//    }
    
    
//    /**
//     * Attempt to locate the current design view in use; may return
//     * null if no designer is found.
//     */
//    private static DesignerTopComp findCurrent() {
//        // Fish for the designer pane
//        DesignerTopComp formView = null;
//
//        // Search through workspaces, then modes, then topcomponents
//        Set modes = WindowManager.getDefault().getModes();
//        Iterator it2 = modes.iterator();
//
//        while (it2.hasNext()) {
//            Mode m = (Mode)it2.next();
//            TopComponent[] tcs = m.getTopComponents();
//
//            if (tcs != null) {
//                for (int j = 0; j < tcs.length; j++) {
//                    if (!tcs[j].isShowing()) {
//                        continue;
//                    }
//
//                    // Go hunting for our DesignerTopComp
//                    DesignerTopComp comp = findDesigner(tcs[j], 0);
//
//                    if (comp != null) {
//                        if (comp.isShowing()) {
//                            return comp;
//                        }
//                    }
//                }
//            }
//
//            if (formView != null) {
//                break;
//            }
//        }
//
//        return formView;
//    }
//
//    /** Fish for a DesignerPane within a container hierarchy
//     */
//    private static DesignerTopComp findDesigner(Container c, int depth) {
//        if (c == null) {
//            return null;
//        }
//
//        // Only look slightly into the hiearchy since TopComponents should
//        // be near the top
//        if (depth == 4) {
//            return null;
//        }
//
//        depth++;
//
//        int n = c.getComponentCount();
//
//        for (int i = 0; i < n; i++) {
//            java.awt.Component child = c.getComponent(i);
//
//            if (child instanceof DesignerTopComp) {
//                return (DesignerTopComp)child;
//            } else if (child instanceof Container) {
//                DesignerTopComp result = findDesigner((Container)child, depth);
//
//                if (result != null) {
//                    return result;
//                }
//            }
//        }
//
//        return null;
//    }

    
//    public float getBlockWidth(Element element) {
//        return CssBox.getBlockWidth(element);
//    }
//    
//    public float getBlockHeight(Element element) {
//        return CssBox.getBlockHeight(element);
//    }

    
    public void copyBoxForElement(Element fromElement, Element toElement) {
//        CssBox.copyBoxForElement(fromElement, toElement);
//        DesignerServiceHackProvider.copyBoxForElement(fromElement, toElement);
        Designer[] designers = JsfForm.findDesignersForElement(toElement);
        for (Designer designer : designers) {
            designer.copyBoxForElement(fromElement, toElement);
        }
    }

// <missing designtime api>

// <separation of models>
    public FileObject getContextFileForFragmentFile(FileObject fragmentFile) {
//        WebForm webform = WebForm.findWebFormForFileObject(fragmentFile);
//        WebForm contextWebform;
//        if (webform == null) {
//            contextWebform = null;
//        } else {
//            contextWebform = webform.getContextPage();
//        }
//        
//        return contextWebform == null ? null : contextWebform.getModel().getMarkupFile();
//        return DesignerServiceHackProvider.getContextFileForFragmentFile(fragmentFile);
//        WebForm webform = WebForm.findWebFormForFileObject(fragmentFile);
        JsfForm jsfForm = JsfForm.findJsfForm(fragmentFile);
        
//        WebForm contextWebform;
//        if (webform == null) {
//            contextWebform = null;
//        } else {
//            contextWebform = webform.getContextPage();
//        }
        JsfForm contextJsfForm = jsfForm == null ? null : jsfForm.getContextJsfForm();
        
//        return contextWebform == null ? null : contextWebform.getModel().getMarkupFile();
//        if (contextWebform == null) {
        if (contextJsfForm == null) {
            return null;
        }
//        DataObject jspDataObject = contextWebform.getJspDataObject();
        DataObject jspDataObject = contextJsfForm.getJspDataObject();
        return jspDataObject == null ? null : jspDataObject.getPrimaryFile();
    }
    
    public FileObject getExternalFormFileForElement(Element element) {
//        CssBox includeBox = CssBox.getBox(element);
//
//        if ((includeBox != null) && includeBox instanceof JspIncludeBox) {
//            WebForm frameForm = ((JspIncludeBox)includeBox).getExternalForm();
//
//            if ((frameForm != null) && (frameForm != WebForm.EXTERNAL)) {
//                return frameForm.getModel().getMarkupFile();
//            }
//        }
//        return null;
//        return DesignerServiceHackProvider.getExternalFormFileForElement(element);
        JsfForm jsfForm = JsfForm.findJsfForm(element);
        if (jsfForm == null) {
            return null;
        }
        
        // XXX Instead of traversing the boxes, traverse the elements directly.
//        CssBox includeBox = CssBox.getBox(element);
        Designer[] designers = JsfForm.findDesigners(jsfForm);
        if (designers.length == 0) {
            return null;
        }
        Designer designer = designers[0];
        Box includeBox = designer.findBoxForElement(element);

        if (includeBox instanceof ExternalBox) {
            DomProvider domProvider = ((ExternalBox)includeBox).getExternalDomProvider();
            JsfForm frameForm = JsfForm.findJsfFormForDomProvider(domProvider);

//            if ((frameForm != null) && (frameForm != WebForm.EXTERNAL)) {
            if (frameForm != null) {
//                return frameForm.getModel().getMarkupFile();
                DataObject jspDataObject = frameForm.getJspDataObject();
                return jspDataObject == null ? null : jspDataObject.getPrimaryFile();
            }
        }
        return null;
    }
    
// </separation of models>
    
// </missing designtime api>
    
//    /**
//     * XXX Horrible method, too long, needs to be refactored, it is unreadable now.
//     * Paint a preview of the given component, with the given CSS style
//     * applied, and return it as an image. Use the preferred initial
//     * width, unless the component is larger.
//     */
//    private static BufferedImage paintCssPreview(Graphics2D g2d, URL base, Map properties, int width, int height) {
//        // Restore?
//        BufferedImage image = null;
//
//        if (g2d != null) {
//            GraphicsConfiguration config = g2d.getDeviceConfiguration();
//            image = config.createCompatibleImage(width, height);
//        } else {
//            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//        }
//
//        if (image == null) {
//            return null;
//        }
//
//        Graphics2D og = (Graphics2D)image.getGraphics();
//
//        try {
////            RaveDocument doc = null;
//            // <markup_separation>
////            XhtmlCssEngine engine = XhtmlCssEngine.create(doc, null, base);
//            // ====
////            XhtmlCssEngine engine = XhtmlCssEngine.create(null, base);
////// <moved from engine impl> it doesn't (shoudn't) know about RaveDocument.
//////            if (doc != null) {
//////                doc.setCssEngine(engine);
//////            }
////// </moved from engine impl>
//            Document fakeDocument = new FakeDocument();
//            CssEngineService cssEngineService = CssProvider.getEngineService();
//            cssEngineService.createCssEngineForDocument(fakeDocument, null);
////            XhtmlCssEngine engine = CssEngineServiceProvider.getDefault().getCssEngine(fakeDocument);
//            
//            // </markup_separation>
////            engine.setErrorHandler(XhtmlCssEngine.SILENT_ERROR_HANDLER);
//            cssEngineService.setSilentErrorHandlerForDocument(fakeDocument);
//
////            String styles = engine.mapToStyle(properties);
//            String styles = cssEngineService.getStringFromStyleMapForDocument(fakeDocument, properties);
////            PreviewElement element = new PreviewElement(fakeDocument, /*engine,*/ base, styles);
//            Element element = cssEngineService.createPreviewElementForDocument(fakeDocument, base, styles);
//
////            Color bg = CssLookup.getColor(element, XhtmlCss.BACKGROUND_COLOR_INDEX);
//            Color bg = CssProvider.getValueService().getColorForElement(element, XhtmlCss.BACKGROUND_COLOR_INDEX);
//
//            if (bg != null) {
//                og.setColor(bg);
//                og.fillRect(0, 0, width, height);
//            } else {
//                // Use a transparent color.... any will do!
//                //		Color curr = g2d.getColor();
//                //		og.setColor(new Color(curr.getRed(), curr.getGreen(), curr.getBlue(), 0));
//                //og.setColor(new Color(0, 0, 0, 0));
//                bg = (Color)UIManager.getDefaults().get("Label.background"); // NOI18N
//                og.setColor(bg);
//                og.fillRect(0, 0, width, height);
//            }
//
//            //            ImageIcon bgImage = BackgroundImagePainter.getBackgroundImage(doc, element);
////            ImageIcon bgImage = BackgroundImagePainter.getBackgroundImage(base, element);
//            URL imageUrl = CssBoxUtilities.getBackgroundImageUrl(element, base);
//            ImageIcon bgImage = imageUrl == null ? null : new ImageIcon(imageUrl);
//
//            if (bgImage != null) {
////                Value repeatValue = CssLookup.getValue(element, XhtmlCss.BACKGROUND_REPEAT_INDEX);
//                CssValue cssRepeatValue = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.BACKGROUND_REPEAT_INDEX);
////                ListValue positionValue =
////                    CssLookup.getListValue(CssLookup.getValue(element,
////                            XhtmlCss.BACKGROUND_POSITION_INDEX));
//                CssListValue cssPositionValue = CssProvider.getValueService().getComputedCssListValue(
//                        CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.BACKGROUND_POSITION_INDEX));
////                BackgroundImagePainter bgPainter =
////                    new BackgroundImagePainter(bgImage, repeatValue, positionValue);
//                BackgroundImagePainter bgPainter = new BackgroundImagePainter(bgImage, cssRepeatValue, cssPositionValue);
//
//                if (bgPainter != null) {
//                    bgPainter.paint(og, 0, 0, width, height);
//                }
//            }
//
//            boolean hasText = false;
//            boolean hasBorder = false;
//            boolean hasPosition = false;
//            Iterator it = properties.keySet().iterator();
//
//            while (it.hasNext()) {
//                String property = (String)it.next();
//
////                if (isPositionProperty(property)) {
//                if (CssProvider.getValueService().isPositionProperty(property)) {
//                    hasPosition = true;
//                }
//
////                if (isTextProperty(property)) {
//                if (CssProvider.getValueService().isTextProperty(property)) {
//                    // Insert text
//                    hasText = true;
//                }
//
////                if (isBorderProperty(property)) {
//                if (property.startsWith("border-")) { // NOI18N
//                    hasBorder = true;
//                }
//            }
//
////            if (hasPosition) {
////                // Do some position painting (abbreviated)
////            }
//
//            CssBorder border = null;
//
//            if (hasBorder) {
//                // Paint border
//                // XXX If you just set ONE property (like color) but
//                // not solid or anything else, we don't preview! That's not good...
//                border = CssBorder.getBorder(element);
//
//                if (border != null) {
//                    border.paintBorder(og, 0, 0, width, height);
//                }
//            }
//
//            if (hasText) {
//                // Paint text
//                // Check font size and attributes
//                int decoration = 0;
//                Color fg = Color.black;
//                FontMetrics metrics = null;
//                //boolean collapseSpaces = true;
//                //boolean hidden = false;
////                metrics = CssLookup.getFontMetrics(element);
//                metrics = CssProvider.getValueService().getFontMetricsForElement(element);
////                fg = CssLookup.getColor(element, XhtmlCss.COLOR_INDEX);
//                fg = CssProvider.getValueService().getColorForElement(element, XhtmlCss.COLOR_INDEX);
//
//                if (fg == null) {
//                    if (fg == null) {
//                        fg = Color.black;
//                    }
//                }
//
////                Value val = CssLookup.getValue(element, XhtmlCss.TEXT_DECORATION_INDEX);
//                CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.TEXT_DECORATION_INDEX);
//
////                switch (val.getCssValueType()) {
////                case CSSValue.CSS_VALUE_LIST:
////
////                    ListValue lst = CssLookup.getListValue(val);
////                
////                    if (lst == null) {
////                        break;
////                    }
//                CssListValue cssList = CssProvider.getValueService().getComputedCssListValue(cssValue);
//                if (cssList != null) {
//
////                    int len = lst.getLength();
//                    int len = cssList.getLength();
//
//                    for (int i = 0; i < len; i++) {
////                        Value v = lst.item(i);
//                        CssValue cssV = cssList.item(i);
////                        String s = v.getStringValue();
//                        String s = cssV.getStringValue();
//
//                        switch (s.charAt(0)) {
//                        case 'u':
//                            decoration |= TextBox.UNDERLINE;
//
//                            break;
//
//                        case 'o':
//                            decoration |= TextBox.OVERLINE;
//
//                            break;
//
//                        case 'l':
//                            decoration |= TextBox.STRIKE;
//
//                            break;
//                        }
//                    }
//
////                    break;
////                default:
//                } else {
//                    // XXX what happened?
//                }
//
//                // XXX Technically, should check for decoration=="overline" too...
//                // (See section 16.3.1). However, does that have ANY practical
//                // utility?
////                val = CssLookup.getValue(element, XhtmlCss.WHITE_SPACE_INDEX);
////
////                if ((val == CssValueConstants.PRE_VALUE) ||
////                        (val == CssValueConstants.PRE_WRAP_VALUE)) {
////                    collapseSpaces = false;
////                }
//
//                String content = "ABCabc123";
////                Value v1 = CssLookup.getValue(element, XhtmlCss.FONT_VARIANT_INDEX);
////                Value v2 = CssLookup.getValue(element, XhtmlCss.TEXT_TRANSFORM_INDEX);
//                CssValue cssV1 = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.FONT_VARIANT_INDEX);
//                CssValue cssV2 = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.TEXT_TRANSFORM_INDEX);
//
////                if ((v1 == CssValueConstants.SMALL_CAPS_VALUE) ||
////                        (v2 == CssValueConstants.UPPERCASE_VALUE)) {
//                if (CssProvider.getValueService().isSmallCapsValue(cssV1)
//                || CssProvider.getValueService().isUpperCaseValue(cssV2)) {
//                    // Uppercase the text
//                    content = content.toUpperCase();
//
//                    // TODO (much later): split the text up like under capitalization
//                    // and apply different fonts to the initial letters
//                    // and the rest of the words. I can't trivially do that
//                    // here because I would create separate TextBoxes for the
//                    // initial character and the rest of the words, and this
//                    // COULD be split up both in text justification and in word
//                    // wrapping by the LineBox and LineBoxGroup containers, which
//                    // would be visually disasterous. I think the painting of
//                    // this would really have to be done in the TextBox itself.
////                } else if (v2 == CssValueConstants.LOWERCASE_VALUE) {
//                } else if (CssProvider.getValueService().isLowerCaseValue(cssV2)) {
//                    content = content.toLowerCase();
////                } else if (v2 == CssValueConstants.CAPITALIZE_VALUE) {
//                } else if (CssProvider.getValueService().isCapitalizeValue(cssV2)) {
//                    content = "Abcabc123";
//                }
//
////                int leftMargin = CssLookup.getLength(element, XhtmlCss.MARGIN_LEFT_INDEX);
//                int leftMargin = CssBox.getCssLength(element, XhtmlCss.MARGIN_LEFT_INDEX);
////                int rightMargin = CssLookup.getLength(element, XhtmlCss.MARGIN_RIGHT_INDEX);
////                int topMargin = CssLookup.getLength(element, XhtmlCss.MARGIN_TOP_INDEX);
//                int topMargin = CssBox.getCssLength(element, XhtmlCss.MARGIN_TOP_INDEX);
////                int bottomMargin = CssLookup.getLength(element, XhtmlCss.MARGIN_BOTTOM_INDEX);
//
////                int leftPadding = CssLookup.getLength(element, XhtmlCss.PADDING_LEFT_INDEX);
////                int rightPadding = CssLookup.getLength(element, XhtmlCss.PADDING_RIGHT_INDEX);
//                int leftPadding = CssBox.getCssLength(element, XhtmlCss.PADDING_LEFT_INDEX);
//                int rightPadding = CssBox.getCssLength(element, XhtmlCss.PADDING_RIGHT_INDEX);
//
//                // Unlike margins, padding values are not allowed to be negative!
//                if (leftPadding < 0) {
//                    leftPadding = 0;
//                }
//
//                if (rightPadding < 0) {
//                    rightPadding = 0;
//                }
//
////                int topPadding = CssLookup.getLength(element, XhtmlCss.PADDING_TOP_INDEX);
////                int bottomPadding = CssLookup.getLength(element, XhtmlCss.PADDING_BOTTOM_INDEX);
//                int topPadding = CssBox.getCssLength(element, XhtmlCss.PADDING_TOP_INDEX);
//                int bottomPadding = CssBox.getCssLength(element, XhtmlCss.PADDING_BOTTOM_INDEX);
//
//                if (topPadding < 0) {
//                    topPadding = 0;
//                }
//
//                if (bottomPadding < 0) {
//                    bottomPadding = 0;
//                }
//
//                int leftBorderWidth = 0;
//                int topBorderWidth = 0;
////                int rightBorderWidth = 0;
////                int bottomBorderWidth = 0;
//
//                if (border != null) {
//                    leftBorderWidth = border.getLeftBorderWidth();
//                    topBorderWidth = border.getTopBorderWidth();
////                    bottomBorderWidth = border.getBottomBorderWidth();
////                    rightBorderWidth = border.getRightBorderWidth();
//                }
//
//                int x = leftMargin + leftBorderWidth + leftPadding;
//                int y = topMargin + topBorderWidth + topPadding;
//                og.setColor(fg);
//                og.setFont(metrics.getFont());
//
//                // determine the y coordinate to render the glyphs
//                int yadj = (y + metrics.getHeight()) - metrics.getDescent();
//
//                // Draw text!
//                char[] contentChars = content.toCharArray();
//                og.drawChars(contentChars, 0, contentChars.length, x, yadj);
//
//                // render underline or strikethrough if set.
//                if (decoration != 0) {
//                    int textWidth =
//                        DesignerUtils.getNonTabbedTextWidth(contentChars, 0, contentChars.length,
//                            metrics);
//
//                    if ((decoration & TextBox.UNDERLINE) != 0) {
//                        int yTmp = yadj;
//                        yTmp += 1;
//                        og.drawLine(x, yTmp, x + textWidth, yTmp);
//                    }
//
//                    if ((decoration & TextBox.STRIKE) != 0) {
//                        int yTmp = yadj;
//
//                        // move y coordinate above baseline
//                        yTmp -= (int)(metrics.getAscent() * 0.4f);
//                        og.drawLine(x, yTmp, x + textWidth, yTmp);
//                    }
//
//                    if ((decoration & TextBox.OVERLINE) != 0) {
//                        og.drawLine(x, y, x + textWidth, y);
//                    }
//                }
//            }
//        } finally {
//            og.dispose();
//        }
//
//        return image;
//    }

    
    // XXX Hack.
    private static MarkupDesignBean adjustRenderBeanHack(MarkupDesignBean renderBean) {
        // Handle hyperlinks. We really need to render its surrounding content
        // to see the CS stylerules for <a> apply
        if (renderBean.getInstance() instanceof HtmlOutputText) {
            DesignBean parent = renderBean.getBeanParent();

            if ((parent != null) && (parent.getChildBeanCount() == 1) &&
                    (parent.getInstance() instanceof HtmlCommandLink ||
                    parent.getInstance() instanceof HtmlOutputLink)) {
                renderBean = (MarkupDesignBean)parent;
            }
        }
        
        // Embedded table portions (rowgroups, columns) aren't happy being rendered
        // without their surrounding table.
        // It would be better to modify the preview code to actually go and -try- rendering
        // components and then progressively retry on parents until it succeeds.
        // But given that the code is freezing today I'm playing it safe
        if (renderBean.getInstance() instanceof com.sun.rave.web.ui.component.TableColumn
        || renderBean.getInstance() instanceof com.sun.webui.jsf.component.TableColumn) {
            if (renderBean.getBeanParent() instanceof MarkupDesignBean) {
                renderBean = (MarkupDesignBean)renderBean.getBeanParent();
            } else {
                return null;
            }
        } else if (renderBean.getBeanParent().getInstance() instanceof com.sun.rave.web.ui.component.TableColumn
        || renderBean.getBeanParent().getInstance() instanceof com.sun.webui.jsf.component.TableColumn) {
            // We also have to render components that are children of a TableColumn as part of the whole
            // table as well, because their value binding expressions can involve data providers set up
            // by the table. This is clearly not a clean solution. See comment above about trying arbitary
            // rendering instead. This breaks once you nest components in a column inside a container
            // component for example. Just doing a low risk, 90% fix now right before FCS.
            if (renderBean.getBeanParent().getBeanParent() instanceof MarkupDesignBean) {
                renderBean = (MarkupDesignBean)renderBean.getBeanParent().getBeanParent();
            } else {
                return null;
            }
        }

        // Not else: a TableColumn can be inside a TableRowGroup so keep moving outwards if necessary:
        if (renderBean.getInstance() instanceof com.sun.rave.web.ui.component.TableRowGroup
        || renderBean.getInstance() instanceof com.sun.webui.jsf.component.TableRowGroup) {
            if (renderBean.getBeanParent() instanceof MarkupDesignBean) {
                renderBean = (MarkupDesignBean)renderBean.getBeanParent();
            } else {
                return null;
            }
        }
        return renderBean;
    }

    
    // XXX Moved from designer/../DesignerUtils.
////     Same as style set in Text renderer and in default style sheet
//    private static final String ignoreClass = "rave-uninitialized-text"; // NOI18N

    // XXX Moved from designer/../DesignerUtils.
    /** Recursively remove the rave-uninitialized-text class attribute
     *  from a node tree.
     * @return True iff any nodes were actually changed
     */
//    private static boolean stripDesignStyleClasses(Node node) {
//        boolean changedStyles = false;
//        
////        if(DEBUG) {
////            debugLog(DesignerUtils.class.getName() + ".stripDesignStyleClasses(Node)");
////        }
//        if(node == null) {
//            throw(new IllegalArgumentException("Null node."));// NOI18N
//        }
//        
//        if (node.getNodeType() == Node.ELEMENT_NODE) {
//            Element e = (Element)node;
//            
//            if (e.getAttribute(HtmlAttribute.CLASS).indexOf(ignoreClass) != -1) {
//                String newClass = e.getAttribute(HtmlAttribute.CLASS).replaceAll(ignoreClass, ""); // ignore stripped out
//                e.setAttribute(HtmlAttribute.CLASS, newClass);
//                changedStyles = true;
//            }
//        }
//        
//        NodeList nl = node.getChildNodes();
//        
//        for (int i = 0, n = nl.getLength(); i < n; i++) {
//            changedStyles |= stripDesignStyleClasses(nl.item(i)); // recurse
//        }
//        
//        return changedStyles;
//    }
    
}

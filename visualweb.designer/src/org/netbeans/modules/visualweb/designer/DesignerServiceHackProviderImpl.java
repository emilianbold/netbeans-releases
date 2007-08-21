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

package org.netbeans.modules.visualweb.designer;

import org.netbeans.modules.visualweb.api.designer.cssengine.CssEngineService;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssListValue;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssValue;
import org.netbeans.modules.visualweb.api.designer.cssengine.XhtmlCss;
import org.netbeans.modules.visualweb.css2.BackgroundImagePainter;
import org.netbeans.modules.visualweb.css2.CssBorder;
import org.netbeans.modules.visualweb.css2.CssBox;
import org.netbeans.modules.visualweb.designer.CssUtilities;
import org.netbeans.modules.visualweb.css2.PageBox;
import org.netbeans.modules.visualweb.css2.TableBox;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

import org.apache.xerces.dom.DocumentImpl;
import org.netbeans.modules.visualweb.api.designer.Designer;
import org.openide.windows.WindowManager;
import org.w3c.dom.Document;
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
public final class DesignerServiceHackProviderImpl /*extends DesignerServiceHack*/ {
//    private static final String[] LENGTH_UNITS =
//        { "%", "em", "ex", "px", "cm", "mm", "in", "pt", "pc" };
//    private static volatile String[] properties;

//    /**
//     * The following mime types are valid mime types for files
//     * that will be considered webforms in the WebAppProject
//     */
//    private static final String[] FORM_MIME_TYPES = new String[] { "text/x-jsp" }; // NOI18N

//    /** For temporary use by getCurrentDesigner runnable */
//    private static transient TopComponent temptc;

    private DesignerServiceHackProviderImpl() {
    }

//    private static FacesPageUnit getFacesUnit(DesignContext context) {
//        LiveUnit lu = (LiveUnit)context;
//
//        // Find the model
//        BeansUnit bu = lu.getBeansUnit();
//
//        if (!(bu instanceof FacesPageUnit)) {
//            return null;
//        }
//
//        return (FacesPageUnit)bu;
//    }

//    public static Image getCssPreviewImage(String cssStyle, String[] cssStyleClasses,
//        MarkupDesignBean bean, int width, int height) {
//        if (bean.getElement() == null) {
//            return null;
//        }
//
//        FacesPageUnit fu = getFacesUnit(bean.getDesignContext());
//
//        if (fu == null) {
//            return null;
//        }
//
//        MarkupUnit mu = fu.getPageUnit();
//        FileObject fo = mu.getFileObject();
//        DataObject dobj = null;
//
//        try {
//            dobj = DataObject.find(fo);
//        } catch (DataObjectNotFoundException ex) {
//            return null;
//        }
//
//        WebForm webform = DesignerUtils.getWebForm(dobj);
    public static Image getCssPreviewImage(/*DataObject dataObject,*/ Designer designer, Graphics2D g2d,
    String cssStyle, String[] cssStyleClasses,
    /*MarkupDesignBean bean,*/ Element componentRootElement, DocumentFragment df, Element element,
    int width, int height) {
//        WebForm webform = WebForm.getWebFormForDataObject(dataObject);
//
//        if (webform == null) {
//            return null;
//        }
        if (!(designer instanceof WebForm)) {
            return null;
        }
        
        WebForm webform = (WebForm)designer;

        // Phew! On to the preview painting.
        PageBox pageBox = PageBox.getPageBox(null, webform, webform.getHtmlBody());
//        WindowManager wm = WindowManager.getDefault();
//        Graphics2D g2d = (Graphics2D)wm.getMainWindow().getGraphics();

        return pageBox.paintCssPreview(g2d, cssStyle,
                /*bean,*/ componentRootElement, df, element,
                width, height);
    }

    public static Image getCssPreviewImage(Map<String, String> properties, URL base,
    int width, int height, int defaultFontSize) {
        WindowManager wm = WindowManager.getDefault();
        Graphics2D g2d = (Graphics2D)wm.getMainWindow().getGraphics();

        return paintCssPreview(g2d, base, properties, width, height, defaultFontSize);
    }

    /** Computes a preview image of the specified size for given <code>DataObject</code>.
     * @return the image or <code>null</code> if the specified DataObject is not a webform one. */
//    public static Image getPageBoxPreviewImage(DataObject dobj, int width, int height) {
//        // Copied from navigation/../PageFlowGraph to get rid of dependencies.
//        // Obtain a page box for the given page
//        WebForm webform = DesignerUtils.getWebForm(dobj);
    public static Image getPageBoxPreviewImage(/*DataObject dobj,*/ Designer designer, int width, int height) {
//        WebForm webform = WebForm.getWebFormForDataObject(dobj);
//
//        if (webform == null) {
//            return null;
//        }
        if (!(designer instanceof WebForm)) {
            return null;
        }
        
        WebForm webform = (WebForm)designer;

        // XXX Moved to designer/jsf/../DesignerHackImpl.
////        webform.getModel().sync();
//        webform.syncModel();
//
////        if (webform.getModel().isBusted()) {
//        if (webform.isModelBusted()) {
//            return null;
//        }

        Element body = webform.getHtmlBody();

        if (body == null) {
            return null;
        }

        PageBox pageBox = PageBox.getPageBox(null, webform, body);

        return pageBox.createPreviewImage(width, height);
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

//    public static boolean canDrop(DataFlavor flavor) {
//        // Fish for the designer pane
//        DesignerTopComp dtc = findCurrent();
//
//        if (dtc == null) {
//            return false;
//        }
//
//        // TODO -- additional flavor checking?
//        return true;
//    }

//    public static void drop(Transferable transferable) {
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
//    }

//    public static void registerTransferable(Transferable transferable) {
//        if(DesignerUtils.DEBUG) {
//            DesignerUtils.debugLog(DesignerServiceHackProviderImpl.class.getName() + ".registerTransferable(Transferable)");
//        }
//        if(transferable == null) {
//            throw(new IllegalArgumentException("Null transferable."));
//        }
////        DndHandler.setActiveTransferable(transferable);
//    }

//    private static TopComponent getCurrentDesigner() {
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

//    public static FileObject getCurrentFile() {
//        DesignerTopComp tc = (DesignerTopComp)getCurrentDesigner();
//
////        if ((tc == null) || (tc.getWebForm().getMarkup() == null)) {
//        if (tc == null) {
//            return null;
//        }
//
////        return tc.getWebForm().getMarkup().getFileObject();
//        DataObject jspDataObject = tc.getWebForm().getJspDataObject();
//        return jspDataObject == null ? null : jspDataObject.getPrimaryFile();
//    }

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
    
    /** XXX Fake document, to be able to create engine.
     * TODO Better is just to impl the <code>Document</code> interface, without dep on xerces. */
    private static class FakeDocument extends DocumentImpl {
    } // End of FakeDocument.

//    public static Object getTableInfo(MarkupDesignBean bean) {
    public static Object getTableInfo(Element componentRootElement) {
//        assert bean.getElement() != null;

//        Element element = bean.getElement();
//        if (element == null) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                    new NullPointerException("There is no element in markup design bean=" + bean)); // NOI18N
//            return null;
//        }
        
        WebForm webForm = WebForm.findWebFormForElement(componentRootElement);
        if (webForm == null) {
            return null;
        }
        
//        CssBox box = CssBox.getBox(bean.getElement());
        CssBox box = webForm.findCssBoxForElement(componentRootElement);

        if (box instanceof TableBox) {
            return box;
        }

        return null;
    }

    public static org.w3c.dom.Element getCellElement(Object tableInfo, int row, int column) {
        assert tableInfo instanceof TableBox;

        TableBox table = (TableBox)tableInfo;
        CssBox box = table.getCell(row, column);

        if (box == null) {
            return null;
        }

        return box.getElement();
    }

//    public static MarkupDesignBean getCellBean(Object tableInfo, int row, int column) {
    public static Element getCellComponent(Object tableInfo, int row, int column) {
        assert tableInfo instanceof TableBox;

        TableBox table = (TableBox)tableInfo;
        CssBox box = table.getCell(row, column);

        if (box == null) {
            return null;
        }

//        return box.getDesignBean();
//        return CssBox.getMarkupDesignBeanForCssBox(box);
        return CssBox.getElementForComponentRootCssBox(box);
    }

    public static int getColSpan(Object tableInfo, int row, int column) {
        assert tableInfo instanceof TableBox;

        TableBox table = (TableBox)tableInfo;

        return table.getCellSpan(CssBox.Y_AXIS, row, column);
    }

    public static int getRowSpan(Object tableInfo, int row, int column) {
        assert tableInfo instanceof TableBox;

        TableBox table = (TableBox)tableInfo;

        return table.getCellSpan(CssBox.X_AXIS, row, column);
    }

    public static int getColumnCount(Object tableInfo) {
        assert tableInfo instanceof TableBox;

        TableBox table = (TableBox)tableInfo;

        return table.getColumns();
    }

    public static int getRowCount(Object tableInfo) {
        assert tableInfo instanceof TableBox;

        TableBox table = (TableBox)tableInfo;

        return table.getRows();
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
//                    FileObject webroot = JsfProjectUtils.getDocumentRoot(project);
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
//        FileObject fobj = JsfProjectUtils.getDocumentRoot(project);
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

//    public static void notifyCssEdited(DataObject dobj) {
//        DesignerTopComp.setPendingRefreshAll();
//    }

//    public static void refresh(Project project, DataObject dobj, boolean deep) {
//        if (dobj != null) {
//            WebForm.refresh(dobj, deep);
//        } else {
//            WebForm.refreshAll(project, deep);
//        }
//    }
//    public static void refreshDataObject(DataObject dobj, boolean deep) {
//        WebForm.refresh(dobj, deep);
//    }
//    public static void refreshProject(Project project, boolean deep) {
//        WebForm.refreshAll(project, deep);
//    }

//    public static void destroyWebFormForFileObject(FileObject fo) {
//        WebForm webform = WebForm.findWebFormForFileObject(fo);
//        if (webform != null) {
//            webform.destroy();
//            WebForm.removeWebFormForFileObject(fo);
//        }
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

    
    public static void copyBoxForElement(Element fromElement, Element toElement) {
        WebForm[] webForms = WebForm.findAllWebFormsForElement(toElement);
        for (WebForm webForm : webForms) {
//            CssBox.copyBoxForElement(fromElement, toElement);
            webForm.copyBoxForElement(fromElement, toElement);
        }
    }

// <missing designtime api>

// <separation of models>
//    public static FileObject getContextFileForFragmentFile(FileObject fragmentFile) {
//        WebForm webform = WebForm.findWebFormForFileObject(fragmentFile);
//        WebForm contextWebform;
//        if (webform == null) {
//            contextWebform = null;
//        } else {
//            contextWebform = webform.getContextPage();
//        }
//        
////        return contextWebform == null ? null : contextWebform.getModel().getMarkupFile();
//        if (contextWebform == null) {
//            return null;
//        }
//        DataObject jspDataObject = contextWebform.getJspDataObject();
//        return jspDataObject == null ? null : jspDataObject.getPrimaryFile();
//    }
  
    // XXX Moved to designer/jsf/../DesignerServiceHackImpl.
//    public static FileObject getExternalFormFileForElement(Element element) {
//        WebForm webForm = WebForm.findWebFormForElement(element);
//        if (webForm == null) {
//            return null;
//        }
////        CssBox includeBox = CssBox.getBox(element);
//        CssBox includeBox = webForm.findCssBoxForElement(element);
//
//        if ((includeBox != null) && includeBox instanceof JspIncludeBox) {
//            WebForm frameForm = ((JspIncludeBox)includeBox).getExternalForm();
//
////            if ((frameForm != null) && (frameForm != WebForm.EXTERNAL)) {
//            if (frameForm != null) {
////                return frameForm.getModel().getMarkupFile();
//                DataObject jspDataObject = frameForm.getJspDataObject();
//                return jspDataObject == null ? null : jspDataObject.getPrimaryFile();
//            }
//        }
//        return null;
//    }
    
// </separation of models>
    
// </missing designtime api>
    
    // Copied from TextBox.
    private static final int TEXT_UNDERLINE = 1;
    private static final int TEXT_STRIKE    = 2;
    private static final int TEXT_OVERLINE  = 4;
    /**
     * XXX Horrible method, too long, needs to be refactored, it is unreadable now.
     * Paint a preview of the given component, with the given CSS style
     * applied, and return it as an image. Use the preferred initial
     * width, unless the component is larger.
     */
    private static BufferedImage paintCssPreview(Graphics2D g2d, URL base, Map<String, String> properties,
    int width, int height, int defaultFontSize) {
        // Restore?
        BufferedImage image = null;

        if (g2d != null) {
            GraphicsConfiguration config = g2d.getDeviceConfiguration();
            image = config.createCompatibleImage(width, height);
        } else {
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }

        if (image == null) {
            return null;
        }

        Graphics2D og = (Graphics2D)image.getGraphics();

        try {
//            RaveDocument doc = null;
            // <markup_separation>
//            XhtmlCssEngine engine = XhtmlCssEngine.create(doc, null, base);
            // ====
//            XhtmlCssEngine engine = XhtmlCssEngine.create(null, base);
//// <moved from engine impl> it doesn't (shoudn't) know about RaveDocument.
////            if (doc != null) {
////                doc.setCssEngine(engine);
////            }
//// </moved from engine impl>
            Document fakeDocument = new FakeDocument();
            CssEngineService cssEngineService = CssProvider.getEngineService();
            cssEngineService.createCssEngineForDocument(fakeDocument, null);
//            XhtmlCssEngine engine = CssEngineServiceProvider.getDefault().getCssEngine(fakeDocument);
            
            // </markup_separation>
//            engine.setErrorHandler(XhtmlCssEngine.SILENT_ERROR_HANDLER);
            cssEngineService.setSilentErrorHandlerForDocument(fakeDocument);

//            String styles = engine.mapToStyle(properties);
            String styles = cssEngineService.getStringFromStyleMapForDocument(fakeDocument, properties);
//            PreviewElement element = new PreviewElement(fakeDocument, /*engine,*/ base, styles);
            Element element = cssEngineService.createPreviewElementForDocument(fakeDocument, base, styles);

//            Color bg = CssLookup.getColor(element, XhtmlCss.BACKGROUND_COLOR_INDEX);
            Color bg = CssProvider.getValueService().getColorForElement(element, XhtmlCss.BACKGROUND_COLOR_INDEX);

            if (bg != null) {
                og.setColor(bg);
                og.fillRect(0, 0, width, height);
            } else {
                // Use a transparent color.... any will do!
                //		Color curr = g2d.getColor();
                //		og.setColor(new Color(curr.getRed(), curr.getGreen(), curr.getBlue(), 0));
                //og.setColor(new Color(0, 0, 0, 0));
                bg = (Color)UIManager.getDefaults().get("Label.background"); // NOI18N
                og.setColor(bg);
                og.fillRect(0, 0, width, height);
            }

            //            ImageIcon bgImage = BackgroundImagePainter.getBackgroundImage(doc, element);
//            ImageIcon bgImage = BackgroundImagePainter.getBackgroundImage(base, element);
//            URL imageUrl = CssBoxUtilities.getBackgroundImageUrl(element, base);
            URL imageUrl = CssProvider.getEngineService().getBackgroundImageUrlForElement(element, base);
            ImageIcon bgImage = imageUrl == null ? null : new ImageIcon(imageUrl);

            if (bgImage != null) {
//                Value repeatValue = CssLookup.getValue(element, XhtmlCss.BACKGROUND_REPEAT_INDEX);
                CssValue cssRepeatValue = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.BACKGROUND_REPEAT_INDEX);
//                ListValue positionValue =
//                    CssLookup.getListValue(CssLookup.getValue(element,
//                            XhtmlCss.BACKGROUND_POSITION_INDEX));
                CssListValue cssPositionValue = CssProvider.getValueService().getComputedCssListValue(
                        CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.BACKGROUND_POSITION_INDEX));
//                BackgroundImagePainter bgPainter =
//                    new BackgroundImagePainter(bgImage, repeatValue, positionValue);
                BackgroundImagePainter bgPainter = new BackgroundImagePainter(bgImage, cssRepeatValue, cssPositionValue, element, defaultFontSize);

                if (bgPainter != null) {
                    bgPainter.paint(og, 0, 0, width, height);
                }
            }

            boolean hasText = false;
            boolean hasBorder = false;
            boolean hasPosition = false;
            Iterator<String> it = properties.keySet().iterator();

            while (it.hasNext()) {
                String property = it.next();

//                if (isPositionProperty(property)) {
                if (CssProvider.getValueService().isPositionProperty(property)) {
                    hasPosition = true;
                }

//                if (isTextProperty(property)) {
                if (CssProvider.getValueService().isTextProperty(property)) {
                    // Insert text
                    hasText = true;
                }

//                if (isBorderProperty(property)) {
                if (property.startsWith("border-")) { // NOI18N
                    hasBorder = true;
                }
            }

//            if (hasPosition) {
//                // Do some position painting (abbreviated)
//            }

            CssBorder border = null;

            if (hasBorder) {
                // Paint border
                // XXX If you just set ONE property (like color) but
                // not solid or anything else, we don't preview! That's not good...
                border = CssBorder.getBorder(element);

                if (border != null) {
                    border.paintBorder(og, 0, 0, width, height);
                }
            }

            if (hasText) {
                // Paint text
                // Check font size and attributes
                int decoration = 0;
                Color fg = Color.black;
                FontMetrics metrics = null;
                //boolean collapseSpaces = true;
                //boolean hidden = false;
//                metrics = CssLookup.getFontMetrics(element);
//                metrics = CssProvider.getValueService().getFontMetricsForElement(element);
                // XXX Missing text.
                metrics = CssUtilities.getDesignerFontMetricsForElement(element, null, defaultFontSize);
                
//                fg = CssLookup.getColor(element, XhtmlCss.COLOR_INDEX);
                fg = CssProvider.getValueService().getColorForElement(element, XhtmlCss.COLOR_INDEX);

                if (fg == null) {
                    if (fg == null) {
                        fg = Color.black;
                    }
                }

//                Value val = CssLookup.getValue(element, XhtmlCss.TEXT_DECORATION_INDEX);
                CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.TEXT_DECORATION_INDEX);

//                switch (val.getCssValueType()) {
//                case CSSValue.CSS_VALUE_LIST:
//
//                    ListValue lst = CssLookup.getListValue(val);
//                
//                    if (lst == null) {
//                        break;
//                    }
                CssListValue cssList = CssProvider.getValueService().getComputedCssListValue(cssValue);
                if (cssList != null) {

//                    int len = lst.getLength();
                    int len = cssList.getLength();

                    for (int i = 0; i < len; i++) {
//                        Value v = lst.item(i);
                        CssValue cssV = cssList.item(i);
//                        String s = v.getStringValue();
                        String s = cssV.getStringValue();

                        switch (s.charAt(0)) {
                        case 'u':
                            decoration |= TEXT_UNDERLINE;

                            break;

                        case 'o':
                            decoration |= TEXT_OVERLINE;

                            break;

                        case 'l':
                            decoration |= TEXT_STRIKE;

                            break;
                        }
                    }

//                    break;
//                default:
                } else {
                    // XXX what happened?
                }

                // XXX Technically, should check for decoration=="overline" too...
                // (See section 16.3.1). However, does that have ANY practical
                // utility?
//                val = CssLookup.getValue(element, XhtmlCss.WHITE_SPACE_INDEX);
//
//                if ((val == CssValueConstants.PRE_VALUE) ||
//                        (val == CssValueConstants.PRE_WRAP_VALUE)) {
//                    collapseSpaces = false;
//                }

                String content = "ABCabc123";
//                Value v1 = CssLookup.getValue(element, XhtmlCss.FONT_VARIANT_INDEX);
//                Value v2 = CssLookup.getValue(element, XhtmlCss.TEXT_TRANSFORM_INDEX);
                CssValue cssV1 = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.FONT_VARIANT_INDEX);
                CssValue cssV2 = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.TEXT_TRANSFORM_INDEX);

//                if ((v1 == CssValueConstants.SMALL_CAPS_VALUE) ||
//                        (v2 == CssValueConstants.UPPERCASE_VALUE)) {
                if (CssProvider.getValueService().isSmallCapsValue(cssV1)
                || CssProvider.getValueService().isUpperCaseValue(cssV2)) {
                    // Uppercase the text
                    content = content.toUpperCase();

                    // TODO (much later): split the text up like under capitalization
                    // and apply different fonts to the initial letters
                    // and the rest of the words. I can't trivially do that
                    // here because I would create separate TextBoxes for the
                    // initial character and the rest of the words, and this
                    // COULD be split up both in text justification and in word
                    // wrapping by the LineBox and LineBoxGroup containers, which
                    // would be visually disasterous. I think the painting of
                    // this would really have to be done in the TextBox itself.
//                } else if (v2 == CssValueConstants.LOWERCASE_VALUE) {
                } else if (CssProvider.getValueService().isLowerCaseValue(cssV2)) {
                    content = content.toLowerCase();
//                } else if (v2 == CssValueConstants.CAPITALIZE_VALUE) {
                } else if (CssProvider.getValueService().isCapitalizeValue(cssV2)) {
                    content = "Abcabc123";
                }

//                int leftMargin = CssLookup.getLength(element, XhtmlCss.MARGIN_LEFT_INDEX);
                int leftMargin = CssUtilities.getCssLength(element, XhtmlCss.MARGIN_LEFT_INDEX);
//                int rightMargin = CssLookup.getLength(element, XhtmlCss.MARGIN_RIGHT_INDEX);
//                int topMargin = CssLookup.getLength(element, XhtmlCss.MARGIN_TOP_INDEX);
                int topMargin = CssUtilities.getCssLength(element, XhtmlCss.MARGIN_TOP_INDEX);
//                int bottomMargin = CssLookup.getLength(element, XhtmlCss.MARGIN_BOTTOM_INDEX);

//                int leftPadding = CssLookup.getLength(element, XhtmlCss.PADDING_LEFT_INDEX);
//                int rightPadding = CssLookup.getLength(element, XhtmlCss.PADDING_RIGHT_INDEX);
                int leftPadding = CssUtilities.getCssLength(element, XhtmlCss.PADDING_LEFT_INDEX);
                int rightPadding = CssUtilities.getCssLength(element, XhtmlCss.PADDING_RIGHT_INDEX);

                // Unlike margins, padding values are not allowed to be negative!
                if (leftPadding < 0) {
                    leftPadding = 0;
                }

                if (rightPadding < 0) {
                    rightPadding = 0;
                }

//                int topPadding = CssLookup.getLength(element, XhtmlCss.PADDING_TOP_INDEX);
//                int bottomPadding = CssLookup.getLength(element, XhtmlCss.PADDING_BOTTOM_INDEX);
                int topPadding = CssUtilities.getCssLength(element, XhtmlCss.PADDING_TOP_INDEX);
                int bottomPadding = CssUtilities.getCssLength(element, XhtmlCss.PADDING_BOTTOM_INDEX);

                if (topPadding < 0) {
                    topPadding = 0;
                }

                if (bottomPadding < 0) {
                    bottomPadding = 0;
                }

                int leftBorderWidth = 0;
                int topBorderWidth = 0;
//                int rightBorderWidth = 0;
//                int bottomBorderWidth = 0;

                if (border != null) {
                    leftBorderWidth = border.getLeftBorderWidth();
                    topBorderWidth = border.getTopBorderWidth();
//                    bottomBorderWidth = border.getBottomBorderWidth();
//                    rightBorderWidth = border.getRightBorderWidth();
                }

                int x = leftMargin + leftBorderWidth + leftPadding;
                int y = topMargin + topBorderWidth + topPadding;
                og.setColor(fg);
                og.setFont(metrics.getFont());

                // determine the y coordinate to render the glyphs
                int yadj = (y + metrics.getHeight()) - metrics.getDescent();

                // Draw text!
                char[] contentChars = content.toCharArray();
                og.drawChars(contentChars, 0, contentChars.length, x, yadj);

                // render underline or strikethrough if set.
                if (decoration != 0) {
                    int textWidth =
                        DesignerUtils.getNonTabbedTextWidth(contentChars, 0, contentChars.length,
                            metrics);

                    if ((decoration & TEXT_UNDERLINE) != 0) {
                        int yTmp = yadj;
                        yTmp += 1;
                        og.drawLine(x, yTmp, x + textWidth, yTmp);
                    }

                    if ((decoration & TEXT_STRIKE) != 0) {
                        int yTmp = yadj;

                        // move y coordinate above baseline
                        yTmp -= (int)(metrics.getAscent() * 0.4f);
                        og.drawLine(x, yTmp, x + textWidth, yTmp);
                    }

                    if ((decoration & TEXT_OVERLINE) != 0) {
                        og.drawLine(x, y, x + textWidth, y);
                    }
                }
            }
        } finally {
            og.dispose();
        }

        return image;
    }

//    public static MultiViewElement getDesignerMultiViewElement(DataObject dataObject) {
//        return WebForm.getDesignerMultiViewElement(dataObject);
//    }
    
}

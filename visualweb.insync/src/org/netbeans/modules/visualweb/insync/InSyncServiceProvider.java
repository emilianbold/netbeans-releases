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
package org.netbeans.modules.visualweb.insync;


import org.netbeans.modules.visualweb.api.designer.markup.MarkupService;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignEvent;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.markup.MarkupMouseRegion;

import org.netbeans.modules.visualweb.insync.beans.BeansUnit;
import org.netbeans.modules.visualweb.insync.faces.Entities;
import org.netbeans.modules.visualweb.insync.faces.FacesPageUnit;
import org.netbeans.modules.visualweb.insync.live.LiveUnit;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;

import org.netbeans.modules.visualweb.api.insync.InSyncService;
import org.netbeans.modules.visualweb.api.insync.JsfJspDataObjectMarker;
import com.sun.rave.designtime.DesignProject;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import org.netbeans.modules.visualweb.insync.faces.ElAttrUpdater;
//NB6.0 import org.netbeans.modules.visualweb.insync.faces.refactoring.MdrInSyncSynchronizer;
import org.netbeans.modules.visualweb.insync.jsf.SourceMonitor;
import org.netbeans.modules.visualweb.insync.markup.MarkupUnit;
import org.netbeans.modules.visualweb.insync.markup.MarkupVisitor;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.insync.models.FacesModelSet;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.openide.text.Line;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * This class provides a concrete implementation of InSyncService.
 * As well, it provides some functionality to handle the mapping of Java <-> JSP files.
 * This code should really exist in a Faces module to InSync, but these services will
 * be needed once I start removing any knowledge of this mapping from all other modules.
 *
 * The key would be to creating a Faces InSync module, and have a FacesInSyncService
 * that modules would then depend on.  Otherwise we cannot break out the Faces specific
 * functionality out of InSync's core.
 *
 * TODO
 * @author eric
 *
 */
public class InSyncServiceProvider extends InSyncService {

    protected static InSyncServiceProvider singleton;

    public static InSyncServiceProvider get() {
        if (singleton == null) {
            InSyncService iService = InSyncService.getProvider();
            if (iService instanceof InSyncServiceProvider)
                singleton = (InSyncServiceProvider) iService;
            else
                singleton = new InSyncServiceProvider();
        }
        return singleton;
    }

    public InSyncServiceProvider() {
        super();
    }

    /**
     * This is a hack until we get things done correctly with integration of Retouche.
     * Should only be called from JsfJspDataObject to notify us that toDataObject was created by copying fromDataObject.
     * Go through and rename the EL expression references to the original page name to the new page name.
     *
     * This is NOT the way to fix the problem of copy, however there are too many other issues that get in the way
     * that prevent us from creating a simple fix that will yield the same results.  Since ALL of this code will change
     * when we properly integrate insync into platform, this is throw away code.
     * 
     *  TODO: Remove
     *  
     * @param fromDataObject
     * @param toDataObject
     */
    public void copied(JsfJspDataObjectMarker fromDataObject, JsfJspDataObjectMarker toDataObject) {
        InSyncServiceProviderQuery query = new InSyncServiceProviderQuery(fromDataObject.getPrimaryFile(), (DataObject) fromDataObject);
        String oldName = query.getBeanNameViaJsp();
        query = new InSyncServiceProviderQuery(toDataObject.getPrimaryFile(), (DataObject) toDataObject);
        String newName = query.getBeanNameViaJsp();
        if (oldName == null || newName == null)
            return;
        MarkupUnit markupUnit = new MarkupUnit(toDataObject.getPrimaryFile(), MarkupUnit.ALLOW_XML, true, new UndoManager());
        markupUnit.sync();
        if (markupUnit.isBusted())
            return;
        MarkupVisitor v = new ElAttrUpdater(oldName, newName);
        v.apply(markupUnit.getSourceDom());
        markupUnit.flush();
    }
    
    public void doOutsideOfRefactoringSession(Runnable runnable) {
        //NB6.0 MdrInSyncSynchronizer.get().doOutsideOfRefactoringSession(runnable);
    }
    
    public String getBeanNameForJsp(FileObject fileObject) {
        String result = FacesModel.getBeanNameForJsp(fileObject);
        return result;
    }
    
    public DataObject getJavaDataObjectEquivalent(DataObject dataObject, String originalName, boolean forceCreate) {
        InSyncServiceProviderQuery query = newQuery(null, dataObject);
        return query.getJavaDataObjectEquivalent(originalName, forceCreate);
    }

    public DataObject getJavaDataObjectEquivalent(FileObject fileObject, String originalName, boolean forceCreate) {
        InSyncServiceProviderQuery query = newQuery(fileObject, null);
        return query.getJavaDataObjectEquivalent(originalName, forceCreate);
    }

    public DataObject getJavaDataObjectEquivalent(Node node, String originalName, boolean forceCreate) {
        DataObject dataObject = (DataObject) node.getCookie(DataObject.class);
        return getJavaDataObjectEquivalent(dataObject, originalName, forceCreate);
    }

    public FileObject getJavaFileObjectEquivalent(FileObject fileObject, String originalName, boolean forceCreate) {
        InSyncServiceProviderQuery query = newQuery(fileObject, null);
        return query.getJavaFileObjectEquivalent(originalName, forceCreate);
    }
    
    /**
     * Gets corresponding java file object for specified jsp file object if exists.
     * 
     * @return corresponding java file object or <code>null</code>
     */
    public FileObject getJavaForJsp(FileObject jspFileObject) {
        InSyncServiceProviderQuery query = newQuery(jspFileObject, null);
        return query.getJavaForJsp();
    }

    public FileObject getJavaFolderForJsp(FileObject fileObject) {
        InSyncServiceProviderQuery query = newQuery(fileObject, null);
        return query.getJavaFolderForJsp();
    }

    protected InSyncServiceProviderQuery newQuery(FileObject fileObject, DataObject dataObject) {
        return new InSyncServiceProviderQuery(fileObject, dataObject);
    }

    // XXX The three methods dealing with triggering of parsing of jsp.
    public void jspDataObjectTopComponentActivated(DataObject dobj) {
        if (dobj == null) {
            return;
        }

        FacesModel facesModel = FacesModelSet.getFacesModelIfAvailable(dobj.getPrimaryFile());

        if (facesModel != null) {
            SourceMonitor sm = SourceMonitor.getSourceMonitorForFacesModel(facesModel);
            sm.activated();
        }
    }

    public void jspDataObjectTopComponentHidden(DataObject dobj) {
        if (dobj == null) {
            return;
        }

        FacesModel facesModel = FacesModelSet.getFacesModelIfAvailable(dobj.getPrimaryFile());

        if (facesModel != null) {
            SourceMonitor sm = SourceMonitor.getSourceMonitorForFacesModel(facesModel);
            sm.hidden();
        }
    }

    public void jspDataObjectTopComponentShown(DataObject dobj) {
        if (dobj == null) {
            return;
        }

        FacesModel facesModel = FacesModelSet.getFacesModelIfAvailable(dobj.getPrimaryFile());

        if (facesModel != null) {
            SourceMonitor sm = SourceMonitor.getSourceMonitorForFacesModel(facesModel);
            sm.shown();
        }
    }

// <missing_designtime_api>
    
    // <markup_separation> XXX See the InSyncService
    public void appendParsedString(Document doc, org.w3c.dom.Node node, String xhtml, MarkupDesignBean bean) {
        MarkupUnit unit = MarkupUnit.getMarkupUnitForDocument(doc);
        if (unit == null) {
            return;
        }
        unit.appendParsedString(node, xhtml, bean);
    }
    
    public FileObject getFileObject(Document doc) {
        return Util.getFileObject(doc);
    }
    
    public int computeLine(Document doc, Element element) {
//        MarkupUnit unit = MarkupUnit.getMarkupUnitForDocument(doc);
//        if (unit == null) {
//            return 0;
//        }
//        return unit.computeLine(element);
        return Util.computeLine(doc, element);
    }
    
    public URL getDocumentUrl(Document doc) {
////        if (!(doc instanceof RaveDocument)) {
////            return null;
////        }
//        if (doc == null) {
//            return null;
//        }
//        
////        RaveDocument rDoc = (RaveDocument)doc;
//// <removing set/getRoot from RaveDocument>
////        DesignProject designProject = rDoc.getRoot().getDesignBean().getDesignContext().getProject();
//        MarkupUnit unit = MarkupUnit.getMarkupUnitForDocument(doc);
//        DesignProject designProject;
//        if (unit != null) {
//            FacesModel facesModel = FacesModel.getInstance(unit.getFileObject());
//            designProject = ((DesignContext)facesModel.getLiveUnit()).getProject();
//        } else {
//            designProject = null;
//        }
//// <removing set/getRoot from RaveDocument>
//        if(designProject instanceof FacesModelSet) {
//            FacesModelSet fModelSet = (FacesModelSet)designProject;
//            FileObject documentRoot = JsfProjectUtils.getDocumentRoot(fModelSet.getProject());
//            try {
//                return documentRoot.getURL();
//            } catch(FileStateInvalidException fsie) {
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, fsie);
//            }
//        }
//        
//        return null;
        return Util.getDocumentUrl(doc);
    }
    // </markup_separation>

    public void setUrl(Document doc, URL url) {
        MarkupUnit.setUrlForDocument(doc, url);
    }
    
    public URL getUrl(Document doc) {
        return MarkupUnit.getUrlForDocument(doc);
    }

    // JSF rendering.
    public DocumentFragment renderHtml(FileObject markupFile, MarkupDesignBean bean) {
        FacesModel facesModel = FacesModel.getInstance(markupFile);
        
        if (facesModel == null) {
            return null;
        } else {
            return FacesPageUnit.renderHtml(facesModel, bean);
        }
    }
    
    public Exception getRenderFailure(FileObject markupFile) {
        FacesModel facesModel = FacesModel.getInstance(markupFile);
        if (facesModel == null) {
            return null;
        } else {
            FacesPageUnit facesPageUnit = facesModel.getFacesUnit();
            return facesPageUnit.getRenderFailure();
        }
    }
    
    public DesignBean getRenderFailureComponent(FileObject markupFile) {
        FacesModel facesModel = FacesModel.getInstance(markupFile);
        if (facesModel == null) {
            return null;
        } else {
            FacesPageUnit facesPageUnit = facesModel.getFacesUnit();
            return facesPageUnit.getRenderFailureComponent();
        }
    }
// </missing_designtime_api>

// <separation of models>
    public Document getJspDomForMarkupFile(FileObject markupFile) {
        FacesModel facesModel = FacesModel.getInstance(markupFile);
        if (facesModel == null) {
            return null;
        } else {
            return facesModel.getJspDom();
        }
    }
    
    public Document getHtmlDomForMarkupFile(FileObject markupFile) {
        FacesModel facesModel = FacesModel.getInstance(markupFile);
        if (facesModel == null) {
            return null;
        } else {
            return facesModel.getHtmlDom();
        }
    }
    
//    public DocumentFragment getHtmlDomFragmentForMarkupFile(FileObject markupFile) {
//        FacesModel facesModel = FacesModel.getInstance(markupFile);
//        if (facesModel == null) {
//            return null;
//        } else {
//            return facesModel.getHtmlDomFragment();
//        }
//    }
    
    public DocumentFragment getHtmlDomFragmentForDocument(Document document) {
//        FileObject markupFile = getFileObject(document);
//        if (markupFile == null) {
//            return null;
//        }
//        
//        FacesModel facesModel = FacesModel.getInstance(markupFile);
//        if (facesModel == null) {
//            return null;
//        } else {
//            return facesModel.getHtmlDomFragment();
//        }
        return Util.getHtmlDomFragmentForDocument(document);
    }
    
    public Element getHtmlBodyForMarkupFile(FileObject markupFile) {
//        FacesModel facesModel = FacesModel.getInstance(markupFile);
//        if (facesModel == null) {
//            return null;
//        } else {
//            return facesModel.getHtmlBody();
//        }
        return Util.getHtmlBodyForMarkupFile(markupFile);
    }
    
    // XXX Refresh antipatern.
    public void clearHtmlForMarkupFile(FileObject markupFile) {
        FacesModel facesModel = FacesModel.getInstance(markupFile);
        if (facesModel != null) {
            facesModel.clearHtml();
        }
    }
    
    public boolean isBraveheartPage(Document dom) {
        return Util.isBraveheartPage(dom);
    }
    
    public boolean isWoodstockPage(Document dom) {
        return Util.isWoodstockPage(dom);
    }
    
// </separation of models>

// <service_methods>
    public String fromURL(String url) {
//        if (url.startsWith("file:")) { // NOI18N
//            int n = url.length();
//            StringBuffer sb = new StringBuffer(n);
//            for (int i = 5; i < n; i++) {
//                char c = url.charAt(i);
//                // TODO -- any File.separatorChar manipulation perhaps?
//                if (c == '%' && i < n-3) {
//                    char d1 = url.charAt(i+1);
//                    char d2 = url.charAt(i+2);
//                    if (Character.isDigit(d1) && Character.isDigit(d2)) {
//                        String numString = ""+d1+d2;
//                        try {
//                            int num = Integer.parseInt(numString, 16);
//                            if (num >= 0 && num <= 255) {
//                                sb.append((char)num);
//                                i += 2;
//                                continue;
//                            }
//                        } catch (NumberFormatException nex) {
//                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, nex);
//                        }
//                    }
//                    sb.append(c);
//                } else {
//                    sb.append(c);
//                }
//            }
//            return sb.toString();
//        }
//        return url;
        return Util.fromURL(url);
    }
    
//    // XXX From org.netbeans.modules.visualweb.insync.Util.
//    /**
//     * Given an element which may be in a rendered DocumentFragment, return the corresponding JSF
//     * element in the source.
//     */
//    public Element getCorrespondingSourceElement(Element elem) {
//        if (!(elem instanceof RaveElement)) {
//            return elem;
//        }
//        
//        RaveElement element = (RaveElement)elem;
//        
//        if (!element.isRendered()) {
//            return element;
//        }
//        
//        org.w3c.dom.Node node = element;
//        while (node != null) {
//            if (node instanceof RaveElement) {
//                RaveElement xel = (RaveElement)node;
//                if (xel.isRendered()) {
//                    RaveElement src = xel.getSource();
//                    if (src != null) {
//                        return src;
//                    }
//                }
//            }
//            node = node.getParentNode();
//        }
//        
////        return element.getSourceElement();
//        return element.getSource();
//    }
    
    public String computeFileName(Object location) {
//        if (location instanceof String) {
//            return (String)location;
//        } else if (location instanceof URL) {
//            // <markup_separation>
////            return MarkupUnit.fromURL(((URL)location).toExternalForm());
//            // ====
//            return InSyncServiceProvider.get().fromURL(((URL)location).toExternalForm());
//            // </markup_separation>
//        } else if (location instanceof Element) {
//            // Locate the filename for a given element
//            Element element = (Element)location;
//            element = MarkupService.getCorrespondingSourceElement(element);
//
//            // <markup_separation>
////            // XXX I should derive this from the engine instead, after all
////            // the engine can know the unit! (Since engines cannot be used
////            // with multiple DOMs anyway)
////            FileObject fo = unit.getFileObject();
//            // ====
//            FileObject fo = getFileObject(element.getOwnerDocument());
//            // </markup_separation>
//            File f = FileUtil.toFile(fo);
//
//            return f.toString();
//        } else if (location != null) {
//            return location.toString();
//        }
//
//        return "";
        return Util.computeFileName(location);
    }

    public int computeLineNumber(Object location, int lineno) {
//        if (location instanceof Element) {
//            /*
//            // The location is an XhtmlElement -- so the line number
//            // needs to be relative to it.... compute the line number
//            // of the element
//            if (lineno == -1)
//                lineno = 0;
//            Element element = (Element)location;
//            RaveDocument doc = (RaveDocument)element.getOwnerDocument();
//            lineno += doc.getLineNumber(element);
//             */
//            if (lineno == -1) {
//                lineno = 0;
//            }
//
//            Element element = (Element)location;
//            element = MarkupService.getCorrespondingSourceElement(element);
//            // <markup_separation>
////            lineno += unit.computeLine(element);
//            // ====
//            lineno += InSyncServiceProvider.get().computeLine(element.getOwnerDocument(), element);
//            // </markup_separation>
//        }
//
//        return lineno;
        return Util.computeLineNumber(location, lineno);
    }
// </service_methods>

    
// <error_handling> Moved from RaveDocument.
    private final RaveErrorHandler raveErrorHandler = new RaveErrorHandlerImpl();
    
    public InSyncService.RaveErrorHandler getRaveErrorHandler() {
        return raveErrorHandler;
    }

    /** Class implementing the Rave IDE specific error handling. */
    private static class RaveErrorHandlerImpl implements InSyncService.RaveErrorHandler {
//        private boolean clearErrors = false;
        
        public void clearErrors(boolean delayed) {
//            if (delayed) {
//                clearErrors = true;
//            } else {
//                OutputWriter out = getOutputWriter();
//                try {
//                    out.reset();
//                }
//                catch (IOException ioe) {
//                    // This is lame - our own output window shouldn't
//                    // throw IO exceptions!
//                    ErrorManager.getDefault().notify(ioe);
//                }
//            }
            Util.clearErrors(delayed);
        }

        public void selectErrors() {
//            InputOutput io = getInputOutput();
//            io.select();
            Util.selectErrors();
        }

        public void displayError(String message) {
            displayErrorForFileObject(message, null, -1, -1);
        }

        public void displayErrorForLocation(String message, Object location, int line, int column) {
//            String fileName = InSyncServiceProvider.get().computeFileName(location);
//            line = InSyncServiceProvider.get().computeLineNumber(location, line);
//
//            File file = new File(fileName);
//            FileObject fo = FileUtil.toFileObject(file);
//
//            displayErrorForFileObject(message, fo, line >= 1 ? line : 1, column);
            Util.displayErrorForLocation(message, location, line, column);
        }

        public void displayErrorForFileObject(String message, final FileObject fileObject, final int line, final int column) {
//    //        final XhtmlElement e = Util.getSource(element);
//            OutputListener listener;
//            if (fileObject == null) {
//                listener = null;
//            } else {
//                listener = new OutputListener() {
//                    public void outputLineSelected(OutputEvent ev) {
//                    }
//                    public void outputLineAction(OutputEvent ev) {
//    //                    Util.show(null, unit.getFileObject(), unit.getLine(e),
//    //                              0, true);
//                        // <markup_separation>
//    //                    Util.show(null, fileObject, lineNumber, 0, true);
//                        // ====
//                        showLineAt(fileObject, line, column);
//                        // </markup_separation>
//                    }
//                    public void outputLineCleared (OutputEvent ev) {
//                    }
//                };
//            }
//
//            displayError(message, listener);
            Util.displayErrorForFileObject(message, fileObject, line, column);
        }

//        /** 
//         * Display the given error message to the user. The optional listener argument
//         * (pass in null if not applicable) will make the line hyperlinked and the
//         * listener is invoked to process any user clicks.
//         * @param message The string to be displayed to the user
//         * @param listener null, or a listener to be notified when the user clicks
//         *   the linked message
//         */
//        private void displayError(String message, OutputListener listener) {
//            OutputWriter out = getOutputWriter();
//            try {
//                if (clearErrors) {
//                    out.reset();
//                    clearErrors = false;
//                }
//                // Write the error message to the output tab:
//                out.println(message, listener);
//            }
//            catch (IOException ioe) {
//                // This is lame - our own output window shouldn't throw IO exceptions!
//                ErrorManager.getDefault().notify(ioe);
//            }
//        }
//        
//        private  static void showLineAt(FileObject fo, int lineno, int column) {
//            DataObject dobj;
//            try {
//                dobj = DataObject.find(fo);
//            }
//            catch (DataObjectNotFoundException ex) {
//                ErrorManager.getDefault().notify(ex);
//                return;
//            }
//
//            // Try to open doc before showing the line. This SHOULD not be
//            // necessary, except without this the IDE hangs in its attempt
//            // to open the file when the file in question is a CSS file.
//            // Probably a bug in the xml/css module's editorsupport code.
//            // This has the negative effect of first flashing the top
//            // of the file before showing the destination line, so
//            // this operation is made conditional so only clients who
//            // actually need it need to use it.
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
//
//            LineCookie lc = (LineCookie)dobj.getCookie(LineCookie.class);
//            if (lc != null) {
//                Line.Set ls = lc.getLineSet();
//                if (ls != null) {
//                    // -1: convert line numbers to be zero-based
//                    Line line = ls.getCurrent(lineno-1);
//                    // TODO - pass in a column too?
//                    line.show(Line.SHOW_GOTO, column);
//                }
//            }
//        }
//    
//        private static InputOutput getInputOutput() {
//            return IOProvider.getDefault().getIO(NbBundle.getMessage(InSyncServiceProvider.class, "LBL_Output"), false);
//        }
//        private static OutputWriter getOutputWriter() {
//            InputOutput io = getInputOutput();
//            return io.getOut();
//        }
    } // End of RaveErrorHandlerImpl.
// </error_handling>
    
    public void copyMarkupMouseRegionForElement(Element fromElement, Element toElement) {
        MarkupMouseRegion region = FacesPageUnit.getMarkupMouseRegionForElement(fromElement);
        FacesPageUnit.setMarkupMouseRegionForElement(toElement, region);
    }
    
    public MarkupMouseRegion getMarkupMouseRegionForElement(Element element) {
        return FacesPageUnit.getMarkupMouseRegionForElement(element);
    }
    
    public void copyMarkupDesignBeanForElement(Element fromElement, Element toElement) {
        MarkupDesignBean bean = MarkupUnit.getMarkupDesignBeanForElement(fromElement);
        MarkupUnit.setMarkupDesignBeanForElement(toElement, bean);
    }
    
    public MarkupDesignBean getMarkupDesignBeanForElement(Element element) {
        return MarkupUnit.getMarkupDesignBeanForElement(element);
    }
    
    public void setMarkupDesignBeanForElement(Element element, MarkupDesignBean markupDesignBean) {
        MarkupUnit.setMarkupDesignBeanForElement(element, markupDesignBean);
    }
    
    /**
     * Generate the html string from the given node. This will return
     * an empty string unless the Node is an Element or a DocumentFragment
     * or a Document.
     */
    public String getHtmlStream(org.w3c.dom.Node node) {
        return Util.getHtmlStream(node);
    }
    
    /** Generate the html string from the given element */
    public String getHtmlStream(Element element) {
        return Util.getHtmlStream(element);
    }
    
    /** Generate the html string from the given element. Does formatting. */
    public String getHtmlStream(org.w3c.dom.Document document) {
        return Util.getHtmlStream(document);
    }
    
    /** Generate the html string from the given document fragment */
    public String getHtmlStream(DocumentFragment df) {
        return Util.getHtmlStream(df);
    }
    
    
    
    public boolean isWebPage(FileObject fo) {
        return Util.isWebPage(fo);
    }

    public String[] getMimeTypes() {
        return Util.getMimeTypes();
    }

    public List getWebPages(Project project, boolean includePages, boolean includeFragments) {
        return Util.getWebPages(project, includePages, includeFragments);
    }

    public URL resolveUrl(URL base, Document document, String src) {
        return Util.resolveUrl(base, document, src);
    }
    
    public WriteLock writeLockContext(DesignContext designContext, String message) {
        // XXX Blind casting, just matching the original code.
        return (WriteLock)((LiveUnit)designContext).getModel().writeLock(message);
    }
    public void writeUnlockContext(DesignContext designContext, WriteLock lock) {
        // XXX Blind casting, just matching the original code.
        ((LiveUnit)designContext).getModel().writeUnlock((UndoEvent)lock);
    }
    
    public void addLocalStyleValueForElement(Element element, int style, String value) {
        Util.addLocalStyleValueForElement(element, style, value);
    }
    public void removeLocalStyleValueForElement(Element element, int style) {
        Util.removeLocalStyleValueForElement(element, style);
    }

    
    public Project getProjectForDesignProject(DesignProject designProject) {
        FacesModelSet set = (FacesModelSet)designProject;
        return set.getProject();
    }
    
    public FileObject getMarkupFileObjectForDesignContext(DesignContext designContext) {
        LiveUnit lu = (LiveUnit)designContext;
        FacesPageUnit fu = (FacesPageUnit)lu.getBeansUnit();
        MarkupUnit mu = fu.getPageUnit();
        return mu.getFileObject();
    }
    
    public void initModelsForWebformFile(Project project, FileObject webformFile) {
        FacesModelSet modelset = FacesModelSet.getInstance(project);
        FacesModel model = modelset.getFacesModel(webformFile);
        if (model == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException(webformFile + " has no insync Model!")); // NOI18N
        }
    }
    
    public String expandHtmlEntities(String html, boolean warn, org.w3c.dom.Node node) {
        return Entities.expandHtmlEntities(html, warn, node);
    }
    public int getExpandedOffset(String unexpanded, int unexpandedOffset) {
        return Entities.getExpandedOffset(unexpanded, unexpandedOffset);
    }
    public int getUnexpandedOffset(String unexpanded, int expandedOffset) {
        return Entities.getUnexpandedOffset(unexpanded, expandedOffset);
    }
    
//  Thread.currentThread().getContextClassLoader() stuff
    public ClassLoader getContextClassLoader(DesignContext designContext) {
    	if (designContext == null) {
    		return null;
    	}
    	BeansUnit beansUnit = ((LiveUnit) designContext).getBeansUnit();
    	if (beansUnit == null) {
    	    return null;
    	}
        return beansUnit.getClassLoader();
    }
    
    public ClassLoader getContextClassLoader(DesignBean designBean) {
    	if (designBean == null) {
    		return null;
    	}
    	return getContextClassLoader(designBean.getDesignContext());
    }
    
    public ClassLoader getContextClassLoader(DesignProperty designProperty) {
    	if (designProperty == null) {
    		return null;
    	}
    	
    	return getContextClassLoader(designProperty.getDesignBean());
    }
    
    public ClassLoader getContextClassLoader(DesignEvent designEvent) {
    	if (designEvent == null) {
    		return null;
    	}
    	return getContextClassLoader(designEvent.getDesignBean());
    }
    
}

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
package org.netbeans.modules.visualweb.insync;

import org.netbeans.modules.visualweb.api.designer.cssengine.StyleData;
import org.netbeans.modules.visualweb.designer.html.HtmlAttribute;
import org.netbeans.modules.visualweb.insync.faces.FacesBean;
import org.netbeans.modules.visualweb.insync.live.SourceLiveRoot;
import org.netbeans.modules.visualweb.insync.markup.JspxSerializer;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssValue;
import org.netbeans.modules.visualweb.api.designer.cssengine.XhtmlCss;
import org.netbeans.modules.visualweb.api.designer.markup.MarkupService;
import com.sun.rave.designtime.Customizer2;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignInfo;
import com.sun.rave.designtime.DesignProject;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import org.netbeans.modules.visualweb.insync.beans.Bean;
import org.netbeans.modules.visualweb.insync.faces.FacesPageUnit;
import org.netbeans.modules.visualweb.insync.faces.HtmlBean;
import org.netbeans.modules.visualweb.insync.faces.MarkupBean;
import org.netbeans.modules.visualweb.insync.live.BeansDesignBean;
import org.netbeans.modules.visualweb.insync.live.LiveUnit;
import org.netbeans.modules.visualweb.insync.markup.MarkupUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.insync.models.FacesModelSet;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.visualweb.xhtml.F_Verbatim;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.faces.component.UIComponent;
import javax.swing.text.Segment;
import javax.swing.text.StyledDocument;
import org.apache.xml.serialize.OutputFormat;
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
import org.openide.text.Line;
import org.netbeans.modules.visualweb.extension.openide.util.Trace;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.w3c.dom.NodeList;

/**
 * Common utility methods for helping with NB, Swing and DOM objects.
 *
 * @author Carl Quinn
 * @author Tor Norbye
 */
public final class Util {
    private Util() {}

    /**
     * Get/find the NB DataObject for a given FileObject
     *
     * @param file The given FileObject.
     * @return The corresponding DataObject.
     */
    public static DataObject findDataObject(FileObject file) {
        if (file != null && !file.isValid())
            return null;
        try {
            return DataObject.find(file);
        }
        catch (DataObjectNotFoundException e) {
            return null;
        }
    }

//    /**
//     * Find/get the corresponding Project GenericItem given a NB FileObject
//     *
//     * @param file The given FileObject.
//     * @return The corresponding GenericItem
//     */
//    public static GenericItem findItem(FileObject file) {
//        DataObject dobj = findDataObject(file);
//        return dobj != null ? GenericItem.findItem(dobj) : null;
//    }

    /**
     * Get the NB (data) cookie of a specified type for a given FileObject.
     *
     * @param file The given FileObject.
     * @param type  The cookie type to get.
     * @return The cookie.
     */
    public static org.openide.nodes.Node.Cookie getCookie(FileObject file, Class type) {
        DataObject dobj = findDataObject(file);
        return dobj != null ? dobj.getCookie(type) : null;
    }

    /**
     * Retrieve the swing Abstract Document from a given data object.
     *
     * @param file The file object to retrieve the document from
     * @param open If true, open the document if it's not already open. If false, this method will
     *            return null for data object whose document is not already open.
     * @return The document, or null if the data object is not openable (or if the document is not
     *         open and you did not set the open parameter to true.)
     */
    public static StyledDocument retrieveDocument(FileObject file, boolean open) {

        DataObject dobj;
        try {
            dobj = DataObject.find(file);
        }
        catch (DataObjectNotFoundException e) {
            return null;
        }

        EditorCookie edit = (EditorCookie)dobj.getCookie(EditorCookie.class);
        if (edit == null)
            return null;

        StyledDocument doc = null;
        if (open) {
            try {
                doc = edit.openDocument();  // opens, blocking if needed
            } catch (java.io.IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        } else {
            doc = edit.getDocument();  // gets iff open, & does not block
        }
        return doc;
    }

    /**
     * Load a swing abstract document into a buffer. Use doc.getLength() to get the length in chars.
     *
     *
     * @param doc
     * @return BufferResult with the allocated char[] buffer and size or null and 0.
     */
    public static BufferResult loadDocumentBuffer(StyledDocument doc) {
        try {
            int len = doc.getLength();
            Segment seg = new Segment();
            doc.getText(0, len, seg);
            // make sure we leave at least one pad char for gjc 2.2+ Scanner
            if (seg.array.length == len) {
                char[] buf = new char[len+1];
                System.arraycopy(seg.array, 0, buf, 0, len);
                return new BufferResultImpl(buf, doc.getLength());
            } else {
                return new BufferResultImpl(seg.array, doc.getLength());
            }
        } catch (javax.swing.text.BadLocationException ex) {
            // we know the location we passed is good...
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return new BufferResultImpl(null, 0);
    }
    
    public static final String JAVA_EXT = "java"; // NOI18N
    public static final String JSP_EXT = "jsp"; // NOI18N
    public static final String JSPF_EXT = "jspf"; // NOI18N
    public static final String XML_EXT = "xml"; // NOI18N
    
    public static BufferResult loadFileObjectBuffer(FileObject fileObject) {
        BufferedReader bufferedReader = null;
        String encoding = null;
        String fileExt = fileObject.getExt();
        try {
            BufferedInputStream inputStream = new BufferedInputStream(fileObject.getInputStream());
            if (fileObject.getExt().equals(JAVA_EXT)){
//NB60          encoding = org.netbeans.modules.java.Util.getFileEncoding(fileObject); // Talk to Wiston or Java folks
            } if (fileExt.equals(JSP_EXT) || fileExt.equals(JSPF_EXT) || fileExt.equals(XML_EXT)){
                encoding = EncodingHelper.detectEncoding(inputStream);
            }else{
                encoding = getFileEncoding(fileObject);
            }
            if(encoding != null){
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream,encoding));
            }else{
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            }
            // Read the data line by line (we deal only with files of type .jsp, .java, .xml)
            // and add new line as line separator. This is to emulate how StyledDocument keeps
            // the data internally. 
            // See http://java.sun.com/j2se/1.5.0/docs/api/javax/swing/text/DefaultEditorKit.html (Newlines)

            String aLine;
            StringBuffer strBuilder = new StringBuffer((int)fileObject.getSize() + 1);
            while ((aLine = bufferedReader.readLine()) != null){
                strBuilder.append(aLine + "\n"); 
            }
            strBuilder.append(' ');
            return new BufferResultImpl(strBuilder.toString().toCharArray(), strBuilder.length()-1);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } finally {
            if (bufferedReader != null){
                try {
                    bufferedReader.close();
                } catch (IOException ex){
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
        }
        return new BufferResultImpl(null, 0);
    }
    
    private static final String ATTR_FILE_ENCODING = "Content-Encoding"; // NOI18N
    
    /**
     *  Grab the file object's idea of the source encoding 
     */
    private static String getFileEncoding(FileObject someFile) {
       return (String)someFile.getAttribute(ATTR_FILE_ENCODING);
    }
     
    public static BufferResult loadDocumentOrFileObjectBuffer(FileObject fileObject) {
        StyledDocument styledDocument = retrieveDocument(fileObject, false);
        if (styledDocument == null) {
            return loadFileObjectBuffer(fileObject);
        } else {
            return loadDocumentBuffer(styledDocument);
        }
    }
    
    
    /** XXX Result encapsulating buffer and size read. 
     * Investigate why the size is separate, and not used buffer size directly.
     * It is very suspicious. */
    public interface BufferResult {
        public char[] getBuffer();
        public int getSize();
    } // End of BufferResult.
    
    private static class BufferResultImpl implements BufferResult {
        private final char[] buffer;
        private final int size;
        
        public BufferResultImpl(char[] buffer, int size) {
            this.buffer = buffer;
            this.size = size;
        }
        
        public char[] getBuffer() {
            return buffer;
        }
        public int getSize() {
            return size;
        }
    } // End of BufferResutlImpl.
    
    
    /**
     * Update a Project GenericItem's property to a given value, removing the property if the value
     * is null or blank, and not touching the property if the new value is not a change.
     * @param item The item whose property is to be set.
     * @param name The property name to set.
     * @param newval The new property value.
     */
    public static void updateItemProperty(FileObject file, String name, String newval) {
        // PROJECTTODO2: cleanup, find a different way to do this ?  Like use project API instead ?
        //System.err.println("updateItemProperty on:" + item + " name:" + name + " val:" + newval);
        String oldval = (String) file.getAttribute(name);
        if (oldval == null)
            oldval = "";  // make sure pm's none is the same as our none--very unlikely to be null
        if (!oldval.equals(newval)) {
            try {
	            if (newval != null && newval.length() > 0)
	                file.setAttribute(name, newval);  // have new value: add it
	            else
	                file.setAttribute(name, null);  // have no value: remove property
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // <markup_separation> moved to designer/markup
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
//    public static void show(String filename, FileObject fileObject, int lineno, int column,
//                            boolean openFirst) {
//        assert (filename != null && fileObject == null) || (filename == null && fileObject != null);
//        if (fileObject != null) {
//            show(fileObject, lineno, column, openFirst);
//        }
//        else {
//            File file = new File(filename);
//            FileObject fo = FileUtil.toFileObject(file);
//            if (fo != null) {
//                show(fo, lineno, column, openFirst);
//            }
//        }
//    }
//
//    /**
//     * @param fo
//     * @param lineno
//     * @param column
//     * @param openFirst
//     * @return
//     */
//    private static boolean show(FileObject fo, int lineno, int column, boolean openFirst) {
//        DataObject dobj;
//        try {
//            dobj = DataObject.find(fo);
//        }
//        catch (DataObjectNotFoundException ex) {
//            ErrorManager.getDefault().notify(ex);
//            return false;
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
//        if (openFirst) {
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
//        return false;
//    }
    // </markup_separation>
    
    /**
     * Locate an element of the given tag name as a direct child of the given parent. If no element
     * of that tag name is found it will either return null if the create flag is false, or if the
     * create flag is true, the element will be created and inserted before it is returned.
     * <p>
     * @todo Combine with ensureElement! This method might be slightly faster since it does not
     * search the whole hierarchy for a match; it is intended to build up specific structures, such
     * as <html><head><link>... - it knows it should only look for "head" directly under "html".
     *
     * @param tag The tag name of the tag to be found or created
     * @param parent The element parent under which we want to search
     * @param create If true, create the element if it doesn't exist, otherwise return null if the
     *            tag is not found.
     */
    public static Element findChild(String tag, Node parent, boolean create) {
        NodeList list = parent.getChildNodes();
        int len = list.getLength();
        for (int i = 0; i < len; i++) {
            org.w3c.dom.Node child = list.item(i);
            if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element element = (Element)child;
                if (tag.equals(element.getTagName()))
                    return element;
            }
        }
        if (create) {
            Document document = parent.getOwnerDocument();
            Element element = document.createElement(tag);
            parent.appendChild(element);
            return element;
        }
        return null;
    }

    /**
     * Find the given tag anywhere in the subtree of the given node
     * (possibly including the node itself).
     * @param tag The name of the tag element to be found
     * @param node The root node to begin the search
     * @return The first element in the node tree with the given tag name
     */
    public static Element findDescendant(String tag, Node node) {
        if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
            Element element = (Element)node;
            if (tag.equals(element.getTagName()))
                return element;
        }
        NodeList list = node.getChildNodes();
        int len = list.getLength();
        for (int i = 0; i < len; i++) {
            org.w3c.dom.Node child = list.item(i);
            Element e = findDescendant(tag, child);
            if (e != null) {
                return e;
            }
        }
        return null;
    }


    public static List/*<FileObject>*/ getWebPages(Project project, boolean includePages, boolean includeFragments) {
        List list = new ArrayList(20);
        FileObject fobj = JsfProjectUtils.getDocumentRoot(project);
        addWebPages(list, fobj, includePages, includeFragments);

        return list;
    }

    private static void addWebPages(List list, FileObject folder, boolean includePages, boolean includeFragments) {
        if(folder == null) {
            throw(new IllegalArgumentException("Null folder."));
        }
        if(list == null) {
            throw(new IllegalArgumentException("Null list."));
        }

        FileObject[] children = folder.getChildren();

        for (int i = 0; i < children.length; i++) {
            FileObject fo = children[i];

            if (fo.isFolder()) {
                addWebPages(list, fo, includePages, includeFragments);
            } else {
                if (isWebPage(fo)) {
                    boolean isFragment = "jspf".equals(fo.getExt()); // NOI18N

                    if (isFragment) {
                        if (includeFragments) {
                            list.add(fo);
                        }
                    } else if (includePages) {
                        list.add(fo);
                    }
                }
            }
        }
    }

    /**
     * The following mime types are valid mime types for files
     * that will be considered webforms in the WebAppProject
     */
    private static final String[] FORM_MIME_TYPES = new String[] { "text/x-jsp" }; // NOI18N
    
    public static boolean isWebPage(FileObject fo) {
        String mime = fo.getMIMEType();

        String[] mimeTypes = FORM_MIME_TYPES;

        for (int i = 0; i < mimeTypes.length; i++) {
            if (mimeTypes[i].equals(mime) && 
                    FacesModel.getJavaForJsp(fo) != null) {
                return true;
            }
        }

        return false;
    }

    public static String[] getMimeTypes() {
        return FORM_MIME_TYPES;
    }
    
    public static URL resolveUrl(URL base, Document document, String src) {
        if (src == null) {
            src = "";
        }

        // TODO after Reef: push this into superclass
        URL reference = null;

        // Relative to the web folder?
        if (src.startsWith("/")) { // NOI18N

            // What if it's a local file, e.g. /home/tor/foo.jspf?? that wouldn't work at deploy time anyway..
            try {
                // <markup_separation>
//                MarkupUnit markup = ((RaveDocument)document).getMarkup();
//                FileObject fo = markup.getFileObject();
                // ====
                FileObject fo = getFileObject(document);
                // </markup_separation>
                Project project = FileOwnerQuery.getOwner(fo);

                if (project != null) {
                    FileObject webroot = JsfProjectUtils.getDocumentRoot(project);
                    reference = FileUtil.toFile(webroot).toURI().toURL();
                }

                src = src.substring(1); // strip off leading "/" or URL class will ignore base
            } catch (Exception ex) {
                reference = base;
            }
        } else {
            reference = base;
        }

        try {
            URL u = new URL(reference, src); // XXX what if it's absolute?

            return u;
        } catch (MalformedURLException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);

            return null;
        }
    }

    public static FileObject getFileObject(Document doc) {
        MarkupUnit unit = MarkupUnit.getMarkupUnitForDocument(doc);
        if (unit == null) {
            return null;
        }
        return unit.getFileObject();
    }

    
    public static void customizeCreation(DesignBean[] beans, FacesModel facesModel) {
        for (int i = 0; i < beans.length; i++) {
            DesignBean lb = beans[i];
            DesignInfo lbi = lb.getDesignInfo();

            if (lbi != null) {
                Customizer2 customizer = null; //lbi.getCreateCustomizer(lb);

                if (customizer != null) {
                    CustomizerDisplayer lcd =
                        new CustomizerDisplayer(lb, customizer, customizer.getHelpKey(), facesModel);
                    lcd.show();
                }
            }
        }
    }

    // Moved from FacesDnDSupport.
    /**
     * Return true iff the given DesignBean is the body bean, or the form bean, OR THE LIVE BEAN
     * CONTAINER. These beans have special behavior since they are not draggable, not deletable,
     * etc.
     * TODO Move this into insync.
     * @todo Prevent deletion of f:subview in page fragments!
     */
    public static boolean isSpecialBean(/*WebForm webform,*/ DesignBean bean) {
//        FacesModel model = webform.getModel();
        if (bean == null) {
            // XXX Log NPE?
            return false;
        }
        DesignContext context = bean.getDesignContext();
        // XXX Casting is error-prone.
        FacesModel model = ((LiveUnit)context).getModel();
        if (model == null) {
            // XXX #6469393 Possible NPE.
            return false;
        }
        
        if (bean == model.getRootBean()) {
            return true;
        }

        FacesPageUnit facesUnit = model.getFacesUnit();

        if (facesUnit != null) {
            MarkupBean formBean = facesUnit.getDefaultParent();
            MarkupBean markup = getMarkupBean(bean);

            if (markup == null) {
                return false;
            }

            if (markup == formBean) {
                return true;
            }

//            RaveElement e = (RaveElement)markup.getElement();
//            if (e.getRendered() != null) {
//                e = (RaveElement)e.getRendered();
            Element e = markup.getElement();
            Element rendered = MarkupService.getRenderedElementForElement(e);
            if (rendered != null) {
                e = rendered;

                // Anything from the body or up is special -- cannot be removed
//                Node curr = webform.getBody();
                Node curr = InSyncServiceProvider.get().getHtmlBodyForMarkupFile(model.getMarkupFile());

                while (curr != null) {
                    if (curr == e) {
                        return true;
                    }

                    curr = curr.getParentNode();
                }
            } else {
                // Anything from the body or up is special -- cannot be removed
//                Node curr = webform.getBody().getSource();
                Element body = InSyncServiceProvider.get().getHtmlBodyForMarkupFile(model.getMarkupFile());
                Node curr = MarkupService.getSourceElementForElement(body);

                while (curr != null) {
                    if (curr == e) {
                        return true;
                    }

                    curr = curr.getParentNode();
                }
            }
        }

        return false;
    }

    // Moved from FacesDnDSupport.
    /**
     * Return the MarkupBean for the live bean. May be null, for non markup live beans.
     *
     * @param lb The live bean to get the faces bean for. May be null.
     * @return the MarkupBean corresponding to the live bean, or null.
     */
    public static MarkupBean getMarkupBean(DesignBean lb) {
        if (!(lb instanceof BeansDesignBean)) {
            return null;
        }

        Bean b = ((BeansDesignBean)lb).getBean();

        if (b instanceof MarkupBean) {
            return (MarkupBean)b;
        }

        return null;
    }
    
    /** Strip the given string to the given maximum length of
     * characters. If the string is not that long, just return
     * it.  If it needs to be truncated, truncate it and append
     * "...".  maxLength must be at least 4. */
    public static String truncateString(String s, int maxLength) {
        assert maxLength >= 4;
        
//        if(DEBUG) {
//            debugLog(DesignerUtils.class.getName() + ".truncateString(String, int)");
//        }
        if(s == null) {
            throw(new IllegalArgumentException("Null string to truncate."));// NOI18N
        }
        
        if (s.length() > maxLength) {
            // Should "..." be localizable?
            return s.substring(0, maxLength - 3) + "...";
        } else {
            return s;
        }
    }

    // Moved from FacesDnDSupport.
    public static DesignBean findParent(String className, DesignBean droppee, Node parentNode,
    boolean searchUp, FacesModel facesModel) {
        if (isGridMode(facesModel) && (droppee == null) && (facesModel.getLiveUnit() != null)) {
            MarkupBean bean = facesModel.getFacesUnit().getDefaultParent();

            if (bean != null) {
                droppee = facesModel.getLiveUnit().getDesignBean(bean);
            }
        }

        DesignBean parent = droppee;

        if (searchUp) {
            for (; (parent != null) && !parent.isContainer(); parent = parent.getBeanParent()) {
                ;
            }
        }

        LiveUnit unit = facesModel.getLiveUnit();

        if (searchUp) {
            boolean isHtmlBean =
                className.startsWith(HtmlBean.PACKAGE) &&
                // f:verbatim is explicitly allowed where jsf components can go
                // XXX Why not F_Verbatim.class.getName() ?
                !(HtmlBean.PACKAGE + "F_Verbatim").equals(className); // NOI18N

            if (isHtmlBean) {
                // We can't drop anywhere below a "renders children" JSF
                // component
                parent = findHtmlContainer(parent);
            }
        }

        // Validate the parent: walk up the parent chain until you find
        // a parent which will accept the child.
        for (; parent != null; parent = parent.getBeanParent()) {
            if (unit.canCreateBean(className, parent, null)) {
                // Found it
                break;
            }

            if (!searchUp) {
                return null;
            }
        }

        if ((parent == null) && (parentNode != null)) {
            // Adjust hierarchy: we should pass in a parent
            // pointer based on where we are: locate the closest
            // jsf parent above
            Node n = parentNode;
            MarkupBean mb = null;

            while (n != null) {
                if (n instanceof Element) {
                    Element e = (Element)n;
//                    mb = FacesSupport.getMarkupBean(webform.getDocument(), e);
                    mb = getMarkupBean(facesModel, e);

                    if (mb != null) {
                        break;
                    }
                }

                n = n.getParentNode();
            }

            if (mb != null) {
                DesignBean lmb = facesModel.getLiveUnit().getDesignBean(mb);

                if (lmb.isContainer()) {
                    parent = lmb;
                }
            }

            if (parent == null) {
                parent = facesModel.getRootBean();
            }
        }

        return parent;
    }

    // Moved from FacesDnDSupport.
    /**
     *  Return true if this document is in "grid mode" (objects
     *  should be positioned by absolute coordinates instead of in
     *  "flow" order.
     *
     *  @return true iff the document should be in grid mode
     */
    public static boolean isGridMode(FacesModel facesModel) {
        Element b = facesModel.getHtmlBody();

        if (b == null) {
            return false;
        }

//        Value val = CssLookup.getValue(b, XhtmlCss.RAVELAYOUT_INDEX);
        CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(b, XhtmlCss.RAVELAYOUT_INDEX);

//        return val == CssValueConstants.GRID_VALUE;
        return CssProvider.getValueService().isGridValue(cssValue);
    }

    // Moved from FacesDnDSupport.
    /**
     * Find the nearest DesignBean container that allows html children.
     * This will typically be the parent you pass in, but if there
     * are any beans up in the hierarchy that renders their own
     * children, then the outermost such parent's parent will be used,
     * since "renders children" jsf components cannot contain markup.
     */
    public static DesignBean findHtmlContainer(DesignBean parent) {
        DesignBean curr = parent;

        for (; curr != null; curr = curr.getBeanParent()) {
            if (curr.getInstance() instanceof F_Verbatim) {
                // If you have a verbatim, we're okay to add html comps below it
                return parent;
            }

            if (curr.getInstance() instanceof UIComponent) {
                // XXX Maybe now, whitin insync one could provide a better check for the classloader.
                
				// Need to set the Thread's context classloader to be the Project's ClassLoader.
            	ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
            	try {
            		Thread.currentThread().setContextClassLoader(InSyncServiceProvider.get().getContextClassLoader(curr));
	            	if (((UIComponent)curr.getInstance()).getRendersChildren()) {
	            		parent = curr.getBeanParent();

						// Can't break here - there could be an outer
                        // renders-children parent
	            	}
            	} finally {
            		Thread.currentThread().setContextClassLoader(oldContextClassLoader);
            	}
            }
        }

        return parent;
    }

    // Moved from FacesDnDSupport.
    /**
     * Given an element which possibly maps to a markup bean, return the corresponding bean.
     */
    private static MarkupBean getMarkupBean(FacesModel model, Element elem) {
//        FacesModel model = doc.getWebForm().getModel();

        if (model == null) { // testsuite
            return null;
        }

        FacesPageUnit facesunit = model.getFacesUnit();
        MarkupBean bean = null;

        if (facesunit != null) {
            bean = facesunit.getMarkupBean(elem);
            // Find component for this element:
        }

        return bean;
    }

    // Moved from FacesDnDSupport.
    /** Given a node, return the nearest DesignBean that "contains" it */
    public /*private*/ static DesignBean findParentBean(Node node) {
        while (node != null) {
//            if (node instanceof RaveElement) {
//                RaveElement element = (RaveElement)node;
            if (node instanceof Element) {
                Element element = (Element)node;

//                if (element.getDesignBean() != null) {
//                    return element.getDesignBean();
//                }
                MarkupDesignBean markupDesignBean = InSyncServiceProvider.get().getMarkupDesignBeanForElement(element);
                if (markupDesignBean != null) {
                    return markupDesignBean;
                }
            }

            node = node.getParentNode();
        }

        return null;
    }

    // Moved from FacesDnDSupport.
    /** Attempt to set the given attribute on the bean to the given length
     * and return true iff it succeeds.
     */
    public static boolean setDesignProperty(DesignBean bean, String attribute, int length) {
        DesignProperty prop = bean.getProperty(attribute);

        if (prop != null) {
            PropertyDescriptor desc = prop.getPropertyDescriptor();
            Class clz = desc.getPropertyType();

            // I can do == instead of isAssignableFrom because
            // both String and Integer are final!
            if (clz == String.class) {
                prop.setValue(Integer.toString(length));

                return true;
            } else if (clz == Integer.TYPE) {
                prop.setValue(new Integer(length));

                return true;
            }
        }

        return false;
    }

    public static boolean isPageRootContainerDesignBean(DesignBean designBean) {
        // XXX Bad way how to determine this is bean representing Page (not Session, etc.).
        if (designBean instanceof SourceLiveRoot) {
            DesignContext designContext = designBean.getDesignContext();
            if (designContext instanceof LiveUnit) {
                LiveUnit liveUnit = (LiveUnit)designContext;
                FacesModel facesModel = liveUnit.getModel();
                if (facesModel != null && facesModel.getMarkupFile() != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Generate the html string from the given node. This will return
     * an empty string unless the Node is an Element or a DocumentFragment
     * or a Document.
     */
    public static String getHtmlStream(org.w3c.dom.Node node) {
        if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
            return getHtmlStream((Element)node);
        } else if (node.getNodeType() == org.w3c.dom.Node.DOCUMENT_FRAGMENT_NODE) {
            return getHtmlStream((DocumentFragment)node);
        } else if (node.getNodeType() == org.w3c.dom.Node.DOCUMENT_NODE) {
            return getHtmlStream((org.w3c.dom.Document)node);
        } else if ((node.getNodeType() == org.w3c.dom.Node.TEXT_NODE) ||
                (node.getNodeType() == org.w3c.dom.Node.CDATA_SECTION_NODE)) {
            return node.getNodeValue();
        } else {
            return ""; // NOI18N
        }
    }
    
    /** Generate the html string from the given element */
    public static String getHtmlStream(Element element) {
        StringWriter w = new StringWriter(); // XXX initial size?
        OutputFormat format = new OutputFormat(element.getOwnerDocument(), null, true); // default enc, do-indent
        format.setLineWidth(160);
        format.setIndent(4);

        JspxSerializer serializer = new JspxSerializer(w, format);

        try {
            serializer.serialize(element);
        } catch (java.io.IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        return w.getBuffer().toString();
    }
    
    /** Generate the html string from the given element. Does formatting. */
    public static String getHtmlStream(org.w3c.dom.Document document) {
        StringWriter w = new StringWriter(); // XXX initial size?
		// Bug Fix# 6452255 Source File error Fix HTML errors removes UTF-8 encoding in JSP
		// Use the document encoding from the original document
        OutputFormat format = new OutputFormat(document, document.getXmlEncoding(), true); // do-indent
        format.setLineWidth(160);
        format.setIndent(4);

        JspxSerializer serializer = new JspxSerializer(w, format);

        try {
            serializer.serialize(document);
        } catch (java.io.IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        return w.getBuffer().toString();
    }
    
    /** Generate the html string from the given document fragment */
    public static String getHtmlStream(DocumentFragment df) {
        OutputFormat format = new OutputFormat(df.getOwnerDocument()); // default enc, do-indent
        format.setLineWidth(160);
        format.setIndent(4);

        StringWriter w = new StringWriter(); // XXX initial size?
        JspxSerializer serializer = new JspxSerializer(w, format);

        try {
            serializer.serialize(df);
        } catch (java.io.IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        return w.getBuffer().toString();
    }

    public static boolean isBraveheartPage(Document dom) {
        if(dom == null) {
            return false;
        }
        // Many possibilities here:
        // (1) Scan through all tags, look to see if we find any ui: tags
        // (2) Look on the jsp:root, see if we include the braveheart taglib
        // (3) See if the top most tag under f:view is a braveheart one
        // (4) See if the body is a plain html <body> tag or if it's rendered from
        //    another component
        Element element = dom.getDocumentElement();

//        if (element.hasAttribute("xmlns:ui")) { // NOI18N
//            assert element.getAttribute("xmlns:ui").equals("http://www.sun.com/web/ui"); // NOI18N
//
//            return true;
//        }
//
//        return false;
        return hasXmlnsOfValue(element, "http://www.sun.com/web/ui"); // NOI18N
    }
    
    public static boolean isWoodstockPage(Document dom) {
        if(dom == null) {
            return false;
        }
        // Many possibilities here:
        // (1) Scan through all tags, look to see if we find any ui: tags
        // (2) Look on the jsp:root, see if we include the braveheart taglib
        // (3) See if the top most tag under f:view is a braveheart one
        // (4) See if the body is a plain html <body> tag or if it's rendered from
        //    another component
        Element element = dom.getDocumentElement();
        
//        if (element.hasAttribute("xmlns:webuijsf")) { // NOI18N
//            assert element.getAttribute("xmlns:webuijsf").equals("http://www.sun.com/webui/webuijsf"); // NOI18N
//
//            return true;
//        }
//
//        return false;
        return hasXmlnsOfValue(element, "http://www.sun.com/webui/webuijsf"); // NOI18N
    }

    private static boolean hasXmlnsOfValue(Element element, String xmlnsValue) {
        if (element == null || xmlnsValue == null) {
            return false;
        }
        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node attr = attrs.item(i);
            String name = attr.getNodeName();
            if (name.startsWith("xmlns:")) { // NOI18N
                if (xmlnsValue.equals(attr.getNodeValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    ///////////////////////////////////    
    // CSS style sheet modifications >>
    
    public static void addLocalStyleValueForElement(Element element, int style, String value) {
        List set = new ArrayList(1);
        set.add(new StyleData(style, value));
        updateLocalStyleValuesForElement(element,(StyleData[])set.toArray(new StyleData[set.size()]), null);
    }
    
    public static void removeLocalStyleValueForElement(Element element, int style) {
        List remove = new ArrayList(1);
        remove.add(new StyleData(style));
        updateLocalStyleValuesForElement(element, null, (StyleData[])remove.toArray(new StyleData[remove.size()]));
    }
    
    public static void updateLocalStyleValuesForElement(Element element, StyleData[] stylesToSet, StyleData[] stylesToRemove) {
        try {
//            String value = engine.getUpdatedLocalStyleValues(element, set, remove);
            String value = CssProvider.getEngineService().getUpdatedLocalStyleValuesForElement(element, stylesToSet, stylesToRemove);
            
//            if (element instanceof RaveElement) {
//                RaveElement raveElement = (RaveElement)element;
////                DesignBean markupBean = raveElement.getDesignBean();
//                DesignBean markupBean = InSyncService.getProvider().getMarkupDesignBeanForElement(raveElement);
            if (element != null) {
//                DesignBean markupBean = InSyncService.getProvider().getMarkupDesignBeanForElement(element);
                DesignBean markupBean = MarkupUnit.getMarkupDesignBeanForElement(element);
                if (markupBean != null) {
                    DesignProperty property = markupBean.getProperty(HtmlAttribute.STYLE);
                    if (property != null) {
                        try {
                            if ((value != null) && (value.length() > 0)) {
                                property.setValue(value);
                            } else {
                                property.unset();
                            }

                            return;
                        } catch (Exception ex) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                            // For some reason the above throws exceptions
                            // sometimes, not sure why org.w3c.dom.DOMException:
                            // NOT_FOUND_ERR: An attempt is made to reference a
                            // node in a context where it does not exist.  TODO
                            // - figure out WHY!  For now just swallow since
                            // there's nothing more we can do about it.
                            // (Update: It think this may be fixed now)
                        }
                    }
                }
            }

            // If the above fails (shouldn't)
//            engine.setStyleAttributeValue(element, value);
            CssProvider.getEngineService().setStyleAttributeForElement(element, value);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }

    // CSS style sheet modifications <<
    ///////////////////////////////////
    
    
    // XXX Moved from InSyncServiceProvider.
    public static String computeFileName(Object location) {
        if (location instanceof String) {
            return (String)location;
        } else if (location instanceof URL) {
            // <markup_separation>
//            return MarkupUnit.fromURL(((URL)location).toExternalForm());
            // ====
            return fromURL(((URL)location).toExternalForm());
            // </markup_separation>
        } else if (location instanceof Element) {
            // Locate the filename for a given element
            Element element = (Element)location;
            element = MarkupService.getCorrespondingSourceElement(element);

            // <markup_separation>
//            // XXX I should derive this from the engine instead, after all
//            // the engine can know the unit! (Since engines cannot be used
//            // with multiple DOMs anyway)
//            FileObject fo = unit.getFileObject();
            // ====
            FileObject fo = getFileObject(element.getOwnerDocument());
            // </markup_separation>
            File f = FileUtil.toFile(fo);

            return f.toString();
        } else if (location != null) {
            return location.toString();
        }

        return "";
    }

    // XXX Moved from InSyncServiceProvider.
    public static int computeLineNumber(Object location, int lineno) {
        if (location instanceof Element) {
            /*
            // The location is an XhtmlElement -- so the line number
            // needs to be relative to it.... compute the line number
            // of the element
            if (lineno == -1)
                lineno = 0;
            Element element = (Element)location;
            RaveDocument doc = (RaveDocument)element.getOwnerDocument();
            lineno += doc.getLineNumber(element);
             */
            if (lineno == -1) {
                lineno = 0;
            }

            Element element = (Element)location;
            element = MarkupService.getCorrespondingSourceElement(element);
            // <markup_separation>
//            lineno += unit.computeLine(element);
            // ====
            lineno += computeLine(element.getOwnerDocument(), element);
            // </markup_separation>
        }

        return lineno;
    }

    // XXX Moved from InSyncServiceProvider.
    public static int computeLine(Document doc, Element element) {
        MarkupUnit unit = MarkupUnit.getMarkupUnitForDocument(doc);
        if (unit == null) {
            return 0;
        }
        return unit.computeLine(element);
    }
    
    // XXX Moved from InSyncServiceProvider.
    public static String fromURL(String url) {
        if (url.startsWith("file:")) { // NOI18N
            int n = url.length();
            StringBuffer sb = new StringBuffer(n);
            for (int i = 5; i < n; i++) {
                char c = url.charAt(i);
                // TODO -- any File.separatorChar manipulation perhaps?
                if (c == '%' && i < n-3) {
                    char d1 = url.charAt(i+1);
                    char d2 = url.charAt(i+2);
                    if (Character.isDigit(d1) && Character.isDigit(d2)) {
                        String numString = ""+d1+d2;
                        try {
                            int num = Integer.parseInt(numString, 16);
                            if (num >= 0 && num <= 255) {
                                sb.append((char)num);
                                i += 2;
                                continue;
                            }
                        } catch (NumberFormatException nex) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, nex);
                        }
                    }
                    sb.append(c);
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }
        return url;
    }

    // XXX Moved from InSyncServiceProvider.
    public static URL getDocumentUrl(Document doc) {
//        if (!(doc instanceof RaveDocument)) {
//            return null;
//        }
        if (doc == null) {
            return null;
        }
        
//        RaveDocument rDoc = (RaveDocument)doc;
// <removing set/getRoot from RaveDocument>
//        DesignProject designProject = rDoc.getRoot().getDesignBean().getDesignContext().getProject();
        MarkupUnit unit = MarkupUnit.getMarkupUnitForDocument(doc);
        DesignProject designProject;
        if (unit != null) {
            FacesModel facesModel = FacesModel.getInstance(unit.getFileObject());
            designProject = ((DesignContext)facesModel.getLiveUnit()).getProject();
        } else {
            designProject = null;
        }
// <removing set/getRoot from RaveDocument>
        if(designProject instanceof FacesModelSet) {
            FacesModelSet fModelSet = (FacesModelSet)designProject;
            FileObject documentRoot = JsfProjectUtils.getDocumentRoot(fModelSet.getProject());
            try {
                return documentRoot.getURL();
            } catch(FileStateInvalidException fsie) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, fsie);
            }
        }
        
        return null;
    }

    
    ////////////////////
    // Error Handling >>
    private static boolean clearErrors = false;

    public static void clearErrors(boolean delayed) {
        if (delayed) {
            clearErrors = true;
        } else {
            OutputWriter out = getOutputWriter();
            try {
                out.reset();
            }
            catch (IOException ioe) {
                // This is lame - our own output window shouldn't
                // throw IO exceptions!
                ErrorManager.getDefault().notify(ioe);
            }
        }
    }

    public static void selectErrors() {
        InputOutput io = getInputOutput();
        io.select();
    }
        
    public static void displayErrorForLocation(String message, Object location, int line, int column) {
        String fileName = InSyncServiceProvider.get().computeFileName(location);
        line = InSyncServiceProvider.get().computeLineNumber(location, line);

        // XXX There was no file found.
        if (fileName == null || "".equals(fileName)) { // NOI18N
            displayError(message, new DummyOutputListener());
        } else {
            File file = new File(fileName);
            FileObject fo = FileUtil.toFileObject(file);

            displayErrorForFileObject(message, fo, line >= 1 ? line : 1, column);
        }
    }

    public static void displayErrorForFileObject(String message, final FileObject fileObject, final int line, final int column) {
//        final XhtmlElement e = Util.getSource(element);
        OutputListener listener = new OutputListener() {
            public void outputLineSelected(OutputEvent ev) {
            }
            public void outputLineAction(OutputEvent ev) {
//                    Util.show(null, unit.getFileObject(), unit.getLine(e),
//                              0, true);
                // <markup_separation>
//                    Util.show(null, fileObject, lineNumber, 0, true);
                // ====
                if (fileObject != null) {
                    showLineAt(fileObject, line, column);
                }
                // </markup_separation>
            }
            public void outputLineCleared (OutputEvent ev) {
            }
        };

        // XXX There needs to be a non-null listener.
        displayError(message, listener);
    }

    /** 
     * Display the given error message to the user. The optional listener argument
     * (pass in null if not applicable) will make the line hyperlinked and the
     * listener is invoked to process any user clicks.
     * @param message The string to be displayed to the user
     * @param listener null, or a listener to be notified when the user clicks
     *   the linked message
     */
    private static void displayError(String message, OutputListener listener) {
        OutputWriter out = getOutputWriter();
        try {
            if (clearErrors) {
                out.reset();
                clearErrors = false;
            }
            
            if (listener == null) {
                // #107754 Now the null is not accepted as a listener.
                listener = new DummyOutputListener();
            }
            
            // Write the error message to the output tab:
            out.println(message, listener);
        }
        catch (IOException ioe) {
            // This is lame - our own output window shouldn't throw IO exceptions!
            ErrorManager.getDefault().notify(ioe);
        }
    }

    private  static void showLineAt(FileObject fo, int lineno, int column) {
        DataObject dobj;
        try {
            dobj = DataObject.find(fo);
        }
        catch (DataObjectNotFoundException ex) {
            ErrorManager.getDefault().notify(ex);
            return;
        }

        // Try to open doc before showing the line. This SHOULD not be
        // necessary, except without this the IDE hangs in its attempt
        // to open the file when the file in question is a CSS file.
        // Probably a bug in the xml/css module's editorsupport code.
        // This has the negative effect of first flashing the top
        // of the file before showing the destination line, so
        // this operation is made conditional so only clients who
        // actually need it need to use it.
        EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
        if (ec != null) {
            try {
                ec.openDocument(); // ensure that it has been opened - REDUNDANT?
                //ec.open();
            }
            catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }

        LineCookie lc = (LineCookie)dobj.getCookie(LineCookie.class);
        if (lc != null) {
            Line.Set ls = lc.getLineSet();
            if (ls != null) {
                // -1: convert line numbers to be zero-based
                Line line = ls.getCurrent(lineno-1);
                // TODO - pass in a column too?
                line.show(Line.SHOW_GOTO, column);
            }
        }
    }

    private static InputOutput getInputOutput() {
        return IOProvider.getDefault().getIO(NbBundle.getMessage(Util.class, "LBL_Output"), false);
    }
    private static OutputWriter getOutputWriter() {
        InputOutput io = getInputOutput();
        return io.getOut();
    }
    // Error Handling <<
    ////////////////////
    
    public static Element getHtmlBodyForMarkupFile(FileObject markupFile) {
        FacesModel facesModel = FacesModel.getInstance(markupFile);
        if (facesModel == null) {
            return null;
        } else {
            return facesModel.getHtmlBody();
        }
    }
    
    public static Element getHtmlBodyForDocument(Document document) {
        FileObject markupFile = getFileObject(document);
        if (markupFile == null) {
            return null;
        } else {
            return getHtmlBodyForMarkupFile(markupFile);
        }
    }

    public static DocumentFragment getHtmlDomFragmentForDocument(Document document) {
        FileObject markupFile = getFileObject(document);
        if (markupFile == null) {
            return null;
        }
        
        FacesModel facesModel = FacesModel.getInstance(markupFile);
        if (facesModel == null) {
            return null;
        } else {
            return facesModel.getHtmlDomFragment();
        }
    }

    
    /**
     * Return true iff the given DesignBean is the form bean for this form OR THE LIVE BEAN CONTAINER,
     * since it acts like the form bean in many ways (not draggable, not deletable, etc.)
     */
    public static boolean isFormBean(FacesModel facesModel, DesignBean bean) {
//        FacesModel model = webform.getModel();

        if (bean == facesModel.getRootBean()) {
            return true;
        }

        FacesPageUnit facesUnit = facesModel.getFacesUnit();
        MarkupBean formBean = facesUnit.getDefaultParent();

        return getFacesBean(bean) == formBean;
    }
    
    /**
     * Return the FacesBean for the live bean. May be null, for non faces live beans.
     *
     * @param lb The live bean to get the faces bean for. May be null.
     * @return the FacesBean corresponding to the live bean, or null.
     */
    public static FacesBean getFacesBean(DesignBean lb) {
        if (!(lb instanceof BeansDesignBean)) {
            return null;
        }

        Bean b = ((BeansDesignBean)lb).getBean();

        if (b instanceof FacesBean) {
            return (FacesBean)b;
        }

        return null;
    }

    /**
     * Return the element for the live bean. May be null, for non faces beans for example.
     */
    public static Element getElement(DesignBean lb) {
        if (lb instanceof MarkupDesignBean) {
            return ((MarkupDesignBean)lb).getElement();
        } else {
            return null;
        }
    }
    
    
    private static class DummyOutputListener implements OutputListener {
        public void outputLineSelected(OutputEvent ev) {
            // No op.
        }

        public void outputLineAction(OutputEvent ev) {
            // No op.
        }

        public void outputLineCleared(OutputEvent ev) {
            // No op.
        }
    } // DummyOutputListener.
}

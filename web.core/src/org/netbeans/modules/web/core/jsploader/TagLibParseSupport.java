/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.jsploader;

import java.beans.*;
import java.lang.ref.WeakReference;
import java.util.*;
import java.io.IOException;

import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.MultiDataObject;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author  pjiricka
 * @version 
 */
public class TagLibParseSupport implements Node.Cookie {

    private JspDataObject jspdo;
    private boolean documentDirty;
    private RequestProcessor.Task parsingTask = null;
    private WeakReference tagLibEditorDataRef;

    /** Creates new TagLibParseSupport */
    public TagLibParseSupport(JspDataObject jspdo) {
        this.jspdo = jspdo;
    }

    /** Gets the tag library data relevant for the editor. */
    public TagLibEditorData getTagLibEditorData() {
        return getTagLibEditorData(true);
    }
    
    TagLibEditorData getTagLibEditorData(boolean prepare) {
        if (tagLibEditorDataRef != null) {
            Object o = tagLibEditorDataRef.get();
            if (o != null)
                return (TagLibEditorData)o;
        }
        TagLibEditorData tled = new TagLibEditorData(this);
        tagLibEditorDataRef = new WeakReference(tled);
        if (prepare) {
            prepare();
        }
        return tled;
    }

    /** Sets the dirty flag - if the document was modified after last parsing. */
    synchronized void setDocumentDirty(boolean b) {
        documentDirty = b;
    }

    /** Tests the documentDirty flag. */
    boolean isDocumentDirty() {
        return documentDirty;
    }

    /** Starts the parsing if the this class is 'dirty' and status != STATUS_NOT
    * and parsing is not running yet.
      @return parsing task so caller may listen on its completion.
    */
    Task autoParse() {
        return parseObject(Thread.MIN_PRIORITY);
    }

    /** Method that instructs the implementation of the source element
    * to prepare the element. It is non blocking method that returns
    * task that can be used to control if the operation finished or not.
    *
    * @return task to control the preparation of the elemement
    */
    public Task prepare() {
        return parseObject(Thread.MAX_PRIORITY - 1);
    }

    private synchronized Task parseObject(int priority) {
        RequestProcessor.Task t = parsingTask;

        if (t != null) {
            t.setPriority(Math.max(t.getPriority(), priority));
            return t;
        }

//System.out.println("[Parsing] Got parse request");
        setDocumentDirty(false);
        t = RequestProcessor.postRequest(new ParsingRunnable(), 0, priority);
        parsingTask = t;
        return parsingTask;
    }

    private class ParsingRunnable implements Runnable {
        public void run() {
            try {
                try {
                    JspParserAPI parser = JspCompileUtil.getJspParser();
                    if (parser == null) {
                        ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, 
                        new NullPointerException());
                    }
                    else {
                        JspParserAPI.ParseResult result = 
                            parser.parsePage(jspdo, JspCompileUtil.getContextPath(jspdo.getPrimaryFile()));
                        if (result.isParsingSuccess()) {
                            JspInfo info = result.getPageInfo();
                            getTagLibEditorData(false).applyParsedData(info.getTagLibraryData(), 
                                                                  jspdo.getPrimaryFile().getFileSystem());
                            getTagLibEditorData(false).setBeanData(info.getBeans());
                            getTagLibEditorData(false).setErrorPage(info.isErrorPage ());
                        }
                        // if failure do nothing
                    }
                }
                catch (IOException e) { 
                    ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, e);
                }
            }
            finally {
                synchronized (TagLibParseSupport.this) {
                    parsingTask = null;
                }
            }
        }
    }
   

    /** Data structure which provides data to JSP syntax coloring and code completion for one page. 
    * It does not attempt to faithfully represent the tag library as specified by the JSP spec,
    * it only provides information necessary for syntax coloring and tag completion.
     * It also provides other information about page such as bean info's and error page
     * attribute.
    */
    public static class TagLibEditorData extends PropertyChangeSupport {

        /** An imaginary property whose change is fired always when the tag library 
        *  information changes in such a way that recoloring of the document is required. */
        public static final String PROP_COLORING_CHANGE = "coloringChange"; // NOI18N

        private TreeMap libraryMap = new TreeMap();
        private JspInfo.BeanData[] beanData = new JspInfo.BeanData[0];
        private boolean errorPage = false;
        
        TagLibEditorData(Object sourceBean) {
            super(sourceBean);
        }
        
        public void setBeanData(JspInfo.BeanData[] beanData) {
            this.beanData = beanData;
        }

        public JspInfo.BeanData[] getBeanData() {
            return beanData;
        }
        
        public void setErrorPage (boolean errorPage) {
            this.errorPage = errorPage;
        }
        
        public boolean isErrorPage () {
            return errorPage;
        }

        void applyParsedData(JspInfo.TagLibraryData[] taglibs, FileSystem fs) {
            // pending information from inside the library
            TreeMap otherMap = new TreeMap();
            for (int i = 0; i < taglibs.length; i++) {
                String prefix = taglibs[i].getPrefix();
                otherMap.put(prefix, createTagLibData(taglibs[i], fs));
            }
            boolean coloringChanged = false;
            if (libraryMap.size() != otherMap.size()) {
                coloringChanged = true;
            }
            else {
                TagLibData[] myTagLibData = getTagLibData();
                TagLibData[] otherTagLibData = 
                    (TagLibData[])otherMap.values().toArray(new TagLibData[otherMap.size()]);
                for (int i = 0; i < myTagLibData.length; i++) {
                    if (!myTagLibData[i].equalsColoringInformation(otherTagLibData[i])) {
                        coloringChanged = true;
                        break;
                    }
                }
            }
            libraryMap = otherMap;
            if (coloringChanged) {
                firePropertyChange(PROP_COLORING_CHANGE, null, null);
            }
        }

        public TagLibData[] getTagLibData() {
            return (TagLibData[])libraryMap.values().toArray(new TagLibData[libraryMap.size()]);
        }

        public TagLibData getTagLibData(String prefix) {
            return (TagLibData)libraryMap.get(prefix);
        }

        protected void finalize() throws Throwable {
            super.finalize();
        }

    }
    
    static TagLibData createTagLibData(JspInfo.TagLibraryData info, FileSystem fs) {
        JspParserAPI parser = JspCompileUtil.getJspParser();
        if (parser == null) {
            ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, 
            new NullPointerException());
            return null;
        }
        else {
            return parser.createTagLibData(info, fs);
        }
    }
    
    /** Information about one tag library, to be implemented by the JSPParser module. */
    public static abstract class TagLibData {
        
        public abstract boolean equalsColoringInformation(TagLibData other);
        
        public abstract String getPrefix();
        
        /** Should really return javax.servlet.tagext.TagLibraryInfo,
         * only returning object so we don't depend on servlet.jar.
         */
        public abstract Object getTagLibraryInfo();
            
    }

}
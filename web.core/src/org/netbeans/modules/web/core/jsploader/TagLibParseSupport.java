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

import javax.servlet.jsp.tagext.*;

import org.openide.TopManager;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.MultiDataObject;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

import org.apache.jasper.runtime.JspLoader;
import org.apache.jasper.compiler.JspReader;
import org.apache.jasper.compiler.Parser;

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
                    JspInfo info = parsePage(JspCompileUtil.getContextPath(jspdo.getPrimaryFile()));
                    getTagLibEditorData(false).applyParsedData(info.getTagLibraryData(), 
                                                          jspdo.getPrimaryFile().getFileSystem());
                    getTagLibEditorData(false).setBeanData(info.getBeans());
                }
                catch (Exception e) { 
                    // this exception signifies that a parse error occurred in the page - 
                    // can't really do anything
                    if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                        // only report the error in netbeans.debug.exceptions mode, as this is a usual 
                        // and common exception, which does not imply a bug or an unusual condition
                        TopManager.getDefault ().getErrorManager ().notify (ErrorManager.INFORMATIONAL, e);
                    }
                }
            }
            finally {
                synchronized (TagLibParseSupport.this) {
                    parsingTask = null;
                }
            }
        }
    }
   
    private JspInfo parsePage(String compilationURI)
    throws Exception {
        OptionsImpl options = new OptionsImpl(jspdo);
        
        ParsingDescriptor pd = new ParsingDescriptor(
            JspCompileUtil.getContextRoot(jspdo.getPrimaryFile()).getFileSystem(), compilationURI);
        String jspResource = JspCompileUtil.getContextPath(jspdo.getPrimaryFile());

        AnalyzerCompilerContext ctxt = new AnalyzerCompilerContext(jspResource, pd, options);
        JspReader reader = JspReader.createJspReader(jspResource, ctxt, "8859_1");
        ctxt.setReader(reader);

        AnalyzerParseEventListener listener = new AnalyzerParseEventListener(reader, ctxt, 
            false, AnalyzerParseEventListener.ERROR_IGNORE);
        Parser parser = new Parser(reader, listener);
        listener.beginPageProcessing();
        parser.parse();
        listener.endPageProcessing();
        return listener.getJspInfo();
    }

    /** Data structure which provides data to JSP syntax coloring and code completion for one page. 
    * It does not attempt to faithfully represent the tag library as specified by the JSP spec,
    * it only provides information necessary for syntax coloring and tag completion.
    */
    public static class TagLibEditorData extends PropertyChangeSupport {

        /** An imaginary property whose change is fired always when the tag library 
        *  information changes in such a way that recoloring of the document is required. */
        public static final String PROP_COLORING_CHANGE = "coloringChange";

        private TreeMap libraryMap = new TreeMap();
        private JspInfo.BeanData[] beanData = new JspInfo.BeanData[0];
        
        TagLibEditorData(Object sourceBean) {
            super(sourceBean);
        }
        
        public void setBeanData(JspInfo.BeanData[] beanData) {
            this.beanData = beanData;
        }

        public JspInfo.BeanData[] getBeanData() {
            return beanData;
        }

        void applyParsedData(JspInfo.TagLibraryData[] taglibs, FileSystem fs) {
            // pending information from inside the library
            TreeMap otherMap = new TreeMap();
            for (int i = 0; i < taglibs.length; i++) {
                String prefix = taglibs[i].getPrefix();
                otherMap.put(prefix, new TagLibData(taglibs[i], fs));
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

    /** Information about one tag library. */
    public static class TagLibData {

        private String prefix;
        private TagLibraryInfo tagLibraryInfo;

        /** Map of (String tagName, TagData tagData) */
        private TreeMap tagMap = new TreeMap();

        // pending - put tags in
        public TagLibData(JspInfo.TagLibraryData info, FileSystem fs) {
            this.prefix = info.getPrefix();
            tagLibraryInfo = null;

            // find the tag library            
            ContextDescriptor desc = new ContextDescriptor(fs);
            FileObject tagLibFile = desc.getResourceAsObject(info.getResolvedURI());
            if (tagLibFile != null) {
                try {
                    DataObject tagLib = (DataObject)DataObject.find(tagLibFile);
                    TagLibraryInfoSupport sup = TagLibraryInfoSupport.getTagLibraryInfoSupport(tagLib);
                    if (sup != null) {
                        tagLibraryInfo = sup.getTagLibraryInfo(prefix, info.getUnresolvedURI());
                    }
                }
                catch (DataObjectNotFoundException e) { /* ignore */ }
                catch (ClassCastException e) { /* ignore */ }
            }
            
            // now try to get the tags from tagLibraryInfo
            if (tagLibraryInfo != null) {
                TagInfo tagInfo[] = tagLibraryInfo.getTags();
                for (int i = 0; i < tagInfo.length; i++) {
                    String name = tagInfo[i].getTagName();
                    tagMap.put(name, new TagData(name, tagInfo[i].getBodyContent()));
                }
            }
        }

        boolean equalsColoringInformation(TagLibData other) {
            if (!prefix.equals(other.prefix))
                return false;
            // as the prefixes are ordered alphabetically, just compare the 
            // corresponding TagData in both arrays of tags
            TagData[] myTagData    = getTagData();
            TagData[] otherTagData = other.getTagData();
            if (myTagData.length != otherTagData.length)
                return false;
            for (int i = 0; i < myTagData.length; i++) {
                if (!myTagData[i].equalsColoringInformation(otherTagData[i]))
                    return false;
            }
            return true;
        }

        public String getPrefix() {
            return prefix;
        }

        public TagData[] getTagData() {
            return (TagData[])tagMap.values().toArray(new TagData[tagMap.size()]);
        }

        public TagData getTagData(String tagName) {
            return (TagData)tagMap.get(tagName);
        }
        
        public TagLibraryInfo getTagLibraryInfo() {
            return tagLibraryInfo;
        }
    }

    /** Information about one tag. */
    public static class TagData {

        private String tagName;
        private String bodyContent;
        //private String[] attributeNames = new String[0];

        public TagData(String tagName, String bodyContent) {
            this.tagName = tagName;
            this.bodyContent = bodyContent;
        }

        boolean equalsColoringInformation(TagData other) {
            return (tagName.equals(other.tagName) && bodyContent.equals(other.bodyContent));
        }

        public String getTagName() {
            return tagName;
        }

        public String getBodyContent() {
            return bodyContent;
        }

        /*public String[] getAttributes() {
            return attributeNames;
        }*/
    }
}
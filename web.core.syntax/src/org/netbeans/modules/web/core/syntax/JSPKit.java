/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.syntax;

import java.beans.*;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.*;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.ext.Completion;
import org.netbeans.editor.ext.ExtEditorUI;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.cookies.*;
import org.openide.util.WeakListeners;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.ext.CompletionJavaDoc;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.editor.ext.html.HTMLDrawLayerFactory;
import org.netbeans.editor.ext.java.JavaDrawLayerFactory;
import org.netbeans.editor.ext.html.HTMLSyntax;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.web.core.syntax.spi.JSPColoringData;
import org.netbeans.modules.web.core.syntax.spi.JspContextInfo;

/**
 * Editor kit implementation for JSP content type
 * @author Miloslav Metelka, Petr Jiricka, Yury Kamen
 * @versiob 1.5
 */
public class JSPKit extends NbEditorKit {

    public static final String JSP_MIME_TYPE = "text/x-jsp"; // NOI18N
    public static final String TAG_MIME_TYPE = "text/x-tag"; // NOI18N
    
    /** serialVersionUID */
    private static final long serialVersionUID = 8933974837050367142L;

    public static final boolean debug = false;

    /** Default constructor */
    public JSPKit() {
        super();
    }
    
    public String getContentType() {
        return JSP_MIME_TYPE;
    }

    /** Creates a new instance of the syntax coloring parser */
    public Syntax createSyntax(Document doc) {
        DataObject dobj = NbEditorUtilities.getDataObject(doc);
        FileObject fobj = (dobj != null) ? dobj.getPrimaryFile() : null;
        
        //String mimeType = NbEditorUtilities.getMimeType(doc);
        
        Syntax contentSyntax   = getSyntaxForLanguage(doc, JspUtils.getContentLanguage());
        Syntax scriptingSyntax = getSyntaxForLanguage(doc, JspUtils.getScriptingLanguage());
        final Jsp11Syntax newSyntax = new Jsp11Syntax(contentSyntax, scriptingSyntax);

        // tag library coloring data stuff
        JSPColoringData data = data = JspUtils.getJSPColoringData (doc, fobj); 
        // construct the listener
        PropertyChangeListener pList = new ColoringListener(doc, data, newSyntax);
        // attach the listener
        // PENDING - listen on the language
        //jspdo.addPropertyChangeListener(WeakListeners.propertyChange(pList, jspdo));
        if (data != null) {
            data.addPropertyChangeListener(WeakListeners.propertyChange(pList, data));
        }
        return newSyntax;
    }

    protected Action[] createActions() {
        Action[] javaActions = new Action[] {
                                   new JavaKit.JavaDocShowAction()                                   
                               };
        return TextAction.augmentList(super.createActions(), javaActions);
    }

    private static class ColoringListener implements PropertyChangeListener {
        private Document doc;
        private Object parsedDataRef; // hold a reference to the data we are listening on 
                                      // so it does not get garbage collected
        private Jsp11Syntax syntax;                              
        //private JspDataObject jspdo;

        public ColoringListener(Document doc, JSPColoringData data, Jsp11Syntax syntax) {
            this.doc = doc;
            // we must keep the reference to the structure we are listening on so it's not gc'ed
            this.parsedDataRef = data;
            this.syntax = syntax;
            // syntax must keep a reference to this object so it's not gc'ed
            syntax.listenerReference = this;
            syntax.data = data;
            /* jspdo = (JspDataObject)NbEditorUtilities.getDataObject(doc);*/
        }
        
        private void recolor() {
            if (doc instanceof BaseDocument) 
                ((BaseDocument)doc).invalidateSyntaxMarks();
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (syntax == null)
                return;
            if (syntax.listenerReference != this) {
                syntax = null; // should help garbage collection
                return;
            }
           /* if (JspDataObject.PROP_CONTENT_LANGUAGE.equals(evt.getPropertyName())) {
                syntax.setContentSyntax(JSPKit.getSyntaxForLanguage(doc, jspdo.getContentLanguage()));
                recolor();
            }
            if (JspDataObject.PROP_SCRIPTING_LANGUAGE.equals(evt.getPropertyName())) {
                syntax.setScriptingSyntax(JSPKit.getSyntaxForLanguage(doc, jspdo.getScriptingLanguage()));
                recolor();
            }*/
            if (JSPColoringData.PROP_COLORING_CHANGE.equals(evt.getPropertyName())) {
                recolor();
            }
        }
    }
    
    public static Syntax getSyntaxForLanguage(Document doc, String language) {
        EditorKit kit = JEditorPane.createEditorKitForContentType(language);
        if (kit instanceof BaseKit) {
            return ((BaseKit)kit).createSyntax(doc);
        }
        else {
            return new HTMLSyntax();
        }
    }

    /** Create syntax support */
    public SyntaxSupport createSyntaxSupport(BaseDocument doc) {
        DataObject dobj = NbEditorUtilities.getDataObject(doc);
        if (dobj != null) {
            return new JspSyntaxSupport(doc, 
                JspContextInfo.getContextInfo().getCachedOpenInfo(doc, dobj.getPrimaryFile(), false).isXmlSyntax());
        }
        return new JspSyntaxSupport(doc, false);
        
    }

    /** Returns completion for given language (MIME type).
     * Note that JspJavaCompletion competion is returned instead of 
     * NbJavaCompletion to add specific stuff related to Java class generated 
     * from JSP.
     */
    private static Completion getCompletionForLanguage(
        ExtEditorUI extEditorUI, String language) {
        Completion compl = null;
        if (JavaKit.JAVA_MIME_TYPE.equals (language)) {
            compl = new JspJavaCompletion (extEditorUI);
        }
        else {
            EditorKit kit = JEditorPane.createEditorKitForContentType(language);
            if (kit instanceof ExtKit)
                compl = ((ExtKit)kit).createCompletion(extEditorUI);
        }
        return compl;
    }
    
    public Completion createCompletion(ExtEditorUI extEditorUI) {
        BaseDocument doc = extEditorUI.getDocument();
        FileObject fobj = null;
        if (doc != null){
            DataObject dobj = NbEditorUtilities.getDataObject(doc);
            fobj = (dobj != null) ? NbEditorUtilities.getDataObject(doc).getPrimaryFile(): null;
        }
        
        String mimeType = NbEditorUtilities.getMimeType(doc);
        Completion contentCompletion = (!(mimeType.equals(JSP_MIME_TYPE) || mimeType.equals(TAG_MIME_TYPE))) ? 
            null :
            getCompletionForLanguage(extEditorUI, JspUtils.getContentLanguage());
        Completion scriptingCompletion = (!(mimeType.equals(JSP_MIME_TYPE) || mimeType.equals(TAG_MIME_TYPE))) ? 
            null : 
            getCompletionForLanguage(extEditorUI, JspUtils.getScriptingLanguage());
        final JspCompletion completion = 
            new JspCompletion(extEditorUI, contentCompletion, scriptingCompletion);
        return completion;
        /*final JspDataObject jspdo = (dobj instanceof JspDataObject) ? (JspDataObject)dobj : null;
        Completion contentCompletion = (jspdo == null) ? 
            null :
            getCompletionForLanguage(extEditorUI, jspdo.getContentLanguage());
        Completion scriptingCompletion = (jspdo == null) ? 
            null : 
            getCompletionForLanguage(extEditorUI, jspdo.getScriptingLanguage());
            
        final JspCompletion completion = 
            new JspCompletion(extEditorUI, contentCompletion, scriptingCompletion);
        return completion;*/
        
    }

    public CompletionJavaDoc createCompletionJavaDoc(ExtEditorUI extEditorUI) {
        return new JspCompletionJavaDoc (extEditorUI);
    }
    
    protected void initDocument(BaseDocument doc) {
        doc.addLayer(new JavaDrawLayerFactory.JavaLayer(),
                JavaDrawLayerFactory.JAVA_LAYER_VISIBILITY);
        doc.addDocumentListener(new JavaDrawLayerFactory.LParenWatcher());
	doc.addLayer(new ELDrawLayerFactory.ELLayer(),
                ELDrawLayerFactory.EL_LAYER_VISIBILITY);
        doc.addDocumentListener(new ELDrawLayerFactory.LParenWatcher());
	doc.addDocumentListener(new HTMLDrawLayerFactory.TagParenWatcher());
    }
    
    public Formatter createFormatter() {        
        return new JspFormatter(this.getClass());	
    }
    
}

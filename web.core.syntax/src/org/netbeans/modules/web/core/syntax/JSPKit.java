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

package org.netbeans.modules.web.core.syntax;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Vector;
import java.beans.*;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.*;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.ext.Completion;
import org.netbeans.editor.ext.ExtEditorUI;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.TopManager;
import org.openide.cookies.*;
import org.openide.util.WeakListener;
import org.openide.debugger.Breakpoint;
import org.openide.debugger.Debugger;
import org.openide.debugger.DebuggerNotFoundException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;

import org.netbeans.modules.web.core.jsploader.JspDataObject;
import org.netbeans.modules.web.core.jsploader.TagLibParseSupport;
import org.netbeans.modules.web.core.FeatureFactory;

import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.ext.CompletionJavaDoc;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.editor.ext.java.JavaCompletion;
import org.netbeans.editor.ext.java.JavaSyntax;
import org.netbeans.editor.ext.java.JavaDrawLayerFactory;
import org.netbeans.editor.ext.html.HTMLSyntax;
import org.netbeans.editor.ext.plain.PlainSyntax;
import org.netbeans.modules.editor.html.HTMLKit;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.editor.NbEditorUtilities;


/**
 * Editor kit implementation for JSP content type
 * @author Miloslav Metelka, Petr Jiricka, Yury Kamen
 * @versiob 1.5
 */
public class JSPKit extends NbEditorKit {

    public static final String JSP_MIME_TYPE = "text/x-jsp"; // NOI18N
    
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
        final JspDataObject jspdo = (dobj instanceof JspDataObject) ? (JspDataObject)dobj : null;
        Syntax contentSyntax = (jspdo == null) ? 
            new HTMLSyntax() : 
            getSyntaxForLanguage(doc, jspdo.getContentLanguage());
        Syntax scriptingSyntax = (jspdo == null) ? 
            new JavaSyntax() : 
            getSyntaxForLanguage(doc, jspdo.getScriptingLanguage());
        final Jsp11Syntax newSyntax = new Jsp11Syntax(contentSyntax, scriptingSyntax);
        if (jspdo != null) {
            // get the tag library data
            TagLibParseSupport parseSupport = (TagLibParseSupport)jspdo.getCookie(TagLibParseSupport.class);
            TagLibParseSupport.TagLibEditorData data = null;
            if (parseSupport != null) {
                data = parseSupport.getTagLibEditorData();
            }
            // construct the listener
            PropertyChangeListener pList = new ColoringListener(doc, data, newSyntax);
            // attach the listener
            jspdo.addPropertyChangeListener(WeakListener.propertyChange(pList, jspdo));
            if (data != null) {
                data.addPropertyChangeListener(WeakListener.propertyChange(pList, data));
            }
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
        private JspDataObject jspdo;

        public ColoringListener(Document doc, TagLibParseSupport.TagLibEditorData data, Jsp11Syntax syntax) {
            this.doc = doc;
            // we must keep the reference to the structure we are listening on so it's not gc'ed
            this.parsedDataRef = data;
            this.syntax = syntax;
            // syntax must keep a reference to this object so it's not gc'ed
            syntax.listenerReference = this;
            syntax.data = data;
            jspdo = (JspDataObject)NbEditorUtilities.getDataObject(doc);
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
            if (JspDataObject.PROP_CONTENT_LANGUAGE.equals(evt.getPropertyName())) {
                syntax.setContentSyntax(JSPKit.getSyntaxForLanguage(doc, jspdo.getContentLanguage()));
                recolor();
            }
            if (JspDataObject.PROP_SCRIPTING_LANGUAGE.equals(evt.getPropertyName())) {
                syntax.setScriptingSyntax(JSPKit.getSyntaxForLanguage(doc, jspdo.getScriptingLanguage()));
                recolor();
            }
            if (TagLibParseSupport.TagLibEditorData.PROP_COLORING_CHANGE.equals(evt.getPropertyName())) {
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
        return new JspSyntaxSupport(doc);
    }

    public static Completion getCompletionForLanguage(
        ExtEditorUI extEditorUI, String language) {
        EditorKit kit = JEditorPane.createEditorKitForContentType(language);
        if (kit instanceof ExtKit)
            return ((ExtKit)kit).createCompletion(extEditorUI);
        else       
            return null;
/*        if ("text/html".equals(language))
            return null;
            //return new HTMLKit().createCompletion(extEditorUI);
        if ("text/x-java".equals(language))
            return new JavaKit().createCompletion(extEditorUI);
        return null;*/
    }
    
    public Completion createCompletion(ExtEditorUI extEditorUI) {
        BaseDocument doc = extEditorUI.getDocument();
        DataObject dobj = (doc == null) ? null : NbEditorUtilities.getDataObject(doc);
        final JspDataObject jspdo = (dobj instanceof JspDataObject) ? (JspDataObject)dobj : null;
        Completion contentCompletion = (jspdo == null) ? 
            null :
            getCompletionForLanguage(extEditorUI, jspdo.getContentLanguage());
        Completion scriptingCompletion = (jspdo == null) ? 
            null : 
            getCompletionForLanguage(extEditorUI, jspdo.getScriptingLanguage());
            
        // customized JavaCompletion
        if ((scriptingCompletion instanceof JavaCompletion) &&
            jspdo.getScriptingLanguage().equals (JavaKit.JAVA_MIME_TYPE)) {
            scriptingCompletion = new JspJavaCompletion (extEditorUI);
        }
        final JspCompletion completion = 
            new JspCompletion(extEditorUI, contentCompletion, scriptingCompletion);
        return completion;
    }

    public CompletionJavaDoc createCompletionJavaDoc(ExtEditorUI extEditorUI) {
        return new JspCompletionJavaDoc (extEditorUI);
    }
    
    protected void initDocument(BaseDocument doc) {
        doc.addLayer(new JavaDrawLayerFactory.JavaLayer(),
                JavaDrawLayerFactory.JAVA_LAYER_VISIBILITY);
        doc.addDocumentListener(new JavaDrawLayerFactory.LParenWatcher());
    }

}






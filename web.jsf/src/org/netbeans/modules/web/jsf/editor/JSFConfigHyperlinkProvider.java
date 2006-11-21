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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.web.jsf.editor;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Hashtable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtSyntaxSupport;
//import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
//import org.netbeans.modules.editor.java.JMIUtils;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JSFConfigDataObject;
//import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.openide.ErrorManager;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author petr
 */
public class JSFConfigHyperlinkProvider implements HyperlinkProvider {
    
    static private boolean debug = false;
    private static Hashtable hyperlinkTable;
    
    private final int JAVA_CLASS = 0;
    private final int RESOURCE_PATH = 2;
    
    {
        hyperlinkTable = new Hashtable();
        hyperlinkTable.put("managed-bean-class", new Integer(JAVA_CLASS));      //NOI18N
        hyperlinkTable.put("component-class", new Integer(JAVA_CLASS));      //NOI18N
        hyperlinkTable.put("renderer-class", new Integer(JAVA_CLASS));      //NOI18N
        hyperlinkTable.put("property-class", new Integer(JAVA_CLASS));      //NOI18N
        hyperlinkTable.put("validator-class", new Integer(JAVA_CLASS));      //NOI18N
        hyperlinkTable.put("attribute-class", new Integer(JAVA_CLASS));      //NOI18N
        hyperlinkTable.put("message-bundle", new Integer(JAVA_CLASS));      //NOI18N
        hyperlinkTable.put("action-listener", new Integer(JAVA_CLASS));      //NOI18N
        hyperlinkTable.put("application-factory", new Integer(JAVA_CLASS));      //NOI18N
        hyperlinkTable.put("converter-class", new Integer(JAVA_CLASS));      //NOI18N
        hyperlinkTable.put("converter-for-class", new Integer(JAVA_CLASS));      //NOI18N
        hyperlinkTable.put("faces-context-factory", new Integer(JAVA_CLASS));      //NOI18N
        hyperlinkTable.put("key-class", new Integer(JAVA_CLASS));      //NOI18N
        hyperlinkTable.put("lifecycle-factory", new Integer(JAVA_CLASS));      //NOI18N
        hyperlinkTable.put("navigation-handler", new Integer(JAVA_CLASS));      //NOI18N
        hyperlinkTable.put("phase-listener", new Integer(JAVA_CLASS));      //NOI18N
        hyperlinkTable.put("property-resolver", new Integer(JAVA_CLASS));      //NOI18N
        hyperlinkTable.put("referenced-bean-class", new Integer(JAVA_CLASS));      //NOI18N
        hyperlinkTable.put("render-kit-class", new Integer(JAVA_CLASS));      //NOI18N
        hyperlinkTable.put("render-kit-factory", new Integer(JAVA_CLASS));      //NOI18N
        hyperlinkTable.put("value-class", new Integer(JAVA_CLASS));      //NOI18N
        hyperlinkTable.put("variable-resolver", new Integer(JAVA_CLASS));      //NOI18N
        hyperlinkTable.put("from-view-id", new Integer(RESOURCE_PATH));   //NOI18N
        hyperlinkTable.put("to-view-id", new Integer(RESOURCE_PATH));     //NOI18N
    }
    
    private int valueOffset;
    private String [] ev = null;
    /** Creates a new instance of StrutsHyperlinkProvider */
    public JSFConfigHyperlinkProvider() {
    }

    public int[] getHyperlinkSpan(javax.swing.text.Document doc, int offset) {
        if (debug) debug (":: getHyperlinkSpan");
        if (ev != null){
            return new int []{valueOffset, valueOffset + ev[1].length() -1};
        }
        return null;
    }

    public boolean isHyperlinkPoint(javax.swing.text.Document doc, int offset) {
        if (debug) debug (":: isHyperlinkSpan - offset: " + offset); //NOI18N
        
        // PENDING - this check should be removed, when 
        // the issue #61704 is solved.
        DataObject dObject = NbEditorUtilities.getDataObject(doc);
        if (! (dObject instanceof JSFConfigDataObject))
            return false;
        ev = getElementValue(doc, offset);
        if (ev != null){ 
            if (hyperlinkTable.get(ev[0])!= null)
                return true;
        }
        return false;
    }

    public void performClickAction(javax.swing.text.Document doc, int offset) {
        if (debug) debug (":: performClickAction");
        
        if (hyperlinkTable.get(ev[0])!= null){
            int type = ((Integer)hyperlinkTable.get(ev[0])).intValue();
            switch (type){
                case JAVA_CLASS: findJavaClass(ev[1], doc); break;
                case RESOURCE_PATH: findResourcePath(ev[1], (BaseDocument)doc);break;
            }
        }
    }
    
    static void debug(String message){
        System.out.println("JSFConfigHyperlinkProvider: " + message); //NoI18N
    }
    
    private String[] getElementValue(javax.swing.text.Document doc, int offset){
        String tag = null;
        String value = null;
        
        try {
            BaseDocument bdoc = (BaseDocument) doc;
            JTextComponent target = Utilities.getFocusedComponent();
            
            if (target == null || target.getDocument() != bdoc)
                return null;
            ExtSyntaxSupport sup = (ExtSyntaxSupport)bdoc.getSyntaxSupport();
            TokenItem token = sup.getTokenChain(offset, offset+1);
            if (token == null || token.getTokenID().getNumericID() != JSFEditorUtilities.XML_TEXT)
                return null;
            value = token.getImage();
            if (value != null){
                String original = value;
                value = value.trim();
                valueOffset = token.getOffset()+(original.indexOf(value));
            }
            // Find element
            // 4 - tag
            while(token != null 
                    && !(token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT
                    && !token.getImage().equals(">")))
                token = token.getPrevious();
            if (token == null)
                return null;
            tag = token.getImage().substring(1);
            if (debug) debug ("element: " + tag );   // NOI18N
            if (debug) debug ("value: " + value );  //NOI18N
            return new String[]{tag, value};
            
        } 
        catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        }
        return null;
    }
    
    private boolean isWhiteChar(char c){
        return (c == ' ' || c == '\n' || c == '\t' || c == '\r');
    }
    
    private void findJavaClass(final String fqn, javax.swing.text.Document doc){
        FileObject fo = NbEditorUtilities.getFileObject(doc);
        if (fo != null){
            WebModule wm = WebModule.getWebModule(fo);
            if (wm != null){
                try {
                    final ClasspathInfo cpi = ClasspathInfo.create(wm.getDocumentBase());
                    JavaSource js = JavaSource.create(cpi, Collections.EMPTY_LIST);
                    
                    js.runUserActionTask(new AbstractTask<CompilationController>() {
                        
                        public void run(CompilationController cc) throws Exception {
                            Elements elements = cc.getElements();
                            TypeElement element = elements.getTypeElement(fqn.trim());
                            if (element != null) {
                                if (!UiUtils.open(cpi, element)){
                                    String key = "goto_source_not_found"; // NOI18N
                                    String msg = NbBundle.getBundle(JSFConfigHyperlinkProvider.class).getString(key);
                                    org.openide.awt.StatusDisplayer.getDefault().setStatusText(MessageFormat.format(msg, new Object [] { fqn } ));
                                    
                                }
                            }
                        }
                    }, false);
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                            ex.getMessage(), ex);
                };
            }
        }
    }
   
    
    
    private void findResourcePath(String path, BaseDocument doc){
        //normalize path
        int qmIndex = path.indexOf('?');
        if(qmIndex == 0) {
            return ; //filter sg. like '?id=val'
        }
        if(qmIndex != -1) {
            //trim the path
            path = path.substring(0, qmIndex);
        }
        
        WebModule wm = WebModule.getWebModule(NbEditorUtilities.getFileObject(doc));
        if (wm != null){
            FileObject docBase= wm.getDocumentBase();
            FileObject fo = docBase.getFileObject(path);
            if (fo != null)
                openInEditor(fo);
        }
    }
    
    
    
    private void openInEditor(FileObject fObj){
        if (fObj != null){
            DataObject dobj = null;
            try{
                dobj = DataObject.find(fObj);
            }
            catch (DataObjectNotFoundException e){
               ErrorManager.getDefault().notify(e);
               return; 
            }
            if (dobj != null){
                Node.Cookie cookie = dobj.getCookie(OpenCookie.class);
                if (cookie != null)
                    ((OpenCookie)cookie).open();
            }
        }
    }
}

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.syntax;


import java.text.MessageFormat;
import java.util.List;
import java.util.StringTokenizer;
import javax.servlet.jsp.tagext.TagFileInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.java.JavaTokenContext;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.ErrorManager;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public class JSPHyperlinkProvider implements HyperlinkProvider {
    
    /** Creates a new instance of JSPHyperlinkProvider */
    public JSPHyperlinkProvider() {
    }
    
    /**
     * Should determine whether there should be a hyperlink on the given offset
     * in the given document. May be called any number of times for given parameters.
     * <br>
     * This method is called from event dispatch thread.
     * It should run very fast as it is called very often.
     *
     * @param doc document on which to operate.
     * @param offset &gt;=0 offset to test (it generally should be offset &lt; doc.getLength(), but
     *               the implementations should not depend on it)
     * @return true if the provided offset should be in a hyperlink
     *         false otherwise
     */
    public boolean isHyperlinkPoint(Document doc, int offset){
        if (!(doc instanceof BaseDocument))
            return false;
        
        try {
            BaseDocument bdoc = (BaseDocument) doc;
            JTextComponent target = Utilities.getFocusedComponent();
            
            if (target == null || target.getDocument() != bdoc)
                return false;
            
            SyntaxSupport sup = bdoc.getSyntaxSupport();
            JspSyntaxSupport jspSup = (JspSyntaxSupport)sup.get(JspSyntaxSupport.class);
            
            TokenItem token = jspSup.getTokenChain(offset, offset+1);
            if (token == null) return false;
            TokenID tokenID = token.getTokenID();
            if (tokenID == null) return false;
            
            // is it the static include?
            if (tokenID.getCategory() != null
                    && tokenID.getCategory().getNumericID() == JspDirectiveTokenContext.TAG_CATEGORY_ID
                    && tokenID.getNumericID() == JspDirectiveTokenContext.ATTR_VALUE_ID){
                while (token.getTokenID().getCategory().getNumericID() == JspDirectiveTokenContext.TAG_CATEGORY_ID
                       && token.getTokenID().getNumericID() != JspDirectiveTokenContext.ATTRIBUTE_ID){
                    token = token.getPrevious();
                }
                
                if (token.getTokenID().getCategory().getNumericID() == JspDirectiveTokenContext.TAG_CATEGORY_ID
                    && token.getTokenID().getNumericID() == JspDirectiveTokenContext.ATTRIBUTE_ID
                    && "file".equals(token.getImage().trim())) //NOI18N
                    return true;
                
            }
            
            // is it a forward or dynamic include?
            if (tokenID.getNumericID() == JspTagTokenContext.ATTR_VALUE_ID){
                while (token.getTokenID().getNumericID() != JspTagTokenContext.ATTRIBUTE_ID
                       && token.getTokenID().getNumericID() != JspTagTokenContext.TAG_ID
                       && !(token.getTokenID().getNumericID() == JspTagTokenContext.SYMBOL_ID && token.getImage().charAt(0) == '<'))
                    token = token.getPrevious();
                
                if (token.getTokenID().getNumericID() == JspDirectiveTokenContext.ATTRIBUTE_ID
                    && "page".equals(token.getImage().trim())){
                    return true;
                }
            }
            
            // is the a reachable tag file?
            return (getTagFile(token, jspSup) != null);
            
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
            return false;
        }
    }
    
    /**
     * Should determine the span of hyperlink on given offset. Generally, if
     * isHyperlinkPoint returns true for a given parameters, this class should
     * return a valid span, but it is not strictly required.
     * <br>
     * This method is called from event dispatch thread.
     * This method should run very fast as it is called very often.
     *
     * @param doc document on which to operate.
     * @param offset &gt;=0 offset to test (it generally should be offset &lt; doc.getLength(), but
     *               the implementations should not depend on it)
     * @return a two member array which contains starting and ending offset of a hyperlink
     *         that should be on a given offset
     */
    public int[] getHyperlinkSpan(Document doc, int offset){
        if (!(doc instanceof BaseDocument))
            return null;
        
        try {
            BaseDocument bdoc = (BaseDocument) doc;
            JTextComponent target = Utilities.getFocusedComponent();
            
            if (target == null || target.getDocument() != bdoc)
                return null;
            
            SyntaxSupport sup = bdoc.getSyntaxSupport();
            JspSyntaxSupport jspSup = (JspSyntaxSupport)sup.get(JspSyntaxSupport.class);
            
            TokenItem token = jspSup.getTokenChain(offset, offset+1);
            if (token == null) return null;
            TokenID tokenID = token.getTokenID();
            if (tokenID == null) return null;
            
            if (getTagFile(token, jspSup) != null){
                // a reachable tag file. 
                return new int[]{token.getOffset(), token.getOffset() + token.getImage().length()-1}; 
            }
            else{
                // dynamic or static include, forward.
                // remove all white space between the value. 
                int tokenOffset = 0;
                String image = token.getImage();
                while (tokenOffset < image.length() && bdoc.isWhitespace(image.charAt(tokenOffset)))
                    tokenOffset++;
                tokenOffset++;
                return new int[]{token.getOffset()+tokenOffset, token.getOffset() + token.getImage().length()-2};
            }
        }
        catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
            return null;
        }
    }
    
    /**
     * The implementor should perform an action
     * corresponding to clicking on the hyperlink on the given offset. The
     * nature of the action is given by the nature of given hyperlink, but
     * generally should open some resource or move cursor
     * to certain place in the current document.
     *
     * @param doc document on which to operate.
     * @param offset &gt;=0 offset to test (it generally should be offset &lt; doc.getLength(), but
     *               the implementations should not depend on it)
     */
    public void performClickAction(Document doc, int offset){
        TokenItem token = null;
        try {
            BaseDocument bdoc = (BaseDocument) doc;
            JTextComponent target = Utilities.getFocusedComponent();
            
            if (target == null || target.getDocument() != bdoc)
                return;
            
            SyntaxSupport sup = bdoc.getSyntaxSupport();
            JspSyntaxSupport jspSup = (JspSyntaxSupport)sup.get(JspSyntaxSupport.class);
            
            token = jspSup.getTokenChain(offset, offset+1);
            
            if (token == null) return;
            
            FileObject fObj = getTagFile(token, jspSup);
            if ( fObj != null)
                openInEditor(fObj);
            else {
                String path = token.getImage();
                path = path.substring(path.indexOf('"') +1);
                path = path.substring(0, path.indexOf('"'));

                fObj = getFileObject(doc, path);
                if (fObj != null) {
                    openInEditor(fObj);
                }
                else {
                    // when the file was not found.
                    String msg = NbBundle.getBundle(JSPHyperlinkProvider.class).getString("LBL_file_not_found"); //NOI18N
                    org.openide.awt.StatusDisplayer.getDefault().setStatusText(MessageFormat.format(msg, new Object [] { path } ));
                }

            }
        }
        catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    
    private FileObject getFileObject (Document doc, String path){
        //Find out the file object from the document
        DataObject dobj = NbEditorUtilities.getDataObject(doc);
        FileObject fobj = (dobj != null) ? NbEditorUtilities.getDataObject(doc).getPrimaryFile(): null;
            
        if (fobj != null){
            return getFileObject (doc, fobj, path);
        }
        return null;
    }
    /**
     * This method finds the file object according the path or null if the file object doesn't exist.
     * @param doc Document, where user perform the hyperlink action.
     * @param file 
     * @param path 
     * @return 
     */
    private FileObject getFileObject (Document doc,FileObject file, String path){
        if (path == null)       // it the path is null -> don't find it
            return file;
        path = path.trim();
        FileObject find = file;
        if (!file.isFolder())  // if the file is not folder, get the parent
                find = file.getParent();
        
        if (path.charAt(0) == '/'){  // is the absolute path in the web module?
            find = JspUtils.guessWebModuleRoot(doc, file);  // find the folder, where the absolut path starts
            if (find == null)       
                return null;            // we are not able to find out the webmodule root

            path = path.substring(1);   // if we have folder, where the webmodule starts, the path can me relative to this folder
        }
        // find relative path to the folder
        StringTokenizer st = new StringTokenizer(path, "/");
        String token;
        while (find != null && st.hasMoreTokens()) {
            token = st.nextToken();
            if ("..".equals(token))     // move to parent
                find = find.getParent();
            else if (!".".equals(token))        // if there is . - don't move
                find = find.getFileObject(token);
        }
        return find;
        
    }
    
    private String getTagName(String tagwithprefix){
        int index = tagwithprefix.indexOf(':');
        if (index > 0)
            return tagwithprefix.substring(index+1);
        else
            return tagwithprefix;
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
    
    private FileObject getTagFile(TokenItem token, JspSyntaxSupport jspSup){
        if (token != null 
                    && token.getTokenID().getCategory() == null
                    && token.getTokenID().getNumericID() == JspTagTokenContext.TAG_ID){
            String image = token.getImage().trim();
            if (!image.startsWith("jsp:")){  // NOI18N
                List l = jspSup.getTags(image);
                if (l.size() == 1){
                    TagLibraryInfo libInfo = ((TagInfo)l.get(0)).getTagLibrary();
                    if (libInfo != null){
                        TagFileInfo fileInfo = libInfo.getTagFile(getTagName(image));
                        if (fileInfo != null)
                            return getFileObject (jspSup.getDocument(), fileInfo.getPath());
                    }
                }
            }
        }
        return null;
    }
}

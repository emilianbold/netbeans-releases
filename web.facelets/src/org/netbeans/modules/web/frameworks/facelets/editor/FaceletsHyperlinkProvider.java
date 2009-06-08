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

package org.netbeans.modules.web.frameworks.facelets.editor;

import java.util.ArrayList;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.frameworks.facelets.FaceletsUtils;
import org.openide.ErrorManager;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;

/**
 *
 * @author Petr Pisl
 */
public class FaceletsHyperlinkProvider implements HyperlinkProvider {
    //These constants are taken from JspTagTokenContext
    private static final int TAG_ID = 3;
    private static final int ATTRIBUTE_ID = 7;
    private static final int ATTR_VALUE_ID = 8;
    
    private TAVItem tav = null;
    private int valueOffset;
    
    private static ArrayList hyperlinkTable;
    {
        hyperlinkTable = new ArrayList();
        hyperlinkTable.add("composition&template");      //NOI18N
        //hyperlinkTable.add("define&name");      //NOI18N
        hyperlinkTable.add("include&src");      //NOI18N
    }
    
    /** Creates a new instance of FaceletsHyperlinkProvider */
    public FaceletsHyperlinkProvider() {
    }
    
    public boolean isHyperlinkPoint(Document doc, int offset) {
        tav = getTagAttributeValue(doc, offset);
        if (tav != null && hyperlinkTable.contains(tav.tag + "&" +tav.attribute))
            return true;
        return false;
    }
    
    public int[] getHyperlinkSpan(Document doc, int offset) {
        if (tav != null)
            return new int []{valueOffset, valueOffset + tav.value.length() -1};
        return null;
    }
    
    public void performClickAction(Document doc, int offset) {
        if (tav != null ){
            openResource(tav.value, (BaseDocument)doc);
        }
    }
    
    private TAVItem getTagAttributeValue(javax.swing.text.Document doc, int offset){
        String tag = null;
        String attribute = null;
        String value = null;
        
        try {
            BaseDocument bdoc = (BaseDocument) doc;
            JTextComponent target = Utilities.getFocusedComponent();
            
            if (target == null || target.getDocument() != bdoc)
                return null;
            ExtSyntaxSupport sup = (ExtSyntaxSupport)bdoc.getSyntaxSupport();
            TokenItem token = sup.getTokenChain(offset, offset+1);
            if (token == null || token.getTokenID().getNumericID() != ATTR_VALUE_ID)
                return null;
            value = token.getImage();
            if (value != null && value.length() > 2){
                String orig = value;
                value = value.substring(1);
                value = value.substring(0, value.length()-1);
                valueOffset = token.getOffset()+1;
            }
            
            // Find attribute
            while (token != null
                    && token.getTokenID().getNumericID() != ATTRIBUTE_ID
                    && token.getTokenID().getNumericID() != TAG_ID)
                token = token.getPrevious();
            if (token == null || token.getTokenID().getNumericID() != ATTRIBUTE_ID || "=".equals(token.getImage()))
                return null;
            attribute = token.getImage();
            
            while (token != null
                    && token.getTokenID().getNumericID() != TAG_ID)
                token = token.getPrevious();
            if (token == null)
                return null;
            tag = token.getImage();
            int index = tag.indexOf(':');
            if ( index > -1)
                tag = tag.substring(index + 1);
            
            return new TAVItem(tag, attribute, value);
            
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        }
        return null;
    }
    
    private void openResource(String path, BaseDocument doc){
        FileObject fileObject = NbEditorUtilities.getFileObject(doc);
        FileObject target = FaceletsUtils.getRelativeFO(fileObject, path);
        if (target != null)
            openInEditor(target);
    }
    
    private void openInEditor(FileObject fObj){
        if (fObj != null){
            DataObject dobj = null;
            try{
                dobj = DataObject.find(fObj);
            } catch (DataObjectNotFoundException e){
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
    
    private static class TAVItem {
        String tag;
        String attribute;
        String value;
        
        TAVItem(String tag, String attribute, String value){
            this.tag = tag;
            this.attribute = attribute;
            this.value = value;
        }
    }
}

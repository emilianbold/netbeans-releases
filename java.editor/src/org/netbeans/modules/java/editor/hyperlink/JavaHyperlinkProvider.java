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

package org.netbeans.modules.java.editor.hyperlink;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.java.JavaTokenContext;
import org.netbeans.jmi.javamodel.ClassDefinition;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.openide.ErrorManager;
import org.netbeans.modules.editor.java.*;


/**
 * Implementation of the hyperlink provider for java language.
 * <br>
 * The hyperlinks are constructed for identifiers.
 * <br>
 * The click action corresponds to performing the goto-declaration action.
 *
 * @author Jan Lahoda
 */
public final class JavaHyperlinkProvider implements HyperlinkProvider {
    
    /** Creates a new instance of JavaHyperlinkProvider */
    public JavaHyperlinkProvider() {
    }

    public void performClickAction(Document originalDoc, int offset) {
        if (!(originalDoc instanceof BaseDocument))
            return ;
        
        BaseDocument doc = (BaseDocument) originalDoc;
        JTextComponent target = Utilities.getFocusedComponent();
        
        if (target.getDocument() != doc)
            return ;
        
        SyntaxSupport sup = doc.getSyntaxSupport();
        NbJavaJMISyntaxSupport nbJavaSup = (NbJavaJMISyntaxSupport)sup.get(NbJavaJMISyntaxSupport.class);
        
        JMIUtils jmiUtils = JMIUtils.get(doc);
        
        
        Object item = null;
        String itemDesc = null;
        jmiUtils.beginTrans(false);
        try {
            item = jmiUtils.findItemAtCaretPos(target);
            if (item instanceof NbJMIResultItem.VarResultItem) {
                int pos = nbJavaSup.findLocalDeclarationPosition(((NbJMIResultItem.VarResultItem)item).getItemText(), target.getCaretPosition());
                target.setCaretPosition(pos);
            } else {
                if (item instanceof ClassDefinition)
                    item = JMIUtils.getSourceElementIfExists((ClassDefinition)item);
                itemDesc = nbJavaSup.openSource(item, true);
            }
        } finally {
            jmiUtils.endTrans(false);
        }
    }

    public boolean isHyperlinkPoint(Document doc, int offset) {
        if (!(doc instanceof BaseDocument))
            return false;
        
        try {
            BaseDocument bdoc = (BaseDocument) doc;
            JTextComponent target = Utilities.getFocusedComponent();
            
            if (target.getDocument() != bdoc)
                return false;
            
            SyntaxSupport sup = bdoc.getSyntaxSupport();
            NbJavaJMISyntaxSupport nbJavaSup = (NbJavaJMISyntaxSupport)sup.get(NbJavaJMISyntaxSupport.class);
            
            TokenID token = nbJavaSup.getTokenID(offset);
            
            if (token == JavaTokenContext.IDENTIFIER)
                return true;
            
            return false;
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
            return false;
        }
    }

    public int[] getHyperlinkSpan(Document doc, int offset) {
        if (!(doc instanceof BaseDocument))
            return null;
        
        try {
            if (isHyperlinkPoint(doc, offset))
                return org.netbeans.editor.Utilities.getIdentifierBlock((BaseDocument) doc, offset);
            else
                return null;
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
            return null;
        }
    }
    
}

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

import java.awt.Toolkit;
import java.text.MessageFormat;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.JumpList;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.java.JavaTokenContext;
import org.netbeans.jmi.javamodel.ClassDefinition;
import org.netbeans.jmi.javamodel.JavaPackage;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.openide.ErrorManager;
import org.netbeans.modules.editor.java.*;
import org.openide.util.NbBundle;


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
 
    private static final int TOKEN_LIMIT = 100;
    
    private static MessageFormat mf = null;
    
    private static synchronized MessageFormat getCannotOpenElementMF() {
        if (mf == null) {
            mf = new MessageFormat(NbBundle.getBundle(JavaHyperlinkProvider.class).getString("cannot-open-element"));
        }
        
        return mf;
    }
    
    /** Creates a new instance of JavaHyperlinkProvider */
    public JavaHyperlinkProvider() {
    }

    private String findName(BaseDocument doc, int offset) {
        String name = "";
        SyntaxSupport sup = doc.getSyntaxSupport();
        NbJavaJMISyntaxSupport nbJavaSup = (NbJavaJMISyntaxSupport)sup.get(NbJavaJMISyntaxSupport.class);
        boolean wasIdentifier = false;
        int tokenLimitCounter = 0;
        
        try {
            while (tokenLimitCounter < TOKEN_LIMIT) {
                TokenID token = nbJavaSup.getTokenID(offset);
                
                tokenLimitCounter++;
                
                if (token == JavaTokenContext.IDENTIFIER && !wasIdentifier) {
                    int[] span = org.netbeans.editor.Utilities.getIdentifierBlock(doc, offset);
                    
                    name = doc.getText(span) + name;
                    offset = span[0] - 1;
                    
                    wasIdentifier = true;
                } else {
                    if (token == JavaTokenContext.DOT) {
                        offset--;
                        name = "." + name; // NOI18N
                        wasIdentifier = false;
                    } else {
                        if (    token == JavaTokenContext.WHITESPACE 
                             || token == JavaTokenContext.BLOCK_COMMENT
                             || token == JavaTokenContext.LINE_COMMENT) {
                            offset--;
                        } else {
                            break;
                        }
                    } 
                }
            }
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            return "<unknown>"; // NOI18N
        }
        
        return name;
    }
    
    public void performClickAction(Document originalDoc, int offset) {
        if (!(originalDoc instanceof BaseDocument))
            return ;
        
        BaseDocument doc = (BaseDocument) originalDoc;
        JTextComponent target = Utilities.getFocusedComponent();
        
        if (target == null || target.getDocument() != doc)
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
                
                if (item == null || item instanceof JavaPackage || (itemDesc = nbJavaSup.openSource(item, true)) != null) {
                    //nothing found (item == null), package (item instanceof JavaPackage) or no source found (itemDesc != null)
                    //inform user, that we were not able to open the resource.
                    Toolkit.getDefaultToolkit().beep();
                    
                    String key;
                    String name;
                    
                    if (itemDesc != null) {
                        key = "goto_source_source_not_found"; // NOI18N
                        name = itemDesc;
                    } else {
                        key = "cannot-open-element";// NOI18N
                        name = findName((BaseDocument) doc, offset);
                    }
                    
                    String msg = NbBundle.getBundle(JavaKit.class).getString(key);
                    
                    org.openide.awt.StatusDisplayer.getDefault().setStatusText(MessageFormat.format(msg, new Object [] { name } ));
                }
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
            
            if (target == null || target.getDocument() != bdoc)
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

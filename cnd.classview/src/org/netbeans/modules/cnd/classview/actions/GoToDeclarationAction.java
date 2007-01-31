/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.classview.actions;

import java.beans.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.List;
import java.io.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.openide.awt.*;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.*;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.*;
import org.openide.text.PositionBounds;
import org.openide.filesystems.*;
import org.openide.util.*;
import org.openide.loaders.*;
import org.openide.windows.*;

import  org.netbeans.modules.cnd.api.model.*;

import org.netbeans.modules.cnd.classview.resources.I18n;

/**
 * @author Vladimir Kvashin
 */
public class GoToDeclarationAction extends AbstractAction {
    
    private CsmOffsetable csmObject;
    
    public GoToDeclarationAction(CsmOffsetable csmObject) {
        this.csmObject = csmObject;
        putValue(Action.NAME, I18n.getMessage("LBL_GoToDeclaration")); //NOI18N
    }
    
    public void actionPerformed(ActionEvent e) {
        //JOptionPane.showMessageDialog(null, "Not implemented");
        openElement(csmObject);
    }

    private static boolean openElement(CsmOffsetable element) {
        return openElement(element, false);
    }
    
    private static boolean openElement(final CsmOffsetable element, final boolean jumpLineStart) {
        try {
            FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(new File(element.getContainingFile().getAbsolutePath())));
	    if( fo == null ) {
		return false;
	    }
            DataObject dob = DataObject.find(fo);
            final EditorCookie.Observable ec = (EditorCookie.Observable)dob.getCookie(EditorCookie.Observable.class);
            if (ec != null) {
                StatusDisplayer.getDefault().setStatusText(
                    I18n.getMessage("MSG_OpeningElement", // NOI18N
                                        getElementJumpName(element))
                    );
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        JEditorPane[] panes = ec.getOpenedPanes();
                        boolean opened = true;
                        if (panes != null && panes.length >= 0) {
                            //editor already opened, so just select
                            opened = true;
                        } else {
                            // editor not yet opened
                            // XXX: commented out following code, because on the time
                            // of firing even no chance to get opened panes yet...
//                            ec.addPropertyChangeListener(new PropertyChangeListener() {
//                                public void propertyChange(PropertyChangeEvent evt) {
//                                    if (EditorCookie.Observable.PROP_OPENED_PANES.equals(evt.getPropertyName())) {
//                                        final JEditorPane[] panes = ec.getOpenedPanes();
//                                        if (panes != null && panes.length > 0) {
//                                            selectElementInPane(panes[0], element, true);
//                                        }
//                                        ec.removePropertyChangeListener(this);
//                                    }
//                                }
//                            });
                            opened = false;
                            ec.open();
                            // XXX: get panes here instead of in listener
                            panes = ec.getOpenedPanes();
                        }
                        if (panes != null && panes.length > 0) {
                            selectElementInPane(panes[0], element, !opened, jumpLineStart);
                        }                        
                    }
                    
                });
		return true;
            }
        } catch( DataObjectNotFoundException e ) {
            e.printStackTrace(System.err);
        }
        return false;
    }
    
    /** Jumps to element in given editor pane. When delayProcessing is
     * specified, waits for real visible open before jump
     */
    private static void selectElementInPane(final JEditorPane pane, final CsmOffsetable element, 
                                            boolean delayProcessing, final boolean jumpLineStart) {
        //final Cursor editCursor = pane.getCursor();
        //pane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (delayProcessing) {
            // [dafe] I don't know why, but editor guys are waiting for focus
            // in delay processing, so I will do the same
            pane.addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    // here we don't need using CsmModel.enqueue, 
                    // since actually there's no model calls (except for getting positions)
                    RequestProcessor.getDefault().post(new Runnable() {
                        public void run() {
                            jumpToElement(pane, element, jumpLineStart);
                        }
                    });
                    pane.removeFocusListener(this);
                }
            });
        } else {
            // immediate processing
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    jumpToElement(pane, element, jumpLineStart);
                }
            });
            // try to activate outer TopComponent
            Container temp = pane;
            while (!(temp instanceof TopComponent)) {
                temp = temp.getParent();
            }
            ((TopComponent) temp).requestActive();
       }
    }
    
    
    /** Jumps to element on given editor pane. Call only outside AWT thread!
     */
    private static void jumpToElement(JEditorPane pane, CsmOffsetable element) {
        jumpToElement(pane, element, false);
    }
    
    private static void jumpToElement(JEditorPane pane, CsmOffsetable element, boolean jumpLineStart) {
        int caretPos = pane.getCaretPosition();
        Container parent = pane.getParent();
        Point viewPos = parent instanceof JViewport ? ((JViewport)parent).getViewPosition() : null;
        int start = jumpLineStart ? lineToPosition(pane, element.getStartPosition().getLine()) : element.getStartOffset();
        if( start > 0 && pane.getCaretPosition() == caretPos &&
            pane.getDocument() != null && start < pane.getDocument().getLength() &&
            (viewPos == null || viewPos.equals(((JViewport)parent).getViewPosition()))) {
            pane.setCaretPosition(start);
        }
        StatusDisplayer.getDefault().setStatusText(""); // NOI18N
    }
    
    private static int lineToPosition(JEditorPane pane, int line) {
        Document doc = pane.getDocument();
        int len = doc.getLength();
        int lineSt = 0;
        try {
            String text = doc.getText(0, len);
            boolean afterEOL = false;
            for( int i = 0; i < len; i++ ) {
                char c = text.charAt(i);
                if( c == '\n') {
                    line--;
                    if( line == 0 ) {
                        return lineSt;
                    }
                    afterEOL = true;
                } else if (afterEOL) {
                    lineSt = i;
                    afterEOL = false;
                }
            }
        }
        catch( BadLocationException e ) {
        }
        return lineSt;
    } 
    
    private static String getElementJumpName(CsmOffsetable element) {
        String text = ""; // NOI18N
        if (element != null) {
            text = (element instanceof CsmNamedElement) ? 
                        ((CsmNamedElement) element).getName() : 
                        element.getContainingFile().getAbsolutePath();
        }
        return text;
    }
}

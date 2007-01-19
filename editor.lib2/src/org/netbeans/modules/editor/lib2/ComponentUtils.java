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

package org.netbeans.modules.editor.lib2;

import java.awt.Component;
import java.awt.Frame;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

/**
 * 
 * @author Vita Stejskal
 */
public final class ComponentUtils {

    private static final Logger LOG = Logger.getLogger(Logger.class.getName());
    
    public static boolean isGuardedException(BadLocationException exc) {
        return exc.getClass().getName().equals("GuardedException");
    }

    public static void returnFocus() {
         JTextComponent c = DocumentsRegistry.getMostActiveComponent();
         if (c != null) {
             requestFocus(c);
         }
    }

    public static void requestFocus(JTextComponent c) {
        if (c != null) {
            if (!EditorImplementation.getDefault().activateComponent(c)) {
                Frame f = getParentFrame(c);
                if (f != null) {
                    f.requestFocus();
                }
                c.requestFocus();
            }
        }
    }

    public static void setStatusText(JTextComponent c, String text) {
        // TODO: fix this, do not use reflection
        try {
            Object editorUI = getEditorUI(c);
            Method getSbMethod = editorUI.getClass().getMethod("getStatusBar");
            Object statusBar = getSbMethod.invoke(editorUI);
            Method setTextMethod = statusBar.getClass().getMethod("setText", String.class, String.class);
            setTextMethod.invoke(statusBar, "main", text);
        } catch (Exception e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
//        StatusBar sb = getEditorUI(c).getStatusBar();
//        if (sb != null) {
//            sb.setText(StatusBar.CELL_MAIN, text);
//        }
    }

//    public static void setStatusText(JTextComponent c, String text, Coloring extraColoring) {
//        TextUI textUI = c.getUI();
//        try {
//            Method getSbMethod = textUI.getClass().getMethod("getStatusBar");
//            Object statusBar = getSbMethod.invoke(textUI);
//            Method setTextMethod = statusBar.getClass().getMethod("setText", String.class, String.class);
//            setTextMethod.invoke(statusBar, "main", text);
//        } catch (Exception e) {
//            LOG.log(Level.WARNING, e.getMessage(), e);
//        }
////        StatusBar sb = getEditorUI(c).getStatusBar();
////        if (sb != null) {
////            sb.setText(StatusBar.CELL_MAIN, text, extraColoring);
////        }
//    }

    public static void setStatusBoldText(JTextComponent c, String text) {
        // TODO: fix this, do not use reflection
        try {
            Object editorUI = getEditorUI(c);
            Method getSbMethod = editorUI.getClass().getMethod("getStatusBar"); //NOI18N
            Object statusBar = getSbMethod.invoke(editorUI);
            Method setTextMethod = statusBar.getClass().getMethod("setBoldText", String.class, String.class); //NOI18N
            setTextMethod.invoke(statusBar, "main", text); //NOI18N
        } catch (Exception e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
//        StatusBar sb = getEditorUI(c).getStatusBar();
//        if (sb != null) {
//            sb.setBoldText(StatusBar.CELL_MAIN, text);
//        }
    }

    public static String getStatusText(JTextComponent c) {
        // TODO: fix this, do not use reflection
        try {
            Object editorUI = getEditorUI(c);
            Method getSbMethod = editorUI.getClass().getMethod("getStatusBar"); //NOI18N
            Object statusBar = getSbMethod.invoke(editorUI);
            Method getTextMethod = statusBar.getClass().getMethod("getText", String.class); //NOI18N
            return (String) getTextMethod.invoke(statusBar, "main"); //NOI18N
        } catch (Exception e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
            return ""; //NOI18N
        }
//        StatusBar sb = getEditorUI(c).getStatusBar();
//        return (sb != null) ? sb.getText(StatusBar.CELL_MAIN) : null;
    }

    public static void clearStatusText(JTextComponent c) {
        setStatusText(c, ""); // NOI18N
    }
    
    
    private static Object getEditorUI(JTextComponent c) throws Exception {
        // TODO: fix this, do not use reflection
        TextUI textUI = c.getUI();
        Method getEuiMethod = textUI.getClass().getMethod("getEditorUI"); //NOI18N
        return getEuiMethod.invoke(textUI);
    }
    
    private static Frame getParentFrame(Component c) {
        do {
            c = c.getParent();
            if (c instanceof Frame) {
                return (Frame)c;
            }
        } while (c != null);
        return null;
    }
    
    /** Creates a new instance of DocUtils */
    private ComponentUtils() {
    }
}

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

package org.netbeans.modules.xml.multiview;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import java.awt.*;

/**
 * Utils.java
 *
 * Created on November 16, 2004, 3:21 PM
 * @author mkuchtiak
 */
public class Utils {
    
    /** This method update document in editor after change in beans hierarchy.
     * It takes old document and new document in String.
     * To avoid regeneration of whole document in text editor following steps are done:
     *  1) compare the begin of both documents (old one and new one)
     *     - find the first position where both documents differ
     *  2) do the same from the ends of documents
     *  3) remove old middle part of text (modified part) and insert new one
     * 
     * @param doc original document
     * @param newDoc new value of whole document
     * @param prefixMark - beginning part of the document before this mark should be preserved
     */
    public static void replaceDocument(javax.swing.text.Document doc, String newDoc, String prefixMark)
            throws javax.swing.text.BadLocationException {
        String origDoc = doc.getText(0, doc.getLength());
        int prefixIndex = 0;
        if (prefixMark!=null) {
            prefixIndex = origDoc.indexOf(prefixMark);
            if (prefixIndex < 0) {
                prefixIndex = 0;
            } else {
                origDoc = origDoc.substring(prefixIndex);
            }
            int prefixIndNewDoc = newDoc.indexOf(prefixMark);
            if (prefixIndNewDoc > 0) {
                newDoc = newDoc.substring(prefixIndNewDoc);
            }
        }
        newDoc = filterEndLines(newDoc);

        if (origDoc.equals(newDoc)) {
            // no change in document
            return;
        }

        char[] origChars = origDoc.toCharArray();
        char[] newcChars = newDoc.toCharArray();
        int offset = 0;
        int tailIndex = origChars.length;
        int delta = newcChars.length - tailIndex;
        int n = delta < 0 ? tailIndex + delta : tailIndex;
        for (offset = 0; offset < n; offset++) {
            if (origChars[offset] != newcChars[offset]) {
                break;
            }
        }
        n = delta < 0 ? offset - delta : offset;
        for (int i = tailIndex - 1; i >= n; i--) {
            if (origChars[i] == newcChars[i + delta]) {
                tailIndex = i;
            } else {
                i = tailIndex;
                break;
            }
        }

        String s = newDoc.substring(offset, tailIndex + delta);
        int length = tailIndex - offset;
        offset += prefixIndex;
        if (doc instanceof AbstractDocument) {
            ((AbstractDocument) doc).replace(offset, length, s, null);
        } else {
            if (length > 0) {
                doc.remove(offset, length);
            }
            if (s.length() > 0) {
                doc.insertString(offset, s, null);
            }
        }
    }

    public static void replaceDocument(javax.swing.text.Document doc, String newDoc) throws javax.swing.text.BadLocationException {
        replaceDocument(doc,newDoc,null);
    }
    
    /** Filter characters #13 (CR) from the specified String
     * @param str original string
     * @return the string without #13 characters
     */
    public static String filterEndLines(String str) {
        char[] text = str.toCharArray();
        if (text.length==0) return "";
        int pos = 0;
        for (int i = 0; i < text.length; i++) {
            char c = text[i];
            if (c != 13) {
                if (pos != i)
                    text[pos] = c;
                pos++;
            }
        }
        return new String(text, 0, pos);
    }

    /**
     * Sets focus to the next focusable component according to focus traversal policy
     * @param component currently focused component
     */
    public static void focusNextComponent(Component component) {
        Container focusCycleRoot = component.getFocusCycleRootAncestor();
        if (focusCycleRoot == null) {
            return;
        }
        final FocusTraversalPolicy focusTraversalPolicy = focusCycleRoot.getFocusTraversalPolicy();
        if (focusTraversalPolicy == null) {
            return;
        }
        final Component componentAfter = focusTraversalPolicy.getComponentAfter(focusCycleRoot, component);
        if (componentAfter != null) {
            componentAfter.requestFocus();
        }
    }

    /**
     * Scroll panel to make the component visible
     * @param component
     */
    public static void scrollToVisible(final JComponent component) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                component.scrollRectToVisible(new Rectangle(10, component.getHeight()));
            }
        });
    }

    /**
     * Make sure that the code will run in AWT dispatch thread
     * @param runnable
     */
    public static void runInAwtDispatchThread(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }

    }
}

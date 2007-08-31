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

package org.netbeans.modules.cnd.debugger.gdb.models;

import javax.swing.ImageIcon;

import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;

import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;


/**
* Helper methods for debugging.
*
* @author  Jan Jancura
*/
public class Utils {
    
    public static String getIdentifier() {
        EditorCookie e = getCurrentEditorCookie();
        if (e == null) {
            return null;
        }
        JEditorPane ep = getCurrentEditor(e);
        if (ep == null) {
            return null;
        }
        return getIdentifier(e.getDocument(), ep, ep.getCaret().getDot());
    }
    
    private static String getIdentifier(StyledDocument doc, JEditorPane ep, int offset) {
        String t = null;
        if ((ep.getSelectionStart() <= offset) && (offset <= ep.getSelectionEnd())) {
            t = ep.getSelectedText();
        }
        if (t != null) {
            return t;
        }
        
        int line = NbDocument.findLineNumber(doc, offset);
        int col = NbDocument.findLineColumn(doc, offset);
        try {
            Element lineElem = NbDocument.findLineRootElement(doc).getElement(line);

            if (lineElem == null) {
                return null;
            }
            int lineStartOffset = lineElem.getStartOffset();
            int lineLen = lineElem.getEndOffset() - lineStartOffset;
            t = doc.getText(lineStartOffset, lineLen);
            int identStart = col;
            // FIXME - Need to make this C/C++ oriented
            while (identStart > 0 && (Character.isJavaIdentifierPart(t.charAt(identStart - 1)) ||
                (t.charAt(identStart - 1) == '.'))) {
                identStart--;
            }
            int identEnd = col;
            while (identEnd < lineLen && Character.isJavaIdentifierPart(t.charAt(identEnd))) {
                identEnd++;
            }

            if (identStart == identEnd) {
                return null;
            }
            return t.substring(identStart, identEnd);
        } catch (BadLocationException e) {
            return null;
        }
    }
    
    /** 
     * Returns current editor component instance.
     *
     * @return current editor component instance
     */
    private static JEditorPane getCurrentEditor () {
        EditorCookie e = getCurrentEditorCookie ();
        if (e == null) return null;
        JEditorPane[] op = e.getOpenedPanes ();
        if ((op == null) || (op.length < 1)) return null;
        return op [0];
    }
    
    /** 
     * Returns current editor component instance.
     *
     * @return current editor component instance
     */
    private static JEditorPane getCurrentEditor (EditorCookie e) {
        JEditorPane[] op = e.getOpenedPanes ();
        if ((op == null) || (op.length < 1)) return null;
        return op [0];
    }
     
    /** 
     * Returns current editor component instance.
     *
     * @return current editor component instance
     */
    private static String getSelectedText () {
        JEditorPane ep = getCurrentEditor ();
        if (ep == null) return null;
        return ep.getSelectedText ();
    }
    
    /** 
     * Returns current editor component instance.
     *
     * @return current editor component instance
     */
    private static EditorCookie getCurrentEditorCookie () {
        Node[] nodes = TopComponent.getRegistry ().getActivatedNodes ();
        if ( (nodes == null) ||
             (nodes.length != 1) ) return null;
        Node n = nodes [0];
        return (EditorCookie) n.getCookie (
            EditorCookie.class
        );
    }
//
//    public static Line getCurrentLine () {
//        EditorCookie e = getCurrentEditorCookie (); // grr ugly, but safe
//        if (e == null) return null;                 // i am very sorry..
//        JEditorPane ep = getCurrentEditor (e);
//        if (ep == null) return null;
//        StyledDocument d = e.getDocument ();
//        if (d == null) return null;
//        Line.Set ls = e.getLineSet ();
//        if (ls == null) return null;
//        Caret c = ep.getCaret ();
//        if (c == null) return null;
//        return ls.getCurrent (
//            NbDocument.findLineNumber (
//                d,
//                c.getDot ()
//            )
//        );
//    }
    
    public static ImageIcon getIcon (String iconBase) {
        return new ImageIcon (Utilities.loadImage (iconBase+".gif"));
    }
    
    /**
     * Returns all registered DebuggerManager Implementations ({@link DebuggerPlugIn}).
     *
     * @return all registered DebuggerManager Implementations ({@link DebuggerPlugIn})
     */
//    private static List loadMetaInf (String resourceName) {
//        ArrayList l = new ArrayList ();
//        try {
//            ClassLoader cl = Thread.currentThread ().getContextClassLoader ();
//            System.out.println("");
//            System.out.println("loadMetaInf " + resourceName);
//            Enumeration e = cl.getResources (resourceName);
//            while (e.hasMoreElements ()) {
//                URL url = (URL) e.nextElement();
//                //S ystem.out.println("  url: " + url);
//                BufferedReader br = new BufferedReader (
//                    new InputStreamReader (url.openStream ())
//                );
//                String s = br.readLine ();
//                while (s != null) {
//                    System.out.println("  class:" + s);
//                    Object o = cl.loadClass (s).newInstance ();
//                    l.add (o);
//                    s = br.readLine ();
//                }
//            }
//            return l; 
//        } catch (IOException e) {
//            e.printStackTrace ();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace ();
//        } catch (InstantiationException e) {
//            e.printStackTrace ();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace ();
//        }
//        throw new InternalError ("Can not read from Meta-inf!");
//    }
//    
//    public static List getProviders (Class cl) {
//        ArrayList l = new ArrayList ();
//        l.addAll (loadMetaInf (
//            "META-INF/debugger/" + cl.getName ()
//        ));
//        return l; 
//    }
}

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

package org.netbeans.modules.debugger.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ClassLoader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import javax.swing.ImageIcon;

import javax.swing.JEditorPane;
import javax.swing.text.Caret;
import javax.swing.text.StyledDocument;

import org.netbeans.api.debugger.DebuggerEngine;

import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;


/**
* Helper methods for debugging.
*
* @author  Jan Jancura
*/
public class Utils {
    
    /**
     * Creates new node property for the specified object.
     *
     * @param  inst  object for which we are creating the new property
     * @param  type  type of property value
     *               (e.g. <TT>Boolean.TYPE</TT> for boolean property)
     * @param  name  internal property name (not displayed)
     * @param  dispName  property name (displayed)
     * @param  shortDesc  short description of the property (hint)
     * @param  getter  name of getter method
     *                 (if <TT>null</TT>, the property will be write-only)
     * @param  setter  name of setter method
     *                 (if <TT>null</TT>, the property will be read-only)
     * @see org.openide.nodes.PropertySupport.Reflection
     */
//    public static Node.Property createProperty (
//        Object instance, Class type,
//        String name, String dispName,
//        String shortDesc,
//        String getter, String setter
//    ) {
//        Node.Property prop;
//        try {
//            prop = new PropertySupport.Reflection (
//                instance, type, getter, setter
//            );
//        }
//        catch (NoSuchMethodException ex) {
//            throw new IllegalStateException (ex.getMessage ());
//        }
//        prop.setName (name);
//        prop.setDisplayName (dispName);
//        prop.setShortDescription (shortDesc);
//        return prop;
//    }
    
    public static String getIdentifier () {
        EditorCookie e = getCurrentEditorCookie ();
        if (e == null) return null;
        JEditorPane ep = getCurrentEditor (e);
        if (ep == null) return null;
        return getIdentifier (
            e.getDocument (),
            ep,
            ep.getCaret ().getDot ()
        );
    }
    
    private static String getIdentifier (
        StyledDocument doc, 
        JEditorPane ep, 
        int offset
    ) {
        String t = null;
        if ( (ep.getSelectionStart () <= offset) &&
             (offset <= ep.getSelectionEnd ())
        )   t = ep.getSelectedText ();
        if (t != null) return t;
        
        int line = NbDocument.findLineNumber (
            doc,
            offset
        );
        int col = NbDocument.findLineColumn (
            doc,
            offset
        );
        try {
            javax.swing.text.Element lineElem = 
                org.openide.text.NbDocument.findLineRootElement (doc).
                getElement (line);

            if (lineElem == null) return null;
            int lineStartOffset = lineElem.getStartOffset ();
            int lineLen = lineElem.getEndOffset() - lineStartOffset;
            t = doc.getText (lineStartOffset, lineLen);
            int identStart = col;
            while (identStart > 0 && 
                (Character.isJavaIdentifierPart (
                    t.charAt (identStart - 1)
                ) ||
                (t.charAt (identStart - 1) == '.'))) {
                identStart--;
            }
            int identEnd = col;
            while (identEnd < lineLen && 
                   Character.isJavaIdentifierPart(t.charAt(identEnd))
            ) {
                identEnd++;
            }

            if (identStart == identEnd) return null;
            return t.substring (identStart, identEnd);
        } catch (javax.swing.text.BadLocationException e) {
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
        String n = iconBase + ".gif"; // NOI18N
        if (n.startsWith ("/"))
            n = n.substring (1);
        ClassLoader currentClassLoader = (ClassLoader) Lookup.getDefault ().
            lookup (ClassLoader.class);
        URL url = currentClassLoader.getResource (n);
        //		URL url = TopManager.getDefault ().systemClassLoader ().getResource (n);
        if (url == null) {
            System.out.println (
            "Icon: " + n +  // NOI18N
            " does not exist!" // NOI18N
            );
            url = Utils.class.getResource (
            "org/openide/resources/actions/properties.gif" // NOI18N
            );
        }
        return new ImageIcon (url);
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

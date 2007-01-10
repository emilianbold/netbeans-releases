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

package org.netbeans.modules.web.debug.util;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import javax.swing.*;
import javax.swing.text.*;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;

import org.openide.ErrorManager;
import org.openide.nodes.*;
import org.openide.filesystems.*;
import org.openide.text.*;
import org.openide.cookies.*;
import org.openide.util.Mutex;
import org.openide.windows.TopComponent;

import org.netbeans.modules.web.api.webmodule.*;
import org.netbeans.modules.j2ee.deployment.devmodules.api.JSPServletFinder;

import org.netbeans.api.project.*;



/**
 *
 * @author Martin Grebac
 */
public class Utils {
    
    /** Logger for web.debug module. */
    private static ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.web.debug");   // NOI18N
     
    public static ErrorManager getEM () {
        return err;
    }
    
    public static FileObject getFileObjectFromUrl(String url) {
        
        FileObject fo = null;
        
        try {
            fo = URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException e) {
            //noop
        }

        return fo;
    }
    
    public static boolean isJsp(FileObject fo) {
        return fo != null && "text/x-jsp".equals(fo.getMIMEType());   //NOI18N
    }
    
    public static boolean isJsp(String url) {
        FileObject fo = getFileObjectFromUrl(url);
        return isJsp(fo);
    }

    public static boolean isTag(FileObject fo) {
        return fo != null && "text/x-tag".equals(fo.getMIMEType());   //NOI18N
    }
    
    public static boolean isTag(String url) {
        FileObject fo = getFileObjectFromUrl(url);
        return isTag(fo);
    }

    public static String getTargetServerID(FileObject fo) {
        if (fo != null) {
            Project p = FileOwnerQuery.getOwner(fo);
            if (p != null) {
                J2eeModuleProvider mp = (J2eeModuleProvider)p.getLookup().lookup(J2eeModuleProvider.class);
                if (mp != null) {
                    String serverID = mp.getServerID();
                    return serverID;
                }
            }
        }
        return null;
    }
    
    public static String getJspName(String url) {

        FileObject fo = getFileObjectFromUrl(url);
        if (fo != null) {
            return fo.getNameExt();
        }
        return (url == null) ? null : url.toString();
    }
    
    public static String getJspPath(String url) {
       
        FileObject fo = getFileObjectFromUrl(url);
        String jspRelativePath = url;
        if (fo != null) {
            WebModule wm = WebModule.getWebModule (fo);
            if (wm != null)
                jspRelativePath = FileUtil.getRelativePath(wm.getDocumentBase(), fo);
        }
        
        return jspRelativePath;

    }
    
    public static String getServletClass(String url) {
        FileObject fo = getFileObjectFromUrl(url);
        if (fo == null) {
            return null;
        }
        JSPServletFinder finder = JSPServletFinder.findJSPServletFinder (fo);
        WebModule wm = WebModule.getWebModule (fo);
        if (wm == null)
            return null;
        
        String jspRelativePath = FileUtil.getRelativePath(wm.getDocumentBase(), fo);
        String contextPath = wm.getContextPath();

        String servletPath = finder.getServletResourcePath(jspRelativePath);      
        if (servletPath == null) // we don't have class name, so assume we are debugging tomcat or appsrv
                servletPath = JspNameUtil.getServletResourcePath(contextPath, jspRelativePath);
        if (servletPath != null) {
            servletPath = servletPath.substring(0, servletPath.length()-5); // length of ".java"
            servletPath = servletPath.replace('/', '.'); //NOI18N
        }
        Utils.getEM().log("servlet class: " + servletPath);
        return servletPath;
    }

    public static String getClassFilter(String url) {
        String filter = getServletClass(url);
        if (filter != null) {
            // get package only
            filter = filter.substring(0, filter.lastIndexOf('.')) + ".*"; //NOI18N
            if (filter.startsWith("org.apache.jsp")) 
                filter = "org.apache.jsp.*";
        }
        return filter;
    }
    
//    public static String getCompoundClassFilter(String url) {
//
//        FileObject wmfo = getJspFileObjectFromUrl(url);
//        if (wmfo == null) {
//            return null;
//        }
//        WebModule wm = WebModule.getWebModule(wmfo);
//        Enumeration files = wm.getDocumentBase().getChildren(true);
//                        
//        String filter = null; //NOI18N
//        while (files.hasMoreElements()) {
//            FileObject fo = (FileObject)files.nextElement();
//            if (!fo.isFolder() && "text/x-jsp".equals(fo.getMIMEType()) && (fo != null)) {
//                String jspRelPath = FileUtil.getRelativePath(wm.getDocumentBase(), fo);
//                JSPServletFinder finder = JSPServletFinder.findJSPServletFinder(fo);
//                String servletPath = finder.getServletResourcePath(jspRelPath);
//                if ((servletPath != null) && !servletPath.equals("")) {                
//                    servletPath = servletPath.substring(0, servletPath.length()-5); // length of ".java"
//                    servletPath = servletPath.substring(0, servletPath.lastIndexOf('/')); // get package only
//                    if (filter == null) {
//                        filter = servletPath;
//                    } else {
//                        if (!(servletPath.startsWith(filter))) {
//                            while (!servletPath.startsWith(filter)) {
//                                filter = filter.substring(0, filter.lastIndexOf('/'));
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        filter = filter.replace('/', '.') + "."; //NOI18N
//        Utils.getEM().log("compound filter: " + filter);
//        return filter;
//    }

    public static String getContextPath(String url) {
        FileObject wmfo = getFileObjectFromUrl(url);
        if (wmfo == null) {
            return null;
        }
        WebModule wm = WebModule.getWebModule(wmfo);        
        if (wm != null) {
            return wm.getContextPath();
        }
        return null;   
    }
        
    /** 
     * Returns current editor component instance.
     *
     * @return current editor component instance
     */
    public static EditorCookie getCurrentEditorCookie () {
        Node[] nodes = TopComponent.getRegistry ().getCurrentNodes();
        if ( (nodes == null) ||
             (nodes.length != 1) ) return null;
        Node n = nodes [0];
        return (EditorCookie) n.getCookie (
            EditorCookie.class
        );
    }
    
    public static JEditorPane getCurrentEditor () {
        EditorCookie e = getCurrentEditorCookie ();
        if (e == null) {
            return null;
        }
        return getCurrentEditor(e);
    }    
    
    /** 
     * Returns current editor component instance.
     *
     * @return current editor component instance
     */
    public static JEditorPane getCurrentEditor (final EditorCookie e) {
        return Mutex.EVENT.readAccess(new Mutex.Action<JEditorPane>() {
            public JEditorPane run() {
                JEditorPane[] op = e.getOpenedPanes();
                return (op == null ? null : op[0]);
            }
        });
    }
        
    public static String getJavaIdentifier(StyledDocument doc, JEditorPane ep, int offset) {        
        String t = null;
        if ( (ep.getSelectionStart() <= offset) && (offset <= ep.getSelectionEnd())) {
            t = ep.getSelectedText();
        }
        if (t != null) {
            return t;
        }
        int line = NbDocument.findLineNumber(doc, offset);
        int col = NbDocument.findLineColumn(doc, offset);
        try {
            javax.swing.text.Element lineElem = 
                org.openide.text.NbDocument.findLineRootElement(doc).
                getElement(line);
            if (lineElem == null) {
                return null;
            }
            int lineStartOffset = lineElem.getStartOffset();
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
            while (identEnd < lineLen && Character.isJavaIdentifierPart(t.charAt(identEnd))) {
                identEnd++;
            }
            if (identStart == identEnd) {
                return null;
            }
            return t.substring (identStart, identEnd);
        } catch (javax.swing.text.BadLocationException e) {
            return null;
        }
    }    

    public static boolean isScriptlet(StyledDocument doc, JEditorPane ep, int offset) {
        String t;
        int line = NbDocument.findLineNumber(doc, offset);
        int col = NbDocument.findLineColumn(doc, offset);
        try {
            while (line > 0) {
                javax.swing.text.Element lineElem = 
                    org.openide.text.NbDocument.findLineRootElement(doc).getElement(line);
                if (lineElem == null) {
                    continue;
                }
                int lineStartOffset = lineElem.getStartOffset();
                int lineLen = lineElem.getEndOffset() - lineStartOffset;
                t = doc.getText (lineStartOffset, lineLen);
                if ((t != null) && (t.length() > 1)) {
                    int identStart;
                    if (line == NbDocument.findLineNumber(doc, offset)) {
                        identStart = col;
                    } else {
                        identStart = lineLen-1;
                    }
                    while (identStart > 0) {
                        if ((t.charAt(identStart) == '%') && (t.charAt(identStart-1) == '<')) {
                            return true;
                        }
                        if ((t.charAt(identStart) == '>') && (t.charAt(identStart-1) == '%')) {
                            return false;
                        }                    
                        identStart--;
                    }
                }
                line--;
            }
        } catch (javax.swing.text.BadLocationException e) {
        }
        return false;
    }        
    
    public static String getELIdentifier(StyledDocument doc, JEditorPane ep, int offset) {
        String t = null;
        if ( (ep.getSelectionStart () <= offset) &&
             (offset <= ep.getSelectionEnd ())
        )   t = ep.getSelectedText ();
        if (t != null) {
            if ((t.startsWith("$")) && (t.endsWith("}"))) {
                return t;
            } else {
                return null;
            }
        }
        
        int line = NbDocument.findLineNumber(doc, offset);
        int col = NbDocument.findLineColumn(doc, offset);
        try {
            javax.swing.text.Element lineElem = 
                org.openide.text.NbDocument.findLineRootElement (doc).
                getElement (line);

            if (lineElem == null) {
                return null;
            }
            int lineStartOffset = lineElem.getStartOffset ();
            int lineLen = lineElem.getEndOffset() - lineStartOffset;
            t = doc.getText (lineStartOffset, lineLen);
            int identStart = col;
            while (identStart > 0 && (t.charAt(identStart) != '$')) {
                identStart--;
            }
            if ((identStart > 0) && (t.charAt(identStart) == '$') && (t.charAt(identStart-1) == '\\')) {
                return null;
            }
            int identEnd = col;
            while ((identEnd < lineLen) && identEnd > 0 && identEnd <= t.length() && (t.charAt(identEnd-1) != '}'))  {
                identEnd++;
            }
            if (identStart == identEnd) {
                return null;
            }
            String outp = t.substring(identStart, identEnd);
            if ((outp.startsWith("$")) && (outp.endsWith("}"))) {
                return outp;
            } else {            
                return null;
            }
        } catch (javax.swing.text.BadLocationException e) {
            return null;
        }
    }
    
    public static String getJavaIdentifier () {
        EditorCookie e = getCurrentEditorCookie ();
        if (e == null) {
            return null;
        }
        JEditorPane ep = getCurrentEditor (e);
        if (ep == null) {
            return null;
        }
        return getJavaIdentifier (
            e.getDocument (),
            ep,
            ep.getCaret ().getDot ()
        );
    }

//    public static String getELIdentifier () {
//        EditorCookie e = getCurrentEditorCookie ();
//        if (e == null) {
//            return null;
//        }
//        JEditorPane ep = getCurrentEditor (e);
//        if (ep == null) {
//            return null;
//        }
//        return getELIdentifier (
//            e.getDocument (),
//            ep,
//            ep.getCaret ().getDot ()
//        );
//    }

    public static boolean isScriptlet() {
        EditorCookie e = getCurrentEditorCookie ();
        if (e == null) {
            return false;
        }
        JEditorPane ep = getCurrentEditor (e);
        if (ep == null) {
            return false;
        }
        return isScriptlet(
            e.getDocument (),
            ep,
            ep.getCaret ().getDot ()
        );
    }
  
    
    
}

/*
 * Utils.java
 *
 * Created on November 1, 2003, 8:24 AM
 */

package org.netbeans.modules.web.debug.util;

import java.util.*;

import javax.swing.*;
import javax.swing.text.*;

import org.openide.ErrorManager;
import org.openide.nodes.*;
import org.openide.filesystems.*;
import org.openide.text.*;
import org.openide.loaders.DataObject;
import org.openide.cookies.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

import org.openide.filesystems.Repository;

import org.netbeans.modules.web.core.jsploader.*;
import org.netbeans.modules.web.debug.Context;
import org.netbeans.api.project.*;
import org.netbeans.modules.web.spi.webmodule.*;

import java.net.*;

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
    
    public static Object[] getJsps() {
        
        //HashMap sorted = new TreeMap();
        List jsps = new Vector();
        Enumeration e;

        String currentUrl = Context.getCurrentURL();
        if (currentUrl == null) {
            return null;
        }
        
        Project project = FileOwnerQuery.getOwner(URI.create(currentUrl));
        WebModuleImplementation wmi = (WebModuleImplementation)project.getLookup().lookup(WebModuleImplementation.class);
        e = wmi.getDocumentBase().getChildren(true);
        
        while (e.hasMoreElements()) {
            FileObject ch = (FileObject)e.nextElement();
            getEM().log("ch: " + ch);
            if (!ch.isFolder() && !ch.isRoot() && !ch.isVirtual() && ch.isValid() && JspLoader.JSP_MIME_TYPE.equals(ch.getMIMEType())) {
                String idStr = ch.getPath();
                if (!jsps.contains(idStr)) {
                    jsps.add(idStr);
                }
            }
        }
        getEM().log("jsps : " + jsps);
        Object[] sorted = jsps.toArray();
        Arrays.sort(sorted);
        return sorted;
    }

    public static String getCurrentJspName() {
        FileObject fo = getCurrentFileObject();
        if (fo == null) {
            return "";
        }
        if (!fo.getMIMEType().equals(JspLoader.JSP_MIME_TYPE)) {
            return "";
        }
        return fo.getPath();
    }

    public static String getCurrentContextRoot() {
        FileObject fo = getCurrentFileObject();
        String ctx = "";
        DataObject data = null;
        
        if (fo == null) {
            return ctx;
        }
        if (!fo.getMIMEType().equals(JspLoader.JSP_MIME_TYPE)) {
            return ctx;
        }
        
        try {
            data = DataObject.find(fo);
        } catch (Exception excep) {
            // don't care
        }
        
//        if ((data instanceof JspDataObject) && (data!=null)) {
//            data = ((JspDataObject)data).getModule();
//        }
        
//        if ((data instanceof WebContextObject) && (data!=null)) {
//            ctx = ((WebContextObject)data).getContextPath();
//        }
        
        return ctx;
    }

    public static FileObject getCurrentFileObject() {
        getEM().log("Utils.getCurrentObject");
//        AddBreakpointAction aba = (AddBreakpointAction)SystemAction.get(AddBreakpointAction.class);
//        Node[] nodes = aba.getActivatedNodes();
//        if (nodes == null) {
//            return null;
//        }
//        if (nodes.length != 1) {
//            return null;
//        }
//        Node n = nodes[0];
//        DataObject dO = null;
//        if (dO == null) {
//            dO = (DataObject) n.getCookie (DataObject.class);
//        }
//        if (dO == null) {
            return null;
//        }
//        if (dO instanceof org.openide.loaders.DataShadow) {
//            dO = ((org.openide.loaders.DataShadow) dO).getOriginal ();
//        }
//        FileObject fo = dO.getPrimaryFile();
//        getEM().log("Utils.getCurrentObject - returning: " + fo);
//        return fo;
    }
    
    public static String getContextPath(FileObject jsp) {
        try {
            getEM().log("Utils.getContextPath: " + jsp);
            String webRoot = jsp.getFileSystem().getRoot().getPath();
            getEM().log("webroot: " + webRoot);
            String jspPath = jsp.getPath();
            getEM().log("jspPath: " + jspPath);
            String contextPath = jspPath.substring(webRoot.length(), jspPath.length());
            getEM().log("contextPath: " + contextPath);
            return contextPath;
        } catch (FileStateInvalidException fe) {
            return null;
        }
    }
        
    /** 
     * Returns current editor component instance.
     *
     * @return current editor component instance
     */
    public static EditorCookie getCurrentEditorCookie () {
        Node[] nodes = TopComponent.getRegistry ().getActivatedNodes ();
        if ( (nodes == null) ||
             (nodes.length != 1) ) return null;
        Node n = nodes [0];
        return (EditorCookie) n.getCookie (
            EditorCookie.class
        );
    }
    
    /** 
     * Returns current editor component instance.
     *
     * @return current editor component instance
     */
    public static JEditorPane getCurrentEditor (EditorCookie e) {
        JEditorPane[] op = e.getOpenedPanes ();
        if ((op == null) || (op.length < 1)) return null;
        return op [0];
    }
    
    public static Node.Property createProperty(Object instance, Class type,
                                               String name, String dispName,
                                               String shortDesc,
                                               String getter, String setter) {
        Node.Property prop;
        try {
            prop = new PropertySupport.Reflection(instance, type, getter, setter);
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException(ex.getMessage());
        }
        prop.setName(name);
        prop.setDisplayName(dispName);
        prop.setShortDescription(shortDesc);
        return prop;
    }
    
    /**
    * Return line for given params.
    */
    public static Line getLine (String jspName, String ctxRoot, int lineNumber) {
        getEM().log("Utils.getLine for: " + jspName + ":" + lineNumber + ", " + ctxRoot);
        Line.Set ls = getLineSet (jspName, ctxRoot);
        if (ls == null) return null;
        try {
            //Line l = ls.getOriginal (lineNumber - 1);
            Line l = ls.getCurrent(lineNumber - 1);
            FileSystem fs = org.openide.text.DataEditorSupport.findDataObject(l).getPrimaryFile ().getFileSystem ();
//            if (fs.getCapability ().capableOf (GUIManager.DEBUG_SRC))
//                return l;
            //if (fs.isHidden ()) return null;
            getEM().log("Utils.getLine returns: " + l);
            return l;
        } catch (IndexOutOfBoundsException e) {
        } catch (FileStateInvalidException ex) {
        } catch (IllegalArgumentException e) {
        }
        return null;
    }
        
    /**
    * Return line set for given class name.
    */
    public static Line.Set getLineSet (String jspName, String ctxRoot) {
        getEM().log("Utils.getLineSet for: " + jspName + ", " + ctxRoot);
        Enumeration files = null; //Repository.getDefault().findAllResources(jspName); TODO
        if ((files == null) || (!files.hasMoreElements())) {
            return null;
        }
        LineCookie lineCookie = null;
        while (files.hasMoreElements()) {
            
            FileObject file = (FileObject)files.nextElement();
            getEM().log("file: " + file);
            if (file == null) {
                continue;
            }
            DataObject data = null;
            try {
                data = DataObject.find(file);
            } catch (Exception e) {  
                continue;
            }
//            if (data.getCookie(DebuggerCookie.class) == null) {
//                continue;
//            }
            if ((data == null) || !(data instanceof JspDataObject)) {
                continue;
            }
//            WebContextObject wco = (WebContextObject)((JspDataObject)data).getModule();
//            if (wco == null) {
//                continue;
//            }
//            if (!(((ctxRoot == null) && (wco.getContextPath() == null)) || ctxRoot.equals(wco.getContextPath()))) {
//                continue;
//            }
//            lineCookie = (LineCookie) data.getCookie (LineCookie.class);
//            if (lineCookie == null) {
//                continue;
//            }
            
        }

        if (lineCookie == null) {
            getEM().log("returning: null");                                     // NOI18N
            return null;
        }
        
        getEM().log("returning: " + lineCookie.getLineSet());                   // NOI18N
        return lineCookie.getLineSet ();
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

    public static String getELIdentifier () {
        EditorCookie e = getCurrentEditorCookie ();
        if (e == null) {
            return null;
        }
        JEditorPane ep = getCurrentEditor (e);
        if (ep == null) {
            return null;
        }
        return getELIdentifier (
            e.getDocument (),
            ep,
            ep.getCaret ().getDot ()
        );
    }

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
  
    public static ImageIcon getIcon (String iconBase) {
        String n = iconBase + ".gif"; // NOI18N
        if (n.startsWith ("/")) {
            n = n.substring (1);
        }
        ClassLoader currentClassLoader = (ClassLoader) Lookup.getDefault ().
            lookup (ClassLoader.class);
        URL url = currentClassLoader.getResource (n);
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
    
}
/*
 * Utils.java
 *
 * Created on November 1, 2003, 8:24 AM
 */

package org.netbeans.modules.web.debug.util;

import java.util.*;

import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Caret;
import javax.swing.text.StyledDocument;

import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.actions.SystemAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.text.NbDocument;
import org.openide.text.Line;
import org.openide.loaders.DataObject;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.DebuggerCookie;
import org.openide.src.*;
import org.openide.filesystems.Repository;

import org.netbeans.modules.debugger.support.actions.DebuggerWindowPerformer;
import org.netbeans.modules.debugger.support.nodes.DebuggerWindow;
import org.netbeans.modules.debugger.support.actions.AddBreakpointAction;
import org.netbeans.modules.debugger.GUIManager;
import org.netbeans.modules.web.core.jsploader.JspLoader;
import org.netbeans.modules.web.context.WebContextObject;
import org.netbeans.modules.web.core.jsploader.JspDataObject;


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
        Enumeration e, fsenum;
        
        fsenum = Repository.getDefault().getFileSystems();
        getEM().log("fsystems: " + fsenum);

        while (fsenum.hasMoreElements()) {
            FileSystem fs = (FileSystem)fsenum.nextElement();
            getEM().log("fsystem: " + fs);
            if (fs.findResource("WEB-INF") != null) {
                FileObject f = fs.getRoot();
                try {
                    e = f.getFileSystem().getRoot().getChildren(true);
                    getEM().log("children: " + e);
                    while (e.hasMoreElements()) {
                        FileObject ch = (FileObject)e.nextElement();
                        getEM().log("ch: " + ch);
                        if (!ch.isFolder() && !ch.isRoot() && !ch.isVirtual() && ch.isValid() && JspLoader.JSP_MIME_TYPE.equals(ch.getMIMEType())) {
                            String ctx = "";                                    
                            FileObject root = ch.getFileSystem().getRoot();
                            DataObject data = null;
                            try {
                                data = DataObject.find(root);
                            } catch (Exception excep) {
                                // don't care
                            }
                            if ((data instanceof WebContextObject) && (data!=null)) {
                                ctx = ((WebContextObject)data).getContextPath();
                                String idStr = ctx + " : " + ch.getPath();
                                if (!jsps.contains(idStr)) {
                                    jsps.add(idStr);
                                }
                            }
                        }
                    }
                } catch (FileStateInvalidException fe) {
                    // just continue with other fsystem
                }
            }
            
        }
        //Arrays.
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
        if (!fo.getMIMEType().equals(JspLoader.JSP_MIME_TYPE)) return "";        
        return fo.getPath();
    }

    public static FileObject getCurrentFileObject() {
        getEM().log("Utils.getCurrentObject");
        AddBreakpointAction aba = (AddBreakpointAction)SystemAction.get(AddBreakpointAction.class);
        Node[] nodes = aba.getActivatedNodes();
        if (nodes == null) return null;
        if (nodes.length != 1) return null;
        Node n = nodes[0];
        DataObject dO = null;
        if (dO == null) dO = (DataObject) n.getCookie (DataObject.class);
        if (dO == null) return null;
        if (dO instanceof org.openide.loaders.DataShadow)
            dO = ((org.openide.loaders.DataShadow) dO).getOriginal ();
        FileObject fo = dO.getPrimaryFile();
        getEM().log("Utils.getCurrentObject - returning: " + fo);
        return fo;
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
    
    public static Line getCurrentLine () {
        EditorCookie e = getCurrentEditorCookie ();
        if (e == null) return null;
        JEditorPane ep = getCurrentEditor (e);
        if (ep == null) return null;
        StyledDocument d = e.getDocument ();
        if (d == null) return null;
        Line.Set ls = e.getLineSet ();
        if (ls == null) return null;
        Caret c = ep.getCaret ();
        if (c == null) return null;
        Line l = ls.getCurrent( NbDocument.findLineNumber(d, c.getDot()));
        if ( (l == null) ||
             (org.openide.text.DataEditorSupport.findDataObject(l) == null) ||
             (org.openide.text.DataEditorSupport.findDataObject(l).getPrimaryFile () == null)
        ) {
            return null;
        }
        try {
            FileSystem fs = org.openide.text.DataEditorSupport.findDataObject(l).getPrimaryFile ().getFileSystem ();
            if (fs.getCapability ().capableOf (GUIManager.DEBUG_SRC)) {
                return l;
            }
            if (fs.isHidden()) {
                return null;
            }
            return l;
        } catch (FileStateInvalidException ex) {
            return null;
        }
    }
    
    /** 
     * Returns current editor component instance.
     *
     * @return current editor component instance
     */
    public static EditorCookie getCurrentEditorCookie () {
        AddBreakpointAction aba = (AddBreakpointAction) AddBreakpointAction.get(AddBreakpointAction.class);
        Node[] nodes = aba.getActivatedNodes ();
        if ( (nodes == null) || (nodes.length != 1)) {
            return null;
        }
        Node n = nodes [0];
        return (EditorCookie) n.getCookie(EditorCookie.class);
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
    
    public static String getServletClassName(DataObject o) {
        return "";
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
            if (fs.getCapability ().capableOf (GUIManager.DEBUG_SRC))
                return l;
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
        Enumeration files = Repository.getDefault().findAllResources(jspName);
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
            if (data.getCookie(DebuggerCookie.class) == null) {
                continue;
            }
            if ((data == null) || !(data instanceof JspDataObject)) {
                continue;
            }
            WebContextObject wco = (WebContextObject)((JspDataObject)data).getModule();
            if (wco == null) {
                continue;
            }
            if (!ctxRoot.equals(wco.getContextPath())) {
                continue;
            }
            lineCookie = (LineCookie) data.getCookie (LineCookie.class);
            if (lineCookie == null) {
                continue;
            }
            
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

    public static Boolean isScriptlet(StyledDocument doc, JEditorPane ep, int offset) {
        String t;
        int line = NbDocument.findLineNumber(doc, offset);
        int col = NbDocument.findLineColumn(doc, offset);
//        getEM().log("isscriptlet: line: " + line + ", col: " + col);
        try {
            while (line > 0) {
//                getEM().log("line: " + line);
                javax.swing.text.Element lineElem = 
                    org.openide.text.NbDocument.findLineRootElement(doc).getElement(line);
//                getEM().log("lineelem: " + lineElem);
                if (lineElem == null) {
                    continue;
                }
                int lineStartOffset = lineElem.getStartOffset();
                int lineLen = lineElem.getEndOffset() - lineStartOffset;
                t = doc.getText (lineStartOffset, lineLen);
//                getEM().log("t: " + t);
                if ((t != null) && (t.length() > 1)) {
                    int identStart;
                    if (line == NbDocument.findLineNumber(doc, offset)) {
                        identStart = col;
                    } else {
                        identStart = lineLen-1;
                    }
//                    getEM().log("identstart: " + identStart);
                    while (identStart > 0) {
//                        getEM().log("chars: " + t.charAt(identStart-1) + "|" + t.charAt(identStart));
                        if ((t.charAt(identStart) == '%') && (t.charAt(identStart-1) == '<')) {
//                            getEM().log("found opening");
                            return Boolean.TRUE;
                        }
                        if ((t.charAt(identStart) == '>') && (t.charAt(identStart-1) == '%')) {
//                            getEM().log("found closing");
                            return Boolean.FALSE;
                        }                    
                        identStart--;
                    }
                }
                line--;
            }
//            getEM().log("not found anything");
            return Boolean.FALSE;
        } catch (javax.swing.text.BadLocationException e) {
            return Boolean.FALSE;
        }
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

            if (lineElem == null) return null;
            int lineStartOffset = lineElem.getStartOffset ();
            int lineLen = lineElem.getEndOffset() - lineStartOffset;
            t = doc.getText (lineStartOffset, lineLen);
            int identStart = col;
            while (identStart > 0 && (t.charAt(identStart) != '$')) {
                identStart--;
            }
            if ((identStart > 0) && (t.charAt(identStart) == '$') && (t.charAt(identStart-1) == '\\')) {
//                getEM().log("the $ sign is not valid");
                return null;
            }
            int identEnd = col;
            while ((identEnd < lineLen) && identEnd > 0 && identEnd <= t.length() && (t.charAt(identEnd-1) != '}'))  {
                identEnd++;
            }
            if (identStart == identEnd) return null;
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
        if (e == null) return null;
        JEditorPane ep = getCurrentEditor (e);
        if (ep == null) return null;
        return getJavaIdentifier (
            e.getDocument (),
            ep,
            ep.getCaret ().getDot ()
        );
    }

    public static String getELIdentifier () {
        EditorCookie e = getCurrentEditorCookie ();
        if (e == null) return null;
        JEditorPane ep = getCurrentEditor (e);
        if (ep == null) return null;
        return getELIdentifier (
            e.getDocument (),
            ep,
            ep.getCaret ().getDot ()
        );
    }

    public static Boolean isScriptlet() {
        EditorCookie e = getCurrentEditorCookie ();
        if (e == null) return null;
        JEditorPane ep = getCurrentEditor (e);
        if (ep == null) return null;
        return isScriptlet(
            e.getDocument (),
            ep,
            ep.getCaret ().getDot ()
        );
    }
  
    public static void setViewVisibility (final GUIManager.View v, final boolean visible) {
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                DebuggerWindow dw = DebuggerWindowPerformer.getDebuggerWindow ();
                dw.setVisible (v, visible);
            }
        });
    }
    
}

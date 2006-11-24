/*
 * CSS.java
 *
 * Created on May 18, 2006, 1:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.languages.css;

import org.netbeans.modules.languages.parser.ASTNode;
import org.netbeans.modules.languages.parser.PTPath;
import org.netbeans.modules.languages.parser.SToken;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jan Jancura
 */
public class CSS {

    public static String tooltip (PTPath path) {
        ASTNode n = (ASTNode) path.getLeaf ();
        StringBuilder sb = new StringBuilder ();
        sb.append ("<html>");
        sb.append ("<p style=\"");
        ASTNode body = n.getParent ().getNode ("body");
        if (body == null) return null;
        ASTNode declarations = body.getNode ("declarations");
        if (declarations == null) return null;
        String s = declarations.getAsText ();
        s = s.replaceAll ("\"", "");
        sb.append (s);
        sb.append ("\">Text Preview.</p>");
        sb.append ("</html>");
        return sb.toString ();
    }

    public static String navigatorTooltip (PTPath path) {
        ASTNode n = (ASTNode) path.getLeaf ();
        StringBuilder sb = new StringBuilder ();
        sb.append ("<html>");
        sb.append ("<p style=\"");
        ASTNode body = n.getNode ("body");
        if (body == null) return null;
        ASTNode declarations = body.getNode ("declarations");
        if (declarations == null) return null;
        String s = declarations.getAsText ();
        s = s.replaceAll ("\"", "");
        sb.append (s);
        sb.append ("\">Text Preview.</p>");
        sb.append ("</html>");
        return sb.toString ();
    }
    
    public static Runnable hyperlink (PTPath path) {
        SToken t = (SToken) path.getLeaf ();
        String s = t.getIdentifier ();
        s = s.substring (1, s.length () - 1);
        s = s.replace ("%20", " "); // HACK
        Lookup l = TopComponent.getRegistry ().getActivated ().getLookup ();
        DataObject dob = (DataObject) l.lookup (DataObject.class);
        FileObject fo = dob.getPrimaryFile ().getParent ();
        final FileObject file = fo.getFileObject (s);
        if (file != null)
            return new Runnable () {
                public void run () {
                    try {
                        DataObject d = DataObject.find (file);
                        OpenCookie oc = (OpenCookie) d.getCookie (OpenCookie.class);
                        oc.open ();
                    } catch (DataObjectNotFoundException ex) {
                    }
                }
            };
        return null;
    }
}

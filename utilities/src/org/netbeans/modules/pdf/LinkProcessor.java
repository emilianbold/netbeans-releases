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

package org.netbeans.modules.pdf;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

import org.openide.TopManager;
import org.openide.cookies.InstanceCookie;
import org.openide.loaders.XMLDataObject;
import org.openide.util.NbBundle;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Permits a special kind of .xml file to be used for PDF links.
 * After this processor is registered, any .xml file which matches
 * the specified DTD (it must declare a <code>&lt;!DOCTYPE&gt;</code>)
 * will provide an instance of a {@link JMenuItem}.
 * This menu item will be named according to the XML file's display
 * name (which may be controlled via localized filenames from a
 * bundle as elsewhere).
 * Selecting it will try to show the mentioned PDF file.
 * The PDF file may be referred to as an absolute file name,
 * or as a localized path within the IDE installation,
 * or (in the future) as an arbitrary URL.
 * The XML file is suitable for direct inclusion in a menu
 * bar folder, for example <samp>..../system/Menu/Help/</samp>.
 * @author Jesse Glick
 */
public class LinkProcessor implements InstanceCookie, XMLDataObject.Processor, ActionListener {

    public static final String PUBLIC_ID = "-//NetBeans//DTD PDF Document Menu Link 1.0//EN"; // NOI18N
    public static final String PUBLIC_WWW = "http://www.netbeans.org/dtds/pdf_link-1_0.dtd"; // NOI18N
    
    public static void init () {
        XMLDataObject.registerCatalogEntry
            (PUBLIC_ID, "org/netbeans/modules/pdf/pdf_link.dtd", LinkProcessor.class.getClassLoader ()); // NOI18N
        XMLDataObject.Info info = new XMLDataObject.Info ();
        info.setIconBase ("/org/netbeans/modules/pdf/PDFDataIcon"); // NOI18N
        info.addProcessorClass (LinkProcessor.class);
        XMLDataObject.registerInfo (PUBLIC_ID, info);
    }
    
    private XMLDataObject obj;

    public void attachTo (XMLDataObject obj) {
        this.obj = obj;
    }
    
    public Class instanceClass () throws IOException, ClassNotFoundException {
        return JMenuItem.class;
    }
    
    public Object instanceCreate () throws IOException, ClassNotFoundException {
        String name = obj.getNodeDelegate ().getDisplayName ();
        Icon icon = new ImageIcon (Toolkit.getDefaultToolkit ().getImage
            (LinkProcessor.class.getResource ("PDFDataIcon.gif"))); // NOI18N
        // [PENDING] chop mnemonics
        JMenuItem mi = new JMenuItem (name, icon);
        mi.addActionListener (this);
        return mi;
    }

    public void actionPerformed (ActionEvent ev) {
        try {
            // [PENDING] better exceptions, ideally--not toString()
            Document doc = obj.getDocument ();
            Element pdfEl = doc.getDocumentElement ();
            NodeList ns = pdfEl.getChildNodes ();
            Node n = null;
            for (int i = 0; i < ns.getLength (); i++) {
                Node nn = ns.item (i);
                if (nn.getNodeType () == Node.ELEMENT_NODE) {
                    if (n != null) throw new Exception (doc.toString ());
                    n = nn;
                }
            }
            if (n == null) throw new Exception (doc.toString ());
            Element innerEl = (Element) n;
            String type = innerEl.getTagName ();
            File f;
            if (type.equals ("file")) { // NOI18N
                f = new File ((String) innerEl.getAttribute ("path")); // NOI18N
            } else if (type.equals ("idefile")) { // NOI18N
                String base = (String) innerEl.getAttribute ("base"); // NOI18N
                Map m = new HashMap (); // Map<String,File>
                String home = System.getProperty ("netbeans.home"); // NOI18N
                if (home != null)
                    addAll (home, base, m);
                String user = System.getProperty ("netbeans.user"); // NOI18N
                if (user != null && ! user.equals (home))
                    addAll (user, base, m);
                f = (File) NbBundle.getLocalizedValue (m, ""); // NOI18N
            } else if (type.equals ("url")) { // NOI18N
                throw new Exception ("unimplemented"); // NOI18N
            } else {
                throw new Exception (doc.toString ());
            }
            // [PENDING] in-process PDF viewer support
            new PDFOpenSupport (f).open ();
        } catch (Exception e) {
            TopManager.getDefault ().notifyException (e);
        }
    }
    
    private static void addAll (String idehome, String base, Map m) {
        int idx;
        String dir = idehome;
        while ((idx = base.indexOf ('.')) != -1) {
            dir += File.separatorChar + base.substring (0, idx);
            base = base.substring (idx + 1);
        }
        File f = new File (dir);
        File[] kids = f.listFiles ();
        for (int i = 0; i < kids.length; i++) {
            String name = kids[i].getName ();
            String ext = ".pdf"; // NOI18N
            if (name.startsWith (base) && name.endsWith (ext)) {
                String key = name.substring (base.length (), name.length () - ext.length ());
                m.put (key, kids[i]);
            }
        }
    }
    
    public String instanceName () {
        return obj.getName ();
    }
    
}

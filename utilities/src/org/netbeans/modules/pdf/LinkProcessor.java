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


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

import org.openide.cookies.InstanceCookie;
import org.openide.loaders.XMLDataObject;
import org.openide.TopManager;
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
 *
 * @author Jesse Glick
 * @see org.openide.loaders.XMLDataObject.Processor
 */
public class LinkProcessor implements InstanceCookie, XMLDataObject.Processor, ActionListener {

    /** Public ID of catalog. */
    public static final String PUBLIC_ID = "-//NetBeans//DTD PDF Document Menu Link 1.0//EN"; // NOI18N
    /** */
    public static final String PUBLIC_WWW = "http://www.netbeans.org/dtds/pdf_link-1_0.dtd"; // NOI18N

    /** <code>XMLDataObject</code> this processor is linked to. */
    private XMLDataObject xmlDataObject;
    
    

    /** Initilializes <code>LinkProcessor</code>. */
    public static void init () {
        // Registering of catalog is in xml layer, see org/netbeans/modules/utilities/Layer.xml.
        
        XMLDataObject.Info xmlInfo = new XMLDataObject.Info ();
        
        xmlInfo.setIconBase("/org/netbeans/modules/pdf/PDFDataIcon"); // NOI18N
        xmlInfo.addProcessorClass(LinkProcessor.class);
        XMLDataObject.registerInfo(PUBLIC_ID, xmlInfo);
    }

    /** Attaches this processor to specified xml data object. Implements <code>XMLDataObject.Processor</code> interface. 
     * @param xmlDataObject xml data object to which attach this processor */
    public void attachTo(XMLDataObject xmlDataObject) {
        this.xmlDataObject = xmlDataObject;
    }

    /** Gets instance class. Implements <code>InstanceCookie</code> interface method. 
     * @return <code>JMenuItem</code> class */
    public Class instanceClass() throws IOException, ClassNotFoundException {
        return JMenuItem.class;
    }

    /** Creates instance. Implements <code>InstanceCookie</code> interface method. */
    public Object instanceCreate() throws IOException, ClassNotFoundException {
        String name = xmlDataObject.getNodeDelegate().getDisplayName();
        
        Icon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage
            (LinkProcessor.class.getResource("PDFDataIcon.gif"))); // NOI18N
        // [PENDING] chop mnemonics
        JMenuItem menuItem = new JMenuItem(name, icon);
        menuItem.addActionListener(this);
        
        return menuItem;
    }
    
    /** Gets name of instance. Implements <code>InstanceCookie</code> interface method. 
     * @return name of <code>xmlDataObject</code> */
    public String instanceName() {
        return xmlDataObject.getName();
    }

    /** Performs action. Retrieves pdf data obect from specified xml one and opens it.
     * Implements <code>ActionListener</code> interface method. */
    public void actionPerformed(ActionEvent evt) {
        try {
            // [PENDING] better exceptions, ideally--not toString()
            Document document = xmlDataObject.getDocument();
            Element pdfLinkElement = document.getDocumentElement();
            
            NodeList nodeList = pdfLinkElement.getChildNodes ();
            Node node = null;
            
            for(int i = 0; i < nodeList.getLength(); i++) {
                Node nextNode = nodeList.item(i);
                if(nextNode.getNodeType() == Node.ELEMENT_NODE) {
                    if(node != null) 
                        throw new Exception(document.toString());
                    
                    node = nextNode;
                }
            }
            
            if(node == null)
                throw new Exception(document.toString());
            
            Element innerElement = (Element)node;
            
            String type = innerElement.getTagName();
            
            // Retrieve pdf file.
            File file;
            
            if("file".equals(type)) { // NOI18N
                file = new File((String)innerElement.getAttribute("path")); // NOI18N
            } else if("idefile".equals(type)) { // NOI18N
                String base = (String)innerElement.getAttribute("base"); // NOI18N
                
                Map map = new HashMap(); // Map<String,File>
                String home = System.getProperty("netbeans.home"); // NOI18N
                
                if(home != null)
                    addAll(home, base, map);
                String user = System.getProperty("netbeans.user"); // NOI18N
                
                if(user != null && ! user.equals(home))
                    addAll(user, base, map);
                
                file = (File)NbBundle.getLocalizedValue(map, ""); // NOI18N
            } else if("url".equals(type)) { // NOI18N
                throw new Exception("PDF: unimplemented."); // NOI18N
            } else {
                throw new Exception(document.toString());
            }
            
            // [PENDING] in-process PDF viewer support
            new PDFOpenSupport(file).open();
        } catch(Exception e) {
            TopManager.getDefault().notifyException(e);
        }
    }
    
    /** Adds all .pdf files from package specified by idehome starting with
     * base name to map.
     * @param idehome name of dir to search
     * @param base base name of .pdf file it has to start with 
     * @param map map where found name <code>String</code>, <code>File</code> are put */
    private static void addAll(String idehome, String base, Map map) {
        int index;
        
        String dir = idehome;
        
        while((index = base.indexOf('.')) != -1) {
            dir += File.separatorChar + base.substring (0, index);
            base = base.substring (index + 1);
        }
        
        File homeDir = new File(dir);
        
        File[] kids = homeDir.listFiles();
        
        for (int i = 0; i < kids.length; i++) {
            String name = kids[i].getName ();
            String ext = ".pdf"; // NOI18N
            
            if(name.startsWith(base) && name.endsWith(ext)) {
                String key = name.substring(base.length(), name.length() - ext.length());
                map.put(key, kids[i]);
            }
        }
    }
    
}

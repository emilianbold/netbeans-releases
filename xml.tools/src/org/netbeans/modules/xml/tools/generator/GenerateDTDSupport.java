/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.tools.generator;

import java.io.*;
import java.util.*;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.util.UserCancelException;

import org.netbeans.tax.*;
import org.netbeans.modules.xml.core.XMLDataObject;
import org.netbeans.modules.xml.tools.lib.GuiUtil;

/**
 * GenerateDTDSupport class generate a DTD by guessing it from
 * XML document.
 *
 * @author   Libor Kramolis
 * @author   Petr Kuzel 
 */
public class GenerateDTDSupport implements XMLGenerateCookie {
    static final String DTD_EXT = "dtd"; // NOI18N

    DataObject DO;
    TreeElement element;
//      ElementNode node;


    public GenerateDTDSupport (XMLDataObject DO) {
  	this.DO = DO;
  	this.element = null;
    }


//      public GenerateDTDSupport (DataObject DO, TreeElement element) {
//          this.DO = DO;
//          this.element = element;
//      }

//      public GenerateDTDSupport (ElementNode node, TreeElement element) {
//          this.node = node;
//          this.element = element;
//      }


    public void generate () {
//          if (DO == null && node != null) {  // init now getRootNode() should not return null
//              this.DO = node.getDataNode().getDataObject();
//              if (DO == null)
//  		return; 
//          }

        try {
	    TreeDocument treeDoc = (TreeDocument)((XMLDataObject)DO).getDocumentRoot();
            if (treeDoc == null)
                return;

            if (element == null)
                element = treeDoc.getDocumentElement();
            if (element == null)
                return;

            FileObject primFile = DO.getPrimaryFile();
            String name = primFile.getName() + "_" + element.getQName(); // NOI18N
            FileObject folder = primFile.getParent();

            FileObject generFile = (new SelectFileDialog (folder, name, DTD_EXT)).getFileObject();
            name = generFile.getName();

	    String encoding = null;
	    try {
		encoding = element.getOwnerDocument().getEncoding();
	    } catch (NullPointerException e) { /* NOTHING */ }

            String dtd = xml2dtd (element, name, encoding);

            // write to file
            FileLock lock = null;
            Writer writer = null;
            try {
                lock = generFile.lock ();
		encoding = TreeUtilities.iana2java (encoding == null ? "UTF-8" : encoding); // NOI18N
		OutputStream output = generFile.getOutputStream (lock);
		try {
		    writer = new OutputStreamWriter (output, encoding);
		} catch (UnsupportedEncodingException e) {
		    writer = new OutputStreamWriter (output);
		}
                writer = new PrintWriter (writer);
                writer.write (dtd.toString());
                lock.releaseLock ();
            } finally {
                if (writer != null)
                    writer.close();
                if (lock != null)
                    lock.releaseLock();
            }
	    trySetDocumentType (name);

            GenerateSupportUtils.tryEditFile (generFile);

        } catch (UserCancelException e) {
//          } catch (FileStateInvalidException e) {
//          } catch (TreeException e) {
//          } catch (IOException e) {
        } catch (Exception exc) {
            Util.notifyException (exc);
        }
    }

    private void trySetDocumentType (String fileName) {
	if ( element.getParentNode() instanceof TreeDocument ) { // try to set only when element is root document element
            TreeDocument document = (TreeDocument)element.getParentNode();
            if ( GuiUtil.confirmAction (Util.getString ("MSG_use_dtd_as_document_type?")) ) {
		try {
		    TreeDocumentType newDoctype = new TreeDocumentType (element.getQName(), null, fileName + "." + DTD_EXT); // NOI18N
                    document.setDocumentType (newDoctype);
//    		    ((XMLDataNode)DO.getNodeDelegate()).setDocumentType (newDoctype);
		} catch (TreeException exc) {
		    Util.notifyTreeException (exc);
		}
	    }
	}
    }
	

    String xml2dtd (TreeElement element, String name, String encoding) {
        StringBuffer sb = new StringBuffer();
	
	if (encoding != null)
	    sb.append ("<?xml version='1.0' encoding='").append (encoding).append ("'?>\n\n"); // NOI18N

        String tagName = element.getQName();
        sb.append ("<!--\n").append ("    Typical usage:\n\n").append ("    <?xml version=\"1.0\"?>\n\n"). // NOI18N
        append ("    <!DOCTYPE ").append (tagName).append (" SYSTEM \""). // NOI18N
        append (name).append (".").append (DTD_EXT).append ("\">\n\n"). // NOI18N
        append ("    <").append (tagName).append (">\n    ...\n").append ("    </").append (tagName).append (">\n"). // NOI18N
        append ("-->\n"); // NOI18N

        
        // fill table of dtd declarations
        
        HashMap table = new HashMap();
        
        fillTable (element, table);
        
        // generate DTD contaent by the table
        
        Iterator I = table.values().iterator();
        DTDElement elem;
        while (I.hasNext()) {
            sb.append ("\n"); // NOI18N
            elem = (DTDElement)I.next();
            // <!ELEMENT ...
            sb.append ("  <!ELEMENT ").append (elem.name).append (" "); // NOI18N
            if ((elem.pcdata == false) && (elem.children.size() == 0)) {
                sb.append ("EMPTY"); // NOI18N
            } else {
                Collection collect = elem.children.values();
                if (elem.pcdata) {
                    Vector vect = new Vector (collect);
                    vect.insertElementAt (new String ("#PCDATA"), 0); // NOI18N
                    collect = vect;
                }
                Iterator I2 = collect.iterator();
                String elemName;
                elemName = (String)I2.next();
                sb.append ("(").append (elemName); // NOI18N
                while (I2.hasNext()) {
                    elemName = (String)I2.next();
                    sb.append ("|").append (elemName); // NOI18N
                }

                //!!!HACK #6928
                if (!sb.toString().endsWith("#PCDATA")) { // NOI18N
                    sb.append (")*"); // NOI18N
                } else {
                    sb.append(")"); // NOI18N 
                }
            }
            sb.append (">\n"); // NOI18N
            
            // <!ATTLIST ...
            if (elem.attributes.size() != 0) {
                sb.append ("  <!ATTLIST ").append (elem.name).append ("\n"); // NOI18N
                Iterator I3 = elem.attributes.values().iterator();
                String attName;
                while (I3.hasNext()) {
                    attName = (String)I3.next();
                    sb.append ("    ").append (attName).append (" CDATA #IMPLIED\n"); // NOI18N
                }
                sb.append ("  >\n"); // NOI18N
            }
        }

        return sb.toString();
    }

    /**
     * Fills table by parameters got from element. 
     * <p>Recursive method!
     */
    void fillTable (TreeElement element, HashMap table) {
        String name = element.getQName();
        Iterator nodes = element.getChildNodes().iterator();
        TreeNamedObjectMap attrs = element.getAttributes();
        
        // init dtd declarations map
        
        DTDElement dtdElem = (DTDElement)table.get (name);
        if (dtdElem == null) {
            dtdElem = new DTDElement (name);
            table.put (name, dtdElem);
        }        
        
        // check for attributes
        
        for (int i = 0; i < attrs.size(); i++) {
            dtdElem.addAttribute (((TreeAttribute)attrs.get(i)).getQName());
        }
        
        // check content
        
        while (nodes.hasNext()) {
            TreeNode node = (TreeNode)nodes.next();

            if (node instanceof TreeElement) {
		TreeElement elem = (TreeElement)node;
		dtdElem.addChild(elem.getQName());
		fillTable (elem, table);  // recursion entry point
	    } else if (node instanceof TreeCDATASection) {
                dtdElem.hasPCDATA();
            } else if (node instanceof TreeEntityReference) {
                dtdElem.hasPCDATA();
	    } else if (node instanceof TreeText) {
		if (!!! dtdElem.isTextAllowed()) {
		    // perform check for PCDATA
                    
		    TreeText text = (TreeText) node;
		    String data = text.getData();
		    if (!!! wsOnly(data)) {  //!!! could be ui parametrized
			dtdElem.hasPCDATA();
		    }
		}
            }
        }
    }

    
    /**
     * Return true if parameter contains just white spaces.
     */
    private boolean wsOnly(String s) {
        if (s == null) return true;
        
        char[] data = s.toCharArray();
        for (int i = 0; i<data.length; i++) {
            if (Character.isWhitespace(data[i]) == false) {
                return false;
            }
        }
        
        return true;
    }
    

    // class DTDElement
    private class DTDElement {
        String name;
        HashMap children;
        HashMap attributes;
        boolean pcdata;

        public DTDElement (String name) {
            this.name = name;
            children = new HashMap();
            attributes = new HashMap();
            pcdata = false;
        }

        public void hasPCDATA () {
            pcdata = true;
        }

        public boolean isTextAllowed() {
            return pcdata;
        }
        
        public void addChild (String child) {
            children.put (child, child);
        }

        public void addAttribute (String attr) {
            attributes.put (attr, attr);
        }
                
    } // end of inner class DTDElement

}

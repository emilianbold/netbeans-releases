/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.settings;

import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.InstanceDataObject;
import org.openide.xml.EntityCatalog;
import org.openide.xml.XMLUtil;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author  Jan Pokorsky
 */
public class DOMConvertorTest extends NbTestCase {
    FileSystem fs;
    
    /** Creates a new instance of EnvTest */
    public DOMConvertorTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new org.netbeans.junit.NbTestSuite(DOMConvertorTest.class));
        System.exit(0);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        
        org.openide.TopManager.getDefault();
        fs = org.openide.filesystems.Repository.getDefault().getDefaultFileSystem();
    }
    
    public void testCreateSetting() throws Exception {
        org.openide.filesystems.FileUtil.createFolder(fs.getRoot(), "testCreateSetting");
        DataFolder folder = DataFolder.findFolder(fs.findResource("testCreateSetting"));
        
        ComposedSetting cs = new ComposedSetting();
        cs.b1 = new java.awt.Button();
        cs.b2 = cs.b1;
        DataObject dobj = InstanceDataObject.create(folder, "testCreateSetting", cs, null);
        
        // test reading
        FileObject fo = dobj.getPrimaryFile().copy(fs.getRoot(), dobj.getPrimaryFile().getName() + "_copy", "settings");
        org.openide.cookies.InstanceCookie ic = (org.openide.cookies.InstanceCookie)
            DataObject.find(fo).getCookie(org.openide.cookies.InstanceCookie.class);
        assertNotNull("missing InstanceCookie", ic);
        assertEquals(cs.getClass(), ic.instanceClass());
        
        ComposedSetting cs2 = (ComposedSetting) ic.instanceCreate();
        assertEquals(cs2.b1, cs2.b2);
    }
    
    public static class ComposedSetting {
        java.awt.Button b1;
        java.awt.Button b2;
    }
    
    public static class ComposedSettingConvertor extends DOMConvertor {
        private final static String PUBLIC_ID = "-//NetBeans org.netbeans.modules.settings.xtest//DTD ComposedSetting 1.0//EN"; // NOI18N
        private final static String SYSTEM_ID = "nbres:/org/netbeans/modules/settings/convertors/data/composedsetting-1_0.dtd"; // NOI18N
        private final static String ELM_COMPOSED_SETTING = "composedsetting"; // NOI18N
        
        public Object read(java.io.Reader r) throws java.io.IOException, ClassNotFoundException {
            try {
                XMLReader xr = XMLUtil.createXMLReader(false, false);
                InputSource is = new InputSource(r);
                Document doc = XMLUtil.parse(is, false, false, null, EntityCatalog.getDefault());
                return readElement(doc.getDocumentElement());
            } catch (SAXException ex) {
                IOException ioe = new IOException(ex.getLocalizedMessage());
                ErrorManager emgr = ErrorManager.getDefault();
                emgr.annotate(ioe, ex);
                if (ex.getException () != null) {
                    emgr.annotate (ioe, ex.getException());
                }
                throw ioe;
            }
        }
        
        protected Object readElement(org.w3c.dom.Element element) throws java.io.IOException, ClassNotFoundException {
            if (!element.getTagName().equals(ELM_COMPOSED_SETTING)) {
                throw new IllegalArgumentException("required element: " +
                    ELM_COMPOSED_SETTING + ", but was: " + element.getTagName());
            }
            
            ComposedSetting cs = new ComposedSetting();
            NodeList nl = element.getChildNodes();
            for (int i = 0, len = nl.getLength(); i < len; i++) {
                Node n = nl.item(i);
                if (n instanceof Element) {
                    Object obj = delegateRead((Element) n);
                    if (obj instanceof java.awt.Button) {
                        if (cs.b1 == null) {
                            cs.b1 = (java.awt.Button) obj;
                        } else {
                            cs.b2 = (java.awt.Button) obj;
                        }
                    }
                }
            }
            return cs;
        }
        
        public void registerSaver(Object inst, Saver s) {
        }
        
        public void unregisterSaver(Object inst, Saver s) {
        }
        
        public void write(java.io.Writer w, Object inst) throws java.io.IOException {
            try {
                Document doc = XMLUtil.createDocument(ELM_COMPOSED_SETTING, null, PUBLIC_ID, SYSTEM_ID);
                writeElement(doc, doc.getDocumentElement(), inst);
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream(1024);
                XMLUtil.write(doc, baos, "UTF-8"); // NOI18N
                w.write(baos.toString("UTF-8")); // NOI18N
            } catch (DOMException ex) {
                throw (IOException) ErrorManager.getDefault().annotate(new IOException(ex.getLocalizedMessage()), ex);
            }
        }
        
        protected org.w3c.dom.Element writeElement(org.w3c.dom.Document doc, Object obj) throws java.io.IOException, org.w3c.dom.DOMException {
            Element el = doc.createElement(ELM_COMPOSED_SETTING);
            writeElement(doc, el, obj);
            el.setAttribute(ATTR_PUBLIC_ID, PUBLIC_ID);
            return el;
        }
        
        private void writeElement(org.w3c.dom.Document doc, org.w3c.dom.Element el, Object inst) throws java.io.IOException, org.w3c.dom.DOMException {
            if (!(inst instanceof ComposedSetting)) {
                throw new IllegalArgumentException("required: " + ComposedSetting.class.getName() + " but was: " + inst.getClass());
            }
            ComposedSetting cs = (ComposedSetting) inst;
            // test CDATA wrapping
            Element subel = delegateWrite(doc, cs.b1);
            el.appendChild(subel);
            subel = delegateWrite(doc, cs.b2);
            el.appendChild(subel);
        }
        
    }
}

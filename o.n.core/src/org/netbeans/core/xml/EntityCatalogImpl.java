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

package org.netbeans.core.xml;

import java.io.*;
import java.util.*;

import javax.swing.event.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.openide.loaders.*;
import org.openide.cookies.InstanceCookie;
import org.openide.util.*;
import org.openide.xml.*;

/**
 * This Entity Catalog implementation recognizes registrations defined at XMLayer.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class EntityCatalogImpl extends EntityCatalog {

    /** map between publicId and privateId (String, String) */
    private Map id2uri;  

    private static final XMLDataObject.Info INFO = new XMLDataObject.Info();

    public static void init () {
        INFO.addProcessorClass(RegistrationProcessor.class);
//        INFO.setIconBase("/org/netbeans/core/windows/toolbars/xmlToolbars");  //!!!
        XMLDataObject.registerInfo(EntityCatalog.PUBLIC_ID, INFO);
    }
    
    /** Creates new EntityCatalogImpl */
    private EntityCatalogImpl(Map map) {
        id2uri = map;
    }
    
    /**
     * Resolve an entity using cached mapping.
     */
    public InputSource resolveEntity(String publicID, String systemID) {
        synchronized (id2uri) {
            if (publicID == null) return null;

            String res = (String) id2uri.get(publicID);

            InputSource ret = null;
            if (res != null) {
                ret = new InputSource(res);
            }
            
//            System.err.println("" + publicID + " => " + ret);
            return ret;
        }
    }

    /** 
     * XMLDataObject.Processor implementation recognizing EntityCatalog.PUBLIC_ID DTDs
     * giving them instance cookie returning registered entries.
     */
    public static class RegistrationProcessor extends DefaultHandler implements XMLDataObject.Processor, InstanceCookie, Runnable {

        private XMLDataObject peer;
        private Map map;
        private RequestProcessor.Task parsingTask = RequestProcessor.createRequest(this);

        // Processor impl

        public void attachTo (XMLDataObject xmlDO) {
            peer = xmlDO;                        
            map = new Hashtable();  //be synchronized
            parsingTask.schedule(0);
        }

        // DefaultHandler extension

        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
            if ("public".equals(qName)) {  //NOI18N
                String key = atts.getValue("publicId");  //NOI18N
                String val = atts.getValue("uri");  //NOI18N

                if (key != null && val != null) {
                    map.put(key, val);
                }
            }
        }

        // Runnable impl (can be a task body)

        public void run() {
            map.clear();

            try {
                String loc = peer.getPrimaryFile().getURL().toExternalForm();
                InputSource src = new InputSource(loc);
                XMLReader reader = XMLUtil.createXMLReader(true);
                reader.setErrorHandler(this);
                reader.setContentHandler(this);
                reader.setEntityResolver( new EntityResolver() {
                    public InputSource resolveEntity(String pid, String sid) {
                        if (EntityCatalog.PUBLIC_ID.equals(pid)) {
                            return new InputSource("nbresboot:/org/openide/xml/EntityCatalog.dtd");
                        }
                        return null;
                    }
                });
                reader.parse(src);
            } catch (SAXException ex) {
                // ignore
                //ex.printStackTrace();
            } catch (IOException ex) {
                // ignore
                //ex.printStackTrace();
            }
        }

        // InstanceCookie impl

        public Class instanceClass() throws IOException, ClassNotFoundException {
            return EntityCatalogImpl.class;
        }

        public Object instanceCreate() throws IOException, ClassNotFoundException {
            parsingTask.waitFinished();

            return new EntityCatalogImpl (map);
        }

        //do not understand what it means, but it must return the value
        public String instanceName() {
            return "org.netbeans.core.xml.EntityCatalogImpl"; // NOI18N
        }
    }
}

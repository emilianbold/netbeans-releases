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
public class EntityCatalogImpl extends EntityCatalog implements LookupListener {

    private Hashtable id2uri;  //cache of registered mappings

    private static final XMLDataObject.Info INFO = new XMLDataObject.Info();

    private Lookup.Result result;  //lookup result

    static {
        INFO.addProcessorClass(RegistrationProcessor.class);
//        INFO.setIconBase("/org/netbeans/core/windows/toolbars/xmlToolbars");  //!!!
        XMLDataObject.registerInfo(EntityCatalog.PUBLIC_ID, INFO);
    }
    
    /** Creates new EntityCatalogImpl */
    public EntityCatalogImpl() {
        id2uri = new Hashtable(17);
        initLookupListening();        
    }
    
    /**
     * Resolve an entity using cached mapping.
     */
    public InputSource resolveEntity(String publicID, String systemID) {
        synchronized (id2uri) {
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
     * Lookup result callback handler updates cache. 
     */
    public void resultChanged(LookupEvent e) {
        
        synchronized (id2uri) {            
            id2uri.clear();

            Collection col = result.allInstances();
            Iterator it = col.iterator();

            while( it.hasNext() ) {
                Entry next = (Entry) it.next();
                id2uri.putAll(next.mapping);
            }
        }
    }

    /**
     * Start listening at Lookup.Result.
     */
    private void initLookupListening() {
        Lookup.Template templ = new Lookup.Template(Entry.class);
        result = Lookup.getDefault().lookup(templ);

        result.addLookupListener(this);
        resultChanged(null);
    }

    /** 
     * XMLDataObject.Processor implementation recognizing EntityCatalog.PUBLIC_ID DTDs
     * giving them instance cookie returning registered entries.
     */
    public static class RegistrationProcessor extends DefaultHandler implements XMLDataObject.Processor, InstanceCookie, Runnable {

        private XMLDataObject peer;
        private TreeMap map;
        private Entry instance;
        private RequestProcessor.Task parsingTask = RequestProcessor.createRequest(this);

        // Processor impl

        public void attachTo (XMLDataObject xmlDO) {
            peer = xmlDO;                        
            map = new TreeMap();
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

        public void endDocument() {
            instance = new Entry();
            instance.setMap(map);
        }
        
        // Runnable impl (can be a task body)

        public void run() {

            instance = null;
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
            return Entry.class;
        }

        public Object instanceCreate() throws IOException, ClassNotFoundException {
            parsingTask.waitFinished();

            if (instance == null) {
                throw new IOException("Error parsing " + peer.getPrimaryFile().getName());  //NOI18N
            }
            return instance;
        }

        public String instanceName() {
            return "Entity Catalog Entries (" + peer.getPrimaryFile().getName() + ")";  //!!!
        }
    }

    /** 
     * Set of registrations recognized by RegistrationProcessor and provided at XMLayer. 
     */
    public static class Entry {

        Map mapping;

        void setMap(Map mapping) {
            this.mapping = mapping;
        }
    }    
}

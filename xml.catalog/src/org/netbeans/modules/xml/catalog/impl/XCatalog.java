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
package org.netbeans.modules.xml.catalog.impl;

import java.awt.*;
import java.io.*;
import java.beans.*;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.openide.util.*;
import org.openide.xml.XMLUtil;

import org.netbeans.modules.xml.catalog.spi.*;
import org.netbeans.modules.xml.catalog.lib.*;

/**
 * This catalog supports the XCatalog 0.2 and XML Catalog 0.4 proposal as
 * described at
 * <a href="http://www.ccil.org/~cowan/XML/XCatalog.html">draft</a>.
 *
 * @author  Petr Kuzel
 */
public class XCatalog extends AbstractCatalog
       implements CatalogReader, CatalogDescriptor, Serializable, EntityResolver {
    
    /** Serial Version UID MUST NOT change. */
    private static final long serialVersionUID = 06022001L;
    

    // ~~~~~~~~~~~~~~~ 0.4 grammar ~~~~~~~~~~~~~~~~~~
    public static final String DTD_PUBLIC_ID_4 = "-//DTD XMLCatalog//EN"; // NOI18N
       
    /** XCatalog DTD resource name ("xcatalog.dtd"). */
    static final String DTD = "xcatalog.dtd"; // NOI18N
    
    // tag and attribute names
    
    /** PublicID attribute name ("PublicID"). */
    static final String PUBLICID_ATT_4 = "PublicId"; // NOI18N
    
    /** SystemID attribute name ("SystemID"). */
    static final String SYSTEMID_ATT_4 = "SystemID"; // NOI18N
    
    // ~~~~~~~~~~~~~~~~~~~~ 0.2 grammar ~~~~~~~~~~~~~~~~~~~

    public static final String DTD_PUBLIC_ID_2 = "-//DTD XCatalog//EN"; // NOI18N

    /** XCatalog element name ("XCatalog"). */
    static final String XCATALOG_2 = "XMLCatalog"; // NOI18N

    /** XML Catalog version */
    static final String VERSION_2 = "Version"; // NOI18N
    
    
    /** PublicID attribute name ("PublicID"). */
    static final String PUBLICID_ATT_2 = "PublicID"; // NOI18N
     
    /** SystemID attribute name ("SystemID"). */
    static final String SYSTEMID_ATT_2 = "SystemID"; // NOI18N
    
    // ~~~~~~~~~~~~~~~~~~~ 0.X grammar ~~~~~~~~~~~~~~~~~~~~`

    static final String MAP = "Map"; // NOI18N

    static final String EXTEND = "Extend"; // NOI18N

    static final String BASE = "Base"; // NOI18N

    static final String DELEGATE = "Delegate"; // NOI18N

    static final String REMAP = "Remap"; // NOI18N

    static final String HREF_ATT = "HRef"; // NOI18N


    /** Set to true and recompile to include debugging code in class. */
    private static final boolean DEBUG = false;
    
    
    /**
     * @serial Only catalog location is serialized.
     */
    private String catalogSrc = null;
    
    private transient String shortDescription;
    
    private transient Image icon;
    
    
    // INIT ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    
    /**
     * Constructs an XCatalog instance.
     */
    public XCatalog() {
    }

    /**
     * Deserialization constructor.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        loadCatalog(catalogSrc);  //!!! loading catalog during deserialization. Could be deferred
    }

    
    // Catalog loading (and error handling) ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    
    /**
     * Load catalog from given URL.
     */
    public void loadCatalog(String systemID) {
        try {

            clearAll();
            
            if (systemID == null) return;  //balk it
            
            new CatalogParser(new InputSource(systemID));
            
            updateShortDescription(catalogSrc);
            updateIcon(getDefaultIcon(0));             //!!! icon type should be deffered 

            
        } catch (SAXException ex) {
            handleLoadError(ex);
        } catch (IOException ex) {
            handleLoadError(ex);
        } finally {
            notifyInvalidate();
        }            
    }
    
    
    private void handleLoadError(Exception ex) {
        updateShortDescription(ex.getLocalizedMessage());
        updateIcon(getDefaultErrorIcon(0));               //!!!
        if (DEBUG) trace("Can not read: " + shortDescription); // NOI18N
    }


    /** Update and fire. */
    private void updateShortDescription(String loc) {
        String old = shortDescription;
        shortDescription = loc;
        firePropertyChange(PROP_CATALOG_DESC, old, shortDescription);
    }
    
    /** Update and fire. */
    private void updateIcon(Image newIcon) {
        Image old = icon;
        icon = newIcon;
        firePropertyChange(PROP_CATALOG_ICON, old, icon);
    }
    
    // Properties (serialized) ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Set catalog source (URL) to be used. If specified invalid source
     * then no catalog is loaded update short description and icon.
     *
     * @param source should be be URL pointing to valid XML Catalog file
     */
    public void setSource(String source) {
        catalogSrc = source;
        loadCatalog(source);
        firePropertyChange(PROP_CATALOG_NAME, null, getDisplayName());        
    }
    
    /**
     * @return currently catalog location URL, the URL may be invalid
     */
    public String getSource() {
        return catalogSrc;
    }


    // Catalog Reader Interface ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
        
    /**
     * Reload the catalog from its original location.
     */
    public void refresh() {
        if (DEBUG) trace("Refreshing catalog...impl..."); // NOI18N
        loadCatalog(getSource());
    }
    
    
    // Catalog Descriptor Interface ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
    
    /**
     * Return display name of this reader (e.g. "XCatalog Reader" or "SOCAT Reader"
     */
    public String getDisplayName() {
        return Util.getString("PROP_display_name", catalogSrc);
    }
        
    public Image getIcon(int type) {
        return icon;
    }
    
    public String getShortDescription() {
        return shortDescription;
    }
    

    public String toString() {
        return super.toString() + ":" + catalogSrc; // NOI18N
    }
    
    
    // Entity resolver interface ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
        
    /**
     * Resolves external entities using this catalog.
     * @see org.xml.sax.EntityResolver#resolveEntity(String,String)
     */
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        
        if (DEBUG) trace("resolveEntity(\""+publicId+"\", \""+systemId+"\")"); // NOI18N
                
        // public identifier resolution
        if (publicId != null) {
            
            // direct public id mappings
            String value = getPublicMapping(publicId);
            if (DEBUG) trace("  map: \""+publicId+"\" -> \""+value+"\""); // NOI18N
            
            if (value != null) {
                InputSource source = resolveEntity(null, value);
                if (source == null) {
                    source = new InputSource(value);
                }
                source.setPublicId(publicId);
                return source;
            }
            
            // delegates
            Enumeration delegates = getDelegateCatalogKeys();
            while (delegates.hasMoreElements()) {
                String key = (String)delegates.nextElement();
                if (DEBUG) trace("  delegate: \""+key+"\""); // NOI18N
                if (publicId.startsWith(key)) {
                    AbstractCatalog catalog = getDelegateCatalog(key);
                    InputSource source = catalog.resolveEntity(publicId, systemId);
                    if (source != null) {
                        return source;
                    }
                }
            }
        }
        
        // system identifier resolution
        String value = getSystemMapping(systemId);
        if (value != null) {
            if (DEBUG) trace("  remap: \""+systemId+"\" -> \""+value+"\""); // NOI18N
            InputSource source = new InputSource(value);
            source.setPublicId(publicId);
            return source;
        }
        

        // try extenders
        Iterator it = extenders.iterator();
        while (it.hasNext()) {
            XCatalog cat = (XCatalog) it.next();
            InputSource mytry = cat.resolveEntity(publicId, systemId);
            if (mytry != null) return mytry;
        }

        // use default behavior
        if (DEBUG) trace("  returning null!"); // NOI18N
        return null;
        
    }
    

    

    
    // Catalog parsing classes ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /**
     * Parser for XML Catalog document instances.
     * SAX 2 is used only to avoid deprecation messages.
     */
    private class CatalogParser extends DefaultHandler {
                
        /** Current base. */
        private String base;
                
        /** Parses the specified input source. */
        public CatalogParser(InputSource source) throws SAXException, IOException {
            
            XMLReader parser = XMLUtil.createXMLReader(true);
            
            // setup parser
            parser.setEntityResolver(new Resolver());  // overwrite system entity resolver
            parser.setContentHandler(this);
            parser.setErrorHandler(this);
            
            // set initial base and parse
            setBase(source.getSystemId());
            parser.parse(source);
            
        }
        
        
        /**
         * Sets the base from the given system identifier. The base is
         * the same as the system identifier with the least significant
         * part (the filename) removed.
         */
        private void setBase(String systemId) throws SAXException {
            
            // normalize system id
            if (systemId == null) {
                systemId = ""; // NOI18N
            }
            
            // cut off the least significant part
            int index = systemId.lastIndexOf('/');
            if (index != -1) {
                systemId = systemId.substring(0, index + 1);
            }
            
            // save base
            base = systemId;
            
        }
        

        //~~~~~~~~~~~~~~~~~ XCATALOG ATTRIBUTES SCANNER ~~~~~~~~~~~~~~~~~~~~~
        
        /** Stop parsing on fatal error. */
        public void fatalError(SAXException ex) throws SAXException {
            throw ex;
        }
        
        /** Stop parsing on error*/
        public void error(SAXException ex) throws SAXException {
            throw ex;
        }
        
        /** 
          * The start of an element. Parse attributes.
          */
        public void startElement(String ns, String local, String qName, Attributes attrList) throws SAXException {
            
            try {
                
                // <XCatalog Version="...">
/*                if (qName.equals(XCATALOG)) {
                    String version = attrList.getValue(VERSION);
                    if ("1.0".equals(version))
                        return;
                    throw new SAXException("Illeagal XML catalog version");
 
                    return;
                }
*/                                
                if (qName.equals(MAP)) {
                    
                    // <Map PublicID="..." HRef="..."/>
                    String publicId = attrList.getValue(PUBLICID_ATT_4);
                    if (publicId == null) publicId = attrList.getValue(PUBLICID_ATT_2);

                    String href     = attrList.getValue(HREF_ATT);
                    if (DEBUG) trace("MAP \""+publicId+"\" \""+href+"\""); // NOI18N
                    
                    // create mapping
                    if (Categorizer.isURL(href) == false) {
                        href = base + href;  // seems to be relative
                    }
                    addPublicMapping(publicId, href);
                    
                } else if (qName.equals(DELEGATE)) {
                    
                    // <Delegate PublicId="..." HRef="..."/>
                    String publicId = attrList.getValue(PUBLICID_ATT_4);
                    if (publicId == null) publicId = attrList.getValue(PUBLICID_ATT_2);
                    String href     = attrList.getValue(HREF_ATT);
                    
                    if (DEBUG) trace("DELEGATE \""+publicId+"\" \""+href+"\""); // NOI18N
                    
                    // expand system id
                    if (Categorizer.isURL(href) == false) {
                        href = base + href;
                    }
                    String systemId = href; //!!!fEntityHandler.expandSystemId(href);
                    
                    // create delegate
                    XCatalog catalog = new XCatalog();
                    catalog.loadCatalog(systemId);
                    addDelegateCatalog(publicId, catalog);
                    
                } else if (qName.equals(EXTEND)) {
                    
                    // <Extend HRef="..."/>
                    String href = attrList.getValue(HREF_ATT);
                    
                    if (DEBUG) trace("EXTEND \""+href+"\""); // NOI18N
                    
                    // expand system id
                    if (Categorizer.isURL(href) == false) {
                        href = base + href;
                    }
                    String systemId = href; //!!!fEntityHandler.expandSystemId(href);
                    
                    // create "patch/extender" catalog
                    XCatalog extender = new XCatalog();
                    extender.loadCatalog(systemId);
                    extenders.add(extender);
                    
                } else if (qName.equals(BASE)) {
                    
                    // <Base HRef="..."/>
                    String href = attrList.getValue(HREF_ATT);
                    
                    // set new base  //!!! new specs replaces it with XBase
                    if (href != null) { 
                        base = href;
                    }
                    if (DEBUG) trace("BASE \""+href+"\" -> \""+base+"\""); // NOI18N
                    
                } else if (qName.equals(REMAP)) {
                    
                    // <Remap SystemID="..." HRef="..."/>
                    
                    String systemId = attrList.getValue(SYSTEMID_ATT_4);
                    if (systemId == null) systemId=attrList.getValue(SYSTEMID_ATT_2);

                    String href = attrList.getValue(HREF_ATT);
                    
                    if (DEBUG) trace("REMAP \""+systemId+"\" \""+href+"\""); // NOI18N
                                        
                    // create mapping
                    if (Categorizer.isURL(href) == false) {
                        href = base + href;
                    }
                    addSystemMapping(systemId, href);
                    
                }
                
            } catch (Exception e) {
                throw new SAXException(e);
            }
            
        }
        
        
        private class Resolver implements EntityResolver {
            
            /** Resolves the XCatalog DTD entity. */
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                
                // parser does not validate, let skip DTD
                if (DTD_PUBLIC_ID_2.equals(publicId) || DTD_PUBLIC_ID_4.equals(publicId)) {
                    InputSource src = new InputSource();
                    src.setPublicId(publicId);
                    InputStream is = new ByteArrayInputStream(new byte[0]);
                    src.setByteStream(is);
                    src.setCharacterStream(new InputStreamReader(is, "UTF8")); // NOI18N
                    return src;
                }
                
                // no resolution possible
                return null;
                
            }
            
        }
        
    }
    

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    private void trace(String msg) {
        Util.trace(msg);
    }
    
}

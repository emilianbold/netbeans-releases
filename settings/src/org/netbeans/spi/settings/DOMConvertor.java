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

/** DOMConvertor extends the Convertor to allow to compose output of several
 *  convertors into one xml document. For more info see
 *  <a href='./doc-files/api.html#use-composed'>Composed Content</a>.
 *
 * @author  Jan Pokorsky
 * @since 1.1
 */
public abstract class DOMConvertor extends Convertor {
    /** an attribute containing public ID of DTD describing nested elements
     * @since 1.1
     */
    protected final static String ATTR_PUBLIC_ID = "dtd_public_id"; // NOI18N
    
    private final static String ATTR_ID = "id"; // NOI18N
    private final static String ATTR_IDREF = "idref"; // NOI18N
    /** element used to wrap output from non-DOMConvertor or to reference
     * already written object.
     * @see #ATTR_ID
     * @see #ATTR_IDREF
     */
    // Usage:
    // <domconvertor dtd_public_id='...'><[!CDATA[...]]></domconvertor>
    // or
    // ...
    // <foo id='1' dtd_public_id='...'>...</foo>
    // ...
    // <domconvertor idref='1'/>

    private final static String ELM_DELEGATE = "domconvertor"; // NOI18N
    
    private final static java.util.Map refsCache = new java.util.WeakHashMap();
    
    /** Provide an object constructed from the element.
     * @param element represents a read object in a DOM document
     * @return an setting object
     * @exception IOException if the object cannot be read
     * @exception ClassNotFoundException if the object class cannot be resolved
     * @since 1.1
     */
    protected abstract Object readElement(org.w3c.dom.Element element) throws java.io.IOException, ClassNotFoundException;
    
    /** Provide a DOM element describing obj.
     * @param doc a DOM document allowing to create elements describing passed object
     * @param obj an object to convert
     * @return a DOM element representation
     * @exception IOException if the object cannot be written
     * @exception org.w3c.dom.DOMException if an element construction failed
     * @since 1.1
     */
    protected abstract org.w3c.dom.Element writeElement(org.w3c.dom.Document doc, Object obj) throws java.io.IOException, org.w3c.dom.DOMException;

    /** delegate the read operation to a convertor referenced by {@link #ATTR_PUBLIC_ID}
     * @param element DOM element that should be read
     * @return an setting object
     * @exception IOException if the object cannot be read
     * @exception ClassNotFoundException if the object class cannot be resolved
     * @since 1.1
     */
    protected final static Object delegateRead(org.w3c.dom.Element element) throws java.io.IOException, ClassNotFoundException {
        Object obj;
        
        // in case of a reference a cache of already read objects should
        // be consulted instead of delegating
        String idref = element.getAttribute(ATTR_IDREF);
        if (idref.length() != 0 && element.getTagName().equals(ELM_DELEGATE)) {
            obj = getCache(element.getOwnerDocument(), idref);
            if (obj != null) {
                return obj;
            } else {
                throw new IOException("broken reference: " + element); // NOI18N
            }
        }
        
        // lookup convertor
        String publicId = element.getAttribute(ATTR_PUBLIC_ID);
        Convertor c = ConvertorResolver.getDefault().getConvertor(publicId);
        if (c == null) throw new IOException("Convertor not found. publicId: " + publicId); // NOI18N
        
        // read
        if (element.getTagName().equals(ELM_DELEGATE)) {
            // read CDATA block
            org.w3c.dom.Node n = element.getFirstChild();
            String content = n.getNodeValue();
            if (n.getNodeType() != org.w3c.dom.Node.CDATA_SECTION_NODE || content == null) {
                throw new IOException("Expected CDATA block: " + n);
            }
            obj = readFromString(c, content);
        } else if (c instanceof DOMConvertor) {
            DOMConvertor dc = (DOMConvertor) c;
            obj = dc.readElement(element);
        } else {
            throw new IOException(
                "Missing DOMConvertor for publicId: " + publicId); // NOI18N
        }
        
        // cache reference
        String id = element.getAttribute(ATTR_ID);
        if (id.length() != 0) {
            setCache(element.getOwnerDocument(), id, obj);
        }
        
        return obj;
    }
    
    /** delegate the write operation to a convertor able to write <code>obj<code>.
     * @param doc a DOM document allowing to create elements describing passed object
     * @param obj an object to convert
     * @return a DOM element representation
     * @exception IOException if the object cannot be written
     * @exception org.w3c.dom.DOMException if an element construction failed
     * @since 1.1
     */
    protected final static org.w3c.dom.Element delegateWrite(org.w3c.dom.Document doc, Object obj) throws java.io.IOException, org.w3c.dom.DOMException {
        // first lookup a cache of already written objects to prevent
        // storing of the same instance multiple times.
        CacheRec cache = setCache(doc, obj);
        if (cache.used) {
            return writeReference(doc, cache);
        }
        
        ConvertorResolver res = ConvertorResolver.getDefault();
        Class clazz = obj.getClass();
        Convertor c = res.getConvertor(clazz);
        if (c == null) {
            throw new IOException("Convertor not found for object: " + obj); // NOI18N
        }
        
        org.w3c.dom.Element el;
        if (c instanceof DOMConvertor) {
            DOMConvertor dc = (DOMConvertor) c;
            el = dc.writeElement(doc, obj);
            if (el.getAttribute(ATTR_PUBLIC_ID).length() == 0) {
                el.setAttribute(ATTR_PUBLIC_ID, res.getPublicID(clazz));
            }
        } else {
            // plain convertor -> wrap content to CDATA block
            el = doc.createElement(ELM_DELEGATE);
            el.setAttribute(ATTR_PUBLIC_ID, res.getPublicID(clazz));
            el.appendChild(doc.createCDATASection(writeToString(c, obj)));
        }
        
        // bind cached object with original element
        cache.elm = el;
        return el;
    }
    
    /** write an object obj to String using Convertor.write() */
    private static String writeToString(Convertor c, Object obj) throws IOException {
        java.io.Writer w = new java.io.CharArrayWriter(1024);
        c.write(w, obj);
        w.close();
        return w.toString();
    }
    
    /** read an object from String using Convertor.read() */
    private static Object readFromString(Convertor c, String s) throws IOException, ClassNotFoundException {
        java.io.Reader r = new java.io.StringReader(s);
        return c.read(r);
    }
    
    /** create an element referencing an already stored object */
    private static org.w3c.dom.Element writeReference(org.w3c.dom.Document doc, CacheRec cache) throws org.w3c.dom.DOMException {
        org.w3c.dom.Element el = doc.createElement(ELM_DELEGATE);
        el.setAttribute(ATTR_IDREF, (String) cache.value);
        cache.elm.setAttribute(ATTR_ID, (String) cache.value);
        return el;
    }
    
    /** remember an object obj being stored in document key and generate its ID
     * in scope of the document. Used during write operations.
     */
    private static CacheRec setCache(org.w3c.dom.Document key, Object obj) {
        synchronized (refsCache) {
            java.util.Map refs = (java.util.Map) refsCache.get(key);
            if (refs == null) {
                refs = new java.util.HashMap();
                refsCache.put(key, refs);
            }
            
            CacheRec cr = (CacheRec) refs.get(obj);
            if (cr == null) {
                cr = new CacheRec();
                cr.key = obj;
                cr.value = String.valueOf(refs.size());
                refs.put(obj, cr);
            }
            cr.used = cr.elm != null;
            return cr;
        }
    }
    
    /** remember an object obj and its ID being stored in document key to allow 
     * to resolve stored references in scope of the document.
     * Used during read operations.
     * @see #getCache
     */
    private static CacheRec setCache(org.w3c.dom.Document key, Object id, Object obj) {
        synchronized (refsCache) {
            java.util.Map refs = (java.util.Map) refsCache.get(key);
            if (refs == null) {
                refs = new java.util.HashMap();
                refsCache.put(key, refs);
            }
            
            CacheRec cr = (CacheRec) refs.get(id);
            if (cr == null) {
                cr = new CacheRec();
                cr.key = id;
                cr.value = obj;
                refs.put(id, cr);
            }
            return cr;
        }
    }
    
    /** resolve a reference idref in scope of the document key.
     * Used during read operations.
     */
    private static Object getCache(org.w3c.dom.Document key, Object idref) {
        synchronized (refsCache) {
            java.util.Map refs = (java.util.Map) refsCache.get(key);
            if (refs == null) {
                return null;
            }
            
            CacheRec cr = (CacheRec) refs.get(idref);
            return cr.value;
        }
    }
    
    private static class CacheRec {
        // key/value are paired as id/settings_object or settings_object/id
        // depends on performed operation (read/write)
        Object key;
        org.w3c.dom.Element elm;
        Object value;
        boolean used;
    }
}

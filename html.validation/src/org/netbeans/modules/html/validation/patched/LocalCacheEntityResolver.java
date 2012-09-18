/*
 * Copyright (c) 2006 Henri Sivonen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 */

package org.netbeans.modules.html.validation.patched;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import nu.validator.xml.TypedInputSource;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @version $Id: LocalCacheEntityResolver.java 74 2008-09-17 10:34:15Z hsivonen $
 * @author hsivonen
 */
public class LocalCacheEntityResolver implements EntityResolver {

    private static final ClassLoader LOADER = LocalCacheEntityResolver.class.getClassLoader();

    private static final Map<String, String> PATH_MAP = new HashMap<String, String>();

    static {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(
                    LOADER.getResourceAsStream("nu/validator/localentities/files/entitymap"), "UTF-8"));
            String line;
            while ((line = r.readLine()) != null) {
                if ("".equals(line.trim())) {
                    break;
                }
                String s[] = line.split("\t");
                PATH_MAP.put(s[0], "nu/validator/localentities/files/" + s[1]);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream getPresetsAsStream() {
        return LOADER.getResourceAsStream("org/netbeans/modules/html/validation/patched/presets");
    }

    public static InputStream getHtml5SpecAsStream() {
        return LOADER.getResourceAsStream("nu/validator/localentities/files/html5spec");
    }
    
    private EntityResolver delegate;

    private boolean allowRnc = false;

    /**
     * The map must be safe for concurrent reads.
     * 
     * @param pathMap
     * @param delegate
     */
    public LocalCacheEntityResolver(EntityResolver delegate) {
        this.delegate = delegate;
    }

    public static URL getResource(String systemId) {
        String path = PATH_MAP.get(systemId);
        return path != null ? LOADER.getResource(path) : null;
    }

    /**
     * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String,
     *      java.lang.String)
     */
    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException {
        long a = System.currentTimeMillis();
        String path = PATH_MAP.get(systemId);
        if (path != null) {
            InputStream stream = LOADER.getResourceAsStream(path);
            if (stream != null) {
                TypedInputSource is = new TypedInputSource();
                is.setByteStream(stream);
                is.setSystemId(systemId);
                is.setPublicId(publicId);
                if (systemId.endsWith(".rnc")) {
                    is.setType("application/relax-ng-compact-syntax");
                    if (!allowRnc) {
                        throw new IOException("Not an XML resource: "
                                + systemId);
                    }
                } else if (systemId.endsWith(".dtd")) {
                    is.setType("application/xml-dtd");
                } else if (systemId.endsWith(".ent")) {
                    is.setType("application/xml-external-parsed-entity");
                } else {
                    is.setType("application/xml");
                }
                return is;
            }
        }
        return delegate.resolveEntity(publicId, systemId);
    }

    /**
     * @return Returns the allowRnc.
     */
    public boolean isAllowRnc() {
        return allowRnc;
    }

    /**
     * @param allowRnc
     *            The allowRnc to set.
     */
    public void setAllowRnc(boolean allowRnc) {
        this.allowRnc = allowRnc;
    }
}
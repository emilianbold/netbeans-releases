/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.openide.xml.EntityCatalog;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Register filesystem XML layer DTDs.
 * @author Jesse Glick
 */
public final class FsDtdEntityCatalog extends EntityCatalog {
    
    private final Map DTD_MAP = new HashMap();
    
    /** Default constructor for lookup. */
    public FsDtdEntityCatalog() {
        DTD_MAP.put("-//NetBeans//DTD Filesystem 1.0//EN", "org/openide/filesystems/filesystem.dtd");
        DTD_MAP.put("-//NetBeans//DTD Filesystem 1.1//EN", "org/openide/filesystems/filesystem1_1.dtd");
    }
    
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        String resourcePath = (String) DTD_MAP.get(publicId);
        if (resourcePath == null) {
            return null;
        }
        URL location = FsDtdEntityCatalog.class.getClassLoader().getResource(resourcePath);
        assert location != null : resourcePath;
        return new InputSource(location.toExternalForm());
    }
    
}

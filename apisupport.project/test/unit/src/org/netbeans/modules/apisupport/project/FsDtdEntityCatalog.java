/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

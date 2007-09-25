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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.favorites.templates;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/** Reads files from Templates/Properties and makes their properties available
 * to all.
 *
 * @author Jaroslav Tulach
 */
public final class PropertiesProvider implements CreateFromTemplateAttributesProvider {

    public Map<String, ?> attributesFor(DataObject template, DataFolder target, String name) {
        FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
        FileObject dir = root.getFileObject("Templates/Properties");
        if (dir == null) {
            return Collections.emptyMap();
        }
        Charset set;
        InputStream is;
        
        Map<String, Object> ret = new HashMap<String, Object>();
        for (Enumeration<? extends FileObject> en = dir.getChildren(true); en.hasMoreElements(); ) {
            try {
                FileObject fo = en.nextElement();
                Properties p = new Properties();
                is = fo.getInputStream();
                p.load(is);
                is.close();
                for (Map.Entry<Object, Object> entry : p.entrySet()) {
                    if (entry.getKey() instanceof String) {
                        if (!ret.containsKey(entry.getKey())) {
                            ret.put((String)entry.getKey(), entry.getValue());
                        }
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return ret;
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
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
@org.openide.util.lookup.ServiceProvider(service=org.openide.loaders.CreateFromTemplateAttributesProvider.class)
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

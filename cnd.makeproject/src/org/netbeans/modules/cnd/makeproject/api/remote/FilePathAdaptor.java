/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.makeproject.api.remote;

import org.openide.util.Lookup;
import org.openide.util.Utilities;

public class FilePathAdaptor {

    public static String mapToRemote(String local) {
        FilePathMapper conv = getConverting();
        return conv.mapToRemote(local);
    }

    public static String mapToLocal(String remote) {
        FilePathMapper conv = getConverting();
        return conv.mapToLocal(remote);
    }

    public static String normalize(String path) {
        FilePathMapper conv = getConverting();
        return conv.normalize(path);
    }
    
    public static String naturalize(String path) {
        FilePathMapper conv = getConverting();
        return conv.naturalize(path);
    }

    private static FilePathMapper getConverting() {
        FilePathMapper conv = (FilePathMapper) Lookup.getDefault().lookup(FilePathMapper.class);
        return conv == null ? FilePathMapperDefault.DEFAULT : conv;
    }
    
    private static class FilePathMapperDefault implements FilePathMapper {
        public final static FilePathMapper DEFAULT = new FilePathMapperDefault();
        
        public String mapToRemote(String local) {
            return local;
        }

        public String mapToLocal(String remote) {
            return remote;
        }

        public String normalize(String path) {
            // Always use Unix file separators
            return path.replaceAll("\\\\", "/"); // NOI18N
        }
        
        public String naturalize(String path) {
            if (Utilities.isUnix())
                return path.replaceAll("\\\\", "/"); // NOI18N
            else if (Utilities.isWindows())
                return path.replaceAll("/", "\\\\"); // NOI18N
            else
                return path;
        }
    }
}

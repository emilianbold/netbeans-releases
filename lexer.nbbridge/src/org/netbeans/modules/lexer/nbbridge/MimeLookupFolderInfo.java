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

package org.netbeans.modules.lexer.nbbridge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.editor.mimelookup.Class2LayerFolder;
import org.netbeans.spi.editor.mimelookup.InstanceProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author vita
 */
public class MimeLookupFolderInfo implements Class2LayerFolder, InstanceProvider {
    
    private static final Logger LOG = Logger.getLogger(MimeLookupFolderInfo.class.getName());
    
    /** Creates a new instance of MimeLookupFolderInfo */
    public MimeLookupFolderInfo() {
    }

    public Class getClazz() {
        return LanguagesEmbeddingMap.class;
    }

    public String getLayerFolderName() {
        return "languagesEmbeddingMap"; //NOI18N
    }

    public InstanceProvider getInstanceProvider() {
        return this;
    }

    public Object createInstance(List fileObjectList) {
        HashMap<String, String> map = new HashMap<String, String>();
        
        for(Object o : fileObjectList) {
            assert o instanceof FileObject : "fileObjectList should contain FileObjects and not " + o; //NOI18N
            
            FileObject f = (FileObject) o;
            try {
                String mimeType = readMimeType(f);
                if (isMimeTypeValid(mimeType)) {
                    map.put(f.getName(), mimeType);
                } else {
                    LOG.log(Level.WARNING, "Ignoring invalid mime type '" + mimeType + "' from: " + f.getPath());
                }
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, "Can't read language embedding definition from: " + f.getPath());
            }
        }
        
        return new LanguagesEmbeddingMap(map);
    }
    
    private boolean isMimeTypeValid(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        int slashIndex = mimeType.indexOf('/'); //NOI18N
        if (slashIndex == -1) { // no slash
            return false;
        }
        if (mimeType.indexOf('/', slashIndex + 1) != -1) { //NOI18N
            return false;
        }
        return true;
    }
    
    private String readMimeType(FileObject f) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(f.getInputStream()));
        try {
            String line;
            
            while (null != (line = r.readLine())) {
                line.trim();
                if (line.length() != 0) {
                    return line;
                }
            }
            
            return null;
        } finally {
            r.close();
        }
    }
}

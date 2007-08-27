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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.freeform;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.w3c.dom.Element;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.openide.filesystems.FileUtil;

/**
 * Implementation of FileEncodingQuery for Freeform project, its instance can be 
 * obtained from project lookup
 * 
 * @author Milan Kubec
 */
public class FreeformFileEncodingQueryImpl extends FileEncodingQueryImplementation implements AntProjectListener {
    
    private AntProjectHelper helper;
    private Map<FileObject,String> encodingsCache = null; // source folder -> encoding
    
    public FreeformFileEncodingQueryImpl(AntProjectHelper aph) {
        helper = aph;
    }
    
    public Charset getEncoding(FileObject file) {
        synchronized (this) {
            if (encodingsCache == null) {
                encodingsCache = computeEncodingsCache();
            }
            for (Iterator<Entry<FileObject,String>> iter = encodingsCache.entrySet().iterator(); iter.hasNext(); ) {
                Entry<FileObject,String> entry = iter.next();
                FileObject srcRoot = entry.getKey();
                if (FileUtil.isParentOf(srcRoot, file)) {
                    try {
                        return Charset.forName(entry.getValue());
                    } catch (IllegalCharsetNameException icne) {
                        return null;
                    }
                }
            }
        }
        // return null if no encoding for the file was found
        return null;
    }
    
    private Map<FileObject,String> computeEncodingsCache() {
        Element data = Util.getPrimaryConfigurationData(helper);
        Element foldersEl = Util.findElement(data, "folders", Util.NAMESPACE); // NOI18N
        if (foldersEl == null) {
            return Collections.<FileObject,String>emptyMap();
        }
        for (Element sourceFolderEl : Util.findSubElements(foldersEl)) {
            if (!sourceFolderEl.getLocalName().equals("source-folder")) { // NOI18N
                continue;
            }
            FileObject srcRoot = null;
            Element el = Util.findElement(sourceFolderEl, "location", Util.NAMESPACE); // NOI18N
            if (el != null) {
                srcRoot = helper.getProjectDirectory().getFileObject(Util.findText(el));
            }
            el = Util.findElement(sourceFolderEl, "encoding", Util.NAMESPACE); // NOI18N
            if (el != null && srcRoot != null) {
                if (encodingsCache == null) {
                    encodingsCache = new HashMap<FileObject,String>(3);
                }
                encodingsCache.put(srcRoot, Util.findText(el));
            }
        }
        if (encodingsCache == null) {
            return Collections.<FileObject,String>emptyMap();
        }
        return encodingsCache;
    }
    
    // ---
    
    public void configurationXmlChanged(AntProjectEvent ev) {
        // invalidate cache
        synchronized (this) {
            encodingsCache = null;
        }
    }
    
    public void propertiesChanged(AntProjectEvent ev) {
        // do nothing
    }
    
}

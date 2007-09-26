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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

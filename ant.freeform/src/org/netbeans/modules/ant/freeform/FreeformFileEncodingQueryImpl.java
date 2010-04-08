/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.w3c.dom.Element;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.xml.XMLUtil;

/**
 * Implementation of FileEncodingQuery for Freeform project, its instance can be 
 * obtained from project lookup
 * 
 * @author Milan Kubec
 */
public class FreeformFileEncodingQueryImpl extends FileEncodingQueryImplementation 
        implements AntProjectListener, PropertyChangeListener {
    
    private AntProjectHelper helper;
    private PropertyEvaluator evaluator;
    private Map<FileObject,Charset> encodingsCache;
    
    public FreeformFileEncodingQueryImpl(AntProjectHelper aph, PropertyEvaluator eval) {
        helper = aph;
        evaluator = eval;
        evaluator.addPropertyChangeListener(this);
    }
    
    public Charset getEncoding(final FileObject file) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Charset>() {
            public Charset run() {
                Charset toReturn = null;
                synchronized (this) {
                    if (encodingsCache == null) {
                        computeEncodingsCache();
                    }
                    if (encodingsCache.size() > 0) {
                        Set<FileObject> roots = encodingsCache.keySet();
                        FileObject parent = getNearestParent(roots, file);
                        if (parent != null) {
                            toReturn = encodingsCache.get(parent);
                        }
                    }
                }
                return toReturn;
            }
        });
    }

    private FileObject getNearestParent(Set<FileObject> parents, FileObject file) {
        while (file != null) {
             if (parents.contains(file)) {
                 return file;
             }
             file = file.getParent();
         }
        return null;
    }
    
    private void computeEncodingsCache() {
        Map<FileObject,Charset> cache = new HashMap<FileObject,Charset>(3);
        Element data = Util.getPrimaryConfigurationData(helper);
        Element foldersEl = XMLUtil.findElement(data, "folders", Util.NAMESPACE); // NOI18N
        if (foldersEl != null) {
            for (Element sourceFolderEl : XMLUtil.findSubElements(foldersEl)) {
                if (!sourceFolderEl.getLocalName().equals("source-folder")) { // NOI18N
                    continue;
                }
                FileObject srcRoot = null;
                Element locationEl = XMLUtil.findElement(sourceFolderEl, "location", Util.NAMESPACE); // NOI18N
                if (locationEl != null) {
                    String location = evaluator.evaluate(XMLUtil.findText(locationEl));
                    if (location != null) {
                        srcRoot = helper.resolveFileObject(location);
                    }
                }
                Element encodingEl = XMLUtil.findElement(sourceFolderEl, "encoding", Util.NAMESPACE); // NOI18N
                if (encodingEl != null && srcRoot != null) {
                    String encoding = evaluator.evaluate(XMLUtil.findText(encodingEl));
                    Charset charset = null;
                    if (encoding != null) {
                        try {
                            charset = Charset.forName(encoding);
                        } catch (IllegalCharsetNameException icne) {
                            Exceptions.printStackTrace(icne);
                        }
                        cache.put(srcRoot, charset);
                    }
                }
            }
        }
        if (cache.size() > 0) {
            encodingsCache = cache;
        } else {
            encodingsCache = Collections.<FileObject,Charset>emptyMap();
        }
    }
    
    // ---
    
    public void configurationXmlChanged(AntProjectEvent ev) {
        invalidateCache();
    }
    
    public void propertiesChanged(AntProjectEvent ev) {
        invalidateCache();
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        invalidateCache();
    }
    
    private synchronized void invalidateCache() {
        synchronized (this) {
            encodingsCache = null;
        }
    }
    
}

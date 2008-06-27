/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.usages;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Zezula
 */
public final class VirtualSourceProviderQuery {
    private VirtualSourceProviderQuery () {}
    
    private static final Lookup.Result<VirtualSourceProvider> result = Lookup.getDefault().lookupResult(VirtualSourceProvider.class);
    private static Map<String,VirtualSourceProvider> ext2prov;
    private static final LookupListener l = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            reset();
        }
    };
    
    static {
        result.addLookupListener(WeakListeners.create(LookupListener.class, l, result));
    }
    
    public static boolean hasVirtualSource (final File file) {
        Parameters.notNull("file", file);
        final String ext = FileObjects.getExtension(file.getName());
        return getExt2ProvMap().keySet().contains(ext);
    }
    
    public static boolean hasVirtualSource (final FileObject file) {
        Parameters.notNull("file", file);
        final String ext = file.getExt();
        return getExt2ProvMap().keySet().contains(ext);
    }
    
    public static boolean hasVirtualSource (final String extension) {
        Parameters.notNull("extension", extension);
        return getExt2ProvMap().keySet().contains(extension);
    }
    
    public static Iterable<FileObjects.InferableJavaFileObject> translate (final Iterable<? extends File> files, final File root) {
        Parameters.notNull("files", files);     //NOI18N
        Parameters.notNull("root", root);       //NOI18N
        final Map<String,Pair<VirtualSourceProvider,List<File>>> m = new HashMap<String,Pair<VirtualSourceProvider,List<File>>>();
        final Map<String,VirtualSourceProvider> e2p = getExt2ProvMap();
        for (File f : files) {
           final String ext = FileObjects.getExtension(f.getName());
           final VirtualSourceProvider prov = e2p.get(ext);
           if (prov != null) {
               Pair<VirtualSourceProvider,List<File>> p = m.get(ext);
               List<File> l = null;
               if (p == null) {
                   l = new LinkedList<File>();
                   m.put(ext, Pair.of(prov, l));
               }
               else {
                   l = p.second;
               }
               l.add(f);
           }
        }
        
        final R r = new R (root);        
        for (Pair<VirtualSourceProvider,List<File>> p : m.values()) {
            final VirtualSourceProvider prov = p.first;
            final List<File> tf = p.second;
            prov.translate(tf, root,r);
        }
        return r.getResult();
    }
    
    private static synchronized Map<String,VirtualSourceProvider> getExt2ProvMap () {
        if (ext2prov == null) {
            Collection<? extends VirtualSourceProvider> allInstances = new LinkedList<VirtualSourceProvider>(result.allInstances());
            ext2prov = new HashMap<String, VirtualSourceProvider>();
            for (VirtualSourceProvider vsp : allInstances) {
                for (String ext : vsp.getSupportedExtensions()) {
                    ext2prov.put(ext, vsp);
                }
            }
        }
        return ext2prov;
    }
    
    private static synchronized void reset () {
        ext2prov = null;
    }
    
    private static class R implements VirtualSourceProvider.Result {
        
        private final File root;
        final List<FileObjects.InferableJavaFileObject> res = new LinkedList<FileObjects.InferableJavaFileObject>();
        
        public R (final File root) {
            assert root != null;
            this.root = root;
        }
        
        public List<FileObjects.InferableJavaFileObject> getResult () {
            return res;
        }

        public void add(final File source, final String packageName, final String relativeName, final CharSequence content) {
            res.add(FileObjects.memoryFileObject(packageName, relativeName, source.toURI(), System.currentTimeMillis(), content));
        }                
    }
}

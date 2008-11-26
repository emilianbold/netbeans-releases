/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.ide.ergonomics.fod;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.modules.ide.ergonomics.fod.FeatureInfoAccessor.Internal;
import org.openide.filesystems.FileObject;
import org.openide.modules.ModuleInfo;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/** Description of <em>Feature On Demand</em> capabilities and a 
 * factory to create new instances.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>, Jirka Rechtacek <jrechtacek@netbeans.org>
 */
public final class FeatureInfo {
    private final URL delegateLayer;
    private final Internal internal = new Internal(this);
    private final Set<String> cnbs;
    final Map<String,String> nbproject = new HashMap<String,String>();
    final Map<String,String> files = new HashMap<String,String>();
    private Properties properties;

    private FeatureInfo(Set<String> cnbs, URL delegateLayer, Properties p) {
        this.cnbs = cnbs;
        this.delegateLayer = delegateLayer;
        this.properties = p;
    }
    

    public static FeatureInfo create(URL delegateLayer, URL bundle) throws IOException {
        Properties p = new Properties();
        p.load(bundle.openStream());
        String cnbs = p.getProperty("cnbs");
        assert cnbs != null : "Error loading from " + bundle; // NOI18N
        TreeSet<String> s = new TreeSet<String>();
        s.addAll(Arrays.asList(cnbs.split(",")));

        FeatureInfo info = new FeatureInfo(s, delegateLayer, p);
        final String prefix = "nbproject.";
        final String prefFile = "project.file.";
        for (Object k : p.keySet()) {
            String key = (String) k;
            if (key.startsWith(prefix)) {
                info.nbproject(
                    key.substring(prefix.length()),
                    p.getProperty(key)
                );
            }
            if (key.startsWith(prefFile)) {
                info.projectFile(
                    key.substring(prefFile.length()),
                    p.getProperty(key)
                );
            }
        }
        return info;
    }
    static {
        FeatureInfoAccessor.DEFAULT = new FeatureInfoAccessor() {
            @Override
            public Set<String> getCodeName(FeatureInfo info) {
                return info.getCodeNames();
            }

            @Override
            public URL getDelegateLayer(FeatureInfo info) {
                return info.delegateLayer;
            }

            @Override
            public Internal getInternal(FeatureInfo info) {
                return info.internal;
            }
        };
    }

    public String getAttachTypeName() {
        return properties.getProperty("attachTypeName");
    }

    String getPreferredCodeNameBase() {
        return properties.getProperty("mainModule");
    }

    boolean isProject(FileObject dir, boolean deepCheck) {
        if (isNbProject(dir, deepCheck)) {
            return true;
        } else {
            if (files.isEmpty()) {
                return false;
            } else {
                for (String s : files.keySet()) {
                    if (dir.getFileObject(s) != null) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public final Set<String> getCodeNames() {
        return Collections.unmodifiableSet(cnbs);
    }

    public boolean isPresent() {
        Set<String> codeNames = new HashSet<String>(getCodeNames());
        for (ModuleInfo moduleInfo : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            codeNames.remove(moduleInfo.getCodeNameBase());
        }
        return codeNames.isEmpty();
    }
    
    private boolean isNbProject(FileObject dir, boolean deepCheck) {
        if (nbproject.isEmpty()) {
            return false;
        } else {
            FileObject prj = dir.getFileObject("nbproject/project.xml");
            if (prj == null) {
                return false;
            }
            if (!deepCheck) {
                return true;
            }
            byte[] arr = new byte[2048];
            int len;
            InputStream is = null;
            try {
                is = prj.getInputStream();
                len = is.read(arr);
            } catch (IOException ex) {
                len = -1;
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            if (len == -1) {
                return false;
            }
            String text = new String(arr, 0, len);
            for (String t : nbproject.keySet()) {
                if (text.indexOf("<type>" + t + "</type>") >= 0) { // NOI18N
                    return true;
                }
            }
            return false;
        }
    }

    final void nbproject(String prjType, String clazz) {
        nbproject.put(prjType, clazz);
    }
    final void projectFile(String file, String clazz) {
        files.put(file, clazz);
    }
}

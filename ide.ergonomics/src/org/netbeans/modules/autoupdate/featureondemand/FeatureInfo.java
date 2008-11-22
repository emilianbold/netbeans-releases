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

package org.netbeans.modules.autoupdate.featureondemand;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.autoupdate.featureondemand.FeatureInfoAccessor.Internal;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/** Description of <em>Feature On Demand</em> capabilities and a 
 * factory to create new instances.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>, Jirka Rechtacek <jrechtacek@netbeans.org>
 */
public final class FeatureInfo {
    private final String codeName;
    private final URL delegateLayer;
    private final String delegateFilePath;
    private final Internal internal = new Internal(this);
    private final Set<String> cnbs;
    final Map<String,String> nbproject = new HashMap<String,String>();
    final Map<String,String> files = new HashMap<String,String>();

    private FeatureInfo(Set<String> cnbs, URL delegateLayer) {
        this.cnbs = cnbs;
        this.delegateLayer = delegateLayer;
        this.codeName = null;
        this.delegateFilePath = null;
    }
    
    private FeatureInfo(String codeName, URL delegateLayer, String delegateFilePath) {
        this.codeName = codeName;
        this.delegateLayer = delegateLayer;
        this.delegateFilePath = delegateFilePath;
        this.cnbs = null;
    }
    
    /** Creates new <em>Feature On Demand</em> descriptor. Whenever the module
     * named <code>codeName</code> is not enabled, the system plugs in the
     * {@link XMLFileSystem} specified by <code>delegateLayer</code> URL.
     * Instances of the returned <code>FeatureInfo</code> need to be registered
     * in {@link Lookups#forPath} at <code>FeaturesOnDemand</code> location:
     * 
     * <pre>
&lt;folder name="FeaturesOnDemand"&gt;
    &lt;file name="cnd.instance"&gt;
        &lt;attr name="instanceCreate" methodvalue="org.netbeans.modules.autoupdate.featureondemand.api.FeatureInfo.create"/&gt;
        &lt;attr name="codeName" stringvalue="org.openide.util.enum"/&gt;
        &lt;attr name="delegateLayer" urlvalue="nbresloc:/org/netbeans/modules/autoupdate/featureondemand/api/FeatureInfoTest.xml"/&gt;
    &lt;/file&gt;
&lt;/folder&gt;
     * </pre>
     * 
     * @param codeName name of module to check for
     * @param delegateLayer layer file to enable when the module 
     * @param delegateFilePath relative path to some important file in projectFile this module provides, file structure, or null
     * @return feature info descriptor to be used by the infrastructure
     */
    public static FeatureInfo create(String codeName, URL delegateLayer, String delegateFilePath) {
        return new FeatureInfo(codeName, delegateLayer, delegateFilePath);
    }

    public static FeatureInfo create(Set<String> cnbs, URL delegateLayer) {
        return new FeatureInfo(cnbs, delegateLayer);
    }
    
    static FeatureInfo create(FileObject fo) {
        Object cnb = fo.getAttribute("codeName"); // NOI18N
        Object layer = fo.getAttribute("delegateLayer"); // NOI18N
        Object pfp = fo.getAttribute("delegateFilePath"); // NOI18N
        return create((String)cnb, (URL)layer, (String)pfp);
    }
    
    static {
        FeatureInfoAccessor.DEFAULT = new FeatureInfoAccessor() {
            @Override
            public Set<String> getCodeName(FeatureInfo info) {
                if (info.codeName != null) {
                    return Collections.singleton(info.codeName);
                } else {
                    return info.cnbs;
                }
            }

            @Override
            public URL getDelegateLayer(FeatureInfo info) {
                return info.delegateLayer;
            }

            @Override
            public String getDelegateFilePath(FeatureInfo info) {
                return info.delegateFilePath;
            }

            @Override
            public Internal getInternal(FeatureInfo info) {
                return info.internal;
            }
        };
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

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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.autoupdate.updateprovider;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.netbeans.modules.autoupdate.services.Utilities;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author Jiri Rechtacek
 */
public class InstalledModuleProvider implements InstalledUpdateProvider {
    private static InstalledModuleProvider DEFAULT;
    private LookupListener  lkpListener;
    private Lookup.Result<ModuleInfo> result;
    private Map<String, ModuleInfo> moduleInfos;

    // XXX: should be removed
    public static Map<String, ModuleInfo> getInstalledModules () {
        return getDefault ().getModuleInfos (false);
    }
    
    private Map<String, ModuleInfo> getModuleInfos (boolean force) {
        if (moduleInfos == null || force) {
            moduleInfos = new HashMap<String, ModuleInfo> ();
            Collection<? extends ModuleInfo> infos = Collections.unmodifiableCollection (result.allInstances ());
            for (ModuleInfo info: infos) {
                moduleInfos.put (info.getCodeNameBase (), info);
            }
            
        }
        assert moduleInfos != null;
        return moduleInfos;
    }

    public static InstalledModuleProvider getDefault () {
        if (DEFAULT == null) {
            DEFAULT = new InstalledModuleProvider ();
        }
        return DEFAULT;
    }
        
    private InstalledModuleProvider() {
        result = Lookup.getDefault().lookup(new Lookup.Template<ModuleInfo> (ModuleInfo.class));
        lkpListener = new LookupListener() {
            public void resultChanged(LookupEvent ev) {
                moduleInfos = null;
            }
        };
        result.addLookupListener(lkpListener);
    }

    public String getName () {
        return "installed-module-provider";
    }

    public String getDisplayName () {
        return getName ();
    }

    public String getDescription () {
        return null;
    }

    public Map<String, UpdateItem> getUpdateItems () throws IOException {
        Map<String, UpdateItem> res = new HashMap<String, UpdateItem> ();
        for (ModuleInfo info : getModuleInfos (true).values ()) {
            Date time = null; // XXX: it's too expensive, should be extracted lazy - Utilities.readInstallTimeFromUpdateTracking (info);
            String installTime = null;
            if (time != null) {
                installTime = Utilities.formatDate(time);
            }
            UpdateItemImpl impl = new InstalledModuleItem (
                    info.getCodeNameBase (),
                    info.getSpecificationVersion () == null ? null : info.getSpecificationVersion ().toString (),
                    info,
                    null, // XXX author
                    null, // installed cluster
                    installTime
                    
                    );
            
            UpdateItem updateItem = Utilities.createUpdateItem (impl);
            res.put (info.getCodeName () + '_' + info.getSpecificationVersion (), updateItem);
        }
        return res;
    }

    public boolean refresh (boolean force) throws IOException {
        if (moduleInfos == null) {
            moduleInfos = new HashMap<String, ModuleInfo> ();
            Collection<? extends ModuleInfo> infos = result.allInstances ();
            for (ModuleInfo info: infos) {
                moduleInfos.put (info.getCodeNameBase (), info);
            }
            
        }
        assert moduleInfos != null;
        return true;
    }

    public CATEGORY getCategory() {
        return CATEGORY.COMMUNITY;
    }
}

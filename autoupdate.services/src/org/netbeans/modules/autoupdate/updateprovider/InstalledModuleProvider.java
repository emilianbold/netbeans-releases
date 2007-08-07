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

package org.netbeans.modules.autoupdate.updateprovider;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
            SimpleItem simpleItem = new SimpleItem.InstalledModule (info);
            Date time = null; // XXX: it's too expensive, should be extracted lazy - Utilities.readInstallTimeFromUpdateTracking (info);
            String installTime = null;
            if (time != null) {
                installTime = Utilities.DATE_FORMAT.format (time);
            }
            UpdateItem updateItem = simpleItem.toUpdateItem (null, installTime);
            res.put (simpleItem.getId (), updateItem);
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
}

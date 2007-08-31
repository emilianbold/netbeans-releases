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
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateProvider;

/**
 *
 * @author Jiri Rechtacek
 */
public class AutoupdateCatalogProvider implements UpdateProvider {
    private URL updateCenter;
    private String codeName;
    private String displayName;
    private AutoupdateCatalogCache cache = AutoupdateCatalogCache.getDefault ();
    private Logger log = Logger.getLogger ("org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalog");
    private String description = null;
    private CATEGORY category = null;

    public AutoupdateCatalogProvider (String name, String displayName, URL updateCenter) {
        this(name, displayName, updateCenter, CATEGORY.COMMUNITY);
    }
    
    /**
     * Creates a new instance of AutoupdateCatalog
     */
    public AutoupdateCatalogProvider (String name, String displayName, URL updateCenter, CATEGORY category) {
        this.codeName = name;
        this.displayName = displayName;
        this.updateCenter = updateCenter;
        this.category = (category != null) ? category : CATEGORY.COMMUNITY;
    }
    
    public String getName () {
        assert codeName != null : "UpdatesProvider must have a name.";
        return codeName;
    }
    
    public String getDisplayName () {
        return displayName == null ? codeName : displayName;
    }
    
    public String getDescription () {
        if (description == null) {
            URL toParse = cache.getCatalogURL (codeName);
            if (toParse == null) {
                return null;
            }

            description = AutoupdateCatalogParser.getNotification (toParse, getUpdateCenterURL ());
        }
        return description;
    }

    public Map<String, UpdateItem> getUpdateItems () throws IOException {
        URL toParse = cache.getCatalogURL (codeName);
        /*if (toParse == null && !firstRefreshDone) {
            firstRefreshDone = true;
            refresh(true);
            toParse = cache.getCatalogURL (codeName);
        }*/
        if (toParse == null) {
            log.log (Level.INFO, "No content in cache for " + codeName + " provider. Returns EMPTY_MAP");
            return Collections.emptyMap ();
        }
        
        return AutoupdateCatalogParser.getUpdateItems (toParse, getUpdateCenterURL ());
    }
    
    public boolean refresh (boolean force) throws IOException {
        boolean res = false;
        log.log (Level.FINER, "Try write(force? " + force + ") to cache Update Provider " + codeName + " from "  + getUpdateCenterURL ());
        if (force) {
            res = cache.writeCatalogToCache (codeName, getUpdateCenterURL ()) != null;
            description = null;
        } else {
            res = true;
        }
        return res;
    }
    
    public URL getUpdateCenterURL () {
        assert updateCenter != null : "XMLCatalogUpdatesProvider " + codeName + " must have a URL to Update Center";
        return updateCenter;
    }
    
    public void setUpdateCenterURL (URL newUpdateCenter) {
        assert newUpdateCenter != null;
        updateCenter = newUpdateCenter;
    }
    
    public String toString () {
        return displayName + "[" + codeName + "] to " + updateCenter;
    }

    public CATEGORY getCategory() {
        return category;
    }    
}

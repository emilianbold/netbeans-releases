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

package org.netbeans.modules.autoupdate.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.UpdateElement;
import org.openide.modules.SpecificationVersion;


public class UpdateUnitImpl extends Object {
    private String codeName;
    private UpdateElement installed;
    private List<UpdateElement> updates;
    private UpdateElement installedLocalization;
    private List<UpdateElement> localizationUpdates;
    private UpdateElement backup;
    
    private Logger err = Logger.getLogger (this.getClass ().getName ());

    public UpdateUnitImpl (String codename) {
        this.codeName = codename;
    }

    public String getCodeName () {
        return codeName;
    }

    public UpdateElement getInstalled () {
        return installed;
    }

    public List<UpdateElement> getAvailableUpdates () {
        return identifyUpdates (installed, updates);
    }

    public UpdateElement getInstalledLocalization () {
        return installedLocalization;
    }

    public List<UpdateElement> getAvailableLocalizations () {
        return identifyLocalizationUpdates (installedLocalization, localizationUpdates);
    }

    public UpdateElement getBackup () {
        return backup;
    }

    public void addUpdate (UpdateElement update) {
        if (updates == null) {
            updates = new ArrayList<UpdateElement> ();
        }
        //assert ! updates.contains (update);
        //TODO: make better
        int idx = updates.indexOf(update);
        if (idx != -1) {
            updates.remove(update);
        }
        updates.add (update);
    }
            
    public void setInstalled (UpdateElement installed) {
        assert this.installed == null;
        assert installed != null;
        this.installed = installed;
    }
    
    public void setAsUninstalled () {
        assert this.installed != null;
        this.installed = null;
    }
    
    
    public void updateInstalled (UpdateElement installed) {
        //assert this.installed != null;
        this.installed = null;
        setInstalled (installed);
    }
    
    public void addLocalizationUpdate (UpdateElement update) {
        if (localizationUpdates == null) {
            localizationUpdates = new ArrayList<UpdateElement> ();
        }
        assert ! localizationUpdates.contains (update);
        localizationUpdates.add (update);
    }

    public void setInstalledLocalization (UpdateElement installed) {
        assert this.installedLocalization == null;
        this.installedLocalization = installed;
    }

    public void setBackup (UpdateElement backup) {
        assert this.backup == null;
        this.backup = backup;
    }
    
    private List<UpdateElement> identifyUpdates (UpdateElement installed, List<UpdateElement> updates) {
        List<UpdateElement> res = null;

        // check if potentinally updates exist
        if (updates != null && ! updates.isEmpty ()) {

            // check if a module is already installed
            if (installed == null) {
                if (updates != null) {
                    res = updates;
                }
            } else {
                // compare installed with optionallly update
                String moduleId = installed.getCodeName ();
                List<UpdateElement> realUpdates = new ArrayList<UpdateElement> ();
                for (UpdateElement update : updates) {
                    if (new SpecificationVersion(update.getSpecificationVersion ()).compareTo (new SpecificationVersion(installed.getSpecificationVersion ())) > 0) {
                        err.log (Level.FINE, "Module " + moduleId + "[" + installed.getSpecificationVersion () + "] has update " + moduleId + "[" + update.getSpecificationVersion () + "]");
                        realUpdates.add (update);
                    }
                }
                if (! realUpdates.isEmpty ()) {
                    res = realUpdates;
                }
            }
        }

        if (res == null) {
            res = java.util.Collections.emptyList();
        }
        Collections.sort(res,new Comparator<UpdateElement>(){
            public int compare(UpdateElement o1, UpdateElement o2) {
                return new SpecificationVersion(o2.getSpecificationVersion()).compareTo(new SpecificationVersion(o1.getSpecificationVersion()));
            }
        });
        return res; 
    }

    private List<UpdateElement> identifyLocalizationUpdates (UpdateElement installed, List<UpdateElement> updates) {
        // XXX: handle identifyLocalizationUpdates
        List<UpdateElement> res = null;

        // check if potentinally updates exist
        if (updates != null && ! updates.isEmpty ()) {

            // check if a module is already installed
            if (installed == null) {
                return updates;

            } else {
                // compare installed with optionallly update
                String moduleId = installed.getCodeName ();
                List<UpdateElement> realUpdates = new ArrayList<UpdateElement> ();
                for (UpdateElement update : updates) {
                    if (update.getSpecificationVersion ().compareTo (installed.getSpecificationVersion ()) > 0) {
                        err.log (Level.FINE, "Module " + moduleId + "[" + installed.getSpecificationVersion () + "] has update " + moduleId + "[" + update.getSpecificationVersion () + "]");
                        realUpdates.add (update);
                    }
                }
                if (! realUpdates.isEmpty ()) {
                    res = realUpdates;
                }
            }
        }

        return res;
    }
    
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final UpdateUnitImpl other = (UpdateUnitImpl) obj;

        if (this.codeName != other.codeName &&
            (this.codeName == null || !this.codeName.equals(other.codeName)))
            return false;
        if (this.installed != other.installed &&
            (this.installed == null || !this.installed.equals(other.installed)))
            return false;
         
        if (this.updates != other.updates &&
            (this.updates == null || !this.updates.equals(other.updates)))
            return false;
        if (this.installedLocalization != other.installedLocalization &&
            (this.installedLocalization == null ||
             !this.installedLocalization.equals(other.installedLocalization)))
            return false;
        if (this.localizationUpdates != other.localizationUpdates &&
            (this.localizationUpdates == null ||
             !this.localizationUpdates.equals(other.localizationUpdates)))
            return false;
        if (this.backup != other.backup &&
            (this.backup == null || !this.backup.equals(other.backup)))
            return false;
        return true;
    }

    public int hashCode() {
        int hash = 7;

        hash = 53 * hash + (this.codeName != null ? this.codeName.hashCode()
                                                  : 0);
        hash = 53 * hash +
               (this.installed != null ? this.installed.hashCode()
                                       : 0);
        hash = 53 * hash + (this.updates != null ? this.updates.hashCode()
                                                 : 0);
        hash = 53 * hash +
               (this.installedLocalization != null ? this.installedLocalization.hashCode()
                                                   : 0);
        hash = 53 * hash +
               (this.localizationUpdates != null ? this.localizationUpdates.hashCode()
                                                 : 0);
        hash = 53 * hash + (this.backup != null ? this.backup.hashCode()
                                                : 0);
        return hash;
    }

}


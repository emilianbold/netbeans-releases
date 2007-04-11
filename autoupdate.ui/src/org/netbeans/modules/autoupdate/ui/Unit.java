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

package org.netbeans.modules.autoupdate.ui;

import java.util.Set;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.openide.modules.ModuleInfo;

/**
 *
 * @author Jiri Rechtacek
 */
public abstract class Unit {
    UpdateUnit updateUnit = null;
    
    protected abstract UpdateElement getRelevantElement();
    public abstract boolean isMarked();
    public abstract void setMarked(boolean marked);
    public abstract int getCompleteSize ();

    public String getDisplayName() {
        return getRelevantElement().getDisplayName();
    }
    
    public final boolean isVisible(final String filter) {
        return filter.length() == 0 || getDisplayName().toLowerCase().contains(filter);
    }
    
    public String getDescription() {
        return getRelevantElement().getDescription();
    }
    
    public String getAuthor() {
        return getRelevantElement().getAuthor();
    }
    
    public String getHomepage() {
        return getRelevantElement().getHomepage();
    }
    
    public String getSource() {
        return getRelevantElement().getSource();
    }
    
    public String getDisplayVersion() {
        return getRelevantElement().getSpecificationVersion().toString();
    }    
    
    public static class Installed extends Unit {
        
        private UpdateElement installEl = null;
        private UpdateElement backupEl = null;
        private boolean isNotEditable ;
        
        public static boolean isNotEditable(UpdateUnit uUnit, UpdateElement element ) {
            ModuleInfo mInfo = ModuleProvider.getInstalledModules().get(element.getCodeName());
            return mInfo == null || (mInfo != null && mInfo.isEnabled() && !Containers.forDisable().canBeAdded(uUnit, element) ||
                    mInfo != null && !mInfo.isEnabled() && !Containers.forEnable().canBeAdded(uUnit, element));
        }
        public boolean isModuleEnabled() {
            ModuleInfo mInfo = ModuleProvider.getInstalledModules().get(installEl.getCodeName());
            return mInfo != null ? mInfo.isEnabled() : false;
        }
        public Installed(UpdateUnit unit) {
            this.updateUnit = unit;
            this.installEl = unit.getInstalled();
            assert installEl != null : "Installed UpdateUnit " + unit + " has Installed UpdateElement.";
            this.backupEl = unit.getBackup();
            this.isNotEditable = isNotEditable(this.updateUnit, installEl);
        }
        
        public boolean isMarked() {
            return Containers.forUninstall().contains(installEl);
        }
        
        public void setMarked(boolean marked) {
            assert marked != isMarked();
            if (marked) {
                Containers.forUninstall().add(updateUnit, installEl);
            } else {
                Containers.forUninstall().remove(installEl);
            }
        }
        
        /*public boolean isMarked() {//alternative for enable/disable
            // XXX add new API in UpdateUnit
            ModuleInfo mInfo = getModuleInfos().get(updateUnit.getCodeName());
            assert mInfo != null;
            boolean retval = mInfo.isEnabled();
            if (retval) {
                retval = Containers.forDisable().contains(installEl);
            } else {
                retval = Containers.forEnable().contains(installEl);
            }
            return retval;
        }*/
        
        /*public void setMarked(boolean marked) {//alternative for enable/disable
            assert marked != isMarked();
            ModuleInfo mInfo = getModuleInfos().get(updateUnit.getCodeName());
            assert mInfo != null;
            boolean isEnabled = mInfo.isEnabled();
            Containers.forEnable().remove(installEl);
            Containers.forDisable().remove(installEl);
            if (marked) {
                if (isEnabled) {
                    Containers.forDisable().add(installEl);;
                } else {
                    Containers.forEnable().add(installEl);
                }
            } else {
                if (isEnabled) {
                    Containers.forDisable().remove(installEl);;
                } else {
                    Containers.forEnable().remove(installEl);
                }
            }
        }*/
        
        public boolean isNotEditable() {
            return isNotEditable ;
        }
        
        public String getInstalledVersion() {
            assert installEl.getSpecificationVersion() != null : installEl + " has specification version.";
            return installEl.getSpecificationVersion().toString();
        }
        
        public String getBackupVersion() {
            return backupEl == null ? "-" : backupEl.getSpecificationVersion().toString();
        }
        
        public Integer getMyRating() {
            return null;
        }
        
        public UpdateElement getRelevantElement() {
            return installEl;
        }

        public int getCompleteSize() {
            return -1;
        }
        
    }
    
    public static class Update extends Unit {
        private UpdateElement installEl = null;
        private UpdateElement updateEl = null;
        private boolean isNbms;
        private int size = -1;
        
        public Update(UpdateUnit unit, boolean isNbms) {
            this.isNbms = isNbms;
            this.updateUnit = unit;
            this.installEl = unit.getInstalled();
            assert installEl != null : "Updateable UpdateUnit " + unit + " has Installed UpdateElement.";
            // XXX: find highest version
            this.updateEl = unit.getAvailableUpdates().get(unit.getAvailableUpdates().size()-1);
            assert updateEl != null : "Updateable UpdateUnit " + unit + " has UpdateElement for update.";
        }
        
        public boolean isMarked() {
            OperationContainer<InstallSupport> container = (isNbms) ? Containers.forUpdateNbms() : Containers.forUpdate();            
            return container.contains(updateEl);
        }
        
        public void setMarked(boolean marked) {
            assert marked != isMarked();
            OperationContainer<InstallSupport> container = (isNbms) ? Containers.forUpdateNbms() : Containers.forUpdate();
            if (marked) {
                container.add(updateUnit, updateEl);
            } else {
                container.remove(updateEl);
            }
        }
        
        public String getInstalledVersion() {
            return installEl.getSpecificationVersion().toString();
        }
        
        public String getAvailableVersion() {
            return updateEl.getSpecificationVersion().toString();
        }
        
        public String getSize() {
            return getSize(updateEl.getDownloadSize());
        }
        
        public UpdateElement getRelevantElement() {
            return updateEl;
        }

        public int getCompleteSize() {
            if (size == -1) {
                size = 0;
                OperationContainer<OperationSupport> c = OperationContainer.createForDirectUpdate ();
                OperationInfo<OperationSupport> i = c.add (getRelevantElement ());
                Set<UpdateElement> elems = i.getRequiredElements ();
                for (UpdateElement el : elems) {
                    size += el.getDownloadSize();
                }
                size += getRelevantElement ().getDownloadSize ();
                c.removeAll ();
            }
            return size;
        }
        
    }
    
    public static class Available extends Unit {
        private UpdateElement updateEl = null;
        private boolean isNbms;
        private int size = -1;
        
        public Available(UpdateUnit unit, boolean isNbms) {
            this.isNbms = isNbms;
            this.updateUnit = unit;
            // XXX: find highest version
            this.updateEl = unit.getAvailableUpdates().get(unit.getAvailableUpdates().size()-1);
            assert updateEl != null : "Updateable UpdateUnit " + unit + " has UpdateElement for update.";
        }

        public boolean isMarked() {
            OperationContainer<InstallSupport> container = (isNbms) ? Containers.forAvailableNbms() : Containers.forAvailable();            
            return container.contains(updateEl);
        }
        
        public void setMarked(boolean marked) {
            assert marked != isMarked();
            OperationContainer<InstallSupport> container = (isNbms) ? Containers.forAvailableNbms() : Containers.forAvailable();
            
            if (marked) {
                container.add(updateUnit, updateEl);
            } else {
                container.remove(updateEl);
            }
        }
        
        public String getAvailableVersion() {
            return updateEl.getSpecificationVersion().toString();
        }
        
        public Integer getMyRating() {
            return null;
        }
        
        public String getSize() {
            return getSize(updateEl.getDownloadSize());
        }
        
        public UpdateElement getRelevantElement() {
            return updateEl;
        }
        
        public int getCompleteSize() {
            if (size == -1) {
                size = 0;
                OperationContainer<OperationSupport> c = OperationContainer.createForDirectInstall ();
                OperationInfo<OperationSupport> i = c.add (getRelevantElement ());
                Set<UpdateElement> elems = i.getRequiredElements ();
                for (UpdateElement el : elems) {
                    size += el.getDownloadSize();
                }
                size += getRelevantElement ().getDownloadSize ();
                c.removeAll ();
            }
            return size;
        }
        
    }
    
    public static class DummyUnit extends Unit {
        private UpdateElement element;
        public DummyUnit(UpdateElement el) {
            element = el;
        }
        
        protected UpdateElement getRelevantElement() {
            return element;
        }
        
        public boolean isMarked() {
            return false;
        }
    
        public void setMarked(boolean marked) {}

        public int getCompleteSize() {
            throw new UnsupportedOperationException ("Not supported yet.");
        }
}
    
    static String getSize(int size) {
        int gbSize = size / (1024 * 1024 * 1024);
        if (gbSize > 0) {
            return gbSize + "GB";
        }
        int mbSize = size / (1024 * 1024);
        if (mbSize > 0) {
            return mbSize + "MB";
        }
        int kbSize = size / 1034;
        if (kbSize > 0) {
            return kbSize + "kB";
        }
        return size + "B";
    }
    
}

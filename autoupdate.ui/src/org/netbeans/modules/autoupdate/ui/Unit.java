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

package org.netbeans.modules.autoupdate.ui;

import java.text.Collator;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.netbeans.modules.autoupdate.ui.UnitCategoryTableModel.Type;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jiri Rechtacek, Radek Matous
 */
public abstract class Unit {
    UpdateUnit updateUnit = null;
    private boolean isVisible;
    private String filter;
    private String categoryName;
    private static Logger log = Logger.getLogger (Unit.class.getName ());
    private String displayDate = null;
    
    protected abstract UpdateElement getRelevantElement ();
    public abstract boolean isMarked ();
    public abstract void setMarked (boolean marked);
    public abstract int getCompleteSize ();
    
    Unit (String categoryName) {
        this.categoryName = categoryName;
    }
    
    public abstract UnitCategoryTableModel.Type getModelType();
    public void initState() {
        if (UnitCategoryTableModel.isMarkedAsDefault(getModelType())) {
            if (!isMarked() && canBeMarked()) {
                setMarked(true);
            }
        }
    }
    
    public String getCategoryName () {
        return categoryName;
    }
    
    public boolean canBeMarked () {
        RequestProcessor.Task t = PluginManagerUI.getRunningTask ();
        return t == null || t.isFinished ();
    }
    
    public String getDisplayName () {
        return getRelevantElement ().getDisplayName ();
    }
    
    public final boolean isVisible (final String filter) {
        if (this.filter != null && this.filter.equals (filter)) {
            return isVisible;
        }
        this.filter = filter;
        Iterable<String> iterable = details ();
        for (String detail : iterable) {
            isVisible = filter.length () == 0 || detail.toLowerCase ().contains (filter);
            if (isVisible) break;
        }
        return isVisible;
    }
    
    private Iterable<String> details () {
        Iterable<String> retval = new Iterable<String>(){
            public Iterator<String> iterator () {
                return new Iterator<String>() {
                    int step = 0;
                    public boolean hasNext () {
                        return step <= 7;
                    }
                    
                    public String next () {
                        String next = null;
                        switch(step++) {
                        case 0:
                            next = getDisplayName ();break;
                        case 1:
                            next = getCategoryName ();break;
                        case 2:
                            next = getDescription ();break;
                        case 3:
                            next = updateUnit.getCodeName ();break;
                        case 4:
                            next = getDisplayVersion ();break;
                        case 5:
                            next = getAuthor ();break;
                        case 6:
                            next = getHomepage ();break;
                        case 7:
                            next = getSource ();break;
                        }
                        return next != null ? next : "";//NOI18N
                    }
                    
                    public void remove () {
                        throw new UnsupportedOperationException ("Not supported yet.");
                    }
                };
            }
        };
        return retval;
    }     
    
    public String getFilter() {
        return filter;
    }
    
    public String getDescription () {
        return getRelevantElement ().getDescription ();
    }
    
    public String getNotification () {
        return getRelevantElement ().getNotification ();
    }
    
    public String getAuthor () {
        return getRelevantElement ().getAuthor ();
    }
    
    public String getHomepage () {
        return getRelevantElement ().getHomepage ();
    }
    
    public String getSource () {
        return getRelevantElement ().getSource ();
    }
    
    public String getDisplayVersion () {
        return getRelevantElement ().getSpecificationVersion ();
    }
    
    public String getDisplayDate () {
        if (displayDate == null) {
            String sd = getRelevantElement ().getDate ();
            if (sd != null) {
                try {
                    Date d = Utilities.DATE_FORMAT.parse (sd);
                    displayDate = DateFormat.getDateInstance (DateFormat.SHORT, Locale.getDefault ()).format (d);
                } catch (ParseException pe) {
                    log.log (Level.INFO, "ParseException while parsing date " + sd, pe);
                }
            }
        }
        return displayDate;
    }
    
    public static int compareDisplayNames (Unit unit1, Unit unit2) {
        //if (!Utilities.modulesOnly()) {
        return Utilities.getCategoryComparator().compare(unit1.getDisplayName(), unit2.getDisplayName());
        //}
        //return Collator.getInstance().compare(unit1.getDisplayName(), unit2.getDisplayName());
    }
    
    public static int compareCategories (Unit unit1, Unit unit2) {
        return Utilities.getCategoryComparator ().compare (unit1.getCategoryName (), unit2.getCategoryName ());
    }
    
    public static int compareSimpleFormatDates (Unit u1, Unit u2) {
        
        if (u1.getRelevantElement ().getDate () == null) {
            if (u2.getRelevantElement ().getDate () == null) {
                return 0;
            } else {
                return -1;
            }
        } else if (u2.getRelevantElement ().getDate () == null) {
            return 1;
        }
        
        Date d1;
        Date d2;
        try {
            d1 = Utilities.DATE_FORMAT.parse (u1.getRelevantElement ().getDate ());
        } catch (ParseException pe) {
            log.log (Level.INFO, "ParseException while parsing date " + u1.getRelevantElement ().getDate (), pe);
            return -1;
        }
        try {
            d2 = Utilities.DATE_FORMAT.parse (u2.getRelevantElement ().getDate ());
        } catch (ParseException pe) {
            log.log (Level.INFO, "ParseException while parsing date " + u2.getRelevantElement ().getDate (), pe);
            return 1;
        }
        return d1.compareTo (d2);
    }
    
    public static int compareDisplayVersions (Unit unit1, Unit unit2) {
        if (unit1.getDisplayVersion () == null) {
            if (unit2.getDisplayVersion () == null) {
                return 0;
            } else {
                return -1;
            }
        } else if (unit2.getDisplayVersion () == null) {
            return 1;
        }
        return new SpecificationVersion (unit1.getDisplayVersion ()).compareTo (new SpecificationVersion (unit2.getDisplayVersion ()));
    }
    
    public static int compareCompleteSizes (Unit unit1, Unit unit2) {
        return Integer.valueOf (unit1.getCompleteSize ()).compareTo (unit2.getCompleteSize ());
    }
    
    public static class Installed extends Unit {
        
        private UpdateElement installEl = null;
        private UpdateElement backupEl = null;
        private boolean uninstallationAllowed;
        private boolean deactivationAllowed;
        
        public static boolean isOperationAllowed (UpdateUnit uUnit, UpdateElement element, OperationContainer<OperationSupport> container) {
            return container.canBeAdded (uUnit, element);
        }
        
        public Installed (UpdateUnit unit, String categoryName) {
            super (categoryName);
            this.updateUnit = unit;
            if (unit.getInstalled () == null && unit.isPending ()) {
                this.installEl = unit.getAvailableUpdates ().get (0);
                assert installEl != null : "Pending UpdateUnit " + unit + " has UpdateElement for update.";
            } else {
                this.installEl = unit.getInstalled ();
                assert installEl != null : "Installed UpdateUnit " + unit + " has Installed UpdateElement.";
            }
            this.backupEl = unit.getBackup ();
            OperationContainer<OperationSupport> container = null;
            if (UpdateManager.TYPE.CUSTOM_HANDLED_COMPONENT == updateUnit.getType ()) {
                container = Containers.forCustomUninstall ();
            } else {
                container = Containers.forUninstall ();
            }
            uninstallationAllowed = isOperationAllowed (this.updateUnit, installEl, container);
            deactivationAllowed = isOperationAllowed (this.updateUnit, installEl, Containers.forDisable());
                
            initState();
        }

        public boolean isUninstallAllowed() {
            return uninstallationAllowed;
        }
        public boolean isDeactivationAllowed() {
            return deactivationAllowed;
        }
        
        public boolean isMarked () {
            boolean uninstallMarked;
            OperationContainer container = null;
            if (UpdateManager.TYPE.CUSTOM_HANDLED_COMPONENT == updateUnit.getType ()) {
                container = Containers.forCustomUninstall ();
            } else {
                container = Containers.forUninstall ();
            }
            uninstallMarked = container.contains (installEl);
            boolean deactivateMarked = Containers.forDisable().contains (installEl);
            return deactivateMarked || uninstallMarked;
        }
        
        public void setMarked (boolean marked) {
            assert marked != isMarked ();
            if (isUninstallAllowed()) {
                OperationContainer container = null;
                if (UpdateManager.TYPE.CUSTOM_HANDLED_COMPONENT == updateUnit.getType ()) {
                    container = Containers.forCustomUninstall ();
                } else {
                    container = Containers.forUninstall ();
                }
                if (marked) {
                    container.add (updateUnit, installEl);
                } else {
                    container.remove (installEl);
                }
            }
            if (isDeactivationAllowed()) {
                OperationContainer container = Containers.forDisable();
                if (marked) {
                    container.add (updateUnit, installEl);
                } else {
                    container.remove (installEl);
                }
            }
        }
        
        public static int compareEnabledState (Unit u1, Unit u2) {
            if (u1 instanceof Unit.Installed && u2 instanceof Unit.Installed) {
                Unit.Installed unit1 = (Unit.Installed )u1;
                Unit.Installed unit2 = (Unit.Installed )u2;
                final int retval = Boolean.valueOf(unit1.getRelevantElement().isEnabled()).compareTo(unit2.getRelevantElement().isEnabled());
                return (retval == 0) ? Boolean.valueOf(unit1.updateUnit.isPending()).compareTo(unit2.updateUnit.isPending()) : retval;
            }
            return Unit.compareDisplayVersions (u1, u2);
        }
        
        public static int compareInstalledVersions (Unit u1, Unit u2) {
            if (u1 instanceof Unit.Installed && u2 instanceof Unit.Installed) {
                Unit.Installed unit1 = (Unit.Installed )u1;
                Unit.Installed unit2 = (Unit.Installed )u2;
                if (unit1.getInstalledVersion () == null) {
                    if (unit2.getInstalledVersion () == null) {
                        return 0;
                    } else {
                        return -1;
                    }
                } else if (unit2.getInstalledVersion () == null) {
                    return 1;
                }
                return new SpecificationVersion (unit1.getInstalledVersion ()).compareTo (new SpecificationVersion (unit2.getInstalledVersion ()));
            }
            return Unit.compareDisplayVersions (u1, u2);
        }

        @Override
        public boolean canBeMarked () {
            return super.canBeMarked () && (isDeactivationAllowed() || isUninstallAllowed());
        }
        
        public String getInstalledVersion () {
            return installEl.getSpecificationVersion ();
        }
        
        public String getBackupVersion () {
            return backupEl == null ? "-" : backupEl.getSpecificationVersion ();
        }
        
        public Integer getMyRating () {
            return null;
        }
        
        public UpdateElement getRelevantElement () {
            return installEl;
        }
        
        public int getCompleteSize () {
            return -1;
        }

        public Type getModelType() {
            return UnitCategoryTableModel.Type.INSTALLED;
        }
        
    }

    public static class InternalUpdate extends Unit.Update  {
        
        private List <UpdateUnit> internalUpdates;        

        public InternalUpdate(UpdateUnit updateUnit, String categoryName, boolean isNbms) {
            super(updateUnit, false, categoryName);
        }

        public List <UpdateUnit> getUpdateUnits() {
            if(internalUpdates == null) {
                internalUpdates = new ArrayList <UpdateUnit>();
            }
            return internalUpdates;
        }
        public UpdateUnit getVisibleUnit() {
            return updateUnit;
        }
        
        @Override
        public UpdateElement getRelevantElement() {
            return updateUnit.getInstalled();
        }

        @Override
        public boolean isMarked() {
            OperationContainer container = Containers.forUpdate ();
            for(UpdateUnit invisible : getUpdateUnits()) {
                if(!container.contains(invisible.getAvailableUpdates().get(0))) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String getAvailableVersion () {
            return getInstalledVersion() + " " + getBundle("Unit_InternalUpdates_Version");
        }
        @Override
        public void setMarked(boolean marked) {
            assert marked != isMarked();
            OperationContainer container = Containers.forUpdate();
            for (UpdateUnit invisible : getUpdateUnits()) {
                if (marked) {
                    if (container.canBeAdded(invisible, invisible.getAvailableUpdates().get(0))) {
                        container.add(invisible, invisible.getAvailableUpdates().get(0));
                    }
                } else {
                    container.remove(invisible.getAvailableUpdates().get(0));
                }
            }
        }

        @Override
        public int getCompleteSize() {
            if (size == -1) {
                size = 0;
                for (UpdateUnit u : getUpdateUnits()) {
                    size += u.getAvailableUpdates().get(0).getDownloadSize();
                }

            }
            return size;
        }

        public String getSize () {
            return Utilities.getDownloadSizeAsString (getCompleteSize());
        }

        @Override
        public Type getModelType() {
            return Type.UPDATE;
        }
        
    }
    
    public static class Update extends Unit {
        private UpdateElement installEl = null;
        private UpdateElement updateEl = null;
        private boolean isNbms;
        protected int size = -1;
        
        public Update (UpdateUnit unit, boolean isNbms,String categoryName) {
            super (categoryName);
            this.isNbms = isNbms;
            this.updateUnit = unit;
            this.installEl = unit.getInstalled ();
            assert installEl != null : "Updateable UpdateUnit " + unit + " has Installed UpdateElement.";
            if(unit.getAvailableUpdates().size() > 0) {
                this.updateEl = unit.getAvailableUpdates ().get (0);
                assert updateEl != null : "Updateable UpdateUnit " + unit + " has UpdateElement for update.";
            }
            initState();
        }
        
        public boolean isMarked () {
            OperationContainer container = null;
            if (isNbms) {
                container = Containers.forUpdateNbms ();
            } else if (UpdateManager.TYPE.CUSTOM_HANDLED_COMPONENT == updateUnit.getType ()) {
                container = Containers.forCustomInstall ();
            } else {
                container = Containers.forUpdate ();
            }
            return container.contains (updateEl);
        }
        
        public void setMarked (boolean marked) {
            assert marked != isMarked ();
            OperationContainer container = null;
            if (isNbms) {
                container = Containers.forUpdateNbms ();
            } else if (UpdateManager.TYPE.CUSTOM_HANDLED_COMPONENT == updateUnit.getType ()) {
                container = Containers.forCustomInstall ();
            } else {
                container = Containers.forUpdate ();
            }
            if (marked) {
                container.add (updateUnit, updateEl);
            } else {
                container.remove (updateEl);
            }
        }
        
        public static int compareInstalledVersions (Unit u1, Unit u2) {
            if (u1 instanceof Unit.Update && u2 instanceof Unit.Update) {
                Unit.Update unit1 = (Unit.Update)u1;
                Unit.Update unit2 = (Unit.Update)u2;
                return new SpecificationVersion (unit1.getInstalledVersion ()).compareTo (new SpecificationVersion (unit2.getInstalledVersion ()));
            }
            return Unit.compareDisplayVersions (u1, u2);
        }
        
        public static int compareAvailableVersions (Unit u1, Unit u2) {
            if (u1 instanceof Unit.Update && u2 instanceof Unit.Update) {
                Unit.Update unit1 = (Unit.Update)u1;
                Unit.Update unit2 = (Unit.Update)u2;
                return new SpecificationVersion (unit1.getAvailableVersion ()).compareTo (new SpecificationVersion (unit2.getAvailableVersion ()));
            }
            return Unit.compareDisplayVersions (u1, u2);
        }
        
        
        public String getInstalledVersion () {
            return installEl.getSpecificationVersion ();
        }
        
        public String getAvailableVersion () {
            return updateEl.getSpecificationVersion ();
        }
        
        public String getSize () {
            return Utilities.getDownloadSizeAsString (updateEl.getDownloadSize ());
        }
        
        public UpdateElement getRelevantElement () {
            return updateEl;
        }
        
        public int getCompleteSize () {
            if (size == -1) {
                size = 0;
                OperationContainer<OperationSupport> c = OperationContainer.createForDirectUpdate ();
                OperationInfo<OperationSupport> i = c.add (getRelevantElement ());
                Set<UpdateElement> elems = i.getRequiredElements ();
                for (UpdateElement el : elems) {
                    size += el.getDownloadSize ();
                }
                size += getRelevantElement ().getDownloadSize ();
                c.removeAll ();
            }
            return size;
        }

        public Type getModelType() {
            return (isNbms) ? UnitCategoryTableModel.Type.LOCAL : UnitCategoryTableModel.Type.UPDATE;            
        }
        
    }
    
    public static class Available extends Unit {
        private UpdateElement updateEl = null;
        private boolean isNbms;
        private int size = -1;
        
        public Available (UpdateUnit unit, boolean isNbms,String categoryName) {
            super (categoryName);
            this.isNbms = isNbms;
            this.updateUnit = unit;
            this.updateEl = unit.getAvailableUpdates ().get (0);
            assert updateEl != null : "Updateable UpdateUnit " + unit + " has UpdateElement for update.";
            initState();
        }
        
        public boolean isMarked () {
            OperationContainer container = null;
            if (isNbms) {
                container = Containers.forAvailableNbms();
            } else if (UpdateManager.TYPE.CUSTOM_HANDLED_COMPONENT == updateUnit.getType ()) {
                container = Containers.forCustomInstall ();
            } else {
                container = Containers.forAvailable ();
            }
            return container.contains (updateEl);
        }
        
        public void setMarked (boolean marked) {
            assert marked != isMarked ();
            OperationContainer container = null;
            if (isNbms) {
                container = Containers.forAvailableNbms();
            } else if (UpdateManager.TYPE.CUSTOM_HANDLED_COMPONENT == updateUnit.getType ()) {
                container = Containers.forCustomInstall ();
            } else {
                container = Containers.forAvailable ();
            }
            if (marked) {
                container.add (updateUnit, updateEl);
            } else {
                container.remove (updateEl);
            }
        }
        
        public static int compareAvailableVersion (Unit u1, Unit u2) {
            if (u1 instanceof Unit.Available && u2 instanceof Unit.Available) {
                Unit.Available unit1 = (Unit.Available)u1;
                Unit.Available unit2 = (Unit.Available)u2;
                return new SpecificationVersion (unit1.getAvailableVersion ()).compareTo (new SpecificationVersion (unit2.getAvailableVersion ()));
            }
            return Unit.compareDisplayVersions (u1, u2);
        }

        public static int compareSourceCategories(Unit u1, Unit u2) {
            if (u1 instanceof Unit.Available && u2 instanceof Unit.Available) {
                Unit.Available unit1 = (Unit.Available)u1;
                Unit.Available unit2 = (Unit.Available)u2;
                return Collator.getInstance().compare(unit1.getSourceCategory().name(), unit2.getSourceCategory().name());
            }
            
            throw new IllegalStateException();
        }
        
        public String getAvailableVersion () {
            return updateEl.getSpecificationVersion ();
        }
        
        public Integer getMyRating () {
            return null;
        }
        
        public String getSize () {
            return Utilities.getDownloadSizeAsString (updateEl.getDownloadSize ());
        }
        
        public UpdateElement getRelevantElement () {
            return updateEl;
        }
        
        public int getCompleteSize () {
            if (size == -1) {
                size = 0;
                OperationContainer<OperationSupport> c = OperationContainer.createForDirectInstall ();
                OperationInfo<OperationSupport> i = c.add (getRelevantElement ());
                Set<UpdateElement> elems = i.getRequiredElements ();
                for (UpdateElement el : elems) {
                    size += el.getDownloadSize ();
                }
                size += getRelevantElement ().getDownloadSize ();
                c.removeAll ();
            }
            return size;
        }

        public Type getModelType() {
            return (isNbms) ? UnitCategoryTableModel.Type.LOCAL : UnitCategoryTableModel.Type.AVAILABLE;
        }        
        
        public CATEGORY getSourceCategory() {
            return updateEl.getSourceCategory();
        }
    }

    private static String getBundle (String key) {
        return NbBundle.getMessage (Unit.class, key);
    }
    
}

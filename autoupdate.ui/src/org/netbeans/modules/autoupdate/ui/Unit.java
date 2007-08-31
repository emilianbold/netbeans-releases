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

import java.text.Collator;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
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
        return true;
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
    public String annotateDisplayName (String toAnnotate) {
        if (this instanceof Unit.Installed) {
            Unit.Installed i = ((Unit.Installed)this);
            if (i.getRelevantElement ().isEnabled ()) {
                return "<font color=\"green\">"+toAnnotate+"</font>";
            } else {
                return "<font color=\"red\">"+toAnnotate+"</font>";
            }
        }
        return toAnnotate;
    }
    public String annotate (String toAnnotate) {
        if (isVisible && filter.length () != 0) {
            String toAnnotate2 = toAnnotate.toLowerCase ();
            int startIdx = 0;
            int stopIdx = toAnnotate2.indexOf (filter);
            StringBuffer sb = new StringBuffer ();
            while (stopIdx > -1) {                
                sb.append (toAnnotate.substring (startIdx, stopIdx));
                sb.append ("<font bgcolor=\"yellow\">"+toAnnotate.substring (stopIdx,stopIdx+filter.length ())+"</font>");
                startIdx = stopIdx + +filter.length ();
                stopIdx = toAnnotate2.indexOf (filter, startIdx);
            }
            sb.append (toAnnotate.substring (startIdx));
            if (startIdx > 0) {
                return sb.toString ();
            }
        }
        return toAnnotate;
    }

    public int findCaretPosition (String toAnnotate) {
        if (isVisible && filter.length () != 0) {
            return toAnnotate.toLowerCase ().indexOf (filter);
        }
        return -1;
    }
    
    
    public String getDescription () {
        return getRelevantElement ().getDescription ();
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
        return getRelevantElement ().getSpecificationVersion ().toString ();
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
        return new SpecificationVersion (unit1.getDisplayVersion ()).compareTo (new SpecificationVersion (unit2.getDisplayVersion ()));
    }
    
    public static int compareCompleteSizes (Unit unit1, Unit unit2) {
        return Integer.valueOf (unit1.getCompleteSize ()).compareTo (unit2.getCompleteSize ());
    }
    
    public static class Installed extends Unit {
        
        private UpdateElement installEl = null;
        private UpdateElement backupEl = null;
        private boolean isUninstallAllowed ;
        
        public static boolean isOperationAllowed (UpdateUnit uUnit, UpdateElement element, OperationContainer<OperationSupport> container) {
            return container.canBeAdded (uUnit, element);
        }
        public Installed (UpdateUnit unit, String categoryName) {
            super (categoryName);
            this.updateUnit = unit;
            this.installEl = unit.getInstalled ();
            assert installEl != null : "Installed UpdateUnit " + unit + " has Installed UpdateElement.";
            this.backupEl = unit.getBackup ();
            this.isUninstallAllowed = isOperationAllowed (this.updateUnit, installEl, Containers.forUninstall ());
            initState();
        }
        
        public boolean isMarked () {
            return Containers.forUninstall ().contains (installEl);
        }
        
        public void setMarked (boolean marked) {
            assert marked != isMarked ();
            if (marked) {
                Containers.forUninstall ().add (updateUnit, installEl);
            } else {
                Containers.forUninstall ().remove (installEl);
            }
        }
        
        public static int compareEnabledState (Unit u1, Unit u2) {
            if (u1 instanceof Unit.Installed && u2 instanceof Unit.Installed) {
                Unit.Installed unit1 = (Unit.Installed )u1;
                Unit.Installed unit2 = (Unit.Installed )u2;
                return Boolean.valueOf (unit1.getRelevantElement ().isEnabled ()).compareTo (unit2.getRelevantElement ().isEnabled ());
            }
            return Unit.compareDisplayVersions (u1, u2);
        }
        
        public static int compareInstalledVersions (Unit u1, Unit u2) {
            if (u1 instanceof Unit.Installed && u2 instanceof Unit.Installed) {
                Unit.Installed unit1 = (Unit.Installed )u1;
                Unit.Installed unit2 = (Unit.Installed )u2;
                return new SpecificationVersion (unit1.getInstalledVersion ()).compareTo (new SpecificationVersion (unit2.getInstalledVersion ()));
            }
            return Unit.compareDisplayVersions (u1, u2);
        }
        
        @Override
        public boolean canBeMarked () {
            return isUninstallAllowed ;
        }
        
        public String getInstalledVersion () {
            assert installEl.getSpecificationVersion () != null : installEl + " has specification version.";
            return installEl.getSpecificationVersion ().toString ();
        }
        
        public String getBackupVersion () {
            return backupEl == null ? "-" : backupEl.getSpecificationVersion ().toString ();
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
    
    public static class Update extends Unit {
        private UpdateElement installEl = null;
        private UpdateElement updateEl = null;
        private boolean isNbms;
        private int size = -1;
        
        public Update (UpdateUnit unit, boolean isNbms,String categoryName) {
            super (categoryName);
            this.isNbms = isNbms;
            this.updateUnit = unit;
            this.installEl = unit.getInstalled ();
            assert installEl != null : "Updateable UpdateUnit " + unit + " has Installed UpdateElement.";
            this.updateEl = unit.getAvailableUpdates ().get (unit.getAvailableUpdates ().size () - 1);
            assert updateEl != null : "Updateable UpdateUnit " + unit + " has UpdateElement for update.";
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
            return installEl.getSpecificationVersion ().toString ();
        }
        
        public String getAvailableVersion () {
            return updateEl.getSpecificationVersion ().toString ();
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
            return updateEl.getSpecificationVersion ().toString ();
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
    
}

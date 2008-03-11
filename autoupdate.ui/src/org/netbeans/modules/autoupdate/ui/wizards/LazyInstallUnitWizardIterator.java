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

package org.netbeans.modules.autoupdate.ui.wizards;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.modules.autoupdate.ui.actions.AutoupdateCheckScheduler;
import org.netbeans.modules.autoupdate.ui.wizards.OperationWizardModel.OperationType;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jiri Rechtacek
 */
public final class LazyInstallUnitWizardIterator implements WizardDescriptor.Iterator<WizardDescriptor> {
    
    private int index;
    private List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>> ();
    private Collection<LazyUnit> installModel;
    private OperationType doOperation;

    public LazyInstallUnitWizardIterator (Collection<LazyUnit> model, OperationType doOperation) {
        this.installModel = model;
        this.doOperation = doOperation;
        createPanels ();
        index = 0;
    }
    
    private void createPanels () {
        assert panels != null && panels.isEmpty() : "Panels are still empty";
        panels.add (new LazyOperationDescriptionStep (installModel, doOperation));
    }
    
    public WizardDescriptor.Panel<WizardDescriptor> current () {
        assert panels != null;
        return panels.get (index);
    }
    
    public String name () {
        return NbBundle.getMessage (LazyInstallUnitWizardIterator.class, "InstallUnitWizard_Title");
    }
    
    public boolean hasNext () {
        return false;
    }
    
    public boolean hasPrevious () {
        return false;
    }
    
    public void nextPanel () {}
    
    public void previousPanel () {}
    
    public synchronized void addChangeListener(ChangeListener l) {}

    public synchronized void removeChangeListener(ChangeListener l) {}

    public static class LazyUnit extends Object {
        private String codeName;
        private String displayName;
        private String oldVersion;
        private String newVersion;
        private String notification;
        
        private static final String DELIMETER = "|";
        
        private LazyUnit (String codeName, String displayName, String oldVersion, String newVersion, String notification) {
            this.codeName = codeName;
            this.displayName = displayName;
            this.oldVersion = oldVersion;
            this.newVersion = newVersion;
            this.notification = notification;
        }
        
        public static void storeLazyUnits (OperationType operationType, Collection<UpdateElement> elements) {
            Preferences p = getPreferences (operationType);
            try {
                if (p.keys ().length > 0) {
                    p.clear ();
                }
            } catch (BackingStoreException ex) {
                Logger.getLogger (LazyInstallUnitWizardIterator.class.getName ()).log (Level.WARNING, ex.getLocalizedMessage (), ex);
            }
            if (elements == null) {
                return ;
            }
            for (UpdateElement el : elements) {
                p.put (el.getCodeName (), toString (el));
            }
        }
        
        public static Collection<LazyUnit> loadLazyUnits (OperationType operationType) {
            Preferences p = getPreferences (operationType);
            Collection<LazyUnit> units = new HashSet<LazyUnit> ();
            try {
                for (String cn : p.keys ()) {
                    units.add (parseLazyUnit (p.get (cn, null)));
                }
            } catch (BackingStoreException ex) {
                Logger.getLogger (LazyInstallUnitWizardIterator.class.getName ()).log (Level.WARNING, ex.getLocalizedMessage (), ex);
                return null;
            }
            return units;
        }
        
        public String getCodeName () {
            return codeName;
        }
        
        public String getDisplayName () {
            return displayName == null ? codeName : displayName;
        }
        
        public String getOldVersion () {
            return oldVersion == null ? "" : oldVersion;
        }
        
        public String getNewVersion () {
            return newVersion == null ? "" : newVersion;
        }
        
        public String getNotification () {
            return notification == null ? "" : notification.trim ();
        }
        
        private static String toString (UpdateElement el) {
            return  el.getCodeName () + DELIMETER +
                    (el.getDisplayName () == null ? el.getCodeName () : el.getDisplayName ()) + DELIMETER +
                    (el.getUpdateUnit ().getInstalled () == null ? "" : el.getUpdateUnit ().getInstalled ().getSpecificationVersion ()) + DELIMETER +
                    (el.getSpecificationVersion () == null ? "" : el.getSpecificationVersion ()) + DELIMETER +
                    (el.getNotification () == null ? " " : el.getNotification ());
        }
        
        private static LazyUnit parseLazyUnit (String s) {
            StringTokenizer tokenizer = new StringTokenizer (s, DELIMETER);
            assert 5 == tokenizer.countTokens () : "5 tokens for " + s;
            String codeName = tokenizer.nextToken ();
            String displayName = tokenizer.nextToken ();
            String oldVersion = tokenizer.nextToken ();
            String newVersion = tokenizer.nextToken ();
            String notification = tokenizer.nextToken ();
            return new LazyUnit (codeName, displayName, oldVersion, newVersion, notification);
        }
        
        private static Preferences getPreferences (OperationType type) {
            return NbPreferences.forModule (AutoupdateCheckScheduler.class).node (type.toString ());
        }
    
    }

}

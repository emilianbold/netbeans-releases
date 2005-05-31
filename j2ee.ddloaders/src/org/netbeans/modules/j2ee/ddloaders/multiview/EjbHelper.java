/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.common.EnvEntry;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;

/**
 * @author pfiala
 */
public class EjbHelper {
    private EjbJarMultiViewDataObject dataObject;
    private Ejb ejb;

    public EjbHelper(EjbJarMultiViewDataObject dataObject, Ejb ejb) {
        this.dataObject = dataObject;
        this.ejb = ejb;
    }

    public EnvEntryHelper getEnvEntryHelper(int rowIndex) {
        return new EnvEntryHelper(ejb.getEnvEntry(rowIndex));
    }

    public int getEnvEntryCount() {
        return ejb.getEnvEntry().length;
    }

    public EnvEntryHelper newEnvEntry() {
        EnvEntry entry = ejb.newEnvEntry();
        ejb.addEnvEntry(entry);
        modelUpdatedFromUI();
        return new EnvEntryHelper(entry);
    }

    public void removeEnvEntry(int row) {
        ejb.removeEnvEntry(ejb.getEnvEntry(row));
        modelUpdatedFromUI();
    }

    private void modelUpdatedFromUI() {
        dataObject.modelUpdatedFromUI();
    }

    public class EnvEntryHelper {
        private EnvEntry envEntry;

        public EnvEntryHelper(EnvEntry envEntry) {
            this.envEntry = envEntry;
            modelUpdatedFromUI();
        }

        public void setEnvEntryName(String value) {
            envEntry.setEnvEntryName(value);
            modelUpdatedFromUI();
        }

        public void setEnvEntryType(String value) {
            envEntry.setEnvEntryType(value);
            modelUpdatedFromUI();
        }

        public void setEnvEntryValue(String value) {
            envEntry.setEnvEntryValue(value);
            modelUpdatedFromUI();
        }

        public void setDescription(String description) {
            envEntry.setDescription(description);
            modelUpdatedFromUI();
        }

        public String getEnvEntryName() {
            return envEntry.getEnvEntryName();
        }

        public String getEnvEntryType() {
            return envEntry.getEnvEntryType();
        }

        public String getEnvEntryValue() {
            return envEntry.getEnvEntryValue();
        }

        public String getDefaultDescription() {
            return envEntry.getDefaultDescription();
        }
    }
}

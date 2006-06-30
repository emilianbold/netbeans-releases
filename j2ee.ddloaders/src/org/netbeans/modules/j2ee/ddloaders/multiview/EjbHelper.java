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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

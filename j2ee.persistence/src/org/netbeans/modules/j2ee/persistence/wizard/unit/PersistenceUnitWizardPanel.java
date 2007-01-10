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

package org.netbeans.modules.j2ee.persistence.wizard.unit;

import javax.swing.JPanel;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.Provider;

/**
 *
 * @author Pavel Buzek
 */
public abstract class PersistenceUnitWizardPanel extends JPanel {
 
    public static final String IS_VALID = "PersistenceUnitWizardPanel_isValid"; //NOI18N

    /**
     * Table generation strategy.
     */
    public enum TableGeneration {
        CREATE, DROP_CREATE, NONE
    }
    
    public abstract String getPersistenceUnitName();

    public abstract String getTableGeneration();
    
    public abstract boolean isValidPanel();
    
    /** Either data source jdbc name or connection name */
    public abstract void setPreselectedDB(String db);

    /**
     * Checks whether name of the persistence unit is unique among current
     * project's persistence units.
     * @return true if the name is unique, false otherwise.
     * @throws InvalidPersistenceXmlException if the project has an invalid 
     *  persistence.xml file.
     */
    public abstract boolean isNameUnique() throws InvalidPersistenceXmlException;
    
    /**
     * @return the selected provider.
     */
    public abstract Provider getSelectedProvider();
    
    /**
     * Sets an error message to the panel.
     * @param msg the message to set.
     */
    public abstract void setErrorMessage(String msg);
    
}

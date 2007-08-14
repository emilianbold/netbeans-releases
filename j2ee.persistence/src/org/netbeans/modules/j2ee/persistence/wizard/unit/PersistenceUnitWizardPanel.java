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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
import org.openide.util.Parameters;

/**
 *
 * @author Pavel Buzek
 */
public abstract class PersistenceUnitWizardPanel extends JPanel {

    protected final Project project;
    private static final Logger LOG = Logger.getLogger(PersistenceUnitWizardPanel.class.getName());
    
    public static final String IS_VALID = "PersistenceUnitWizardPanel_isValid"; //NOI18N

    protected PersistenceUnitWizardPanel(Project project) {
        Parameters.notNull("project", project); //NO18N
        this.project = project;
    }

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
     * Checks whether the name of the persistence unit is unique among current
     * project's persistence units.
     * @return true if the name is unique, false otherwise.
     * @throws InvalidPersistenceXmlException if the project has an invalid 
     *  persistence.xml file.
     */
    public final boolean isNameUnique() throws InvalidPersistenceXmlException {
        if (!ProviderUtil.persistenceExists(project)) {
            return true;
        }
        PUDataObject pudo = ProviderUtil.getPUDataObject(project);
        Persistence persistence = pudo.getPersistence();
        return isUnique(getPersistenceUnitName(), persistence.getPersistenceUnit());
    }

    /**
     *@return an initial name for a persistence unit, i.e. a name that 
     * is unique.
     */ 
    protected final String getCandidateName(){
        String candidateNameBase = ProjectUtils.getInformation(project).getName() + "PU"; //NO18N
        try {
            if (!ProviderUtil.persistenceExists(project)) {
                return candidateNameBase;
            }
            PUDataObject pudo = ProviderUtil.getPUDataObject(project);
            Persistence persistence = pudo.getPersistence();

            int suffix = 2;
            PersistenceUnit[] punits = persistence.getPersistenceUnit();
            String candidateName = candidateNameBase;
            while (!isUnique(candidateName, punits)) {
                candidateName = candidateNameBase + suffix++;
            }
            return candidateName;
        } catch (InvalidPersistenceXmlException ipex) {
            // just log, the user is notified about invalid persistence.xml when 
            // the panel is validated
            LOG.log(Level.FINE, "Invalid persistence.xml found", ipex); //NO18N
        }
        return candidateNameBase;
    }
    
    /**
     * @return true if the given <code>candidate</code> represents a unique
     * name within the names of the given <code>punits</code>, false otherwise.
     */ 
    private boolean isUnique(String candidate, PersistenceUnit[] punits){
        for (PersistenceUnit punit : punits){
            if (candidate.equals(punit.getName())){
                return false;
            }
        }
        return true;
    }
    
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

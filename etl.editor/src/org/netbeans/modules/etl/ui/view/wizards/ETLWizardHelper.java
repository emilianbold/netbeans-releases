/*
 * ETLWizardHelper.java
 *
 * Created on June 25, 2006, 5:12 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.etl.ui.view.wizards;

import java.util.Collections;
import java.util.List;

import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.openide.WizardDescriptor;


/**
 *
 * @author radval
 */
public class ETLWizardHelper {
    
    private WizardDescriptor mDescriptor;
    
    /** Creates a new instance of ETLWizardHelper */
    public ETLWizardHelper(WizardDescriptor descriptor) {
        this.mDescriptor = descriptor;
    }
    
     /**
     * Gets List of destination Databases as selected by user.
     * 
     * @return List (possibly empty) of selected destination Databases
     */
    public List getSelectedDestinationDb() {
        return getSelectedDbOfType(ETLCollaborationWizard.TARGET_DB, this.mDescriptor);
    }

    /**
     * Gets List of source Databases as selected by user.
     * 
     * @return List (possibly empty) of selected source Databases
     */
    public List getSelectedSourceDb() {
        return getSelectedDbOfType(ETLCollaborationWizard.SOURCE_DB, this.mDescriptor);
    }
    
    public SQLJoinView getSQLJoinView() {
        return (SQLJoinView) this.mDescriptor.getProperty(ETLCollaborationWizard.JOIN_VIEW);
    }

    public List getTableColumnNodes() {
        return (List) this.mDescriptor.getProperty(ETLCollaborationWizard.JOIN_VIEW_VISIBLE_COLUMNS);
    }
    
    private static List getSelectedDbOfType(String typeKey, WizardDescriptor descriptor) {
        List selections = Collections.EMPTY_LIST;
        if (descriptor != null && typeKey != null) {
            selections = (List) descriptor.getProperty(typeKey);
        }

        return selections;
    }
    
}

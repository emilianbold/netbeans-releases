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
package org.netbeans.modules.etl.ui.view.cookies;

import java.util.List;

import org.netbeans.modules.etl.model.impl.ETLDefinitionImpl;
import org.netbeans.modules.etl.ui.DataObjectHelper;
import org.netbeans.modules.etl.ui.DataObjectProvider;
import org.netbeans.modules.etl.ui.ETLDataObject;
import org.netbeans.modules.etl.ui.model.impl.ETLCollaborationModel;
import org.netbeans.modules.etl.ui.view.wizards.ETLTableSelectionWizard;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;

import com.sun.sql.framework.utils.Logger;

/**
 * Cookie for exposing access to a dialog box for selecting tables to participate in an
 * eTL collaboration.
 *
 * @author Jonathan Giron
 * @version $Revision$
 */
public class SelectTablesCookie implements Node.Cookie {
    
    private static final boolean DEBUG = false;
    
    private static final String LOG_CATEGORY = SelectTablesCookie.class.getName();
    
    private ETLDataObject dataObj;
    /**
     * Creates a new instance of SelectTablesCookie associated with the given
     * ProjectElement.
     *
     * @param pElement the associated project element
     */
    public SelectTablesCookie() {
    }
    
    /**
     * Displays the table selection wizard for the current data object.
     */
    public void showDialog() {
        try {
            dataObj = DataObjectProvider.getProvider().getActiveDataObject();
            ETLCollaborationModel collabModel = DataObjectProvider.getProvider().
                    getActiveDataObject().getModel();
            ETLDefinitionImpl def = collabModel.getETLDefinition();            
            ETLTableSelectionWizard wizard = new ETLTableSelectionWizard(def);
            DataObjectHelper.setDefaultCursor();
            
            if (wizard.show()) {
                List sources = wizard.getSelectedSourceModels();
                List targets = wizard.getSelectedDestinationModels();
                
                // Update definition object.
                DataObjectHelper helper = new DataObjectHelper(dataObj);
                helper.updateTableSelections(dataObj, sources, targets);
                
                if (DEBUG) {
                    Logger.print(Logger.DEBUG, LOG_CATEGORY, "showDialog()", "Selected source tables:\n" + sources);
                    
                    Logger.print(Logger.DEBUG, LOG_CATEGORY, "showDialog()", "Selected target tables:\n" + targets);
                    
                    Logger.print(Logger.DEBUG, LOG_CATEGORY, "showDialog()", "New state of ETL Definition:\n" + def.toXMLString(""));
                }
                dataObj.getETLEditorSupport().synchDocument();
            }
        } catch (Exception e) {
            NotifyDescriptor d = new NotifyDescriptor.Message("Table selection failed.", NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
    }
}


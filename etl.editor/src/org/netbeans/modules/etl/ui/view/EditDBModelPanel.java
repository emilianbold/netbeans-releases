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
package org.netbeans.modules.etl.ui.view;

import java.util.List;

import javax.swing.JSplitPane;

import org.netbeans.modules.etl.ui.ETLDataObject;
import org.netbeans.modules.etl.ui.model.impl.ETLCollaborationModel;


/**
 * @author Sanjeeth Duvuru
 * @version $Revision$
 */
public class EditDBModelPanel extends JSplitPane {

    private DBModelTreeView dbModelTreeView;

    /** Creates a new instance of EditDBModelPanel */
    public EditDBModelPanel(ETLDataObject mObj) {
        ETLCollaborationModel collabModel = mObj.getModel();
        List dbModelList = collabModel.getSourceDatabaseModels();
        List targetdbModelList = collabModel.getTargetDatabaseModels();
        dbModelList.addAll(targetdbModelList);
        dbModelTreeView = new DBModelTreeView(dbModelList, this);
        setOneTouchExpandable(true);
        setDividerLocation(200);
        setLeftComponent(dbModelTreeView);
    }
    
    /** Creates a new instance of EditDBModelPanel */
    public EditDBModelPanel(ETLCollaborationModel collabModel) {
        List dbModelList = collabModel.getSourceDatabaseModels();
        List targetdbModelList = collabModel.getTargetDatabaseModels();
        dbModelList.addAll(targetdbModelList);
        dbModelTreeView = new DBModelTreeView(dbModelList, this);
        setOneTouchExpandable(true);
        setDividerLocation(200);
        setLeftComponent(dbModelTreeView);
    }

    /**
     * Gets currently associated DBModelTreeView.
     * 
     * @return current DBModelTreeView instance.
     */
    public DBModelTreeView getDBModelTreeView() {
        return dbModelTreeView;
    }
}


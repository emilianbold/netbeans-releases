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

package org.netbeans.modules.iep.editor.ps;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

public class SchemaSelectionDialog {

    public static  SelectionArtifacts showDialog(Project project, List<String> existingArtificatNames) {
        List<AXIComponent> artifactNamesList = new ArrayList<AXIComponent>();
        List<AXIComponent> removedArtifactList = new ArrayList<AXIComponent>();
        SchemaSelectionPanel panel = new SchemaSelectionPanel(existingArtificatNames, project);
        
        String title = NbBundle.getMessage(SchemaSelectionDialog.class, "SchemaSelectionDialog.Title");
        String tooltip = NbBundle.getMessage(SchemaSelectionDialog.class, "SchemaSelectionDialog.Tooltip");
        
        panel.setToolTipText(tooltip);
        panel.getAccessibleContext().setAccessibleDescription(tooltip);
        
        DialogDescriptor dd = new DialogDescriptor(panel, "Select schema elements or types", true, null);
        DialogDisplayer dDisplayer = DialogDisplayer.getDefault();
        
        dDisplayer.notify(dd);
        
        //ok is clicked do processing
        artifactNamesList = panel.getSelectedArtifactNames();
        removedArtifactList = panel.getRemovedArtifactNames();
        
        return new SelectionArtifacts(artifactNamesList, removedArtifactList);
        
        //return artifactNamesList;
    }
    
}

class SelectionArtifacts {
	List<AXIComponent> mSelectedList;
	List<AXIComponent> mRemovedList;

	public SelectionArtifacts(List<AXIComponent> selectedList, List<AXIComponent> removedList) {
	    mSelectedList = selectedList;
	    mRemovedList = removedList;
	}
	
	public List<AXIComponent> getSelectedList() {
	    return mSelectedList;
	}
	
	public List<AXIComponent> getRemovedList() {
	    return mRemovedList;
	}
}

/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.uml.project.ui.common;
import org.netbeans.modules.uml.project.UMLProject;
import java.util.ResourceBundle;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import org.openide.util.NbBundle;


/**
 * Support class for uml common ui (wizard and customizer)
 * @author mike frisino
 */
public class CommonUiSupport
{
	
	// TODO - MCF - add the support for the Language Source Level
	// You can copy the source level code from J2SE if needed.
	
	// TODO - MCF - be careful, the Source Level should be unchangeable when in
	// Implementation mode - i.e. it should defer to the Source Level of the
	// target Java project.
	// So really the only time the user should be able to 'select" the Source 
	// level is in the case where the Mode is "Design".
	
	private CommonUiSupport()
	{
	}
	
	/**
	 * Creates {@link ComboBoxModel} of modeling modes
	 * @param modelingModeComboBoxModel the platform's model used for listenning
	 * @param initialValue initial source level value
	 * @return {@link ComboBoxModel} of {@link SpecificationVersion}
	 */
	public static ComboBoxModel createModelingModeComboBoxModel(
		String initialValue)
	{
		
		DefaultComboBoxModel model = new DefaultComboBoxModel(new String[]
		{
			NbBundle.getMessage(CommonUiSupport.class,
				"LBL_ProjectMode_Analysis"), // NOI18N
				NbBundle.getMessage(CommonUiSupport.class,
				"LBL_ProjectMode_Design"), // NOI18N
				NbBundle.getMessage(CommonUiSupport.class,
				"LBL_ProjectMode_Implementation") // NOI18N
		});
		
		if (initialValue == null)
		{
			// need to specify the translated string since all options in the
			//  combobox are translated already
			// model.setSelectedItem(UMLProject.PROJECT_MODE_DEFAULT_STR);
			model.setSelectedItem(NbBundle.getMessage(
				CommonUiSupport.class, "LBL_ProjectMode_Analysis")); // NOI18N
		}
		
		else
			model.setSelectedItem(initialValue);
		
		return model;
	}
	
	public static String getModeProgramName(String localizedName)
	{
		String retVal = UMLProject.PROJECT_MODE_IMPL_STR;
		
		ResourceBundle bundle = NbBundle.getBundle(CommonUiSupport.class);
		
		if (localizedName.equals(
			bundle.getString("LBL_ProjectMode_Analysis"))) // NOI18N
		{
			retVal = UMLProject.PROJECT_MODE_ANALYSIS_STR;
		}
		
		else if (localizedName.equals(
			bundle.getString("LBL_ProjectMode_Design"))) // NOI18N
		{
			retVal = UMLProject.PROJECT_MODE_DESIGN_STR;
		}
		
		return retVal;
	}
	
	/**
	 * Creates {@link ComboBoxModel} of modeling modes
	 * @param modelingModeComboBoxModel the platform's model used for listenning
	 * @param initialValue initial source level value
	 * @return {@link ComboBoxModel} of {@link SpecificationVersion}
	 */
	public static ComboBoxModel createModelingModeComboBoxModel()
	{
		return createModelingModeComboBoxModel(
			// need to specify the translated string since all options in the
			//  combobox are translated already
			// UMLProject.PROJECT_MODE_DEFAULT_STR);
			NbBundle.getMessage(
				CommonUiSupport.class, "LBL_ProjectMode_Analysis")); // NOI18N
		
	}
}

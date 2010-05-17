/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

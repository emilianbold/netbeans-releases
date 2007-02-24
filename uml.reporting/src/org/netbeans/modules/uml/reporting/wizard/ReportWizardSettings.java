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


package org.netbeans.modules.uml.reporting.wizard;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.project.ProjectUtil;
import org.openide.WizardDescriptor;

/**
 *
 * @author Sheryl
 */
public class ReportWizardSettings {
	IElement element;
	WizardDescriptor descriptor;
	Project project;
	File folder;
	
	
	/** Creates a new instance of ReportWizardSettings */
	public ReportWizardSettings() {
	}
	
	public ReportWizardSettings(IElement element)
	{
		this.element = element;
		setProject(ProjectUtil.findElementOwner(element));
	}
	
	public Project getProject()
	{
		return project;
	}
	
	
	public void setProject(Project p)
	{
		this.project = p;
	}
	
	public IElement getElement()
	{
		return this.element;
	}
	
	
	public void setElement(IElement element)
	{
		this.element = element;
	}
	
	public File getReportFolder()
	{
		return folder;
	}
	
	
	public void setReportFolder(File file)
	{
		this.folder = file;
	}
	
	
	/**
	 *
	 *
	 */
	public WizardDescriptor getWizardDescriptor()
	{
		return descriptor;
	}


	/**
	 *
	 *
	 */
	public void setWizardDescriptor(WizardDescriptor value)
	{
		descriptor=value;
	}

}

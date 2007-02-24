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


package org.netbeans.modules.uml.core.metamodel.structure;

import org.dom4j.Document;


public class RequirementArtifact extends Artifact implements IRequirementArtifact
{
	/**
	 *
	 */
	public RequirementArtifact()
	{
		super();	
	}

	/**
	 * The absolute path to the associated fil
	 * @return String
	 */
	public String getRequirementID()
	{
		return getAttributeValue("requirementID");
	}

	/**
	 * @return
	 */
	public String getRequirementProviderID()
	{
		return getAttributeValue("requirementProviderID");
	}

	/**
	 * @return
	 */
	public String getRequirementSourceID() 
	{
		return getAttributeValue("requirementSourceID");
	}

	/**
	 * @param string
	 */
	public void setRequirementID(String newVal)
	{
		setAttributeValue("requirementID",newVal);
	}

	/**
	 * @param string
	 */
	public void setRequirementProviderID(String newVal) 
	{
		setAttributeValue("requirementProviderID",newVal);		
	}

    public void setRequirementModName(String value)
    {
       setAttributeValue("requirementModName",value);
    }
    
    public String getRequirementModName()
    {
       return getAttributeValue("requirementModName");
    }
    
    public void setRequirementProjectName(String value)
    {
       setAttributeValue("requirementProjectName",value);
    }
    
    public String getRequirementProjectName()
    {
       return getAttributeValue("requirementProjectName");
    }
    
	/**
	 * @param newVal
	 */
	public void setRequirementSourceID(String newVal) 
	{
		setAttributeValue("requirementSourceID",newVal);	 	
	}

	public void establishNodePresence(Document doc,org.dom4j.Node parent )
	{
		buildNodePresence("UML:RequirementArtifact",doc,parent);
	}
    
}



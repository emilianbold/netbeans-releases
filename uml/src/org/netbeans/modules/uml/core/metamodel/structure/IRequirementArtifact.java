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


public interface IRequirementArtifact extends IArtifact
{	
    /** Gets the RequirementID. */
	public String getRequirementID();
	
    /** Sets the RequirementID. */
	public void setRequirementID(String reqId);	
	
    /** Gets the RequirementProviderID. */
	public String getRequirementProviderID();
	
    /** Sets the RequirementProviderID. */
	public void setRequirementProviderID(String value);
	
    /** Gets the RequirementSourceID. */
	public String getRequirementSourceID();
	
    /** Sets the RequirementSourceID. */
	public void setRequirementSourceID(String value); 
    
    /** Gets the RequirementProjectName. */
    public String getRequirementProjectName();
    
    /** Sets the RequirementProjectName. */
    public void setRequirementProjectName(String value);
    
    /** Gets the RequirementModName. */
    public String getRequirementModName();
    
    /** Sets the RequirementModName. */
    public void setRequirementModName(String value);    

}



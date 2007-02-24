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

/*
 * IRequirementsManager.java
 *
 * Created on June 24, 2004, 1:52 PM
 */

package org.netbeans.modules.uml.core.requirementsframework;

/**
 *
 * @author  Trey Spiva
 */
public interface IRequirementsManager
{
   /** RequirementSource to the .etd file. */
   public void processSource( IRequirementSource pRequirementSource) throws RequirementsException;

   /** RequirementSource from the .etd file. */
   public IRequirementSource getSource( String RequirementSourceID);


   /** RequirementSource's Satisifer element to the .etrp proxy file. */
   public void processProxy( IRequirement pRequirement,  ISatisfier pSatisfier);
   
   /** The RequirementSource's Satisifer element from the .etrp proxy file. */
   public void deleteProxy( IRequirement pRequirement,  ISatisfier pSatisfier);
   
   /** Retrieves the requiremets providers */
   public IRequirementsProvider[] getAddIns();
   
   /**
	 * The requirements manager knows about the requirements addins.
	 * This routine retrieves a particular addin based on the prog id passed in.
	 * 
	 *
	 * @param sProgID[in]		The prog id of the addin to get
	 * @param pAddIn[out]		The found add in
	 *
	 * @return HRESULT
	 *
	 */
	public IRequirementsProvider getRequirementsProvider(String sProgID) ;
   
}

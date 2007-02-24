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

package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
public interface IFacilityManager
{
	/**
	 * Retrieves the specified facility from the manager.
	*/
	public IFacility retrieveFacility( String name );

	/**
	 * The configuration file that defines the facilities that are managed by the FacilityManager.
	*/
	public String getConfigurationFile();

	/**
	 * The configuration file that defines the facilities that are managed by the FacilityManager.
	*/
	public void setConfigurationFile( String value );

	/**
	 * Retrieves the facilities that are managed by the FacilityManager.
	*/
	public IStrings getFacilityNames();
    
    public String getName();
    
    public void setName(String name);

}

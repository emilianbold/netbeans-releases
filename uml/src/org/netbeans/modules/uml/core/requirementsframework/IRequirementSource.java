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
 * IRequirementSource.java
 *
 * Created on June 24, 2004, 1:23 PM
 */

package org.netbeans.modules.uml.core.requirementsframework;

/**
 *
 * @author  Trey Spiva
 */
public interface IRequirementSource
{
   /** Returns true if this requirement source requires a login */
	public boolean getRequiresLogin();

	/** [propput, helpstring("Sets whether this requirement source requires a login */
	public void setRequiresLogin(boolean newVal);

	/** XML ID of the source in its .etd file */
	public String getID();

	/** XML ID of the source in its .etd file */
   public void setID(String newVal);
   
   /** Name displayed in the Design Center tree for this source */
	public String getDisplayName();
    
    /** Name displayed in the Design Center tree for this source */
	public void setDisplayName(String newVal);
    
	/** Prog ID of the provider sources addin */
	public String getProvider();
    
	/** Prog ID of the provider sources addin */
	public void setProvider(String newVal);
    
	/** Location  of the source providers requirements file */
	public String getLocation();
    
	/** Location  of the source providers requirements file */
	public void setLocation(String newVal);
    
	/** Location of the source providers proxy file */
	public String getProxyFile();
    
	/** Location of the source prov */
   public void setProxyFile(String newVal);
}

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

package org.netbeans.modules.uml.core.requirementsframework;

import org.openide.nodes.Node;

import org.netbeans.modules.uml.core.support.umlutils.ETList;

/*
 * IRequirement.java
 *
 * Created on June 24, 2004, 1:30 PM
 */

/**
 *
 * @author  Trey Spiva
 */
public interface IRequirement extends Node.Cookie
{
  /** A category contains a group of requirements */
  public boolean isCategory();

  /** A category contains a group of requirements" */
  public void setIsCategory(boolean newVal);

  /** The text of the requirement" */
  public String getDescription();

  /** The text of the requirement" */
  public void setDescription(String newVal);
  
  /** property Requirements" */
  public ETList < IRequirement > getRequirements();
  
  /** property Requirements" */
  public void setRequirements( ETList < IRequirement > newVal);
  
  /** property Satisfiers" */
  public ETList < ISatisfier > getSatisfiers();
  
  /** property Satisfiers" */
  public void setSatisfiers( ETList < ISatisfier > newVal);
  
  /** property Name" */
  public String getName();
  
  /** property Name" */
  public void setName( String newVal);
  
  /** property ID" */
  public String getID();
  
  /** property ID" */
  public void setID( String newVal);
  
  /** property Type" */
  public String getType();
  
  /** property Type" */
  public void setType( String newVal);
  
  /** property ModName" */
  public String getModName();
  
  /** property ModName" */
  public void setModName( String newVal);
  
  /** getSubRequirements" */
  public ETList < IRequirement > getSubRequirements( IRequirementSource pRequirementSource ) 
     throws RequirementsException;
  
  /** property ProjectName */
  public String getProjectName();
  
  /** property ProjectName" */
  public void setProjectName( String newVal);
  
  /** property ProviderID" */
  public String getProviderID();
  
  /** property ProviderID" */
  public void setProviderID( String newVal);
  
  /** property SourceID" */
  public String getSourceID();
  
  /** property SourceID" */
  public void setSourceID( String newVal);
  
  /** method AddSatisfier */
  public void addSatisfier( ISatisfier pSatisfier) throws RequirementsException;
  
  /** method RemoveSatisfier */ 
  public void removeSatisfier( ISatisfier pSatisfier) throws RequirementsException;
  
  /** Checks if the node can participate in a drag operation. */
  public boolean isAllowedToDrag();
}

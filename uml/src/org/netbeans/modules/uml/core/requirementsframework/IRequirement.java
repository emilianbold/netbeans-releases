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

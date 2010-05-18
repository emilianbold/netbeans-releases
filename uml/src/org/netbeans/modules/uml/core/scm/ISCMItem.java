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

package org.netbeans.modules.uml.core.scm;

import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;

/**
 * The root item used for handling the disparate types that can be versioned.
 */
public interface ISCMItem
{
	/**
	 * The file location of this item.
	*/
	public String getFileName();

	/**
	 * The file location of this item.
	*/
	public void setFileName( String value );

	/**
	 * The ISCMItemGroup this item belongs to. Can be 0 if this item does not belong to a group.
	*/
	public ISCMItemGroup getGroup();

	/**
	 * The ISCMItemGroup this item belongs to. Can be 0 if this item does not belong to a group.
	*/
	public void setGroup( ISCMItemGroup value );

	/**
	 * Retrieves a unique ID for caching purposes.
	*/
	public String getUniqueID();

	/**
	 * The ISCMTool this item is associated with.
	*/
	public ISCMTool getSCMTool();

	/**
	 * The ISCMTool this item is associated with.
	*/
	public void setSCMTool( ISCMTool value );

	/**
	 * The IProject this item is associated with.
	*/
	public IProject getProject();

	/**
	 * The IProject this item is associated with.
	*/
	public void setProject( IProject value );

	/**
	 * Prepares this item for version control. Should only be called once.
	*/
	public boolean prepare( ISCMIntegrator integrator );

	/**
	 * The name of this item. For GUI display purposes only.
	*/
	public String getName();

	/**
	 * Clears the cached status on this item off the integrator.
	*/
	public void clearStatus( ISCMIntegrator integrator );

	/**
	 * Retrieves the fully qualified name of the item. This will be in the form 'A::B::C'.
	*/
	public String getQualifiedName();

	/**
	 * Retrieves all the files associated with this item.
	*/
	public IStrings getFiles();

   /**
    *
    * Retrieves the files associated with this item.
    *
    * @param curFiles The files that must also be included in the return value.
    * @return The collection. If curFiles is null, one is created otherwise
    *         the files associated with this SCM Item will be added to the
    *         curFiles collection.
    */
   public IStrings getFiles(IStrings curFiles);

	/**
	 * Indicates whether or not this ISCMItem houses more than one file.
	*/
	public boolean getHasMultiFiles();

	/**
	 * The SCM type.
	*/
	public String getType();

	/**
	 * Removes all remnants of version control from this item.
	*/
	public void removeVersionInformation( ISCMOptions pOptions );

	/**
	 * The SCM ID this item belongs to. Most of the times this is 0, as sub class interfaces handle more of the details.
	*/
	public String getSCMID();

	/**
	 * The SCM ID this item belongs to. Most of the times this is 0, as sub class interfaces handle more of the details.
	*/
	public void setSCMID( String value );

	/**
	 * Performs item specific validation.
	*/
	public void validate();

//   /** Determines if the metadata indicates that the model element has been versioned. */
//   public boolean isVersioned();

}

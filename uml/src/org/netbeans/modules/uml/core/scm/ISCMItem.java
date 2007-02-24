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

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


package org.netbeans.modules.uml.ui.support.archivesupport;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IProductArchive
{
	/**
	 * Saves the archive to an XML file.
	*/
	public boolean save( String sFilename );

	/**
	 * Loads the archive from the file
	*/
	public boolean load( String sFilename );

   /**
    * Checking if the archive file has been loaded.
    *
    * @return true if the file has been loaded.
    */
   public boolean isLoaded();

   /**
    * Creates an element
   */
   public IProductArchiveElement createElement( String sID );
      
	/**
	 * Creates an element
	*/
	public IProductArchiveElement createElement( String sID, boolean bTopOfDocument );

	/**
	 * Gets an element based on the name
	*/
	public IProductArchiveElement getElement( String sID );

	/**
	 * Returns all the presentation element types that refer to the IElement with an ID of sElementID
	*/
	public ETList<IProductArchiveElement> getElementsFromModelElement( String sElementID );

	/**
	 * All the toplevel elements
	*/
	public ETList<IProductArchiveElement> getElements();

	/**
	 * Inserts an item into an index table and returns back the key and created element.
	*/
	public ETPairT<IProductArchiveElement, Integer> insertIntoTable( String sTableName, 
                                                  String sTableEntry);

	/**
	 * Inserts sTableEntry into the table sTableName and creates an attribute named sIndexAttributeName on pElementToAddIndexTo with the key value
	*/
	public IProductArchiveAttribute insertIntoTable( String sTableName, 
                                                    String sTableEntry, 
                                                    String sIndexAttributeName, 
                                                    IProductArchiveElement pElementToAddIndexTo);

	/**
	 * Removes an item by index from the table.
	*/
	public boolean removeFromTable( String sTableName, String sTableEntry);

	/**
	 * Based on the table name and index this routine returns the found element.
    * 
    * @param sTableName The name of table that contains the entry.
    * @param nTableEntry The entry to retrieve.
    * @return The element from the table.
	*/
	public IProductArchiveElement getTableEntry( String sTableName, int nTableEntry);

	public IProductArchiveElement getTableEntry( String sTableName, String nTableEntry);

	/**
	 * Gets an item from an index table, returning the key and the found element.
	*/
	public IProductArchiveElement getTableEntry( String sTableName, 
                                                String sTableEntry, 
                                                int pKey);

	/**
	 * Find sAttributeName in pArchiveElement, then look into the table sTableName and return the pFoundElement
	*/
	public ETPairT<IProductArchiveElement,String> getTableEntry( IProductArchiveElement pArchiveElement, 
                                                String sAttributeName, 
                                                String sTableName);

	public ETList<IProductArchiveElement> getAllTableEntries(String tableName);
}

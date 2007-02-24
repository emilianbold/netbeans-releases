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

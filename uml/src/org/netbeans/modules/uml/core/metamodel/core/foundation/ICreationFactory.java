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


package org.netbeans.modules.uml.core.metamodel.core.foundation;

//import com.embarcadero.describe.foundation.IConfigManager;

public interface ICreationFactory
{
	/**
	 * Retrieves the singleton CreationFactory object, running on the Running Object Table.
	*/
	public ICreationFactory getCreationFactory();

	/**
	 * Retrieves the singleton CreationFactory object, running on the Running Object Table.
	*/
	public void setCreationFactory( ICreationFactory value );

	/**
	 * Removes this CreationFactory off the ROT, if it is on it.
	*/
	public long cleanUp();

	/**
	 * Retrieves the revocation number that was assigned when registering this interface on the ROT.
	*/
	public long getRevokeNumber();

	/**
	 * Retrieves the revocation number that was assigned when registering this interface on the ROT.
	*/
	public void setRevokeNumber( long value );

	/**
	 * Retrieve the type specified by the type name, e.g. Class. The type has been fully prepared and initialized.
	*/
	public Object retrieveMetaType( String typeName, Object outer );

	/**
	 * Retrieve the type specified by the type name, e.g. Class. The type is a shell. No initialization has been done.
	*/
	public Object retrieveEmptyMetaType( String typeName, Object outer );

	/**
	 * Retrieve the type specified by the type name, e.g. Class. The type is a shell. No initialization has been done.  The location is the stop under the hive where the type can be found (ie ReverseEngineering will equate to [HKEY_CURRENT_USERSoftwareEmbarcaderoDesc?¿?
	*/
	public Object retrieveEmptyMetaType( String subKey, String typeName, Object outer );

	/**
	 * Sets the ConfigManager on this factory. Ref counts are NOT bumped.
	*/
	public void setConfigManager( IConfigManager value );

}

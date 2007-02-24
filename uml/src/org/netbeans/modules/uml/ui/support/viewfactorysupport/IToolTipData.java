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


package org.netbeans.modules.uml.ui.support.viewfactorysupport;

public interface IToolTipData
{
	/**
	 * Adds a field to the tooltip window
	*/
	public long addField( String sFieldName, String sFieldValue );

	/**
	 * Returns the number of fields in the tooltip window
	*/
	public int getNumFields();

	/**
	 * Returns a specific field by index
	*/
	public String getField( int nIndex, StringBuffer sFieldName );

	/**
	 * The client x for this event (relative to node)
	*/
	public int getClientX();

	/**
	 * The client x for this event (relative to node)
	*/
	public void setClientX( int value );

	/**
	 * The client y for this event (relative to node)
	*/
	public int getClientY();

	/**
	 * The client y for this event (relative to node)
	*/
	public void setClientY( int value );

	/**
	 * Should we display debug information?
	*/
	public boolean getShowDebugInformation();

	/**
	 * Should we display debug information?
	*/
	public void setShowDebugInformation( boolean value );

}

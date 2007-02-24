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

import java.awt.Frame;

/**
 * Used to pass various options to the particular ISCMTools.
 */
public interface ISCMOptions
{
	/**
	 * The description to include with the SCM feature execution.
	*/
	public String getDescription();

	/**
	 * The description to include with the SCM feature execution.
	*/
	public void setDescription( String value );

	/**
	 * A flag used when adding new files to SCM.
	*/
	public boolean getCheckOutImmediately();

	/**
	 * A flag used when adding new files to SCM.
	*/
	public void setCheckOutImmediately( boolean value );

	/**
	 * HWND of the host application.
	*/
	public Frame getHostWindowHandle();

	/**
	 * HWND of the host application.
	*/
	public void setHostWindowHandle( Frame value );

	/**
	 * Determines whether or not the file used in a silent diff differs from what is checked in.
	*/
	public boolean getFileDiffers();

	/**
	 * Determines whether or not the file used in a silent diff differs from what is checked in.
	*/
	public void setFileDiffers( boolean value );

	/**
	 * Used during an open from SCM process. This is the directory specified by the user to retrieve file into.
	*/
	public String getLocalDirectory();

	/**
	 * Used during an open from SCM process. This is the directory specified by the user to retrieve file into.
	*/
	public void setLocalDirectory( String value );

	/**
	 * Specifies whether or not the feature that was just attempted was cancelled by the user or not.
	*/
	public boolean getFeatureCancelled();

	/**
	 * Specifies whether or not the feature that was just attempted was cancelled by the user or not.
	*/
	public void setFeatureCancelled( boolean value );

}

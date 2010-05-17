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

package org.netbeans.modules.uml.core.workspacemanagement;

import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

public interface IWSElementEventsSink
{
	/**
	 * Fired whenever a WSElement is about to be created.
	*/
	public void onWSElementPreCreate( IWSProject wsProject, String location, String Name, String data, IResultCell cell );

	/**
	 * Fired right after a WSElement is created.
	*/
	public void onWSElementCreated( IWSElement element, IResultCell cell );

	/**
	 * Fired whenever a WSElement is about to be saved.
	*/
	public void onWSElementPreSave( IWSElement element, IResultCell cell );

	/**
	 * Fired right after a WSElement is saved.
	*/
	public void onWSElementSaved( IWSElement element, IResultCell cell );

	/**
	 * Fired whenever a WSElement is about to be removed from the WSProject.
	*/
	public void onWSElementPreRemove( IWSElement element, IResultCell cell );

	/**
	 * Fired right after a WSElement is removed.
	*/
	public void onWSElementRemoved( IWSElement element, IResultCell cell );

	/**
	 * Fired whenever the name of the WSElement is about to be changed.
	*/
	public void onWSElementPreNameChanged( IWSElement element, String proposedValue, IResultCell cell );

	/**
	 * Fired right after a WSElement's name has changed.
	*/
	public void onWSElementNameChanged( IWSElement element, IResultCell cell );

	/**
	 * Fired whenever the owner of the WSElement is about to be changed.
	*/
	public void onWSElementPreOwnerChange( IWSElement element, IWSProject newOwner, IResultCell cell );

	/**
	 * Fired right after a WSElement's owner has changed.
	*/
	public void onWSElementOwnerChanged( IWSElement element, IResultCell cell );

	/**
	 * Fired whenever the location of the WSElement is about to be changed.
	*/
	public void onWSElementPreLocationChanged( IWSElement element, String proposedLocation, IResultCell cell );

	/**
	 * Fired right after a WSElement's location has changed.
	*/
	public void onWSElementLocationChanged( IWSElement element, IResultCell cell );

	/**
	 * Fired whenever the data of the WSElement is about to be changed.
	*/
	public void onWSElementPreDataChanged( IWSElement element, String newData, IResultCell cell );

	/**
	 * Fired right after a WSElement's data has changed.
	*/
	public void onWSElementDataChanged( IWSElement element, IResultCell cell );

	/**
	 * Fired whenever the documentation field of the WSElement is about to be changed.
	*/
	public void onWSElementPreDocChanged( IWSElement element, String doc, IResultCell cell );

	/**
	 * Fired right after a WSElement's documentation field has changed.
	*/
	public void onWSElementDocChanged( IWSElement element, IResultCell cell );

	/* (non-Javadoc)
	 */
	public void onWSElementPreAliasChanged(IWSElement element, String proposedValue, IResultCell cell);

	/* (non-Javadoc)
	 */
	public void onWSElementAliasChanged(IWSElement element, IResultCell cell);

}

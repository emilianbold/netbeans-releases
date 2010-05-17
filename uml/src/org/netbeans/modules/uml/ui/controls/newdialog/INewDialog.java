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


package org.netbeans.modules.uml.ui.controls.newdialog;

import java.awt.Frame;

import org.netbeans.modules.uml.ui.support.commondialogs.ISilentDialog;

public interface INewDialog extends ISilentDialog
{
	/**
	 * Display the dialog.
	*/
	public INewDialogTabDetails display( Frame parent );

	/**
	 * Display the dialog with a validate processor.
	*/
	public INewDialogTabDetails display2( INewDialogValidateProcessor pValidateProcessor, Frame parent );

	/**
	 * The default tab for the dialog
	*/
	public long putDefaultTab( /* NewDialogTabKind */ int nDefaultTab );

	/**
	 * Allows you to specify the tabs in the dialog.  If unset the all tabs are shown, otherwise just those specified through AddTab are shown
	*/
	public long addTab( /* NewDialogTabKind */ int nTabKind );

	/**
	 * Has this tab been added.
	*/
	public boolean isTab( /* NewDialogTabKind */ int nTabKind );

	/**
	 * Provides defaults to one of the tabs
	*/
	public long specifyDefaults( INewDialogTabDetails pDetails );

	/**
	 * Returns the result of the dialog.  NULL if the user hit cancel.
	*/
	public INewDialogTabDetails getResult();

}

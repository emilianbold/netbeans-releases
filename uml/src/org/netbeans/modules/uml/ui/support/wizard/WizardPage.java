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



package org.netbeans.modules.uml.ui.support.wizard;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public abstract class WizardPage extends JPanel implements IWizardPage {

	private IWizardSheet m_Parent = null;

	/// The page that should be made active when the user navigates
	/// backwards from this page.
	protected int m_nPgBack;

	/// The page that should be made active when the user navigates
	/// forwards from this page.
	protected int m_nPgNext;

	private String m_Caption = null;
	
	public WizardPage(IWizardSheet parent) {
		super();
		this.m_Parent = parent;
	}

	public WizardPage() {
		super();
	}

	protected void createUI() {
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createRaisedBevelBorder());
	}

	public void onSetActive() {

		m_nPgNext = Math.min(getParentSheet().getActiveIndex() + 1, getParentSheet().getPageCount() - 1);
		m_nPgBack = Math.max(getParentSheet().getActiveIndex() - 1, 0);

	}

	public void onWizardBack() {
		getParentSheet().setActivePage(m_nPgBack);
	}

	public void onWizardNext() {
		getParentSheet().setActivePage(m_nPgNext);
	}

	public boolean onDismiss() {
		return true;
	}

	protected boolean onInitDialog() {
		return true;
	}

	public IWizardSheet getParentSheet() {
		return this.m_Parent;
	}

	public void setParentSheet(IWizardSheet newValue) {
		m_Parent = newValue;
	}

	protected void setBackPage(int nID) {
		m_nPgBack = nID;
	}

	protected void setNextPage(int nID) {
		m_nPgNext = nID;
	}
	
	protected void setCursor(int newValue){
	  m_Parent.setCursor(newValue);
	}

	public String getCaption() {
		return m_Caption;
	}

	public void setCaption(String string) {
		m_Caption = string;
	}

}

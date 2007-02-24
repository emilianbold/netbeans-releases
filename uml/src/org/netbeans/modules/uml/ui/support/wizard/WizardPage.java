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

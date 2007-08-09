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

package org.netbeans.modules.uml.designpattern;

import java.awt.Frame;
import java.awt.GraphicsConfiguration;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.netbeans.modules.uml.ui.support.wizard.WizardSheet;


public class Wizard extends WizardSheet {

	private static final String PG_INTRO = "IntroPage";
	private static final String PG_PATTERNSEL = "PatternSelectionPage";
	private static final String PG_TARGET = "TargetScopePage";
	private static final String PG_ROLES = "RolesPage";
	private static final String PG_OPTIONS = "OptionsPage";
	private static final String PG_SUMMARY = "SummaryPage";

	WizardIntro m_IntroPage = null;
    WizardPatternSelection	m_PatternSelectionPage = null;
    WizardTarget				m_TargetPage = null;
    WizardRoles					m_RolesPage = null;
    WizardOptions		    	m_OptionsPage = null;
    WizardSummary				m_SummaryPage = null;

   private IDesignPatternDetails m_PatternDetails = null;
   private IDesignPatternManager m_PatternManager = null;

   public boolean m_RefreshPages;

   private int m_selectPage = 0;

	public Wizard(int nIDCaption, Frame pParentWnd, int iSelectPage, Icon hbmWatermark, GraphicsConfiguration hpalWatermark, Icon hbmHeader) {
		this("", pParentWnd, iSelectPage, hbmWatermark, hpalWatermark, hbmHeader);
	}

	public Wizard(String pszCaption, Frame pParentWnd, int iSelectPage, Icon hbmWatermark, GraphicsConfiguration hpalWatermark, Icon hbmHeader) {
		super(pszCaption, pParentWnd, iSelectPage, hbmWatermark, hpalWatermark, hbmHeader);
		init(hbmWatermark, hpalWatermark, hbmHeader);
		m_selectPage = iSelectPage;
	}

	public Wizard(Frame frame, String title, boolean modal) {
		super(frame, title, modal);
		center(frame);
	}

	public Wizard() {
		super();
	}

        public void init(Icon hbmWatermark, GraphicsConfiguration hpalWatermark, Icon hbmHeader)
        {
            getAccessibleContext().setAccessibleDescription(DefaultDesignPatternResource.getString("ACSD_WIZARD"));

            super.init(null, null, null);
            
//            if (hbmWatermark != null && hbmHeader != null)
//            {
//                super.init(hbmWatermark, hpalWatermark, hbmHeader);
//            }
//            
//            else
//            {
//                // just load up the default images
//                super.init(new ImageIcon(Wizard.class.getResource("wiz01.gif")), 
//                    null, new ImageIcon(Wizard.class.getResource("wiz02.gif")));
//            }
            
            m_IntroPage = new WizardIntro(this);
            m_PatternSelectionPage = new WizardPatternSelection(this);
            m_TargetPage = new WizardTarget(this);
            m_RolesPage = new WizardRoles(this);
            m_OptionsPage = new WizardOptions(this);
            m_SummaryPage = new WizardSummary(this);
            
            this.addPage(m_IntroPage, PG_INTRO);
            this.addPage(m_PatternSelectionPage, PG_PATTERNSEL);
            this.addPage(m_TargetPage, PG_TARGET);
            this.addPage(m_RolesPage, PG_ROLES);
            this.addPage(m_OptionsPage, PG_OPTIONS);
            this.addPage(m_SummaryPage, PG_SUMMARY);
            
            m_RefreshPages = false;
            
            this.setActivePage(0);
        }

	public IDesignPatternDetails getDetails() {
		return m_PatternDetails;
	}

	public void setDetails(IDesignPatternDetails details) {
		m_PatternDetails = details;
	}

   public IDesignPatternManager getManager()
   {
      return m_PatternManager;
   }

   public void setManager(IDesignPatternManager manager)
   {
      m_PatternManager = manager;
   }

}

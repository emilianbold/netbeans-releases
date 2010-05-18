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

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


package org.netbeans.modules.uml.ui.support.messaging;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;


import org.netbeans.modules.uml.common.Util;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.ui.swing.commondialogs.JCenterDialog;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

public class ZoomDialog extends JCenterDialog implements IZoomDialog
{

 private int lastButtonPressed;

 private long m_CurrentZoom = 100;
 private boolean m_FitToWindow = false;

 // DWORD m_UserZoomLevels[5];

 private JPanel panel1 = new JPanel();
 private JPanel spacePanel = new JPanel();
 private JButton jbOK = new JButton();
 private JButton jbCancel = new JButton();
 private JComboBox cbZoom = new JComboBox();
 private JCheckBox cbFitToWindow = new JCheckBox();
 private JLabel lblPercent = new JLabel();
 private JPanel topPanel = new JPanel();
 private JPanel midPanel = new JPanel();
 private JPanel bottomPanel = new JPanel();
	public ZoomDialog(Frame frame)
	{
		this(frame, MessagingResources.getString("IDS_ZOOM"), false);
	}

 public ZoomDialog(Frame frame, String title, boolean modal)
 {
    super(frame, title, modal);
    try
    {
       createUI();
       pack();

    }
    catch (Exception ex)
    {
       ex.printStackTrace();
    }
 }

 public ZoomDialog()
 {
    this(null, MessagingResources.getString("IDS_ZOOM"), false);
 }

 private void createUI() throws Exception
 {
    GridBagConstraints gridBagConstraints=new GridBagConstraints();
	panel1.setLayout(new GridBagLayout());

    jbOK.setToolTipText("");
	Mnemonics.setLocalizedText(jbOK, NbBundle.getMessage(ZoomDialog.class, "IDS_OK"));
    getRootPane().setDefaultButton(jbOK);
	Mnemonics.setLocalizedText(jbCancel, NbBundle.getMessage(ZoomDialog.class, "IDS_CANCEL"));

    cbFitToWindow.setText(MessagingResources.determineText(MessagingResources.getString("IDS_FITTOWINDOW")));
    MessagingResources.setMnemonic(cbFitToWindow, MessagingResources.getString("IDS_FITTOWINDOW"));
    lblPercent.setOpaque(true);
    lblPercent.setToolTipText("");
    lblPercent.setText(MessagingResources.determineText(MessagingResources.getString("IDS_PERCENT")));
    lblPercent.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 2, 0)));
    MessagingResources.setMnemonic(lblPercent, MessagingResources.getString("IDS_PERCENT"));
    MessagingResources.setFocusAccelerator(cbZoom, MessagingResources.getString("IDS_PERCENT"));

    getContentPane().add(panel1);

    topPanel.setLayout(new GridBagLayout());
    gridBagConstraints.gridx=0;
    gridBagConstraints.gridy=0;
    gridBagConstraints.weightx=0;
    gridBagConstraints.fill=GridBagConstraints.BOTH;
    gridBagConstraints.insets=new Insets(7,7,5,2);
    topPanel.add(lblPercent,gridBagConstraints);

    gridBagConstraints.gridx=1;
    gridBagConstraints.gridy=0;
    gridBagConstraints.weightx=5;
    gridBagConstraints.insets=new Insets(6,2,5,5);
    gridBagConstraints.ipadx=-20;
    gridBagConstraints.fill=GridBagConstraints.BOTH;
    topPanel.add(cbZoom,gridBagConstraints);

    gridBagConstraints.gridx=0;
    gridBagConstraints.gridy=0;
    panel1.add(topPanel,gridBagConstraints);

    gridBagConstraints.gridx=0;
    gridBagConstraints.gridy=1;
    gridBagConstraints.weightx=1.0;
    gridBagConstraints.weighty=1.0;
    gridBagConstraints.ipadx=5;
    gridBagConstraints.fill=GridBagConstraints.BOTH;
    gridBagConstraints.insets=new Insets(0,5,5,2);
    panel1.add(cbFitToWindow,gridBagConstraints);

    bottomPanel.setLayout(new GridBagLayout());
    gridBagConstraints.gridx=0;
    gridBagConstraints.gridy=0;
    gridBagConstraints.ipadx=12;
    gridBagConstraints.fill=GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets=new Insets(2,5,5,2);
    bottomPanel.add(jbOK,gridBagConstraints);

    gridBagConstraints.gridx=1;
    gridBagConstraints.gridy=0;
    gridBagConstraints.fill=GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets=new Insets(2,2,5,4);
    bottomPanel.add(jbCancel,gridBagConstraints);

    gridBagConstraints.gridx=0;
    gridBagConstraints.gridy=2;
    panel1.add(bottomPanel,gridBagConstraints);

    Dimension buttonSize=getMaxButtonWidth();
    jbOK.setMaximumSize(buttonSize);
    jbOK.setPreferredSize(buttonSize);
    jbCancel.setMaximumSize(buttonSize);
    jbCancel.setPreferredSize(buttonSize);

    this.addActionListeners();
		this.onInitDialog();
 }

 private Dimension getMaxButtonWidth()
	{
		Dimension ret = null;
		Dimension d = jbCancel.getPreferredSize();
		double max  = d.width;

		d = jbOK.getPreferredSize();
		if(d.width < max){
			 max = d.width;
			 ret = d;
		}
		return ret;

	}

 private void addActionListeners()
 {

    jbOK.addActionListener(new java.awt.event.ActionListener()
    {
       public void actionPerformed(ActionEvent e)
       {
          jbOK_actionPerformed(e);
       }
    });

    jbCancel.addActionListener(new java.awt.event.ActionListener()
    {
       public void actionPerformed(ActionEvent e)
       {
          jbCancel_actionPerformed(e);
       }
    });

    cbZoom.addActionListener(new java.awt.event.ActionListener()
    {
       public void actionPerformed(ActionEvent e)
       {
          cbZoom_actionPerformed(e);
       }
    });

    cbFitToWindow.addActionListener(new java.awt.event.ActionListener()
    {
       public void actionPerformed(ActionEvent e)
       {
          cbFitToWindow_actionPerformed(e);
       }
    });
 }

 void cbZoom_actionPerformed(ActionEvent e)
 {
    try
    {
       this.m_CurrentZoom = Util.toLong(cbZoom.getSelectedItem());
    }
    catch (RuntimeException e1)
    {
    }

 }

 void cbFitToWindow_actionPerformed(ActionEvent e)
 {
		if (cbFitToWindow.isSelected()){
			this.m_FitToWindow = true;
			this.cbZoom.setEnabled(false);
		}else{
			this.m_FitToWindow = false;
			this.cbZoom.setEnabled(true);
		}
 }

 void jbOK_actionPerformed(ActionEvent e)
 {
    lastButtonPressed = IZoomDialog.FINISH;
    try
    {
       this.m_CurrentZoom = Util.toLong(cbZoom.getSelectedItem());
    }
    catch (RuntimeException e1)
    {
    }
		this.m_FitToWindow = this.cbFitToWindow.isSelected();
		this.dispose();

 }

 void jbCancel_actionPerformed(ActionEvent e)
 {
    lastButtonPressed = IZoomDialog.CANCEL;
    this.dispose();

 }

 protected boolean onInitDialog()
 {

    cbFitToWindow.setSelected(m_FitToWindow);

    cbZoom.addItem(new String("400"));
    cbZoom.addItem(new String("200"));
    cbZoom.addItem(new String("100"));
    cbZoom.addItem(new String("50"));
    cbZoom.addItem(new String("25"));

    cbZoom.setSelectedItem("100");
    cbZoom.setEditable(true);

    return true; // return TRUE unless you set the focus to a control
 }

 public double getCurrentZoom()
 {

    try
    {
       return (double)m_CurrentZoom/100 ;
    }
    catch (RuntimeException e)
    {
       return 0;
    }

 }

 public void setCurrentZoom(double nCurrentZoom)
 {

    try
    {
       cbZoom.setSelectedItem(Util.toString(new Long(Math.round(nCurrentZoom * 100))));
    }
    catch (RuntimeException e1)
    {
    }

 }

 public boolean getFitToWindow()
 {
    return this.m_FitToWindow;
 }

 public ETPairT < Double, Boolean > display(double pCurrentZoom)
 {

		double currentZoom = pCurrentZoom;
		boolean fitToWindow = false;

		setCurrentZoom(pCurrentZoom);

    this.setModal(true);
    super.show();

    if (lastButtonPressed == IZoomDialog.FINISH)
    {
			currentZoom = getCurrentZoom();
			fitToWindow = getFitToWindow();

    }
    else if (lastButtonPressed == IZoomDialog.CANCEL)
    {
			return null;
    }

    return new ETPairT < Double, Boolean > (new Double(currentZoom), new Boolean(fitToWindow));

 }

}

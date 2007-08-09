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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.netbeans.modules.uml.ui.swing.commondialogs.JCenterDialog;

public abstract class WizardSheet extends JCenterDialog implements IWizardSheet
{
    
    private JLabel spacer1 = new JLabel();
    private JButton jbNext = new JButton();
    private JPanel pnlButtonBar = new JPanel();
    private JButton jbFinish = new JButton();
    private JButton jbCancel = new JButton();
    private JLabel spacer2 = new JLabel();
    private JLabel spacer3 = new JLabel();
    private JButton jbBack = new JButton();
    private JLabel spacer4 = new JLabel();
    private JLabel spacer5 = new JLabel();
    private JLabel spacer6 = new JLabel();
    
    private CardPanel wizardPages = null;
    private int lastButtonPressed;
    
    /// watermark image to use on EndPages (pages at beginning or end of the wizard)
    private Icon bmpWatermark = null;
    
    /// header image used on all InteriorPages
    private Icon bmpHeader = null;
    
    private String m_Title = null;
    
    /// name of this Wizard's help file, get it from Kathy
    /// do not include the extension, always assumed to be ".hlp"
    protected String m_sHelpFilePath;
    
    public WizardSheet(int nIDCaption, Frame pParentWnd, int iSelectPage, Icon hbmWatermark, GraphicsConfiguration hpalWatermark, Icon hbmHeader)
    {
        this("", pParentWnd, iSelectPage, hbmWatermark, hpalWatermark, hbmHeader); //$NON-NLS-1$
    }
    
    public WizardSheet(String pszCaption, Frame pParentWnd, int iSelectPage, Icon hbmWatermark, GraphicsConfiguration hpalWatermark, Icon hbmHeader)
    {
        this(pParentWnd, pszCaption, true);
        this.bmpWatermark = hbmWatermark;
        this.bmpHeader = hbmHeader;
    }
    
    public WizardSheet(Frame frame, String title, boolean modal)
    {
        super(frame, title, modal);
        m_Title = title;
        
        try
        {
            createUI();
            pack();
            
            this.setResizable(false);
            
            // center on screen
            Dimension SS = this.getToolkit().getScreenSize();
            Dimension CS = this.getSize();
            this.setLocation((SS.width - CS.width) / 2, (SS.height - CS.height) / 2);
            
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    public WizardSheet()
    {
        this(null, "", true); //$NON-NLS-1$
    }
    
    public boolean is256ColorsSupported()
    {
        return true;
    }
    
    /**
     * Enables us to adjust the panel size to handle larger fonts
     * CBeckham
     */
    private Dimension setPanelSize()
    {
        
        int fontsize;
        java.awt.Font f =
            javax.swing.UIManager.getFont("controlFont"); //NOI18N
        if (f != null)
        {
            fontsize = f.getSize();
        }
        else
        {
            fontsize = 12;
        }
        int width  = 500;
        int height = 350;
        int multiplyer = 2;
        
        if (fontsize > 17) multiplyer = 3;
        width  = width  + Math.round(width*(multiplyer*fontsize/100f));
        height = height + Math.round(height*(multiplyer*fontsize/100f));
        
        return new java.awt.Dimension(width, height);
    }
    
    private void createUI()
    {
        wizardPages = new CardPanel(this);
        
        wizardPages.setPreferredSize(setPanelSize());
        this.getContentPane().setLayout(new BorderLayout());
        
        pnlButtonBar.setBorder(BorderFactory.createRaisedBevelBorder());
        pnlButtonBar.setLayout(new GridBagLayout());
        
        this.jbBack.setMnemonic(KeyEvent.VK_B);
        this.jbNext.setMnemonic(KeyEvent.VK_N);
        this.jbFinish.setMnemonic(KeyEvent.VK_F);
        
        Insets insets = new java.awt.Insets(10, 5, 10, 5);
        if(canAddNavigationButtons() == true)
        {
            jbBack.setText(getBackButtonCaption());
            jbNext.setText(getNextButtonCaption());
            pnlButtonBar.add(spacer1, new GridBagConstraints(
                1, 0, // gridx, gridy
                1, 1, // gridwidth, gridheight
                1.0, 0.0, // weightx, weighty
                GridBagConstraints.CENTER, // anchor
                GridBagConstraints.BOTH, // fill
                insets, // insets
                0, // ipadx
                0));
            pnlButtonBar.add(jbBack, new GridBagConstraints(
                2, 0, // gridx, gridy
                1, 1, // gridwidth, gridheight
                0.0, 0.0, // weightx, weighty
                GridBagConstraints.CENTER, // anchor
                GridBagConstraints.BOTH, // fill
                insets, // insets
                0, // ipadx
                0));
            pnlButtonBar.add(jbNext, new GridBagConstraints(
                3, 0, // gridx, gridy
                1, 1, // gridwidth, gridheight
                0.0, 0.0, // weightx, weighty
                GridBagConstraints.CENTER, // anchor
                GridBagConstraints.BOTH, // fill
                insets, // insets
                0, // ipadx
                0));
        }
        else
        {
            pnlButtonBar.add(spacer4, new GridBagConstraints(
                1, 0, // gridx, gridy
                1, 1, // gridwidth, gridheight
                1.0, 0.0, // weightx, weighty
                GridBagConstraints.CENTER, // anchor
                GridBagConstraints.BOTH, // fill
                insets, // insets
                0, // ipadx
                0));
            pnlButtonBar.add(spacer5, new GridBagConstraints(
                2, 0, // gridx, gridy
                1, 1, // gridwidth, gridheight
                0.0, 0.0, // weightx, weighty
                GridBagConstraints.CENTER, // anchor
                GridBagConstraints.BOTH, // fill
                insets, // insets
                0, // ipadx
                0));
            pnlButtonBar.add(spacer6, new GridBagConstraints(
                3, 0, // gridx, gridy
                1, 1, // gridwidth, gridheight
                0.0, 0.0, // weightx, weighty
                GridBagConstraints.CENTER, // anchor
                GridBagConstraints.BOTH, // fill
                insets, // insets
                0, // ipadx
                0));
        }
        
        jbFinish.setText(getCommitButtonCaption());
        getRootPane().setDefaultButton(jbFinish);
        
        jbCancel.setText(getCancelButtonCaption());
        Dimension buttonSize = getMaxButtonWidth();
        jbBack.setMaximumSize(buttonSize);
        jbBack.setPreferredSize(buttonSize);
        jbBack.getAccessibleContext().setAccessibleDescription(WizardResouces.getString("WizardSheet.BACK_BTN_Description"));
        jbCancel.setMaximumSize(buttonSize);
        jbCancel.setPreferredSize(buttonSize);
        jbCancel.getAccessibleContext().setAccessibleDescription(WizardResouces.getString("WizardSheet.CANCEL_BTN_Description"));
        jbFinish.setMaximumSize(buttonSize);
        jbFinish.setPreferredSize(buttonSize);
        jbNext.setMaximumSize(buttonSize);
        jbNext.setPreferredSize(buttonSize);
        jbNext.getAccessibleContext().setAccessibleDescription(WizardResouces.getString("WizardSheet.NEXT_BTN_Description"));
        
        pnlButtonBar.add(jbFinish, new GridBagConstraints(
            4, 0, // gridx, gridy
            1, 1, // gridwidth, gridheight
            0.0, 0.0, // weightx, weighty
            GridBagConstraints.CENTER, // anchor
            GridBagConstraints.BOTH, // fill
            insets, // insets
            0, // ipadx
            0));
        
                /*pnlButtonBar.add(spacer2, new GridBagConstraints(
                5, 0, // gridx, gridy
                1, 1, // gridwidth, gridheight
                0.0, 0.0, // weightx, weighty
                GridBagConstraints.CENTER, // anchor
                GridBagConstraints.BOTH, // fill
                insets, // insets
                0, // ipadx
                0));
                 */
        pnlButtonBar.add(jbCancel, new GridBagConstraints(
            6, 0, // gridx, gridy
            1, 1, // gridwidth, gridheight
            0.0, 0.0, // weightx, weighty
            GridBagConstraints.CENTER, // anchor
            GridBagConstraints.BOTH, // fill
            insets, // insets
            0, // ipadx
            0));
        pnlButtonBar.add(spacer3, new GridBagConstraints(
            7, 0, // gridx, gridy
            1, 1, // gridwidth, gridheight
            0.0, 0.0, // weightx, weighty
            GridBagConstraints.CENTER, // anchor
            GridBagConstraints.BOTH, // fill
            insets, // insets
            0, // ipadx
            0));
        
        this.getContentPane().add(wizardPages, BorderLayout.CENTER);
        this.getContentPane().add(pnlButtonBar, BorderLayout.SOUTH);
        this.addActionListeners();
    }
    
    /**
     * @return
     */
    protected String getCancelButtonCaption()
    {
        return WizardResouces.getString("WizardSheet.CANCEL_BTN"); //$NON-NLS-1$
    }
    
    /**
     * @return
     */
    protected String getCommitButtonCaption()
    {
        return WizardResouces.getString("WizardSheet.COMMIT_BTN"); //$NON-NLS-1$
    }
    
    /**
     * @return
     */
    protected String getNextButtonCaption()
    {
        return WizardResouces.getString("WizardSheet.NEXT_BTN"); //$NON-NLS-1$
    }
    
    /**
     * @return
     */
    protected String getBackButtonCaption()
    {
        return WizardResouces.getString("WizardSheet.BACK_BTN"); //$NON-NLS-1$
    }
    
    /**
     * @return
     */
    protected boolean canAddNavigationButtons()
    {
        return true;
    }
    
    private void addActionListeners()
    {
        jbBack.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jbBack_actionPerformed(e);
            }
        });
        
        jbNext.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jbNext_actionPerformed(e);
            }
        });
        
        jbFinish.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jbFinish_actionPerformed(e);
            }
        });
        
        jbCancel.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jbCancel_actionPerformed(e);
            }
        });
        
    }
    
    // override this method to add your pages plus any other setup
    // @warning be sure to call Init() from your derived class!
    public void init(Icon hbmWatermark, GraphicsConfiguration hpalWatermark, Icon hbmHeader)
    {
        
        this.bmpWatermark = hbmWatermark;
        this.bmpHeader = hbmHeader;
        
    }
    
    protected void showPage(String pageName)
    {
        wizardPages.showCard(pageName);
    }
    
    protected void addPage(IWizardPage page, String pageName)
    {
        if (page instanceof JPanel)
        {
            page.setParentSheet(this);
            wizardPages.add((JPanel) page, pageName);
        }
    }
    
    void jbBack_actionPerformed(ActionEvent e)
    {
        onWizardBack();
    }
    
    void jbNext_actionPerformed(ActionEvent e)
    {
        onWizardNext();
    }
    
    void jbFinish_actionPerformed(ActionEvent e)
    {
        onWizardFinish();
    }
    
    void jbCancel_actionPerformed(ActionEvent e)
    {
        onWizardCancel();
    }
    
    public Icon getBmpHeader()
    {
        return bmpHeader;
    }
    
    public Icon getBmpWatermark()
    {
        return bmpWatermark;
    }
    
    public void setTitle(String title)
    {
        this.m_Title = title;
        if (title != null)
        {
            super.setTitle(title);
        }
    }
    
    public int getActiveIndex()
    {
        return wizardPages.getCurrentCardIndex();
    }
    
    public void setActivePage(int pIndex)
    {
        wizardPages.showCard(pIndex);
    }
    
    public int getPageCount()
    {
        return wizardPages.getCardCount();
    }
    
    // notification from cardPanel that a page event has occured
    public void onPageChange()
    {
        
        updateButtons();
        
        Component currCard = wizardPages.getCurrentCard();
        
        if (currCard instanceof IWizardPage)
        {
            
            IWizardPage currPage = ((IWizardPage) currCard);
            String pageCaption = currPage.getCaption();
            
            if (pageCaption != null && pageCaption.length() > 0)
            {
                super.setTitle(pageCaption);
            }
            else
            {
                super.setTitle(m_Title);
            }
            currPage.onSetActive();
        }
        
    }
    
    private void updateButtons()
    {
        int nIndex = getActiveIndex();
        if (nIndex == 0)
        {
            jbBack.setEnabled(false);
            jbNext.setEnabled(true);
            jbFinish.setEnabled(false);
            
        }
        else if (nIndex > 0 && nIndex < getPageCount() - 1)
        {
            jbBack.setEnabled(true);
            jbNext.setEnabled(true);
            jbFinish.setEnabled(false);
            
        }
        else if (nIndex == getPageCount() - 1)
        {
            jbBack.setEnabled(true);
            jbNext.setEnabled(false);
            jbFinish.setEnabled(true);
        }
    }
    
    public void onWizardBack()
    {
        
        lastButtonPressed = IWizardSheet.PSWIZB_BACK;
        Component currCard = wizardPages.getCurrentCard();
        
        if (currCard instanceof IWizardPage)
        {
            IWizardPage currPage = ((IWizardPage) currCard);
            currPage.onWizardBack();
        }
    }
    
    public void onWizardNext()
    {
        
        lastButtonPressed = IWizardSheet.PSWIZB_NEXT;
        Component currCard = wizardPages.getCurrentCard();
        if (currCard instanceof IWizardPage)
        {
            IWizardPage currPage = ((IWizardPage) currCard);
            currPage.onWizardNext();
        }
    }
    
    public void onWizardCancel()
    {
        lastButtonPressed = IWizardSheet.PSWIZB_CANCEL;
        this.dispose();
    }
    
    public void onWizardFinish()
    {
        lastButtonPressed = IWizardSheet.PSWIZB_FINISH;
        Component currCard = wizardPages.getCurrentCard();
        
        boolean bDispose = true;
        
        if (currCard instanceof IWizardPage)
        {
            IWizardPage currPage = ((IWizardPage) currCard);
            bDispose = currPage.onDismiss();
        }
        
        if( bDispose )
        {
            dispose();
        }
    }
    
    public int doModal()
    {
        
        super.setVisible(true);
        return this.lastButtonPressed;
    }
    
    public void setCursor(int newValue)
    {
        switch (newValue)
        {
        case Cursor.DEFAULT_CURSOR :
            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            break;
            
        case Cursor.WAIT_CURSOR :
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            break;
            
        }
    }
    
    public void setButtonEnabled(int button, boolean enabled)
    {
        switch (button)
        {
        case IWizardSheet.PSWIZB_BACK :
            this.jbBack.setEnabled(enabled);
            break;
        case IWizardSheet.PSWIZB_NEXT :
            this.jbNext.setEnabled(enabled);
            break;
        case IWizardSheet.PSWIZB_CANCEL :
            this.jbCancel.setEnabled(enabled);
            break;
        case IWizardSheet.PSWIZB_FINISH :
            this.jbFinish.setEnabled(enabled);
            break;
        }
        
    }
    private Dimension getMaxButtonWidth()
    {
        Dimension ret = null;
        Dimension d = jbBack.getPreferredSize();
        double max  = d.width;
        
        d = jbCancel.getPreferredSize();
        if(d.width > max)
        {
            max = d.width;
            ret = d;
        }
        d = jbFinish.getPreferredSize();
        if(d.width > max)
        {
            max = d.width;
            ret = d;
        }
        d = jbNext.getPreferredSize();
        if(d.width > max)
        {
            max = d.width;
            ret = d;
        }
        
        return ret;
        
    }
}

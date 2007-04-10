/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 *
 *     "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */
package org.netbeans.installer.wizard.containers;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.MalformedURLException;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.helper.swing.NbiButton;
import org.netbeans.installer.utils.helper.swing.NbiFrame;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.helper.swing.NbiPanel;
import org.netbeans.installer.utils.helper.swing.NbiSeparator;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;

/**
 *
 * @author Kirill Sorokin
 */
public class SwingFrameContainer extends NbiFrame implements SwingContainer {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private SwingUi currentUi;
    private WizardFrameContentPane contentPane;
    
    private int frameWidth;
    private int frameHeight;
    private File frameIcon;
    private String frameTitlePrefix;
    private String frameTitlePattern;
    
    public SwingFrameContainer() {
        super();
        
        if (System.getProperty(WIZARD_FRAME_WIDTH_PROPERTY) != null) {
            try {
                frameWidth = Integer.parseInt(
                        System.getProperty(WIZARD_FRAME_WIDTH_PROPERTY));
            } catch (NumberFormatException e) {
                ErrorManager.notifyWarning(ResourceUtils.getString(
                        SwingFrameContainer.class,
                        RESOURCE_FAILED_TO_PARSE_SYSTEM_PROPERTY,
                        WIZARD_FRAME_WIDTH_PROPERTY,
                        System.getProperty(WIZARD_FRAME_WIDTH_PROPERTY)), e);
            }
        } else {
            frameWidth = DEFAULT_WIZARD_FRAME_WIDTH;
        }
        
        if (System.getProperty(WIZARD_FRAME_HEIGHT_PROPERTY) != null) {
            try {
                frameHeight = Integer.parseInt(
                        System.getProperty(WIZARD_FRAME_HEIGHT_PROPERTY));
            } catch (NumberFormatException e) {
                ErrorManager.notifyWarning(ResourceUtils.getString(
                        SwingFrameContainer.class,
                        RESOURCE_FAILED_TO_PARSE_SYSTEM_PROPERTY,
                        WIZARD_FRAME_HEIGHT_PROPERTY,
                        System.getProperty(WIZARD_FRAME_HEIGHT_PROPERTY)), e);
            }
        } else {
            frameHeight = DEFAULT_WIZARD_FRAME_HEIGHT;
        }
        
        final String frameIconUri;
        if (System.getProperty(WIZARD_FRAME_ICON_URI_PROPERTY) != null) {
            frameIconUri = System.getProperty(WIZARD_FRAME_ICON_URI_PROPERTY);
        } else {
            frameIconUri = DEFAULT_WIZARD_FRAME_ICON_URI;
        }
        try {
            frameIcon = FileProxy.getInstance().getFile(frameIconUri);
        } catch (DownloadException e) {
            ErrorManager.notifyWarning(ResourceUtils.getString(
                    SwingFrameContainer.class,
                    RESOURCE_FAILED_TO_DOWNLOAD_WIZARD_ICON,
                    frameIconUri), e);
        }
        
        if (System.getProperty(WIZARD_FRAME_TITLE_PREFIX_PROPERTY) != null) {
            frameTitlePrefix =
                    System.getProperty(WIZARD_FRAME_TITLE_PREFIX_PROPERTY);
        } else {
            frameTitlePrefix = DEFAULT_WIZARD_FRAME_TITLE_PREFIX;
        }
        
        if (System.getProperty(WIZARD_FRAME_TITLE_PATTERN_PROPERTY) != null) {
            frameTitlePattern =
                    System.getProperty(WIZARD_FRAME_TITLE_PATTERN_PROPERTY);
        } else {
            frameTitlePattern = DEFAULT_WIZARD_FRAME_TITLE_PATTERN;
        }
        
        initComponents();
    }
    
    @Override
    public void setVisible(final boolean visible) {
        super.setVisible(visible);
        
        if (visible == false) {
            dispose();
        }
    }
    
    public void updateWizardUi(final WizardUi wizardUi) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    updateWizardUi(wizardUi);
                }
            });
            return;
        }
        
        // save the ui reference
        currentUi = wizardUi.getSwingUi(this);
        
        // update the frame title
        if (currentUi.hasTitle()) {
            setTitle(StringUtils.format(
                    frameTitlePattern,
                    frameTitlePrefix,
                    currentUi.getTitle()));
        } else {
            setTitle(frameTitlePrefix);
        }
        
        // display the panel
        contentPane.updatePanel(currentUi);
        
        // handle the default buttons - Enter
        getRootPane().setDefaultButton(currentUi.getDefaultEnterButton());
        
        // handle the default buttons - Escape
        getRootPane().getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true),
                CANCEL_ACTION_NAME);
        getRootPane().getActionMap().put(CANCEL_ACTION_NAME, new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                final NbiButton button = currentUi.getDefaultEscapeButton();
                if (button != null) {
                    if (button.equals(getHelpButton())) {
                        currentUi.evaluateHelpButtonClick();
                    }
                    if (button.equals(getBackButton())) {
                        currentUi.evaluateBackButtonClick();
                    }
                    if (button.equals(getNextButton())) {
                        currentUi.evaluateNextButtonClick();
                    }
                    if (button.equals(getCancelButton())) {
                        currentUi.evaluateCancelButtonClick();
                    }
                }
            }
        });
        
        // set the default focus for the current page
        if (currentUi.getDefaultFocusOwner() != null) {
            currentUi.getDefaultFocusOwner().requestFocusInWindow();
        }
        
        // a11y
        getAccessibleContext().setAccessibleName(currentUi.getTitle());
        getAccessibleContext().setAccessibleDescription(currentUi.getDescription());
    }
    
    public NbiButton getHelpButton() {
        return contentPane.getHelpButton();
    }
    
    public NbiButton getBackButton() {
        return contentPane.getBackButton();
    }
    
    public NbiButton getNextButton() {
        return contentPane.getNextButton();
    }
    
    public NbiButton getCancelButton() {
        return contentPane.getCancelButton();
    }
    
    private void initComponents() {
        setDefaultCloseOperation(NbiFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                if (contentPane.getCancelButton().isEnabled()) {
                    if (currentUi != null) {
                        currentUi.evaluateCancelButtonClick();
                    }
                }
            }
        });
        
        setSize(frameWidth, frameHeight);
        
        try {
            setIconImage(new ImageIcon(frameIcon.toURI().toURL()).getImage());
        } catch (MalformedURLException e) {
            ErrorManager.notifyWarning(ResourceUtils.getString(
                    SwingFrameContainer.class,
                    RESOURCE_FAILED_TO_SET_WIZARD_ICON), e);
        }
        
        contentPane = new WizardFrameContentPane();
        setContentPane(contentPane);
        
        contentPane.getHelpButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                currentUi.evaluateHelpButtonClick();
            }
        });
        
        contentPane.getBackButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                currentUi.evaluateBackButtonClick();
            }
        });
        
        contentPane.getNextButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                currentUi.evaluateNextButtonClick();
            }
        });
        
        contentPane.getCancelButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                currentUi.evaluateCancelButtonClick();
            }
        });
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class WizardFrameContentPane extends NbiFrameContentPane {
        private NbiLabel titleLabel;
        private NbiTextPane descriptionPane;
        private NbiPanel titlePanel;
        
        private NbiSeparator topSeparator;
        
        private NbiSeparator bottomSeparator;
        
        private NbiButton helpButton;
        private NbiButton backButton;
        private NbiButton nextButton;
        private NbiButton cancelButton;
        private NbiPanel buttonsPanel;
        
        private NbiPanel currentPanel;
        
        public WizardFrameContentPane() {
            initComponents();
        }
        
        public void updatePanel(final SwingUi panel) {
            if (currentPanel != null) {
                remove(currentPanel);
            }
            currentPanel = panel;
            
            if (panel.hasTitle()) {
                titleLabel.setText(panel.getTitle());
                descriptionPane.setText(panel.getDescription());
                
                titlePanel.setVisible(true);
                topSeparator.setVisible(true);
                
                currentPanel.setOpaque(false);
            } else {
                titlePanel.setVisible(false);
                topSeparator.setVisible(false);
                
                currentPanel.setOpaque(true);
                currentPanel.setBackground(Color.WHITE);
            }
            
            add(currentPanel, new GridBagConstraints(
                    0, 2,                             // x, y
                    1, 1,                             // width, height
                    1.0, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            
            validate();
            repaint();
        }
        
        public NbiButton getHelpButton() {
            return helpButton;
        }
        
        public NbiButton getBackButton() {
            return backButton;
        }
        
        public NbiButton getNextButton() {
            return nextButton;
        }
        
        public NbiButton getCancelButton() {
            return cancelButton;
        }
        
        // private //////////////////////////////////////////////////////////////////
        private void initComponents() {
            // titleLabel ///////////////////////////////////////////////////////////
            titleLabel = new NbiLabel();
            titleLabel.setFocusable(true);
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
            
            // descriptionPane //////////////////////////////////////////////////////
            descriptionPane = new NbiTextPane();
            
            // titlePanel ///////////////////////////////////////////////////////////
            titlePanel = new NbiPanel();
            titlePanel.setBackground(Color.WHITE);
            titlePanel.setLayout(new GridBagLayout());
            titlePanel.setOpaque(true);
            
            titlePanel.add(titleLabel, new GridBagConstraints(
                    0, 0,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            titlePanel.add(descriptionPane, new GridBagConstraints(
                    0, 1,                             // x, y
                    1, 1,                             // width, height
                    1.0, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.PAGE_START,    // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(6, 22, 11, 11),        // padding
                    0, 0));                           // padx, pady - ???
            
            // topSeparator /////////////////////////////////////////////////////////
            topSeparator = new NbiSeparator();
            
            // currentPanel /////////////////////////////////////////////////////////
            currentPanel = new NbiPanel();
            
            // bottomSeparator //////////////////////////////////////////////////////
            bottomSeparator = new NbiSeparator();
            
            // helpButton ///////////////////////////////////////////////////////////
            helpButton = new NbiButton();
            
            // backButton ///////////////////////////////////////////////////////////
            backButton = new NbiButton();
            
            // nextButton ///////////////////////////////////////////////////////////
            nextButton = new NbiButton();
            
            // cancelButton /////////////////////////////////////////////////////////
            cancelButton = new NbiButton();
            
            // buttonsPanel /////////////////////////////////////////////////////////
            buttonsPanel = new NbiPanel();
            buttonsPanel.setLayout(new GridBagLayout());
            
            buttonsPanel.add(helpButton, new GridBagConstraints(
                    0, 0,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.NONE,          // fill
                    new Insets(11, 11, 11, 11),       // padding
                    0, 0));                           // padx, pady - ???
            buttonsPanel.add(backButton, new GridBagConstraints(
                    1, 0,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_END,      // anchor
                    GridBagConstraints.NONE,          // fill
                    new Insets(11, 0, 11, 6),         // padding
                    0, 0));                           // padx, pady - ???
            buttonsPanel.add(nextButton, new GridBagConstraints(
                    2, 0,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_END,      // anchor
                    GridBagConstraints.NONE,          // fill
                    new Insets(11, 0, 11, 11),        // padding
                    0, 0));                           // padx, pady - ???
            buttonsPanel.add(cancelButton, new GridBagConstraints(
                    3, 0,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_END,      // anchor
                    GridBagConstraints.NONE,          // fill
                    new Insets(11, 0, 11, 11),        // padding
                    0, 0));                           // padx, pady - ???
            
            // this /////////////////////////////////////////////////////////////////
            setLayout(new GridBagLayout());
            
            add(titlePanel, new GridBagConstraints(
                    0, 0,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            add(topSeparator, new GridBagConstraints(
                    0, 1,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            add(currentPanel, new GridBagConstraints(
                    0, 2,                             // x, y
                    1, 1,                             // width, height
                    1.0, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            add(bottomSeparator, new GridBagConstraints(
                    0, 3,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            add(buttonsPanel, new GridBagConstraints(
                    0, 4,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            
            // debugging plug ///////////////////////////////////////////////////////
            //KeyboardFocusManager.getCurrentKeyboardFocusManager().
            //        addPropertyChangeListener(new PropertyChangeListener() {
            //    public void propertyChange(PropertyChangeEvent event) {
            //        if (event.getPropertyName().equals("focusOwner")) {
            //            if (event.getNewValue() != null) {
            //                System.out.println(event.getNewValue());
            //            }
            //        }
            //    }
            //});
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String WIZARD_FRAME_WIDTH_PROPERTY =
            "nbi.wizard.ui.swing.frame.width"; // NOI18N
    public static final String WIZARD_FRAME_HEIGHT_PROPERTY =
            "nbi.wizard.ui.swing.frame.height"; // NOI18N
    public static final String WIZARD_FRAME_ICON_URI_PROPERTY =
            "nbi.wizard.ui.swing.frame.icon"; // NOI18N
    public static final String WIZARD_FRAME_TITLE_PREFIX_PROPERTY =
            "nbi.wizard.ui.swing.frame.title.prefix"; // NOI18N
    public static final String WIZARD_FRAME_TITLE_PATTERN_PROPERTY =
            "nbi.wizard.ui.swing.frame.title.pattern"; // NOI18N
    
    public static final int DEFAULT_WIZARD_FRAME_WIDTH =
            NbiFrame.DEFAULT_FRAME_WIDTH;
    public static final int DEFAULT_WIZARD_FRAME_HEIGHT =
            NbiFrame.DEFAULT_FRAME_HEIGHT;
    public static final String DEFAULT_WIZARD_FRAME_ICON_URI =
            NbiFrame.DEFAULT_FRAME_ICON_URI;
    public static final String DEFAULT_WIZARD_FRAME_TITLE_PREFIX =
            ResourceUtils.getString(SwingFrameContainer.class,
            "SFC.frame.title.prefix"); // NOI18N
    public static final String DEFAULT_WIZARD_FRAME_TITLE_PATTERN =
            ResourceUtils.getString(SwingFrameContainer.class,
            "SFC.frame.title.pattern"); // NOI18N
    
    public static final String RESOURCE_FAILED_TO_PARSE_SYSTEM_PROPERTY =
            "SFC.error.failed.to.parse.property"; // NOI18N
    public static final String RESOURCE_FAILED_TO_DOWNLOAD_WIZARD_ICON =
            "SFC.error.failed.to.download.icon"; // NOI18N
    public static final String RESOURCE_FAILED_TO_SET_WIZARD_ICON =
            "SFC.error.failed.to.set.wizard.icon"; // NOI18N
    
    public static final String CANCEL_ACTION_NAME =
            "evaluate.cancel"; // NOI18N
}

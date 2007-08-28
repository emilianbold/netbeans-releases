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

import java.awt.BorderLayout;
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
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.UiUtils;
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
 * This class is a conscrete implementation of the {@link SwingContainer} interface.
 * In this case the container is an {@link NbiFrame}.
 *
 * @author Kirill Sorokin
 * @sicne 1.0
 */
public class SwingFrameContainer extends NbiFrame implements SwingContainer {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * Instance of {@link SwingUi} that is currently shown by the container.
     */
    private SwingUi currentUi;
    
    /**
     * Content pane used by the container.
     */
    private WizardFrameContentPane contentPane;
    
    /**
     * Prefix of the container frame title.
     */
    private String frameTitlePrefix;
    
    /**
     * Pattern which should be used to combine the container frame's title prefix
     * and the title of the current UI.
     */
    private String frameTitlePattern;
    
    /**
     * Creates a new instance of {@link SwingFrameContainer}. The constructor calls
     * the initialization routine of the parent class and searches the system
     * properties for settings which may be releavant to this type of container.
     * Additionally it initializes and lays out the core swing components of the
     * container.
     */
    public SwingFrameContainer() {
        super();
        
        frameWidth = UiUtils.getDimension(System.getProperties(),
                WIZARD_FRAME_WIDTH_PROPERTY,  
                DEFAULT_WIZARD_FRAME_WIDTH);
        frameMinimumWidth = UiUtils.getDimension(System.getProperties(),
                WIZARD_FRAME_MINIMUM_WIDTH_PROPERTY,
                DEFAULT_WIZARD_FRAME_MINIMUM_WIDTH);
        frameMaximumWidth = UiUtils.getDimension(System.getProperties(),
                WIZARD_FRAME_MAXIMUM_WIDTH_PROPERTY,
                DEFAULT_WIZARD_FRAME_MAXIMUM_WIDTH);
        
        frameHeight = UiUtils.getDimension(System.getProperties(),
                WIZARD_FRAME_HEIGHT_PROPERTY,  
                DEFAULT_WIZARD_FRAME_HEIGHT);
        frameMinimumHeight = UiUtils.getDimension(System.getProperties(),
                WIZARD_FRAME_MINIMUM_HEIGHT_PROPERTY,
                DEFAULT_WIZARD_FRAME_MINIMUM_HEIGHT);
        frameMaximumHeight = UiUtils.getDimension(System.getProperties(),
                WIZARD_FRAME_MAXIMUM_HEIGHT_PROPERTY,
                DEFAULT_WIZARD_FRAME_MAXIMUM_HEIGHT);
        
        boolean customIconLoaded = false;
        if (System.getProperty(WIZARD_FRAME_ICON_URI_PROPERTY) != null) {
            final String frameIconUri =
                    System.getProperty(WIZARD_FRAME_ICON_URI_PROPERTY);
            
            try {
                frameIcon = FileProxy.getInstance().getFile(frameIconUri);
                customIconLoaded = true;
            } catch (DownloadException e) {
                ErrorManager.notifyWarning(ResourceUtils.getString(
                        SwingFrameContainer.class,
                        RESOURCE_FAILED_TO_DOWNLOAD_WIZARD_ICON,
                        frameIconUri), e);
            }
        }
        
        if (!customIconLoaded) {
            final String frameIconUri = DEFAULT_WIZARD_FRAME_ICON_URI;
            
            try {
                frameIcon = FileProxy.getInstance().getFile(frameIconUri);
                customIconLoaded = true;
            } catch (DownloadException e) {
                ErrorManager.notifyWarning(ResourceUtils.getString(
                        SwingFrameContainer.class,
                        RESOURCE_FAILED_TO_DOWNLOAD_WIZARD_ICON,
                        frameIconUri), e);
            }
        }
        
        frameTitlePrefix = DEFAULT_WIZARD_FRAME_TITLE_PREFIX;
        if (System.getProperty(WIZARD_FRAME_TITLE_PREFIX_PROPERTY) != null) {
            frameTitlePrefix =
                    System.getProperty(WIZARD_FRAME_TITLE_PREFIX_PROPERTY);
        }
        
        frameTitlePattern = DEFAULT_WIZARD_FRAME_TITLE_PATTERN;
        if (System.getProperty(WIZARD_FRAME_TITLE_PATTERN_PROPERTY) != null) {
            frameTitlePattern =
                    System.getProperty(WIZARD_FRAME_TITLE_PATTERN_PROPERTY);
        }
        
        initComponents();
    }
    
    /**
     * This method overrides {@link NbiFrame#setVisible()} and at the same time
     * implements {@link WizardContainer#setVisible()}. It is responsible for
     * showing and hiding the wizard container frame.
     *
     * @param visible Whether to show the frame - <code>true</code>, or to hide
     *      it - <code>false</code>.
     */
    @Override
    public void setVisible(final boolean visible) {
        super.setVisible(visible);
        
        if (visible == false) {
            dispose();
        }
    }
    
    /**
     * {@inheritDoc}
     */
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
        if (currentUi.getTitle() != null) {
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
        getRootPane().getInputMap(JRootPane.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
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
    
    /**
     * {@inheritDoc}
     */
    public NbiButton getHelpButton() {
        return contentPane.getHelpButton();
    }
    
    /**
     * {@inheritDoc}
     */
    public NbiButton getBackButton() {
        return contentPane.getBackButton();
    }
    
    /**
     * {@inheritDoc}
     */
    public NbiButton getNextButton() {
        return contentPane.getNextButton();
    }
    
    /**
     * {@inheritDoc}
     */
    public NbiButton getCancelButton() {
        return contentPane.getCancelButton();
    }
    
    // protected ////////////////////////////////////////////////////////////////////
    /**
     * Initializes and lays out the Swing components for the container frame. This
     * method also sets some frame properties which will be required at runtime,
     * such as size, position, etc.
     */
    @Override
    protected void initComponents() {
        super.initComponents();
        
        try {
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
        } catch (SecurityException e) {
            // we might fail here with a custom security manager (e.g. the netbeans
            // one); in this case just log the exception and "let it be" (c)
            ErrorManager.notifyDebug(
                    ResourceUtils.getString(
                    SwingFrameContainer.class,
                    RESOURCE_ERROR_SET_CLOSE_OPERATION),
                    e);
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
    /**
     * This class is an extension of {@link NbiFrameContentPane} which adds some
     * functionality specific to the wizard container's needs. It is used as the
     * content pane for the wizard frame.
     *
     * @author Kirill Sorokin
     * @since 1.0
     */
    public static class WizardFrameContentPane extends NbiFrameContentPane {
        /**
         * {@link NbiLabel} which would be used to display the
         * {@link WizardComponent}'s title.
         */
        private NbiLabel titleLabel;
        
        /**
         * {@link NbiTextPane} which would be used to display the
         * {@link WizardComponent}'s description.
         */
        private NbiTextPane descriptionPane;
        
        /**
         * Container for the title and description components.
         */
        private NbiPanel titlePanel;
        
        /**
         * Separator between the wizard page header (title and description) and the
         * main wizard page contents.
         */
        private NbiSeparator topSeparator;
        
        /**
         * Separator between the wizard page footer (standard wizard container
         * buttons) and the main wizard page contents.
         */
        private NbiSeparator bottomSeparator;
        
        /**
         * The standard <code>Help</code> button.
         */
        private NbiButton helpButton;
        
        /**
         * The standard <code>Back</code> button.
         */
        private NbiButton backButton;
        
        /**
         * The standard <code>Next</code> button.
         */
        private NbiButton nextButton;
        
        /**
         * The standard <code>Cancel</code> button.
         */
        private NbiButton cancelButton;
        
        /**
         * Spacer panel which used to correctly position the standard buttons.
         */
        private NbiPanel spacerPanel;
        
        /**
         * Container for the standard buttons swing components.
         */
        private NbiPanel buttonsPanel;
        
        /**
         * Reference to the {@link SwingUi} being currently displayed.
         */
        private NbiPanel currentPanel;
        
        /**
         * Creates a new instance of {@link WizardFrameContentPane}. The default
         * constructor simply initializes and lays out the swing components
         * required by the content pane.
         */
        public WizardFrameContentPane() {
            initComponents();
        }
        
        /**
         *
         * @param panel
         */
        public void updatePanel(final SwingUi panel) {
            if (currentPanel != null) {
                remove(currentPanel);
            }
            currentPanel = panel;
            
            if (panel.getTitle() != null) {
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
            
            add(currentPanel, BorderLayout.CENTER);
            
            validate();
            repaint();
        }
        
        /**
         * Returns the Swing implementation of the standard <code>Help</code>
         * button. This method is called by the {@link SwingFrameContainer} when it
         * needs to get the handle of the button.
         *
         * @return <code>Help</code> button instance.
         * @see SwingFrameContainer#getHelpButton.
         */
        public NbiButton getHelpButton() {
            return helpButton;
        }
        
        /**
         * Returns the Swing implementation of the standard <code>Back</code>
         * button. This method is called by the {@link SwingFrameContainer} when it
         * needs to get the handle of the button.
         *
         * @return <code>Back</code> button instance.
         * @see SwingFrameContainer#getBackButton.
         */
        public NbiButton getBackButton() {
            return backButton;
        }
        
        /**
         * Returns the Swing implementation of the standard <code>Next</code>
         * button. This method is called by the {@link SwingFrameContainer} when it
         * needs to get the handle of the button.
         *
         * @return <code>Next</code> button instance.
         * @see SwingFrameContainer#getNextButton.
         */
        public NbiButton getNextButton() {
            return nextButton;
        }
        
        /**
         * Returns the Swing implementation of the standard <code>Cancel</code>
         * button. This method is called by the {@link SwingFrameContainer} when it
         * needs to get the handle of the button.
         *
         * @return <code>Cancel</code> button instance.
         * @see SwingFrameContainer#getCancelButton.
         */
        public NbiButton getCancelButton() {
            return cancelButton;
        }
        
        // private //////////////////////////////////////////////////////////////////
        /**
         * Initializes and lays out the swing components required by the content
         * pane.
         */
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
            
            String imageUri = System.getProperty(WIZARD_FRAME_HEAD_IMAGE_URI_PROPERTY);
            if(imageUri!=null) {
                titlePanel.setBackgroundImage(imageUri, titlePanel.ANCHOR_BOTTON_RIGHT);
                
            }
            
            // topSeparator /////////////////////////////////////////////////////////
            topSeparator = new NbiSeparator();
            
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
            titlePanel.add(topSeparator, new GridBagConstraints(
                    0, 2,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            
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
            
            // spacerPanel //////////////////////////////////////////////////////////
            spacerPanel = new NbiPanel();
            
            // buttonsPanel /////////////////////////////////////////////////////////
            buttonsPanel = new NbiPanel();
            
            buttonsPanel.add(bottomSeparator, new GridBagConstraints(
                    0, 0,                             // x, y
                    5, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            buttonsPanel.add(helpButton, new GridBagConstraints(
                    0, 1,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.NONE,          // fill
                    new Insets(11, 11, 11, 11),       // padding
                    0, 0));                           // padx, pady - ???
            buttonsPanel.add(spacerPanel, new GridBagConstraints(
                    1, 1,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            buttonsPanel.add(backButton, new GridBagConstraints(
                    2, 1,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.NONE,          // fill
                    new Insets(11, 0, 11, 6),         // padding
                    0, 0));                           // padx, pady - ???
            buttonsPanel.add(nextButton, new GridBagConstraints(
                    3, 1,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.NONE,          // fill
                    new Insets(11, 0, 11, 11),        // padding
                    0, 0));                           // padx, pady - ???
            buttonsPanel.add(cancelButton, new GridBagConstraints(
                    4, 1,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.NONE,          // fill
                    new Insets(11, 0, 11, 11),        // padding
                    0, 0));                           // padx, pady - ???
            
            // currentPanel /////////////////////////////////////////////////////////
            currentPanel = new NbiPanel();
            
            // this /////////////////////////////////////////////////////////////////
            setLayout(new BorderLayout());
            
            add(titlePanel, BorderLayout.PAGE_START);
            add(currentPanel, BorderLayout.CENTER);
            add(buttonsPanel, BorderLayout.PAGE_END);
            
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
    /**
     * Name of the system property which is expected to contain the desired value
     * for the initial width of the wizard frame.
     */
    public static final String WIZARD_FRAME_WIDTH_PROPERTY =
            "nbi.wizard.ui.swing.frame.width"; // NOI18N
    
    /**
     * Name of the system property which is expected to contain the desired value
     * for the minimum width of the wizard frame.
     */
    public static final String WIZARD_FRAME_MINIMUM_WIDTH_PROPERTY =
            "nbi.wizard.ui.swing.frame.minimum.width"; // NOI18N
    
    /**
     * Name of the system property which is expected to contain the desired value
     * for the maximum width of the wizard frame.
     */
    public static final String WIZARD_FRAME_MAXIMUM_WIDTH_PROPERTY =
            "nbi.wizard.ui.swing.frame.maximum.width"; // NOI18N
    
    /**
     * Name of the system property which is expected to contain the desired value
     * for the initial height of the wizard frame.
     */
    public static final String WIZARD_FRAME_HEIGHT_PROPERTY =
            "nbi.wizard.ui.swing.frame.height"; // NOI18N
    
    /**
     * Name of the system property which is expected to contain the desired value
     * for the minimum height of the wizard frame.
     */
    public static final String WIZARD_FRAME_MINIMUM_HEIGHT_PROPERTY =
            "nbi.wizard.ui.swing.frame.minimum.height"; // NOI18N
    
    /**
     * Name of the system property which is expected to contain the desired value
     * for the maximum height of the wizard frame.
     */
    public static final String WIZARD_FRAME_MAXIMUM_HEIGHT_PROPERTY =
            "nbi.wizard.ui.swing.frame.maximum.height"; // NOI18N
    
    /**
     * Name of the system property which is expected to contain the desired value
     * for the URI of the wizard frame icon.
     */
    public static final String WIZARD_FRAME_ICON_URI_PROPERTY =
            "nbi.wizard.ui.swing.frame.icon"; // NOI18N
    
    /**
     * Name of the system property which is expected to contain the desired value
     * for the standard prefix of the wizard frame's title.
     */
    public static final String WIZARD_FRAME_TITLE_PREFIX_PROPERTY =
            "nbi.wizard.ui.swing.frame.title.prefix"; // NOI18N
    
    /**
     * Name of the system property which is expected to contain the desired value
     * for the URI of the wizard frame head background image.
     */
    public static final String WIZARD_FRAME_HEAD_IMAGE_URI_PROPERTY =
            "nbi.wizard.ui.swing.frame.head.image"; // NOI18N
    
    
    /**
     * Name of the system property which is expected to contain the desired value
     * for the pattern for merging the standard title prefix with the component's
     * title.
     */
    public static final String WIZARD_FRAME_TITLE_PATTERN_PROPERTY =
            "nbi.wizard.ui.swing.frame.title.pattern"; // NOI18N
    
    /**
     * Default value for the wizard frame's initial width.
     */
    public static final int DEFAULT_WIZARD_FRAME_WIDTH =
            NbiFrame.DEFAULT_FRAME_WIDTH;
    
    /**
     * Default value for the wizard frame's minimum width.
     */
    public static final int DEFAULT_WIZARD_FRAME_MINIMUM_WIDTH =
            NbiFrame.DEFAULT_FRAME_MINIMUM_WIDTH;
    
    /**
     * Default value for the wizard frame's maximum width.
     */
    public static final int DEFAULT_WIZARD_FRAME_MAXIMUM_WIDTH =
            NbiFrame.DEFAULT_FRAME_MAXIMUM_WIDTH;
    
    /**
     * Default value for the wizard frame's initial height.
     */
    public static final int DEFAULT_WIZARD_FRAME_HEIGHT =
           NbiFrame.DEFAULT_FRAME_HEIGHT;
    
    /**
     * Default value for the wizard frame's minimum height.
     */
    public static final int DEFAULT_WIZARD_FRAME_MINIMUM_HEIGHT =
            NbiFrame.DEFAULT_FRAME_MINIMUM_WIDTH;
    
    /**
     * Default value for the wizard frame's maximum height.
     */
    public static final int DEFAULT_WIZARD_FRAME_MAXIMUM_HEIGHT =
            NbiFrame.DEFAULT_FRAME_MAXIMUM_HEIGHT;
    
    /**
     * Default value for the wizard frame's icon's URI.
     */
    public static final String DEFAULT_WIZARD_FRAME_ICON_URI =
            NbiFrame.DEFAULT_FRAME_ICON_URI;
    
    /**
     * Default value for the wizard frame's standard title prefix.
     */
    public static final String DEFAULT_WIZARD_FRAME_TITLE_PREFIX =
            ResourceUtils.getString(SwingFrameContainer.class,
            "SFC.frame.title.prefix"); // NOI18N
    
    /**
     * Default value for the pattern for merging the standard title prefix with the
     * component's title.
     */
    public static final String DEFAULT_WIZARD_FRAME_TITLE_PATTERN =
            ResourceUtils.getString(SwingFrameContainer.class,
            "SFC.frame.title.pattern"); // NOI18N
    
    // private //////////////////////////////////////////////////////////////////////
    /**
     * Name of a resource bundle entry.
     */
    private static final String RESOURCE_FAILED_TO_PARSE_SYSTEM_PROPERTY =
            "SFC.error.failed.to.parse.property"; // NOI18N
    
    /**
     * Name of a resource bundle entry.
     */
    private static final String RESOURCE_FAILED_TO_DOWNLOAD_WIZARD_ICON =
            "SFC.error.failed.to.download.icon"; // NOI18N
    private static final String RESOURCE_ERROR_SET_CLOSE_OPERATION =
            "SFC.error.close.operation"; //NOI18N
    /**
     * Name of the {@link AbstractAction} which is invoked when the user presses the
     * <code>Escape</code> button.
     */
    private static final String CANCEL_ACTION_NAME =
            "evaluate.cancel"; // NOI18N
}

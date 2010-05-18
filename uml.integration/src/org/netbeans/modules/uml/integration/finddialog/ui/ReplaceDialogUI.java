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


package org.netbeans.modules.uml.integration.finddialog.ui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.Preferences;

import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.commondialogs.IErrorDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;
import org.netbeans.modules.uml.integration.finddialog.DefaultFindDialogResource;
import org.netbeans.modules.uml.integration.finddialog.FindController;
import org.netbeans.modules.uml.integration.finddialog.FindResults;
import org.netbeans.modules.uml.integration.finddialog.FindUtilities;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.swing.commondialogs.JCenterDialog;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingErrorDialog;
import org.netbeans.modules.uml.util.DummyCorePreference;
import org.openide.awt.Mnemonics;
import org.openide.util.NbPreferences;

public class ReplaceDialogUI extends JCenterDialog
{
    private JScrollPane jScrollResultTable;
    
    /** Creates new form finddialog */
    public ReplaceDialogUI(Frame parent, boolean modal, FindController controller)
    {
        super(parent, modal);
        
        setController(controller);
        initComponents();
        initTextFieldListeners();
        selectionListener = new SelectionListener();
        searchResultsTable.getSelectionModel().addListSelectionListener(selectionListener);
        initDialog();
        center(parent);
        // pack();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents()
    {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        
        mainPanel = new JPanel();
        searchPanelsPanel = new JPanel();
        findWhatFieldsPanel = new JPanel();
        replaceFielsPanel = new JPanel();
        findWhatLabel = new JLabel();
        textLabel2 = new JLabel();
        findWhatCombo = new JComboBox();
        searchOptionsPanel = new JPanel();
        matchCaseCheck = new JCheckBox();
        xpathCheck = new JCheckBox();
        wholeWordCheck = new JCheckBox();
        searchAliasRadio = new JRadioButton();
        projectFieldsPanel = new JPanel();
        projectListPanel = new JPanel();
        searchElementsRadio = new JRadioButton();
        searchDescrRadio = new JRadioButton();
        projectList = new JList();
        searchInFieldsPanel = new JPanel();
        projectsLabel = new JLabel();
        resultsFieldsPanel = new JPanel();
        searchResultsLabel = new JLabel();
        FindTableModel model = new FindTableModel(this, null);
        searchResultsTable = new JReplaceTable(model, this);
        navigateFieldsPanel = new JPanel();
        navigateCheck = new JCheckBox();
        statusLabel = new JLabel();
        findButton = new JButton();
        closeButton = new JButton();
        replaceWithCombo = new JComboBox();
        replaceButton = new JButton();
        replaceAllButton = new JButton();
        
        initButtons();
        configureFont();
        
        setTitle(DefaultFindDialogResource.getString("IDS_REPLACETITLE"));
        
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent evt)
            {
                closeDialog(evt);
            }
        });
        
        GridBagConstraints mainGBC = new GridBagConstraints();
        mainGBC.anchor = GridBagConstraints.FIRST_LINE_START;
        mainGBC.fill = GridBagConstraints.BOTH;

        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
        
        findWhatFieldsPanel.setLayout(new GridBagLayout());

        // text label
        findWhatLabel.setText(DefaultFindDialogResource.determineText(
            DefaultFindDialogResource.getString("IDS_FINDWHAT")));
        
        findWhatLabel.setLabelFor(findWhatCombo);
        
        DefaultFindDialogResource.setMnemonic(findWhatLabel, 
            DefaultFindDialogResource.getString("IDS_FINDWHAT"));
        
        DefaultFindDialogResource.setFocusAccelerator(findWhatCombo, 
            DefaultFindDialogResource.getString("IDS_FINDWHAT"));
        
        findWhatLabel.setName("findLabel");
        gridBagConstraints.anchor=GridBagConstraints.LINE_START;
        gridBagConstraints.fill=GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=0;
        gridBagConstraints.weightx=0.0;
        gridBagConstraints.weighty=0.1;
        gridBagConstraints.insets=new Insets(0,5,0,0);
        findWhatFieldsPanel.add(findWhatLabel,gridBagConstraints);
        
        // combo box
        gridBagConstraints.gridx=1;
        gridBagConstraints.gridy=0;
        gridBagConstraints.weightx=1.0;
        gridBagConstraints.insets=new Insets(0,5,0,0);
        findWhatFieldsPanel.add(findWhatCombo, gridBagConstraints);

        findWhatCombo.setEditable(true);
        findWhatCombo.setMaximumRowCount(10);
        
        gridBagConstraints.gridx=2;
        gridBagConstraints.gridy=0;
        gridBagConstraints.weightx=0.0;
        gridBagConstraints.insets=new Insets(0,5,0,5);
        findWhatFieldsPanel.add(findButton, gridBagConstraints);

        mainGBC.gridx=0;
        mainGBC.gridy=0;
        mainGBC.weightx = 2.0;
        mainGBC.weighty = 10.0;
        mainPanel.add(findWhatFieldsPanel, mainGBC);
        // mainPanel.add(findWhatFieldsPanel);
        

        searchPanelsPanel.setLayout(new GridBagLayout());

        ///////////////////////////////////////////////////////////////////////
        // Match Case/XPath Expr/Match Whole Word check boxes
        
        searchOptionsPanel.setLayout(new GridBagLayout());
        
        TitledBorder bord = new TitledBorder(
            DefaultFindDialogResource.getString("IDS_SEARCHOPTIONS"));
        
        searchOptionsPanel.setBorder(bord);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;

        
        // Match Case checkbox
        // default to checked to try and make the query faster
        matchCaseCheck.setSelected(isMatchCase());
        findController.setCaseSensitive(matchCaseCheck.isSelected());
        
        matchCaseCheck.setText(DefaultFindDialogResource.determineText(
            DefaultFindDialogResource.getString("IDS_MATCHCASE")));
        
        DefaultFindDialogResource.setMnemonic(matchCaseCheck, 
            DefaultFindDialogResource.getString("IDS_MATCHCASE"));
        
        matchCaseCheck.getAccessibleContext().setAccessibleDescription(
            DefaultFindDialogResource.getString("ACSD_MatchCaseCheck"));
        
        matchCaseCheck.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                onMatchCaseCheck(evt);
            }
        });

        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=0;
        gridBagConstraints.weightx=1.0;
        gridBagConstraints.weighty = 1.0;
        searchOptionsPanel.add(matchCaseCheck, gridBagConstraints);
     
        
        // Match Whole Word checkbox
        wholeWordCheck.setText(DefaultFindDialogResource.determineText(
            DefaultFindDialogResource.getString("IDS_MATCHWHOLE")));
        
        DefaultFindDialogResource.setMnemonic(wholeWordCheck, 
            DefaultFindDialogResource.getString("IDS_MATCHWHOLE"));
        
        wholeWordCheck.getAccessibleContext().setAccessibleDescription(
            DefaultFindDialogResource.getString("ACSD_WholeWordCheck"));
        
        wholeWordCheck.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                onWholeWordCheck(evt);
            }
        });
        
        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=1;
        searchOptionsPanel.add(wholeWordCheck, gridBagConstraints);

        
        // This is an XPath Expression checkbox
        xpathCheck.setText(DefaultFindDialogResource.determineText(
            DefaultFindDialogResource.getString("IDS_XPATHEXPRESSION")));
        
        DefaultFindDialogResource.setMnemonic(xpathCheck,
            DefaultFindDialogResource.getString("IDS_XPATHEXPRESSION"));

        xpathCheck.getAccessibleContext().setAccessibleDescription(
            DefaultFindDialogResource.getString("ACSD_XpathCheck"));

        xpathCheck.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                onXPathCheck(evt);
            }
        });

        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=2;
        searchOptionsPanel.add(xpathCheck, gridBagConstraints);
        
        
        // add "search options" panel to search panels panel
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets=new Insets(5,0,5,5);
        searchPanelsPanel.add(searchOptionsPanel, gridBagConstraints);
        
        // Match Case/XPath Expr/Match Whole Word check boxes
        ///////////////////////////////////////////////////////////////////////

        
        ///////////////////////////////////////////////////////////////////////
        // Elements/Descriptions/Alias radio buttons

        searchInFieldsPanel.setLayout(new GridBagLayout());
        
        bord = new TitledBorder(
            DefaultFindDialogResource.getString("IDS_SEARCHIN"));
        
        searchInFieldsPanel.setBorder(bord);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        
        // Elements radio button
        // default the dialog to have the element radio button checked
        searchElementsRadio.setSelected(true);
        
        searchElementsRadio.setText(DefaultFindDialogResource.determineText(
            DefaultFindDialogResource.getString("IDS_ELEMENTS")));
        
        DefaultFindDialogResource.setMnemonic(searchElementsRadio,
            DefaultFindDialogResource.getString("IDS_ELEMENTS"));
        
        searchElementsRadio.getAccessibleContext().setAccessibleDescription(
            DefaultFindDialogResource.getString("ACSD_Search_Element"));
        
        searchElementsRadio.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                onSearchElementsRadio(evt);
            }
        });
        
        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=0;
        gridBagConstraints.weightx=1.0;
        searchInFieldsPanel.add(searchElementsRadio,gridBagConstraints);

        
        // Descriptions radio button
        searchDescrRadio.setText(DefaultFindDialogResource.determineText(
            DefaultFindDialogResource.getString("IDS_DESCRIPTIONS")));
        
        DefaultFindDialogResource.setMnemonic(searchDescrRadio, 
            DefaultFindDialogResource.getString("IDS_DESCRIPTIONS"));
        
        searchDescrRadio.getAccessibleContext().setAccessibleDescription(
            DefaultFindDialogResource.getString("ACSD_Search_Description"));
        
        searchDescrRadio.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                onSearchDescriptionsRadio(evt);
            }
        });
        
        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=1;
        gridBagConstraints.weightx=1.0;
        searchInFieldsPanel.add(searchDescrRadio,gridBagConstraints);
        
        // Alias radio button
        searchAliasRadio.setText(DefaultFindDialogResource.determineText(
            DefaultFindDialogResource.getString("IDS_ALIASTEXT")));
        
        DefaultFindDialogResource.setMnemonic(searchAliasRadio, 
            DefaultFindDialogResource.getString("IDS_ALIASTEXT"));
        
        searchAliasRadio.getAccessibleContext().setAccessibleDescription(
            DefaultFindDialogResource.getString("ACSD_SearchAliasCheck"));
        
        searchAliasRadio.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                onAliasCheck(evt);
            }
        });

        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=2;
        gridBagConstraints.weightx=1.0;
        searchInFieldsPanel.add(searchAliasRadio,gridBagConstraints);

        
        // add "search in" panel to search panels panel
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx=1;
        gridBagConstraints.gridy=0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets=new Insets(5,0,5,5);
        searchPanelsPanel.add(searchInFieldsPanel, gridBagConstraints);

        // Elements/Eescriptions/Alias radio buttons
        ///////////////////////////////////////////////////////////////////////

        mainGBC.gridy = 1;
        mainPanel.add(searchPanelsPanel, mainGBC);

        
        projectFieldsPanel.setLayout(new GridBagLayout());
        projectListPanel.setLayout(new GridBagLayout());
        
        Mnemonics.setLocalizedText(projectsLabel,
            DefaultFindDialogResource.getString("IDS_PROJECTS"));
        
        projectsLabel.setLabelFor(projectList);
        
        projectsLabel.getAccessibleContext().setAccessibleDescription(
            DefaultFindDialogResource.getString("ACSD_ProjectLabel"));
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets=new Insets(0,0,5,0);
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        projectListPanel.add(projectsLabel, gridBagConstraints);
        
        projectList.setBorder(new LineBorder(new Color(0, 0, 0)));
        projectList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jScrollPrjList = new JScrollPane(projectList);
        jScrollPrjList.setMinimumSize(new Dimension(200,50));
        jScrollPrjList.setPreferredSize(new Dimension(200,50));
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(0, 0, 0, 5);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        projectListPanel.add(jScrollPrjList, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets=new Insets(0,0,5,0);
        projectFieldsPanel.add(projectListPanel, gridBagConstraints);
        
        mainGBC.gridy = 2;
        mainPanel.add(projectFieldsPanel, mainGBC);
        
        // results grid
        Mnemonics.setLocalizedText(searchResultsLabel,
            DefaultFindDialogResource.getString("LBL_SearchResult"));
        
        searchResultsLabel.setLabelFor(searchResultsTable);
        
        searchResultsLabel.getAccessibleContext().setAccessibleDescription(
            DefaultFindDialogResource.getString("LBL_SearchResult"));
        
        jScrollResultTable = new JScrollPane(searchResultsTable);
        resultsFieldsPanel.setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 0;
        gridBagConstraints2.insets = new Insets(5,0,5,5);
        gridBagConstraints2.fill = GridBagConstraints.BOTH;
        resultsFieldsPanel.add(searchResultsLabel, gridBagConstraints2);
        
        searchResultsTable.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 1;
        gridBagConstraints2.fill = GridBagConstraints.BOTH;
        gridBagConstraints2.weightx = 1.0;
        gridBagConstraints2.weighty = 1.0;
        jScrollResultTable.setMinimumSize(new Dimension(150,80));
        jScrollResultTable.setPreferredSize(new Dimension(150,80));
        resultsFieldsPanel.add(jScrollResultTable, gridBagConstraints2);
        
        mainGBC.gridy = 3;
        mainPanel.add(resultsFieldsPanel, mainGBC);
        
        // navigate check
        navigateFieldsPanel.setLayout(new GridBagLayout());
        // default the navigate button to true
        navigateCheck.setSelected(true);
        
        navigateCheck.setText(DefaultFindDialogResource.determineText(
            DefaultFindDialogResource.getString("IDS_NAVIGATE")));
        
        DefaultFindDialogResource.setMnemonic(navigateCheck, 
            DefaultFindDialogResource.getString("IDS_NAVIGATE"));
        
        navigateCheck.getAccessibleContext().setAccessibleDescription(
            DefaultFindDialogResource.getString("IDS_NAVIGATE"));
        
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        navigateFieldsPanel.add(navigateCheck,gridBagConstraints);
        
        navigateCheck.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                onNavigateCheck(evt);
            }
        });
        
        mainGBC.gridy = 4;
        mainPanel.add(navigateFieldsPanel, mainGBC);
        
        // replace combo
        replaceFielsPanel.setLayout(new GridBagLayout());
        // text label
        textLabel2.setText(DefaultFindDialogResource.determineText(
            DefaultFindDialogResource.getString("IDS_REPLACEWITH")));
        
        textLabel2.setLabelFor(replaceWithCombo);
        
        DefaultFindDialogResource.setMnemonic(textLabel2, 
            DefaultFindDialogResource.getString("IDS_REPLACEWITH"));
        
        DefaultFindDialogResource.setFocusAccelerator(replaceWithCombo, 
            DefaultFindDialogResource.getString("IDS_REPLACEWITH"));
        
        textLabel2.setName("replaceLabel");
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0,5,0,5);
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.weighty = 0.0;
        replaceFielsPanel.add(textLabel2, gridBagConstraints);
        
        // combo box
        replaceWithCombo.setEditable(true);
        replaceWithCombo.setMaximumRowCount(10);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0,5,0,0);
        replaceFielsPanel.add(replaceWithCombo, gridBagConstraints);

        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(0,5,5,5);
        replaceFielsPanel.add(replaceButton, gridBagConstraints);

        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(0,5,0,5);
        replaceFielsPanel.add(replaceAllButton, gridBagConstraints);

        mainGBC.gridy = 5;
        mainPanel.add(replaceFielsPanel, mainGBC);
        
        // getContentPane().add(mainPanel, BorderLayout.CENTER);
        getContentPane().add(mainPanel, BorderLayout.NORTH);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridBagLayout());
        GridBagConstraints bottomGBC = new GridBagConstraints();

        bottomGBC.gridx = 0;
        bottomGBC.gridy = 0;
        bottomGBC.anchor = GridBagConstraints.LINE_START;
        bottomGBC.fill = GridBagConstraints.BOTH;
        bottomGBC.weightx = 1.0;
        bottomGBC.weighty = 1.0;
        bottomGBC.insets = new Insets(0, 5, 0, 5);
        bottomPanel.add(statusLabel, bottomGBC);

        bottomGBC.gridx = 1;
        bottomGBC.gridy = 0;
        bottomGBC.anchor = GridBagConstraints.LINE_END;
        bottomGBC.fill = GridBagConstraints.NONE;
        bottomGBC.insets = new Insets(0,0,5,5);
        bottomGBC.weightx = 0.0;
        bottomPanel.add(closeButton, bottomGBC);

        
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        
        getAccessibleContext().setAccessibleDescription(
            DefaultFindDialogResource.getString(
            "Action.ReplaceSymbol.Description"));
    }
    
    
    private void configureFont()
    {
        // CBeckham -  added to dynamicaly adjust panel size for larger fonts
        // Note...getFoint.getSize will not return the ide parm -fontsize
        // in most cases of localized version, the user will use the -fontsize 
        // to start the ide regaqrdless of what the os font size setting is, 
        // however in some remote cases the user may actaully have the 
        // OS fontsize setting high
        
        int fontsize;
        Font f = UIManager.getFont("controlFont"); //NOI18N
        if (f != null)
            fontsize = f.getSize();

        else
            fontsize = 12;

        int width  = 450;
        int height = 400;
        int multiplyer = 2;
        
        if (fontsize > 17)
            multiplyer =3;
        
        width  = width  + Math.round(width*(multiplyer*fontsize/100f));
        height = height + Math.round(height*(multiplyer*fontsize/100f));
        setSize(width,height);
    }

    
    private void initButtons()
    {
        // find button
        findButton.setEnabled(false);
        getRootPane().setDefaultButton(findButton);
        
        findButton.setText(DefaultFindDialogResource.determineText(
            DefaultFindDialogResource.getString("IDS_FIND")));
        
        DefaultFindDialogResource.setMnemonic(findButton, 
            DefaultFindDialogResource.getString("IDS_FIND"));
        
        findButton.getAccessibleContext().setAccessibleDescription(
            DefaultFindDialogResource.getString("IDS_FIND"));
        
        findButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                onFindButton(evt);
            }
        });
        
        
        // replace button
        replaceButton.setText(DefaultFindDialogResource.determineText(
            DefaultFindDialogResource.getString("IDS_REPLACE")));
        
        DefaultFindDialogResource.setMnemonic(replaceButton, 
            DefaultFindDialogResource.getString("IDS_REPLACE"));
        
        replaceButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                onReplaceButton(evt);
            }
        });


        // replace all button
        replaceAllButton.setText(DefaultFindDialogResource.determineText(
            DefaultFindDialogResource.getString("IDS_REPLACEALL")));
        
        DefaultFindDialogResource.setMnemonic(replaceAllButton, 
            DefaultFindDialogResource.getString("IDS_REPLACEALL"));
        
        replaceAllButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                onReplaceAllButton(evt);
            }
        });
        
        
        // close button
        closeButton.setText(DefaultFindDialogResource.determineText(
            DefaultFindDialogResource.getString("IDS_CLOSE")));
        
        DefaultFindDialogResource.setMnemonic( closeButton, 
            DefaultFindDialogResource.getString("IDS_CLOSE"));
        
        closeButton.getAccessibleContext().setAccessibleDescription(
            DefaultFindDialogResource.getString("IDS_CLOSE"));
        
        closeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                setVisible(false);
                dispose();
            }
        });
        
        
        // now figure out the button sizes
        Dimension buttonSize = getMaxButtonWidth();
        findButton.setMaximumSize(buttonSize);
        findButton.setPreferredSize(buttonSize);
        closeButton.setPreferredSize(buttonSize);
        closeButton.setMaximumSize(buttonSize);
        replaceButton.setPreferredSize(buttonSize);
        replaceButton.setMaximumSize(buttonSize);
        replaceAllButton.setPreferredSize(buttonSize);
        replaceAllButton.setMaximumSize(buttonSize);
    }
    
    private Dimension getMaxButtonWidth()
    {
        Dimension ret = null;
        Dimension d = findButton.getPreferredSize();
        double max  = d.width;
        
        d =  closeButton.getPreferredSize();
        if(d.width > max)
        {
            max = d.width;
            ret = d;
        }
        d = replaceButton.getPreferredSize();
        if(d.width > max)
        {
            max = d.width;
            ret = d;
        }
        d = replaceAllButton.getPreferredSize();
        if(d.width > max)
        {
            max = d.width;
            ret = d;
        }
        
        return ret;
        
    }
    
    private void initTextFieldListeners()
    {
        class TextChangeListener implements DocumentListener
        {
            private JTextField textField;
            TextChangeListener(JTextField textField)
            {
                this.textField = textField;
            }
            public void changedUpdate(DocumentEvent e)
            {
                documentChanged();
            }
            public void insertUpdate(DocumentEvent e)
            {
                documentChanged();
            }
            public void removeUpdate(DocumentEvent e)
            {
                documentChanged();
            }
            private void documentChanged()
            {
                updateState(textField);
            }
        }
        
        ((JTextField)findWhatCombo.getEditor().getEditorComponent()).getDocument().addDocumentListener(
            new TextChangeListener((JTextField)findWhatCombo.getEditor().getEditorComponent()));
        
        ((JTextField)replaceWithCombo.getEditor().getEditorComponent()).getDocument().addDocumentListener(
            new TextChangeListener((JTextField)replaceWithCombo.getEditor().getEditorComponent()));
    }
    
    private void updateState(JTextField textField)
    {
        if (update == false)
            return;
        
        String text = textField.getText().trim();
        
        if (textField==(JTextField)findWhatCombo.getEditor().getEditorComponent())
        {
            findButton.setEnabled(!"".equals(text));
        }
        
        else if (textField==(JTextField)replaceWithCombo.getEditor().getEditorComponent())
        {
            replaceButton.setEnabled(!"".equals(text) && searchResultsTable.getSelectedRowCount()>0);
            replaceAllButton.setEnabled(!"".equals(text));
        }
    }

    
    private void onXPathCheck(ActionEvent evt)
    {
        Object obj = evt.getSource();

        if (obj instanceof JCheckBox)
        {
            JCheckBox box = (JCheckBox)obj;
            boolean checkboxState = box.isSelected();
            
            if (checkboxState)
            {
                findController.setKind(1);
                findController.setCaseSensitive(true);
                matchCaseCheck.setEnabled(false);
                searchDescrRadio.setEnabled(false);
                searchElementsRadio.setEnabled(false);
                searchAliasRadio.setEnabled(false);
                wholeWordCheck.setEnabled(false);
            }
            
            else
            {
                findController.setKind(0);
                findController.setCaseSensitive(matchCaseCheck.isSelected());
                matchCaseCheck.setEnabled(true);
                searchDescrRadio.setEnabled(true);
                searchElementsRadio.setEnabled(true);
                searchAliasRadio.setEnabled(true);
                wholeWordCheck.setEnabled(true);
            }
        }
    }
    
    private void onAliasCheck(ActionEvent evt)
    {
        Object obj = evt.getSource();
        
        if (obj instanceof JRadioButton)
        {
            findController.setResultType(-1);
            findController.setSearchAlias(true);
            searchElementsRadio.setSelected(false);
            searchDescrRadio.setSelected(false);
            searchAliasRadio.setSelected(true);
        }
    }
    
    private void onWholeWordCheck(ActionEvent evt)
    {
        Object obj = evt.getSource();

        if (obj instanceof JCheckBox)
        {
            JCheckBox box = (JCheckBox)obj;
            boolean checkboxState = box.isSelected();
            
            if (checkboxState)
                findController.setWholeWordSearch(true);

            else
                findController.setWholeWordSearch(false);
        }
    }
    
    private void onMatchCaseCheck(ActionEvent evt)
    {
        Object obj = evt.getSource();
        
        if (obj instanceof JCheckBox)
        {
            Preferences prefs = NbPreferences.forModule (DummyCorePreference.class) ;
            JCheckBox box = (JCheckBox)obj;
            boolean checkboxState = box.isSelected();
        
            if (checkboxState)
            {
                prefs.put ("UML_ShowMe_Allow_Lengthy_Searches", "PSK_NEVER") ;
            }

            else
            {
                findController.setCaseSensitive(false);
                String find = prefs.get ("UML_ShowMe_Allow_Lengthy_Searches", "PSK_ASK");
                
                if (find.equals("PSK_NEVER"))
                    prefs.put("UML_ShowMe_Allow_Lengthy_Searches", "PSK_ALWAYS");
            }
        }
    }
    
    private void onSearchElementsRadio(ActionEvent evt)
    {
        Object obj = evt.getSource();
        if (obj instanceof JRadioButton)
        {
            findController.setResultType(0);
            searchElementsRadio.setSelected(true);
            searchDescrRadio.setSelected(false);
            searchAliasRadio.setSelected(false);
            findController.setSearchAlias(false);
        }
    }
    
    private void onSearchDescriptionsRadio(ActionEvent evt)
    {
        Object obj = evt.getSource();
        if (obj instanceof JRadioButton)
        {
            findController.setResultType(1);
            searchDescrRadio.setSelected(true);
            searchElementsRadio.setSelected(false);
            searchAliasRadio.setSelected(false);
            findController.setSearchAlias(false);
        }
    }
    
    private void onNavigateCheck(ActionEvent evt)
    {
        Object obj = evt.getSource();

        if (obj instanceof JCheckBox)
        {
            JCheckBox box = (JCheckBox)obj;
            boolean checkboxState = box.isSelected();
            if (checkboxState)
                findController.setDiagramNavigate(true);

            else
                findController.setDiagramNavigate(false);
        }
    }
    
    private void onFindButton(ActionEvent evt)
    {
        Object obj = evt.getSource();
        try
        {
            FindUtilities.startWaitCursor(getContentPane());
            onFindButton();
        }

        catch (Exception ex)
        {
            String msg;
            
            if (xpathCheck.isSelected())
                msg = FindUtilities.translateString("IDS_ERROR1");

            else
                msg = FindUtilities.translateString("IDS_NONEFOUND");
            
            statusLabel.setText(msg);
        }

        finally
        {
            FindUtilities.endWaitCursor(getContentPane());
        }
    }
    
    private void onFindButton() throws Exception
    {
        statusLabel.setText("");
        update = false;
        String searchStr = (String)(findWhatCombo.getSelectedItem());
        
        boolean continueFlag = true;
        // Save the values of the search combo
        FindUtilities.saveSearchString("LastSearchStrings", findWhatCombo);
        // reset what is in the search combo
        
        FindUtilities.populateComboBoxes("LastSearchStrings", findWhatCombo);
        // if they have project selected, make sure there is a project selected
        
        int count = projectList.getSelectedIndex();
        if (count == -1)
        {
            continueFlag = false;
            String msg = FindUtilities.translateString("IDS_ERROR2");
            String title = FindUtilities.translateString("IDS_PROJNAME2");
            IErrorDialog pTemp = new SwingErrorDialog(this);
            if (pTemp != null)
            {
                pTemp.display(msg, MessageIconKindEnum.EDIK_ICONINFORMATION, title);
            }
        }
        
        if (continueFlag)
        {
            findController.setSearchString(searchStr);
            FindUtilities.loadProjectListOfController(projectList, findController);
            // do the search
            FindResults pResults = new FindResults();
            findController.search(pResults);
            if (pResults != null)
            {
                ETList<IElement> pElements = pResults.getElements();
                ETList<IProxyDiagram> pDiagrams = pResults.getDiagrams();
                if ( (pElements != null) && (pDiagrams != null))
                {
                    int countD = pDiagrams.size();
                    if (pElements.size() > 0 || countD > 0)
                    {
                        // show the results
                        ETList< Object > findResults = FindUtilities.loadResultsIntoArray(pResults);
                        FindTableModel model = new FindTableModel(this, findResults);
                        searchResultsTable.setModel(model);
                        replaceWithCombo.setEnabled(true);
                        
                        long totalC = pElements.size() + countD;
                        String strMsg = totalC + " ";
                        strMsg += FindUtilities.translateString("IDS_NUMFOUND");
                        statusLabel.setText(strMsg);
                        //
                        // This is special code to aid in the automating testing.  We had no way to access
                        // the information in the grid from the automated scripts and/or VisualTest, so
                        // if a flag is set in the registry, we will dump the results of the grid to a
                        // specified file
                        //
                            /* TODO
                            if( GETDEBUGFLAG_RELEASE(_T("DumpGridResults"), 0))
                            {
                             CComBSTR file = CRegistry::GetItem( CString(_T("DumpGridResultsFile")), CString(_T("")));
                                 if (file.Length())
                                 {
                                     m_FlexGrid->SaveGrid(file, flexFileCommaText, CComVariant(FALSE));
                                 }
                             }
                             */
                    }
                    else
                    {
                        clearGrid();
                        String noneStr = FindUtilities.translateString("IDS_NONEFOUND");
                        statusLabel.setText(noneStr);
                    }
                }
                else
                {
                    String canStr = FindUtilities.translateString("IDS_CANCELLED");
                    statusLabel.setText(canStr);
                }
            }
            else
            {
                String str2 = FindUtilities.translateString("IDS_NONEFOUND2");
                statusLabel.setText(str2);
            }
            
        }
        findWhatCombo.setSelectedItem(searchStr);

        update = true;
        if (replaceWithCombo.isEnabled())
            updateState((JTextField)replaceWithCombo.getEditor().getEditorComponent());
        findWhatCombo.getEditor().selectAll();
    }
    
    
    private void onReplaceButton(ActionEvent evt)
    {
        Object obj = evt.getSource();
        if (obj instanceof JButton)
        {
            statusLabel.setText("");
            String str;
            str = (String)(replaceWithCombo.getSelectedItem());
            if (str != null && str.length() > 0)
            {
                FindUtilities.startWaitCursor(getContentPane());
                // Save the values of the search combo
                FindUtilities.saveSearchString("LastReplaceStrings", replaceWithCombo);
                // reset what is in the search combo
                FindUtilities.populateComboBoxes("LastReplaceStrings", replaceWithCombo);
                if (findController != null)
                {
                    findController.setReplaceString(str);
                    FindResults pResults = new FindResults();
                    if (pResults != null)
                    {
                        loadResultsFromGrid(pResults, true);
                        ETList<IElement> pElements = pResults.getElements();
                        ETList<IProxyDiagram> pDiagrams = pResults.getDiagrams();
                        if ( (pElements != null) && (pDiagrams != null))
                        {
                            int count = pElements.size();
                            int countD = pDiagrams.size();
                            if (count > 0 || countD > 0)
                            {
                                // clear the grid
                                findController.replace(pResults);
                                ETList< Object > findResults = FindUtilities.loadResultsIntoArray(pResults);
                                FindTableModel model = new FindTableModel(this, findResults);
                                searchResultsTable.setModel(model);
                                
                                //
                                // This is special code to aid in the automating testing.  We had no way to access
                                // the information in the grid from the automated scripts and/or VisualTest, so
                                // if a flag is set in the registry, we will dump the results of the grid to a
                                // specified file
                                //
                                // TODO
                                //if( GETDEBUGFLAG_RELEASE(_T("DumpGridResults"), 0))
                                //{
                                //	CComBSTR file = CRegistry::GetItem( CString(_T("DumpGridResultsFile")), CString(_T("")));
                                //	if (file.Length())
                                //	{
                                //		m_FlexGrid->SaveGrid(file, flexFileCommaText, CComVariant(FALSE));
                                //	}
                                //}
                            }
                            else
                            {
                                // no items selected in the grid
                                String noneStr = FindUtilities.translateString("IDS_NOITEMSSELECTED");
                                String str2 = FindUtilities.translateString("IDS_PROJNAME2");
                                IErrorDialog pTemp = new SwingErrorDialog(this);
                                if (pTemp != null)
                                {
                                    pTemp.display(noneStr, MessageIconKindEnum.EDIK_ICONINFORMATION, str2);
                                }
                            }
                        }
                    }
                }
                replaceWithCombo.setSelectedItem(str);
                FindUtilities.endWaitCursor(getContentPane());
                disableReplaceSection();
            }
            else
            {
                String strNo = FindUtilities.translateString("IDS_NOREPLACESTR");
                String str2 = FindUtilities.translateString("IDS_PROJNAME2");
                IErrorDialog pTemp = new SwingErrorDialog(this);
                if (pTemp != null)
                {
                    pTemp.display(strNo, MessageIconKindEnum.EDIK_ICONINFORMATION, str2);
                }
            }
        }
    }
    
    public void onDblClickFindResults(int row, FindTableModel model, boolean isShift)
    {
        statusLabel.setText("");
        boolean hr = FindUtilities.onDblClickFindResults(row, model, findController, isShift);
        if (!hr)
        {
            String msg =  FindUtilities.translateString("IDS_NOPRESELEMENTS");
            statusLabel.setText(msg);
        }
    }
    
    private void onReplaceAllButton(ActionEvent evt)
    {
        Object obj = evt.getSource();
        if (obj instanceof JButton)
        {
            statusLabel.setText("");
            String str;
            str = (String)(replaceWithCombo.getSelectedItem());
            if (str != null && str.length() > 0)
            {
                FindUtilities.startWaitCursor(getContentPane());
                // Save the values of the search combo
                FindUtilities.saveSearchString("LastReplaceStrings", replaceWithCombo);
                // reset what is in the search combo
                FindUtilities.populateComboBoxes("LastReplaceStrings", replaceWithCombo);
                if (findController != null)
                {
                    findController.setReplaceString(str);
                    FindResults pResults = new FindResults();
                    if (pResults != null)
                    {
                        loadResultsFromGrid(pResults, false);
                        // clear the grid
                        findController.replace(pResults);
                        ETList< Object > findResults = FindUtilities.loadResultsIntoArray(pResults);
                        FindTableModel model = new FindTableModel(this, findResults);
                        searchResultsTable.setModel(model);
                        //
                        // This is special code to aid in the automating testing.  We had no way to access
                        // the information in the grid from the automated scripts and/or VisualTest, so
                        // if a flag is set in the registry, we will dump the results of the grid to a
                        // specified file
                        //
                        // TODO
                        //if( GETDEBUGFLAG_RELEASE(_T("DumpGridResults"), 0))
                        //{
                        //	CComBSTR file = CRegistry::GetItem( CString(_T("DumpGridResultsFile")), CString(_T("")));
                        //	if (file.Length())
                        //	{
                        //		m_FlexGrid->SaveGrid(file, flexFileCommaText, CComVariant(FALSE));
                        //	}
                        //}
                    }
                }
                FindUtilities.endWaitCursor(getContentPane());
                disableReplaceSection();
            }
            else
            {
                String strNo = FindUtilities.translateString("IDS_NOREPLACESTR");
                String str2 = FindUtilities.translateString("IDS_PROJNAME2");
                IErrorDialog pTemp = new SwingErrorDialog(this);
                if (pTemp != null)
                {
                    pTemp.display(strNo, MessageIconKindEnum.EDIK_ICONINFORMATION, str2);
                }
            }
        }
    }
    
    private void loadResultsFromGrid(FindResults pResults, boolean bSelect)
    {
        if (pResults != null)
        {
            // get the elements array from the results object
            ETList<IElement> pElements = pResults.getElements();
            // get the diagrams array from the results object
            ETList<IProxyDiagram> pDiagrams = pResults.getDiagrams();
            if ( (pElements != null) && (pDiagrams != null))
            {
                if (bSelect)
                {
                    // loop through the information in the table
                    int[] selRows = searchResultsTable.getSelectedRows();
                    for (int x = 0; x < selRows.length; x++)
                    {
                        int selRow = selRows[x];
                        FindTableModel model = (FindTableModel)searchResultsTable.getModel();
                        
                        if (model != null)
                        {
                            IElement pElement = model.getElementAtRow(selRow);
                            if (pElement != null)
                            {
                                pElements.add(pElement);
                            }
                            else
                            {
                                IProxyDiagram pDiagram = model.getDiagramAtRow(selRow);
                                if (pDiagram != null)
                                {
                                    pDiagrams.add(pDiagram);
                                }
                            }
                        }
                    }
                }
                else
                {
                    int rows = searchResultsTable.getRowCount();
                    for (int x = 0; x < rows; x++)
                    {
                        FindTableModel model = (FindTableModel)searchResultsTable.getModel();
                        if (model != null)
                        {
                            IElement pElement = model.getElementAtRow(x);
                            if (pElement != null)
                            {
                                pElements.add(pElement);
                            }
                            else
                            {
                                IProxyDiagram pDiagram = model.getDiagramAtRow(x);
                                if (pDiagram != null)
                                {
                                    pDiagrams.add(pDiagram);
                                }
                            }
                        }
                    }
                    
                }
            }
        }
    }
    
    private void clearGrid()
    {
        // clear the results
        FindTableModel model = new FindTableModel(this, null);
        searchResultsTable.setModel(model);
        disableReplaceSection();
    }
    
    private void disableReplaceSection()
    {
        replaceWithCombo.setEnabled(false);
        replaceButton.setEnabled(false);
        replaceAllButton.setEnabled(false);
    }
    
    
    private void initDialog()
    {
        statusLabel.setText("");
        FindUtilities.populateProjectList(projectList);
        FindUtilities.selectProjectInList( projectList );
        FindUtilities.populateComboBoxes("LastSearchStrings", findWhatCombo);
        FindUtilities.populateComboBoxes("LastReplaceStrings", replaceWithCombo);
        disableReplaceSection();
        findWhatCombo.getEditor().selectAll();
    }
    
    public void setController(FindController controller)
    {
        findController = controller;
        findController.setDialog(this);
    }
    
    
    /** Closes the dialog */
    private void closeDialog(WindowEvent evt)
    {
        setVisible(false);
        dispose();
    }
    
    private boolean isMatchCase()
    {
        return !"PSK_ALWAYS".equals(ProductHelper.getPreferenceManager().getPreferenceValue("FindDialog", "LongSearch"));
    }
    
    private class SelectionListener implements ListSelectionListener
    {
        public void valueChanged(ListSelectionEvent e)
        {
            updateState((JTextField)replaceWithCombo.getEditor().getEditorComponent());
        }
    }
    
    // Variables declaration - do not modify
    private JButton findButton;
    private JButton  closeButton;
    private JLabel findWhatLabel;
    private JLabel textLabel2;
    private JComboBox findWhatCombo;
    private JCheckBox matchCaseCheck;
    private JCheckBox navigateCheck;
    private JList projectList;
    private JLabel projectsLabel;
    private JLabel searchResultsLabel;
    private JTable searchResultsTable;
    private JRadioButton searchAliasRadio;
    private JRadioButton searchDescrRadio;
    private JRadioButton searchElementsRadio;
    private JLabel statusLabel;
    private JCheckBox wholeWordCheck;
    private JCheckBox xpathCheck;
    private JPanel mainPanel;
    private JPanel resultsFieldsPanel;
    private JPanel navigateFieldsPanel;
    private JPanel searchPanelsPanel;
    private JPanel searchOptionsPanel;
    private JPanel searchInFieldsPanel;
    private JPanel findWhatFieldsPanel;
    private JPanel replaceFielsPanel;
    private JPanel projectListPanel;
    private JPanel projectFieldsPanel;
    private JButton replaceButton;
    private JButton replaceAllButton;
    private JComboBox replaceWithCombo;
    private JScrollPane jScrollPrjList;
    private SelectionListener selectionListener;
    private boolean update = true;
    
    
    // End of variables declaration
    private org.netbeans.modules.uml.integration.finddialog.FindController findController = null;
}

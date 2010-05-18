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
package org.netbeans.modules.visualweb.faces.dt.std.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import org.netbeans.modules.visualweb.propertyeditors.StandardUrlPanel;

public class HtmlDataTableCustomizerPagingPanel extends JPanel {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(
        HtmlDataTableCustomizerPagingPanel.class);
    private static final int DEFAULT_PAGING_ROWS = 10;
    private Map inputDependents = new HashMap();

    private JCheckBox chkEnablePaging = new JCheckBox();
    private JTextField tfPageSize = new JTextField();
    private JLabel lblPageSize = new JLabel();
    private JLabel lblNav = new JLabel();
    private JComboBox comboNav = new JComboBox();
    private JCheckBox chkFirst = new JCheckBox();
    private JCheckBox chkPrevious = new JCheckBox();
    private JCheckBox chkNext = new JCheckBox();
    private JCheckBox chkLast = new JCheckBox();
    private JLabel lblLocation = new JLabel();
    private JComboBox comboLocation = new JComboBox();
    private JTextField tfFirst = new JTextField();
    private JTextField tfPrevious = new JTextField();
    private JTextField tfNext = new JTextField();
    private JTextField tfLast = new JTextField();
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JPanel fillerPanel = new JPanel();
    private HtmlDataTableState table;
    private JButton btnBrowseFirst = new JButton();
    private JButton btnBrowsePrevious = new JButton();
    private JButton btnBrowseNext = new JButton();
    private JButton btnBrowseLast = new JButton();
    private JLabel lblAlign = new JLabel();
    private JComboBox comboAlign = new JComboBox();
    private String previousComboNavSelection;

    HtmlDataTableCustomizerPagingPanel() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void setTable(HtmlDataTableState table) {
        this.table = table;
        initState();
    }

    private void jbInit() throws Exception {
        ChangeListener chkListener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                chk_stateChanged(e);
            }
        };
        ActionListener pagingActionListener = new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		pagingPanelActionPerformed(e);
        	}
        };
        chkEnablePaging.addChangeListener(chkListener);
        chkEnablePaging.setText(bundle.getMessage("enablePaging")); //NOI18N
        lblPageSize.setText(bundle.getMessage("pageSize")); //NOI18N
        //tfPageSize.setPreferredSize(new Dimension(100, 24));
        tfPageSize.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                tfPageSize_changed(e);
            }

            public void removeUpdate(DocumentEvent e) {
                tfPageSize_changed(e);
            }

            public void changedUpdate(DocumentEvent e) {
                tfPageSize_changed(e);
            }
        });
        lblNav.setText(bundle.getMessage("pageNavControls"));	//NOI18N
        comboNav.setMinimumSize(new Dimension(30, 24));
        //comboNav.setPreferredSize(new Dimension(150, 24));
        comboNav.addItem(HtmlDataTableState.BUTTON_TEXT);
        comboNav.addItem(HtmlDataTableState.BUTTON_IMAGE);
        comboNav.addItem(HtmlDataTableState.BUTTON_NONE);
        comboNav.addActionListener(pagingActionListener);

        chkFirst.addChangeListener(chkListener);
        chkFirst.setText(bundle.getMessage("firstPage")); //NOI18N
        chkPrevious.addChangeListener(chkListener);
        chkPrevious.setText(bundle.getMessage("prevPage")); //NOI18N
        chkNext.addChangeListener(chkListener);
        chkNext.setText(bundle.getMessage("nextPage")); //NOI18N
        chkLast.addChangeListener(chkListener);
        chkLast.setText(bundle.getMessage("lastPage")); //NOI18N

        tfFirst.setColumns(20);
        tfPrevious.setColumns(20);
        tfNext.setColumns(20);
        tfLast.setColumns(20);

        lblLocation.setText(bundle.getMessage("position")); //NOI18N
        comboLocation.setMinimumSize(new Dimension(30, 24));
        //comboLocation.setPreferredSize(new Dimension(150, 24));
        comboLocation.addItem(HtmlDataTableState.TOP);
        comboLocation.addItem(HtmlDataTableState.BOTTOM);
        comboLocation.addItem(HtmlDataTableState.TOP_AND_BOTTOM);

        lblAlign.setText(bundle.getMessage("align")); //NOI18N
        comboAlign.setMinimumSize(new Dimension(30, 24));
        //comboAlign.setPreferredSize(new Dimension(150, 24));
        comboAlign.addItem(HtmlDataTableState.ALIGN_LEFT);
        comboAlign.addItem(HtmlDataTableState.ALIGN_CENTER);
        comboAlign.addItem(HtmlDataTableState.ALIGN_RIGHT);

        String browseText = bundle.getMessage("browseEllipse"); //NOI18N
        btnBrowseFirst.setText(browseText);
        btnBrowseFirst.addActionListener(pagingActionListener);
        btnBrowsePrevious.setText(browseText);
        btnBrowsePrevious.addActionListener(pagingActionListener);
        btnBrowseNext.setText(browseText);
        btnBrowseNext.addActionListener(pagingActionListener);
        btnBrowseLast.setText(browseText);
        btnBrowseLast.addActionListener(pagingActionListener);

        inputDependents.put(chkEnablePaging, new JComponent[] {
            lblPageSize, tfPageSize, comboNav
        });
        inputDependents.put(comboNav, new JComponent[] {
            chkFirst, tfFirst, btnBrowseFirst,
            chkPrevious, tfPrevious, btnBrowsePrevious,
            chkNext, tfNext, btnBrowseNext,
            chkLast, tfLast, btnBrowseLast,
            lblLocation, comboLocation, lblAlign, comboAlign
        });

        this.setLayout(gridBagLayout1);
        this.add(chkEnablePaging,	new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(8, 8, 2, 8), 0, 0));

        this.add(lblPageSize,		new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 22, 4, 8), 0, 0));
        this.add(tfPageSize,		new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 22, 8, 8), 0, 0));

        this.add(lblNav,			new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(8, 22, 16, 4), 0, 0));
        this.add(comboNav,			new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(8, 4, 16, 4), 0, 0));

        this.add(chkFirst,			new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 32, 8, 4), 0, 0));
        this.add(tfFirst,			new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 4, 8, 4), 0, 0));
        this.add(btnBrowseFirst,	new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 4, 8, 8), 0, 0));

        this.add(chkPrevious,		new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 32, 8, 4), 0, 0));
        this.add(tfPrevious,		new GridBagConstraints(1, 5, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 4, 8, 4), 0, 0));
        this.add(btnBrowsePrevious,	new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 4, 8, 8), 0, 0));

        this.add(chkNext,			new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 32, 8, 4), 0, 0));
        this.add(tfNext,			new GridBagConstraints(1, 6, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 4, 8, 4), 0, 0));
        this.add(btnBrowseNext,		new GridBagConstraints(2, 6, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 4, 8, 8), 0, 0));

        this.add(chkLast,			new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 32, 8, 4), 0, 0));
        this.add(tfLast,			new GridBagConstraints(1, 7, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 4, 8, 4), 0, 0));
        this.add(btnBrowseLast,		new GridBagConstraints(2, 7, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 4, 8, 8), 0, 0));

        this.add(lblLocation,		new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 35, 8, 4), 0, 0));
        this.add(comboLocation,		new GridBagConstraints(1, 8, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 4, 8, 4), 0, 0));

        this.add(lblAlign,			new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 35, 8, 4), 0, 0));
        this.add(comboAlign,		new GridBagConstraints(1, 9, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 4, 8, 4), 0, 0));

        this.add(fillerPanel, 		new GridBagConstraints(0, 10, 3, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    }

    public void pagingPanelActionPerformed(ActionEvent e) {
        if (e.getSource() == comboNav) {
        	String comboNavSelectedItem = (String)comboNav.getSelectedItem();
        	boolean textOrImage = (HtmlDataTableState.BUTTON_TEXT.equals(comboNavSelectedItem) || HtmlDataTableState.BUTTON_IMAGE.equals(comboNavSelectedItem));
            if (textOrImage && !chkFirst.isSelected() && !chkPrevious.isSelected() &&
                !chkNext.isSelected() && !chkLast.isSelected()) {
                chkFirst.setSelected(true);
                chkPrevious.setSelected(true);
                chkNext.setSelected(true);
                chkLast.setSelected(true);
            }

            //put existing text field content into HtmlDataTableState

            if (HtmlDataTableState.BUTTON_NONE.equals(previousComboNavSelection) || HtmlDataTableState.BUTTON_TEXT.equals(previousComboNavSelection)) {
            	//save existing text in all text fields as button text
            	table.paging.firstButtonText = tfFirst.getText();
            	table.paging.previousButtonText = tfPrevious.getText();
            	table.paging.nextButtonText = tfNext.getText();
            	table.paging.lastButtonText = tfLast.getText();
            }
            else if (HtmlDataTableState.BUTTON_IMAGE.equals(previousComboNavSelection)) {
            	table.paging.firstButtonUrl = tfFirst.getText();
            	table.paging.previousButtonUrl = tfPrevious.getText();
            	table.paging.nextButtonUrl = tfNext.getText();
            	table.paging.lastButtonUrl = tfLast.getText();
            }

            //modify text field contents

            if (HtmlDataTableState.BUTTON_IMAGE.equals(comboNavSelectedItem)) {
            	tfFirst.setText(table.paging.firstButtonUrl);
            	tfPrevious.setText(table.paging.previousButtonUrl);
            	tfNext.setText(table.paging.nextButtonUrl);
            	tfLast.setText(table.paging.lastButtonUrl);
            }
            else {
            	tfFirst.setText(table.paging.firstButtonText);
            	tfPrevious.setText(table.paging.previousButtonText);
            	tfNext.setText(table.paging.nextButtonText);
            	tfLast.setText(table.paging.lastButtonText);
            }

            setDependentsEnabled(comboNav);

            previousComboNavSelection = comboNavSelectedItem;
        }
        else {
	        JButton[] browseButtons = {btnBrowseFirst, btnBrowsePrevious, btnBrowseNext, btnBrowseLast};
	        JTextField[] textFields = {tfFirst, tfPrevious, tfNext, tfLast};
	        for (int i = 0; i < browseButtons.length; i++) {
		    	if (e.getSource() == browseButtons[i]) {
		            StandardUrlPanel urlPanel = new StandardUrlPanel();
		            urlPanel.setDesignProperty(null);
		            urlPanel.setDesignContext(table.getTableBean().getDesignContext());
		            urlPanel.initialize();
		            URLDialog urlDialog = new URLDialog(getParentFrame(this), urlPanel);
		            urlDialog.show();
		            textFields[i].setText(urlDialog.getUrlString());
		            urlPanel = null;
		            urlDialog = null;
		            break;
		    	}
	        }
        }
    }

    void initState() {
        chkEnablePaging.setSelected(table.paging.rows > 0);
        tfPageSize.setText(table.paging.rows > 0 ? String.valueOf(table.paging.rows) :
            String.valueOf(DEFAULT_PAGING_ROWS));

        comboNav.setSelectedItem(table.paging.navigation);
        previousComboNavSelection = table.paging.navigation;

        chkFirst.setSelected(table.paging.firstButton);
        tfFirst.setText(HtmlDataTableState.BUTTON_IMAGE.equals(table.paging.navigation) ? table.paging.firstButtonUrl : table.paging.firstButtonText);
        chkPrevious.setSelected(table.paging.previousButton);
        tfPrevious.setText(HtmlDataTableState.BUTTON_IMAGE.equals(table.paging.navigation) ? table.paging.previousButtonUrl : table.paging.previousButtonText);
        chkNext.setSelected(table.paging.nextButton);
        tfNext.setText(HtmlDataTableState.BUTTON_IMAGE.equals(table.paging.navigation) ? table.paging.nextButtonUrl : table.paging.nextButtonText);
        chkLast.setSelected(table.paging.lastButton);
        tfLast.setText(HtmlDataTableState.BUTTON_IMAGE.equals(table.paging.navigation) ? table.paging.lastButtonUrl : table.paging.lastButtonText);

        if (table.paging.navOnTop && table.paging.navOnBottom) {
            comboLocation.setSelectedItem(HtmlDataTableState.TOP_AND_BOTTOM);
        } else if (table.paging.navOnTop) {
            comboLocation.setSelectedItem(HtmlDataTableState.TOP);
        } else if (table.paging.navOnBottom) {
            comboLocation.setSelectedItem(HtmlDataTableState.BOTTOM);
        }

        comboAlign.setSelectedItem(table.paging.align);

        setDependentsEnabled(chkEnablePaging);
    }

    private int getPagingRows() {
        int rows = 0;
        if (chkEnablePaging.isSelected()) {
            try {
                rows = Integer.parseInt(tfPageSize.getText().trim());
            } catch (Exception x) {
                //let rows be 0
            }
        }
        return rows;
    }

    void saveState() {
        table.paging.rows = getPagingRows();
        String comboNavSelection = (String)comboNav.getSelectedItem();
        table.paging.navigation = comboNavSelection;
        String location = (String)comboLocation.getSelectedItem();
        table.paging.navOnTop = location.equals(HtmlDataTableState.TOP) ||
            location.equals(HtmlDataTableState.TOP_AND_BOTTOM);
        table.paging.navOnBottom = location.equals(HtmlDataTableState.BOTTOM) ||
            location.equals(HtmlDataTableState.TOP_AND_BOTTOM);
        table.paging.align = (String)comboAlign.getSelectedItem();
        table.paging.firstButton = chkFirst.isSelected();
        table.paging.previousButton = chkPrevious.isSelected();
        table.paging.nextButton = chkNext.isSelected();
        table.paging.lastButton = chkLast.isSelected();
        if (HtmlDataTableState.BUTTON_NONE.equals(comboNavSelection) || HtmlDataTableState.BUTTON_TEXT.equals(comboNavSelection)) {
        	//save existing text in all text fields as button text
        	table.paging.firstButtonText = tfFirst.getText();
        	table.paging.previousButtonText = tfPrevious.getText();
        	table.paging.nextButtonText = tfNext.getText();
        	table.paging.lastButtonText = tfLast.getText();
        }
        else if (HtmlDataTableState.BUTTON_IMAGE.equals(comboNavSelection)) {
        	table.paging.firstButtonUrl = tfFirst.getText();
        	table.paging.previousButtonUrl = tfPrevious.getText();
        	table.paging.nextButtonUrl = tfNext.getText();
        	table.paging.lastButtonUrl = tfLast.getText();
        }
    }

    private void chk_stateChanged(ChangeEvent e) {
        setDependentsEnabled((JComponent)e.getSource());
    }

    private void tfPageSize_changed(DocumentEvent e) {
        setDependentsEnabled(chkEnablePaging);
    }

    private void setDependentsEnabled(JComponent source) {
        JComponent[] dependents = (JComponent[])inputDependents.get(source);
        if (dependents == null) {
        	return;
        }
        boolean on = false;
        String selectedComboItem = null;
        if (source instanceof JCheckBox) {
        	JCheckBox chkSource = (JCheckBox)source;
        	on = chkSource.isEnabled() && chkSource.isSelected();
        }
        else if (source == comboNav) {
        	selectedComboItem = (String)comboNav.getSelectedItem();
        	on = comboNav.isEnabled() && (HtmlDataTableState.BUTTON_TEXT.equals(selectedComboItem) || HtmlDataTableState.BUTTON_IMAGE.equals(selectedComboItem));
        }
        for (int i = 0; i < dependents.length; i++) {
        	boolean localOn = on;
            if (source == chkEnablePaging && dependents[i] != lblPageSize && dependents[i] != tfPageSize) {
            	localOn = localOn && getPagingRows() > 0;
            }
            if (source == comboNav && (dependents[i].equals(btnBrowseFirst) || dependents[i].equals(btnBrowsePrevious) || dependents[i].equals(btnBrowseNext) || dependents[i].equals(btnBrowseLast))) {
            	localOn = localOn && HtmlDataTableState.BUTTON_IMAGE.equals(selectedComboItem);
            }
            dependents[i].setEnabled(localOn);
            if (dependents[i] instanceof JTextField) {
                dependents[i].setBackground(localOn ? SystemColor.text : SystemColor.control);
            }
            setDependentsEnabled(dependents[i]);
        }
    }

	private Frame getParentFrame(Component c) {
		while (true) {
			if (c == null) {
				return null;
			}
			if (c instanceof Frame) {
				return (Frame)c;
			}
			c = c.getParent();
		}
	}

    private class URLDialog extends JDialog {
    	private JButton btnOk, btnCancel;
    	private StandardUrlPanel urlPanel;
    	private String urlString;

    	public URLDialog(Frame parent, StandardUrlPanel urlPanel) {
    		super(parent, bundle.getMessage("urlDlgTitle"), true);	//NOI18N
    		this.urlPanel = urlPanel;
       		ActionListener dialogActionListener = new ActionListener() {
    			public void actionPerformed(ActionEvent e) {
    				dialogActionPerformed(e);
    			}
    		};
    		JPanel buttonPanelParent = new JPanel();
    		((FlowLayout)buttonPanelParent.getLayout()).setAlignment(FlowLayout.RIGHT);
    		JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
    		buttonPanelParent.add(buttonPanel);
    		btnOk = new JButton(bundle.getMessage("okCaps"));	//NOI18N
    		btnCancel =  new JButton(bundle.getMessage("cancel"));	//NOI18N
    		btnOk.addActionListener(dialogActionListener);
    		btnCancel.addActionListener(dialogActionListener);
    		buttonPanel.add(btnOk);
    		buttonPanel.add(btnCancel);
    		getContentPane().add(urlPanel);
    		getContentPane().add(buttonPanelParent, BorderLayout.SOUTH);
    		pack();
    		setResizable(true);
    		setLocationRelativeTo(parent);
    	}

    	public void dialogActionPerformed(ActionEvent e) {
			if (e.getSource() == btnOk) {
	    		Object urlObj = this.urlPanel.getPropertyValue();
	    		if (urlObj == null) {
	    			this.urlString = null;
	    		}
	    		if (urlObj instanceof String) {
	    			this.urlString = (String)urlObj;
	    		}
	    		else {
	    			this.urlString = urlObj.toString();
	    		}
	    		dispose();
			}
			else if (e.getSource() == btnCancel) {
				dispose();
			}
    	}

    	public String getUrlString() {
    		return this.urlString;
    	}
    }
}

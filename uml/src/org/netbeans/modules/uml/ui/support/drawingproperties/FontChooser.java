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


package org.netbeans.modules.uml.ui.support.drawingproperties;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.netbeans.modules.uml.ui.swing.commondialogs.JCenterDialog;

import java.awt.event.ActionEvent;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/*
 * Created on Mar 7, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author swadebeshp
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FontChooser extends JCenterDialog implements ItemListener{
	protected static JPanel mainPanel = null;
	protected static JPanel UIPanel = null;
	protected static JPanel stylePanel = null;
	protected static JPanel buttonPanel = null;
	protected static JPanel optionPanel = null;
	protected static JPanel samplePanel = null;
	protected static JList m_NameList = null;
	protected static JList m_SizeList = null;
	protected static JLabel fontLabel = null;
	protected static JLabel fontSizeLabel = null;
	protected static JLabel m_sampleField = null;
	protected JLabel m_selectedFontName = null;
	protected JLabel m_selectedFontSize = null;
	protected static JButton m_ok = null;
	protected static JButton m_cancel = null;
	protected static JScrollPane fontScrollPane = null;
	protected static JScrollPane fontSizePane = null;
	protected static JCheckBox m_boldCheck = null;
	protected static JCheckBox m_italicCheck = null;
	
	private Font m_DefaultFont = null;
	protected static Font m_font = null;
	protected static String m_fontName = "";
	protected static int m_size = 0;
	
	protected static boolean m_bBold = false;
	protected static boolean m_bItalic = false;
	protected static String m_sampleStr = DrawingPropertyResource.getString("IDS_SAMPLESTRING");
	
	GridBagConstraints gridBagConstraints;
	
	protected void init()
	{
		getContentPane().setLayout(new GridBagLayout());
		
		//All panel lists of the frame.
		mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		UIPanel = new JPanel();
		UIPanel.setLayout(new GridBagLayout());
		stylePanel = new JPanel();
		stylePanel.setLayout(new GridBagLayout());
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
	
		//Components of the UIPanel
                String fontText = DrawingPropertyResource.getString("IDS_FONT");
		fontLabel = new JLabel(DrawingPropertyResource.determineText(fontText));
                DrawingPropertyResource.setMnemonic(fontLabel, fontText);
                
		gridBagConstraints= new GridBagConstraints();
		gridBagConstraints.fill=GridBagConstraints.BOTH;
		gridBagConstraints.weightx=0.7;
		gridBagConstraints.weighty=0.1;
		gridBagConstraints.gridx=0;
		gridBagConstraints.gridy=0;
		UIPanel.add(fontLabel,gridBagConstraints);
		
		m_selectedFontName = new JLabel();
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] names = env.getAvailableFontFamilyNames();
		m_NameList = new JList(names);
                fontLabel.setLabelFor(m_NameList);
                String fontNameDesc = NbBundle.getMessage(FontChooser.class, "ADDS_FONT");
                m_NameList.getAccessibleContext().setAccessibleDescription(fontNameDesc);
                
		m_NameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_NameList.setVisibleRowCount(6);
		m_NameList.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				JList names = (JList)e.getSource();
				m_selectedFontName.setText((String)names.getSelectedValue());
				m_fontName = m_selectedFontName.getText();
				fontChanged();
			}
		});	
		gridBagConstraints= new GridBagConstraints();
		gridBagConstraints.fill=GridBagConstraints.BOTH;
		gridBagConstraints.weightx=0.7;
		gridBagConstraints.weighty=0.1;
		gridBagConstraints.gridx=0;
		gridBagConstraints.gridy=1;
		UIPanel.add(m_selectedFontName,gridBagConstraints);
		
		fontScrollPane = new JScrollPane(m_NameList);
		 
		gridBagConstraints= new GridBagConstraints();
		gridBagConstraints.fill=GridBagConstraints.BOTH;
		gridBagConstraints.weightx=0.7;
		gridBagConstraints.weighty=0.8;
		gridBagConstraints.gridx=0;
		gridBagConstraints.gridy=2;
		UIPanel.add(fontScrollPane,gridBagConstraints);
		
                String fontSizeText = DrawingPropertyResource.getString("IDS_SIZE");
                fontSizeLabel = new JLabel(DrawingPropertyResource.determineText(fontSizeText));
                DrawingPropertyResource.setMnemonic(fontSizeLabel, fontSizeText);
		gridBagConstraints= new GridBagConstraints();
		gridBagConstraints.fill=GridBagConstraints.BOTH;
		gridBagConstraints.weightx=0.3;
		gridBagConstraints.weighty=0.1;
		gridBagConstraints.gridx=1;
		gridBagConstraints.gridy=0;
		UIPanel.add(fontSizeLabel,gridBagConstraints);
		
		m_selectedFontSize = new JLabel();
		
		Object[] objs = {"8", "10", "12", "14", "18", "24", "36"};
		m_SizeList = new JList(objs);
		m_SizeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_SizeList.setVisibleRowCount(6);
                fontSizeLabel.setLabelFor(m_SizeList);
                
                String fontSizeDesc = NbBundle.getMessage(FontChooser.class, "ADDS_SIZE");
                m_SizeList.getAccessibleContext().setAccessibleDescription(fontSizeDesc);
		
		FontMetrics metrics = null;
		if (m_DefaultFont != null){
			metrics = getFontMetrics(m_DefaultFont);
		}
		else{
			metrics = getFontMetrics(getFont());
		}
		int width = metrics.stringWidth("36");
		Dimension size = m_SizeList.getPreferredSize();
		size.width = width * 2;
		m_SizeList.setPreferredSize(size);
		m_SizeList.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				JList sizes = (JList)e.getSource();
				if (sizes.getSelectedValue() != null)
				{
					m_selectedFontSize.setText((String)sizes.getSelectedValue());
					m_size = Integer.parseInt(m_selectedFontSize.getText());
					fontChanged();
				}
			}
		});
		
		fontSizePane = new JScrollPane(m_SizeList);
		
		gridBagConstraints= new GridBagConstraints();
		gridBagConstraints.fill=GridBagConstraints.BOTH;
		gridBagConstraints.weightx=0.3;
		gridBagConstraints.weighty=0.9;
		gridBagConstraints.gridx=1;
		gridBagConstraints.gridy=2;
		UIPanel.add(fontSizePane,gridBagConstraints);
		// End of UIPanel.
		
		
		// UIPane ----------> Main Frame
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
		gridBagConstraints.weightx = 1;
		gridBagConstraints.weighty = 0.3;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		getContentPane().add(UIPanel, gridBagConstraints);
		//End of UIPanel
		
		// Setting for the StylePanel
		optionPanel = new JPanel();
		
		// Setting of Optional Panel
		m_boldCheck = new JCheckBox(DrawingPropertyResource.determineText(DrawingPropertyResource.getString("IDS_BOLD")));
		DrawingPropertyResource.setMnemonic(m_boldCheck, DrawingPropertyResource.getString("IDS_BOLD"));
                m_boldCheck.getAccessibleContext().setAccessibleDescription(DrawingPropertyResource.getString("ADDS_BOLD"));
		m_boldCheck.setSelected(false);
		m_boldCheck.addItemListener(this);
		gridBagConstraints= new GridBagConstraints();
		gridBagConstraints.fill=GridBagConstraints.BOTH;
		gridBagConstraints.weightx=1;
		gridBagConstraints.weighty=0.5;
		gridBagConstraints.gridx=0;
		gridBagConstraints.gridy=0;
		optionPanel.add(m_boldCheck,gridBagConstraints);
		
		m_italicCheck = new JCheckBox(DrawingPropertyResource.determineText(DrawingPropertyResource.getString("IDS_ITALIC")));
		DrawingPropertyResource.setMnemonic(m_italicCheck, DrawingPropertyResource.getString("IDS_ITALIC"));
                m_italicCheck.getAccessibleContext().setAccessibleDescription(DrawingPropertyResource.getString("ADDS_ITALIC"));
		m_italicCheck.setSelected(false);
		m_italicCheck.addItemListener(this);
		gridBagConstraints= new GridBagConstraints();
		gridBagConstraints.fill=GridBagConstraints.BOTH;
		gridBagConstraints.weightx=1;
		gridBagConstraints.weighty=0.5;
		gridBagConstraints.gridx=0;
		gridBagConstraints.gridy=1;
		optionPanel.add(m_italicCheck,gridBagConstraints);
		optionPanel.setBorder(new TitledBorder(DrawingPropertyResource.getString("IDS_STYLES")));
		//End of Option Panel.
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.2;
//		gridBagConstraints.weighty = 0.2;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		stylePanel.add(optionPanel,gridBagConstraints);
		
		samplePanel = new JPanel();
		samplePanel.setBorder(BorderFactory.createTitledBorder(DrawingPropertyResource.getString("IDS_SAMPLE")));
		samplePanel.setLayout(new BorderLayout());
		m_sampleField = new JLabel();
		m_sampleField.setText(m_sampleStr);
		samplePanel.add(m_sampleField);
		samplePanel.setSize(50, 50);
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.8;
//		gridBagConstraints.weighty = 0.8;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		stylePanel.add(samplePanel,gridBagConstraints);
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
		gridBagConstraints.weightx = 1;
		gridBagConstraints.weighty = 0.2;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		getContentPane().add(stylePanel, gridBagConstraints);
		//End of Style Panel
		
		// Setting for the Button Panel
                String okName = DrawingPropertyResource.getString("IDS_OK");
		m_ok = new JButton(okName);
                m_ok.getAccessibleContext().setAccessibleDescription(okName);
                getRootPane().setDefaultButton(m_ok);
                
                String cancelName = DrawingPropertyResource.getString("IDS_CANCEL");
		m_cancel = new JButton(cancelName);
                m_cancel.getAccessibleContext().setAccessibleDescription(cancelName);
                
		Box paneButton = Box.createHorizontalBox();
		paneButton.add(Box.createHorizontalGlue());
		paneButton.add(m_ok);
		
		m_ok.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					okPressed(e);
				}
			}
		);
		
		paneButton.add(Box.createHorizontalStrut(5));
		paneButton.add(Box.createHorizontalGlue());
		paneButton.add(m_cancel);
		
		m_cancel.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					cancelPressed(e);
				}
			}
		);
		
		buttonPanel.add(paneButton,BorderLayout.EAST);
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 7);
		gridBagConstraints.weightx = 1;
		gridBagConstraints.weighty = 0.1;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		getContentPane().add(buttonPanel, gridBagConstraints);
		//End of Button Panel.
		
		// Frame setting.
		
		setTitle(DrawingPropertyResource.getString("IDS_DIALOGTITLE"));
                getAccessibleContext().setAccessibleDescription(DrawingPropertyResource.getString("ADDS_DIALOGTITLE"));
		setSize(400,300);
	}
	public FontChooser()
	{
		init();
	}
	public FontChooser(Font defaultFont)
	{
		m_DefaultFont = defaultFont;
		init();
	}
	
	protected static int getFontStyle()
	{
		int style = Font.PLAIN;
		
		if (m_bItalic)
		{
			style |= Font.ITALIC;
		}
		if (m_bBold)
		{
			style |= Font.BOLD;
		}
		
		return style;
	}
	
	protected static void fontChanged()
	{
		int style = getFontStyle();
		m_font = new Font(m_fontName, style, m_size);
		m_sampleField.setFont(m_font);
	}
	
	protected void cancelPressed(ActionEvent e)
	{
		m_font = null;
		dispose();
	}
	
	protected void okPressed(ActionEvent e)
	{
		dispose();
	}

	public void itemStateChanged(ItemEvent e)
	{
		Object source = e.getItemSelectable();
		if (source == m_boldCheck)
		{
			if (m_boldCheck.isSelected()){
				m_bBold = true;
			}
			else{
				m_bBold = false;
			}
		}
		else if (source == m_italicCheck)
		{
			if (m_italicCheck.isSelected()){
				m_bItalic = true;
			}
			else{
				m_bItalic = false;
			}
		}

		fontChanged();
	}
	
	public static Font selectFont()
	{
//		Font font = null;
//		FontChooser pFontChooser = new FontChooser();
//		pFontChooser.setModal(true);
////		pFontChooser.show();
//                pFontChooser.setVisible(true);
//		font = pFontChooser.m_font;
//		return font;
            Frame mainWindow = WindowManager.getDefault().getMainWindow();
            return selectFont(null, mainWindow);
	}
	
	public static Font selectFont(Font defaultFont)
	{
//		Font font = null;
//		FontChooser pFontChooser = new FontChooser(defaultFont);
//		pFontChooser.m_font = defaultFont;
//		FontChooser.setDefaultFontValues(defaultFont);
//		pFontChooser.setModal(true);
////		pFontChooser.show();
//                pFontChooser.setVisible(true);
//		font = pFontChooser.m_font;
//		return font;
            Frame mainWindow = WindowManager.getDefault().getMainWindow();
            return selectFont(defaultFont, mainWindow);
	}
	public static Font selectFont(Font defaultFont, Component parent)
	{
		Font font = null;
                
                FontChooser pFontChooser = null;
                if(defaultFont != null)
                {
                    pFontChooser = new FontChooser(defaultFont);
                    pFontChooser.m_font = defaultFont;
                    FontChooser.setDefaultFontValues(defaultFont);
                }
                else
                {
                    pFontChooser = new FontChooser();
                }
                
                pFontChooser.setModal(true);
		if (parent != null)
		{
			pFontChooser.center(parent);
		}
//		pFontChooser.show();
                pFontChooser.setVisible(true);
		font = pFontChooser.m_font;
		return font;
	}
	
	private static void setDefaultFontValues(Font defaultFont)
	{
		m_fontName = defaultFont.getName();
		m_bItalic = defaultFont.isItalic();
		m_bBold = defaultFont.isBold();
		m_size = defaultFont.getSize();
		setCurrentFontName(m_fontName);
		setCurrentFontSize(m_size);
		setCurrentFontBold(m_bBold);
		setCurrentFontItalic(m_bItalic);
		fontChanged();
	}
	
	private static void setCurrentFontName(String curFontName)
	{
		if (curFontName != null && curFontName.length() > 0){
			m_NameList.setSelectedValue(curFontName, true);
		}
		else{
			m_NameList.setSelectedIndex(0);
		}
	}
	private static void setCurrentFontSize(int curFontSize)
	{
		Integer i = new Integer(curFontSize);
		if (i.intValue() > 0){
			m_SizeList.setSelectedValue(i.toString(), true);
		}
		else{
			m_SizeList.setSelectedIndex(0);
		}
	}
	private static void setCurrentFontBold(boolean bBold)
	{
		m_boldCheck.setSelected(bBold);
	}
	private static void setCurrentFontItalic(boolean bItalic)
	{
		m_italicCheck.setSelected(bItalic);
	}
}

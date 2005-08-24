/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.options.indentation;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.editor.BaseDocument;

import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;


/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
public final class IndentationPanel extends JPanel implements 
OptionsCategory.Panel, ChangeListener, ActionListener {

    private JCheckBox       cbExpandTabs = new JCheckBox ();
    private JCheckBox       cbAddStar = new JCheckBox ();
    private JCheckBox       cbNewLine = new JCheckBox ();
    private JCheckBox       cbSpace = new JCheckBox ();
    private JSpinner        tfStatementIndent = new JSpinner ();
    private JSpinner        tfIndent = new JSpinner ();
    private JEditorPane     epPreview = new JEditorPane ();
    private String          originalText;
	private boolean         originalExpandedTabs;
	private boolean         originalAddStar;
	private boolean         originalNewLine;
	private boolean         originalSpace;
	private int             originalStatementIndent;
	private int             originalIndent;

	
    public IndentationPanel () {
        // localization
        loc (cbExpandTabs, "Expand_Tabs");
        loc (cbAddStar, "Add_Leading_Star");
        loc (cbNewLine, "Add_New_Line");
        loc (cbSpace, "Add_Space");
        // save original values
		Model model = Model.getDefault ();
        originalExpandedTabs = model.isExpandTabs ();
        originalAddStar= model.getJavaFormatLeadingStarInComment ();
        originalNewLine = model.getJavaFormatNewlineBeforeBrace ();
        originalSpace = model.getJavaFormatSpaceBeforeParenthesis ();
        originalStatementIndent = model.
            getJavaFormatStatementContinuationIndent ();
        originalIndent = model.getSpacesPerTab ();
        // init components
        epPreview.setContentType ("text/x-java");
//        epPreview.setEditorKit (new JavaKit ());
        epPreview.setBorder (new EtchedBorder ());
		cbExpandTabs.setSelected (originalExpandedTabs);
        cbAddStar.setSelected (originalAddStar);
        cbNewLine.setSelected (originalNewLine);
        cbSpace.setSelected (originalSpace);
        tfIndent.setValue (new Integer (originalIndent));
        tfStatementIndent.setValue (new Integer (originalStatementIndent));
        //listeners
        cbNewLine.addActionListener (this);
        cbAddStar.addActionListener (this);
        cbExpandTabs.addActionListener (this);
        cbSpace.addActionListener (this);
        tfStatementIndent.addChangeListener (this);
        tfIndent.addChangeListener (this);
        epPreview.setEditable (false);
        // add text to preview
        InputStream is = getClass ().getResourceAsStream 
            ("/org/netbeans/modules/options/indentation/indentationExample");
        BufferedReader r = new BufferedReader (new InputStreamReader (is));
        StringBuffer sb = new StringBuffer ();
        try {
            String line = r.readLine ();
            while (line != null) {
                sb.append (line).append ('\n');
                line = r.readLine ();
            }
            originalText = new String (sb);
        } catch (IOException ex) {
            ex.printStackTrace ();
        }
        reformat ();

        
        FormLayout layout = new FormLayout (
            "p, 5dlu, 30dlu, 10dlu, p:g", // cols
            "p, 5dlu, p, 5dlu, p, 5dlu, p, 10dlu, p, 3dlu, f:p:g"
        );      // rows
        
        PanelBuilder builder = new PanelBuilder (layout, this);

        CellConstraints lc = new CellConstraints ();
        CellConstraints cc = new CellConstraints ();
//        builder.addSeparator (loc ("Properties"),  cc.xyw (1, 1, 7));
        
        builder.addLabel (    loc ("Statement_Indent"),  lc.xy  (1, 1),
                              tfStatementIndent,   cc.xy  (3, 1));
        builder.addLabel (    loc ("Indent"),      lc.xy  (1, 3),
                              tfIndent,            cc.xy  (3, 3));

        builder.add (         cbExpandTabs,        cc.xy  (5, 1));
        builder.add (         cbAddStar,           cc.xy  (5, 3));
        builder.add (         cbNewLine,           cc.xy  (5, 5));
        builder.add (         cbSpace,             cc.xy  (5, 7));
        
//        builder.addSeparator (loc ("Preview"),     cc.xyw (1, 11, 7));
        builder.addLabel (    loc ("Preview"),     lc.xyw (1, 9, 5),
                              epPreview,           cc.xyw (1, 11, 5, "f, f"));
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (IndentationPanel.class, key);
    }
    
    private static void loc (Component c, String key) {
        if (c instanceof AbstractButton)
            Mnemonics.setLocalizedText (
                (AbstractButton) c, 
                loc (key)
            );
        else
            Mnemonics.setLocalizedText (
                (JLabel) c, 
                loc (key)
            );
    }

    private void updatePreview () {
		Model model = Model.getDefault ();
        model.setJavaFormatLeadingStarInComment (cbAddStar.isSelected ());
        model.setJavaFormatNewlineBeforeBrace (cbNewLine.isSelected ());
        model.setJavaFormatSpaceBeforeParenthesis (cbSpace.isSelected ());
        model.setExpandTabs (cbExpandTabs.isSelected ());
        model.setJavaFormatStatementContinuationIndent (
            ((Integer) tfStatementIndent.getValue ()).intValue ()
        );
        model.setSpacesPerTab (
            ((Integer) (tfIndent.getValue ())).intValue ()
        );
        reformat ();
    }

    private void reformat () {
        epPreview.setText (originalText);
        BaseDocument doc = (BaseDocument) epPreview.getDocument ();
        try {
            doc.getFormatter ().reformat (
                doc, 
                0, 
                doc.getEndPosition ().getOffset ()
            );
        } catch (BadLocationException ex) {
            ex.printStackTrace ();
        }
    }
    
    
    // ActionListener ..........................................................
    
    public void stateChanged (ChangeEvent e) {
        updatePreview ();
    }
    
    public void actionPerformed (ActionEvent e) {
        updatePreview ();
    }
    
    
    // OptionsCategory.Panel ..................................................
    
    public void applyChanges () {
    }
    
    public void cancel () {
	Model model = Model.getDefault ();
        model.setJavaFormatLeadingStarInComment (originalAddStar);
        model.setJavaFormatNewlineBeforeBrace (originalNewLine);
        model.setJavaFormatSpaceBeforeParenthesis (originalSpace);
        model.setExpandTabs (originalExpandedTabs);
        model.setJavaFormatStatementContinuationIndent (originalStatementIndent);
        model.setSpacesPerTab (originalIndent);
    }
    
    public boolean isValid () {
        return true;
    }
    
    public boolean isChanged () {
        return true;
    }
}
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

package org.netbeans.modules.options.colors;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Coloring;
import org.netbeans.modules.editor.options.AllOptionsFolder;
import org.netbeans.modules.editor.options.BaseOptions;
import org.netbeans.spi.options.OptionsCategory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
public class FontAndColorsPanel extends JPanel implements 
ActionListener {
    
    private ColorModel		    colorModel = ColorModel.getDefault ();
    private JComboBox		    cbSchemes;
    private JButton		    bDelete;
    private JButton		    bClone;
    private JTabbedPane		    tabbedPane;
    
    private boolean		    listen = false;
    private SyntaxColoringPanel	    syntaxColoringPanel;
    private EditorPanel		    editorPanel;
    private AnnotationsPanel	    annotationsPanel;
    private String		    currentScheme;
    
 
    /** Creates new form FontAndColorsPanel */
    public FontAndColorsPanel () {
        currentScheme = colorModel.getCurrentScheme ();
        syntaxColoringPanel = new SyntaxColoringPanel (currentScheme);
        editorPanel = new EditorPanel (currentScheme);
        annotationsPanel = new AnnotationsPanel (currentScheme);
        
        cbSchemes = new JComboBox();
        cbSchemes.addItemListener (new ItemListener () {
            public void itemStateChanged (ItemEvent evt) {
                cbSchemesItemStateChanged (evt);
            }
        });

        JPanel pButtons = new JPanel (new GridLayout (1, 2));
        loc (bClone = new JButton (), "CTL_Create_New");
        bClone.addActionListener (this);
        pButtons.add (bClone);
        loc (bDelete = new JButton (), "CTL_Delete");
        bDelete.addActionListener (this);
        pButtons.add (bDelete);

        tabbedPane = new JTabbedPane ();

        FormLayout layout = new FormLayout (
            "5dlu, p, 3dlu, p:g, 5dlu, p", // cols
            "p, 3dlu, p, 5dlu, f:p:g");      // rows

        PanelBuilder builder = new PanelBuilder (layout, this);

        CellConstraints cc = new CellConstraints ();
        CellConstraints lc = new CellConstraints ();

        builder.addSeparator ( loc ("CTL_Color_Scheme"),      cc.xyw (1, 1, 6));
        builder.addLabel (     loc ("CTL_Color_Scheme_Name"), lc.xy (2, 3), 
                               cbSchemes,                     cc.xy (4, 3));
        builder.add (          pButtons,                      cc.xy (6, 3, "l,d"));
	
        builder.add (          tabbedPane,                    cc.xyw (1, 5, 6));

	tabbedPane.addTab (loc ("Syntax_coloring_tab"), syntaxColoringPanel);
	tabbedPane.addTab (loc ("Editor_tab"), editorPanel);
	tabbedPane.addTab (loc ("Annotations_tab"), annotationsPanel);
        
        // init schemes
        listen = false;
        Iterator it = colorModel.getSchemeNames ().iterator ();
        while (it.hasNext ())
            cbSchemes.addItem (it.next ());
        listen = true;
        cbSchemes.setSelectedItem (currentScheme);
    }

    private void cbSchemesItemStateChanged (ItemEvent evt) {
        if (!listen) return;
        setCurrentScheme ((String) cbSchemes.getSelectedItem ());
    }
    
    private void setCurrentScheme (String scheme) {
        currentScheme = scheme;
        editorPanel.setCurrentScheme (currentScheme);
        syntaxColoringPanel.setCurrentScheme (currentScheme);
        annotationsPanel.setCurrentScheme (currentScheme);
    }
     
    
    // other methods ...........................................................
    
    void applyChanges () {
        editorPanel.applyChanges ();
        syntaxColoringPanel.applyChanges ();
        annotationsPanel.applyChanges ();
        colorModel.setCurrentScheme (currentScheme);
    }
    
    void cancel () {
    }
    
    boolean dataValid () {
        return true;
    }
    
    boolean isChanged () {
        return true;
    }
   
    public void actionPerformed (ActionEvent e) {
        if (!listen) return;
        if (e.getSource () == bClone) {
            NotifyDescriptor.InputLine il = new NotifyDescriptor.InputLine (
                loc ("CTL_Create_New_Scheme_Message"),
                loc ("CTL_Create_New_Scheme_Title")
            );
            il.setInputText (currentScheme);
            DialogDisplayer.getDefault ().notify (il);
            if (il.getValue () == NotifyDescriptor.OK_OPTION) {
                setCurrentScheme (il.getInputText ());
                listen = false;
                cbSchemes.addItem (il.getInputText ());
                cbSchemes.setSelectedItem (il.getInputText ());
                listen = true;
            }
            return;
        }
        if (e.getSource () == bDelete) {
            return;
        }
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (FontAndColorsPanel.class, key);
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
}

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

package org.netbeans.modules.options;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.AWTKeyStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sound.sampled.Line;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.spi.options.OptionsCategory;
import org.openide.awt.Mnemonics;
import org.openide.awt.ToolbarButton;
import org.openide.awt.ToolbarToggleButton;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderLookup;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import org.openide.util.Utilities;
import org.netbeans.modules.options.ui.ShadowBorder;


/**
 *
 * @author  Administrator
 */
public class OptionsPanel extends JPanel {
    
    //private JList                   jList;
    private JPanel                  pCategories;
    private JPanel                  pOptions;
    private JScrollPane             jScrollPane1;
    private JLabel                  lTitle;
    private JComponent              currentComponent;
    // list of OptionsCategories.
    private List                    optionPanels;
    private int                     currentCategory = -1;
    private Button[]                buttons;
    private Map                     categoryToPanel = new HashMap ();
        
    private Color selected = new Color (193, 210, 238);
    private Color selectedB = new Color (149, 106, 197);
    private Color highlighted = new Color (224, 232, 246);
    private Color highlightedB = new Color (152, 180, 226);
    private Color iconViewBorder = new Color (127, 157, 185);
    
    private static String loc (String key) {
        return NbBundle.getMessage (OptionsPanel.class, key);
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

    
    /** Creates new form OptionsPanel */
    public OptionsPanel () {
        // 1) Load panels.
        FileObject fo = Repository.getDefault ().getDefaultFileSystem ().
            findResource ("OptionsDialog");
        if (fo != null) {
            Lookup lookup = new FolderLookup (DataFolder.findFolder (fo)).
                getLookup ();
            optionPanels = new ArrayList (lookup.lookup (
                new Lookup.Template (OptionsCategory.class)
            ).allInstances ());
        }

        pOptions = new JPanel ();
        pOptions.setLayout (new BorderLayout ());
        
        // size of options panel shoud be max of all nested panels
        int maxW = 0, maxH = 0;
        int i, k = optionPanels.size ();
        for (i = 0; i < k; i++) {
            OptionsCategory ocp = (OptionsCategory) optionPanels.
                get (i);
            JComponent c = ocp.getPane ();
            categoryToPanel.put (ocp, c);
            maxW = Math.max (maxW, c.getPreferredSize ().width);
            maxH = Math.max (maxH, c.getPreferredSize ().height);
            //S ystem.out.println (ocp.getCategoryName () + " : " + c.getPreferredSize ());
        }
        pOptions.setPreferredSize (new Dimension (maxW, maxH));

        // title bar
        JPanel pTitle = new JPanel (new BorderLayout ());
        lTitle = new JLabel ();
        lTitle.setBackground (new Color (0, 0, 200));
        lTitle.setForeground (Color.white);
        Font f = lTitle.getFont ();
        lTitle.setFont (new Font (f.getName (), Font.BOLD, 16));
        lTitle.setIconTextGap (8);
        lTitle.setOpaque (true);
        lTitle.setBorder (new EmptyBorder (2, 2, 2, 2));
        pTitle.setBorder (new BevelBorder (BevelBorder.LOWERED));
        pTitle.add ("Center", lTitle);

        // icon view
        pCategories = new JPanel (new BorderLayout ());
        JPanel pCategories2 = new JPanel (new GridLayout (
            optionPanels.size (),
            1, 0, 0
        ));
        pCategories.add ("North", pCategories2);
        pCategories.setBorder (new LineBorder (iconViewBorder)); 
        pCategories.setBackground (Color.white);
        pCategories2.setBackground (Color.white);
        pCategories2.setBorder (null);
        k = optionPanels.size ();
        buttons = new Button [k];
        for (i = 0; i < k; i++) {
            final OptionsCategory ocp = (OptionsCategory) 
                optionPanels.get (i);
            Button b = new Button (ocp, i);
            buttons [i] = b;
            pCategories2.add (b);
        }
        
        // layout
        FormLayout layout = new FormLayout(
            "p, 5dlu, p:g", // cols
            "p, 5dlu, p:g");      // rows
        PanelBuilder builder = new PanelBuilder (layout, this);
        builder.setDefaultDialogBorder ();
        CellConstraints cc = new CellConstraints ();
        builder.add (    pCategories, cc.xywh  (1, 1, 1, 3));
        builder.add (    pTitle,       cc.xy    (3, 1));
        builder.add (    pOptions,     cc.xy    (3, 3, "f,f"));
        
        if (k > 0)
            setCurrentIndex (0);
    }
    
    int getCurrentIndex () {
        return currentCategory;
    }
    
    void setCurrentIndex (int i) {
        if (currentCategory != -1)
            buttons [currentCategory].setNormal ();
        if (i != -1)
            buttons [i].setSelected ();
        currentCategory = i;
        OptionsCategory ocp = (OptionsCategory) 
            optionPanels.get (i);
        if (currentComponent != null)
            pOptions.remove (currentComponent);
        pOptions.add (
            "Center",
            currentComponent = (JComponent) categoryToPanel.get (ocp)
        );
        lTitle.setIcon (new ImageIcon (Utilities.loadImage (ocp.getIcon () + ".png")));
        lTitle.setText (ocp.getTitle ());
        invalidate ();
        validate ();
        repaint ();
    }
    
    void save () {
        ((OptionsCategory.Panel) currentComponent).applyChanges ();
    }
    
    void cancel () {
        ((OptionsCategory.Panel) currentComponent).cancel ();
    }
    
    
    // innerclasses ............................................................
    
    class Button extends JLabel implements MouseListener {
        
        private int index;
        
        Button (OptionsCategory ocp, int index) {
            super (
                new ImageIcon (Utilities.loadImage (ocp.getIcon () + ".png"))
            );
            this.index = index;
            Mnemonics.setLocalizedText (this, ocp.getCategoryName ());
            setOpaque (true);
            setVerticalTextPosition (BOTTOM);
            setHorizontalTextPosition (CENTER);
            setHorizontalAlignment (CENTER);
            addMouseListener (this);
            setFocusable (true);
            setFocusTraversalKeysEnabled (true);
            Set s = new HashSet ();
            s.add (AWTKeyStroke.getAWTKeyStroke (KeyEvent.VK_DOWN, 0));
            setFocusTraversalKeys (
                KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                s
            );
            s = new HashSet ();
            s.add (AWTKeyStroke.getAWTKeyStroke (KeyEvent.VK_UP, 0));
            setFocusTraversalKeys (
                KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                s
            );
            setForeground (Color.black);
            if (index == currentCategory)
                setSelected ();
            else
                setNormal ();
        }
        
        void setNormal () {
            setBorder (new EmptyBorder (2, 4, 2, 4));
            setBackground (Color.white);
        }
        
        void setSelected () {
            setBorder (new CompoundBorder (
                new CompoundBorder (
                    new LineBorder (Color.white),
                    new LineBorder (selectedB)
                ),
                new EmptyBorder (0, 2, 0, 2)
            ));
            setBackground (selected);
        }
        
        void setHighlighted () {
            setBorder (new CompoundBorder (
                new CompoundBorder (
                    new LineBorder (Color.white),
                    new LineBorder (highlightedB)
                ),
                new EmptyBorder (0, 2, 0, 2)
            ));
            setBackground (highlighted);
        }
        
        public void mouseClicked (MouseEvent e) {
            setCurrentIndex (index);
            setSelected ();
        }

        public void mousePressed (MouseEvent e) {
            setSelected ();
        }

        public void mouseReleased (MouseEvent e) {
        }

        public void mouseEntered (MouseEvent e) {
            if (index != currentCategory)
                setHighlighted ();
        }

        public void mouseExited (MouseEvent e) {
            if (index != currentCategory)
                setNormal ();
        }
    }
}

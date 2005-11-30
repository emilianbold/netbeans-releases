/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with thie License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.KeyboardFocusManager;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.FocusManager;

import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import org.netbeans.modules.options.ui.LoweredBorder;
import org.netbeans.modules.options.ui.VariableBorder;
import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.ErrorManager;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderLookup;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import org.openide.util.Utilities;
import org.openide.util.lookup.ProxyLookup;

public class OptionsPanel extends JPanel {
    
    private JPanel                  pCategories;
    private JPanel                  pOptions;
    private JLabel                  lTitle;
    //                              List (OptionsCategory)
    private List                    optionCategories;
    private int                     currentCategory = -1;
    private Button[]                buttons;
    private Map                     categoryToPanel = new HashMap ();
    private Map                     categoryToController = new HashMap ();
    private Set                     updatedCategories = new HashSet ();
        
    private Color                   selected = new Color (193, 210, 238);
    private Color                   selectedB = new Color (149, 106, 197);
    private Color                   highlighted = new Color (224, 232, 246);
    private Color                   highlightedB = new Color (152, 180, 226);
    private Color                   iconViewBorder = new Color (127, 157, 185);
    private ControllerListener      coltrollerListener = new ControllerListener ();
    
    private final boolean           isMac = UIManager.getLookAndFeel().getID().equals("Aqua");
    private final Color             selectedMac = new Color(221, 221, 221);
    private final Color             selectedBMac = new Color(183, 183, 183);
    private final Color             borderMac = new Color(141, 141, 141);
    private final Font              labelFontMac = new Font("Lucida Grande", 0, 10);    
    
    
    private static String loc (String key) {
        return NbBundle.getMessage (OptionsPanel.class, key);
    }

    
    /** Creates new form OptionsPanel */
    public OptionsPanel () {

        // 1) Load panels.
        FileObject fo = Repository.getDefault ().getDefaultFileSystem ().
            findResource ("OptionsDialog");
        if (fo != null) {
            Lookup lookup = new FolderLookup (DataFolder.findFolder (fo)).
                getLookup ();
            optionCategories = new ArrayList (lookup.lookup (
                new Lookup.Template (OptionsCategory.class)
            ).allInstances ());
        }

        pOptions = new JPanel ();
        pOptions.setLayout (new BorderLayout ());
        
        // size of options panel shoud be max of all nested panels
        int maxW = 0, maxH = 0;
        List lookups = new ArrayList ();
        int i, k = optionCategories.size ();
        for (i = 0; i < k; i++) {
            OptionsCategory category = (OptionsCategory) optionCategories.
                get (i);
            OptionsPanelController controller = category.create ();
            lookups.add (controller.getLookup ());
            categoryToController.put (category, controller);
            controller.addPropertyChangeListener (coltrollerListener);
        }
        Lookup masterLookup = new ProxyLookup 
            ((Lookup[]) lookups.toArray (new Lookup [lookups.size ()]));
        k = optionCategories.size ();
        for (i = 0; i < k; i++) {
            OptionsCategory category = (OptionsCategory) optionCategories.
                get (i);
            OptionsPanelController controller = (OptionsPanelController) 
                categoryToController.get (category);
            JComponent component = controller.getComponent (masterLookup);
            categoryToPanel.put (category, component);
            maxW = Math.max (maxW, component.getPreferredSize ().width);
            maxH = Math.max (maxH, component.getPreferredSize ().height);
            //S ystem.out.println (category.getCategoryName () + " : " + component.getPreferredSize ());
        }
        pOptions.setPreferredSize (new Dimension (maxW, maxH));

        // title bar
        JPanel pTitle = new JPanel (new BorderLayout ());
        lTitle = new JLabel ();
        if (Utilities.isWindows ()) {
            lTitle.setBackground (SystemColor.activeCaption);
            lTitle.setForeground (SystemColor.activeCaptionText);
        } else {
            lTitle.setBackground (Color.white);
            lTitle.setForeground (Color.black);
        }
        Font f = lTitle.getFont ();
        lTitle.setFont (new Font (f.getName (), Font.BOLD, 16));
        lTitle.setIconTextGap (8);
        lTitle.setOpaque (true);
        if (Utilities.isWindows ()) {
            pTitle.setBorder (new CompoundBorder (
                new LoweredBorder (),
                new LineBorder (SystemColor.activeCaption, 1)
            ));
        } else {
            pTitle.setBorder (new CompoundBorder (
                new LineBorder (iconViewBorder, 1),
                new LineBorder (Color.white, 2)
            ));
        }
        pTitle.add ("Center", lTitle);

        // icon view
        pCategories = new JPanel (new BorderLayout ());
        JPanel pCategories2 = new JPanel (new GridBagLayout());
        pCategories.add ("North", pCategories2);
        if(isMac) {
            pCategories.setBorder(new CompoundBorder (
                    new VariableBorder(null, null, borderMac, null),
                    BorderFactory.createEmptyBorder(0, 4, 0, 4)
                ));
        } else {
            pCategories.setBorder(new LineBorder (iconViewBorder));
        }
        pCategories.setBackground (Color.white);
        pCategories2.setBackground (Color.white);
        pCategories2.setBorder (null);
        
        // add buttons
        k = optionCategories.size ();
        buttons = new Button [k];
        InputMap inputMap = getInputMap 
            (JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put (
            isMac ? KeyStroke.getKeyStroke (KeyEvent.VK_LEFT, 0) : 
                    KeyStroke.getKeyStroke (KeyEvent.VK_UP, 0), 
            "UP"
        );
        getActionMap ().put ("UP", new UpAction ());
        inputMap.put (
            KeyStroke.getKeyStroke (KeyEvent.VK_SPACE, 0), 
            "SPACE"
        );
        getActionMap ().put ("SPACE", new SelectCurrentAction ());
        inputMap.put (
            isMac ? KeyStroke.getKeyStroke (KeyEvent.VK_RIGHT, 0) :
                    KeyStroke.getKeyStroke (KeyEvent.VK_DOWN, 0), 
            "DOWN"
        );
        getActionMap ().put ("DOWN", new DownAction ());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        if (isMac) {
            gbc.fill = GridBagConstraints.VERTICAL;
            gbc.weightx = 0.0;
            gbc.weighty = 1.0;
        } else {
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            gbc.weighty = 0.0;
        }
        
        for (i = 0; i < k; i++) {
            final OptionsCategory category = (OptionsCategory) 
                optionCategories.get (i);
            Button b = new Button (category, i);
            buttons [i] = b;
            int mnemonic = b.getDisplayedMnemonic ();
            KeyStroke keyStroke = KeyStroke.getKeyStroke 
                (mnemonic, KeyEvent.ALT_MASK);
            inputMap.put (keyStroke, b);
            getActionMap ().put (b, new SelectAction (i));
            
            if (isMac) {
                gbc.gridx = i;
                gbc.gridy = 0;
            } else {
                gbc.gridx = 0;
                gbc.gridy = i;
            }
            
            pCategories2.add (b, gbc);
        }
        
        /* i don't know a better workaround */
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        if (isMac) {
            gbc.gridx = gbc.gridx + 1;
            gbc.gridy = 0;
        } else {
            gbc.gridx = 0;
            gbc.gridy = gbc.gridy + 1;
        }
        pCategories2.add (new javax.swing.JLabel(""), gbc);
        
        
        // layout
        FormLayout layout;
        if (isMac) {
            layout = new FormLayout(
                "p:g", // cols
                "p, 5dlu, p:g");      // rows
        } else {
            layout = new FormLayout(
                "p, 5dlu, p:g", // cols
                "p, 5dlu, p:g");      // rows
        }
        PanelBuilder builder = new PanelBuilder (layout, this);
        if (isMac) {
            pOptions.setBorder(new CompoundBorder (
                    new VariableBorder(null, null, borderMac, null),
                    BorderFactory.createEmptyBorder(0, 20, 5, 20)
                    ));
        } else {
            builder.setDefaultDialogBorder ();
        }
        CellConstraints cc = new CellConstraints ();
        if (isMac) {
            builder.add (    pCategories, cc.xy  (1, 1));
            builder.add (    pOptions,     cc.xy    (1, 3, "f,f"));
        } else {
            builder.add (    pCategories, cc.xywh  (1, 1, 1, 3));
            builder.add (    pTitle,       cc.xy    (3, 1));
            builder.add (    pOptions,     cc.xy    (3, 3, "f,f"));
        }
        
        if (k < 1) return;
        OptionsCategory category = (OptionsCategory) optionCategories.get (0);
        OptionsPanelController controller = (OptionsPanelController) 
            categoryToController.get (category);
        try {
            controller.update ();
            updatedCategories.add (category);
        } catch (Throwable t) {
            ErrorManager.getDefault ().notify (t);
        }
        setCurrentIndex (0);
        RequestProcessor.getDefault ().post (new Runnable () {
            public void run () {
                Iterator it = optionCategories.iterator ();
                it.next ();
                int i = 1;
                while (it.hasNext ())
                    try {
                        OptionsCategory category = (OptionsCategory) it.next ();
                        ((OptionsPanelController) categoryToController.get (category)).
                            update ();
                        updatedCategories.add (category);
                        if (getCurrentIndex () == i)
                            setCurrentIndex (i);
                        i++;
                    } catch (Throwable t) {
                        ErrorManager.getDefault ().notify (t);
                    }
            }
        });
    }
    
    int getCurrentIndex () {
        return currentCategory;
    }
    
    void setCurrentIndex (final int i) {
        if (currentCategory != -1)
            buttons [currentCategory].setNormal ();
        if (i != -1)
            buttons [i].setSelected ();
        currentCategory = i;
        OptionsCategory category = (OptionsCategory) 
            optionCategories.get (i);
        pOptions.removeAll ();
        if (updatedCategories.contains (category)) {
            JComponent component = (JComponent) categoryToPanel.get (category);
            pOptions.add (
                "Center",
                component
            );
            
        } else {
            JLabel label = new JLabel (loc ("CTL_Loading_Options"));
            label.setHorizontalAlignment (label.CENTER);
            pOptions.add ("Center", label);
        }
        lTitle.setIcon (new ImageIcon (Utilities.loadImage (category.getIconBase () + ".png")));
        lTitle.setText (category.getTitle ());
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                invalidate ();
                validate ();
                repaint ();
                if (i != -1)
                    buttons [i].requestFocus ();
            }
        });
        firePropertyChange ("buran" + OptionsPanelController.PROP_HELP_CTX, null, null);
    }
    
    HelpCtx getHelpCtx () {
        OptionsCategory category = (OptionsCategory) 
            optionCategories.get (getCurrentIndex ());
        OptionsPanelController controller = (OptionsPanelController) categoryToController.
            get (category);
        return controller.getHelpCtx ();
    }
    
    void update () {
        Iterator it = categoryToController.values ().iterator ();
        while (it.hasNext ())
            try {
                ((OptionsPanelController) it.next ()).update ();
            } catch (Throwable t) {
                ErrorManager.getDefault ().notify (t);
            }
    }
    
    void save () {
        Iterator it = categoryToController.values ().iterator ();
        while (it.hasNext ())
            ((OptionsPanelController) it.next ()).applyChanges ();
    }
    
    void cancel () {
        Iterator it = categoryToController.values ().iterator ();
        while (it.hasNext ())
            ((OptionsPanelController) it.next ()).cancel ();
    }
    
    boolean dataValid () {
        Iterator it = categoryToController.values ().iterator ();
        while (it.hasNext ())
            if (!((OptionsPanelController) it.next ()).isValid ()) return false;
        return true;
    }
    
    boolean isChanged () {
        Iterator it = categoryToController.values ().iterator ();
        while (it.hasNext ())
            if (((OptionsPanelController) it.next ()).isChanged ()) return true;
        return false;
    }
    
    
    // innerclasses ............................................................
    
    private class SelectAction extends AbstractAction {
        private int index;
        
        SelectAction (int index) {
            this.index = index;
        }
        public void actionPerformed (ActionEvent e) {
            setCurrentIndex (index);
        }
    }
    
    private class SelectCurrentAction extends AbstractAction {
        
        public void actionPerformed (ActionEvent e) {
            Component c = FocusManager.getCurrentManager ().getFocusOwner ();
            if (c instanceof Button) {
                setCurrentIndex (((Button) c).index);
                ((Button) c).setSelected ();
            }
        }
    }
    
    private class UpAction extends AbstractAction {

        public void actionPerformed (ActionEvent e) {
            int i = getCurrentIndex ();
            if (i > 0)
                setCurrentIndex (i - 1);
            else
                setCurrentIndex (optionCategories.size () - 1);
        }
    }
    
    private class DownAction extends AbstractAction {

        public void actionPerformed (ActionEvent e) {
            int i = getCurrentIndex ();
            if (i < (optionCategories.size () - 1))
                setCurrentIndex (i + 1);
            else
                setCurrentIndex (0);
        }
    }
    
    class ControllerListener implements PropertyChangeListener {
        public void propertyChange (PropertyChangeEvent evt) {
            OptionsPanel.this.firePropertyChange 
                ("buran" + evt.getPropertyName (), null, null);
        }
    }
    
    class Button extends JLabel implements MouseListener {
        
        private int index;
        
        Button (OptionsCategory category, int index) {
            super (
                new ImageIcon (Utilities.loadImage (category.getIconBase () + ".png"))
            );
            this.index = index;
            Mnemonics.setLocalizedText (this, category.getCategoryName ());
            setOpaque (true);
            setVerticalTextPosition (BOTTOM);
            setHorizontalTextPosition (CENTER);
            setHorizontalAlignment (CENTER);
            addMouseListener (this);
            setFocusable (true);
            setFocusTraversalKeysEnabled (true);
            setForeground (Color.black);
            
            if (isMac) {
                setFont(labelFontMac);
                setIconTextGap(2);
            }
            
            if (index == currentCategory)
                setSelected ();
            else
                setNormal ();
            addFocusListener (new FocusListener () {
                public void focusGained (FocusEvent e) {
                    if (Button.this.index != currentCategory)
                        setHighlighted ();
                }
                public void focusLost (FocusEvent e) {
                    if (Button.this.index != currentCategory)
                        setNormal ();
                }
            });
        }
        
        void setNormal () {
            if (isMac) {
                setBorder (new EmptyBorder (5, 6, 3, 6));
            } else {
                setBorder (new EmptyBorder (2, 4, 2, 4));
            }
            setBackground (Color.white);
        }
        
        void setSelected () {
            if (isMac) {
                setBorder(new CompoundBorder (
                        new VariableBorder(null, selectedBMac, null, selectedBMac),
                        BorderFactory.createEmptyBorder(5, 5, 3, 5)
                        ));
                setBackground (selectedMac);
            } else {
                setBorder (new CompoundBorder (
                    new CompoundBorder (
                        new LineBorder (Color.white),
                        new LineBorder (selectedB)
                    ),
                    new EmptyBorder (0, 2, 0, 2)
                ));
                setBackground (selected);
            }
        }
        
        void setHighlighted () {
            if (isMac) {
                setBorder(new CompoundBorder (
                        new VariableBorder(null, selectedBMac, null, selectedBMac),
                        BorderFactory.createEmptyBorder(5, 5, 3, 5)
                        ));
                setBackground (selectedMac);
            } else {
                setBorder (new CompoundBorder (
                    new CompoundBorder (
                        new LineBorder (Color.white),
                        new LineBorder (highlightedB)
                    ),
                    new EmptyBorder (0, 2, 0, 2)
                ));
                setBackground (highlighted);
            }
        }
        
        public void mouseClicked (MouseEvent e) {
            setCurrentIndex (index);
//            setSelected ();
        }

        public void mousePressed (MouseEvent e) {
            if (!isMac)
                setSelected ();
        }

        public void mouseReleased (MouseEvent e) {
        }

        public void mouseEntered (MouseEvent e) {
            if (index != currentCategory && !isMac)
                setHighlighted ();
        }

        public void mouseExited (MouseEvent e) {
            if (index != currentCategory && !isMac)
                setNormal ();
        }
    }
}

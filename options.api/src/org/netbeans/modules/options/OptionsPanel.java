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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.KeyboardFocusManager;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
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

import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import org.netbeans.modules.options.ui.LoweredBorder;
import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.options.OptionsCategory.PanelController;
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
            PanelController controller = category.create ();
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
            PanelController controller = (PanelController) 
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
        JPanel pCategories2 = new JPanel (new GridLayout (
            optionCategories.size (),
            1, 0, 0
        ));
        pCategories.add ("North", pCategories2);
        pCategories.setBorder (new LineBorder (iconViewBorder)); 
        pCategories.setBackground (Color.white);
        pCategories2.setBackground (Color.white);
        pCategories2.setBorder (null);
        
        // add buttons
        k = optionCategories.size ();
        buttons = new Button [k];
        InputMap inputMap = getInputMap 
            (JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put (
            KeyStroke.getKeyStroke (KeyEvent.VK_UP, 0), 
            "UP"
        );
        getActionMap ().put ("UP", new UpAction ());
        inputMap.put (
            KeyStroke.getKeyStroke (KeyEvent.VK_DOWN, 0), 
            "DOWN"
        );
        getActionMap ().put ("DOWN", new DownAction ());
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
        
        if (k < 1) return;
        OptionsCategory category = (OptionsCategory) optionCategories.get (0);
        PanelController controller = (PanelController) 
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
                        ((PanelController) categoryToController.get (category)).
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
    
    void setCurrentIndex (int i) {
        if (currentCategory != -1)
            buttons [currentCategory].setNormal ();
        if (i != -1)
            buttons [i].setSelected ();
        currentCategory = i;
        OptionsCategory category = (OptionsCategory) 
            optionCategories.get (i);
        pOptions.removeAll ();
        if (updatedCategories.contains (category))
            pOptions.add (
                "Center",
                (JComponent) categoryToPanel.get (category)
            );
        else {
            JLabel label = new JLabel (loc ("CTL_Loading_Options"));
            label.setHorizontalAlignment (label.CENTER);
            pOptions.add ("Center", label);
        }
        lTitle.setIcon (new ImageIcon (Utilities.loadImage (category.getIconBase () + ".png")));
        lTitle.setText (category.getTitle ());
        invalidate ();
        validate ();
        repaint ();
        firePropertyChange ("buran" + PanelController.PROP_HELP_CTX, null, null);
    }
    
    HelpCtx getHelpCtx () {
        OptionsCategory category = (OptionsCategory) 
            optionCategories.get (getCurrentIndex ());
        PanelController controller = (PanelController) categoryToController.
            get (category);
        return controller.getHelpCtx ();
    }
    
    void update () {
        Iterator it = categoryToController.values ().iterator ();
        while (it.hasNext ())
            try {
                ((PanelController) it.next ()).update ();
            } catch (Throwable t) {
                ErrorManager.getDefault ().notify (t);
            }
    }
    
    void save () {
        Iterator it = categoryToController.values ().iterator ();
        while (it.hasNext ())
            ((PanelController) it.next ()).applyChanges ();
    }
    
    void cancel () {
        Iterator it = categoryToController.values ().iterator ();
        while (it.hasNext ())
            ((PanelController) it.next ()).cancel ();
    }
    
    boolean dataValid () {
        Iterator it = categoryToController.values ().iterator ();
        while (it.hasNext ())
            if (!((PanelController) it.next ()).isValid ()) return false;
        return true;
    }
    
    boolean isChanged () {
//        System.out.println("\nChanged panels: ");
//        Iterator it = categoryToController.values ().iterator ();
//        while (it.hasNext ()) {
//            PanelController p = (PanelController) it.next ();
//            if (p.isChanged ()) System.out.println("  " + p);
//        }
        Iterator it = categoryToController.values ().iterator ();
        while (it.hasNext ())
            if (((PanelController) it.next ()).isChanged ()) return true;
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

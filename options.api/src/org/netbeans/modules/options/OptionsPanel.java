/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.options;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.SystemColor;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.FocusManager;
import javax.swing.Icon;

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
import org.netbeans.modules.options.advanced.Advanced;
import org.netbeans.modules.options.ui.LoweredBorder;
import org.netbeans.modules.options.ui.VariableBorder;
import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderLookup;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import org.openide.util.Utilities;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.WindowManager;

public class OptionsPanel extends JPanel {
    
    private JPanel pCategories;
    private JPanel pCategories2;
    private JPanel pOptions;
    private JLabel lTitle;
    //                              List (OptionsCategory)
    private List optionCategories = Collections.EMPTY_LIST;
    private int currentCategory = -1;
    private int highlightedCategory = -1;    
    //                              List (Button)
    private List buttons = new ArrayList ();
    private Map categoryToPanel = new HashMap ();
    private Map categoryToController = new HashMap ();
    private Set updatedCategories = new HashSet ();
    
    private final boolean isMac = UIManager.getLookAndFeel ().getID ().equals ("Aqua");    
    private Color selected = isMac ? new Color(221, 221, 221) : new Color (193, 210, 238);
    private Color selectedB = isMac ? new Color(183, 183, 183) : new Color (149, 106, 197);
    private Color highlighted = isMac ? new Color(221, 221, 221) : new Color (224, 232, 246);
    private Color highlightedB = new Color (152, 180, 226);
    private Color iconViewBorder = new Color (127, 157, 185);
    private ControllerListener coltrollerListener = new ControllerListener ();
    
    private final Color borderMac = new Color(141, 141, 141);
    private final Font labelFontMac = new Font("Lucida Grande", 0, 10);    
    
    
    
    private static String loc (String key) {
        return NbBundle.getMessage (OptionsPanel.class, key);
    }

    
    /** Creates new form OptionsPanel */
    public OptionsPanel () {
        
        // 0) change cursor
        final Frame frame = WindowManager.getDefault ().getMainWindow ();
        final Cursor cursor = frame.getCursor ();
        frame.setCursor (Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR));
        setCursor (Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR));

        // 1) init UI components, layout and actions, and add some default values
        initUI ();
        
        RequestProcessor.getDefault().post(new Runnable() {

                                               public void run() {
                                                   SwingUtilities.invokeLater(new Runnable() {

                                                                                  public void run() {
                                                                                      // 2) Load OptionsCategory instances from layers
                                                                                      optionCategories = loadOptionsCategories();
                                                                                      // 4) init OptionsPanelControllers
                                                                                      // inits categoryToController
                                                                                      initControllers();
                                                                                      // 5) create master lookup
                                                                                      Lookup masterLookup = createMasterLookup();
                                                                                      // 6) init option panels & categoryToPanel map
                                                                                      Dimension maxSize = initPanels(masterLookup);
                                                                                      int i;
                                                                                      int k = optionCategories.size();

                                                                                      for (i = 0; i <
                                                                                                  k; i++)
                                                                                          try {
                                                                                              OptionsCategory category = (OptionsCategory) optionCategories.get(i);
                                                                                              OptionsPanelController controller = (OptionsPanelController) categoryToController.get(category);

                                                                                              updatedCategories.add(category);
                                                                                              if (controller ==
                                                                                                  null)
                                                                                                  continue;
                                                                                              controller.update();
                                                                                          }
                                                                                          catch (Throwable t) {
                                                                                              Exceptions.printStackTrace(t);
                                                                                          }
                                                                                      // paint
                                                                                      refreshButtons();
                                                                                      checkSize(maxSize);
                                                                                      int index = getCurrentIndex();

                                                                                      if (index <
                                                                                          0)
                                                                                          index = 0;
                                                                                      setCurrentIndex(index);
                                                                                      // 7) reset cursor
                                                                                      frame.setCursor(cursor);
                                                                                      setCursor(cursor);
                                                                                  }
                                                                              });
                                               }
                                           }, 250);
    }
    
    int getCurrentIndex () {
        return currentCategory;
    }
    
    void setCurrentIndex (final int i) {
        if (currentCategory != -1)
            ((Button) buttons.get (currentCategory)).setNormal ();
        if (i != -1)
            ((Button) buttons.get (i)).setSelected ();
        currentCategory = i;
        if (i >= optionCategories.size ()) {
            switch (i) {
            case 0:
                lTitle.setIcon (new ImageIcon (Utilities.loadImage ("org/netbeans/modules/options/resources/generalOptions.png")));
                lTitle.setText (NbBundle.getMessage (OptionsPanel.class, "CTL_General_Options_Title"));
                break;
            case 1:
                lTitle.setIcon (new ImageIcon (Utilities.loadImage ("org/netbeans/modules/options/resources/editor.png")));
                lTitle.setText (NbBundle.getMessage (OptionsPanel.class, "CTL_Editor_Title"));
                break;
            case 2:
                lTitle.setIcon (new ImageIcon (Utilities.loadImage ("org/netbeans/modules/options/resources/colors.png")));
                lTitle.setText (NbBundle.getMessage (OptionsPanel.class, "CTL_Font_And_Color_Options_Title"));
                break;
            case 3:
                lTitle.setIcon (new ImageIcon (Utilities.loadImage ("org/netbeans/modules/options/resources/keymap.png")));
                lTitle.setText (NbBundle.getMessage (OptionsPanel.class, "CTL_Keymap_Options_Title"));
                break;
            case 4:
                lTitle.setIcon (new ImageIcon (Utilities.loadImage ("org/netbeans/modules/options/resources/advanced.png")));
                lTitle.setText (NbBundle.getMessage (Advanced.class, "CTL_Advanced_Options_Title"));
                break;
            }
            return;
        }
        
        OptionsCategory category = (OptionsCategory) 
            optionCategories.get (i);
        
        // refresh central panel
        pOptions.removeAll ();
        final Dimension size;
        if (updatedCategories.contains (category)) {
            JComponent component = (JComponent) categoryToPanel.get (category);
            size = component.getSize ();
            pOptions.add (
                "Center",
                component
            );
            
        } else {
            JLabel label = new JLabel (loc ("CTL_Loading_Options"));
            label.setHorizontalAlignment (label.CENTER);
            size = label.getSize ();
            pOptions.add ("Center", label);
        }
        
        // set title
        Icon icon = category.getIcon ();
        if (icon != null)
            lTitle.setIcon (icon);
        lTitle.setText (category.getTitle ());
        
        // repaint
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                if (!checkSize (size)) {
                    revalidate ();
                    repaint ();
                }
                if (i != -1)
                    ((Button) buttons.get (i)).requestFocus ();
            }
        });
        firePropertyChange ("buran" + OptionsPanelController.PROP_HELP_CTX, null, null);
    }
    
    HelpCtx getHelpCtx () {
        if (getCurrentIndex () < 0) return null;
        if (getCurrentIndex () >= optionCategories.size ()) return null;
        OptionsCategory category = (OptionsCategory) 
            optionCategories.get (getCurrentIndex ());
        OptionsPanelController controller = (OptionsPanelController) categoryToController.
            get (category);
        if (controller == null) return null;
        return controller.getHelpCtx ();
    }
    
    void update () {
        Iterator it = categoryToController.values ().iterator ();
        while (it.hasNext ())
            try {
                ((OptionsPanelController) it.next ()).update ();
            } catch (Throwable t) {
                Exceptions.printStackTrace(t);
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

    
    // private methods .........................................................

    private void initUI () {

        // central panel
        pOptions = new JPanel ();
        pOptions.setLayout (new BorderLayout ());
        pOptions.setPreferredSize (new Dimension (500, 500));
        JLabel label = new JLabel (loc ("CTL_Loading_Options"));
        label.setHorizontalAlignment (label.CENTER);
        pOptions.add ("Center", label);
        
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
        lTitle.setIcon (getIcon (
            "org/netbeans/modules/options/resources/generalOptions.png"
        ));
        lTitle.setText (NbBundle.getMessage (OptionsPanel.class, "CTL_General_Options_Title"));
        pTitle.add ("Center", lTitle);

        // icon view
        pCategories2 = new JPanel (new GridBagLayout());
        pCategories2.setBackground (Color.white);
        pCategories2.setBorder (null);
        addFakeButtons ();
        pCategories = new JPanel (new BorderLayout ());
        if (isMac) {
            pCategories.setBorder (new CompoundBorder (
                new VariableBorder (null, null, borderMac, null),
                BorderFactory.createEmptyBorder (0, 4, 0, 4)
            ));
        } else {
            pCategories.setBorder (new LineBorder (iconViewBorder));
        }
        pCategories.setBackground (Color.white);
        pCategories.add ("North", pCategories2);
        
        // layout
        setLayout (new BorderLayout (10, 10));
        if (isMac) {
            pOptions.setBorder (new CompoundBorder (
                new VariableBorder (null, null, borderMac, null),
                BorderFactory.createEmptyBorder (0, 20, 5, 20)
            ));
            add (pCategories, BorderLayout.NORTH);
            add (pOptions, BorderLayout.CENTER);
        } else {
            JPanel centralPanel = new JPanel (new BorderLayout (10, 10));
            centralPanel.add (pTitle, BorderLayout.NORTH);
            centralPanel.add (pOptions, BorderLayout.CENTER);
            add (pCategories, BorderLayout.WEST);
            add (centralPanel, BorderLayout.CENTER);
            setBorder (new EmptyBorder (10, 10, 0, 10));
        }
        
        initActions ();
    }
    
    private void initActions () {
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
    }
    
    private static List loadOptionsCategories () {
        FileObject fo = Repository.getDefault ().getDefaultFileSystem ().
            findResource ("OptionsDialog");                            // NOI18N
        if (fo != null) {
            Lookup lookup = new FolderLookup (DataFolder.findFolder (fo)).
                getLookup ();
            return Collections.unmodifiableList (new ArrayList (lookup.lookup (
                new Lookup.Template (OptionsCategory.class)
            ).allInstances ()));
        }
        return Collections.EMPTY_LIST;
    }
    
    private void initControllers () {
        Iterator it = optionCategories.iterator ();
        while (it.hasNext ()) {
            OptionsCategory category = (OptionsCategory) it.next ();
            try {
                OptionsPanelController controller = category.create ();
                categoryToController.put (category, controller);
                controller.addPropertyChangeListener (coltrollerListener);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    private Lookup createMasterLookup () {
        List lookups = new ArrayList ();
        Iterator it = categoryToController.values ().iterator ();
        while (it.hasNext ()) {
            OptionsPanelController controller = (OptionsPanelController) 
                it.next ();
            lookups.add (controller.getLookup ());
        }
        return new ProxyLookup 
            ((Lookup[]) lookups.toArray (new Lookup [lookups.size ()]));
    }

    /**
     * Inits options panels adn categoryToPanel map.
     */
    private Dimension initPanels (Lookup masterLookup) {
        // size of options panel shoud be max of all nested panels
        int maxW = 0, maxH = 0;
        Iterator it = optionCategories.iterator ();
        while (it.hasNext ()) {
            OptionsCategory category = (OptionsCategory) it.next ();
            OptionsPanelController controller = (OptionsPanelController) 
                categoryToController.get (category);
            JComponent component = null;
            if (controller == null) {
                component = new JLabel (loc ("CTL_Error_Loading_Options"));
                ((JLabel) component).setHorizontalAlignment (JLabel.CENTER);
            } else
                component = controller.getComponent (masterLookup);
            categoryToPanel.put (category, component);
            maxW = Math.max (maxW, component.getPreferredSize ().width);
            maxH = Math.max (maxH, component.getPreferredSize ().height);
            //S ystem.out.println (category.getCategoryName () + " : " + component.getPreferredSize ());
        }
        return new Dimension (maxW, maxH);
    }

    private void addFakeButtons () {
        // init icon view
        addButton (
            getIcon ("org/netbeans/modules/options/resources/generalOptions.png"),
            NbBundle.getMessage (OptionsPanel.class, "CTL_General_Options")
        );
        addButton (
            getIcon ("org/netbeans/modules/options/resources/editor.png"),
            NbBundle.getMessage (OptionsPanel.class, "CTL_Editor")
        );
        addButton (
            getIcon ("org/netbeans/modules/options/resources/colors.png"),
            NbBundle.getMessage (OptionsPanel.class, "CTL_Font_And_Color_Options")
        );
        addButton (
            getIcon ("org/netbeans/modules/options/resources/keymap.png"),
            NbBundle.getMessage (OptionsPanel.class, "CTL_Keymap_Options")
        );
        addButton (
            getIcon ("org/netbeans/modules/options/resources/advanced.png"),
            NbBundle.getMessage (Advanced.class, "CTL_Advanced_Options")
        );
        addFakeButton ();
    }
    
    private void refreshButtons () {
        // remove old buttons
        Iterator it = buttons.iterator ();
        while (it.hasNext ())
            removeButton ((Button) it.next ());
        pCategories2.removeAll ();
        buttons = new ArrayList ();
        
        // add new buttons
        it = optionCategories.iterator ();
        while (it.hasNext ()) {
            final OptionsCategory category = (OptionsCategory) it.next ();
            addButton (
                category.getIcon (),
                category.getCategoryName ()
            );
        }
        
        addFakeButton ();
    }
    
    private void addButton (
        Icon icon,
        String name
    ) {
        int index = buttons.size ();
        Button button = new Button (index, icon, name);

        // add shortcut
        KeyStroke keyStroke = KeyStroke.getKeyStroke 
            (button.getDisplayedMnemonic (), KeyEvent.ALT_MASK);
        getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW).put (keyStroke, button);
        getActionMap ().put (button, new SelectAction (index));

        if (isMac) {
            GridBagConstraints gbc = new GridBagConstraints ();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.VERTICAL;
            gbc.weightx = 0.0;
            gbc.weighty = 1.0;
            gbc.gridx = index;
            gbc.gridy = 0;
            pCategories2.add (button, gbc);
        } else {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            gbc.weighty = 0.0;
            gbc.gridx = 0;
            gbc.gridy = index;
            pCategories2.add (button, gbc);
        }
        buttons.add (button);
    }
    
    private void removeButton (Button button) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke 
            (button.getDisplayedMnemonic (), KeyEvent.ALT_MASK);
        getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW).remove (keyStroke);
        getActionMap ().remove (button);
    }
    
    private void addFakeButton () {
        /* i don't know a better workaround */
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        if (isMac)
            gbc.gridy = 0;
        else
            gbc.gridx = 0;
        pCategories2.add (new javax.swing.JLabel (""), gbc);
    }
    
    private boolean checkSize (Dimension maxSize) {
        if (pOptions.getPreferredSize ().width < maxSize.width ||
            pOptions.getPreferredSize ().height < maxSize.height
        ) {
            pOptions.setPreferredSize (new Dimension (
                Math.max (pOptions.getPreferredSize ().width, maxSize.width),
                Math.max (pOptions.getPreferredSize ().height, maxSize.height)
            ));
            Window w = (Window) SwingUtilities.getAncestorOfClass 
                (Window.class, this);
            invalidate ();
            if (w != null)
                w.pack ();
            return true;
        }
        return false;
    }

    private Map iconsCache = new HashMap ();
    private Icon getIcon (String resourceName) {
        if (!iconsCache.containsKey (resourceName))
            iconsCache.put (
                resourceName,
                new ImageIcon (Utilities.loadImage (resourceName))
            );
        return (Icon) iconsCache.get (resourceName);
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
        private int status;
        
        Button (
            int index, 
            Icon icon,
            String name
        ) {
            super (icon);
            this.index = index;
            Mnemonics.setLocalizedText (this, name);
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
            addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent e) {
                    if (Button.this.index != currentCategory) {
                        setHighlighted();
                    }
                }
                public void focusLost(FocusEvent e) {
                    if (Button.this.index != currentCategory && !isMac)
                        setNormal();
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
                        new VariableBorder(null, selectedB, null, selectedB),
                        BorderFactory.createEmptyBorder(5, 5, 3, 5)
                        ));
            } else {
                setBorder (new CompoundBorder (
                    new CompoundBorder (
                        new LineBorder (Color.white),
                        new LineBorder (selectedB)
                    ),
                    new EmptyBorder (0, 2, 0, 2)
                ));
            }
            setBackground (selected);            
        }
        
        void setHighlighted() {
            if (!isMac) {
                setBorder(new CompoundBorder(
                        new CompoundBorder(
                        new LineBorder(Color.white),
                        new LineBorder(highlightedB)
                        ),
                        new EmptyBorder(0, 2, 0, 2)
                        ));
                setBackground(highlighted);
            }
            if (highlightedCategory != index) {
                if (highlightedCategory >= 0 && highlightedCategory < buttons.size()) {
                    Button b = (Button)buttons.get(highlightedCategory);
                    if (b != null && b.index != currentCategory) {
                        b.setNormal();
                    }
                }
                highlightedCategory = index;
            }
        }
        
        public void mouseClicked (MouseEvent e) {            
        }

        public void mousePressed (MouseEvent e) {
            if (!isMac)
                setSelected ();
        }

        public void mouseReleased (MouseEvent e) {
            if (index != currentCategory && index == highlightedCategory) {
                setCurrentIndex (index);
            }
        }

        public void mouseEntered (MouseEvent e) {
            if (index != currentCategory) {
                setHighlighted ();
            }
        }

        public void mouseExited (MouseEvent e) {
            if (index != currentCategory && !isMac)
                setNormal ();
        }
    }
}

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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.FocusManager;
import javax.swing.Icon;
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
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

public class OptionsPanel extends JPanel {
    private CategoryModel model = CategoryModel.getInstance();
    private JPanel pCategories;
    private JPanel pCategories2;
    private JPanel pOptions;
    private JLabel lTitle;
    private JPanel pTitle;    

    private Map<String, CategoryButton> buttons = new LinkedHashMap<String, CategoryButton>();    
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
        this(null);
    }
    
    public OptionsPanel (String categoryID) {        
        // init UI components, layout and actions, and add some default values
        initUI(categoryID);        
    }
    
    private String getCategoryID(String categoryID) {
        return categoryID == null ? model.getCurrentCategoryID() : categoryID;
    }
        
    void initCurrentCategory (final String categoryID) {                    
        //generalpanel should be moved to core/options and then could be implemented better
        //generalpanel doesn't need lookup
        boolean isGeneralPanel = "General".equals(getCategoryID(categoryID));//NOI18N
        if (model.isLookupInitialized() || isGeneralPanel) {
            setCurrentCategory(model.getCategory(getCategoryID(categoryID)));
            initActions();                        
        } else {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            // change cursor                            
                            final Frame frame = WindowManager.getDefault().getMainWindow();
                            final Cursor cursor = frame.getCursor();
                            frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                            setCurrentCategory(model.getCategory(getCategoryID(categoryID)));
                            initActions();
                            // reset cursor
                            frame.setCursor(cursor);
                            setCursor(cursor);
                        }
                    });
                }
            }, 500);                            
        }
    }
    
    private void setCurrentCategory (final CategoryModel.Category category) {
        CategoryModel.Category oldCategory = model.getCurrent();
        if (oldCategory != null) {
            ((CategoryButton) buttons.get (oldCategory.getID())).setNormal ();
        }
        if (category != null) {
            ((CategoryButton) buttons.get (category.getID())).setSelected ();
        }
        
        model.setCurrent(category);                
        // refresh central panel
        pOptions.removeAll ();
        final Dimension size;
        JComponent component = category.getComponent();
        category.update(coltrollerListener, false);
        size = component.getSize();
        pOptions.add("Center",component);
        // set title
        Icon icon = category.getIcon ();
        if (icon != null) {
            lTitle.setIcon (icon);
        }
        lTitle.setText (category.getTitle ());
        
        // repaint
        // repaint
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                if (!checkSize (size)) {
                    revalidate ();
                    repaint ();
                }
                if (model.getCurrent() != null) {
                    ((CategoryButton) buttons.get (model.getCurrentCategoryID())).requestFocus ();
                }
            }
        });
        firePropertyChange ("buran" + OptionsPanelController.PROP_HELP_CTX, null, null);
    }
        
    HelpCtx getHelpCtx () {
        return model.getHelpCtx ();
    }
    
    void update () {
        model.update(coltrollerListener, true);
    }
    
    void save () {
        model.save();
    }
    
    void cancel () {
        model.cancel();    
    }
    
    boolean dataValid () {
        return model.dataValid();
    }
    
    boolean isChanged () {
        return model.isChanged();
    }
    
    boolean needsReinit() {
        return model.needsReinit();
    }
    
    // private methods .........................................................

    private void initUI(String categoryName) {
        // central panel
        pOptions = new JPanel ();
        pOptions.setLayout (new BorderLayout ());
        pOptions.setPreferredSize (getUserSize());
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
        
        pTitle.add ("Center", lTitle);

        // icon view
        pCategories2 = new JPanel (new GridBagLayout());        
        pCategories2.setBackground (Color.white);
        pCategories2.setBorder (null);
        addCategoryButtons();        

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
     
        categoryName = getCategoryID(categoryName);
        if (categoryName != null) {
            CategoryModel.Category c = model.getCategory(getCategoryID(categoryName));
            Icon icon = c.getIcon();
            if (icon != null) {
                lTitle.setIcon(icon);
            }
            lTitle.setText(c.getTitle());
            
            CategoryButton b = (CategoryButton) buttons.get(categoryName);
            if (b != null) {
                b.setSelected();
            }
        }
    }
        
    private void initActions () {
        if (getActionMap ().get("UP") == null) {
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
    }
    
    private void addCategoryButtons () {
        // remove old buttons
        Iterator it = buttons.values().iterator ();
        while (it.hasNext ()) {
            removeButton ((CategoryButton) it.next ());
        }
        pCategories2.removeAll ();
        buttons = new LinkedHashMap<String, CategoryButton>();
        
        // add new buttons
        String[] names = model.getCategoryIDs();
        for (int i = 0; i < names.length; i++) {
            CategoryModel.Category category = model.getCategory(names[i]);
            addButton (category);            
        }
        
        addFakeButton ();
    }
                
    private void addButton (CategoryModel.Category category) {
        int index = buttons.size ();
        CategoryButton button = new CategoryButton (category);

        // add shortcut
        KeyStroke keyStroke = KeyStroke.getKeyStroke 
            (button.getDisplayedMnemonic (), KeyEvent.ALT_MASK);
        getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW).put (keyStroke, button);
        getActionMap ().put (button, new SelectAction (category));

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
        buttons.put (category.getID(), button);
    }
    
    private void removeButton (CategoryButton button) {
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
        
    private Dimension getInitSize() {
        //if necessary init size could be chosen for individual resolutions differently
        //DisplayMode dm = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();         
        return new Dimension(750, 500);
    }
    
    Dimension getUserSize() {
        int w = NbPreferences.forModule(OptionsPanel.class).getInt("OptionsWidth",getInitSize().width);//NOI18N
        int h = NbPreferences.forModule(OptionsPanel.class).getInt("OptionsHeight",getInitSize().height);//NOI18N
        return new Dimension (w, h);
    }
    
    void storeUserSize() {
        Dimension d = pOptions.getSize();
        NbPreferences.forModule(OptionsPanel.class).putInt("OptionsWidth",d.width);//NOI18N
        NbPreferences.forModule(OptionsPanel.class).putInt("OptionsHeight",d.height);//NOI18N
        pOptions.setPreferredSize(d);
    }
    
    private boolean checkSize(Dimension componentSize) {
        boolean retval = false;
        Dimension prefSize = pOptions.getPreferredSize();
        Dimension userSize = getUserSize();
        componentSize = new Dimension(Math.max(componentSize.width, userSize.width),Math.max(componentSize.height, userSize.height));
        if (prefSize.width < componentSize.width || prefSize.height < componentSize.height) {
            Dimension newSize = new Dimension(Math.max(prefSize.width, componentSize.width),Math.max(prefSize.height, componentSize.height));
            pOptions.setPreferredSize(newSize);
            Window w = (Window) SwingUtilities.getAncestorOfClass(Window.class, this);
            invalidate();
            if (w != null) w.pack();
            retval = true;            
        }        
        return retval;
    }

    // innerclasses ............................................................
    
    private class SelectAction extends AbstractAction {
        private CategoryModel.Category category;
        
        SelectAction (CategoryModel.Category category) {
            this.category = category;
        }
        public void actionPerformed (ActionEvent e) {
            setCurrentCategory (category);
        }
    }
    
    private class SelectCurrentAction extends AbstractAction {        
        public void actionPerformed (ActionEvent e) {
            Component c = FocusManager.getCurrentManager ().getFocusOwner ();
            if (c instanceof CategoryButton) {
                setCurrentCategory (((CategoryButton) c).category);
                ((CategoryButton) c).setSelected ();
            }
        }
    }
    
    private class UpAction extends AbstractAction {
        public void actionPerformed (ActionEvent e) {
            model.setPreviousCategoryAsCurrent();
            setCurrentCategory (model.getCurrent());
        }
    }
    
    private class DownAction extends AbstractAction {
        public void actionPerformed (ActionEvent e) {            
            model.setNextCategoryAsCurrent();
            setCurrentCategory (model.getCurrent());            
        }
    }
    
    class ControllerListener implements PropertyChangeListener {
        public void propertyChange (PropertyChangeEvent evt) {
            OptionsPanel.this.firePropertyChange 
                ("buran" + evt.getPropertyName (), null, null);
        }
    }
    
    class CategoryButton extends JLabel implements MouseListener {
        private final CategoryModel.Category category;                
        CategoryButton (final CategoryModel.Category category) {
            super (category.getIcon());
            this.category = category;
            Mnemonics.setLocalizedText (this, category.getCategoryName());
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
            
            setNormal ();
            addFocusListener(new FocusListener() {                
                public void focusGained(FocusEvent e) {
                    if (model.getCurrent() != null && !category.isCurrent()) {
                        setHighlighted();
                    }
                }
                public void focusLost(FocusEvent e) {
                    if (model.getCurrent() != null && !category.isCurrent() && !isMac) {
                        setNormal();
                    }
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
            if (!category.isHighlited()) {
                if (model.getHighlitedCategoryID() != null) {
                    CategoryButton b = (CategoryButton)buttons.get(model.getHighlitedCategoryID());
                    if (b != null && !b.category.isCurrent()) {
                        b.setNormal();
                    }
                }
                model.setHighlited(category);
            }
        }
        
        public void mouseClicked (MouseEvent e) {            
        }

        public void mousePressed (MouseEvent e) {
            if (!isMac && model.getCurrent() != null) {
                setSelected ();
            }
        }

        public void mouseReleased (MouseEvent e) {
            if (!category.isCurrent() && category.isHighlited() && model.getCurrent() != null) {
                setCurrentCategory(category);
            }
        }

        public void mouseEntered (MouseEvent e) {
            if (!category.isCurrent() && model.getCurrent() != null) {
                setHighlighted ();
            }
        }

        public void mouseExited (MouseEvent e) {
            if (!category.isCurrent() && !isMac && model.getCurrent() != null) {
                setNormal ();
            }
        }
    }
}

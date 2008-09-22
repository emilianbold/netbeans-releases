/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.options;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import org.netbeans.modules.options.ui.VariableBorder;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

public class OptionsPanel extends JPanel {
    private JPanel pCategories;
    private JPanel pCategories2;
    private JPanel pOptions;
    private CardLayout cLayout;

    private Map<String, CategoryButton> buttons = new LinkedHashMap<String, CategoryButton>();    
    private final boolean isMac = UIManager.getLookAndFeel ().getID ().equals ("Aqua");
    private final boolean isNimbus = UIManager.getLookAndFeel ().getID ().equals ("Nimbus");
    private final boolean isGTK = UIManager.getLookAndFeel ().getID ().equals ("GTK");
    private Color selected = isMac ? new Color(221, 221, 221) : new Color (193, 210, 238);
    private Color selectedB = isMac ? new Color(183, 183, 183) : new Color (149, 106, 197);
    private Color highlighted = isMac ? new Color(221, 221, 221) : new Color (224, 232, 246);
    private Color highlightedB = new Color (152, 180, 226);
    private Color iconViewBorder = new Color (127, 157, 185);
    private ControllerListener controllerListener = new ControllerListener ();
    
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
        return categoryID == null ? CategoryModel.getInstance().getCurrentCategoryID() : categoryID;
    }

    void initCurrentCategory (final String categoryID, final String subpath) {
        //generalpanel should be moved to core/options and then could be implemented better
        //generalpanel doesn't need lookup
        boolean isGeneralPanel = "General".equals(getCategoryID(categoryID));//NOI18N
        if (CategoryModel.getInstance().isLookupInitialized() || isGeneralPanel) {
            setCurrentCategory(CategoryModel.getInstance().getCategory(getCategoryID(categoryID)), subpath);
            initActions();                        
        } else {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            // change cursor                            
                            Frame[] all = Frame.getFrames();
                            if (all == null || all.length == 0) {
                                return;
                            }
                            final Frame frame = all[0];
                            final Cursor cursor = frame.getCursor();
                            frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                            setCurrentCategory(CategoryModel.getInstance().getCategory(getCategoryID(categoryID)), subpath);
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
    
    private void setCurrentCategory (final CategoryModel.Category category, String subpath) {
        CategoryModel.Category oldCategory = CategoryModel.getInstance().getCurrent();
        if (oldCategory != null) {
            (buttons.get(oldCategory.getID())).setNormal ();
        }
        if (category != null) {
            (buttons.get(category.getID())).setSelected ();
        }
        
        CategoryModel.getInstance().setCurrent(category);
        JComponent component = category.getComponent();                
        category.update(controllerListener, false);
        final Dimension size = component.getSize();
        if( component.getParent() == null || !pOptions.equals(component.getParent()) ) {
            pOptions.add(component, category.getCategoryName());
        }
        cLayout.show(pOptions, category.getCategoryName());
        checkSize (size);
        /*if (CategoryModel.getInstance().getCurrent() != null) {
            ((CategoryButton) buttons.get (CategoryModel.getInstance().getCurrentCategoryID())).requestFocus();
        } */       
        firePropertyChange ("buran" + OptionsPanelController.PROP_HELP_CTX, null, null);
        if(subpath != null) {
            category.setCurrentSubcategory(subpath);
        }
    }
        
    HelpCtx getHelpCtx () {
        return CategoryModel.getInstance().getHelpCtx ();
    }
    
    void update () {
        CategoryModel.getInstance().update(controllerListener, true);
    }
    
    void save () {
        CategoryModel.getInstance().save();
    }
    
    void cancel () {
        CategoryModel.getInstance().cancel();    
    }
    
    boolean dataValid () {
        return CategoryModel.getInstance().dataValid();
    }
    
    boolean isChanged () {
        return CategoryModel.getInstance().isChanged();
    }
    
    boolean needsReinit() {
        return CategoryModel.getInstance().needsReinit();
    }
    
    // private methods .........................................................

    private void initUI(String categoryName) {
        this.getAccessibleContext().setAccessibleDescription(loc("ACS_OptionsPanel"));//NOI18N
        // central panel
        pOptions = new JPanel ();
        cLayout = new CardLayout();
        pOptions.setLayout (cLayout);
        pOptions.setPreferredSize (getUserSize());
        JLabel label = new JLabel (loc ("CTL_Loading_Options"));
        label.setHorizontalAlignment (JLabel.CENTER);
        pOptions.add (label, label.getText());//NOI18N

        // icon view
        pCategories2 = new JPanel (new GridBagLayout());        
        pCategories2.setBackground (Color.white);
        pCategories2.setBorder (null);
        addCategoryButtons();        

        pCategories = new JPanel (new BorderLayout ());
        pCategories.setBorder (BorderFactory.createMatteBorder(0,0,1,0,Color.lightGray));        
        pCategories.setBackground (Color.white);
        pCategories.add ("Center", pCategories2);
        
        // layout
        setLayout (new BorderLayout (10, 10));

        pOptions.setBorder(new CompoundBorder(
                new VariableBorder(null, null, borderMac, null),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)
                ));
        add(pCategories, BorderLayout.NORTH);
        add(pOptions, BorderLayout.CENTER);
     
        categoryName = getCategoryID(categoryName);
        if (categoryName != null) {
            CategoryModel.Category c = CategoryModel.getInstance().getCategory(getCategoryID(categoryName));
            
            CategoryButton b = buttons.get(categoryName);
            if (b != null) {
                b.setSelected();
            }
        }
    }
        
    private void initActions () {
        if (getActionMap ().get("PREVIOUS") == null) {//NOI18N
            InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            
            inputMap.put (KeyStroke.getKeyStroke (KeyEvent.VK_LEFT, 0), "PREVIOUS");//NOI18N
            getActionMap ().put ("PREVIOUS", new PreviousAction ());//NOI18N
            
            inputMap.put (KeyStroke.getKeyStroke (KeyEvent.VK_RIGHT, 0),"NEXT");//NOI18N
            getActionMap ().put ("NEXT", new NextAction ());//NOI18N
        }
    }
    
    private void addCategoryButtons () {
        // remove old buttons
        Iterator<CategoryButton> it = buttons.values().iterator ();
        while (it.hasNext ()) {
            removeButton ((CategoryButton) it.next ());
        }
        pCategories2.removeAll ();
        buttons = new LinkedHashMap<String, CategoryButton>();
        
        // add new buttons
        Dimension maxSize = new Dimension(0,0);        
        String[] names = CategoryModel.getInstance().getCategoryIDs();
        for (int i = 0; i < names.length; i++) {
            CategoryModel.Category category = CategoryModel.getInstance().getCategory(names[i]);
            CategoryButton button = addButton (category);            
            Dimension d = button.getPreferredSize();
            maxSize.width = Math.max(maxSize.width, d.width);
            // #141121 - ignore big height which can appear for uknown reason
            if(d.height < d.width*10) {
                maxSize.height = Math.max(maxSize.height, d.height);
            }
        }        
        it = buttons.values().iterator ();
        while (it.hasNext ()) {
            ((CategoryButton) it.next ()).setPreferredSize(maxSize);
        }
        
        addFakeButton ();
    }
                
    private CategoryButton addButton (CategoryModel.Category category) {
        int index = buttons.size ();
        CategoryButton button = isNimbus || isGTK 
                ? new NimbusCategoryButton(category)
                : new CategoryButton(category);

        // add shortcut
        KeyStroke keyStroke = KeyStroke.getKeyStroke 
            (button.getDisplayedMnemonic (), KeyEvent.ALT_MASK);
        getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW).put (keyStroke, button);
        getActionMap ().put (button, new SelectAction (category));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.gridx = index;
        gbc.gridy = 0;
        pCategories2.add(button, gbc);
        buttons.put (category.getID(), button);
        return button;
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
        gbc.gridy = 0;
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

    @Override
    public Dimension getPreferredSize() {
        //#108865 Scrollbars appear on Options dialog - preferredSize mustn't exceed screenBounds.? - 100 
        //else NbPresenter will show up scrollbars                            
        Dimension d = super.getPreferredSize();
        final Rectangle screenBounds = Utilities.getUsableScreenBounds();
        return new Dimension(Math.min(d.width, screenBounds.width - 101), Math.min(d.height, screenBounds.height - 101));
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
            setCurrentCategory (category, null);
        }
    }
        
    private class SelectCurrentAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            CategoryModel.Category highlightedB = CategoryModel.getInstance().getCategory(CategoryModel.getInstance().getHighlitedCategoryID());            
            if (highlightedB != null) {
                setCurrentCategory(highlightedB, null);
            }
        }
    }
    
    private class PreviousAction extends AbstractAction {
        public void actionPerformed (ActionEvent e) {
            setCurrentCategory (CategoryModel.getInstance().getPreviousCategory(), null);
        }
    }
    
    private class NextAction extends AbstractAction {
        public void actionPerformed (ActionEvent e) {            
            setCurrentCategory (CategoryModel.getInstance().getNextCategory(), null);
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
            setDisplayedMnemonic(0);            
            setOpaque (true);
            setVerticalTextPosition (BOTTOM);
            setHorizontalTextPosition (CENTER);
            setHorizontalAlignment (CENTER);
            addMouseListener (this);
            setFocusable (false);
            setFocusTraversalKeysEnabled (false);
            setForeground (Color.black);
            
            if (isMac) {
                setFont(labelFontMac);
                setIconTextGap(2);
            }
            
            setNormal ();
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
                if (CategoryModel.getInstance().getHighlitedCategoryID() != null) {
                    CategoryButton b = buttons.get(CategoryModel.getInstance().getHighlitedCategoryID());
                    if (b != null && !b.category.isCurrent()) {
                        b.setNormal();
                    }
                }
                CategoryModel.getInstance().setHighlited(category,true);
            }
        }
        
        public void mouseClicked (MouseEvent e) {            
        }

        public void mousePressed (MouseEvent e) {
            if (!isMac && CategoryModel.getInstance().getCurrent() != null) {
                setSelected ();
            }
        }

        public void mouseReleased (MouseEvent e) {
            if (!category.isCurrent() && category.isHighlited() && CategoryModel.getInstance().getCurrent() != null) {
                setCurrentCategory(category, null);
            }
        }

        public void mouseEntered (MouseEvent e) {
            if (!category.isCurrent() && CategoryModel.getInstance().getCurrent() != null) {
                setHighlighted ();
            } else {
                CategoryModel.getInstance().setHighlited(CategoryModel.getInstance().getCategory(CategoryModel.getInstance().getHighlitedCategoryID()),false);
            }
        }

        public void mouseExited (MouseEvent e) {
            if (!category.isCurrent() && !isMac && CategoryModel.getInstance().getCurrent() != null) {
                setNormal ();
            }
        }
    }


    private static final int BORDER_WIDTH = 4;
    private static final Border selBorder = new CompoundBorder( 
            new CompoundBorder(BorderFactory.createEmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH),
                new NimbusBorder() ),
                BorderFactory.createEmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));
    private static final Border normalBorder = BorderFactory.createEmptyBorder(2*BORDER_WIDTH+1, 2*BORDER_WIDTH+1, 2*BORDER_WIDTH+3, 2*BORDER_WIDTH+3);

    private static final short STATUS_NORMAL = 0;
    private static final short STATUS_SELECTED = 1;
    private static final short STATUS_HIGHLIGHTED = 2;

    private static final Color COL_GRADIENT1 = new Color(244,245,249);
    private static final Color COL_GRADIENT2 = new Color(163,184,203);
    private static final Color COL_GRADIENT3 = new Color(206,227,246);

    private static final Color COL_OVER_GRADIENT1 = new Color(244,245,249,128);
    private static final Color COL_OVER_GRADIENT2 = new Color(163,184,203,128);
    private static final Color COL_OVER_GRADIENT3 = new Color(206,227,246,128);

    private class NimbusCategoryButton extends CategoryButton {

        private short status = STATUS_NORMAL;

        public NimbusCategoryButton( final CategoryModel.Category category ) {
            super( category );
            setOpaque(false);
            setBorder( normalBorder );
        }

        @Override
        protected void paintChildren(Graphics g) {
            super.paintChildren(g);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if( status == STATUS_SELECTED || status == STATUS_HIGHLIGHTED ) {
                Insets in = getInsets();
                in.top -= BORDER_WIDTH;
                in.left -= BORDER_WIDTH;
                in.bottom -= BORDER_WIDTH;
                in.right -= BORDER_WIDTH;
                Graphics2D g2d = (Graphics2D) g.create();

                int width = getWidth()-in.left-in.right+1;
                int height = getHeight()-in.top-in.bottom+1;
                int topGradient = (int)(0.7*height);
                int bottomGradient = height-topGradient;
                Color c1 = (status == STATUS_HIGHLIGHTED ? COL_OVER_GRADIENT1 : COL_GRADIENT1);
                Color c2 = (status == STATUS_HIGHLIGHTED ? COL_OVER_GRADIENT2 : COL_GRADIENT2);
                Color c3 = (status == STATUS_HIGHLIGHTED ? COL_OVER_GRADIENT3 : COL_GRADIENT3);
                g2d.setPaint( new GradientPaint(in.left, in.top, c1, in.left, in.top+topGradient, c2));
                g2d.fillRect(in.left,in.top, width, topGradient );

                g2d.setPaint( new GradientPaint(in.left, in.top+topGradient, c2, in.left, in.top+topGradient+bottomGradient, c3));
                g2d.fillRect(in.left,in.top+topGradient, width, bottomGradient  );

                g2d.dispose();
            }
            super.paintComponent(g);
        }

        @Override
        void setHighlighted() {
            super.setHighlighted();
            status = STATUS_HIGHLIGHTED;
            setBorder(selBorder);
            repaint();
        }


        @Override
        void setNormal() {
            setBorder(normalBorder);
            status = STATUS_NORMAL;
            repaint();
        }

        @Override
        void setSelected() {
            setBorder(selBorder);
            status = STATUS_SELECTED;
            repaint();
        }
    }

    private static class NimbusBorder implements Border {

        private static final Color COLOR_BORDER = new Color(72,93,112, 255);
        private static final Color COLOR_SHADOW1 = new Color(72,93,112, 100);
        private static final Color COLOR_SHADOW2 = new Color(72,93,112, 60) ;

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D)g;

            g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
            Area rect = new Area(new RoundRectangle2D.Float(x, y, width-3, height-2, 4, 4));
            g2d.setColor( COLOR_BORDER );
            g2d.draw( rect );

            Area shadow = new Area( rect );
            AffineTransform tx = new AffineTransform();
            tx.translate(1, 1);
            shadow.transform(tx);
            shadow.subtract(rect);
            g2d.setColor( COLOR_SHADOW1 );
            g2d.draw( shadow );

            shadow = new Area( rect );
            tx = new AffineTransform();
            tx.translate(2, 2);
            shadow.transform(tx);
            shadow.subtract(rect);
            g2d.setColor( COLOR_SHADOW2 );
            g2d.draw( shadow );
        }

        public Insets getBorderInsets(Component c) {
            return new Insets( 1,1,3,3 );
        }

        public boolean isBorderOpaque() {
            return false;
        }
    }
}

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.multiview;

import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;
import javax.swing.JToggleButton.ToggleButtonModel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.metal.MetalLookAndFeel;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;


/**
 * Temporary solution tomultiview tabs..
 * @author  mkleint
 */
class TabsComponent extends JPanel {
    
    private JComponent EMPTY;
    private final static String TOOLBAR_MARKER = "MultiViewPanel"; //NOI18N
    
    MultiViewModel model;
    private ActionListener listener;
    private MouseListener buttonMouseListener = null;
    private JComponent toolbarPanel;
    private JPanel componentPanel;
    private CardLayout cardLayout;
    private Set alreadyAddedElements;
    private JToolBar bar;
    
    /** Creates a new instance of TabsComponent */
    public TabsComponent() {
        super();
        bar = new JToolBar();
        // special border installed by core or no border if not available
        Border b = (Border)UIManager.get("Nb.Editor.Toolbar.border"); //NOI18N
        bar.setBorder(b);
//        setBorder (BorderFactory.createEmptyBorder(0,0,0,0));
//        setLayout (new AdaptiveGridLayout());
        bar.setLayout (new OneLineGridLayout());
        bar.setFloatable(false);
        bar.setFocusable(true);
        bar.setPreferredSize(new Dimension(10, 26));
        
        setLayout(new BorderLayout());
        add(bar, BorderLayout.NORTH);
        startToggling();
    }
    
    
    
    public void setModel(MultiViewModel model) {
        if (this.model != null) {
            bar.removeAll();
        }
        this.model = model;
        
        componentPanel = new JPanel();
        cardLayout = new CardLayout();
        componentPanel.setLayout(cardLayout);
        add(componentPanel, BorderLayout.CENTER);
        alreadyAddedElements = new HashSet();
        
        MultiViewDescription[] descs = model.getDescriptions();
        MultiViewDescription def = model.getActiveDescription();
        JToggleButton active = null;
        for (int i = 0; i < descs.length; i++) {
            JToggleButton button = createButton(descs[i]);
            model.getButtonGroup().add(button);
            bar.add(button);
            if (descs[i] == model.getActiveDescription()) {
                active = button;
                
            }
        }
        if (active != null) {
            active.setSelected(true);
        }
        toolbarPanel = getEmptyInnerToolBar();
        bar.add(toolbarPanel);
//        if (isVisible()) {
//            revalidate();
//            repaint();
//        }
//        // add toolar and separator now..
    }

    
    void switchToCard(MultiViewElement elem, String id) {
        if (! alreadyAddedElements.contains(elem)) {
            componentPanel.add(elem.getVisualRepresentation(), id);
            alreadyAddedElements.add(elem);
        }
        cardLayout.show(componentPanel, id);
    }
    
    
    void changeActiveManually(MultiViewDescription desc) {
        Enumeration en = model.getButtonGroup().getElements();
        while (en.hasMoreElements()) {
            JToggleButton obj = (JToggleButton)en.nextElement();
            
            if (obj.getModel() instanceof TabsComponent.TabsButtonModel) {
                TabsButtonModel btnmodel = (TabsButtonModel)obj.getModel();
                if (btnmodel.getButtonsDescription().equals(desc)) {
                    obj.setSelected(true);
                    MultiViewElement elem = model.getElementForDescription(desc);
                    elem.getVisualRepresentation().requestFocus();
                    break;
                }
            }
        }
    }

    void changeVisibleManually(MultiViewDescription desc) {
        Enumeration en = model.getButtonGroup().getElements();
        while (en.hasMoreElements()) {
            JToggleButton obj = (JToggleButton)en.nextElement();
            
            if (obj.getModel() instanceof TabsComponent.TabsButtonModel) {
                TabsButtonModel btnmodel = (TabsButtonModel)obj.getModel();
                if (btnmodel.getButtonsDescription().equals(desc)) {
                    obj.setSelected(true);
                    break;
                }
            }
        }
    }
    
    private JToggleButton createButton(MultiViewDescription description) {
        final JToggleButton button = new JToggleButton(description.getDisplayName());
        button.setModel(new TabsButtonModel(description));
        button.setRolloverEnabled(true);
        Border b = (getButtonBorder());
        if (b != null) {
           button.setBorder(b);
        }
          
        if (buttonMouseListener == null) {
            buttonMouseListener = new ButtonMouseListener();
        }
        button.addMouseListener (buttonMouseListener);
        
        //
        Font font = button.getFont();
        FontMetrics fm = button.getFontMetrics(font);
        int height = fm.getHeight();
        Dimension dim = button.getPreferredSize();
        button.setPreferredSize(new Dimension(dim.width,height+6));   
//        button.setMinimumSize(new Dimension(dim.width,height+6));
//        button.setMaximumSize(new Dimension(dim.width,height+6));
        
        button.setFocusable(true);
        button.setFocusPainted(true);
        return button;
    }

    void setInnerToolBar(JComponent innerbar) {
        synchronized (getTreeLock()) {
            if (toolbarPanel != null) {
                bar.remove(toolbarPanel);
            } else {
                System.out.println("something wrong.. model was not set..");
            }
            if (innerbar == null) {
                innerbar = getEmptyInnerToolBar();
            }
            innerbar.putClientProperty(TOOLBAR_MARKER, "X"); //NOI18N
            // need to set it to null, because CloneableEditor set's the border for the editor bar part only..
            innerbar.setBorder(null);
            toolbarPanel = innerbar;
            if (toolbarPanel != null) {
                bar.add(toolbarPanel);
            }
            // rootcycle is the tabscomponent..
//            toolbarPanel.setFocusCycleRoot(false);
            bar.revalidate();
            bar.repaint();
        }
    }
    
    
    
    JComponent getEmptyInnerToolBar() {
        if (EMPTY == null) {
            EMPTY = new JPanel();
        }
        return EMPTY;
    }
    
    
    void requestFocusForSelectedButton() {
        bar.setFocusable(true);
        Enumeration en = model.getButtonGroup().getElements();
        while (en.hasMoreElements()) {
            JToggleButton but = (JToggleButton)en.nextElement();
            if (model.getButtonGroup().isSelected(but.getModel())) {
                but.requestFocus();
                return;
            }
        }
        throw new IllegalStateException("How come none of the buttons is selected?");
    }

    void requestFocusForPane() {
        bar.setFocusable(false);
        componentPanel.requestFocus();
    }
    
    
    private Border buttonBorder = null;
    private boolean isMetal = false;
    private boolean isXP = false;
    private boolean isWindows = false;
    private Border getButtonBorder() {
        if (buttonBorder == null) {
            //For some lf's, core will supply one
            buttonBorder = UIManager.getBorder ("nb.tabbutton.border"); //NOI18N
        }
        
        if (buttonBorder == null) {
            //use the hack for XP & Metal - stolen from form editor's CategorySelectPanel
            Class clazz = UIManager.getLookAndFeel().getClass();
            //these flags will be used later by paintComponent()
            isMetal = MetalLookAndFeel.class.isAssignableFrom(clazz);
            isWindows = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel".equals(
                clazz.getName()); //NOI18N
            isXP = isXPTheme();
            
            if (isMetal || isWindows) {
            
                AbstractButton button = new JToggleButton("foo");
                button.setRolloverEnabled(true);
                    
                JToolBar toolbar = new JToolBar();
                toolbar.setRollover(true);
                toolbar.add(button);
                button.getModel().setRollover(true);
                buttonBorder = button.getBorder();
                toolbar.remove(button);

                // in case this is CompoundBorder with an inner EmptyBorder,
                // we provide our (smaller) border to have lower buttons
                if (buttonBorder instanceof CompoundBorder) {
                    CompoundBorder compound = (CompoundBorder) buttonBorder;
                    if (compound.getInsideBorder() instanceof EmptyBorder) {
                        buttonBorder = 
                            BorderFactory.createCompoundBorder(
                            compound.getOutsideBorder(),
                            BorderFactory.createEmptyBorder(0, 2, 0, 2));
                    } 
                }
            }
        }
        return buttonBorder;
    }
    
    public static boolean isXPTheme () {
        Boolean isXP = (Boolean)Toolkit.getDefaultToolkit().
                        getDesktopProperty("win.xpstyle.themeActive"); //NOI18N
        return isXP == null ? false : isXP.booleanValue();
    }
    
  
    void startToggling() {
        ActionMap map = bar.getActionMap();
        Action act = new TogglesGoEastAction();
        // JToolbar action name
        map.put("navigateRight", act);
        InputMap input = bar.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        
        act = new TogglesGoWestAction();
        // JToolbar action name
        map.put("navigateLeft", act);
        
        act = new TogglesGoDownAction();
        map.put("TogglesGoDown", act);
        // JToolbar action name
        map.put("navigateUp", act);
        KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE"); //NOI18N
        input.put(stroke, "TogglesGoDown");
    }

    
    private class TogglesGoWestAction extends AbstractAction {
        
        public void actionPerformed(ActionEvent e) {
            MultiViewDescription[] descs = model.getDescriptions();
            MultiViewDescription active = model.getActiveDescription();
            for (int i = 0; i < descs.length; i++) {
                if (descs[i] == active) {
                    int next = i - 1;
                    if (next < 0) {
                        next = descs.length - 1;
                    }
                    changeVisibleManually(descs[next]);
                    requestFocusForSelectedButton();
                }
            }
        }
    }
    
    private class TogglesGoEastAction extends AbstractAction {
        
        public void actionPerformed(ActionEvent e) {
            MultiViewDescription[] descs = model.getDescriptions();
            MultiViewDescription active = model.getActiveDescription();
            for (int i = 0; i < descs.length; i++) {
                if (descs[i] == active) {
                    int next = i + 1;
                    if (next >= descs.length) {
                        next = 0;
                    }
                    changeVisibleManually(descs[next]);
                    requestFocusForSelectedButton();
                }
            }
        }
    }

    private class TogglesGoDownAction extends AbstractAction {
        
        public void actionPerformed(ActionEvent e) {
            changeActiveManually(model.getActiveDescription());
            model.getActiveElement().getVisualRepresentation().requestFocusInWindow();
        }
    }
    
    
/**
 * used in 
 */    
    static class TabsButtonModel extends ToggleButtonModel {

        private MultiViewDescription desc;
        public TabsButtonModel(MultiViewDescription description) {
            super();
            desc = description;
        }
        
        public MultiViewDescription getButtonsDescription() {
            return desc;
        }
    }
    
    class ButtonMouseListener extends MouseAdapter {
        public void mouseEntered(MouseEvent e) {
            AbstractButton b = (AbstractButton)e.getComponent();
            b.getModel().setRollover(true);
        }
        public void mouseExited(MouseEvent e) {
            AbstractButton b = (AbstractButton)e.getComponent();
            b.getModel().setRollover(false);
        }
        
        /** for user triggered clicks, do activate the current element.. */
        public void mouseClicked(MouseEvent e) {
            AbstractButton b = (AbstractButton)e.getComponent();
            MultiViewModel model = TabsComponent.this.model;
            if (model != null) {
                model.fireActivateCurrent();
            }

        }
        
    }    
    

    // Copied from opneide's SheetTabbedPane which was a copy itself.
    //Copied from form editor's CategorySelectPanel
    static class AdaptiveGridLayout implements LayoutManager {

        private int h_margin_left = 2; // margin on the left
        private int h_margin_right = 1; // margin on the right
        private int v_margin_top = 2; // margin at the top
        private int v_margin_bottom = 3; // margin at the bottom
        private int h_gap = 1; // horizontal gap between components
        private int v_gap = 1; // vertical gap between components
        private int MINIMUM_HEIGHT = 20;

        public void addLayoutComponent(String name, Component comp) {
        }

        public void removeLayoutComponent(Component comp) {
        }

        public Dimension preferredLayoutSize(Container parent) {
            synchronized (parent.getTreeLock()) {
                int containerWidth = parent.getWidth();
                int count = parent.getComponentCount();

                if (containerWidth <= 0 || count == 0) {
                    // compute cumulated width of all components placed on one row
                    int cumulatedWidth = 0;
                    int height = MINIMUM_HEIGHT;
                    for (int i=0; i < count; i++) {
                        Dimension size = parent.getComponent(i).getPreferredSize();
                        cumulatedWidth += size.width;
                        if (i + 1 < count)
                            cumulatedWidth += h_gap;
                        if (size.height > height)
                            height = size.height;
                    }
                    cumulatedWidth += h_margin_left + h_margin_right;
                    height += v_margin_top + v_margin_bottom;
                    return new Dimension(cumulatedWidth, height);
                }

                // otherwise the container already has some width set - so we
                // just compute preferred height for it

                // get max. component width and height
                int columnWidth = 0;
                int rowHeight = MINIMUM_HEIGHT;
                for (int i=0; i < count; i++) {
                    Dimension size = parent.getComponent(i).getPreferredSize();
                    if (size.width > columnWidth)
                        columnWidth = size.width;
                    if (size.height > rowHeight)
                        rowHeight = size.height;
                }

                // compute column count
                int columnCount = 0;
                int w = h_margin_left + columnWidth + h_margin_right;
                do {
                    columnCount++;
                    w += h_gap + columnWidth;
                }
                while (w <= containerWidth && columnCount < count);

                // compute row count and preferred height
                int rowCount = count / columnCount +
                               (count % columnCount > 0 ? 1 : 0);
                int prefHeight = v_margin_top + rowCount * rowHeight
                                     + (rowCount - 1) * v_gap + v_margin_bottom;

                return new Dimension(containerWidth, prefHeight);
            }
        }

        public Dimension minimumLayoutSize(Container parent) {
            return new Dimension(h_margin_left + h_margin_right,
                                 v_margin_top + v_margin_bottom);
        }

        public void layoutContainer(Container parent) {
            synchronized (parent.getTreeLock()) {
                int count = parent.getComponentCount();
                if (count == 0)
                    return;

                // get max. component width and height
                int columnWidth = 0;
                int rowHeight = MINIMUM_HEIGHT;
                
                Component jsc = null;
                Component bar = null;
                
                for (int i=0; i < count; i++) {
                    Component c = parent.getComponent(i);
                    if (!(c instanceof JPanel || c instanceof JToolBar)) {
                        Dimension size = c.getPreferredSize();
//                        if (!(c instanceof JToolBar)) {
                            if (size.width > columnWidth)
                                columnWidth = size.width;
//                        }
                        if (size.height > rowHeight)
                            rowHeight = size.height;
                    } else {
                        if (c instanceof JPanel) {
                            jsc = c;
                        }                        
                        if (c instanceof JToolBar) {
                            bar = c;
                        }
                    }
                }
                count -=2;

                // compute column count
                int containerWidth = parent.getWidth();
                int columnCount = 0;
                int w = h_margin_left + columnWidth + h_margin_right;
                do {
                    columnCount++;
                    w += h_gap + columnWidth;
                }
                while (w <= containerWidth && columnCount < count);

                int roundedRowCount = 1;
                // adjust layout matrix - balance number of columns according
                // to last row
                if (count % columnCount > 0) {
                    roundedRowCount = count / columnCount;
                    int lastRowEmpty = columnCount - count % columnCount;
                    if (lastRowEmpty > roundedRowCount) {
                        columnCount -= lastRowEmpty / (roundedRowCount + 1);
                        roundedRowCount --;
                    }
                }

                // adjust column width
                if (count > columnCount)
                    columnWidth = (containerWidth - h_margin_left - h_margin_right
                                     - (columnCount - 1) * h_gap) / columnCount;
                if (columnWidth < 0)
                    columnWidth = 0;

                int row = 0;
                int top = v_margin_top;
                int left = h_margin_left;
                // layout the components
                for (int i=0, col=0; i < count+1; i++) {
                    Component c = parent.getComponent(i);
                    if (c != jsc && c != bar) {
                        parent.getComponent(i).setBounds(
                                           h_margin_left + col * (columnWidth + h_gap),
                                           v_margin_top + row * (rowHeight + v_gap),
                                           columnWidth,
                                           rowHeight);
                        top = v_margin_top + row * (rowHeight + v_gap);
                        left = h_margin_left + (col + 1) * (columnWidth + h_gap) ;
                        if (++col >= columnCount) {
                            col = 0;
                            row++;
                        }
                    }
                }
                
                top += rowHeight + 3;
                
                if (jsc != null) {
                    jsc.setBounds (0, top, parent.getWidth(), parent.getHeight() - top);
                }
                if (bar != null) {
                    bar.setBounds(left, (row - 1) * (rowHeight + v_gap), parent.getWidth() - left, rowHeight);
                }
            }
        }
    }    
  
    
    // Copied from opneide's SheetTabbedPane which was a copy itself.
    //Copied from form editor's CategorySelectPanel
    static class OneLineGridLayout implements LayoutManager {

        private int h_margin_left = 2; // margin on the left
        private int h_margin_right = 1; // margin on the right
        private int v_margin_top = 2; // margin at the top
        private int v_margin_bottom = 3; // margin at the bottom
        private int v_gap = 1; // vertical gap between components
        private int h_gap = 1; // vertical gap between components
        private int MINIMUM_HEIGHT = 22;

        public void addLayoutComponent(String name, Component comp) {
        }

        public void removeLayoutComponent(Component comp) {
        }

        public Dimension preferredLayoutSize(Container parent) {
            synchronized (parent.getTreeLock()) {
                int containerWidth = parent.getWidth();
                int count = parent.getComponentCount();

                if (containerWidth <= 0 || count == 0) {
                    // compute cumulated width of all components placed on one row
                    int cumulatedWidth = 0;
                    int height = MINIMUM_HEIGHT;
                    for (int i=0; i < count; i++) {
                        Dimension size = parent.getComponent(i).getPreferredSize();
                        cumulatedWidth += size.width;
                        if (i + 1 < count)
                            cumulatedWidth += h_gap;
                        if (size.height > height)
                            height = size.height;
                    }
                    cumulatedWidth += h_margin_left + h_margin_right;
                    height += v_margin_top + v_margin_bottom;
                    return new Dimension(cumulatedWidth, height);
                }

                // otherwise the container already has some width set - so we
                // just compute preferred height for it

                // get max. component width and height
                int columnWidth = 0;
                int rowHeight = MINIMUM_HEIGHT;
                for (int i=0; i < count; i++) {
                    Dimension size = parent.getComponent(i).getPreferredSize();
                    if (size.width > columnWidth)
                        columnWidth = size.width;
                    if (size.height > rowHeight)
                        rowHeight = size.height;
                }

                // compute column count
                int columnCount = 0;
                int w = h_margin_left + columnWidth + h_margin_right;
                do {
                    columnCount++;
                    w += h_gap + columnWidth;
                }
                while (w <= containerWidth && columnCount < count);

                // compute row count and preferred height
                int rowCount = count / columnCount +
                               (count % columnCount > 0 ? 1 : 0);
                int prefHeight = v_margin_top + rowCount * rowHeight
                                     + (rowCount - 1) * v_gap + v_margin_bottom;
                
//                System.out.println("preffered dim=" + new Dimension(containerWidth, prefHeight));
                return new Dimension(containerWidth, prefHeight);
            }
        }

        public Dimension minimumLayoutSize(Container parent) {
            int count = parent.getComponentCount();
            
            if (count > 0) {
                // compute cumulated width of all components placed on one row
                int cumulatedWidth = 0;
                int height = MINIMUM_HEIGHT;
                for (int i=0; i < count; i++) {
                    Component comp = parent.getComponent(i);
                    if (!(comp instanceof JPanel || comp instanceof JToolBar)) {
                        Dimension size = comp.getMinimumSize();
                        cumulatedWidth += size.width;
                        if (i + 1 < count)
                            cumulatedWidth += h_gap;
                        if (size.height > height)
                            height = size.height;
                    }
                }
                cumulatedWidth += h_margin_left + h_margin_right;
                height += v_margin_top + v_margin_bottom;
                return new Dimension(cumulatedWidth, height);
            }
            return new Dimension(0,0);
        }

        public void layoutContainer(Container parent) {
            synchronized (parent.getTreeLock()) {
                int count = parent.getComponentCount();
                if (count == 0)
                    return;

                // get max. component width and height
                int columnWidth = 0;
                int rowHeight = MINIMUM_HEIGHT;
                
                int minColumnWidth = 0;
                int minRowHeight = 0;
                
                Component jsc = null;
                Component bar = null;
                
                for (int i=0; i < count; i++) {
                    Component c = parent.getComponent(i);
                    boolean isToolbar = false;
                    if (c instanceof JComponent) {
                        JComponent cpm = (JComponent)c;
                        isToolbar = (cpm.getClientProperty(TOOLBAR_MARKER) != null);
                    }
                    //jpanel is the panel with card layout..
                    if (!(c instanceof JPanel) && !isToolbar) {
                        Dimension size = c.getPreferredSize();
                        
                        if (size.height > rowHeight)
                            rowHeight = size.height;
                        
                        Dimension minsize = c.getMinimumSize();
                        if (minsize.height > minRowHeight)
                            minRowHeight = minsize.height;
                        if (minsize.width > minColumnWidth)
                            minColumnWidth = minsize.width;
                        if (size.width > columnWidth)
                            columnWidth = size.width;
                    } else {
                        if (isToolbar) {
                            bar = c;
                        } else {
                            // editorpane/content
                            jsc = c;
                        }
                    }
                }
                // substract 2 because we have the panel and toolbar.
                count -=2;

                // compute column count
                int containerWidth = parent.getWidth();
                
                int projectedToggleWidth = h_margin_left + ((columnWidth + h_gap) * count) + h_margin_right;
                int projectedMinToggleWidth = h_margin_left + ((minColumnWidth + h_gap) * count) + h_margin_right;
                
                int barPrefWidth = (bar == null ? 0 : bar.getPreferredSize().width);
                int barMinWidth = (bar == null ? 0 : bar.getMinimumSize().width);
                int barWidth;
//                System.out.println("barpref=" + barPrefWidth);
//                System.out.println("barmin=" + barMinWidth);
//                System.out.println("columnpref=" + columnWidth);
//                System.out.println("columnpref=" + minColumnWidth);
                // now let's check which size resolution strtategy to use..
                if (containerWidth >= projectedToggleWidth + barPrefWidth) {
                    //we're safe, everything fits on..
//                    System.out.println("safe");
                    barWidth = barPrefWidth;
                } else if (containerWidth >= projectedToggleWidth + barMinWidth) {
                    // for start,let's minimize the toolbar.
//                    System.out.println("minimize1");
                    barWidth = barMinWidth;
                } else if (containerWidth >= projectedMinToggleWidth + barMinWidth) {
                    // last resort with reasonable results.. everything get's minimized.
                    columnWidth = minColumnWidth;
                    barWidth = barMinWidth;
//                    System.out.println("minimize2");
                } else {
                    // give up, nothing can be saved now..
                    int newcolumnWidth = (containerWidth - (count * v_gap)) / (count);
                    if (newcolumnWidth < columnWidth) {
                        columnWidth = newcolumnWidth;
                    }
                    barWidth = 0;
//                    System.out.println("gone..");
                }
                

//                System.out.println("column =" + columnWidth);
                int row = 0;
                int top = v_margin_top;
                int left = h_margin_left;
                // layout the components
                for (int i=0, col=0; i < count+1; i++) {
                    Component c = parent.getComponent(i);
                    if (c != jsc && c != bar) {
                        parent.getComponent(i).setBounds(
                                           h_margin_left + col * (columnWidth + h_gap),
                                           v_margin_top + row * (rowHeight + v_gap),
                                           columnWidth,
                                           rowHeight);
                        top = v_margin_top + row * (rowHeight + v_gap);
                        left = h_margin_left + (col + 1) * (columnWidth + h_gap) ;
                        col++;
                    }
                }
                if (bar != null) {
                    bar.setBounds(left, top, parent.getWidth() - left, rowHeight);
                }
                
                top += rowHeight + 3;
                
                if (jsc != null) {
                    jsc.setBounds (0, top, parent.getWidth(), parent.getHeight() - top);
                }
            }
        }
    }        
    
}

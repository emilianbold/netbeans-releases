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

package org.netbeans.core.multiview;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.KeyEvent;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;
import javax.swing.JToggleButton.ToggleButtonModel;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.text.Keymap;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallbackSystemAction;


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
    
    private static final boolean AQUA = "Aqua".equals(UIManager.getLookAndFeel().getID()); //NOI18N

    private boolean toolbarVisible = true;
    
    /** Creates a new instance of TabsComponent */
    public TabsComponent(boolean toolVis) {
        super();
        bar = AQUA ? new TB() : new JToolBar();
        Border b = (Border)UIManager.get("Nb.Editor.Toolbar.border"); //NOI18N
        bar.setBorder(b);
        bar.setFloatable(false);
        bar.setFocusable(true);
        
        setLayout(new BorderLayout());
        add(bar, BorderLayout.NORTH);
        startToggling();
        setToolbarBarVisible(toolVis);
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
        GridBagLayout grid = new GridBagLayout();
        bar.setLayout(grid);
        JToggleButton active = null;
        int prefHeight = -1;
        int prefWidth = -1;
        for (int i = 0; i < descs.length; i++) {
            JToggleButton button = createButton(descs[i]);
            model.getButtonGroup().add(button);
            GridBagConstraints cons = new GridBagConstraints();
            cons.anchor = GridBagConstraints.WEST;
            prefHeight = Math.max(button.getPreferredSize().height, prefHeight);
            bar.add(button, cons);
            prefWidth = Math.max(button.getPreferredSize().width, prefWidth);
            if (descs[i] == model.getActiveDescription()) {
                active = button;
                
            }
        }
        Enumeration en = model.getButtonGroup().getElements();
        while (en.hasMoreElements()) {
            JToggleButton but = (JToggleButton)en.nextElement();
            Insets ins = but.getBorder().getBorderInsets(but);
            but.setPreferredSize(new Dimension(prefWidth + 10, prefHeight));
            but.setMinimumSize(new Dimension(prefWidth + 10, prefHeight));
            
        }
        if (active != null) {
            active.setSelected(true);
        }
        toolbarPanel = getEmptyInnerToolBar();
        GridBagConstraints cons = new GridBagConstraints();
        cons.anchor = GridBagConstraints.EAST;
        cons.fill = GridBagConstraints.BOTH;
        cons.gridwidth = GridBagConstraints.REMAINDER;
        cons.weightx = 1;

        bar.add(toolbarPanel, cons);
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

        //HACK start - now find the global action shortcut
        Keymap map = (Keymap)Lookup.getDefault().lookup(Keymap.class);
        KeyStroke stroke = null;
        KeyStroke stroke2 = null;
//in tests map can be null, that's why the check..
        if (map != null) {
            // map is null in tests..
            Action[] acts = map.getBoundActions();
            for (int i = 0; i < acts.length;i++) {
                if (acts[i] instanceof CallbackSystemAction) {
                    CallbackSystemAction sa = (CallbackSystemAction)acts[i];
                    if ("NextViewAction".equals(sa.getActionMapKey())) { //NOI18N
                        KeyStroke[] strokes = map.getKeyStrokesForAction(acts[i]);
                        if (strokes != null && strokes.length > 0) {
                            stroke = strokes[0];
                        }
                    }
                    if ("PreviousViewAction".equals(sa.getActionMapKey())) { //NOI18N
                        KeyStroke[] strokes = map.getKeyStrokesForAction(acts[i]);
                        if (strokes != null && strokes.length > 0) {
                            stroke2 = strokes[0];
                        }
                    }
                }
            }
        }
        //HACK end
        String key1 = stroke == null ? "" : KeyEvent.getKeyModifiersText(stroke.getModifiers()) + "+" + KeyEvent.getKeyText(stroke.getKeyCode());//NOI18N
        String key2 = stroke2 == null ? "" : KeyEvent.getKeyModifiersText(stroke2.getModifiers()) + "+" + KeyEvent.getKeyText(stroke2.getKeyCode());//NOI18N
        button.setToolTipText(NbBundle.getMessage(TabsComponent.class, "TabButton.tooltip",//NOI18N
                              description.getDisplayName(), 
                              key1,
                              key2));
        button.setFocusable(true);
        button.setFocusPainted(true);
        return button;
    }

    void setInnerToolBar(JComponent innerbar) {
        synchronized (getTreeLock()) {
            if (toolbarPanel != null) {
                bar.remove(toolbarPanel);
            }
            if (innerbar == null) {
                innerbar = getEmptyInnerToolBar();
            }
            innerbar.putClientProperty(TOOLBAR_MARKER, "X"); //NOI18N
            // need to set it to null, because CloneableEditor set's the border for the editor bar part only..
            if (!AQUA) {
                innerbar.setBorder(null);
            } else {
                innerbar.setBorder (BorderFactory.createEmptyBorder(2, 0, 2, 0));
            }
            toolbarPanel = innerbar;
            if (toolbarPanel != null) {
                GridBagConstraints cons = new GridBagConstraints();
                cons.anchor = GridBagConstraints.EAST;
                cons.fill = GridBagConstraints.BOTH;
                cons.weightx = 1;
                toolbarPanel.setMinimumSize(new Dimension(10, 10));
                cons.gridwidth = GridBagConstraints.REMAINDER;
                
                bar.add(toolbarPanel, cons);
            }
            // rootcycle is the tabscomponent..
//            toolbarPanel.setFocusCycleRoot(false);
            bar.revalidate();
            bar.repaint();
        }
    }
    
    void setToolbarBarVisible(boolean visible) {
        if (toolbarVisible == visible) {
            return;
        }
        toolbarVisible = visible;
        bar.setVisible(visible);
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
    private boolean isWindows = false;
    private Border getButtonBorder() {
        if (buttonBorder == null) {
            //For some lf's, core will supply one
            buttonBorder = UIManager.getBorder ("nb.tabbutton.border"); //NOI18N
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
        
        /** for user triggered clicks, do activate the current element..
            make it on mousePressed to be in synch with the topcpomponent activation code in the winsys impl #68505
         */
        public void mousePressed(MouseEvent e) {
            AbstractButton b = (AbstractButton)e.getComponent();
            MultiViewModel model = TabsComponent.this.model;
            if (model != null) {
                model.getButtonGroup().setSelected(b.getModel(), true);
                model.fireActivateCurrent();
            }

        }
        
    }    
   
    
    private static final class TB extends JToolBar {
        private boolean updating = false;
        
        public TB() {
            //Aqua UI will look for this value to ensure the
            //toolbar is tall enough that the "glow" which paints
            //outside the combo box bounds doesn't make a mess painting
            //into other components
            setName("editorToolbar");
        }
        
        public void setBorder (Border b) {
            if (!updating) {
                return;
            }
            super.setBorder(b);
        }
        
        public void updateUI() {
            updating = true;
            try {
                super.updateUI();
            } finally {
                updating = false;
            }
        }
        
        public String getUIClassID() {
            return UIManager.get("Nb.Toolbar.ui") == null ?
                super.getUIClassID() : "Nb.Toolbar.ui";
        }
    }
}

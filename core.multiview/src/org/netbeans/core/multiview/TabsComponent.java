/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.core.multiview;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;
import javax.swing.JToggleButton.ToggleButtonModel;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.awt.Actions;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;


/**
 * Temporary solution tomultiview tabs..
 * @author  mkleint
 */
class TabsComponent extends JPanel {
    
    private JComponent EMPTY;
    private final static String TOOLBAR_MARKER = "MultiViewPanel"; //NOI18N
    
    MultiViewModel model;
    private MouseListener buttonMouseListener = null;
    private JComponent toolbarPanel;
    private JComponent toolbarPanelSplit;
    JPanel componentPanel; /** package private for tests */
    JPanel componentPanelSplit; /** package private for tests */
    private CardLayout cardLayout;
    private CardLayout cardLayoutSplit;
    private Set<MultiViewElement> alreadyAddedElements;
    private Set<MultiViewElement> alreadyAddedElementsSplit;
    private JToolBar bar;
    private JToolBar barSplit;

    private JSplitPane splitPane;
    private AWTEventListener awtEventListener;
    private boolean isTopLeft = true;
    private JPanel topLeftComponent;
    private JPanel bottomRightComponent;
    private Dimension barMinSize;
    private Dimension panelMinSizep;
    private MultiViewDescription[] topBottomDescriptions = null;
    private PropertyChangeListener splitterPropertyChangeListener;
    private boolean removeSplit = false;
    private MultiViewPeer mvPeer;
    private boolean hiddenTriggeredByMultiViewButton = false;
    
    private static final boolean AQUA = "Aqua".equals(UIManager.getLookAndFeel().getID()); //NOI18N

    private boolean toolbarVisible = true;
    
    /** Creates a new instance of TabsComponent */
    public TabsComponent(boolean toolVis) {
        super();
        bar = new JToolBar();
        Border b = (Border)UIManager.get("Nb.Editor.Toolbar.border"); //NOI18N
        bar.setBorder(b);
        bar.setFloatable(false);
        bar.setFocusable(true);
        if( "Windows".equals( UIManager.getLookAndFeel().getID()) 
                && !isXPTheme()) {
            bar.setRollover(true);
        } else if( AQUA ) {
            bar.setBackground(UIManager.getColor("NbExplorerView.background"));
        }
        
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
        alreadyAddedElements = new HashSet<MultiViewElement>();
        
        MultiViewDescription[] descs = model.getDescriptions();
        MultiViewDescription def = model.getActiveDescription();
        GridBagLayout grid = new GridBagLayout();
        bar.setLayout(grid);
        JToggleButton active = null;
        int prefHeight = -1;
        int prefWidth = -1;
        for (int i = 0; i < descs.length; i++) {
	    boolean shouldCreateToggleButton = true;
	    if(descs[i] instanceof ContextAwareDescription) {
		shouldCreateToggleButton = !((ContextAwareDescription)descs[i]).isSplitDescription();
	    }
	    if (shouldCreateToggleButton) {
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

    
    MultiViewDescription getTopComponentDescription() {
	return topBottomDescriptions == null ? model.getActiveDescription() : topBottomDescriptions[0];
    }

    MultiViewDescription getBottomComponentDescription() {
	return topBottomDescriptions == null ? model.getActiveDescription() : topBottomDescriptions[1];
    }

    void peerClearSplit() {
	MultiViewDescription activeDescription = topBottomDescriptions[0];
	Toolkit.getDefaultToolkit().removeAWTEventListener(getAWTEventListener());
	splitPane.removePropertyChangeListener(splitterPropertyChangeListener);
	removeAll();
	splitPane = null;
	topBottomDescriptions = null;
	isTopLeft = true;
	topLeftComponent = null;
	bottomRightComponent = null;
	alreadyAddedElementsSplit = null;
	awtEventListener = null;
	barSplit = null;
	cardLayoutSplit = null;
	componentPanelSplit = null;
	toolbarPanelSplit = null;
	splitterPropertyChangeListener = null;
	mvPeer = null;
	
	add(bar, BorderLayout.NORTH);
        add(componentPanel, BorderLayout.CENTER);
	model.setActiveDescription(activeDescription);
    }

    private void setupSplit() {
	topLeftComponent = new JPanel(new BorderLayout());
	barMinSize = bar.getMinimumSize();
	panelMinSizep = componentPanel.getMinimumSize();
	topLeftComponent.add(bar, BorderLayout.NORTH);
        topLeftComponent.add(componentPanel, BorderLayout.CENTER);

	bottomRightComponent = new JPanel();
	barSplit = new JToolBar();
        Border b = (Border)UIManager.get("Nb.Editor.Toolbar.border"); //NOI18N
        barSplit.setBorder(b);
        barSplit.setFloatable(false);
        barSplit.setFocusable(true);
        if( "Windows".equals( UIManager.getLookAndFeel().getID())
                && !isXPTheme()) {
            barSplit.setRollover(true);
        } else if( AQUA ) {
            barSplit.setBackground(UIManager.getColor("NbExplorerView.background"));
        }

        bottomRightComponent.setLayout(new BorderLayout());
        bottomRightComponent.add(barSplit, BorderLayout.NORTH);
        startTogglingSplit();
        setToolbarBarVisibleSplit(bar.isVisible());

        componentPanelSplit = new JPanel();
        cardLayoutSplit = new CardLayout();
        componentPanelSplit.setLayout(cardLayoutSplit);
        bottomRightComponent.add(componentPanelSplit, BorderLayout.CENTER);
        alreadyAddedElementsSplit = new HashSet<MultiViewElement>();

        MultiViewDescription[] descs = model.getDescriptions();
        MultiViewDescription def = model.getActiveDescription();
        GridBagLayout grid = new GridBagLayout();
        barSplit.setLayout(grid);
	JToggleButton activeSplit = null;
        int prefHeight = -1;
        int prefWidth = -1;
        for (int i = 0; i < descs.length; i++) {
	    if (descs[i] instanceof ContextAwareDescription && ((ContextAwareDescription)descs[i]).isSplitDescription()) {
		JToggleButton button = createButton(descs[i]);
		model.getButtonGroupSplit().add(button);
		GridBagConstraints cons = new GridBagConstraints();
		cons.anchor = GridBagConstraints.WEST;
		prefHeight = Math.max(button.getPreferredSize().height, prefHeight);
		barSplit.add(button, cons);
		prefWidth = Math.max(button.getPreferredSize().width, prefWidth);
		if (descs[i].getDisplayName().startsWith(def.getDisplayName())) {
		    activeSplit = button;
		}
	    }
        }
        Enumeration en = model.getButtonGroupSplit().getElements();
        while (en.hasMoreElements()) {
            JToggleButton but = (JToggleButton)en.nextElement();
            Insets ins = but.getBorder().getBorderInsets(but);
            but.setPreferredSize(new Dimension(prefWidth + 10, prefHeight));
            but.setMinimumSize(new Dimension(prefWidth + 10, prefHeight));

        }
        if (activeSplit != null) {
            activeSplit.setSelected(true);
        }

        toolbarPanelSplit = getEmptyInnerToolBar();
        GridBagConstraints cons = new GridBagConstraints();
        cons.anchor = GridBagConstraints.EAST;
        cons.fill = GridBagConstraints.BOTH;
        cons.gridwidth = GridBagConstraints.REMAINDER;
        cons.weightx = 1;

        barSplit.add(toolbarPanelSplit, cons);
    }

    void peerSplitComponent(int orientation, MultiViewPeer mvPeer, MultiViewDescription defaultDesc, MultiViewDescription defaultDescClone) {
	MultiViewDescription[] descriptions = model.getDescriptions();
	this.mvPeer = mvPeer;

	MultiViewDescription activeDescription = model.getActiveDescription();
	if (splitPane == null) {
	    splitPane = new JSplitPane();
	    topBottomDescriptions = new MultiViewDescription[2];
	    if (defaultDesc != null && defaultDescClone != null) {// called during deserialization
		topBottomDescriptions[0] = defaultDesc;
		topBottomDescriptions[1] = defaultDescClone;
	    } else {
		int activeDescIndex = 0;
		for (int i = 0; i < descriptions.length; i++) {
		    MultiViewDescription multiViewDescription = descriptions[i];
		    if (multiViewDescription.getDisplayName().equals(activeDescription.getDisplayName())) {
			activeDescIndex = i;
			break;
		    }
		}
		topBottomDescriptions[0] = descriptions[activeDescIndex];
		topBottomDescriptions[1] = descriptions[activeDescIndex + 1];
	    }
	    
	    setupSplit();
	    splitPane.setOneTouchExpandable(true);
	    splitPane.setContinuousLayout(true);
	    splitPane.setResizeWeight(0.5);
            splitPane.setBorder( BorderFactory.createEmptyBorder() );

	    removeAll();
	    Toolkit.getDefaultToolkit().addAWTEventListener(getAWTEventListener(), MouseEvent.MOUSE_EVENT_MASK | MouseEvent.MOUSE_MOTION_EVENT_MASK);
	    add(splitPane, BorderLayout.CENTER);


	    MultiViewDescription bottomDescription = topBottomDescriptions[1];
	    isTopLeft = false;
	    model.setActiveDescription(bottomDescription);
	    if (defaultDesc != null && defaultDescClone != null) {// called during deserialization
		selecteAppropriateButton();
	    }

	    MultiViewDescription topDescription = topBottomDescriptions[0];
	    isTopLeft = true;
	    model.setActiveDescription(topDescription);
	    if (defaultDesc != null && defaultDescClone != null) {// called during deserialization
		selecteAppropriateButton();
	    }
	} else {
	    topLeftComponent = (JPanel) splitPane.getTopComponent();
	    bottomRightComponent = (JPanel) splitPane.getBottomComponent();
	}
	if(orientation != splitPane.getOrientation()) {
	    splitPane.removePropertyChangeListener(splitterPropertyChangeListener);
	    splitterPropertyChangeListener = null;
	}
	splitPane.setOrientation(orientation);
	splitPane.setTopComponent(topLeftComponent);
	splitPane.setBottomComponent(bottomRightComponent);
	splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, getSplitterPropertyChangeListener(orientation));
	topLeftComponent.setMinimumSize(new Dimension(0, 0));
	bottomRightComponent.setMinimumSize(new Dimension(0, 0));
    }

    private void selecteAppropriateButton() {
	Enumeration en = model.getButtonGroupSplit().getElements();
	while (en.hasMoreElements()) {
	    JToggleButton but = (JToggleButton) en.nextElement();
	    MultiViewDescription buttonsDescription = ((TabsButtonModel) but.getModel()).getButtonsDescription();
	    if (buttonsDescription == (isTopLeft ? topBottomDescriptions[0] : topBottomDescriptions[1])) {
		but.setSelected(true);
	    }
	}
    }

    private PropertyChangeListener getSplitterPropertyChangeListener(final int orientation) {
	if (splitterPropertyChangeListener == null) {
	    splitterPropertyChangeListener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent pce) {
		    if (splitPane != null) {
			int current = Integer.parseInt(pce.getNewValue().toString());
			int dividerSize = splitPane.getDividerSize();
			int splitSize;
			int topMinSize;
			int bottomMinSize;
			if (orientation == JSplitPane.VERTICAL_SPLIT) {
			    splitSize = splitPane.getHeight();
			    topMinSize = (int) topLeftComponent.getMinimumSize().getHeight();
			    bottomMinSize = (int) bottomRightComponent.getMinimumSize().getHeight();
			} else {
			    splitSize = splitPane.getWidth();
			    topMinSize = (int) topLeftComponent.getMinimumSize().getWidth();
			    bottomMinSize = (int) bottomRightComponent.getMinimumSize().getWidth();
			}
			int min = topMinSize;
			int max = splitSize - bottomMinSize - dividerSize;
			if (current <= min || current >= max) {
			    removeSplit = true;
			} else {
			    removeSplit = false;
			}
		    }
		}
	    };
	}
	return splitterPropertyChangeListener;
    }


    @NbBundle.Messages({"LBL_ClearAllSplitsDialogMessage=Do you really want to clear the split?",
	"LBL_ClearAllSplitsDialogTitle=Clear Split"})
    private AWTEventListener getAWTEventListener() {
	if (awtEventListener == null) {
	    awtEventListener = new AWTEventListener() {
                @Override
                public void eventDispatched(AWTEvent event) {
                    MouseEvent e = (MouseEvent) event;
		    if (splitPane != null && e.getID() == MouseEvent.MOUSE_PRESSED) {
			MultiViewDescription activeDescription = null;
			Point locationOnScreen = e.getLocationOnScreen();
			SwingUtilities.convertPointFromScreen(locationOnScreen, splitPane);
			Component component = e.getComponent();
			while(component != null && component != splitPane) {
			    component = component.getParent();
			}
			if (component == splitPane && splitPane.getTopComponent().getBounds().contains(locationOnScreen)) {
			    isTopLeft = true;
			    activeDescription = topBottomDescriptions[0];
			} else if (component == splitPane && splitPane.getBottomComponent().getBounds().contains(locationOnScreen)) {
			    isTopLeft = false;
			    activeDescription = topBottomDescriptions[1];
			}
			if (activeDescription != null) {
			    if (!model.getActiveDescription().equals(activeDescription) ||
				    ((ContextAwareDescription)model.getActiveDescription()).isSplitDescription() != ((ContextAwareDescription)activeDescription).isSplitDescription()) {
				model.setActiveDescription(activeDescription);
			    }
			}
		    } else if (splitPane != null && e.getID() == MouseEvent.MOUSE_RELEASED) {
			if (removeSplit) {
			    if (e.getClickCount() != 0) {
				return;
			    }
			    if (mvPeer != null) {
				mvPeer.peerClearSplit();
				bar.setMinimumSize(barMinSize);
				componentPanel.setMinimumSize(panelMinSizep);
			    }
			    removeSplit = false;
			}
		    }
                }
            };
        }
        return awtEventListener;
    }

    private void changeSplitOrientation(int orientation) {
	splitPane.removePropertyChangeListener(splitterPropertyChangeListener);
	splitterPropertyChangeListener = null;
	splitPane.setOrientation(orientation);
	splitPane.setDividerLocation(0.5);
	splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, getSplitterPropertyChangeListener(orientation));
    }

    boolean isHiddenTriggeredByMultiViewButton() {
	return hiddenTriggeredByMultiViewButton;
    }

    boolean isShowing(MultiViewDescription descr) {
	if(descr == null) {
	    return false;
	}
	if(splitPane == null) {
	    return model.getActiveDescription() == descr;
	}
	return isTopLeft ? topBottomDescriptions[1] == descr : topBottomDescriptions[0] == descr;
    }

    void switchToCard(MultiViewElement elem, String id, boolean isSplitElement) {
	if (isSplitElement) {
	    switchToCardSplit(elem, id);
	    return;
	}
        if (! alreadyAddedElements.contains(elem)) {
            componentPanel.add(elem.getVisualRepresentation(), id);
            alreadyAddedElements.add(elem);
        }
        cardLayout.show(componentPanel, id);
    }

    private void switchToCardSplit(MultiViewElement elem, String id) {
	if (!alreadyAddedElementsSplit.contains(elem)) {
	    componentPanelSplit.add(elem.getVisualRepresentation(), id);
	    alreadyAddedElementsSplit.add(elem);
	}
	cardLayoutSplit.show(componentPanelSplit, id);
    }

    /** Part of 130919 fix - don't hold visual representations after close */
    void peerComponentClosed() {
        if (componentPanel != null) {
            componentPanel.removeAll();
        }
        if (alreadyAddedElements != null) {
            alreadyAddedElements.clear();
        }
        if (componentPanelSplit != null) {
            componentPanelSplit.removeAll();
        }
        if (alreadyAddedElementsSplit != null) {
            alreadyAddedElementsSplit.clear();
        }
    }
    
    void changeActiveManually(MultiViewDescription desc) {
        Enumeration en = model.getButtonGroup().getElements();
	MultiViewDescription activeDescription = model.getActiveDescription();
	if (activeDescription instanceof ContextAwareDescription && ((ContextAwareDescription) activeDescription).isSplitDescription()) {
	    en = model.getButtonGroupSplit().getElements();
	}
        while (en.hasMoreElements()) {
            JToggleButton obj = (JToggleButton)en.nextElement();
            
            if (obj.getModel() instanceof TabsComponent.TabsButtonModel) {
                TabsButtonModel btnmodel = (TabsButtonModel)obj.getModel();
                if (btnmodel.getButtonsDescription().getDisplayName().equals(desc.getDisplayName())) {
		    if (splitPane != null) {
			TabsComponent.this.hiddenTriggeredByMultiViewButton = true;
			if (((ContextAwareDescription) activeDescription).isSplitDescription()) {
			    model.getButtonGroupSplit().setSelected(obj.getModel(), true);
			    TabsComponent.this.isTopLeft = false;
			    TabsComponent.this.topBottomDescriptions[1] = btnmodel.getButtonsDescription();
			} else {
			    model.getButtonGroup().setSelected(obj.getModel(), true);
			    TabsComponent.this.isTopLeft = true;
			    TabsComponent.this.topBottomDescriptions[0] = btnmodel.getButtonsDescription();
			}
			model.fireActivateCurrent();
			TabsComponent.this.hiddenTriggeredByMultiViewButton = false;
		    } else {
			obj.setSelected(true);
			MultiViewElement elem = model.getElementForDescription(desc);
			elem.getVisualRepresentation().requestFocus();
		    }
                    break;
                }
            }
        }
    }

    void changeVisibleManually(MultiViewDescription desc) {
        Enumeration en = model.getButtonGroup().getElements();
	MultiViewDescription activeDescription = model.getActiveDescription();
	if (activeDescription instanceof ContextAwareDescription && ((ContextAwareDescription) activeDescription).isSplitDescription()) {
	    en = model.getButtonGroupSplit().getElements();
	    desc = model.getActiveDescription();
	}
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
        final JToggleButton button = new JToggleButton();
        Mnemonics.setLocalizedText(button, Actions.cutAmpersand(description.getDisplayName()));
        button.setModel(new TabsButtonModel(description));
        button.setRolloverEnabled(true);
        Border b = (getButtonBorder());
        if (b != null) {
           button.setBorder(b);
        }
        if( AQUA ) {
            button.putClientProperty("JButton.buttonType", "square");
            button.putClientProperty("JComponent.sizeVariant", "small");
        }
          
        if (buttonMouseListener == null) {
            buttonMouseListener = new ButtonMouseListener();
        }
        button.addMouseListener (buttonMouseListener);
        button.setToolTipText(NbBundle.getMessage(TabsComponent.class, "TabButton.tool_tip", button.getText()));
        button.setFocusable(true);
        button.setFocusPainted(true);
        return button;
    }

    void setInnerToolBar(JComponent innerbar, boolean isSplitElement) {
	if (isSplitElement) {
	    setInnerToolBarSplit(innerbar);
	    return;
	}
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
            GridBagConstraints cons = new GridBagConstraints();
            cons.anchor = GridBagConstraints.EAST;
            cons.fill = GridBagConstraints.BOTH;
            cons.gridwidth = GridBagConstraints.REMAINDER;
            cons.weightx = 1;
            toolbarPanel.setMinimumSize(new Dimension(10, 10));
//            cons.gridwidth = GridBagConstraints.REMAINDER;

            bar.add(toolbarPanel, cons);

            // rootcycle is the tabscomponent..
//            toolbarPanel.setFocusCycleRoot(false);
            bar.revalidate();
            bar.repaint();
        }
    }

    private void setInnerToolBarSplit(JComponent innerbar) {
        synchronized (getTreeLock()) {
            if (toolbarPanelSplit != null) {
                barSplit.remove(toolbarPanelSplit);
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
            toolbarPanelSplit = innerbar;
            GridBagConstraints cons = new GridBagConstraints();
            cons.anchor = GridBagConstraints.EAST;
            cons.fill = GridBagConstraints.BOTH;
            cons.gridwidth = GridBagConstraints.REMAINDER;
            cons.weightx = 1;
            toolbarPanelSplit.setMinimumSize(new Dimension(10, 10));
//            cons.gridwidth = GridBagConstraints.REMAINDER;

            barSplit.add(toolbarPanelSplit, cons);

            // rootcycle is the tabscomponent..
//            toolbarPanel.setFocusCycleRoot(false);
            barSplit.revalidate();
            barSplit.repaint();
        }
    }
    
    void setToolbarBarVisible(boolean visible) {
        if (toolbarVisible == visible) {
            return;
        }
        toolbarVisible = visible;
        bar.setVisible(visible);
    }

    void setToolbarBarVisibleSplit(boolean visible) {
        if (toolbarVisible == visible) {
            return;
        }
        toolbarVisible = visible;
        barSplit.setVisible(visible);
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
        map.put("navigateRight", act);//NOI18N
        InputMap input = bar.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        
        act = new TogglesGoWestAction();
        // JToolbar action name
        map.put("navigateLeft", act);//NOI18N
        
        act = new TogglesGoDownAction();
        map.put("TogglesGoDown", act);//NOI18N
        // JToolbar action name
        map.put("navigateUp", act);//NOI18N
        KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE"); //NOI18N
        input.put(stroke, "TogglesGoDown");//NOI18N
    }

    void startTogglingSplit() {
        ActionMap map = barSplit.getActionMap();
        Action act = new TogglesGoEastAction();
        // JToolbar action name
        map.put("navigateRight", act);//NOI18N
        InputMap input = barSplit.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        act = new TogglesGoWestAction();
        // JToolbar action name
        map.put("navigateLeft", act);//NOI18N

        act = new TogglesGoDownAction();
        map.put("TogglesGoDown", act);//NOI18N
        // JToolbar action name
        map.put("navigateUp", act);//NOI18N
        KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE"); //NOI18N
        input.put(stroke, "TogglesGoDown");//NOI18N
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
        @Override
        public void mouseEntered(MouseEvent e) {
            AbstractButton b = (AbstractButton)e.getComponent();
            b.getModel().setRollover(true);
        }
        @Override
        public void mouseExited(MouseEvent e) {
            AbstractButton b = (AbstractButton)e.getComponent();
            b.getModel().setRollover(false);
        }
        
        /** for user triggered clicks, do activate the current element..
            make it on mousePressed to be in synch with the topcpomponent activation code in the winsys impl #68505
         */
        @Override
        public void mousePressed(MouseEvent e) {
            e.consume();
            AbstractButton b = (AbstractButton)e.getComponent();
            MultiViewModel model = TabsComponent.this.model;
            if (model != null) {
		if (TabsComponent.this.splitPane != null) {
		    ButtonModel buttonModel = b.getModel();
		    if (buttonModel instanceof TabsButtonModel) {
			MultiViewDescription buttonsDescription = ((TabsButtonModel) buttonModel).getButtonsDescription();
			TabsComponent.this.hiddenTriggeredByMultiViewButton = true;
			if(((ContextAwareDescription)buttonsDescription).isSplitDescription()) {
			    model.getButtonGroupSplit().setSelected(b.getModel(), true);
			    TabsComponent.this.isTopLeft = false;
			    TabsComponent.this.topBottomDescriptions[1] = buttonsDescription;
			} else {
			    model.getButtonGroup().setSelected(b.getModel(), true);
			    TabsComponent.this.isTopLeft = true;
			    TabsComponent.this.topBottomDescriptions[0] = buttonsDescription;
			}
			model.fireActivateCurrent();
			TabsComponent.this.hiddenTriggeredByMultiViewButton = false;
		    }
		    return;
		}
                model.getButtonGroup().setSelected(b.getModel(), true);
                model.fireActivateCurrent();
            }

        }
        
    }    
}

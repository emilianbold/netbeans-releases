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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.core.windows.view.ui.slides;

import java.awt.Component;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.DefaultSingleSelectionModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SingleSelectionModel;
import javax.swing.event.ChangeListener;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.actions.ActionUtils;
import org.netbeans.core.windows.view.ui.Tabbed;
import org.netbeans.swing.tabcontrol.DefaultTabDataModel;
import org.netbeans.core.windows.view.dnd.DragAndDropFeedbackVisualizer;
import org.netbeans.swing.tabcontrol.SlideBarDataModel;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabDataModel;
import org.netbeans.swing.tabcontrol.TabbedContainer;
import org.openide.util.ChangeSupport;
import org.openide.windows.TopComponent;

/*
 * Adapts SlideBar to match Tabbed interface, which is used by TabbedHandler
 * for talking to component containers. SlideBar is driven indirectly,
 * through modifications of its model.
 *
 * @author Dafe Simonek
 */
public final class TabbedSlideAdapter implements Tabbed {
    
    /** data model of informations about top components in container */
    private TabDataModel dataModel;
    /** selection model which contains selection info in container */
    private SingleSelectionModel selModel;
    /** Visual component for displaying box for sliding windows */
    private SlideBar slideBar;
    /** List of action listeners */
    private List<ActionListener> actionListeners;
    private final ChangeSupport cs = new ChangeSupport(this);
    
    /** Creates a new instance of SlideBarTabs */
    public TabbedSlideAdapter(String side) {
        dataModel = new SlideBarDataModel.Impl();
        setSide(side);
        selModel = new DefaultSingleSelectionModel();
        slideBar = new SlideBar(this, (SlideBarDataModel)dataModel, selModel);
    }
    
    public void requestAttention (TopComponent tc) {
        slideBar.setBlinking(tc, true);
    }
    
    public void cancelRequestAttention (TopComponent tc) {
        slideBar.setBlinking(tc, false);
    }
    

    private void setSide (String side) {
        int orientation = SlideBarDataModel.WEST;
        if (Constants.LEFT.equals(side)) {
            orientation = SlideBarDataModel.WEST;
        } else if (Constants.RIGHT.equals(side)) {
            orientation = SlideBarDataModel.EAST;
        } else if (Constants.BOTTOM.equals(side)) {
            orientation = SlideBarDataModel.SOUTH;
        }
        ((SlideBarDataModel)dataModel).setOrientation(orientation);
    }

    public final synchronized void addActionListener(ActionListener listener) {
        if (actionListeners == null) {
            actionListeners = new ArrayList<ActionListener>();
        }
        actionListeners.add(listener);
    }

    /**
     * Remove an action listener.
     *
     * @param listener The listener to remove.
     */
    public final synchronized void removeActionListener(ActionListener listener) {
        if (actionListeners != null) {
            actionListeners.remove(listener);
            if (actionListeners.isEmpty()) {
                actionListeners = null;
            }
        }
    }

    final void postActionEvent(ActionEvent event) {
        List<ActionListener> list;
        synchronized (this) {
            if (actionListeners == null)
                return;
            list = Collections.unmodifiableList(actionListeners);
        }
        for (int i = 0; i < list.size(); i++) {
            list.get(i).actionPerformed(event);
        }
    }
    
    public void addChangeListener(ChangeListener listener) {
        cs.addChangeListener(listener);
    }    
    
    public void removeChangeListener(ChangeListener listener) {
        cs.removeChangeListener(listener);
    }
    
    final void postSelectionEvent() {
        cs.fireChange();
    }
    
    public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
        slideBar.addPropertyChangeListener(name, listener);
    }
    
    public void removePropertyChangeListener(String name, PropertyChangeListener listener) {
        slideBar.removePropertyChangeListener(name, listener);
    }
    
    public void addTopComponent(String name, Icon icon, TopComponent tc, String toolTip) {
        dataModel.addTab(dataModel.size(), new TabData(tc, icon, name, toolTip));
    }
    
    public TopComponent getSelectedTopComponent() {
        int index = selModel.getSelectedIndex();
        return index < 0 ? null : (TopComponent)dataModel.getTab(index).getComponent();
    }
    
    public TopComponent getTopComponentAt(int index) {
        return (TopComponent)dataModel.getTab(index).getComponent();
    }
    
    public TopComponent[] getTopComponents() {
        int size = dataModel.size();
        TopComponent[] result = new TopComponent[size];
        for (int i=0; i < size; i++) {
            result[i] = (TopComponent) dataModel.getTab(i).getComponent();
        }
        return result;
    }
    
    public void setActive(boolean active) {
        slideBar.setActive(active);
    }
    
    public void setIconAt(int index, Icon icon) {
        dataModel.setIcon(index, icon);
    }
    
    public void setTitleAt(int index, String title) {
        dataModel.setText(index, title);
    }
    
    public void setToolTipTextAt(int index, String toolTip) {
        // XXX - not supported yet
    }
    
    public void setTopComponents(TopComponent[] tcs, TopComponent selected) {
        TabData[] data = new TabData[tcs.length];
        int toSelect=-1;
        for(int i = 0; i < tcs.length; i++) {
            TopComponent tc = tcs[i];
            Image icon = tc.getIcon();
            String displayName = WindowManagerImpl.getInstance().getTopComponentDisplayName(tc);
            data[i] = new TabData(
                tc,
                icon == null ? null : new ImageIcon(icon),
                displayName == null ? "" : displayName, // NOI18N
                tc.getToolTipText());
            if (selected == tcs[i]) {
                toSelect = i;
            }
        }

        dataModel.setTabs(data);
        setSelectedComponent(selected);
    }
    
    public int getTabCount() {
        return dataModel.size();
    }    
    
    public int indexOf(Component tc) {
        int size = dataModel.size();
        for (int i=0; i < size; i++) {
            if (tc == dataModel.getTab(i).getComponent()) return i;
        }
        return -1;
    }
    
    public void insertComponent(String name, Icon icon, Component comp, String toolTip, int position) {
        dataModel.addTab(position, new TabData(comp, icon, name, toolTip));
    }
    
    public void removeComponent(Component comp) {
        int i = indexOf(comp);
        dataModel.removeTab(i);
    }
    
    public void setSelectedComponent(Component comp) {
        int newIndex = indexOf(comp);
        if (selModel.getSelectedIndex() != newIndex) {
            selModel.setSelectedIndex(newIndex);
        }
        if (comp instanceof TopComponent) {
            //Inelegant to do this here, but it guarantees blinking stops
            TopComponent tc = (TopComponent) comp;
            tc.cancelRequestAttention();
        }
    }
    
    public int tabForCoordinate(Point p) {
        return slideBar.tabForCoordinate(p.x, p.y);
    }
    
    public Component getComponent() {
        return slideBar;
    }

/*************** No DnD support yet **************/
    
    public Object getConstraintForLocation(Point location, boolean attachingPossible) {
        int tab = slideBar.nextTabForCoordinate(location.x, location.y);
        return Integer.valueOf(tab);
    }
    
    public Shape getIndicationForLocation(Point location, TopComponent startingTransfer, Point startingPoint, boolean attachingPossible) {
        
//        int tab = tabForCoordinate(location);
        int nextTab = slideBar.nextTabForCoordinate(location.x, location.y);
        SlideBarDataModel sbdm = (SlideBarDataModel)dataModel;
        if (getTabCount() != 0) {
            if (nextTab == 0) {
                Rectangle rect = getTabBounds(0);
                if (sbdm.getOrientation() == SlideBarDataModel.SOUTH) {
                    rect.x = 0;
                    rect.width = rect.width / 2;
                } else {
                    rect.y = 0;
                    rect.height = rect.height / 2;
                }
                return rect;
            } else if (nextTab < getTabCount()) {
                Rectangle rect1 = getTabBounds(nextTab - 1);
                Rectangle rect2 = getTabBounds(nextTab);
                Rectangle result = new Rectangle();
                if (sbdm.getOrientation() == SlideBarDataModel.SOUTH) {
                    result.y = rect1.y;
                    result.height = rect1.height;
                    result.x = rect1.x + (rect1.width / 2);
                    result.width = rect2.x + (rect2.width / 2) - result.x;
                } else {
                    result.x = rect1.x;
                    result.width = rect1.width;
                    result.y = rect1.y + (rect1.height / 2);
                    result.height = rect2.y + (rect2.height / 2) - result.y;
                }
                return result;
            } else if (nextTab == getTabCount()) {
                Rectangle rect = getTabBounds(getTabCount() - 1);
                if (sbdm.getOrientation() == SlideBarDataModel.SOUTH) {
                    rect.x = rect.x + rect.width;
                } else {
                    rect.y = rect.y + rect.height;
                }
                return rect;
            }
        } 
        Rectangle rect = slideBar.getBounds();
        if (sbdm.getOrientation() == SlideBarDataModel.SOUTH) {
            return new Rectangle(10, 0, 50, 20);
        }
        return new Rectangle(0, 10, 20, 50);
    }
    
    public Image createImageOfTab(int tabIndex) {
        TabData dt = slideBar.getModel().getTab(tabIndex);
        if (dt.getComponent() instanceof TopComponent) {
            DefaultTabDataModel tempModel = new DefaultTabDataModel( new TabData[] { dt } );
            TabbedContainer temp = new TabbedContainer( tempModel, TabbedContainer.TYPE_VIEW );
            temp.setSize( 300,300 );
            
            return temp.createImageOfTab(0);
        }
        
        return null;
    }
    
    public DragAndDropFeedbackVisualizer getDragAndDropFeedbackVisualizer( int tabIndex ) {
        slideBar.getSelectionModel().setSelectedIndex(tabIndex);
        return new DragAndDropFeedbackVisualizer( this, tabIndex );
//        TabData dt = slideBar.getModel().getTab(tabIndex);
//        if (dt.getComponent() instanceof TopComponent) {
//            DefaultTabDataModel tempModel = new DefaultTabDataModel( new TabData[] { dt } );
//            TabbedContainer temp = new TabbedContainer( tempModel, TabbedContainer.TYPE_VIEW );
//JWindow w = new JWindow();
//w.setBounds(-2000, -2000, 300, 300);
//w.getContentPane().add( temp );     
//w.setVisible(true);       
////temp.setSize( 300,300 );
////temp.setLocation( -10000,-10000);
////temp.setVisible(true);
////temp.invalidate();
////temp.revalidate();
////temp.repaint();
////            temp.updateUI();
////temp.getSelectionModel().setSelectedIndex(0);
//            
//    Window drag = temp.createDragWindow(0);
////    w.dispose();
//return drag;
//        }
//        
//        return null;
    }
    
    /** Add action for disabling slide */
    public Action[] getPopupActions(Action[] defaultActions, int tabIndex) {
        boolean isMDI = WindowManagerImpl.getInstance().getEditorAreaState() == Constants.EDITOR_AREA_JOINED;
        Action[] result = new Action[defaultActions.length + (isMDI ? 2 : 0)];
        System.arraycopy(defaultActions, 0, result, 0, defaultActions.length);
        if (isMDI) {
            result[defaultActions.length-1] = 
                new ActionUtils.AutoHideWindowAction(slideBar, tabIndex, true);
            result[defaultActions.length] = 
                new ActionUtils.ToggleWindowTransparencyAction(slideBar, tabIndex, slideBar.isSlidedTabTransparent());
        }
        return result;
    }
    
    public Rectangle getTabBounds(int tabIndex) {
        return slideBar.getTabBounds(tabIndex);
    }

    public boolean isTransparent() {
        return false;
    }

    public void setTransparent(boolean transparent) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}


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

package org.netbeans.editor.ext;


import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.Iterator;
import java.util.Map;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.SettingsChangeListener;
import org.netbeans.editor.SettingsChangeEvent;
import org.netbeans.editor.Utilities;


/**
* Pane displaying the completion view and accompanying components
* like label for title etc.
*
* @author Miloslav Metelka, Martin Roskanin
* @version 1.00
*/

public class ScrollCompletionPane extends JScrollPane implements ExtCompletionPane,
    PropertyChangeListener, SettingsChangeListener {

    private ExtEditorUI extEditorUI;

    private JComponent view;

    private JLabel topLabel;

    private Dimension minSize;

    private Dimension maxSize;

    private ViewMouseListener viewMouseL;

    private Dimension scrollBarSize;
    
    private Dimension minSizeDefault;

    public ScrollCompletionPane(ExtEditorUI extEditorUI) {
        this.extEditorUI = extEditorUI;

        // Compute size of the scrollbars
        Dimension smallSize = getPreferredSize();
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_ALWAYS);
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
        scrollBarSize = getPreferredSize();
        scrollBarSize.width -= smallSize.width;
        scrollBarSize.height -= smallSize.height;
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_AS_NEEDED);

        // Make it invisible initially
        super.setVisible(false);

        // Add the title component
        installTitleComponent();

        // Add the completion view
        CompletionView completionView = extEditorUI.getCompletion().getView();
        if (completionView instanceof JComponent) {
            view = (JComponent)completionView;
            setViewportView(view);
        }

        // Prevent the bug with displaying without the scrollbar
        getViewport().setMinimumSize(new Dimension(4,4));

        Settings.addSettingsChangeListener(this);

        viewMouseL = new ViewMouseListener();
        synchronized (extEditorUI.getComponentLock()) {
            // if component already installed in ExtEditorUI simulate installation
            JTextComponent component = extEditorUI.getComponent();
            if (component != null) {
                propertyChange(new PropertyChangeEvent(extEditorUI,
                                                       ExtEditorUI.COMPONENT_PROPERTY, null, component));
            }

            extEditorUI.addPropertyChangeListener(this);
        }
        
        putClientProperty ("HelpID", ScrollCompletionPane.class.getName ()); // !!! NOI18N
    }
    
    
    public void settingsChange(SettingsChangeEvent evt) {
        Class kitClass = Utilities.getKitClass(extEditorUI.getComponent());

        if (kitClass != null) {
            minSize = (Dimension)SettingsUtil.getValue(kitClass,
                      ExtSettingsNames.COMPLETION_PANE_MIN_SIZE,
                      ExtSettingsDefaults.defaultCompletionPaneMinSize);
            minSizeDefault = new Dimension(minSize);
            setMinimumSize(minSize);
            
            maxSize = (Dimension)SettingsUtil.getValue(kitClass,
                      ExtSettingsNames.COMPLETION_PANE_MAX_SIZE,
                      ExtSettingsDefaults.defaultCompletionPaneMaxSize);
            setMaximumSize(maxSize);
            
        }
    }

    
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();

        if (ExtEditorUI.COMPONENT_PROPERTY.equals(propName)) {
            if (evt.getNewValue() != null) { // just installed
     
                settingsChange(null);

                if (view != null) {
                    // Add mouse listener
                    view.addMouseListener(viewMouseL);
                }

                
            } else { // just deinstalled

                if (view != null) {
                     // Unregister Escape key
                    view.removeMouseListener(viewMouseL);
                }
            }
        }
    }
    
    public void setVisible(boolean visible){
        //new RuntimeException("ScrollCompletionPane.setVisible(" + visible + ")").printStackTrace();
        if (view instanceof JList) {
            JList listView = (JList)view;
            listView.ensureIndexIsVisible(listView.getSelectedIndex());
        }
        
        super.setVisible(visible);
    }
    
    public void refresh() {
        if (view instanceof JList) {
            JList listView = (JList)view;
            listView.ensureIndexIsVisible(listView.getSelectedIndex());
        }
        
        SwingUtilities.invokeLater( // !!! ? is it needed
            new Runnable() {
                public void run() {
                    if (isShowing()) { // #18810
//                        extEditorUI.getPopupManager().reset(extEditorUI.getComponent());
                        revalidate();
                    }
                }
            }
        );
    }

    /** Set the title of the pane according to the completion query results. */
    public void setTitle(String title) {
        topLabel.setText(title);
    }

    
    protected void installTitleComponent() {
        topLabel = new JLabel();
        topLabel.setForeground(Color.blue);
        topLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        setColumnHeaderView(topLabel);
    }

    protected Dimension getTitleComponentPreferredSize() {
        return topLabel.getPreferredSize();
    }
    
    public void setSize(int width, int height){
        int maxWidth = width;
        int maxHeight = height;

        minSize.width = minSizeDefault.width;
        minSize.height = minSizeDefault.height;
        setMinimumSize(minSize);
        
        Dimension ps = getPreferredSize();

        /* Add size of the vertical scrollbar by default. This could be improved
        * to be done only if the height exceeds the bounds. */
        ps.width += scrollBarSize.width;
        ps.width = Math.max(Math.max(ps.width, minSize.width),
                            getTitleComponentPreferredSize().width);

        maxWidth = Math.min(maxWidth, maxSize.width);
        maxHeight = Math.min(maxHeight, maxSize.height);
        boolean displayHorizontalScrollbar = (ps.width-scrollBarSize.width)>maxWidth;

        if (ps.width > maxWidth) {
            ps.width = maxWidth;
            if (displayHorizontalScrollbar){
                ps.height += scrollBarSize.height; // will show horizontal scrollbar
                minSize.height += scrollBarSize.height;
                setMinimumSize(minSize);
            }
            
        }

        ps.height = Math.min(Math.max(ps.height, minSize.height), maxHeight);
        super.setSize(ps.width, ps.height);
    }
    
    public void setSize(Dimension d){
        setSize(d.width, d.height);
    }
    
    public JComponent getComponent() {
        return this;
    }    
    
    class ViewMouseListener extends MouseAdapter {

        public void mouseClicked(MouseEvent evt) {
            if (SwingUtilities.isLeftMouseButton(evt)) {
                JTextComponent component = extEditorUI.getComponent();
                if( component != null && evt.getClickCount() == 2 ) {
                    JDCPopupPanel jdc = ExtUtilities.getJDCPopupPanel(component);
                    if (jdc != null) {
                        Action a = jdc.getActionMap().get(JDCPopupPanel.COMPLETION_SUBSTITUTE_TEXT);
                        if (a != null) {
                            a.actionPerformed(new ActionEvent(component, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
                        }
                    }
                }
            }
        }
    }

}

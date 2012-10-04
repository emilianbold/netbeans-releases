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

package org.netbeans.modules.groovy.support.options;

import java.awt.BorderLayout;
import java.util.Iterator;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;


/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
public final class GroovyPanel extends JPanel {
    
    private final transient LookupListener listener;
    private final transient ChangeListener changeListener;
    private Model model;
    JTabbedPane tabbedPanel;    


    GroovyPanel () {
        listener = new LookupListenerImpl();
        changeListener = new ChangeListenerImpl();
        model = new Model(listener);
    }
        
    public void update () {
        String category = tabbedPanel.getTitleAt(tabbedPanel.getSelectedIndex());
        model.update (category);        
    }
    
    public void applyChanges () {
        model.applyChanges ();
    }
    
    public void cancel () {
        model.cancel ();
    }
    
    public HelpCtx getHelpCtx () {
        return model.getHelpCtx ((tabbedPanel != null) ? ((JComponent)tabbedPanel.getSelectedComponent ()) : null);
    }
    
    public boolean dataValid () {
        return model.isValid ();
    }
    
    public boolean isChanged () {
        return model.isChanged ();
    }
    
    public Lookup getLookup () {
        return model.getLookup ();
    }
    
    void init (Lookup masterLookup) {
        // init components
        tabbedPanel = new JTabbedPane();
        
        // define layout
        setLayout (new BorderLayout ());
        add (tabbedPanel, BorderLayout.CENTER);
        initTabbedPane (masterLookup);
    }
    
    private void initTabbedPane(Lookup masterLookup) {
        tabbedPanel.removeChangeListener(changeListener);
        tabbedPanel.removeAll();
        Iterator it = model.getCategories().iterator();
        while (it.hasNext()) {
            String category = (String) it.next ();
            tabbedPanel.addTab(category, new JLabel(category));
        }
        tabbedPanel.addChangeListener(changeListener);
        handleTabSwitched();
    }

    private void handleTabSwitched() {        
        final int selectedIndex = tabbedPanel.getSelectedIndex() >= 0 ? tabbedPanel.getSelectedIndex() : 0;
        String category = tabbedPanel.getTitleAt(selectedIndex);
        if (tabbedPanel.getSelectedComponent() instanceof JLabel) {
            tabbedPanel.setComponentAt(tabbedPanel.getSelectedIndex(), model.getPanel(category));
            ((JComponent)tabbedPanel.getSelectedComponent()).setBorder (new EmptyBorder(11,11,11,11));
        }
        model.update(category);
        firePropertyChange (OptionsPanelController.PROP_HELP_CTX, null, null);        
    }
    
    private class LookupListenerImpl implements LookupListener {
        
        @Override
        public void resultChanged(LookupEvent ev) {
            Lookup masterLookup = model.getLookup();
            model = new Model(listener);
            initTabbedPane(masterLookup);
        }        
    }

    private class ChangeListenerImpl implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            handleTabSwitched();
        }
    }
}

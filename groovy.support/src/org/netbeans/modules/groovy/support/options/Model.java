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

import java.beans.PropertyChangeListener;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Jan Jancura
 */
public final class Model extends TabbedPanelModel {
    
    private Map<String,AdvancedOption> categoryToOption = new HashMap<String,AdvancedOption>();
    private Map<String, JComponent> categoryToPanel = new HashMap<String, JComponent> ();
    private Map<String, OptionsPanelController> categoryToController = new HashMap<String, OptionsPanelController>();
    private Lookup masterLookup;
    private LookupListener lkpListener;
    private Result<AdvancedOption> lkpResult;

    public Model(LookupListener listener) {
        this.lkpListener = listener;
    }

    
    public List getCategories () {
        init ();
        List<String> l = new ArrayList<String>(categoryToOption.keySet ());
        Collections.sort(l, Collator.getInstance());
        return l;
    }
    
    public String getToolTip (String category) {
        AdvancedOption option = categoryToOption.get (category);
        return option.getTooltip ();
    }
    
    public JComponent getPanel (String category) {
        init ();
        JComponent panel = categoryToPanel.get (category);        
        if (panel != null) return panel;
        AdvancedOption option = categoryToOption.get (category);
        OptionsPanelController controller = new DelegatingController(option.create ());
        categoryToController.put (category, controller);
        panel = controller.getComponent (masterLookup);
        categoryToPanel.put (category, panel);
        Border b = panel.getBorder ();
        if (b != null)
            b = new CompoundBorder (
                new EmptyBorder (6, 16, 6, 6),
                b
            );
        else
            b = new EmptyBorder (6, 16, 6, 6);
        panel.setBorder (b);
        //panel.setBackground (Color.white);
        panel.setMaximumSize (panel.getPreferredSize ());
        return panel;
    }
    
    
    // implementation ..........................................................
    void update (String category) {
        OptionsPanelController controller = categoryToController.get(category);
        if (controller != null) {
            controller.update();
        }
    }
    
    void applyChanges () {
        Iterator it = categoryToController.values ().iterator ();
        while (it.hasNext ())
            ((OptionsPanelController) it.next ()).applyChanges ();
    }
    
    void cancel () {
        Iterator it = categoryToController.values ().iterator ();
        while (it.hasNext ())
            ((OptionsPanelController) it.next ()).cancel ();
    }
    
    boolean isValid () {
        Iterator it = categoryToController.values ().iterator ();
        while (it.hasNext ())
            if (!((OptionsPanelController) it.next ()).isValid ())
                return false;
        return true;
    }
    
    boolean isChanged () {
        Iterator it = categoryToController.values ().iterator ();
        while (it.hasNext ())
            if (((OptionsPanelController) it.next ()).isChanged ())
                return true;
        return false;
    }
    
    Lookup getLookup () {
        List<Lookup> lookups = new ArrayList<Lookup> ();
        Iterator<OptionsPanelController> it = categoryToController.values ().iterator ();
        while (it.hasNext ())
            lookups.add (it.next ().getLookup ());
        return new ProxyLookup 
            (lookups.toArray (new Lookup [lookups.size ()]));
    }
    
    HelpCtx getHelpCtx (JComponent panel) {
        Iterator it = categoryToPanel.keySet ().iterator ();
        while (it.hasNext ()) {
            String category = (String) it.next ();
            if (panel == null || panel == categoryToPanel.get (category)) {
                OptionsPanelController controller = categoryToController.get (category);
                if (controller != null) {
                    return controller.getHelpCtx ();
                }
            }
        }
        return new HelpCtx ("netbeans.optionsDialog.advanced");
    }
    
    private boolean initialized = false;
    
    private void init () {
        if (initialized) return;
        initialized = true;
        
        Lookup lookup = Lookups.forPath("OptionsDialog/Groovy"); // NOI18N
        lkpResult = lookup.lookup(new Lookup.Template<AdvancedOption>(AdvancedOption.class));
        lkpResult.addLookupListener(lkpListener);
        lkpListener = null;
        Iterator<? extends AdvancedOption> it = lkpResult.
                allInstances ().iterator ();
        while (it.hasNext ()) {
            AdvancedOption option = it.next ();
            categoryToOption.put (option.getDisplayName (), option);
        }
    }
    
    void setLoookup (Lookup masterLookup) {
        this.masterLookup = masterLookup;
    }
    
    private static final class DelegatingController extends OptionsPanelController {
        private OptionsPanelController delegate;
        private boolean isUpdated;
        private DelegatingController(OptionsPanelController delegate) {
            this.delegate = delegate;
        }
        public void update() {
            if (!isUpdated) {
                isUpdated = true;
                delegate.update();
            }
        }

        public void applyChanges() {
            isUpdated = false;
            delegate.applyChanges();
        }

        public void cancel() {
            isUpdated = false;
            delegate.cancel();
        }

        public boolean isValid() {
            return delegate.isValid();
        }

        public boolean isChanged() {
            return delegate.isChanged();
        }

        public JComponent getComponent(Lookup masterLookup) {
            return delegate.getComponent(masterLookup);
        }

        public HelpCtx getHelpCtx() {
            return delegate.getHelpCtx();
        }

        public void addPropertyChangeListener(PropertyChangeListener l) {
            delegate.addPropertyChangeListener(l);
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
            delegate.removePropertyChangeListener(l);
        }        
    }            
}



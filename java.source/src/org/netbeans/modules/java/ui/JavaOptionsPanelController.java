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
package org.netbeans.modules.java.ui;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderLookup;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

final class JavaOptionsPanelController extends OptionsPanelController {
    
    private static final String TAB_FOLDER = "org.netbeans.modules.java.source/options/";
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private Map<String, OptionsPanelController> id2Controller;
    private Map<JComponent, OptionsPanelController> component2Option;
    private Lookup.Result<AdvancedOption> options;    

    private JTabbedPane pane;
    
    public JavaOptionsPanelController() {
        readPanels();
    }
            
    public void update() {
        for (OptionsPanelController c : getControllers()) {
            c.update();
        }
    }
    
    public void applyChanges() {
        for (OptionsPanelController c : getControllers()) {
            c.applyChanges();
        }
    }
    
    public void cancel() {
        for (OptionsPanelController c : getControllers()) {
            c.cancel();
        }
    }
    
    public boolean isValid() {
        for (OptionsPanelController c : getControllers()) {
            if (!c.isValid()) {
                return false;
            }
        }
        return true; 	
    }
    
    public boolean isChanged() {
        for (OptionsPanelController c : getControllers()) {
            if (c.isChanged()) {
                return true;
            }
        }
        return false;
    }
    
    public HelpCtx getHelpCtx() {
	return pane != null ? getHelpCtx(pane.getSelectedComponent()) : null;
    }
    
    private HelpCtx getHelpCtx(Component c) {
        OptionsPanelController o = component2Option.get(c);
        
        if (o != null) {
            return o.getHelpCtx();
        }
        
        return new HelpCtx("netbeans.optionsDialog.java");
    }
    
    public synchronized JComponent getComponent(Lookup masterLookup) {
        if ( pane == null ) {
            pane = new JTabbedPane();
            component2Option = new HashMap<JComponent, OptionsPanelController>();
            for (OptionsPanelController c : getControllers()) {
                JComponent comp = c.getComponent( c.getLookup());
                pane.add( controllers2Options.get(c).getDisplayName(), comp);
                component2Option.put(comp, c);
            }
        }
        return pane;
    }

    @Override
    protected void setCurrentSubcategory(String path) {
        String subcategoryID = path.indexOf('/') == -1 ? path : path.substring(0, path.indexOf('/'));
        OptionsPanelController controller = id2Controller.get(subcategoryID);
        if (controller == null)
            return;
        JComponent c = controller.getComponent(controller.getLookup());
        if (c != pane.getSelectedComponent())
            pane.setSelectedComponent(c);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
	pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
	pcs.removePropertyChangeListener(l);
    }
    
    // Private methods ---------------------------------------------------------
    
    private Map<OptionsPanelController, AdvancedOption> controllers2Options;
    private List<OptionsPanelController> controllers;
    
    private synchronized Collection<OptionsPanelController> getControllers() {
        if (controllers == null) {
            id2Controller = new HashMap<String, OptionsPanelController>();
            controllers2Options = new LinkedHashMap<OptionsPanelController, AdvancedOption>();
            controllers = new LinkedList<OptionsPanelController>();
            for (Lookup.Item<AdvancedOption> item : options.allItems()) {
                AdvancedOption o = item.getInstance();
                OptionsPanelController c = o.create();
                String id = item.getId().substring(item.getId().lastIndexOf('/') + 1);
                id2Controller.put(id, c);
                controllers2Options.put(c, o);
                controllers.add(c);
            }
        }
        
        return controllers;
    }
    
    private void readPanels() {
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        FileObject fo = fs.getRoot().getFileObject(TAB_FOLDER);
        
        DataFolder dataFolder = DataFolder.findFolder(fo);        
        FolderLookup fl = new FolderLookup( dataFolder );
        
        Lookup lookup = fl.getLookup();
        
        options = lookup.lookup(new Lookup.Template<AdvancedOption>( AdvancedOption.class ));
    }
}

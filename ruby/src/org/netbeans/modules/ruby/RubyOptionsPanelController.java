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
package org.netbeans.modules.ruby;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
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

final class RubyOptionsPanelController extends OptionsPanelController {
    
    private static final String TAB_FOLDER = "org.netbeans.modules.ruby/options/";
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private List<AdvancedOption> options;

    private JTabbedPane pane;
    
    public RubyOptionsPanelController() {
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
	return null; // new HelpCtx("...ID") if you have a help set
    }
    
    public synchronized JComponent getComponent(Lookup masterLookup) {
        if ( pane == null ) {
            pane = new JTabbedPane();
            for (OptionsPanelController c : getControllers()) {
                pane.add( controllers2Options.get(c).getDisplayName(), c.getComponent( c.getLookup()));
            }
        }
        return pane;
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
            controllers2Options = new LinkedHashMap<OptionsPanelController, AdvancedOption>();
            controllers = new LinkedList<OptionsPanelController>();
            for (AdvancedOption o : options) {
                OptionsPanelController c = o.create();
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
        
        Lookup.Result<AdvancedOption> result = lookup.lookup(new Lookup.Template<AdvancedOption>( AdvancedOption.class ));
        
        options = new LinkedList<AdvancedOption>();
        
        for( AdvancedOption advancedOption : result.allInstances()) {
            options.add(advancedOption);
        }
        
    }
}

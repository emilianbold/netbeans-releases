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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.options;

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
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author avk
 */
final class PhpOptionsPanelController extends OptionsPanelController {

    private static final String TAB_FOLDER = "org.netbeans.modules.php/options/";
    
    public PhpOptionsPanelController() {
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
        if ( myPane == null ) {
            myPane = new JTabbedPane();
            for (OptionsPanelController c : getControllers()) {
                myPane.add( myControllers2Options.get(c).getDisplayName(), 
                        c.getComponent( c.getLookup()));
            }
        }
        return myPane;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
	myPcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
	myPcs.removePropertyChangeListener(l);
    }
    
    private void readPanels() {
        myOptions = new LinkedList<AdvancedOption>();
        for(AdvancedOption advancedOption : Lookups.forPath(TAB_FOLDER).lookupAll(AdvancedOption.class)) {
            myOptions.add(advancedOption);
        }
    }

    private synchronized Collection<OptionsPanelController> getControllers() {
        if (myControllers == null) {
            myControllers2Options = new LinkedHashMap<OptionsPanelController, AdvancedOption>();
            myControllers = new LinkedList<OptionsPanelController>();
            for (AdvancedOption o : myOptions) {
                OptionsPanelController c = o.create();
                myControllers2Options.put(c, o);
                myControllers.add(c);
            }
        }
        
        return myControllers;
    }

    private Map<OptionsPanelController, AdvancedOption> myControllers2Options;

    private List<OptionsPanelController> myControllers;
    
    private final PropertyChangeSupport myPcs = new PropertyChangeSupport(this);
    
    private List<AdvancedOption> myOptions;

    private JTabbedPane myPane;
    
}

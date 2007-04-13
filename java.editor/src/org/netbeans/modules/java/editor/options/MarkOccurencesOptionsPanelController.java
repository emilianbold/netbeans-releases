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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.editor.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

final class MarkOccurencesOptionsPanelController extends OptionsPanelController {
    
    private static final String MARK_OCCURENCES = "MarkOccurences";
    
    static String ON_OFF = "OnOff";
    static String TYPES = "Types";
    static String METHODS = "Methods";
    static String CONSTANTS = "Constants";
    static String FIELDS = "Fields";
    static String LOCAL_VARIABLES = "LocalVariables";
    static String EXCEPTIONS = "Exceptions";
    static String EXIT = "Exit";
    static String IMPLEMENTS = "Implements";
    static String OVERRIDES = "Overrides";
    static String BREAK_CONTINUE = "BreakContinue";
   
    private Preferences node;
    
    private MarkOccurencesPanel panel;
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;
                    
    public void update() {
        panel.load( this );
    }
    
    public void applyChanges() {
        panel.store();
    }
    
    public void cancel() {
	// need not do anything special, if no changes have been persisted yet
    }
    
    public boolean isValid() {
        return true; // Always valid 
    }
    
    public boolean isChanged() {
	return panel.changed();
    }
    
    public HelpCtx getHelpCtx() {
	return null; // new HelpCtx("...ID") if you have a help set
    }
    
    public synchronized JComponent getComponent(Lookup masterLookup) {
        if ( panel == null ) {
            panel = new MarkOccurencesPanel(this);
        }
        return panel;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
	pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
	pcs.removePropertyChangeListener(l);
    }
    
    void changed() {
	if (!changed) {
	    changed = true;
	    pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
	}
	pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }
   
    synchronized Preferences getCurrentNode() {
        if ( node == null ) {
            Preferences preferences = NbPreferences.forModule(MarkOccurencesOptionsPanelController.class);
            node = preferences.node(MARK_OCCURENCES).node(getCurrentProfileId());
        }
                
        return node;
    }
    
    private String getCurrentProfileId() {
        return "default"; // NOI18N
    }
}

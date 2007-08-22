/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.ui.options;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;

public final class CndOptionsPanelController extends OptionsPanelController implements LookupListener {

    private OptionsPanelController[] controllers = new OptionsPanelController[0];
    private Lookup.Result<OptionsPanelController> res = null;
    private boolean inited = false;
    private CndOptionsPanel component = null;
    public CndOptionsPanelController() {
        // no logic in constructor to speedup Tools->Options display time
    }

    private static final String CND_OPTIONS_FOLDER_IN_SYSTEM_FILESYSTEM = "OptionsDialog/CPlusPlus"; // NOI18N
    
    public void update() {
        init();
        for (int i = 0; i < controllers.length; i++) {
            controllers[i].update();
        }
    }

    public void applyChanges() {
        for (int i = 0; i < controllers.length; i++) {
            controllers[i].applyChanges();
        }
    }
    
    public void cancel() {
        for (int i = 0; i < controllers.length; i++) {
            controllers[i].cancel();
        }
    }
    
    public boolean isValid() {      
        for (int i = 0; i < controllers.length; i++) {
            if (!controllers[i].isValid()) {
                return false;
            }
        }
        return true;
    }
    
    public boolean isChanged() {
        for (int i = 0; i < controllers.length; i++) {
            if (controllers[i].isChanged()) {
                return true;
            }
        }
        return false;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx("cnd.optionsDialog.NEED_TOPIC!"); // NOI18N
    }
    
    public JComponent getComponent(Lookup lookup) {
        if (component == null) {
            component = new CndOptionsPanel();
            init();
        }
        return component;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
//        tools.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
//        tools.removePropertyChangeListener(l);
    }
    
    private void init() {
        if (!inited) {
            this.controllers = findEmbeddedControllers();  
            inited = true;
            component.updateControllers(this.controllers);            
        }
    }

    private OptionsPanelController[] findEmbeddedControllers() {
        Collection<? extends OptionsPanelController> out = Collections.<OptionsPanelController>emptyList();
        if (res == null) { // init and remember
            res = Lookups.forPath(CND_OPTIONS_FOLDER_IN_SYSTEM_FILESYSTEM).lookupResult(OptionsPanelController.class);
        }
        if (res != null) {
            out = res.allInstances();
        }
        return out.toArray(new OptionsPanelController[out.size()]);
    }    
    
    public void resultChanged(LookupEvent ev) {
        inited = false;
    }

}

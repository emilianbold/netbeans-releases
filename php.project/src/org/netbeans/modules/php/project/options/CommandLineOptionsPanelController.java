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
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 *
 * @author avk
 */
public class CommandLineOptionsPanelController extends OptionsPanelController {

    public void update() {
        myChanged = false;
	myPanel.load();
    }
    
    public void applyChanges() {
	myPanel.store();
        myChanged = false;
    }
    
    public void cancel() {
	myPanel.cancel();
    }
    
    public boolean isValid() {
        return myPanel.isValid();
    }
    
    public boolean isChanged() {
	return myChanged;
    }
    
    public HelpCtx getHelpCtx() {
	return null; // new HelpCtx("...ID") if you have a help set
    }
    
    public synchronized JComponent getComponent(Lookup masterLookup) {
        if ( myPanel == null ) {
            myPanel = new CommandLineOptionsPanel(this);
        }
        return myPanel;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
	myPcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
	myPcs.removePropertyChangeListener(l);
    }
        
    void changed() {
	if (!myChanged) {
	    myChanged = true;
	    myPcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
	}
	myPcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }
    
    private final PropertyChangeSupport myPcs = new PropertyChangeSupport(this);

    CommandLineOptionsPanel myPanel;
    
    private boolean myChanged;
                    
    
}

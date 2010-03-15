/* 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.terminal.ui;

import org.netbeans.lib.terminalemulator.support.TermOptions;
import org.netbeans.lib.terminalemulator.support.TermOptionsPanel;
import java.util.prefs.Preferences;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;

import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.NbBundle;

/**
 * Sets up an options category under Miscellaneous.
 * Needs to be registered in mf-layer in the folder OptionsDialog/Advanced.
 */

public class TermAdvancedOption extends AdvancedOption {
    static private class MyOptionsPanelController extends OptionsPanelController {
	private TermOptions termOptions;
	private TermOptions clonedTermOptions;
	private TermOptionsPanel panel;

	/**
	 * Prefererences in which we store term settings.
	 */
	private static final Preferences prefs =
            NbPreferences.forModule(TermAdvancedOption.class);

	private void reset() {
	    termOptions = TermOptions.getDefault(prefs);
	    clonedTermOptions = termOptions.makeCopy();
	    panel.setTermOptions(clonedTermOptions);
	    
	}

        // implement OptionsPanelController
        public JComponent getComponent(Lookup masterLookup) {
	    panel = new TermOptionsPanel();
	    return panel;
	}

        /** 
         * Load data from model.
         * Called after getComponent().
         */

        // implement OptionsPanelController
        public void update() {
	    reset();
        }

        // implement OptionsPanelController
        public void cancel() {
	    reset();
        }

        // implement OptionsPanelController
        public void applyChanges()  {
	    if (termOptions == null)
		return;		// update wasn't called
	    // assign will fire a property change
	    termOptions.assign(clonedTermOptions);
	    termOptions.storeTo(prefs);
        }

        // implement OptionsPanelController
        public boolean isChanged() {
	    clonedTermOptions = termOptions.makeCopy();
            if (clonedTermOptions == null) {
                return false;
            } else {
                return clonedTermOptions.isDirty();
            }
        }

        // implement OptionsPanelController
        public boolean isValid() {
            // always valid
            return true;
        }

        // implement OptionsPanelController
        public HelpCtx getHelpCtx() {
            return null;
        }

        // implement OptionsPanelController
        @Override
        public Lookup getLookup() {
            return null;
        }

	// implement OptionsPanelController
	public void addPropertyChangeListener(PropertyChangeListener l) {
	}

        // implement OptionsPanelController
        public void removePropertyChangeListener(PropertyChangeListener l) {
        }
    }

    public OptionsPanelController create() {
	return new MyOptionsPanelController();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(TermAdvancedOption.class, "CTL_Term_options");
    }

    public String getTooltip() {
        return NbBundle.getMessage(TermAdvancedOption.class, "CTL_Term_options");
    }
}

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

package org.netbeans.bluej.options;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * 
 */
public class BluejAdvancedOption extends org.netbeans.spi.options.AdvancedOption {
    
    /** Creates a new instance of BluejAdvancedOption */
    public BluejAdvancedOption() {
    }

    public OptionsPanelController create() {
        return new Controller();
    }

    static class Controller extends OptionsPanelController {
        private List listeners = new ArrayList();
        private BlueJPanel panel;
        
        public void update() {
            File fil = BlueJSettings.getDefault().getHome();
            getPanel().setBlueJHome(fil != null ? fil.getAbsolutePath() : "");  // NOI18N
        }

        public void applyChanges() {
            String str = getPanel().getBlueJHome().trim();
            File fil = null;
            if (str.length() > 0) {
                fil = new File(str);
            } 
            BlueJSettings.getDefault().setHome(fil);
        }

        public void cancel() {
        }

        public boolean isValid() {
            return getPanel().isValidData();
        }

        public boolean isChanged() {
            return getPanel().isChangedData();
        }

        public JComponent getComponent(Lookup masterLookup) {
            return getPanel();
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        public void addPropertyChangeListener(PropertyChangeListener l) {
            listeners.add(l);
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
            listeners.remove(l);
        }

        private BlueJPanel getPanel() {
            if(panel == null) {
                panel = new BlueJPanel();
            }
            return panel;
        }
        
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getBundle(BluejAdvancedOption.class).getString("AdvanceOption_title"); // NOI18N
    }

    @Override
    public String getTooltip() {
        return NbBundle.getBundle(BluejAdvancedOption.class).getString("AdvancedOption_tooltip"); // NOI18N
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.navigator;

import java.util.Collection;
import javax.swing.JComponent;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author mkleint
 */
public class POMInheritanceNavigator implements NavigatorPanel {
    private POMInheritancePanel component;
    
    protected Lookup.Result<DataObject> selection;

    protected final LookupListener selectionListener = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            if(selection == null)
                return;
            navigate(selection.allInstances());
        }
    };
    

    public String getDisplayName() {
        return org.openide.util.NbBundle.getMessage(POMInheritanceNavigator.class, "POM_NAME");
    }

    public String getDisplayHint() {
        return org.openide.util.NbBundle.getMessage(POMInheritanceNavigator.class, "POM_HINT");
    }

    public JComponent getComponent() {
        return getNavigatorUI();
    }
    
    private POMInheritancePanel getNavigatorUI() {
        if (component == null) {
            component = new POMInheritancePanel();
        }
        return component;
    }

    public void panelActivated(Lookup context) {
        getNavigatorUI().showWaitNode();
        selection = context.lookup(new Lookup.Template<DataObject>(DataObject.class));
        selection.addLookupListener(selectionListener);
        selectionListener.resultChanged(null);
    }
    
    public void panelDeactivated() {
        getNavigatorUI().showWaitNode();
        if(selection != null) {
            selection.removeLookupListener(selectionListener);
            selection = null;
        }
        getNavigatorUI().release();
    }

    public Lookup getLookup() {
        return Lookup.EMPTY;
    }
    
    /**
     * 
     * @param selectedFiles 
     */

    public void navigate(Collection<? extends DataObject> selectedFiles) {
        if(selectedFiles.size() == 1) {
            DataObject d = (DataObject) selectedFiles.iterator().next();
            getNavigatorUI().navigate(d);           
        }
    }
    

}

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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.xml.wsdl.ui.navigator;

import java.beans.PropertyChangeEvent;
import java.util.Collection;

import javax.swing.JComponent;

import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * An implementation of NavigatorPanel for WSDL navigator.
 *
 * @author Marek Fukala
 * @author Nathan Fiedler
 */
public class WSDLNavigatorPanel implements LookupListener, NavigatorPanel {
    private Lookup.Result selection;
    private WSDLNavigatorContent content;

    /**
     * Public nullary constructor needed for system to instantiate the provider.
     */
    public WSDLNavigatorPanel() {
    }

    public String getDisplayHint() {
        return NbBundle.getMessage(WSDLNavigatorPanel.class,
                "LBL_WSDLNavigatorPanel_Hint");
    }

    public String getDisplayName() {
        return NbBundle.getMessage(WSDLNavigatorPanel.class,
                "LBL_WSDLNavigatorPanel_Name");
    }

    public JComponent getComponent() {
        return getContent();
    }
    
    private WSDLNavigatorContent getContent() {
        if (content == null) {
            content = new WSDLNavigatorContent();
        }
        return content;
    }

    public Lookup getLookup() {
        return null;
    }

    public void panelActivated(Lookup context) {
        WSDLNavigatorContent nContent = getContent();
        nContent.showWaitNode();
        
        TopComponent.getRegistry().removePropertyChangeListener(nContent);
        TopComponent.getRegistry().addPropertyChangeListener(nContent);
        selection = context.lookup(new Lookup.Template<DataObject>(DataObject.class));
        selection.addLookupListener(this);
        
        navigate();
        // hack to init selection if any
        nContent.propertyChange(new PropertyChangeEvent(this,
                WSDLNavigatorContent.CURRENT_NODES, false, true));
    }

    public void panelDeactivated() {
        WSDLNavigatorContent nContent = getContent();
        TopComponent.getRegistry().removePropertyChangeListener(nContent);
        if (selection != null) {
            selection.removeLookupListener(this);
            selection = null;
        }
        // If we set navigator to null its parent tc ref goes away.
        //navigator = null;
        if(nContent != null)
            nContent.release(); //hide the UI
    }

    public void resultChanged(LookupEvent ev) {
        navigate();
    }
    
    private void navigate() {
        if (selection == null) return;
        Collection selected = selection.allInstances();
        if (selected.size() == 1) {
            DataObject dobj = (DataObject) selected.iterator().next();
            getContent().navigate(dobj);
        }
    }
}

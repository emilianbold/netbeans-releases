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


package org.netbeans.modules.compapp.casaeditor.navigator;

import java.util.Collection;
import javax.swing.JComponent;
import org.netbeans.modules.compapp.casaeditor.CasaDataObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;

public class CasaSatelliteView implements LookupListener, NavigatorPanel {
    
    private static String navPanelName = NbBundle.getMessage(
            CasaNavigatorView.class, "LBL_SATELLITE_VIEW"); // NOI18N
    
    private CasaSatelliteContentPanel mNavigatorPanel;
    private Lookup.Result mLookupResult;
    private CasaWrapperModel mCasaModel;
    
    
    public CasaSatelliteView() {
    }
    
    
    public static String getUName() {
        return navPanelName;
    }
    
    public String getDisplayName() {
        return navPanelName;
    }
    
    public String getDisplayHint() {
        return NbBundle.getMessage(CasaNavigatorView.class, "LBL_SATELLITE_VIEW_TOOLTIP"); // NOI18N
    }
    
    public JComponent getComponent() {
        if (mNavigatorPanel == null) {
            mNavigatorPanel = new CasaSatelliteContentPanel();
        }
        return mNavigatorPanel;
    }
    
    public void panelActivated(Lookup context) {
        getComponent();
        mLookupResult = context.lookup(new Lookup.Template<DataObject>(DataObject.class));
        mLookupResult.addLookupListener(this);
        resultChanged(null);
    }
    
    public void panelDeactivated() {
        if (mLookupResult != null) {
            mLookupResult.removeLookupListener(this);
            mLookupResult = null;
        }
    }
    
    public Lookup getLookup() {
        // go with default activated Node strategy
        //Default mechanism chooses first Node from Utilities.actionsGlobalContext()
        //as activated Node for Navigator's TopComponent.
        return null;
    }
    
    public void resultChanged(LookupEvent ev) {
        Collection selected = mLookupResult.allInstances();
        if (selected.size() == 1) {
            Object result = selected.iterator().next();
            if (result instanceof CasaDataObject) {
                mNavigatorPanel.navigate((CasaDataObject) result);
            } else {
                mNavigatorPanel.navigate(null);
            }
        }
    }
}

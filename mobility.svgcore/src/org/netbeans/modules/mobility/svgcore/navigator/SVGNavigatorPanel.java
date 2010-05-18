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
package org.netbeans.modules.mobility.svgcore.navigator;

import java.util.Collection;
import java.util.logging.Level;
import javax.swing.JComponent;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;

/** An implementation of NavigatorPanel for XML navigator.
 *
 * @author Pavel Benes (based on the class NavigatorPanel by Marek Fukala)
 * @version 1.0
 */
public class SVGNavigatorPanel implements NavigatorPanel {
    
    private SVGNavigatorContent navigator = SVGNavigatorContent.getDefault();    
    private Lookup.Result       dataObjectSelection;
    
    private final LookupListener dataObjectListener = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            navigate(dataObjectSelection.allInstances());
        }
    };
        
    /** public no arg constructor needed for system to instantiate the provider. */
    public SVGNavigatorPanel() {
    }
    
    public String getDisplayHint() {
        return NbBundle.getMessage(SVGNavigatorPanel.class, "SVG_files_navigator");  //NOI18N
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(SVGNavigatorPanel.class, "SVG_View");  //NOI18N
    }
    
    public JComponent getComponent() {
        return navigator;
    }
    
    public Lookup getLookup() {
        return null;
    }    
    
    public void panelActivated(Lookup context) {
        dataObjectSelection = context.lookup(new Lookup.Template<SVGDataObject>(SVGDataObject.class));
        dataObjectSelection.addLookupListener(dataObjectListener);
        dataObjectSelection.allItems();
        dataObjectListener.resultChanged(null);
    }
    
    public void panelDeactivated() {
        dataObjectSelection.removeLookupListener(dataObjectListener);
        dataObjectSelection = null;
        navigator.release(); //hide the UI
    }
        
    public void navigate(Collection selectedFiles) {
        switch( selectedFiles.size()) {
            default:
                SceneManager.log(Level.SEVERE, "Multiple selection not allowed; using first node ..."); //NOI18N
            case 1:
                final SVGDataObject d = (SVGDataObject) selectedFiles.iterator().next();
                navigator.navigate(d);        
                break;
            case 0:
                navigator.navigate(null);
                break;
        }
    }    
}

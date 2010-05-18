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

package org.netbeans.modules.uml.propertysupport.options;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import org.netbeans.modules.uml.propertysupport.options.api.UMLOptionsPanel;
import org.netbeans.modules.uml.propertysupport.options.panels.GeneralOptionsPanel;
import org.netbeans.modules.uml.propertysupport.options.panels.GeneralOptionsPanelForm;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
/**
 *
 * @author krichard
 */
public class UMLPanelController extends OptionsPanelController {
    
    
    private boolean changed = false ;
    private static final boolean debug = false ;
    private GeneralOptionsPanel defaultPanel = null ;
    private MiscOptionsPanel misc = null ;
    private OptionsSupport support = null ;
    private JTabbedPane pane = null ;    
    
    /**
     * Creates a new instance of UMLPanelController
     */
    public UMLPanelController() {
        if (debug) log("MyPanelController"); // NOI18N
    }
    
    
    public void update() {
        changed = false ;
        if (debug) log("update"); // NOI18N
        
        // (1) apply changes for "general" panel options
        defaultPanel.update();
        
        // (2..n) apply changes for registered panel options
        for (UMLOptionsPanel panel: support.getMainPanels()) {
            panel.update();
        }
        
        //((n+1)..m) apply changes for misc panels options
        for (UMLOptionsPanel panel: support.getMiscPanels()) {
            panel.update();
        }
    }
    
    public void applyChanges() {
        changed = true ;
        if (debug) log("applyChanges"); // NOI18N
        
        // (1) apply changes for "general" panel options
        defaultPanel.applyChanges();
        
        // (2..n) apply changes for registered panel options
        for (UMLOptionsPanel panel: support.getMainPanels()) {
            panel.applyChanges();
        }
        
        //((n+1)..m) apply changes for misc panels options
        for (UMLOptionsPanel panel: support.getMiscPanels()) {
            panel.applyChanges();
        }
    }
    
    public void cancel() {
        for (UMLOptionsPanel panel: support.getMainPanels()) {
            panel.cancel();
        }
    }
    
    public boolean isValid() {
        if (debug) log("isValid"); // NOI18N
        return true ;
    }
    
    public boolean isChanged() {
        if (debug) log("isChanged");  // NOI18N
        return changed ;
    }
    
    public JComponent getComponent(Lookup lookup) {
        
        if (debug) log("getComponent"); // NOI18N

        // this is the main component that holds the various tabs. There are two
        // panels being added directly: General and Misc. The other tabs are found
        // from the lookup (via layer file). See OptionsSupport getMainPanels() and
        // getMiscPanels().
        pane = new JTabbedPane() ;
        
        // (1) create the General panel and populate it.
        
        // (1) create the General panel and populate it.
        defaultPanel = new GeneralOptionsPanel() ;
        GeneralOptionsPanelForm defaultForm = (GeneralOptionsPanelForm) defaultPanel.create() ;
        
        pane.addTab(defaultPanel.getDisplayName(), defaultForm) ;
        
        // (2..n) add all panels that are registered via layer files
        support = new OptionsSupport() ;
        Vector<UMLOptionsPanel> panels = support.getMainPanels() ;
        
        int i = 1;
        for (UMLOptionsPanel panel: panels) {
            pane.addTab(panel.getDisplayName(), panel.create()) ;
        }
        
        // ((n+1)..m) create the Misc panel and populate it.
        //the following will add a "Misc" tab to this tabbed pane and populate
        //the panel with panels registered in their layer files as UML|Misc panels.
        //It is removed for now because there is no need for such a category.
        //        misc = new MiscOptionsPanel() ;
        //        UMLMiscOptionsPanelForm miscPanel = (UMLMiscOptionsPanelForm) misc.create() ;
        //
        //        pane.addTab(misc.getDisplayName(), miscPanel) ;
        //
        //        for (UMLOptionsPanel panel:support.getMiscPanels()) {
        //            miscPanel.addTab(panel) ;
        //        }
        
        
        return pane ;
        
    }
    
    
    public HelpCtx getHelpCtx() {
        if (debug) log("getHelpCtx"); // NOI18N

        return new HelpCtx("uml_prefs_categories_in_preferences_editor") ;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        if (debug) log("addPropertyChangeListener::"+propertyChangeListener.toString()); // NOI18N
        
    }
    
    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        if (debug) log("removePropertyChangeListener"); // NOI18N
        
    }
    
    private static void log(String s) {
        System.out.println("MyPanelController::"+s); // NOI18N
    }
    
    
}

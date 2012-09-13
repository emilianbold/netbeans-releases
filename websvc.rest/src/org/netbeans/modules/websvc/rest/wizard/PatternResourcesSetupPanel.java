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

package org.netbeans.modules.websvc.rest.wizard;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.source.ui.ScanDialog;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.rest.model.api.RestApplication;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.modules.websvc.rest.spi.WebRestSupport;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author nam
 */
final class PatternResourcesSetupPanel extends AbstractPanel {
    private JComponent component;
    private Pattern currentPattern = PatternResourcesSetupPanel.Pattern.CONTAINER;
    
    /** Create the wizard panel descriptor. */
    public PatternResourcesSetupPanel(String name, WizardDescriptor wizardDescriptor) {
        super(name, wizardDescriptor);
    }
    
    @Override
    public boolean isFinishPanel() {
        return true;
    }

    public enum Pattern {
        CONTAINER,
        STANDALONE,
        CLIENTCONTROLLED;
                
        public JComponent createUI(String name) {   
            return new PatternPanel(name, this);
        }
    }

    public void setCurrentPattern(Pattern pattern) {
        if (currentPattern != pattern) {
            component = null;
            currentPattern = pattern;
        }
    }
    
    @Override
    public JComponent getComponent() {
        if (component == null) {
            component = currentPattern.createUI(panelName);
            if (allWizardSteps != null) {
                component.putClientProperty(Util.WIZARD_PANEL_CONTENT_DATA, allWizardSteps);
                component.putClientProperty(Util.WIZARD_PANEL_CONTENT_SELECTED_INDEX, Integer.valueOf(indexInAllSteps));
            }
            component.setName(panelName);
            ((Settings)component).addChangeListener(this);
        }
        return component;
    }
    
    private String[] allWizardSteps;
    private int indexInAllSteps = 2;
    
    void saveStepsAndIndex() {
        if (component != null) {
            allWizardSteps = (String[]) component.getClientProperty(Util.WIZARD_PANEL_CONTENT_DATA);
            indexInAllSteps = (Integer) component.getClientProperty(Util.WIZARD_PANEL_CONTENT_SELECTED_INDEX);
        }        
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(PatternResourcesSetupPanel.class.getCanonicalName() + "." + this.currentPattern);
    }
    
    static class PatternPanel extends JPanel implements AbstractPanel.Settings, 
        SourcePanel
    {
        private static final long serialVersionUID = -5802330662876130253L;
        
        PatternPanel(String name , Pattern pattern) {
            JPanel panel = null;
            switch( pattern){
                case CONTAINER:
                case CLIENTCONTROLLED:
                    panel = new ContainerItemSetupPanelVisual(name);
                    break;
                case STANDALONE:
                    panel = new SingletonSetupPanelVisual(name );
                    break;
                default :
                    assert false;
            }
            setLayout( new BoxLayout(this, BoxLayout.Y_AXIS));
            add( panel );
            mainPanel = (AbstractPanel.Settings)panel;
            jerseyPanel = new JerseyPanel( this );
            mainPanel.addChangeListener(jerseyPanel );
            add( jerseyPanel );
            // Fix for BZ#214951 - Hidden Text Box during REST endpoint creation
            /*addHierarchyListener( new HierarchyListener(){

                @Override
                public void hierarchyChanged( HierarchyEvent e ) {*/
                    double height = 0;
                    Component[] components = getComponents();
                    for (Component component : components) {
                        height+= component.getPreferredSize().getHeight();
                    }
                    Dimension dim = getPreferredSize();
                    int newHeight = (int)height;
                    if ( dim.height < newHeight ) {
                        setPreferredSize( new Dimension( dim.width, newHeight ));
                    }      
             /*  }
                
            });*/ 
        }
        
        @Override
        public SourceGroup getSourceGroup(){
            return ((SourcePanel)mainPanel).getSourceGroup();
        }

        @Override
        public void read(final WizardDescriptor wizard) {
            mainPanel.read(wizard);
            Project project = Templates.getProject(wizard);
            WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
            Profile profile = webModule.getJ2eeProfile();
            final WebRestSupport restSupport = project.getLookup().lookup(WebRestSupport.class);
            boolean hasSpringSupport = restSupport!= null && restSupport.hasSpringSupport();
            boolean hasJaxRs = restSupport.hasJaxRsApi();
            if ( hasSpringSupport ){
                wizard.putProperty( WizardProperties.USE_JERSEY, true);
            }
            if (jerseyPanel != null) {
                if (!hasJaxRs || hasSpringSupport
                        || restSupport.isRestSupportOn())
                {
                    remove(jerseyPanel);
                    jerseyPanel = null;
                }
                if (restSupport instanceof WebRestSupport) {
                    ScanDialog.runWhenScanFinished(new Runnable() {

                        @Override
                        public void run() {
                            List<RestApplication> restApplications = ((WebRestSupport) restSupport)
                                    .getRestApplications();
                            boolean configured = restApplications != null
                                    && !restApplications.isEmpty();
                            configureJersey(configured, wizard);
                        }
                    }, NbBundle.getMessage(PatternResourcesSetupPanel.class,
                            "LBL_SearchAppConfig")); // NOI18N

                }
                else {
                    jerseyPanel.read(wizard);
                }
            }
        }

        @Override
        public void store(WizardDescriptor wizard) {
            mainPanel.store(wizard);
            if ( hasJerseyPanel() ){
                jerseyPanel.store(wizard);
            }
        }

        @Override
        public boolean valid(WizardDescriptor wizard) {
            boolean isValid = ((AbstractPanel.Settings)mainPanel).valid(wizard);
            if ( isValid && hasJerseyPanel() ){
                return jerseyPanel.valid(wizard);
            }
            else {
                return isValid;
            }
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            mainPanel.addChangeListener(l);
            if ( hasJerseyPanel() ){
                jerseyPanel.addChangeListener(l);
            }
        }
        
        private boolean hasJerseyPanel(){
            return jerseyPanel != null;
        }
        
        private void configureJersey(boolean remove, WizardDescriptor wizard){
            if ( jerseyPanel == null ){
                return;
            }
            if ( remove )
            {
                remove( jerseyPanel );
                jerseyPanel = null;
            }
            else {
                jerseyPanel.read(wizard);
            }
        }
        
        private AbstractPanel.Settings mainPanel;    
        private JerseyPanel jerseyPanel;
    }
}

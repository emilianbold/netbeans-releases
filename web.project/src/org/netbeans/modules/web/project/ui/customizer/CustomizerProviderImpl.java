/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project.ui.customizer;

import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.web.project.ProjectWebModule;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.UpdateHelper;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/** Customization of Web project
 *
 * @author Petr Hrebejk, Radko Najman
 */
public class CustomizerProviderImpl implements CustomizerProvider {
    
    private final Project project;
    private final UpdateHelper updateHelper;
    private final PropertyEvaluator evaluator;
    private final ReferenceHelper refHelper;
    
    private ProjectCustomizer.Category categories[];
    private ProjectCustomizer.CategoryComponentProvider panelProvider;
    
    // Option indexes
    private static final int OPTION_OK = 0;
    
    private static Map /*<Project,Dialog>*/project2Dialog = new HashMap(); 
    
    public CustomizerProviderImpl(Project project, UpdateHelper updateHelper, PropertyEvaluator evaluator, ReferenceHelper refHelper) {
        this.project = project;
        this.updateHelper = updateHelper;
        this.evaluator = evaluator;
        this.refHelper = refHelper;
    }
            
    public void showCustomizer() {
        showCustomizer( null );
    }
    
    public void showCustomizer( String preselectedCategory ) {
        showCustomizer( preselectedCategory, null );
    }
    
    public void showCustomizer( String preselectedCategory, String preselectedSubCategory ) {
        
        Dialog dialog = (Dialog)project2Dialog.get (project);
        if ( dialog != null ) {            
            dialog.show ();
            return;
        }
        else {
            WebProjectProperties uiProperties = new WebProjectProperties((WebProject) project, updateHelper, evaluator, refHelper);
            init( uiProperties );

            OptionListener listener = new OptionListener( project, uiProperties );
            if (preselectedCategory != null && preselectedSubCategory != null) {
                for (int i = 0; i < categories.length; i++) {
                    if (preselectedCategory.equals(categories[i].getName())) {
                        JComponent component = panelProvider.create(categories[i]);
                        if (component instanceof SubCategoryProvider) {
                            ((SubCategoryProvider)component).showSubCategory(preselectedSubCategory);
                        }
                    }
                }
            }
            dialog = ProjectCustomizer.createCustomizerDialog( categories, panelProvider, preselectedCategory, listener, null );
            dialog.addWindowListener( listener );
            dialog.setTitle( MessageFormat.format(                 
                    NbBundle.getMessage( CustomizerProviderImpl.class, "LBL_Customizer_Title" ), // NOI18N 
                    new Object[] { ProjectUtils.getInformation(project).getDisplayName() } ) );

            project2Dialog.put(project, dialog);
            dialog.show();
        }
    }    
    
    // Names of categories
    private static final String BUILD_CATEGORY = "BuildCategory";
    
    private static final String SOURCES = "Sources";
    private static final String LIBRARIES = "Libraries";
    private static final String FRAMEWORKS = "Frameworks";
    
    private static final String BUILD = "Build";
    private static final String WAR = "War";
    private static final String JAVADOC = "Javadoc";
    private static final String RUN = "Run";    
    
    private static final String WEBSERVICE_CATEGORY = "WebServiceCategory";
    private static final String WEBSERVICES = "WebServices";
    private static final String WEBSERVICECLIENTS = "WebServiceClients";

    private void init(WebProjectProperties uiProperties) {
        ResourceBundle bundle = NbBundle.getBundle( CustomizerProviderImpl.class );
    
        ProjectCustomizer.Category sources = ProjectCustomizer.Category.create(
                SOURCES,
                bundle.getString ("LBL_Config_Sources"),
                null);
        
        ProjectCustomizer.Category frameworks = ProjectCustomizer.Category.create (
                FRAMEWORKS,
                bundle.getString( "LBL_Config_Frameworks" ), // NOI18N
                null );
        
        ProjectCustomizer.Category libraries = ProjectCustomizer.Category.create (
                LIBRARIES,
                bundle.getString( "LBL_Config_Libraries" ), // NOI18N
                null );
        
        ProjectCustomizer.Category build = ProjectCustomizer.Category.create(
                BUILD, 
                bundle.getString( "LBL_Config_Build" ), // NOI18N
                null);
        ProjectCustomizer.Category war = ProjectCustomizer.Category.create(
                WAR,
                bundle.getString( "LBL_Config_War" ), // NOI18N
                null );
        ProjectCustomizer.Category javadoc = ProjectCustomizer.Category.create(
                JAVADOC,
                bundle.getString( "LBL_Config_Javadoc" ), // NOI18N
                null );
        
        ProjectCustomizer.Category run = ProjectCustomizer.Category.create(
                RUN,
                bundle.getString( "LBL_Config_Run" ), // NOI18N
                null );    

        ProjectCustomizer.Category buildCategory = ProjectCustomizer.Category.create(
                BUILD_CATEGORY,
                bundle.getString( "LBL_Config_BuildCategory" ), // NOI18N
                null,
                new ProjectCustomizer.Category[] { build, war, javadoc }  );
        
        ProjectCustomizer.Category webServices=null;
        ProjectCustomizer.Category services=null;
        ProjectCustomizer.Category clients=null;
        
        List servicesSettings = null;
        List serviceClientsSettings = null;
        
        ProjectWebModule wm = (ProjectWebModule) uiProperties.getProject().getLookup().lookup(ProjectWebModule.class);
        FileObject docBase = wm.getDocumentBase();
        if (docBase != null) {
            WebServicesSupport servicesSupport = WebServicesSupport.getWebServicesSupport(docBase);
            if (servicesSupport != null) {
                servicesSettings = servicesSupport.getServices();
            }
            WebServicesClientSupport clientSupport = WebServicesClientSupport.getWebServicesClientSupport(docBase);
            if (clientSupport != null) {
                serviceClientsSettings = clientSupport.getServiceClients();
            }
        }

        if ((servicesSettings!=null && servicesSettings.size()>0) || (serviceClientsSettings!=null && serviceClientsSettings.size()>0)) {
            services = ProjectCustomizer.Category.create(
                    WEBSERVICES,
                    bundle.getString( "LBL_Config_WebServices" ), // NOI18N
                    null);
            clients = ProjectCustomizer.Category.create(
                    WEBSERVICECLIENTS,
                    bundle.getString( "LBL_Config_WebServiceClients" ), // NOI18N
                    null);
            webServices = ProjectCustomizer.Category.create(
                    WEBSERVICE_CATEGORY,
                    bundle.getString( "LBL_Config_WebServicesRoot" ), // NOI18N
                    null,
                    new ProjectCustomizer.Category[] { services, clients } );
            
            categories = new ProjectCustomizer.Category[] { 
                    sources,
                    frameworks,
                    libraries,
                    buildCategory,
                    run,  
                    webServices
            };
        } else {
            categories = new ProjectCustomizer.Category[] { 
                    sources,
                    frameworks,
                    libraries,
                    buildCategory,
                    run
            };
        }
        
        Map panels = new HashMap();
        panels.put( sources, new CustomizerSources( uiProperties ) );
        panels.put(frameworks, new CustomizerFrameworks(uiProperties));
        panels.put( libraries, new CustomizerLibraries( uiProperties ) );
        panels.put( build, new CustomizerCompile( uiProperties ) );
        panels.put( war, new CustomizerWar( uiProperties ) );
        panels.put( javadoc, new CustomizerJavadoc( uiProperties ) );
        panels.put( run, new CustomizerRun( uiProperties ) ); 
        
        if(servicesSettings != null && servicesSettings.size() > 0) {
            panels.put( services, new CustomizerWSServiceHost( uiProperties, servicesSettings ));
        } else {
            panels.put( services, new NoWebServicesPanel());
        }
        if(serviceClientsSettings != null && serviceClientsSettings.size() > 0) {
            panels.put( clients, new CustomizerWSClientHost( uiProperties, serviceClientsSettings ));
        } else {
            panels.put( clients, new NoWebServiceClientsPanel());
        }

        panelProvider = new PanelProvider( panels );
        
    }
    
    private static class PanelProvider implements ProjectCustomizer.CategoryComponentProvider {
        
        private JPanel EMPTY_PANEL = new JPanel();
        
        private Map /*<Category,JPanel>*/ panels;
        
        PanelProvider( Map panels ) {
            this.panels = panels;            
        }
        
        public JComponent create( ProjectCustomizer.Category category ) {
            JComponent panel = (JComponent)panels.get( category );
            return panel == null ? EMPTY_PANEL : panel;
        }
                        
    }
    
    /** Listens to the actions on the Customizer's option buttons */
    private class OptionListener extends WindowAdapter implements ActionListener {
    
        private Project project;
        private WebProjectProperties uiProperties;
        
        OptionListener( Project project, WebProjectProperties uiProperties ) {
            this.project = project;
            this.uiProperties = uiProperties;            
        }
        
        // Listening to OK button ----------------------------------------------
        
        public void actionPerformed( ActionEvent e ) {
            // Store the properties into project 
            uiProperties.save();
            
            // Close & dispose the the dialog
            Dialog dialog = (Dialog)project2Dialog.get( project );
            if ( dialog != null ) {
                dialog.hide();
                dialog.dispose();
            }
        }        
        
        // Listening to window events ------------------------------------------
                
        public void windowClosed( WindowEvent e) {
            project2Dialog.remove( project );
        }   
        
        public void windowClosing( WindowEvent e ) {
            //Dispose the dialog otherwsie the {@link WindowAdapter#windowClosed}
            //may not be called
            Dialog dialog = (Dialog)project2Dialog.get( project );
            if ( dialog != null ) {
                dialog.hide ();
                dialog.dispose();
            }
        }
    }    
    
    static interface SubCategoryProvider {
        public void showSubCategory(String name);
    }
      
    private class LabelPanel extends JPanel {
        private JLabel label;
        
        LabelPanel(String text) {
            setLayout(new GridBagLayout());
            
            label = new JLabel(text);
            label.setVerticalAlignment(SwingConstants.CENTER);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            
            add(label, gridBagConstraints);
        }
    }
    
    private abstract class LabelPanelWithHelp extends LabelPanel implements HelpCtx.Provider {
        LabelPanelWithHelp(String text) {
            super(text);
        }
    }
    
    private class NoWebServicesPanel extends LabelPanelWithHelp {
        NoWebServicesPanel() {
            super(NbBundle.getMessage(CustomizerProviderImpl.class, "LBL_CustomizeWsServiceHost_NoWebServices"));
        }
        
        public HelpCtx getHelpCtx() {
            return new HelpCtx(CustomizerWSServiceHost.class.getName() + "Disabled"); // NOI18N
        }
    }
    
    private class NoWebServiceClientsPanel extends LabelPanelWithHelp {
        NoWebServiceClientsPanel() {
            super(NbBundle.getMessage(CustomizerProviderImpl.class, "LBL_CustomizeWsServiceClientHost_NoWebServiceClients"));
        }
        
        public HelpCtx getHelpCtx() {
            return new HelpCtx(CustomizerWSClientHost.class.getName() + "Disabled"); // NOI18N
        }
    }

}

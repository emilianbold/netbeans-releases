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

package org.netbeans.modules.j2ee.clientproject.ui.customizer;

import java.awt.Dialog;
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
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.clientproject.AppClientProject;
import org.netbeans.modules.j2ee.clientproject.UpdateHelper;
import org.netbeans.modules.j2ee.clientproject.wsclient.CustomizerWSClientHost;
import org.netbeans.modules.j2ee.clientproject.wsclient.NoWebServiceClientsPanel;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.NbBundle;


/** Customization of J2SE project
 *
 * @author Petr Hrebejk
 */
public class CustomizerProviderImpl implements CustomizerProvider {
    
    private final Project project;
    private final UpdateHelper updateHelper;
    private final PropertyEvaluator evaluator;
    private final ReferenceHelper refHelper;
    private final GeneratedFilesHelper genFileHelper;
    
    private ProjectCustomizer.Category categories[];
    private ProjectCustomizer.CategoryComponentProvider panelProvider;
    
    // Option indexes
    private static final int OPTION_OK = 0;
    private static final int OPTION_CANCEL = OPTION_OK + 1;
    
    // Option command names
    private static final String COMMAND_OK = "OK";          // NOI18N
    private static final String COMMAND_CANCEL = "CANCEL";  // NOI18N
    
    private static Map<Project, Dialog> project2Dialog = new HashMap<Project, Dialog>();
    
    public CustomizerProviderImpl(Project project, UpdateHelper updateHelper, PropertyEvaluator evaluator, ReferenceHelper refHelper, GeneratedFilesHelper genFileHelper) {
        this.project = project;
        this.updateHelper = updateHelper;
        this.evaluator = evaluator;
        this.refHelper = refHelper;
        this.genFileHelper = genFileHelper;
    }
            
    public void showCustomizer() {
        showCustomizer( null );
    }
    
    
    public void showCustomizer ( String preselectedCategory ) {
        showCustomizer ( preselectedCategory, null );
    }
    
    public void showCustomizer( String preselectedCategory, String preselectedSubCategory ) {
        
        Dialog dialog = (Dialog)project2Dialog.get (project);
        if ( dialog != null ) {            
            dialog.setVisible(true);
            return;
        }
        else {
            AppClientProjectProperties uiProperties = new AppClientProjectProperties( (AppClientProject)project, updateHelper, evaluator, refHelper, genFileHelper );        
            init( uiProperties );

            OptionListener listener = new OptionListener( project, uiProperties );
            // HelpCtx helpCtx = new HelpCtx( "org.netbeans.modules.java.j2seproject.ui.customizer.J2SECustomizer" );
            if (preselectedCategory != null && preselectedSubCategory != null) {
                for (int i=0; i<categories.length; i++ ) {
                    if (preselectedCategory.equals(categories[i].getName())) {
                        JComponent component = panelProvider.create (categories[i]);
                        if (component instanceof SubCategoryProvider) {
                            ((SubCategoryProvider)component).showSubCategory(preselectedSubCategory);
                        }
                        break;
                    }
                }
            }
            dialog = ProjectCustomizer.createCustomizerDialog( categories, panelProvider, preselectedCategory, listener, null );
            dialog.addWindowListener( listener );
            dialog.setTitle( MessageFormat.format(                 
                    NbBundle.getMessage( CustomizerProviderImpl.class, "LBL_Customizer_Title" ), // NOI18N 
                    new Object[] { ProjectUtils.getInformation(project).getDisplayName() } ) );

            project2Dialog.put(project, dialog);
            dialog.setVisible(true);
        }
    }    
    
    // Names of categories
    private static final String BUILD_CATEGORY = "BuildCategory"; // NOI18N
    private static final String RUN_CATEGORY = "RunCategory"; // NOI18N
    
    private static final String GENERAL = "General"; // NOI18N
    private static final String SOURCES = "Sources"; // NOI18N
    private static final String LIBRARIES = "Libraries"; // NOI18N
    
    private static final String BUILD = "Build"; // NOI18N
    private static final String BUILD_TESTS = "BuildTests"; // NOI18N
    private static final String JAR = "Jar"; // NOI18N
    private static final String JAVADOC = "Javadoc"; // NOI18N
    private static final String RUN = "Run"; // NOI18N
    private static final String RUN_TESTS = "RunTests"; // NOI18N
    
    private static final String WEBSERVICECLIENTS = "WebServiceClients"; // NOI18N
    private static final String WEBSERVICE_CATEGORY = "WebServiceCategory"; // NOI18N
    
    private void init( AppClientProjectProperties uiProperties ) {
        
        ResourceBundle bundle = NbBundle.getBundle( CustomizerProviderImpl.class );
    
        /*
        ProjectCustomizer.Category general = ProjectCustomizer.Category.create( 
                BUILD, 
                bundle.getString( "LBL_Config_Build" ), 
                null, 
                null );        
        */
        
        
        ProjectCustomizer.Category sources = ProjectCustomizer.Category.create(
                SOURCES,
                bundle.getString ("LBL_Config_Sources"),
                null,
                null);
        
        ProjectCustomizer.Category libraries = ProjectCustomizer.Category.create (
                LIBRARIES,
                bundle.getString( "LBL_Config_Libraries" ), // NOI18N
                null,
                null );
        
        ProjectCustomizer.Category build = ProjectCustomizer.Category.create(
                BUILD, 
                bundle.getString( "LBL_Config_Build" ), // NOI18N
                null,
                null);
        ProjectCustomizer.Category jar = ProjectCustomizer.Category.create(
                JAR,
                bundle.getString( "LBL_Config_Jar" ), // NOI18N
                null,
                null );
        ProjectCustomizer.Category javadoc = ProjectCustomizer.Category.create(
                JAVADOC,
                bundle.getString( "LBL_Config_Javadoc" ), // NOI18N
                null,
                null );
        
        ProjectCustomizer.Category run = ProjectCustomizer.Category.create(
                RUN,
                bundle.getString( "LBL_Config_Run" ), // NOI18N
                null,
                null );    
        ProjectCustomizer.Category runTests = ProjectCustomizer.Category.create(
                RUN_TESTS,
                bundle.getString( "LBL_Config_Test" ), // NOI18N
                null,
                null);

        ProjectCustomizer.Category buildCategory = ProjectCustomizer.Category.create(
                BUILD_CATEGORY,
                bundle.getString( "LBL_Config_BuildCategory" ), // NOI18N
                null,
                new ProjectCustomizer.Category[] { build, jar, javadoc }  );

        ProjectCustomizer.Category webServices=null;
        ProjectCustomizer.Category clients=null;
        
        List serviceClientsSettings = null;
        
        WebServicesClientSupport clientSupport = WebServicesClientSupport.getWebServicesClientSupport(project.getProjectDirectory());
        if (clientSupport != null) {
            serviceClientsSettings = clientSupport.getServiceClients();
        }
        if (serviceClientsSettings!=null && serviceClientsSettings.size()>0) {
            clients = ProjectCustomizer.Category.create(
                    WEBSERVICECLIENTS,
                    bundle.getString( "LBL_Config_WebServiceClients" ), // NOI18N
                    null,
                    null);
            webServices = ProjectCustomizer.Category.create(
                    WEBSERVICE_CATEGORY,
                    bundle.getString( "LBL_Config_WebServices" ), // NOI18N
                    null,
                    new ProjectCustomizer.Category[] { clients } );
            
            categories = new ProjectCustomizer.Category[] { 
                    sources,
                    libraries,
                    buildCategory,
                    run,  
                    webServices
            };
        } else {
            categories = new ProjectCustomizer.Category[] { 
                    sources,
                    libraries,
                    buildCategory,
                    run
            };
        }      
        
        Map<ProjectCustomizer.Category, JPanel> panels = new HashMap<ProjectCustomizer.Category, JPanel>();
        panels.put( sources, new CustomizerSources( uiProperties ) );
        panels.put( libraries, new CustomizerLibraries( uiProperties ) );
        panels.put( build, new CustomizerCompile( uiProperties ) );
        panels.put( jar, new CustomizerJar( uiProperties ) );
        panels.put( javadoc, new CustomizerJavadoc( uiProperties ) );
        panels.put( run, new CustomizerRun( uiProperties ) ); 
        
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
        private AppClientProjectProperties uiProperties;
        
        OptionListener( Project project, AppClientProjectProperties uiProperties ) {
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
                dialog.setVisible(false);
                dialog.dispose();
            }
        }        
        
        // Listening to window events ------------------------------------------
                
        public void windowClosed( WindowEvent e) {
            project2Dialog.remove( project );
        }    
        
        public void windowClosing (WindowEvent e) {
            //Dispose the dialog otherwsie the {@link WindowAdapter#windowClosed}
            //may not be called
            Dialog dialog = (Dialog)project2Dialog.get( project );
            if ( dialog != null ) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }
    }
    
    static interface SubCategoryProvider {
        public void showSubCategory (String name);
    }
   
                            
}

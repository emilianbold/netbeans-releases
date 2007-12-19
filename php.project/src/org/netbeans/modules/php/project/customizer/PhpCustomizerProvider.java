/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.php.project.customizer;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.rt.utils.ProjectWithHostCustomizer;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class PhpCustomizerProvider implements CustomizerProvider, ProjectWithHostCustomizer {
    
    public static final String SOURCES = "Sources";        // NOI18N
    private static final String HOSTS = "Hosts"; // NOI18N
    private static final String COMMAND_LINE   = "CommandLine";          // NOI18N
    
    public PhpCustomizerProvider( PhpProject project ) {
        myProject = project ;
    }

    public void showCustomizerHostCategory() {
        showCustomizer(HOSTS);
    }

    public void showCustomizer(String category) {
        mySelectedCategory = category;
        showCustomizer();
        mySelectedCategory = null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.project.ui.CustomizerProvider#showCustomizer()
     */
    public void showCustomizer() {
        if ( myDialog != null) {
            // check if the project is being customized
            if (myDialog.isShowing ()) {
                // make it showed
                myDialog.setVisible(true);
                return ;
            }
        }
        else {
            PhpProjectProperties uiProperties = new PhpProjectProperties(
                    getProject());
            // load PhpProjectProperties here to do it once inside 
            // 'ProjectManager.mutex().readAccess'
            uiProperties.load();
            init( uiProperties );

            OptionListener listener = new OptionListener( uiProperties );
            
            myDialog = ProjectCustomizer.createCustomizerDialog( myCategories, 
                    myPanelProvider, mySelectedCategory, listener, null  );
            myDialog.addWindowListener( listener );
            myDialog.setTitle( MessageFormat.format(                 
                    NbBundle.getMessage(PhpCustomizerProvider.class, 
                            "LBL_Customizer_Title" ), // NOI18N 
                    new Object[] { ProjectUtils.getInformation(
                            getProject()).getDisplayName() } ) );
            myDialog.setVisible( true );
        }
        
    }
        
    private void init(PhpProjectProperties uiProperties) {
        ResourceBundle bundle = NbBundle.getBundle(PhpCustomizerProvider.class );
        
        ProjectCustomizer.Category sources = ProjectCustomizer.Category.create(
                SOURCES,
                bundle.getString ("LBL_Config_Sources"),
                null,
                null);
        

        List<ProjectCustomizer.Category> categories = 
            new LinkedList<ProjectCustomizer.Category>();
        categories.add( sources );
        
        Map<Category,JPanel> panels = new HashMap<Category,JPanel>();
        panels.put( sources, new CustomizerSources( uiProperties )  );
        
        initWebProviderPanel(panels , categories , uiProperties );
        
        initCommandLinePanel(panels , categories , uiProperties );
        
        myCategories = (ProjectCustomizer.Category[]) 
            categories.toArray(new ProjectCustomizer.Category[categories.size()]);
        
        myPanelProvider = new PanelProvider( panels );
    }
    
    private void initWebProviderPanel( Map<Category,JPanel> panels , 
            List<ProjectCustomizer.Category> categories , 
            PhpProjectProperties properties )
    {
        ResourceBundle bundle = NbBundle.getBundle(PhpCustomizerProvider.class );
        
        ProjectCustomizer.Category hosts = ProjectCustomizer.Category.create(
                HOSTS,
                bundle.getString ("LBL_Config_Hosts"),
                null,
                null);
        
        categories.add( hosts );

        panels.put(hosts, new CustomizerHost(properties));
        
        
        
    }
    
    private void initCommandLinePanel( Map<Category,JPanel> panels , 
            List<ProjectCustomizer.Category> categories , 
            PhpProjectProperties properties )
    {
        ResourceBundle bundle = NbBundle.getBundle(PhpCustomizerProvider.class );
        
        ProjectCustomizer.Category commandLine = ProjectCustomizer.Category.create(
                COMMAND_LINE,
                bundle.getString ("LBL_Config_Command_Line"),
                null,
                null);
        
        categories.add( commandLine );

        panels.put(commandLine, new CustomizerCommandLine(properties));
        
        
        
    }
    
    private PhpProject getProject() {
        return myProject;
    }
    
    /** Listens to the actions on the Customizer's option buttons */
    private class OptionListener extends WindowAdapter implements ActionListener {
    
        OptionListener( PhpProjectProperties uiProperties ) {
            myProperties = uiProperties;            
        }
        
        // Listening to OK button ----------------------------------------------
        
        public void actionPerformed( ActionEvent e ) {
            // Store the properties into project 
            myProperties.save();
            
            // Close & dispose the the dialog
            if ( myDialog != null ) {
                myDialog.setVisible(false);
                myDialog.dispose();
            }
        }        
        
        // Listening to window events ------------------------------------------
                
        public void windowClosed( WindowEvent e) {
            myDialog = null;
        }   
        
        public void windowClosing( WindowEvent e ) {
            //Dispose the dialog otherwsie the {@link WindowAdapter#windowClosed}
            //may not be called
            if ( myDialog != null ) {
                myDialog.setVisible(false);
                myDialog.dispose();
            }
        }
        
        private PhpProjectProperties myProperties;
    } 
    
    private static class PanelProvider implements 
        ProjectCustomizer.CategoryComponentProvider 
    {
        
        private JPanel EMPTY_PANEL = new JPanel();
        
        PanelProvider( Map<Category,JPanel> panels ) {
            myPanels = panels;            
        }
        
        public JComponent create( ProjectCustomizer.Category category ) {
            JComponent panel = myPanels.get( category );
            return panel == null ? EMPTY_PANEL : panel;
        }

        private Map<Category,JPanel> myPanels;
    }

    private Dialog myDialog;

    private PhpProject myProject;
    
    private PanelProvider myPanelProvider;
    
    private ProjectCustomizer.Category[] myCategories;

    private String mySelectedCategory;
}

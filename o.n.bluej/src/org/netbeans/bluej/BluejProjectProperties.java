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

package org.netbeans.bluej;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ui.StoreGroup;
import org.openide.ErrorManager;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 * @author mkleint
 */
public class BluejProjectProperties {
    
    
    // Properties stored in the PROJECT.PROPERTIES    
    public static final String RUN_JVM_ARGS = "run.jvmargs"; // NOI18N
    public static final String RUN_WORK_DIR = "work.dir"; // NOI18N
    public static final String MAIN_CLASS = "main.class"; // NOI18N
    
    
                
    // Properties stored in the PRIVATE.PROPERTIES
    public static final String APPLICATION_ARGS = "application.args"; // NOI18N

    

    // CustomizerRun
    Document MAIN_CLASS_MODEL;
    Document APPLICATION_ARGS_MODEL;
    Document RUN_JVM_ARGS_MODEL;
    Document RUN_WORK_DIR_MODEL;


    // CustomizerRunTest

    // Private fields ----------------------------------------------------------    
    private BluejProject project;
    private HashMap properties;    
    private UpdateHelper updateHelper;
    private PropertyEvaluator evaluator;
    
    private StoreGroup privateGroup; 
    private StoreGroup projectGroup;
    
    private Properties additionalProperties;    
    
    BluejProject getProject() {
        return project;
    }
    
    /** Creates a new instance of J2SEUIProperties and initializes them */
    public BluejProjectProperties( BluejProject project, UpdateHelper updateHelper, PropertyEvaluator evaluator ) {
        this.project = project;
        this.updateHelper  = updateHelper;
        this.evaluator = evaluator;
        privateGroup = new StoreGroup();
        projectGroup = new StoreGroup();
                
        
        additionalProperties = new Properties();
        
        init(); // Load known properties        
    }

    /** Initializes the visual models 
     */
    private void init() {
        
    
                
        // CustomizerLibraries
        EditableProperties projectProperties = updateHelper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );                
        

        // CustomizerRun
        MAIN_CLASS_MODEL = projectGroup.createStringDocument( evaluator, MAIN_CLASS ); 
        APPLICATION_ARGS_MODEL = privateGroup.createStringDocument( evaluator, APPLICATION_ARGS );
        RUN_JVM_ARGS_MODEL = projectGroup.createStringDocument( evaluator, RUN_JVM_ARGS );
        RUN_WORK_DIR_MODEL = privateGroup.createStringDocument( evaluator, RUN_WORK_DIR );
                
    }
    
    public void save() {
        try {                        
            // Store properties 
            Boolean result = (Boolean) ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                   
                    storeProperties();
                    return Boolean.TRUE;
                }
            });
            // and save the project
            if (result == Boolean.TRUE) {
                ProjectManager.getDefault().saveProject(project);
            }
        } 
        catch (MutexException e) {
            ErrorManager.getDefault().notify((IOException)e.getException());
        }
        catch ( IOException ex ) {
            ErrorManager.getDefault().notify( ex );
        }
    }
    
    
        
    private void storeProperties() throws IOException {
        // Store special properties
        

        // Store standard properties
        EditableProperties projectProperties = updateHelper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );        
        EditableProperties privateProperties = updateHelper.getProperties( AntProjectHelper.PRIVATE_PROPERTIES_PATH );
        
        if ( getDocumentText( RUN_WORK_DIR_MODEL ).trim().equals( "" ) ) { // NOI18N
            privateProperties.remove( RUN_WORK_DIR ); // Remove the property completely if not set
        }
        
        // Standard store of the properties
        projectGroup.store( projectProperties );        
        privateGroup.store( privateProperties );
                
        storeAdditionalProperties(projectProperties);
        
        // Store the property changes into the project
        updateHelper.putProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties );
        updateHelper.putProperties( AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProperties );        
        
    }
  
    private void storeAdditionalProperties(EditableProperties projectProperties) {
        for (Iterator i = additionalProperties.keySet().iterator(); i.hasNext();) {
            String key = (String)i.next();
            projectProperties.put(key, additionalProperties.getProperty(key));
        }
    }
    
    private static String getDocumentText( Document document ) {
        try {
            return document.getText( 0, document.getLength() );
        }
        catch( BadLocationException e ) {
            return ""; // NOI18N
        }
    }
    
    /* This is used by CustomizerWSServiceHost */
    public void putAdditionalProperty(String propertyName, String propertyValue) {
        additionalProperties.setProperty(propertyName, propertyValue);
    }
    
//    private static boolean showModifiedMessage (String title) {
//        String message = NbBundle.getMessage(BluejProjectProperties.class,"TXT_Regenerate");
//        JButton regenerateButton = new JButton (NbBundle.getMessage(BluejProjectProperties.class,"CTL_RegenerateButton"));
//        regenerateButton.setDefaultCapable(true);
//        regenerateButton.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage(BluejProjectProperties.class,"AD_RegenerateButton"));
//        NotifyDescriptor d = new NotifyDescriptor.Message (message, NotifyDescriptor.WARNING_MESSAGE);
//        d.setTitle(title);
//        d.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
//        d.setOptions(new Object[] {regenerateButton, NotifyDescriptor.CANCEL_OPTION});        
//        return DialogDisplayer.getDefault().notify(d) == regenerateButton;
//    }

}

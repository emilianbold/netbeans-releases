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

package org.netbeans.bluej;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CategoryComponentProvider;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class CustomizerProviderImpl implements CustomizerProvider {
    private BluejProject project;

    private UpdateHelper updateHelper;

    private PropertyEvaluator evaluator;
    /** Creates a new instance of CustomizerProviderImpl */
    public CustomizerProviderImpl(BluejProject proj, PropertyEvaluator eval, UpdateHelper helper) {
        project = proj;
        updateHelper = helper;
        evaluator = eval;
    }
    
    public void showCustomizer() {
        
        ProjectCustomizer.Category runCat = ProjectCustomizer.Category.create("run", "Run", null, null);
        BluejProjectProperties props = new BluejProjectProperties(project, updateHelper, evaluator);
        CategoryComponentProvider provider = new SubCategoryProvider(props);
        OptionListener listener = new OptionListener(project, props);
        Dialog dialog = ProjectCustomizer.createCustomizerDialog(new ProjectCustomizer.Category[] {runCat},  provider, null, listener, null);
////            OptionListener listener = new OptionListener( project, uiProperties );
//            dialog = ProjectCustomizer.createCustomizerDialog( CUSTOMIZER_FOLDER_PATH, context, preselectedCategory, listener, null );
            dialog.addWindowListener( listener );
            dialog.setTitle( MessageFormat.format(
                    NbBundle.getMessage( CustomizerProviderImpl.class, "LBL_Customizer_Title" ), // NOI18N
                    new Object[] { ProjectUtils.getInformation(project).getDisplayName() } ) );
//
        dialog.setVisible(true);
        
    }
    
    
    
    /** Listens to the actions on the Customizer's option buttons */
    private class OptionListener extends WindowAdapter implements ActionListener {
        
        private Project project;
        private BluejProjectProperties uiProperties;
        private Dialog dialog;
        
        OptionListener( Project project, BluejProjectProperties uiProperties ) {
            this.project = project;
            this.uiProperties = uiProperties;
        }
        
        public void setDialog(Dialog dial) {
            dialog = dial;
        }
        
        // Listening to OK button ----------------------------------------------
        
        public void actionPerformed( ActionEvent e ) {
            // Store the properties into project
            uiProperties.save();
            
            // Close & dispose the the dialog
            if ( dialog != null ) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }
        
        // Listening to window events ------------------------------------------
        
        public void windowClosed( WindowEvent e) {
        }
        
        public void windowClosing(WindowEvent e) {
            //Dispose the dialog otherwsie the {@link WindowAdapter#windowClosed}
            //may not be called
            if ( dialog != null ) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }
    }
    
    static final class SubCategoryProvider implements ProjectCustomizer.CategoryComponentProvider {

        private BluejProjectProperties properties;
        
        
        SubCategoryProvider(BluejProjectProperties props) {
            properties = props;
        }
        
        public JComponent create(ProjectCustomizer.Category category) {
            return new CustomizerRun(properties);
        }
    }
    
}

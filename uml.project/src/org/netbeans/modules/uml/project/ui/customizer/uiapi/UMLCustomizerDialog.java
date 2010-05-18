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

package org.netbeans.modules.uml.project.ui.customizer.uiapi;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.spi.project.ui.support.*;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CategoryComponentProvider;



/** Implementation of standard customizer dialog.
 *
 * @author Petr Hrebejk
 * copied because we need access to the OK button
 */
public class UMLCustomizerDialog {
    
    /** Factory class only 
     */
    private UMLCustomizerDialog() {}
    
    // Option indexes
    private static final int OPTION_OK = 0;
    private static final int OPTION_CANCEL = OPTION_OK + 1;
    
    // Option command names
    private static final String COMMAND_OK = "OK";          // NOI18N
    private static final String COMMAND_CANCEL = "CANCEL";  // NOI18N
                
    public static Dialog createDialog( ActionListener okOptionListener, JPanel innerPane, HelpCtx helpCtx ) {
        
        // Create options
        JButton options[] = new JButton[] { 
            new JButton( NbBundle.getMessage( UMLCustomizerDialog.class, "LBL_Customizer_Ok_Option") ), // NOI18N
            new JButton( NbBundle.getMessage( UMLCustomizerDialog.class, "LBL_Customizer_Cancel_Option" ) ) , // NOI18N
        };

        // Set commands
        options[ OPTION_OK ].setActionCommand( COMMAND_OK );
        options[ OPTION_CANCEL ].setActionCommand( COMMAND_CANCEL );
        
        //A11Y
        options[ OPTION_OK ].getAccessibleContext().setAccessibleDescription ( NbBundle.getMessage( UMLCustomizerDialog.class, "AD_Customizer_Ok_Option") ); // NOI18N
        options[ OPTION_CANCEL ].getAccessibleContext().setAccessibleDescription ( NbBundle.getMessage( UMLCustomizerDialog.class, "AD_Customizer_Cancel_Option") ); // NOI18N
        

        // RegisterListener
        ActionListener optionsListener = new OptionListener( okOptionListener );        
        options[ OPTION_OK ].addActionListener( optionsListener );
        options[ OPTION_CANCEL ].addActionListener( optionsListener );
        
        innerPane.getAccessibleContext().setAccessibleName( NbBundle.getMessage( UMLCustomizerDialog.class, "AN_ProjectCustomizer") ); //NOI18N
        innerPane.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( UMLCustomizerDialog.class, "AD_ProjectCustomizer") ); //NOI18N      
                        
        if ( helpCtx == null ) {
            helpCtx = HelpCtx.DEFAULT_HELP;
        }
        
        DialogDescriptor dialogDescriptor = new DialogDescriptor( 
            innerPane,                             // innerPane
            NbBundle.getMessage( UMLCustomizerDialog.class, "LBL_Customizer_Title" ), // NOI18N // displayName
            false,                                  // modal
            options,                                // options
            options[OPTION_OK],                     // initial value
            DialogDescriptor.BOTTOM_ALIGN,          // options align
            helpCtx,                                // helpCtx
            null );                                 // listener 

        innerPane.addPropertyChangeListener( new HelpCtxChangeListener( dialogDescriptor, helpCtx ) );
        innerPane.addPropertyChangeListener( new OkEnableListener( dialogDescriptor) );
        
        if ( innerPane instanceof HelpCtx.Provider ) {
            dialogDescriptor.setHelpCtx( ((HelpCtx.Provider)innerPane).getHelpCtx() );
        }
        dialogDescriptor.setClosingOptions( new Object[] { options[ OPTION_OK ], options[ OPTION_CANCEL ] } );

        Dialog dialog = DialogDisplayer.getDefault().createDialog( dialogDescriptor );
        return dialog;
        
    }    
    
    /** Listens to the actions on the Customizer's option buttons */
    private static class OptionListener implements ActionListener {
    
        private ActionListener okOptionListener;
        
        OptionListener( ActionListener okOptionListener ) {
            this.okOptionListener = okOptionListener;
        }
        
        public void actionPerformed( ActionEvent e ) {
            String command = e.getActionCommand();
            
            if ( COMMAND_OK.equals( command ) ) {
                // Call the OK option listener
                okOptionListener.actionPerformed( e ); // XXX maybe create new event
            }
            
        }        
        
    }
    
    private static class HelpCtxChangeListener implements PropertyChangeListener {
                
        DialogDescriptor dialogDescriptor;
        HelpCtx defaultHelpCtx;
        
        HelpCtxChangeListener( DialogDescriptor dialogDescriptor, HelpCtx defaultHelpCtx ) {
            this.dialogDescriptor = dialogDescriptor;
            this.defaultHelpCtx = defaultHelpCtx;
        }
        
        public void propertyChange( PropertyChangeEvent evt ) {
            
            if ( CustomizerPane.HELP_CTX_PROPERTY.equals( evt.getPropertyName() ) ) {
                HelpCtx newHelp = (HelpCtx)evt.getNewValue();
                dialogDescriptor.setHelpCtx( newHelp == null  || newHelp == HelpCtx.DEFAULT_HELP  ? defaultHelpCtx : newHelp );
            }
                        
        }        
        
    }
    
    private static class OkEnableListener implements PropertyChangeListener
    {
       DialogDescriptor dialogDescriptor;
       
       public OkEnableListener(DialogDescriptor dialogDescriptor)
       {
          this.dialogDescriptor = dialogDescriptor;
       }
       
       public void propertyChange( PropertyChangeEvent evt )
       {
          if ( CustomizerPane.OK_ENABLE_PROPERTY.equals( evt.getPropertyName() ) ) {
                Boolean value = (Boolean)evt.getNewValue();
                Object[] options = dialogDescriptor.getOptions();
                
                if ((options != null) && (options.length > OPTION_OK) && 
                   (options[OPTION_OK] instanceof JButton))
                {
                   JButton okBtn = (JButton)options[OPTION_OK];
                   // okBtn.setEnabled(value.booleanValue());
                }
            }
       }
    }
          
     /** Creates standard which can be used for implementation
     * of {@link org.netbeans.spi.project.ui.CustomizerProvider}. You don't need
     * to call <code>pack()</code> method on the dialog. The resulting dialog will 
     * be non-modal. <br> 
     * Call <code>show()</code> on the dialog to make it visible. If you wnat the dialog to be 
     * closed after user presses the "OK" button you have to call hide() and dispose() on it.
     * (Usually in the <code>actionPerformed(...)</code> method of the listener
     * you provided as a parameter. In case of the click on the "Cancel" button 
     * the dialog will be closed automatically.
     * @param categories array of descriptions of categories to be shown in 
     *        the dialog.
     * @param componentProvider creator of GUI components for categories in the
     *        customizer dialog.
     * @param preselectedCategory name of one of the supplied categories or null. 
     *        Category with given name will be selected. If  <code>null</code>
     *        or if the category of given name does not exist the first category will 
     *        be selected. 
     * @param okOptionListener listener which will be notified when the user presses 
     *        the OK button.
     * @param helpCtx Help context for the dialog, which will be used when the 
     *        panels in the customizer do not specify their own help context.
     * @return standard project customizer dialog.
     */    
    public static Dialog createCustomizerDialog( Category[] categories,
                                                 CategoryComponentProvider componentProvider,
                                                 String preselectedCategory,
                                                 ActionListener okOptionListener,
                                                 HelpCtx helpCtx ) {
        JPanel innerPane = createCustomizerPane( categories, componentProvider, preselectedCategory );
        Dialog dialog = UMLCustomizerDialog.createDialog( okOptionListener, innerPane, helpCtx );
        return dialog;
    }
        
    /** Creates standard innerPane for customizer dialog.
     */
    private static JPanel createCustomizerPane( Category[] categories,
                                                CategoryComponentProvider componentProvider,
                                                String preselectedCategory ) {
        
        CategoryModel categoryModel = new CategoryModel( categories );
        JPanel categoryView = new CategoryView( categoryModel );
        JPanel customizerPane = new CustomizerPane( categoryView, categoryModel, componentProvider );
        
        if ( preselectedCategory == null ) {
            preselectedCategory = categories[0].getName();
        }
        
        Category c = categoryModel.getCategory( preselectedCategory );
        if ( c != null ) {
            categoryModel.setCurrentCategory( c );
        }
        
        return customizerPane;
    }   
    
    
}

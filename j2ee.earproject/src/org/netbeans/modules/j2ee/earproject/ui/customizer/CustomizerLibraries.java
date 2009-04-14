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

package org.netbeans.modules.j2ee.earproject.ui.customizer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.j2ee.common.SharabilityUtility;
import org.netbeans.modules.j2ee.earproject.EarProjectGenerator;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport;
import org.netbeans.modules.java.api.common.project.ui.customizer.EditMediator;
import org.netbeans.spi.java.project.support.ui.SharableLibrariesUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public final class CustomizerLibraries extends JPanel implements HelpCtx.Provider, ListDataListener {

    private EarProjectProperties uiProperties;
    private boolean isSharable;
    
    public CustomizerLibraries(final EarProjectProperties uiProperties) {
        this.uiProperties = uiProperties;
        initComponents();
        jListCp.setModel( uiProperties.DEBUG_CLASSPATH_MODEL );
        jListCp.setCellRenderer( uiProperties.CLASS_PATH_LIST_RENDERER );
        EditMediator.register( uiProperties.getProject(),
                               uiProperties.getProject().getAntProjectHelper(),
                               uiProperties.getProject().getReferenceHelper(),
                               EditMediator.createListComponent( jListCp) , 
                               jButtonAddJar.getModel(), 
                               jButtonAddLib.getModel(), 
                               jButtonAddProject.getModel(), 
                               jButtonRemove.getModel(), 
                               jButtonMoveUp.getModel(), 
                               jButtonMoveDown.getModel(),
                               jButtonEdit.getModel(),
                               uiProperties.SHARED_LIBRARIES_MODEL,
                               null,
                               new String[]{EjbProjectConstants.ARTIFACT_TYPE_J2EE_MODULE_IN_EAR_ARCHIVE, JavaProjectConstants.ARTIFACT_TYPE_JAR, JavaProjectConstants.ARTIFACT_TYPE_FOLDER},
                               EditMediator.JAR_ZIP_FILTER, JFileChooser.FILES_AND_DIRECTORIES
                               );
        librariesLocation.setDocument(uiProperties.SHARED_LIBRARIES_MODEL);
        testBroken();
        uiProperties.DEBUG_CLASSPATH_MODEL.addListDataListener( this );
        //check the sharability status of the project.
        isSharable = uiProperties.getProject().getAntProjectHelper().isSharableProject();
        if (!isSharable) {
            sharedLibrariesLabel.setEnabled(false);
            librariesLocation.setEnabled(false);
            librariesBrowse.setText(NbBundle.getMessage(CustomizerLibraries.class, 
                    "LBL_CustomizeLibraries_MakeSharable"));
        } else {
            librariesLocation.setText(uiProperties.getProject().getAntProjectHelper().getLibrariesLocation());
        }
    }
    
    private void testBroken() {
        
        DefaultListModel[] models = new DefaultListModel[] {
            uiProperties.DEBUG_CLASSPATH_MODEL,
        };
        
        boolean broken = false;
        
        for( int i = 0; i < models.length; i++ ) {
            for( Iterator it = ClassPathUiSupport.getIterator( models[i] ); it.hasNext(); ) {
                if ( ((ClassPathSupport.Item)it.next()).isBroken() ) {
                    broken = true;
                    break;
                }
            }
            if ( broken ) {
                break;
            }
        }
        
        if ( broken ) {
            jLabelErrorMessage.setText( NbBundle.getMessage( CustomizerLibraries.class, "LBL_CustomizeLibraries_Libraries_Error" ) ); // NOI18N
        }
        else {
            jLabelErrorMessage.setText( " " ); // NOI18N
        }
//        J2eeArchiveLogicalViewProvider viewProvider = (J2eeArchiveLogicalViewProvider) uiProperties.getProject().getLookup().lookup(J2eeArchiveLogicalViewProvider.class);
//        //Update the state of project's node if needed
//        viewProvider.testBroken();        
    }
    
    /** split file name into folder and name */
    private static String[] splitPath(String s) {
        int i = Math.max(s.lastIndexOf('/'), s.lastIndexOf('\\'));
        if (i == -1) {
            return new String[]{s, null};
        } else {
            return new String[]{s.substring(0, i), s.substring(i+1)};
        }
    }

    private void switchLibrary() {
        String loc = librariesLocation.getText();
        LibraryManager man;
        if (loc.trim().length() > -1) {
            try {
                File base = FileUtil.toFile(uiProperties.getProject().getProjectDirectory());
                File location = FileUtil.normalizeFile(PropertyUtils.resolveFile(base, loc));
                URL url = location.toURI().toURL();
                man = LibraryManager.forLocation(url);
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
                //TODO show as error in UI
                man = LibraryManager.getDefault();
            }
        } else {
            man = LibraryManager.getDefault();
        }
        
        
        DefaultListModel[] models = new DefaultListModel[]{
            uiProperties.DEBUG_CLASSPATH_MODEL,
           };
        for (int i = 0; i < models.length; i++) {
            for (Iterator it = ClassPathUiSupport.getIterator(models[i]); it.hasNext();) {
                ClassPathSupport.Item itm = (ClassPathSupport.Item) it.next();
                if (itm.getType() == ClassPathSupport.Item.TYPE_LIBRARY) {
                    itm.reassignLibraryManager(man);
                }
            }
        }
        jListCp.repaint();
        testBroken();
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelEmbeddedCP = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jListCp = new javax.swing.JList();
        jButtonAddJar = new javax.swing.JButton();
        jButtonAddLib = new javax.swing.JButton();
        jButtonAddProject = new javax.swing.JButton();
        jButtonRemove = new javax.swing.JButton();
        sharedLibrariesLabel = new javax.swing.JLabel();
        librariesLocation = new javax.swing.JTextField();
        librariesBrowse = new javax.swing.JButton();
        jButtonEdit = new javax.swing.JButton();
        jButtonMoveUp = new javax.swing.JButton();
        jButtonMoveDown = new javax.swing.JButton();
        jLabelErrorMessage = new javax.swing.JLabel();

        jLabelEmbeddedCP.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelEmbeddedCP, org.openide.util.NbBundle.getBundle(CustomizerLibraries.class).getString("LBL_CustomizerRun_EmbeddedClasspathElements_JLabel")); // NOI18N
        jLabelEmbeddedCP.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        jScrollPane2.setPreferredSize(new java.awt.Dimension(252, 202));
        jScrollPane2.setViewportView(jListCp);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJar, org.openide.util.NbBundle.getBundle(CustomizerLibraries.class).getString("LBL_CustomizeEAR_AddJar_JButton")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddLib, org.openide.util.NbBundle.getBundle(CustomizerLibraries.class).getString("LBL_CustomizeCompile_Classpath_AddLibrary_JButton")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddProject, org.openide.util.NbBundle.getBundle(CustomizerLibraries.class).getString("LBL_CustomizeCompile_Classpath_AddProject_JButton")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemove, org.openide.util.NbBundle.getBundle(CustomizerLibraries.class).getString("LBL_CustomizeCompile_Classpath_Remove_JButton")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(sharedLibrariesLabel, java.text.MessageFormat.format(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeGeneral_SharedLibraries"), new Object[] {})); // NOI18N

        librariesLocation.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(librariesBrowse, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizerLibraries_Browse_JButton")); // NOI18N
        librariesBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                librariesBrowseActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButtonEdit, org.openide.util.NbBundle.getBundle(CustomizerLibraries.class).getString("LBL_CustomizeCompile_Classpath_Edit_JButton")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveUp, org.openide.util.NbBundle.getBundle(CustomizerLibraries.class).getString("LBL_CustomizeCompile_Classpath_MoveUp_JButton")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveDown, org.openide.util.NbBundle.getBundle(CustomizerLibraries.class).getString("LBL_CustomizeCompile_Classpath_MoveDown_JButton")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabelErrorMessage, " ");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jLabelEmbeddedCP)
                .addContainerGap())
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(sharedLibrariesLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(librariesLocation, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE))
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, librariesBrowse, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jButtonAddProject, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jButtonAddLib, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jButtonAddJar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jButtonEdit, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jButtonRemove, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jButtonMoveUp, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jButtonMoveDown, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)))
            .add(jLabelErrorMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 539, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(sharedLibrariesLabel)
                    .add(librariesLocation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(librariesBrowse))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabelEmbeddedCP)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jButtonAddProject)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonAddLib)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonAddJar)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jButtonEdit)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jButtonRemove)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jButtonMoveUp)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonMoveDown))
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabelErrorMessage))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void librariesBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_librariesBrowseActionPerformed
        if (!isSharable) {
            boolean result = makeSharable(uiProperties, new String[1]);
            if (result) {
                isSharable = true;
                sharedLibrariesLabel.setEnabled(true);
                librariesLocation.setEnabled(true);
                librariesLocation.setText(uiProperties.getProject().getAntProjectHelper().getLibrariesLocation());
                Mnemonics.setLocalizedText(librariesBrowse, NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizerLibraries_Browse_JButton")); // NOI18N
                updateJars(uiProperties.DEBUG_CLASSPATH_MODEL);
                switchLibrary();
            }
        } else {
            File prjLoc = FileUtil.toFile(uiProperties.getProject().getProjectDirectory());
            String s[] = splitPath(librariesLocation.getText().trim());
            String loc = SharableLibrariesUtils.browseForLibraryLocation(s[0], this, prjLoc);
            if (loc != null) {
                librariesLocation.setText(s[1] != null ? loc + File.separator + s[1] :
                    loc + File.separator + SharableLibrariesUtils.DEFAULT_LIBRARIES_FILENAME);
                switchLibrary();
            }
        }
    }//GEN-LAST:event_librariesBrowseActionPerformed

    static boolean makeSharable(final EarProjectProperties uiProperties, final String returnServerLibrary[]) {
        List<String> libs = new ArrayList<String>();
        List<String> jars = new ArrayList<String>();
        collectLibs(uiProperties.DEBUG_CLASSPATH_MODEL, libs, jars);
        collectLibs(uiProperties.EAR_CONTENT_ADDITIONAL_MODEL.getDefaultListModel(), libs, jars);
        libs.add("CopyLibs"); // NOI18N
        boolean res = SharableLibrariesUtils.showMakeSharableWizard(uiProperties.getProject().getAntProjectHelper(), uiProperties.getProject().getReferenceHelper(), libs, jars);
        if (res) {
            // more or less just for consistency I'm adding server library question here.
            // server library IMO never made any sense in EAR as it never compiles anything
            // against server jars
            if (DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_ServerLibrary"),
                    NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_ServerLibrary_Title"),
                    NotifyDescriptor.YES_NO_OPTION)) == NotifyDescriptor.YES_OPTION) {
                ProjectManager.mutex().writeAccess(new Runnable() {
                    public void run()  {
                        File loc = PropertyUtils.resolveFile(FileUtil.toFile(uiProperties.getProject().getProjectDirectory()), uiProperties.getProject().getAntProjectHelper().getLibrariesLocation());
                        String serverID = uiProperties.getProject().evaluator().getProperty(EarProjectProperties.J2EE_SERVER_INSTANCE);
                        try {
                            Library serverLibrary = SharabilityUtility.findOrCreateLibrary(loc, serverID);
                            assert returnServerLibrary.length == 1;
                            returnServerLibrary[0] = serverLibrary.getName();
//                            EditableProperties ep = uiProperties.getProject().getAntProjectHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
//                            EarProjectGenerator.setServerProperties(ep, serverLibrary.getName());
//                            ProjectManager.getDefault().saveProject(uiProperties.getProject());
//                            ClassPathUiSupport.addLibraries(uiProperties.JAVAC_CLASSPATH_MODEL.getDefaultListModel(),
//                                    null, new Library[]{serverLibrary}, new HashSet<Library>(), null);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            return;
                        }
                    }
                });
            }
        }
        return res;
    }

    private static void collectLibs(DefaultListModel model, List<String> libs, List<String> jarReferences) {
        for (int i = 0; i < model.size(); i++) {
            ClassPathSupport.Item item = (ClassPathSupport.Item) model.get(i);
            if (item.getType() == ClassPathSupport.Item.TYPE_LIBRARY) {
                if (!item.isBroken() && !libs.contains(item.getLibrary().getName())) {
                    libs.add(item.getLibrary().getName());
                }
            }
            if (item.getType() == ClassPathSupport.Item.TYPE_JAR) {
                if (item.getReference() != null && item.getVariableBasedProperty() == null && !jarReferences.contains(item.getReference())) {
                    //TODO reference is null for not yet persisted items.
                    // there seems to be no way to generate a reference string without actually
                    // creating and writing the property..
                    jarReferences.add(item.getReference());
                }
            }
        }
    }    

    private void updateJars(DefaultListModel model) {
        for (int i = 0; i < model.size(); i++) {
            ClassPathSupport.Item item = (ClassPathSupport.Item) model.get(i);
            if (item.getType() == ClassPathSupport.Item.TYPE_JAR) {
                if (item.getReference() != null) {
                    item.updateJarReference(uiProperties.getProject().getAntProjectHelper());
                }
            }
        }
        
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddJar;
    private javax.swing.JButton jButtonAddLib;
    private javax.swing.JButton jButtonAddProject;
    private javax.swing.JButton jButtonEdit;
    private javax.swing.JButton jButtonMoveDown;
    private javax.swing.JButton jButtonMoveUp;
    private javax.swing.JButton jButtonRemove;
    private javax.swing.JLabel jLabelEmbeddedCP;
    private javax.swing.JLabel jLabelErrorMessage;
    private javax.swing.JList jListCp;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton librariesBrowse;
    private javax.swing.JTextField librariesLocation;
    private javax.swing.JLabel sharedLibrariesLabel;
    // End of variables declaration//GEN-END:variables
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizerLibraries.class);
    }

    public void intervalAdded(ListDataEvent e) {
    }

    public void intervalRemoved(ListDataEvent e) {
        testBroken(); 
    }

    public void contentsChanged(ListDataEvent e) {
        testBroken(); 
    }
    
}

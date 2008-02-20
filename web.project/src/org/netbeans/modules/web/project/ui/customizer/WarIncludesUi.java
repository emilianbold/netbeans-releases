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

package org.netbeans.modules.web.project.ui.customizer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.ButtonModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;

import javax.swing.text.Document;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import org.netbeans.api.java.project.JavaProjectConstants;

import org.netbeans.api.project.ant.FileChooser;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryChooser;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.j2ee.common.project.classpath.ClassPathSupport;
import org.netbeans.modules.j2ee.common.project.ui.AntArtifactChooser;
import org.netbeans.modules.j2ee.common.project.ui.ProjectProperties;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.modules.web.project.classpath.ClassPathSupportCallbackImpl;
import org.netbeans.modules.j2ee.common.project.ui.UserProjectSettings;
import org.netbeans.modules.web.project.ui.customizer.WarIncludesUiSupport.ClasspathTableModel;
import org.openide.util.Exceptions;

/** Classes containing code speciic for handling UI of J2SE project classpath 
 *
 * @author Petr Hrebejk, Radko Najman
 */
public class WarIncludesUi {
    
    private WarIncludesUi() {}
           
    // Innerclasses ------------------------------------------------------------

    public static class EditMediator implements ActionListener, ListSelectionListener, TableModelListener {
                
        private final WebProject project;
        private final JTable list;
        private final ClasspathTableModel listModel;
        private final ListSelectionModel selectionModel;
        private final ButtonModel addJar;
        private final ButtonModel addLibrary;
        private final ButtonModel addAntArtifact;
        private final ButtonModel remove;
        private Document libraryPath;
                    
        public EditMediator( WebProject project,
                             JTable list,
                             ButtonModel addJar,
                             ButtonModel addLibrary, 
                             ButtonModel addAntArtifact,
                             ButtonModel remove,
                             Document libPath) {
                             
            this.list = list;
            
            if ( !( list.getModel() instanceof ClasspathTableModel ) ) {
                throw new IllegalArgumentException( "The list's model has to be of class DefaultListModel" ); // NOI18N
            }
            
            this.listModel = (ClasspathTableModel) list.getModel();
            this.selectionModel = list.getSelectionModel();
            
            this.addJar = addJar;
            this.addLibrary = addLibrary;
            this.addAntArtifact = addAntArtifact;
            this.remove = remove;
            this.libraryPath = libPath;

            this.project = project;
        }

        public static void register(WebProject project,
                                    JTable list,
                                    ButtonModel addJar,
                                    ButtonModel addLibrary, 
                                    ButtonModel addAntArtifact,
                                    ButtonModel remove,
                                    Document libPath) {
            
            EditMediator em = new EditMediator(project, list, addJar, addLibrary, addAntArtifact, remove, libPath);
                        
            // Register the listener on all buttons
            addJar.addActionListener( em ); 
            addLibrary.addActionListener( em );
            addAntArtifact.addActionListener( em );
            remove.addActionListener( em );
            // On list selection
            em.selectionModel.addListSelectionListener( em );
            em.listModel.addTableModelListener(em);
            // Set the initial state of the buttons
            em.valueChanged( null );
        }
    
        // Implementation of ActionListener ------------------------------------
        
        /** Handles button events
         */        
        public void actionPerformed( ActionEvent e ) {
            
            Object source = e.getSource();
            
            if ( source == addJar ) { 
                // Let user search for the Jar file
                FileChooser chooser = new FileChooser(project.getAntProjectHelper(), true);
                FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
                chooser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
                chooser.setMultiSelectionEnabled( true );
                chooser.setDialogTitle( NbBundle.getMessage( WarIncludesUi.class, "LBL_AddFile_DialogTitle" ) ); // NOI18N
                chooser.setAcceptAllFileFilterUsed(true);
                File curDir = UserProjectSettings.getDefault().getLastUsedClassPathFolder(); 
                chooser.setCurrentDirectory (curDir);
                int option = chooser.showOpenDialog( SwingUtilities.getWindowAncestor( list ) ); // Show the chooser
                
                if ( option == JFileChooser.APPROVE_OPTION ) {
                    
                    String filePaths[];
                    try {
                        filePaths = chooser.getSelectedPaths();
                    } catch (IOException ex) {
                        // TODO: add localized message
                        Exceptions.printStackTrace(ex);
                        return;
                    }
                    WarIncludesUiSupport.addJarFiles(filePaths, FileUtil.toFile(project.getProjectDirectory()), listModel);
                    curDir = FileUtil.normalizeFile(chooser.getCurrentDirectory());
                    UserProjectSettings.getDefault().setLastUsedClassPathFolder(curDir);
                }
            }
            else if ( source == addLibrary ) {
                Set/*<Library>*/includedLibraries = new HashSet ();
                Iterator it = WarIncludesUiSupport.getIterator(listModel);
                while (it.hasNext()) {
                    ClassPathSupport.Item item = (ClassPathSupport.Item) it.next();
                    if (item.getType() == ClassPathSupport.Item.TYPE_LIBRARY) {
                        includedLibraries.add( item.getLibrary() );
                    }
                }
                
                Set<Library> added = LibraryChooser.showDialog(
                    project.getReferenceHelper().getProjectLibraryManager(), null, 
                    project.getReferenceHelper().getLibraryChooserImportHandler()); // XXX restrict to j2se libs only?
                if (added != null) {
                   WarIncludesUiSupport.addLibraries(added.toArray(new Library[added.size()]), includedLibraries, list);
                }
            }
            else if ( source == addAntArtifact ) { 
                AntArtifactChooser.ArtifactItem artifactItems[] = AntArtifactChooser.showDialog(
                        new String[] {JavaProjectConstants.ARTIFACT_TYPE_JAR, JavaProjectConstants.ARTIFACT_TYPE_FOLDER}, project, list.getParent());
                if (artifactItems != null) {
                    WarIncludesUiSupport.addArtifacts(artifactItems, listModel);
                }
            }
            else if ( source == remove ) { 
                WarIncludesUiSupport.remove( list);
            }
        }    
        
        
        /** Handles changes in the selection
         */        
        public void valueChanged( ListSelectionEvent e ) {
            DefaultListSelectionModel sm = (DefaultListSelectionModel) list.getSelectionModel();
            int index = sm.getMinSelectionIndex();
            
            // remove enabled only if selection is not empty
            boolean canRemove = index != -1;
            // and when the selection does not contain unremovable item
            if (canRemove) {
                ClassPathSupport.Item vcpi = (ClassPathSupport.Item) listModel.getValueAt(index, 0);
                if (!vcpi.canDelete())
                    canRemove = false;
            }
                        
            remove.setEnabled(canRemove);

        }
        
        // TableModelListener --------------------------------------
        public void tableChanged(TableModelEvent e) {
            if (e.getColumn() == 1) {
                ClassPathSupport.Item cpItem = (ClassPathSupport.Item) listModel.getValueAt(e.getFirstRow(), 0);
                String newPathInWar = (String) listModel.getValueAt(e.getFirstRow(), 1);
                String message = null;
//                if (userInitiatedChange && cpItem.getType() == ClassPathSupport.Item.TYPE_JAR && newPathInWar.startsWith("WEB-INF")) { //NOI18N
                if (cpItem.getType() == ClassPathSupport.Item.TYPE_JAR && newPathInWar.startsWith("WEB-INF")) { //NOI18N
                    if (newPathInWar.equals("WEB-INF\\lib") || newPathInWar.equals("WEB-INF/lib")) { //NOI18N
                        if (((File) cpItem.getObject()).isDirectory()) {
                            message = NbBundle.getMessage(WarIncludesUi.class,
                                "MSG_NO_FOLDER_IN_WEBINF_LIB", newPathInWar); // NOI18N
                        } else {
                            message = NbBundle.getMessage(WarIncludesUi.class,
                                "MSG_NO_FILE_IN_WEBINF_LIB", newPathInWar); // NOI18N
                        }
                    } else if (newPathInWar.equals("WEB-INF\\classes") || newPathInWar.equals("WEB-INF/classes")) { //NOI18N
                            message = NbBundle.getMessage(WarIncludesUi.class,
                                "MSG_NO_FOLDER_IN_WEBINF_CLASSES", newPathInWar); // NOI18N
                    }
                }
                if (message != null) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message (message, NotifyDescriptor.WARNING_MESSAGE));
                }
                cpItem.setAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT, (String) listModel.getValueAt(e.getFirstRow(), 1));
            }
        }
    }
    
    static class ClassPathCellRenderer extends DefaultTableCellRenderer {
        
        private static String RESOURCE_ICON_CLASSPATH = "org/netbeans/modules/web/project/ui/resources/referencedClasspath.gif"; //NOI18N
        
        private static ImageIcon ICON_FOLDER = null; 
        private static ImageIcon ICON_CLASSPATH  = new ImageIcon( Utilities.loadImage( RESOURCE_ICON_CLASSPATH ) );
        
        private static ImageIcon ICON_BROKEN_JAR;
        private static ImageIcon ICON_BROKEN_LIBRARY;
        private static ImageIcon ICON_BROKEN_ARTIFACT;

        // Contains well known paths in the WebProject
        private static final Map WELL_KNOWN_PATHS_NAMES = new HashMap();
        static {
            WELL_KNOWN_PATHS_NAMES.put( ProjectProperties.JAVAC_CLASSPATH, NbBundle.getMessage( WarIncludesUi.class, "LBL_JavacClasspath_DisplayName" ) );
            WELL_KNOWN_PATHS_NAMES.put( ProjectProperties.JAVAC_TEST_CLASSPATH, NbBundle.getMessage( WarIncludesUi.class,"LBL_JavacTestClasspath_DisplayName") );
            WELL_KNOWN_PATHS_NAMES.put( ProjectProperties.RUN_TEST_CLASSPATH, NbBundle.getMessage( WarIncludesUi.class, "LBL_RunTestClasspath_DisplayName" ) );
            WELL_KNOWN_PATHS_NAMES.put( ProjectProperties.BUILD_CLASSES_DIR, NbBundle.getMessage( WarIncludesUi.class, "LBL_BuildClassesDir_DisplayName" ) );            
            WELL_KNOWN_PATHS_NAMES.put( ProjectProperties.BUILD_TEST_CLASSES_DIR, NbBundle.getMessage (WarIncludesUi.class,"LBL_BuildTestClassesDir_DisplayName") );
        };

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String s = null;
            if (value instanceof ClassPathSupport.Item) {
                final ClassPathSupport.Item item = (ClassPathSupport.Item) value;
                setIcon(getIcon(item));
                setToolTipText(getToolTipText(item));
                s = getDisplayName(item);
            }
            return super.getTableCellRendererComponent(table, s, isSelected, false, row, column);
        }
        
        private String getDisplayName( ClassPathSupport.Item item ) {
            switch ( item.getType() ) {

                case ClassPathSupport.Item.TYPE_LIBRARY:
                    if ( item.isBroken() ) {
                        return NbBundle.getMessage( WarIncludesUi.class, "LBL_MISSING_LIBRARY", getLibraryName( item ) );
                    }
                    else { 
                        return item.getLibrary().getDisplayName();
                    }
                case ClassPathSupport.Item.TYPE_CLASSPATH:
                    String name = (String)WELL_KNOWN_PATHS_NAMES.get( CommonProjectUtils.getAntPropertyName( item.getReference() ) );
                    return name == null ? item.getReference() : name;
                case ClassPathSupport.Item.TYPE_ARTIFACT:
                    if ( item.isBroken() ) {
                        return NbBundle.getMessage( WarIncludesUi.class, "LBL_MISSING_PROJECT", getProjectName( item ) );
                    }
                    else {
                        return item.getArtifactURI().toString();
                    }
                case ClassPathSupport.Item.TYPE_JAR:
                    if ( item.isBroken() ) {
                        return NbBundle.getMessage( WarIncludesUi.class, "LBL_MISSING_FILE", getFileRefName( item ) );
                    }
                    else {
                        return item.getFilePath();
                    }
            }

            return item.getReference(); // XXX            
        }

        static Icon getIcon( ClassPathSupport.Item item ) {
            
            switch ( item.getType() ) {
                
                case ClassPathSupport.Item.TYPE_LIBRARY:
                    if ( item.isBroken() ) {
                        if ( ICON_BROKEN_LIBRARY == null ) {
                            ICON_BROKEN_LIBRARY = new ImageIcon( Utilities.mergeImages( ProjectProperties.ICON_LIBRARY.getImage(), ProjectProperties.ICON_BROKEN_BADGE.getImage(), 7, 7 ) );
                        }
                        return ICON_BROKEN_LIBRARY;
                    }
                    else {
                        return ProjectProperties.ICON_LIBRARY;
                    }
                case ClassPathSupport.Item.TYPE_ARTIFACT:
                    if ( item.isBroken() ) {
                        if ( ICON_BROKEN_ARTIFACT == null ) {
                            ICON_BROKEN_ARTIFACT = new ImageIcon( Utilities.mergeImages( ProjectProperties.ICON_ARTIFACT.getImage(), ProjectProperties.ICON_BROKEN_BADGE.getImage(), 7, 7 ) );
                        }
                        return ICON_BROKEN_ARTIFACT;
                    }
                    else {
                        return ProjectProperties.ICON_ARTIFACT;
                    }
                case ClassPathSupport.Item.TYPE_JAR:
                    if ( item.isBroken() ) {
                        if ( ICON_BROKEN_JAR == null ) {
                            ICON_BROKEN_JAR = new ImageIcon( Utilities.mergeImages( ProjectProperties.ICON_JAR.getImage(), ProjectProperties.ICON_BROKEN_BADGE.getImage(), 7, 7 ) );
                        }
                        return ICON_BROKEN_JAR;
                    }
                    else {
                        File file = item.getResolvedFile();
                        return file.isDirectory() ? getFolderIcon() : ProjectProperties.ICON_JAR;
                    }
                case ClassPathSupport.Item.TYPE_CLASSPATH:
                    return ICON_CLASSPATH;
                
            }
            
            return null; // XXX 
        }
        
        private String getToolTipText( ClassPathSupport.Item item ) {
//            if ( item.isBroken() && 
//                 ( item.getType() == ClassPathSupport.Item.TYPE_JAR || 
//                   item.getType() == ClassPathSupport.Item.TYPE_ARTIFACT )  ) {
//                return evaluator.evaluate( item.getReference() );
//            }
            
            return getDisplayName( item ); // XXX
        }
        
        private static ImageIcon getFolderIcon() {
        
            if ( ICON_FOLDER == null ) {
                FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
                DataFolder dataFolder = DataFolder.findFolder( root );
                ICON_FOLDER = new ImageIcon( dataFolder.getNodeDelegate().getIcon( BeanInfo.ICON_COLOR_16x16 ) );            
            }

            return ICON_FOLDER;   
        }

        private String getProjectName( ClassPathSupport.Item item ) {
            String ID = item.getReference();
            // something in the form of "${reference.project-name.id}"
            return ID.substring(12, ID.indexOf('.', 12)); // NOI18N
        }

        private String getLibraryName( ClassPathSupport.Item item ) {
            String ID = item.getReference();
            // something in the form of "${libs.junit.classpath}"
            return ID.substring(7, ID.indexOf(".classpath")); // NOI18N
        }

        private String getFileRefName( ClassPathSupport.Item item ) {
            String ID = item.getReference();        
            // something in the form of "${file.reference.smth.jar}"
            return ID.substring(17, ID.length()-1);
        }
    }
}

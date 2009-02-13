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

package org.netbeans.modules.ruby.javaint;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ButtonModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.project.Project;
import org.netbeans.modules.ruby.rubyproject.ProjectPropertyExtender;
import org.netbeans.modules.ruby.rubyproject.ui.FoldersListSettings;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyEvaluator;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/** Classes containing code speciic for handling UI of J2SE project classpath 
 *
 * @author Petr Hrebejk
 */
public class RubyClassPathUi {
    
    private RubyClassPathUi() {}
           
    // Innerclasses ------------------------------------------------------------
            
    /** Renderer which can be used to render the classpath in lists
     */    
    public static class ClassPathListCellRenderer extends DefaultListCellRenderer {
        
        private static final Pattern FOREIGN_PLAIN_FILE_REFERENCE = Pattern.compile("\\$\\{file\\.reference\\.([^${}]+)\\}"); // NOI18N
        private static final Pattern UNKNOWN_FILE_REFERENCE = Pattern.compile("\\$\\{([^${}]+)\\}"); // NOI18N
        
        private static String RESOURCE_ICON_JAR = "org/netbeans/modules/ruby/javaint//jar.gif"; //NOI18N
        private static String RESOURCE_ICON_LIBRARY = "org/netbeans/modules/ruby/javaint/libraries.gif"; //NOI18N
        private static String RESOURCE_ICON_ARTIFACT = "org/netbeans/modules/ruby/javaint//projectDependencies.gif"; //NOI18N
        private static String RESOURCE_ICON_CLASSPATH = "org/netbeans/modules/ruby/javaint//referencedClasspath.gif"; //NOI18N
        private static String RESOURCE_ICON_BROKEN_BADGE = "org/netbeans/modules/ruby/javaint//brokenProjectBadge.gif"; //NOI18N
        
        
        private static ImageIcon ICON_JAR = ImageUtilities.loadImageIcon(RESOURCE_ICON_JAR, false);
        private static ImageIcon ICON_FOLDER = null; 
        private static ImageIcon ICON_LIBRARY = ImageUtilities.loadImageIcon(RESOURCE_ICON_LIBRARY, false);
        private static ImageIcon ICON_ARTIFACT  = ImageUtilities.loadImageIcon(RESOURCE_ICON_ARTIFACT, false);
        private static ImageIcon ICON_CLASSPATH  = ImageUtilities.loadImageIcon(RESOURCE_ICON_CLASSPATH, false);
        private static ImageIcon ICON_BROKEN_BADGE  = ImageUtilities.loadImageIcon(RESOURCE_ICON_BROKEN_BADGE, false);
        
        private static ImageIcon ICON_BROKEN_JAR;
        private static ImageIcon ICON_BROKEN_LIBRARY;
        private static ImageIcon ICON_BROKEN_ARTIFACT;
                
        private PropertyEvaluator evaluator;
        
        // Contains well known paths in the RubyProject
        private static final Map WELL_KNOWN_PATHS_NAMES = new HashMap();
        static {
//            WELL_KNOWN_PATHS_NAMES.put( RubyProjectProperties.JAVAC_CLASSPATH, NbBundle.getMessage( RubyProjectProperties.class, "LBL_JavacClasspath_DisplayName" ) );
//            WELL_KNOWN_PATHS_NAMES.put( RubyProjectProperties.JAVAC_TEST_CLASSPATH, NbBundle.getMessage( RubyProjectProperties.class,"LBL_JavacTestClasspath_DisplayName") );
//            WELL_KNOWN_PATHS_NAMES.put( RubyProjectProperties.RUN_CLASSPATH, NbBundle.getMessage( RubyProjectProperties.class, "LBL_RunClasspath_DisplayName" ) );
//            WELL_KNOWN_PATHS_NAMES.put( RubyProjectProperties.RUN_TEST_CLASSPATH, NbBundle.getMessage( RubyProjectProperties.class, "LBL_RunTestClasspath_DisplayName" ) );
//            WELL_KNOWN_PATHS_NAMES.put( RubyProjectProperties.BUILD_CLASSES_DIR, NbBundle.getMessage( RubyProjectProperties.class, "LBL_BuildClassesDir_DisplayName" ) );            
//            WELL_KNOWN_PATHS_NAMES.put( RubyProjectProperties.BUILD_TEST_CLASSES_DIR, NbBundle.getMessage (RubyProjectProperties.class,"LBL_BuildTestClassesDir_DisplayName") );
        };
                
        public ClassPathListCellRenderer( PropertyEvaluator evaluator ) {
            super();
            this.evaluator = evaluator;
        }
        
        public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            
            ProjectPropertyExtender.Item item = (ProjectPropertyExtender.Item)value;
            
            super.getListCellRendererComponent( list, getDisplayName( item ), index, isSelected, cellHasFocus );
            setIcon( getIcon( item ) );
            setToolTipText( getToolTipText( item ) );
            
            return this;
        }
        
        
        private String getDisplayName( ProjectPropertyExtender.Item item ) {
            
            switch ( item.getType() ) {
                
//                case ProjectPropertyExtender.Item.TYPE_LIBRARY:
//                    if ( item.isBroken() ) {
//                        return NbBundle.getMessage( RubyClassPathUi.class, "LBL_MISSING_LIBRARY", getLibraryName( item ) );
//                    }
//                    else { 
//                        return item.getLibrary().getDisplayName();
//                    }
//                case ProjectPropertyExtender.Item.TYPE_CLASSPATH:
//                    String name = (String)WELL_KNOWN_PATHS_NAMES.get( ClassPathSupport.getAntPropertyName( item.getReference() ) );
//                    return name == null ? item.getReference() : name;
//                case ProjectPropertyExtender.Item.TYPE_ARTIFACT:
//                    if ( item.isBroken() ) {
//                        return NbBundle.getMessage( RubyClassPathUi.class, "LBL_MISSING_PROJECT", getProjectName( item ) );
//                    }
//                    else {
//                        Project p = item.getArtifact().getProject();
//                        ProjectInformation pi = ProjectUtils.getInformation(p);
//                        String projectName = pi.getDisplayName();
//                        return MessageFormat.format (NbBundle.getMessage(RubyClassPathUi.class,"MSG_ProjectArtifactFormat"), new Object[] {
//                            projectName,
//                            item.getArtifactURI().toString()
//                        });
//                    }
                case ProjectPropertyExtender.Item.TYPE_JAR:
                    if ( item.isBroken() ) {
                        return NbBundle.getMessage( RubyClassPathUi.class, "LBL_MISSING_FILE", getFileRefName( item ) );
                    }
                    else {
                        return item.getFile().getPath();
                    }
            }
            
            return item.getReference(); // XXX            
        }
        
        static Icon getIcon( ProjectPropertyExtender.Item item ) {
            
            switch ( item.getType() ) {
                
                case ProjectPropertyExtender.Item.TYPE_LIBRARY:
                    if ( item.isBroken() ) {
                        if ( ICON_BROKEN_LIBRARY == null ) {
                            ICON_BROKEN_LIBRARY = new ImageIcon( ImageUtilities.mergeImages( ICON_LIBRARY.getImage(), ICON_BROKEN_BADGE.getImage(), 7, 7 ) );
                        }
                        return ICON_BROKEN_LIBRARY;
                    }
                    else {
                        return ICON_LIBRARY;
                    }
//                case ProjectPropertyExtender.Item.TYPE_ARTIFACT:
//                    if ( item.isBroken() ) {
//                        if ( ICON_BROKEN_ARTIFACT == null ) {
//                            ICON_BROKEN_ARTIFACT = new ImageIcon( ImageUtilities.mergeImages( ICON_ARTIFACT.getImage(), ICON_BROKEN_BADGE.getImage(), 7, 7 ) );
//                        }
//                        return ICON_BROKEN_ARTIFACT;
//                    }
//                    else {
//                        Project p = item.getArtifact().getProject();
//                        if (p != null) {
//                            ProjectInformation pi = ProjectUtils.getInformation(p);
//                            return pi.getIcon();
//                        }
//                        return ICON_ARTIFACT;
//                    }
                case ProjectPropertyExtender.Item.TYPE_JAR:
                    if ( item.isBroken() ) {
                        if ( ICON_BROKEN_JAR == null ) {
                            ICON_BROKEN_JAR = new ImageIcon( ImageUtilities.mergeImages( ICON_JAR.getImage(), ICON_BROKEN_BADGE.getImage(), 7, 7 ) );
                        }
                        return ICON_BROKEN_JAR;
                    }
                    else {
                        File file = item.getFile();
                        return file.isDirectory() ? getFolderIcon() : ICON_JAR;
                    }
                case ProjectPropertyExtender.Item.TYPE_CLASSPATH:
                    return ICON_CLASSPATH;
                
            }
            
            return null; // XXX 
        }
        
        private String getToolTipText( ProjectPropertyExtender.Item item ) {
            if ( item.isBroken() && 
                 ( item.getType() == ProjectPropertyExtender.Item.TYPE_JAR || 
                   item.getType() == ProjectPropertyExtender.Item.TYPE_ARTIFACT )  ) {
                return evaluator.evaluate( item.getReference() );
            }
            
            return getDisplayName( item ); // XXX
        }
        
        private static ImageIcon getFolderIcon() {
        
            if ( ICON_FOLDER == null ) {
                DataFolder dataFolder = DataFolder.findFolder( FileUtil.getConfigRoot() );
                ICON_FOLDER = new ImageIcon( dataFolder.getNodeDelegate().getIcon( BeanInfo.ICON_COLOR_16x16 ) );            
            }

            return ICON_FOLDER;   
        }
        
        private String getProjectName( ProjectPropertyExtender.Item item ) {
            String ID = item.getReference();
            // something in the form of "${reference.project-name.id}"
            return ID.substring(12, ID.indexOf(".", 12)); // NOI18N
        }

        private String getLibraryName( ProjectPropertyExtender.Item item ) {
            String ID = item.getReference();
            // something in the form of "${libs.junit.classpath}"
            return ID.substring(7, ID.indexOf(".classpath")); // NOI18N
        }

        private String getFileRefName( ProjectPropertyExtender.Item item ) {
            String ID = item.getReference();        
            // something in the form of "${file.reference.smth.jar}"
            Matcher m = FOREIGN_PLAIN_FILE_REFERENCE.matcher(ID);
            if (m.matches()) {
                return m.group(1);
            }
            m = UNKNOWN_FILE_REFERENCE.matcher(ID);
            if (m.matches()) {
                return m.group(1);
            }
            return ID;
        }


        
    }
    
    
    public static class EditMediator implements ActionListener, ListSelectionListener {
                
        private final Project project;
        private final JList list;
        private final DefaultListModel listModel;
        private final ListSelectionModel selectionModel;
        private final ButtonModel addJar;
        private final ButtonModel addLibrary;
        private final ButtonModel addAntArtifact;
        private final ButtonModel remove;
        private final ButtonModel moveUp;
        private final ButtonModel moveDown;
                    
        public EditMediator( Project project,
                             JList list,
                             DefaultListModel listModel, 
                             ButtonModel addJar,
                             ButtonModel addLibrary, 
                             ButtonModel addAntArtifact,
                             ButtonModel remove, 
                             ButtonModel moveUp,
                             ButtonModel moveDown ) {
                             
            // Remember all buttons
            
            this.list = list;
            
            if ( !( list.getModel() instanceof DefaultListModel ) ) {
                throw new IllegalArgumentException( "The list's model has to be of class DefaultListModel" ); // NOI18N
            }
            
            this.listModel = (DefaultListModel)list.getModel();
            this.selectionModel = list.getSelectionModel();
            
            this.addJar = addJar;
            this.addLibrary = addLibrary;
            this.addAntArtifact = addAntArtifact;
            this.remove = remove;
            this.moveUp = moveUp;
            this.moveDown = moveDown;

            this.project = project;
        }

        public static void register(Project project,
                                    JList list,
                                    DefaultListModel listModel, 
                                    ButtonModel addJar,
                                    ButtonModel addLibrary, 
                                    ButtonModel addAntArtifact,
                                    ButtonModel remove, 
                                    ButtonModel moveUp,
                                    ButtonModel moveDown ) {    
            
            EditMediator em = new EditMediator( project, 
                                                list,
                                                listModel, 
                                                addJar,
                                                addLibrary, 
                                                addAntArtifact,
                                                remove,    
                                                moveUp,
                                                moveDown  );
            
            // Register the listener on all buttons
            addJar.addActionListener( em ); 
            addLibrary.addActionListener( em );
            addAntArtifact.addActionListener( em );
            remove.addActionListener( em );
            moveUp.addActionListener( em );
            moveDown.addActionListener( em );
            // On list selection
            em.selectionModel.addListSelectionListener( em );
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
                JFileChooser chooser = new JFileChooser();
                FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
                chooser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
                chooser.setMultiSelectionEnabled( true );
                chooser.setDialogTitle( NbBundle.getMessage( RubyClassPathUi.class, "LBL_AddJar_DialogTitle" ) ); // NOI18N
                //#61789 on old macosx (jdk 1.4.1) these two method need to be called in this order.
                chooser.setAcceptAllFileFilterUsed( false );
                chooser.setFileFilter( new SimpleFileFilter( 
                    NbBundle.getMessage( RubyClassPathUi.class, "LBL_ZipJarFolderFilter" ),                  // NOI18N
                    new String[] {"ZIP","JAR"} ) );                                                                 // NOI18N 
                File curDir = FoldersListSettings.getDefault().getLastUsedClassPathFolder(); 
                chooser.setCurrentDirectory (curDir);
                int option = chooser.showOpenDialog( SwingUtilities.getWindowAncestor( list ) ); // Sow the chooser
                
                if ( option == JFileChooser.APPROVE_OPTION ) {
                    
                    File files[] = chooser.getSelectedFiles();
                    int[] newSelection = ClassPathUiSupport.addJarFiles( listModel, list.getSelectedIndices(), files  );
                    list.setSelectedIndices( newSelection );
                    curDir = FileUtil.normalizeFile(chooser.getCurrentDirectory());
                    FoldersListSettings.getDefault().setLastUsedClassPathFolder(curDir);
                }
            }
//            else if ( source == addLibrary ) {
//                Set/*<Library>*/includedLibraries = new HashSet ();
//                for (int i=0; i< listModel.getSize(); i++) {
//                    ProjectPropertyExtender.Item item = (ProjectPropertyExtender.Item) listModel.get(i);
//                    if (item.getType() == ProjectPropertyExtender.Item.TYPE_LIBRARY && !item.isBroken() ) {
//                        includedLibraries.add( item.getLibrary() );
//                    }
//                }
//                Object[] options = new Object[] {
//                    new JButton (NbBundle.getMessage (RubyClassPathUi.class,"LBL_AddLibrary")),
//                    DialogDescriptor.CANCEL_OPTION
//                };
//                ((JButton)options[0]).setEnabled(false);
//                ((JButton)options[0]).getAccessibleContext().setAccessibleDescription (NbBundle.getMessage (RubyClassPathUi.class,"AD_AddLibrary"));
//                LibrariesChooser panel = new LibrariesChooser ((JButton)options[0], includedLibraries);
//                DialogDescriptor desc = new DialogDescriptor(panel,NbBundle.getMessage( RubyClassPathUi.class, "LBL_CustomizeCompile_Classpath_AddLibrary" ),
//                    true, options, options[0], DialogDescriptor.DEFAULT_ALIGN,null,null);
//                Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
//                dlg.setVisible(true);
//                if (desc.getValue() == options[0]) {
//                   int[] newSelection = ClassPathUiSupport.addLibraries( listModel, list.getSelectedIndices(), panel.getSelectedLibraries(), includedLibraries );
//                   list.setSelectedIndices( newSelection );
//                }
//                dlg.dispose();
////            }
////            else if ( source == addAntArtifact ) { 
////                AntArtifactChooser.ArtifactItem artifactItems[] = AntArtifactChooser.showDialog(
////                        new String[] { JavaProjectConstants.ARTIFACT_TYPE_JAR, JavaProjectConstants.ARTIFACT_TYPE_FOLDER},
////                        project, list.getParent() );
////                if (artifactItems != null) {
////                    int[] newSelection = ClassPathUiSupport.addArtifacts( listModel, list.getSelectedIndices(), artifactItems);
////                    list.setSelectedIndices( newSelection );
////                }
//            }
            else if ( source == remove ) { 
                int[] newSelection = ClassPathUiSupport.remove( listModel, list.getSelectedIndices() );
                list.setSelectedIndices( newSelection );
            }
            else if ( source == moveUp ) {
                int[] newSelection = ClassPathUiSupport.moveUp( listModel, list.getSelectedIndices() );
                list.setSelectedIndices( newSelection );
            }
            else if ( source == moveDown ) {
                int[] newSelection = ClassPathUiSupport.moveDown( listModel, list.getSelectedIndices() );
                list.setSelectedIndices( newSelection );
            }
        }    
        
        
        /** Handles changes in the selection
         */        
        public void valueChanged( ListSelectionEvent e ) {
            
            // remove enabled only if selection is not empty
            boolean canRemove = false;
            // and when the selection does not contain unremovable item
            if ( selectionModel.getMinSelectionIndex() != -1 ) {
                canRemove = true;
                int iMin = selectionModel.getMinSelectionIndex();
                int iMax = selectionModel.getMinSelectionIndex();
                for ( int i = iMin; i <= iMax; i++ ) {
                    
                    if ( selectionModel.isSelectedIndex( i ) ) {
                        ProjectPropertyExtender.Item item = (ProjectPropertyExtender.Item)listModel.get( i );
                        if ( item.getType() == ProjectPropertyExtender.Item.TYPE_CLASSPATH ) {
                            canRemove = false;
                            break;
                        }
                    }
                }
            }
            
            // addJar allways enabled            
            // addLibrary allways enabled            
            // addArtifact allways enabled            
            // editButton.setEnabled( edit );            
            remove.setEnabled( canRemove );
            moveUp.setEnabled( ClassPathUiSupport.canMoveUp( selectionModel ) );
            moveDown.setEnabled( ClassPathUiSupport.canMoveDown( selectionModel, listModel.getSize() ) );       
            
        }
    }
    
    private static class SimpleFileFilter extends FileFilter {

        private String description;
        private Collection extensions;


        public SimpleFileFilter (String description, String[] extensions) {
            this.description = description;
            this.extensions = Arrays.asList(extensions);
        }

        public boolean accept(File f) {
            if (f.isDirectory())
                return true;
            String name = f.getName();
            int index = name.lastIndexOf('.');   //NOI18N
            if (index <= 0 || index==name.length()-1)
                return false;
            String extension = name.substring (index+1).toUpperCase();
            return this.extensions.contains(extension);
        }

        public String getDescription() {
            return this.description;
        }
    }
        
}

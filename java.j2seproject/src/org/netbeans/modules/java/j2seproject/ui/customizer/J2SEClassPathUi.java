/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seproject.ui.customizer;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.ButtonModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.java.j2seproject.classpath.ClassPathSupport;
import org.netbeans.modules.java.j2seproject.ui.FoldersListSettings;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** Classes containing code speciic for handling UI of J2SE project classpath 
 *
 * @author Petr Hrebejk
 */
public class J2SEClassPathUi {
    
    private J2SEClassPathUi() {}
           
    // Innerclasses ------------------------------------------------------------
            
    /** Renderer which can be used to render the classpath in lists
     */    
    public static class ClassPathListCellRenderer extends DefaultListCellRenderer {
        
        private static String RESOURCE_ICON_JAR = "org/netbeans/modules/java/j2seproject/ui/resources/jar.gif"; //NOI18N
        private static String RESOURCE_ICON_LIBRARY = "org/netbeans/modules/java/j2seproject/ui/resources/libraries.gif"; //NOI18N
        private static String RESOURCE_ICON_ARTIFACT = "org/netbeans/modules/java/j2seproject/ui/resources/projectDependencies.gif"; //NOI18N
        private static String RESOURCE_ICON_CLASSPATH = "org/netbeans/modules/java/j2seproject/ui/resources/referencedClasspath.gif"; //NOI18N

        private static Icon ICON_JAR = new ImageIcon( Utilities.loadImage( RESOURCE_ICON_JAR ) );
        private static Icon ICON_FOLDER = null; 
        private static Icon ICON_LIBRARY = new ImageIcon( Utilities.loadImage( RESOURCE_ICON_LIBRARY ) );
        private static Icon ICON_ARTIFACT  = new ImageIcon( Utilities.loadImage( RESOURCE_ICON_ARTIFACT ) );
        private static Icon ICON_CLASSPATH  = new ImageIcon( Utilities.loadImage( RESOURCE_ICON_CLASSPATH ) );

                
        // Contains well known paths in the J2SEProject
        private static final Map WELL_KNOWN_PATHS_NAMES = new HashMap();
        static {
            WELL_KNOWN_PATHS_NAMES.put( J2SEProjectProperties.JAVAC_CLASSPATH, NbBundle.getMessage( J2SEProjectProperties.class, "LBL_JavacClasspath_DisplayName" ) );
            WELL_KNOWN_PATHS_NAMES.put( J2SEProjectProperties.JAVAC_TEST_CLASSPATH, NbBundle.getMessage( J2SEProjectProperties.class,"LBL_JavacTestClasspath_DisplayName") );
            WELL_KNOWN_PATHS_NAMES.put( J2SEProjectProperties.RUN_CLASSPATH, NbBundle.getMessage( J2SEProjectProperties.class, "LBL_RunClasspath_DisplayName" ) );
            WELL_KNOWN_PATHS_NAMES.put( J2SEProjectProperties.RUN_TEST_CLASSPATH, NbBundle.getMessage( J2SEProjectProperties.class, "LBL_RunTestClasspath_DisplayName" ) );
            WELL_KNOWN_PATHS_NAMES.put( J2SEProjectProperties.BUILD_CLASSES_DIR, NbBundle.getMessage( J2SEProjectProperties.class, "LBL_BuildClassesDir_DisplayName" ) );            
            WELL_KNOWN_PATHS_NAMES.put( J2SEProjectProperties.BUILD_TEST_CLASSES_DIR, NbBundle.getMessage (J2SEProjectProperties.class,"LBL_BuildTestClassesDir_DisplayName") );
        };
                       
        public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            
            ClassPathSupport.Item item = (ClassPathSupport.Item)value;
            
            super.getListCellRendererComponent( list, getDisplayName( item ), index, isSelected, cellHasFocus );
            setIcon( getIcon( item ) );
            setToolTipText( getToolTipText( item ) );
            
            return this;
        }
        
        
        static String getDisplayName( ClassPathSupport.Item item ) {
            
            switch ( item.getType() ) {
                
                case ClassPathSupport.Item.TYPE_LIBRARY:
                    return item.getLibrary().getDisplayName();
                    // return NbBundle.getMessage(VisualClassPathItem.class, "LBL_MISSING_LIBRARY", getLibraryName(getRaw()));
                case ClassPathSupport.Item.TYPE_CLASSPATH:
                    String name = (String)WELL_KNOWN_PATHS_NAMES.get( J2SEProjectProperties.getAntPropertyName( item.getReference() ) );
                    return name == null ? item.getReference() : name;
                case ClassPathSupport.Item.TYPE_ARTIFACT:
                    return item.getArtifactURI().toString();
                    // return NbBundle.getMessage(VisualClassPathItem.class, "LBL_MISSING_PROJECT", getProjectName(getEvaluated()));
                case ClassPathSupport.Item.TYPE_JAR:
                    return item.getFile().getPath();
                    // return NbBundle.getMessage(VisualClassPathItem.class, "LBL_MISSING_FILE", getFileRefName(getEvaluated()));
            }
            
            return item.getReference(); // XXX            
        }
        
        static Icon getIcon( ClassPathSupport.Item item ) {
            
            switch ( item.getType() ) {
                
                case ClassPathSupport.Item.TYPE_LIBRARY:
                    return ICON_LIBRARY;
                case ClassPathSupport.Item.TYPE_ARTIFACT:
                    return ICON_ARTIFACT;
                case ClassPathSupport.Item.TYPE_JAR:
                    File file = item.getFile();
                    if ( file.isDirectory() ) {
                        return getFolderIcon();
                    }
                    else {
                        return ICON_JAR;
                    }
                case ClassPathSupport.Item.TYPE_CLASSPATH:
                    return ICON_CLASSPATH;
                
            }
            
            return null; // XXX 
        }
        
        static String getToolTipText( ClassPathSupport.Item item ) {
            return getDisplayName( item ); // XXX
        }
        
        private static Icon getFolderIcon() {
        
            if ( ICON_FOLDER == null ) {
                FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
                DataFolder dataFolder = DataFolder.findFolder( root );
                ICON_FOLDER = new ImageIcon( dataFolder.getNodeDelegate().getIcon( BeanInfo.ICON_COLOR_16x16 ) );            
            }

            return ICON_FOLDER;   
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
                chooser.setDialogTitle( NbBundle.getMessage( J2SEClassPathUi.class, "LBL_AddJar_DialogTitle" ) ); // NOI18N
                chooser.setFileFilter( new SimpleFileFilter( 
                    NbBundle.getMessage( J2SEClassPathUi.class, "LBL_ZipJarFolderFilter" ),                  // NOI18N
                    new String[] {"ZIP","JAR"} ) );                                                                 // NOI18N 
                chooser.setAcceptAllFileFilterUsed( false );
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
            else if ( source == addLibrary ) {
                Set/*<Library>*/includedLibraries = new HashSet ();
                for (int i=0; i< listModel.getSize(); i++) {
                    ClassPathSupport.Item item = (ClassPathSupport.Item) listModel.get(i);
                    if (item.getType() == ClassPathSupport.Item.TYPE_LIBRARY) {
                        includedLibraries.add( item.getLibrary() );
                    }
                }
                Object[] options = new Object[] {
                    new JButton (NbBundle.getMessage (J2SEClassPathUi.class,"LBL_AddLibrary")),
                    DialogDescriptor.CANCEL_OPTION
                };
                ((JButton)options[0]).setEnabled(false);
                ((JButton)options[0]).getAccessibleContext().setAccessibleDescription (NbBundle.getMessage (J2SEClassPathUi.class,"AD_AddLibrary"));
                LibrariesChooser panel = new LibrariesChooser ((JButton)options[0], includedLibraries);
                DialogDescriptor desc = new DialogDescriptor(panel,NbBundle.getMessage( J2SEClassPathUi.class, "LBL_CustomizeCompile_Classpath_AddLibrary" ),
                    true, options, options[0], DialogDescriptor.DEFAULT_ALIGN,null,null);
                Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
                dlg.setVisible(true);
                if (desc.getValue() == options[0]) {
                   int[] newSelection = ClassPathUiSupport.addLibraries( listModel, list.getSelectedIndices(), panel.getSelectedLibraries(), includedLibraries );
                   list.setSelectedIndices( newSelection );
                }
                dlg.dispose();
            }
            else if ( source == addAntArtifact ) { 
                AntArtifactChooser.ArtifactItem artifactItems[] = AntArtifactChooser.showDialog(JavaProjectConstants.ARTIFACT_TYPE_JAR, project);
                if (artifactItems != null) {
                    int[] newSelection = ClassPathUiSupport.addArtifacts( listModel, list.getSelectedIndices(), artifactItems);
                    list.setSelectedIndices( newSelection );
                }
            }
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
                        ClassPathSupport.Item item = (ClassPathSupport.Item)listModel.get( i );
                        if ( item.getType() == ClassPathSupport.Item.TYPE_CLASSPATH ) {
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

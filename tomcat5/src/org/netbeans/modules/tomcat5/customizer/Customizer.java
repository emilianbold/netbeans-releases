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
package org.netbeans.modules.tomcat5.customizer;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.tomcat5.util.TomcatProperties;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.J2eePlatformImpl;
import org.netbeans.modules.tomcat5.TomcatManager;
import org.openide.NotifyDescriptor;


/**
 * Tomcat instance customizer which is accessible from server manager.
 *
 * @author Stepan Herold
 */
public class Customizer extends JTabbedPane {

    private static final String CLASSPATH = J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH;
    private static final String SOURCES = J2eeLibraryTypeProvider.VOLUME_TYPE_SRC;
    private static final String JAVADOC = J2eeLibraryTypeProvider.VOLUME_TYPE_JAVADOC;

    private TomcatManager manager;
    private J2eePlatformImpl platform;

    public Customizer(TomcatManager aManager) {
        manager = aManager;
        platform = manager.getTomcatPlatform();
        initComponents ();
    }

    private void initComponents() {
        getAccessibleContext().setAccessibleName (NbBundle.getMessage(Customizer.class,"ACS_Customizer")); // NOI18N
        getAccessibleContext().setAccessibleDescription (NbBundle.getMessage(Customizer.class,"ACS_Customizer")); // NOI18N
        CustomizerDataSupport custData = new CustomizerDataSupport(manager);
        addTab(NbBundle.getMessage(Customizer.class,"TXT_General"), new CustomizerGeneral(custData));
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Startup"), new CustomizerStartup(custData));
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Platform"), new CustomizerJVM(custData));
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Classes"), 
               new PathView(custData, custData.getClassModel(), CLASSPATH));
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Sources"), 
               new PathView(custData, custData.getSourceModel(), SOURCES));
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Javadoc"), 
               new PathView(custData, custData.getJavadocsModel(), JAVADOC));
    }

    private static class PathView extends JPanel {
        
        private JList resources;
        private JButton addButton;
        private JButton addURLButton;
        private JButton removeButton;
        private JButton moveUpButton;
        private JButton moveDownButton;
        private File currentDir;
        private String type;
        private CustomizerDataSupport custData;

        public PathView (CustomizerDataSupport custData, PathModel model, String aType) {
            type = aType;
            this.custData = custData;
            initComponents(model);
        }

        private void initComponents(PathModel model) {
            setLayout(new GridBagLayout());
            JLabel label = new JLabel ();
            String key = null;
            String mneKey = null;
            String ad = null;
            if (type.equals(CLASSPATH)) {
                key = "TXT_Classes";       // NOI18N
                mneKey = "MNE_Classes";    // NOI18N
                ad = "AD_Classes";       // NOI18N                
            } else if (type.equals(SOURCES)) {
                key = "TXT_Sources";        // NOI18N
                mneKey = "MNE_Sources";     // NOI18N
                ad = "AD_Sources";          // NOI18N
            } else if (type.equals(JAVADOC)) {
                key = "TXT_Javadoc";        // NOI18N
                mneKey = "MNE_Javadoc";     // NOI18N
                ad = "AD_Javadoc";          // NOI18N                
            } else {
                assert false : "Illegal type of panel"; //NOI18N
                return;
            }
            label.setText(NbBundle.getMessage(Customizer.class,key));
            label.setDisplayedMnemonic(NbBundle.getMessage(Customizer.class,mneKey).charAt(0));
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = GridBagConstraints.RELATIVE;
            c.gridy = GridBagConstraints.RELATIVE;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.insets = new Insets (6,12,2,0);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            ((GridBagLayout)getLayout()).setConstraints(label,c);
            add(label);
            resources = new JList(model);
            label.setLabelFor(resources);
            resources.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(Customizer.class,ad));
            resources.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    selectionChanged ();
                }
            });
            JScrollPane spane = new JScrollPane (this.resources);            
            c = new GridBagConstraints();
            c.gridx = GridBagConstraints.RELATIVE;
            c.gridy = GridBagConstraints.RELATIVE;
            c.gridwidth = 1;
            c.gridheight = 5;
            c.insets = new Insets (0,12,12,6);
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            c.weighty = 1.0;
            ((GridBagLayout)this.getLayout()).setConstraints(spane,c);
            add(spane);
            if (type == SOURCES || type == JAVADOC) {
                this.addButton = new JButton ();
                String text;
                char mne;
                if (type == SOURCES) {
                    text = NbBundle.getMessage(Customizer.class, "CTL_Add");
                    mne = NbBundle.getMessage(Customizer.class, "MNE_Add").charAt(0);
                    ad = NbBundle.getMessage(Customizer.class, "AD_Add");
                }
                else {
                    text = NbBundle.getMessage(Customizer.class, "CTL_AddZip");
                    mne = NbBundle.getMessage(Customizer.class, "MNE_AddZip").charAt(0);
                    ad = NbBundle.getMessage(Customizer.class, "AD_AddZip");
                }
                this.addButton.setText(text);
                this.addButton.setMnemonic(mne);
                this.addButton.getAccessibleContext().setAccessibleDescription (ad);
                addButton.addActionListener( new ActionListener () {
                    public void actionPerformed(ActionEvent e) {
                        addPathElement ();
                    }
                });
                addButton.addFocusListener(custData.getSaveOnFocusLostListener());
                c = new GridBagConstraints();
                c.gridx = 1;
                c.gridy = 1;
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.anchor = GridBagConstraints.NORTHWEST;
                c.insets = new Insets (0,6,0,6);
                ((GridBagLayout)this.getLayout()).setConstraints(addButton,c);
                this.add (addButton);
//                if (this.type == JAVADOC) {
//                    addURLButton  = new JButton (NbBundle.getMessage(Customizer.class, "CTL_AddURL"));
//                    addURLButton.setMnemonic(NbBundle.getMessage(Customizer.class, "MNE_AddURL").charAt(0));
//                    addURLButton.addActionListener(new ActionListener () {
//                        public void actionPerformed(ActionEvent e) {
//                            addURLElement ();
//                        }
//                    });
//                    c = new GridBagConstraints();
//                    c.gridx = 1;
//                    c.gridy = 2;
//                    c.gridwidth = GridBagConstraints.REMAINDER;
//                    c.fill = GridBagConstraints.HORIZONTAL;
//                    c.anchor = GridBagConstraints.NORTHWEST;
//                    c.insets = new Insets (0,6,6,12);
//                    ((GridBagLayout)this.getLayout()).setConstraints(addURLButton,c);
//                    this.add (addURLButton);
//                }
                removeButton = new JButton (NbBundle.getMessage(Customizer.class, "CTL_Remove"));
                removeButton.setMnemonic(NbBundle.getMessage(Customizer.class, "MNE_Remove").charAt(0));
                removeButton.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage(Customizer.class,"AD_Remove"));
                removeButton.addActionListener( new ActionListener () {
                    public void actionPerformed(ActionEvent e) {
                        removePathElement ();
                    }
                });
                removeButton.setEnabled(false);
                removeButton.addFocusListener(custData.getSaveOnFocusLostListener());
                c = new GridBagConstraints();
                c.gridx = 1;
                c.gridy = 3;
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.anchor = GridBagConstraints.NORTHWEST;
                c.insets = new Insets (12,6,0,6);
                ((GridBagLayout)this.getLayout()).setConstraints(removeButton,c);
                this.add (removeButton);
                moveUpButton = new JButton (NbBundle.getMessage(Customizer.class, "CTL_Up"));
                moveUpButton.setMnemonic(NbBundle.getMessage(Customizer.class, "MNE_Up").charAt(0));
                moveUpButton.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage(Customizer.class,"AD_Up"));
                moveUpButton.addActionListener( new ActionListener () {
                    public void actionPerformed(ActionEvent e) {
                        moveUpPathElement ();
                    }
                });
                moveUpButton.addFocusListener(custData.getSaveOnFocusLostListener());
                moveUpButton.setEnabled(false);
                c = new GridBagConstraints();
                c.gridx = 1;
                c.gridy = 4;
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.anchor = GridBagConstraints.NORTHWEST;
                c.insets = new Insets (12,6,0,6);
                ((GridBagLayout)this.getLayout()).setConstraints(moveUpButton,c);
                this.add (moveUpButton);
                moveDownButton = new JButton (NbBundle.getMessage(Customizer.class, "CTL_Down"));
                moveDownButton.setMnemonic (NbBundle.getMessage(Customizer.class, "MNE_Down").charAt(0));
                moveDownButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(Customizer.class,"AD_Down"));
                moveDownButton.addActionListener( new ActionListener () {
                    public void actionPerformed(ActionEvent e) {
                        moveDownPathElement ();
                    }
                });
                moveDownButton.addFocusListener(custData.getSaveOnFocusLostListener());
                moveDownButton.setEnabled(false);
                c = new GridBagConstraints();
                c.gridx = 1;
                c.gridy = 5;
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.anchor = GridBagConstraints.NORTHWEST;
                c.insets = new Insets (5,6,6,6);
                ((GridBagLayout)this.getLayout()).setConstraints(moveDownButton,c);
                this.add (moveDownButton);
            }
        }
        
//        private void addURLElement() {
//            JPanel p = new JPanel ();
//            GridBagLayout lm = new GridBagLayout();
//            p.setLayout (lm);
//            GridBagConstraints c = new GridBagConstraints ();
//            c.gridx = c.gridy = GridBagConstraints.RELATIVE;
//            c.insets = new Insets (12,12,12,6);
//            c.anchor = GridBagConstraints.NORTHWEST;
//            JLabel label = new JLabel (NbBundle.getMessage(Customizer.class,"CTL_AddJavadocURLMessage"));
//            label.setDisplayedMnemonic ('U');
//            lm.setConstraints(label,c);
//            p.add (label);
//            c = new GridBagConstraints ();
//            c.gridx = c.gridy = GridBagConstraints.RELATIVE;
//            c.gridwidth = GridBagConstraints.REMAINDER;
//            c.insets = new Insets (12,0,12,6);
//            c.fill = GridBagConstraints.HORIZONTAL;
//            c.anchor = GridBagConstraints.NORTHWEST;
//            JTextField text = new JTextField ();
//            text.setColumns(30);
//            text.setText (NbBundle.getMessage(Customizer.class,"TXT_DefaultProtocol"));
//            text.selectAll();
//            label.setLabelFor(text);
//            lm.setConstraints(text,c);
//            p.add (text);            
//            JButton[] options = new JButton[] {
//                new JButton (NbBundle.getMessage(Customizer.class,"CTL_AddJavadocURLTitle")),
//                new JButton (NbBundle.getMessage(Customizer.class,"CTL_Cancel"))
//            };
//            options[0].setMnemonic(NbBundle.getMessage(Customizer.class,"MNE_Add").charAt(0));
//            options[1].setMnemonic(NbBundle.getMessage(Customizer.class,"MNE_Cancel").charAt(0));
//            DialogDescriptor input = new DialogDescriptor (
//                p,
//                NbBundle.getMessage(Customizer.class,"CTL_AddJavadocURLTitle"),
//                true, options, options[0], DialogDescriptor.DEFAULT_ALIGN, null, null);            
//            if (DialogDisplayer.getDefault().notify(input) == options[0]) {
//                try {
//                    String value = text.getText();
//                    URL url = new URL (value);
//                    ((PathModel)this.resources.getModel()).addPath(url);
//                    this.resources.setSelectedIndex (this.resources.getModel().getSize()-1);
//                } catch (MalformedURLException mue) {
//                    DialogDescriptor.Message message = new DialogDescriptor.Message (
//                        NbBundle.getMessage(Customizer.class,"CTL_InvalidURLFormat"),
//                        DialogDescriptor.ERROR_MESSAGE);
//                    DialogDisplayer.getDefault().notify(message);
//                }
//            }
//        }

        private void addPathElement () {
            JFileChooser chooser = new JFileChooser ();
            FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
            chooser.setMultiSelectionEnabled (true);
            String title = null;
            String message = null;
            String approveButtonName = null;
            String approveButtonNameMne = null;
            if (this.type == SOURCES) {
                title = NbBundle.getMessage (Customizer.class,"TXT_OpenSources");
                message = NbBundle.getMessage (Customizer.class,"TXT_Sources");
                approveButtonName = NbBundle.getMessage (Customizer.class,"TXT_OpenSources");
                approveButtonNameMne = NbBundle.getMessage (Customizer.class,"MNE_OpenSources");
            }
            else if (this.type == JAVADOC) {
                title = NbBundle.getMessage (Customizer.class,"TXT_OpenJavadoc");
                message = NbBundle.getMessage (Customizer.class,"TXT_Javadoc");
                approveButtonName = NbBundle.getMessage (Customizer.class,"TXT_OpenJavadoc");
                approveButtonNameMne = NbBundle.getMessage (Customizer.class,"MNE_OpenJavadoc");
            }
            chooser.setDialogTitle(title);
            chooser.setApproveButtonText(approveButtonName);
            chooser.setApproveButtonMnemonic (approveButtonNameMne.charAt(0));
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setFileFilter (new SimpleFileFilter(message,new String[] {"ZIP","JAR"}));   //NOI18N
            chooser.setAcceptAllFileFilterUsed(false);
            if (this.currentDir != null) {
                chooser.setCurrentDirectory(this.currentDir);
            }
            if (chooser.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) {
                File[] fs = chooser.getSelectedFiles();
                PathModel model = (PathModel) this.resources.getModel();
                boolean addingFailed = false;
                int firstIndex = this.resources.getModel().getSize();
                for (int i = 0; i < fs.length; i++) {
                    File f = fs[i];
                    //XXX: JFileChooser workaround (JDK bug #5075580), double click on folder returns wrong file
                    // E.g. for /foo/src it returns /foo/src/src
                    // Try to convert it back by removing last invalid name component
                    if (!f.exists()) {
                        File parent = f.getParentFile();
                        if (parent != null && f.getName().equals(parent.getName()) && parent.exists()) {
                            f = parent;
                        }
                    }
                    addingFailed|=!model.addPath (f);
                }
                if (addingFailed) {
                    new NotifyDescriptor.Message (NbBundle.getMessage(Customizer.class,"TXT_CanNotAddResolve"),
                            NotifyDescriptor.ERROR_MESSAGE);
                }
                int lastIndex = this.resources.getModel().getSize()-1;
                if (firstIndex<=lastIndex) {
                    int[] toSelect = new int[lastIndex-firstIndex+1];
                    for (int i = 0; i < toSelect.length; i++) {
                        toSelect[i] = firstIndex+i;
                    }
                    this.resources.setSelectedIndices(toSelect);
                }
                this.currentDir = FileUtil.normalizeFile(chooser.getCurrentDirectory());
            }
        }

        private void removePathElement () {
            int[] indices = this.resources.getSelectedIndices();
            if (indices.length == 0) {
                return;
            }
            PathModel model = (PathModel) this.resources.getModel();
            model.removePath (indices);
            if ( indices[indices.length-1]-indices.length+1 < this.resources.getModel().getSize()) {
                this.resources.setSelectedIndex (indices[indices.length-1]-indices.length+1);
            }
            else if (indices[0]>0) {
                this.resources.setSelectedIndex (indices[0]-1);
            }
        }

        private void moveDownPathElement () {
            int[] indices = this.resources.getSelectedIndices();
            if (indices.length == 0) {
                return;
            }
            PathModel model = (PathModel) this.resources.getModel();
            model.moveDownPath (indices);
            for (int i=0; i< indices.length; i++) {
                indices[i] = indices[i] + 1;
            }
            this.resources.setSelectedIndices (indices);
        }

        private void moveUpPathElement () {
            int[] indices = this.resources.getSelectedIndices();
            if (indices.length == 0) {
                return;
            }
            PathModel model = (PathModel) this.resources.getModel();
            model.moveUpPath (indices);
            for (int i=0; i< indices.length; i++) {
                indices[i] = indices[i] - 1;
            }
            this.resources.setSelectedIndices (indices);
        }

        private void selectionChanged () {
            if (this.type == CLASSPATH) {
                return;
            }
            int indices[] = this.resources.getSelectedIndices();
            this.removeButton.setEnabled (indices.length > 0);
            this.moveUpButton.setEnabled (indices.length > 0 && indices[0]>0);
            this.moveDownButton.setEnabled(indices.length > 0 && indices[indices.length-1]<this.resources.getModel().getSize()-1);
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

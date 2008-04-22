/*
 * AddJavaScriptLibraryToProjectPanel.java
 *
 * Created on April 1, 2008, 4:42 PM
 */
package org.netbeans.modules.javascript.libraries.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author  ctnguyen
 */
public class AddJavaScriptLibraryToProjectPanel extends javax.swing.JPanel {

    private static final String WEB_PROJECT = "org.netbeans.modules.web.project.WebProject";
    private static final String WEB_PROJECT_DEFAULT_RELATIVE_PATH = "/web/resources";
    private static final String RUBY_PROJECT = "org.netbeans.modules.ruby.rubyproject.RubyProject";
    private static final String RUBY_PROJECT_DEFAULT_RELATIVE_PATH = "/public/resources";
    private static final String PHP_PROJECT = "org.netbeans.modules.php.project";
    private static final String OTHER_PROJECT_DEFAULT_RELATIVE_PATH = "/javascript/resources";
    private JDialog dialog;
    private DialogDescriptor dlg = null;
    private String okString = NbBundle.getMessage(AddJavaScriptLibraryToProjectPanel.class, "LBL_OK");
    private String cancelString = NbBundle.getMessage(AddJavaScriptLibraryToProjectPanel.class, "LBL_CANCEL");
    private JButton okButton = new JButton(okString);
    private JButton cancelButton = new JButton(cancelString);

    /** Creates new form AddJavaScriptLibraryToProjectPanel */
    public AddJavaScriptLibraryToProjectPanel(java.awt.Frame parent, boolean modal) {

        //super(parent, modal);
        initComponents();

        // Get a list of JavaScript library objects to display
        LibraryManager manager = LibraryManager.getDefault();
        List<Library> javascriptLibraries = new ArrayList<Library>();
        for (Library lib : manager.getLibraries()) {
            if (lib.getType().equals("javascript")) {
                javascriptLibraries.add(lib);
            }
        }
        //jList1.setCellRenderer(new MyCellRenderer());
        jList1.setListData(javascriptLibraries.toArray(new Library[]{}));

        // Get a list of open projects
        if (OpenProjects.getDefault() != null) {
            Project[] projects = OpenProjects.getDefault().getOpenProjects();


            // Get project name? : Lookup l = project[i].getLookup()

            // FileObject fo = projects[i].getProjectDirectory();
            // project type?
            if (projects.length > 0) {

                ProjectObject[] projs = new ProjectObject[projects.length];
                for (int i = 0; i < projects.length; i++) {
                    projs[i] = new ProjectObject(projects[i]);
                // System.out.println(projs[i].toString());

                }

                projectComboBox.setModel(new DefaultComboBoxModel(projs));
                // projectComboBox.setRenderer(new MyCellRenderer());

                // Set the default location
                setDestinationLocation();

            } else {
                // Tell user to open project first.
                okButton.setEnabled(false);
                String[] stringArray = new String[1];
                stringArray[0] = new String(NbBundle.getMessage(AddJavaScriptLibraryToProjectPanel.class, "NoProjectOpenTxt"));
                projectComboBox.setModel(new DefaultComboBoxModel(stringArray));
            }
        }
    }

    /**
     * Show the dialog using dialog displayer
     */
    public void showDialog() {

        // Add a listener to the dialog's buttons
        ActionListener listener = new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                Object o = evt.getSource();
                Object[] option = dlg.getOptions();

                if (o == option[0]) {
                    // Dismiss the dialog
                    dialog.hide();
                } else if (o == option[1]) {
                    // Don't do anything
                }
            }
        };
        dlg = new DialogDescriptor(this, NbBundle.getMessage(AddJavaScriptLibraryToProjectPanel.class, "SELECT_LIBRARY_DIALOG_TITLE"), true, listener);
        dlg.setOptions(new Object[]{okButton, cancelButton});
        dlg.setClosingOptions(new Object[]{cancelButton});

        okButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                try {
                    okButtonActionPerformed(evt);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });

        dialog = (JDialog) DialogDisplayer.getDefault().createDialog(dlg);
        dialog.setResizable(true);
        dialog.pack();
        dialog.show();
    }

    // Set the destination location
    private void setDestinationLocation() {
        Project currentProj = ((ProjectObject) projectComboBox.getSelectedItem()).getProject();

        if (currentProj.toString().startsWith(PHP_PROJECT)) {
            locationText.setText(WEB_PROJECT_DEFAULT_RELATIVE_PATH);
        }

        Project p = currentProj.getLookup().lookup(Project.class);
        if (p != null) {
            if (p.getClass().getName().equals(WEB_PROJECT)) {
                // Set the default relative path for the corresponding project

                locationText.setText(WEB_PROJECT_DEFAULT_RELATIVE_PATH);
            } else if (p.getClass().getName().equals(RUBY_PROJECT)) {
                locationText.setText(RUBY_PROJECT_DEFAULT_RELATIVE_PATH);

            } else {
                locationText.setText(OTHER_PROJECT_DEFAULT_RELATIVE_PATH);
            }

        }
    }

    // Validate the project relative location before extracting the library
    private boolean validateProjectFolder(Project project, Library library, String relativePath) throws IOException {

        relativePath = relativePath + File.separator + library.getName().replaceAll(" ", "_");
        FileObject fo = project.getProjectDirectory().getFileObject(relativePath);
        // File jsDir = new File(FileUtil.toFile(project.getProjectDirectory()), relativePath);

        // System.out.println("Testing: relativePath = " + relativePath);

        if (fo != null && fo.getChildren().length > 0 && overwriteCheckBox.isSelected() == false) {

            overwriteCheckBox.setText("override? Directory already exists and not empty.");
            return false;
        }
        return true;
    }

    // Get the library
    private boolean extractLibrary(Project project, Library library, String relativePath) {
        try {
            relativePath = relativePath + File.separator + library.getName().replaceAll(" ", "_");
            File jsDir = new File(FileUtil.toFile(project.getProjectDirectory()), relativePath);


            // Check if the folder already exists before creating
            FileObject jsFolder = FileUtil.createFolder(jsDir);
            // Now do the actual extract
            // System.out.println("Now do the actual extract");
            for (URL url : library.getContent("scriptpath")) {
                url = FileUtil.getArchiveFile(url);
                FileObject fo = URLMapper.findFileObject(url);
                extractZip(FileUtil.toFile(jsFolder), FileUtil.toFile(fo).toURL());
            }
        } catch (IOException ioe) {
            // display error here
            ErrorManager.getDefault().notify(ioe);
            // System.out.println("Error: Cannot create folder");
            return false;
        }

        return true;
    }

    private void extractJar() {
    }

    private void extractZip(File outDir, URL zipUrl) throws IOException {

        // Open the ZIP file
        InputStream source = zipUrl.openStream();
        try {
            ZipInputStream zipInputStream = new ZipInputStream(source);
            BufferedReader reader = new BufferedReader(new InputStreamReader(zipInputStream));
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                String entryName = zipEntry.getName();
                if (zipEntry.isDirectory()) {
                    File newFolder = new File(outDir, entryName);
                    newFolder.mkdirs();
                } else {

                    File file = new File(outDir, entryName);
                    FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
                    FileLock lock = channel.lock();
                    OutputStream out = Channels.newOutputStream(channel);
                    try {
                        final byte[] buffer = new byte[4096];
                        int len;
                        for (;;) {
                            len = ((InputStream) zipInputStream).read(buffer);

                            if (len == -1) {
                                break;
                            }
                            out.write(buffer, 0, len);
                        }
                    } finally {
                        lock.release();
                        out.close();
                    }
                }
            }
        } finally {
            source.close();
        }

    }

    class MyCellRenderer extends JLabel implements ListCellRenderer {


        // This is the only method defined by ListCellRenderer.
        // We just reconfigure the JLabel each time we're called.
        public Component getListCellRendererComponent(
                JList list,
                Object value, // value to display
                int index, // cell index
                boolean isSelected, // is the cell selected
                boolean cellHasFocus) // the list and the cell have the focus
        {
            Project proj = (Project) value;
            setText(proj.toString());

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setOpaque(true);
            return this;
        }
    }

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) throws IOException {
        this.setVisible(false);
        Object[] selValues = jList1.getSelectedValues();
        Project project = ((ProjectObject) projectComboBox.getSelectedItem()).getProject();
        for (Object lib : selValues) {
            if (locationText.getText() != null && project != null && lib != null && validateProjectFolder(project, (Library) lib, locationText.getText())) {
                extractLibrary(project, (Library) lib, locationText.getText());
            // dialog.dispose();
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new JCheckBoxList();
        jLabel1 = new javax.swing.JLabel();
        projectComboBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        locationText = new javax.swing.JTextField();
        overwriteCheckBox = new javax.swing.JCheckBox();

        setLayout(new java.awt.BorderLayout());

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList1ValueChanged(evt);
            }
        });
        jList1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jList1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        jLabel1.setText(org.openide.util.NbBundle.getMessage(AddJavaScriptLibraryToProjectPanel.class, "TestDialog_Project_Open_LBL")); // NOI18N

        projectComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        projectComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                projectComboBoxItemStateChanged(evt);
            }
        });

        jLabel2.setText(org.openide.util.NbBundle.getMessage(AddJavaScriptLibraryToProjectPanel.class, "TestDialog_Location_LBL")); // NOI18N

        overwriteCheckBox.setText(org.openide.util.NbBundle.getMessage(AddJavaScriptLibraryToProjectPanel.class, "AddJavaScriptLibraryToProjectPanel.overwriteCheckBox.text")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1Layout.createSequentialGroup()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(locationText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1Layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(projectComboBox, 0, 180, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, overwriteCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .add(15, 15, 15)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                .add(7, 7, 7)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(locationText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(19, 19, 19)
                .add(overwriteCheckBox)
                .add(17, 17, 17))
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void projectComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_projectComboBoxItemStateChanged
        // TODO add your handling code here;

        // Find out what type of project is first
        setDestinationLocation();

    }//GEN-LAST:event_projectComboBoxItemStateChanged

private void jList1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList1MouseClicked
// TODO add your handling code here:
}//GEN-LAST:event_jList1MouseClicked

private void jList1ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList1ValueChanged
// TODO add your handling code here:
}//GEN-LAST:event_jList1ValueChanged

    private class ProjectObject {

        private Project project;

        public ProjectObject(Project p) {
            project = p;
        }

        @Override
        public String toString() {
            return (ProjectUtils.getInformation(project).getDisplayName());
        }

        public Project getProject() {
            return project;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField locationText;
    private javax.swing.JCheckBox overwriteCheckBox;
    private javax.swing.JComboBox projectComboBox;
    // End of variables declaration//GEN-END:variables
}

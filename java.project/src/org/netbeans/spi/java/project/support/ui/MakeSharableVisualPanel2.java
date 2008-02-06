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

package org.netbeans.spi.java.project.support.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

final class MakeSharableVisualPanel2 extends JPanel {
    DefaultTableModel model;
    private String location = null;
    
    String ACTION_COPY = "copy"; //NOI18N
    String ACTION_RELATIVE = "keep"; //NOI18N
    String ACTION_ABSOLUTE = "abs"; //NOI18N
    String ACTION_USE_LOCAL_LIBRARY = "use";
    
    String[] comboValues = new String[] {
        ACTION_COPY, ACTION_RELATIVE, ACTION_ABSOLUTE, ACTION_USE_LOCAL_LIBRARY
    
    };
    
    /** Creates new form MakeSharableVisualPanel2 */
    public MakeSharableVisualPanel2() {
        initComponents();
        createTableDefinition();
        
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(MakeSharableVisualPanel2.class, "MakeSharablePanel2.LBL_Actions");
    }

    void readSettings(WizardDescriptor wiz) {
        System.out.println("readsettings..");
        String loc = (String) wiz.getProperty(MakeSharableUtils.PROP_LOCATION);
        System.out.println("loc=" + loc);
        AntProjectHelper helper = (AntProjectHelper) wiz.getProperty(MakeSharableUtils.PROP_HELPER);
        List<String> libraries = (List<String>) wiz.getProperty(MakeSharableUtils.PROP_LIBRARIES);
        List<String> jars = (List<String>) wiz.getProperty(MakeSharableUtils.PROP_JAR_REFS);
        if (!loc.equals(location)) {
            location = loc;
            populateTable(helper, libraries, jars);
            populateDescriptionField();
        }
    }

    void storeSettings(WizardDescriptor wiz) {
        ReferenceHelper helper = (ReferenceHelper) wiz.getProperty(MakeSharableUtils.PROP_REFERENCE_HELPER);
        AntProjectHelper anthelper = (AntProjectHelper) wiz.getProperty(MakeSharableUtils.PROP_HELPER);

        List<Action> actions = new ArrayList<Action>();
        for (int i = 0; i < model.getRowCount(); i++) {
            Object item = model.getValueAt(i, 0);
            String action = (String) model.getValueAt(i, 1);
            if (item instanceof Library) {
                Library lib = (Library)item;
                if (ACTION_ABSOLUTE.equals(action)) {
                    actions.add(new KeepLibraryAtLocation(lib, false, anthelper));
                } else if (ACTION_RELATIVE.equals(action)) {
                    actions.add(new KeepLibraryAtLocation(lib, true, anthelper));
                } else if (ACTION_COPY.equals(action)) {
                    actions.add(new CopyLibraryJars(helper, lib));
                } else if (ACTION_USE_LOCAL_LIBRARY.equals(action)) {
                    //DO nothing..
                } else {
                    assert false : "No handling defined for action: " + action;
                }
            } else if (item instanceof String) {
                //file reference
            } else if (item instanceof AntArtifact) {
                //project dependency
            }
        }
        wiz.putProperty(MakeSharableUtils.PROP_ACTIONS, actions);
        
    }

    private void createTableDefinition() {
        model = new DefaultTableModel() {

            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 1;
            }
            
        };
        model.addColumn("jar");
        model.addColumn("action");
        tblJars.setModel(model);
        TableColumn col1 = tblJars.getColumn("jar");
        col1.setHeaderValue(NbBundle.getMessage(MakeSharableVisualPanel2.class, "tblJars.header1"));
        col1.setResizable(true);
        col1.setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                String text = "";
                if (value instanceof Library) {
                    Library lib = (Library)value;
                    text = lib.getDisplayName();
                } else if (value instanceof String) {
                    text = (String)value;
                }
                
                return super.getTableCellRendererComponent(table, text, isSelected, hasFocus, row, column);
            }
            
        });
        TableColumn col2 = tblJars.getColumn("action");
        col2.setHeaderValue(NbBundle.getMessage(MakeSharableVisualPanel2.class, "tblJars.header2"));
        col2.sizeWidthToFit();
        
        JComboBox editorBox = new JComboBox(comboValues);
        editorBox.setEditable(false);
        editorBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String val = (String)value;
                if (ACTION_ABSOLUTE.equals(val)) {
                    val = NbBundle.getMessage(MakeSharableVisualPanel2.class, "TXT_Absolute");
                } else if (ACTION_COPY.equals(val)) {
                    val = NbBundle.getMessage(MakeSharableVisualPanel2.class, "TXT_Copy");
                } else if (ACTION_RELATIVE.equals(val)) {
                    val = NbBundle.getMessage(MakeSharableVisualPanel2.class, "TXT_Keep");
                } else if (ACTION_USE_LOCAL_LIBRARY.equals(val)) {
                    val = NbBundle.getMessage(MakeSharableVisualPanel2.class, "TXT_UseLocal");
                }
                return super.getListCellRendererComponent(list, val, index, isSelected, cellHasFocus);
            }
        });
        DefaultCellEditor ed = new DefaultCellEditor(editorBox);
        col2.setCellEditor(ed);
        col2.setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                String val = (String)value;
                if (ACTION_ABSOLUTE.equals(val)) {
                    val = NbBundle.getMessage(MakeSharableVisualPanel2.class, "TXT_Absolute");
                } else if (ACTION_COPY.equals(val)) {
                    val = NbBundle.getMessage(MakeSharableVisualPanel2.class, "TXT_Copy");
                } else if (ACTION_RELATIVE.equals(val)) {
                    val = NbBundle.getMessage(MakeSharableVisualPanel2.class, "TXT_Keep");
                } else if (ACTION_USE_LOCAL_LIBRARY.equals(val)) {
                    val = NbBundle.getMessage(MakeSharableVisualPanel2.class, "TXT_UseLocal");
                }
                return super.getTableCellRendererComponent(table, val, isSelected, hasFocus, row, column);
            }
            
        });
        tblJars.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                populateDescriptionField();
            }

        });
        tblJars.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblHint = new javax.swing.JLabel();
        lblJars = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblJars = new javax.swing.JTable();
        lblDetails = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        taDetails = new javax.swing.JTextArea();

        org.openide.awt.Mnemonics.setLocalizedText(lblHint, org.openide.util.NbBundle.getMessage(MakeSharableVisualPanel2.class, "MakeSharableVisualPanel2.lblHint.text")); // NOI18N
        lblHint.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        lblJars.setLabelFor(tblJars);
        org.openide.awt.Mnemonics.setLocalizedText(lblJars, org.openide.util.NbBundle.getMessage(MakeSharableVisualPanel2.class, "MakeSharableVisualPanel2.lblJars.text")); // NOI18N

        jScrollPane1.setViewportView(tblJars);

        lblDetails.setLabelFor(taDetails);
        org.openide.awt.Mnemonics.setLocalizedText(lblDetails, org.openide.util.NbBundle.getMessage(MakeSharableVisualPanel2.class, "MakeSharableVisualPanel2.lblDetails.text")); // NOI18N

        taDetails.setColumns(20);
        taDetails.setRows(5);
        jScrollPane2.setViewportView(taDetails);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(lblHint, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(lblJars)
                .addContainerGap())
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(lblDetails)
                .addContainerGap())
            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(lblHint, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 86, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblJars)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblDetails)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblDetails;
    private javax.swing.JLabel lblHint;
    private javax.swing.JLabel lblJars;
    private javax.swing.JTextArea taDetails;
    private javax.swing.JTable tblJars;
    // End of variables declaration//GEN-END:variables

    private void populateTable(AntProjectHelper helper, List<String> libraries, List<String> jars) {
        try {
            File libraryFile = helper.resolveFile(location);
            LibraryManager newmanager = LibraryManager.forLocation(libraryFile.toURI().toURL());
            LibraryManager oldmanager = LibraryManager.getDefault(); //TODO once we support moving from one place to another, change this
            for (String lib : libraries) {
                Library library = oldmanager.getLibrary(lib);
                Library newLib = newmanager.getLibrary(lib);
                String action = ACTION_COPY;
                if (newLib != null) {
                    action = ACTION_USE_LOCAL_LIBRARY;
                }
                if (library != null) {
                    model.addRow(new Object[] {library, action});
                } 
            }
            for (String jar : jars) {
                
            }
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private void populateDescriptionField() {
        int row = tblJars.getSelectedRow();
        if (row != -1) {
            Library lib = (Library)tblJars.getModel().getValueAt(row, 0);
            String type = lib.getType();
            LibraryTypeProvider provider = LibrariesSupport.getLibraryTypeProvider(type);
            assert provider != null;
            String typeString = provider.getDisplayName();
            String[] volumes = provider.getSupportedVolumeTypes();
            StringBuffer contents = new StringBuffer();
            for (String vol : volumes) {
                //TODO
                List<URL> urls = lib.getContent(vol);
                boolean any = false;
                for (URL url : urls) {
                    FileObject fo = URLMapper.findFileObject(url);
                    if (fo != null) {
                        if (FileUtil.getArchiveFile(fo) != null) {
                            url = URLMapper.findURL(FileUtil.getArchiveFile(fo), URLMapper.EXTERNAL);
                        }
                    }
                    contents.append(url).append("\n");
                    any = true;
                }
            }
            taDetails.setText("Type:" + typeString + 
                    "\nContents:\n" + contents);   
        } else {
            taDetails.setText("<No items selected>");
        }
    }
    
    private class KeepLibraryAtLocation extends AbstractAction {
        private boolean keepRelativeLocations;
        private Library library;
        private AntProjectHelper helper;

        KeepLibraryAtLocation(Library l , boolean relative, AntProjectHelper h) {
            library = l;
            keepRelativeLocations = relative;
            helper = h;
        }
        public void actionPerformed(ActionEvent e) {
            String loc = helper.getLibrariesLocation();
            assert loc != null;
            File mainPropertiesFile = helper.resolveFile(loc);
            try {
                LibraryManager man = LibraryManager.forLocation(mainPropertiesFile.toURI().toURL());
                Map<String, List<URL>> volumes = new HashMap<String, List<URL>>();
                LibraryTypeProvider provider = LibrariesSupport.getLibraryTypeProvider(library.getType());
                assert provider != null;
                for (String volume : provider.getSupportedVolumeTypes()) {
                    List<URL> urls = library.getContent(volume);
                    List<URL> newurls = new ArrayList<URL>();
                    for (URL url : urls) {
                        String jarFolder = null;
                        boolean isArchive = false;
                        if ("jar".equals(url.getProtocol())) { // NOI18N
                            jarFolder = getJarFolder(url);
                            url = FileUtil.getArchiveFile(url);
                            isArchive = true;
                        }
                        System.out.println("url=" + url);
                        FileObject fo = URLMapper.findFileObject(url);

                        if (fo != null) {
                            if (keepRelativeLocations) {
                                File path = FileUtil.toFile(fo);
                                String str = PropertyUtils.relativizeFile(mainPropertiesFile.getParentFile(), path);
                                url = LibrariesSupport.convertFilePathToURL(str);
                            } else {
                                url = fo.getURL();
                            }
                            if (isArchive) {
                                url = FileUtil.getArchiveRoot(url);
                            }
                            if (jarFolder != null) {
                                 url = appendJarFolder(url, jarFolder);
                            }
                            
                        }
                        

                        newurls.add(url);
                    }
                    volumes.put(volume, newurls);
                }
                
                man.createLibrary(library.getType(), library.getName(), volumes);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
    }
    
    private class CopyLibraryJars extends AbstractAction {
        private Library library;
        private ReferenceHelper refHelper;
 
        public CopyLibraryJars(ReferenceHelper h, Library l) {
            refHelper = h;
            library = l;
        }
         
        public void actionPerformed(ActionEvent e) {
            assert library.getManager() == LibraryManager.getDefault() : "Only converting from non-sharable to sharable is supported."; //NOi18N
            try {
                refHelper.copyLibrary(library);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
    }
    
    private class UseLocalLibrary extends AbstractAction {
        private AntProjectHelper ant;
        private ReferenceHelper helper;
        private String library;
        
        public UseLocalLibrary(ReferenceHelper r, String l, AntProjectHelper h) {
            helper = r;
            ant = h;
            library = l;
        }
        
        public void actionPerformed(ActionEvent e) {
            String loc = ant.getLibrariesLocation();
            assert loc != null;
            File mainPropertiesFile = ant.resolveFile(loc);
            try {
                    LibraryManager man = LibraryManager.forLocation(mainPropertiesFile.toURI().toURL());
                    Library lib = man.getLibrary(library);
                    if (lib != null) {
                        helper.createLibraryReference(lib, "classpath"); //TODO how to figure the original volume that was used in the original location?
                    } else {
                        assert false : "cannot reference a library that doesn't exist";
                    }
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            
        }
    }
    
    
    /** for jar url this method returns path wihtin jar or null*/
    private static String getJarFolder(URL url) {
        assert "jar".equals(url.getProtocol()) : url;
        String u = url.toExternalForm();
        int index = u.indexOf("!/"); //NOI18N
        if (index != -1 && index + 2 < u.length()) {
            return u.substring(index + 2);
        }
        return null;
    }

    /** append path to given jar root url */
    private static URL appendJarFolder(URL u, String jarFolder) {
        assert "jar".equals(u.getProtocol()) && u.toExternalForm().endsWith("!/") : u;
        try {
            return new URL(u + jarFolder.replace('\\', '/')); //NOI18N
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }     
    
}


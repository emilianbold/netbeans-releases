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

package org.netbeans.beaninfo.editors;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.ref.*;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.beans.*;
import java.io.File;

import javax.swing.event.DocumentListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.*;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbCollections;
import org.openide.windows.TopComponent;
/**
 * A panel for selecting an existing data folder. 
 * @author  Jaroslav Tulach, David Strupl
 * @version 
 */
class DataFolderPanel extends TopComponent implements
                    DocumentListener, DataFilter,
                    PropertyChangeListener, VetoableChangeListener {

    /** prefered dimmension of the panels */
    static java.awt.Dimension PREF_DIM = new java.awt.Dimension (450, 250);
                    
    /** format to for default package */
    /** listener to changes in the panel */
    private ChangeListener listener;

    /** file system reference */
    Reference<FileSystem>  system = new WeakReference<FileSystem> (null);

    /** root node */
    private Node rootNode;

    /** last DataFolder object that can be returned */
    private DataFolder df;

    /** */
    private DataFolderEditor editor;
    
    private static final String PATH_TOKEN_DELIMITER = "/" + java.io.File.separatorChar; // NOI18N
    
    private String last_suggestion = "";
    
    public DataFolderPanel(DataFolderEditor ed) {
        this();
        editor = ed;

        editor.env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        editor.env.addPropertyChangeListener(this);
    }
    
    /** Creates new form DataFolderPanel */
    public DataFolderPanel() {
        initComponents ();

        setName (getString("LAB_TargetLocationPanelName"));

        setBorder (new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 12, 0, 11)));
        /*
        packagesPanel.setBorder (new javax.swing.border.CompoundBorder(
                                     new javax.swing.border.TitledBorder(getString("LAB_SelectPackageBorder")),
                                     new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8)))
                                );
         */

        rootNode = createPackagesNode ();
        
        beanTreeView.setRootVisible (false);

        packagesPanel.getExplorerManager ().setRootContext (rootNode);
        packagesPanel.getExplorerManager ().addPropertyChangeListener (this);
        packagesPanel.getExplorerManager ().addVetoableChangeListener (this);

        // registers itself to listen to changes in the content of document
        packageName.getDocument().addDocumentListener(this);
        packageName.unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        
        beanTreeView.getAccessibleContext().setAccessibleDescription(getString("ACSD_DataFolderTree"));
        packageName.getAccessibleContext().setAccessibleDescription(getString("ACSD_package"));
        directoryName.getAccessibleContext().setAccessibleDescription(getString("ACSD_directory"));
        createButton.getAccessibleContext().setAccessibleDescription(getString("ACSD_Create"));
        getAccessibleContext().setAccessibleDescription(getString("ACSD_DataFolderPanel"));
    }

    /** Preffered size */
    public java.awt.Dimension getPreferredSize() {
        return PREF_DIM;
    }

    /** Creates node that displays all packages.
    */
    private Node createPackagesNode () {
        Node topNode = RepositoryNodeFactory.getDefault().repository(this);
        Node [] nodes = topNode.getChildren ().getNodes (true);
        assert nodes != null && nodes.length == 1 : "Only one subnode " + topNode + " found, but was " + Arrays.asList (nodes);
        return nodes [0];
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        packagesPanel = new org.netbeans.beaninfo.ExplorerPanel();
        beanTreeView = new org.openide.explorer.view.BeanTreeView();
        descriptionLabel = new javax.swing.JLabel();
        packageLabel = new javax.swing.JLabel();
        packageName = new javax.swing.JTextField();
        dirLabel = new javax.swing.JLabel();
        directoryName = new javax.swing.JTextField();
        createButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        packagesPanel.setLayout(new java.awt.GridBagLayout());

        beanTreeView.setDefaultActionAllowed(false);
        beanTreeView.setPopupAllowed(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        packagesPanel.add(beanTreeView, gridBagConstraints);

        descriptionLabel.setLabelFor(beanTreeView);
        org.openide.awt.Mnemonics.setLocalizedText(descriptionLabel, org.openide.util.NbBundle.getBundle(DataFolderPanel.class).getString("LAB_TargetLocationDescription")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        packagesPanel.add(descriptionLabel, gridBagConstraints);

        packageLabel.setLabelFor(packageName);
        org.openide.awt.Mnemonics.setLocalizedText(packageLabel, org.openide.util.NbBundle.getBundle(DataFolderPanel.class).getString("LAB_package")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 12);
        packagesPanel.add(packageLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        packagesPanel.add(packageName, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(dirLabel, org.openide.util.NbBundle.getBundle(DataFolderPanel.class).getString("LAB_directory")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        packagesPanel.add(dirLabel, gridBagConstraints);

        directoryName.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        packagesPanel.add(directoryName, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(createButton, org.openide.util.NbBundle.getBundle(DataFolderPanel.class).getString("CTL_Create")); // NOI18N
        createButton.setEnabled(false);
        createButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        packagesPanel.add(createButton, gridBagConstraints);

        add(packagesPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

  private void createButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createButtonActionPerformed
        try {
            // create the folder
            final DataFolder newDf = (DataFolder)getPropertyValue();
            // TODO: this line does not work - because the Node is not there yet
            setTargetFolder(newDf);
            updateDirectory ();
            updatePropertyEditor();
            enableCreateButton();
        } catch (IllegalStateException ex) {
              throw new RuntimeException(ex.getMessage());
        }
  }//GEN-LAST:event_createButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.openide.explorer.view.BeanTreeView beanTreeView;
    private javax.swing.JButton createButton;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JLabel dirLabel;
    private javax.swing.JTextField directoryName;
    private javax.swing.JLabel packageLabel;
    private javax.swing.JTextField packageName;
    private org.netbeans.beaninfo.ExplorerPanel packagesPanel;
    // End of variables declaration//GEN-END:variables

    //
    // Filter to accept only folders
    //

    /** Should the data object be displayed or not?
    * @param obj the data object
    * @return <CODE>true</CODE> if the object should be displayed,
    *    <CODE>false</CODE> otherwise
    */
    public boolean acceptDataObject(DataObject obj) {
        return obj instanceof DataFolder;
    }

    /** Allow only simple selection.
    */
    public void vetoableChange(PropertyChangeEvent ev)
    throws PropertyVetoException {
        if (ExplorerManager.PROP_SELECTED_NODES.equals (ev.getPropertyName ())) {
            Node[] arr = (Node[])ev.getNewValue();

            if (arr.length > 1) {
                throw new PropertyVetoException ("Only single selection allowed", ev); // NOI18N
            }
        }
    }

    /** Changes in selected node in packages.
    */
    public void propertyChange (PropertyChangeEvent ev) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals (ev.getPropertyName ())) {
            Node[] arr = packagesPanel.getExplorerManager ().getSelectedNodes ();
            if (!isVisible()) {
                // in the case we are not shown don't update the panel's state
                return;
            }
            if (arr.length == 1) {
                if (!isValid ()) {
                    setTargetFolder (null);
                    implSetDataFolder (null);
                    return ;
                }
                DataFolder df = (DataFolder)arr[0].getCookie (DataFolder.class);
                if (df != null) {
                    setTargetFolder (df);
                    updatePropertyEditor();
                    enableCreateButton();
                    return;
                }
            }
            setTargetFolder (null);
            implSetDataFolder (null);
        }


        if (PropertyEnv.PROP_STATE.equals(ev.getPropertyName()) && ev.getNewValue() == PropertyEnv.STATE_VALID) {
            editor.setValue(getPropertyValue());
        }

    }

    /** Fires info to listener.
    */
    private void fireStateChanged () {
        if (listener != null) {
            listener.stateChanged (new ChangeEvent (this));
        }
    }

    //
    // Modification of package name
    //

    public void changedUpdate(final javax.swing.event.DocumentEvent p1) {
        if (p1.getDocument () == packageName.getDocument ()) {
            SwingUtilities.invokeLater (new Runnable () {
                                            public void run () {
                                                String text = packageName.getText ();
                                                if (text != null) {
                                                    if (isValid()) {
                                                        setTargetFolder (text, false);
                                                        updatePropertyEditor();
                                                    }
                                                    updateDirectory ();
                                                }
                                                enableCreateButton();
                                            }
                                        });
            return;
        }
    }

    public void removeUpdate(final javax.swing.event.DocumentEvent p1) {
        // when deleted => do no looking for folder
        // changedUpdate (p1);
        if (p1.getDocument () == packageName.getDocument ()) {
            SwingUtilities.invokeLater(new Runnable () {
                                            public void run () {
                                                if (packageName.getText ().length () == 0) {
                                                    FileSystem fs = system.get ();
                                                    if (fs != null) {
                                                        DataFolder df = DataFolder.findFolder (fs.getRoot ());
                                                        setTargetFolder (df);
                                                        packageName.selectAll ();
                                                    }
                                                }
                                                String text = packageName.getText ();
                                                if (text != null) {
                                                    if (isValid()) {
                                                        setTargetFolder (text, true);
                                                        updatePropertyEditor();
                                                   }
                                                   updateDirectory ();
                                               }
                                               enableCreateButton();
                                            }
                                        });
        }
    }
    
    public void insertUpdate(final javax.swing.event.DocumentEvent p1) {
        changedUpdate (p1);
    }


    /** Help for this panel.
    * @return the help or <code>null</code> if no help is supplied
    */
    public org.openide.util.HelpCtx getHelp () {
        return new HelpCtx (DataFolderPanel.class);
    }

    /** Test whether the panel is finished and it is safe to proceed to the next one.
    * If the panel is valid, the "Next" (or "Finish") button will be enabled.
    * @return <code>true</code> if the user has entered satisfactory information
    */
    public boolean isValid () {
        String text = packageName.getText ();
        if (text.length () == 0) {
            Node[] arr = packagesPanel.getExplorerManager ().getSelectedNodes ();
            if (arr.length == 1 && arr[0] == rootNode) {
                return false;
            }
        }

        return true;
    }

    /** Add a listener to changes of the panel's validity.
    * @param l the listener to add
    * @see #isValid
    */
    public void addChangeListener (ChangeListener l) {
        if (listener != null) throw new IllegalStateException ();

        listener = l;
    }

    /** Remove a listener to changes of the panel's validity.
    * @param l the listener to remove
    */
    public void removeChangeListener (ChangeListener l) {
        listener = null;
    }

    /** Computes a suggestion for a given prefix and
    * a list of file objects.
    *
    * @param node the node to start with
    * @param pref prefix
    * @param first [0] is the first node that satisfies the suggestion
    * @return the longest continuation string for all folders that 
    *    starts with prefix
    */
    private static String computeSuggestion (
        Node node,
        String pref,
        Node[] first
    ) {
        Node[] arr = node.getChildren ().getNodes ();

        String match = null;

        for (int i = 0; i < arr.length; i++) {
            String name = arr[i].getName ();
            if (name.startsWith (pref)) {
                // ok, has the right prefix
                if (match == null) {
                    // first match
                    match = name;
                    if (first != null) {
                        first[0] = arr[i];
                    }
                } else {
                    // find common part of the names
                    int indx = pref.length ();
                    int end = Math.min (name.length (), match.length ());
                    while (indx < end && match.charAt (indx) == name.charAt (indx)) {
                        indx++;
                    }
                    match = match.substring (0, indx);
                }
            }
        }

        if (match == null ) {  // why? || match.length () == pref.length ()) {
            return null;
        } else {
            return match.substring (pref.length ());
        }
    }


    /** Presets a target folder.
    * @param f the folder
    * @return true if succeeded
    */
    boolean setTargetFolder (final DataFolder f) {
        boolean exact;
        Node n = null;
        String name;
        
        df = f;
        
        if (f != null) {
            FileObject fo = f.getPrimaryFile ();
            name = fo.getPath();

            StringTokenizer st = new StringTokenizer (name, PATH_TOKEN_DELIMITER);
            try {
                FileSystem fs = fo.getFileSystem ();

                if (fo.isRoot ()) {
                    // bugfix #31645, possibility create new folder under root
                    name = packageName.getText().trim();
                    // bugfix #32910, possibility create only for single folder
                    boolean withSubfolder = name.indexOf (File.separatorChar) != -1;
                    if (fo.getFileObject (name) != null || withSubfolder) {
                        name = ""; // NOI18N
                    }
                }

                system = new WeakReference<FileSystem> (fs);

                n = NodeOp.findPath(rootNode, NbCollections.checkedEnumerationByFilter(st, String.class, true));

                exact = true;
                
            } catch (FileStateInvalidException ex) {
                // invalid state of file system => back to root
                n = rootNode;
                name = ""; // NOI18N
                exact = false;
            } catch (NodeNotFoundException ex) {
                n = ex.getClosestNode();
                DataFolder df = (DataFolder)n.getCookie (DataFolder.class);
                if (df != null) {
                    name = df.getPrimaryFile ().getPath ();
                } else {
                    name = ""; // NO-I18N // NOI18N
                }
                exact = false;
            }


        } else {
            // null folder => use root
            n = rootNode;
            name = null;
            exact = true;
        }

        // remove listener + do change + add listener
        ExplorerManager em = packagesPanel.getExplorerManager ();
        em.removePropertyChangeListener (this);
        packageName.getDocument ().removeDocumentListener (this);

        try {
            em.setSelectedNodes (new Node[] { n });
        } catch (PropertyVetoException ex) {
            throw new InternalError ();
        }

        packageName.setText (name);
        updateDirectory ();

        packageName.getDocument ().addDocumentListener (this);
        em.addPropertyChangeListener (this);

        fireStateChanged ();

        return exact;
    }
    
    /** Getter for target folder. If the folder does not
    * exists it is created at this point.
    * @param create true if the target folder should be created.
    * @return the target folder
    * @exception IOException if the possible creation of the folder fails
    */
    private DataFolder getTargetFolder(boolean create) throws IOException {
        if (create && isValid()) {
            FileSystem fs = system.get ();
            if (fs != null) {
                DataFolder folder = DataFolder.findFolder (fs.getRoot ());
                String currentName = packageName.getText().replace('\\', '/');
                if (currentName.length () > 0) {
                    folder = DataFolder.create (folder, currentName);
                }
                df = folder;
                return folder;
            }
        }
        return df;
    }

    /** Presets a target folder.
    * @param f the name of target folder
    * @return true if succeeded
    */
    private boolean setTargetFolder (final String f, boolean afterDelete) {
        Node n = null;
        NodeNotFoundException closest = null;

        // first of all test the currently selected nod
        // for location of closest
        java.util.Collection<Node> selected = new java.util.HashSet<Node> ();
        Node[] nodes = packagesPanel.getExplorerManager().getSelectedNodes();
        for ( int i = 0; i < nodes.length; i++ ) {
            Node n1 = nodes[i];
            if ( n1.getParentNode() == null ) {
                continue;
            }
            while ( n1.getParentNode().getParentNode() != null )
                n1 = n1.getParentNode();
            selected.add( n1 );
        }

        StringTokenizer st = new StringTokenizer (f, PATH_TOKEN_DELIMITER);

        try {
            n = NodeOp.findPath(rootNode, NbCollections.checkedEnumerationByFilter(st, String.class, true));
        } catch (NodeNotFoundException ex) {
            if (!st.hasMoreElements ()) {
                // a test for !hasMoreElements is here to be sure that
                // all tokens has been read, so only the last item
                // has not been found

                // check whether we can continue from the nod
                final String sugg = computeSuggestion (
                                        ex.getClosestNode (),
                                        ex.getMissingChildName(),
                                        null
                                    );

                if ( sugg != null ) {
                    // if we can go on and there has been no suggestion o
                    // this is the current filesystem => go o
                    closest = ex;
                } 
            }
        }

        if (n != null) {
            // closest node not used
            closest = null;
        } else {

            if (closest == null) {
                // the node has not been even found
                return false;
            }

            // we will select the closest node found - old version
            n = closest.getClosestNode ();

            // new - try to build shadow nodes hierarchy
            
        }

        // remove listener + do change + add listener
        ExplorerManager em = packagesPanel.getExplorerManager ();
        em.removePropertyChangeListener (this);

        // change the text if we want to add suggestion
        if (closest != null) {
            Node[] first = new Node[1];
            String sugg = computeSuggestion (
                                    closest.getClosestNode (),
                                    closest.getMissingChildName(),
                                    first
                                );

            if ( afterDelete && sugg != null && sugg.equals( last_suggestion ) )
                sugg = null;
            
            last_suggestion = sugg;
            if (sugg != null) {
                packageName.getDocument ().removeDocumentListener (
                    DataFolderPanel.this
                );

                packageName.setText (f + sugg);
                updateDirectory ();

                javax.swing.text.Caret c = packageName.getCaret ();
                c.setDot (f.length () + sugg.length ());
                c.moveDot (f.length ());

                packageName.getDocument ().addDocumentListener (
                    DataFolderPanel.this
                );
            }

            if (first[0] != null) {
                // show the first node that fits
                n = first[0];
            }
        }


        // change the node
        try {
            em.setSelectedNodes(new Node[] { n });
            //beanTreeView.selectionChanged(new Node[] { n }, em);
        } catch (PropertyVetoException ex) {
            throw new InternalError ();
        }

        // change the selected filesystem
        df = (DataFolder)n.getCookie (DataFolder.class);
        if (df != null) {
            try {
                FileSystem fs = df.getPrimaryFile ().getFileSystem ();
                system = new WeakReference<FileSystem> (fs);
            } catch (FileStateInvalidException ex) {
            }
        }


        em.addPropertyChangeListener (this);

        fireStateChanged ();

        return closest == null;
    }

    /** Updates directory name
    */
    void updateDirectory () {
        FileSystem fs = system.get ();
        if (fs == null) {
            // No known directory?? Leave it blank.
            directoryName.setText(""); // NOI18N
            return;
        }
        String name = packageName.getText ();
        FileObject folder = fs.findResource(name);
        if (folder != null) {
            File f = FileUtil.toFile(folder);
            if (f != null) {
                // A folder is selected which exists on disk.
                directoryName.setText(f.getAbsolutePath());
            } else {
                // A folder is selected which is nowhere on disk (e.g. in a JAR).
                directoryName.setText(""); // NOI18N
            }
        } else {
            FileObject fo = fs.getRoot();
            assert fo != null : fs;
            File f = FileUtil.toFile(fo);
            if (f != null) {
                    // The folder does not really exist, but the FS root does
                    // exist on disk. Guess that the resulting file name will
                    // be derived simply from the folder of the root (not always
                    // true, note).
                    File f2 = new File(f, name.replace('/', File.separatorChar)); // NOI18N
                    directoryName.setText(f2.getAbsolutePath());
                } else {
                    // The folder has not been made, and even if it were, the FS
                    // root is not on disk anyway. Leave it blank.
                    directoryName.setText(""); // NOI18N
                }
        }
    }
    
    // bugfix #29401, correct notify all changes in data folders
    private void implSetDataFolder (DataFolder df) {
        if (editor != null) {
            if (!isValid ()) {
                editor.setDataFolder (null);
            } else {
                FileSystem fs = null;
                if (system != null) {
                    fs =  system.get();
                }
                if (df == null && fs!= null) {
                    FileObject fo = fs.getRoot();
                    //issue 34896, for whatever reason the root is sometimes null
                    if (fo != null) {
                        df = DataFolder.findFolder (fo);
                    }
                }
                if (df != null) {
                    String name = df.getPrimaryFile ().getPath ();
                    if (name.equals(packageName.getText())) {
                        editor.setDataFolder (df);
                    } else {
                        editor.setDataFolder (null);
                    }
                } else {
                    editor.setDataFolder(null);
                }
            }
        }
    }

    /** Updates associated editor by calling setDataFolder(...) . */
    void updatePropertyEditor() {
        try {
            DataFolder newF = getTargetFolder(false);
            //fix for issue 31434, DataFolder may be null if
            //user used the search popup in the target folder tree
            implSetDataFolder (newF);
        } catch (IOException ex) {
             Exceptions.printStackTrace(ex);
        }
    }
    
    /** Sets the state of the createButton */
    void enableCreateButton() {
        String name = null;
        if (df != null) {
            name = df.getPrimaryFile ().getPath ();
        } else {
            name = ""; // NOI18N
        }
        if (name.equals(packageName.getText())) {
            // nothing to create
            createButton.setEnabled(false);
        } else {
            createButton.setEnabled(isValid());
        }
    }
        
    
    /** Get the customized property value.
     * @return the property value
     * @exception InvalidStateException when the custom property editor does not contain a valid property value
     *           (and thus it should not be set)
     */
    private Object getPropertyValue() throws IllegalStateException {
        if (isValid()) {
            try {
                df = getTargetFolder(true);
                return df;
            } catch (IOException x) {
                Exceptions.printStackTrace(x);
                throw new IllegalStateException();
            }
        } else {
            throw new IllegalStateException();
        }
    }
    public static class ShadowDirNode extends AbstractNode {
        public ShadowDirNode(Children children) {
            super(children);
        }
    } 

    public static class ShadowLeafNode extends AbstractNode {
        public ShadowLeafNode() {
            super(Children.LEAF);
        }
    } 

    private static String getString (String s) {
        return org.openide.util.NbBundle.getBundle (DataFolderPanel.class).getString (s);
    }
}

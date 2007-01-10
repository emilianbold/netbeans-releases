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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx;

import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import javax.swing.JTextField;
import org.openide.util.NbBundle;
import org.openide.awt.Mnemonics;
import org.netbeans.api.java.source.JavaSource;
import org.openide.nodes.Node;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.NodeAcceptor;
import org.openide.nodes.NodeOperation;
import org.openide.loaders.DataObject;
import org.netbeans.api.project.SourceGroup;
import org.openide.util.UserCancelException;
import org.openide.filesystems.FileObject;

/**
 *
 * @author tl156378
 */
public class MBeanClassButton {
    
    private JTextField tf;
    private JButton btnBrowse;
    
    private SourceGroup[] projectSourceGroups;
    /**
     * index of the first <code>SourceGroup</code> where a file named
     * according to contents of {@link #srcRelFileNameSys} was found.
     * The search is performed in {@link #projectSourceGroupsRoots}.
     * If such a file is not found in any of the source groups roots,
     * this variable is set to <code>-1</code>.
     *
     * @see  #classExists
     */
    private int sourceGroupParentIndex = -1;
    
    private SourceGroup srcGroup = null;
    
    private transient ResourceBundle bundle;
    
    /** Creates a new instance of ClassButton */
    public MBeanClassButton(JButton btnBrowse, JTextField tf, 
            SourceGroup[] projectSourceGroups) {
        this.projectSourceGroups = projectSourceGroups;
        this.btnBrowse = btnBrowse;
        this.tf = tf;
        
        bundle = NbBundle.getBundle(ClassButton.class);
        
        Mnemonics.setLocalizedText(btnBrowse,
                                   bundle.getString("LBL_Browse")); //NOI18N
        
        final UIListener listener = new UIListener();
        
        btnBrowse.addActionListener(listener);
        btnBrowse.addFocusListener(listener);
    }
    
    private void btnBrowseFocusLost(FocusEvent e) {
        
    }
    
    private void classNameChanged() {
        
    }
    
    /**
     * Displays a class chooser dialog and lets the user to select a class.
     * If the user confirms their choice, full name of the selected class
     * is put into the <em>Class</em> text field.
     */
    private void chooseClass() {
        try {
            final Node[] sourceGroupNodes
                    = new Node[projectSourceGroups.length];
            for (int i = 0; i < sourceGroupNodes.length; i++) {
                /*
                 * Note:
                 * Precise structure of this view is *not* specified by the API.
                 */
                Node srcGroupNode
                       = PackageView.createPackageView(projectSourceGroups[i]);
                sourceGroupNodes[i]
                       = new FilterNode(srcGroupNode,
                                        new MBeanChildren(srcGroupNode));
            }
            
            Node rootNode;
            if (sourceGroupNodes.length == 1) {
                rootNode = new FilterNode(
                        sourceGroupNodes[0],
                        new JavaChildren(sourceGroupNodes[0]));
            } else {
                Children children = new Children.Array();
                children.add(sourceGroupNodes);
                
                AbstractNode node = new AbstractNode(children);
                node.setName("Project Source Roots");                   //NOI18N
                node.setDisplayName(
                        NbBundle.getMessage(getClass(), "LBL_Sources"));//NOI18N
                //PENDING - set a better icon for the root node
                rootNode = node;
            }
            
            NodeAcceptor acceptor = new NodeAcceptor() {
                public boolean acceptNodes(Node[] nodes) {
                    Node.Cookie cookie;
                    boolean accepted = nodes.length == 1
                           && (cookie = nodes[0].getCookie(DataObject.class))
                              != null
                           && ((DataObject) cookie).getPrimaryFile().isFolder()
                              == false;
                    if (accepted) {
                        DataObject dob = (DataObject)nodes[0].getCookie(DataObject.class);
                        FileObject fo = null;
                        if (dob != null) fo = dob.getPrimaryFile();
                        JavaSource foClass = JavaModelHelper.getSource(fo);
                        try {
                            accepted = JavaModelHelper.testMBeanCompliance(foClass);
                        }catch(IOException ioe) {
                            ioe.printStackTrace();
                            accepted = false;
                        }
                    }
                    return accepted;
                }
            };
            
            Node selectedNode = NodeOperation.getDefault().select(
                    bundle.getString("LBL_WinTitle_SelectClass"),    //NOI18N
                    bundle.getString("LBL_SelectClass"),       //NOI18N
                    rootNode,
                    acceptor)[0];
            
            SourceGroup selectedSourceGroup;
            if (sourceGroupNodes.length == 1) {
                selectedSourceGroup = projectSourceGroups[0];
            } else {
                Node previous = null;
                Node current = selectedNode.getParentNode();
                Node parent;
                while ((parent = current.getParentNode()) != null) {
                    previous = current;
                    current = parent;
                }
                /*
                 * 'current' now contains the root node of displayed node
                 * hierarchy. 'current' contains a parent node of the source
                 * root and 'previous' contains the parent source root of
                 * the selected class.
                 */
                selectedSourceGroup = null;
                Node selectedSrcGroupNode = previous;
                for (int i = 0; i < sourceGroupNodes.length; i++) {
                    if (sourceGroupNodes[i] == selectedSrcGroupNode) {
                        selectedSourceGroup = projectSourceGroups[i];
                        sourceGroupParentIndex = i;
                        break;
                    }
                }
                assert selectedSourceGroup != null;
                assert sourceGroupParentIndex >= 0;
            }
            srcGroup = selectedSourceGroup;
            
            FileObject selectedFileObj
                    = ((DataObject) selectedNode.getCookie(DataObject.class))
                      .getPrimaryFile();
            
            /* display selected class name: */
            try {
                String className = getClassName(selectedFileObj);
                tf.setText(className);
            }catch(IOException ioe) {
                ioe.printStackTrace();
            }
            
        } catch (UserCancelException ex) {
            // if the user cancels the choice, do nothing
        }
    }
    
    private static String getClassName(FileObject fileObj) throws IOException {
        JavaSource js = JavaModelHelper.getSource(fileObj);
        return JavaModelHelper.getFullClassName(js);
    }
    
    class UIListener implements ActionListener, DocumentListener,
                                    FocusListener {
            public void actionPerformed(ActionEvent e) {
                
                /* button Browse... pressed */
                
                chooseClass();
            }
            public void insertUpdate(DocumentEvent e) {
                classNameChanged();
            }
            public void removeUpdate(DocumentEvent e) {
                classNameChanged();
            }
            public void changedUpdate(DocumentEvent e) {
                classNameChanged();
            }
            public void focusGained(FocusEvent e) {
                
            }
            public void focusLost(FocusEvent e) {
                Object source = e.getSource();
                if ((source == btnBrowse) && !e.isTemporary()) {
                    btnBrowseFocusLost(e);
                }
            }
            
        }
    
}

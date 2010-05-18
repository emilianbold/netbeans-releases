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

package org.netbeans.modules.uml.core.reverseengineering.parsers.parsertestbed;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.coreapplication.CoreProduct;
import org.netbeans.modules.uml.core.coreapplication.CoreProductManager;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParser;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.RenderedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class TestbedUI extends JDialog
{
    void parseFile()
    {
        tokenMap.clear();
        ((DefaultTreeModel) trParserEvents.getModel()).setRoot(null);
        
        String filename = txtFilename.getText();
        if (filename == null || (filename = filename.trim()).length() == 0)
            return ;
        
        File f = new File(filename);
        if (!f.exists())
            return ;
        
        
        ILanguage lang = ProductRetriever.retrieveProduct()
                .getLanguageManager().getLanguageForFile(filename);
        if (lang != null)
        {
            final ILanguageParser parser = lang.getParser("Default");
            if (parser != null)
            {
                Filter stateFilter = new Filter();
                StateListener listener = new StateListener(this);
                final ErrorListener errListener = new ErrorListener(this);
                
                ListModel model = lstFilteredStates.getModel();
                for (int i = 0, count = model.getSize(); i < count; ++i)
                    stateFilter.addState(model.getElementAt(i).toString());
                
                model = lstFilteredTokens.getModel();
                for (int i = 0, count = model.getSize(); i < count; ++i)
                    stateFilter.addToken(model.getElementAt(i).toString());
                
                parser.setStateListener(listener);
                parser.setStateFilter(stateFilter);
                parser.setTokenProcessor(listener);
                parser.setTokenFilter(stateFilter);
                parser.setErrorListener(errListener);
                
                lastNode = null;
        
                final String fileToParse = filename;
                Thread t = new Thread()
                {
                    public void run()
                    {
                        parser.parseFile(fileToParse);
                        setRenderer();                       
                        ETSystem.out.println("Found " + errListener.getErrorCount() + " errors");
                        DefaultTreeModel model = 
                            (DefaultTreeModel)trParserEvents.getModel();
                        TreeNode root = (TreeNode) model.getRoot();
                        expandTreeNode(root);                       
                        
                    }
                };
                t.start();
            }
        }
    }
    
    public void setLastNode(TreeNode node)
    {
        lastNode = node;
    }
    
    public TreeNode getLastNode()
    {
        return lastNode;
    }
    
    /**
     * @param node
     * @param curName
     * @param string
     */
    public TreeNode addErrorNode(TreeNode parent, String curName, String text)
    {
        return addNode(parent, new ErrorNode(text));
    }
    
    /**
     * @param curName
     * @param string
     */
    public TreeNode addNode(TreeNode parent, MutableTreeNode newNode)
    {
        if (parent == null)
        {
            DefaultTreeModel model = (DefaultTreeModel) trParserEvents.getModel();
            parent = (TreeNode) model.getRoot();
            if (parent == null)
               {
                TreeNode node = new DefaultMutableTreeNode("Compilation Unit");
                model.setRoot(node);
                parent = node;
            }
        }

        ((DefaultMutableTreeNode) parent).add(newNode);
        
        return newNode;
    }
    
    /**
     * @param curName
     * @param string
     */
    public TreeNode addNode(TreeNode parent, String nodeKey, String nodeText)
    {
        return addNode(parent, new DefaultMutableTreeNode(nodeText));
    }
    
    public TreeNode getSelectedTreeNode()
    {
        TreePath path = trParserEvents.getSelectionPath();
        return path != null && path.getPathCount() > 0?
            (TreeNode) path.getPathComponent(path.getPathCount() - 1) : null;
    }
    
    public void treeSelectionChanged(TreeSelectionEvent e)
    {
        clearFields();
        TreePath path = e.getNewLeadSelectionPath();
        if (path != null && path.getPathCount() > 0)
        {    
            TreeNode node = (TreeNode) 
                    path.getPathComponent(path.getPathCount() - 1);
            ETPairT<ITokenDescriptor,String> tokd = tokenMap.get(node);
            
            if (tokd == null) return;
            ITokenDescriptor tok = tokd.getParamOne();
            String language      = tokd.getParamTwo();
            
            txtValue.setText(tok.getValue());
            txtLength.setText(String.valueOf(tok.getLength()));
            txtLine.setText(String.valueOf(tok.getLine()));
            txtColumn.setText(String.valueOf(tok.getColumn()));
            txtPosition.setText(String.valueOf(tok.getPosition()));
            txtLanguage.setText(language);
            txtDescription.setText(tok.getProperty("Comment"));
            txtLine1.setText(tok.getProperty("CommentStartLine"));
            txtColumn1.setText(tok.getProperty("CommentStartColumn"));
            txtPosition1.setText(tok.getProperty("CommentStartPos"));
            txtLength1.setText(tok.getProperty("CommentLength"));
        }
    }
    
    private void clearFields()
    {
        txtValue.setText("");
        txtLength.setText("");
        txtLine.setText("");
        txtColumn.setText("");
        txtPosition.setText("");
        txtLanguage.setText("");
        txtDescription.setText("");
        txtLine1.setText("");
        txtColumn1.setText("");
        txtPosition1.setText("");
        txtLength1.setText("");
    }
    
    private void jbInit() throws Exception 
    {
        //lblFilename.setRequestFocusEnabled(true);
        lblFilename.setText("Filename");
        lblFilename.setBounds(new Rectangle(20, 19, 59, 15));
        this.getContentPane().setLayout(null);
        txtFilename.setText("d:\\temp\\TestbedUI.java");
        txtFilename.setBounds(new Rectangle(82, 14, 385, 24));
        cmdChooseFile.setBounds(new Rectangle(481, 14, 42, 24));
        cmdChooseFile.setText("...");
        cmdChooseFile.setRequestFocusEnabled(true);
        cmdChooseFile.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                JFileChooser chooser = new JFileChooser();
                chooser.setAcceptAllFileFilterUsed(false);

                chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                    public boolean accept(File file) {
                        return file.isDirectory() ||
                            file.toString().toLowerCase().endsWith(".java");
                    }

                    public String getDescription() {
                        return "Java source files";
                    }
                });
                int returnVal = chooser.showOpenDialog(TestbedUI.this);
                if (returnVal == JFileChooser.APPROVE_OPTION)
                    txtFilename.setText(chooser.getSelectedFile().toString());


            }
        });
        trParserEvents.setBorder(BorderFactory.createEtchedBorder());
        trParserEvents.setBounds(new Rectangle(20, 80, 255, 397));
        trParserEvents.setScrollsOnExpand(true);
        lblEventsReceived.setRequestFocusEnabled(true);
        lblEventsReceived.setText("Events Received");
        jPanel1.setBounds(new Rectangle(286, 80, 234, 185));
        jPanel1.setLayout(null);
        jPanel1.setBorder(BorderFactory.createTitledBorder(
                             "Token Details"));
        lblValue.setText("Value");
        lblValue.setBounds(new Rectangle(14, 20, 82, 15));
        txtValue.setText("");
        txtValue.setBounds(new Rectangle(108, 17, 115, 20));
        jLabel2.setText("Length");
        jLabel2.setBounds(new Rectangle(14, 47, 82, 15));
        txtLength.setText("");
        txtLength.setBounds(new Rectangle(108, 44, 115, 20));
        jLabel3.setText("Line");
        jLabel3.setBounds(new Rectangle(14, 75, 82, 15));
        txtLine.setText("");
        txtLine.setBounds(new Rectangle(108, 72, 115, 20));
        jLabel4.setText("Column");
        jLabel4.setBounds(new Rectangle(14, 102, 82, 15));
        txtColumn.setText("");
        txtColumn.setBounds(new Rectangle(108, 99, 115, 20));
        jLabel5.setText("Position");
        jLabel5.setBounds(new Rectangle(14, 130, 82, 15));
        txtPosition.setText("");
        txtPosition.setBounds(new Rectangle(108, 127, 115, 20));
        jLabel6.setText("Language");
        jLabel6.setBounds(new Rectangle(14, 157, 82, 15));
        txtLanguage.setText("");
        txtLanguage.setBounds(new Rectangle(108, 154, 115, 20));
        jLabel13.setText("Length");
        jLabel13.setBounds(new Rectangle(13, 102, 82, 15));
        jPanel3.setLayout(null);
        jPanel3.setBounds(new Rectangle(286, 268, 234, 282));
        jPanel3.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder(
                                     "Comment Details"),
                                BorderFactory.createEmptyBorder(5,5,5,5)));
        txtLength1.setText("");
        txtLength1.setBounds(new Rectangle(106, 99, 116, 20));
        jLabel15.setRequestFocusEnabled(true);
        jLabel15.setText("Description");
        jLabel15.setBounds(new Rectangle(13, 125, 85, 15));
        txtDescription.setText("");
        txtDescription.setTabSize(8);
        txtDescription.setBounds(new Rectangle(10, 145, 213, 44));

        commentDesc = new JScrollPane(txtDescription);
        commentDesc.setBounds(new Rectangle(10, 145, 213, 126));
        commentDesc.setPreferredSize(new Dimension(400, 500));
        commentDesc.setVisible(true);

        lstFilteredStates.setBorder(BorderFactory.createEtchedBorder());
        lstFilteredStates.setBounds(new Rectangle(19, 574, 256, 95));
        lstFilteredTokens.setBorder(BorderFactory.createEtchedBorder());
        lstFilteredTokens.setBounds(new Rectangle(284, 574, 235, 95));
        cmdParse.setBounds(new Rectangle(204, 675, 150, 25));
        cmdParse.setText("Parse");
        cmdClose.setBounds(new Rectangle(369, 675, 150, 25));
        cmdClose.setText("Close");
        cmdClose.setDefaultCapable(true);
        jLabel18.setRequestFocusEnabled(true);
        jLabel18.setText("Filtered States");
        jLabel18.setBounds(new Rectangle(19, 555, 116, 15));
        jLabel19.setBounds(new Rectangle(284, 555, 123, 15));
        jLabel19.setText("Filtered Tokens");
        jLabel14.setText("Column");
        jLabel14.setBounds(new Rectangle(13, 47, 82, 15));
        txtPosition1.setText("");
        txtPosition1.setBounds(new Rectangle(106, 72, 116, 20));
        txtLine1.setText("");
        txtLine1.setBounds(new Rectangle(106, 17, 116, 20));
        txtColumn1.setText("");
        txtColumn1.setBounds(new Rectangle(106, 44, 116, 20));
        jLabel16.setText("Line");
        jLabel16.setBounds(new Rectangle(13, 20, 82, 15));
        jLabel17.setText("Position");
        jLabel17.setBounds(new Rectangle(13, 75, 82, 15));

        treeView = new JScrollPane(trParserEvents);
        treeView.setBounds(new Rectangle(20, 80, 255, 470));
        treeView.setPreferredSize(new Dimension(400, 500));
        treeView.setVisible(true);

        
        this.getContentPane().add(lblFilename, null);
        this.getContentPane().add(txtFilename, null);
        this.getContentPane().add(cmdChooseFile, null);
        this.getContentPane().add(treeView);
        this.getContentPane().add(lblEventsReceived, null);
        
        txtValue.setEditable(false);
        txtLength.setEditable(false);
        txtLine.setEditable(false);
        txtColumn.setEditable(false);
        txtPosition.setEditable(false);
        txtLanguage.setEditable(false);
        txtDescription.setEditable(false);
        txtLine1.setEditable(false);
        txtColumn1.setEditable(false);
        txtPosition1.setEditable(false);
        txtLength1.setEditable(false);
        
        txtValue.setHorizontalAlignment(JTextField.CENTER);
        txtLength.setHorizontalAlignment(JTextField.CENTER);
        txtLine.setHorizontalAlignment(JTextField.CENTER);
        txtColumn.setHorizontalAlignment(JTextField.CENTER);
        txtPosition.setHorizontalAlignment(JTextField.CENTER);
        txtLanguage.setHorizontalAlignment(JTextField.CENTER);
        txtLine1.setHorizontalAlignment(JTextField.CENTER);
        txtColumn1.setHorizontalAlignment(JTextField.CENTER);
        txtPosition1.setHorizontalAlignment(JTextField.CENTER);
        txtLength1.setHorizontalAlignment(JTextField.CENTER);
        
        txtValue.setFont(new Font("Verdana", Font.BOLD, 11));
        txtLength.setFont(new Font("Verdana", Font.BOLD, 11));
        txtLine.setFont(new Font("Verdana", Font.BOLD, 11));
        txtColumn.setFont(new Font("Verdana", Font.BOLD, 11));
        txtPosition.setFont(new Font("Verdana", Font.BOLD, 11));
        txtLanguage.setFont(new Font("Verdana", Font.BOLD, 11));
        txtLine1.setFont(new Font("Verdana", Font.BOLD, 11));
        txtColumn1.setFont(new Font("Verdana", Font.BOLD, 11));
        txtPosition1.setFont(new Font("Verdana", Font.BOLD, 11));
        txtLength1.setFont(new Font("Verdana", Font.BOLD, 11));

        jPanel1.add(jLabel3, null);
        jPanel1.add(txtLine, null);
        jPanel1.add(jLabel4, null);
        jPanel1.add(txtColumn, null);
        jPanel1.add(jLabel5, null);
        jPanel1.add(txtPosition, null);
        jPanel1.add(jLabel6, null);
        jPanel1.add(txtLanguage, null);
        jPanel1.add(lblValue, null);
        jPanel1.add(txtLength, null);
        jPanel1.add(jLabel2, null);
        jPanel1.add(txtValue, null);
        this.getContentPane().add(cmdParse, null);
        this.getContentPane().add(cmdClose, null);
        this.getContentPane().add(lstFilteredStates, null);
        this.getContentPane().add(jLabel18, null);
        this.getContentPane().add(jLabel19, null);
        this.getContentPane().add(lstFilteredTokens, null);
        this.getContentPane().add(jPanel3, null);
        jPanel3.add(commentDesc, null);
        this.getContentPane().add(jPanel1, null);
        jPanel3.add(txtLength1, null);
        jPanel3.add(txtPosition1, null);
        jPanel3.add(txtColumn1, null);
        jPanel3.add(txtLine1, null);
        jPanel3.add(jLabel14, null);
        jPanel3.add(jLabel17, null);
        jPanel3.add(jLabel13, null);
        jPanel3.add(jLabel16, null);
        jPanel3.add(jLabel15, null);

        
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setModal(true);
        setResizable(false);
        
        ((DefaultTreeModel) trParserEvents.getModel()).setRoot(null);
        
        cmdParse.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                parseFile();
            }
        });
        
        cmdClose.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                dispose();
            }
        });
        
        trParserEvents.addTreeSelectionListener(new TreeSelectionListener()
        {
            public void valueChanged(TreeSelectionEvent e)
            {
                treeSelectionChanged(e);
            }
        });
        
        constructPopups();
        getRootPane().setDefaultButton(cmdChooseFile);
        
                
        setTitle("Parsing Framework Testbed");
        pack();
    }
    
    protected void filterState()
    {
        TreeNode sel = getSelectedTreeNode();
        if (sel != null)
        {    
            ((DefaultListModel) lstFilteredStates.getModel()).addElement(
                    sel.toString());
        }
    }
    
    protected void filterToken()
    {
        TreeNode sel = getSelectedTreeNode();
        if (sel != null)
        {
            ((DefaultListModel) lstFilteredTokens.getModel()).addElement(
                    sel.toString());
        }
    }
    
    // If expand is true, expands all nodes in the tree.
    // Otherwise, collapses all nodes in the tree.
    protected void expandAll(JTree tree, boolean expand) {
        TreeNode root = (TreeNode)tree.getModel().getRoot();
        
        // Traverse tree from root
        expandAll(tree, new TreePath(root), expand);
    }
    
    private void expandAll(JTree tree, TreePath parent, boolean expand) {
        // Traverse children
        TreeNode node = (TreeNode)parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e=node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode)e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path, expand);
            }
        }
        
        // Expansion or collapse must be done bottom-up
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }
    
    protected void expandAll()
    {
        expandAll(trParserEvents, true);
    }
    
    protected void collapseAll()
    {
        expandAll(trParserEvents, false);
    }
    
    protected void expandTreeNode(TreeNode node)
    {
        TreePath sel = trParserEvents.getSelectionPath();
        if(node != null)
            sel = new TreePath(node);
        
        if (sel != null)
            trParserEvents.expandPath(sel);
    }
    
    protected void collapseTreeNode()
    {
        TreePath sel = trParserEvents.getSelectionPath();
        if (sel != null)
            trParserEvents.collapsePath(sel);
    }
    
    protected void showNextError()
    {
        
    }
    
    protected void showAllErrors()
    {
        
    }
    
    private void constructPopups()
    {
        stateMenu = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem("Filter State");
        menuItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                filterState();
            }
        });
        stateMenu.add(menuItem);
        
        stateMenu.add(new JSeparator());
        
        menuItem = new JMenuItem("Expand All");
        menuItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                expandAll();
            }
        });
        stateMenu.add(menuItem);
        
        menuItem = new JMenuItem("Collapse All");
        menuItem.addActionListener(new ActionListener()
                {
            public void actionPerformed(ActionEvent e)
            {
                collapseAll();
            }
        });
        stateMenu.add(menuItem);
        
        stateMenu.add(new JSeparator());
        
        menuItem = new JMenuItem("Expand");
        menuItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                expandTreeNode(null);
            }
        });
        stateMenu.add(menuItem);
        
        menuItem = new JMenuItem("Collapse");
        menuItem.addActionListener(new ActionListener()
                {
            public void actionPerformed(ActionEvent e)
            {
                collapseTreeNode();
            }
        });
        stateMenu.add(menuItem);
        
//        stateMenu.add(new JSeparator());
//        
//
//        menuItem = new JMenuItem("Show Next Error");
//        menuItem.addActionListener(new ActionListener()
//        {
//            public void actionPerformed(ActionEvent e)
//            {
//                showNextError();
//            }
//        });
//        stateMenu.add(menuItem);
//        
//        menuItem = new JMenuItem("Show All Errors");
//        menuItem.addActionListener(new ActionListener()
//                {
//            public void actionPerformed(ActionEvent e)
//            {
//                showAllErrors();
//            }
//        });
//        stateMenu.add(menuItem);
        
        
        tokenMenu = new JPopupMenu();
        menuItem = new JMenuItem("Filter Token");
        menuItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                filterToken();
            }
        });
        tokenMenu.add(menuItem);
        
//        tokenMenu.add(new JSeparator());
//
//        menuItem = new JMenuItem("Show Next Error");
//        menuItem.addActionListener(new ActionListener()
//        {
//            public void actionPerformed(ActionEvent e)
//            {
//                showNextError();
//            }
//        });
//        tokenMenu.add(menuItem);
//        
//        menuItem = new JMenuItem("Show All Errors");
//        menuItem.addActionListener(new ActionListener()
//        {
//            public void actionPerformed(ActionEvent e)
//            {
//                showAllErrors();
//            }
//        });
//        tokenMenu.add(menuItem);
        

        // Add listener to components that can bring up popup menus.
        MouseListener popupListener = new MouseAdapter() 
        {
            
            public void mousePressed(MouseEvent e) 
            {
                maybeShowPopup(e);
            }

            public void mouseReleased(MouseEvent e) 
            {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) 
            {
                if (e.isPopupTrigger()) 
                {
                    TreeNode node = getSelectedTreeNode();
                    if (node != null && !(node instanceof ErrorNode) &&
                            !"Compilation Unit".equals(node.toString()))
                        if (tokenMap.containsKey(node))
                            tokenMenu.show(e.getComponent(),
                                           e.getX(), e.getY());
                        else
                            stateMenu.show(e.getComponent(),
                                           e.getX(), e.getY());
                    else if(node != null && "Compilation Unit".equals(node.toString()))
                    {
                        if(compUnitMenu == null)
                        {
                            compUnitMenu = new JPopupMenu();
                            JMenuItem menuItem = new JMenuItem("Expand All");
                            menuItem.addActionListener(new ActionListener()
                            {
                                public void actionPerformed(ActionEvent e)
                                {
                                    expandAll();
                                }
                            });
                            compUnitMenu.add(menuItem);
            
                            menuItem = new JMenuItem("Collapse All");
                            menuItem.addActionListener(new ActionListener()
                                    {
                                public void actionPerformed(ActionEvent e)
                                {
                                    collapseAll();
                                }
                            });
                            compUnitMenu.add(menuItem);
            
                            compUnitMenu.add(new JSeparator());
            
                            menuItem = new JMenuItem("Expand");
                            menuItem.addActionListener(new ActionListener()
                            {
                                public void actionPerformed(ActionEvent e)
                                {
                                    expandTreeNode(null);
                                }
                            });
                            compUnitMenu.add(menuItem);
            
                            menuItem = new JMenuItem("Collapse");
                            menuItem.addActionListener(new ActionListener()
                                    {
                                public void actionPerformed(ActionEvent e)
                                {
                                    collapseTreeNode();
                                }
                            });
                            compUnitMenu.add(menuItem);
                        }
                        compUnitMenu.show(e.getComponent(),
                                         e.getX(), e.getY());
                    }
                }
            }
        };
        trParserEvents.addMouseListener(popupListener);
        
        
        KeyListener kl = new KeyAdapter()
        {
            public void keyReleased(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_DELETE)
                {
                    JList list = (JList) e.getSource();
                    int sel = list.getSelectedIndex();
                    if (sel != -1)
                        ((DefaultListModel) list.getModel()).remove(sel);
                }
            }
        };
        
        lstFilteredStates.addKeyListener(kl);
        lstFilteredTokens.addKeyListener(kl);        

    }
    
    private void setRenderer()
    {
        URL tokenURL = getClass().getResource("token.gif");
        URL stateURL = getClass().getResource("state.gif");
        URL errorURL = getClass().getResource("error.gif");
        ImageIcon tokenIcon = new ImageIcon(tokenURL);
        ImageIcon stateIcon = new ImageIcon(stateURL);
        ImageIcon errorIcon = new ImageIcon(errorURL);
        if ((tokenIcon != null && stateIcon != null) || errorIcon != null) {
            DefaultTreeCellRenderer renderer = 
            new DefaultTreeCellRenderer();
            renderer.setLeafIcon(errorsExist?errorIcon:tokenIcon);
            renderer.setFont(new Font("Arial", Font.BOLD, 11));
            renderer.setOpenIcon(errorsExist?errorIcon:stateIcon);
            renderer.setClosedIcon(errorsExist?errorIcon:stateIcon);
            trParserEvents.setCellRenderer(renderer);
        }
    }
    
    /* (non-Javadoc)
     * @see java.awt.Dialog#dispose()
     */
    public void dispose()
    {
        super.dispose();
        System.exit(0);
    }
    
    public void addToken(TreeNode node, ITokenDescriptor token, String language)
    {
        tokenMap.put(node, new ETPairT<ITokenDescriptor,String>(token, language) );
    }
    
    public static void main(String[] args) 
    {
        CoreProduct product = new CoreProduct();
        CoreProductManager.instance().setCoreProduct(product);
        ICoreProduct prod = CoreProductManager.instance().getCoreProduct();
        prod.initialize();
        
        try 
        {
            UIManager.setLookAndFeel(
                "com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        }
        catch (Exception e) 
        {
        }

        new TestbedUI().setVisible(true);
    }

    public TestbedUI() throws HeadlessException 
    {
        try 
        {
            jbInit();
            this.setSize(550, 730);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void errorsFound(boolean err)
    {
        errorsExist = err;
    }
    
    private static class ErrorNode extends DefaultMutableTreeNode
    {
        public ErrorNode(String text)
        {
            super(text);
        }
    }
    
    private Map<TreeNode, ETPairT<ITokenDescriptor,String>> tokenMap = 
            new HashMap<TreeNode, ETPairT<ITokenDescriptor,String>>();

    private TreeNode lastNode;
    
    /**
     * This is the filename JLabel
     */
    private JLabel lblFilename = new JLabel();
    
    private JTextField txtFilename = new JTextField();
    private JButton cmdChooseFile = new JButton();
    private JTree trParserEvents = new JTree();
    private JLabel lblEventsReceived = new JLabel();
    private JPanel jPanel1 = new JPanel();
    private JLabel lblValue = new JLabel();
    private JTextField txtValue = new JTextField();
    private JLabel jLabel2 = new JLabel();
    private JTextField txtLength = new JTextField();
    private JLabel jLabel3 = new JLabel();
    private JTextField txtLine = new JTextField();
    private JLabel jLabel4 = new JLabel();
    private JTextField txtColumn = new JTextField();
    private JLabel jLabel5 = new JLabel();
    private JTextField txtPosition = new JTextField();
    private JLabel jLabel6 = new JLabel();
    private JTextField txtLanguage = new JTextField();
    private JTextField jTextField7 = new JTextField();
    private JLabel jLabel7 = new JLabel();
    private JLabel jLabel8 = new JLabel();
    private JTextField jTextField8 = new JTextField();
    private JLabel jLabel9 = new JLabel();
    private JLabel jLabel10 = new JLabel();
    private JLabel jLabel11 = new JLabel();
    private JTextField jTextField9 = new JTextField();
    private JTextField jTextField10 = new JTextField();
    private JTextField jTextField11 = new JTextField();
    private JTextField jTextField12 = new JTextField();
    private JLabel jLabel12 = new JLabel();
    private JLabel jLabel13 = new JLabel();
    private JPanel jPanel3 = new JPanel();
    private JTextField txtLength1 = new JTextField();
    private JLabel jLabel15 = new JLabel();
    private JTextArea txtDescription = new JTextArea();
    private JList lstFilteredStates = new JList(new DefaultListModel());
    private JList lstFilteredTokens = new JList(new DefaultListModel());
    private JButton cmdParse = new JButton();
    private JButton cmdClose = new JButton();
    private JLabel jLabel18 = new JLabel();
    private JLabel jLabel19 = new JLabel();
    private JLabel jLabel14 = new JLabel();
    private JTextField txtPosition1 = new JTextField();
    private JTextField txtLine1 = new JTextField();
    private JTextField txtColumn1 = new JTextField();
    private JLabel jLabel16 = new JLabel();
    private JLabel jLabel17 = new JLabel();
    
    private boolean errorsExist = false;
    
    
    private JPopupMenu stateMenu, tokenMenu, compUnitMenu;
    
    JScrollPane treeView;
    JScrollPane commentDesc;
}

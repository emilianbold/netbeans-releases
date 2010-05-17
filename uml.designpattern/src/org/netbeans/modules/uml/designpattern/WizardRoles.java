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

package org.netbeans.modules.uml.designpattern;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.dynamics.Message;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.ui.support.commondialogs.IErrorDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;
import org.netbeans.modules.uml.ui.support.commonresources.CommonResourceManager;
import org.netbeans.modules.uml.ui.support.commonresources.ICommonResourceManager;
import org.netbeans.modules.uml.ui.support.wizard.IWizardSheet;
import org.netbeans.modules.uml.ui.support.wizard.WizardInteriorPage;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingErrorDialog;
import org.netbeans.modules.uml.ui.swing.treetable.JDefaultMutableTreeNode;
import org.netbeans.modules.uml.ui.swing.treetable.JTreeTable;

public class WizardRoles extends WizardInteriorPage {
    
    private static final String PG_CAPTION = DefaultDesignPatternResource.getString("IDS_WIZARDCAPTION");
    private static final String PG_TITLE = DefaultDesignPatternResource.getString("IDS_CHOOSEPARTICIPANTS");
    private static final String PG_SUBTITLE = DefaultDesignPatternResource.getString("IDS_CHOOSEPARTICIPANTSHELP");
    
    private Wizard m_Wizard = null;
    
    private JDefaultMutableTreeNode m_Root = null;
    private RoleTreeTableModel m_Model = null;
    private JTreeTable m_Tree = null;
    
    private ICommonResourceManager m_ResourceMgr = CommonResourceManager.instance();
    
    // map of element type to element name to element id
    private Hashtable <String, Hashtable>		m_TypeMap = new Hashtable <String, Hashtable>();
    
    //this variable captures the row where user right clicked.
    private int m_RightClickRow = 0;
    
    public WizardRoles(IWizardSheet parent, String caption, String headerTitle, String headerSubTitle) {
        super(parent, caption, headerTitle, headerSubTitle);
        createUI();
    }
    
    public WizardRoles(IWizardSheet parent) {
        this(parent, PG_CAPTION, PG_TITLE, PG_SUBTITLE);
    }
    
    protected void createUI() {
        super.createUI();
        
        this.addActionListeners();
        this.onInitDialog();
    }
    
    private void addActionListeners() {
    }
    
    /**
     * Called when dialog is initialized
     *
     *
     *
     * @return BOOL
     *
     */
    protected boolean onInitDialog() {
        super.onInitDialog();
        
        IWizardSheet parent = getParentSheet();
        m_Wizard = (Wizard) parent;
        
        populateGrid();
        
        return true; // return TRUE unless you set the focus to a control
    }
    /**
     * Called when the page becomes active
     *
     *
     *
     * @return BOOL
     */
    public void onSetActive() {
        if (m_Wizard != null) {
            populateGrid();
        }
        super.onSetActive();
    }
    
    /**
     * Populate the grid
     *
     * @return HRESULT
     */
    private void populateGrid() {
        m_Root = new JDefaultMutableTreeNode("Root");
        //
        // get the roles from the pattern details and have each one add itself
        //
        if (m_Wizard != null) {
            IDesignPatternDetails pDetails = m_Wizard.getDetails();
            if (pDetails != null) {
                ETList <IDesignPatternRole> pRoles = pDetails.getRoles();
                if (pRoles != null) {
                    int count = pRoles.size();
                    for (int x = 0; x < count; x++) {
                        IDesignPatternRole pRole = pRoles.get(x);
                        if (pRole != null) {
                            if (!addRoleToGrid(pRole)) {
                                break;
                            }
                        }
                    }
                    m_Model = null;
                    m_Model = new RoleTreeTableModel(m_Root, this);
                    m_Tree = null;
                    m_Tree = new JRoleTreeTable(m_Model, this);
                    FontMetrics metrics = m_Tree.getFontMetrics(m_Tree.getFont());
                    m_Tree.setRowHeight(metrics.getHeight() + 6);
                    m_Tree.getTree().setRootVisible(false);
                    m_Tree.getAccessibleContext().setAccessibleName(DefaultDesignPatternResource.getString("ACSN_ROLETREE"));
                    m_Tree.getAccessibleContext().setAccessibleDescription(DefaultDesignPatternResource.getString("ACSD_ROLETREE"));
                    m_Model.setTreeTable(m_Tree);
                    int count2 = m_Model.getChildCount(m_Root);
                    for (int i=count2-1; i>=0; i--) {
                        m_Model.expand(i, true);
                    }
                    prepareToShow();
                }
            }
        }
    }
    public void prepareToShow() {
        pnlContents.setLayout(new GridBagLayout());
        pnlContents.removeAll();
        if (m_Tree != null) {
            JScrollPane pane = new JScrollPane(m_Tree);
            GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 0.8;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.insets=new Insets(12,10,12,10);
            pnlContents.add(pane, gridBagConstraints);
            refresh();
        }
    }
    /**
     *
     */
    public void refresh() {
        Graphics g = this.getGraphics();
        if (g != null) {
            this.paintAll(g);
        }
    }
    
    /**
     * Called when the user clicks back
     *
     *
     *
     * @return LRESULT		Whether or not to continue to the next page
     *
     */
    public void onWizardBack() {
        
        if (m_Wizard != null) {
            m_Wizard.m_RefreshPages = false;
        }
        super.onWizardBack();
    }
    /**
     * Called when the user clicks next
     *
     *
     *
     * @return LRESULT		Whether or not to continue to the next page
     *
     */
    public void onWizardNext() {
        // store it on the pattern details
        if (storeParticipants()) {
            // validate the page information
            ETList <String> errorList = validatePage();
            if (errorList != null && errorList.size() == 0) {
                super.onWizardNext();
            } else if (errorList != null && errorList.size() > 0) {
                // display the errors
                String msg = DesignPatternUtilities.formatErrorMessage(errorList);
                DesignPatternUtilities.displayErrorMessage(m_Wizard, msg);
            }
        }
    }
    /**
     * Add the passed in design pattern role to the grid for display
     *
     * @param[in] pRole		The object representing the role
     *
     * @return HRESULT
     */
    private boolean addRoleToGrid(IDesignPatternRole pRole) {
        boolean result = true;
        //
        // get the information from the role
        //
        if (pRole != null) {
            String roleName = pRole.getName();
            int mult = pRole.getMultiplicity();
            //
            // add the item to the grid
            //
            WizardRoleObject obj = new WizardRoleObject("", pRole);
            JDefaultMutableTreeNode node = new JDefaultMutableTreeNode(obj, true);
            m_Root.add(node);
            if (mult > 1) {
                // if the role has been designated having the ability to have more than one
                // participant fulfilling it, we will add the participants as child nodes
                result = addChildNodeForRole(node, pRole);
            } else {
                // the role can only be played by one participant, so figure out the name
                // and put it in the grid cell
                calculateName(node, obj);
            }
        }
        return result;
    }
    /**
     * Builds the maps used by the dialog.  Used for performance reasons
     * to build the list of element names to display in the picklist for
     * a particular role (element type)
     *
     * @param[in] pRole		The object representing the role
     *
     * @return HRESULT
     */
    private void buildRoleMaps(IDesignPatternRole pRole) {
        if (pRole != null) {
            Hashtable <String, Vector<String> > eleNameToIDMap = new Hashtable < String, Vector <String> >();
            
            String type = pRole.getTypeID();
            if (type == null || type.length() == 0) {
                type = "Classifier";
            }
            Hashtable ht = m_TypeMap.get(type);
            if (ht == null || type == null || type.length() == 0) {
                // otherwise, populate the combo list box of the grid cell with
                // similar element types of the role
                String pattern = "";
                if (type.equals("Classifier")) {
                    pattern = "//*[name() = \'UML:Class\'";
                    pattern += " or name() = \'UML:Activity\'";
                    pattern += " or name() = \'UML:Actor\'";
                    pattern += " or name() = \'UML:Aggregation\'";
                    pattern += " or name() = \'UML:Artifact\'";
                    pattern += " or name() = \'UML:Association\'";
                    pattern += " or name() = \'UML:Collaboration\'";
                    pattern += " or name() = \'UML:Component\'";
                    pattern += " or name() = \'UML:DataType\'";
                    pattern += " or name() = \'UML:Interface\'";
                    pattern += " or name() = \'UML:Node\'";
                    pattern += " or name() = \'UML:UseCase\'";
                    pattern += "]";
                } else {
                    pattern = "//UML:";
                    pattern += type;
                }
                // get the names of the element's matching the type of the participant role
                if (m_Wizard != null) {
                    IDesignPatternDetails pDetails = m_Wizard.getDetails();
                    if (pDetails != null) {
                        IProject pProject = pDetails.getProject();
                        if (pProject != null) {
                            ETList<String> pStrings = DesignPatternUtilities.getElementNames(pProject, pattern, false);
                            ETList<String> pStrings2 = DesignPatternUtilities.getElementNames(pProject, pattern, true);
                            if (pStrings != null) {
                                // turn the names into a string that the grid knows about
                                String formattedStr = formatElementNames(pStrings);
                                // store what we found in the project in a map for fast lookup
                                int count = pStrings.size();
                                for (int x = 0; x < count; x++) {
                                    String str = pStrings.get(x);
                                    String id = pStrings2.get(x);
                                    if (id != null && id.length() > 0) {
                                        Vector<String> v = eleNameToIDMap.get(str);
                                        if (v == null) {
                                            v = new Vector<String>();
                                        }
                                        v.add(id);
                                        eleNameToIDMap.put(str, v);
                                    }
                                }
                                m_TypeMap.put(type, eleNameToIDMap);
                            }
                        }
                    }
                }
            }
        }
    }
    /**
     * Adds another node to the grid representing the passed in role
     *
     * @param[in]	row		The row in which to add the new node to
     * @param[in]	pRole		The role that the new node will represent
     *
     * @return HRESULT
     */
    private boolean addChildNodeForRole(JDefaultMutableTreeNode node, IDesignPatternRole pRole) {
        boolean result = true;
        if (pRole != null) {
            // get the role name
            String roleName = pRole.getName();
            // get the role multiplicity
            int mult = pRole.getMultiplicity();
            // determine if we should add another role
            int children = node.getChildCount();
            if (children < mult) {
                // add a child node
                WizardRoleObject obj = new WizardRoleObject(roleName, pRole);
                JDefaultMutableTreeNode pnode = new JDefaultMutableTreeNode(obj, true);
                node.add(pnode);
                if (mult > 1) {
                    // figure out the new name
                    calculateName(pnode, obj);
                }
            } else {
                IErrorDialog pTemp = new SwingErrorDialog(m_Wizard);
                if (pTemp != null) {
                    Integer i = new Integer(mult);
                    String cstr =  DesignPatternUtilities.translateString("IDS_INVALIDROLECOUNT");
                    cstr = StringUtilities.replaceSubString(cstr, "%d", i.toString());
                    String title = DesignPatternUtilities.translateString("IDS_TITLE1");
                    pTemp.display(cstr, MessageIconKindEnum.EDIK_ICONINFORMATION, title);
                }
            }
        }
        return result;
    }
    /**
     * Remove the node in the grid at the passed in row
     *
     * @param[in]	row	The row which to remove
     *
     * @return HRESULT
     */
    private void removeChildNodeForRole(int row) {
        JDefaultMutableTreeNode pNode = getNodeAtGridRow(row);
        if (pNode != null) {
            JDefaultMutableTreeNode parentNode = (JDefaultMutableTreeNode)pNode.getParent();
            if (parentNode != null) {
                // if there is a parent of the node that was the current node
                // when the user hit the delete key
                // do not allow the user to delete all of the children, must
                // have at least one
                int cnt = parentNode.getChildCount();
                if (cnt > 1) {
                    // get the node on the row in which they clicked
                    // and remove it
                    parentNode.remove(pNode);
                }
            }
        }
    }
    /**
     * While the user is assigning participants to roles, we figure out a default name
     * for the participant.
     *
     * @param[in]	row		Row where the name needs to be determined
     * @param[in]	pRole		Role of the participant whose name needs to be determined
     * @param[out] sName		The calculated name
     *
     * @return HRESULT
     */
    public void calculateName(JDefaultMutableTreeNode node, WizardRoleObject pObj) {
        String sName = "";
        if (pObj != null) {
            IDesignPatternRole pRole = pObj.getRole();
            if (pRole != null) {
                // get some information from the role
                String roleID = pRole.getID();
                String roleName = pRole.getName();
                int mult = pRole.getMultiplicity();
                // default the calculated name to the role name
                if (mult > 1) {
                    if (node.getParent() == null) {
                    } else if (node.getParent().equals(m_Root)) {
                    } else {
                        sName = roleName;
                    }
                } else {
                    sName = roleName;
                }
                // now do some more work to determine the name
                // the first attempt will be to take the rolename and make
                // it unique based on the number of child nodes
                int children = 0;
                TreeNode parentNode = node.getParent();
                if ( parentNode != null && !parentNode.equals(m_Root)) {
                    // does this node have any children
                    if (parentNode.getChildCount() > 1) {
                        // yes, it has children, so take the number of children
                        // and append it to the rolename, this will make the name
                        // unique
                        Integer i = new Integer(parentNode.getChildCount());
                        sName += i.toString();
                    }
                }
                // No matter what, the name is now either only the role name, or it is the
                // rolename with a number appended to it
                
                // This next case will set the name if there are already designated participants
                // so it will take the participant name and make it unique by adding a number
                IDesignPatternDetails pDetails = m_Wizard.getDetails();
                if (pDetails != null) {
                    ETList <String> pNames = pDetails.getParticipantNames(roleID);
                    if (pNames != null) {
                        int count = pNames.size();
                        if (count > 0) {
                            String name2 = pNames.get(0);
                            // this could be a name or an id, if it is an id, we need to transform it to
                            // a name
                            IProject pProject = pDetails.getProject();
                            String pattern = "//*[@xmi.id=\"" + name2 + "\"]";
                            INamedElement pElement = getElement(pProject, pattern);
                            if (pElement != null) {
                                sName = pElement.getQualifiedName();
                                pObj.setChosenID(name2);
                            } else {
                                sName = name2;
                            }
                            if (children > 1) {
                                Integer i = new Integer(children);
                                String buffer = i.toString();
                                sName += buffer;
                            }
                        }
                    }
                }
            }
        }
        pObj.setChosenName(sName);
    }
    /**
     * Turn the array of passed in strings into a format that the grid understands
     *
     * @param[in] pStrings		An array of strings
     * @param[out]	pStr			A "|" concatenated string
     *
     * @return HRESULT
     */
    private String formatElementNames(ETList<String> pStrings) {
        String pStr = "";
        if (pStrings != null) {
            int count = pStrings.size();
            if (count > 0) {
                pStr += "|";
            }
            for (int x = 0; x < count; x++) {
                String str = pStrings.get(x);
                pStr += str;
                pStr += "|";
            }
        }
        return pStr;
    }
    /**
     * Get the element from the project that matches the pattern
     *
     *
     * @param pProject[in]			The project to look in
     * @param pattern[in]			The xpath query to use in the search
     * @param pElement[out]			The found element
     *
     * @return HRESULT
     *
     */
    private INamedElement getElement(IProject pProject, String pattern) {
        INamedElement pElement = null;
        if (pProject != null) {
            // use the element locator to do this
            IElementLocator pLocator = new ElementLocator();
            if (pLocator != null) {
                // find any elements matching the xpath query
                ETList <IElement> pElements = pLocator.findElementsByDeepQuery(pProject, pattern);
                if (pElements != null) {
                    // put one of them into the out param
                    int count = pElements.size();
                    for (int x = 0; x < count; x++) {
                        IElement pEle = pElements.get(x);
                        if (pEle != null) {
                            if (pEle instanceof INamedElement) {
                                pElement = (INamedElement)pEle;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return pElement;
    }
    /**
     * Grid event - called before the grid cell enters edit mode
     *
     *
     * @param Row[in]				The row of the grid cell
     * @param Col[in]				The column of the grid cell
     * @param Cancel[out]		Whether or not to cancel the edit
     *
     * @return
     */
    private void onBeforeEditGrid(int Row, int Col) {
        // See TreeTableCellEditor.getTableCellEditorComponent for
        // this code
    }
    /**
     * Grid event - called when the user clicks on the button that is displayed in the grid cell
     *
     *
     * @param Row[in]		The row of the grid cell
     * @param Col[in]		The column of the grid cell
     *
     * @return
     */
    public void onCellButtonClickGrid(int Row, int Col) {
        WizardRoleObject wro = getObjectAtGridRow(Row);
        if (wro != null) {
            IDesignPatternRole pRole = wro.getRole();
            JTree tree = m_Tree.getTree();
            TreePath path = tree.getPathForRow(Row);
            if (path != null) {
                Object obj = path.getLastPathComponent();
                if (obj instanceof JDefaultMutableTreeNode) {
                    JDefaultMutableTreeNode node = (JDefaultMutableTreeNode)obj;
                    addChildNodeForRole(node, pRole);
                    refreshTree(node, Row);
                }
            }
        }
    }
    private void refreshTree(JDefaultMutableTreeNode node, int row) {
        m_Tree.getTree().updateUI();
        m_Model.expand(row, true);
        if (node != null) {
            node.setExpanded(true);
            int childCount = node.getChildCount();
            m_Model.expand(row + childCount, true);
        }
    }
    /**
     * Called when a cell is entered in the grid - grid event
     */
    private void onEnterCell() {
        // Not needed in java
    }
    /**
     * Called when a key is pressed in the grid - grid event
     *
     * @param[in] KeyAscii   The key that was pressed
     * @param[in] Shift      Whether the Shift key was down
     *
     * @return VOID
     *
     */
    private void onKeyDownGrid(/*short FAR* KeyAscii, short Shift*/) {
                /* TODO
                        long row;
                  _VH(m_Grid->get_Row(&row));
                  // did they press the return key
                  if (*KeyAscii == VK_RETURN)
                  {
                         // will want to expand or collapse the node accordingly
                         CComPtr < IVSFlexNode > node;
                         _VH(m_Grid->GetNode(CComVariant(row), &node));
                         if (node)
                         {
                                CollapsedSettings is;
                                _VH(m_Grid->get_IsCollapsed(row, &is));
                                if (is == flexOutlineCollapsed)
                                {
                                   _VH(node->put_Expanded(TRUE));
                                }
                         }
                  }
                  // did they press the left arrow key
                  else if (*KeyAscii == VK_LEFT)
                  {
                         // yes, so collapse the grid node
                         CComPtr < IVSFlexNode > node;
                         _VH(m_Grid->GetNode(CComVariant(row), &node));
                         if (node)
                         {
                                _VH(node->put_Expanded(FALSE));
                         }
                  }
                  // did they press the right arrow key
                  else if (*KeyAscii == VK_RIGHT)
                  {
                         // yes, so expand the grid node
                         CComPtr < IVSFlexNode > node;
                         _VH(m_Grid->GetNode(CComVariant(row), &node));
                         if (node)
                         {
                                _VH(node->put_Expanded(TRUE));
                         }
                  }
                  // did they press the insert key
                  else if (*KeyAscii == VK_INSERT)
                  {
                                CComVariant pVar;
                                _VH(m_Grid->get_Cell(flexcpData, CComVariant(row), CComVariant(0), vtMissing, vtMissing, &pVar));
                                if (pVar.vt == VT_DISPATCH)
                                {
                                        // get the role object that is hidden on the cell
                                        CComQIPtr < IDesignPatternRole > pRole( (pVar.pdispVal) );
                                        if (pRole)
                                        {
                                                // check multiplicity of role
                                                long mult;
                                                _VH(pRole->get_Multiplicity(&mult));
                                                if (mult > 1)
                                                {
                                                        // role is a multiple
                                                        long parentRow;
                                                        m_Grid->GetNodeRow(row, flexNTRoot, &parentRow);
                                                        if ( parentRow > -1 )
                                                        {
                                                                // so add a child node for it
                                                                hr = AddChildNodeForRole(parentRow, pRole);
                                                        }
                                                }
                                        }
                                }
                  }
                  // did they press the delete key
                  else if (*KeyAscii == VK_DELETE)
                  {
                                hr = RemoveChildNodeForRole(row);
                  }
                  else
                  {
                  }
                 */
    }
    /**
     * Called when the create menu button is clicked
     *
     * @return
     */
    public void onPopupCreate(int curRow) {
        JDefaultMutableTreeNode node = getNodeAtGridRow(curRow);
        if (node != null) {
            // now get its parent
            JDefaultMutableTreeNode parentNode = (JDefaultMutableTreeNode)node.getParent();
            if (parentNode != null) {
                if (parentNode.getParent() == null) {
                    onCellButtonClickGrid(curRow, 1);
                } else {
                    int parentRow = parentNode.getRow();
                    onCellButtonClickGrid(parentRow-1, 1);
                }
            }
        }
        
        updateUI() ;
    }
    /**
     * Called when the delete menu button is clicked
     *
     * @return
     */
    public void onPopupDelete(int row) {
        removeChildNodeForRole(row);
        JTree tree = m_Tree.getTree();
        TreePath path = tree.getPathForRow(row);
        if (path != null) {
            Object obj = path.getLastPathComponent();
            if (obj instanceof JDefaultMutableTreeNode) {
                JDefaultMutableTreeNode node = (JDefaultMutableTreeNode)obj;
                refreshTree(node, row);
            }
        }
        
        updateUI() ;
    }
    /**
     * Called when the rename menu button is clicked
     *
     * @return
     */
    public void onPopupRename(int row) {
        m_Tree.editCellAt(row, 2);
    }
    
    /**
     * Called before the mouse click is registered with the grid - grid event
     *
     * @param[in] Button
     * @param[in] Shift
     * @param[in] X
     * @param[in] Y
     * @param Cancel
     *
     * @return VOID
     *
     */
    private void onBeforeMouseDownGrid(/*short Button, short Shift, float X, float Y, BOOL FAR* Cancel */) {
        // See show menu for c++ functionality
    }
    public ETList<String> showMenu(int row) {
        ETList<String> pStrs = new ETArrayList<String>();
        // figure out which menu to display
        // if on a single row, just display Rename
        // if on a row that can have multiple children, display create
        // if on a row that is a child of a multiple, display create, delete and rename
        boolean isMult = isMultipleParticipant(row);
        boolean isMultInst = isMultipleParticipantInstance(row);
        String createStr = DefaultDesignPatternResource.getString("IDS_CREATE");
        String delStr = DefaultDesignPatternResource.getString("IDS_DELETE");
        String renStr = DefaultDesignPatternResource.getString("IDS_RENAME");
        if (isMult) {
            pStrs.add(createStr);
        } else if (isMultInst) {
            pStrs.add(createStr);
            pStrs.add(delStr);
            //pStrs.add(renStr);
        } else {
            //pStrs.add(renStr);
        }
        return pStrs;
    }
    
    /**
     * Returns list of supported key events
     * @param row row number
     * @return ETList of supported key events.
     */
    public ETList<Integer> handleKeys(int row) {
        ETList<Integer> pKeys = new ETArrayList<Integer>();
        // figure out which key event should be handled
        // if on a row that can have multiple children, handle INSERT
        // if on a row that is a child of a multiple, handle INSERT and DELETE
        boolean isMult = isMultipleParticipant(row);
        boolean isMultInst = isMultipleParticipantInstance(row);
        if (isMult) {
            pKeys.add(new Integer(java.awt.event.KeyEvent.VK_INSERT));
        } else if (isMultInst) {
            pKeys.add(new Integer(java.awt.event.KeyEvent.VK_INSERT));
            pKeys.add(new Integer(java.awt.event.KeyEvent.VK_DELETE));
        }
        return pKeys;
    }
    
    /**
     *  Grid event when something in the combo list changes
     *
     *
     *
     * @return
     *
     */
    private void onChangeEditGrid() {
        // Handled by JRoleTextField and JRoleComboBox and getIDForElementNamed
    }
    public String getIDForElementNamed(String name, WizardRoleObject obj) {
        String str = "";
        if (obj != null) {
            IDesignPatternRole pRole = obj.getRole();
            if (pRole != null) {
                // need to loop through the TypeMap's name-to-id map
                // and find this value
                // if found, store the id of what was found in the hidden column
                // to be used later to populate the pattern details
                //
                String type = pRole.getTypeID();
                if (type == null || type.length() == 0) {
                    type = "Classifier";
                }
                Hashtable ht = m_TypeMap.get(type);
                if (ht != null) {
                    Object nameObj = ht.get(name);
                    if (nameObj != null) {
                        Vector names = (Vector)nameObj;
                        if (names != null && names.size() > 0) {
                            Object strObj = names.get(0);
                            if (strObj instanceof String) {
                                str = (String)strObj;
                            }
                        }
                    }
                }
            }
        }
        return str;
    }
    /**
     * Determines whether or not the role at the passed in role can have multiple
     * participants associated with it
     *
     *
     * @param row[in]			The row to process
     * @param bMult[out]		Whether or not the role can have multiple participants
     *
     * @return HRESULT
     *
     */
    private boolean isMultipleParticipant(int row) {
        boolean bMult = false;
        WizardRoleObject pObj = getObjectAtGridRow(row);
        if (pObj != null) {
            IDesignPatternRole pRole = pObj.getRole();
            if (pRole != null) {
                JDefaultMutableTreeNode pNode = getNodeAtGridRow(row);
                if (pNode != null) {
                    // trying to determine if we can add to this row or not, default is no
                    int mult = pRole.getMultiplicity();
                    if ( (mult > 1) && pNode.getParent().equals(m_Root)) {
                        bMult = true;
                    }
                }
            }
        }
        return bMult;
    }
    /**
     * Determines whether or not the role at the passed in role is a participant of a multiple
     * participant role
     *
     *
     * @param row[in]			The row to process
     * @param bMult[out]		Whether or not the role is a multiple participant
     *
     * @return HRESULT
     *
     */
    private boolean isMultipleParticipantInstance(int row) {
        boolean bMult = false;
        WizardRoleObject pObj = getObjectAtGridRow(row);
        if (pObj != null) {
            // if it is a role
            IDesignPatternRole pRole = pObj.getRole();
            if (pRole != null) {
                JDefaultMutableTreeNode pNode = getNodeAtGridRow(row);
                if (pNode != null) {
                    // trying to determine if we can add to this row or not, default is no
                    int mult = pRole.getMultiplicity();
                    if ( (mult > 1) && (!(pNode.getParent().equals(m_Root)))) {
                        bMult = true;
                    }
                }
            }
        }
        return bMult;
    }
    
    /**
     * Performs page validations -
     *
     *
     * @param errList[out]		An array of errors that occurred on the page
     *
     * @return HRESULT
     *
     */
    private ETList<String> validatePage() {
        ETList<String> tempList = new ETArrayList<String>();
        if (m_Wizard != null) {
            IDesignPatternDetails pDetails = m_Wizard.getDetails();
            if (pDetails != null) {
                IDesignPatternManager pManager = m_Wizard.getManager();
                if (pManager != null) {
                    pManager.setDetails(pDetails);
                    int hr = pManager.validatePattern(pDetails);
                    if (hr == -1) {
                    } else {
                        String msg = DesignPatternUtilities.translateString("IDS_MISSINGPARTICIPANT");
                        tempList.add(msg);
                    }
                }
            }
        }
        return tempList;
    }
    /**
     * Store the participant information from the GUI
     *
     * @return bool
     */
    private boolean storeParticipants() {
        boolean result = true;
        if (m_Wizard != null) {
            IDesignPatternDetails pDetails = m_Wizard.getDetails();
            if (pDetails != null) {
                pDetails.clearParticipantNames();
                // loop through the participant grid
                int count = m_Tree.getRowCount();
                for (int i=0; i<count; i++) {
                    // get the data from the cell on each row
                    WizardRoleObject pObj = getObjectAtGridRow(i);
                    if (pObj != null) {
                        IDesignPatternRole pRole = pObj.getRole();
                        if (pRole != null) {
                            // store the roleID and the participant name on the details
                            // for processing the apply pattern logic
                            String roleID = pRole.getID();
                            String part = pObj.getChosenName();
                            String id = pObj.getChosenID();
                            if (id != null && id.length() > 0) {
                                // if an id has been set, use it
                                // it would have been set by the user going into edit mode in a text field
                                // or picking something from the combo box
                                pDetails.addParticipantName(roleID, id);
                            } else {
                                if (part != null && part.length() > 0) {
                                    // do one more check to see if the name that is chosen exists in the system
                                    // because otherwise we were creating duplicate elements
                                    //buildRoleMap(pRole);
                                    //String name = getIDForElementNamed(part, pObj);
                                    //if (name != null && name.length() > 0)
                                    //{
                                    //	pDetails.addParticipantName(roleID, name);
                                    //}
                                    //else
                                    //{
                                    pDetails.addParticipantName(roleID, part);
                                    //}
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
    private void setPicture(IDesignPatternRole pRole, int row) {
        if (pRole != null) {
            // add the icon to the role
            String type = "PartFacade";
            String typeID = pRole.getTypeID();
            type += typeID;
            Icon hicon = m_ResourceMgr.getIconForElementType(type);
            if (hicon != null) {
                                /* TODO
                                CPictureHolder pic;
                                pic.CreateFromIcon(hicon, FALSE);
                                LPDISPATCH pPic = pic.GetPictureDispatch();
                                CComVariant vpic(pPic);
                                m_Grid->put_Cell(flexcpPicture, CComVariant(row), CComVariant(0), vtMissing, vtMissing, vpic);
                                pPic->Release();
                                ::DestroyIcon( hicon );
                                 */
            }
        }
    }
    public String buildRoleMap(IDesignPatternRole pRole) {
        String str = "";
        if (pRole != null) {
            String type = pRole.getTypeID();
            if (type == null || type.length() == 0) {
                type = "Classifier";
            }
            Hashtable ht = m_TypeMap.get(type);
            if (ht == null) {
                DesignPatternUtilities.startWaitCursor(m_Tree);
                buildRoleMaps(pRole);
                DesignPatternUtilities.endWaitCursor(m_Tree);
            }
            ht = m_TypeMap.get(type);
            if (ht != null) {
                Enumeration e = ht.keys();
                while (e.hasMoreElements()) {
                    Object obj = e.nextElement();
                    String s = (String)obj;
                    str += "|";
                    str += s;
                }
            }
        }
        return str;
    }
    /**
     * Shortcut method to retrieve the data from the grid cell which is in the form of a WizardRoleObject
     *
     * @param[in] row     The row to retrieve the property element from
     * @param[out] pEle   The found WizardRoleObject
     *
     * @return HRESULT
     *
     */
    public WizardRoleObject getObjectAtGridRow(int row) {
        WizardRoleObject retEle = null;
        JTree tree = m_Tree.getTree();
        TreePath path = tree.getPathForRow(row);
        if (path != null) {
            Object obj = path.getLastPathComponent();
            if (obj instanceof JDefaultMutableTreeNode) {
                JDefaultMutableTreeNode node = (JDefaultMutableTreeNode)obj;
                retEle = (WizardRoleObject)node.getUserObject();
            }
        }
        return retEle;
    }
    public JDefaultMutableTreeNode getNodeAtGridRow(int row) {
        JDefaultMutableTreeNode retNode = null;
        
        if (row >= 0 && m_Tree != null) {
            JTree tree = m_Tree.getTree();
            TreePath path = tree.getPathForRow(row);
            if (path != null) {
                Object obj = path.getLastPathComponent();
                if (obj instanceof JDefaultMutableTreeNode) {
                    retNode = (JDefaultMutableTreeNode)obj;
                }
            }
        }
        return retNode;
    }
    public void setRightClickRow(int newRow) {
        m_RightClickRow = newRow;
    }
    
}

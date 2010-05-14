/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.wsdlextensions.ldap;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.naming.NamingException;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.wsdlextensions.ldap.impl.LDAPTree;
import org.netbeans.modules.wsdlextensions.ldap.impl.ResultSetAttribute;
import org.netbeans.modules.wsdlextensions.ldap.impl.SearchFilterAttribute;
import org.netbeans.modules.wsdlextensions.ldap.impl.SelectedBackData;
import org.netbeans.modules.wsdlextensions.ldap.impl.UpdateSetAttribute;
import org.netbeans.modules.wsdlextensions.ldap.ldif.GenerateWSDL;
import org.netbeans.modules.wsdlextensions.ldap.ldif.GenerateXSD;
import org.netbeans.modules.wsdlextensions.ldap.ldif.LdifDataParser;
import org.netbeans.modules.wsdlextensions.ldap.ldif.LdifObjectClass;
import org.netbeans.modules.wsdlextensions.ldap.ldif.LdifParser;
import org.netbeans.modules.wsdlextensions.ldap.utils.LdapConnection;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

public final class LDAPServerBrowserVisualPanel3 extends JPanel {

    private static final ResourceBundle mMessages =
            ResourceBundle.getBundle("org.netbeans.modules.wsdlextensions.ldap.Bundle");
    private LDAPTree ldapTree = new LDAPTree();
    private LdapConnection conn;
    private String base = "";
    private String mFunction = "Search";
    private Map mObjectClassesMap = new HashMap();
    private HashMap mSelectedObjectMap = new HashMap();
    private Map mSelectedObjectMaps = new HashMap();
    private JTextField baseDN;
    private List mSelectedAttrList = new ArrayList();
    private List mResultSetAttrList = new ArrayList();
    private String mSelectedDN = "";
    private String mMainAttrInAdd = "";
    private String mServerType;
    List objectsList = null;
    private WSDLComponent mWSDLComponent = null;

    @SuppressWarnings("unchecked")
    private void clearData() {
        mObjectClassesMap.clear();
        mSelectedObjectMap.clear();
        mSelectedObjectMaps.clear();
        mSelectedAttrList.clear();
        mResultSetAttrList.clear();
        
        jListSearchFilterSeleted.removeAll();
        jListSearchFilterSeleted.setListData(new Vector());
        
        jListSearchResultSelected.removeAll();
        jListSearchResultSelected.setListData(new Vector());
        
        jListAddAttributeSelect.removeAll();
        jListAddAttributeSelect.setListData(new Vector());
        
        jListAddObjectClassSelect.removeAll();
        jListAddObjectClassSelect.setListData(new Vector());
        
        jListDeleteFilterSeleted.removeAll();
        jListDeleteFilterSeleted.setListData(new Vector());
        
        jListUpdateFilterSeleted.removeAll();
        jListUpdateFilterSeleted.setListData(new Vector());
        
        jListUpdateSetSelected.removeAll();
        jListUpdateSetSelected.setListData(new Vector());
        
        jListAddAttributeAvailable.removeAll();
        jListAddAttributeAvailable.setListData(new Vector());
        
        jListAddObjcecClassAvailable.removeAll();
        jListAddObjcecClassAvailable.setListData(new Vector());
        
        jListDeleteFilterAvailable.removeAll();
        jListDeleteFilterAvailable.setListData(new Vector());
        
        jListSearchFilterAvailable.removeAll();
        jListSearchFilterAvailable.setListData(new Vector());
        
        jListSearchResultAvailable.removeAll();
        jListSearchResultAvailable.setListData(new Vector());
        
        jListUpdateFilterAvailable.removeAll();
        jListUpdateFilterAvailable.setListData(new Vector());
        
        jListUpdateSetAvailable.removeAll();
        jListUpdateSetAvailable.setListData(new Vector());
        
        jComboBoxSortType.setSelectedIndex(0);
        jTextFieldRecordsPerPage.setText("0");
        jTextFieldSortByAttribute.setText("");
    }
    
    /** Creates new form LDAPServerBrowserVisualPanel3 */
    public LDAPServerBrowserVisualPanel3() {
        initComponents();
        initiate();
    }

    @Override
    public String getName() {
        return "Operation setting";
    }

    private void changeCompareOp(String compareOp) {
        JList jListSelected = jListSearchFilterSeleted;
        if (mFunction.equals("Search")) {
            jListSelected = jListSearchFilterSeleted;
        } else if (mFunction.equals("Update")) {
            jListSelected = jListUpdateFilterSeleted;
        } else {
            jListSelected = jListDeleteFilterSeleted;
        }
        Object[] selected = jListSelected.getSelectedValues();
        if (selected.length == 0) {
            JOptionPane.showMessageDialog(null, "Please select a item", "Message", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (selected.length > 1) {
            JOptionPane.showMessageDialog(null, "Only one item can be selected", "Message", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String str = DisplayFormatControl.toAttribute((String) selected[0]);
        int index = str.indexOf(".");
        String obj = str.substring(0, index);
        String att = str.substring(index + 1);
        //update mSelectedAttrList
        for (int j = 0; j < mSelectedAttrList.size(); j++) {
            SearchFilterAttribute sfa = (SearchFilterAttribute) mSelectedAttrList.get(j);
            if (sfa.getObjName().equals(obj) & sfa.getAttributeName().equals(att)) {
                sfa.setCompareOp(compareOp);
            }
            sfa = null;
        }
        redisplaySearchFilter(obj);
        obj = null;
        att = null;
        jListSelected = null;
    }

    @SuppressWarnings("unchecked")
    private void redisplaySearchFilter(String comboboxItemName) {
        JComboBox jCombobox = null;
        JList jListAvailable = null;
        JList jListSelected = null;
        if (mFunction.equals("Search")) {
            jCombobox = jComboBoxSearchFilter;
            jListAvailable = jListSearchFilterAvailable;
            jListSelected = jListSearchFilterSeleted;
        } else if (mFunction.equals("Update")) {
            jCombobox = jComboBoxUpdateFilter;
            jListAvailable = jListUpdateFilterAvailable;
            jListSelected = jListUpdateFilterSeleted;
        } else {
            jCombobox = jComboBoxDeleteFilter;
            jListAvailable = jListDeleteFilterAvailable;
            jListSelected = jListDeleteFilterSeleted;
        }
        jCombobox.getModel().setSelectedItem(comboboxItemName);
        List listAvailable = getUnselectedAttribute(comboboxItemName);
        List jListData = DisplayFormatControl.filterListToJList(mSelectedAttrList);

        jListAvailable.removeAll();
        jListAvailable.setListData(new Vector(listAvailable));

        jListSelected.removeAll();
        jListSelected.setListData(new Vector(jListData));

    }

    private void initiatePopupMenu() {
        JMenuItem jMenuitem1 = new JMenuItem("=");
        JMenuItem jMenuitem3 = new JMenuItem(">=");
        JMenuItem jMenuitem4 = new JMenuItem("<=");
        JMenuItem jMenuitem7 = new JMenuItem("add()");
        JMenuItem jMenuitem8 = new JMenuItem("remove()");

        jMenuitem1.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                changeCompareOp("=");
            }
        });

        jMenuitem3.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                changeCompareOp(">=");
            }
        });

        jMenuitem4.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                changeCompareOp("<=");
            }
        });

        jMenuitem7.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                addBracket();
            }
        });

        jMenuitem8.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                removeBracket();
            }
        });

        jPopupMenu.add(jMenuitem1);
        jPopupMenu.add(jMenuitem3);
        jPopupMenu.add(jMenuitem4);
        jPopupMenu.add(jMenuitem7);
        jPopupMenu.add(jMenuitem8);

        JMenuItem jMenuitemAdd = new JMenuItem("change to Add");
        JMenuItem jMenuitemReplace = new JMenuItem("change to Replace");
        JMenuItem jMenuitemRemove = new JMenuItem("change to Remove");
        JMenuItem jMenuitemRemoveAll = new JMenuItem("change to RemoveAll");

        jMenuitemAdd.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                changeUpdateSetOptype("Add");
            }
        });
        jMenuitemReplace.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                changeUpdateSetOptype("Replace");
            }
        });
        jMenuitemRemove.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                changeUpdateSetOptype("Remove");
            }
        });
        jMenuitemRemoveAll.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                changeUpdateSetOptype("RemoveAll");
            }
        });
        jPopupMenu1.add(jMenuitemAdd);
        jPopupMenu1.add(jMenuitemReplace);
        jPopupMenu1.add(jMenuitemRemove);
//        jPopupMenu1.add(jMenuitemRemoveAll);

        JMenuItem jMenuitemSetToMain = new JMenuItem("set to RDN");
        jMenuitemSetToMain.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setToMainAttr();
            }
        });
        jPopupMenu2.add(jMenuitemSetToMain);
    }

    private void initiate() {
        jButtonAddObjectSelect.setEnabled(false);
        jButtonAddObjectUnselect.setEnabled(false);
        jButtonAddAttributeSelect.setEnabled(false);
        jButtonAddAttributeUnselect.setEnabled(false);
        jButtonSearchFilterSelectAnd.setEnabled(false);
        jButtonSearchFilterSelectOr.setEnabled(false);
        jButtonSearchFilterUnselect.setEnabled(false);
        jButtonResultSetSelect.setEnabled(false);
        jButtonResultSetUnselect.setEnabled(false);    
        jButtonDeleteFilterSelectAnd.setEnabled(false);
        jButtonDeleteFilterSelectOr.setEnabled(false);
        jButtonDeleteFilterUnselect.setEnabled(false);
        jButtonUpdateFilterSelectAnd.setEnabled(false);
        jButtonUpdateFilterSelectOr.setEnabled(false);
        jButtonUpdateFilterUnselect.setEnabled(false);
        jButtonUpdateSetSelect.setEnabled(false);
        jButtonUpdateSetUnselect.setEnabled(false);      
        initiatePopupMenu();
        jListSearchFilterSeleted.addMouseListener(new MouseAdapter() {

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() && e.getClickCount() == 1) {
                    showPopupMenu(e);
                }
            }

            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() && e.getClickCount() == 1) {
                    showPopupMenu(e);
                }
            }
        });

        jListUpdateFilterSeleted.addMouseListener(new MouseAdapter() {

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() && e.getClickCount() == 1) {
                    showPopupMenu(e);
                }
            }

            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() && e.getClickCount() == 1) {
                    showPopupMenu(e);
                }
            }
        });

        jListUpdateSetSelected.addMouseListener(new MouseAdapter() {

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() && e.getClickCount() == 1) {
                    showPopupMenu1(e);
                }
            }

            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() && e.getClickCount() == 1) {
                    showPopupMenu1(e);
                }
            }
        });

        jListAddAttributeSelect.addMouseListener(new MouseAdapter() {

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() && e.getClickCount() == 1) {
                    showPopupMenu2(e);
                }
            }

            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() && e.getClickCount() == 1) {
                    showPopupMenu2(e);
                }
            }
        });

        jListDeleteFilterSeleted.addMouseListener(new MouseAdapter() {

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() && e.getClickCount() == 1) {
                    showPopupMenu(e);
                }
            }

            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() && e.getClickCount() == 1) {
                    showPopupMenu(e);
                }
            }
        });


    }

    private void changeUpdateSetOptype(String opType) {
        Object[] selected = jListUpdateSetSelected.getSelectedValues();
        if (selected.length < 1) {
            return;
        }
//        if (selected.length > 1) {
//            JOptionPane.showMessageDialog(null, "Please selecte only one item", "Message", JOptionPane.INFORMATION_MESSAGE);
//            return;
//        }
        for (int k = 0; k < selected.length; k++) {
            String str = DisplayFormatControl.jListUpdateSettoAttr((String) selected[k]);
            int index = str.indexOf(".");
            String objName = str.substring(0, index);
            String attName = str.substring(index + 1);
            LdifObjectClass obj = (LdifObjectClass) mSelectedObjectMap.get(objName);
            if (obj != null) {
                List results = obj.getResultSet();
                if (results != null) {
                    for (int j = 0; j < results.size(); j++) {
                        UpdateSetAttribute usa = (UpdateSetAttribute) results.get(j);
                        if (usa.getAttrName().equals(attName)) {
                            usa.setOpType(opType);
                        }
                    }
                }
                results = null;
            }
        }
        refreshUpdateSetSelectList();
    }

    /*
     * add Bracket to the selected Items in JList;
     * */
    private void addBracket() {
        JList jListSelected = jListSearchFilterSeleted;
        if (mFunction.equals("Search")) {
            jListSelected = jListSearchFilterSeleted;
        } else if (mFunction.equals("Update")) {
            jListSelected = jListUpdateFilterSeleted;
        } else {
            jListSelected = jListDeleteFilterSeleted;
        }
        Object[] selected = jListSelected.getSelectedValues();
        if (selected.length < 2) {
            JOptionPane.showMessageDialog(null, "Please select more than one item!", "Message", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int beginIndex = jListSelected.getMinSelectionIndex();
        int endIndex = jListSelected.getMaxSelectionIndex();
        SearchFilterAttribute sfaBegin = (SearchFilterAttribute) mSelectedAttrList.get(beginIndex);
        SearchFilterAttribute sfaEnd = (SearchFilterAttribute) mSelectedAttrList.get(endIndex);

        if (selected.length != endIndex - beginIndex + 1) {
            JOptionPane.showMessageDialog(null, "Not correctly selected!", "Message", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (sfaBegin.isBeginBracket() & sfaEnd.isEndBracket()) {
            JOptionPane.showMessageDialog(null, "Bracket already exists!", "Message", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        boolean canAddBracket = true;
        int beginBracketDepth = 0;
        int endBracketDepth = 0;
        for (int i = beginIndex; i <= endIndex; i++) {
            SearchFilterAttribute sfa = (SearchFilterAttribute) mSelectedAttrList.get(i);
            beginBracketDepth += sfa.getBracketBeginDepth();
            endBracketDepth += sfa.getBracketEndDepth();
            if (beginBracketDepth < endBracketDepth) {
                canAddBracket = false;
                break;
            }
            sfa = null;
        }
        if (beginBracketDepth != endBracketDepth) {
            canAddBracket = false;
        }

        if (!canAddBracket) {
            JOptionPane.showMessageDialog(null, "Can't add Bracket to selected items!", "Message", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        sfaBegin.increaseBracketBeginDepth();
        sfaEnd.increaseBracketEndDepth();
        sfaBegin = null;
        sfaEnd = null;
        String finalObjName = "";
        for (int i = 0; i < selected.length; i++) {
            String str = DisplayFormatControl.toAttribute((String) selected[i]);
            int index = str.indexOf(".");
            String obj = str.substring(0, index);
            String att = str.substring(index + 1);
            finalObjName = obj;
            //increaseBracketDepth from mSelectedAttrList
            for (int j = 0; j < mSelectedAttrList.size(); j++) {
                SearchFilterAttribute sfa = (SearchFilterAttribute) mSelectedAttrList.get(j);
                if (sfa.getObjName().equals(obj) & sfa.getAttributeName().equals(att)) {
                    sfa.increaseBracketDepth();
                }
                sfa = null;
            }
            obj = null;
            att = null;
        }

        jListSelected = null;
        redisplaySearchFilter(finalObjName);
    }

    private void removeBracket() {
        JList jListSelected = jListSearchFilterSeleted;
        if (mFunction.equals("Search")) {
            jListSelected = jListSearchFilterSeleted;
        } else if (mFunction.equals("Update")) {
            jListSelected = jListUpdateFilterSeleted;
        } else {
            jListSelected = jListDeleteFilterSeleted;
        }
        Object[] selected = jListSelected.getSelectedValues();
        if (selected.length < 2) {
            JOptionPane.showMessageDialog(null, "Please select more than one item!", "Message", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int beginIndex = jListSelected.getMinSelectionIndex();
        int endIndex = jListSelected.getMaxSelectionIndex();
        SearchFilterAttribute sfaBegin = (SearchFilterAttribute) mSelectedAttrList.get(beginIndex);
        SearchFilterAttribute sfaEnd = (SearchFilterAttribute) mSelectedAttrList.get(endIndex);


        if (!sfaBegin.isBeginBracket() | !sfaEnd.isEndBracket()) {
            JOptionPane.showMessageDialog(null, "Selected items do not have bracket!", "Message", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        sfaBegin.reduceBracketBeginDepth();
        sfaEnd.reduceBracketEndDepth();
        sfaBegin = null;
        sfaEnd = null;

        String finalObjName = "";
        for (int i = 0; i < selected.length; i++) {
            String str = DisplayFormatControl.toAttribute((String) selected[i]);
            int index = str.indexOf(".");
            String obj = str.substring(0, index);
            String att = str.substring(index + 1);
            finalObjName = obj;
            //reduceBracketDepth from mSelectedAttrList
            for (int j = 0; j < mSelectedAttrList.size(); j++) {
                SearchFilterAttribute sfa = (SearchFilterAttribute) mSelectedAttrList.get(j);
                if (sfa.getObjName().equals(obj) & sfa.getAttributeName().equals(att) & sfa.getBracketDepth() > 0) {
                    sfa.reduceBracketDepth();
                }
                sfa = null;
            }
            obj = null;
            att = null;
        }
        jListSelected = null;
        redisplaySearchFilter(finalObjName);
    }

    private void showPopupMenu(MouseEvent e) {
        jPopupMenu.show(e.getComponent(), e.getX(), e.getY());
    }

    private void showPopupMenu1(MouseEvent e) {
        jPopupMenu1.show(e.getComponent(), e.getX(), e.getY());
    }

    private void showPopupMenu2(MouseEvent e) {
        jPopupMenu2.show(e.getComponent(), e.getX(), e.getY());
    }

    private DefaultTreeModel getDefaultTreeModelFromServer() {
        base = conn.getDn();
        ldapTree.initiate(conn);
        return ldapTree.getTreeModel();
    }

    private DefaultTreeModel getDefaultTreeModelFromFiles(String fileName, String rootDN) {
        LdifDataParser ldp = new LdifDataParser(fileName);
        List data = ldp.parse();
        return ldapTree.getTreeModel(data, rootDN);
    }

    @SuppressWarnings("unchecked")
    public void read(WizardDescriptor wd) {
        conn = (LdapConnection) wd.getProperty("LDAP_CONNECTION");
        if (!conn.isConnectionReconnect()) {
            return;
        }
        clearData();
        mServerType = (String) wd.getProperty("LDAP_DATA_FROM_TYPE");
        DefaultTreeModel treeModel = null;

        if ("FROM_SERVER".equals(mServerType)) {
            treeModel = getDefaultTreeModelFromServer();
            if (conn.getConnection() == null) {
                JOptionPane.showMessageDialog(null, "Can not connect to server\n" + conn.toString(), "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                objectsList = conn.getObjectNames();
                Iterator it = objectsList.iterator();
                while (it.hasNext()) {
                    String item = (String) it.next();
                    mObjectClassesMap.put(item, conn.getObjectClass(item));
                }
            } catch (NamingException ex) {
                ex.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if ("FROM_FILES".equals(mServerType)) {
            String fileName = (String) wd.getProperty("LDAP_DATA_FILE_PATH");
            String schemaFileName = (String) wd.getProperty("LDAP_SCHEMA_FILE_PATH");
            base = (String) wd.getProperty("LDAP_ROOT_DN");
            treeModel = getDefaultTreeModelFromFiles(fileName, base);

            LdifParser lp = new LdifParser(new File(schemaFileName));
            try {
                objectsList = new ArrayList();
                List data = lp.parse();
                Iterator it = data.iterator();
                while (it.hasNext()) {
                    LdifObjectClass loc = (LdifObjectClass) it.next();
                    objectsList.add(loc.getName());
                    mObjectClassesMap.put(loc.getName(), loc);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        jTreeSearch.setModel(treeModel);
        jTreeSearch.invalidate();
        jTextFieldSearchBaseDN.setText(base);

        jTreeAdd.setModel(treeModel);
        jTreeAdd.invalidate();
        jTextFieldAddBaseDN.setText(base);

        jTreeUpdate.setModel(treeModel);
        jTreeUpdate.invalidate();
        jTextFieldUpdateBaseDN.setText(base);

        jTreeDelete.setModel(treeModel);
        jTreeDelete.invalidate();
        jTextFieldDeleteBaseDN.setText(base);

        if (objectsList != null) {
            readObjectClasses(objectsList);
            initiateAdd(objectsList);
        }
        baseDN = jTextFieldSearchBaseDN;
        
        conn.setConnectionReconnect(false);
    }

    public void closeConnection() {
        if (null != conn) {
            conn.closeConnection();
        }
    }

    @SuppressWarnings("unchecked")
    public void store(WizardDescriptor wd) {
//        LdapConnection con = (LdapConnection) wd.getProperty("LDAP_CONNECTION");
        String fileName = (String) wd.getProperty("FILE_NAME");
        // take the name from wsdl wizard instead if entry point is from WSDL Wizard
        if (mWSDLComponent != null) {
            fileName = (String) wd.getProperty("WSDL_DEFINITION_NAME");            
        }
        
        if (mSelectedObjectMap.keySet().size() > 0) {
            HashMap selectedObject = copyMSelectedObjectMap();
            String basedn = baseDN.getText();
            SelectedBackData data = new SelectedBackData(selectedObject, mFunction, basedn, mMainAttrInAdd);
            data.setMResultSetAttrList(copyMResultSetAttrList());
            data.setMSelectedAttrList(copyMSelectedAttrList());
            mSelectedObjectMaps.put(new String(mFunction), data);
        }
        if (mSelectedObjectMaps.keySet().size() == 0) {
            return;
        }
        Iterator it = mSelectedObjectMaps.keySet().iterator();
        try {
            Project project = (Project) wd.getProperty("project");
            String destinationDir = FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath() + 
                    File.separator + "src" + File.separator + "ldapwsdls";
            if (mWSDLComponent != null) {
                destinationDir = (String) wd.getProperty("TARGETFOLDER_PATH");
            }
            File dir = new File(destinationDir);
            dir.mkdirs();
            while (it.hasNext()) {
                String function = (String) it.next();
                SelectedBackData backupData = (SelectedBackData) mSelectedObjectMaps.get(new String(function));
                GenerateXSD genXsd = new GenerateXSD(dir, backupData.getMSelectedObjectMap(),
                        function, fileName, backupData.getMBaseDN(), backupData.getMMainAttrInAdd(),
                        conn, jTextFieldRecordsPerPage.getText(), mServerType,
                        jTextFieldSortByAttribute.getText().trim(), jComboBoxSortType.getSelectedItem().toString());
                genXsd.generate(mWSDLComponent);

//                GenerateWSDL genWsdl = new GenerateWSDL(dir, backupData.getMSelectedObjectMaps(), function, fileName, con);
//                genWsdl.generate();
            }
//            GenerateXSD genXsd = new GenerateXSD(dir, mSelectedObjectMap,
//                    mFunction, fileName, base, mMainAttrInAdd, conn);
//            genXsd.generate();
            GenerateWSDL genWsdl = new GenerateWSDL(dir, mSelectedObjectMaps, fileName, conn);
            File generatedTmpWsdlFile = genWsdl.generate(mWSDLComponent);
            
            if (wd.getProperty("TEMP_WSDLMODEL") != null) {
                WSDLModel ldapWSDLModel = prepareModelFromFile(generatedTmpWsdlFile, null, null);
                if (wd != null) {
                    wd.putProperty("TEMP_WSDLMODEL", ldapWSDLModel);            
                }
            }
            
            project.getProjectDirectory().refresh();
//            conn.closeConnection();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            //this.mObjectClassesMap = null;
        }
    }

    @SuppressWarnings("unchecked")
    public void readObjectClasses(List list) {
        if (list.size() < 1) {
            return;
        }
        SortedComboboxModel searchComboboxModel = new SortedComboboxModel();
        SortedComboboxModel updateComboboxModel = new SortedComboboxModel();
        SortedComboboxModel deleteComboboxModel = new SortedComboboxModel();
        for (int i = 0; i < list.size(); i++) {
            String item = (String) list.get(i);
            searchComboboxModel.addElement(item);
            updateComboboxModel.addElement(item);
            deleteComboboxModel.addElement(item);
        }
        jComboBoxSearchFilter.setModel(searchComboboxModel);
        jComboBoxResultSet.setModel(searchComboboxModel);
        jComboBoxSearchFilter.setSelectedIndex(0);
        jComboBoxResultSet.setSelectedIndex(0);

        jComboBoxUpdateFilter.setModel(updateComboboxModel);
        jComboBoxUpdateSet.setModel(updateComboboxModel);
        jComboBoxUpdateFilter.setSelectedIndex(0);
        jComboBoxUpdateSet.setSelectedIndex(0);

        jComboBoxDeleteFilter.setModel(deleteComboboxModel);
        jComboBoxDeleteFilter.setSelectedIndex(0);
    }

    @SuppressWarnings("unchecked")
    public List getUnselectedAttribute(String objName) {
        List ret = new ArrayList();
        LdifObjectClass obj = (LdifObjectClass) mObjectClassesMap.get(objName);
        List must = obj.getMust();
        List may = obj.getMay();
        List selected = DisplayFormatControl.filterToAttr(obj.getSelected());
        if (null != must & must.size() > 0) {
            ret.addAll(must);
        }
        if (null != may & may.size() > 0) {
            ret.addAll(may);
        }
        if (null != selected & ret.size() > 0 & ret.size() > 0) {
            ret.removeAll(selected);
        }
        must = null;
        may = null;
        selected = null;
        return ret;
    }

    @SuppressWarnings("unchecked")
    public void filterAdd(String logicOp) {
        JComboBox jCombobox = null;
        JList jListAvailable = null;
        JList jListSelected = null;
        if (mFunction.equals("Search")) {
            jCombobox = jComboBoxSearchFilter;
            jListAvailable = jListSearchFilterAvailable;
            jListSelected = jListSearchFilterSeleted;
        } else if (mFunction.equals("Update")) {
            jCombobox = jComboBoxUpdateFilter;
            jListAvailable = jListUpdateFilterAvailable;
            jListSelected = jListUpdateFilterSeleted;
        } else {
            jCombobox = jComboBoxDeleteFilter;
            jListAvailable = jListDeleteFilterAvailable;
            jListSelected = jListDeleteFilterSeleted;
        }
        Object[] selected = jListAvailable.getSelectedValues();
        if (selected.length == 0) {
            return;
        }
        String objName = jCombobox.getSelectedItem().toString();
        LdifObjectClass obj;
        if (null == mSelectedObjectMap.get(objName)) {
            mSelectedObjectMap.put(objName, (LdifObjectClass) mObjectClassesMap.get(objName));
//            mObjectClassesMap.remove(objName);
        }
        obj = (LdifObjectClass) mSelectedObjectMap.get(objName);
        int existSelectedLength = jListSelected.getModel().getSize();
        for (int i = 0; i < selected.length; i++) {
            int posIndex = existSelectedLength + i;
            SearchFilterAttribute sfa = DisplayFormatControl.attrToFilter((String) selected[i], logicOp, objName, posIndex);
            mSelectedAttrList.add(sfa);
            obj.addSelected(sfa);
        }
        redisplaySearchFilter(objName);
    }

    public String treePathToDN(TreePath treePath) {
        String str = treePath.toString();
        String ret = "";
        String str1;
        int pathLenth = str.length();
        int baseLenth = base.length();
        if (pathLenth == baseLenth + 2) {
            return base;
        }
        str1 = str.substring(baseLenth + 2, pathLenth - 1);
        String[] pathArray = str1.split(",");
        int i = pathArray.length - 1;
        do {
            ret += pathArray[i] + ",";
            i--;
        } while (i >= 0);
        ret += base;
        ret = ret.replaceAll(" ", "");
        return ret;
    }

    /**
     * Load and initialize the WSDL model from the given file, which should
     * already have a minimal WSDL definition. The preparation includes
     * setting the definition name, adding a namespace and prefix, and
     * adding the types component.
     *
     * @param  file  the file with a minimal WSDL definition.
     * @return  the model.
     */
    public static WSDLModel prepareModelFromFile(File file, String definitionName,
            String targetNameSpace) {
        File f = FileUtil.normalizeFile(file);
        FileObject fobj = FileUtil.toFileObject(f);
        ModelSource modelSource = org.netbeans.modules.xml.retriever.
                catalog.Utilities.getModelSource(fobj, fobj.canWrite());
        WSDLModel model = WSDLModelFactory.getDefault().getModel(modelSource);
        if (model.getState() == WSDLModel.State.VALID) {
            model.startTransaction();
            if (definitionName != null) {
                model.getDefinitions().setName(definitionName);
            }
            if (targetNameSpace != null) {
                model.getDefinitions().setTargetNamespace(targetNameSpace);
                ((AbstractDocumentComponent) model.getDefinitions()).addPrefix("tns", targetNameSpace);
            }
            if (model.getDefinitions().getTypes() == null) {
                model.getDefinitions().setTypes(model.getFactory().createTypes());
            }
            model.endTransaction();
        } else {
            assert false : "Model is invalid, correct the template if any";
        }
        return model;
    }    
    
    private void unSelectFilter() {
        JList jListSelected = jListSearchFilterSeleted;
        if (mFunction.equals("Search")) {
            jListSelected = jListSearchFilterSeleted;
        } else if (mFunction.equals("Update")) {
            jListSelected = jListUpdateFilterSeleted;
        } else {
            jListSelected = jListDeleteFilterSeleted;
        }
        Object[] selected = jListSelected.getSelectedValues();
        if (selected.length == 0) {
            return;
        }
        String finalObjName = "";
        for (int i = 0; i < selected.length; i++) {
            String str = DisplayFormatControl.toAttribute((String) selected[i]);
            int index = str.indexOf(".");
            String obj = str.substring(0, index);
            String att = str.substring(index + 1);
            finalObjName = obj;
            //remove from mSelectedAttrList
            for (int j = 0; j < mSelectedAttrList.size(); j++) {
                SearchFilterAttribute sfa = (SearchFilterAttribute) mSelectedAttrList.get(j);
                if (sfa.getObjName().equals(obj) & sfa.getAttributeName().equals(att)) {
                    mSelectedAttrList.remove(j);
                }
            }
            //remove from mSelectedObjectMap;
            LdifObjectClass loc = (LdifObjectClass) mSelectedObjectMap.get(obj);
            loc.removeSelected(att);
            if (!loc.isSelected()) {
                mSelectedObjectMap.remove(obj);
            }
        }

        //reset posIndex;
        for (int i = 0; i < mSelectedAttrList.size(); i++) {
            SearchFilterAttribute sfa2 = (SearchFilterAttribute) mSelectedAttrList.get(i);
            sfa2.setPositionIndex(i);
        }
        jListSelected = null;
        redisplaySearchFilter(finalObjName);
    }

    public void setWSDLComponent(WSDLComponent wsdlComponent) {
        mWSDLComponent = wsdlComponent;
    }   
    
    @SuppressWarnings("unchecked")
    private void clearSelect() {
        Iterator it = mSelectedObjectMap.values().iterator();
        while (it.hasNext()) {
            LdifObjectClass loc = (LdifObjectClass) it.next();
            loc.clearSelect();
        }
        mSelectedAttrList.clear();
        mSelectedObjectMap.clear();
        mResultSetAttrList.clear();
//        JComboBox jCombobox = null;
//        JList jListSelected = null;
//        JList jListSelectedResult = null;
//        if (mFunction.equals("Search")) {
//            jCombobox = jComboBoxSearchFilter;
//            jListSelected = jListSearchFilterSeleted;
//            jListSelectedResult = jListSearchResultSelected;
//        } else if (mFunction.equals("Update")) {
//            jCombobox = jComboBoxUpdateFilter;
//            jListSelected = jListUpdateFilterSeleted;
//            jListSelectedResult = jListUpdateSetSelected;
//        } else {
//            jCombobox = jComboBoxDeleteFilter;
//            jListSelected = jListDeleteFilterAvailable;
//            jListSelectedResult = jListDeleteFilterSeleted;
//        }
//        jListSelected.removeAll();
//        jListSelected.setListData(new Vector(new ArrayList()));
//        jListSelectedResult.removeAll();
//        jListSelectedResult.setListData(new Vector(new ArrayList()));
//        if (jCombobox.getItemCount() > 0) {
//            jCombobox.setSelectedIndex(0);
//        }
    }

    @SuppressWarnings("unchecked")
    private void refreshUpdateSetSelectList() {
        mResultSetAttrList.clear();
        Iterator it = mSelectedObjectMap.values().iterator();
        while (it.hasNext()) {
            LdifObjectClass loc = (LdifObjectClass) it.next();
            List resultSet = loc.getResultSet();
            if (resultSet != null) {
                Iterator it2 = resultSet.iterator();
                while (it2.hasNext()) {
                    UpdateSetAttribute usa2 = (UpdateSetAttribute) it2.next();
                    mResultSetAttrList.add(new String(usa2.getOpType() + " " + usa2.getObjName() + "." + usa2.getAttrName()));
                }
            }
        }
        jListUpdateSetSelected.removeAll();
        jListUpdateSetSelected.setListData(new Vector(mResultSetAttrList));
    }

//add operation code begin
    @SuppressWarnings("unchecked")
    private void initiateAdd(List list) {
        if (list.size() < 1) {
            return;
        }
        SortedListModel listModel = new SortedListModel();
        for (int i = 0; i < list.size(); i++) {
            String item = (String) list.get(i);
            listModel.addElement(item);
        }
        jListAddObjcecClassAvailable.removeAll();
        jListAddObjcecClassAvailable.setListData(new Vector(listModel.getElements()));
        jListAddObjcecClassAvailable.setSelectedIndex(0);
        jListAddObjectClassSelect.removeAll();
        jListAddObjectClassSelect.setListData(new Vector());
    }

    @SuppressWarnings("unchecked")
    private void refreshAddTabSelectedAttrsList() {
        mResultSetAttrList.clear();
        Iterator it = mSelectedObjectMap.values().iterator();
        while (it.hasNext()) {
            LdifObjectClass loc = (LdifObjectClass) it.next();
            mResultSetAttrList.addAll(DisplayFormatControl.attrToJListAddSeletectAttr(loc.getName(), loc.getResultSet()));
        }
        jListAddAttributeSelect.removeAll();
        jListAddAttributeSelect.setListData(new Vector(mResultSetAttrList));
    }

    @SuppressWarnings("unchecked")
    private void refreshAddTabAttributeAvailableList(String objName) {
        LdifObjectClass obj = null;
        List ret = new ArrayList();
        List must;
        List may;
        List selectedAttrs;
        obj = (LdifObjectClass) mObjectClassesMap.get(objName);
        selectedAttrs = obj.getResultSet();
        must = obj.getMust();
        may = obj.getMay();
        if (null != must & must.size() > 0) {
            ret.addAll(must);
        }

        if (null != may & may.size() > 0) {
            ret.addAll(may);
        }
        if (null != selectedAttrs & selectedAttrs.size() > 0) {
            ret.removeAll(selectedAttrs);
        }
        jListAddAttributeAvailable.removeAll();
        jListAddAttributeAvailable.setListData(new Vector(ret));
    }

    private void setToMainAttr() {
        Object[] selected = jListAddAttributeSelect.getSelectedValues();
        if (selected.length != 1) {
            JOptionPane.showMessageDialog(null, "Please select an item!", "Message", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String str = (String) selected[0];
        mMainAttrInAdd = str;
//        mSelectedObjectMap.put("MainAttribute", str);
    }

    @SuppressWarnings("unchecked")
    private HashMap copyMSelectedObjectMap() {
        HashMap ret = new HashMap();
        Iterator it = mSelectedObjectMap.keySet().iterator();
        while (it.hasNext()) {
            try {
                String objName = (String) it.next();
                LdifObjectClass loc = (LdifObjectClass) ((LdifObjectClass) mSelectedObjectMap.get(new String(objName))).clone();
                ret.put(objName, loc);
            } catch (CloneNotSupportedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    private List copyMSelectedAttrList() {
        List ret = new ArrayList();
        if (mSelectedAttrList.size() > 0) {
            for (int i = 0; i < mSelectedAttrList.size(); i++) {
                ret.add(mSelectedAttrList.get(i));
            }
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    private List copyMResultSetAttrList() {
        List ret = new ArrayList();
        if (mResultSetAttrList.size() > 0) {
            for (int i = 0; i < mResultSetAttrList.size(); i++) {
                ret.add(mResultSetAttrList.get(i));
            }
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    private void getBackupData() {
        if (mSelectedObjectMaps.containsKey(new String(mFunction))) {
            SelectedBackData data = (SelectedBackData) mSelectedObjectMaps.get(new String(mFunction));
            mSelectedObjectMap = data.getMSelectedObjectMap();
            mSelectedAttrList = data.getMSelectedAttrList();
            mResultSetAttrList = data.getMResultSetAttrList();
            Iterator it = mSelectedObjectMap.keySet().iterator();
            String key = "";
            while (it.hasNext()) {
                key = (String) it.next();
                LdifObjectClass loc = (LdifObjectClass) mSelectedObjectMap.get(new String(key));
                mObjectClassesMap.put(new String(key), loc);
            }
//            refreshSearchFilter(key);
//            jListSearchResultSelected.removeAll();
//            jListSearchResultSelected.setListData(new Vector(mResultSetAttrList));
        }
    }

    @SuppressWarnings("unchecked")
    private List copyObjectsList() {
        List ret = new ArrayList();
        Iterator it = objectsList.iterator();
        while (it.hasNext()) {
            String obj = (String) it.next();
            ret.add(obj);
        }
        return ret;
    }

//add operation code end.
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu = new javax.swing.JPopupMenu();
        jPopupMenu1 = new javax.swing.JPopupMenu();
        jPopupMenu2 = new javax.swing.JPopupMenu();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabelSearchBaseDN = new javax.swing.JLabel();
        jTextFieldSearchBaseDN = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        jComboBoxSearchFilter = new javax.swing.JComboBox();
        jScrollPane2 = new javax.swing.JScrollPane();
        jListSearchFilterAvailable = new javax.swing.JList();
        jButtonSearchFilterSelectAnd = new javax.swing.JButton();
        jButtonSearchFilterSelectOr = new javax.swing.JButton();
        jButtonSearchFilterUnselect = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jListSearchFilterSeleted = new javax.swing.JList();
        jPanel7 = new javax.swing.JPanel();
        jComboBoxResultSet = new javax.swing.JComboBox();
        jScrollPane4 = new javax.swing.JScrollPane();
        jListSearchResultAvailable = new javax.swing.JList();
        jButtonResultSetSelect = new javax.swing.JButton();
        jButtonResultSetUnselect = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        jListSearchResultSelected = new javax.swing.JList();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTreeSearch = new javax.swing.JTree();
        jLabelRecordsPerPage = new javax.swing.JLabel();
        jTextFieldRecordsPerPage = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldSortByAttribute = new javax.swing.JTextField();
        jComboBoxSortType = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();
        jLabelAddBaseDN = new javax.swing.JLabel();
        jTextFieldAddBaseDN = new javax.swing.JTextField();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTreeAdd = new javax.swing.JTree();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        jListAddObjcecClassAvailable = new javax.swing.JList();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane8 = new javax.swing.JScrollPane();
        jListAddAttributeAvailable = new javax.swing.JList();
        jButtonAddAttributeUnselect = new javax.swing.JButton();
        jButtonAddAttributeSelect = new javax.swing.JButton();
        jButtonAddObjectUnselect = new javax.swing.JButton();
        jButtonAddObjectSelect = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane9 = new javax.swing.JScrollPane();
        jListAddObjectClassSelect = new javax.swing.JList();
        jPanel10 = new javax.swing.JPanel();
        jScrollPane10 = new javax.swing.JScrollPane();
        jListAddAttributeSelect = new javax.swing.JList();
        jPanel3 = new javax.swing.JPanel();
        jLabelUpdateBaseDN = new javax.swing.JLabel();
        jTextFieldUpdateBaseDN = new javax.swing.JTextField();
        jScrollPane11 = new javax.swing.JScrollPane();
        jTreeUpdate = new javax.swing.JTree();
        jPanel11 = new javax.swing.JPanel();
        jComboBoxUpdateFilter = new javax.swing.JComboBox();
        jScrollPane12 = new javax.swing.JScrollPane();
        jListUpdateFilterAvailable = new javax.swing.JList();
        jButtonUpdateFilterSelectAnd = new javax.swing.JButton();
        jButtonUpdateFilterSelectOr = new javax.swing.JButton();
        jButtonUpdateFilterUnselect = new javax.swing.JButton();
        jScrollPane13 = new javax.swing.JScrollPane();
        jListUpdateFilterSeleted = new javax.swing.JList();
        jPanel12 = new javax.swing.JPanel();
        jComboBoxUpdateSet = new javax.swing.JComboBox();
        jScrollPane14 = new javax.swing.JScrollPane();
        jListUpdateSetAvailable = new javax.swing.JList();
        jButtonUpdateSetSelect = new javax.swing.JButton();
        jButtonUpdateSetUnselect = new javax.swing.JButton();
        jScrollPane15 = new javax.swing.JScrollPane();
        jListUpdateSetSelected = new javax.swing.JList();
        jPanel4 = new javax.swing.JPanel();
        jLabelDeleteBaseDN = new javax.swing.JLabel();
        jTextFieldDeleteBaseDN = new javax.swing.JTextField();
        jPanel13 = new javax.swing.JPanel();
        jComboBoxDeleteFilter = new javax.swing.JComboBox();
        jScrollPane17 = new javax.swing.JScrollPane();
        jListDeleteFilterAvailable = new javax.swing.JList();
        jButtonDeleteFilterSelectAnd = new javax.swing.JButton();
        jButtonDeleteFilterSelectOr = new javax.swing.JButton();
        jButtonDeleteFilterUnselect = new javax.swing.JButton();
        jScrollPane18 = new javax.swing.JScrollPane();
        jListDeleteFilterSeleted = new javax.swing.JList();
        jScrollPane16 = new javax.swing.JScrollPane();
        jTreeDelete = new javax.swing.JTree();

        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                selectedTabIndexChange(evt);
            }
        });

        jLabelSearchBaseDN.setLabelFor(jTextFieldSearchBaseDN);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelSearchBaseDN, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jLabelSearchBaseDN.text")); // NOI18N

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("search filter"));

        jComboBoxSearchFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxSearchFilterActionPerformed(evt);
            }
        });
        jComboBoxSearchFilter.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jComboBoxSearchFilterPropertyChange(evt);
            }
        });

        jListSearchFilterAvailable.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListSearchFilterAvailableValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jListSearchFilterAvailable);
        jListSearchFilterAvailable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jListSearchFilterAvailable.name")); // NOI18N
        jListSearchFilterAvailable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jListSearchFilterAvailable.desc")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonSearchFilterSelectAnd, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jButtonSearchFilterSelectAnd.text")); // NOI18N
        jButtonSearchFilterSelectAnd.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButtonSearchFilterSelectAnd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSearchFilterSelectAndActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButtonSearchFilterSelectOr, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jButtonSearchFilterSelectOr.text")); // NOI18N
        jButtonSearchFilterSelectOr.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButtonSearchFilterSelectOr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSearchFilterSelectOrActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButtonSearchFilterUnselect, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jButtonSearchFilterUnselect.text")); // NOI18N
        jButtonSearchFilterUnselect.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButtonSearchFilterUnselect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSearchFilterUnselectActionPerformed(evt);
            }
        });

        jListSearchFilterSeleted.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListItemOnSelected(evt);
            }
        });
        jListSearchFilterSeleted.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListSearchFilterSeletedValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(jListSearchFilterSeleted);
        jListSearchFilterSeleted.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jListSearchFilterSelected.name")); // NOI18N
        jListSearchFilterSeleted.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jListSearchFilterSelected.desc")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jComboBoxSearchFilter, 0, 115, Short.MAX_VALUE)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE))
                .add(8, 8, 8)
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(jButtonSearchFilterUnselect, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jButtonSearchFilterSelectOr, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jButtonSearchFilterSelectAnd))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 144, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jScrollPane3, 0, 0, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel6Layout.createSequentialGroup()
                        .add(jComboBoxSearchFilter, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel6Layout.createSequentialGroup()
                                .add(jButtonSearchFilterSelectAnd)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jButtonSearchFilterSelectOr)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jButtonSearchFilterUnselect))
                            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 81, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        jComboBoxSearchFilter.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jComboBoxSearchFilter.name")); // NOI18N
        jComboBoxSearchFilter.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jComboBoxSearchFilter.desc")); // NOI18N
        jButtonSearchFilterSelectAnd.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jButtonSearchFilterSelectAnd.desc")); // NOI18N
        jButtonSearchFilterSelectOr.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jButtonSearchFilterSelectOr.desc")); // NOI18N
        jButtonSearchFilterUnselect.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jButtonSearchFilterUnselect.desc")); // NOI18N

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("result set"));

        jComboBoxResultSet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxResultSetActionPerformed(evt);
            }
        });

        jListSearchResultAvailable.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListSearchResultAvailableValueChanged(evt);
            }
        });
        jScrollPane4.setViewportView(jListSearchResultAvailable);
        jListSearchResultAvailable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jListSearchResultAvailable.name")); // NOI18N
        jListSearchResultAvailable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jListSearchResultAvailable.desc")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonResultSetSelect, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jButtonResultSetSelect.text")); // NOI18N
        jButtonResultSetSelect.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButtonResultSetSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonResultSetSelectActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButtonResultSetUnselect, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jButtonResultSetUnselect.text")); // NOI18N
        jButtonResultSetUnselect.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButtonResultSetUnselect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonResultSetUnselectActionPerformed(evt);
            }
        });

        jListSearchResultSelected.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListSearchResultSelectedValueChanged(evt);
            }
        });
        jScrollPane5.setViewportView(jListSearchResultSelected);
        jListSearchResultSelected.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jListSearchResultSelected.name")); // NOI18N
        jListSearchResultSelected.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jListSearchResultSelected.desc")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel7Layout = new org.jdesktop.layout.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel7Layout.createSequentialGroup()
                .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jScrollPane4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
                    .add(jComboBoxResultSet, 0, 118, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(jButtonResultSetUnselect, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jButtonResultSetSelect, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 143, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel7Layout.createSequentialGroup()
                .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jScrollPane5, 0, 0, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel7Layout.createSequentialGroup()
                        .add(jComboBoxResultSet, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel7Layout.createSequentialGroup()
                                .add(jButtonResultSetSelect)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jButtonResultSetUnselect)
                                .add(41, 41, 41))
                            .add(jScrollPane4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE))))
                .addContainerGap())
        );

        jComboBoxResultSet.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jComboBoxResultSet.name")); // NOI18N
        jComboBoxResultSet.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jComboBoxResultSet.desc")); // NOI18N
        jButtonResultSetSelect.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jButtonResultSetSelect.desc")); // NOI18N
        jButtonResultSetUnselect.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jButtonResultSetUnselect.desc")); // NOI18N

        jTreeSearch.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                searchTreeOnSelected(evt);
            }
        });
        jScrollPane1.setViewportView(jTreeSearch);
        jTreeSearch.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jTreeSearch.name")); // NOI18N
        jTreeSearch.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jTreeSearch.desc")); // NOI18N

        jLabelRecordsPerPage.setLabelFor(jTextFieldRecordsPerPage);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelRecordsPerPage, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jLabelRecordPerPage.text")); // NOI18N

        jTextFieldRecordsPerPage.setText("0");

        jLabel1.setLabelFor(jTextFieldSortByAttribute);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jLabel1.text")); // NOI18N

        jComboBoxSortType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ASC", "DESC" }));

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(4, 4, 4)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 122, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabelSearchBaseDN))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jTextFieldRecordsPerPage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 53, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 51, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(6, 6, 6)
                                .add(jTextFieldSortByAttribute, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 103, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(18, 18, 18)
                                .add(jComboBoxSortType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(jPanel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jTextFieldSearchBaseDN, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)))
                    .add(jLabelRecordsPerPage))
                .add(68, 68, 68))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelSearchBaseDN, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jTextFieldSearchBaseDN, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(16, 16, 16)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelRecordsPerPage)
                    .add(jTextFieldRecordsPerPage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1)
                    .add(jTextFieldSortByAttribute, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jComboBoxSortType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 312, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(36, Short.MAX_VALUE))
        );

        jTextFieldSearchBaseDN.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jTextFieldSearchBaseDN.name")); // NOI18N
        jTextFieldSearchBaseDN.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jTextFieldSearchBaseDN.desc")); // NOI18N
        jTextFieldRecordsPerPage.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jTextFieldRecordsPerPage.name")); // NOI18N
        jTextFieldRecordsPerPage.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jTextFieldRecordsPerPage.desc")); // NOI18N
        jTextFieldSortByAttribute.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jTextFieldSortByAttribute.name")); // NOI18N
        jTextFieldSortByAttribute.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jTextFieldSortByAttribute.desc")); // NOI18N
        jComboBoxSortType.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jComboBoxSortType.name")); // NOI18N
        jComboBoxSortType.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jComboBoxSortType.desc")); // NOI18N

        jTabbedPane1.addTab("Search", jPanel1);

        jLabelAddBaseDN.setLabelFor(jTextFieldAddBaseDN);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelAddBaseDN, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jLabelAddBaseDN.text")); // NOI18N

        jTreeAdd.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                addTreeOnSelected(evt);
            }
        });
        jScrollPane6.setViewportView(jTreeAdd);
        jTreeAdd.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jTreeAdd.name")); // NOI18N
        jTreeAdd.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jTreeAdd.desc")); // NOI18N

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("ObjectClass"));

        jListAddObjcecClassAvailable.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListAddObjcecClassAvailableValueChanged(evt);
            }
        });
        jScrollPane7.setViewportView(jListAddObjcecClassAvailable);
        jListAddObjcecClassAvailable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jListAddObjectClassAvailable.name")); // NOI18N
        jListAddObjcecClassAvailable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jListAddObjectClassAvailable.desc")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .add(jScrollPane7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 131, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .add(jScrollPane7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 108, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Attributes"));

        jListAddAttributeAvailable.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListAddAttributeAvailableValueChanged(evt);
            }
        });
        jScrollPane8.setViewportView(jListAddAttributeAvailable);
        jListAddAttributeAvailable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jListAddAttributeAvailable.name")); // NOI18N
        jListAddAttributeAvailable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jListAddAttributeAvailable.desc")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel8Layout = new org.jdesktop.layout.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 146, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel8Layout.createSequentialGroup()
                .add(jScrollPane8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 108, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddAttributeUnselect, "∧"); // NOI18N
        jButtonAddAttributeUnselect.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButtonAddAttributeUnselect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddAttributeUnselectActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddAttributeSelect, "∨"); // NOI18N
        jButtonAddAttributeSelect.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButtonAddAttributeSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddAttributeSelectActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddObjectUnselect, "∧"); // NOI18N
        jButtonAddObjectUnselect.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButtonAddObjectUnselect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddObjectUnselectActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddObjectSelect, "∨"); // NOI18N
        jButtonAddObjectSelect.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButtonAddObjectSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddObjectSelectActionPerformed(evt);
            }
        });

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder("ObjectClass"));

        jListAddObjectClassSelect.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListAddObjectClassSelectValueChanged(evt);
            }
        });
        jScrollPane9.setViewportView(jListAddObjectClassSelect);
        jListAddObjectClassSelect.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jListAddObjectClassSelect.name")); // NOI18N
        jListAddObjectClassSelect.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jListAddObjectClassSelected.desc")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel9Layout = new org.jdesktop.layout.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel9Layout.createSequentialGroup()
                .add(jScrollPane9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 131, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel9Layout.createSequentialGroup()
                .add(jScrollPane9, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder("Attributes"));

        jListAddAttributeSelect.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListAddAttributeSelectValueChanged(evt);
            }
        });
        jScrollPane10.setViewportView(jListAddAttributeSelect);
        jListAddAttributeSelect.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jListAddAttributeSelect.name")); // NOI18N
        jListAddAttributeSelect.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jListAddAttributeSelect.desc")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel10Layout = new org.jdesktop.layout.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 146, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel10Layout.createSequentialGroup()
                .add(jScrollPane10, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(54, 54, 54)
                        .add(jLabelAddBaseDN))
                    .add(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jScrollPane6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 117, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(4, 4, 4)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel2Layout.createSequentialGroup()
                                .add(36, 36, 36)
                                .add(jButtonAddObjectSelect, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 36, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(18, 18, 18)
                                .add(jButtonAddObjectUnselect, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jPanel2Layout.createSequentialGroup()
                                .add(8, 8, 8)
                                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(jPanel9, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .add(18, 18, 18)
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(jPanel2Layout.createSequentialGroup()
                                .add(39, 39, 39)
                                .add(jButtonAddAttributeSelect, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 33, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jButtonAddAttributeUnselect, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 36, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jPanel8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jPanel10, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .add(jTextFieldAddBaseDN))
                .add(67, 67, 67))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabelAddBaseDN)
                    .add(jTextFieldAddBaseDN, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 15, Short.MAX_VALUE)
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jButtonAddObjectUnselect)
                            .add(jButtonAddObjectSelect))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel9, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(jScrollPane6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jPanel8, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jButtonAddAttributeSelect)
                            .add(jButtonAddAttributeUnselect))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel10, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jTextFieldAddBaseDN.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jTextFieldAddBaseDN.name")); // NOI18N
        jTextFieldAddBaseDN.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jTextFieldAddBaseDN.desc")); // NOI18N
        jButtonAddAttributeUnselect.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jButtonAddAttributeUnselect.desc")); // NOI18N
        jButtonAddAttributeSelect.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jButtonAddAttributeSelect.desc")); // NOI18N
        jButtonAddObjectUnselect.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jButtonAddObjectUnselect.desc")); // NOI18N
        jButtonAddObjectSelect.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jButtonAddObjectSelect.desc")); // NOI18N

        jTabbedPane1.addTab(" Add ", jPanel2);

        jLabelUpdateBaseDN.setLabelFor(jTextFieldUpdateBaseDN);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelUpdateBaseDN, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jLabelUpdateBaseDN.text")); // NOI18N

        jTreeUpdate.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                updateTreeOnSelected(evt);
            }
        });
        jScrollPane11.setViewportView(jTreeUpdate);
        jTreeUpdate.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jTreeUpdate.name")); // NOI18N
        jTreeUpdate.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jTreeUpdate.desc")); // NOI18N

        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder("Update filter"));

        jComboBoxUpdateFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxUpdateFilterActionPerformed(evt);
            }
        });

        jListUpdateFilterAvailable.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListUpdateFilterAvailableValueChanged(evt);
            }
        });
        jScrollPane12.setViewportView(jListUpdateFilterAvailable);
        jListUpdateFilterAvailable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jListUpdateFilterAvailable.name")); // NOI18N
        jListUpdateFilterAvailable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jListUpdateFilterAvailable.desc")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonUpdateFilterSelectAnd, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jButtonUpdateFilterSelectAnd.text")); // NOI18N
        jButtonUpdateFilterSelectAnd.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButtonUpdateFilterSelectAnd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUpdateFilterSelectAndActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButtonUpdateFilterSelectOr, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jButtonUpdateFilterSelectOr.text")); // NOI18N
        jButtonUpdateFilterSelectOr.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButtonUpdateFilterSelectOr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUpdateFilterSelectOrActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButtonUpdateFilterUnselect, "  < "); // NOI18N
        jButtonUpdateFilterUnselect.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButtonUpdateFilterUnselect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUpdateFilterUnselectActionPerformed(evt);
            }
        });

        jListUpdateFilterSeleted.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListUpdateSelectedItemOnSelected(evt);
            }
        });
        jListUpdateFilterSeleted.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListUpdateFilterSeletedValueChanged(evt);
            }
        });
        jScrollPane13.setViewportView(jListUpdateFilterSeleted);
        jListUpdateFilterSeleted.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jListUpdateFilterSelected.name")); // NOI18N
        jListUpdateFilterSeleted.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jListUpdateFilterSelected.desc")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel11Layout = new org.jdesktop.layout.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel11Layout.createSequentialGroup()
                .add(jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jComboBoxUpdateFilter, 0, 143, Short.MAX_VALUE)
                    .add(jScrollPane12, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(jButtonUpdateFilterSelectAnd, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jButtonUpdateFilterSelectOr, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jButtonUpdateFilterUnselect, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane13, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 138, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel11Layout.createSequentialGroup()
                .add(jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jScrollPane13, 0, 0, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel11Layout.createSequentialGroup()
                        .add(jComboBoxUpdateFilter, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel11Layout.createSequentialGroup()
                                .add(jButtonUpdateFilterSelectAnd)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jButtonUpdateFilterSelectOr)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jButtonUpdateFilterUnselect))
                            .add(jScrollPane12, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 81, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        jComboBoxUpdateFilter.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jComboBoxUpdateFilter.name")); // NOI18N
        jComboBoxUpdateFilter.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jComboBoxUpdateFilter.desc")); // NOI18N
        jButtonUpdateFilterSelectAnd.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jButtonUpdateFilterSelectAnd.desc")); // NOI18N
        jButtonUpdateFilterSelectOr.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jButtonUpdateFilterSelectOr.desc")); // NOI18N
        jButtonUpdateFilterUnselect.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jButtonUpdateFilterUnselect.desc")); // NOI18N

        jPanel12.setBorder(javax.swing.BorderFactory.createTitledBorder("Update set"));

        jComboBoxUpdateSet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxUpdateSetActionPerformed(evt);
            }
        });

        jListUpdateSetAvailable.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListUpdateSetAvailableValueChanged(evt);
            }
        });
        jScrollPane14.setViewportView(jListUpdateSetAvailable);
        jListUpdateSetAvailable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jListUpdateSetAvailable.name")); // NOI18N
        jListUpdateSetAvailable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jListUpdateSetAvailable.desc")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonUpdateSetSelect, " > "); // NOI18N
        jButtonUpdateSetSelect.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButtonUpdateSetSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUpdateSetSelectActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButtonUpdateSetUnselect, " < "); // NOI18N
        jButtonUpdateSetUnselect.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButtonUpdateSetUnselect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUpdateSetUnselectActionPerformed(evt);
            }
        });

        jListUpdateSetSelected.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListUpdateSetSelectedValueChanged(evt);
            }
        });
        jScrollPane15.setViewportView(jListUpdateSetSelected);
        jListUpdateSetSelected.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jListUpdateSetSelected.name")); // NOI18N
        jListUpdateSetSelected.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jListUpdateSetSelected.desc")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel12Layout = new org.jdesktop.layout.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel12Layout.createSequentialGroup()
                .add(jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jComboBoxUpdateSet, 0, 146, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane14, 0, 0, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jButtonUpdateSetUnselect, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jButtonUpdateSetSelect, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane15, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel12Layout.createSequentialGroup()
                .add(jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jPanel12Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jScrollPane15, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE))
                    .add(jPanel12Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jComboBoxUpdateSet, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jScrollPane14, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel12Layout.createSequentialGroup()
                        .add(56, 56, 56)
                        .add(jButtonUpdateSetSelect)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonUpdateSetUnselect)))
                .addContainerGap())
        );

        jComboBoxUpdateSet.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jComboBox.UpdateSet.name")); // NOI18N
        jComboBoxUpdateSet.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jComboBoxUpdateSet.desc")); // NOI18N
        jButtonUpdateSetSelect.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jButtonUpdateSetSelect.desc")); // NOI18N
        jButtonUpdateSetUnselect.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jButtonUpdateSetUnselect.desc")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(59, 59, 59)
                        .add(jLabelUpdateBaseDN, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 64, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jScrollPane11, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel12, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jTextFieldUpdateBaseDN, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 344, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel11, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .add(115, 115, 115))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabelUpdateBaseDN)
                    .add(jTextFieldUpdateBaseDN, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(jPanel11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel12, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(jScrollPane11, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTextFieldUpdateBaseDN.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jTextFieldUpdateBaseDN.name")); // NOI18N
        jTextFieldUpdateBaseDN.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jTextFieldUpdateBaseDN.desc")); // NOI18N

        jTabbedPane1.addTab("Update", jPanel3);

        jLabelDeleteBaseDN.setLabelFor(jTextFieldDeleteBaseDN);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelDeleteBaseDN, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jLabelDeleteBaseDN.text")); // NOI18N

        jTextFieldDeleteBaseDN.setAutoscrolls(false);

        jPanel13.setBorder(javax.swing.BorderFactory.createTitledBorder("remove filter"));
        jPanel13.setAutoscrolls(true);

        jComboBoxDeleteFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxDeleteFilterActionPerformed(evt);
            }
        });

        jListDeleteFilterAvailable.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListDeleteFilterAvailableValueChanged(evt);
            }
        });
        jScrollPane17.setViewportView(jListDeleteFilterAvailable);
        jListDeleteFilterAvailable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jListDeleteFilterAvailable.name")); // NOI18N
        jListDeleteFilterAvailable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jListDeleteFilterAvailable.desc")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonDeleteFilterSelectAnd, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jButtonDeleteFilterSelectAnd.text")); // NOI18N
        jButtonDeleteFilterSelectAnd.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButtonDeleteFilterSelectAnd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteFilterSelectAndActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButtonDeleteFilterSelectOr, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jButtonDeleteFilterSelectOr.text")); // NOI18N
        jButtonDeleteFilterSelectOr.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButtonDeleteFilterSelectOr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteFilterSelectOrActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButtonDeleteFilterUnselect, "  < "); // NOI18N
        jButtonDeleteFilterUnselect.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButtonDeleteFilterUnselect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteFilterUnselectActionPerformed(evt);
            }
        });

        jListDeleteFilterSeleted.setAutoscrolls(false);
        jListDeleteFilterSeleted.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListDeleteFilterSeletedItemOnSelected(evt);
            }
        });
        jListDeleteFilterSeleted.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListDeleteFilterSeletedValueChanged(evt);
            }
        });
        jScrollPane18.setViewportView(jListDeleteFilterSeleted);
        jListDeleteFilterSeleted.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jListDeleteFilterSelected.name")); // NOI18N
        jListDeleteFilterSeleted.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jListDeleteFilterSelected.desc")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel13Layout = new org.jdesktop.layout.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel13Layout.createSequentialGroup()
                .add(12, 12, 12)
                .add(jPanel13Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jComboBoxDeleteFilter, 0, 142, Short.MAX_VALUE)
                    .add(jScrollPane17, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel13Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jButtonDeleteFilterUnselect, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jButtonDeleteFilterSelectOr, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jButtonDeleteFilterSelectAnd))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 126, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(26, 26, 26))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel13Layout.createSequentialGroup()
                .add(jPanel13Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel13Layout.createSequentialGroup()
                        .add(jComboBoxDeleteFilter, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jScrollPane17, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 312, Short.MAX_VALUE))
                    .add(jPanel13Layout.createSequentialGroup()
                        .add(79, 79, 79)
                        .add(jButtonDeleteFilterSelectAnd)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonDeleteFilterSelectOr)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonDeleteFilterUnselect)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 197, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane18, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE))
                .addContainerGap())
        );

        jComboBoxDeleteFilter.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jComboBoxDeleteFilter.name")); // NOI18N
        jComboBoxDeleteFilter.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jComboBoxDeleteFilter.desc")); // NOI18N
        jButtonDeleteFilterSelectAnd.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jButtonDeleteFilterSelectAnd.desc")); // NOI18N
        jButtonDeleteFilterSelectOr.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jButtonDeleteFilterSelectOr.desc")); // NOI18N
        jButtonDeleteFilterUnselect.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jButtonDeleteFilterUnselect.desc")); // NOI18N

        jTreeDelete.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                removeTreeOnSelected(evt);
            }
        });
        jScrollPane16.setViewportView(jTreeDelete);
        jTreeDelete.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jTreeDelete.name")); // NOI18N
        jTreeDelete.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jTreeDelete.desc")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jScrollPane16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 108, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabelDeleteBaseDN))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jTextFieldDeleteBaseDN)
                    .add(jPanel13, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(52, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelDeleteBaseDN)
                    .add(jTextFieldDeleteBaseDN, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane16, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)
                    .add(jPanel13, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jTextFieldDeleteBaseDN.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jTextFieldDeleteBaseDN.name")); // NOI18N
        jTextFieldDeleteBaseDN.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jTextFieldDeleteBaseDN.desc")); // NOI18N

        jTabbedPane1.addTab("Delete", jPanel4);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 553, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(29, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jTabbedPane1)
                .addContainerGap())
        );

        jTabbedPane1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.jTabbedPane1.desc")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    private void searchTreeOnSelected(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_searchTreeOnSelected
        // TODO add your handling code here:
        TreePath path = evt.getPath();
        jTextFieldSearchBaseDN.setText(treePathToDN(path));
        mSelectedDN = treePathToDN(path);
        path = null;
}//GEN-LAST:event_searchTreeOnSelected

    private void addTreeOnSelected(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_addTreeOnSelected
        // TODO add your handling code here:
        TreePath path = evt.getPath();
        jTextFieldAddBaseDN.setText(treePathToDN(path));
        mSelectedDN = treePathToDN(path);
        path = null;
    }//GEN-LAST:event_addTreeOnSelected

    private void updateTreeOnSelected(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_updateTreeOnSelected
        // TODO add your handling code here:
        TreePath path = evt.getPath();
        jTextFieldUpdateBaseDN.setText(treePathToDN(path));
        mSelectedDN = treePathToDN(path);
        path = null;
    }//GEN-LAST:event_updateTreeOnSelected

    private void removeTreeOnSelected(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_removeTreeOnSelected
        // TODO add your handling code here:
        TreePath path = evt.getPath();
        jTextFieldDeleteBaseDN.setText(treePathToDN(path));
        mSelectedDN = treePathToDN(path);
        path = null;
    }//GEN-LAST:event_removeTreeOnSelected

    private void jButtonSearchFilterSelectAndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSearchFilterSelectAndActionPerformed
        // TODO add your handling code here:
        filterAdd("And");
    }//GEN-LAST:event_jButtonSearchFilterSelectAndActionPerformed

    @SuppressWarnings("unchecked")
    private void jComboBoxSearchFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxSearchFilterActionPerformed
        // TODO add your handling code here:

        String objName = jComboBoxSearchFilter.getSelectedItem().toString();
        List attributeList = getUnselectedAttribute(objName);
        jListSearchFilterAvailable.removeAll();
        jListSearchFilterAvailable.setListData(new Vector(attributeList));
//        this.jComboBoxResultSet.setSelectedIndex(jComboBoxSearchFilter.getSelectedIndex());
    }//GEN-LAST:event_jComboBoxSearchFilterActionPerformed

    @SuppressWarnings("unchecked")
    private void jComboBoxResultSetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxResultSetActionPerformed
        // TODO add your handling code here:
        String objName = (String) jComboBoxResultSet.getSelectedItem();
        List ret = new ArrayList();
        if (objName != null && objName.length() > 0) {
            LdifObjectClass obj = (LdifObjectClass) mObjectClassesMap.get(objName);
            List mays = obj.getMay();
            List musts = obj.getMust();
            List selected = DisplayFormatControl.setToAttr(obj.getResultSet());
            if (musts != null) {
//                for (int i = 0; i < musts.size(); i++) {
//                    ret.add("* " + (String) musts.get(i));
//                }
                ret.addAll(musts);
            }
            if (mays != null) {
                ret.addAll(mays);
            }
            if (selected != null) {
                ret.removeAll(selected);
            }
            this.jListSearchResultAvailable.setListData(new Vector(ret));
        }
    }//GEN-LAST:event_jComboBoxResultSetActionPerformed

    @SuppressWarnings("unchecked")
    private void jButtonResultSetSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonResultSetSelectActionPerformed
        // TODO add your handling code here:
        Object[] selected = this.jListSearchResultAvailable.getSelectedValues();
        if (selected.length == 0) {
            return;
        }
        List listCurrentSelected = new ArrayList();
        for (int i = 0; i < selected.length; i++) {
            listCurrentSelected.add(selected[i]);
        }
        String objName = (String) this.jComboBoxResultSet.getSelectedItem();
        if (mSelectedObjectMap.get(objName) == null) {
            mSelectedObjectMap.put(objName, (LdifObjectClass) mObjectClassesMap.get(objName));
        }
        LdifObjectClass obj = (LdifObjectClass) mSelectedObjectMap.get(objName);
        List resultSetAttributes = DisplayFormatControl.attrToSet(objName, listCurrentSelected);
        Iterator it = resultSetAttributes.iterator();
        while (it.hasNext()) {
            ResultSetAttribute attr = (ResultSetAttribute) it.next();
            obj.addResultSet(attr);
        }
        List ret = DisplayFormatControl.setToJlist(resultSetAttributes);
        List listAvailable = new ArrayList();
        for (int i = 0; i < jListSearchResultAvailable.getModel().getSize(); i++) {
            listAvailable.add(jListSearchResultAvailable.getModel().getElementAt(i));
        }
        listAvailable.removeAll(listCurrentSelected);
        mResultSetAttrList.addAll(ret);

        jListSearchResultAvailable.removeAll();
        jListSearchResultAvailable.setListData(new Vector(listAvailable));

        jListSearchResultSelected.removeAll();
        jListSearchResultSelected.setListData(new Vector(mResultSetAttrList));
    }//GEN-LAST:event_jButtonResultSetSelectActionPerformed

    private void jButtonSearchFilterUnselectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSearchFilterUnselectActionPerformed
        // TODO add your handling code here:
        unSelectFilter();
    }//GEN-LAST:event_jButtonSearchFilterUnselectActionPerformed

    @SuppressWarnings("unchecked")
    private void jButtonResultSetUnselectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonResultSetUnselectActionPerformed
        // TODO add your handling code here:
        Object[] selected = this.jListSearchResultSelected.getSelectedValues();
        if (selected.length == 0) {
            return;
        }
        List listCurrentSelected = new ArrayList();
        for (int i = 0; i < selected.length; i++) {
            listCurrentSelected.add(selected[i]);
        }
        // List attrs = new ArrayList();
        String finalObjName = "";
        List resultSetAttributes = DisplayFormatControl.jListToAttr(listCurrentSelected);
        Iterator it = resultSetAttributes.iterator();
        while (it.hasNext()) {
            ResultSetAttribute attr = (ResultSetAttribute) it.next();
            finalObjName = attr.getObjName();
            //String attribute = attr.getAttributeName();
            //attrs.add(attribute);
            LdifObjectClass obj = (LdifObjectClass) mObjectClassesMap.get(finalObjName);
            List resultSet = obj.getResultSet();
            for (int i = 0; i < resultSet.size(); i++) {
                ResultSetAttribute rsa = (ResultSetAttribute) resultSet.get(i);
                if (attr.equals(rsa)) {
                    obj.removeResultSet(rsa);
                }
            }
            if (!obj.isSelected()) {
                mSelectedObjectMap.remove(finalObjName);
            }
        }

        jComboBoxResultSet.getModel().setSelectedItem(finalObjName);
        List ret = new ArrayList();
        if (finalObjName != null && finalObjName.length() > 0) {
            LdifObjectClass fianlObj = (LdifObjectClass) mObjectClassesMap.get(finalObjName);
            List mays = fianlObj.getMay();
            List musts = fianlObj.getMust();
            List selectedattr = DisplayFormatControl.setToAttr(fianlObj.getResultSet());
            if (musts != null) {
//                for (int i = 0; i < musts.size(); i++) {
//                    ret.add("* " + (String) musts.get(i));
//                }
                ret.addAll(musts);
            }
            if (mays != null) {
                ret.addAll(mays);
            }
            if (selectedattr != null) {
                ret.removeAll(selectedattr);
            }
        }
        mResultSetAttrList.removeAll(listCurrentSelected);

        jListSearchResultAvailable.removeAll();
        jListSearchResultAvailable.setListData(new Vector(ret));

        jListSearchResultSelected.removeAll();
        jListSearchResultSelected.setListData(new Vector(mResultSetAttrList));
    }//GEN-LAST:event_jButtonResultSetUnselectActionPerformed

    private void jButtonSearchFilterSelectOrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSearchFilterSelectOrActionPerformed
        // TODO add your handling code here:
        filterAdd("Or");
    }//GEN-LAST:event_jButtonSearchFilterSelectOrActionPerformed

    private void jListItemOnSelected(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListItemOnSelected
        // TODO add your handling code here:
        Object[] selected = jListSearchFilterSeleted.getSelectedValues();
        if (selected.length == 0) {
            return;
        }
        int[] lastSelectedIndexes = jListSearchFilterSeleted.getSelectedIndices();
        if (evt.getClickCount() == 1) {
            int currentSelectedIndex = jListSearchFilterSeleted.locationToIndex(evt.getPoint());
            int currentBracketDepth = 0;
            int beginIndex = currentSelectedIndex;
            int endIndex = currentSelectedIndex;

            String str = DisplayFormatControl.toAttribute((String) jListSearchFilterSeleted.getSelectedValue());
            int index = str.indexOf(".");
            String obj = str.substring(0, index);
            String att = str.substring(index + 1);

            int endFlag = 0;
            int beginFlag = 0;

            Iterator it1 = mSelectedAttrList.iterator();
            SearchFilterAttribute currentSelectedAttribute = null;
            while (it1.hasNext()) {
                SearchFilterAttribute sfa = (SearchFilterAttribute) it1.next();
                if (sfa.getObjName().equals(obj) & sfa.getAttributeName().equals(att)) {
                    currentSelectedAttribute = sfa;
                    currentBracketDepth = sfa.getBracketDepth();
//                    if (sfa.getBracketBeginDepth() > 0) {
//                        endFlag = sfa.getBracketBeginDepth();
//                    }
//                    if (sfa.getBracketEndDepth() > 0) {
//                        beginFlag = sfa.getBracketEndDepth();
//                    }
                    break;
                }
                sfa = null;
            }
            selected = null;
            if (currentSelectedAttribute == null) {
                return;
            }

            if (currentBracketDepth > 0) {
                //get the beginIndex and endIndex;      
                if (!(currentSelectedAttribute.getBracketEndDepth() > 0)) {
                    for (int i = currentSelectedIndex + 1; i < mSelectedAttrList.size(); i++) {
                        boolean stopFlag = false;
                        Iterator it = mSelectedAttrList.iterator();
                        while (it.hasNext()) {
                            SearchFilterAttribute sfa = (SearchFilterAttribute) it.next();
                            if (sfa.getPositionIndex() != i) {
                                continue;
                            }
                            if (sfa.getBracketDepth() >= currentBracketDepth) {
                                if (sfa.getBracketEndDepth() <= 0) {

                                    endFlag += sfa.getBracketBeginDepth();
                                } else {
                                    if (endFlag - sfa.getBracketEndDepth() < 0) {
                                        stopFlag = true;
                                    } else {
                                        endFlag -= sfa.getBracketEndDepth();
                                    }
                                }
                                endIndex = i;
                            } else {
                                stopFlag = true;
                            }
                            sfa = null;
                            break;
                        }
                        if (stopFlag) {
                            break;
                        }
                    }
                }
                if (!(currentSelectedAttribute.getBracketBeginDepth() > 0)) {
                    for (int j = currentSelectedIndex - 1; j >= 0; j--) {
                        boolean stopFlag2 = false;
                        Iterator it = mSelectedAttrList.iterator();
                        while (it.hasNext()) {
                            SearchFilterAttribute sfa2 = (SearchFilterAttribute) it.next();
                            if (sfa2.getPositionIndex() != j) {
                                continue;
                            }
                            if (sfa2.getBracketDepth() >= currentBracketDepth) {
                                if (sfa2.getBracketBeginDepth() <= 0) {
//                                beginIndex = j;
                                    beginFlag += sfa2.getBracketEndDepth();
                                } else {
                                    if (beginFlag - sfa2.getBracketBeginDepth() < 0) {
                                        stopFlag2 = true;
                                    } else {

                                        beginFlag -= sfa2.getBracketBeginDepth();
                                    }
                                }
                                beginIndex = j;
                                break;
                            } else {
                                stopFlag2 = true;
                            }
                            sfa2 = null;
                            break;
                        }
                        if (stopFlag2) {
                            break;
                        }
                    }
                }
                int lastLength = lastSelectedIndexes.length;
                int currentLength = endIndex - beginIndex + 1;
                int[] selectedIndexs = new int[lastLength + currentLength];
                int j;
                for (j = 0; j < currentLength; j++) {
                    selectedIndexs[j] = beginIndex + j;
                }
                if (lastLength > 0) {
                    for (j = currentLength; j < lastLength + currentLength; j++) {
                        selectedIndexs[j] = lastSelectedIndexes[j - currentLength];
                    }
                }
                jListSearchFilterSeleted.setSelectedIndices(selectedIndexs);
            }
        }
    }//GEN-LAST:event_jListItemOnSelected

    @SuppressWarnings("unchecked")
    private void jComboBoxUpdateFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxUpdateFilterActionPerformed
        // TODO add your handling code here:
        String objName = jComboBoxUpdateFilter.getSelectedItem().toString();
        List attributeList = getUnselectedAttribute(objName);
        jListUpdateFilterAvailable.removeAll();
        jListUpdateFilterAvailable.setListData(new Vector(attributeList));
    }//GEN-LAST:event_jComboBoxUpdateFilterActionPerformed

    private void jListSearchFilterAvailableValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListSearchFilterAvailableValueChanged
        // TODO add your handling code here:
        Object[] selected = jListSearchFilterAvailable.getSelectedValues();
        if (selected.length != 1) {
            jButtonSearchFilterSelectAnd.setEnabled(false); 
            jButtonSearchFilterSelectOr.setEnabled(false);
            return;
        }
        jButtonSearchFilterSelectAnd.setEnabled(true); 
        jButtonSearchFilterSelectOr.setEnabled(true);        
    }//GEN-LAST:event_jListSearchFilterAvailableValueChanged

    private void jListSearchResultAvailableValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListSearchResultAvailableValueChanged
        // TODO add your handling code here:
        Object[] selected = jListSearchResultAvailable.getSelectedValues();
        if (selected.length != 1) {
            jButtonResultSetSelect.setEnabled(false); 
            return;
        }
        jButtonResultSetSelect.setEnabled(true);    
    }//GEN-LAST:event_jListSearchResultAvailableValueChanged

    private void jComboBoxSearchFilterPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jComboBoxSearchFilterPropertyChange
        // TODO add your handling code here:
        this.jListSearchFilterAvailable.requestFocus();
    }//GEN-LAST:event_jComboBoxSearchFilterPropertyChange

    @SuppressWarnings("unchecked")
    private void selectedTabIndexChange(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_selectedTabIndexChange
        // TODO add your handling code here:
        if (mSelectedObjectMap.keySet().size() > 0) {
            HashMap selectedObject = copyMSelectedObjectMap();
            String basedn = baseDN.getText();
            SelectedBackData data = new SelectedBackData(selectedObject, mFunction, basedn, mMainAttrInAdd);
            data.setMResultSetAttrList(copyMResultSetAttrList());
            data.setMSelectedAttrList(copyMSelectedAttrList());
            mSelectedObjectMaps.put(new String(mFunction), data);
        }
        clearSelect();
        int index = jTabbedPane1.getSelectedIndex();
        if (index == 0) {
            mFunction = "Search";
            baseDN = jTextFieldSearchBaseDN;
        }
        if (index == 1) {
            mFunction = "Add";
            baseDN = jTextFieldAddBaseDN;
//            Iterator it = mSelectedObjectMap.values().iterator();
//            while (it.hasNext()) {
//                LdifObjectClass loc = (LdifObjectClass) it.next();
//                loc.clearSelect();
//            }
//            mSelectedAttrList.clear();
//            mSelectedObjectMap.clear();
//            mResultSetAttrList.clear();
        }
        if (index == 2) {
            mFunction = "Update";
            baseDN = jTextFieldUpdateBaseDN;
        }
        if (index == 3) {
            mFunction = "Delete";
            baseDN = jTextFieldDeleteBaseDN;
        }
        getBackupData();
    }//GEN-LAST:event_selectedTabIndexChange

    @SuppressWarnings("unchecked")
    private void jComboBoxUpdateSetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxUpdateSetActionPerformed
        // TODO add your handling code here:        
        String objName = (String) jComboBoxUpdateSet.getSelectedItem();
        List ret = new ArrayList();
        if (objName != null && objName.length() > 0) {
            LdifObjectClass obj = (LdifObjectClass) mObjectClassesMap.get(objName);
            List mays = obj.getMay();
            List musts = obj.getMust();
            List selected = DisplayFormatControl.updateSetAttrToAttr(obj.getResultSet());
            if (musts != null) {
//                for (int i = 0; i < musts.size(); i++) {
//                    ret.add("* " + (String) musts.get(i));
//                }
                ret.addAll(musts);
            }
            if (mays != null) {
                ret.addAll(mays);
            }
            if (selected != null) {
                ret.removeAll(selected);
            }
            this.jListUpdateSetAvailable.setListData(new Vector(ret));
        }
    }//GEN-LAST:event_jComboBoxUpdateSetActionPerformed

    private void jButtonUpdateFilterSelectAndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUpdateFilterSelectAndActionPerformed
        // TODO add your handling code here:
        filterAdd("And");
    }//GEN-LAST:event_jButtonUpdateFilterSelectAndActionPerformed

    private void jButtonUpdateFilterSelectOrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUpdateFilterSelectOrActionPerformed
        // TODO add your handling code here:
        filterAdd("Or");
    }//GEN-LAST:event_jButtonUpdateFilterSelectOrActionPerformed

    private void jListUpdateSelectedItemOnSelected(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListUpdateSelectedItemOnSelected
        // TODO add your handling code here:
        Object[] selected = jListUpdateFilterSeleted.getSelectedValues();
        if (selected.length == 0) {
            return;
        }
        int[] lastSelectedIndexes = jListUpdateFilterSeleted.getSelectedIndices();
        if (evt.getClickCount() == 1) {
            int currentSelectedIndex = jListUpdateFilterSeleted.locationToIndex(evt.getPoint());
            int currentBracketDepth = 0;
            int beginIndex = currentSelectedIndex;
            int endIndex = currentSelectedIndex;

            String str = DisplayFormatControl.toAttribute((String) jListUpdateFilterSeleted.getSelectedValue());
            int index = str.indexOf(".");
            String obj = str.substring(0, index);
            String att = str.substring(index + 1);

            int endFlag = 0;
            int beginFlag = 0;

            Iterator it1 = mSelectedAttrList.iterator();
            SearchFilterAttribute currentSelectedAttribute = null;
            while (it1.hasNext()) {
                SearchFilterAttribute sfa = (SearchFilterAttribute) it1.next();
                if (sfa.getObjName().equals(obj) & sfa.getAttributeName().equals(att)) {
                    currentSelectedAttribute = sfa;
                    currentBracketDepth = sfa.getBracketDepth();
                    break;
                }
                sfa = null;
            }
            selected = null;
            if (currentSelectedAttribute == null) {
                return;
            }

            if (currentBracketDepth > 0) {
                //get the beginIndex and endIndex;      
                if (!(currentSelectedAttribute.getBracketEndDepth() > 0)) {
                    for (int i = currentSelectedIndex + 1; i < mSelectedAttrList.size(); i++) {
                        boolean stopFlag = false;
                        Iterator it = mSelectedAttrList.iterator();
                        while (it.hasNext()) {
                            SearchFilterAttribute sfa = (SearchFilterAttribute) it.next();
                            if (sfa.getPositionIndex() != i) {
                                continue;
                            }
                            if (sfa.getBracketDepth() >= currentBracketDepth) {
                                if (sfa.getBracketEndDepth() <= 0) {

                                    endFlag += sfa.getBracketBeginDepth();
                                } else {
                                    if (endFlag - sfa.getBracketEndDepth() < 0) {
                                        stopFlag = true;
                                    } else {
                                        endFlag -= sfa.getBracketEndDepth();
                                    }
                                }
                                endIndex = i;
                            } else {
                                stopFlag = true;
                            }
                            sfa = null;
                            break;
                        }
                        if (stopFlag) {
                            break;
                        }
                    }
                }
                if (!(currentSelectedAttribute.getBracketBeginDepth() > 0)) {
                    for (int j = currentSelectedIndex - 1; j >= 0; j--) {
                        boolean stopFlag2 = false;
                        Iterator it = mSelectedAttrList.iterator();
                        while (it.hasNext()) {
                            SearchFilterAttribute sfa2 = (SearchFilterAttribute) it.next();
                            if (sfa2.getPositionIndex() != j) {
                                continue;
                            }
                            if (sfa2.getBracketDepth() >= currentBracketDepth) {
                                if (sfa2.getBracketBeginDepth() <= 0) {
//                                beginIndex = j;
                                    beginFlag += sfa2.getBracketEndDepth();
                                } else {
                                    if (beginFlag - sfa2.getBracketBeginDepth() < 0) {
                                        stopFlag2 = true;
                                    } else {

                                        beginFlag -= sfa2.getBracketBeginDepth();
                                    }
                                }
                                beginIndex = j;
                                break;
                            } else {
                                stopFlag2 = true;
                            }
                            sfa2 = null;
                            break;
                        }
                        if (stopFlag2) {
                            break;
                        }
                    }
                }
                int lastLength = lastSelectedIndexes.length;
                int currentLength = endIndex - beginIndex + 1;
                int[] selectedIndexs = new int[lastLength + currentLength];
                int j;
                for (j = 0; j < currentLength; j++) {
                    selectedIndexs[j] = beginIndex + j;
                }
                if (lastLength > 0) {
                    for (j = currentLength; j < lastLength + currentLength; j++) {
                        selectedIndexs[j] = lastSelectedIndexes[j - currentLength];
                    }
                }
                jListUpdateFilterSeleted.setSelectedIndices(selectedIndexs);
            }
        }
    }//GEN-LAST:event_jListUpdateSelectedItemOnSelected

    @SuppressWarnings("unchecked")
    private void jButtonUpdateSetSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUpdateSetSelectActionPerformed
        // TODO add your handling code here:
        Object[] selected = this.jListUpdateSetAvailable.getSelectedValues();
        if (selected.length == 0) {
            return;
        }
        List listCurrentSelected = new ArrayList();
        for (int i = 0; i < selected.length; i++) {
            listCurrentSelected.add(selected[i]);
        }
        String objName = (String) this.jComboBoxUpdateSet.getSelectedItem();
        if (mSelectedObjectMap.get(objName) == null) {
            mSelectedObjectMap.put(objName, (LdifObjectClass) mObjectClassesMap.get(objName));
        }
        LdifObjectClass obj = (LdifObjectClass) mSelectedObjectMap.get(objName);
        List updateSetAttributes = DisplayFormatControl.attrToUpdateSetAttr(objName, listCurrentSelected);
        Iterator it = updateSetAttributes.iterator();
        while (it.hasNext()) {
            UpdateSetAttribute attr = (UpdateSetAttribute) it.next();
            obj.addResultSet(attr);
        }
        List ret = DisplayFormatControl.updateSetToJList(updateSetAttributes);
        List listAvailable = new ArrayList();
        for (int i = 0; i < jListUpdateSetAvailable.getModel().getSize(); i++) {
            listAvailable.add(jListUpdateSetAvailable.getModel().getElementAt(i));
        }
        listAvailable.removeAll(listCurrentSelected);
//        mResultSetAttrList.addAll(ret);

        jListUpdateSetAvailable.removeAll();
        jListUpdateSetAvailable.setListData(new Vector(listAvailable));

        refreshUpdateSetSelectList();
//        jListUpdateSetSelected.removeAll();
//        jListUpdateSetSelected.setListData(new Vector(mResultSetAttrList)); 
    }//GEN-LAST:event_jButtonUpdateSetSelectActionPerformed

    @SuppressWarnings("unchecked")
    private void jButtonUpdateSetUnselectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUpdateSetUnselectActionPerformed
        // TODO add your handling code here:
        Object[] selected = this.jListUpdateSetSelected.getSelectedValues();
        if (selected.length == 0) {
            return;
        }
        List listCurrentSelected = new ArrayList();
        for (int i = 0; i < selected.length; i++) {
            listCurrentSelected.add(selected[i]);
        }
        String finalObjName = "";
        List updateSetAttributes = DisplayFormatControl.jListToUpdateSetAttr(listCurrentSelected);
        Iterator it = updateSetAttributes.iterator();
        while (it.hasNext()) {
            UpdateSetAttribute attr = (UpdateSetAttribute) it.next();
            finalObjName = attr.getObjName();
            LdifObjectClass obj = (LdifObjectClass) mObjectClassesMap.get(finalObjName);
            List resultSet = obj.getResultSet();
            for (int i = 0; i < resultSet.size(); i++) {
                UpdateSetAttribute usa = (UpdateSetAttribute) resultSet.get(i);
                if (attr.equals(usa)) {
                    obj.removeResultSet(usa);
                }
            }
            if (!obj.isSelected()) {
                mSelectedObjectMap.remove(finalObjName);
            }
        }

        jComboBoxUpdateSet.getModel().setSelectedItem(finalObjName);
//        List ret = new ArrayList();
//        if (finalObjName != null && finalObjName.length() > 0) {
//            LdifObjectClass fianlObj = (LdifObjectClass) mObjectClassesMap.get(finalObjName);
//            List mays = fianlObj.getMay();
//            List musts = fianlObj.getMust();
//            List selectedattr = DisplayFormatControl.updateSetAttrToAttr(fianlObj.getResultSet());
//            if (musts != null) {
////                for (int i = 0; i < musts.size(); i++) {
////                    ret.add("* " + (String) musts.get(i));
////                }
//                ret.addAll(musts);
//            }
//            if (mays != null) {
//                ret.addAll(mays);
//            }
//            if (selectedattr != null) {
//                ret.removeAll(selectedattr);
//            }
//        }
////        mResultSetAttrList.removeAll(listCurrentSelected);
//
//        jListUpdateSetAvailable.removeAll();
//        jListUpdateSetAvailable.setListData(new Vector(ret));

//        jListUpdateSetSelected.removeAll();
//        jListUpdateSetSelected.setListData(new Vector(mResultSetAttrList)); 
        refreshUpdateSetSelectList();

    }//GEN-LAST:event_jButtonUpdateSetUnselectActionPerformed

    private void jButtonUpdateFilterUnselectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUpdateFilterUnselectActionPerformed
        // TODO add your handling code here:
        unSelectFilter();
    }//GEN-LAST:event_jButtonUpdateFilterUnselectActionPerformed

    @SuppressWarnings("unchecked")
    private void jButtonAddObjectSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddObjectSelectActionPerformed
        // TODO add your handling code here:
        Object[] selected = jListAddObjcecClassAvailable.getSelectedValues();
        if (selected.length != 1) {
            return;
        }
        String objName = (String) selected[0];
        LdifObjectClass loc = (LdifObjectClass) mObjectClassesMap.get(objName);
        mSelectedObjectMap.put(objName, loc);

        //refresh jListAddObjectClassSelect
        List objAvaiList = copyObjectsList();
        List objSelecteList = new ArrayList();
//        List attrAvaiList  = new ArrayList();
        List must = loc.getMust();
        for (int i = 0; i < must.size(); i++) {
            loc.addResultSet((String) must.get(i));
        }

        Iterator it = mSelectedObjectMap.values().iterator();
        while (it.hasNext()) {
            objSelecteList.add(((LdifObjectClass) it.next()).getName());
        }

//        try {
//            objAvaiList = conn.getObjectNames();
//        } catch (NamingException ex) {
//            Exceptions.printStackTrace(ex);
//        }
        objAvaiList.removeAll(objSelecteList);

        jListAddObjectClassSelect.removeAll();
        jListAddObjectClassSelect.setListData(new Vector(objSelecteList));
        jListAddObjectClassSelect.setSelectedValue(objName, true);

        jListAddObjcecClassAvailable.removeAll();
        jListAddObjcecClassAvailable.setListData(new Vector(objAvaiList));

    }//GEN-LAST:event_jButtonAddObjectSelectActionPerformed

    private void jListAddObjcecClassAvailableValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListAddObjcecClassAvailableValueChanged
        // TODO add your handling code here:
        Object[] selected = jListAddObjcecClassAvailable.getSelectedValues();
        if (selected.length != 1) {
            jButtonAddObjectSelect.setEnabled(false);
            return;
        }
        String objName = (String) selected[0];
        refreshAddTabAttributeAvailableList(objName);
        refreshAddTabSelectedAttrsList();

        jButtonAddObjectSelect.setEnabled(true);
    }//GEN-LAST:event_jListAddObjcecClassAvailableValueChanged

    private void jListAddObjectClassSelectValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListAddObjectClassSelectValueChanged
        // TODO add your handling code here:
        Object[] selected = jListAddObjectClassSelect.getSelectedValues();
        if (selected.length != 1) {
            jButtonAddObjectUnselect.setEnabled(false);       
            return;
        }
        String objName = (String) selected[0];

        refreshAddTabAttributeAvailableList(objName);
        refreshAddTabSelectedAttrsList();

        jButtonAddObjectUnselect.setEnabled(true);
    }//GEN-LAST:event_jListAddObjectClassSelectValueChanged

    @SuppressWarnings("unchecked")
    private void jButtonAddObjectUnselectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddObjectUnselectActionPerformed
        // TODO add your handling code here:
        Object[] selected = jListAddObjectClassSelect.getSelectedValues();
        if (selected.length != 1) {
            return;
        }
        String objName = (String) selected[0];
        LdifObjectClass loc = (LdifObjectClass) mSelectedObjectMap.get(objName);
        loc.setResultSet(new ArrayList());
        mSelectedObjectMap.remove(objName);

        List objAvaiList = copyObjectsList();
        List objSelecteList = new ArrayList();

        Iterator it = mSelectedObjectMap.values().iterator();
        while (it.hasNext()) {
            objSelecteList.add(((LdifObjectClass) it.next()).getName());
        }

//        try {
//            objAvaiList = conn.getObjectNames();
//        } catch (NamingException ex) {
//            Exceptions.printStackTrace(ex);
//        }
        objAvaiList.removeAll(objSelecteList);

        jListAddObjectClassSelect.removeAll();
        jListAddObjectClassSelect.setListData(new Vector(objSelecteList));


        jListAddObjcecClassAvailable.removeAll();
        jListAddObjcecClassAvailable.setListData(new Vector(objAvaiList));

        jListAddObjcecClassAvailable.setSelectedValue(objName, true);

    }//GEN-LAST:event_jButtonAddObjectUnselectActionPerformed

    private void jButtonAddAttributeSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddAttributeSelectActionPerformed
        // TODO add your handling code here:
        Object[] selected = jListAddAttributeAvailable.getSelectedValues();
        int lenth = selected.length;
        if (lenth < 1) {
            return;
        }
        if(this.mMainAttrInAdd.length() < 1){
            JOptionPane.showMessageDialog(null, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.Select_RDN"), "Message", JOptionPane.INFORMATION_MESSAGE);
        }
        if (jListAddObjectClassSelect.getSelectedValues().length > 1) {
            return;
        }
        String objName = (String) jListAddObjectClassSelect.getSelectedValue();
        for (int i = 0; i < lenth; i++) {
            String attrName = (String) selected[i];
            LdifObjectClass loc = (LdifObjectClass) mSelectedObjectMap.get(objName);
            loc.addResultSet(attrName);
        }
        refreshAddTabAttributeAvailableList(objName);
        refreshAddTabSelectedAttrsList();
    }//GEN-LAST:event_jButtonAddAttributeSelectActionPerformed

    private void jButtonAddAttributeUnselectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddAttributeUnselectActionPerformed
        // TODO add your handling code here:
        Object[] selected = jListAddAttributeSelect.getSelectedValues();
        int lenth = selected.length;
        if (lenth < 1) {
            return;
        }
        if (jListAddObjectClassSelect.getSelectedValues().length > 1) {
            return;
        }
        String objName = "";
        for (int i = 0; i < lenth; i++) {
            String str = (String) selected[i];
            int index = str.indexOf(".");
            objName = str.substring(0, index);
            String attrName = str.substring(index + 1);
            LdifObjectClass loc = (LdifObjectClass) mSelectedObjectMap.get(objName);
            List must = loc.getMust();
            boolean flag = false;
            if (null != must & must.size() > 0) {
                for (int j = 0; j < must.size(); j++) {
                    String attr = (String) must.get(j);
                    if (attrName.equals(attr)) {
                        flag = true;
                        break;
                    }
                }
            }
            if (flag) {
                JOptionPane.showMessageDialog(null, "The attribute " + attrName + " is must in ObjectClass " + objName, "Message", JOptionPane.INFORMATION_MESSAGE);
                continue;
            }
            if(this.mMainAttrInAdd.equalsIgnoreCase(str)){
                this.mMainAttrInAdd = "";
				JOptionPane.showMessageDialog(null, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.Select_RDN_Removed"), "Message", JOptionPane.INFORMATION_MESSAGE);
            }
            loc.removeResultSet(attrName);
        }
        refreshAddTabAttributeAvailableList(objName);
        refreshAddTabSelectedAttrsList();
    }//GEN-LAST:event_jButtonAddAttributeUnselectActionPerformed

    @SuppressWarnings("unchecked")
private void jComboBoxDeleteFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxDeleteFilterActionPerformed
// TODO add your handling code here:
        String objName = jComboBoxDeleteFilter.getSelectedItem().toString();
        List attributeList = getUnselectedAttribute(objName);
        jListDeleteFilterAvailable.removeAll();
        jListDeleteFilterAvailable.setListData(new Vector(attributeList));
}//GEN-LAST:event_jComboBoxDeleteFilterActionPerformed

private void jButtonDeleteFilterSelectAndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteFilterSelectAndActionPerformed
// TODO add your handling code here:
    filterAdd("And");
}//GEN-LAST:event_jButtonDeleteFilterSelectAndActionPerformed

private void jButtonDeleteFilterSelectOrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteFilterSelectOrActionPerformed
// TODO add your handling code here:
    filterAdd("Or");
}//GEN-LAST:event_jButtonDeleteFilterSelectOrActionPerformed

private void jButtonDeleteFilterUnselectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteFilterUnselectActionPerformed
// TODO add your handling code here:
    unSelectFilter();
}//GEN-LAST:event_jButtonDeleteFilterUnselectActionPerformed

private void jListDeleteFilterSeletedItemOnSelected(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListDeleteFilterSeletedItemOnSelected
// TODO add your handling code here:
    Object[] selected = jListDeleteFilterSeleted.getSelectedValues();
    if (selected.length == 0) {
        return;
    }
    int[] lastSelectedIndexes = jListDeleteFilterSeleted.getSelectedIndices();
    if (evt.getClickCount() == 1) {
        int currentSelectedIndex = jListDeleteFilterSeleted.locationToIndex(evt.getPoint());
        int currentBracketDepth = 0;
        int beginIndex = currentSelectedIndex;
        int endIndex = currentSelectedIndex;

        String str = DisplayFormatControl.toAttribute((String) jListDeleteFilterSeleted.getSelectedValue());
        int index = str.indexOf(".");
        String obj = str.substring(0, index);
        String att = str.substring(index + 1);

        int endFlag = 0;
        int beginFlag = 0;

        Iterator it1 = mSelectedAttrList.iterator();
        SearchFilterAttribute currentSelectedAttribute = null;
        while (it1.hasNext()) {
            SearchFilterAttribute sfa = (SearchFilterAttribute) it1.next();
            if (sfa.getObjName().equals(obj) & sfa.getAttributeName().equals(att)) {
                currentSelectedAttribute = sfa;
                currentBracketDepth = sfa.getBracketDepth();
                break;
            }
            sfa = null;
        }
        selected = null;
        if (currentSelectedAttribute == null) {
            return;
        }

        if (currentBracketDepth > 0) {
            //get the beginIndex and endIndex;      
            if (!(currentSelectedAttribute.getBracketEndDepth() > 0)) {
                for (int i = currentSelectedIndex + 1; i < mSelectedAttrList.size(); i++) {
                    boolean stopFlag = false;
                    Iterator it = mSelectedAttrList.iterator();
                    while (it.hasNext()) {
                        SearchFilterAttribute sfa = (SearchFilterAttribute) it.next();
                        if (sfa.getPositionIndex() != i) {
                            continue;
                        }
                        if (sfa.getBracketDepth() >= currentBracketDepth) {
                            if (sfa.getBracketEndDepth() <= 0) {

                                endFlag += sfa.getBracketBeginDepth();
                            } else {
                                if (endFlag - sfa.getBracketEndDepth() < 0) {
                                    stopFlag = true;
                                } else {
                                    endFlag -= sfa.getBracketEndDepth();
                                }
                            }
                            endIndex = i;
                        } else {
                            stopFlag = true;
                        }
                        sfa = null;
                        break;
                    }
                    if (stopFlag) {
                        break;
                    }
                }
            }
            if (!(currentSelectedAttribute.getBracketBeginDepth() > 0)) {
                for (int j = currentSelectedIndex - 1; j >= 0; j--) {
                    boolean stopFlag2 = false;
                    Iterator it = mSelectedAttrList.iterator();
                    while (it.hasNext()) {
                        SearchFilterAttribute sfa2 = (SearchFilterAttribute) it.next();
                        if (sfa2.getPositionIndex() != j) {
                            continue;
                        }
                        if (sfa2.getBracketDepth() >= currentBracketDepth) {
                            if (sfa2.getBracketBeginDepth() <= 0) {
//                                beginIndex = j;
                                beginFlag += sfa2.getBracketEndDepth();
                            } else {
                                if (beginFlag - sfa2.getBracketBeginDepth() < 0) {
                                    stopFlag2 = true;
                                } else {

                                    beginFlag -= sfa2.getBracketBeginDepth();
                                }
                            }
                            beginIndex = j;
                            break;
                        } else {
                            stopFlag2 = true;
                        }
                        sfa2 = null;
                        break;
                    }
                    if (stopFlag2) {
                        break;
                    }
                }
            }
            int lastLength = lastSelectedIndexes.length;
            int currentLength = endIndex - beginIndex + 1;
            int[] selectedIndexs = new int[lastLength + currentLength];
            int j;
            for (j = 0; j < currentLength; j++) {
                selectedIndexs[j] = beginIndex + j;
            }
            if (lastLength > 0) {
                for (j = currentLength; j < lastLength + currentLength; j++) {
                    selectedIndexs[j] = lastSelectedIndexes[j - currentLength];
                }
            }
            jListDeleteFilterSeleted.setSelectedIndices(selectedIndexs);
        }
    }
}//GEN-LAST:event_jListDeleteFilterSeletedItemOnSelected

private void jListAddAttributeSelectValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListAddAttributeSelectValueChanged
// TODO add your handling code here:
        Object[] selected = this.jListAddAttributeSelect.getSelectedValues();
        if (selected.length != 1) {
            jButtonAddAttributeUnselect.setEnabled(false);
            return;
        }    
    jButtonAddAttributeUnselect.setEnabled(true);
}//GEN-LAST:event_jListAddAttributeSelectValueChanged

private void jListAddAttributeAvailableValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListAddAttributeAvailableValueChanged
// TODO add your handling code here:
        Object[] selected = jListAddAttributeAvailable.getSelectedValues();
        Object[] objectSelected = this.jListAddObjectClassSelect.getSelectedValues();
        if(objectSelected.length != 1) {
            JOptionPane.showMessageDialog(null, org.openide.util.NbBundle.getMessage(LDAPServerBrowserVisualPanel3.class, "LDAPServerBrowserVisualPanel3.Select_Add_Object"), "Message", JOptionPane.INFORMATION_MESSAGE);            
            jButtonAddAttributeSelect.setEnabled(false);  
            return;
        } else if(selected.length != 1 ){
            jButtonAddAttributeSelect.setEnabled(false);  
            return;
        }    
        jButtonAddAttributeSelect.setEnabled(true);   
        
}//GEN-LAST:event_jListAddAttributeAvailableValueChanged
private void jListSearchFilterSeletedValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListSearchFilterSeletedValueChanged
// TODO add your handling code here:
        Object[] selected = jListSearchFilterSeleted.getSelectedValues();
        if (selected.length != 1) {
            jButtonSearchFilterUnselect.setEnabled(false);  
            return;
        }    
        jButtonSearchFilterUnselect.setEnabled(true);      
}//GEN-LAST:event_jListSearchFilterSeletedValueChanged

private void jListSearchResultSelectedValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListSearchResultSelectedValueChanged
// TODO add your handling code here:
        Object[] selected = jListSearchResultSelected.getSelectedValues();
        if (selected.length != 1) {
            jButtonResultSetUnselect.setEnabled(false);  
            return;
        }    
        jButtonResultSetUnselect.setEnabled(true);     
}//GEN-LAST:event_jListSearchResultSelectedValueChanged

private void jListDeleteFilterAvailableValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListDeleteFilterAvailableValueChanged
// TODO add your handling code here:
        Object[] selected = jListDeleteFilterAvailable.getSelectedValues();
        if (selected.length != 1) {
            jButtonDeleteFilterSelectAnd.setEnabled(false);
            jButtonDeleteFilterSelectOr.setEnabled(false);
            return;
        }    
        jButtonDeleteFilterSelectAnd.setEnabled(true); 
        jButtonDeleteFilterSelectOr.setEnabled(true);
}//GEN-LAST:event_jListDeleteFilterAvailableValueChanged

private void jListDeleteFilterSeletedValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListDeleteFilterSeletedValueChanged
// TODO add your handling code here:
        Object[] selected = jListDeleteFilterSeleted.getSelectedValues();
        if (selected.length != 1) {
            jButtonDeleteFilterUnselect.setEnabled(false);
            return;
        }    
        jButtonDeleteFilterUnselect.setEnabled(true); 
}//GEN-LAST:event_jListDeleteFilterSeletedValueChanged

private void jListUpdateFilterAvailableValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListUpdateFilterAvailableValueChanged
// TODO add your handling code here:
        Object[] selected = jListUpdateFilterAvailable.getSelectedValues();
        if (selected.length != 1) {
            jButtonUpdateFilterSelectAnd.setEnabled(false);
            jButtonUpdateFilterSelectOr.setEnabled(false);
            return;
        }    
        jButtonUpdateFilterSelectAnd.setEnabled(true); 
        jButtonUpdateFilterSelectOr.setEnabled(true);    
}//GEN-LAST:event_jListUpdateFilterAvailableValueChanged

private void jListUpdateFilterSeletedValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListUpdateFilterSeletedValueChanged
// TODO add your handling code here:
        Object[] selected = jListUpdateFilterSeleted.getSelectedValues();
        if (selected.length != 1) {
            jButtonUpdateFilterUnselect.setEnabled(false);
            return;
        }    
        jButtonUpdateFilterUnselect.setEnabled(true);     
}//GEN-LAST:event_jListUpdateFilterSeletedValueChanged

private void jListUpdateSetAvailableValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListUpdateSetAvailableValueChanged
// TODO add your handling code here:
        Object[] selected = jListUpdateSetAvailable.getSelectedValues();
        if (selected.length != 1) {
            jButtonUpdateSetSelect.setEnabled(false);
            return;
        }    
        jButtonUpdateSetSelect.setEnabled(true);     
}//GEN-LAST:event_jListUpdateSetAvailableValueChanged

private void jListUpdateSetSelectedValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListUpdateSetSelectedValueChanged
// TODO add your handling code here:
        Object[] selected = jListUpdateSetSelected.getSelectedValues();
        if (selected.length != 1) {
            jButtonUpdateSetUnselect.setEnabled(false);
            return;
        }    
        jButtonUpdateSetUnselect.setEnabled(true);     
}//GEN-LAST:event_jListUpdateSetSelectedValueChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddAttributeSelect;
    private javax.swing.JButton jButtonAddAttributeUnselect;
    private javax.swing.JButton jButtonAddObjectSelect;
    private javax.swing.JButton jButtonAddObjectUnselect;
    private javax.swing.JButton jButtonDeleteFilterSelectAnd;
    private javax.swing.JButton jButtonDeleteFilterSelectOr;
    private javax.swing.JButton jButtonDeleteFilterUnselect;
    private javax.swing.JButton jButtonResultSetSelect;
    private javax.swing.JButton jButtonResultSetUnselect;
    private javax.swing.JButton jButtonSearchFilterSelectAnd;
    private javax.swing.JButton jButtonSearchFilterSelectOr;
    private javax.swing.JButton jButtonSearchFilterUnselect;
    private javax.swing.JButton jButtonUpdateFilterSelectAnd;
    private javax.swing.JButton jButtonUpdateFilterSelectOr;
    private javax.swing.JButton jButtonUpdateFilterUnselect;
    private javax.swing.JButton jButtonUpdateSetSelect;
    private javax.swing.JButton jButtonUpdateSetUnselect;
    private javax.swing.JComboBox jComboBoxDeleteFilter;
    private javax.swing.JComboBox jComboBoxResultSet;
    private javax.swing.JComboBox jComboBoxSearchFilter;
    private javax.swing.JComboBox jComboBoxSortType;
    private javax.swing.JComboBox jComboBoxUpdateFilter;
    private javax.swing.JComboBox jComboBoxUpdateSet;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelAddBaseDN;
    private javax.swing.JLabel jLabelDeleteBaseDN;
    private javax.swing.JLabel jLabelRecordsPerPage;
    private javax.swing.JLabel jLabelSearchBaseDN;
    private javax.swing.JLabel jLabelUpdateBaseDN;
    private javax.swing.JList jListAddAttributeAvailable;
    private javax.swing.JList jListAddAttributeSelect;
    private javax.swing.JList jListAddObjcecClassAvailable;
    private javax.swing.JList jListAddObjectClassSelect;
    private javax.swing.JList jListDeleteFilterAvailable;
    private javax.swing.JList jListDeleteFilterSeleted;
    private javax.swing.JList jListSearchFilterAvailable;
    private javax.swing.JList jListSearchFilterSeleted;
    private javax.swing.JList jListSearchResultAvailable;
    private javax.swing.JList jListSearchResultSelected;
    private javax.swing.JList jListUpdateFilterAvailable;
    private javax.swing.JList jListUpdateFilterSeleted;
    private javax.swing.JList jListUpdateSetAvailable;
    private javax.swing.JList jListUpdateSetSelected;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPopupMenu jPopupMenu;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JPopupMenu jPopupMenu2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane15;
    private javax.swing.JScrollPane jScrollPane16;
    private javax.swing.JScrollPane jScrollPane17;
    private javax.swing.JScrollPane jScrollPane18;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextFieldAddBaseDN;
    private javax.swing.JTextField jTextFieldDeleteBaseDN;
    private javax.swing.JTextField jTextFieldRecordsPerPage;
    private javax.swing.JTextField jTextFieldSearchBaseDN;
    private javax.swing.JTextField jTextFieldSortByAttribute;
    private javax.swing.JTextField jTextFieldUpdateBaseDN;
    private javax.swing.JTree jTreeAdd;
    private javax.swing.JTree jTreeDelete;
    private javax.swing.JTree jTreeSearch;
    private javax.swing.JTree jTreeUpdate;
    // End of variables declaration//GEN-END:variables
}


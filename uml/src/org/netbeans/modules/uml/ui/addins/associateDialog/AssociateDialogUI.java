/*
 * AssociateDialogUI.java
 *
 * Created on September 29, 2008, 11:27 AM
 */

package org.netbeans.modules.uml.ui.addins.associateDialog;

import java.awt.Dimension;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IReference;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IRelationFactory;
import org.netbeans.modules.uml.core.metamodel.infrastructure.RelationFactory;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.addins.associateDialog.AssociateTableModel;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.commondialogs.IErrorDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.SwingErrorDialog;
import org.netbeans.modules.uml.ui.support.finddialog.FindController;
import org.netbeans.modules.uml.ui.support.finddialog.FindResults;
import org.netbeans.modules.uml.ui.support.finddialog.FindUtilities;
import org.netbeans.modules.uml.ui.support.finddialog.IFindResults;
import org.netbeans.modules.uml.ui.swing.finddialog.FindTableModel;
import org.netbeans.modules.uml.util.DummyCorePreference;
import org.openide.util.NbPreferences;

/**
 *
 * @author  jyothi
 */
public class AssociateDialogUI extends javax.swing.JDialog implements IAssociateDlgGUI {

    private boolean m_LockIsChecked = false;
    
    /** Creates new form AssociateDialogUI */
    public AssociateDialogUI(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        m_Controller = new FindController();
        initComponents();
        initTextFieldListeners();
        selectionListener = new SelectionListener();
        m_ResultsTable.getSelectionModel().addListSelectionListener(selectionListener);
        initDialog();
        pack();  // pack() should come before center(parent)
//        center(parent);
    }

     private void initTextFieldListeners()
    {
        class TextChangeListener implements DocumentListener
        {
            private JTextField textField;
            TextChangeListener(JTextField textField)
            {
                this.textField = textField;
            }
            public void changedUpdate(DocumentEvent e)
            {
                documentChanged();
            }
            public void insertUpdate(DocumentEvent e)
            {
                documentChanged();
            }
            public void removeUpdate(DocumentEvent e)
            {
                documentChanged();
            }
            private void documentChanged()
            {
                updateState(textField);
            }
        }
        ((JTextField)m_FindCombo.getEditor().getEditorComponent()).getDocument().
                addDocumentListener(new TextChangeListener(
                (JTextField)m_FindCombo.getEditor().getEditorComponent()));
    }
    
    private void updateState(JTextField textField)
    {
        if (update)
            m_FindButton.setEnabled(!"".equals(textField.getText().trim()));
    }
    
    private Dimension getMaxButtonWidth()
    {
        Dimension ret = null;
        Dimension d = m_FindButton.getPreferredSize();
        double max  = d.width;
        
        d = m_CloseButton.getPreferredSize();
        if(d.width > max)
        {
            max = d.width;
            ret = d;
        }
        d = m_AssociateButton.getPreferredSize();
        if(d.width > max)
        {
            max = d.width;
            ret = d;
        }
        d = m_AssociateAllButton.getPreferredSize();
        if(d.width > max)
        {
            max = d.width;
            ret = d;
        }
        return ret;
    }
    
    private void onLoadExternalCheck(java.awt.event.ActionEvent evt)
    {
        Object obj = evt.getSource();
        if (obj instanceof JCheckBox)
        {
            JCheckBox box = (JCheckBox)obj;
            boolean checkboxState = box.isSelected();
            if (checkboxState)
            {
                m_Controller.setExternalLoad(true);
            }
            else
            {
                m_Controller.setExternalLoad(false);
            }
        }
    }
    
    private void onXPathCheck(java.awt.event.ActionEvent evt)
    {
        Object obj = evt.getSource();
        if (obj instanceof JCheckBox)
        {
            JCheckBox box = (JCheckBox)obj;
            boolean checkboxState = box.isSelected();
            if (checkboxState)
            {
                m_Controller.setKind(1);
                m_Controller.setCaseSensitive(true);
                m_MatchCaseCheck.setEnabled(false);
                m_SearchDescriptionsRadio.setEnabled(false);
                m_SearchElementsRadio.setEnabled(false);
                m_SearchAliasCheck.setEnabled(false);
                m_WholeWordCheck.setEnabled(false);
            }
            else
            {
                m_Controller.setKind(0);
                m_Controller.setCaseSensitive(m_MatchCaseCheck.isSelected());
                m_MatchCaseCheck.setEnabled(true);
                m_SearchDescriptionsRadio.setEnabled(true);
                m_SearchElementsRadio.setEnabled(true);
                m_SearchAliasCheck.setEnabled(true);
                m_WholeWordCheck.setEnabled(true);
            }
        }
    }
    
    private void onAliasCheck(java.awt.event.ActionEvent evt)
    {
        Object obj = evt.getSource();
        if (obj instanceof JCheckBox)
        {
            JCheckBox box = (JCheckBox)obj;
            boolean checkboxState = box.isSelected();
            if (checkboxState)
            {
                m_Controller.setSearchAlias(true);
                m_SearchElementsRadio.setSelected(true);
                m_SearchDescriptionsRadio.setSelected(false);
                m_SearchDescriptionsRadio.setEnabled(false);
            }
            else
            {
                m_Controller.setSearchAlias(false);
                m_SearchDescriptionsRadio.setEnabled(true);
            }
        }
    }
    
    private void onWholeWordCheck(java.awt.event.ActionEvent evt)
    {
        Object obj = evt.getSource();
        if (obj instanceof JCheckBox)
        {
            JCheckBox box = (JCheckBox)obj;
            boolean checkboxState = box.isSelected();
            if (checkboxState)
            {
                m_Controller.setWholeWordSearch(true);
            }
            else
            {
                m_Controller.setWholeWordSearch(false);
                
            }
        }
    }
    
    private void onMatchCaseCheck(java.awt.event.ActionEvent evt)
    {
        Object obj = evt.getSource();
        if (obj instanceof JCheckBox)
        {
            Preferences prefs = NbPreferences.forModule (DummyCorePreference.class) ;
            JCheckBox box = (JCheckBox)obj;
            boolean checkboxState = box.isSelected();
            if (checkboxState)
            {
                m_Controller.setCaseSensitive(true);
                prefs.put ("UML_ShowMe_Allow_Lengthy_Searches", "PSK_NEVER") ;
            }
            else
            {
                m_Controller.setCaseSensitive(false);
                String find = prefs.get ("UML_ShowMe_Allow_Lengthy_Searches", "PSK_ASK");
                if (find.equals("PSK_NEVER"))
                    prefs.put("UML_ShowMe_Allow_Lengthy_Searches", "PSK_ALWAYS");
            }
        }
    }
    
    private void onSearchElementsRadio(java.awt.event.ActionEvent evt)
    {
        Object obj = evt.getSource();
        if (obj instanceof JRadioButton)
        {
            m_Controller.setResultType(0);
            m_SearchElementsRadio.setSelected(true);
            m_SearchDescriptionsRadio.setSelected(false);
            m_SearchAliasCheck.setEnabled(true);
        }
    }
    
    private void onSearchDescriptionsRadio(java.awt.event.ActionEvent evt)
    {
        Object obj = evt.getSource();
        if (obj instanceof JRadioButton)
        {
            m_Controller.setResultType(1);
            m_SearchDescriptionsRadio.setSelected(true);
            m_SearchElementsRadio.setSelected(false);
            m_SearchAliasCheck.setSelected(false);
            m_SearchAliasCheck.setEnabled(false);
        }
    }
    
    private void onNavigateCheck(java.awt.event.ActionEvent evt)
    {
        Object obj = evt.getSource();
        if (obj instanceof JCheckBox)
        {
            JCheckBox box = (JCheckBox)obj;
            boolean checkboxState = box.isSelected();
            if (checkboxState)
            {
                m_LockIsChecked = true;
            }
            else
            {
                m_LockIsChecked = false;
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.addins.associateDialog.IAssociateDlgGUI#display()
         */
    public void display()
    {
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.addins.associateDialog.IAssociateDlgGUI#getResults()
         */
    public IFindResults getResults()
    {
        return m_Results;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.addins.associateDialog.IAssociateDlgGUI#setResults(org.netbeans.modules.uml.ui.support.finddialog.IFindResults)
         */
    public void setResults(IFindResults newVal)
    {
        m_Results = newVal;
        
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.addins.associateDialog.IAssociateDlgGUI#getProject()
         */
    public IProject getProject()
    {
        return m_Project;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.addins.associateDialog.IAssociateDlgGUI#setProject(org.netbeans.modules.uml.core.metamodel.structure.IProject)
         */
    public void setProject(IProject newVal)
    {
        m_Project = newVal;
    }
    private void onFindButton(java.awt.event.ActionEvent evt)
    {
        Object obj = evt.getSource();
        if (obj instanceof JButton)
        {
            onFindButton();
        }
    }
    
    
    private void onFindButton()
    {
        m_Status.setText("");
        update = false;
        clearGrid();
        // get the string that the user typed in
        String searchStr = (String) m_FindCombo.getSelectedItem();
        
        // Save the values of the search combo
        FindUtilities.saveSearchString("LastAssociateStrings", m_FindCombo); // NOI18N
        // reset what is in the search combo
        FindUtilities.populateComboBoxes("LastAssociateStrings", m_FindCombo); // NOI18N
        FindUtilities.startWaitCursor(getContentPane());
        // do the search
        m_Controller.setSearchString(searchStr);
        FindResults pResults = new FindResults();
        try
        {
            m_Controller.search2(m_Project, pResults);
            if (pResults != null)
            {
                ETList < IElement > pElements = pResults.getElements();
                ETList < IProxyDiagram > pDiagrams = pResults.getDiagrams();
                if ((pElements != null) && (pDiagrams != null))
                {
                    int count = pElements.size();
                    int countD = pDiagrams.size();
                    if (count > 0 || countD > 0)
                    {
                        // show the results
                        ETList < Object > findResults =
                                FindUtilities.loadResultsIntoArray(pResults);
                        AssociateTableModel model =
                                new AssociateTableModel(this, findResults);
                        m_ResultsTable.setModel(model);
                        m_AssociateAllButton.setEnabled(true);
                        
                        long totalC = count + countD;
                        String strMsg = totalC + " ";
                        strMsg += FindUtilities.translateString("IDS_NUMFOUND"); // NOI18N
                        m_Status.setText(strMsg);
                        enableLockCheck();
                        //
                        // This is special code to aid in the automating testing.  We had no way to access
                        // the information in the grid from the automated scripts and/or VisualTest, so
                        // if a flag is set in the registry, we will dump the results of the grid to a
                        // specified file
                        //
                                                /* TODO
                                                if( GETDEBUGFLAG_RELEASE(_T("DumpGridResults"), 0))
                                                {
                                                        CComBSTR file = CRegistry::GetItem( CString(_T("DumpGridResultsFile")), CString(_T("")));
                                                                if (file.Length())
                                                                {
                                                                        m_FlexGrid->SaveGrid(file, flexFileCommaText, CComVariant(FALSE));
                                                                }
                                                        }
                                                 */
                    }
                    else
                    {
                        String noneStr =
                                FindUtilities.translateString("IDS_NONEFOUND"); // NOI18N
                        m_Status.setText(noneStr);
                    }
                }
                else
                {
                    String canStr =
                            FindUtilities.translateString("IDS_CANCELLED"); // NOI18N
                    m_Status.setText(canStr);
                }
            }
            else
            {
                String str2 = FindUtilities.translateString("IDS_NONEFOUND2"); // NOI18N
                m_Status.setText(str2);
            }
        }
        catch (Exception e)
        {
            String msg;
            
            if (xPathCheck.isSelected())
                msg = FindUtilities.translateString("IDS_ERROR1");
            else
                msg = FindUtilities.translateString("IDS_NONEFOUND");
            
            m_Status.setText(msg);
        }
        m_FindCombo.setSelectedItem(searchStr);
        FindUtilities.endWaitCursor(getContentPane());
        
        update = true;
        m_FindCombo.getEditor().selectAll();
    }
    
    
    private void onAssociateButton(java.awt.event.ActionEvent evt)
    {
        Object obj = evt.getSource();
        if (obj instanceof JButton)
        {
            m_Status.setText("");
            FindResults pResults = new FindResults();
            if (pResults != null)
            {
                loadResultsFromGrid(pResults, true);
                ETList<IElement> pElements = pResults.getElements();
                ETList<IProxyDiagram> pDiagrams = pResults.getDiagrams();
                if ( (pElements != null) && (pDiagrams != null))
                {
                    int count = pElements.size();
                    int countD = pDiagrams.size();
                    if (count > 0 || countD > 0)
                    {
                        associate(pResults);
                        clearGrid();
                    }
                    else
                    {
                        // no items selected in the grid
                        String noneStr = FindUtilities.translateString("IDS_NOITEMSSELECTED"); // NOI18N
                        String str2 = FindUtilities.translateString("IDS_PROJNAME2"); // NOI18N
                        IErrorDialog pTemp = new SwingErrorDialog(this);
                        if (pTemp != null)
                        {
                            pTemp.display(noneStr, MessageIconKindEnum.EDIK_ICONINFORMATION, str2);
                        }
                    }
                }
            }
        }
    }
    
    
    private void onAssociateAllButton(java.awt.event.ActionEvent evt)
    {
        Object obj = evt.getSource();
        if (obj instanceof JButton)
        {
            m_Status.setText("");
            FindResults pResults = new FindResults();
            if (pResults != null)
            {
                loadResultsFromGrid(pResults, false);
                ETList<IElement> pElements = pResults.getElements();
                ETList<IProxyDiagram> pDiagrams = pResults.getDiagrams();
                if ( (pElements != null) && (pDiagrams != null))
                {
                    int count = pElements.size();
                    int countD = pDiagrams.size();
                    if (count > 0 || countD > 0)
                    {
                        // disable replace buttons in case the search fails
                        m_AssociateButton.setEnabled(false);
                        m_AssociateAllButton.setEnabled(false);
                        m_NavigateCheck.setEnabled(false);
                        associate(pResults);
                        clearGrid();
                    }
                    else
                    {
                        // no items selected in the grid
                        String noneStr = FindUtilities.translateString("IDS_NOITEMSSELECTED"); // NOI18N
                        String str2 = FindUtilities.translateString("IDS_PROJNAME2"); // NOI18N
                        IErrorDialog pTemp = new SwingErrorDialog(this);
                        if (pTemp != null)
                        {
                            pTemp.display(noneStr, MessageIconKindEnum.EDIK_ICONINFORMATION, str2);
                        }
                    }
                }
            }
        }
    }
    
    /**
     *
     * Determines whether or not the edit lock check box should be enabled or not.
     * We are only going to allow it if what was selected in the tree or draw area to begin
     * the associate are elements (no diagrams).
     *
     *
     * @return
     *
     */
    private void enableLockCheck()
    {
        if (m_Results != null)
        {
            ETList < IProxyDiagram > pDiagrams = m_Results.getDiagrams();
            if (pDiagrams != null)
            {
                int count = pDiagrams.size();
                if (count == 0)
                {
                    m_NavigateCheck.setEnabled(true);
                }
            }
        }
    }
    
    
    private void loadResultsFromGrid(FindResults pResults, boolean bSelect)
    {
        if (pResults != null)
        {
            // get the elements array from the results object
            ETList<IElement> pElements = pResults.getElements();
            // get the diagrams array from the results object
            ETList<IProxyDiagram> pDiagrams = pResults.getDiagrams();
            if ( (pElements != null) && (pDiagrams != null))
            {
                if (bSelect)
                {
                    // loop through the information in the table
                    int[] selRows = m_ResultsTable.getSelectedRows();
                    for (int x = 0; x < selRows.length; x++)
                    {
                        int selRow = selRows[x];
                        AssociateTableModel model = (AssociateTableModel)m_ResultsTable.getModel();
//						FindTableModel model = (FindTableModel)m_ResultsTable.getModel();
                        if (model != null)
                        {
                            IElement pElement = model.getElementAtRow(selRow);
                            if (pElement != null)
                            {
                                pElements.add(pElement);
                            }
                            else
                            {
                                IProxyDiagram pDiagram = model.getDiagramAtRow(selRow);
                                if (pDiagram != null)
                                {
                                    pDiagrams.add(pDiagram);
                                }
                            }
                        }
                    }
                }
                else
                {
                    int rows = m_ResultsTable.getRowCount();
                    for (int x = 0; x < rows; x++)
                    {
                        AssociateTableModel model = (AssociateTableModel)m_ResultsTable.getModel();
                        if (model != null)
                        {
                            IElement pElement = model.getElementAtRow(x);
                            if (pElement != null)
                            {
                                pElements.add(pElement);
                            }
                            else
                            {
                                IProxyDiagram pDiagram = model.getDiagramAtRow(x);
                                if (pDiagram != null)
                                {
                                    pDiagrams.add(pDiagram);
                                }
                            }
                        }
                    }
                    
                }
            }
        }
    }
    
    /**
     * Associate the member elements of this dialog to those passed into this routine
     *
     * @param[in] results   The array of elements that should be associated
     *
     * @return HRESULT
     *
     */
    private void associate(IFindResults pResults)
    {
        if (m_Results != null && pResults != null)
        {
            // the relation factory is going to handle the creation of the
            // Referencing/Referred relationships
            IRelationFactory relFactory = new RelationFactory();
            if (relFactory != null)
            {
                // first we will "associate" any symbol to symbol relationships
                ETList <IElement> pElements = m_Results.getElements();
                if (pElements != null)
                {
                    int count = pElements.size();
                    for (int x = 0; x < count; x++)
                    {
                        IElement pRefEle = pElements.get(x);
                        if (pRefEle != null)
                        {
                            // symbol to symbol
                            ETList < IElement > pElements2 = pResults.getElements();
                            if (pElements2 != null)
                            {
                                int count2 = pElements2.size();
                                for (int y = 0; y < count2; y++)
                                {
                                    IElement pEle = pElements2.get(y);
                                    if (pEle != null)
                                    {
                                        IReference pRef = relFactory.createReference(pRefEle, pEle);
                                    }
                                }
                            }
                            // symbol to diagram
                            ETList < IProxyDiagram > pDiagrams = pResults.getDiagrams();
                            if (pDiagrams != null)
                            {
                                int count2 = pDiagrams.size();
                                for (int y = 0; y < count2; y++)
                                {
                                    IProxyDiagram pDiagram = pDiagrams.get(y);
                                    if (pDiagram != null)
                                    {
                                        pDiagram.addAssociatedElement(pRefEle);
                                    }
                                }
                            }
                        }
                    }
                }
                // Now we will process the diagrams
                // first we will "associate" any symbol to symbol relationships
                ETList < IProxyDiagram > pDiagrams = m_Results.getDiagrams();
                if (pDiagrams != null)
                {
                    int count = pDiagrams.size();
                    for (int x = 0; x < count; x++)
                    {
                        IProxyDiagram pRef = pDiagrams.get(x);
                        if (pRef != null)
                        {
                            // diagram to symbol
                            ETList <IElement> pElements2 = pResults.getElements();
                            if (pElements2 != null)
                            {
                                int count2 = pElements2.size();
                                for (int y = 0; y < count2; y++)
                                {
                                    IElement pEle = pElements2.get(y);
                                    if (pEle != null)
                                    {
                                        pRef.addAssociatedElement(pEle);
                                    }
                                }
                            }
                            // diagram to diagram
                            ETList <IProxyDiagram> pDiagrams3 = pResults.getDiagrams();
                            if (pDiagrams3 != null)
                            {
                                int count2 = pDiagrams3.size();
                                for (int y = 0; y < count2; y++)
                                {
                                    IProxyDiagram pDiagram = pDiagrams3.get(y);
                                    if (pDiagram != null)
                                    {
                                        pRef.addDualAssociatedDiagrams(pRef, pDiagram);
                                    }
                                }
                            }
                        }
                    }
                }
                // now that we are done associating elements, check to see if the user wants
                // to lock the edit mode of the element(s) that are the start of the associated process
                processLockEdit();
            }
        }
    }
    
    
    /**
     *
     * If the user has told us to lock the edit mode of the selected associated element(s),
     * then we will go through all of its presentation elements and set the lock edit flag
     * on them.
     *
     *
     * @return
     *
     */
    private void processLockEdit()
    {
        if (m_LockIsChecked)
        {
            if (m_Results != null)
            {
                // get the elements that started the associate process
                ETList < IElement > pElements = m_Results.getElements();
                if (pElements != null)
                {
                    // loop through
                    int count = pElements.size();
                    for (int x = 0; x < count; x++)
                    {
                        // got one
                        IElement pRefEle = pElements.get(x);
                        if (pRefEle != null)
                        {
                            // now get its presentation elements
                            ETList < IPresentationElement > pPresEles = pRefEle.getPresentationElements();
                            if (pPresEles != null)
                            {
                                // loop through
                                int presCnt = pPresEles.size();
                                for (int y = 0; y < presCnt; y++)
                                {
                                    IPresentationElement pPresEle = pPresEles.get(y);
                                    if (pPresEle != null)
                                    {
                                        // the lock edit flag is only available for nodes
                                        // TODO: meteora
//                                        if (pPresEle instanceof INodePresentation)
//                                        {
//                                            INodePresentation pNode = (INodePresentation)pPresEle;
//                                            if (pNode != null)
//                                            {
//                                                pNode.setLockEdit(true);
//                                            }
//                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    
    private void clearGrid()
    {
        m_ResultsTable.setModel(new FindTableModel());
        m_AssociateAllButton.setEnabled(false);
    }
    
    private void initDialog()
    {
        m_Status.setText("");
        FindUtilities.populateComboBoxes("LastAssociateStrings", m_FindCombo); // NOI18N
        m_FindCombo.getEditor().selectAll();
    }
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt)
    {
        setVisible(false);
        dispose();
    }
    
    
    private boolean isMatchCase()
    {
        return !"PSK_ALWAYS".equals(ProductHelper.getPreferenceManager().getPreferenceValue("FindDialog", "LongSearch"));
    }
    
    private class SelectionListener implements ListSelectionListener
    {
        public void valueChanged(ListSelectionEvent e)
        {
            m_AssociateButton.setEnabled(m_ResultsTable.getSelectedRows().length>0);
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
        jPanel2 = new javax.swing.JPanel();
        m_FindButton = new javax.swing.JButton();
        m_CloseButton = new javax.swing.JButton();
        m_AssociateButton = new javax.swing.JButton();
        m_AssociateAllButton = new javax.swing.JButton();
        findLabel = new javax.swing.JLabel();
        m_FindCombo = new javax.swing.JComboBox();
        m_MatchCaseCheck = new javax.swing.JCheckBox();
        xPathCheck = new javax.swing.JCheckBox();
        m_WholeWordCheck = new javax.swing.JCheckBox();
        m_SearchAliasCheck = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        m_SearchElementsRadio = new javax.swing.JRadioButton();
        m_SearchDescriptionsRadio = new javax.swing.JRadioButton();
        m_ResultsLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        m_ResultsTable = new javax.swing.JTable();
        m_NavigateCheck = new javax.swing.JCheckBox();
        m_Status = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        org.openide.awt.Mnemonics.setLocalizedText(m_FindButton, org.openide.util.NbBundle.getMessage(AssociateDialogUI.class, "AssociateDialogUI.m_FindButton.text")); // NOI18N
        m_FindButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_FindButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(m_CloseButton, org.openide.util.NbBundle.getMessage(AssociateDialogUI.class, "AssociateDialogUI.m_CloseButton.text")); // NOI18N
        m_CloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_CloseButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(m_AssociateButton, org.openide.util.NbBundle.getMessage(AssociateDialogUI.class, "AssociateDialogUI.m_AssociateButton.text")); // NOI18N
        m_AssociateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_AssociateButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(m_AssociateAllButton, org.openide.util.NbBundle.getMessage(AssociateDialogUI.class, "AssociateDialogUI.m_AssociateAllButton.text")); // NOI18N
        m_AssociateAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_AssociateAllButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(m_AssociateAllButton)
                    .add(m_AssociateButton)
                    .add(m_FindButton)
                    .add(m_CloseButton)))
        );

        jPanel2Layout.linkSize(new java.awt.Component[] {m_AssociateAllButton, m_AssociateButton, m_CloseButton, m_FindButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(m_FindButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(m_CloseButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 265, Short.MAX_VALUE)
                .add(m_AssociateButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(m_AssociateAllButton))
        );

        m_FindButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AssociateDialogUI.class, "ACSD_FindDialog")); // NOI18N
        m_CloseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AssociateDialogUI.class, "ACSD_Close")); // NOI18N
        m_AssociateButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AssociateDialogUI.class, "ACSD_Associate")); // NOI18N
        m_AssociateAllButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AssociateDialogUI.class, "ACSD_AssociateAll")); // NOI18N

        findLabel.setLabelFor(m_FindCombo);
        org.openide.awt.Mnemonics.setLocalizedText(findLabel, org.openide.util.NbBundle.getMessage(AssociateDialogUI.class, "AssociateDialogUI.findLabel.text")); // NOI18N

        m_FindCombo.setEditable(true);
        m_FindCombo.setMaximumRowCount(10);

        m_MatchCaseCheck.setSelected(isMatchCase());
        org.openide.awt.Mnemonics.setLocalizedText(m_MatchCaseCheck, org.openide.util.NbBundle.getMessage(AssociateDialogUI.class, "AssociateDialogUI.m_MatchCaseCheck.text")); // NOI18N
        m_MatchCaseCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_MatchCaseCheckActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(xPathCheck, org.openide.util.NbBundle.getMessage(AssociateDialogUI.class, "AssociateDialogUI.xPathCheck.text")); // NOI18N
        xPathCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xPathCheckActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(m_WholeWordCheck, org.openide.util.NbBundle.getMessage(AssociateDialogUI.class, "AssociateDialogUI.m_WholeWordCheck.text")); // NOI18N
        m_WholeWordCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_WholeWordCheckActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(m_SearchAliasCheck, org.openide.util.NbBundle.getMessage(AssociateDialogUI.class, "AssociateDialogUI.m_SearchAliasCheck.text")); // NOI18N
        m_SearchAliasCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_SearchAliasCheckActionPerformed(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Search in"));

        org.openide.awt.Mnemonics.setLocalizedText(m_SearchElementsRadio, org.openide.util.NbBundle.getMessage(AssociateDialogUI.class, "AssociateDialogUI.m_SearchElementsRadio.text")); // NOI18N
        m_SearchElementsRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_SearchElementsRadioActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(m_SearchDescriptionsRadio, org.openide.util.NbBundle.getMessage(AssociateDialogUI.class, "AssociateDialogUI.m_SearchDescriptionsRadio.text")); // NOI18N
        m_SearchDescriptionsRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_SearchDescriptionsRadioActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(m_SearchElementsRadio)
                    .add(m_SearchDescriptionsRadio))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(m_SearchElementsRadio)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(m_SearchDescriptionsRadio)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        m_SearchElementsRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AssociateDialogUI.class, "ACSD_Search_Element")); // NOI18N
        m_SearchDescriptionsRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AssociateDialogUI.class, "ACSD_Search_Description")); // NOI18N

        m_ResultsLabel.setLabelFor(m_ResultsTable);
        org.openide.awt.Mnemonics.setLocalizedText(m_ResultsLabel, org.openide.util.NbBundle.getMessage(AssociateDialogUI.class, "AssociateDialogUI.m_ResultsLabel.text")); // NOI18N

        m_ResultsTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        m_ResultsTable.setSelectionBackground(new java.awt.Color(255, 255, 255));
        AssociateTableModel model = new AssociateTableModel(this);
        m_ResultsTable = new JAssociateTable(model, this);
        m_ResultsTable.getSelectionModel().addListSelectionListener(selectionListener);
        jScrollPane1.setViewportView(m_ResultsTable);

        org.openide.awt.Mnemonics.setLocalizedText(m_NavigateCheck, org.openide.util.NbBundle.getMessage(AssociateDialogUI.class, "AssociateDialogUI.m_NavigateCheck.text")); // NOI18N
        m_NavigateCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_NavigateCheckActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 442, Short.MAX_VALUE)
                    .add(m_NavigateCheck)
                    .add(m_Status)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(findLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(m_FindCombo, 0, 372, Short.MAX_VALUE))
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jPanel1Layout.createSequentialGroup()
                                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(jPanel1Layout.createSequentialGroup()
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(m_WholeWordCheck))
                                            .add(m_MatchCaseCheck))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(xPathCheck)
                                            .add(m_SearchAliasCheck)))
                                    .add(m_ResultsLabel))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .add(6, 6, 6)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(17, 17, 17))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(13, 13, 13)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(findLabel)
                            .add(m_FindCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(m_MatchCaseCheck)
                                    .add(xPathCheck))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(m_SearchAliasCheck)
                                    .add(m_WholeWordCheck))
                                .add(18, 18, 18)
                                .add(m_ResultsLabel))
                            .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
                        .add(18, 18, 18)
                        .add(m_NavigateCheck)
                        .add(7, 7, 7)
                        .add(m_Status))
                    .add(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        findLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AssociateDialogUI.class, "IDS_FINDWHAT")); // NOI18N
        m_FindCombo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AssociateDialogUI.class, "ACSD_FindWhatComboBox")); // NOI18N
        m_FindCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AssociateDialogUI.class, "ACSD_FindWhatComboBox")); // NOI18N
        m_MatchCaseCheck.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AssociateDialogUI.class, "ACSD_MatchCaseCheck")); // NOI18N
        xPathCheck.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AssociateDialogUI.class, "ACSD_XpathCheck")); // NOI18N
        m_WholeWordCheck.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AssociateDialogUI.class, "ACSD_WholeWordCheck")); // NOI18N
        m_SearchAliasCheck.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AssociateDialogUI.class, "ACSD_SearchAliasCheck")); // NOI18N
        m_ResultsLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AssociateDialogUI.class, "LBL_SearchResult")); // NOI18N
        m_NavigateCheck.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AssociateDialogUI.class, "IDS_NAVIGATE")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void m_FindButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_FindButtonActionPerformed
onFindButton(evt);
}//GEN-LAST:event_m_FindButtonActionPerformed

private void m_CloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_CloseButtonActionPerformed
setVisible(false);
    dispose();
}//GEN-LAST:event_m_CloseButtonActionPerformed

private void m_AssociateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_AssociateButtonActionPerformed
onAssociateButton(evt);
}//GEN-LAST:event_m_AssociateButtonActionPerformed

private void m_AssociateAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_AssociateAllButtonActionPerformed
onAssociateAllButton(evt);
}//GEN-LAST:event_m_AssociateAllButtonActionPerformed

private void m_MatchCaseCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_MatchCaseCheckActionPerformed
onMatchCaseCheck(evt);
}//GEN-LAST:event_m_MatchCaseCheckActionPerformed

private void xPathCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xPathCheckActionPerformed
onXPathCheck(evt);
}//GEN-LAST:event_xPathCheckActionPerformed

private void m_WholeWordCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_WholeWordCheckActionPerformed
onWholeWordCheck(evt);
}//GEN-LAST:event_m_WholeWordCheckActionPerformed

private void m_SearchAliasCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_SearchAliasCheckActionPerformed
onAliasCheck(evt);
}//GEN-LAST:event_m_SearchAliasCheckActionPerformed

private void m_SearchElementsRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_SearchElementsRadioActionPerformed
onSearchElementsRadio(evt);
}//GEN-LAST:event_m_SearchElementsRadioActionPerformed

private void m_SearchDescriptionsRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_SearchDescriptionsRadioActionPerformed
onSearchDescriptionsRadio(evt);
}//GEN-LAST:event_m_SearchDescriptionsRadioActionPerformed

private void m_NavigateCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_NavigateCheckActionPerformed
onNavigateCheck(evt);
}//GEN-LAST:event_m_NavigateCheckActionPerformed

//    /**
//    * @param args the command line arguments
//    */
//    public static void main(String args[]) {
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                AssociateDialogUI dialog = new AssociateDialogUI(new javax.swing.JFrame(), true);
//                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
//                    public void windowClosing(java.awt.event.WindowEvent e) {
//                        System.exit(0);
//                    }
//                });
//                dialog.setVisible(true);
//            }
//        });
//    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel findLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton m_AssociateAllButton;
    private javax.swing.JButton m_AssociateButton;
    private javax.swing.JButton m_CloseButton;
    private javax.swing.JButton m_FindButton;
    private javax.swing.JComboBox m_FindCombo;
    private javax.swing.JCheckBox m_MatchCaseCheck;
    private javax.swing.JCheckBox m_NavigateCheck;
    private javax.swing.JLabel m_ResultsLabel;
    private javax.swing.JTable m_ResultsTable;
    private javax.swing.JCheckBox m_SearchAliasCheck;
    private javax.swing.JRadioButton m_SearchDescriptionsRadio;
    private javax.swing.JRadioButton m_SearchElementsRadio;
    private javax.swing.JLabel m_Status;
    private javax.swing.JCheckBox m_WholeWordCheck;
    private javax.swing.JCheckBox xPathCheck;
    // End of variables declaration//GEN-END:variables

    private FindController m_Controller = null;
    private IFindResults m_Results = null;
    private IProject m_Project = null;
    private SelectionListener selectionListener;
    private boolean update = true;

}

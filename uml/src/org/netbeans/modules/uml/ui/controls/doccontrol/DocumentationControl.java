/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */



package org.netbeans.modules.uml.ui.controls.doccontrol;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.StyledEditorKit.AlignmentAction;
import javax.swing.text.StyledEditorKit.StyledTextAction;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.preferenceframework.IPreferenceAccessor;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;
import org.netbeans.modules.uml.core.requirementsframework.IRequirement;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.IDataFormatter;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.core.support.umlutils.InvalidArguments;
import org.netbeans.modules.uml.core.workspacemanagement.IWSElement;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.resources.images.ImageUtil;
//import org.netbeans.modules.uml.ui.controls.drawingarea.IUIDiagram;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.products.ad.application.ApplicationView;
import org.netbeans.modules.uml.ui.products.ad.application.action.BaseAction;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.commonresources.CommonResourceManager;
//import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
//import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

/**
 *
 *
 */
public class DocumentationControl extends ApplicationView
        implements IDocumentationControl, DocumentListener,MouseListener
{
    /// The event handler for the various controls and the core UML metamodel
    private DocumentControlEventHandler m_EventsSink = null;
    private boolean m_CurMarkedDirty = false;
    private boolean m_SinksConnected = false;
    private String m_ElementName = "";
    private String m_BodyText = "";
    private String mBaseName = "";
    
    private IProxyDiagram m_ProxyDiagram = null;
    private IElement m_ModelElement = null;
    private IWSElement m_WSElement = null;
    
    private IProxyDiagram m_ProxyDiagramLast = null;
    private IElement m_ModelElementLast = null;
    private IWSElement m_WSElementLast = null;
    
    private IProxyDiagram m_ProxyDiagramPut = null;
    private IElement m_ModelElementPut = null;
    private IWSElement m_WSElementPut = null;
    
    private IProxyDiagram m_ProxyDiagramTemp = null;
    private IElement m_ModelElementTemp = null;
    private IWSElement m_WSElementTemp = null;
    
    private JTextPane m_TextPane = null;
    protected JLabel m_Label = null;
    private boolean m_BlockDocChangeEvents = false;
    
    private IRequirement m_Requirement = null;
    private IRequirement m_RequirementTemp = null;
    private IRequirement m_RequirementLast = null;
    
    /**
     *
     */
    public DocumentationControl()
    {
        super("org.netbeans.modules.uml.view.doccontrol"); // NOI18N
        init();
        initialize();
    }
    
    public DocumentationControl(String name)
    {
        super("org.netbeans.modules.uml.view.doccontrol"); // NOI18N
        setBaseName(name);
        
        String desc = DocumentationResources.getString("ADDS_DOCUMENTATION");
        getAccessibleContext().setAccessibleDescription(desc);
        init();
        initialize();
    }
    
    
    private void init()
    {
        this.setLayout(new GridBagLayout());
        
        HTMLDocument doc = new HTMLDocument();
        m_TextPane = new JTextPane(doc);
        m_TextPane.setDragEnabled(true);
        m_TextPane.setText("<html><head></head><body></body></html>");
        m_TextPane.setEditorKitForContentType("text/html", new HTMLEditorKit());
        m_TextPane.setContentType("text/html");
        m_TextPane.addMouseListener(this);
        StyledDocument doc1 = m_TextPane.getStyledDocument();
        doc1.addDocumentListener(this);
        
        JPanel namePanel = new JPanel();
        m_Label = new JLabel("");
        GridBagConstraints constraints = new GridBagConstraints();
        // IZ# 78924  - conover: it was "BOTH"
        constraints.fill = GridBagConstraints.NONE;
        constraints.weightx = 0.05;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        // IZ# 78924  - conover: not needed anymore
        // namePanel.add(m_Label);
        add(namePanel, constraints);
        
        JScrollPane scrollPane = new JScrollPane(getView());
        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.8;
        constraints.weighty = 0.8;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
    }
    
    /**
     * Retrieves the text pane used to display and edit
     */
    protected JTextPane getTextPane()
    {
        return m_TextPane;
    }

    /**
     * Initializes the doc control by creating an event sink and then listening to
     * drawing area and tree select/deselect events.
     */
    public void initialize()
    {
        enableDocControl(false);
        connectSinks(true);
        
        // now set up the text pane to default to the font that is in the preferences
        // for the documentation pane
        // this will be for any new text entered into the text pane
        Font defaultFont = getDefaultFontFromPreferences();
        if (defaultFont != null)
        {
            if (defaultFont.isBold())
            {
                Action action = new StyledEditorKit.BoldAction();
                action.putValue(Action.NAME, "Bold");
                action.actionPerformed(new ActionEvent(m_TextPane, 0, ""));
            }
            if (defaultFont.isItalic())
            {
                Action action = new StyledEditorKit.ItalicAction();
                action.putValue(Action.NAME, "Italic");
                action.actionPerformed(new ActionEvent(m_TextPane, 0, ""));
            }
            String fontName = defaultFont.getFamily();
            Action action = new StyledEditorKit.FontFamilyAction(fontName, fontName);
            action.actionPerformed(new ActionEvent(m_TextPane, 0, ""));
            
            int fontSize = defaultFont.getSize();
            Integer i = new Integer(fontSize);
            Action action2 = new StyledEditorKit.FontSizeAction(i.toString(), fontSize);
            action.actionPerformed(new ActionEvent(m_TextPane, 0, ""));
            
        }
        
        // Add accelerators
        registerTreeAccelerator(DocumentationResources.getString("IDSCTRLB"));
        registerTreeAccelerator(DocumentationResources.getString("IDSCTRLI"));
        registerTreeAccelerator(DocumentationResources.getString("IDSCTRLU"));
        registerTreeAccelerator(DocumentationResources.getString("IDSCTRLALTA"));
        registerTreeAccelerator(DocumentationResources.getString("IDSCTRLSHITLC"));
        registerTreeAccelerator(DocumentationResources.getString("IDSCTRLSHITLL"));
        registerTreeAccelerator(DocumentationResources.getString("IDSCTRLSHITLR"));
    }
    
    /**
     * Enables the Doc Control's window.
     */
    public void enableDocCtrl()
    {
        enableDocControl(true);
    }
    
    /**
     * Disables the Doc Control's window.
     */
    public void disableDocCtrl()
    {
        //save the current element before getting a new one
        setElementDescription();
        enableDocControl(false);
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.doccontrol.IDocumentationControl#getEditorCtrl(java.lang.Object)
         */
    public void getEditorCtrl(Object obj)
    {
        // TODO Auto-generated method stub
        
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.doccontrol.IDocumentationControl#getEnabled()
         */
    public int getEnabled()
    {
        // TODO Auto-generated method stub
        return 0;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.doccontrol.IDocumentationControl#setEnabled(int)
         */
    public void setEnabled(int value)
    {
        // TODO Auto-generated method stub
        
    }
    
    /**
     *	Save the current IElement's documentation before
     *	switching into Browse mode for example.
     */
    public void setCurElementDescription()
    {
        setElementDescription();
    }
    
    /**
     * Registers or revokes event sinks.
     *
     * @param bConnect
     */
    public void connectSinks(boolean bConnect)
    {
        DispatchHelper helper = new DispatchHelper();
        if (bConnect && !m_SinksConnected)
        {
            if (m_EventsSink == null)
            {
                m_EventsSink = new DocumentControlEventHandler();
                m_EventsSink.setDocCtrl(this);
            }
            if (m_EventsSink != null)
            {
                helper.registerForWorkspaceEvents(m_EventsSink);
                helper.registerForWSProjectEvents(m_EventsSink);
                helper.registerForDocumentationModifiedEvents(m_EventsSink);
//                helper.registerDrawingAreaSelectionEvents(m_EventsSink);
//                helper.registerDrawingAreaEvents(m_EventsSink);
                helper.registerProjectTreeEvents(m_EventsSink);
                helper.registerForLifeTimeEvents(m_EventsSink);
//                helper.registerDrawingAreaCompartmentEvents(m_EventsSink);
                helper.registerForPreferenceManagerEvents(m_EventsSink);
                helper.registerForInitEvents(m_EventsSink);
                helper.registerForNamedElementEvents(m_EventsSink);
                helper.registerForProjectEvents(m_EventsSink);
                m_SinksConnected = true;
            }
        }
        else if (!bConnect && m_SinksConnected)
        {
            try
            {
                helper.revokeWorkspaceSink(m_EventsSink);
                helper.revokeWSProjectSink(m_EventsSink);
                helper.revokeDocumentationModifiedSink(m_EventsSink);
//                helper.revokeDrawingAreaSelectionSink(m_EventsSink);
//                helper.revokeDrawingAreaSink(m_EventsSink);
                helper.revokeProjectTreeSink(m_EventsSink);
                helper.revokeLifeTimeSink(m_EventsSink);
//                helper.revokeDrawingAreaCompartmentSink(m_EventsSink);
                helper.revokePreferenceManagerSink(m_EventsSink);
                helper.revokeInitSink(m_EventsSink);
                helper.revokeNamedElementSink(m_EventsSink);
                helper.revokeProjectSink(m_EventsSink);
            }
            catch (InvalidArguments e)
            {
                e.printStackTrace();
            }
            m_SinksConnected = false;
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.doccontrol.IDocumentationControl#showLastSelectedElement()
         */
    public void showLastSelectedElement()
    {
        // TODO Auto-generated method stub
        
    }
    
    /**
     * Sets focus on the doc ctrl.
     */
    public void setFocus()
    {
        this.setFocus();
    }
    
    /**
     * Initializes the dialog.
     */
    public void onInitDialog()
    {
        // Make sure no text is displayed if nothing is selected
        enableDocControl(false);
    }
    
    public void onDiagramAliasChanged(IProxyDiagram dia)
    {
        //if this diagram is currently selected then update
        if (m_ProxyDiagram != null && m_ProxyDiagram.equals(dia))
        {
            String name = m_ProxyDiagram.getNameWithAlias();
            // IZ# 78924  - conover
            //  need to include elements name
            // setDisplayName(name);
            setDocWindowTitle(name);
        }
    }
    
    /**
     * @param name
     */
    private void setDisplayName(String name)
    {
        m_Label.setText(name);
    }
    
    public void setDocWindowTitle(String name)
    {
        // String title =
        //     DocumentationResources.getString("IDS_WINDOW_TITLE"); // NOI18N
        
        // IZ# 78924 - conover
        String title = getBaseName();
        
        if (getParent() != null)
        {
            if (name != null && name.length() > 0)
                title = name + " - " + title; // NOI18N
            
            // IZ# 82694 - conover
            // was getting a IllegalStateException;
            // needs to run in separate AWT thread
            final String finalTitle = title;
            Runnable r = new Runnable()
            {
                public void run()
                {
                    getParent().setName(finalTitle);
                }
            };
            SwingUtilities.invokeLater(r);
        }
    }
    
    public String getBaseName()
    {
        return mBaseName;
    }
    
    public void setBaseName(String val)
    {
        mBaseName = val;
    }
    
    public void onDestroy()
    {
        //revoke sinks
        connectSinks(false);
        if (m_EventsSink != null)
        {
            m_EventsSink = null;
        }
    }
    
    /**
     * Handles selection in the drawing area.
     *
     * @param pParentDiagram
     * @param pTreeItems
     */
    public void onDrawingAreaSelect(IDiagram pParentDiagram, ETList<IPresentationElement> pTreeItems)
    {
        boolean bClearSel = true;
        if (pTreeItems != null)
        {
            int count = pTreeItems.size();
            if (count == 1)
            {
                IPresentationElement pPresEle = pTreeItems.get(0);
                
                if (getModelElement(pPresEle))
                {
                    bClearSel = false;
                    if (!displayElement())
                    {
                        bClearSel = true;
                    }
                }
            }
            else
            {
                // Multiple items  or nothing selected
                //Show a diagram's documentation
                if (pParentDiagram != null)
                {
                    if (getModelElement(pParentDiagram))
                    {
                        bClearSel = false;
                        if (!displayElement())
                        {
                            bClearSel = true;
                        }
                    }
                }
            }
        }
        enableDocControl(!bClearSel);
    }
    
    /**
     *	Handles compartment selection in the drawing area.
     *
     * @param pCompartment
     * @param bSelected
     * @param cell
     */
//    public void onCompartmentSelected( ICompartment pCompartment, boolean bSelected,  IResultCell cell)
//    {
//        if (bSelected)
//        {
//            boolean bClearSel = true;
//            if (pCompartment != null)
//            {
//                String name = pCompartment.getName();
//                m_ElementName = name != null ? name : "";
//                if (getModelElement(pCompartment))
//                {
//                    bClearSel = false;
//                    if (!displayElement())
//                    {
//                        bClearSel = true;
//                    }
//                }
//            }
//            enableDocControl(!bClearSel);
//        }
//    }
    
    /**
     *
     * Displays a ModelElement or Diagram member if it is
     *	different from what is currently displayed.
     *
     * @return HRESULT
     *
     */
    private boolean displayElement()
    {
        boolean retVal = false;
        boolean display = false;
        if (m_ModelElement != null)
        {
            if (m_ModelElementTemp == null || !m_ModelElementTemp.isSame(m_ModelElement) || m_ModelElementTemp.isSame(m_ModelElement))
            {
                display = true;
            }
        }
        else if (m_ProxyDiagram != null)
        {
//            if (m_ProxyDiagramTemp == null || !m_ProxyDiagramTemp.isSame(m_ProxyDiagram))
//            if (m_ProxyDiagramTemp == null)
//            {
                display = true;
//            }
        }
        else if (m_WSElement != null)
        {
            if (!wsIsSame(m_WSElementTemp, m_WSElement))
            {
                display = true;
            }
        }
        else if (m_ModelElementTemp != null ||
                m_ProxyDiagramTemp != null ||
                m_WSElementTemp != null    ||
                m_Requirement != null)
        {
            display = true;
        }
        
        if (display)
        {
            //save the current element before getting a new one
            setElementDescription();
            
            // changed current element member to new element
            setModelElement();
            
            getNextElementDescription();
            retVal = true;
        }
        return retVal;
    }
    
    /**
     *	Gets the IElement pointer and sets the document with
     *	the element's description.
     */
    private String getNextElementDescription()
    {
        if ((m_ModelElement != null) ||
                (m_ProxyDiagram != null) ||
                (m_WSElement != null)    ||
                (m_Requirement != null))
        {
            //don't reload the same element description
            boolean proceed = false;
            if (m_ModelElement != null)
            {
                //proceed = !m_ModelElement.isSame(m_ModelElementLast);
                proceed = true;
            }
            else if (m_ProxyDiagram != null)
            {
                proceed = !m_ProxyDiagram.isSame(m_ProxyDiagramLast);
            }
            else if (m_WSElement != null)
            {
                proceed = (m_WSElement != m_WSElementLast);
            }
            else
            {
                proceed = true;
            }
            
            if (proceed)
            {
                // Get the name of the presentation element
                getElementName();
                
                // Get the description of the presentation element
                getElementDescription();
                
                if (!setBaseURL())
                {
                    // there was a problem getting the element path, show documentation anyway.
                    displayTextInEditControl();
                    enableDocControl(true);
                }
            }
        }
        return m_BodyText;
    }
    
    /**
     *
     * Compares two IWSElement pointers
     *
     * @param pWSElem[in]
     * @param pWSElem2[in]
     *
     * @return TRUE if the same, FALSE if not.
     *
     */
    public boolean wsIsSame(IWSElement elem1, IWSElement elem2)
    {
        boolean same = false;
        if (elem1 != null && elem2 != null)
        {
            same = (elem1.equals(elem2));
        }
        return same;
    }
    
    /**
     * @return
     */
    private boolean setBaseURL()
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    /**
     *	Handles tree selection change.
     *
     * @param pITheItems
     */
    public void onProjectTreeSelChanged( IProjectTreeItem[] pItems )
    {
        boolean bClearSel = true;
        if (pItems != null)
        {
            int count = pItems.length;
            if (count == 1)
            {
                IProjectTreeItem item = pItems[0];
                if (getModelElement(item))
                {
                    bClearSel = false;
                    if (!displayElement())
                    {
                        bClearSel = true;
                    }
                }
            }
            else
            {
                //save the current element before getting a new one
                setElementDescription();
            }
            enableDocControl(!bClearSel);
        }
    }
    
    /**
     * Get the model element and set the member variable for it.
     *
     * @param pITheDisp
     */
    private boolean getModelElement(Object pDisp)
    {
        boolean retVal = false;
        m_ModelElementTemp = null;
        m_ProxyDiagramTemp = null;
        m_WSElementTemp = null;
        m_Requirement = null;
        
        IElement pElement = null;
        
        if (pDisp instanceof IPresentationElement)
        {
            IPresentationElement pPE = (IPresentationElement)pDisp;
            pElement = pPE.getFirstSubject();
            if (pElement == null && pDisp instanceof IDiagram)
            {
                pElement = (IDiagram)pDisp;
            }
        }
        else if (pDisp instanceof IProjectTreeItem)
        {
            IProjectTreeItem item = (IProjectTreeItem)pDisp;
            pElement = item.getModelElement();
            if (pElement == null)
            {
                boolean isWork = item.isWorkspace();
                boolean isProj = item.isProject();
                boolean isDia = item.isDiagram();
                if (isWork)
                {
                    // have a workspace selected
                    IWorkspace pSpace = ProductHelper.getWorkspace();
                    if (pSpace != null && pSpace instanceof IWSElement)
                    {
                        m_WSElementTemp = (IWSElement)pSpace;
                    }
                }
                else if (isProj)
                {
                    IProject proj = item.getProject();
                    if (proj != null)
                    {
                        pElement = proj;
                    }
                }
                else if (isDia)
                {
                    //check for IDiagram documentation
                    IProxyDiagram pDia = item.getDiagram();
                    if (pDia != null)
                    {
                        IDiagram dia = pDia.getDiagram();
                        if (dia != null)
                        {
                            pElement = dia;
                        }
                        else
                        {
                            //this is a workaround for an unopened IDiagram
                            m_ProxyDiagramTemp = pDia;
                            retVal = true;
                        }
                    }
                }
                else
                {
                    Object obj = item.getData();
                    if (obj instanceof IRequirement)
                    {
                        m_Requirement = (IRequirement)obj;
                        m_RequirementTemp = m_Requirement;
                        retVal = true;
                    }
                }
            }
        }
//        else if (pDisp instanceof ICompartment)
//        {
//            pElement = ((ICompartment)pDisp).getModelElement();
//        }
        else if(pDisp instanceof IElement)
        {
            pElement = (IElement)pDisp;
        }
        
        if (pElement != null)
        {
            m_ModelElementTemp = pElement;
            retVal = true;
        }
        
        return retVal;
    }
    
    public void displayElement(IElement element)
    {
        boolean bClearSel = true;
        if (element != null)
        {
            if (getModelElement(element))
            {
                bClearSel = false;
                if (!displayElement())
                {
                    bClearSel = true;
                }
            }
            
            enableDocControl(!bClearSel);
        }
    }
    
    /**
     * Get the description of a presentation element.
     */
    public String getElementDescription()
    {
        String docText = "";
        if (m_ModelElement != null)
        {
            docText = m_ModelElement.getDocumentation();
        }
        
        if (m_ProxyDiagram != null)
        {
            docText = m_ProxyDiagram.getDocumentation();
        }
        
        if (m_WSElement != null)
        {
            docText = m_WSElement.getDocumentation();
        }
        
        if(m_Requirement != null)
        {
            docText = m_Requirement.getDescription();
        }
        
        if (docText != null && docText.length() > 0 && !docText.equals("<P>&nbsp;</P>"))
        {
            docText = DocUtils.convertToTags(docText);
            m_BodyText = docText;
        }
        else
        {
            m_BodyText = "";
        }
        
        return m_BodyText;
    }
    
    /**
     * Get the name of a presentation element.
     */
    public String getElementName()
    {
        String name = "";
        if (m_ModelElement != null)
        {
            if (m_ModelElement instanceof INamedElement)
            {
                name = ((INamedElement)m_ModelElement).getName();
            }
        }
        else if (m_ProxyDiagram != null)
        {
            name = m_ProxyDiagram.getName();
        }
        else if (m_WSElement != null)
        {
            name = m_WSElement.getName();
        }
        
        m_ElementName = (name != null) ? name : "";
        
        return m_ElementName;
    }
    
    /**
     * EnableDocControl - Enables or disables the doc control.
     *
     * @ Param bEnable TRUE for Enabling or FALSE for Disabling
     */
    public void enableDocControl( boolean bEnable )
    {
        if (!bEnable)
        {
            clearSelection();
        }
    }
    
    /**
     *
     */
    private void clearSelection()
    {
        m_TextPane.setText("");
        m_BodyText = "";
        m_ElementName = "";
        m_CurMarkedDirty = false;
        m_ModelElement = null;
        m_ProxyDiagram = null;
        m_WSElement = null;
        m_Requirement = null;
        
        // Clear the icon and associated text
        setModelElement();
    }
    
    /**
     * Sets the element description.
     */
    public void setElementDescription()
    {
        if((m_ModelElement != null) ||
                (m_ProxyDiagram != null) ||
                (m_WSElement != null)    ||
                (m_Requirement != null))
        {
            setCurElemDocumentation(false);
            
            //We're done with this element
            m_ModelElement = null;
            m_ProxyDiagram = null;
            m_WSElement = null;
            m_Requirement = null;
            m_BodyText = "";
            m_ElementName = "";
        }
    }
    
    /**
     *
     * Handles OnAliasNameModified event.
     *
     * @param *pElem[in]
     *
     * @return HRESULT
     *
     */
    public void onNameModified( INamedElement pElem )
    {
        //if this element is currently selected then update
        if (pElem != null)
        {
            boolean isSame = pElem.isSame(m_ModelElement);
            if (isSame)
            {
                String name = pElem.getNameWithAlias();
                
                // IZ# 78924  - conover
                //  need to include elements name
                // setDisplayName(name);
                setDocWindowTitle(name);
            }
        }
    }
    
    /**
     *
     * Handles OnAliasNameModified event.
     *
     * @param *pElem[in]
     *
     * @return HRESULT
     *
     */
    public void onAliasNameModified( INamedElement pElem )
    {
        //if this element is currently selected then update
        if (pElem != null)
        {
            boolean isSame = pElem.isSame(m_ModelElement);
            if (isSame)
            {
                String name = pElem.getNameWithAlias();
                
                // conover
                // setDisplayName(name);
                setDocWindowTitle(name);
            }
        }
    }
    
    /**
     *
     * Handles OnDocumentModifed event.
     *
     * @param *pElem[in]
     *
     * @return HRESULT
     *
     */
    public void onDocumentModified(IElement pElem)
    {
        //if this element is currently selected then update
        if (pElem != null && m_ModelElement != null)
        {
            // don't udpate the display if this was the element that triggered the event
            if (m_ModelElementPut != null && m_ModelElementPut.isSame(pElem))
            {
                m_ModelElementPut = null;
            }
            else
            {
                if (m_ModelElement != null && m_ModelElement.isSame(pElem))
                {
                    // Get the description of the presentation element
                    getElementDescription();
                    
                    // Display the information
                    displayTextInEditControl();
                    
                    m_CurMarkedDirty = false;
                }
            }
        }
    }
    
    /**
     * Display text in the edit control.
     */
    private void displayTextInEditControl()
    {
        m_BlockDocChangeEvents = true;
        m_TextPane.setText(m_BodyText);
        m_TextPane.setCaretPosition(0);
        m_BlockDocChangeEvents = false;
    }
    
    /**
     *
     * Sets documentation if it has changed.
     *
     * @return HRESULT
     *
     */
    public void setCurElemDocumentation( boolean bForceUpdate )
    {
        //if there is no model element selected, it's not an error
        if ((bForceUpdate || m_CurMarkedDirty) &&
                (m_ModelElement != null || m_ProxyDiagram != null || m_WSElement != null))
        {
            String text = m_TextPane.getText();
            
            // A 'blank' document may have <P>&nbsp;</P>
            if (text != null && text.length() > 0 && !text.equals("<P>&nbsp;</P>"))
            {
                text = text.replaceAll("\r","");
                text = text.replaceAll("\n","<br>");
                
                m_BodyText = text;
            }
            else
            {
                m_BodyText = "";
            }
            
            String docText = "";
            String bodyTextStripped = m_BodyText;
            if (m_ModelElement != null)
            {
                docText = m_ModelElement.getDocumentation();
            }
            else
            {
                if (m_ProxyDiagram != null)
                {
                    docText = m_ProxyDiagram.getDocumentation();
                }
                else
                {
                    if (m_WSElement != null)
                    {
                        docText = m_WSElement.getDocumentation();
                    }
                    else if(m_Requirement != null)
                    {
                        docText = m_Requirement.getDescription();
                    }
                }
            }
            
            if (docText != null && docText.length() > 0)
            {
                if (m_BodyText != null)
                {
                    // Strip off carriage returns and add newlines
                    bodyTextStripped = DocUtils.convertFromTags(bodyTextStripped);
                    
                    if (!isDocSame(docText, bodyTextStripped))
                    {
                        setDocumentation(bodyTextStripped);
                    }
                }
            }
            else
            {
                if (m_BodyText != null && m_BodyText.length() > 0)
                {
                    bodyTextStripped = DocUtils.convertFromTags(bodyTextStripped);
                    setDocumentation(bodyTextStripped);
                }
            }
        }
        m_CurMarkedDirty = false;
    }
    
    /**
     *
     *	Sets member with currently viewed ModelElement or Diagram.
     *
     *
     *
     *
     * @return HRESULT
     *
     */
    public void setModelElement()
    {
        boolean bClearDiag = true;
        boolean bClearElem = true;
        boolean bClearWSElem = true;
        boolean bClearReq = true;
        String dispName = "";
        Object dispIcon = null;
        
        if (m_ModelElementTemp != null)
        {
            bClearElem = false;
            m_ModelElementLast = m_ModelElement;
            m_ModelElement = m_ModelElementTemp;
            m_ModelElementTemp = null;
            
//            if (m_ModelElement instanceof IUIDiagram)
//            {
//                IDrawingAreaControl control = ((IUIDiagram)m_ModelElement).getDrawingArea();
//                if (control != null)
//                {
//                    dispName = control.getNameWithAlias();
//                }
//            }
//            else
            {
                IDataFormatter formatter = ProductHelper.getDataFormatter();
                if (formatter != null)
                {
                    dispName = formatter.formatElement(m_ModelElement);
                }
            }
            dispIcon = m_ModelElement;
        }
        else if (m_ProxyDiagramTemp != null)
        {
            bClearDiag = false;
            m_CurMarkedDirty = false;
            m_ProxyDiagramLast = m_ProxyDiagram;
            m_ProxyDiagram = m_ProxyDiagramTemp;
            m_ProxyDiagramTemp = null;
            if (m_ProxyDiagram != null)
            {
                dispName = m_ProxyDiagram.getNameWithAlias();
                dispIcon = m_ProxyDiagram;
            }
        }
        else if (m_WSElementTemp != null)
        {
            bClearWSElem = false;
            m_CurMarkedDirty = false;
            m_WSElementLast = m_WSElement;
            m_WSElement = m_WSElementTemp;
            m_WSElementTemp = null;
            if (m_WSElement != null)
            {
                dispName = m_WSElement.getName();
                dispIcon = m_WSElement;
            }
        }
        else if(m_RequirementTemp != null)
        {
            bClearReq = false;
            m_CurMarkedDirty = false;
            
            m_RequirementLast = m_Requirement;
            m_Requirement = m_RequirementTemp;
            m_RequirementTemp = null;
            
            if(m_Requirement != null)
            {
                dispName = m_Requirement.getName();
                dispIcon = m_Requirement;
            }
        }
        
        if (bClearDiag)
        {
            m_ProxyDiagram = null;
            m_ProxyDiagramLast = null;
        }
        if (bClearElem)
        {
            m_ModelElement = null;
            m_ModelElementLast = null;
        }
        if (bClearWSElem)
        {
            m_WSElement = null;
            m_WSElementLast = null;
        }
        
        if(bClearReq)
        {
            m_Requirement = null;
            m_RequirementLast = null;
        }
        
        // Fix W6814:  Set the description based on the name of the element
        // IZ# 78924  - conover
        //  need to include elements name
        // setDisplayName(dispName);
        setDocWindowTitle(dispName);
        
        if (dispIcon != null)
        {
            Icon icon = setIconForElement(dispIcon);
        }
    }
    
    public Icon setIconForElement(Object dispIcon)
    {
        CommonResourceManager mgr = CommonResourceManager.instance();
        Icon icon = mgr.getIconForDisp(dispIcon);
        
        //set the icon on the toolbar
        // IZ# 78924  - conover
        //  removed icon from the toolbar
        // m_Label.setIcon(icon);
        return icon;
    }
    
    /**
     *
     * Marks an element 'Dirty'
     *
     *
     * @return HRESULT
     *
     */
    public void markElementDirty()
    {
        if (!m_CurMarkedDirty &&
                (m_ModelElement != null ||
                m_ProxyDiagram != null ||
                m_WSElement != null    ||
                m_Requirement != null))
        {
            //setCurElemDocumentation(true);
            m_CurMarkedDirty = true;
        }
    }
    
    /**
     *
     * Sets the documentation for the current element
     *
     * @param bsNewDoc[in] documention
     *
     * @return HRESULT
     *
     */
    public void setDocumentation(String newDoc)
    {
        // mark the element whose documentation got saved
        // we should have to release here because the modified event should have cleared these
        m_ProxyDiagramPut = null;
        m_ModelElementPut = null;
        m_WSElementPut = null;
        
        // keep track of who triggered the changed event.
        m_ProxyDiagramPut = m_ProxyDiagram;
        m_ModelElementPut = m_ModelElement;
        m_WSElementPut = m_WSElement;
        
        newDoc = DocUtils.normalizeHTML(newDoc);
        
        // set the documentation
        if (m_ModelElement != null)
        {
            m_ModelElement.setDocumentation(newDoc);
        }
        else
        {
            if (m_ProxyDiagram != null)
            {
                m_ProxyDiagram.setDocumentation(newDoc);
            }
            else
            {
                if (m_WSElement != null)
                {
                    m_WSElement.setDocumentation(newDoc);
                }
            }
        }
    }
    
    /**
     *
     * compares strings with tags but ignores case within tags since the DHTML control.
     *	will change tags to upper case.
     *
     * @param str1[in]
     * @param str2[in]
     *
     * @return TRUE if the same, FALSE if not.
     *
     */
    private boolean isDocSame(String str1, String str2)
    {
        boolean retVal = false;
        
        //check for differing cases in tags, which don't count
        if (str1 != null && str2 != null)
        {
            int len1 = str1.length();
            int len2 = str2.length();
            
            if (len1 == len2)
            {
                retVal = true;
                boolean inTag = false;
                for (int i=0; i<len1; i++)
                {
                    if (str1.charAt(i) == '<' && str2.charAt(i) == '<')
                    {
                        inTag = true;
                    }
                    else if (str1.charAt(i) == '>' && str2.charAt(i) == '>')
                    {
                        inTag = false;
                    }
                    
                    if (str1.charAt(i) != str2.charAt(i))
                    {
                        if (!inTag || (str1.charAt(i) - str2.charAt(i) != 32))
                        {
                            retVal = false;
                            break;
                        }
                    }
                }
            }
        }
        return retVal;
    }
    
    /**
     * Project has opened
     *
     * @param project [in] The project that just opened.
     */
    public void onProjectOpened(IProject project)
    {
        m_ModelElementTemp = project;
        enableDocControl(false);
    }
    
    /**
     * Project has closed
     *
     * @param project [in] The project that got closed.
     */
    public void onProjectClosed(IProject project)
    {
        enableDocControl(false);
    }
    
    /**
     *
     * Updates the documentation editor when the Workspace
     * documentation changes.
     *
     * @param element[in]
     * @param cell[in]
     *
     * @return
     *
     */
    public void onWSElementAliasChanged(IWSElement element)
    {
        //if this element is currently selected then update
        if (element != null && element.equals(m_WSElement))
        {
            if (m_WSElement instanceof INamedElement)
            {
                String name = m_WSElement.getName();
                // IZ# 78924  - conover
                //  need to include elements name
                // setDisplayName(name);
                setDocWindowTitle(name);
            }
        }
    }
    
    /**
     *
     * Updates the documentation editor when the Workspace documentation changes.
     *
     * @param element[in]
     * @param cell[in]
     *
     * @return
     *
     */
    public void onWSElementDocChanged(IWSElement pElem, IResultCell cell)
    {
        //if this element is currently selected then update
        if (pElem != null && m_WSElement != null)
        {
            if (wsIsSame(pElem, m_WSElementPut))
            {
                m_WSElementPut = null;
            }
            else if (wsIsSame(pElem, m_WSElement))
            {
                // don't udpate the display if this was the element that triggered the event
                // Get the description of the presentation element
                String desc = getElementDescription();
                if (desc != null)
                {
                    // Display the information
                    displayTextInEditControl();
                    m_CurMarkedDirty = false;
                }
            }
        }
    }
    
    /**
     * Handles the change of the preference
     *
     * @param name [in] The name of the preference that got changed.
     * @param pElement [in] The element that is the preference item.
     */
    public void onPreferenceChange(String name, IPropertyElement pElement)
    {
        if ("ShowAliasedNamed".equals(name))
        {
            if (m_ModelElement instanceof INamedElement)
            {
                onAliasNameModified((INamedElement)m_ModelElement);
            }
            else if (m_ProxyDiagram != null)
            {
                onDiagramAliasChanged(m_ProxyDiagram);
            }
            else if (m_WSElement != null)
            {
                onWSElementAliasChanged(m_WSElement);
            }
        }
    }
    
    /**
     *
     * Handles OnWSProjectPreSave event.
     *
     *
     * @return HRESULT
     *
     */
    public void onWSProjectPreSave()
    {
        // Fix W4011:  Documentation needs to be saved when the project is saved.
        setElementDescription();
    }
    
    /**
     *
     * Handles WSProjectPreClose event.
     *
     *
     * @return HRESULT
     *
     */
    public void onWSProjectPreClose()
    {
        // Fix W4010:  A closed diagram needs special handling
        //             since it will not set the project to modified
        if (m_ProxyDiagram != null)
        {
            boolean isOpen = m_ProxyDiagram.isOpen();
            if (!isOpen)
            {
                setElementDescription();
            }
        }
        
        // clear display and release element without doing a put_Documentation()
        enableDocControl(false);
    }
    
    /**
     *
     */
    public void onWorkspaceClosed()
    {
        enableDocControl(false);
    }
    
    /**
     *
     * OnDrawingAreaDocumentModified updates the display if this is the
     *	currently selected element.
     *
     * @param *pDiag
     *
     * @return
     *
     */
    public void onDrawingAreaDocumentationModified(IProxyDiagram pProxyDiagram)
    {
        //if this element is currently selected then update
        if (pProxyDiagram != null)
        {
            IDiagram pDia = pProxyDiagram.getDiagram();
            
            // don't update the display if this was the element that triggered this event
            if ( (m_ProxyDiagramPut != null && m_ProxyDiagramPut.isSame(pProxyDiagram)) ||
                    (m_ModelElementPut != null && m_ModelElementPut.isSame(pDia)) )
            {
                m_ProxyDiagramPut = null;
                m_ModelElementPut = null;
                m_WSElementPut = null;
            }
            else // something external changed the documentation, update if currently displayed.
            {
                if ( (m_ProxyDiagram != null && m_ProxyDiagram.isSame(pProxyDiagram)) ||
                        (m_ModelElement != null && m_ModelElement.isSame(pDia)) )
                {
                    // Get the description of the presentation element
                    getElementDescription();
                    
                    // Display the information
                    displayTextInEditControl();
                    m_CurMarkedDirty = false;
                }
            }
        }
    }
    
    /**
     *
     * OnDrawingAreaClosed saves the documentation for an element if it is currently
     *	displayed and it's diagram is being closed.
     *
     * @param pParentDiagram
     * @param bDiagramIsDirty not used
     * @param cell not used
     *
     * @return
     *
     */
    public void onDrawingAreaClosed(IDiagram pParentDiagram, boolean bDiagramIsDirty, IResultCell cell)
    {
        enableDocControl(false);
    }
    
    /**
     *
     * OnDrawingAreaPreSave saves the documentation of the proxy diagram or
     *	the actual opened diagram if it is currently displayed.
     *
     * @param pParentDiagram
     * @param cell
     *
     * @return
     *
     */
    public void onDrawingAreaPreSave(IProxyDiagram pParentDiagram, IResultCell cell)
    {
        //if this element is currently selected then update
        if (pParentDiagram != null)
        {
            if (m_ProxyDiagram != null && m_ProxyDiagram.isSame(pParentDiagram))
            {
                // Do a put_Documentation() if text has changed and then clear current element
                setElementDescription();
            }
            else if (m_ModelElement != null)
            {
                if (pParentDiagram instanceof IDiagram)
                {
                    IDiagram pDia = (IDiagram)pParentDiagram;
                    if (pDia instanceof IElement)
                    {
                        IElement pElem = (IElement)pDia;
                        if (m_ModelElement.isSame(pElem))
                        {
                            // Do a put_Documentation() if text has changed and then clear current element
                            setElementDescription();
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Gives notification that there was an insert into the document.  The
     * range given by the DocumentEvent bounds the freshly inserted region.
     *
     * @param e the document event
     */
    public void insertUpdate(DocumentEvent e)
    {
        handleDocModified(e);
    }
    
    /**
     * Gives notification that a portion of the document has been
     * removed.  The range is given in terms of what the view last
     * saw (that is, before updating sticky positions).
     *
     * @param e the document event
     */
    public void removeUpdate(DocumentEvent e)
    {
        handleDocModified(e);
    }
    
    /**
     * Gives notification that an attribute or set of attributes changed.
     *
     * @param e the document event
     */
    public void changedUpdate(DocumentEvent e)
    {
        handleDocModified(e);
    }
    
    private void handleDocModified(DocumentEvent e)
    {
        if (m_BlockDocChangeEvents)
        {
            return;
        }
        int len = e.getLength();
        int offset = e.getOffset();
        String curText = m_TextPane.getText();
        String oldText = "";
        if (m_ModelElement != null)
        {
            oldText = m_ModelElement.getDocumentation();
        }
        else if (m_ProxyDiagram != null)
        {
            oldText = m_ProxyDiagram.getDocumentation();
        }
        else if (m_WSElement != null)
        {
            oldText = m_WSElement.getDocumentation();
        }
        
        if ( (oldText != null && !oldText.equals(curText)) ||
                (oldText == null && curText != null) )
        {
            markElementDirty();
        }
    }
    
    // MouseListener -------------------------------------------------------
    
    public void mouseClicked(MouseEvent e)
    {  
    }
    
    public void mouseEntered(MouseEvent e)
    {
       // set cursor?
    }
    
    public void mouseExited(MouseEvent e)
    {
        setCurElemDocumentation(false);
    }
    
    public void mousePressed(MouseEvent e)
    {}
    
    public void mouseReleased(MouseEvent e)
    {}
    
    
    public class ColorAction extends BaseAction
    {
        DocumentationControl parent = null;
        
        public ColorAction(DocumentationControl par)
        {
            parent = par;
            setSmallIcon(ImageUtil.instance().getIcon("color-chooser.png"));
            setToolTipText(DocumentationResources.getString("IDS_FONTCOLOR"));
        }
        
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                parent.getTextPane().requestFocus();
                Color c = JColorChooser.showDialog(parent,
                        DocumentationResources.getString("IDS_TITLE"), m_TextPane.getForeground());
                if (c != null)
                {
                    Action act = new StyledEditorKit.ForegroundAction("", c);
                    act.actionPerformed(e);
                }
            }
            catch(IllegalArgumentException ie)
            {
                // When the documentation control does not have focus when the user
                // presses the button we can get an IllegalArgumentException.
                // I tried to protected against this but the required methods
                // are package protected.  So I will just catch the exception and
                // continue.
            }
        }
        
        @Override
        public boolean isEnabled()
        {
            //always enabled
            return true;
        }
        
        public int getStyle()
        {
            return AS_PUSH_BUTTON;
        }
        public JComponent getCustomComponent()
        {
            return null;
        }
    }
    
    public class BoldAction extends BaseAction
    {
        DocumentationControl parent = null;
        
        public BoldAction(DocumentationControl par)
        {
            parent = par;
            setSmallIcon(ImageUtil.instance().getIcon("bold.png"));
            setFocusable(false);
            setToolTipText(DocumentationResources.getString("IDS_BOLD"));
        }
        
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                parent.getTextPane().requestFocus();
                Action act = new StyledEditorKit.BoldAction();
                act.actionPerformed(e);
            }
            catch(IllegalArgumentException ie)
            {
                // When the documentation control does not have focus when the user
                // presses the button we can get an IllegalArgumentException.
                // I tried to protected against this but the required methods
                // are package protected.  So I will just catch the exception and
                // continue.
            }
        }
        
        public boolean isEnabled()
        {
            //always enabled
            return true;
        }
    }
    
    public class ItalicAction extends BaseAction
    {
        DocumentationControl parent = null;
        
        public ItalicAction(DocumentationControl par)
        {
            parent = par;
            setSmallIcon(ImageUtil.instance().getIcon("italics.png"));
            setToolTipText(DocumentationResources.getString("IDS_ITALIC"));
        }
        
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                parent.getTextPane().requestFocus();
                Action act = new StyledEditorKit.ItalicAction();
                act.actionPerformed(e);
            }
            catch(IllegalArgumentException ie)
            {
                // When the documentation control does not have focus when the user
                // presses the button we can get an IllegalArgumentException.
                // I tried to protected against this but the required methods
                // are package protected.  So I will just catch the exception and
                // continue.
            }
        }
        
        public boolean isEnabled()
        {
            //always enabled
            return true;
        }
    }
    
    public class UnderlineAction extends BaseAction
    {
        DocumentationControl parent = null;
        
        public UnderlineAction(DocumentationControl par)
        {
            parent = par;
            setSmallIcon(ImageUtil.instance().getIcon("underline.png"));
            setToolTipText(DocumentationResources.getString("IDS_UNDER"));
        }
        
        public void actionPerformed(final ActionEvent e)
        {
            try
            {
                parent.getTextPane().requestFocus();
                
                Action act = new StyledEditorKit.UnderlineAction();
                act.actionPerformed(e);
            }
            catch(IllegalArgumentException ie)
            {
                // When the documentation control does not have focus when the user
                // presses the button we can get an IllegalArgumentException.
                // I tried to protected against this but the required methods
                // are package protected.  So I will just catch the exception and
                // continue.
            }
        }
        
        public boolean isEnabled()
        {
            //always enabled
            return true;
        }
    }
    
    public class LeftAlignAction extends BaseAction
    {
        DocumentationControl parent = null;
        
        public LeftAlignAction(DocumentationControl par)
        {
            parent = par;
            setSmallIcon(ImageUtil.instance().getIcon("align-left-text.png"));
            setToolTipText(DocumentationResources.getString("IDS_ALIGNLEFT"));
        }
        
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                parent.getTextPane().requestFocus();
                Action act = new CustomAlignmentAction("left-justify", StyleConstants.ALIGN_LEFT);
                act.actionPerformed(e);
                
            }
            catch(IllegalArgumentException ie)
            {
                // When the documentation control does not have focus when the user
                // presses the button we can get an IllegalArgumentException.
                // I tried to protected against this but the required methods
                // are package protected.  So I will just catch the exception and
                // continue.
            }
        }
        
        public boolean isEnabled()
        {
            //always enabled
            return true;
        }
    }
    
    public class CenterAlignAction extends BaseAction
    {
        DocumentationControl parent = null;
        
        public CenterAlignAction(DocumentationControl par)
        {
            parent = par;
            setSmallIcon(ImageUtil.instance().getIcon("align-center-text.png"));
            setToolTipText(DocumentationResources.getString("IDS_CENTER"));
        }
        
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                parent.getTextPane().requestFocus();
                Action act = new CustomAlignmentAction("center-justify", StyleConstants.ALIGN_CENTER);
                act.actionPerformed(e);
            }
            catch(IllegalArgumentException ie)
            {
                // When the documentation control does not have focus when the user
                // presses the button we can get an IllegalArgumentException.
                // I tried to protected against this but the required methods
                // are package protected.  So I will just catch the exception and
                // continue.
            }
        }
        public boolean isEnabled()
        {
            //always enabled
            return true;
        }
    }
    
    public class CustomAlignmentAction extends StyledTextAction
    {
        
        /**
         * Creates a new AlignmentAction.
         *
         * @param nm the action name
         * @param a the alignment >= 0
         */
        public CustomAlignmentAction(String nm, int a)
        {
            super(nm);
            this.a = a;
            this.alignment = nm;
        }
        
        /**
         * Sets the alignment.
         *
         * @param e the action event
         */
        public void actionPerformed(ActionEvent e)
        {
            AlignmentAction aa = new AlignmentAction(this.alignment, this.a);
            aa.actionPerformed(e);
            JEditorPane editor = getEditor(e);
            if (editor != null)
            {
                int a = this.a;
                if ((e != null) && (e.getSource() == editor))
                {
                    String s = e.getActionCommand();
                    try
                    {
                        a = Integer.parseInt(s, 10);
                    }
                    catch (NumberFormatException nfe)
                    {
                    }
                }
                SimpleAttributeSet attr = new SimpleAttributeSet();
                StyleConstants.setAlignment(attr, a);
                setParagraphAttributes(editor, attr, true);
            }
        }
        private String alignment;
        private int a;
    }
    public class RightAlignAction extends BaseAction
    {
        DocumentationControl parent = null;
        
        public RightAlignAction(DocumentationControl par)
        {
            parent = par;
            setSmallIcon(ImageUtil.instance().getIcon("align-right-text.png"));
            setToolTipText(DocumentationResources.getString("IDS_ALIGNRIGHT"));
        }
        
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                parent.getTextPane().requestFocus();
                Action act = new CustomAlignmentAction("right-justify", StyleConstants.ALIGN_RIGHT);
                act.actionPerformed(e);
            }
            catch(IllegalArgumentException ie)
            {
                // When the documentation control does not have focus when the user
                // presses the button we can get an IllegalArgumentException.
                // I tried to protected against this but the required methods
                // are package protected.  So I will just catch the exception and
                // continue.
            }
        }
        
        public boolean isEnabled()
        {
            //always enabled
            return true;
        }
    }
    
// IZ# 78924  - conover: removed Font toolbar items
//	public class FontFamilyAction extends BaseAction
//	{
//		DocumentationControl parent = null;
//		JComboBox m_Box = null;
//
//		public FontFamilyAction(DocumentationControl par)
//		{
//			parent = par;
//
//			GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
//			String[] names = env.getAvailableFontFamilyNames();
//			if (names != null)
//			{
//				m_Box = new JComboBox(names);
//				m_Box.setFocusable(false);
//				m_Box.addActionListener(this);
//				m_Box.setSize(100, 30);
//				m_Box.setToolTipText(DocumentationResources.getString("IDS_FONT"));
//				// set the selected item in the list box to be what is in the preferences
//				Font defaultFont = par.getDefaultFontFromPreferences();
//				if (defaultFont != null)
//				{
//					m_Box.setSelectedItem(defaultFont.getFamily());
//				}
//			}
//		}
//
//		public void actionPerformed(ActionEvent e)
//		{
//			try
//			{
//				parent.getTextPane().requestFocus();
//				Object obj = e.getSource();
//				if (obj instanceof JComboBox)
//				{
//					try
//					{
//						JComboBox combo = (JComboBox)obj;
//						String name = (String)combo.getSelectedItem();
//						Action act = new StyledEditorKit.FontFamilyAction("", name);
//						act.actionPerformed(e);
//					}
//					catch (Exception exc)
//					{
//					}
//				}
//			}
//			catch(IllegalArgumentException ie)
//			{
//				// When the documentation control does not have focus when the user
//				// presses the button we can get an IllegalArgumentException.
//				// I tried to protected against this but the required methods
//				// are package protected.  So I will just catch the exception and
//				// continue.
//			}
//		}
//
//		public boolean isEnabled()
//		{
//			//always enabled
//			return true;
//		}
//
//		public int getStyle()
//		{
//			return AS_CUSTOM_COMPONENT;
//		}
//		public JComponent getCustomComponent()
//		{
//			return m_Box;
//		}
//	}
//
//	public class FontSizeAction extends BaseAction
//	{
//		DocumentationControl parent = null;
//		JComboBox m_Box = null;
//
//		public FontSizeAction(DocumentationControl par)
//		{
//			parent = par;
//
//			Object[] objs =
//			{"8", "10", "12", "14", "18", "24", "36"};
//			m_Box = new JComboBox(objs);
//			m_Box.addActionListener(this);
//			m_Box.setSize(20, 30);
//			m_Box.setToolTipText(DocumentationResources.getString("IDS_FONTSIZE"));
//			// set the selected item in the list box to be what is in the preferences
//			Font defaultFont = par.getDefaultFontFromPreferences();
//			if (defaultFont != null)
//			{
//				m_Box.setSelectedItem(new Integer(defaultFont.getSize()).toString());
//			}
//		}
//
//		public void actionPerformed(ActionEvent e)
//		{
//			try
//			{
//				parent.getTextPane().requestFocus();
//				Object obj = e.getSource();
//				if (obj instanceof JComboBox)
//				{
//					try
//					{
//						JComboBox combo = (JComboBox)obj;
//						String name = (String)combo.getSelectedItem();
//						Action act = new StyledEditorKit.FontSizeAction("", Integer.parseInt(name));
//						act.actionPerformed(e);
//					}
//					catch (Exception exc)
//					{
//					}
//				}
//			}
//			catch(IllegalArgumentException ie)
//			{
//				// When the documentation control does not have focus when the user
//				// presses the button we can get an IllegalArgumentException.
//				// I tried to protected against this but the required methods
//				// are package protected.  So I will just catch the exception and
//				// continue.
//			}
//		}
//
//		public boolean isEnabled()
//		{
//			//always enabled
//			return true;
//		}
//		public int getStyle()
//		{
//			return AS_CUSTOM_COMPONENT;
//		}
//		public JComponent getCustomComponent()
//		{
//			return m_Box;
//		}
//	}
    
    public class LabelAction extends BaseAction
    {
        DocumentationControl parent = null;
        JComboBox m_Box = null;
        
        public LabelAction(DocumentationControl par)
        {
            parent = par;
            if (m_Label != null)
            {
                setText(m_Label.getText());
                setSmallIcon(m_Label.getIcon());
            }
        }
        
        public void actionPerformed(ActionEvent e)
        {
        }
        
        public boolean isEnabled()
        {
            //never enabled
            return false;
        }
        public int getStyle()
        {
            return AS_CUSTOM_COMPONENT;
        }
        public JComponent getCustomComponent()
        {
            return m_Label;
        }
    }
    
    public void createViewControl(JPanel parent)
    {
        parent.setLayout(new BorderLayout());
        JScrollPane pane = new JScrollPane(m_TextPane);
        parent.add(pane, BorderLayout.CENTER);
        
        String desc = DocumentationResources.getString("ACDS_EDITOR");
        String name = DocumentationResources.getString("ACNS_EDITOR");
        m_TextPane.getAccessibleContext().setAccessibleDescription(desc);
        m_TextPane.getAccessibleContext().setAccessibleName(name);
    }
    
    protected void contributeActionBars(JToolBar bars)
    {
        if (bars != null)
        {
            JToolBar tManager = bars; //.getToolBarManager();
            // tManager.setAlignmentX(JToolBar.LEFT_ALIGNMENT);
            
            String boldAcc = DocumentationResources.getString("ACNS_BOLD");
            JButton boldBtn = tManager.add(new BoldAction(this));
            boldBtn.getAccessibleContext().setAccessibleName(boldAcc);
            
            String italicAcc = DocumentationResources.getString("ACNS_ITALIC");
            JButton italicBtn = tManager.add(new ItalicAction(this));
            italicBtn.getAccessibleContext().setAccessibleName(italicAcc);
            
            String underlineAcc = DocumentationResources.getString("ACNS_UNDER");
            JButton underlineBtn = tManager.add(new UnderlineAction(this));
            underlineBtn.getAccessibleContext().setAccessibleName(underlineAcc);
            
            String colorAcc = DocumentationResources.getString("ACNS_FONTCOLOR");
            JButton colorBtn = tManager.add(new ColorAction(this));
            colorBtn.getAccessibleContext().setAccessibleName(colorAcc);
            
            String leftAcc = DocumentationResources.getString("ACNS_ALIGNLEFT");
            JButton leftBtn = tManager.add(new LeftAlignAction(this));
            leftBtn.getAccessibleContext().setAccessibleName(leftAcc);
            
            String centerAcc = DocumentationResources.getString("ACNS_CENTER");
            JButton centerBtn = tManager.add(new CenterAlignAction(this));
            centerBtn.getAccessibleContext().setAccessibleName(centerAcc);
            
            String rightAcc = DocumentationResources.getString("ACNS_ALIGNRIGHT");
            JButton rightBtn = tManager.add(new RightAlignAction(this));
            rightBtn.getAccessibleContext().setAccessibleName(rightAcc);
            
            tManager.setAlignmentX(JToolBar.LEFT_ALIGNMENT);
            
            // IZ# 78924 - conover: removed Font toolbar items
            // FontFamilyAction fontAction = new FontFamilyAction(this);
            // tManager.add(fontAction.getCustomComponent());
            //
            // FontSizeAction fontSizeAction = new FontSizeAction(this);
            // tManager.add(fontSizeAction.getCustomComponent());
        }
    }
    
        /*
         * gets the default font for the documentation pane from the preference file
         */
    public Font getDefaultFontFromPreferences()
    {
        Font pFont = null;
        IPreferenceAccessor pAccessor = PreferenceAccessor.instance();
        if (pAccessor != null)
        {
            try
            {
                String name = pAccessor.getFontName("DefaultDocFont");
                String size = pAccessor.getFontSize("DefaultDocFont");
                Integer height = new Integer(size);
                int style = Font.PLAIN;
                boolean bBold = pAccessor.getFontBold("DefaultDocFont");
                boolean bItalic = pAccessor.getFontItalic("DefaultDocFont");
                if (bBold)
                {
                    style |= Font.BOLD;
                }
                if (bItalic)
                {
                    style |= Font.ITALIC;
                }
                pFont = new Font(name, style, height.intValue());
            }
            catch(Exception e)
            {
                pFont = new Font("Arial", Font.PLAIN, 8);
            }
            
        }
        return pFont;
    }
    
    // Register keys
    protected void registerTreeAccelerator(final String accelerator)
    {
        ActionListener action = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                onAcceleratorAction(accelerator);
            }
        };
        registerKeyboardAction(action, KeyStroke.getKeyStroke(accelerator), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
    }
    
    //	Accelerator handler
    protected void onAcceleratorAction(String accelerator)
    {
        if (accelerator.equals(DocumentationResources.getString("IDSCTRLB"))) // Bold
        {
            //m_TextPane.requestFocus();
            StyledEditorKit.BoldAction action = new StyledEditorKit.BoldAction();
            action.putValue(Action.NAME, "Bold");
            action.actionPerformed(new ActionEvent(m_TextPane, 0, ""));
        }
        else if (accelerator.equals(DocumentationResources.getString("IDSCTRLI"))) // Italic
        {
            Action action = new StyledEditorKit.ItalicAction();
            action.putValue(Action.NAME, "Italic");
            action.actionPerformed(new ActionEvent(m_TextPane, 0, ""));
        }
        else if (accelerator.equals(DocumentationResources.getString("IDSCTRLU"))) //Underline
        {
            Action action = new StyledEditorKit.UnderlineAction();
            action.actionPerformed(new ActionEvent(m_TextPane, 0, ""));
        }
        else if (accelerator.equals(DocumentationResources.getString("IDSCTRLALTA"))) // Font Color Chooser
        {   try
            {
                Color c = JColorChooser.showDialog(this,
                        DocumentationResources.getString("IDS_TITLE"), m_TextPane.getForeground());
                if (c != null)
                {
                    Action act = new StyledEditorKit.ForegroundAction("", c);
                    act.actionPerformed(new ActionEvent(m_TextPane, 0, ""));
                }
            }
            catch (HeadlessException ex)
            {
                ex.printStackTrace();
            }
        }
        else if (accelerator.equals(DocumentationResources.getString("IDSCTRLSHITLC"))) // Center Alignment
        {
            Action action = new StyledEditorKit.AlignmentAction("", 1);
            action.actionPerformed(new ActionEvent(m_TextPane, 0, ""));
        }
        else if (accelerator.equals(DocumentationResources.getString("IDSCTRLSHITLL"))) //Left Alignment
        {
            Action action = new StyledEditorKit.AlignmentAction("", 0);
            action.actionPerformed(new ActionEvent(m_TextPane, 0, ""));
        }
        else if (accelerator.equals(DocumentationResources.getString("IDSCTRLSHITLR"))) //Right Alignment
        {
            Action action = new StyledEditorKit.AlignmentAction("", 2);
            action.actionPerformed(new ActionEvent(m_TextPane, 0, ""));
        }
    }
    
    public void startEdit()
    {
        m_TextPane.requestFocus();
        //m_TextPane.setCaretPosition(1);
        m_TextPane.setSelectionStart(1);
        m_TextPane.setSelectionEnd(1);
    }
}

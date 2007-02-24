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

package org.netbeans.modules.uml.ui.swing.drawingarea;

import java.awt.event.KeyEvent;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.uml.common.ETSystem;
import com.tomsawyer.editor.TSEGraph;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.editor.service.layout.jlayout.TSELayoutPropertiesDialog;
import com.tomsawyer.editor.service.layout.properties.TSETabComponent;
import com.tomsawyer.service.client.TSServiceProxy;
import com.tomsawyer.editor.service.TSEAllOptionsServiceInputData;
import com.tomsawyer.editor.TSEResourceBundleWrapper;
import com.tomsawyer.editor.service.layout.jlayout.AccessiblityUtils;

/**
 * @author KevinM
 *
 */
public class ETLayoutPropertiesDialog extends TSELayoutPropertiesDialog implements IETSecondaryWindow
{
    private boolean hasBeenVisible = false;
    private static TSEResourceBundleWrapper tsResBundle = TSEResourceBundleWrapper.getSystemLabelBundle();
    
    //public ETLayoutPropertiesDialog(Frame arg0, String arg1, TSEGraphWindow arg2, TSLayoutServer arg3)
    public ETLayoutPropertiesDialog(Frame arg0, String arg1, TSEGraphWindow arg2, TSServiceProxy arg3, TSEAllOptionsServiceInputData inputData)
    {
        super(arg0, arg1, arg2, arg3, inputData);
        if (arg3 == null)
        {
            ETSystem.out.println("null layout Server passed to the layout properties dialog.");
        }
    }
    
    //public ETLayoutPropertiesDialog(Frame arg0, String arg1, TSEGraphWindow arg2, TSLayoutServer arg3, Class arg4)
    public ETLayoutPropertiesDialog(Frame arg0, String arg1, TSEGraphWindow arg2, TSServiceProxy arg3, TSEAllOptionsServiceInputData inputData, Class arg4)
    {
        super(arg0, arg1, arg2, arg3, inputData, arg4);
    }
    
    //public ETLayoutPropertiesDialog(Frame arg0, String arg1, TSEGraphWindow arg2, TSLayoutServer arg3, int arg4)
    public ETLayoutPropertiesDialog(Frame arg0, String arg1, TSEGraphWindow arg2, TSServiceProxy arg3, TSEAllOptionsServiceInputData inputData, int arg4)
    {
        super(arg0, arg1, arg2, arg3, inputData, arg4);
    }
/*
   //public ETLayoutPropertiesDialog(Frame arg0, String arg1, TSEGraphWindow arg2, TSLayoutServer arg3, Class arg4, int arg5)
   public ETLayoutPropertiesDialog(Frame arg0, String arg1, TSEGraphWindow arg2, TSServiceProxy arg3, TSEAllOptionsServiceInputData inputData, Class arg4, int arg5)
   {
      super(arg0, arg1, arg2, arg3, inputData, arg4, arg5);
   }
 */
    
    protected void init()
    {
        super.init();
        setA11yFeatures();
    }
    
    
   /* (non-Javadoc)
    * @see com.tomsawyer.editor.layout.TSELayoutPropertiesDialog#onLayout()
    */
    public void onLayout()
    {
        IDrawingAreaControl drawingArea = getDrawingArea();
        if (drawingArea != null)
        {
            // Copy the info into the layout server.
            setLayoutStyleFromTab();
            onApply();
            
            // Use the DrawingArea Layout command to dispatch the layout command.
            drawingArea.setLayoutStyle(drawingArea.getLayoutStyle());
        }
        else
        {
            // No drawing area use the super class.
            super.onLayout();
        }
    }
    
   /*
    * Returns the Currently showing tab.
    */
    public TSETabComponent getActiveTab()
    {
        TSETabComponent[] tabs = this.getTabs();
        for (int i = 0; i < this.getNumberOfTabs(); i++)
        {
            TSETabComponent pTab = tabs[i];
            if (pTab != null && pTab.isShowing())
            {
                return pTab;
            }
        }
        return null;
    }
    
   /*
    * Returns the Layout Style of the currently showing tab as a string.
    */
    public int getActiveLayoutStyle()
    {
        //String layoutStyle = null;
        int layoutStyle = -1;
        
        TSETabComponent pActiveTab = getActiveTab();
        if (pActiveTab != null)
            layoutStyle = pActiveTab.getLayoutStyle();
        
        //if (layoutStyle == null || layoutStyle.length() == 0)
        if(layoutStyle == -1)
        {
            // Keep the current layout style.
            //layoutStyle = getGraphWindow().getGraph().getLayoutStyle();
        }
        return layoutStyle;
    }
    
   /*
    * Sets the graph windows active graph's layout style equal to the current tab style..
    */
    protected void setLayoutStyleFromTab()
    {
        //getGraphWindow().getGraph().setLayoutStyle(getActiveLayoutStyle());
        
        //set this thru serviceInputDataObject... as layoutoptions cannot be set on node/edge/graph itself in TS6.0
    }
    
   /*
    * Returns the DrawingArea Control
    */
    protected IDrawingAreaControl getDrawingArea()
    {
        ADGraphWindow graphWindow = getGraphWindow() instanceof ADGraphWindow ? (ADGraphWindow) getGraphWindow() : null;
        return graphWindow != null ? graphWindow.getDrawingArea() : null;
    }
    
   /*
    *  (non-Javadoc)
    * @see java.awt.Component#setVisible(boolean)
    */
    public void setVisible(boolean bShow)
    {
        super.setVisible(bShow);
        if (hasBeenVisible == false && bShow)
        {
            // There is a bug the bottom the layout props dlg is grey the first time its shown.
            super.setVisible(false);
            super.setVisible(true);
            hasBeenVisible = true;
        }
    }
    
    public void setA11yFeatures()
    {
        // Set mnemonics
        okButton.setMnemonic(AccessiblityUtils.getMnemonic("OK"));
        cancelButton.setMnemonic(AccessiblityUtils.getMnemonic("Cancel"));
        applyButton.setMnemonic(AccessiblityUtils.getMnemonic("Apply"));
        layoutButton.setMnemonic(AccessiblityUtils.getMnemonic("Layout"));
        resetButton.setMnemonic(AccessiblityUtils.getMnemonic("Reset"));
        defaultsButton.setMnemonic(AccessiblityUtils.getMnemonic("Defaults"));
        
        // set accessibility name and descritpion
        AccessiblityUtils.setAccessibleProperties(okButton,
            null, "OK");
        AccessiblityUtils.setAccessibleProperties(cancelButton,
            null, "Cancel");
        AccessiblityUtils.setAccessibleProperties(applyButton,
            null, "Apply");
        AccessiblityUtils.setAccessibleProperties(layoutButton,
            null, "Layout");
        AccessiblityUtils.setAccessibleProperties(resetButton,
            null, "Reset");
        AccessiblityUtils.setAccessibleProperties(defaultsButton,
            null, "Defaults");
        
        // set accessible description for the dialog itself
        this.getAccessibleContext().setAccessibleDescription(this.getTitle());
        tabbedPane.getAccessibleContext().setAccessibleDescription("");
        
        // set the font of tabbed to whatever font used for the tabbed panel
        TSETabComponent selTab = (TSETabComponent) tabbedPane.getSelectedComponent();
        Font defaultFont = selTab.getFont();
        if (defaultFont != null)
        {
            System.out.println("ETLayout: one of the tab's font="+ defaultFont);
            tabbedPane.setFont(defaultFont);
        }
        
        // set OK button the default button
        this.getRootPane().setDefaultButton(this.okButton);
        
        //Map hte ESC key to the action of closing the dailog.
        Action escapeAction = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                dispose();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);
        
    }
    
//   protected TSETabComponent createAppropriateTab(String tabName)
//   {
//      ETSystem.out.println("Creating Layout properties Tab " + tabName);
//      if (tabName.endsWith("TSESymmetricTab"))
//      {
//         try
//         {
//            return new ADSymmetricTab(getGraph(), inputData, this);
//         }
//         catch (RuntimeException e)
//         {
//            return null;
//         }
//      }
//      else if (tabName.endsWith("TSECircularTab"))
//      {
//         // We don't want to show this guy, until Pure Java 6.5.
//         return null;
//      }
//      else if (tabName.endsWith("TSEDisconnectedTab"))
//      {
//         try
//         {
//            return new ADDisconnectedTab(getGraph(),  inputData, this);
//         }
//         catch (RuntimeException e)
//         {
//            return null;
//         }
//      }
//      else if (tabName.endsWith("TSEGeneralTab"))
//      {
//         try
//         {
//            return new ADGeneralTab(getGraph(), inputData, this);
//         }
//         catch (RuntimeException e)
//         {
//            return null;
//         }
//      }
//      else if (tabName.endsWith("TSEHierarchicalTab"))
//      {
//         try
//         {
//            return new ADHierarchicalTab(getGraph(), inputData, this);
//         }
//         catch (RuntimeException e)
//         {
//            return null;
//         }
//      }
//      else if (tabName.endsWith("TSEOrthogonalTab"))
//      {
//         try
//         {
//            return new ADOrthogonalTab(getGraph(), inputData, this);
//         }
//         catch (RuntimeException e)
//         {
//            return null;
//         }
//      }
//      /* no tree in 6.0
//      else if (tabName.endsWith("TSETreeTab"))
//      {
//         try
//         {
//            return new ADTreeTab(getGraph(), inputData, this);
//         }
//         catch (RuntimeException e)
//         {
//            return null;
//         }
//      }
//       */
//
//      return super.createAppropriateTab(tabName);
//   }
    
   /* (non-Javadoc)
    * @see com.tomsawyer.editor.layout.TSELayoutPropertiesDialog#getGraph()
    */
    public TSEGraph getGraph()
    {
        TSEGraph theGraph = super.getGraph();
        if (theGraph == null && getDrawingArea() != null)
        {
            TSEGraphWindow window = getDrawingArea().getGraphWindow();
            if (window != null)
            {
                theGraph = window.getGraph();
            }
        }
        return theGraph;
    }
    
    
        /* (non-Javadoc)
         * @see java.awt.Component#show()
         */
    @SuppressWarnings("deprecation") // TODO: change to setVisible(boolean)?
    public void show()
    {
        super.show();
        
        // Hide the help button.
        hideHelpButton();
    }
    
   /*
    * Hides the help button if its visible.
    */
    protected void hideHelpButton()
    {/* jyothi
      // Hide the help button until we have help.
      if (helpButton != null && helpButton.isVisible())
      {
         helpButton.setVisible(false);
      }
     */
    }
    
    //jyothi
    public void setGraphWindow(TSEGraphWindow graphWindow)
    {
        // written to remove compilation error.. need to do more work..
    }
    
    // The following methods are provided by TomSawyer as a workaround
    // to fix the issue of not being able to localize the tab names
    /**
     * This method creates the tab object of the specified tab name.
     * If such a class does not exist, this method returns null.
     */
    protected TSETabComponent createAppropriateTab(String tabName)
    {
        TSETabComponent tabComponent = null;
        
        if (tabName.equals(tsResBundle.getStringSafely("General")))
        {  // NOI18N
            tabComponent =
                new com.tomsawyer.editor.service.layout.jlayout.TSEGeneralTabExt(
                this.getGraph(),
                this.inputData,
                this);
            
        }
        else if (tabName.equals(tsResBundle.getStringSafely("Disconnected")))
        {  // NOI18N
            tabComponent =
                new com.tomsawyer.editor.service.layout.jlayout.TSEDisconnectedTabExt(
                this.getGraph(),
                this.inputData,
                this);
            
        }
        else if (tabName.equals(tsResBundle.getStringSafely("Hierarchical")))
        {  // NOI18N
            tabComponent =
                new com.tomsawyer.editor.service.layout.jlayout.TSEHierarchicalTabExt(
                this.getGraph(),
                this.inputData,
                this);
            
        }
        else if (tabName.equals(tsResBundle.getStringSafely("Orthogonal")))
        {  // NOI18N
            tabComponent =
                new com.tomsawyer.editor.service.layout.jlayout.TSEOrthogonalTabExt(
                this.getGraph(),
                this.inputData,
                this);
            
        }
        else if (tabName.equals(tsResBundle.getStringSafely("Symmetric")))
        {  // NOI18N
            tabComponent =
                new com.tomsawyer.editor.service.layout.jlayout.TSESymmetricTabExt(
                this.getGraph(),
                this.inputData,
                this);
            
        }
        else if (tabName.equals(tsResBundle.getStringSafely("Routing")))
        {  // NOI18N
            tabComponent =
                new com.tomsawyer.editor.service.layout.jlayout.TSERoutingTabExt(
                this.getGraph(),
                this.inputData,
                this);
        }
        
        return tabComponent;
        
    }
    
    /**
     * This method returns the names of the tabs.
     */
    protected String [] getTabNames()
    {
        return internationalizedTabNames;
    }
    
    static String[] internationalizedTabNames =
    {
        tsResBundle.getStringSafely("General"),      // NOI18N
        tsResBundle.getStringSafely("Disconnected"), // NOI18N
        tsResBundle.getStringSafely("Hierarchical"), // NOI18N
        tsResBundle.getStringSafely("Orthogonal"),   // NOI18N
        tsResBundle.getStringSafely("Symmetric"),    // NOI18N
        tsResBundle.getStringSafely("Routing")       // NOI18N
    };
    
}

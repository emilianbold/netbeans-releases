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

package org.netbeans.modules.uml.drawingarea;

import org.netbeans.modules.uml.ui.controls.newdialog.INewDialogDiagramDetails;
import org.netbeans.modules.uml.ui.controls.newdialog.NewDialogDiagramDetails;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.support.diagramsupport.ProxyDiagramManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.SwingUtilities;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.ui.support.applicationmanager.IDiagramCallback;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
import org.netbeans.modules.uml.core.support.Debug;
import java.awt.Dialog;
import java.text.MessageFormat;
import java.util.Collection;
import org.netbeans.modules.uml.core.UMLSettings;
import org.netbeans.modules.uml.ui.controls.newdialog.INewUMLFileTemplates;
import org.netbeans.modules.uml.ui.controls.newdialog.NewUMLDiagWizardIterator;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;

/**
 * The diagram manager is used to manage the opening and closing of diagrams in
 * the platform that contains the environment.
 *  * @author Trey Spiva
 */
public class UMLDiagramManager 
      implements IProductDiagramManager, INewUMLFileTemplates
{
    private HashMap <String, DiagramTopComponent> m_OpenDiagrams = new HashMap<String, DiagramTopComponent>();
    private IDiagram m_CurrentDiagram = null;
    
    /**
     * Create a new diagram manager.
     */
    public UMLDiagramManager()
    {
        super();
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager#openDiagram(java.lang.String, boolean, org.netbeans.modules.uml.ui.support.applicationmanager.IDiagramCallback)
    */
    public IDiagram openDiagram(String sTOMFilename,
            boolean bMaximized,
            IDiagramCallback pDiagramCreatedCallback)
    {
        showDiagram(sTOMFilename);
        
        IDiagram retVal = retrieveDiagram(sTOMFilename);
        m_CurrentDiagram = retVal;
        if(pDiagramCreatedCallback != null)
        {
            pDiagramCreatedCallback.returnedDiagram(retVal);
        }
        raiseWindow(retVal);
        
        garbageCollect();
        return retVal;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager#openDiagram2(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, boolean, org.netbeans.modules.uml.ui.support.applicationmanager.IDiagramCallback)
    */
    public IDiagram openDiagram2(IProxyDiagram proxyDiagram,
            boolean bMaximized,
            IDiagramCallback pDiagramCreatedCallback)
    {
        
        return openDiagram(proxyDiagram.getFilename(), bMaximized, null);
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager#closeDiagram(java.lang.String)
    */
    public long closeDiagram(String sTOMFilename)
    {
        IDiagram retVal = retrieveDiagram(sTOMFilename);
        if (retVal != null)
        {
            retVal.preClose();
        }
        hideDiagram(retVal);
        return 0;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager#closeDiagram2(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram)
    */
    public long closeDiagram2(IDiagram diagram)
    {
        closeDiagram(diagram.getFilename());
        return 0;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager#closeDiagram3(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram)
    */
    public long closeDiagram3(IProxyDiagram diagram)
    {
        closeDiagram(diagram.getFilename());
        return 0;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager#newDiagramDialog(org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace, int, int, org.netbeans.modules.uml.ui.support.applicationmanager.IDiagramCallback)
    */
    public IDiagram newDiagramDialog(final INamespace pNamespace,
            final int nDefaultDiagram,
            final int lAvailableDiagramKinds,
            final IDiagramCallback callback)
    {
        if (pNamespace == null)
            return null;
        
        IDiagram dia = doNewDiagramDialog(pNamespace, nDefaultDiagram, lAvailableDiagramKinds);
        if (dia != null && callback != null)
        {
            callback.returnedDiagram(dia);
        }
        
        return dia;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager#raiseWindow(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram)
     */
    public long raiseWindow(IDiagram diagram)
    {
        final TopComponent tc = findTopComponent(diagram);
        
        if (tc != null)
        {
            Runnable runner = new Runnable()
            {
                public void run()
                {
                    tc.requestActive();
                }
            };
            SwingUtilities.invokeLater(runner);
        }
        
        else
        {
            showDiagram(diagram.getFilename());
        }
        
        return 0;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager#getCurrentDiagram()
    */
    public IDiagram getCurrentDiagram()
    {
        // TODO Auto-generated method stub
        return m_CurrentDiagram;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager#getOpenDiagram(java.lang.String)
    */
    public IDiagram getOpenDiagram(String sTOMFilename)
    {
//      IDiagram retVal = null;
//
//      DiagramTopComponent component = findTopComponent(sTOMFilename);
//      if(component != null)
//      {
//         IDrawingAreaControl ctrl = component.getDrawingAreaControl();
//         if(ctrl != null)
//         {
//            retVal = ctrl.getDiagram();
//         }
//      }
        return retrieveDiagram(sTOMFilename);
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager#createDiagram(int, org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace, java.lang.String, org.netbeans.modules.uml.ui.support.applicationmanager.IDiagramCallback)
    */
    public IDiagram createDiagram(int diagramKind,
            INamespace namespace,
            String diagramName,
            IDiagramCallback callback)
    {
        // UMLSettings.getDefault().incrementDiagramCount(diagramName, diagramKind);
        IDiagram retDia = doCreateDiagram(diagramKind, namespace, diagramName);
        
        if (retDia != null && callback != null)
            callback.returnedDiagram(retDia);

        m_CurrentDiagram = retDia;
        
        //call garbage collection explicitly to collect any left overs.
        garbageCollect();
        return retDia;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager#getOpenDiagrams()
    */
    public ETList<IProxyDiagram> getOpenDiagrams()
    {
        ETList < IProxyDiagram > retVal = new ETArrayList< IProxyDiagram >();
        
        java.util.Collection < DiagramTopComponent > values = m_OpenDiagrams.values();
        for(DiagramTopComponent curComp : values)
        {
            IDrawingAreaControl control = curComp.getDrawingAreaControl();
            IDiagram diagram = control.getDiagram();
            
            ProxyDiagramManager manager = ProxyDiagramManager.instance();
            IProxyDiagram proxy = manager.getDiagram(diagram);
            if(proxy != null)
            {
                retVal.add(proxy);
            }
        }
        
        return retVal;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager#minimizeDiagram(java.lang.String, boolean)
    */
    public long minimizeDiagram(String sTOMFilename, boolean bMinimize)
    {
        // TODO Auto-generated method stub
        return 0;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager#minimizeDiagram2(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, boolean)
    */
    public long minimizeDiagram2(IDiagram pDiagram, boolean bMinimize)
    {
        // TODO Auto-generated method stub
        return 0;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager#minimizeDiagram3(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, boolean)
    */
    public long minimizeDiagram3(IProxyDiagram pProxyDiagram, boolean bMinimize)
    {
        // TODO Auto-generated method stub
        return 0;
    }
    
    //**************************************************
    // Helper Methods
    //**************************************************
    
    protected IDiagram retrieveDiagram(String filename)
    {
        IDiagram retVal = null;
        
        ICoreProduct product = ProductRetriever.retrieveProduct();
        if (product instanceof IProduct)
        {
            retVal = ((IProduct)product).getDiagram(filename);
        }
        
        return retVal;
    }
    
    protected void hideDiagram(String diagramFile)
    {
        if(diagramFile != null)
        {
            final DiagramTopComponent component = findTopComponent(diagramFile);
            if(component != null)
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        String preferredID = component.preferredID();
                        component.close();
                        m_OpenDiagrams.remove(preferredID);
                    }
                });
            }
        }
    }
    
    protected void hideDiagram(IDiagram diagram)
    {
        if(diagram != null)
        {
            final DiagramTopComponent component = findTopComponent(diagram);
            if(component != null)
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        String preferredID = component.preferredID();
                        component.close();
                        m_OpenDiagrams.remove(preferredID);
                    }
                });
            }
        }
    }
    
    protected void hideDiagram(DiagramTopComponent component)
    {
        if(component != null)
        {
            final DiagramTopComponent comp = component;
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    comp.close();
                    String preferredID = comp.preferredID();
                    m_OpenDiagrams.remove(preferredID);
                }
            });
        }
    }
    
    protected void showDiagram(String diagramFile)
    {
        if(diagramFile != null)
        {
            DiagramTopComponent component = findTopComponent(diagramFile);
            
            if(component != null)
            {
                component.open();
                component.requestActive();
            }
            else
            {
                DiagramTopComponent topComponent = new DiagramTopComponent(diagramFile);
                showDiagram(topComponent);
            }
        }
    }
    
    protected void showDiagram(final DiagramTopComponent topComponent)
    {
        if(topComponent != null)
        {
            topComponent.addPropertyChangeListener(new DiagramPropertyListener());
            TopComponent.getRegistry().addPropertyChangeListener(new DiagramPropertyListener());
            
            ShowTopComponentGroup showGroup = new ShowTopComponentGroup("modeling-diagrams", topComponent);
            
            // Since the WindowsManager.findTopComponentGroup can only be called from
            // AWT event dispatch thread use the ShowTopComponentGroup class
            // can be used to display the group.  However, the only time we need to
            // execute the invokeAndWait is when we are currently not in the
            // AWT event dispatch thread.
            ///
            if(SwingUtilities.isEventDispatchThread() == true)
            {
                showGroup.show();
            }
            else
            {
                try
                {
                    SwingUtilities.invokeAndWait(showGroup);
                }
                catch(InterruptedException e)
                {
                }
                catch(java.lang.reflect.InvocationTargetException ie)
                {
                }
            }
            
            String preferredID = topComponent.preferredID(); 
            m_OpenDiagrams.put(preferredID, topComponent);
            
        }
    }
    
    public void refresh(IProxyDiagram proxy)
    {
//      GDDiagramTopComponent tc = getDiagramComponent();
//      if(tc != null)
//      {
//         GDDiagramTopComponent tcDiagram = GDDiagramTopComponent.getInstance();
//
//         if(proxy != null)
//         {
//            //pane.preClose();
//            //pane.load(filename);
//            tcDiagram.reloadDiagram(proxy);
//         }
//         else
//         {
//            String filename = proxy.getFilename();
//            doOpenDiagram(filename);
//         }
//      }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Helper Metohds
    
    protected void garbageCollect()
    {
//      System.gc();
//      System.runFinalization();
//      System.gc();
    }
    
    /////////////////////////////////////////////////////////////////////////////
    // Helper Classes
    
//   public IDiagram doNewDiagramDialog(INamespace pNamespace,
//                                      int defaultKind,
//                                      int selectableKinds)
//   {
//      IDiagram retDia = null;
////      Log.entry("Entering function NBDiagramManager::doNewDiagramDialog");
////
////      Log.out("doNewDiagramDialog called in diagram manager!");
////      Log.out("Got the newDiagramDialog!" );
//
//      INewDialog diag = new org.netbeans.modules.uml.ui.controls.newdialog.NewDialog();
//
//      if (diag != null)
//      {
//         diag.addTab(NewDialogTabKind.NWIK_NEW_DIAGRAM);
//
//         INewDialogDiagramDetails details = new NewDialogDiagramDetails();
//         details.setNamespace(pNamespace);
//         details.setDiagramKind(defaultKind);
//         details.setAvailableDiagramKinds(selectableKinds);
//         diag.specifyDefaults(details);
//
//         Frame handle = ProductHelper.getProxyUserInterface().getWindowHandle();
//         INewDialogTabDetails res = diag.display(handle);
//         if (res != null)
//         {
//            if (res instanceof INewDialogDiagramDetails)
//            {
//               INewDialogDiagramDetails det = (INewDialogDiagramDetails) res;
//               try
//               {
//
//                  String name = det.getName();
//                  INamespace space = det.getNamespace();
//                  int kind = det.getDiagramKind();
//
//                  // Fix J1671:  For some reason this code was searching for a diagram with the
//                  //             same name that we are trying to create.  This is a problem
//                  //             for example in CDFS when the user uses the same name twice, and
//                  //             expects to have 2 different diagrams.
//
//                  boolean found = false;
//
//                  //                   CLEAN, when we no longer need this code, see comment above
//                  //                      //first check if we already have a diagram created an opened for this name
//                  //                      ETList<IProxyDiagram> diags = getOpenDiagrams();
//                  //                      boolean found = false;
//                  //                      if (diags != null)
//                  //                      {
//                  //                         int count = diags.size();
//                  //                         for (int i=0; i<count; i++)
//                  //                         {
//                  //                            IProxyDiagram pDia = diags.get(i);
//                  //                            String pName = pDia.getName();
//                  //                            INamespace pSpace = pDia.getNamespace();
//                  //                            int pKind = pDia.getDiagramKind();
//                  //                            if ( (name != null && name.equals(pName)) &&
//                  //                                (space != null && space.isSame(pSpace)) &&
//                  //                                (kind == pKind) )
//                  //                            {
//                  //                               found = true;
//                  //                               retDia = pDia.getDiagram();
//                  //                               break;
//                  //                            }
//                  //                         }
//                  //                      }
//
//                  if (!found)
//                  {
//                     retDia = createDiagram(det.getDiagramKind(), det.getNamespace(),
//                     det.getName(), null);
//                  }
//               }
//               catch (Exception e)
//               {
//                  e.printStackTrace();
//                  //Log.stackTrace(e);
//               }
//            }
//         }
//      }
//      return retDia;
//   }
    
    public IDiagram doNewDiagramDialog(INamespace pNamespace,
            int defaultKind,
            int selectableKinds)
    {
        IDiagram retDia = null;
        
        INewDialogDiagramDetails details = new NewDialogDiagramDetails();
        details.setNamespace(pNamespace);
        details.setDiagramKind(defaultKind);
        details.setAvailableDiagramKinds(selectableKinds);
        
        //Jyothi:
        WizardDescriptor.Iterator iterator = new NewUMLDiagWizardIterator();
        WizardDescriptor wizardDescriptor = new WizardDescriptor(iterator);
        // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
        // {1} will be replaced by WizardDescriptor.Iterator.name()
        //wizardDescriptor.setTitleFormat(new MessageFormat("{0} ({1})"));
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle(org.openide.util.NbBundle.getMessage(UMLDiagramManager.class, "IDS_NEW_DIAGRAM_WIZARD_TITLE"));
        wizardDescriptor.putProperty(DIAGRAM_DETAILS, details);
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UMLDiagramManager.class, "IDS_NEW_DIAGRAM_WIZARD_TITLE_DESCRIPTION"));
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled)
        {
            // do something
            Object obj = wizardDescriptor.getProperty(DIAGRAM_DETAILS);
            if ((obj != null) && (obj instanceof INewDialogDiagramDetails))
            {
                INewDialogDiagramDetails det = (INewDialogDiagramDetails) obj;
                try
                {  
                    String name = det.getName();
                    INamespace space = det.getNamespace();
                    int kind = det.getDiagramKind();
                    
                    // Fix J1671:  For some reason this code was searching for a diagram with the
                    //             same name that we are trying to create.  This is a problem
                    //             for example in CDFS when the user uses the same name twice, and
                    //             expects to have 2 different diagrams.
                    
                    boolean found = false;
                    if (!found)
                    {
                        retDia = createDiagram(kind, space, name, null);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        return retDia;
    }
    
    public IDiagram doCreateDiagram(int kind, INamespace ns, String name)
    {
        DiagramTopComponent topComponent = new DiagramTopComponent(ns, name, kind);
        showDiagram(topComponent);
        
        return topComponent.getAssociatedDiagram();
    }
    
    public class DiagramPropertyListener implements PropertyChangeListener
    {
        Boolean groupVisible = null;
        
        public void propertyChange(PropertyChangeEvent evt)
        {
            Debug.out.println("Property Change: " + evt.getPropertyName());
            if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName()))
            {
                Debug.out.println("Inside of the property change listener");
                WindowManager wm = WindowManager.getDefault();
                TopComponentGroup group = wm.findTopComponentGroup("uml-diagram");
                Debug.out.println("Found a group: " + group);
                if(group != null)
                {
                    boolean diagramSelected = false;
                    Iterator it = wm.getModes().iterator();
                    while (it.hasNext())
                    {
                        org.openide.windows.Mode mode = (org.openide.windows.Mode) it.next();
                        TopComponent selected = mode.getSelectedTopComponent();
                        if (selected instanceof DiagramTopComponent)
                        {
                            diagramSelected = true;
                            break;
                        }
                    }
                    
                    Debug.out.println("diagramSelected = " + diagramSelected);
                    if (diagramSelected && !Boolean.TRUE.equals(groupVisible))
                    {
                        Debug.out.println("About to open the group");
                        group.open();
                    }
                    else if (!diagramSelected && !Boolean.FALSE.equals(groupVisible))
                    {
                        Debug.out.println("About to close the group");
                        group.close();
                    }
                    
                    groupVisible = diagramSelected ? Boolean.TRUE : Boolean.FALSE;
                }
            }
        }
    }
    
    /**
     * Searches all of the open diagram editor for the one that maps to the
     * specified diagram.
     *
     * @param diagramFile The file that contains the diagram information.
     * @return The TopComponent associated with the diagram.  <code>Null</code>
     *         will be returned if a TopComponent is not associated with the diagram.
     */
    protected DiagramTopComponent findTopComponent(String diagramFile)
    {
        IDiagram diagram = retrieveDiagram(diagramFile);
        return findTopComponent(diagram);
    }
    
    /**
     * Searches all of the open diagram editor for the one that maps to the
     * specified diagram.
     *
     * @param diagram The diagram to retrieve.
     * @return The TopComponent associated with the diagram.  <code>Null</code>
     *         will be returned if a TopComponent is not associated with the diagram.
     */
    protected DiagramTopComponent findTopComponent(IDiagram diagram)
    {
        DiagramTopComponent retVal = null;
        
        if(diagram != null)
        {
            String preferredID = DiagramTopComponent.preferredIDForDiagram(diagram);
            retVal = m_OpenDiagrams.get(preferredID);
//         FindTopComponentLocator locator = new FindTopComponentLocator(preferredID);
//
//         // Since the WindowsManager.findTopComponent can only be called from
//         // AWT event dispatch thread use the FindTopComponentLocator class
//         // to locate the top component.  However, the only time we need to
//         // execute the invokeAndWait is when we are currently not in the
//         // AWT event dispatch thread.
//         ///
//         if(SwingUtilities.isEventDispatchThread() == true)
//         {
//            locator.run();
//         }
//         else
//         {
//            try
//            {
//               SwingUtilities.invokeAndWait(locator);
//            }
//            catch(InterruptedException e)
//            {
//            }
//            catch(java.lang.reflect.InvocationTargetException ie)
//            {
//            }
//         }
//
//         TopComponent tc = locator.getTopComponent();
//         if(tc instanceof DiagramTopComponent)
//         {
//            retVal = (DiagramTopComponent)tc;
//         }
        }
        return retVal;
    }
    
    
    public void closeAllDiagrams()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                Collection < DiagramTopComponent > values = m_OpenDiagrams.values();
                for(DiagramTopComponent curComp : values)
                    curComp.close();
            }
        });       
    }
    
    /////////////////////////////////////////////////////////////////////////////
    // Helper Classes
    
//   /**
//    * A helper class used to find a top component that has a spcecifed ID.  This
//    * class is needed because the WindowsManager.findTopComponent method can only
//    * be called from the AWT event thread.  Therefore, when ever it is needed
//    * to locate a top component the FindTopComponentLocator class can be put
//    * into a invokeAndWait call.
//    */
//   public class FindTopComponentLocator implements Runnable
//   {
//      /** The top component that was located. */
//      private TopComponent mTopComponent = null;
//
//      /** The ID of the top component that must be located. */
//      private String mTopComponentID = "";
//
//      /**
//       * Initializes a new FindTopComponentLocator object.
//       *
//       * @param id The ID of the TopComponent to locate.
//       */
//      public FindTopComponentLocator(String id)
//      {
//         mTopComponentID = id;
//      }
//
//      /**
//       * Locates the TopComponnet.
//       */
//      public void run()
//      {
//         mTopComponent = WindowManager.getDefault().findTopComponent(mTopComponentID);
//      }
//
//      /**
//       * Retrieves the TopCompnent that was found.  You must call <code>run()</code>
//       * before calling the getTopComponent() method.
//       *
//       * @return The TopComponent that has the specified ID.  If no
//       *         TopComponents have the specified ID then <code>null</code> will
//       *         be returned.
//       */
//      public TopComponent getTopComponent()
//      {
//         return mTopComponent;
//      }
//   }
    
    /**
     * A helper class used to find a to open a specific component group.  This
     * class is needed because the WindowsManager.findTopComponentGroup method can only
     * be called from the AWT event thread.  Therefore, when ever it is needed
     * to open a component group the ShowTopComponentGroup class can be put
     * into a invokeAndWait call.
     */
    public class ShowTopComponentGroup implements Runnable
    {
        private String mGroupName = "";
        private DiagramTopComponent mTopComponent = null;
        
        public ShowTopComponentGroup(String name, DiagramTopComponent topComponent)
        {
            mGroupName = name;
            mTopComponent = topComponent;
        }
        
        public void run()
        {
            show();
        }
        
        public void show()
        {
            mTopComponent.open();
            mTopComponent.requestActive();
            
//         TopComponentGroup group = WindowManager.getDefault().findTopComponentGroup(mGroupName);
//         if(group != null)
//         {
//            group.open();
//         }
        }
    }
}


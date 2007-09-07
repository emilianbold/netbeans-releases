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

/*
 * UMLProjectModule.java
 *
 * Created on March 30, 2005, 3:10 PM
 */

package org.netbeans.modules.uml.project;

import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.coreapplication.CoreProductManager;
import org.netbeans.modules.uml.core.coreapplication.ICoreProductManager;
import org.netbeans.modules.uml.core.coreapplication.IProductDescriptor;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.applicationcore.ADProduct;
import org.netbeans.modules.uml.ui.products.ad.applicationcore.IADProduct;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.project.ui.UMLUserInterface;
import org.netbeans.modules.uml.project.ui.nodes.UMLModelRootNode;
import org.openide.modules.ModuleInstall;
import org.netbeans.modules.uml.ui.products.ad.projecttreedefaultengine.ADProjectTreeEngine;
import org.netbeans.modules.uml.project.ui.NetBeansUMLProjectTreeModel;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Trey Spiva
 */
public class UMLProjectModule extends ModuleInstall
{
    private static ADProjectTreeEngine mEngine = null;
//	private static NetBeansUMLProjectTreeModel mModel = new NetBeansUMLProjectTreeModel();
    private static NetBeansUMLProjectTreeModel mModel = null;
    
    /** Creates a new instance of UMLProjectModule */
    public UMLProjectModule()
    {
        
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // ModuleInstall Methods
    
    /**
     * Loads the UML application and the initializes the Application Designer
     * product.
     */
    public void restored()
    {
	lightInit();
    }

    private static void lightInit() 
    {
	if ( ! lightInitialized ) 
	{ 
	    synchronized(initLock) 
	    {
		if ( ! lightInitialized ) 
		{ 
		    final IADProduct product = getProduct();
		    mModel = new NetBeansUMLProjectTreeModel();
        
		    if(product != null)
		    {
//         RequestProcessor.getDefault().post(new Runnable()
//         {
//             public void run()
			{
                
			    DispatchHelper helper = new DispatchHelper();
			    helper.registerDrawingAreaEvents(mModel.getDrawingAreaListener());
			}
//         });
            
			//product.setDiagramManager(new UMLDiagramManager());
            
			// Put the user interface proxy
			product.setProxyUserInterface(new UMLUserInterface());
			product.setProjectManager(new UMLProductProjectManager());
            
			// m_AcceleratorMgr = m_ADProduct.getAcceleratorManager();
		    }
		    else
		    {
			DispatchHelper helper = new DispatchHelper();
			helper.registerDrawingAreaEvents(mModel.getDrawingAreaListener());
		    }        
		}
	    }
	}
    }

    private static Object initLock = new Object();
    private static boolean lightInitialized = false;
    private static boolean initialized = false;
 
    public static void checkInit() 
    {
	if ( ! initialized ) 
	{ 
	    synchronized(initLock) 
	    {
		if ( ! initialized ) 
		{ 
		    lightInit();
		    IADProduct product = getProduct();
		    IApplication app = product.initialize2(false);

		    mEngine = new ADProjectTreeEngine();
		    mEngine.initialize(mModel);

		    initialized = true;
		}
	    }
	}
    }
    
    /**
     * Makes sure that all of the UML projects are saved.
     */
    public boolean closing()
    {
        return true;
        // if project is modified user will be prompted, no need to save the project
        // automatically
//      boolean retVal = SaveProjectsDialog.saveProjects();
        
    }
    
    
    public void uninstalled()
    {
        final Project[] projects = ProjectUtil.getOpenUMLProjects();
        
        // 108119, save changes before uninstalling modules
        for (Project p: projects)
        {
            UMLProjectHelper h = (UMLProjectHelper)p.getLookup().lookup(UMLProjectHelper.class);
            if (h != null)
            {
                h.closeProject(true);
            }
        }
        
        SwingUtilities.invokeLater( new Runnable()
        {
            public void run()
            {
                for (Project p: projects)
                {
                    UMLProjectHelper h = (UMLProjectHelper)p.getLookup().lookup(UMLProjectHelper.class);
                    if (h != null)
                    {
                        IProductDiagramManager pDiaMgr =
                                ProductHelper.getProductDiagramManager();
                        pDiaMgr.closeAllDiagrams();
                    }
                }
                
                TopComponent tc = WindowManager.getDefault().findTopComponent("designpattern");
                if (tc != null)
                    tc.close();
                tc = WindowManager.getDefault().findTopComponent("documentation");
                if (tc != null)
                    tc.close();
                
            }
        });
        
        OpenProjects.getDefault().close(projects);
    }

    
    ////////////////////////////////////////////////////////////////////////////
    // Data Access Methods
    
    public static ADProjectTreeEngine getProjectTreeEngine()
    {
	checkInit();
        return mEngine;
    }
    
    public static NetBeansUMLProjectTreeModel getProjectTreeModel()
    {
	checkInit();
        return mModel;
    }
    
    public static void addModelNode(UMLModelRootNode node, UMLProject project)
    {
	checkInit();
        mModel.addModelRootNode(node, project);
    }
    
    
    //////////////////////////////////////////////////////////////////
    // Helper Methods
    
    protected static IADProduct getProduct()
    {
        IADProduct retVal = null;
        
        ICoreProductManager productManager = CoreProductManager.instance();
        ETList<IProductDescriptor> pDesc = productManager.getProducts();
        //ETList pDesc = productManager.getProducts();
        
        // Make sure that another project has not already created a product
        if((pDesc == null) || (pDesc.size() == 0))
        {
            // Create a new ADProduct
            retVal = new ADProduct();
            productManager.setCoreProduct(retVal);
        }
        else
        {
            IProductDescriptor descriptor = (IProductDescriptor)pDesc.get(0);
            if(descriptor.getCoreProduct() instanceof IADProduct)
            {
                retVal = (IADProduct)descriptor.getCoreProduct();
            }
        }
        
        return retVal;
    }
}

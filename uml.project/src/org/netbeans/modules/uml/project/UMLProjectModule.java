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

/*
 * UMLProjectModule.java
 *
 * Created on March 30, 2005, 3:10 PM
 */

package org.netbeans.modules.uml.project;

import javax.swing.SwingUtilities;
import org.openide.util.Lookup;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.coreapplication.CoreProductManager;
import org.netbeans.modules.uml.core.coreapplication.ICoreProductManager;
import org.netbeans.modules.uml.core.coreapplication.IProductDescriptor;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.applicationcore.ADProduct;
import org.netbeans.modules.uml.ui.products.ad.applicationcore.IADProduct;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
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

                        IProductDiagramManager diagManager = (IProductDiagramManager) Lookup.getDefault()
                            .lookup(IProductDiagramManager.class);
                        product.setDiagramManager(diagManager);

			{
                
			    DispatchHelper helper = new DispatchHelper();
			}
            
			// Put the user interface proxy
			product.setProxyUserInterface(new UMLUserInterface());
			product.setProjectManager(new UMLProductProjectManager());
                        product.setProjectTreeModel(mModel);
		    }
		    else
		    {
			DispatchHelper helper = new DispatchHelper();
		    }      
		    lightInitialized = true;
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
                    org.netbeans.modules.uml.UMLCoreModule.checkInit();
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

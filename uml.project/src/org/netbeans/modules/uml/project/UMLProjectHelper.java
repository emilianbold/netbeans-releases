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
 * UMLProjectHelper.java
 *
 * Created on February 19, 2004, 1:12 PM
 */

package org.netbeans.modules.uml.project;

import java.io.File;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.CacheDirectoryProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.w3c.dom.Element;
import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.coreapplication.CoreProductManager;
import org.netbeans.modules.uml.core.coreapplication.ICoreProductManager;
import org.netbeans.modules.uml.core.coreapplication.IProductDescriptor;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.applicationcore.ADProduct;
import org.netbeans.modules.uml.ui.products.ad.applicationcore.IADApplication;
import org.netbeans.modules.uml.ui.products.ad.applicationcore.IADProduct;
import org.netbeans.modules.uml.project.ui.MDREventProcessor;
import org.netbeans.modules.uml.project.ui.customizer.UMLProjectProperties;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/**
 *
 * @author  treys
 */
public class UMLProjectHelper
{
    private AntProjectHelper antHelper = null;
    private IProject mProject = null;
    private UMLProject mNBProject = null;
    private IADProduct mADProduct = null;
    private IADApplication mApplication = null;
    
    /** Creates a new instance of UMLProjectHelper */
    public UMLProjectHelper(AntProjectHelper aggregate, UMLProject project)
    {
        antHelper = aggregate;
        mNBProject = project;
    }
    
    public AntProjectHelper getAntProjectHelper()
    {
        return antHelper;
    }
    
    public EditableProperties getProperties(String str)
    {
        EditableProperties retValue = antHelper.getProperties(str);
        return retValue;
    }
    
    public int hashCode()
    {
        int retValue = antHelper.hashCode();
        return retValue;
    }
    
    public FileObject getProjectDirectory()
    {
        FileObject retValue= antHelper.getProjectDirectory();
        return retValue;
    }
    
    public Element getPrimaryConfigurationData(boolean param)
    {
        Element retValue = antHelper.getPrimaryConfigurationData(param);
        return retValue;
    }
    
    public String toString()
    {
        String retValue = antHelper.toString();
        return retValue;
    }
    
    public void addAntProjectListener(AntProjectListener antProjectListener)
    {
        antHelper.addAntProjectListener(antProjectListener);
    }
    
    public void putPrimaryConfigurationData(Element element, boolean param)
        throws java.lang.IllegalArgumentException
    {
        antHelper.putPrimaryConfigurationData(element, param);
    }
    
    public void putProperties(String str, EditableProperties editableProperties)
    {
        antHelper.putProperties(str, editableProperties);
    }
    
    public void removeAntProjectListener(AntProjectListener antProjectListener)
    {
        antHelper.removeAntProjectListener(antProjectListener);
    }
    
    public PropertyEvaluator getStandardPropertyEvaluator()
    {
        return antHelper.getStandardPropertyEvaluator();
    }
    
    public CacheDirectoryProvider createCacheDirectoryProvider()
    {
        return antHelper.createCacheDirectoryProvider();
    }
    
    public String resolvePath(String str)
    {
        String retValue;
        
        retValue = antHelper.resolvePath(str);
        return retValue;
    }
    
    public boolean equals(Object obj)
    {
        boolean retValue;
        
        retValue = antHelper.equals(obj);
        return retValue;
    }
    
    public FileObject resolveFileObject(String str)
    {
        FileObject retValue  = antHelper.resolveFileObject(str);
        return retValue;
    }
    
    public File resolveFile(String str)
    {
        File retValue;
        
        retValue = antHelper.resolveFile(str);
        return retValue;
    }
    
    public void setProject(IProject project)
    {
        mProject = project;
    }
    
    public IProject getProject()
    {
        if (mProject == null)
            initializeProject();
        
        return mProject;
    }
    
    public File getProjectFile()
    {
        File retVal = null;
        FileObject fo = getProjectDirectory();
        
        if (fo != null)
        {
            //retVal = new File(getProjectDirectory().getPath());
            retVal = org.openide.filesystems.FileUtil.toFile(fo);
        }
        
        return retVal;
    }
    
    protected String getDisplayName()
    {
        return mNBProject.getName();
    }
    
    
    protected void initializeProject()
    {
        if (mProject != null)
            return;
        
	UMLProjectModule.checkInit();

        mADProduct = getProduct();
        
        if (mADProduct != null)
        {
            IApplication app = mADProduct.initialize2(false);
            mApplication = (IADApplication)app;
        }
        
        if (mApplication != null)
        {
            String projName = getDisplayName();
            
            File projectFile =
                new File(getProjectFile(), GeneratedFilesHelper.BUILD_XML_PATH);
            
//         String relPath ="nbproject" +
//             File.separatorChar + projName + ".etd"; // NOI18N
            String relPath = projName + ".etd"; // NOI18N
            
            File projectFilename =  new File(projectFile.getParentFile(), relPath);
            
            mProject = null;
            // System.out.println("Going to open project " + relPath); // NOI18N
            
            if (projectFilename.exists() == true)
            {
                mProject = mApplication.openProject(projectFilename.getAbsolutePath());
//                mProject.setDirty(false);
            }
            
            if (mProject == null)
            {
                mProject = mApplication.createProject();
                
                EditableProperties ep =
                    getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                
                String mode = (String)ep.getProperty(UMLProjectProperties.MODELING_MODE);
                mProject.setMode(mode);
                mProject.setName(projName);
                mProject.setFileName(projectFilename.getAbsolutePath());
            }
            
            AssociatedSourceProvider provider =
                (AssociatedSourceProvider)mNBProject.getLookup()
                .lookup(AssociatedSourceProvider.class);
            
            mProject.setAssociatedProjectSourceRoots(provider);
        }
    }
    
    protected IADProduct getProduct()
    {
        IADProduct retVal = null;
        
        ICoreProductManager productManager = CoreProductManager.instance();
        //ETList<IProductDescriptor> pDesc = productManager.getProducts();
        ETList pDesc = productManager.getProducts();
        
        // Make sure that another project has not already created a product
        if ((pDesc == null) || (pDesc.size() == 0))
        {
            // Create a new ADProduct
            retVal = new ADProduct();
            productManager.setCoreProduct(retVal);
        }
        
        else
        {
            IProductDescriptor descriptor = (IProductDescriptor)pDesc.get(0);
            
            if(descriptor.getCoreProduct() instanceof IADProduct)
                retVal = (IADProduct)descriptor.getCoreProduct();
        }
        
        return retVal;
    }
    
    /**
     * @param b
     */
    public void closeProject(boolean save)
    {
        // The project will be saved if the user told the save dialog to save
        // the project.
        
        // cvc - CR 6269224
        // this was disabled. reenabled so that open diagram drawing areas will
        // close when its Project is closed
        // 80014, save documentation before exiting IDE
        // made changes to doc pane to listen to mouse event and update element 
//        TopComponent tc = WindowManager.getDefault().findTopComponent("documentation");
//        if (tc != null)
//            tc.canClose();
        
        if (mProject == null)
            return;
        
        mApplication.closeProject(mProject, save);
        mNBProject.removeUMLProjectMetaListener();
        mProject = null;
    }
    
    
    // MCF TODO - I am adding some stuff here that is copied from the
    // UpdateHelper. I am not sure if we need a full blown UpdateHelper or
    // whether we integrate what we need into this class.
    // Review as we go.
    
    
    public void saveProject()
    {
        mApplication.saveProject(mProject);
    }
    
    public void scanSourceGroups()
    {
        scanSourceGroups(mNBProject);
    }
    
    public static void scanSourceGroups(UMLProject currentUMLProj)
    {
        if (currentUMLProj!=null)
        {
            UMLProjectProperties props = currentUMLProj.getUMLProjectProperties();
            props.init();
            
            Lookup l = currentUMLProj.getLookup();
            final AssociatedSourceProvider asp =
                (AssociatedSourceProvider)l.lookup(AssociatedSourceProvider.class);
            
            if (asp != null)
            {
                final SourceGroup[] grp = asp.getSourceGroups();
                RequestProcessor rp = new RequestProcessor
                    ("Post Scan Request Processor"); // NOI18N
                
                rp.post(new Runnable()
                {
                    public void run()
                    {
//                        ((JMManager)JMManager.getManager()).waitScanFinished();
                        MDREventProcessor.getInstance().fireChanged(null,grp);
                    }
                });
            }
        }
    }
}

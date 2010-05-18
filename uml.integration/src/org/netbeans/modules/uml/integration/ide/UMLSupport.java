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

/*
 * Created on Mar 29, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.netbeans.modules.uml.integration.ide;

import java.util.ResourceBundle;
import java.util.StringTokenizer;

//import org.netbeans.jmi.javamodel.Resource;
//import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.uml.common.Util;

import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.coreapplication.CoreProductManager;
import org.netbeans.modules.uml.core.coreapplication.ICoreProductManager;
import org.netbeans.modules.uml.core.coreapplication.IProductDescriptor;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.ui.products.ad.applicationcore.ADProduct;
import org.netbeans.modules.uml.ui.products.ad.applicationcore.IADProduct;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
import org.netbeans.modules.uml.integration.ide.events.EventHandler;
import org.netbeans.modules.uml.integration.ide.listeners.IAttributeChangeListener;
import org.netbeans.modules.uml.integration.ide.listeners.IClassChangeListener;
import org.netbeans.modules.uml.integration.ide.listeners.IEnumLiteralChangeListener;
import org.netbeans.modules.uml.integration.ide.listeners.IOperationChangeListener;
import org.netbeans.modules.uml.util.DummyCorePreference;
import org.openide.util.NbPreferences;


/**
 * @author alagud
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class UMLSupport
{
    
    private static UMLSupport umlsupport;
    private static ProductProjectManager  mProdManager;
    private static IProject defaultProject = null;
//    private static String projectSourceDirectory;
//    private static String defaultProjectXMI = null;
//    private static String defaultProjectLocation = null;
    private static final String IMPLEMENTATION_MODE = "PSK_IMPLEMENTATION"; // NOI18N
    private static boolean roundtripEnabled = true;
    private IProduct product;
    private EventHandler rtQueue = new EventHandler("RoundtripQueue"); // NOI18N
    static ResourceBundle messages = ResourceBundle.getBundle(
        "org.netbeans.modules.uml.integration.ide.Bundle"); // NOI18N
    protected IClassChangeListener classChangeListener;
    protected IOperationChangeListener operationChangeListener;
    protected IAttributeChangeListener attributeChangeListener;
    protected IEnumLiteralChangeListener enumLiteralChangeListener;
    
    /** navigate to source on selecting class on diagram */
    public static boolean navigateToSource = true;
    private IIDEManager ideManager;
    private DefaultSinkManager mSinkManager;
    public static int SU_IDE_NONE;
    public static final String  PROJ_EXT = ".etd"; // NOI18N
    
    /**
     * Singleton insatance of UMLSupport
     */
    public static UMLSupport getUMLSupport()
    {
        if (umlsupport == null)
            umlsupport = new UMLSupport();
        return umlsupport;
        
    }
    
// IZ 78480: conover
// I made this method static. Not sure why it wasn't before
// because mProdManager is a static member
    public static ProductProjectManager getProjectManager()
    {
        if( mProdManager==null )
            mProdManager = new ProductProjectManager();
        
        return mProdManager;
    }
    
    public static IProject getDefaultProject()
    {
        return defaultProject;
    }
    
    /**
     * Determines whether the given Describe project is in the Implementation
     * mode.
     *
     * @param proj The Describe project to be examined, possibly null.
     * @return <code>true</code> if proj is non-null and in Implementation mode.
     */
    public static boolean isImplementationProject(IProject proj)
    {
        return proj != null && IMPLEMENTATION_MODE.equals(proj.getMode());
    }
    
    /**
     * Sets whether the given project is the 'default' project. The
     * default project is usually the target for source-model roundtrip.
     *
     * @param proj The IProject that's the new default, possibly null.
     */
    public static void setDefaultProject(IProject proj)
    {
        Log.out("In setDefaultProject() : " + proj); // NOI18N
        
        defaultProject      = proj;
        
        // Remember the XMI ID. If the IProject reference gets clobbered, we'll
        // need the XMI ID to identify the default project from the available
        // projects.
        if (defaultProject != null)
        {
            getUMLSupport().getProjectManager().setCurrentProject(proj);
//            defaultProjectXMI = defaultProject.getXMIID();
//            defaultProjectLocation = defaultProject.getFileName();
//            projectSourceDirectory = proj.getSourceDir();
            
            // The default project may be in Analysis mode, which is not what
            // we want, so we force it to Implementation mode.
            String existingMode = defaultProject.getMode();
            Log.out("Project " + defaultProject.getName() + " is in mode '" // NOI18N
                + existingMode + "'"); // NOI18N
            if (!isImplementationProject(defaultProject))
            {
                try
                {
                    Log.out("Forcing project " + defaultProject.getName() // NOI18N
                    + " to " + IMPLEMENTATION_MODE + " mode"); // NOI18N
                    defaultProject.setMode(IMPLEMENTATION_MODE);
                }
                catch (Exception ex)
                {
                    Log.stackTrace(ex);
                }
            }
            if (!DefaultSinkManager.RT_LANGUAGE.equals(
                defaultProject.getDefaultLanguage()))
                defaultProject.setDefaultLanguage(
                    DefaultSinkManager.RT_LANGUAGE);
        }
        else
        {
//            defaultProjectXMI = null;
//            defaultProjectLocation = null;
//            projectSourceDirectory = null;
        }
        
        // The default project can't be a library, since it's the target for
        // source-to-model roundtrip activity.
        if (defaultProject != null && defaultProject.getLibraryState())
            defaultProject.setLibraryState(false);
    }
    
    public static IProject getCurrentProject()
    {
        // IZ 78480: conover
        // I added the static getter because the static member was
        // was null everytime.
        return getProjectManager().getCurrentProject();
    }
    
    /**
     *  Sets whether roundtrip events will be sent to the registered event
     * sinks for model changes.
     *
     * @param enabled <code>true</code> to enable the roundtrip.
     */
    public static void setRoundTripEnabled(boolean enabled)
    {
        if (enabled != roundtripEnabled)
        {
            IProduct p = ProductHelper.getProduct();
            p.getRoundTripController().setMode(enabled? 1 : 0);
            
            roundtripEnabled = enabled;
        }
    }
    
    /**
     * @return
     */
    public static boolean isRoundTripEnabled()
    {
        return roundtripEnabled;
    }
    /**
     *
     */
    public IProduct getProduct1()
    {
        if(product == null)
            product = ProductHelper.getProduct();
        return product;
    }
    
    /**
     * Returns the locate specific value for the String message.
     * @param message
     */
    public static String getString(String message)
    {
        try
        {
            return messages.getString(message);
        }
        catch (java.util.MissingResourceException mr)
        {
            Log.out("Resource for " + message + " not found"); // NOI18N
            return "!!" + message + "!!";
        }
    }
    
    public EventHandler getRoundtripQueue()
    {
        return rtQueue;
    }
    
    public String getCollectionOverride()
    {
        //kris richards - made change to nbpreferences
        return NbPreferences.forModule(DummyCorePreference.class).get("UML_COLLECTION_OVERRIDE_DEFAULT", "java.util.ArrayList"); // NOI18N
    }
    
    public IApplication getApplication()
    {
        return ProductHelper.getApplication();
        
    }
    public static IADProduct getProduct()
    {
        IADProduct retVal = null;
        
        ICoreProductManager productManager = CoreProductManager.instance();
        //ETList<IProductDescriptor> pDesc = productManager.getProducts();
        ETList pDesc = productManager.getProducts();
        
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
    
    /* NB60TBD
    public static IProject getProjectForPath(String filename)
    {
        IProject retVal = null;
        if(filename==null)
            return retVal;
        filename = filename.replace("%20"," ");
        filename = NBFileUtils.normalizeFile(filename);
        File test =  new File(filename);
        if(!test.exists())
        {
            while(test.getParentFile() !=null&& test.getParentFile().exists() )
            {   test = test.getParentFile();
                FileObject fobj = FileUtil.toFileObject(test);
                Log.out("Project Path is Null::::getProjectForPath"); // NOI18N
                Project currentJavaProj =
                    FileOwnerQuery.getOwner(fobj);
                Project uml = UMLJavaAssociationUtil.getAssociatedUMLProject(currentJavaProj);
                UMLProjectHelper mHelper = null;
                mHelper = (UMLProjectHelper) uml.getLookup().lookup(UMLProjectHelper.class);
                retVal = mHelper.getProject();
                return retVal;
            }
        }
        
        FileObject fobj = FileUtil.toFileObject(new File(filename));
        if(fobj==null)
            return null;
        DataObject dObj = null;
        try
        {
            dObj = DataObject.find(fobj);
        }
        catch (DataObjectNotFoundException e)
        {
            Log.stackTrace(e);
        }
        Project umlProject = UMLJavaAssociationUtil.getAssociatedUMLProject(dObj);
        if( umlProject == null )
            return null;
        UMLProjectHelper mHelper = null;
        mHelper = (UMLProjectHelper) umlProject.getLookup().lookup(UMLProjectHelper.class);
        retVal = mHelper.getProject();
        
        return retVal;
        
    }
    */
    /* NB60TBD
    public IProject getProjectForClassElement(ClassElement el)
    {
        String retVal = null;
        IProject iproj = null;
        DataObject dobj = (DataObject)el.getCookie(DataObject.class);
        if(dobj!=null)
        {
            iproj = getProjectForPath(FileUtil.toFile(dobj.getPrimaryFile()).getAbsolutePath());
        }
        return iproj;
    }
    */
    /* NB60TBD
    public String getProjectName(ClassElement el)
    {
        String retVal = null;
//        IProject iproj = null;
        DataObject dobj = (DataObject)el.getCookie(DataObject.class);
        if(dobj!=null)
        {
//        iproj = getProjectForPath(FileUtil.toFile(dobj.getPrimaryFile()).getAbsolutePath());
//
//        if( iproj!=null )
//            retVal = iproj.getName();
//        else
//        {
            Project currentProj = FileOwnerQuery.getOwner(dobj.getPrimaryFile());
            
            if( currentProj!=null )
                retVal = currentProj.getProjectDirectory().getName();
//        }
        }
        return retVal;
    }
    */    
/* NB60TBD 
    public static String getFileName(JavaClass clazz)
    {
        String retVal = null;
        Resource resource = clazz.getResource();
        
        FileObject fObj = JavaModel.getFileObject(resource);
        if (fObj != null)
        {
            retVal = fObj.getPath();
            if (retVal != null && retVal.length() > 0)
                retVal = NBFileUtils.normalizeFile(retVal);
        }
        return retVal;
    }
*/
/* NB60TBD 
    public static String getFileName(Resource resource)
    {
        String retVal = null;
        FileObject fObj = JavaModel.getFileObject(resource);
        if (fObj != null)
        {
            retVal = fObj.getPath();
            if (retVal != null && retVal.length() > 0)
                retVal = NBFileUtils.normalizeFile(retVal);
        }
        return retVal;
    }
*/    
    public static IWorkspace getCurrentWorkspace()
    {
        return null;
    }
    
    public static void setProjectForPath(String fileName, String projName)
    {
    }
    
    public static void reviveDescribe()
    {
        // TODO Auto-generated method stub
        
    }
    
    /**
     *  Returns the IIDEManager for the current IDE. Note that this reference
     * should not be cached, since the IDE might change it. Acquire the IDE
     * manager from GDProSupport before each use and do NOT cache it.<br>
     *  If the IDE has not implemented this interface, a reference to the stub
     * implementation IDEManagerAdapter is returned.
     *
     * @return IIDEManager The IDE's IIDEManager.
     */
    synchronized public IIDEManager getIDEManager()
    {
        if (ideManager == null)
            ideManager = new IDEManagerAdapter();
        return ideManager;
    }
    public void initializeSinkManager(DefaultSinkManager sm)
    {
        mSinkManager = sm;
        Log.out("About to boostrap Roundtrip"); // NOI18N
        UMLSupport.getUMLSupport().getSinkManager().registerForProcessorEvents();
        Log.out("registered for Processor events"); // NOI18N
        startRoundtripThread();
    }
    
    public DefaultSinkManager getSinkManager()
    {
        return mSinkManager;
    }
    
    public void closeProject(IProject proj)
    {
        // TODO Auto-generated method stub
        
    }
    
    public static boolean isClobbered()
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    public boolean isConnected()
    {
        // TODO Auto-generated method stub
        return true;
    }
    
    public void setAttributeChangeListener(IAttributeChangeListener listener)
    {
        RoundTripAttributeEventsSink
            .removeAttributeChangeListener(attributeChangeListener);
        attributeChangeListener = listener;
        RoundTripAttributeEventsSink.addAttributeChangeListener(listener);
    }
    
    public void setClassChangeListener(IClassChangeListener listener)
    {
        RoundTripClassEventsSink.removeClassChangeListener(classChangeListener);
        classChangeListener = listener;
        RoundTripClassEventsSink.addClassChangeListener(listener);
    }
    
    public void setEnumLiteralChangeListener(IEnumLiteralChangeListener listener)
    {
        RoundTripEnumLiteralEventsSink.removeEnumLiteralChangeListener(enumLiteralChangeListener);
        enumLiteralChangeListener = listener;
        RoundTripEnumLiteralEventsSink.addEnumLiteralChangeListener(listener);
    }
    
    public void setOperationChangeListener(IOperationChangeListener listener)
    {
        RoundTripOperationEventsSink
            .removeOperationChangeListener(operationChangeListener);
        operationChangeListener = listener;
        RoundTripOperationEventsSink.addOperationChangeListener(listener);
    }
    
    private void startRoundtripThread()
    {
//        rtQueue.setProgressFactory(new IProgressIndicatorFactory()  {
//            public IProgressIndicator getProgressIndicator() {
//                IIDEManager man = getIDEManager();
//                return man != null? man.getProgressIndicator() : null;
//            }
//        });
        rtQueue.startWorker();
    }
 
    public static boolean getUseGenericsDefault()
    {
        //kris richards - made change to nbpreferences
        return NbPreferences.forModule(DummyCorePreference.class).getBoolean("UML_USE_GENERICS_DEFAULT", false); // NOI18N
    }
    
    
    private static String extractNonCollectionType(String name)
    {
        String retVal = name;
        int genericStart = name.lastIndexOf('<');
        
        // see if this type has a generic type specified
        if (genericStart < 0)
        {
            // no '<' found so there is no generic type
            // now is it a Collection type?
            if (Util.isValidCollectionDataType(name))
            {
                // we have a Collection type with no Generic...
                //  so return Object as the type
                return "java.lang.Object"; // NOI18N
            }
        }
        
        // so we must have a generic type specified
        else
        {
            retVal = name.substring(genericStart + 1, name.indexOf('>'));
        }
        
        return retVal;
    }
    
    
    private static String convertGenericsToBrackets(String type)
    {
        int genericStart = type.lastIndexOf('<');
        
        // see if this type has a generic type specified
        if (genericStart < 0)
        {
            // no '<' found so there is no generic type
            // now is it a Collection type?
            if (Util.isValidCollectionDataType(type))
            {
                // we have a Collection type with no Generic...
                //  so return only one set of array brackets
                return "[]"; // NOI18N
            }
            
            else
                return ""; // NOI18N
        }

        else
        {
            StringTokenizer tokenizer = new StringTokenizer(type, "<"); // NOI18N
            int dimensions = tokenizer.countTokens() - 1;
            String brackets = ""; // NOI18N
            
            for (int i=0; i < dimensions; i++)
            {
                brackets += "[]"; // NOI18N
            }
            
            return brackets;
        }
    }

    private static boolean isArray(String type)
    {
        int arrayStart = type.lastIndexOf('[');
        return arrayStart > -1 ? true : false;
    }

    private static String getArrayType(String name)
    {
        String retVal = name;
        int index = name.indexOf('[');
        
        if (index > 0)
            retVal = name.substring(0, index);
        
        return retVal;
    }
    
}

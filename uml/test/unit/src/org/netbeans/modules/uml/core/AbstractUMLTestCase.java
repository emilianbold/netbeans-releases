package org.netbeans.modules.uml.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import org.netbeans.modules.uml.core.coreapplication.CoreProductManager;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IRelationFactory;
import org.netbeans.modules.uml.core.metamodel.infrastructure.RelationFactory;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IImplementation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.TestUtils;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.core.workspacemanagement.WorkspaceManagementException;
import org.netbeans.modules.uml.ui.products.ad.applicationcore.ADProduct;
import org.netbeans.modules.uml.ui.support.ProductHelper;/**
 * Test case that needs an active namespace (workspace, project) to work in.
 * 
 * @author darshans
 */
abstract public class AbstractUMLTestCase extends TestCase
{
    protected static ICoreProduct product = null;
    protected static UMLCreationFactory factory = null;
    protected static IWorkspace workspace;
    protected static IProject project;

    protected static IRelationFactory relFactory = new RelationFactory();

    static
    {
        initialize();
    }
    
    public static void initialize() 
    {
        product = getProduct();
        try
        {
            ProductHelper.getMessenger().setDisableMessaging(true);

            // we don't want to be prompted to save projects,
            // just save them automatically
            ProductHelper.getPreferenceManager().setPreferenceValue(
                "Default", "PromptToSaveProject", "PSK_NO"); // NOI18N
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        factory = (UMLCreationFactory) product.getCreationFactory();
        
        establishNamespaces();
    }

    private List<IElement> createdTypes = new ArrayList<IElement>();
    
    /**
     * A wrapper around the exceedingly clunky TypedFactoryRetriever. Types
     * created here are automatically added to the project and are automatically
     * deleted on tearDown() (so using this in a class field initializer is not
     * a bright idea).
     */
    protected <T extends IElement> T createType(String concreteClassName)
    {
        T t = new TypedFactoryRetriever<T>().createType(concreteClassName);
        if (t != null)
        {    
            project.addElement(t);
            createdTypes.add(t);
        }
        return t;
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        for (int i = createdTypes.size() - 1; i >= 0; --i)
        {
            IElement e = createdTypes.get(i);
            e.delete();
        }
        createdTypes.clear();
        super.tearDown();
    }
    
    protected IClass createClass(String name)
    {
        IClass c = factory.createClass(null);
        project.addOwnedElement(c);
        c.setName(name);
        return c;
    }
    
    protected IInterface createInterface(String name)
    {
        IInterface i = factory.createInterface(null);
        project.addOwnedElement(i);
        i.setName(name);        
        return i;
    }
    
    protected IClass createSuperclass(IClassifier subclass, String superName)
    {
        IClass superC = createClass(superName);
        IGeneralization gen = factory.createGeneralization(null);
        subclass.addGeneralization(gen);
        superC.addSpecialization(gen);
        return superC;
    }
    
    protected IInterface createSuperinterface(IClassifier subclass, String superName)
    {
        IInterface superI = createInterface(superName);
        IImplementation imp = factory.createImplementation(null);
        project.addElement(imp);
        subclass.addImplementation(imp);
        imp.setContract(superI);
        imp.setImplementingClassifier(subclass);
        return superI;
    }
    
    /**
     * Create/open workspace and project.
     */
    public static void establishNamespaces()
    {
        // Create our workspace and project under the current directory.
        File dir = new File(".", "test");
        
        // Clean up the directory so we don't have to deal with the existing
        // workspace and project.
        deleteDirectory(dir);
        
        dir.mkdir();
        workspace = getWorkspace(dir, "test");
        assertNotNull(workspace);
        project   = getProject("A");
        assertNotNull(project);
    }
    
    public static void closeNamespaces()
    {
        IApplication app = product.getApplication();
        if (app == null)
        {    
            initialize();
            app = product.getApplication();
        }
        app.closeAllProjects(false);
        app.closeWorkspace(workspace, null, false);
        project   = null;
        workspace = null;
        
        product.setCurrentWorkspace(null);
    }
    
    private static void deleteDirectory(File dir)
    {
       if( dir != null )
       {
            File[] files = dir.listFiles();
            if (files == null)
            	return;
            	
            if (files != null)
            {
               for (int i = 0; i < files.length; ++i)
               {
                  if (files[i].isDirectory())
                     deleteDirectory(files[i]);
                  else
                     files[i].delete();
               }
            
            }
           
           dir.delete();
       }
    }

    /**
     * @param workspace2
     * @param string
     * @return
     */
    protected static IProject getProject(String projName)
    {
        try
        {
            File workspaceDir =
                new File(new File(workspace.getLocation())
                    .getAbsoluteFile().getParentFile(),
                         projName);
            workspaceDir.mkdirs();
            File projFile = new File(workspaceDir, projName + ".etd");

            IWSProject wsproj = null;
            if (projFile.exists())
            {
                workspace.getWSProjects();
                wsproj = workspace.openWSProjectByLocation(projFile.toString());
            }
            else
            {
                wsproj =
                    workspace.createWSProject(
                        workspaceDir.toString(),
                        projName);
            }
            wsproj.setIsDirty(true);
            wsproj.save(projFile.toString());
            workspace.save();

            workspace.openWSProjectByName(projName);
            
            IApplication app = product.getApplication();
            IProject proj = app.getProjectByName(projName);
//			proj.setMode("PSK_IMPLEMENTATION");
			proj.setMode("Implementation");
            return proj;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail(e.toString());
        }
        return null;
    }

    /**
     * @param dir
     * @param string
     * @return
     */
    protected static IWorkspace getWorkspace(File dir, String wksName) 
    {
        try 
        {
            File wksFile = new File(dir, wksName + ".etw").getAbsoluteFile()
                                .getCanonicalFile();
            IWorkspace wks = null;
            
            wks = (wksFile.exists())? product.openWorkspace(wksFile.toString())
                : product.createWorkspace(wksFile.toString(), wksName);
        
            return wks;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail(e.toString());
        }
        return null;
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void finalize() throws WorkspaceManagementException
    {
        File wksFile = new File(workspace.getLocation()),
             prjFile = new File(project.getFileName());
        workspace.close(true);
        
        wksFile.delete();
        prjFile.delete();
    }
    
    private static ICoreProduct getProduct()
    {
        CoreProductManager.instance().setCoreProduct(new ADProduct());
        ICoreProduct prod = CoreProductManager.instance().getCoreProduct();
        prod.initialize();
        return prod;
    }
    
    public static void writeFile(String path, String contents) {
        TestUtils.writeFile(path, contents);
    }
}

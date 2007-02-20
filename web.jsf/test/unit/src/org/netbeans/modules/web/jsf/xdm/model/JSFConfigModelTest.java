/*
 * JSFConfigModelTest.java
 * JUnit based test
 *
 * Created on January 29, 2007, 3:18 PM
 */

package org.netbeans.modules.web.jsf.xdm.model;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.jsf.api.facesmodel.Converter;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponentFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModelFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFVersion;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigModelImpl;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Pisl
 */
public class JSFConfigModelTest extends NbTestCase {
    
    public JSFConfigModelTest(String testName) {
        super(testName);
    }
    
    
    @Override
    protected Level logLevel() {
        return Level.INFO;
    }

    protected void setUp() throws Exception {
        Logger.getLogger(JSFConfigModelImpl.class.getName()).setLevel(Level.FINEST);
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(JSFConfigModelTest.class);
        return suite;
    }
    
    public void testReadJSFVersion1_1() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-01.xml");
        FacesConfig facesConfig = model.getRootComponent();
        assertNotNull(facesConfig);
        System.out.println("facesConfig: " + facesConfig);
        Collection<ManagedBean> managedBeans = facesConfig.getManagedBeans();
        for (ManagedBean elem : managedBeans) {
            System.out.println(elem.getManagedBeanName() + ", " + elem.getManagedBeanClass() + ", " + elem.getManagedBeanScope());
        }
    }
    
    public void testReadJSFJPAExample() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-jsfjpa-example.xml");
        FacesConfig facesConfig = model.getRootComponent();
        assertNotNull(facesConfig);
        // testing managed bean
        Collection<ManagedBean> managedBeans = facesConfig.getManagedBeans();
        assertEquals("Number of managed beans ", 1, managedBeans.size());
        ManagedBean managedBean = managedBeans.iterator().next();
        assertEquals("ManagedBean name ", "usermanager", managedBean.getManagedBeanName());
        assertEquals("ManagedBean class ", "enterprise.jsf_jpa_war.UserManager", managedBean.getManagedBeanClass());
        assertEquals("ManagedBean scope ", "request", managedBean.getManagedBeanScope());
        //testing navigation rule
        Collection<NavigationRule> navigationRules = facesConfig.getNavigationRules();
        assertEquals("Number of navigation rules ", 1, navigationRules.size());
        NavigationRule navigationRule = navigationRules.iterator().next();
        assertNull("Rule description ", navigationRule.getDescription());
        assertNull("Rule from-view-id ", navigationRule.getFromViewId());
        Collection<NavigationCase> navigationCases = navigationRule.getNavigationCases();
        assertEquals("Number of navigation cases ", 3, navigationCases.size());
        Iterator<NavigationCase> it = navigationCases.iterator();
        NavigationCase navigationCase = it.next();
        assertEquals("login", navigationCase.getFromOutcome());
        assertEquals("/login.jsp", navigationCase.getToViewId());
        assertTrue(navigationCase.isRedirected());
        assertNull(navigationCase.getDescription());
        assertNull(navigationCase.getFromAction());
        navigationCase = it.next();
        assertEquals("create", navigationCase.getFromOutcome());
        assertEquals("/create.jsp", navigationCase.getToViewId());
        assertFalse(navigationCase.isRedirected());
        assertNull(navigationCase.getDescription());
        assertNull(navigationCase.getFromAction());
        navigationCase = it.next();
        assertEquals("app-main", navigationCase.getFromOutcome());
        assertEquals("/welcomeJSF.jsp", navigationCase.getToViewId());
        assertTrue(navigationCase.isRedirected());
        assertNull(navigationCase.getDescription());
        assertNull(navigationCase.getFromAction());
    }
    
    final String newDescription = "Some text.\n Test description\nnew line\n\nnew second line.";
    final String newFromViewID = "/haha.jsp";
    public void testChangeNavigationRuleJSFJPAExample() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-jsfjpa-example.xml");
        FacesConfig facesConfig = model.getRootComponent();
        assertNotNull(facesConfig);
        Collection<NavigationRule> navigationRules = facesConfig.getNavigationRules();
        assertEquals("Number of navigation rules ", 1, navigationRules.size());
        NavigationRule navigationRule = navigationRules.iterator().next();
        assertNull("Rule description ", navigationRule.getDescription());
        assertNull("Rule from-view-id ", navigationRule.getFromViewId());
        
        // provide change in the NavigationRule
        model.startTransaction();
        navigationRule.setDescription(newDescription);
        navigationRule.setFromViewId(newFromViewID);
        model.endTransaction();
        
        // test whether the change is in the model
        navigationRules = facesConfig.getNavigationRules();
        assertEquals("Number of navigation rules ", 1, navigationRules.size());
        navigationRule = navigationRules.iterator().next();
        assertEquals(newDescription, navigationRule.getDescription());
        assertEquals(newFromViewID, navigationRule.getFromViewId());
        
        // save the model into a tmp file and reload. then test again.
        dumpModelToFile(model, "test-config-01.xml");
        navigationRules = model.getRootComponent().getNavigationRules();
        navigationRule = navigationRules.iterator().next();
        assertEquals("Number of navigation rules ", 1, navigationRules.size());
        assertEquals(newDescription, navigationRule.getDescription());
        assertEquals(newFromViewID, navigationRule.getFromViewId());
        
        // delete change in the NavigationRule
        model.startTransaction();
        navigationRule.setDescription(null);
        navigationRule.setFromViewId(null);
        model.endTransaction();
        
        navigationRules = model.getRootComponent().getNavigationRules();
        navigationRule = navigationRules.iterator().next();
        assertNull(navigationRule.getDescription());
        assertNull(navigationRule.getFromViewId());
        dumpModelToFile(model, "test-config-02.xml");
    }
    
    public void testAddRemoveNavigationRuleJSFJPAExample() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-jsfjpa-example.xml");
        FacesConfig facesConfig = model.getRootComponent();
        assertNotNull(facesConfig);
        
        Collection<NavigationRule> navigationRules = facesConfig.getNavigationRules();
        assertEquals("Number of navigation rules ", 1, navigationRules.size());
        
        NavigationRule newRule = model.getFactory().createNavigationRule();
        newRule.setDescription(newDescription);
        newRule.setFromViewId(newFromViewID);
        
        model.startTransaction();
        facesConfig.addNavigationRule(newRule);
        model.endTransaction();
        
        // save the model into a tmp file and reload. then test.
        dumpModelToFile(model, "test-config-03.xml");
        navigationRules = model.getRootComponent().getNavigationRules();
        assertEquals("Number of navigation rules ", 2, navigationRules.size());
        Iterator <NavigationRule> iterator = navigationRules.iterator();
        iterator.next();
        newRule = iterator.next();
        assertEquals(newDescription, newRule.getDescription());
        assertEquals(newFromViewID, newRule.getFromViewId());
        
        model.startTransaction();
        model.getRootComponent().removeNavigationRule(newRule);
        model.endTransaction();
        
        dumpModelToFile(model, "test-config-04.xml");
        navigationRules = model.getRootComponent().getNavigationRules();
        assertEquals("Number of navigation rules ", 1, navigationRules.size());
    }
    
    public void testChangeNavigationCase() throws Exception{
        JSFConfigModel model = Util.loadRegistryModel("faces-config-jsfjpa-example.xml");
        FacesConfig facesConfig = model.getRootComponent();
        assertNotNull(facesConfig);
        
        Collection<NavigationRule> navigationRules = facesConfig.getNavigationRules();
        assertEquals("Number of navigation rules ", 1, navigationRules.size());
        NavigationRule navigationRule = navigationRules.iterator().next();
        Collection<NavigationCase> navigationCases = navigationRule.getNavigationCases();
        assertEquals("Number of navigation cases ", 3, navigationCases.size());
        NavigationCase navigationCase = navigationCases.iterator().next();
        
        model.startTransaction();
        navigationCase.setDescription("Test Description");
        navigationCase.setFromAction("hahatest");
        navigationCase.setToViewId("welcomme.test");
        navigationCase.setRedirected(false);
        model.endTransaction();
        
        dumpModelToFile(model, "test-config-01.xml");
        
        model.startTransaction();
        navigationCase.setRedirected(true);
        model.endTransaction();
        dumpModelToFile(model, "test-config-02.xml");
        
    }
    
    public void testAddRemoveNavigationCaseJSFJPAExample() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-jsfjpa-example.xml");
        FacesConfig facesConfig = model.getRootComponent();
        assertNotNull(facesConfig);
        
        Collection<NavigationRule> navigationRules = facesConfig.getNavigationRules();
        assertEquals("Number of navigation rules ", 1, navigationRules.size());
        NavigationRule navigationRule = navigationRules.iterator().next();
        Collection<NavigationCase> navigationCases = navigationRule.getNavigationCases();
        assertEquals("Number of navigation cases ", 3, navigationCases.size());
        NavigationCase newCase = model.getFactory().createNavigationCase();
        newCase.setDescription("Test case description");
        newCase.setFromOutcome("/fromOutcame.jsp");
        newCase.setToViewId("/toviewide.jsp");
        
        navigationRule.getModel().startTransaction();
        navigationRule.addNavigationCase(newCase);      
        navigationRule.getModel().endTransaction();
        
        System.out.println("pridam case");
        //Util.dumpToStream(((AbstractDocumentModel)model).getBaseDocument(), System.out);
        //model = Util.dumpAndReloadModel(model);        
        navigationRules = model.getRootComponent().getNavigationRules();
        assertEquals("Number of navigation rules ", 1, navigationRules.size());
        navigationRule = navigationRules.iterator().next();
        navigationCases = navigationRule.getNavigationCases();
        assertEquals("Number of navigation cases ", 4, navigationCases.size());
        
    }
    
    public void testJSFVersion() throws Exception{
        JSFConfigModel model = Util.loadRegistryModel("faces-config-jsfjpa-example.xml");
        assertEquals(JSFVersion.JSF_1_2, model.getVersion());
        model = Util.loadRegistryModel("faces-config-01.xml");
        assertEquals(JSFVersion.JSF_1_1, model.getVersion());
    }
    
    public void testComments() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-jsfjpa-example.xml");
        FacesConfig facesConfig = model.getRootComponent();
        assertNotNull(facesConfig);
        facesConfig.getPeer();
        
    }
    
    public void testEditable() throws Exception {
        File file = new File(getDataDir(), "faces-config1.xml");
        
        FileObject fileObject = FileUtil.toFileObject(file);
        ModelSource model = TestCatalogModel.getDefault().createModelSource(fileObject, false);
        //JSFConfigModel jsfConfig1 = JSFConfigModelFactory.getInstance().getModel(model);
        //assertFalse(jsfConfig1.getModelSource().isEditable());
        assertFalse(model.isEditable());
        ModelSource model2 = TestCatalogModel.getDefault().createModelSource(fileObject, true);
        //JSFConfigModel jsfConfig2 = JSFConfigModelFactory.getInstance().getModel(model2);        
        //assertTrue("The model should be editable ", jsfConfig2.getModelSource().isEditable());
        assertTrue(model2.isEditable());
    }
    
    private void dumpModelToFile (JSFConfigModel model, String fileName) throws Exception{
        File file = new File(getWorkDir(), fileName);
        System.out.println("workfile: " + file.getAbsolutePath());
        Util.dumpToFile(model, file);
    }
    
    public void testConverter() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-jsfjpa-example.xml");
        FacesConfig facesConfig = model.getRootComponent();
        assertNotNull(facesConfig);
        
        List <Converter> converters = facesConfig.getConverters();
        assertEquals("Number of converters ", 1, converters.size());
        
    }
}

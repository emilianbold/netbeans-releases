package org.netbeans.modules.visualweb.navigation;


/*
 * VWPContentModelProviderTest.java
 * JUnit 4.x based test
 *
 * Created on July 30, 2007, 6:55 PM
 */



import com.sun.org.apache.bcel.internal.generic.LOOKUPSWITCH;
import java.io.IOException;
import java.util.Collection;
import javax.swing.text.BadLocationException;
import junit.framework.*;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.web.jsf.navigation.pagecontentmodel.PageContentModel;
import org.netbeans.modules.web.jsf.navigation.pagecontentmodel.PageContentModelProvider;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataNode;
import org.openide.util.lookup.Lookups;
import org.openide.util.*;
import org.openide.util.test.MockLookup;



/**
 *
 * @author joelle
 */
public class VWPContentModelProviderTest extends NbTestCase {

    public VWPContentModelProviderTest() {
        super("VWPContentModelProviderTest");
    }    
    //test methods -----------
    

    
     @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        ClassLoader l = this.getClass().getClassLoader();
        MockServices.setServices(VWPContentModelProvider.class);   
        Lookup defaultLookup = Lookup.getDefault();
        
        //MockLookup.setLookup(Lookups.fixed(l), Lookups.metaInfServices(l));
    }
     
    //test methods -----------

    
    public void testPageContentModelProviderExists() throws BadLocationException,  IOException {
        System.out.println("getPageContentModel");
        FileObject fileObject = null;
        //VWPContentModelProvider instance = new VWPContentModelProvider();
        //PageContentModel expResult = null;
        //PageContentModel result = instance.getPageContentModel(fileObject);
        //assertEquals(expResult, result);
        
        //FileObject fileObject = ((DataNode)original).getDataObject().getPrimaryFile();
        Lookup.Template<PageContentModelProvider> templ = new Lookup.Template<PageContentModelProvider>(PageContentModelProvider.class);
        final Lookup.Result<PageContentModelProvider> result = Lookup.getDefault().lookup(templ);
        Collection<? extends PageContentModelProvider> impls =  result.allInstances();
        assertTrue( impls.size() > 0 );
        assertTrue( impls.toArray()[0] instanceof VWPContentModelProvider);
        
        
        for( PageContentModelProvider provider : impls){
            PageContentModel pageContentModel = provider.getPageContentModel(fileObject);
            //exit when you find one.
            if(pageContentModel != null){
                return;
            }
        }      
       
    }
     
}

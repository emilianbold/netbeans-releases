/*
 * ProxySettingsUITest.java
 *
 * Created on 10 May 2006, 11:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.subversion.main.checkout;

import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.subversion.operators.CheckoutWizardOperator;
import org.netbeans.test.subversion.operators.ProxyConfigurationOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;

/**
 *
 * @author peter
 */
public class ProxySettingsUITest extends JellyTestCase {
    
    String os_name;
    Operator.DefaultStringComparator comOperator; 
    Operator.DefaultStringComparator oldOperator;
    
    /** Creates a new instance of ProxySettingsUITest */
    public ProxySettingsUITest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {        
        System.out.println("### "+getName()+" ###");
        
    }
    
    protected boolean isUnix() {
        boolean unix = false;
        if (os_name.indexOf("Windows") == -1) {
            unix = true;
        }
        return unix;
    }
    
    public static Test suite() {
         return NbModuleSuite.create(
                 NbModuleSuite.createConfiguration(ProxySettingsUITest.class).addTest(
                    "testProxySettings",
                    "testProxyBeforeUrl"
                 )
                 .enableModules(".*")
                 .clusters(".*")
        );
     }
    
    public void testProxySettings() {
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        RepositoryStepOperator co1so = new RepositoryStepOperator();
        co1so.setRepositoryURL(RepositoryStepOperator.ITEM_HTTPS + "localhost");
        ProxyConfigurationOperator pco = co1so.invokeProxy();
        pco.verify();
        pco.useSystemProxySettings();
        pco.noProxyDirectConnection();
        pco.hTTPProxy();
        pco.setProxyHost("host");// NOI18N
        pco.setPort("8080");
        pco.checkProxyServerRequiresLogin(true);
        pco.setName("name");// NOI18N
        pco.setPassword("password");// NOI18N
        pco.ok();
        co.btCancel().pushNoBlock();
    }
    
    public void testProxyBeforeUrl() throws Exception {
        try {
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        RepositoryStepOperator co1so = new RepositoryStepOperator();
        co1so.setRepositoryURL(RepositoryStepOperator.ITEM_HTTPS);
                
        TimeoutExpiredException tee = null;
        try {
            co1so.invokeProxy();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull(tee);     
        
        co1so.setRepositoryURL(RepositoryStepOperator.ITEM_HTTP);
        tee = null;
        try {
            co1so.invokeProxy();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull(tee);     
        
        co1so.setRepositoryURL(RepositoryStepOperator.ITEM_SVN);
        tee = null;
        try {
            co1so.invokeProxy();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull(tee);     
        
        co1so.setRepositoryURL(RepositoryStepOperator.ITEM_SVNSSH);
        tee = null;
        try {
            co1so.invokeProxy();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull(tee);     
        co.btCancel().pushNoBlock();
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        }
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.cncppunit;

import org.netbeans.modules.cnd.testrunner.spi.TestHandlerFactory;
import org.netbeans.modules.cnd.testrunner.spi.TestHandlerFactoryProvider;

/**
 * Sample factory provider.
 *
 * @author Nikolay Krasilnikov (http://nnnnnk.name)
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.cnd.testrunner.spi.TestHandlerFactoryProvider.class)
public class CnCppUnitTestHandlerFactoryProvider implements TestHandlerFactoryProvider {

    public TestHandlerFactory getFactory() {
        return new CnCppUnitTestHandlerFactory();
    }

}

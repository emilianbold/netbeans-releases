/*
 * TestStatelessLRLocalHome.java
 *
 * Created on {date}, {time}
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package testGenerateJavaEE14;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

/**
 *
 * @author {user}
 */
public interface TestStatelessLRLocalHome extends EJBLocalHome {
    
    testGenerateJavaEE14.TestStatelessLRLocal create()  throws CreateException;

}

/*
 * TestCmpLocalHome.java
 *
 * Created on {date}, {time}
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package testGenerateJavaEE14;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;
import javax.ejb.FinderException;

/**
 *
 * @author {user}
 */
public interface TestCmpLocalHome extends EJBLocalHome {

    testGenerateJavaEE14.TestCmpLocal findByPrimaryKey(java.lang.Long key)  throws FinderException;
    
    testGenerateJavaEE14.TestCmpLocal create(java.lang.Long key)  throws CreateException;

}

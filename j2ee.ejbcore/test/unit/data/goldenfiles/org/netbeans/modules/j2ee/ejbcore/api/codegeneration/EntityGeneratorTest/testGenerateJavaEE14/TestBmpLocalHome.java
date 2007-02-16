/*
 * TestBmpLocalHome.java
 *
 * Created on {date}, {time}
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package testGenerateJavaEE14;

import javax.ejb.EJBLocalHome;
import javax.ejb.FinderException;

/**
 *
 * @author {user}
 */
public interface TestBmpLocalHome extends EJBLocalHome {

    testGenerateJavaEE14.TestBmpLocal findByPrimaryKey(java.lang.Long key) throws FinderException;
    
}

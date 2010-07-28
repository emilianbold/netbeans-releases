/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pack;

import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Stateless;

/**
 *
 * @author den
 */
@Stateless
public class NewSessionBean implements NewSessionBeanLocal {

    @PrePassivate
    public void businessMethod() {
    }

    @PostActivate
    public void method() {
    }

    public void operation() {
    }
    
}

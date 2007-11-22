/*
 * TestDBCompletion.java
 *
 * Created on May 24, 2006, 4:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package test;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author js201828
 */
@Entity
@Table(name="CUSTOMER")
// completion for tables
/**
@Table(name=|
PRODUCT
@Table(name="PRODUCT"
*/
public class TestDBCompletion implements java.io.Serializable {

    @Id
    private Long id;
    
    /** Creates a new instance of TestDBCompletion */
    // completion for columns 
/**
@Column(name=|
CUSTOMER. ADDRESSLINE1
@Column(name="ADDRESSLINE1"
*/
 
    public TestDBCompletion() {
      }
}

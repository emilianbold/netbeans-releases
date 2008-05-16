/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fu;

/**
 *
 * @author jp159440
 */
public class Subtype2 {

    class InnerClass extends FindSubtype {
        
    }
    
    Object o = new FindSubtype() {
        
    };
            
}

class TopLevel extends FindSubtype {
    
}

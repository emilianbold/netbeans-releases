/*
 * IdVerifier.java
 *
 * Created on March 25, 2005, 9:45 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package util;

/**
 *
 * @author blaha
 */
public class IdVerifier {
    
    /** Creates a new instance of IdVerifier */
    public IdVerifier() {
    }
    
    public boolean validate(String id){
        boolean result = true;
        
        for(int i = 0; i < id.length();i++){
            if(Character.isDigit(id.charAt(i)) == false)
                result = false;
        }
        return result;
    }
     
}

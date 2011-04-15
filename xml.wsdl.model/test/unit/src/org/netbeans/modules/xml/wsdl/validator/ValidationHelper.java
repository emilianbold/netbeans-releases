/*
 * ValidationHelper.java
 *
 * Created on June 29, 2006, 4:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.validator;

import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator;

/**
 *
 * @author radval
 */
public class ValidationHelper {
    
    private static Pattern p = Pattern.compile("\"?+\\{\\d\\}\"?+");
    
    /** Creates a new instance of ValidationHelper */
    public ValidationHelper() {
    }
    
    public static void dumpExpecedErrors(Set<String> expectedErrors) {
        int counter = 1;
        Iterator<String> it = expectedErrors.iterator();
        while(it.hasNext()) {
            String expectedError = it.next();
            System.out.println("expected error :"+ counter + " " +  expectedError);
            counter++;
        }
    }
    
    public static boolean containsExpectedError(Set<String> expectedErrors, String actualError) {
        boolean result = false;
        Iterator<String> it = expectedErrors.iterator();
        while(it.hasNext()) {
            String[] needToMatch = null;
            String expectedError = it.next();
            needToMatch = p.split(expectedError);

            //now let see if expected error can be matched with actual error.
            if(needToMatch != null) {
                //assume we have a match unless we found a mismatch below
                boolean foundMatch = true;
                for(int i = 0; i < needToMatch.length; i++) {
                    String match = needToMatch[i];
                    if(!actualError.contains(match)) {
                        //no exact match found.
                        foundMatch = false;
                        break;
                    }
                }
                
                result = foundMatch;
                if(result) {
                    break;
                }
            }
            
        }
        return result;
    }
    
    public static void dumpErrors(ValidationResult result) {
        int counter = 1;
        
        Iterator<Validator.ResultItem> it = result.getValidationResult().iterator();
        while(it.hasNext()) {
             Validator.ResultItem item = it.next();
             String expectedError = item.getDescription();
            System.out.println("found error :"+ counter + " " +  expectedError);
            counter++;
        }
    }
    
}

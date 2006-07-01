/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * ExPropertyEditorSupport.java
 *
 * Created on March 26, 2003, 4:38 PM
 */

package org.netbeans.beaninfo.editors;
import java.beans.*;
import org.openide.explorer.propertysheet.*;
/** Support class for ExPropertyEditor which provides means for validating
 *  hints from the PropertyEnv instance passed to attachEnv.  Forces
 *  subclasses to be fail-fast in the case that illegal values are passed
 *  via the PropertyEnv (the alternative is cryptic error messages when
 *  the editor tries to use the hints).
 * @author  Tim Boudreau
 * @version 1.0
 */
public abstract class ExPropertyEditorSupport extends PropertyEditorSupport implements ExPropertyEditor {
    
    /** Creates a new instance of ExPropertyEditorSupport */
    protected ExPropertyEditorSupport() {
    }
    
    /** Implementation of PropertyEditorSupport.attachEnv().  This method
     *  is final to ensure that the values from the env are validated.
     *  Subclasses should override attachEnvImpl to provide the actual
     *  attaching behavior.  attachEnvImpl is called first, then
     *  validateEnv (to avoid fetching the values twice). */
    public final void attachEnv(PropertyEnv env) {
        attachEnvImpl(env);
        validateEnv(env);
    }
    
    /** Perform the actual attaching of the PropertyEnv.  */
    protected abstract void attachEnvImpl(PropertyEnv env);
    
    /** Validate values stored in the PropertyEnv.  This method allows
     *  subclasses to be fail-fast if they are supplied illegal values
     *  as hints from the PropertyEnv.  Subclasses should confirm that any
     *  hints used by their property editor are valid values.  If they
     *  are not valid, an EnvException should be thrown with a clear
     *  description of the problem.  */
    protected abstract void validateEnv(PropertyEnv env);
    
    /** This class exists to enable unit tests to differentiate
     *  between code bugs in the editors and invalid values from
     *  the propertyEnv.  */
    public static class EnvException extends IllegalArgumentException {
        public EnvException(String s) { super(s); }
    }
    
    /** Utility method to convert an array of Objects into a comma
     *  delimited string. */
    protected static final String arrToStr(Object[] s) {
        if (s == null) return "null"; //NOI18N
        StringBuffer out = new StringBuffer(s.length * 10);
        for (int i=0; i < s.length; i++) {
            if (s[i] != null) {
                out.append(s[i]);
            } else {
                out.append("null");
            }
            if (i != s.length-1) {
                out.append(","); //NOI18N
            }
        }
        return out.toString();
    }
}

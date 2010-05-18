/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

/*
 * MethodParamValiator.java
 *
 * Created on February 9, 2005, 10:33 AM
 */

package org.netbeans.modules.visualweb.ejb.util;
import org.netbeans.modules.visualweb.ejb.datamodel.MethodInfo;
import org.netbeans.modules.visualweb.ejb.datamodel.MethodParam;
import org.openide.util.NbBundle;

/**
 * This class is used to make sure the parameter name is valid in Java sense 
 *
 * @author  cao
 */
public class MethodParamValidator {
    
    public MethodParamValidator() {
    }
    
    public static void validate(String paramName) throws InvalidParameterNameException {
        validate(paramName, null, -1);
    }
    
    public static void validate( String paramName, MethodInfo method, int argPos ) throws InvalidParameterNameException
    {
        // A legal parameter name 
        // - start with a letter
        // - no space
        // - not keyword
        
        // Make sure it is not one of the keywords
        if( JavaKeywords.isKeyword( paramName ) )
        {
            throw new InvalidParameterNameException( NbBundle.getMessage(MethodParamValidator.class, "PARAMETER_NAME_IS_KEYWORD", paramName ) );
        }
        
        for( int i = 0; i < paramName.length(); i ++ )
        {
            char theChar =paramName.charAt( i );
            
            // The first character must be a letter, underscore, or dollar sign
            if( i == 0 )
            {
                if( !Character.isJavaIdentifierStart( theChar ) ) {
                    throw new InvalidParameterNameException( NbBundle.getMessage(MethodParamValidator.class, "INVALID_FIRST_CHAR", paramName ) );
                }
            }
            else
            {
                if( !Character.isJavaIdentifierPart( theChar ) )
                {
                    if( Character.isSpaceChar( theChar ) )
                        throw new InvalidParameterNameException( NbBundle.getMessage(MethodParamValidator.class, "NO_SPACE_IN_PARAMETER_NAME", paramName ) );
                    else
                        throw new InvalidParameterNameException( NbBundle.getMessage(MethodParamValidator.class, "INVALID_CHAR", paramName, new Character(theChar).toString() ) );
                }
            }
            
            
        }
        
        if (method != null && argPos >= 0) {
            java.util.ArrayList parameters = method.getParameters();
            for (int i = 0; i < parameters.size(); i++) {
                if (i == argPos) continue;
                
                MethodParam param = (MethodParam)parameters.get(i);
                if (param.getName().equals(paramName)) {
                    throw new InvalidParameterNameException( NbBundle.getMessage(MethodParamValidator.class, "PARAMETER_NAME_IS_DUPLICATE", paramName ) );
                }
            }   
        }
    }
}

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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.mobility.e2e.classdata;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author Michal Skvor
 */
public class MethodData {
    
    private final String name, parentClassName;
    private final ClassData returnType;
    private final List<MethodParameter> parameters;
    
    private int requestID;
    
    private boolean isValidReturnType;
    private List<MethodParameter> myInvalidParameters;
    private String  myFQNReturnType;

    public MethodData( String parentClassName, String name, ClassData returnType, List<MethodParameter> parameters ) {
        this.name = name;
        this.parentClassName = parentClassName;
        this.returnType = returnType;
        this.parameters = parameters;
        isValidReturnType = true;
    }
    
    public MethodData( String parentClassName, String name, String returnType, 
            List<MethodParameter> parameters , boolean validReturnType ,
            List<MethodParameter> invalidParameters ) 
    {
        this( parentClassName , name , null , parameters );
        isValidReturnType = validReturnType;
        myInvalidParameters = invalidParameters;
        myFQNReturnType = returnType;
    }

    public String getName() {
        return name;
    }

    public String getParentClassName() {
        return parentClassName;
    }
    
    public ClassData getReturnType() {
        return returnType;
    }
    
    public boolean isValidReturnType(){
        return isValidReturnType;
    }

    public List<MethodParameter> getParameters() {
        return Collections.unmodifiableList( parameters );
    }
    
    public List<MethodParameter> getInvalidParameters() {
        if ( myInvalidParameters == null ){
            return Collections.EMPTY_LIST;
        }
        return Collections.unmodifiableList( myInvalidParameters );
    }
    
    public void setRequestID( int requestID ) {
        this.requestID = requestID;
    }
    
    public int getRequestID() {
        return requestID;
    }
    
    public String getReturnTypeAsText(){
        if ( myFQNReturnType == null ){
            return getReturnType() == null ? null : getReturnType().
                    getFullyQualifiedName();
        }
        else {
            return myFQNReturnType;
        }
    }
    
    public boolean equalsFQN( MethodData method ) {
        if (!method.getParentClassName().equals(parentClassName)) {
            return false;
        }
        if (!method.getName().equals(name)) {
            return false;
        }
        if ( returnType == null || method.getReturnType()==null ){
            return getReturnTypeAsText().equals( method.getReturnTypeAsText() );
        }
        else {
            if (!method.getReturnType().equals( returnType )) {
                return false;
            }
        }
        if (method.getParameters().size() != parameters.size()) {
            return false;
        }
        int i=0;
        for (MethodParameter parameter : parameters) {
            if (!method.getParameters().get( i ).equalsFQN( parameter)) {
                return false;
            }
            i++;
        }
        return true;
    }
    
    @Override
    public boolean equals( Object o ){
        if(!( o instanceof MethodData )) {
            return false;
        }
        MethodData md = (MethodData) o;
        if (!md.getParentClassName().equals(parentClassName)) {
            return false;
        }
        if (!md.getName().equals(name)) {
            return false;
        }
        if (!md.getReturnType().equals( returnType )) {
            return false;
        }
        if (md.getParameters().size() != parameters.size()) {
            return false;
        }
        for (int i = 0; i < parameters.size(); i++ ) {
            if (!md.getParameters().get( i ).equals( parameters.get( i ))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        if ( returnType != null ){
            if ( myFQNReturnType == null ){
                return parentClassName.hashCode() + 7*name.hashCode() + 
                    13*returnType.hashCode() + 17* paramsHashCode();
            }
            else {
                return parentClassName.hashCode() + 7*name.hashCode() + 
                13*returnType.hashCode() + 17* paramsHashCode() +
                    31*myFQNReturnType.hashCode();
            }
        }
        else {
            return parentClassName.hashCode() + 7*name.hashCode() 
                + 13*paramsHashCode();
        }
    }
    
    private int paramsHashCode(){
        if ( parameters == null ){
            return 0;
        }
        else {
            int result =0;
            for (MethodParameter param : parameters) {
                result = 31*result +param.hashCode();
            }
            return result;
        }
    }
}

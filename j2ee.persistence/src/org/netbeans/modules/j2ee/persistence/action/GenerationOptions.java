/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.j2ee.persistence.action;

import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;

/**
 * This class represents code generation options for invoking
 * <code>javax.persistence.EntityManager</code> .
 *
 * @author Erno Mononen
 */
public final class GenerationOptions {
    
    public enum Operation {
        // {0} the name of the entity manager instance
        // {1} the name of the given parameter, i.e. <code>parameterName</code>.
        // {2} the class of the given parameter, i.e. <code>parameterType</code>.
        // {3} the return type of the method, i.e. <code>returnType</code>.
        // {4} a query attribute for the query, i.e. <code>queryAttribute</code>.
        PERSIST("{0}.persist({1});"),
        MERGE("{0}.merge({1});"),
        REMOVE("{0}.remove({0}.merge({1}));"),
        FIND("return {0}.find({3}.class, {1});"),
        // here the query attribute represents the name of the entity class
        FIND_ALL(
                "return {0}.createQuery(\"select object(o) from {4} as o\").getResultList();",
                "javax.persistence.criteria.CriteriaQuery cq = {0}.getQueryBuilder().createQuery();cq.select(cq.from({4}.class));return {0}.createQuery(cq).getResultList();"
                ),
        //querry to get only items starting from {1}[0] up to {1}[1]-1
        FIND_SUBSET(
                "javax.persistence.Query q = {0}.createQuery(\"select object(o) from {4} as o\");\nq.setMaxResults({1}[1]-{1}[0]);\nq.setFirstResult({1}[0]);\nreturn q.getResultList();",
                "javax.persistence.criteria.CriteriaQuery cq = {0}.getQueryBuilder().createQuery();cq.select(cq.from({4}.class));javax.persistence.Query q = {0}.createQuery(cq);q.setMaxResults({1}[1]-{1}[0]);q.setFirstResult({1}[0]);return q.getResultList();"),
        //qurrry to get count(*) on a table
        COUNT("return ((Long) {0}.createQuery(\"select count(o) from DiscountCode as o\").getSingleResult()).intValue();");

        private String body;
        private String body2_0;
        
        private Operation(String body){
            this(body, body);
        }

        private Operation(String body, String body2_0){
            this.body2_0=body2_0;
            this.body = body;
        }

        /*
         * @return default body (for jpa 1.0)
         */
        public String getBody(){
            return getBody(Persistence.VERSION_1_0);
        }

        /*
         * @return body for corresponding jpa version, default is 1.0
         */
        public String getBody(String version){
            if(Persistence.VERSION_2_0.equals(version))
            {
                return body2_0;
            }
            else return body;
        }
    }
    
    private Operation operation;
    private String methodName;
    private String returnType;
    private String parameterName;
    private String parameterType;
    private String queryAttribute;
    
    /** Creates a new instance of GenerationOptions */
    public GenerationOptions() {
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    public Operation getOperation() {
        return operation;
    }

    public String getParameterName() {
        return parameterName;
    }

    public String getParameterType() {
        return parameterType;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

    public String getQueryAttribute() {
        return queryAttribute;
    }
    
    public String getReturnType() {
        return returnType;
    }
    
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    
    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    
    public void setQueryAttribute(String queryAttribute) {
        this.queryAttribute = queryAttribute;
    }
    
    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }
 
}

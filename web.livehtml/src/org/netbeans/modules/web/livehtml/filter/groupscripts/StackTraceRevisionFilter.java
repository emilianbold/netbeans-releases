/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.livehtml.filter.groupscripts;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.netbeans.modules.web.livehtml.StackTrace;
import org.netbeans.modules.web.livehtml.filter.RevisionFilter;

/**
 *
 * @author petr-podzimek
 */
public class StackTraceRevisionFilter implements RevisionFilter<JSONArray> {

    @Override
    public JSONArray filter(JSONArray jsonArray) {
        return jsonArray;
    }

    @Override
    public boolean match(JSONArray jsonArray1, JSONArray jsonArray2) {
        if (jsonArray1 == null || jsonArray2 == null) {
            return false;
        }
        
        if (jsonArray1.size() != jsonArray2.size()) {
            return false;
        }
        
        for (int i = 0; i < jsonArray1.size(); i++) {
            Object object1 = jsonArray1.get(i);
            Object object2 = jsonArray2.get(i);
            
            if (object1 instanceof JSONObject && object2 instanceof JSONObject) {
                JSONObject jSONObject1 = (JSONObject) object1;
                JSONObject jSONObject2 = (JSONObject) object2;
                final Object script1 = jSONObject1.get(StackTrace.SCRIPT);
                final Object script2 = jSONObject2.get(StackTrace.SCRIPT);
                final Object function1 = jSONObject1.get(StackTrace.FUNCTION);
                final Object function2 = jSONObject2.get(StackTrace.FUNCTION);
                final Object lineNumber1 = jSONObject1.get(StackTrace.LINE_NUMBER);
                final Object lineNumber2 = jSONObject2.get(StackTrace.LINE_NUMBER);
                final Object columnNumber1 = jSONObject1.get(StackTrace.COLUMN_NUMBER);
                final Object columnNumber2 = jSONObject2.get(StackTrace.COLUMN_NUMBER);
                
                if (!equals(script1, script2) ||
                        !equals(function1, function2) ||
                        !equals(lineNumber1, lineNumber2) || 
                        !equals(columnNumber1, columnNumber2)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private boolean equals(Object object1, Object object2) {
        if (object1 == null || object2 == null) {
            return object1 == object2;
        }
        return object1.equals(object2);
    }
    
}

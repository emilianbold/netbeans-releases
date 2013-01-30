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
package org.netbeans.modules.web.livehtml.filter;

import java.util.Collection;
import java.util.Collections;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.netbeans.modules.web.livehtml.StackTrace;

/**
 *
 * @author petr-podzimek
 */
public class FilterUtilities {
    
    public static boolean match(JSONArray jsonArray1, JSONArray jsonArray2) {
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
                
                if (!safeEquals(script1, script2) ||
                        !safeEquals(function1, function2) ||
                        !safeEquals(lineNumber1, lineNumber2) /*|| 
                        !safeEquals(columnNumber1, columnNumber2)*/) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private static boolean safeEquals(Object object1, Object object2) {
        if (object1 == null || object2 == null) {
            return object1 == object2;
        }
        return object1.equals(object2);
    }
    
//    protected static void putGoupedRevisionIndex(FilteredAnalysis.GroupedRevisions target, Integer targetIndex, Integer value) {
//        Set<Integer> record = target.get(targetIndex);
//        if (record == null) {
//            record = new HashSet<Integer>();
//            target.put(targetIndex, record);
//        }
//        record.add(value);
//    }
//
//    protected static void putGroupedRevisions(FilteredAnalysis.GroupedRevisions target, Integer targetIndex, Set<Integer> revisions) {
//        if (revisions != null) {
//            for (Integer revision : revisions) {
//                putGoupedRevisionIndex(target, targetIndex, revision);
//            }
//        }
//    }
    
    protected static Integer safeMax(Collection<Integer> collection) {
        if (collection == null) {
            return Integer.MIN_VALUE;
        } else {
            return Collections.max(collection);
        }
    }

//    private static void fixRemovedRevisions(List<Integer> indexes, FilteredAnalysis.GroupedRevisions source, FilteredAnalysis.GroupedRevisions target) {
//        Set<Integer> indexesToRemove = new HashSet<Integer>();
//        for (Map.Entry<Integer, Set<Integer>> entry : target.entrySet()) {
//            final Integer key = entry.getKey();
//            if (!indexes.contains(key)) {
//                final Set<Integer> values = entry.getValue();
//                final Integer indexReplacement = getIndexReplacement(indexes, key, source);
//                if (indexReplacement != null) {
//                    for (Integer value : values) {
//                        putGoupedRevisionIndex(target, indexReplacement, value);
//                    }
//                    indexesToRemove.add(key);
//                }
//            }
//        }
//        target.keySet().removeAll(indexesToRemove);
//    }
//
    /**
     * 
     * @param indexes
     * @param sources 
     */
//
//    protected static Integer getIndexReplacement(List<Integer> indexes, Integer index, FilteredAnalysis.GroupedRevisions source) {
//        for (Map.Entry<Integer, Set<Integer>> entry : source.entrySet()) {
//            final Integer key = entry.getKey();
//            final Set<Integer> values = entry.getValue();
//            if (values.contains(index) && indexes.contains(key)) {
//                return key;
//            }
//        }
//        return null;
//    }

}

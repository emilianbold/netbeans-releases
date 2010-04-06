/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.xml.xpath.ext.schema;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xpath.ext.spi.ExternalModelResolver;

/**
 * Contains a set of static functions to work with type inheritance. 
 * 
 * The derivation map has to be populated at first. 
 * See the method populateDerivationMap. 
 * 
 * Then the collected data can be analysed. 
 * 
 * @author nk160297
 */
public class TypeInheritanceUtil {

    public static Map<GlobalType, GlobalType> populateDerivationMap(
            ExternalModelResolver extModelResolver, boolean simpleTypesOnly) {
        //
        HashMap<GlobalType, GlobalType> result = new HashMap<GlobalType, GlobalType>();
        //
        CollectDerivationVisitor visitor = 
                new CollectDerivationVisitor(result, simpleTypesOnly);
        Collection<SchemaModel> visibleModels = extModelResolver.getVisibleModels();
        for (SchemaModel sModel : visibleModels) {
            visitor.collectDerivationFrom(sModel);
        }
        //
        return result;
    }
    
    public static boolean areTypesDerived(GlobalType derived, GlobalType from, 
            Map<GlobalType, GlobalType> derivationMap) { 
        //
        if (derivationMap == null || derived == null || from == null) {
            return false;
        }
        //
        GlobalType fromCandidate = derivationMap.get(derived);
        while (true) {
            if (fromCandidate == null) {
                break;
            }
            if (fromCandidate.equals(from)) {
                return true;
            }
            //
            fromCandidate = derivationMap.get(fromCandidate);
        }
        //
        return false;
    }
    
    public static boolean hasSubtype(GlobalType base, 
            Map<GlobalType, GlobalType> derivationMap) {
        return derivationMap.values().contains(base);
    }
    
    public static Set<GlobalType> getDirectSubtypes(GlobalType base, 
            Map<GlobalType, GlobalType> derivationMap) {
        // 
        HashSet<GlobalType> derived = new HashSet<GlobalType>();
        Set<GlobalType> keySet = derivationMap.keySet();
        for (GlobalType gType : keySet) {
            if (gType == base) {
                continue;
            }
            GlobalType baseCandidate = derivationMap.get(gType);
            if (baseCandidate == base) {
                derived.add(gType);
            }
        }
        //
        return derived;
    }
    
    public static Set<GlobalType> getAllSubtypes(GlobalType base, 
            Map<GlobalType, GlobalType> derivationMap) {
        // 
        // The method isn't optimised but has to work well with not very big 
        // amount of subtypes.
        HashSet<GlobalType> derived = new HashSet<GlobalType>();
        Set<GlobalType> directSubtypes = getDirectSubtypes(base, derivationMap);
        for (GlobalType subtype : directSubtypes) {
            derived.add(subtype);
            derived.addAll(getAllSubtypes(subtype, derivationMap));
        }
        //
        return derived;
    }
    
}

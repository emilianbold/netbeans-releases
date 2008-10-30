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

package org.netbeans.modules.csl.source.usages;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.csl.api.IndexDocument;

class IndexDocumentImpl implements IndexDocument {
    final List<String> indexedKeys;
    final List<String> indexedValues;
    final List<String> unindexedKeys;
    final List<String> unindexedValues;
    String overrideUrl;

    public IndexDocumentImpl(int initialCapacity) {
        indexedKeys = new ArrayList<String>(initialCapacity);
        indexedValues = new ArrayList<String>(initialCapacity);
        unindexedKeys = new ArrayList<String>(6); // Right? Or pass in?
        unindexedValues = new ArrayList<String>(6);
    }

    public IndexDocumentImpl(int initialCapacity, String overrideUrl) {
        this(initialCapacity);
        this.overrideUrl = overrideUrl;
    }
    
    public void addPair(String key, String value, boolean indexed) {
        if (indexed) {
            indexedKeys.add(key);
            indexedValues.add(value);
        } else {
            unindexedKeys.add(key);
            unindexedValues.add(value);
        }
    }
    
    public String getOverrideUrl() {
        return overrideUrl;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("IndexDocumentImpl("); // NOI18N
        for (int i = 0; i < indexedKeys.size(); i++) {
            sb.append(indexedKeys.get(i));
            sb.append(":"); // NOI18N
            sb.append(indexedValues.get(i));
            sb.append("\n"); // NOI18N
        }
        for (int i = 0; i < unindexedKeys.size(); i++) {
            sb.append(unindexedKeys.get(i));
            sb.append(":"); // NOI18N
            sb.append(unindexedValues.get(i));
            sb.append("\n"); // NOI18N
        }
        sb.append(")"); // NOI18N
        return sb.toString();
    }
}

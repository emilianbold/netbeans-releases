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

package org.netbeans.modules.websvc.saas.codegen.java.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author PeterLiu
 */
public class EntityClassInfo {

    public class FieldInfo {

        private String name;
        private String type;
        private String typeArg;
        private List<String> annotations;
        private Collection<FieldInfo> fieldInfos;

        public FieldInfo() {
            annotations = new ArrayList<String>();
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public String getSimpleTypeName() {
            return type.substring(type.lastIndexOf(".") + 1);
        }

        public void setTypeArg(String typeArg) {
            this.typeArg = typeArg;
        }

        public String getTypeArg() {
            return typeArg;
        }

        public void addAnnotation(String annotation) {
            this.annotations.add(annotation);
        }

        public boolean isId() {
            return matchAnnotation("@javax.persistence.Id") || matchAnnotation("@javax.persistence.EmbeddedId"); //NOI18N
        }

        public boolean isEmbeddedId() {
            return matchAnnotation("@javax.persistence.EmbeddedId"); //NOI18N
        }

        public boolean isRelationship() {
            return isOneToOne() || isOneToMany() || isManyToOne() || isManyToMany();
        }

        public boolean isOneToOne() {
            return matchAnnotation("@javax.persistence.OneToOne"); //NOI18N
        }

        public boolean isOneToMany() {
            return matchAnnotation("@javax.persistence.OneToMany"); //NOI18N
        }

        public boolean isManyToOne() {
            return matchAnnotation("@javax.persistence.ManyToOne"); //NOI18N
        }

        public boolean isManyToMany() {
            return matchAnnotation("@javax.persistence.ManyToMany"); //NOI18N
        }

        private boolean matchAnnotation(String annotation) {
            for (String a : annotations) {
                if (a.startsWith(annotation)) {
                    return true;
                }
            }

            return false;
        }

        public void addFieldInfo(FieldInfo info) {
            if (fieldInfos == null) {
                fieldInfos = new ArrayList<FieldInfo>();
            }

            fieldInfos.add(info);
        }

        public Collection<FieldInfo> getFieldInfos() {
            return fieldInfos;
        }
    }
}
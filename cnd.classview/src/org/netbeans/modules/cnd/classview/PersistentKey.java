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

package org.netbeans.modules.cnd.classview;

import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmCompoundClassifier;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmIdentifiable;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;

/**
 *
 * @author Alexander Simon
 */
public final class PersistentKey {
    private static final byte PROXY = 1;
    private static final byte UID = 2;
    private static final byte NAMESPACE = 4;
    private static final byte DECLARATION = 8;
    private static final byte PROJECT = 16;
    private static final byte STATE = 32;
    
    private Object key;
    private CsmProject project;
    private byte kind;
    
    private PersistentKey(CsmIdentifiable id, boolean state) {
        key = id;
        kind = PROXY;
        if (state) {
            kind |= STATE;
        }
    }
    
    private PersistentKey(CsmUID id, boolean state) {
        key = id;
        kind = UID;
        if (state) {
            kind |= STATE;
        }
    }
    
    private PersistentKey(CharSequence id, CsmProject host, byte type, boolean state) {
        key = id;
        project = host;
        kind = type;
        if (state) {
            kind |= STATE;
        }
    }
    
    public static PersistentKey createGlobalNamespaceKey(CsmProject project){
        return new PersistentKey(CharSequenceKey.empty(), project, NAMESPACE, false); // NOI18N
    }
    
    public static PersistentKey createKey(CsmIdentifiable object){
        if (object instanceof CsmNamespace){
            CsmNamespace ns = (CsmNamespace) object;
            CharSequence uniq = ns.getQualifiedName();
            CsmProject project = ns.getProject();
            if (project != null) {
                return new PersistentKey(uniq, project, NAMESPACE, false);
            }
        } else if (object instanceof CsmEnumerator){
            // special hack.
        } else if (object instanceof CsmOffsetableDeclaration){
            CsmOffsetableDeclaration decl = (CsmOffsetableDeclaration) object;
            CharSequence name = decl.getName();
            CharSequence uniq = decl.getUniqueName();
            CsmScope scope = decl.getScope();
            if ((scope instanceof CsmCompoundClassifier) && name.length() > 0) {
                CsmCompoundClassifier cls = (CsmCompoundClassifier) scope;
                name = cls.getName();
            }
            CsmProject project = decl.getContainingFile().getProject();
            if (name.length() > 0 && uniq.toString().indexOf("::::") < 0 && project != null){ // NOI18N
                return new PersistentKey(uniq, project, DECLARATION, getStateBit(object));
            } else {
                //System.out.println("Skip "+uniq);
            }
        } else if (object instanceof CsmProject){
            return new PersistentKey(null, (CsmProject)object, PROJECT, false);
        }
        return new PersistentKey(object.getUID(), getStateBit(object));
    }
    
    private static boolean getStateBit(CsmIdentifiable object){
        if (object instanceof CsmTypedef){
            CsmTypedef typedef = (CsmTypedef) object;
            if (((CsmTypedef)object).isTypeUnnamed()){
                CsmClassifier cls = typedef.getType().getClassifier();
                if (cls != null && cls.getName().length()==0 &&
                   (cls instanceof CsmCompoundClassifier)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public CsmIdentifiable getObject(){
        int maskKind = kind & 31;
        switch(maskKind){
            case UID:
                return (CsmIdentifiable) ((CsmUID)key).getObject();
            case PROXY:
                return (CsmIdentifiable) key;
            case NAMESPACE:
                return project.findNamespace((CharSequence)key);
            case DECLARATION:
                return project.findDeclaration((CharSequence)key);
            case PROJECT:
                return project;
        }
        return null;
    }
    
    @Override
    public boolean equals(Object object) {
        if (object instanceof PersistentKey){
            PersistentKey what = (PersistentKey) object;
            if (kind != what.kind) {
                return false;
            }
            int maskKind = kind & 31;
            switch(maskKind){
                case PROXY:
                case UID:
                    return key.equals(what.key);
                case NAMESPACE:
                case DECLARATION:
                    if (project.equals(what.project)) {
                        return CharSequenceKey.Comparator.compare((CharSequence)key,(CharSequence)what.key)==0;
                    }
                    return false;
                case PROJECT:
                    return project.equals(what.project);
            }
        }
        return super.equals(object);
    }
    
    @Override
    public int hashCode() {
        int maskKind = kind & 31;
        int res = 0;
        if ((kind & 32) == 32) {
            res = 17;
        }
        switch(maskKind){
            case PROXY:
            case UID:
                return key.hashCode() + res;
            case NAMESPACE:
            case DECLARATION:
                return project.hashCode() ^ key.hashCode() + res;
            case PROJECT:
                return project.hashCode() +  res;
        }
        return 0;
    }
    
    @Override
    public String toString() {
        int maskKind = kind & 31;
        switch(maskKind){
            case PROXY:
                return "Proxy "+key.toString(); // NOI18N
            case UID:
                return "UID "+key.toString(); // NOI18N
            case NAMESPACE:
                return "Namespace "+key; // NOI18N
            case DECLARATION:
                return "Declaration "+key; // NOI18N
            case PROJECT:
                return "Project "+project.getName(); // NOI18N
        }
        return super.toString();
    }
}

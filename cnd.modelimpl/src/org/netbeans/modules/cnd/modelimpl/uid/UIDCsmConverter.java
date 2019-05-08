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
package org.netbeans.modules.cnd.modelimpl.uid;

import org.netbeans.modules.cnd.modelimpl.csm.core.CsmIdentifiable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

/**
 * utilities to convert between CsmUID and CsmObjects
 */
public final class UIDCsmConverter {

    private UIDCsmConverter() {
    }

    ////////////////////////////////////////////////////////////////////////////
    // UID -> Object
//    private static int lastHash = 0;
    public static boolean isIdentifiable(Object obj) {
        if (obj instanceof CsmIdentifiable) {
            return !(obj instanceof CsmParameter);
        }
        return false;
    }

    public static CsmFile UIDtoFile(CsmUID<CsmFile> uid) {
        try {
            CsmFile result = uid == null ? null : uid.getObject();
//	if( result != null ) {
//	    if( uid.toString().indexOf("unresolved") == -1 ) {
//		if( lastHash != 0 && result.hashCode() != lastHash ) {
//		    System.err.printf("SHIT: got another file for %s\n", uid.toString());
//		    CsmFile res2 = uid.getObject();
//		    CsmFile res3 = uid.getObject();
//		    CsmFile res4 = uid.getObject();
//		    CsmFile res5 = uid.getObject();
//		}
//		lastHash = result.hashCode();
//	    }
//	}
            return result;
        } catch (StackOverflowError ex) {
            // needed to analyze IZ99230; it's fixed!
            Exception ex2 = new Exception("StackOverflowError for UID " + uid); // NOI18N
            ex2.setStackTrace(ex.getStackTrace());
            DiagnosticExceptoins.register(ex2);
            return null;
        }
    }

    public static <T> T UIDtoCsmObject(CsmUID<T> uid) {
        return uid == null ? null : uid.getObject();
    }

    public static CsmNamespace UIDtoNamespace(CsmUID<CsmNamespace> uid) {
        return uid == null ? null : uid.getObject();
    }

    public static CsmProject UIDtoProject(CsmUID<CsmProject> uid) {
        return uid == null ? null : uid.getObject();
    }

    public static <T extends CsmDeclaration> T UIDtoDeclaration(CsmUID<T> uid) {
        return uid == null ? null : uid.getObject();
    }

    public static CsmScope UIDtoScope(CsmUID<CsmScope> uid) {
        return uid == null ? null : uid.getObject();
    }

    public static CsmClass UIDtoClass(CsmUID<CsmClass> uid) {
        return uid == null ? null : uid.getObject();
    }

    public static <T> Collection<T> UIDsToNamespaces(Collection<CsmUID<T>> uids) {
        Collection<T> out = UIDsToList(uids, false);
        return out;
    }

    public static <T extends CsmDeclaration> Collection<T> UIDsToDeclarations(Collection<CsmUID<T>> uids) {
        Collection<T> out = UIDsToList(uids, false);
        return out;
    }

    public static CsmInheritance UIDsToInheritance(CsmUID<CsmInheritance> uid) {
        return uid == null ? null : uid.getObject();
    }

    public static <T extends CsmInheritance> Collection<T> UIDsToInheritances(Collection<CsmUID<T>> uids) {
        Collection<T> out = UIDsToList(uids, false);
        return out;
    }

    public static CsmUID<CsmInheritance> inheritanceToUID(CsmInheritance decl) {
        return decl == null ? null : UIDs.get(decl);
    }

    public static <T extends CsmDeclaration> Iterator<T> UIDsToDeclarationsFiltered(Collection<CsmUID<T>> uids, CsmFilter filter) {
        if (uids.isEmpty()) {
            return Collections.<T>emptyList().iterator();
        }
        return new LazyCsmCollection<>(new ArrayList<>(uids), true).iterator(filter);
    }

    public static <T> Collection<T> UIDsToDeclarationsUnsafe(Collection<CsmUID<T>> uids) {
        Collection<T> out = UIDsToList(uids, true);
        return out;
    }

    public static <T> Collection<T> UIDsToMacros(Collection<CsmUID<T>> uids) {
        Collection<T> out = UIDsToList(uids, false);
        return out;
    }

    public static <T> Iterator<T> UIDsToMacros(Collection<CsmUID<T>> uids, CsmFilter filter) {
        if (uids.isEmpty()) {
            return Collections.<T>emptyList().iterator();
        }        
        return new LazyCsmCollection<>(new ArrayList<>(uids), true).iterator(filter);
    }

    public static <T> Collection<T> UIDsToIncludes(Collection<CsmUID<T>> uids) {
        Collection<T> out = UIDsToList(uids, false);
        return out;
    }

    public static <T> Iterator<T> UIDsToIncludes(Collection<CsmUID<T>> uids, CsmFilter filter) {
        if (uids.isEmpty()) {
            return Collections.<T>emptyList().iterator();
        }
        return new LazyCsmCollection<>(new ArrayList<>(uids), true).iterator(filter);
    }

    public static <T> Collection<T> UIDsToInstantiations(Collection<CsmUID<T>> uids) {
        Collection<T> out = UIDsToList(uids, false);
        return out;
    }

    public static <T> Iterator<T> UIDsToInstantiations(Collection<CsmUID<T>> uids, CsmFilter filter) {
        if (uids.isEmpty()) {
            return Collections.<T>emptyList().iterator();
        }
        return new LazyCsmCollection<>(new ArrayList<>(uids), true).iterator(filter);
    }
    
    public static <T> Collection<T> UIDsToCsmObjects(Collection<CsmUID<T>> uids) {
        Collection<T> out = UIDsToList(uids, false);
        return out;
    }

    private static <T> Collection<T> UIDsToList(Collection<CsmUID<T>> uids, boolean allowNullsAndSkip) {
        if (uids.isEmpty()) {
            return Collections.emptyList();
        }
        allowNullsAndSkip |= TraceFlags.SAFE_UID_ACCESS;
        return new LazyCsmCollection<>(new ArrayList<>(uids), allowNullsAndSkip);
    }

    public static <T> Iterator<T> UIDsToDeclarations(Collection<CsmUID<T>> uids, CsmFilter filter) {
        if (uids.isEmpty()) {
            return Collections.<T>emptyList().iterator();
        }
        return new LazyCsmCollection<>(uids, true).iterator(filter);
    }

    public static <T> T UIDtoIdentifiable(CsmUID<T> uid) {
        return uid == null ? null : uid.getObject();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Object -> UID
    public static CsmUID<CsmFile> fileToUID(CsmFile file) {
        return (file == null) ? null : UIDs.get(file);
    }

    public static CsmUID<CsmNamespace> namespaceToUID(CsmNamespace ns) {
        return ns == null ? null : UIDs.get(ns);
    }

    public static CsmUID<CsmProject> projectToUID(CsmProject project) {
        return project == null ? null : UIDs.get(project);
    }

    public static <T extends CsmDeclaration> CsmUID<T> declarationToUID(T decl) {
        return decl == null ? null : UIDs.get(decl);
    }

    public static CsmUID<CsmScope> scopeToUID(CsmScope scope) {
        return scope == null ? null : UIDs.get(scope);
    }

    public static <T> CsmUID<T> identifiableToUID(CsmIdentifiable obj) {
        if (obj == null) {
            return null;
        } else {
            // we need to cast from ? to the exact type
            @SuppressWarnings("unchecked") // checked
            CsmUID<T> res = (CsmUID<T>) obj.getUID();
            return res;
        }
    }

    public static <T extends CsmObject> CsmUID<T> objectToUID(T obj) {
        if (obj == null) {
            return null;
        } else {
            return UIDs.get(obj);
        }
    }

    public static <T extends CsmObject> Collection<CsmUID<T>> objectsToUIDs(Collection<T> objs) {
        if (objs == null) {
            return null;
        }
        if (objs.isEmpty()) {
            return new ArrayList<>(0);
        }
        Collection<CsmUID<T>> out = new ArrayList<>(objs.size());
        for (T csmObj : objs) {
            CsmUID<T> uid = UIDs.get(csmObj);
            out.add(uid);
        }
        return out;
    }
}

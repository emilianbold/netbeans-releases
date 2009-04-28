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

package org.netbeans.modules.cnd.api.model.services;

import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.openide.util.Lookup;

/**
 *
 * @author Alexander Simon
 */
public abstract class CsmClassifierResolver {
    private static CsmClassifierResolver DEFAULT = new Default();

    public abstract CsmClassifier getOriginalClassifier(CsmClassifier orig, CsmFile contextFile);
    public abstract CsmClassifier getTypeClassifier(CsmType type, CsmFile contextFile, int contextOffset, boolean resolveTypeChain);

    /**
     * trying to find classifier with full qualified name used in file
     * @param qualifiedName full qualified name of classifier
     * @param csmFile file where classifier is used by name
     * @param classesOnly true if need to check classes and not typedefs/enums
     * @return best (prefer visible) classifier
     */
    public CsmClassifier findClassifierUsedInFile(CharSequence qualifiedName, CsmFile csmFile, boolean classesOnly) {
        if (csmFile != null) {
            CsmProject project = csmFile.getProject();
            if (project != null) {
                return project.findClassifier(qualifiedName);
            }
        }
        return null;
    }

    public abstract boolean isForwardClass(CsmObject cls);

    protected CsmClassifierResolver() {
    }

    /**
     * Static method to obtain the CsmClassifierResolver implementation.
     * @return the selector
     */
    public static synchronized CsmClassifierResolver getDefault() {
        return DEFAULT;
    }

    /**
     * Implementation of the default resolver
     */
    private static final class Default extends CsmClassifierResolver {
        private final Lookup.Result<CsmClassifierResolver> res;
        private static final boolean FIX_SERVICE = true;
        private CsmClassifierResolver fixedResolver;
        Default() {
            res = Lookup.getDefault().lookupResult(CsmClassifierResolver.class);
        }

        private CsmClassifierResolver getService(){
            CsmClassifierResolver service = fixedResolver;
            if (service == null) {
                for (CsmClassifierResolver selector : res.allInstances()) {
                    service = selector;
                    break;
                }
                if (FIX_SERVICE && service != null) {
                    // I see that it is ugly solution, but NB core cannot fix performance of FolderInstance.waitFinished()
                    fixedResolver = service;
                }
            }
            return service;
        }

        @Override
        public CsmClassifier getOriginalClassifier(CsmClassifier orig, CsmFile contextFile) {
            CsmClassifierResolver service = getService();
            if (service != null) {
                return service.getOriginalClassifier(orig, contextFile);
            }
            return orig;
        }

        public CsmClassifier getTypeClassifier(CsmType type, CsmFile contextFile, int contextOffset, boolean resolveTypeChain) {
            CsmClassifierResolver service = getService();
            if (service != null) {
                return service.getTypeClassifier(type, contextFile, contextOffset, resolveTypeChain);
            }
            CsmClassifier classifier = type.getClassifier();
            if (resolveTypeChain) {
                classifier = getOriginalClassifier(classifier, contextFile);
            }
            return classifier;
        }

        @Override
        public CsmClassifier findClassifierUsedInFile(CharSequence qualifiedName, CsmFile  csmFile, boolean classesOnly) {
            CsmClassifierResolver service = getService();
            if (service != null) {
                return service.findClassifierUsedInFile(qualifiedName, csmFile, classesOnly);
            }
            return super.findClassifierUsedInFile(qualifiedName, csmFile, classesOnly);
        }

        @Override
        public boolean isForwardClass(CsmObject cls) {
            CsmClassifierResolver service = getService();
            if (service != null) {
                return service.isForwardClass(cls);
            }
            return false;
        }

    }
}

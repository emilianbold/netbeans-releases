/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.spi.model;

import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.openide.util.Lookup;

/**
 *
 * @author Vladimir Voskresensky
 */
public abstract class CsmBaseUtilitiesProvider {
    private static CsmBaseUtilitiesProvider DEFAULT = new Default();

    public static CsmBaseUtilitiesProvider getDefault() {
        return DEFAULT;
    }
    
    public abstract boolean isGlobalVariable(CsmVariable var);

    public abstract CsmFunction getFunctionDeclaration(CsmFunction fun);
    
    public abstract CsmNamespace getFunctionNamespace(CsmFunction fun);

    public abstract CsmNamespace getClassNamespace(CsmClassifier cls);
    
    public abstract CsmClass getFunctionClass(CsmFunction fun);
    
    public abstract boolean isUnresolved(Object obj);
    
    /**
     * Implementation of the compound provider
     */
    private static final class Default extends CsmBaseUtilitiesProvider {
        private final Collection<? extends CsmBaseUtilitiesProvider> svcs;;
        Default() {
            svcs = Lookup.getDefault().lookupAll(CsmBaseUtilitiesProvider.class);
        }
        
        @Override
        public boolean isGlobalVariable(CsmVariable var) {
            for (CsmBaseUtilitiesProvider provider : svcs) {
                return provider.isGlobalVariable(var);
            }
            return true;
        }

        @Override
        public CsmFunction getFunctionDeclaration(CsmFunction fun) {
            for (CsmBaseUtilitiesProvider provider : svcs) {
                CsmFunction out = provider.getFunctionDeclaration(fun);
                if (out != null) {
                    return out;
                }
            }
            return null;
        }

        @Override
        public CsmNamespace getFunctionNamespace(CsmFunction fun) {
            for (CsmBaseUtilitiesProvider provider : svcs) {
                CsmNamespace out = provider.getFunctionNamespace(fun);
                if (out != null) {
                    return out;
                }
            }
            return null;
        }

        @Override
        public CsmNamespace getClassNamespace(CsmClassifier cls) {
            for (CsmBaseUtilitiesProvider provider : svcs) {
                CsmNamespace out = provider.getClassNamespace(cls);
                if (out != null) {
                    return out;
                }
            }
            return null;
        }

        @Override
        public CsmClass getFunctionClass(CsmFunction fun) {
            for (CsmBaseUtilitiesProvider provider : svcs) {
                CsmClass out = provider.getFunctionClass(fun);
                if (out != null) {
                    return out;
                }
            }
            return null;
        }

        @Override
        public boolean isUnresolved(Object obj) {
            for (CsmBaseUtilitiesProvider provider : svcs) {
                return provider.isUnresolved(obj);
            }
            return false;
        }
    }
}

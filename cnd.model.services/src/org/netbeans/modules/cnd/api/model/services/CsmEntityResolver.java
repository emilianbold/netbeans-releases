/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.api.model.services;

import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.spi.model.services.CsmEntityResolverImplementation;
import org.openide.util.Lookup;

/**
 *
 * @author Petr Kudryavtsev <petrk@netbeans.org>
 */
public final class CsmEntityResolver {
    
    /**
     * Resolves entity by declaration text or qualified name within a project
     * 
     * @param project
     * @param declText
     * 
     * @return all entities which has the same declaration text
     */    
    public static Collection<CsmObject> resolveEntity(NativeProject project, CharSequence declText) {
        return DEFAULT.resolveEntity(project, declText);
    }    
    
    /**
     * Resolves entity by declaration text or qualified name within a project
     * @param project
     * @param declText
     * @return all functions which has the same signature
     */
    public static Collection<CsmObject> resolveFunction(CsmProject project, CharSequence declText) {
        return DEFAULT.resolveEntity(project, declText);
    }        
        
//<editor-fold defaultstate="collapsed" desc="impl">
    
    private static final CsmEntityResolverImplementation DEFAULT = new Default();
    
    private CsmEntityResolver() {
        throw new AssertionError("Not instantiable"); // NOI18N
    }        
    
    /**
     * Default implementation (just a proxy to a real service)
     */
    private static final class Default implements CsmEntityResolverImplementation {
        
        private final Lookup.Result<CsmEntityResolverImplementation> res;
        
        private CsmEntityResolverImplementation delegate;
        
        
        private Default() {
            res = Lookup.getDefault().lookupResult(CsmEntityResolverImplementation.class);
        }
        
        private CsmEntityResolverImplementation getDelegate(){
            CsmEntityResolverImplementation service = delegate;
            if (service == null) {
                for (CsmEntityResolverImplementation resolver : res.allInstances()) {
                    service = resolver;
                    break;
                }
                delegate = service;
            }
            return service;
        }
        
        @Override
        public Collection<CsmObject> resolveEntity(NativeProject project, CharSequence declText) {
            return getDelegate().resolveEntity(project, declText);
        }    

        @Override
        public Collection<CsmObject> resolveEntity(CsmProject project, CharSequence declText) {
            return getDelegate().resolveEntity(project, declText);
        }            
    }
//</editor-fold>        
}

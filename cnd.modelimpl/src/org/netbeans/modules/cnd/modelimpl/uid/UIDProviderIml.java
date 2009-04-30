/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.uid;

import org.netbeans.modules.cnd.modelimpl.csm.core.CsmIdentifiable;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.spi.model.UIDProvider;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 *
 * @author Vladimir Voskresensky
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.cnd.spi.model.UIDProvider.class)
public final class UIDProviderIml implements UIDProvider {

    private static final Set<Class> nonIdentifiable = new HashSet<Class>();
    private static final boolean debugMode = CndUtils.isDebugMode();

    public static boolean isSelfUID(CsmUID<?> uid) {
        return uid instanceof SelfUID<?>;
    }

    public UIDProviderIml() {
        // public constructor for service initialization
    }

    public <T> CsmUID<T> get(T obj) {
        return get(obj, true);
    }

    public static <T> CsmUID<T> get(T obj, boolean checkNull) {
        CsmUID<T> out;
        if (UIDCsmConverter.isIdentifiable(obj)) {
            final CsmIdentifiable ident = (CsmIdentifiable) obj;
            // we need to cast to the exact type
            @SuppressWarnings("unchecked") // checked
            CsmUID<T> uid = (CsmUID<T>) ident.getUID();
            if (debugMode && !((obj instanceof CsmNamespace) || (obj instanceof CsmProject))) {
                Object object = uid.getObject();
                if (object == null) {
                    // this could happen in clients delayed threads
                    if (checkNull) {
                        String prefix = "no deref object for uid["; // NOI18N
                        if (Thread.currentThread().getName().contains("FileTaskFactory")) { // NOI18N
                            prefix = "it's OK to have invalidated object with uid["; // NOI18N
                        }
                        new Exception(prefix + uid + "] of " + obj).printStackTrace(System.err); // NOI18N
                    }
                } else {
                    // commented because method isAssignableFrom() is too expensive
                    // find alternative method for assertion
                    //for example: under special trace flag
                    if (false) {
                        final Class<? extends Object> derefClass = object.getClass();
                        if (!derefClass.isAssignableFrom(obj.getClass())) {
                            System.err.println("deref class " + derefClass + " is not super class of " + obj.getClass()); // NOI18N
                        }
                    }
                }
            }
            out = uid;
        } else {
            if (debugMode && nonIdentifiable.add(obj.getClass())) {
                final Class<? extends Object> aClass = obj.getClass();
                if (!aClass.equals(org.netbeans.modules.cnd.modelimpl.csm.deep.LabelImpl.class)) {
                    System.err.println("Not implementing CsmIdentifiable: " + obj.getClass()); // NOI18N
                }
            }
            out = createSelfUID(obj);
        }
        return out;
    }

    public static <T> CsmUID<T> createSelfUID(T obj) {
        return new SelfUID<T>(obj);
    }

    private static final class SelfUID<T> implements CsmUID<T> {

        private final T element;

        SelfUID(T element) {
            assert element != null : "impossible to wrap null object";
            this.element = element;
        }

        public T getObject() {
            return this.element;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SelfUID other = (SelfUID) obj;
            if (this.element != other.element && !this.element.equals(other.element)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 89 * hash + this.element.hashCode();
            return hash;
        }

        @Override
        public String toString() {
            return "SUID: " + this.element; // NOI18N
        }
    }
}

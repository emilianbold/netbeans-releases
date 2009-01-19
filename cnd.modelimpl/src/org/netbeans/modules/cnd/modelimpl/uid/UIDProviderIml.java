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
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.spi.model.UIDProvider;

/**
 *
 * @author Vladimir Voskresensky
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.cnd.spi.model.UIDProvider.class)
public final class UIDProviderIml implements UIDProvider {

    private static final Set<Class> nonIdentifiable = new HashSet<Class>();
    private static final Logger LOG = Logger.getLogger(UIDs.class.getName());
    private static final boolean debugMode;

    static {
        boolean debug = false;
        assert debug = true;
        debugMode = debug;
    }
    
    public <T> CsmUID<T> get(T obj) {
        CsmUID<T> out;
        if (UIDCsmConverter.isIdentifiable(obj)) {
            final CsmIdentifiable ident = (CsmIdentifiable) obj;
            // we need to cast to the exact type
            @SuppressWarnings("unchecked") // checked
            CsmUID<T> uid = (CsmUID<T>) ident.getUID();
            if (debugMode) {
                Object object = uid.getObject();
                if (object == null) {
                    // sometimes it is ok that we are unable to get the object
                    //LOG.severe("no deref object for uid[" + uid + "] of " + obj); // NOI18N
                } else {
                    final Class<? extends Object> derefClass = object.getClass();
                    if (!derefClass.isAssignableFrom(obj.getClass())) {
                        LOG.severe("deref class " + derefClass + " is not super class of " + obj.getClass()); // NOI18N
                    }
                }
            }
            out = uid;
        } else {
            if (debugMode && nonIdentifiable.add(obj.getClass())) {
                LOG.severe("Not implementing CsmIdentifiable: " + obj.getClass()); // NOI18N
            }
            out = createSelfUID(obj);
        }
        return out;
    }

    private <T> CsmUID<T> createSelfUID(T obj) {
        return new SelfUID<T>(obj);
    }

    private static final class SelfUID<T> implements CsmUID<T> {

        private final T element;

        SelfUID(T element) {
            this.element = element;
        }

        public T getObject() {
            return this.element;
        }
    }
}

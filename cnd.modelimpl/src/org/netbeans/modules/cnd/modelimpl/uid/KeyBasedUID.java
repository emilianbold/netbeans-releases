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

import java.io.IOException;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.repository.KeyHolder;
import org.netbeans.modules.cnd.modelimpl.repository.KeyObjectFactory;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 * help class for CsmUID based on repository Key
 */
public abstract class KeyBasedUID<T> implements CsmUID<T>, KeyHolder, SelfPersistent, Comparable<CsmUID<T>> {

    private final Key key;

    protected KeyBasedUID(Key key) {
        assert key != null;
        this.key = key;
    }

    @Override
    public T getObject() {
        return RepositoryUtils.get(this);
    }

    @Override
    public Key getKey() {
        return key;
    }

    public abstract void dispose(T obj);

    @Override
    public String toString() {
        String retValue;

        retValue = key.toString();
        return "KeyBasedUID on " + retValue; // NOI18N
    }

    @Override
    public int hashCode() {
        int retValue;

        retValue = key.hashCode();
        return retValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        KeyBasedUID<?> other = (KeyBasedUID<?>) obj;
        return this.key.equals(other.key);
    }

    @Override
    public void write(RepositoryDataOutput aStream) throws IOException {
        KeyObjectFactory.getDefaultFactory().writeKey(key, aStream);
    }

    /* package */ KeyBasedUID(RepositoryDataInput aStream) throws IOException {
        key = KeyObjectFactory.getDefaultFactory().readKey(aStream);
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compareTo(CsmUID<T> o) {
        assert o != null;
        assert o instanceof KeyBasedUID;
        Comparable o1 = (Comparable) this.key;
        Comparable o2 = (Comparable) ((KeyBasedUID) o).key;
        return o1.compareTo(o2);
    }
}    

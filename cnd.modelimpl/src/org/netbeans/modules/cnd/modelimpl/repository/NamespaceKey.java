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
package org.netbeans.modules.cnd.modelimpl.repository;

import java.io.IOException;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmObjectFactory;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.repository.spi.KeyDataPresentation;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/*package*/ class NamespaceKey extends ProjectContainerKey {

    private final CharSequence fqn;
    private final int hashCode; // cashed hash code

    NamespaceKey(CsmNamespace ns) {
        super(((ProjectBase) ns.getProject()).getUnitId());
        this.fqn = ns.getQualifiedName();
        hashCode = _hashCode();
    }

    NamespaceKey(KeyDataPresentation presentation) {
        super(presentation);
        fqn = presentation.getNamePresentation();
        hashCode = _hashCode();
    }

    @Override
    public String toString() {
        return "NSKey " + fqn + " of project " + getProjectName(); // NOI18N
    }

    @Override
    public PersistentFactory getPersistentFactory() {
        return CsmObjectFactory.instance();
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    private int _hashCode() {
        int key = super.hashCode();
        key = 37*KeyObjectFactory.KEY_NAMESPACE_KEY +key;
        key = 17 * key + fqn.hashCode();
        return key;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        NamespaceKey other = (NamespaceKey) obj;
        return this.fqn.equals(other.fqn);
    }

    @Override
    public void write(RepositoryDataOutput aStream) throws IOException {
        super.write(aStream);
        assert fqn != null;
        PersistentUtils.writeUTF(fqn, aStream);
    }

    /*package*/ NamespaceKey(RepositoryDataInput aStream) throws IOException {
        super(aStream);
        fqn = PersistentUtils.readUTF(aStream, QualifiedNameCache.getManager());
        assert fqn != null;
        hashCode = _hashCode();
    }

    @Override
    public int getDepth() {
        assert super.getDepth() == 0;
        return 1;
    }

    @Override
    public CharSequence getAt(int level) {
        assert super.getDepth() == 0 && level < getDepth();
        return this.fqn;
    }

    @Override
    public int getSecondaryDepth() {
        return 1;
    }

    @Override
    public int getSecondaryAt(int level) {
        assert level == 0;
        return KeyObjectFactory.KEY_NAMESPACE_KEY;
    }

    @Override
    public boolean hasCache() {
        return true;
    }

    @Override
    public short getKindPresentation() {
        return KeyObjectFactory.KEY_NAMESPACE_KEY;
    }

    @Override
    public final CharSequence getNamePresentation() {
        return fqn;
    }
}

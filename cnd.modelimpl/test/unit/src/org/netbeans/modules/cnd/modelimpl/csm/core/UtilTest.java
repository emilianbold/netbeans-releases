/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.modelimpl.repository.KeyPresentationFactoryImpl;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities;
import org.netbeans.modules.cnd.modelimpl.test.ModelBasedTestCase;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.KeyDataPresentation;
import org.openide.util.CharSequences;

/**
 *
 * @author Alexander Simon
 */
public class UtilTest extends ModelBasedTestCase {

    public UtilTest(String testName) {
        super(testName);
    }

    @Test
    public void testConsistency() throws Exception {
        Set<String> set = new HashSet<String>();
        for(CsmDeclaration.Kind kind : CsmDeclaration.Kind.values()) {
            String csmDeclarationKindkey = Utils.getCsmDeclarationKindkey(kind);
            if (set.contains(csmDeclarationKindkey)) {
                assert false : "Duplicated key "+csmDeclarationKindkey+" for "+kind;
            }
            set.add(csmDeclarationKindkey);
            char charAt = csmDeclarationKindkey.charAt(0);
            assert Utils.getCsmDeclarationKind(charAt) == kind : "Undefined kind for char"+csmDeclarationKindkey.charAt(0);
            Key key = presentationFactory((short)charAt);
            assert key != null;
            assert KeyUtilities.getKeyChar(key) == charAt;
        }
        for(final CsmVisibility kind : CsmVisibility.values()) {
            String csmInheritanceKindKey = Utils.getCsmInheritanceKindKey(new CsmInheritance() {

                @Override
                public CsmClassifier getClassifier() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public CsmVisibility getVisibility() {
                    return kind;
                }

                @Override
                public boolean isVirtual() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public CsmType getAncestorType() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public CsmFile getContainingFile() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public int getStartOffset() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public int getEndOffset() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public Position getStartPosition() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public Position getEndPosition() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public CharSequence getText() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public CsmScope getScope() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            });
            if (set.contains(csmInheritanceKindKey)) {
                assert false : "Duplicated key "+csmInheritanceKindKey+" for "+kind;
            }
            set.add(csmInheritanceKindKey);
            char charAt = csmInheritanceKindKey.charAt(0);
            assert Utils.getCsmVisibility(charAt) == kind : "Undefined kind for char"+csmInheritanceKindKey.charAt(0);
            Key key = presentationFactory((short)charAt);
            assert key != null;
            assert KeyUtilities.getKeyChar(key) == charAt;
        }
        String key = Utils.getCsmIncludeKindKey();
        assert !set.contains(key) : "Duplicated key "+key;
        set.add(key);
        Key aKey = presentationFactory((short)key.charAt(0));
        assert aKey != null;
        assert KeyUtilities.getKeyChar(aKey) == key.charAt(0);
        
        key = Utils.getCsmParamListKindKey();
        assert !set.contains(key) : "Duplicated key "+key;
        set.add(key);
        aKey = presentationFactory((short)key.charAt(0));
        assert aKey != null;
        assert KeyUtilities.getKeyChar(aKey) == key.charAt(0);

        key = Utils.getCsmInstantiationKindKey();
        assert !set.contains(key) : "Duplicated key "+key;
        set.add(key);
        aKey = presentationFactory((short)key.charAt(0));
        assert aKey != null;
        assert KeyUtilities.getKeyChar(aKey) == key.charAt(0);
        assertNoExceptions();
    }

    private Key presentationFactory(final short kind) {
        KeyDataPresentation presentation = new KeyDataPresentation() {

            @Override
            public int getUnitPresentation() {
                return 10000+1;
            }

            @Override
            public CharSequence getNamePresentation() {
                return CharSequences.empty();
            }

            @Override
            public short getKindPresentation() {
                return kind;
            }

            @Override
            public int getFilePresentation() {
                return 0;
            }

            @Override
            public int getStartPresentation() {
                return 0;
            }

            @Override
            public int getEndPresentation() {
                return 0;
            }
        };
        KeyPresentationFactoryImpl impl = new KeyPresentationFactoryImpl();
        return impl.create(presentation);
    }
}

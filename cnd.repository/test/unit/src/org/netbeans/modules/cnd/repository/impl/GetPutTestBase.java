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
package org.netbeans.modules.cnd.repository.impl;

import java.io.IOException;
import org.netbeans.modules.cnd.repository.api.CacheLocation;
import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.api.RepositoryAccessor;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.KeyDataPresentation;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.test.CndBaseTestCase;

/**
 * Tests Repository.tryGet()
 * @author Vladimir Kvashin
 */
public abstract class GetPutTestBase extends CndBaseTestCase {

    private static final int TEST_UNIT_ID = RepositoryAccessor.getTranslator().getUnitId("Repository_Test_Unit", CacheLocation.DEFAULT);
    
    protected GetPutTestBase(java.lang.String testName) {
        super(testName);
    }
    
    protected abstract class BaseKey implements Key {


        private final String key;
        private final CharSequence unitName;

        public BaseKey(String key) {
            this.key = key;
            this.unitName = RepositoryAccessor.getTranslator().getUnitName(TEST_UNIT_ID);
        }

        @Override
        public int getSecondaryAt(int level) {
            return 0;
        }

        @Override
        public String getAt(int level) {
            return key;
        }

        @Override
        public CharSequence getUnit() {
            return unitName;
        }

        @Override
        public int getUnitId() {
            return TEST_UNIT_ID;
        }

        @Override
        public int getSecondaryDepth() {
            return 0;
        }

        @Override
        public PersistentFactory getPersistentFactory() {
            return factory;
        }

        @Override
        public int getDepth() {
            return 1;
        }
    }

    protected class SmallKey extends BaseKey {

        public SmallKey(String key) {
            super(key);
        }

        @Override
        public Key.Behavior getBehavior() {
            return Key.Behavior.Default;
        }

        @Override
        public boolean hasCache() {
            return false;
        }

        @Override
        public KeyDataPresentation getDataPresentation() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    protected class LargeKey extends BaseKey {

        public LargeKey(String key) {
            super(key);
        }

        @Override
        public Key.Behavior getBehavior() {
            return Key.Behavior.LargeAndMutable;
        }

        @Override
        public boolean hasCache() {
            return false;
        }

        @Override
        public KeyDataPresentation getDataPresentation() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    protected static class Value implements Persistent {

        private String value;

        public Value(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value + " @" + hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Value) {
                return value.equals(((Value) obj).value);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }

    protected class Factory implements PersistentFactory {

        @Override
        public void write(RepositoryDataOutput out, Persistent obj) throws IOException {
            assert obj instanceof Value;
            out.writeUTF(((Value) obj).value);
            onWriteHook(this, obj);
        }

        @Override
        public Persistent read(RepositoryDataInput in) throws IOException {
            String value = in.readUTF();
            Value out = new Value(value);
            onReadHook(this, out);
            return out;
        }

    }
    protected PersistentFactory factory;
    protected Repository repository;

    @Override
    protected void setUp() throws Exception {
        repository = RepositoryAccessor.getRepository();
        factory = new Factory();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    protected void onReadHook(Factory factory, Persistent obj) {
    }

    protected void onWriteHook(Factory factory, Persistent obj) {

    }
}

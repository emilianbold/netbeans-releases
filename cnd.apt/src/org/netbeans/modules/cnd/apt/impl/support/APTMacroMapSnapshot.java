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

package org.netbeans.modules.cnd.apt.impl.support;

import org.netbeans.modules.cnd.utils.cache.MapSnapshot;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.apt.structure.APTDefine;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTSerializeUtils;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.utils.cache.MapSnapshot.Holder;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.cache.TinyMaps;
import org.openide.util.CharSequences;

/**
 *
 */
public final class APTMacroMapSnapshot extends MapSnapshot<APTMacro> {
    /**
     * optimize by memory.
     * one of:
     * 1)APTMacro when only one macro is defined
     * 2)Map<CharSequence, APTMacro> - map of storage
     * 3)CharSequence for alone UNDEFINED_MACRO with specified name
     * 4) frozen array has Holder with sorted array: [name1, name2, ..., macro1, macro2, ...]
     *    macro names followed by corresponding macro objects. Aray is sorted to be comparable by equals
     */

    public APTMacroMapSnapshot(APTMacroMapSnapshot parent) {
        super(parent);
    }

    @Override
    protected APTMacroMapSnapshot getParent() {
        return (APTMacroMapSnapshot) super.getParent();
    }

    private void prepareMacroMapToAddMacro(CharSequence name, APTMacro macro) {
        assert !(storage instanceof Holder) : "frozen snap can not be modified";
        if (storage == EMPTY) {
            return;
        }
        if (storage instanceof Map<?,?>) {
            @SuppressWarnings("unchecked")
            Map<CharSequence, APTMacro> map = (Map<CharSequence, APTMacro>)storage;
            // expand map if needed based on expected next key
            storage = TinyMaps.expandForNextKey(map, name);
        } else {
            CharSequence key;
            APTMacro value;
            if (storage instanceof APTMacro) {
                value = (APTMacro) storage;
                key = value.getName().getTextID();
            } else {
                assert storage instanceof CharSequence;
                value = UNDEFINED_MACRO;
                key = (CharSequence) storage;
            }
            if (key.equals(name)) {
                // clean to let putMacro do the job
                storage = EMPTY;
            } else {
                // create LW map and remember previous value in map
                storage = TinyMaps.createMap(2);
                @SuppressWarnings("unchecked")
                Map<CharSequence, APTMacro> map = (Map<CharSequence, APTMacro>)storage;
                map.put(key, value);
            }
        }
    }

    /*package*/ final void putMacro(CharSequence name, APTMacro macro) {
        prepareMacroMapToAddMacro(name, macro);
        if (storage == EMPTY) {
            if (macro == UNDEFINED_MACRO) {
                storage = name;
            } else {
                assert macro.getName().getTextID().equals(name);
                storage = macro;
            }
        } else {
            assert storage instanceof Map<?,?> : "unexpected class " + storage.getClass();
            @SuppressWarnings("unchecked")
            Map<CharSequence, APTMacro> map = (Map<CharSequence, APTMacro>)storage;
            map.put(name, macro);
        }
    }

    public final APTMacro getMacro(APTToken token) {
        return getMacro(token.getTextID());
    }

    /*package*/ final APTMacro getMacro(CharSequence key) {
        return get(key);
    }

    @Override
    protected APTMacro getImpl(CharSequence key) {
        if (storage == EMPTY) {
            return null;
        } else if (storage instanceof CharSequence) {
            if (storage.equals(key)) {
                return UNDEFINED_MACRO;
            }
            return null;
        } else if (storage instanceof APTMacro) {
            assert storage != UNDEFINED_MACRO;
            if (((APTMacro)storage).getName().getTextID().equals(key)) {
                return (APTMacro)storage;
            }
            return null;
        } else {
            assert storage instanceof Map<?,?> : "unexpected to have get from frozen" + storage.getClass();
            @SuppressWarnings("unchecked")
            APTMacro map = ((Map<CharSequence, APTMacro>)storage).get(key);
            return map;
        }
    }

    @Override
    public String toString() {
        Map<CharSequence, APTMacro> tmpMap = getAll();
        return APTUtils.macros2String(tmpMap);
    }

    @Override
    protected int size() {
        if (storage == EMPTY) {
            return 0;
        } else if (storage instanceof Map<?, ?>) {
            return ((Map<?,?>)storage).size();
        } else if (storage instanceof Holder) {
            return ((Holder)storage).arr.length / 2;
        } else {
            return 1;
        }
    }

    @Override
    protected boolean isRemoved(APTMacro value) {
        return value == UNDEFINED_MACRO;
    }

    @Override
    protected Holder cacheHolder(Holder holder) {
        return SnapshotHolderCache.getManager().getHolder(holder);
    }

    @Override
    public Iterator<Map.Entry<CharSequence, APTMacro>> iterator() {
        if (storage instanceof CharSequence) {
            return new SingleItemIterator<APTMacro>((CharSequence) storage, UNDEFINED_MACRO);
        } else if (storage instanceof APTMacro) {
            APTMacro value = (APTMacro) storage;
            return new SingleItemIterator<APTMacro>(value.getName().getTextID(), value);
        } else {
            return super.iterator();
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // persistence support

    public void write(RepositoryDataOutput output) throws IOException {
        APTSerializeUtils.writeSnapshot(getParent(), output);
        if (this.storage == EMPTY) {
            output.writeInt(0);
        } else if (this.storage instanceof CharSequence) {
            output.writeInt(-1);
            output.writeCharSequenceUTF((CharSequence)this.storage);
        } else if (this.storage instanceof APTMacro) {
            output.writeInt(-2);
            APTSerializeUtils.writeMacro((APTMacro)this.storage, output);
        } else {
            assert this.storage instanceof Holder : "unexpected object " + this.storage;
            output.writeInt(size());
            writeMacros(((Holder)this.storage).arr, output);
        }
    }

    public static void writeMacros(Object[] storage, RepositoryDataOutput output) throws IOException {
        assert storage != null;
        int collSize = storage.length/2;
        for (int i = 0; i < collSize; i++) {
            CharSequence key = (CharSequence) storage[i];
            assert CharSequences.isCompact(key);
            output.writeCharSequenceUTF(key);
            APTMacro macro = (APTMacro) storage[i+collSize];
            assert macro != null;
            APTSerializeUtils.writeMacro(macro, output);
        }
    }

    public APTMacroMapSnapshot(RepositoryDataInput input) throws IOException {
        super(APTSerializeUtils.readSnapshot(input));
        int collSize = input.readInt();
        if (collSize == -2) {
            this.storage = APTSerializeUtils.readMacro(input);
        } else if (collSize == -1) {
            this.storage = CharSequences.create(input.readCharSequenceUTF());
        } else if (collSize == 0) {
            storage = EMPTY;
        } else {
            Object[] arr = readMacros(collSize, input);
            storage = SnapshotHolderCache.getManager().getHolder(new Holder(arr));
        }
    }

    private static Object[] readMacros(int collSize, RepositoryDataInput input) throws IOException {
        Object[] storage = new Object[collSize*2];
        for (int i = 0; i < storage.length; i++) {
            CharSequence key = CharSequences.create(input.readCharSequenceUTF());
            assert key != null;
            APTMacro macro = APTSerializeUtils.readMacro(input);
            assert macro != null;
            storage[i] = key;
            storage[i+collSize] = APTMacroCache.getManager().getMacro(macro);
        }
        return storage;
    }

    //This is a single instance of a class to indicate that macro is undefined,
    //not a child of APTMacro to track errors more easily
    public static final APTMacro UNDEFINED_MACRO = new UndefinedMacro();

    private static final class UndefinedMacro implements APTMacro {
        @Override
        public String toString() {
            return "Macro undefined"; // NOI18N
        }

        @Override
        public CharSequence getFile() {
            return CharSequences.empty();
        }

        @Override
        public Kind getKind() {
            return Kind.USER_SPECIFIED;
        }

        @Override
        public boolean isFunctionLike() {
            throw new UnsupportedOperationException("Not supported in fake impl"); // NOI18N
        }

        @Override
        public APTToken getName() {
            throw new UnsupportedOperationException("Not supported in fake impl"); // NOI18N
        }

        @Override
        public Collection<APTToken> getParams() {
            throw new UnsupportedOperationException("Not supported in fake impl"); // NOI18N
        }

        @Override
        public TokenStream getBody() {
            throw new UnsupportedOperationException("Not supported in fake impl"); // NOI18N
        }

        @Override
        public APTDefine getDefineNode() {
            throw new UnsupportedOperationException("Not supported in fake impl."); // NOI18N
        }

    }
}

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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.cnd.repository.spi.DatabaseTableDescription;
import org.netbeans.modules.cnd.repository.spi.KeyDataPresentation;

/**
 *
 * @author Alexander Simon
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.repository.spi.DatabaseTableDescription.class)
public class DeclarationContainerProjectStorage implements DatabaseTableDescription {
    public static final String TABLE_NAME = "project_declarations"; //NOI18N
    public static final String TABLE_INDEX = "project_declarations_uinindex"; //NOI18N
    private static final Index uinIndex = new Index() {

            @Override
            public String getIndexName() {
                return TABLE_INDEX;
            }

            @Override
            public Class<?> getIndexClass() {
                return UniqueNameImpl.class;
            }

            @Override
            public Object createSecondaryKey(Object key, Object value) {
                return new UniqueNameImpl(((DataPresentationImpl)value).uin);
            }
        };

    @Override
    public String getTableName() {
        return TABLE_NAME; //NOI18N
    }

    @Override
    public Class<?> getKeyClass() {
        return KeyDataPresentationImpl.class;
    }

    @Override
    public Class<?> getDataClass() {
        return DataPresentationImpl.class;
    }

    @Override
    public Collection<Index> getIndexes() {
        return Collections.<Index>singletonList(uinIndex);
    }


    public static final class KeyDataPresentationImpl implements KeyDataPresentation, Serializable, Comparable<KeyDataPresentationImpl>{
        private final int unit;
        private final String name;
        private final short kind;
        private final int file;
        private final int start;
        private final int end;

        public KeyDataPresentationImpl(int unit, CharSequence name, short kind, int file, int start, int end) {
            this.unit = unit;
            this.name = name.toString();
            this.kind = kind;
            this.file = file;
            this.start = start;
            this.end = end;
        }

        @Override
        public int getUnitPresentation() {
            return unit;
        }

        @Override
        public String getNamePresentation() {
            return name;
        }

        @Override
        public short getHandler() {
            return kind;
        }

        @Override
        public int getFilePresentation() {
            return file;
        }

        @Override
        public int getStartPresentation() {
            return start;
        }

        @Override
        public int getEndPresentation() {
            return end;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof KeyDataPresentationImpl) {
                KeyDataPresentationImpl objImpl = (KeyDataPresentationImpl) obj;
                return unit == objImpl.unit && name.equals(objImpl.name) &&
                        kind == objImpl.kind && file == objImpl.file &&
                        start == objImpl.start && end == objImpl.end;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + this.unit;
            hash = 37 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 37 * hash + this.kind;
            hash = 37 * hash + this.file;
            hash = 37 * hash + this.start;
            hash = 37 * hash + this.end;
            return hash;
        }

        @Override
        public int compareTo(KeyDataPresentationImpl o) {
            int res = unit - o.unit;
            if (res == 0) {
                res = name.compareTo(o.name);
            }
            if (res == 0) {
                res = kind - o.kind;
            }
            if (res == 0) {
                res = file - o.file;
            }
            if (res == 0) {
                res = start - o.start;
            }
            if (res == 0) {
                res = end - o.end;
            }
            return res;
        }

        @Override
        public String toString() {
            return ""+((char)kind)+" "+name+"["+unit+","+file+","+start+"]"; //NOI18N
        }
    }

    public static final class UniqueNameImpl implements Serializable, Comparable<UniqueNameImpl> {
        private final String uin;

        public UniqueNameImpl(CharSequence uin) {
            this.uin = uin.toString();
        }

        public String getUin() {
            return uin;
        }


        @Override
        public boolean equals(Object obj) {
            if (obj instanceof UniqueNameImpl) {
                return uin.equals(((UniqueNameImpl)obj).uin);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return uin.hashCode();
        }

        @Override
        public int compareTo(UniqueNameImpl o) {
            return uin.compareTo(o.uin);
        }

        @Override
        public String toString() {
            return uin;
        }
    }

    public static final class DataPresentationImpl implements KeyDataPresentation, Serializable {
        private final int unit;
        private final String name;
        private final short kind;
        private final int file;
        private final int start;
        private final int end;
        private final String uin;

        public DataPresentationImpl(KeyDataPresentationImpl key, UniqueNameImpl uin) {
            unit = key.unit;
            name = key.name;
            kind = key.kind;
            file = key.file;
            start = key.start;
            end = key.end;
            this.uin = uin.uin;
        }

        @Override
        public int getUnitPresentation() {
            return unit;
        }

        @Override
        public String getNamePresentation() {
            return name;
        }

        @Override
        public short getHandler() {
            return kind;
        }

        @Override
        public int getFilePresentation() {
            return file;
        }

        @Override
        public int getStartPresentation() {
            return start;
        }

        @Override
        public int getEndPresentation() {
            return end;
        }

        /**
         * @return the uin
         */
        public String getUin() {
            return uin;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final DataPresentationImpl other = (DataPresentationImpl) obj;
            if (this.unit != other.unit) {
                return false;
            }
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            if (this.kind != other.kind) {
                return false;
            }
            if (this.file != other.file) {
                return false;
            }
            if (this.start != other.start) {
                return false;
            }
            if (this.end != other.end) {
                return false;
            }
            if ((this.uin == null) ? (other.uin != null) : !this.uin.equals(other.uin)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 37 * hash + this.unit;
            hash = 37 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 37 * hash + this.kind;
            hash = 37 * hash + this.file;
            hash = 37 * hash + this.start;
            hash = 37 * hash + this.end;
            hash = 37 * hash + (this.uin != null ? this.uin.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return ""+((char)kind)+" "+name+"["+unit+","+file+","+start+"] "+uin; //NOI18N
        }

    }
}

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

/**
 *
 * @author Alexander Simon
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.repository.spi.DatabaseTableDescription.class)
public class PositionStorageImpl implements DatabaseTableDescription {
    public static final String TABLE_NAME = "position"; //NOI18N

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public Class<?> getKeyClass() {
        return FilePositionKey.class;
    }

    @Override
    public Class<?> getDataClass() {
        return PositionDataImpl.class;
    }

    @Override
    public Collection<Index> getIndexes() {
        return Collections.<Index>emptyList();
    }

    public static final class FilePositionKey implements Serializable {
        private final int file;
        private final int position;

        public FilePositionKey(int file, int position) {
            this.file = file;
            this.position = position;
        }

        public int getFileID() {
            return file;
        }

        public int getPositionID() {
            return position;
        }
    }

    public static final class PositionDataImpl implements PositionManager.Position, Serializable {
        private final int offset;
        private final int line;
        private final int column;

        public PositionDataImpl(int offset, int line, int column) {
            this.offset = offset;
            this.line = line;
            this.column = column;
        }

        @Override
        public int getOffset() {
            return offset;
        }

        @Override
        public int getLine() {
            return line;
        }

        @Override
        public int getColumn() {
            return column;
        }
    }
}

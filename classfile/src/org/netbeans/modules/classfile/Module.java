/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.classfile;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The JDK 9 Module attribute.
 * @since 1.47
 * @author Tomas Zezula
 */
public final class Module {

    private final List<RequiresEntry> requires;
    private final List<ExportsEntry> exports;
    private final List<CPClassInfo> uses;
    private final List<ProvidesEntry> provides;

    Module(final DataInputStream in, final ConstantPool cp) throws IOException {
        final int reqCnt = in.readUnsignedShort();
        final RequiresEntry[] req = new RequiresEntry[reqCnt];
        for (int i=0; i<reqCnt; i++) {
            req[i] = new RequiresEntry(in, cp);
        }
        requires = Collections.unmodifiableList(Arrays.asList(req));
        final int expCnt = in.readUnsignedShort();
        final ExportsEntry[] exp = new ExportsEntry[expCnt];
        for (int i=0; i<expCnt; i++) {
            exp[i] = new ExportsEntry(in, cp);
        }
        exports = Collections.unmodifiableList(Arrays.asList(exp));
        final int usesCnt = in.readUnsignedShort();
        final CPClassInfo[] uss = new CPClassInfo[usesCnt];
        for (int i=0; i<usesCnt; i++) {
            uss[i] = (CPClassInfo) cp.get(in.readUnsignedShort());
        }
        uses = Collections.unmodifiableList(Arrays.asList(uss));
        final int provCnt = in.readUnsignedShort();
        final ProvidesEntry[] prov = new ProvidesEntry[provCnt];
        for (int i=0; i< provCnt; i++) {
            prov[i] = new ProvidesEntry(in, cp);
        }
        provides = Collections.unmodifiableList(Arrays.asList(prov));
    }

    /**
     * Returns the required modules.
     * @return the list of {@link RequiresEntry}
     */
    public List<RequiresEntry> getRequiresEntries() {
        return requires;
    }

    /**
     * Returns the exported packages.
     * @return the list of {@link ExportsEntry}
     */
    public List<ExportsEntry> getExportsEntries() {
        return exports;
    }

    /**
     * Returns the used services.
     * @return the list of services used by this module
     */
    public List<CPClassInfo> getUses() {
        return uses;
    }

    /**
     * Returns the provided services.
     * @return the list of {@link ProvidesEntry}
     */
    public List<ProvidesEntry> getProvidesEntries() {
        return provides;
    }

    /**
     * Required module
     */
    public static final class RequiresEntry {

        public static final int ACC_PUBLIC    =   0x20;
        public static final int ACC_SYNTHETIC = 0x1000;
        public static final int ACC_MANDATED  = 0x8000;


        private final CPUTF8Info name;
        private final int flags;

        RequiresEntry(final DataInputStream in, final ConstantPool cp) throws IOException {
            final int index = in.readUnsignedShort();
            name = (CPUTF8Info) cp.get(index);
            flags = in.readUnsignedShort();
        }

        /**
         * Returns the module name.
         * @return the module name
         */
        public CPUTF8Info getModule() {
            return name;
        }

        /**
         * Returns require modifiers.
         * @return flags
         */
        public int getFlags() {
            return flags;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder()
                .append("require: ")            //NOI18N
                .append(name.getName());
            if ((flags & ACC_PUBLIC) != 0) {
                sb.append(" public");           //NOI18N
            }
            if ((flags & ACC_SYNTHETIC) != 0) {
                sb.append(" synthetic");        //NOI18N
            }
            if ((flags & ACC_MANDATED) != 0) {
                sb.append(" mandated");         //NOI18N
            }
            return sb.toString();
        }
    }

    /**
     * Exported package.
     */
    public static final class ExportsEntry {
        private final CPUTF8Info name;
        private final List<CPUTF8Info> to;

        ExportsEntry(final DataInputStream in, final ConstantPool cp) throws IOException {
            final int index = in.readUnsignedShort();
            name = (CPUTF8Info) cp.get(index);
            final int toCnt = in.readUnsignedShort();
            final CPUTF8Info[] t = new CPUTF8Info[toCnt];
            for (int i=0; i< toCnt; i++) {
                t[i] = (CPUTF8Info) cp.get(in.readUnsignedShort());
            }
            to = Collections.unmodifiableList(Arrays.asList(t));
        }

        /**
         * Name of exported package.
         * @return the package name
         */
        public CPUTF8Info getPackage() {
            return name;
        }

        /**
         * Returns a list of modules to which the package is exported.
         * @return module list.
         */
        public List<CPUTF8Info> getExportsTo() {
            return to;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder()
                .append("exports: ")            //NOI18N
                .append(name.getName());
            if (!to.isEmpty()) {
                sb.append(" [");                //NOI18N
                boolean first = true;
                for (CPUTF8Info m : to) {
                    if (!first) {
                        sb.append(", ");        //NOI18N
                    } else {
                        first = false;
                    }
                    sb.append(m.getName());
                }
                sb.append(']');                 //NOI18N
            }
            return sb.toString();
        }


    }

    /**
     * Provided service.
     */
    public static final class ProvidesEntry {
        private final CPClassInfo service;
        private final CPClassInfo impl;

        ProvidesEntry(final DataInputStream in, final ConstantPool cp) throws IOException {
            final int index = in.readUnsignedShort();
            service = (CPClassInfo) cp.get(index);
            //todo:
            final int with_index = in.readUnsignedShort();
            impl = (CPClassInfo) cp.get(with_index);
        }

        /**
         * Service type.
         * @return the service type
         */
        public CPClassInfo getService() {
            return service;
        }

        /**
         * Service implementation.
         * @return the class implementing the service
         */
        public CPClassInfo getImplementation() {
            return impl;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder()
                .append("provides: ")
                .append(service.getClassName())
                .append(" by: ")
                .append(impl.getClassName());
            return sb.toString();
        }
    }
}

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
package org.netbeans.modules.cnd.model.jclank.bridge.impl;

import java.io.PrintWriter;
import org.clank.support.Casts;
import org.clank.support.Converted;
import org.clank.support.Destructors;
import org.clank.support.aliases.char$ptr;
import org.llvm.support.raw_ostream;

/// java_ostream - A raw_ostream that delegate all output to java print writer
//<editor-fold defaultstate="collapsed" desc="llvm::raw_null_ostream">
//</editor-fold>
@Converted(kind = Converted.Kind.AUTO, source = "${LLVM_SRC}/llvm/include/llvm/Support/raw_ostream.h", line = 478, cmd = "jclank.sh -java-imports=${SPUTNIK}/modules/org.llvm.adtsupport/JavaImports ${LLVM_SRC}/llvm/lib/Support/raw_ostream.cpp -filter=llvm::raw_null_ostream")
public final class PrintWriter_ostream extends raw_ostream implements Destructors.ClassWithDestructor {
    private final PrintWriter delegate;

    PrintWriter_ostream(PrintWriter printOut) {
        this.delegate = printOut;
    }

    /// write_impl - See raw_ostream::write_impl.
    //<editor-fold defaultstate="collapsed" desc="llvm::raw_null_ostream::write_impl">
    //</editor-fold>
    /*private*/
    @Converted(kind = Converted.Kind.AUTO, source = "${LLVM_SRC}/llvm/lib/Support/raw_ostream.cpp", line = 768, cmd = "jclank.sh ${LLVM_SRC}/llvm/lib/Support/raw_ostream.cpp -filter=llvm::raw_null_ostream::write_impl")
    @Override
    protected void write_impl( /*const*/ /*char P*/ char$ptr Ptr, int PtrIdx, /*size_t*/ int Size) {
        for (int idx = PtrIdx, Len = 0; Len < Size; Len++, idx++) {
            delegate.write(Casts.$char(Ptr.$at(idx)));
        }
    }

    /// write_impl - See raw_ostream::write_impl.
    //<editor-fold defaultstate="collapsed" desc="llvm::raw_null_ostream::write_impl">
    //</editor-fold>
    /*private*/
    @Converted(kind = Converted.Kind.AUTO, source = "${LLVM_SRC}/llvm/lib/Support/raw_ostream.cpp", line = 768, cmd = "jclank.sh ${LLVM_SRC}/llvm/lib/Support/raw_ostream.cpp -filter=llvm::raw_null_ostream::write_impl")
    @Override
    protected void write_impl(byte[] buf, int PtrIdx, /*size_t*/ int Size) {
        for (int idx = PtrIdx, Len = 0; Len < Size; Len++, idx++) {
            delegate.write(Casts.$char(buf[idx]));
        }
    }

    /// current_pos - Return the current position within the stream, not
    /// counting the bytes currently in the buffer.
    //<editor-fold defaultstate="collapsed" desc="llvm::raw_null_ostream::current_pos">
    //</editor-fold>
    /*private*/
    /*uint64_t*/
    @Converted(kind = Converted.Kind.AUTO, source = "${LLVM_SRC}/llvm/lib/Support/raw_ostream.cpp", line = 771, cmd = "jclank.sh ${LLVM_SRC}/llvm/lib/Support/raw_ostream.cpp -filter=llvm::raw_null_ostream::current_pos")
    @Override
    protected long current_pos() /*const*/ {
        return 0;
    }

    //===----------------------------------------------------------------------===//
    //  java_ostream
    //===----------------------------------------------------------------------===//
    //<editor-fold defaultstate="collapsed" desc="llvm::raw_null_ostream::~raw_null_ostream">
    //</editor-fold>
    @Converted(kind = Converted.Kind.AUTO, source = "${LLVM_SRC}/llvm/lib/Support/raw_ostream.cpp", line = 759, cmd = "jclank.sh ${LLVM_SRC}/llvm/lib/Support/raw_ostream.cpp -filter=llvm::raw_null_ostream::~raw_null_ostream")
    @Override
    public void $destroy() {
        // ~raw_ostream asserts that the buffer is empty. This isn't necessary
        // with java_ostream, but it's better to have java_ostream follow
        // the rules than to change the rules just for java_ostream.
        flush();
        // must be the last
        super.$destroy();
    }
    
}

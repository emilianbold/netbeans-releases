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
package org.netbeans.modules.cnd.modelimpl.parser.clank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileBuffer;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 *
 * @author Vladimir Voskresensky
 */
public class ClankFileInfoQuerySupport {
    private static final boolean TRACE_IN_CONSOLE;
    static {
      if (CndUtils.isUnitTestMode()) {
        TRACE_IN_CONSOLE = true;
      } else {
        TRACE_IN_CONSOLE = false;
      }
    }
    public static List<CsmReference> getMacroUsages(FileImpl fileImpl, Interrupter interrupter) {
        List<CsmReference> out = Collections.<CsmReference>emptyList();
        FileBuffer buffer = fileImpl.getBuffer();
        Collection<PreprocHandler> handlers = fileImpl.getPreprocHandlersForParse(interrupter);
        if (interrupter.cancelled()) {
          return out;
        }
        CndUtils.assertTrueInConsole(TRACE_IN_CONSOLE, "getClankMacroUsages Not yet implemented");
        if (handlers.isEmpty()) {
          DiagnosticExceptoins.register(new IllegalStateException("Empty preprocessor handlers for " + fileImpl.getAbsolutePath())); //NOI18N
          return Collections.<CsmReference>emptyList();
        } else if (handlers.size() == 1) {
          PreprocHandler handler = handlers.iterator().next();
          PreprocHandler.State state = handler.getState();
        } else {
          TreeSet<CsmReference> result = new TreeSet<>(CsmOffsetable.OFFSET_COMPARATOR);
          for (PreprocHandler handler : handlers) {
            // ask for concurrent entry if absent
            PreprocHandler.State state = handler.getState();
          }
          out = new ArrayList<>(result);
        }
        return out;
    }

    public static CsmOffsetable getGuardOffset(FileImpl fileImpl) {
        assert APTTraceFlags.USE_CLANK;
        CndUtils.assertTrueInConsole(TRACE_IN_CONSOLE, "not yet");
        return null;
    }

    public static String expand(FileImpl fileImpl, String code, PreprocHandler handler, ProjectBase base, int offset) {
        assert APTTraceFlags.USE_CLANK;
        CndUtils.assertTrueInConsole(TRACE_IN_CONSOLE, "not yet");
        return code;
    }

}

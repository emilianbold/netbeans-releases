/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.debugger.jpda.backend.truffle;

import com.oracle.truffle.api.debug.Debugger;
import com.oracle.truffle.api.debug.ExecutionEvent;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.vm.PolyglotEngine;

/**
 *
 * @author martin
 */
class JPDATruffleDebugManager {
    
    private final Debugger debugger;
    private final PolyglotEngine tvm;
    private final ExecutionEvent execEvent;

    public JPDATruffleDebugManager(Debugger debugger, PolyglotEngine tvm, ExecutionEvent event) {
        this.debugger = debugger; // DebugEngine.create(dbgClient, language);
        this.tvm = tvm;
        this.execEvent = event;
    }
    
    static JPDATruffleDebugManager setUp(Debugger debugger, PolyglotEngine tvm, ExecutionEvent event) {
        //System.err.println("JPDATruffleDebugManager.setUp()");
        JPDATruffleDebugManager debugManager = new JPDATruffleDebugManager(debugger, tvm, event);
        //System.err.println("SET UP of JPDATruffleDebugManager = "+debugManager+" for "+engine+" and prober to "+jsContext);
        return debugManager;
    }
    
    Debugger getDebugger() {
        return debugger;
    }
    
    PolyglotEngine getPolyglotEngine() {
        return tvm;
    }
    
    static SourcePosition getPosition(Node node) {
        SourceSection sourceSection = node.getSourceSection();
        if (sourceSection == null) {
            sourceSection = node.getEncapsulatingSourceSection();
        }
        if (sourceSection == null) {
            System.err.println("Node without sourceSection! node = "+node+", of class: "+node.getClass());
            throw new IllegalStateException("Node without sourceSection! node = "+node+", of class: "+node.getClass());
        }
        int line = sourceSection.getStartLine();
        Source source = sourceSection.getSource();
        //System.err.println("source of "+node+" = "+source);
        //System.err.println("  name = "+source.getName());
        //System.err.println("  short name = "+source.getShortName());
        //System.err.println("  path = "+source.getPath());
        //System.err.println("  code at line = "+source.getCode(line));
        String name = source.getShortName();
        String path = source.getPath();
        if (path == null) {
            path = name;
        }
        String code = source.getCode();
        return new SourcePosition(source, name, path, line, code);
    }

    void dispose() {
        /*
        endExecution();
        */
    }

    void prepareExecStepInto() {
        //System.err.println("prepareExecStepInto()...");
        execEvent.prepareStepInto();
        //System.err.println("prepareExecStepInto() DONE.");
    }

    void prepareExecContinue() {
        //System.err.println("prepareExecContinue()...");
        execEvent.prepareContinue();
        //System.err.println("prepareExecContinue() DONE.");
    }
    
}

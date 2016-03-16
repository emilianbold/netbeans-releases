/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.backend.truffle;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.SourceSection;

/**
 *
 * @author Martin
 */
final class DebuggerVisualizer {
    
    private DebuggerVisualizer() {}
    
    static String getDisplayName(CallTarget ct) {
        if (ct instanceof RootCallTarget) {
            RootNode rn = ((RootCallTarget) ct).getRootNode();
            return getMethodName(rn);
        } else {
            //System.err.println("Unexpected CallTarget: "+ct.getClass());
            return ct.toString();
        }
    }
    
    static String getMethodName(RootNode rn) {
        SourceSection ss = rn.getSourceSection();
        if (ss == null) {
            ss = rn.getEncapsulatingSourceSection();
        }
        if (ss != null) {
            return ss.getIdentifier();
        } else {
            //System.err.println("No source section for root node "+rn);
            return "";
        }
    }
    
    /** &lt;File name&gt;:&lt;line number&gt; */
    static String getSourceLocation(Node n) {
        SourceSection ss = n.getSourceSection();
        if (ss == null) {
            ss = n.getEncapsulatingSourceSection();
        }
        if (ss == null) {
            //System.err.println("No source section for node "+n);
            return "unknown";
        }
        return ss.getSource().getShortName() + ":" + ss.getStartLine();
    }

}

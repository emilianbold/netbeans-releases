/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.truffle.source;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.debugger.jpda.JPDADebugger;

/**
 * A script source position.
 * 
 * @author Martin Entlicher
 */
public class SourcePosition {
    /*
    private static final Map<JPDADebugger, Map<Long, SourcePosition>> positions = new WeakHashMap<>();

    public static SourcePosition getExisting(JPDADebugger debugger, long id) {
        synchronized (positions) {
            Map<Long, SourcePosition> dp = positions.get(debugger);
            if (dp == null) {
                return null;
            } else {
                return dp.get(id);
            }
        }
    }
    
    private static void register(JPDADebugger debugger, SourcePosition sp) {
        synchronized (positions) {
            Map<Long, SourcePosition> dp = positions.get(debugger);
            if (dp == null) {
                dp = new HashMap<>();
                positions.put(debugger, dp);
            }
            dp.put(sp.id, sp);
        }
    }
    */
    private final long id;
    //private final String name;
    //private final String path;
    private final Source src;
    private final int line;
    //private final String code;
    
    public SourcePosition(JPDADebugger debugger, long id, Source src, int line) {
        this.id = id;
        this.src = src;
        this.line = line;
        //register(debugger, this);
    }

    /*
    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }
    */
    
    public Source getSource() {
        return src;
    }
    
    public int getLine() {
        return line;
    }

    /*
    public String getCode() {
        return code;
    }
    */
}

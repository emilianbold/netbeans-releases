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

package org.netbeans.modules.debugger.jpda.truffle.vars;

import java.util.function.Supplier;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.modules.debugger.jpda.truffle.source.SourcePosition;

/**
 *
 * @author Martin
 */
public class TruffleStackVariable implements TruffleVariable {
    
    private final JPDADebugger debugger;
    private final String name;
    private final String type;
    private final boolean writable;
    private final String valueStr;
    private final Supplier<SourcePosition> valueSourceSupp;
    private final Supplier<SourcePosition> typeSourceSupp;
    private SourcePosition valueSource;
    private SourcePosition typeSource;
    private final ObjectVariable truffleObj;
    private final boolean leaf;
    
    public TruffleStackVariable(JPDADebugger debugger, String name, String type,
                                boolean writable, String valueStr,
                                Supplier<SourcePosition> valueSource,
                                Supplier<SourcePosition> typeSource,
                                ObjectVariable truffleObj) {
        this.debugger = debugger;
        this.name = name;
        this.type = type;
        this.writable = writable;
        this.valueStr = valueStr;
        this.valueSourceSupp = valueSource;
        this.typeSourceSupp = typeSource;
        this.truffleObj = truffleObj;
        this.leaf = TruffleVariableImpl.isLeaf(truffleObj);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
    }
    
    @Override
    public Object getValue() {
        return valueStr;
    }

    @Override
    public synchronized SourcePosition getValueSource() {
        if (valueSource == null) {
            valueSource = valueSourceSupp.get();
        }
        return valueSource;
    }

    @Override
    public synchronized SourcePosition getTypeSource() {
        if (typeSource == null) {
            typeSource = typeSourceSupp.get();
        }
        return typeSource;
    }
    
    @Override
    public boolean isLeaf() {
        return leaf;
    }
    
    @Override
    public Object[] getChildren() {
        return TruffleVariableImpl.getChildren(truffleObj);
    }
}

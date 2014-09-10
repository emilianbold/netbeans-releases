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
package org.netbeans.lib.v8debug.vars;

import java.util.Map;
import org.netbeans.lib.v8debug.PropertyLong;

/**
 *
 * @author Martin Entlicher
 */
public final class V8Function extends V8Object {
    
    private static final String FUNCTION_CLASS_NAME = "Function";       // NOI18N
    
    private final String name;
    private final String inferredName;
    private final String source;
    private final PropertyLong scriptRef;
    private final long scriptId;
    private final PropertyLong position;
    private final PropertyLong line;
    private final PropertyLong column;
    
    public V8Function(long handle, PropertyLong constructorFunctionHandle,
                      PropertyLong protoObjectHandle, PropertyLong prototypeObjectHandle,
                      String name, String inferredName,
                      String source, PropertyLong scriptRef, long scriptId,
                      PropertyLong position, PropertyLong line, PropertyLong column,
                      Map<String, ReferenceAndValue> properties, String text) {
        super(handle, V8Value.Type.Function, FUNCTION_CLASS_NAME,
              constructorFunctionHandle, protoObjectHandle, prototypeObjectHandle,
              properties, text);
        this.name = name;
        this.inferredName = inferredName;
        this.source = source;
        this.scriptRef = scriptRef;
        this.scriptId = scriptId;
        this.position = position;
        this.line = line;
        this.column = column;
    }

    public String getName() {
        return name;
    }

    public String getInferredName() {
        return inferredName;
    }

    public String getSource() {
        return source;
    }

    public PropertyLong getScriptRef() {
        return scriptRef;
    }

    public long getScriptId() {
        return scriptId;
    }

    public PropertyLong getPosition() {
        return position;
    }

    public PropertyLong getLine() {
        return line;
    }

    public PropertyLong getColumn() {
        return column;
    }
    
}

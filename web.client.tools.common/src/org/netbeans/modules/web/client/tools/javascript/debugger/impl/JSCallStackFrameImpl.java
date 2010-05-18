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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.client.tools.javascript.debugger.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSCallStackFrame;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebugger;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSProperty;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSURILocation;
import org.netbeans.modules.web.client.tools.api.JSLocation;

/**
 *
 * @author Sandip V. Chitale <sandipchitale@netbeans.org>
 */
public class JSCallStackFrameImpl extends JSDebuggerBaseImpl implements JSCallStackFrame {
    private int  depth;
    private TYPE type;

    private JSLocation location;
    private String functionName;

    JSCallStackFrameImpl(JSDebugger debugger, int depth, TYPE type, String where, URI uri, int lineNumber) {
        this(debugger, depth, type, where, uri, lineNumber, -1);
    }

    JSCallStackFrameImpl(JSDebugger debugger, int depth, TYPE type, String functionName, URI uri, int lineNumber, int columneNumber) {
        super(debugger);
        this.depth = depth;
        this.type = type;
        this.location = new JSURILocation(uri, lineNumber, columneNumber);
        this.functionName = functionName;
    }

    JSCallStackFrameImpl(JSDebugger debugger, int depth, TYPE type, String functionName, String uri, int lineNumber, int columneNumber) {
        super(debugger);
        this.depth = depth;
        this.type = type;
        this.location = new JSURILocation(uri, lineNumber, columneNumber);
        this.functionName = functionName;
    }

    public JSProperty getScope() throws IllegalStateException {
        return getJSDebugger().getScope(this);
    }

    public JSProperty getThis() throws IllegalStateException {
        return getJSDebugger().getThis(this);
    }

    public URI getURI() {
        return location.getURI();
    }

    public int getLineNumber() {
        return location.getLineNumber();
    }

    public int getColumnNumber() {
        return -1;
    }

    private Map<String, Object> evaledExpressions = new HashMap<String, Object>();
    
    public JSProperty eval(String expression) throws IllegalStateException {
        Object property = evaledExpressions.get(expression);        
        if (property == null) {
            property = getJSDebugger().eval(this, expression);
            if (property == null) {
                evaledExpressions.put(expression, Void.TYPE);
            } else {
                evaledExpressions.put(expression, property);
            }
        }
        if (property instanceof JSProperty) {
            return (JSProperty) property;
        }
        return null;
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getDisplayName() {
        return functionName;
    }

    public JSLocation getLocation() {
        return location;
    }

    public TYPE getType() {
        return type;
    }

	public int getDepth() {
		return depth;
	}
}

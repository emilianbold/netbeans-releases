/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

import java.util.LinkedHashSet;
import java.util.Set;
import org.netbeans.api.debugger.Breakpoint.HIT_COUNT_FILTERING_STYLE;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSBreakpoint;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSURILocation;
/**
 *
 * @author jdeva
 */
public class JSBreakpointImpl implements JSBreakpoint {
    private String function, exception;
    private Set<String> ids;
    private int hitValue;
    private JSURILocation jsURILocation;
    private Boolean enabled;
    private HIT_COUNT_FILTERING_STYLE hitCondition;
    private JSBreakpoint.Type type;
    private String condition;

    public JSBreakpointImpl(String uri, int lineNumber){
        this(uri, lineNumber, -1);
    }

    public JSBreakpointImpl(String uri, int lineNumber, String id){
        this(uri, lineNumber, -1);
        this.ids = new LinkedHashSet<String>();
        this.ids.add(id);
    }

    public JSBreakpointImpl(String uri, int lineNumber, int columnNumber ){
    	jsURILocation = new JSURILocation(uri, lineNumber, columnNumber);
    }
    
    public JSBreakpointImpl(JSURILocation jsURILocation) {
        this.jsURILocation = jsURILocation;
    }

    public synchronized String getId() {
        if (ids == null || ids.isEmpty()) {
            return null;
        } else {
            return ids.iterator().next();
        }
    }

    public synchronized Set<String> getIds() {
        if (ids == null) {
            ids = new LinkedHashSet<String>();
        }
        return ids;
    }
    
    public synchronized void addId(String id) {
        if (ids == null) {
            ids = new LinkedHashSet<String>();
        }
        ids.add(id);
    }
    
    public synchronized void setId(String id) {
        this.ids = new LinkedHashSet<String>();
        this.ids.add(id);
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getHitValue() {
        return hitValue;
    }

    public void setHitValue(int hitValue) {
        this.hitValue = hitValue;
    }

    public HIT_COUNT_FILTERING_STYLE getHitCondition() {
        return hitCondition;
    }

    public void setHitCondition(HIT_COUNT_FILTERING_STYLE hitCondition) {
        this.hitCondition = hitCondition;
    }

    public JSURILocation getLocation() {
        return jsURILocation;
    }

    public String getCondition() {
        return condition;
    }
    
    public void setCondition(String condition) {
        this.condition = condition;
    }
}

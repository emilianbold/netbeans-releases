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
package org.netbeans.lib.v8debug;

/**
 *
 * @author Martin Entlicher
 */
public final class V8Breakpoint {
    
    public static enum Type {
    
        function,
        script,
        scriptId,
        scriptRegExp;
        
        public static Type typeFrom(String name) {
            if ("scriptName".equals(name)) {
                return Type.script;
            } else {
                return valueOf(name);
            }
        }
    
    }
    
    private final Type type;
    private final long scriptId;
    private final String scriptName;
    private final long number;
    private final long line;
    private final long column;
    private final long groupId;
    private final long hitCount;
    private final boolean active;
    private final long ignoreCount;
    private final ActualLocation[] actualLocations;
    
    public V8Breakpoint(Type type, long scriptId, String scriptName,
                        long number, long line, long column, long groupId,
                        long hitCount, boolean active, long ignoreCount,
                        ActualLocation[] actualLocations) {
        this.type = type;
        this.scriptId = scriptId;
        this.scriptName = scriptName;
        this.number = number;
        this.line = line;
        this.column = column;
        this.groupId = groupId;
        this.hitCount = hitCount;
        this.active = active;
        this.ignoreCount = ignoreCount;
        this.actualLocations = actualLocations;
    }

    public Type getType() {
        return type;
    }

    public long getScriptId() {
        return scriptId;
    }

    public String getScriptName() {
        return scriptName;
    }

    public long getNumber() {
        return number;
    }

    public long getLine() {
        return line;
    }

    public long getColumn() {
        return column;
    }

    public long getGroupId() {
        return groupId;
    }

    public long getHitCount() {
        return hitCount;
    }

    public boolean isActive() {
        return active;
    }

    public long getIgnoreCount() {
        return ignoreCount;
    }

    public ActualLocation[] getActualLocations() {
        return actualLocations;
    }
    
    public static final class ActualLocation {
        
        private final long line;
        private final long column;
        private final long scriptId;
        private final String scriptName;
        
        public ActualLocation(long line, long column, long scriptId) {
            this.line = line;
            this.column = column;
            this.scriptId = scriptId;
            this.scriptName = null;
        }
        
        public ActualLocation(long line, long column, String scriptName) {
            this.line = line;
            this.column = column;
            this.scriptId = -1;
            this.scriptName = scriptName;
        }

        public long getLine() {
            return line;
        }

        public long getColumn() {
            return column;
        }

        public long getScriptId() {
            return scriptId;
        }

        public String getScriptName() {
            return scriptName;
        }
    }
}

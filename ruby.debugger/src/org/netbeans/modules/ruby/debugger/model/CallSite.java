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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ruby.debugger.model;

public final class CallSite {

    private final String path;
    private final int line;

    public CallSite(final String path, final int line) {
        this.path = path;
        this.line = line;
    }

    public int getLine() {
        return line;
    }

    public String getPath() {
        return path;
    }

    public @Override boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CallSite other = (CallSite) obj;
        if (path != other.path && (path == null || !path.equals(other.path))) {
            return false;
        }
        if (line != other.line) {
            return false;
        }
        return true;
    }

    public @Override int hashCode() {
        int hash = 7;
        hash = 13 * hash + (this.path != null ? this.path.hashCode() : 0);
        hash = 13 * hash + this.line;
        return hash;
    }

    public @Override String toString() {
        return path + ':' + line;
    }

}

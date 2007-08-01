/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ruby.debugger.model;

/**
 * @author Martin Krauskopf
 */
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

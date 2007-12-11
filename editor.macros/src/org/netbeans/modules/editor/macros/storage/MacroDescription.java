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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.editor.macros.storage;

import java.util.List;
import org.netbeans.api.editor.settings.MultiKeyBinding;

/**
 *
 * @author Vita Stejskal
 */
public final class MacroDescription {
    
    public MacroDescription(String name, String code, String description, List<? extends MultiKeyBinding> shortcuts) {
        this.name = name;
        this.code = code;
        this.description = description;
        this.shortcuts = shortcuts;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public List<? extends MultiKeyBinding> getShortcuts() {
        return shortcuts;
    }

    public @Override String toString() {
        return "EditorMacro[name='" + name + "', shortcuts=[" + shortcuts + "]"; //NOI18N
    }

    public @Override boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MacroDescription other = (MacroDescription) obj;
        if (System.identityHashCode(this.name) != System.identityHashCode(other.name) &&
            (this.name == null || !this.name.equals(other.name))
        ) {
            return false;
        }
        if (System.identityHashCode(this.code) != System.identityHashCode(other.code) &&
            (this.code == null || !this.code.equals(other.code))
        ) {
            return false;
        }
        if (System.identityHashCode(this.description) != System.identityHashCode(other.description) && 
            (this.description == null || !this.description.equals(other.description))
        ) {
            return false;
        }
        if (System.identityHashCode(this.shortcuts) != System.identityHashCode(other.shortcuts) && 
            (this.shortcuts == null || !this.shortcuts.equals(other.shortcuts))
        ) {
            return false;
        }
        return true;
    }

    public @Override int hashCode() {
        int hash = 3;
        hash = 37 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 37 * hash + (this.code != null ? this.code.hashCode() : 0);
        hash = 37 * hash + (this.description != null ? this.description.hashCode() : 0);
        hash = 37 * hash + (this.shortcuts != null ? this.shortcuts.hashCode() : 0);
        return hash;
    }
    
    // ------------------------------------------
    // private implementation
    // ------------------------------------------

    private final String name;
    private final String code;
    private final String description;
    private final List<? extends MultiKeyBinding> shortcuts;
    
}

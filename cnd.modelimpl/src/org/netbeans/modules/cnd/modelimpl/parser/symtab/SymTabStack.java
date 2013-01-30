/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.parser.symtab;

import java.util.ArrayList;
import org.openide.util.CharSequences;

/**
 *
 * @author Vladimir Voskresensky
 * @author Nikolay Krasilnikov (nnnnnk@netbeans.org)
 */
public final class SymTabStack {
    private final ArrayList<SymTab> stack = new ArrayList<SymTab>();

    public static SymTabStack create() {
        return new SymTabStack();
    }
    
    private SymTabStack() {
       
    }
    
    public SymTab push() {        
        SymTab symTab = new SymTab(stack.size(), CharSequences.empty());
        stack.add(symTab);
        return symTab;
    }
    
    public SymTab push(CharSequence name) {        
        SymTab symTab = new SymTab(stack.size(), name);
        stack.add(symTab);
        return symTab;
    }    

    public SymTab push(SymTab symTab) {        
        stack.add(symTab);
        return symTab;
    }
    
    public SymTab pop() {
        assert stack.size() > 1;
        return stack.remove(stack.size() - 1);
    }

    public SymTab pop(CharSequence name) {
        assert stack.size() > 1;
        if(stack.get(stack.size() - 1).getName().equals(name)) {
            return stack.remove(stack.size() - 1);
        } else {
            return null;
        }
    }
    
    public SymTabEntry lookupLocal(CharSequence entry) {
        return getLocal().lookup(entry);
    }
    
    public SymTabEntry lookup(CharSequence entry) {
        assert stack.size() > 0;
        SymTabEntry out = null;
        for (int i = stack.size() - 1; i >= 0; i--) {
            out = stack.get(i).lookup(entry);
            if (out != null) {
                break;
            }
        }
        return out;
    }
    
    public SymTabEntry enterLocal(CharSequence entry) {
        return getLocal().enter(entry);
    }
    
    public void importToLocal(SymTab symTab) {
        getLocal().importSymTab(symTab);
    }

    private SymTab getLocal() {
        return stack.get(stack.size() - 1);
    }

    @Override
    public String toString() {
        return "SymTabStack{" + "nestingLevel=" + stack.size() + ", stack=" + stack + '}'; // NOI18N
    }
}

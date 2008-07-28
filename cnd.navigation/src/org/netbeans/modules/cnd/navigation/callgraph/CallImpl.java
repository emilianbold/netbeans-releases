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

package org.netbeans.modules.cnd.navigation.callgraph;

import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceSupport;
import org.netbeans.modules.cnd.callgraph.api.Call;
import org.netbeans.modules.cnd.callgraph.api.Function;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;

/**
 *
 * @author Alexander Simon
 */
public class CallImpl implements Call {

    private Function owner;
    private CsmReference reference;
    private Function function;
    private boolean nameOrder;
    
    public CallImpl(CsmFunction owner, CsmReference reference, CsmFunction function, boolean nameOrder){
        this.owner = new FunctionImpl(owner);
        this.reference = reference;
        this.function = new FunctionImpl(function);
        this.nameOrder = nameOrder;
    }

    public Object getReferencedCall() {
        return reference;
    }

    public void open() {
        CsmUtilities.openSource(reference);
    }

    public Function getCallee() {
        return function;
    }

    public Function getCaller() {
        return owner;
    }

    public int compareTo(Call o) {
        if (nameOrder) {
            return getCaller().getName().compareTo(o.getCaller().getName());
        }
        int diff = reference.getStartOffset() - ((CallImpl)o).reference.getStartOffset();
        if (diff == 0) {
             return getCallee().getName().compareTo(o.getCallee().getName());
       }
        return diff;
    }

    @Override
    public String toString() {
        if (nameOrder) {
            return getCallee().getName()+"<-"+getCaller().getName(); // NOI18N
        } else {
            return getCaller().getName()+"->"+getCallee().getName(); // NOI18N
        }
    }

    public String getHtmlDisplayName() {
        return CsmReferenceSupport.getContextLineHtml(reference, true).toString();
    }
}

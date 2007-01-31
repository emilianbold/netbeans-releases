/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.completion.csm;

import org.netbeans.modules.cnd.completion.csm.CsmContext.CsmContextEntry;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;

/**
 * context - ordered collection of scope entries passed till language context
 * @author Vladimir Voskresensky
 */
public class CsmContext {
    // offset for which the context is looking for or last off
    private int offset;
    
    // path of context as ordered list of context entries
    private List/*<CsmContextEntry>*/ context;
    
    // possible not null 
    // when context was found for exact inner object under offset
    // csmLastObject is subelement the last context entry's scope
    private CsmObject   csmLastObject;

    /** Creates a new instance of CsmContext */
    public CsmContext(int offset) {
        this.offset = offset;
        context = new ArrayList();
    }

    public CsmContextEntry get(int index) {
        return (CsmContextEntry)context.get(index);
    }

    public CsmContextEntry create(CsmScope scope) {
        return new CsmContextEntry(scope);
    }
    
    public CsmContextEntry create(CsmScope scope, int offset) {
        return new CsmContextEntry(scope, offset);
    }
    
    protected void add(CsmContextEntry entry) {
        context.add(entry);
    }

    public void add(CsmScope scope) {
        add(create(scope));
    }
    
    public void add(CsmScope scope, int offset) {
        add(create(scope, offset));
    }
    
    public void remove(CsmContextEntry entry) {
        context.remove(entry);
    }

    public CsmContextEntry getLastEntry() {
        if (isEmpty()) {
            return null;
        } else {
            return get(size() - 1);
        }
    }
    
    public CsmScope getLastScope() {
        if (getLastEntry() != null) {
            return getLastEntry().getScope();
        } else {
            return null;
        }
    }
    
    public CsmObject getLastObject() {
        return csmLastObject;
    }

    public void setLastObject(CsmObject obj) {
        this.csmLastObject = obj;
    }
    
    public boolean isEmpty() {
        return size() == 0;
    }
    
    public int size() {
        return context.size();
    }

    public Iterator iterator() {
        return context.iterator();
    }

    /**
     * Returns a string representation of the object. 
     * @return  a string representation of the object.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("context for offset ").append(offset); //NOI18N
        if (isEmpty()) {
            buf.append(" empty"); //NOI18N
        } else {
            buf.append(" with ").append(size()).append(" elements:\n"); //NOI18N
            for (Iterator it = context.iterator(); it.hasNext();) {
                CsmContextEntry elem = (CsmContextEntry) it.next();
                buf.append(elem);
                buf.append("\n"); //NOI18N
            }
        }
        return buf.toString();
    }
    
    
    // help structure to store one context object and offset where was jump in
    // inner scope
    public static class CsmContextEntry {
        // scope element
        private CsmScope    scope;
        
        // offset in scope to stop processing scopeElements
        private int         offset;
        
        public static final int WHOLE_SCOPE = -1;
        
        public CsmContextEntry(CsmScope scope) {
            this(scope, WHOLE_SCOPE);
        }
        
        public CsmContextEntry(CsmScope scope, int offset) {
            this.scope = scope;
            this.offset = offset;            
        }

        public CsmScope getScope() {
            return scope;
        }

        public int getOffset() {
            return offset;
        }
        
        public boolean isWholeScope() {
            return getOffset() == WHOLE_SCOPE;
        }

        /**
         * Returns a string representation of the object. 
         * @return  a string representation of the object.
         */
        public String toString() {
            StringBuffer buf = new StringBuffer();
            buf.append("["); //NOI18N
            if (isWholeScope()) {
                buf.append("whole scope"); //NOI18N
            } else {
                buf.append("jump in ").append(getOffset()); //NOI18N
            }
            CsmOffsetable offs = (CsmKindUtilities.isOffsetable(scope)) ? (CsmOffsetable)scope : null;
            if (offs != null) {
                // add range of scope
                buf.append(" ("); //NOI18N
                // start as line:col,offset
                CsmOffsetable.Position pos=offs.getStartPosition();
                buf.append(pos.getLine()).append(":").append(pos.getColumn()).append(",").append(pos.getOffset()); //NOI18N
                buf.append(";"); //NOI18N
                // end as line:col,offset
                pos=offs.getEndPosition();
                buf.append(pos.getLine()).append(":").append(pos.getColumn()).append(",").append(pos.getOffset()); //NOI18N
                buf.append(")"); //NOI18N
            }
            // add name 
            buf.append(CsmUtilities.getCsmName(scope));
            // add scope info
            buf.append(" scope - "); //NOI18N
            if (CsmKindUtilities.isScope(scope)) {
                buf.append(" [scope object] "); //NOI18N
            }  
            if (CsmKindUtilities.isScopeElement(scope)) {
                buf.append(" [scope element] "); //NOI18N
            }  
            buf.append("]"); //NOI18N
            return buf.toString();
        }
        
    }    
}

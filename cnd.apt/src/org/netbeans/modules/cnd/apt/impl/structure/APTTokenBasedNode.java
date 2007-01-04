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

package org.netbeans.modules.cnd.apt.impl.structure;

import antlr.Token;
import java.io.Serializable;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * base impl for nodes with associated token
 * @author Vladimir Voskresensky
 */
public abstract class APTTokenBasedNode extends APTBaseNode 
                                        implements Serializable {
    private static final long serialVersionUID = -1540565849389504039L;
    // seems, all will have sibling and token, but not all childs
    transient private APT next;
    private APTToken token;
    
    /** Copy constructor */
    /**package*/APTTokenBasedNode(APTTokenBasedNode orig) {
        super(orig);
        this.token = orig.token;
        // clear tree structure information
        this.next = null;
    }
    
    /** constructor for serialization **/
    protected APTTokenBasedNode() {
    }
    
    /** Creates a new instance of APTTokenBasedNode */
    protected APTTokenBasedNode(Token token) {
        this.token = (APTToken) token;
    }
    
    public void dispose() {
        this.token = APTUtils.EOF_TOKEN;
    }
    
    public Token getToken() {
        return token;
    }   
        
        
    public int getOffset() {
        if (token != null && token != APTUtils.EOF_TOKEN) {
            return token.getOffset();
        }
        return 0;
    }
    
    public int getEndOffset() {
        if (token != null && token != APTUtils.EOF_TOKEN) {
            return token.getEndOffset();
        }
        return 0;        
    }
    
    public abstract APT getFirstChild();
    
    public APT getNextSibling() {
        return next;
    }       
        
    public String getText() {
        return "TOKEN{" + (getToken()!= null ? getToken().toString() : "") + "}";
    }
    ////////////////////////////////////////////////////////////////////////////
    // implementation details
    
    /** 
     * sets next sibling element
     */
    public final void setNextSibling(APT next) {
        assert (next != null) : "null sibling, what for?";
        assert (this.next == null) : "why do you change immutable APT?";
        this.next = next;
    } 
    
    public abstract void setFirstChild(APT child);
}

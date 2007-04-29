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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.text.syntax;

import java.lang.ref.WeakReference;
import java.util.*;

import org.w3c.dom.*;

import javax.swing.text.*;

import org.netbeans.editor.ext.*;
import org.netbeans.editor.*;
import org.openide.ErrorManager;

/**
 *
 * Instances are produced by {@link XMLSyntaxSupport}.
 * <p>
 * <b>Warning:</b> class is public only for private purposes!
 *
 * @author  Petr Nejedly - original HTML design
 * @author  Sandeep Randhawa - XML port
 * @author  Petr Kuzel - DOM Nodes
 *
 * @version 1.0
 */
public abstract class SyntaxElement {
    
    // to do do not handle prolog as text!
    // support PIs
    
    protected XMLSyntaxSupport support;  // it produced us
    
    private WeakReference<TokenItem> first; //a weak reference to the fist TokenItem of this SE
    
    private WeakReference<SyntaxElement> previous;    // WR to the cached previous element
    private WeakReference<SyntaxElement> next;        // WR to the cached next element
    
    // let it be visible by static inner classes extending us
    protected int offset;     // original position in document //??? use item instead
    protected int length;     // original lenght in document
    
    /** Creates new SyntaxElement */
    public SyntaxElement(XMLSyntaxSupport support, TokenItem first, int to)  {
        this.support = support;
        this.offset = first.getOffset();
        this.length = to-offset;
        this.first = new WeakReference(first);
    }
    
    /** returns an instance of first TokenItem of this SyntaxElement.
     * The instance is weakly held by this SyntaxElement instance, once
     * it is GC'ed, a new one is created using the offset of the original one.
     *
     * The WeakReference is used here because of a huge deep memory consumption of
     * a TokenItem-s under some circumstances (The SyntaxSupport chains the tokens
     * so it happens that each SyntaxElement instance holds it's own long
     * TokenItem-s chain.
     *
     * The current implementation lowers the CPU performance slightly, but
     * allows to GC the TokenItem-s chains if necessary.
     */
    protected TokenItem first() {
        TokenItem cached_first = first.get();
        if(cached_first == null) {
            try {
                TokenItem new_first = support.getTokenChain(offset, offset + 1); //it is a first token offset, so we shouldn't overlap the document length
                first = new WeakReference(new_first);
                return new_first;
            }catch(BadLocationException e) {
                return null;
            }
        } else {
            return cached_first;
        }
    }
    
    public int getElementOffset() {
        
        return offset;
    }
    
    public int getElementLength() {
        return length;
    }
    
    void setNext(SyntaxElement se) {
        next = new WeakReference(se);
    }
    
    void setPrevious(SyntaxElement se) {
        previous = new WeakReference(se);
    }
    
    /**
     * Get previous SyntaxElement. Weakly cache results.
     * @return previous SyntaxElement or <code>null</code> at document begining
     * or illegal location.
     */
    public SyntaxElement getPrevious() {
        try {
            SyntaxElement cached_previous = previous == null ? null : previous.get();
            if( cached_previous == null ) {
                //we are on the beginning - no previous
                if (offset == 0) {
                    return null;
                }
                //data not inialized yet or GC'ed already - we need to parse again
                SyntaxElement new_previous = support.getElementChain( getElementOffset() - 1 );
                if( new_previous != null ) {
                    setPrevious(new_previous); //weakly cache the element
                    new_previous.setNext(this);
                    if (new_previous.offset == offset) {
                        Exception ex = new IllegalStateException("Previous cannot be the same as current element at offset " + offset);
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        return null;
                    }
                }
                return new_previous;
            } else {
                //use cached data
                return cached_previous;
            }
        } catch (BadLocationException ex) {
            return null;
        }
    }
    
    /**
     * Get next SyntaxElement. Cache results.
     * @return next SyntaxElement or <code>null</code> at document end
     * or illegal location.
     */
    public SyntaxElement getNext() {
        try {
            SyntaxElement cached_next = next == null ? null : next.get();
            if( cached_next == null ) {
                //data not inialized yet or GC'ed already - we need to parse again
                SyntaxElement new_next = support.getElementChain( offset+length + 1 );
                if( new_next != null ) {
                    setNext(new_next); //weakly cache the element
                    new_next.setPrevious(this);
                    if (new_next.offset == offset) {
                        // TODO see #43297 for causes and try to relax them
                        //Exception ex = new IllegalStateException("Next cannot be the same as current element at offset " + offset);
                        //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        return null;
                    }
                }
                return new_next;
            } else {
                //use cached data
                return cached_next;
            }
        } catch (BadLocationException ex) {
            return null;
        }
    }
    
    /**
     * Print element content for debug purposes.
     */
    public String toString() {
        String content = "?";
        try {
            content = support.getDocument().getText(offset, length);
        }catch(BadLocationException e) {}
        return "SyntaxElement [offset=" + offset + "; length=" + length + " ;type = " + this.getClass().getName() + "; content:" + content +"]";
    }
    
    /**
     *
     */
    public int hashCode() {
        return super.hashCode() ^ offset ^ length;
    }
    
    /**
     * DOM Node equals. It's not compatible with Object's equals specs!
     */
    public boolean equals(Object obj) {
        if (obj instanceof SyntaxElement) {
            if (((SyntaxElement)obj).offset == offset) return true;
        }
        return false;
    }
    
    
    // Particular non-DOM syntax elements ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /**
     * It may stop some DOM traversing.  //!!!
     */
    public static class Error extends SyntaxElement {
        
        public Error( XMLSyntaxSupport support, TokenItem from, int to ) {
            super( support, from, to );
        }
        
        public String toString() {
            return "Error" + super.toString();                                  // NOI18N
        }
    }
    
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.antlr.collections.impl;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 */

import org.netbeans.modules.cnd.antlr.collections.Stack;

import java.util.NoSuchElementException;

/**A Linked List Implementation (not thread-safe for simplicity)
 * (adds to the tail) (has an enumeration)
 */
public class LList implements Stack {
    LLCell head = null, tail = null;
    protected int length = 0;


    /** Add an object to the end of the list.
     * @param o the object to add
     */
    /*public void add(Object o) {
        append(o);
    }

    /** Append an object to the end of the list.
     * @param o the object to append
     */
    /*public void append(Object o) {
        LLCell n = new LLCell(o);
        if (length == 0) {
            head = tail = n;
            length = 1;
        }
        else {
            tail.next = n;
            tail = n;
            length++;
        }
    }

    /**Delete the object at the head of the list.
     * @return the object found at the head of the list.
     * @exception NoSuchElementException if the list is empty.
     */
    protected Object deleteHead() throws NoSuchElementException {
        if (head == null) throw new NoSuchElementException();
        Object o = head.data;
        head = head.next;
        length--;
        return o;
    }

    /**Get the ith element in the list.
     * @param i the index (from 0) of the requested element.
     * @return the object at index i
     * NoSuchElementException is thrown if i out of range
     */
    /*public Object elementAt(int i) throws NoSuchElementException {
        int j = 0;
        for (LLCell p = head; p != null; p = p.next) {
            if (i == j) return p.data;
            j++;
        }
        throw new NoSuchElementException();
    }*/

    /**Return an enumeration of the list elements */
    /*public Enumeration elements() {
        return new LLEnumeration(this);
    }*/

    /** How high is the stack? */
    public int height() {
        return length;
    }

    /** Answers whether or not an object is contained in the list
     * @param o the object to test for inclusion.
     * @return true if object is contained else false.
     */
    /*public boolean includes(Object o) {
        for (LLCell p = head; p != null; p = p.next) {
            if (p.data.equals(o)) return true;
        }
        return false;
    }*/
    // The next two methods make LLQueues and LLStacks easier.

    /** Insert an object at the head of the list.
     * @param o the object to add
     */
    protected void insertHead(Object o) {
        LLCell c = head;
        head = new LLCell(o);
        head.next = c;
        length++;
        if (tail == null) tail = head;
    }

    /**Return the length of the list.*/
    public int length() {
        return length;
    }

    /** Pop the top element of the stack off.
     * @return the top of stack that was popped off.
     * @exception NoSuchElementException if the stack is empty.
     */
    public Object pop() throws NoSuchElementException {
        Object o = deleteHead();
        return o;
    }
    // Satisfy the Stack interface now.

    /** Push an object onto the stack.
     * @param o the object to push
     */
    public void push(Object o) {
        insertHead(o);
    }

    public Object top() throws NoSuchElementException {
        if (head == null) throw new NoSuchElementException();
        return head.data;
    }
}

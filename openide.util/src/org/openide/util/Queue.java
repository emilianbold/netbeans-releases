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

package org.openide.util;

import java.util.LinkedList;

/** Queue of objects. When there is no object in the queue the process
* is suspended till some arrives.
* Implementation appears to be LIFO.
*
* @author Jaroslav Tulach
* @deprecated Use {@link java.util.concurrent.BlockingQueue} instead.
*/
@Deprecated
public class Queue<T> extends Object {
    /** Queue enumeration */
    private LinkedList<T> queue = new LinkedList<T>();

    /** Adds new item.
    * @param o object to add
    */
    public synchronized void put(T o) {
        queue.add(o);
        notify();
    }

    /** Gets an object from the queue. If there is no such object the
    * thread is suspended until some object arrives
    *
    * @return object from the queue
    */
    public synchronized T get() {
        for (;;) {
            if (queue.isEmpty()) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                }
            } else {
                break;
            }
        }

        return queue.removeFirst();
    }
}

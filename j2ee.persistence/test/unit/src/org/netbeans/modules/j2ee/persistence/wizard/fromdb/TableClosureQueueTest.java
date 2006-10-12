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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import junit.framework.TestCase;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableClosure.Queue;

/**
 *
 * @author Andrei Badea
 */
public class TableClosureQueueTest extends TestCase {

    public void testInitialElements() {
        Set<String> initial = new HashSet(Arrays.asList("a", "b"));
        Queue<String> queue = new Queue<String>(initial);
        Set<String> dequeued = new HashSet();
        while (!queue.isEmpty()) {
            dequeued.add(queue.poll());
        }
        assertNull("The poll() method should return null at the end of the queue.", queue.poll());
        assertEquals("Should dequeue the initial elements.", initial, dequeued);
    }

    public void testCannotAddEvenDequeuedElements() {
        Set<String> initial = Collections.emptySet();
        Queue<String> queue = new Queue<String>(initial);
        queue.offer("a");
        queue.offer("b");
        queue.offer("c");
        // add some existing elements
        queue.offer("a");
        queue.offer("c");
        assertEquals("Should poll 'a'.", "a", queue.poll());
        assertEquals("Should poll 'b'.", "b", queue.poll());
        // add some already dequeued elements
        queue.offer("b");
        queue.offer("a");
        assertEquals("Should not poll already dequeued elements.", "c", queue.poll());
        assertNull("The queue should be empty.", queue.poll());
    }
}

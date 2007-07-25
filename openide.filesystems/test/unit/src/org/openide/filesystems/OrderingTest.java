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

package org.openide.filesystems;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;

public class OrderingTest extends NbTestCase {

    public OrderingTest(String n) {
        super(n);
    }

    private FileObject dir, apex, ball, cone, dent;
    private CharSequence LOG;

    protected @Override void setUp() throws Exception {
        super.setUp();
        dir = FileUtil.createMemoryFileSystem().getRoot();
        apex = dir.createData("apex");
        ball = dir.createData("ball");
        cone = dir.createData("cone");
        dent = dir.createData("dent");
        LOG = Log.enable(Ordering.class.getName(), Level.WARNING);
        assertEmptyLog();
    }

    @Override
    protected Level logLevel() {
        return Level.OFF;
    }

    private void assertOrder(boolean logWarnings, FileObject... expectedOrder) throws Exception {
        assertEquals(Arrays.asList(expectedOrder), Ordering.getOrder(Arrays.asList(dir.getChildren()), logWarnings));
    }

    private void assertEmptyLog() {
        assertEquals("", LOG.toString());
    }

    private void assertLog(String mentionedSubstring) {
        assertTrue(LOG.toString(), LOG.toString().contains(mentionedSubstring));
    }

    public void testGetOrderNumeric() throws Exception {
        apex.setAttribute("position", 17);
        ball.setAttribute("position", 9);
        cone.setAttribute("position", 22);
        dent.setAttribute("position", 5);
        assertOrder(true, dent, ball, apex, cone);
        assertEmptyLog();
    }

    public void testGetOrderNoPositions() throws Exception {
        assertOrder(true, apex, ball, cone, dent);
        assertEmptyLog();
    }

    public void testStableSort() throws Exception {
        List<FileObject> order = Arrays.asList(ball, dent, apex, cone);
        assertEquals(order, Ordering.getOrder(order, false));
    }

    public void testGetOrderMissingPositions() throws Exception {
        apex.setAttribute("position", 17);
        ball.setAttribute("position", 9);
        assertOrder(false, ball, apex, cone, dent);
        assertEmptyLog();
        assertOrder(true, ball, apex, cone, dent);
        assertLog("cone");
        assertLog("dent");
    }

    public void testGetOrderEqualPositions() throws Exception {
        apex.setAttribute("position", 17);
        ball.setAttribute("position", 5);
        cone.setAttribute("position", 22);
        dent.setAttribute("position", 5);
        assertOrder(false, ball, dent, apex, cone);
        assertEmptyLog();
        assertOrder(true, ball, dent, apex, cone);
        assertLog("ball");
        assertLog("dent");
    }

    public void testGetOrderRelativeAttrs() throws Exception {
        dir.setAttribute("dent/ball", true);
        dir.setAttribute("ball/apex", true);
        dir.setAttribute("apex/cone", true);
        assertOrder(false, dent, ball, apex, cone);
        assertEmptyLog();
        assertOrder(true, dent, ball, apex, cone);
        assertLog("dent/ball");
        assertLog("ball/apex");
        assertLog("apex/cone");
    }

    public void testGetOrderMixed() throws Exception {
        dent.setAttribute("position", 10);
        apex.setAttribute("position", 90);
        dir.setAttribute("dent/cone", true);
        dir.setAttribute("cone/ball", true);
        dir.setAttribute("ball/apex", true);
        assertOrder(false, dent, cone, ball, apex);
        assertEmptyLog();
        assertOrder(true, dent, cone, ball, apex);
        assertLog("dent/cone");
        assertLog("cone/ball");
        assertLog("ball/apex");
    }

    public void testGetOrderNonNaturalNumbers() throws Exception {
        apex.setAttribute("position", 33.333);
        ball.setAttribute("position", -213L);
        cone.setAttribute("position", 5.4e3d);
        dent.setAttribute("position", (short) 200);
        assertOrder(true, ball, apex, dent, cone);
        assertEmptyLog();
    }

    public void testGetOrderNonNumericPositionAttrs() throws Exception {
        apex.setAttribute("position", "Timbuktu");
        assertOrder(false, apex, ball, cone, dent);
        assertEmptyLog();
        assertOrder(true, apex, ball, cone, dent);
        assertLog("apex");
        assertLog("Timbuktu");
    }

    public void testGetOrderRelativeAttrsFalse() throws Exception {
        dir.setAttribute("ball/apex", false);
        assertOrder(true, apex, ball, cone, dent);
        assertEmptyLog();
    }

    public void testGetOrderRelativeAttrsNonBoolean() throws Exception {
        dir.setAttribute("ball/apex", "maybe");
        assertOrder(false, apex, ball, cone, dent);
        assertEmptyLog();
        assertOrder(true, apex, ball, cone, dent);
        assertLog("ball/apex");
        assertLog("maybe");
    }

    public void testGetOrderRelativeAttrsNonexistentChildren() throws Exception {
        dir.setAttribute("apex/wacko", true);
        assertOrder(false, apex, ball, cone, dent);
        assertEmptyLog();
        assertOrder(true, apex, ball, cone, dent);
        assertLog("apex/wacko");
    }

    public void testGetOrderOnOnlySomeChildren() throws Exception {
        assertEquals(Collections.emptyList(), Ordering.getOrder(Collections.<FileObject>emptyList(), true));
        assertEmptyLog();
        apex.setAttribute("position", 20);
        ball.setAttribute("position", 10);
        assertEquals(Arrays.asList(ball, apex), Ordering.getOrder(Arrays.asList(apex, ball), true));
        assertEmptyLog();
    }

    public void testGetOrderDifferentParents() throws Exception {
        FileObject other = FileUtil.createData(dir, "subdir/other");
        try {
            Ordering.getOrder(Arrays.asList(apex, ball, other), false);
            fail();
        } catch (IllegalArgumentException e) {}
    }

    public void testGetOrderDuplicates() throws Exception {
        try {
            Ordering.getOrder(Arrays.asList(apex, apex), false);
            fail();
        } catch (IllegalArgumentException e) {}
    }

    public void testGetOrderTopologicalSortException() throws Exception {
        dir.setAttribute("apex/ball", true);
        dir.setAttribute("ball/apex", true);
        Ordering.getOrder(Arrays.asList(apex, ball, cone, dent), false);
        assertEmptyLog();
        Ordering.getOrder(Arrays.asList(apex, ball, cone, dent), true);
        assertLog("apex");
        assertLog("ball");
    }

    public void testSetOrderBasic() throws Exception {
        Ordering.setOrder(Arrays.asList(dent, ball, apex, cone));
        assertOrder(true, dent, ball, apex, cone);
        assertEmptyLog();
    }

    public void testGetOrderZeroPositions() throws Exception { // #107550
        apex.setAttribute("position", 0);
        ball.setAttribute("position", 0);
        cone.setAttribute("position", 22);
        dent.setAttribute("position", 5);
        assertOrder(true, apex, ball, dent, cone);
        assertEmptyLog();
    }

    public void testSetOrderConservativeOneJump() throws Exception {
        apex.setAttribute("position", 17);
        ball.setAttribute("position", 9);
        cone.setAttribute("position", 23);
        dent.setAttribute("position", 5);
        Ordering.setOrder(Arrays.asList(dent, apex, ball, cone));
        assertOrder(true, dent, apex, ball, cone);
        assertEquals(5, dent.getAttribute("position"));
        assertEquals(23, cone.getAttribute("position"));
        assertEquals(17, apex.getAttribute("position"));
        assertEquals(20, ball.getAttribute("position"));
        assertEmptyLog();
        // XXX test also complex reorders; swaps with left bias; larger rotations; moves to start or end; {X} => {X} and {} => {}; ad nauseam
        // XXX test sO when newly added item (e.g. at end, or elsewhere) has no initial position
    }

    // XXX test IAE, ...

}

/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module.api.support;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Element;

// XXX testMissingImport
// XXX testDiamondImport

/**
 * Tests functionality of {@link TargetLister}.
 * @author Jesse Glick
 */
public class TargetListerTest extends NbTestCase {
    
    public TargetListerTest(String name) {
        super(name);
    }
    
    private FileObject testdir;

    protected void setUp() throws Exception {
        super.setUp();
        FileObject masterTestdir = FileUtil.toFileObject(getDataDir());
        assertNotNull("testdir unit/data exists", masterTestdir);
        testdir = masterTestdir.getFileObject("targetlister");
        assertNotNull("testdir unit/data/targetlister exists", testdir);
    }
    
    public void testSimpleUsage() throws Exception {
        FileObject simple = testdir.getFileObject("simple.xml");
        assertNotNull("simple.xml found", simple);
        List/*<TargetLister.Target>*/ targets = getTargets(simple);
        assertEquals("five targets", 5, targets.size());
        // -internal, -internal-described, described, main, undescribed
        TargetLister.Target t = (TargetLister.Target) targets.get(0);
        assertEquals("correct name #1", "-internal", t.getName());
        assertEquals("correct qname #1", "simple.-internal", t.getQualifiedName());
        Element e = t.getElement();
        assertEquals("correct element name #1", "target", e.getLocalName());
        assertEquals("correct name attribute #1", "-internal", e.getAttribute("name"));
        AntProjectCookie apc = TargetLister.getAntProjectCookie(simple);
        assertEquals("element in script #1", apc.getDocument(), e.getOwnerDocument());
        assertEquals("correct script #1", apc, t.getScript());
        assertFalse("not described #1", t.isDescribed());
        assertTrue("internal #1", t.isInternal());
        assertFalse("not overridden #1", t.isOverridden());
        assertFalse("not default #1", t.isDefault());
        t = (TargetLister.Target) targets.get(1);
        assertEquals("correct name #2", "-internal-described", t.getName());
        assertTrue("described #2", t.isDescribed());
        assertTrue("internal #2", t.isInternal());
        assertFalse("not overridden #2", t.isOverridden());
        assertFalse("not default #2", t.isDefault());
        t = (TargetLister.Target) targets.get(2);
        assertEquals("correct name #3", "described", t.getName());
        assertTrue("described #3", t.isDescribed());
        assertFalse("not internal #3", t.isInternal());
        assertFalse("not overridden #3", t.isOverridden());
        assertFalse("not default #3", t.isDefault());
        t = (TargetLister.Target) targets.get(3);
        assertEquals("correct name #4", "main", t.getName());
        assertFalse("not described #4", t.isDescribed());
        assertFalse("not internal #4", t.isInternal());
        assertFalse("not overridden #4", t.isOverridden());
        assertTrue("default #4", t.isDefault());
        t = (TargetLister.Target) targets.get(4);
        assertEquals("correct name #5", "undescribed", t.getName());
        assertFalse("not described #5", t.isDescribed());
        assertFalse("not internal #5", t.isInternal());
        assertFalse("not overridden #5", t.isOverridden());
        assertFalse("not default #5", t.isDefault());
    }
    
    public void testBasicImportAndOverrides() throws IOException {
        FileObject importing = testdir.getFileObject("importing.xml");
        assertNotNull("importing.xml found", importing);
        List/*<TargetLister.Target>*/ targets = getTargets(importing);
        assertEquals("seven targets", 7, targets.size());
        // dir1/dir3/subimported.subtarget3, dir1/dir3/subimported.whatever, dir1/imported.subtarget1,
        // dir1/imported.subtarget2, dir1/imported.whatever, importing.main, importing.subtarget1
        TargetLister.Target t = (TargetLister.Target) targets.get(0);
        assertEquals("correct qname #1", "dir1/dir3/subimported.subtarget3", t.getQualifiedName());
        assertFalse("not described #1", t.isDescribed());
        assertFalse("not internal #1", t.isInternal());
        assertFalse("not overridden #1", t.isOverridden());
        assertFalse("not default #1", t.isDefault());
        t = (TargetLister.Target) targets.get(1);
        assertEquals("correct qname #2", "dir1/dir3/subimported.whatever", t.getQualifiedName());
        assertFalse("not described #2", t.isDescribed());
        assertFalse("not internal #2", t.isInternal());
        assertTrue("overridden #2", t.isOverridden());
        assertFalse("not default #2", t.isDefault());
        t = (TargetLister.Target) targets.get(2);
        assertEquals("correct qname #3", "dir1/imported.subtarget1", t.getQualifiedName());
        assertFalse("not described #3", t.isDescribed());
        assertFalse("not internal #3", t.isInternal());
        assertTrue("overridden #3", t.isOverridden());
        assertFalse("not default #3", t.isDefault());
        t = (TargetLister.Target) targets.get(3);
        assertEquals("correct qname #4", "dir1/imported.subtarget2", t.getQualifiedName());
        assertFalse("not described #4", t.isDescribed());
        assertFalse("not internal #4", t.isInternal());
        assertFalse("not overridden #4", t.isOverridden());
        assertFalse("not default #4", t.isDefault());
        t = (TargetLister.Target) targets.get(4);
        assertEquals("correct qname #5", "dir1/imported.whatever", t.getQualifiedName());
        assertFalse("not described #5", t.isDescribed());
        assertFalse("not internal #5", t.isInternal());
        assertFalse("not overridden #5", t.isOverridden());
        assertFalse("not default #5", t.isDefault());
        t = (TargetLister.Target) targets.get(5);
        assertEquals("correct qname #6", "importing.main", t.getQualifiedName());
        assertFalse("not described #6", t.isDescribed());
        assertFalse("not internal #6", t.isInternal());
        assertFalse("not overridden #6", t.isOverridden());
        assertTrue("default #6", t.isDefault());
        t = (TargetLister.Target) targets.get(6);
        assertEquals("correct qname #7", "importing.subtarget1", t.getQualifiedName());
        assertTrue("described #7", t.isDescribed());
        assertFalse("not internal #7", t.isInternal());
        assertFalse("not overridden #7", t.isOverridden());
        assertFalse("not default #7", t.isDefault());
    }
    
    public void testImportedDefaultAndDifferentBasedir() throws Exception {
        // #50087: Ant does *not* use the basedir when resolving an <import>!
        FileObject importing4 = testdir.getFileObject("importing4.xml");
        assertNotNull("importing4.xml found", importing4);
        List/*<TargetLister.Target>*/ targets = getTargets(importing4);
        assertEquals("three targets", 3, targets.size());
        // dir2/imported2.subtarget4, dir2/imported2.whatever, importing4.subtarget4
        TargetLister.Target t = (TargetLister.Target) targets.get(0);
        assertEquals("correct qname #1", "dir2/imported2.subtarget4", t.getQualifiedName());
        assertTrue("overridden #1", t.isOverridden());
        assertFalse("not default #1", t.isDefault());
        t = (TargetLister.Target) targets.get(1);
        assertEquals("correct qname #2", "dir2/imported2.whatever", t.getQualifiedName());
        assertFalse("not overridden #2", t.isOverridden());
        assertTrue("default #2", t.isDefault());
        t = (TargetLister.Target) targets.get(2);
        assertEquals("correct qname #3", "importing4.subtarget4", t.getQualifiedName());
        assertFalse("not overridden #3", t.isOverridden());
        assertFalse("not default #3", t.isDefault());
    }

    /** Cf. #55263: stack overflow error */
    public void testRecursiveImport() throws Exception {
        FileObject rec1 = testdir.getFileObject("recursive1.xml");
        assertNotNull("recursive1.xml found", rec1);
        List/*<TargetLister.Target>*/ targets = getTargets(rec1);
        assertEquals("two targets", 2, targets.size());
        TargetLister.Target t = (TargetLister.Target) targets.get(0);
        assertEquals("correct qname #1", "recursive1.x", t.getQualifiedName());
        assertTrue("default #1", t.isDefault());
        t = (TargetLister.Target) targets.get(1);
        assertEquals("correct qname #2", "recursive2.y", t.getQualifiedName());
        assertFalse("not default #2", t.isDefault());
    }
    
    public void testComputedImports() throws Exception {
        FileObject importing = testdir.getFileObject("computedimports/importing.xml");
        assertNotNull("importing.xml found", importing);
        List/*<TargetLister.Target>*/ targets = getTargets(importing);
        assertEquals("three targets", 3, targets.size());
        TargetLister.Target t = (TargetLister.Target) targets.get(0);
        assertEquals("correct qname #1", "importing.master", t.getQualifiedName());
        t = (TargetLister.Target) targets.get(1);
        assertEquals("correct qname #2", "subdir/imported1.foundme", t.getQualifiedName());
        t = (TargetLister.Target) targets.get(2);
        assertEquals("correct qname #3", "subdir/imported3.intermediate", t.getQualifiedName());
    }
    
    private static List/*<TargetLister.Target>*/ getTargets(FileObject fo) throws IOException {
        AntProjectCookie apc = TargetLister.getAntProjectCookie(fo);
        SortedSet/*<Target>*/ targets = new TreeSet(new TargetComparator());
        targets.addAll(TargetLister.getTargets(apc));
        return new ArrayList(targets);
    }
    
    /** Sorts targets by FQN. */
    private static final class TargetComparator implements Comparator/*<TargetLister.Target>*/ {
        
        public TargetComparator() {}

        public int compare(Object o1, Object o2) {
            TargetLister.Target t1 = (TargetLister.Target) o1;
            TargetLister.Target t2 = (TargetLister.Target) o2;
            int x = t1.getQualifiedName().compareTo(t2.getQualifiedName());
            if (x != 0) {
                return x;
            } else {
                return System.identityHashCode(t2) - System.identityHashCode(t1);
            }
        }

    }
    
}

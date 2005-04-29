/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.filesystems;

import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;

/**
 * Trying to mimic IZ 49418.
 *
 * @author Radek Matous
 */
public class MIMESupport49418Test extends NbTestCase {
    private FileSystem lfs;
    private static FileObject mimeFo;
    private static final String MIME_TYPE = "text/x-opqr";

    public MIMESupport49418Test(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        System.setProperty("org.openide.util.Lookup", "org.openide.filesystems.MIMESupport49418Test$Lkp");
        super.setUp();
        assertEquals("Our lookup is registered", Lkp.class, Lookup.getDefault().getClass());
        lfs = TestUtilHid.createLocalFileSystem(getName(), new String[]{"A.opqr", });
        mimeFo = lfs.findResource("A.opqr");
        assertNotNull(mimeFo);
    }


    public void testMIMEResolution()
            throws Exception {
        assertNull(Lookup.getDefault().lookup(Runnable.class));
        assertEquals(MIME_TYPE, mimeFo.getMIMEType());

    }

    /**
     * This is a pair that as a part of its instanceOf method queries the URL resolver.
     */
    private static class QueryingPair extends org.openide.util.lookup.AbstractLookup.Pair {
        public boolean beBroken;

        public String getId() {
            return getType().toString();
        }

        public String getDisplayName() {
            return getId();
        }

        public Class getType() {
            return getClass();
        }

        protected boolean creatorOf(Object obj) {
            return obj == this;
        }

        protected boolean instanceOf(Class c) {
            if (beBroken) {
                beBroken = false;
                assertEquals("content/unknown", mimeFo.getMIMEType());

            }
            return c.isAssignableFrom(getType());
        }

        public Object getInstance() {
            return this;
        }
    }


    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        private static org.openide.util.lookup.InstanceContent ic;

        public Lkp() {
            this(new org.openide.util.lookup.InstanceContent());
        }

        private Lkp(org.openide.util.lookup.InstanceContent ic) {
            super(ic);
            this.ic = ic;
        }

        protected void initialize() {
            // a small trick to make the InheritanceTree storage to be used
            // because if the amount of elements in small, the ArrayStorage is 
            // used and it does not have the same problems like InheritanceTree
            for (int i = 0; i < 1000; i++) {
                ic.add(new Integer(i));
            }

            QueryingPair qp = new QueryingPair();
            ic.addPair(qp);
            ic.add(new MIMEResolver() {
                public String findMIMEType(FileObject fo) {
                    return MIME_TYPE;
                }
            });


            qp.beBroken = true;
        }

    } // end of Lkp
}

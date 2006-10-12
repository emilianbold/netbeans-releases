/**
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

package org.netbeans.modules.j2ee.persistence.unit;

import java.net.URI;
import java.util.Enumeration;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.FileOwnerQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataLoaderPool;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Erno Mononen, Andrei Badea
 */
public abstract class PUDataObjectTestBase extends NbTestCase {

    static {
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
        ((Lkp)Lookup.getDefault()).setLookups(new Object[] { new PUMimeResolver(), new Pool(), new PUFOQI() });

        assertEquals("Unable to set the default lookup!", Lkp.class, Lookup.getDefault().getClass());
        assertEquals("The default MIMEResolver is not our resolver!", PUMimeResolver.class, Lookup.getDefault().lookup(MIMEResolver.class).getClass());
        assertEquals("The default DataLoaderPool is not our pool!", Pool.class, Lookup.getDefault().lookup(DataLoaderPool.class).getClass());
    }

    public PUDataObjectTestBase(String name) {
        super(name);
    }

    /**
     * Our default lookup.
     */
    public static final class Lkp extends ProxyLookup {

        public Lkp() {
            setLookups(new Object[0]);
        }

        public void setLookups(Object[] instances) {
            ClassLoader l = PersistenceEditorTestBase.class.getClassLoader();
            setLookups(new Lookup[] {
                Lookups.fixed(instances),
                Lookups.metaInfServices(l),
                Lookups.singleton(l)
            });
        }
    }

    /**
     * DataLoaderPool which is registered in the default lookup and loads
     * PUDataLoader.
     */
    public static final class Pool extends DataLoaderPool {

        public Enumeration loaders() {
            return Enumerations.singleton(new PUDataLoader());
        }
    }

    /**
     * MIME Resolver that associates persistence.xml with PUDataLoader.
     */
    public static final class PUMimeResolver extends MIMEResolver {

        public String findMIMEType(FileObject fo) {
            if (fo.getName().startsWith("persistence")){
                return PUDataLoader.REQUIRED_MIME;
            }
            return null;
        }
    }

    /**
     * Returns dummy project implementation. Needed since persistence unit needs
     * to be associated with {@link Project} owner. Also see issue #74426.
     */
    private static final class PUFOQI implements FileOwnerQueryImplementation {

        private final Project dummyProject = new Project() {
            public Lookup getLookup() { return Lookup.EMPTY; }
            public FileObject getProjectDirectory() { return null; }
        };

        public Project getOwner(URI file) {
            return dummyProject;
        }

        public Project getOwner(FileObject file) {
            return dummyProject;
        }
    }
}

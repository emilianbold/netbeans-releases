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

package org.netbeans.api.project.libraries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.libraries.WritableLibraryProvider;
import org.netbeans.spi.project.libraries.LibraryFactory;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.InstanceDataObject;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.Lookups;
/**
 *
 * @author  Tomas Zezula
 */
public class LibraryManagerTest extends NbTestCase {
    
    private TestLibraryProvider lp;
    
    private static final String LIBRARY_TYPE = "j2se";  //NOI18N
    private static final String[] VOLUME_TYPES = new String[] {
        "bin",
        "src",
        "doc"
    };
    
    /** Creates a new instance of LibraryManagerTest */
    public LibraryManagerTest (String testName) {
        super (testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        lp = new TestLibraryProvider ();
        TestUtil.setLookup (Lookups.fixed(new Object[] {
            lp
        }));
        registerLibraryTypeProvider();
    }
    
    
    private static void registerLibraryTypeProvider () throws Exception {
        StringTokenizer tk = new StringTokenizer("org-netbeans-api-project-libraries/LibraryTypeProviders","/");
        FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
        while (tk.hasMoreElements()) {
            String pathElement = tk.nextToken();
            FileObject tmp = root.getFileObject(pathElement);
            if (tmp == null) {
                tmp = root.createFolder(pathElement);
            }
            root = tmp;
        }
        if (root.getChildren().length == 0) {
            InstanceDataObject.create (DataFolder.findFolder(root),"TestLibraryTypeProvider",TestLibraryTypeProvider.class);
        }
    }
    
    public void testGetLibraries () throws Exception {        
        LibraryManager lm = LibraryManager.getDefault();
        TestListener tl = new TestListener ();
        lm.addPropertyChangeListener(tl);
        Library[] libs = lm.getLibraries();
        assertEquals ("Libraries count", 0, libs.length);
        LibraryImplementation[] impls = createTestLibs ();
        lp.setLibraries(impls);        
        libs = lm.getLibraries();
        assertEventsEquals(tl.getEventNames(), new String[] {LibraryManager.PROP_LIBRARIES});
        tl.reset();
        assertEquals ("Libraries count", 2, libs.length);
        assertLibsEquals (libs, impls);
        lp.setLibraries(new LibraryImplementation[0]);        
        assertEventsEquals(tl.getEventNames(), new String[] {LibraryManager.PROP_LIBRARIES});
        tl.reset();
        libs = lm.getLibraries();
        assertEquals ("Libraries count", 0, libs.length);
    }
    
    public void testGetLibrary () throws Exception {
        LibraryImplementation[] impls = createTestLibs ();
        lp.setLibraries(impls);        
        LibraryManager lm = LibraryManager.getDefault();
        Library[] libs = lm.getLibraries();
        assertEquals ("Libraries count", 2, libs.length);
        assertLibsEquals (libs, impls);
        Library lib = lm.getLibrary("Library1");
        assertNotNull ("Existing library", lib);
        assertLibsEquals(new Library[] {lib}, new LibraryImplementation[] {impls[0]});
        lib = lm.getLibrary("Library2");
        assertNotNull ("Existing library", lib);
        assertLibsEquals(new Library[] {lib}, new LibraryImplementation[] {impls[1]});
        lib = lm.getLibrary("Library3");
        assertNull ("Nonexisting library", lib);
    }
    
    public void testAddRemoveLibrary () throws Exception {
        final LibraryImplementation[] impls = createTestLibs();
        lp.setLibraries(impls);
        final LibraryManager lm = LibraryManager.getDefault();
        Library[] libs = lm.getLibraries();
        assertEquals ("Libraries count", 2, libs.length);
        assertLibsEquals (libs, impls);
        final LibraryTypeProvider provider = LibrariesSupport.getLibraryTypeProvider(LIBRARY_TYPE);
        assertNotNull (provider);
        final LibraryImplementation newLibImplementation = provider.createLibrary();
        newLibImplementation.setName("NewLib");
        final Library newLibrary = LibraryFactory.createLibrary(newLibImplementation);
        lm.addLibrary(newLibrary);
        libs = lm.getLibraries();
        assertEquals ("Libraries count", 3, libs.length);
        List newLibs = new ArrayList (Arrays.asList(impls));
        newLibs.add (newLibImplementation);
        assertLibsEquals (libs, (LibraryImplementation[])newLibs.toArray(new LibraryImplementation[newLibs.size()]));
        lm.removeLibrary(newLibrary);
        libs = lm.getLibraries();
        assertEquals("Libraries count",2,libs.length);
        assertLibsEquals (libs, impls);
    }
    
    static LibraryImplementation[] createTestLibs () throws MalformedURLException {
        LibraryImplementation[] impls = new LibraryImplementation[] {
            new TestLibraryImplementation (),
            new TestLibraryImplementation ()
        };
        impls[0].setName ("Library1");
        impls[1].setName ("Library2");
        impls[0].setDescription("Library1 description");
        impls[1].setDescription("Library2 description");
        impls[0].setContent("bin", Collections.singletonList( new URL("file:/lib/libest1.so")));
        impls[1].setContent("bin", Collections.singletonList(new URL("file:/lib/libest2.so")));
        return impls;
    }
    
    static void assertLibsEquals (Library[] libs, LibraryImplementation[] impls) {
        assertEquals ("libs equals (size)", impls.length, libs.length);
        for (int i=0; i< libs.length; i++) {
            assertEquals ("libs equals (name)", impls[i].getName(), libs[i].getName());
            assertEquals ("libs equals (description)", impls[i].getDescription(), libs[i].getDescription());
            List lc = libs[i].getContent("bin");
            List ic = impls[i].getContent("bin");
            assertEquals ("libs equals (content bin)", ic, lc);
            lc = libs[i].getContent("src");
            ic = impls[i].getContent("src");
            assertEquals ("libs equals (content src)", ic, lc);
            lc = libs[i].getContent("doc");
            ic = impls[i].getContent("doc");
            assertEquals ("libs equals (content doc)", ic, lc);
        }
    }
    
    static void assertEventsEquals (List eventNames, String[] expectedName) {
        assertEquals ("Events equals", Arrays.asList(expectedName), eventNames);
    }
    
    
    static class TestLibraryProvider implements WritableLibraryProvider  {
        
        private PropertyChangeSupport support;
        private List libraries;
        
        public TestLibraryProvider () {
            this.support = new PropertyChangeSupport (this);
            this.libraries = new ArrayList();
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            this.support.removePropertyChangeListener(listener);
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            this.support.addPropertyChangeListener(listener);
        }

        public LibraryImplementation[] getLibraries() {
            return (LibraryImplementation[]) this.libraries.toArray(new LibraryImplementation[libraries.size()]);
        }                
        
        public void setLibraries (LibraryImplementation[] libraries) {
            this.libraries = new ArrayList(Arrays.asList(libraries));
            this.support.firePropertyChange(PROP_LIBRARIES,null,null);
        }

        public void addLibrary(LibraryImplementation library) throws IOException {
            this.libraries.add (library);
            this.support.firePropertyChange(PROP_LIBRARIES,null,null);
        }

        public void removeLibrary(LibraryImplementation library) throws IOException {
            this.libraries.remove (library);
            this.support.firePropertyChange(PROP_LIBRARIES,null,null);
        }

        public void updateLibrary(LibraryImplementation oldLibrary, LibraryImplementation newLibrary) throws IOException {
            this.libraries.remove(oldLibrary);
            this.libraries.add (newLibrary);
            this.support.firePropertyChange(PROP_LIBRARIES,null,null);
        }
        
    }
    
    static class TestListener implements PropertyChangeListener {
        
        private List<String> events = new ArrayList<String>();
        
        public void propertyChange(PropertyChangeEvent event) {
            this.events.add(event.getPropertyName());
        }                
        
        public void reset () {
            this.events.clear();
        }
        
        public List getEventNames () {
            return this.events;
        }
    }
    
    
    public static class TestLibraryTypeProvider implements LibraryTypeProvider {
            

        public String getDisplayName() {
            return LIBRARY_TYPE;
        }

        public String getLibraryType() {
            return LIBRARY_TYPE;
        }

        public String[] getSupportedVolumeTypes() {
            return VOLUME_TYPES;
        }

        public LibraryImplementation createLibrary() {
            return LibrariesSupport.createLibraryImplementation(LIBRARY_TYPE, VOLUME_TYPES);
        }

        public void libraryDeleted(LibraryImplementation library) {
        }

        public void libraryCreated(LibraryImplementation library) {
        }

        public java.beans.Customizer getCustomizer(String volumeType) {
            return null;
        }

        public org.openide.util.Lookup getLookup() {
            return null;
        }
    }
    
    static class TestLibraryImplementation implements LibraryImplementation {
        
        private static final Set<String> supportedTypes;
        
        private String name;
        private String description;        
        private Map<String,List<URL>> contents;
        private PropertyChangeSupport support;
        
        static {            
            supportedTypes = Collections.unmodifiableSet(new HashSet(Arrays.asList(VOLUME_TYPES)));
        }
        
        public TestLibraryImplementation () {
            this.contents = new HashMap<String,List<URL>>();
            this.support = new PropertyChangeSupport (this);
        }
        
        public String getType() {
            return LIBRARY_TYPE;
        }
        
        public String getName() {
            return this.name;
        }
        
        public void setName(String name) {
            this.name = name;
            this.support.firePropertyChange(PROP_NAME, null, null);
        }

        public String getLocalizingBundle() {
            return null;
        }
        
        public void setLocalizingBundle(String resourceName) {  
        }
        
        public String getDescription() {
            return this.description;
        }

        public void setDescription(String text) {
            this.description = text;
            this.support.firePropertyChange(PROP_DESCRIPTION, null, null);
        }

        public List<URL> getContent(String volumeType) throws IllegalArgumentException {
            if (supportedTypes.contains(volumeType)) {
                List<URL> l = contents.get(volumeType);
                if (l == null) {
                    l = Collections.emptyList();
                }
                return Collections.unmodifiableList(l);
            }
            else {
                throw new IllegalArgumentException ();
            }
        }
        
        public void setContent(String volumeType, List<URL> path) throws IllegalArgumentException {
            if (supportedTypes.contains(volumeType)) {
                this.contents.put (volumeType, path);
                this.support.firePropertyChange(PROP_CONTENT, null, null);
            }
            else {
                throw new IllegalArgumentException ();
            }
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
            this.support.removePropertyChangeListener(l);
        }

        public void addPropertyChangeListener(PropertyChangeListener l) {
            this.support.addPropertyChangeListener(l);
        }              
    }
    
}

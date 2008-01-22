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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.libraries;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.spi.project.libraries.ArealLibraryProvider;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryProvider;
import org.netbeans.spi.project.libraries.LibraryStorageArea;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.util.WeakSet;

/**
 * Common support classes for unit tests in this module.
 */
public class TestUtil {

    private TestUtil() {}

    public static class NWLP implements LibraryProvider<LibraryImplementation> {

        public final List<LibraryImplementation> libs = new ArrayList<LibraryImplementation>();
        final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

        public void set(LibraryImplementation... nue) {
            libs.clear();
            libs.addAll(Arrays.asList(nue));
            pcs.firePropertyChange(PROP_LIBRARIES, null, null);
        }

        public LibraryImplementation[] getLibraries() {
            return libs.toArray(new LibraryImplementation[0]);
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }

    }

    public static final class WLP extends NWLP implements WritableLibraryProvider<LibraryImplementation> {

        public void addLibrary(LibraryImplementation library) throws IOException {
            libs.add(library);
            pcs.firePropertyChange(PROP_LIBRARIES, null, null);
        }

        public void removeLibrary(LibraryImplementation library) throws IOException {
            libs.remove(library);
            pcs.firePropertyChange(PROP_LIBRARIES, null, null);
        }

        public void updateLibrary(LibraryImplementation oldLibrary, LibraryImplementation newLibrary) throws IOException {
            libs.remove(oldLibrary);
            libs.add(newLibrary);
            pcs.firePropertyChange(PROP_LIBRARIES, null, null);
        }

    }

    public static final class Area implements LibraryStorageArea {

        final String id;

        public Area(String id) {
            this.id = id;
        }

        public URL getLocation() {
            try {
                return new URL("http://nowhere.net/" + id);
            } catch (MalformedURLException x) {
                throw new AssertionError(x);
            }
        }

        public String getDisplayName() {
            return id;
        }

        public boolean equals(Object obj) {
            return obj instanceof Area && ((Area) obj).id.equals(id);
        }

        public int hashCode() {
            return id.hashCode();
        }

        public @Override String toString() {
            return "Area[" + id + "]";
        }

    }

    public static final class ALP implements ArealLibraryProvider<Area,LibraryImplementation> {

        final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        public final Map<Area,List<LibraryImplementation>> libs = new HashMap<Area,List<LibraryImplementation>>();
        final Set<LP> lps = new WeakSet<LP>();
        final Set<Area> open = new HashSet<Area>();

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }

        public Class<Area> areaType() {
            return Area.class;
        }

        public Class<LibraryImplementation> libraryType() {
            return LibraryImplementation.class;
        }

        public Area createArea() {
            return new Area("new");
        }

        public Area loadArea(URL location) {
            Matcher m = Pattern.compile("http://nowhere\\.net/(.+)$").matcher(location.toExternalForm());
            if (m.matches()) {
                return new Area(m.group(1));
            } else {
                return null;
            }
        }

        public Set<Area> getOpenAreas() {
            return open;
        }

        public void setOpen(Area... areas) {
            open.clear();
            open.addAll(Arrays.asList(areas));
            pcs.firePropertyChange(PROP_OPEN_AREAS, null, null);
        }

        private class LP implements LibraryProvider<LibraryImplementation> {

            final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
            final Area area;

            LP(Area area) {
                this.area = area;
                lps.add(this);
            }

            public LibraryImplementation[] getLibraries() {
                if (libs.containsKey(area)) {
                    return libs.get(area).toArray(new LibraryImplementation[0]);
                } else {
                    return new LibraryImplementation[0];
                }
            }

            public void addPropertyChangeListener(PropertyChangeListener listener) {
                pcs.addPropertyChangeListener(listener);
            }

            public void removePropertyChangeListener(PropertyChangeListener listener) {
                pcs.removePropertyChangeListener(listener);
            }

        }

        public LibraryProvider<LibraryImplementation> getLibraries(Area area) {
            return new LP(area);
        }

        public LibraryImplementation createLibrary(String type, String name, Area area, Map<String,List<URL>> contents) throws IOException {
            LibraryImplementation lib = LibrariesSupport.createLibraryImplementation(type, contents.keySet().toArray(new String[0]));
            lib.setName(name);
            for (Map.Entry<String,List<URL>> entry : contents.entrySet()) {
                lib.setContent(entry.getKey(), entry.getValue());
            }
            List<LibraryImplementation> l = libs.get(area);
            if (l == null) {
                l = new ArrayList<LibraryImplementation>();
                libs.put(area, l);
            }
            l.add(lib);
            for (LP lp : lps) {
                if (lp.area.equals(area)) {
                    lp.pcs.firePropertyChange(LibraryProvider.PROP_LIBRARIES, null, null);
                }
            }
            return lib;
        }

        public void remove(LibraryImplementation library) throws IOException {
            for (Map.Entry<Area,List<LibraryImplementation>> entry : libs.entrySet()) {
                if (entry.getValue().remove(library)) {
                    for (LP lp : lps) {
                        if (lp.area.equals(entry.getKey())) {
                            lp.pcs.firePropertyChange(LibraryProvider.PROP_LIBRARIES, null, null);
                        }
                    }
                }
            }
        }

    }

    public static URL mkJar(String name) throws MalformedURLException {
        return new URL("jar:http://nowhere.net/" + name + "!/");
    }

}

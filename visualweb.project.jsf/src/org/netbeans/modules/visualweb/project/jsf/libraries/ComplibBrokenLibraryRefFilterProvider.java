/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.

Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Other names may be trademarks of their respective owners.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */

package org.netbeans.modules.visualweb.project.jsf.libraries;

import java.util.List;

import org.netbeans.api.project.Project;
import org.netbeans.modules.visualweb.complib.api.ComplibService;
import org.netbeans.modules.web.project.spi.BrokenLibraryRefFilter;
import org.netbeans.modules.web.project.spi.BrokenLibraryRefFilterProvider;
import org.openide.util.Lookup;

/**
 * Hook used to remove broken library references for complib project migration. See issue 110040.
 *
 * @author Edwin Goei
 */
public class ComplibBrokenLibraryRefFilterProvider implements BrokenLibraryRefFilterProvider {

    public static class BrokenLibRefFilter implements BrokenLibraryRefFilter {

        private List<String> legacyLibraryNames;

        private BrokenLibRefFilter(List<String> legacyLibraryNames) {
            this.legacyLibraryNames = legacyLibraryNames;
        }

        public boolean removeLibraryReference(String libraryName) {
            return legacyLibraryNames.contains(libraryName);
        }

    }

    public BrokenLibraryRefFilter createFilter(Project project) {
        ComplibService complibService = Lookup.getDefault().lookup(ComplibService.class);
        if (complibService == null) {
            return null;
        }

        List<String> legacyLibraryNames = complibService.getLibRefNamesToRemove(project);
        return new BrokenLibRefFilter(legacyLibraryNames);
    }
}

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

package org.netbeans.spi.project.support.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.spi.queries.SharabilityQueryImplementation;
import org.openide.util.WeakListeners;

/**
 * Standard impl of {@link SharabilityQueryImplementation}.
 * @author Jesse Glick
 */
final class SharabilityQueryImpl implements SharabilityQueryImplementation, AntProjectListener {
    
    private final AntProjectHelper h;
    private final String[] includes;
    private final String[] excludes;
    /** Absolute paths of directories or files to treat as sharable (except for the excludes). */
    private String[] includePaths;
    /** Absolute paths of directories or files to treat as not sharable. */
    private String[] excludePaths;
    
    SharabilityQueryImpl(AntProjectHelper h, String[] includes, String[] excludes) {
        this.h = h;
        this.includes = includes;
        this.excludes = excludes;
        computeFiles();
        h.addAntProjectListener((AntProjectListener)WeakListeners.create(AntProjectListener.class, this, h));
    }
    
    /** Compute the absolute paths which are and are not sharable. */
    private void computeFiles() {
        String[] _includePaths = computeFrom(includes);
        String[] _excludePaths = computeFrom(excludes);
        synchronized (this) {
            includePaths = _includePaths;
            excludePaths = _excludePaths;
        }
    }
    
    /** Compute a list of absolute paths based on some abstract names. */
    private String[] computeFrom(String[] list) {
        List/*<String>*/ result = new ArrayList(list.length);
        for (int i = 0; i < list.length; i++) {
            String val = h.evaluateString(list[i]);
            if (val != null) {
                File f = h.resolveFile(val);
                result.add(f.getAbsolutePath());
            }
        }
        // XXX should remove overlaps somehow
        return (String[])result.toArray(new String[result.size()]);
    }
    
    public synchronized int getSharability(File file) {
        String path = file.getAbsolutePath();
        if (contains(path, excludePaths, false)) {
            return SharabilityQuery.NOT_SHARABLE;
        }
        return contains(path, includePaths, false) ?
            (contains(path, excludePaths, true) ? SharabilityQuery.MIXED : SharabilityQuery.SHARABLE) :
            SharabilityQuery.UNKNOWN;
    }
    
    /**
     * Check whether a file path matches something in the supplied list.
     * @param a file path to test
     * @param list a list of file paths
     * @param reverse if true, check if the file is an ancestor of some item; if false,
     *                check if some item is an ancestor of the file
     * @return true if the file matches some item
     */
    private static boolean contains(String path, String[] list, boolean reverse) {
        for (int i = 0; i < list.length; i++) {
            if (path.equals(list[i])) {
                return true;
            } else {
                if (reverse ? list[i].startsWith(path + File.separatorChar) : path.startsWith(list[i] + File.separatorChar)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public void propertiesChanged(AntProjectEvent ev) {
        computeFiles();
    }
    
    public void configurationXmlChanged(AntProjectEvent ev) {
        // ignore
    }
    
}

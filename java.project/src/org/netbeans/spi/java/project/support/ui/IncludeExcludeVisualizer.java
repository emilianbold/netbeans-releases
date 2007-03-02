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

package org.netbeans.spi.java.project.support.ui;

import java.awt.EventQueue;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.project.support.ant.PathMatcher;
import org.openide.util.NbPreferences;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 * Utility permitting a user to easily see the effect of changing include
 * and exclude patterns on a source group (or several).
 * Intended for use in project creation wizards and project properties dialogs.
 * The exact appearance of the panel is not specified but it should
 * permit the user to see, and edit, the current set of includes and excludes;
 * and display the set of files included and excluded by the current pattern.
 * @see PathMatcher
 * @since org.netbeans.modules.java.project/1 1.12
 * @author Jesse Glick
 */
public class IncludeExcludeVisualizer {

    private File[] roots = {};
    private String includes = "**"; // NOI18N
    private String excludes = ""; // NOI18N
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>(1);
    private IncludeExcludeVisualizerPanel panel;
    private SortedSet<File> included = new TreeSet<File>();
    private SortedSet<File> excluded = new TreeSet<File>();
    private boolean busy = false;
    private boolean interrupted = false;
    private static final RequestProcessor RP = new RequestProcessor(IncludeExcludeVisualizer.class.getName());
    private final RequestProcessor.Task task = RP.create(new RecalculateTask());

    /**
     * Create a new visualizer.
     * Initially has no roots and includes anything (equivalent to
     * an include pattern of <samp>**</samp> and an empty exclude pattern).
     */
    public IncludeExcludeVisualizer() {}

    /**
     * Configure a set of root directories to which the includes and excludes apply.
     * @param roots a set of root directories to search
     * @throws IllegalArgumentException if roots contains a non-directory
     */
    public synchronized void setRoots(File[] roots) throws IllegalArgumentException {
        Parameters.notNull("roots", roots);
        for (File root : roots) {
            if (!root.isDirectory()) {
                throw new IllegalArgumentException(root.getAbsolutePath());
            }
        }
        this.roots = roots;
        recalculate();
    }

    /**
     * Get the current include pattern.
     * @return the current pattern (never null)
     */
    public synchronized String getIncludePattern() {
        return includes;
    }

    /**
     * Set the include pattern.
     * This does not fire a change event.
     * @param pattern the new pattern (never null)
     */
    public synchronized void setIncludePattern(String pattern) {
        Parameters.notNull("pattern", pattern);
        includes = pattern;
        updateIncludesExcludes();
        recalculate();
    }

    /**
     * Get the current exclude pattern.
     * @return the current pattern (never null)
     */
    public synchronized String getExcludePattern() {
        return excludes;
    }

    /**
     * Set the exclude pattern.
     * This does not fire a change event.
     * @param pattern the new pattern (never null)
     */
    public synchronized void setExcludePattern(String pattern) {
        Parameters.notNull("pattern", pattern);
        excludes = pattern;
        updateIncludesExcludes();
        recalculate();
    }

    private synchronized void updateIncludesExcludes() {
        if (panel != null) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    panel.setFields(includes, excludes);
                }
            });
        }
    }

    /**
     * Add a listener to changes made by the user in the includes or excludes.
     * @param l the listener
     */
    public synchronized void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    /**
     * Remove a change listener.
     * @param l the listener
     */
    public synchronized void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    /**
     * Can be called from IncludeExcludeVisualizerPanel.
     */
    synchronized void changedPatterns(String includes, String excludes) {
        this.includes = includes;
        this.excludes = excludes;
        recalculate();
        fireChange();
    }

    private synchronized void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener l : listeners) {
            l.stateChanged(e);
        }
    }

    /**
     * Get the associated visual panel.
     * @return a panel displaying this include and exclude information
     * @throws IllegalThreadStateException if not called in the event thread
     */
    public synchronized JComponent getVisualizerPanel() {
        if (!EventQueue.isDispatchThread()) {
            throw new IllegalThreadStateException("must be called in EQ");
        }
        if (panel == null) {
            panel = new IncludeExcludeVisualizerPanel(this);
            panel.setFields(includes, excludes);
            panel.setFiles(included.toArray(new File[included.size()]), excluded.toArray(new File[excluded.size()]), busy);
        }
        return panel;
    }

    private static final int DELAY = 200;
    private synchronized void recalculate() {
        interrupted = true;
        task.schedule(DELAY);
    }

    private void updateFiles() {
        assert Thread.holdsLock(this);
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                synchronized (IncludeExcludeVisualizer.this) {
                    if (panel != null) {
                        panel.setFiles(included.toArray(new File[included.size()]), excluded.toArray(new File[excluded.size()]), busy);
                    }
                }
            }
        });
    }

    private int scanCounter;
    private static final int GRANULARITY = 1000;
    private void scan(File d, String prefix, PathMatcher matcher, Pattern ignoredFiles) {
        String[] children = d.list();
        if (children == null) {
            return;
        }
        for (String child : children) {
            if (ignoredFiles.matcher(child).find()) {
                continue;
            }
            File f = new File(d, child);
            boolean dir = f.isDirectory();
            if (dir) {
                scan(f, prefix + child + "/", matcher, ignoredFiles); // NOI18N
            } else {
                synchronized (this) {
                    if (interrupted) {
                        return;
                    }
                    if (matcher.matches(prefix + child, false)) {
                        included.add(f);
                    } else {
                        excluded.add(f);
                    }
                    if (++scanCounter % GRANULARITY == 0) {
                        updateFiles();
                    }
                }
            }
        }
    }

    private final class RecalculateTask implements Runnable {

        // XXX #95974: VisibilityQuery only works on FileObject, and that would be too slow
        // copied from: org.netbeans.modules.masterfs.GlobalVisibilityQueryImpl
        final Pattern ignoredFiles = Pattern.compile(NbPreferences.root().node("/org/netbeans/core"). // NOI18N
                get("IgnoredFiles", "^(CVS|SCCS|vssver\\.scc|#.*#|%.*%|\\.(cvsignore|svn|DS_Store)|_svn)$|~$|^\\..*$")); // NOI18N

        public void run() {
            File[] _roots;
            String _includes, _excludes;
            synchronized (IncludeExcludeVisualizer.this) {
                busy = true;
                included.clear();
                excluded.clear();
                _roots = roots.clone();
                _includes = includes;
                _excludes = excludes;
                interrupted = false;
                updateFiles();
            }
            PathMatcher matcher = new PathMatcher(_includes, _excludes, null);
            for (File root : _roots) {
                scan(root, "", matcher, ignoredFiles);
            }
            synchronized (IncludeExcludeVisualizer.this) {
                busy = false;
                updateFiles();
            }
        }

    }

}

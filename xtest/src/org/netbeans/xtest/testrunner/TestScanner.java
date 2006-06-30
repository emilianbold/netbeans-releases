/*
 *
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
/*
 * TestScanner.java
 *
 * Created on April 25, 2001, 3:57 PM
 */

package org.netbeans.xtest.testrunner;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;
import java.util.LinkedList;
import java.util.StringTokenizer;
import org.netbeans.junit.Filter;

/**
 *
 * @author <a href="mailto:vitezslav.stejskal@czech.sun.com">Vitezslav Stejskal</a>
 * @version 1.0
 */
public class TestScanner {
    private final static int FILE_PATTERN = 0;
    private final static int TEST_PATTERN = 1;
    private final static int EXPFAIL_PATTERN = 2;
    private final static int PATTERNS_COUNT = 3;
    
    protected File basedir;
    protected String basePath;
    /**
     * The files that where found and matched at least one includes, and matched
     * no excludes.
     */
    protected Vector filesIncluded;
    /**
     * The directories that where found and matched at least one includes, and
     * matched no excludes.
     */
    protected Vector dirsIncluded;
    /**
     * The files that where found and matched at least one includes, and also
     * matched at least one excludes.
     */
    protected Vector filesExcluded;
    /**
     * The files that where found and matched at least one includes, and also
     * matched at least one excludes.
     */
    protected Vector dirsExcluded;
    /**
     * The files that where found and did not match any includes.
     */
    protected Vector filesNotIncluded;
    /**
     * The directories that where found and did not match any includes.
     */
    protected Vector dirsNotIncluded;
    /**
     * The patterns for the files that should be included.
     */
    protected String[][] includes;
    /**
     * The patterns for the files that should be excluded.
     */
    protected String[][] excludes;
    /**
     * Have the Vectors holding our results been built by a slow scan?
     */
    protected boolean haveSlowResults = false;
    
    /** Creates new TestScanner */
    public TestScanner() {
    }

    public void addDefaultExcludes() {
    }
    
    public void setBasedir(java.lang.String basedir) {
        setBasedir(new File(basedir.replace('/',File.separatorChar).replace('\\',File.separatorChar)));
    }
    
    public void setBasedir(java.io.File basedir) {
        this.basedir = basedir;
    }
    
    public java.io.File getBasedir() {
        return basedir;
    }

    public void setBasePath(java.lang.String basePath) {
        this.basePath = basePath;
    }

    public java.lang.String getBasePath() {
        return basePath;
    }
    
    public TestFilter[] getIncludedFiles() {
        return getFilesAndFilters(filesIncluded);
    }

    public TestFilter[] getIncludedDirectories() {
        return getFilesAndFilters(dirsIncluded);
    }
    
    public TestFilter[] getExcludedFiles() throws IOException {
        slowScan();
        return getFilesAndFilters(filesExcluded);
    }

    public TestFilter[] getExcludedDirectories() throws IOException  {
        slowScan();
        return getFilesAndFilters(dirsExcluded);
    }

    public TestFilter[] getNotIncludedFiles() throws IOException  {
        slowScan();
        return getFilesAndFilters(filesNotIncluded);
    }
    
    public TestFilter[] getNotIncludedDirectories() throws IOException  {
        slowScan();
        return getFilesAndFilters(dirsNotIncluded);
    }

    public void setIncludes(java.lang.String[][] includes) {
        this.includes = readPatterns(includes);
    }

    public void setExcludes(java.lang.String[][] excludes) {
        this.excludes = readPatterns(excludes);
    }

    public void scan() throws IOException {
        if (basedir == null && basePath == null) {
            throw new IllegalStateException("Neither basedir nor basepath are set.");
        }
        if (basedir != null ) {
            if (!basedir.exists()) {
                throw new IllegalStateException("basedir " + basedir.getAbsolutePath() + " does not exist");
            }
            if (!basedir.isDirectory()) {
                throw new IllegalStateException("basedir " + basedir.getAbsolutePath() + " is not a directory");
            }
        }
        if (includes == null) {
            // No includes supplied, so set it to 'matches all'
            includes = new String[1][PATTERNS_COUNT];
            includes[0][FILE_PATTERN] = "**";
            includes[0][TEST_PATTERN] = null;
            includes[0][EXPFAIL_PATTERN] = null;
        }
        if (excludes == null) {
            excludes = new String[0][0];
        }

        filesIncluded    = new Vector();
        filesNotIncluded = new Vector();
        filesExcluded    = new Vector();
        dirsIncluded     = new Vector();
        dirsNotIncluded  = new Vector();
        dirsExcluded     = new Vector();

        if (basedir != null) {
            scandir(basedir, "", true);
        }
        if (basePath != null) {
            StringTokenizer t = new StringTokenizer(basePath, File.pathSeparator);
            while(t.hasMoreTokens()) {
                File dir = new File(t.nextToken());
                if (!dir.exists()) {
                    throw new IllegalStateException("dir " + dir.getAbsolutePath() + " does not exist");
                }
                if (!dir.isDirectory()) {
                    throw new IllegalStateException("dir " + dir.getAbsolutePath() + " is not a directory");
                }
                scandir(dir, "", true);
            }
        }
    }
    
    protected void scandir(File dir, String vpath, boolean fast) throws IOException {
        String[] newfiles = dir.list(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                    if (name.endsWith(".class"))
                        return true;
                    if (new File(dir, name).isDirectory())
                        return true;
                    else
                        return false;
             }
         });

        if (newfiles == null) {
            /*
             * two reasons are mentioned in the API docs for File.list
             * (1) dir is not a directory. This is impossible as
             *     we wouldn't get here in this case.
             * (2) an IO error occurred (why doesn't it throw an exception 
             *     then???)
             */
            throw new IOException("Error scanning directory "
                                     + dir.getAbsolutePath());
        }

        for (int i = 0; i < newfiles.length; i++) {
            String name = vpath + newfiles[i];
            File   file = new File(dir, newfiles[i]);
            if (file.isDirectory()) {
                TestFilter tf = new TestFilter(name, null);
                
                if (isIncluded(name, tf)) {
                    if (!isExcluded(name, tf)) {
                        dirsIncluded.addElement(tf);
                        if (fast) {
                            scandir(file, name + File.separator, fast);
                        }
                    } else {
                        dirsExcluded.addElement(tf);
                    }
                } else {
                    dirsNotIncluded.addElement(tf);
                    if (fast && couldHoldIncluded(name)) {
                        scandir(file, name + File.separator, fast);
                    }
                }
                if (!fast) {
                    scandir(file, name + File.separator, fast);
                }
            } else if (file.isFile()) {
                Filter f = new Filter();
                TestFilter tf = new TestFilter(name, f);

                if (isIncluded(name, tf)) {
                    if (!isExcluded(name, tf)) {
                        filesIncluded.addElement(tf);
                    } else {
                        filesExcluded.addElement(tf);
                    }
                } else {
                    filesNotIncluded.addElement(tf);
                }
            }
        }
    }
    /**
     * Toplevel invocation for the scan.
     *
     * <p>Returns immediately if a slow scan has already been requested.
     */
    protected void slowScan() throws IOException {
        if (haveSlowResults) {
            return;
        }

        String[] excl = new String[dirsExcluded.size()];
        dirsExcluded.copyInto(excl);

        String[] notIncl = new String[dirsNotIncluded.size()];
        dirsNotIncluded.copyInto(notIncl);

        for (int i=0; i<excl.length; i++) {
            scandir(new File(basedir, excl[i]), excl[i] + File.separator, false);
        }
        
        for (int i=0; i<notIncl.length; i++) {
            if (!couldHoldIncluded(notIncl[i])) {
                scandir(new File(basedir, notIncl[i]), 
                        notIncl[i] + File.separator, false);
            }
        }

        haveSlowResults  = true;
    }
    /**
     * @param name file/directory name being scanned
     * @param filter the filter for test cases, can be null in case of directory
     * @return -1 means file is not included, >0 means position in include list
     */
    protected boolean isIncluded(String name, TestFilter filter) {
        LinkedList testPatterns = new LinkedList();
        int position = -1;
        
        for (int i = 0; i < includes.length; i++) {
            if (PatternUtilities.matchPath(includes[i][FILE_PATTERN], name)) {
                testPatterns.add(new Filter.IncludeExclude(includes[i][TEST_PATTERN],includes[i][EXPFAIL_PATTERN]));
                if (position == -1)
                    position = i;
            }
        }
        
        if (0 < testPatterns.size() && null != filter.getFilter()) {
            Filter.IncludeExclude [] s = (Filter.IncludeExclude[])testPatterns.toArray(new Filter.IncludeExclude[] {});
            filter.getFilter().setIncludes(s);
            filter.setPosition(position);
        }
        
        return 0 < testPatterns.size();
    }
    /**
     * @param name file/directory name being scanned
     * @param filter the filter for test cases, can be null in case of directory
     */
    protected boolean isExcluded(String name, TestFilter filter) {
        LinkedList testPatterns = new LinkedList();
        boolean excluded = false;
        
        for (int i = 0; i < excludes.length; i++) {
            if (PatternUtilities.matchPath(excludes[i][FILE_PATTERN], name)) {
                if (null == excludes[i][TEST_PATTERN] ||
                    0 == excludes[i][TEST_PATTERN].length()
                    || excludes[i][TEST_PATTERN].equals("*")) {
                    excluded = true;
                }
                testPatterns.add(new Filter.IncludeExclude(excludes[i][TEST_PATTERN], excludes[i][EXPFAIL_PATTERN]));
            }
        }
        
        if (0 < testPatterns.size() && null != filter.getFilter()) {
            Filter.IncludeExclude [] s = (Filter.IncludeExclude[])testPatterns.toArray(new Filter.IncludeExclude[] {});
            filter.getFilter().setExcludes(s);
        }
        return excluded;
    }
    /**
     * Tests whether a name matches the start of at least one include pattern.
     *
     * @param name the name to match
     * @return <code>true</code> when the name matches against at least one
     *         include file pattern, <code>false</code> otherwise.
     */
    protected boolean couldHoldIncluded(String name) {
        for (int i = 0; i < includes.length; i++) {
            if (PatternUtilities.matchPatternStart(includes[i][FILE_PATTERN], name)) {
                return true;
            }
        }
        return false;
    }
    
    private String[][] readPatterns(String [][] patterns) {
        String[][] pttrn;
        if (patterns == null) {
            pttrn = null;
        } else {
            pttrn = new String[patterns.length][PATTERNS_COUNT];
            for (int i = 0; i < patterns.length; i++) {
                String pattern;
                String filter;
                int pos;
                pattern = patterns[i][0].replace('/', File.separatorChar).replace('\\', File.separatorChar);
                if (pattern.endsWith(File.separator)) {
                    pattern += "**";
                }
                if ((-1 != (pos = pattern.indexOf(".class")) ||
                    -1 != (pos = pattern.indexOf(".java"))) &&
                    -1 != (pos = pattern.indexOf(File.separatorChar, pos)) &&
                    !pattern.endsWith("**")) {
                    filter = pattern.substring(pos + 1);
                    pattern = pattern.substring(0, pos);
                }
                else {
                    filter = "*";
                }
                
                pttrn[i][FILE_PATTERN] = pattern;
                pttrn[i][TEST_PATTERN] = filter;
                pttrn[i][EXPFAIL_PATTERN] = patterns[i][1];
            }
        }
        
        return pttrn;
    }

    private TestFilter[] getFilesAndFilters(Vector v) {
        TestFilter[] entries = (TestFilter[])v.toArray(new TestFilter[v.size()]);
        Arrays.sort(entries);
        return entries;
    }
}

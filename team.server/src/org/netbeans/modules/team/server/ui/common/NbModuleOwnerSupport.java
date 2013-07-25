/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.team.server.ui.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Union2;

/**
 * Generic mechanism for reading and finding mappings between paths
 * and auxiliary data. The general use that the client has some
 * <em>absolute</em> path to a&nbsp;file or directory and needs to find data
 * assigned to that path.
 * <p>
 * The mechanism for finding mappings is bound only by the contents of disks
 * (or other storage systems) mounted to the local filesystem(s)&nbsp;&ndash;
 * it does not use the concept of projects and files belonging to them.
 * <p>
 * The mappings (path&thinsp;&rarr;&thinsp;data) are stored in text files,
 * referred to as <em>mapping files</em> in the rest of this documentation.
 * There can exist multiple mapping files of different names&nbsp;&ndash;
 * each name for a&nbsp;different purpose (e.g. different kind of data).
 * When the user asks for data assigned to a&nbsp;certain path, they must
 * specify both the path and the name of the mapping file.
 *
 * <h4>How it works</h4>
 *
 * When the user asks for data assigned to a&nbsp;certain path, the mechanism
 * first finds the mapping file. At first, the name of file given by the
 * specified absolute path is compared with the specified name of the mapping
 * file. If the names match and the specified absolute path denotes an existing
 * plain file (not folder), the file given by the specified absolute path is
 * used as the mapping file. Otherwise, the mechanism tries to locate the
 * mapping file in the parent of the absolute path, then in the parent of that
 * parent and so on, up to the root of the directory hierarchy. As soon as one
 * mapping file is found, the search is stopped. (This means that mapping files
 * deeper in the directory structure <em>completely</em> override mapping files
 * that are located higher in the directory structure.) If no mapping file
 * is found, then the result of the query is {@code null}.
 * <p>
 * When the file is found, its content is loaded and parsed, or taken from
 * the cache if it has already been loaded and parsed in the past and not
 * removed from the cache since then. (The content of an already loaded and
 * parsed file can be removed from the cache if it is edged out by other,
 * more recently used mapping files, or in the case of heap size shortage.)
 * The mapping file is read line-by-line and its content stored into
 * a&nbsp;data structure, which is than stored in the cache. Lines not
 * conforming to the syntax rules (see below) are silently ignored, possibly
 * just a&nbsp;note may be written to the log file.
 * <p>
 * The data structure is then searched for an entry matching the specified
 * absolute path. If such an entry is found, the data stored in that entry
 * is returned, otherwise {@code null} is returned.
 * <p>
 * The returned data (if any) is passed in the form of instance of class
 * {@link NbModuleOwnerSupport.OwnerInfo}. This class is essentially
 * a&nbsp;wrapper around a&nbsp;list of {@code String}s.
 * <p>
 * The assumption is that most clients will be interested only in the
 * first string. So there is a&nbsp;dedicated method for getting the first
 * string:
 * {@link NbModuleOwnerSupport.OwnerInfo#getOwner OwnerInfo.getOwner()}.
 * The other {@code String}s (if any) can be obtained using method
 * {@link NbModuleOwnerSupport.OwnerInfo#getExtraData OwnerInfo.getExtraData()}.
 * There is also a short-cut method that allows to get the first string
 * without first obtaining an instance of {@code OwnerInfo}:
 * {@link NbModuleOwnerSupport#getOwner NbModuleOwnerSupport.getOwner()}.
 *
 * <h4>Syntax of mapping files</h4>
 *
 * A mapping file is a text file consisting of a list of mappings, one mapping
 * per line. The simpliest expression for definition of a mapping is
 * <blockquote>
 * <span style="font-style: oblique">relative path</span>
 * <tt>=</tt>
 * <span style="font-style: oblique">assigned data</span>
 * </blockquote>
 * The path on the left side must be relative and is considered
 * <em>relative to the folder that contains the mapping file</em>.
 * Only plain slash ({@code '/'}) is considered a directory separator,
 * no matter what is the target platform. Backslash character ({@code '\'})
 * is reserved for escaping special characters (see below).
 * <p>
 * There may be any number of white-space characters (spaces, tabs)
 * around the equal-sign ({@code '='}). These white-space characters are
 * ignored. If a white-space character at the end of a path is not to be
 * ignored, it must be escaped with a backslash. Similarly, equal-signs
 * must be escaped should they be considered part of the path.
 * <p>
 * The right side is a slash-separated list of strings. If a slash
 * character should not be considered a separator, it must be escaped
 * by a backslash. Similarly, if a backslash should be part of a string,
 * it must be escaped by another backslash. Slash and backslash are the only
 * characters that must be escaped in the right side of the expression,
 * other characters (including spaces, tabs and equal-signs) may be left
 * un-escaped.
 * <p>
 * Empty lines and lines starting with the hash-character ({@code '#'})
 * are silently ignored.
 *
 * <h5>Path patterns</h5>
 *
 * It is allowed to use simple patterns in the left side of a mapping
 * expression. Regular expressions are not supported, just folder/file-name
 * prefixes are supported. Prefix is a string followed by a star-sign
 * ({@code '*'}):
 * <blockquote>
 * <span style="font-style: oblique">prefix</span><tt>*
 * =
 * </tt><span style="font-style: oblique">value</span>
 * </blockquote>
 * Each part of the pattern on the left side can be expressed as a prefix,
 * e.&thinsp;g.:
 * <blockquote>
 * <span style="font-style: oblique">prefixA</span><tt>&#42;/</tt><span style="font-style: oblique">prefixB</span><tt>*
 * =
 * </tt><span style="font-style: oblique">value</span>
 * </blockquote>
 * Each part of the pattern on the left side is matched against the
 * corresponding part of the path passed to method
 * {@link #getOwnerInfo getOwnerInfo()} or {@link #getOwner getOwner()}.
 * For example, if the mapping file is located in directory
 * <tt>/foo</tt>, it contains expression <tt>bar&#42;/baz=fred</tt> and the user
 * asks for the data assigned to directory <tt>foo/barge/baz</tt>, then
 * directory name <tt>barge</tt> is matched against pattern <tt>bar*</tt>
 * and name <tt>baz</tt> is matched against pattern <tt>baz</tt>.
 *
 * <h5>Resolution of pattern conflicts</h5>
 *
 * If there are multiple patterns matching the corresponding part of the path,
 * then the most specific pattern part is used. The following rules are applied
 * (in this order) when comparing the level of specificity:
 * <ol>
 *     <li>non-prefix patterns are more specific than prefix patterns</li>
 *     <li>longer patterns are more specific than shorter patterns</li>
 * </ol>
 *
 * <h5>Scope of a mapping</h5>
 *
 * Each mapping expression effectively maps not only the path/directory
 * explicitly specified by the left side of the expression but also all
 * its direct and indirect children (files and subdirectories), excluding those
 * that are mapped with a more specific mapping expression. The words &quot;more
 * specific expression&quot; refer to a more specific path pattern on the left
 * side of the mapping expression.
 * <p>
 * Path pattern <var>A</var> is considered to be more specific than
 * path pattern <var>B</var> if the first part of pattern <var>A</var>
 * is more specific than the first part of pattern <var>B</var>.
 * If first parts of the path patterns are equally specific, second parts
 * of the path patterns are compared, etc. If all the compared parts are
 * equally specific and one path pattern consists of more pattern parts than
 * the other pattern, than the path pattern consisting of more pattern parts
 * is considered more specific.
 *
 * <h4>Examples</h4>
 *
 * <h5>API usage</h5>
 *
 * <h6>Example 1</h6>
 *
 * User asks for a name assigned to path &quot;foo/bar/baz&quot; and specifies
 * that the mappings should be taken from mapping file &quot;.names&quot;.
 * This is probably the most common use case and allows to use the short-cut
 * method {@link #getOwner() NbModuleOwnerSupport.getOwner()}:
 * <blockquote>
 *     <code>String name
 *           = NbModuleOwnerSupport.getInstance().getOwner(".names",
 *             "foo/bar/baz")</code>:
 * </blockquote>
 *
 * <h6>Example 2</h6>
 *
 * User asks for names of a component, subcomponent and a subsubcomponent
 * assigned to path &quot;xxx/yyy&quot;:
 * <blockquote>
 * <pre><code>OwnerInfo info = NbModuleOwnerSupport.getInstance().getOwnerInfo(".components", "xxx/yyy");
 *String component       = info.getOwner();
 *List&lt;String&gt; extraData = info.getExtraData();
 *String subcomponent    = extraData.size() &gt;= 1 ? extraData.get(0) : null;
 *String subsubcomponent = extraData.size() &gt;= 2 ? extraData.get(1) : null;</code></pre>
 * </blockquote>
 *
 * <h5>Scope</h5>
 *
 * <h6>Example 1</h6>
 *
 * Mapping file &quot;/foo/.names&quot; contains mapping
 *
 * <blockquote>
 * <code>bar = Lucy</code>
 * </blockquote>
 *
 * and the user asks for data assigned to file
 * &quot;/foo/bar/baz/SomeFile.txt&quot;. The answer is &quot;Lucy&quot;
 * because the mapping applies not only for &quot;/foo/bar&quot; but for the
 * whole subtree below &quot;/foo/bar/&quot;.
 *
 * <h6>Example 2</h6>
 *
 * Mapping file &quot;/foo/.names&quot; contains mapping
 *
 * <blockquote>
 * <pre><code>bar = Lucy
 *bar/baz = Alice
 *bar/boo = Paul</code></pre>
 * </blockquote>
 *
 * Now, if the user asks for data assigned to file
 * &quot;/foo/bar/baz/SomeFile.txt&quot;, the answer would be &quot;Alice&quot;
 * because path pattern <tt>bar/baz</tt> is more specific than pattern
 * <tt>bar</tt> and the more specific mapping overrides the less specific one.
 * <p>
 * If the user asks for data assigned to file (or folder)
 * &quot;/foo/bar/Data.dat&quot;, they will get answer &quot;Lucy&quot; as
 * <tt>bar</tt> is the only matching pattern and it is not overridden by any
 * more specific matching pattern.
 *
 * <h5>Pattern conflicts</h5>
 *
 * <h6>Example 1</h6>
 *
 * A mapping file contains the following mappings:
 *
 * <blockquote>
 * <pre><code>bar/baz = John
 *bar/baz* = Alice</code></pre>
 * </blockquote>
 *
 * If the user asks for data assigned to path &quot;foo/bar/baz&quot;,
 * they would get &quot;John&quot; because &quot;baz&quot; is more specific
 * than &quot;baz*&quot;.
 *
 * <h6>Example 2</h6>
 *
 * A mapping file contains the following mappings:
 *
 * <blockquote>
 * <pre><code>bar&#42;/baz = John
 *bar/baz* = Alice</code></pre>
 * </blockquote>
 *
 * Now, if the user asks for data assigned to path &quot;foo/bar/baz&quot;,
 * they would get &quot;Alice&quot; because &quot;bar&quot; is more specific
 * than &quot;bar*&quot;.
 *
 * <h6>Example 3</h6>
 *
 * A mapping file contains the following mappings:
 *
 * <blockquote>
 * <pre><code>bar/baz* = John
 *bar/baza* = Alice</code></pre>
 * </blockquote>
 *
 * Now, if the user asks for data assigned to path &quot;foo/bar/bazaar&quot;,
 * they would get &quot;Alice&quot; because &quot;baza*&quot; is more specific
 * than &quot;baz*&quot;.
 *
 * <h6>Example 4</h6>
 *
 * A mapping file contains the following mappings:
 * 
 * <blockquote>
 * <pre><code>bar/baz* = John
 *bar/baz&#42;/thing = Alice</code></pre>
 * </blockquote>
 *
 * If the user asks for data assigned to path &quot;foo/bar/baz&quot;,
 * they would get &quot;John&quot; because pattern
 * &quot;bar/baz&#42;/thing&quot; does not match the path.
 *
 * <h6>Example 5</h6>
 *
 * A mapping file contains the following mappings:
 *
 * <blockquote>
 * <pre><code>bar = John
 *bar/baz* = </code></pre>
 * </blockquote>
 *
 * If the user asks for data assigned to path &quot;foo/bar/baz&quot;,
 * they would get {@code null} because more specific pattern
 * &quot;bar/baz*&quot; overrides the assignment &quot;bar = John&quot;.
 *
 * @author Marian Petras
 */
public abstract class NbModuleOwnerSupport {

    public static String NB_BUGZILLA_CONFIG = ".nbbugzilla-components";         // NOI18N

    private static NbModuleOwnerSupport instance;

    protected NbModuleOwnerSupport() { }

    public static NbModuleOwnerSupport getInstance() {
        if (instance == null) {
            instance = Lookup.getDefault().lookup(NbModuleOwnerSupport.class);
            if (instance == null) {
                instance = new DefaultImpl();
            }
        }
        return instance;
    }

    /**
     * Find the first string of all strings mapped to the given (absolute) path.
     * This is a convenient method for getting the first part of data
     * assigned to the path without first obtaining instance
     * of {@link NbModuleOwnerSupport.OwnerInfo}.
     * <p>
     * This is an equivalent of
     * <blockquote><pre><code>NbModuleOwnerSupport.OwnerInfo ownerInfo = getOwnerInfo(configFileName, path);
     *if (ownerInfo != null) {
     *    return ownerInfo.getOwner();
     *} else {
     *    return null;
     *}</code></pre></blockquote>
     * 
     * @param  mapping file to read the mapping data from
     * @param  path  absolute path for which the data should be found for
     * @return  object holding the data assigned to the given path;
     *          or {@code null} if no mapping file of the specified name
     *          has been found, or if the mapping file did not contain
     *          any mapping for the given path
     * @exception  java.lang.IllegalArgumentException
     *             if either the file name or the path was {@code null}
     *             or if the path was not absolute
     */
    public final String getOwner(String configFileName, File path) {
        OwnerInfo ownerInfo = getOwnerInfo(configFileName, path);
        return (ownerInfo != null) ? ownerInfo.getOwner() : null;
    }

    /**
     * Finds data mapped to the given (absolute) path.
     *
     * @param  mapping file to read the mapping data from
     * @param  path  absolute path for which the data should be found for
     * @return  object holding the data assigned to the given path;
     *          or {@code null} if no mapping file of the specified name
     *          has been found, or if the mapping file did not contain
     *          any mapping for the given path
     * @exception  java.lang.IllegalArgumentException
     *             if either the file name or the path was {@code null}
     *             or if the path was not absolute
     */
    public final OwnerInfo getOwnerInfo(String configFileName, File path) {
        checkConfigFileName(configFileName);
        checkPath(path);

        try {
            return getOwnerImpl(configFileName, path);
        } catch (Throwable t) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, t);
            return null;
        }
    }

    /**
     * Finds data mapped to the given Project in a netbeans clone
     *
     * @param  project the {@link Project} for which the data should be found for
     * @return  object holding the data assigned to the given project;
     *          or {@code null} if no mapping file of the specified name
     *          has been found, or if the mapping file did not contain
     *          any mapping for the given path
     * @exception  java.lang.IllegalArgumentException
     *             if either the file name or the path was {@code null}
     *             or if the path was not absolute
     */
    public OwnerInfo getOwnerInfo(Project project) {
        FileObject fileObject = project.getProjectDirectory();
        return getOwnerInfo(fileObject);
    }

    /**
     * Finds data mapped to the given file from a netbeans clone
     *
     * @param  fileObject {@link FileObject} the project for which the data should be found for
     * @return  object holding the data assigned to the given file;
     *          or {@code null} if no mapping file of the specified name
     *          has been found, or if the mapping file did not contain
     *          any mapping for the given path
     * @exception  java.lang.IllegalArgumentException
     *             if either the file name or the path was {@code null}
     *             or if the path was not absolute
     */
    public OwnerInfo getOwnerInfo(FileObject fileObject) {
        if (fileObject == null) {
            return null;
        }
        File file = org.openide.filesystems.FileUtil.toFile(fileObject);
        if (file == null) {
            return null;
        }
        return NbModuleOwnerSupport.getInstance().getOwnerInfo(NbModuleOwnerSupport.NB_BUGZILLA_CONFIG, file);
    }

    /**
     * Finds data mapped to the given node reporsenting a file in a netbeans clone
     *
     * @param  node {@link Node} the project for which the data should be found for
     * @return  object holding the data assigned to a file represented by the given node;
     *          or {@code null} if no mapping file of the specified name
     *          has been found, or if the mapping file did not contain
     *          any mapping for the given path
     * @exception  java.lang.IllegalArgumentException
     *             if either the file name or the path was {@code null}
     *             or if the path was not absolute
     */
    public OwnerInfo getOwnerInfo(Node node) {
        final Lookup nodeLookup = node.getLookup();

        Project project = nodeLookup.lookup(Project.class);
        if (project != null) {
            return getOwnerInfo(project);
        }

        DataObject dataObj = nodeLookup.lookup(DataObject.class);
        if (dataObj != null) {
            return getOwnerInfo(dataObj);
        }
        return null;
    }


    private OwnerInfo getOwnerInfo(DataObject dataObj) {
        FileObject fileObj = dataObj.getPrimaryFile();
        if (fileObj == null) {
            return null;
        }

        Project project = FileOwnerQuery.getOwner(fileObj);
        if (project != null) {
            return getOwnerInfo(project);
        }
        return getOwnerInfo(fileObj);
    }

    private void checkConfigFileName(String configFileName)
                                            throws IllegalArgumentException {
        if (configFileName == null) {
            throw new IllegalArgumentException(
                    "configFileName is <null>");                        //NOI18N
        }
        if ((configFileName.length() == 0)
                || configFileName.equals(".")                           //NOI18N
                || configFileName.equals("..")                          //NOI18N
                || configFileName.lastIndexOf(File.separatorChar) != -1) {
            throw new IllegalArgumentException(
                    "Illegal name of configuration file: \""            //NOI18N
                    + configFileName + '"');
        }
    }

    private void checkPath(File path) throws IllegalArgumentException {
        if (path == null) {
            throw new IllegalArgumentException(
                    "path is <null>");                                  //NOI18N
        }
        if (!path.isAbsolute()) {
            throw new IllegalArgumentException(
                    "Absolute path is required - was: " + path);        //NOI18N
        }
    }

    protected abstract OwnerInfo getOwnerImpl(String configFileName,
                                              File absolutePath);

    protected abstract ConfigData getData(File configFile);

    private static class DefaultImpl extends NbModuleOwnerSupport {

        @Override
        protected OwnerInfo getOwnerImpl(String configFileName,
                                         File absolutePath) {

            File configFile = null;
            String relativePath;

            /* Find the configuration file: */
            final String fileName = absolutePath.getName();
            if (fileName.equals(configFileName) && absolutePath.isFile()) {
                configFile = absolutePath;
                relativePath = configFileName;
            } else {
                List<String> reversePathElements = new ArrayList<String>(15);

                File parent;
                File lastCheckedPath = absolutePath;
                while ((parent = lastCheckedPath.getParentFile()) != null) {
                    File fileToCheck = new File(parent, configFileName);
                    if (fileToCheck.isFile()) {
                        configFile = fileToCheck;
                        break;
                    }
                    reversePathElements.add(parent.getName());
                    lastCheckedPath = parent;
                }
                if (configFile == null) {
                    return null;
                }

                if (reversePathElements.isEmpty()) {
                    relativePath = fileName;
                } else {
                    StringBuilder buf = new StringBuilder(100);
                    for (int i = reversePathElements.size() - 1; i >= 0; i--) {
                        buf.append(reversePathElements.get(i)).append('/');
                    }
                    buf.append(fileName);
                    relativePath = buf.toString();
                }
            }

            /* load data (if not loaded yet) from the configuration file: */
            ConfigData parsedData = getData(configFile);
            if (parsedData == null) {
                return null;
            }

            /* find the requested information in the loaded data: */
            return parsedData.getMatchingInfo(relativePath);
        }

        private final Object cacheLock = new Object();
        private final ConfigFileCache dataCache = new ConfigFileCache();

        protected ConfigData getData(File configFile) {
            synchronized (cacheLock) {
                Reference<ConfigData> ref = dataCache.get(configFile);
                ConfigData data = (ref != null) ? ref.get() : null;
                if (data == null) {
                    data = ConfigData.load(configFile);
                    dataCache.put(configFile, new SoftReference<ConfigData>(data));
                }
                return data;
            }
        }

        private static final class ConfigFileCache
                             extends LinkedHashMap<File,Reference<ConfigData>> {
            private static final int MAX_SIZE = 10;
            ConfigFileCache() {
                super(16, 0.75f, true);     //access order
            }
            @Override
            protected boolean removeEldestEntry(
                                    Entry<File,Reference<ConfigData>> eldest) {
                return size() > MAX_SIZE;
            }
        }

    }

    public static class ConfigData {

        private static final Logger LOG = Logger.getLogger(
                                          ConfigData.class.getCanonicalName());
        private static final int TOP_NODES_COUNT = 26;

        private final Union2<List<Node>,Node[]> topNodes[] = new Union2[TOP_NODES_COUNT];

        public static ConfigData load(File dataFile) {
            if (dataFile == null) {
                throw new IllegalArgumentException(
                        "dataFile is <null>");                          //NOI18N
            }

            ConfigData data = new ConfigData();
            try {
                data.loadData(dataFile);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, ex);
            }
            return data;
        }

        protected ConfigData() { }

        protected final void loadData(File dataFile) throws IOException {
            cleanData();

            final PatternParser parser = new PatternParser();
            int lineNum = 0;
            try {
                for (String line : getConfigFileLines(dataFile)) {
                    processPatternFileLine(dataFile, line, ++lineNum, parser);
                }
            } finally {
                processNodes();
                parser.reset();
            }
        }

        private void cleanData() {
            Arrays.fill(topNodes, null);
        }

        protected Iterable<String> getConfigFileLines(File dataFile)
                                                            throws IOException {
            return new BufferedReaderIterator(
                        new BufferedReader(
                                new InputStreamReader(
                                        new FileInputStream(dataFile))));
        }

        private void processPatternFileLine(File dataFile,
                                            String line, int lineNum,
                                            PatternParser parser) {
            if (line.length() == 0) {
                return;     //silently ignore empty lines
            }

            if (line.charAt(0) == '#') {
                return;     //skip comment lines
            }

            List<String> patternPath = parser.parsePattern(line);
            if (patternPath == null) {
                ParsingFailure failure = parser.getFailure();
                if ((failure != ParsingFailure.EMPTY_LINE)
                        && LOG.isLoggable(Level.INFO)) {
                    StringBuilder msg = new StringBuilder(100);
                    msg.append("syntax error in mapping file ")         //NOI18N
                       .append(dataFile)
                       .append(" at line ").append(lineNum)             //NOI18N
                       .append(": ").append(failure.getDescription());  //NOI18N
                    if (failure == ParsingFailure.SYNTAX_ERROR) {
                        int column = parser.getSyntaxErrorPosition();
                        if (column != -1) {
                            msg.append(" at column ").append(column);   //NOI18N
                        }
                    }
                    LOG.info(msg.toString());
                }
                return;
            }

            String infoString = line.substring(parser.getSeparatorPosition() + 1);
            OwnerInfo info = OwnerInfo.parseSpec(infoString);

            processPatternData(patternPath, info);
        }

        private void processPatternData(List<String> pattern,
                                        OwnerInfo info) {
            String firstPatternPart = pattern.get(0);
            if (firstPatternPart.equals("*")) {                         //NOI18N
                for (int i = 0; i < TOP_NODES_COUNT; i++) {
                    processPatternData(i, pattern, info);
                }
            } else {
                int topNodeIndex = getTopNodeIndex(firstPatternPart);
                processPatternData(topNodeIndex, pattern, info);
            }
        }

        private void processPatternData(int topNodeIndex,
                                        List<String> pattern,
                                        OwnerInfo info) {
            Union2<List<Node>,Node[]> topNodeUnion = topNodes[topNodeIndex];
            if (topNodeUnion == null) {
                List<Node> newList = new ArrayList<Node>(10);
                topNodes[topNodeIndex] = Union2.<List<Node>,Node[]>createFirst(newList);
                newList.add(createNode(pattern, 0, info));
            } else {
                findNode(topNodeUnion.first(), pattern, 0, info);
            }
        }

        private Node findNode(List<Node> nodes,
                              List<String> pattern,
                              int patternPartIndex,
                              OwnerInfo info) {
            boolean isPrefix = isPrefixPattern(pattern.get(patternPartIndex));
            return isPrefix
                   ? findPrefixNode(nodes, pattern, patternPartIndex, info)
                   : findNonPrefixNode(nodes, pattern, patternPartIndex, info);
        }

        private Node findPrefixNode(List<Node> nodes,
                                    List<String> pattern,
                                    int patternPartIndex,
                                    OwnerInfo info) {
            ListIterator<Node> it = nodes.listIterator();
            Node node = it.next();

            while (!node.isPrefix) {
                if (!it.hasNext()) {
                    /* all nodes are non-prefix */
                    nodes.add(node = createNode(pattern,
                                                patternPartIndex,
                                                info));
                    return node;
                }
                node = it.next();
            }

            /* 'node' now points to the first prefix node */

            final String patternPart = pattern.get(patternPartIndex);
            final int length = patternPart.length();

            while (node.name.length() > length) {
                if (!it.hasNext()) {
                    /* all prefixes are longer than the new one */
                    nodes.add(node = createNode(pattern,
                                                patternPartIndex,
                                                info));
                    return node;
                }
                node = it.next();
            }

            /*
             * 'node' now points to the first prefix node whose length
             * is not greater than the length of the new node's prefix
             */

            String nonPrefixPart = patternPart.substring(0, length - 1);
            while (node.name.length() == length) {
                if (node.name.equals(nonPrefixPart)) {
                    /* FOUND! */
                    if (patternPartIndex == (pattern.size() - 1)) {
                        return node;
                    } else {
                        Node childNode;
                        Union2<List<Node>,Node[]> childrenUnion = node.children;
                        if (childrenUnion != null) {
                            List<Node> children = childrenUnion.first();
                            childNode = findNode(children,
                                                 pattern,
                                                 patternPartIndex + 1,
                                                 info);
                        } else {
                            childNode = createNode(pattern,
                                                   patternPartIndex + 1,
                                                   info);
                            node.addChild(childNode);
                        }
                        return childNode;
                    }
                }
                if (!it.hasNext()) {
                    /* there is no prefix node with a shorter prefix */
                    nodes.add(node = createNode(pattern,
                                                patternPartIndex,
                                                info));
                    return node;
                }
                node = it.next();
            }

            /*
             * 'node' now points to the first prefix node whose length
             * is smaller than the length of the new node's prefix
             */

            nodes.add(it.previousIndex(),
                      node = createNode(pattern,
                                        patternPartIndex,
                                        info));
            return node;
        }

        private Node findNonPrefixNode(List<Node> nodes,
                                       List<String> pattern,
                                       int patternPartIndex,
                                       OwnerInfo info) {
            final String patternPart = pattern.get(patternPartIndex);
            final int length = patternPart.length();

            ListIterator<Node> it = nodes.listIterator();
            Node node = it.next();

            while (!node.isPrefix && (node.name.length() < length)) {
                if (!it.hasNext()) {
                    /*
                     * there is no non-prefix node having name as least
                     * as long as the name of the passed node
                     */
                    nodes.add(node = createNode(pattern,
                                                patternPartIndex,
                                                info));
                    return node;
                }
                node = it.next();
            }

            /*
             * 'node' now points to the first prefix node or to the first
             * non-prefix node whose name is at least as long as the name of
             * the passed node
             */

            while (!node.isPrefix && (node.name.length() == length)) {
                if (node.name.equals(patternPart)) {
                    /* FOUND! */
                    if (patternPartIndex == (pattern.size() - 1)) {
                        return node;
                    } else {
                        Node childNode;
                        Union2<List<Node>,Node[]> childrenUnion = node.children;
                        if (childrenUnion != null) {
                            List<Node> children = childrenUnion.first();
                            childNode = findNode(children,
                                                 pattern,
                                                 patternPartIndex + 1,
                                                 info);
                        } else {
                            childNode = createNode(pattern,
                                                   patternPartIndex + 1,
                                                   info);
                            node.addChild(childNode);
                        }
                        return childNode;
                    }
                }
                if (!it.hasNext()) {
                    /*
                     * there is no non-prefix node having name at least
                     * as long as the given node
                     */
                    nodes.add(node = createNode(pattern,
                                                patternPartIndex,
                                                info));
                    return node;
                }
                node = it.next();
            }

            /*
             * 'node' now points to the first prefix node or to the first
             * non-prefix node whose name is longer than the name of
             * the passed node
             */

            nodes.add(it.previousIndex(),
                      node = createNode(pattern,
                                        patternPartIndex,
                                        info));
            return node;
        }

        private Node createNode(List<String> pattern,
                                int patternPartIndex,
                                OwnerInfo info) {
            Node result;
            if (patternPartIndex == (pattern.size() - 1)) {
                result = new Node(pattern.get(patternPartIndex), info);
            } else {
                result = new Node(pattern.get(patternPartIndex), null);
                result.addChild(
                        createNode(pattern, patternPartIndex + 1, info));
            }
            return result;
        }

        private void processNodes() {
            processNodes(topNodes);
        }

        private static void processNodes(Union2<List<Node>,Node[]> unionNodes[]) {
            for (int i = 0; i < unionNodes.length; i++) {
                Union2<List<Node>,Node[]> node = unionNodes[i];
                if (node == null) {
                    continue;
                }

                List<Node> nodeList = node.first();
                assert !nodeList.isEmpty();
                Node[] nodesArray = node.first().toArray(
                                                new Node[nodeList.size()]);
                for (Node nodeArrayElem : nodesArray) {
                    nodeArrayElem.processChildren();
                }
                unionNodes[i] = Union2.<List<Node>,Node[]>createSecond(nodesArray);
                assert unionNodes[i].second().length != 0;
            }
        }

        public OwnerInfo getMatchingInfo(String relativePath) {
            String firstPartOfPath = getFirstPartOfPath(relativePath);
            int topNodeIndex = getTopNodeIndex(firstPartOfPath);

            Union2<List<Node>,Node[]> topNodeUnion = topNodes[topNodeIndex];
            if (topNodeUnion == null) {
                return null;
            }

            Node[] topNode = topNodeUnion.second();
            if (topNode == null) {
                assert false;
                return null;
            }

            OwnerInfo ownerInfo = findInfo(relativePath,
                                           firstPartOfPath,
                                           topNode);
            if (ownerInfo != null && ownerInfo != OwnerInfo.NULL_OWNER_INFO) {
                return ownerInfo;
            } else {
                return null;
            }
        }

        private OwnerInfo findInfo(String relativePath,
                                   String firstPartOfPath,
                                   Node[] nodes) {
            final int firstPartLen = firstPartOfPath.length();

            Node matchingNode = null;

            for (Node node : nodes) {
                if (!node.isPrefix) {
                    if (node.name.equals(firstPartOfPath)) {
                        matchingNode = node;
                        break;
                    }
                } else /* if prefix */ {
                    int prefixLen = node.name.length();
                    int prefixLastCharIndex = prefixLen - 1;
                    if ((prefixLen <= firstPartLen)
                        && ((prefixLen == 0)
                            || (node.name.charAt(prefixLastCharIndex)
                                    == firstPartOfPath.charAt(prefixLastCharIndex))
                               && firstPartOfPath.startsWith(node.name))) {
                        matchingNode = node;
                        break;
                    }
                }
            }

            if (matchingNode == null) {
                return null;
            }

            if ((matchingNode.children != null)
                    && (relativePath.length() > firstPartLen)) {
                String subPath = relativePath.substring(firstPartLen + 1);
                OwnerInfo subnodeInfo = findInfo(subPath,
                                                 getFirstPartOfPath(subPath),
                                                 matchingNode.children.second());
                if (subnodeInfo != null) {
                    return subnodeInfo;
                }
            }

            return matchingNode.info;
        }

        private static final int getTopNodeIndex(String pattern) {
            return getCharIndex(pattern.charAt(0)) % TOP_NODES_COUNT;
        }

        private static final int getCharIndex(char c) {
            return ((c | 0x20) + 159) & 0xff; // | 0x20 .. toLowerCase()
                                              // + 159 ... 256 - 'a'
                                              // & 0xff .. modulo 256
        }

        private static String getFirstPartOfPath(String path) {
            int firstSlashPos = path.indexOf('/');
            return (firstSlashPos != -1) ? path.substring(0, firstSlashPos)
                                         : path;
        }

        enum ParsingFailure {
            EMPTY_LINE("empty line"),                                   //NOI18N
            EMPTY_PATH("empty path"),                                   //NOI18N
            ABSOLUTE_PATH("not a relative path"),                       //NOI18N
            NO_SEPARATOR("missing '='"),                                //NOI18N
            SYNTAX_ERROR("syntax error");                               //NOI18N

            private final String description;
            ParsingFailure(String description) {
                this.description = description;
            }
            String getDescription() {
                return description;
            }
        }

        private static final class PatternParser {

            private static final int INIT = 0;
            private static final int NAME = 1;
            private static final int STAR = 2;

            private String patternBeingParsed;
            private int state;

            /* boundaries of the sub-pattern being recognized: */
            private int begin;

            /* storage for the result: */
            private String firstString;
            private List<String> result;
            private int separatorPosition;
            private ParsingFailure failure;
            private int syntaxErrorPosition;

            PatternParser() {
                reset();
            }

            private void reset() {
                patternBeingParsed = null;

                firstString = null;
                result = null;
                separatorPosition = -1;
                failure = null;
                syntaxErrorPosition = -1;

                begin = -1;

                state = INIT;
            }

            ParsingFailure getFailure() {
                return failure;
            }

            int getSyntaxErrorPosition() {
                return syntaxErrorPosition;
            }

            /**
             * Returns position of the separator between a path pattern
             * and the assigned data (the equal-sign).
             */
            int getSeparatorPosition() {
                return separatorPosition;
            }

            private List<String> parsePattern(final String mappingFileLine) {
                reset();
                this.patternBeingParsed = mappingFileLine;

                final int length = mappingFileLine.length();
                int i;

                for (i = 0; i < length; i++) {
                    char c = mappingFileLine.charAt(i);
                    if ((c != ' ') && (c != '\t')) {
                        break;
                    }
                }
                if (i == length) {
                    failure = ParsingFailure.EMPTY_LINE;
                    return null;
                }
                if (mappingFileLine.charAt(i) == '=') {
                    failure = ParsingFailure.EMPTY_PATH;
                    return null;
                }

                final int patternBeginning = i;

                boolean escaped = false;
                boolean containsEscapedChars = false;
                int lastNonSpacePos = i;
                loop:
                for (; i < length; i++) {
                    final char c = mappingFileLine.charAt(i);

                    switch (state) {
                        case INIT:
                            assert !escaped;
                            if (c == '*') {
                                storePatternPart("*");                  //NOI18N
                                state = STAR;
                            } else if (c == '=') {
                                separatorPosition = i;
                                break loop;
                            } else if (c == '/') {
                                /* slash at the beginning OR two slashes */
                                failure = (i == patternBeginning)
                                          ? ParsingFailure.ABSOLUTE_PATH
                                          : ParsingFailure.SYNTAX_ERROR;
                                break loop;
                            } else {
                                if (c == '\\') {
                                    escaped = true;
                                    containsEscapedChars = true;
                                }
                                if ((c != ' ') && (c != '\t')) {
                                    lastNonSpacePos = i;
                                }
                                begin = i;
                                state = NAME;
                            }
                            break;
                        case NAME:
                            if (escaped) {
                                escaped = false;
                                lastNonSpacePos = i;
                                /* no other change */
                            } else if (c == '*') {
                                storePatternPart(i + 1,     //including the star
                                                 containsEscapedChars);
                                containsEscapedChars = false;
                                state = STAR;
                            } else if (c == '/') {
                                storePatternPart(i, containsEscapedChars);
                                containsEscapedChars = false;
                                state = INIT;
                            } else if (c == '=') {
                                separatorPosition = i;
                                break loop;
                            } else {
                                if (c == '\\') {
                                    escaped = true;
                                    containsEscapedChars = true;
                                }
                                if ((c != ' ') && (c != '\t')) {
                                    lastNonSpacePos = i;
                                }
                            }
                            break;
                        case STAR:
                            assert !escaped;
                            if (c == '*') {         //two stars together
                                failure = ParsingFailure.SYNTAX_ERROR;
                                break loop;
                            } else if (c == '/') {
                                state = INIT;
                            } else if (c == '=') {
                                separatorPosition = i;
                                break loop;
                            } else {
                                /* non-special character after a star */
                                failure = ParsingFailure.SYNTAX_ERROR;
                                break loop;
                            }
                            break;
                        default:
                            assert false;
                            break;
                    }
                } //for (...)

                if (failure != null) {
                    syntaxErrorPosition = i;
                    return null;
                }

                if (separatorPosition == -1) {
                    failure = ParsingFailure.NO_SEPARATOR;
                    return null;
                }

                switch (state) {
                    case INIT:
                        /* pattern ends with '/' */
                        storePatternPart("*");                          //NOI18N
                        break;
                    case NAME:
                        if (lastNonSpacePos >= begin) {
                            storePatternPart(lastNonSpacePos + 1,
                                             containsEscapedChars);
                            containsEscapedChars = false;
                        } else {
                            storePatternPart("*");                      //NOI18N
                        }
                        break;
                }

                return makeFinalResult();
            } // method parsePattern()

            private void storePatternPart(int endIndex,
                                          boolean containsEscapedChars) {
                assert begin >= 0;
                assert endIndex > begin;
                String patternPart;
                if (!containsEscapedChars) {
                    patternPart = patternBeingParsed.substring(begin, endIndex);
                } else {
                    StringBuilder buf = new StringBuilder(endIndex - begin - 1);
                    for (int i = begin; i < endIndex; i++) {
                        char c = patternBeingParsed.charAt(i);
                        if (c == '\\') {
                            continue;
                        }
                        buf.append(c);
                    }
                    assert buf.length() < (endIndex - begin);  //at least one backslash skipped
                    patternPart = buf.toString();
                }
                storePatternPart(patternPart);
            }

            private void storePatternPart(String part) {
                if (firstString == null) {
                    firstString = part;
                } else {
                    if (result == null) {
                        result = new ArrayList<String>(4);
                        result.add(firstString);
                    }
                    result.add(part);
                }

                begin = -1;
            }

            private List<String> makeFinalResult() {
                if (result != null) {
                    return result;
                } else if (firstString != null) {
                    return Collections.singletonList(firstString);
                } else {
                    return Collections.emptyList();
                }
            }

        } // class PatternParser

        private static final class Node {
            final String name;
            final boolean isPrefix;
            final OwnerInfo info;
            private Union2<List<Node>,Node[]> children;
            Node(String patternPart, OwnerInfo info) {
                this.isPrefix = isPrefixPattern(patternPart);
                this.name = isPrefix ? getPrefixPart(patternPart)
                                     : patternPart;
                this.info = (info != null) ? info : OwnerInfo.NULL_OWNER_INFO;
            }
            void addChild(Node child) {
                if (children == null) {
                    children = Union2.<List<Node>,Node[]>createFirst(
                                                    new ArrayList<Node>(4));
                }
                children.first().add(child);
            }
            private void processChildren() {
                if (children == null) {
                    return;
                }

                List<Node> nodeList = children.first();
                assert !nodeList.isEmpty();
                Node[] nodesArray = children.first().toArray(
                                                new Node[nodeList.size()]);
                children = Union2.<List<Node>,Node[]>createSecond(nodesArray);
            }
        }

        private static boolean isPrefixPattern(String patternPart) {
            return patternPart.charAt(patternPart.length() - 1) == '*';
        }

        private static String getPrefixPart(String patternPart) {
            assert isPrefixPattern(patternPart);
            return patternPart.substring(0, patternPart.length() - 1);
        }

    }

    /**
     * Wrapper around a (non-empty) list of {@code String}s.
     */
    public static class OwnerInfo {

        static final OwnerInfo NULL_OWNER_INFO = new OwnerInfo("");     //NOI18N

        private final String owner;
        private final List<String> extraData;

        static OwnerInfo parseSpec(CharSequence spec) {
            if (spec.length() == 0) {
                return null;
            }

            String owner = null;
            ArrayList<String> extraList = null;

            int i;
            final int length = spec.length();

            /* skip initial spaces and tabs */
            for (i = 0; i < length; i++) {
                char c = spec.charAt(i);
                if ((c != ' ') && (c != '\t')) {
                    break;
                }
            }
            if (i == length) {
                return null;
            }

            /*
             * 'i' is now the index of the first non-empty character
             * in the string
             */

            StringBuilder buf = null;

            int start = i;
            boolean escaped = false;
            for ( ; i < length; i++) {
                char c = spec.charAt(i);
                if (escaped) {
                    if (buf == null) {
                        buf = new StringBuilder(20);
                        buf.append(spec, start, i - 1);
                    }
                    buf.append(c);
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '/') {
                    String part = (buf != null)
                                  ? buf.toString()
                                  : spec.subSequence(start, i).toString();
                    if (owner == null) {
                        owner = part;
                    } else {
                        if (extraList == null) {
                            extraList = new ArrayList<String>(4);
                        }
                        extraList.add(part);
                    }
                    start = i + 1;
                    buf = null;
                } else if (buf != null) {
                    buf.append(c);
                }
            }
            String part = (buf != null)
                          ? buf.toString()
                          : spec.subSequence(start, length).toString();
            if (owner == null) {
                owner = part;
            } else {
                if (extraList == null) {
                    extraList = new ArrayList<String>(4);
                }
                extraList.add(part);
            }

            if (extraList != null) {
                return new OwnerInfo(owner, extraList);
            } else {
                return new OwnerInfo(owner);
            }
        }

        /**
         * Creates an instance of {@code OwnerInfo} holding just one
         * {@code String}.
         *
         * @param  owner  the {@code String} to be held by the instance
         * @exception  java.lang.IllegalArgumentException
         *             if the given {@code String} is {@code null}
         */
        public OwnerInfo(String owner) {
            this(owner, (String[]) null);
        }

        /**
         * Creates an instance of {@code OwnerInfo} holding the given
         * {@code String}s.
         *
         * @param  owner  the {@code String} to be held by the instance
         * @param  extraData  additional strings to be held by the instance,
         *                    or {@code null} if no extra data are to be held
         * @exception  java.lang.IllegalArgumentException
         *             if the given {@code String} is {@code null}
         */
        public OwnerInfo(String owner, String... extraData) {
            checkOwner(owner);

            this.owner = owner;
            if ((extraData == null) || (extraData.length == 0)) {
                this.extraData = null;
            } else if (extraData.length == 1) {
                this.extraData = Collections.singletonList(extraData[0]);
            } else {
                this.extraData = new ArrayList<String>(extraData.length);
                for (String str : extraData) {
                    this.extraData.add(str);
                }
            }
        }

        /**
         * Creates an instance of {@code OwnerInfo} holding the given
         * {@code String}s.
         *
         * @param  owner  the {@code String} to be held by the instance
         * @param  extraData  additional strings to be held by the instance,
         *                    or {@code null} if no extra data are to be held
         * @exception  java.lang.IllegalArgumentException
         *             if the given {@code String} is {@code null}
         */
        public OwnerInfo(String owner, List<String> extraData) {
            checkOwner(owner);

            this.owner = owner;
            if ((extraData == null) || (extraData.isEmpty())) {
                this.extraData = null;
            } else if (extraData.size() == 1) {
                this.extraData = Collections.singletonList(extraData.get(0));
            } else {
                this.extraData = new ArrayList<String>(extraData);
            }
        }

        private void checkOwner(String owner) throws IllegalArgumentException {
            if (owner == null) {
                throw new IllegalArgumentException(
                        "owner must not be null");                      //NOI18N
            }
        }

        /**
         * Returns the first string of the list of strings held by this object.
         * 
         * @return  first string held by this object
         */
        public String getOwner() {
            return owner;
        }

        /**
         * Returns a list containing the next (second, third, etc.) strings
         * held by this object.
         *
         * @return  list of strings held by this object,
         *          or an empty list of this object holds just one string
         */
        public List<String> getExtraData() {
            return (extraData != null) ? extraData
                                       : Collections.<String>emptyList();
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + owner.hashCode();
            hash = 79 * hash + (extraData != null ? extraData.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if ((obj != null) && (obj.getClass() == OwnerInfo.class)) {
                OwnerInfo other = (OwnerInfo) obj;
                return equal(owner, other.owner)
                       && equal(extraData, other.extraData);
            }
            return false;
        }

        private static boolean equal(Object o1, Object o2) {
            return (o1 == null) ? (o2 == null) : o1.equals(o2);
        }

        @Override
        public String toString() {
            return super.toString() + paramString();
        }

        public String paramString() {
            StringBuilder buf = new StringBuilder(100);
            buf.append("[component=").append(owner)                     //NOI18N
               .append(",extraData=").append(getExtraDataString())      //NOI18N
               .append(']');
            return buf.toString();
        }

        private String getExtraDataString() {
            if ((extraData == null) || extraData.isEmpty()) {
                return "()";                                            //NOI18N
            }

            StringBuilder buf = new StringBuilder(100);
            buf.append('(');
            boolean first = true;
            for (String item : extraData) {
                if (!first) {
                    buf.append(',');
                }
                buf.append((item != null) ? item : "null");             //NOI18N
                first = false;
            }
            buf.append(')');
            return buf.toString();
        }

    }

    static class BufferedReaderIterator implements Iterable<String>,
                                                   Iterator<String> {

        private final BufferedReader bufReader;
        private String nextLine;
        private boolean nextLinePrepared;

        BufferedReaderIterator(BufferedReader bufReader) {
            this.bufReader = bufReader;
        }

        public Iterator<String> iterator() {
            return this;
        }

        public boolean hasNext() {
            if (!nextLinePrepared) {
                nextLine = null;
                try {
                    nextLine = bufReader.readLine();
                } catch (IOException ex) {
                    //keep 'nextLine' at <null>
                } finally {
                    nextLinePrepared = true;

                    if (nextLine == null) {
                        try {
                            bufReader.close();
                        } catch (IOException ex) {
                            //give up
                        }
                    }
                }
            }
            return nextLine != null;
        }

        public String next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            nextLinePrepared = false;
            return nextLine;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

}

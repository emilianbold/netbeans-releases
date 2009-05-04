/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.options.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Model for export/import options. It reads {@code OptionsExport/<category>/<item>}
 * from layers and evaluates whether items are applicable for export/import.
 *
 * @author Jiri Skrivanek
 */
public final class OptionsExportModel {

    private static final Logger LOGGER = Logger.getLogger(OptionsExportModel.class.getName());
    /** Folder in layer file system where provider are searched for */
    private static final String OPTIONS_EXPORT_FOLDER = "OptionsExport"; //NOI18N
    /** Pattern used to get names of option profiles. **/
    private static final String GROUP_PATTERN = "([^/]*)";  //NOI18N
    /** Source of export/import (zip file or userdir) */
    private File source;
    /** List of categories */
    private List<Category> categories;
    /** Cache of paths relative to source root */
    List<String> relativePaths;

    /** Returns instance of export options model.
     * @param source source of export/import. It is either zip file or userdir
     * @return instance of export options model
     */
    public OptionsExportModel(File source) {
        this.source = source;
    }

    /**
     * Gets list of categories
     * @return list of categories
     */
    List<Category> getCategories() {
        if (categories == null) {
            loadCategories();
        }
        return categories;
    }

    /** Returns state of model - ENABLED, DISABLED or PARTIAL.
     * @return state of model
     */
    State getState() {
        int enabled = 0;
        int disabled = 0;
        int applicableCount = 0;
        for (OptionsExportModel.Category category : getCategories()) {
            if (category.isApplicable()) {
                applicableCount++;
                if (category.getState() == State.ENABLED) {
                    enabled++;
                } else if (category.getState() == State.DISABLED) {
                    disabled++;
                }
            }
        }
        if (enabled == applicableCount) {
            return State.ENABLED;
        } else if (disabled == applicableCount) {
            return State.DISABLED;
        } else {
            return State.PARTIAL;
        }
    }

    /** Sets state of all categories according to given value.
     * @param state new state
     */
    void setState(State state) {
        for (OptionsExportModel.Category category : getCategories()) {
            category.setState(state);
        }
    }

    /** Copies files from source (zip file or userdir) to target dir according
     * to current state of model, i.e. only include/exclude patterns from
     * enabled items are considered.
     * @param targetUserdir target userdir
     */
    void doImport(File targetUserdir) {
        try {
            if (source.isFile()) {
                // zip file
                extractZipFile(targetUserdir);
            } else {
                // userdir
                copy(targetUserdir);
            }
        } catch (IOException ex) {
            Exceptions.attachLocalizedMessage(ex,
                    NbBundle.getMessage(OptionsExportModel.class, "OptionsExportModel.import.error"));
            Exceptions.printStackTrace(ex);
        }
    }

    /** Creates zip file according to current state of model, i.e. only
     * include/exclude patterns from enabled items are copied from source userdir.
     * @param targetZipFile target zip file
     */
    void doExport(File targetZipFile) {
        try {
            createZipFile(targetZipFile);
        } catch (IOException ex) {
            Exceptions.attachLocalizedMessage(ex,
                    NbBundle.getMessage(OptionsExportModel.class, "OptionsExportModel.export.zip.error", targetZipFile));
            Exceptions.printStackTrace(ex);
        }
    }

    /** Copies files from source dir to target dir according to current state
     * of model, i.e. only include/exclude patterns from enabled items are
     * considered.
     * @param targetUserdir target userdir
     * @throws java.io.IOException
     */
    private void copy(File targetUserdir) throws IOException {
        LOGGER.fine("Copying from: " + source + "\nto:" + targetUserdir);  //NOI18N
        List<String> applicablePaths = getApplicablePaths(getIncludePatterns(), getExcludePatterns());
        if (!applicablePaths.isEmpty() && !targetUserdir.exists()) {
            if (!targetUserdir.mkdirs()) {
                throw new IOException("Cannot create folder: " + targetUserdir.getAbsolutePath());  //NOI18N
            }
        }
        for (String path : applicablePaths) {
            LOGGER.fine("Path=" + path);  //NOI18N
            copyFile(new File(source, path), new File(targetUserdir, path));
        }
    }

    /** Extracts files from source zip file to target dir according to current state
     * of model, i.e. only include/exclude patterns from enabled items are
     * considered.
     * @param targetUserdir target userdir
     */
    private void extractZipFile(File targetUserdir) throws IOException {
        LOGGER.fine("Extracting from:" + source + " to:" + targetUserdir);  //NOI18N
        List<String> applicablePaths = getApplicablePaths(getIncludePatterns(), getExcludePatterns());
        extractZipFile(source, targetUserdir, applicablePaths);
    }

    /** Creates zip file from source userdir according to current state
     * of model, i.e. only include/exclude patterns from enabled items are
     * considered.
     * @param targetFile target zip file
     */
    private void createZipFile(File targetFile) throws IOException {
        LOGGER.fine("Creating file:" + targetFile + " from:" + source);  //NOI18N
        List<String> applicablePaths = getApplicablePaths(getIncludePatterns(), getExcludePatterns());
        createZipFile(targetFile, source, applicablePaths);
    }

    /** Returns set of include patterns in this model. */
    private Set<Pattern> getIncludePatterns() {
        Set<Pattern> includePatterns = new HashSet<Pattern>();
        for (OptionsExportModel.Category category : getCategories()) {
            for (OptionsExportModel.Item item : category.getItems()) {
                if (item.isEnabled()) {
                    String include = item.getInclude();
                    if (include != null && include.length() > 0) {
                        includePatterns.add(Pattern.compile(include));
                    }
                }
            }
        }
        return includePatterns;
    }

    /** Returns set of exclude patterns in this model. */
    private Set<Pattern> getExcludePatterns() {
        Set<Pattern> excludePatterns = new HashSet<Pattern>();
        for (OptionsExportModel.Category category : getCategories()) {
            for (OptionsExportModel.Item item : category.getItems()) {
                if (item.isEnabled()) {
                    String exclude = item.getExclude();
                    if (exclude != null && exclude.length() > 0) {
                        excludePatterns.add(Pattern.compile(exclude));
                    }
                }
            }
        }
        return excludePatterns;
    }

    /** Just for debugging. */
    @Override
    public String toString() {
        return getClass().getName() + " source=" + source;  //NOI18N
    }

    /** Represents one item in UI and hold include/exclude patterns. */
    class Item {

        private String displayName;
        private String include;
        private String exclude;
        private boolean enabled = false;
        /** Whether some patterns match current source. */
        private boolean applicable = false;
        private boolean applicableInitialized = false;

        public Item(String displayName, String include, String exclude) {
            this.displayName = displayName;
            this.include = include;
            this.exclude = exclude;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getInclude() {
            return include;
        }

        public String getExclude() {
            return exclude;
        }

        /** Returns true if at least one path in current source
         * matches include/exclude patterns.
         * @return true if at least one path in current source
         * matches include/exclude patterns, false otherwise
         */
        public boolean isApplicable() {
            if (!applicableInitialized) {
                List<String> applicablePaths = getApplicablePaths(
                        Collections.singleton(Pattern.compile(include)),
                        Collections.singleton(Pattern.compile(exclude)));
                LOGGER.fine("    applicablePaths=" + applicablePaths);  //NOI18N
                applicable = !applicablePaths.isEmpty();
                applicableInitialized = true;
            }
            return applicable;
        }

        /** Returns true if user selected this item for export/import.
         * @return returns true if user selected this item for export/import,
         * false otherwise
         */
        public boolean isEnabled() {
            return enabled;
        }

        /** Sets whether user selects this item for export/import.
         * @param newState if selected or not
         */
        public void setEnabled(boolean newState) {
            enabled = newState;
        }

        /** Just for debugging. */
        @Override
        public String toString() {
            return getDisplayName() + ", enabled=" + isEnabled();  //NOI18N
        }
    }

    /** Represents 3 state of category. */
    static enum State {

        ENABLED(Boolean.TRUE),
        DISABLED(Boolean.FALSE),
        PARTIAL(null);
        private final Boolean bool;

        State(Boolean bool) {
            this.bool = bool;
        }

        public Boolean toBoolean() {
            return bool;
        }

        public static State valueOf(Boolean bool) {
            if (bool == null) {
                return PARTIAL;
            } else {
                return bool ? ENABLED : DISABLED;
            }
        }
    };

    /** Represents category in UI holding several items. */
    class Category {

        //xml entry names
        private static final String INCLUDE = "include"; // NOI18N
        private static final String EXCLUDE = "exclude"; // NOI18N
        private static final String DISPLAY_NAME = "displayName"; // NOI18N
        private FileObject categoryFO;
        private String displayName;
        private List<Item> items;
        private State state = State.DISABLED;

        public Category(FileObject fo, String displayName) {
            this.categoryFO = fo;
            this.displayName = displayName;
        }

        private void addItem(String displayName, String includes, String excludes) {
            items.add(new Item(displayName, includes, excludes));
        }

        /** If include pattern contains group pattern, it finds all such groups
         * and creates items for all of them. It is used for example for keymap
         * profiles.
         */
        private void resolveGroups(String dispName, String include, String exclude) {
            LOGGER.fine("resolveGroups include=" + include);  //NOI18N
            List<String> applicablePaths = getApplicablePaths(
                    Collections.singleton(Pattern.compile(include)),
                    Collections.singleton(Pattern.compile(exclude)));
            Set<String> groups = new HashSet<String>();
            Pattern p = Pattern.compile(include);
            for (String path : applicablePaths) {
                Matcher m = p.matcher(path);
                m.matches();
                if (m.groupCount() == 1) {
                    String group = m.group(1);
                    if (group != null) {
                        groups.add(group);
                    }
                }
            }
            LOGGER.fine("GROUPS=" + groups);  //NOI18N
            for (String group : groups) {
                // add additional items according to groups
                String newDisplayName = group;
                if (dispName.contains("{")) {  //NOI18N
                    newDisplayName = MessageFormat.format(dispName, group);
                }
                addItem(newDisplayName, include.replace(GROUP_PATTERN, group), exclude);
            }
        }

        /** Returns items under OptionsExport/<category>. **/
        public List<Item> getItems() {
            if (items == null) {
                items = new ArrayList<Item>();
                FileObject[] itemsFOs = categoryFO.getChildren();
                for (FileObject itemFO : itemsFOs) {
                    String dispName = (String) itemFO.getAttribute(DISPLAY_NAME);
                    assert dispName != null : "Display name of export option item not defined in layer.";  //NOI18N
                    String include = (String) itemFO.getAttribute(INCLUDE);
                    if (include == null) {
                        include = "";  //NOI18N
                    }
                    String exclude = (String) itemFO.getAttribute(EXCLUDE);
                    if (exclude == null) {
                        exclude = "";  //NOI18N
                    }
                    if (include.contains(GROUP_PATTERN)) {
                        resolveGroups(dispName, include, exclude);
                    } else {
                        addItem(dispName, include, exclude);
                    }
                }
            }
            return items;
        }

        public String getName() {
            return categoryFO.getNameExt();
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setState(State state) {
            this.state = state;
            updateItems(state);
        }

        public State getState() {
            return state;
        }

        public boolean isApplicable() {
            for (Item item : getItems()) {
                if (item.isApplicable()) {
                    return true;
                }
            }
            return false;
        }

        /** Just for debugging. */
        @Override
        public String toString() {
            return getDisplayName() + ", state=" + getState();  //NOI18N
        }

        private void updateItems(State state) {
            for (Item item : getItems()) {
                if (state != State.PARTIAL && item.isApplicable()) {
                    item.setEnabled(state.toBoolean());
                }
            }
        }
    } // end of Category

    /** Load categories from filesystem. */
    private void loadCategories() {
        FileObject[] categoryFOs = FileUtil.getConfigFile(OPTIONS_EXPORT_FOLDER).getChildren();
        // respect ordering defined in layers
        List<FileObject> sortedCats = FileUtil.getOrder(Arrays.asList(categoryFOs), false);
        categories = new ArrayList<OptionsExportModel.Category>(sortedCats.size());
        for (FileObject curFO : sortedCats) {
            String displayName = (String) curFO.getAttribute(Category.DISPLAY_NAME);
            categories.add(new Category(curFO, displayName));
        }
    }

    /** Filters relative paths of current source and returns only ones which match given
     * include/exclude patterns.
     * @param includePatterns include patterns
     * @param excludePatterns exclude patterns
     * @return relative patsh which match include/exclude patterns
     */
    private List<String> getApplicablePaths(Set<Pattern> includePatterns, Set<Pattern> excludePatterns) {
        List<String> applicablePaths = new ArrayList<String>();
        for (String relativePath : getRelativePaths()) {
            if (include(relativePath, includePatterns, excludePatterns)) {
                applicablePaths.add(relativePath);
            }
        }
        return applicablePaths;
    }

    /** Returns list of file path relative to current source root. The source is
     * either zip file or userdir. It scans sub folders recursively.
     * @return list of file path relative to current source root
     */
    private List<String> getRelativePaths() {
        if (relativePaths == null) {
            if (source.isFile()) {
                try {
                    // zip file
                    relativePaths = listZipFile(source);
                } catch (IOException ex) {
                    Exceptions.attachLocalizedMessage(ex, NbBundle.getMessage(OptionsExportModel.class, "OptionsExportModel.invalid.zipfile", source));
                    Exceptions.printStackTrace(ex);
                    relativePaths = Collections.emptyList();
                }
            } else {
                // userdir
                relativePaths = getRelativePaths(source);
            }
            LOGGER.fine("relativePaths=" + relativePaths);  //NOI18N
        }
        return relativePaths;
    }

    /** Returns list of file path relative to given source root. It scans
     * sub folders recursively.
     * @param sourceRoot source root
     * @return list of file path relative to given source root
     */
    private static List<String> getRelativePaths(File sourceRoot) {
        List<String> relativePaths = new ArrayList<String>();
        getRelativePaths(sourceRoot, sourceRoot, relativePaths);
        return relativePaths;
    }

    private static void getRelativePaths(File source, File sourceRoot, List<String> relativePaths) {
        if (source.isDirectory()) {
            File[] children = source.listFiles();
            if (children == null) {
                return;
            }
            for (File child : children) {
                getRelativePaths(child, sourceRoot, relativePaths);
            }
        } else {
            relativePaths.add(getRelativePath(sourceRoot, source));
        }
    }

    /** Returns slash separated path relative to given root. */
    private static String getRelativePath(File root, File file) {
        String result = file.getAbsolutePath().substring(root.getAbsolutePath().length());
        result = result.replace('\\', '/');  //NOI18N
        if (result.startsWith("/") && !result.startsWith("//")) {  //NOI18N
            result = result.substring(1);
        }
        return result;
    }

    /** Returns true if given relative path matches at least one of given include
     * patterns and doesn't match all exclude patterns.
     * @param relativePath relative path
     * @param includePatterns include patterns
     * @param excludePatterns exclude patterns
     * @return true if given relative path matches at least one of given include
     * patterns and doesn't match all exclude patterns, false otherwise
     */
    private static boolean include(String relativePath, Set<Pattern> includePatterns, Set<Pattern> excludePatterns) {
        boolean include = false;
        for (Pattern pattern : includePatterns) {
            Matcher matcher = pattern.matcher(relativePath);
            if (matcher.matches()) {
                include = true;
                break;
            }
        }
        if (include) {
            // check excludes
            for (Pattern pattern : excludePatterns) {
                Matcher matcher = pattern.matcher(relativePath);
                if (matcher.matches()) {
                    return false;
                }
            }
        }
        return include;
    }

    /** Copy source file to target file. It creates necessary sub folders.
     * @param sourceFile source file
     * @param targetFile target file
     * @throws java.io.IOException if copying fails
     */
    private static void copyFile(File sourceFile, File targetFile) throws IOException {
        ensureParent(targetFile);
        InputStream ins = null;
        OutputStream out = null;
        try {
            ins = new FileInputStream(sourceFile);
            out = new FileOutputStream(targetFile);
            FileUtil.copy(ins, out);
        } finally {
            if (ins != null) {
                ins.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    /** Creates parent of given file, if doesn't exist. */
    private static void ensureParent(File file) throws IOException {
        final File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) {
                throw new IOException("Cannot create folder: " + parent.getAbsolutePath());  //NOI18N
            }
        }
    }

    /** Extracts given zip file to target directory but only those files which
     * match given list.
     * @param sourceFile source zip file
     * @param targetDir target directory
     * @param applicablePaths list of files to be extracted
     */
    private static void extractZipFile(File sourceFile, File targetDir, List<String> applicablePaths) throws IOException {
        ZipFile zipFile = new ZipFile(sourceFile);
        Enumeration enumeration = zipFile.entries();
        while (enumeration.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) enumeration.nextElement();
            if (!applicablePaths.contains(zipEntry.getName())) {
                // skip not matching entries
                continue;
            }
            LOGGER.fine("Extracting:" + zipEntry.getName());  //NOI18N
            InputStream in = null;
            FileOutputStream out = null;
            try {
                in = zipFile.getInputStream(zipEntry);
                File outFile = new File(targetDir, zipEntry.getName());
                ensureParent(outFile);
                out = new FileOutputStream(outFile);
                FileUtil.copy(in, out);
            } finally {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }
        }
    }

    /** Returns list of paths from given zip file.
     * @param file zip file
     * @return list of paths from given zip file
     * @throws java.io.IOException
     */
    private static List<String> listZipFile(File file) throws IOException {
        List<String> relativePaths = new ArrayList<String>();
        // Open the ZIP file
        ZipFile zipFile = new ZipFile(file);
        // Enumerate each entry
        Enumeration entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            // Get the entry name
            String zipEntryName = ((ZipEntry) entries.nextElement()).getName();
            relativePaths.add(zipEntryName);
        }
        return relativePaths;
    }

    /** Creates zip file containing only selected files from given source dir.
     * @param targetFile target zip file
     * @param sourceDir source dir
     * @param relativePaths paths to be added to zip file
     * @throws java.io.IOException
     */
    private static void createZipFile(File targetFile, File sourceDir, List<String> relativePaths) throws IOException {
        ensureParent(targetFile);
        ZipOutputStream out = null;
        try {
            // Create the ZIP file
            out = new ZipOutputStream(new FileOutputStream(targetFile));
            // Compress the files
            for (String relativePath : relativePaths) {
                LOGGER.fine("Adding to zip: " + relativePath);  //NOI18N
                // Add ZIP entry to output stream.
                out.putNextEntry(new ZipEntry(relativePath));
                // Transfer bytes from the file to the ZIP file
                FileInputStream in = null;
                try {
                    in = new FileInputStream(new File(sourceDir, relativePath));
                    FileUtil.copy(in, out);
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }
                // Complete the entry
                out.closeEntry();
            }
            createProductInfo(out);
            // Complete the ZIP file
            out.close();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /** Adds build.info file with product, os, java version to zip file. */
    private static void createProductInfo(ZipOutputStream out) throws IOException {
        String productVersion = MessageFormat.format(
                NbBundle.getBundle("org.netbeans.core.startup.Bundle").getString("currentVersion"), //NOI18N
                new Object[]{System.getProperty("netbeans.buildnumber")}); //NOI18N
        String os = System.getProperty("os.name", "unknown") + ", " + //NOI18N
                System.getProperty("os.version", "unknown") + ", " + //NOI18N
                System.getProperty("os.arch", "unknown"); //NOI18N
        String java = System.getProperty("java.version", "unknown") + ", " + //NOI18N
                System.getProperty("java.vm.name", "unknown") + ", " + //NOI18N
                System.getProperty("java.vm.version", ""); //NOI18N
        out.putNextEntry(new ZipEntry("build.info"));  //NOI18N
        PrintWriter writer = new PrintWriter(out);
        writer.println("ProductVersion=" + productVersion); //NOI18N
        writer.println("OS=" + os); //NOI18N
        writer.println("Java=" + java); //NOI18Nv
        writer.println("Userdir=" + System.getProperty("netbeans.user")); //NOI18N
        writer.flush();
        // Complete the entry
        out.closeEntry();
    }
}

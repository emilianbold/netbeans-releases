/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.kenai.util;

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
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.util.Union2;

/**
 *
 * @author Marian Petras
 */
public abstract class NbModuleOwnerSupport {

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

    public final OwnerInfo getOwner(String configFileName, File path) {
        checkConfigFileName(configFileName);
        checkPath(path);

        try {
            return getOwnerImpl(configFileName, path);
        } catch (Throwable t) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, t);
            return null;
        }
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
                    processPatternFileLine(line, ++lineNum, parser);
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

        private void processPatternFileLine(String line, int lineNum,
                                            PatternParser parser) {
            int equalSignIndex = line.indexOf('=');
            if (equalSignIndex == -1) {
                LOG.log(Level.INFO,
                        "syntax error at line {0} (missing '=')",   //NOI18N
                        Integer.valueOf(lineNum));
                return;
            }

            String patternString = line.substring(0, equalSignIndex);
            List<String> patternPath = parser.parsePattern(patternString);
            if (patternPath == null) {
                LOG.log(Level.INFO,
                        "syntax error at line {0}"                  //NOI18N
                            + " (invalid pattern on the left side)",//NOI18N
                        Integer.valueOf(lineNum));
                return;
            }

            String infoString = line.substring(equalSignIndex + 1);
            OwnerInfo info = OwnerInfo.parseSpec(infoString);
            if (info == null) {
                LOG.log(Level.INFO,
                        "syntax error at line {0}"                  //NOI18N
                            + " (no Bugzilla component specified)", //NOI18N
                        Integer.valueOf(lineNum));
                return;
            }

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

            return findInfo(relativePath, firstPartOfPath, topNode);
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
                                              // + 159 ... (-'a' modulo 256)
                                              // & 0xff .. modulo 256
        }

        private static String getFirstPartOfPath(String path) {
            int firstSlashPos = path.indexOf('/');
            return (firstSlashPos != -1) ? path.substring(0, firstSlashPos)
                                         : path;
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

            PatternParser() {
                reset();
            }

            private void reset() {
                patternBeingParsed = null;

                firstString = null;
                result = null;

                begin = -1;

                state = INIT;
            }

            private List<String> parsePattern(final String pattern) {
                reset();
                this.patternBeingParsed = pattern;

                final int length = pattern.length();
                if (length == 0) {
                    return null;
                }

                for (int i = 0; i < length; i++) {
                    final char c = pattern.charAt(i);

                    switch (state) {
                        case INIT:
                            if (c == '*') {
                                storePatternPart("*");                  //NOI18N
                                state = STAR;
                            } else if (c != '/') {
                                begin = i;  //remember beginning of the pattern
                                state = NAME;
                            } else {
                                /* slash at the beginning OR two slashes */
                                return null;
                            }
                            break;
                        case NAME:
                            if (c == '*') {
                                storePatternPart(i + 1);    //including the star
                                state = STAR;
                            } else if (c == '/') {
                                storePatternPart(i);
                                state = INIT;
                            }
                                /* else no change */
                            break;
                        case STAR:
                            if (c == '*') {
                                /* no change */
                            } else if (c == '/') {
                                state = INIT;
                            } else {
                                /* non-special character after a star */
                                return null;
                            }
                            break;
                        default:
                            assert false;
                            break;
                    }
                } //for (...)

                switch (state) {
                    case INIT:
                        /* pattern ends with '/' */
                        storePatternPart("*");                          //NOI18N
                        break;
                    case NAME:
                        storePatternPart(length);
                        break;
                }

                return makeFinalResult();
            } // method parsePattern()

            private void storePatternPart(int endIndex) {
                storePatternPart(patternBeingParsed.substring(begin, endIndex));
            }

            private void storePatternPart(String part) {
                if (firstString == null) {
                    firstString = part;
                } else {
                    if (result == null) {
                        result = new ArrayList<String>(3);
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
                this.info = info;
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

    public static class OwnerInfo {

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

        public OwnerInfo(String owner) {
            this(owner, (String[]) null);
        }

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

        public String getOwner() {
            return owner;
        }

        public List<String> getExtraData() {
            return extraData;
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

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
package org.netbeans.modules.xml.wsdl.ui.api.property;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.netbeans.modules.xml.xam.Component;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

public abstract class ChooserHelper<T extends Component> {

    public abstract void populateNodes(Node parentNode);

    public abstract Node selectNode(T comp);

    /*
     * Filternode to make the nodes look enabled.
     */
    public static class EnabledNode extends FilterNode {

        private static Pattern pattern = Pattern.compile("(^<font.*>)(.*)(<.*>$)");

        public EnabledNode(Node node) {
            super(node, new EnabledChildren(node));
        }

        @Override
        public String getHtmlDisplayName() {
            //strips off font tag, to make it not grey. IZ  
            String retValue = super.getHtmlDisplayName();
            if (retValue == null) {
                retValue = getDisplayName();
            }


            if (retValue != null) {
                Matcher matcher = pattern.matcher(retValue);
                if (matcher.find()) {
                    return matcher.group(2);
                }
            }
            return retValue;
        }
    }

    private static class EnabledChildren extends FilterNode.Children {

        public EnabledChildren(Node node) {
            super(node);
        }

        @Override
        protected Node copyNode(Node node) {
            return new EnabledNode(node);
        }
    }

    static class DirFileFilter implements FileFilter {

        public boolean accept(File pathname) {
            return pathname.isDirectory();
        }
    }

    class FileNode extends FilterNode {

        String displayName;

        public FileNode(Node original, String path, int level) {
            super(original, new FileNodeChildren(original, level));
            displayName = path;
        }

        public FileNode(Node original, String path) {
            this(original, path, 1);
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }
    }

    static class FileNodeChildren extends FilterNode.Children {

        int level = 1;

        public FileNodeChildren(Node node) {
            super(node);
        }

        public FileNodeChildren(Node node, int level) {
            super(node);
            this.level = level;
        }

        @Override
        protected Node copyNode(Node key) {
            return new CategoryFilterNode(key, level - 1);
        }
    }

    static class CategoryFilterNode extends FilterNode {

        public CategoryFilterNode(Node node, int level) {
            super(node, new CategoryFilterChildren(node, level));
        }
    }

    static class CategoryFilterChildren extends FilterNode.Children {

        private final int level;

        public CategoryFilterChildren(Node node, int level) {
            super(node);
            this.level = level;
        }

        @Override
        protected Node copyNode(Node key) {
            if (level <= 0) {
                return new ChildLessNode(key);
            }
            return new CategoryFilterNode(key, level - 1);
        }
    }

    static class ChildLessNode extends FilterNode {

        public ChildLessNode(Node node) {
            super(node, Children.LEAF);
        }
    }

    protected List<File> recursiveListFiles(File file, FileFilter filter) {
        List<File> files = new ArrayList<File>();
        if (file != null && file.isDirectory()) {
            File[] filesArr = file.listFiles(filter);
            if (filesArr != null) {
                files.addAll(Arrays.asList(filesArr));
            }
            File[] dirs = file.listFiles(new DirFileFilter());
            if (dirs != null) {
                for (File dir : dirs) {
                    List<File> fs = recursiveListFiles(dir, filter);
                    if (fs != null && fs.size() > 0) {
                        files.addAll(fs);
                    }
                }
            }
        }

        return files;
    }

    /**
     * Gets folders defined in the logical view provider.
     * This reliably seems to filter off non source folders.
     * @param node
     * @param validFolders
     */
    protected void populateValidFolders(Node node, Set<FileObject> validFolders) {
        org.openide.nodes.Children children = node.getChildren();
        for (Node childNode : children.getNodes()) {
            DataObject dobj = childNode.getLookup().lookup(DataObject.class);
            if (dobj != null) {
                FileObject fo = dobj.getLookup().lookup(FileObject.class);
                if (fo != null && fo.isFolder() && !validFolders.contains(fo)) {
                    validFolders.add(fo);
                }
            }
        }
    }
}

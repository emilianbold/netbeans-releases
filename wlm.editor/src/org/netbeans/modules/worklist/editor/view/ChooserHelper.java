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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.view;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.netbeans.modules.xml.xam.Component;
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
        public String getHtmlDisplayName()
        {
            //strips off font tag, to make it not grey. IZ  
            String retValue = super.getHtmlDisplayName();
            if(retValue == null) retValue = getDisplayName();
            
            
            if(retValue != null) {
                Matcher matcher  = pattern.matcher(retValue);
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

    
    
    protected File[] recursiveListFiles(File file, FileFilter filter) {
        List<File> files = new ArrayList<File>();
        File[] filesArr = file.listFiles(filter);
        files.addAll(Arrays.asList(filesArr));
        File[] dirs = file.listFiles(new DirFileFilter());
        for (File dir : dirs) {
            files.addAll(Arrays.asList(recursiveListFiles(dir, filter)));
        }
        return files.toArray(new File[files.size()]);
    }
    
}

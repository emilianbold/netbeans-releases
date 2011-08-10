/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.profiler.selector.api.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.lib.profiler.client.ClientUtils.SourceCodeSelection;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.LanguageIcons;
import org.netbeans.modules.profiler.api.java.SourceClassInfo;
import org.netbeans.modules.profiler.api.java.SourcePackageInfo;


/**
 * A base class for a package node
 * Concrete subclasses must take care of providing the list of contained packages
 * and classes
 * @author Jaroslav Bachorik
 */
public class PackageNode extends ContainerNode {
    private SourceCodeSelection signature;
    private SourcePackageInfo pkg;
    
    /**
     * A private implementation of package children
     */
    private static class PackageChildren extends SelectorChildren<PackageNode> {
        @Override
        protected List<SelectorNode> prepareChildren(PackageNode parent) {
            List<SelectorNode> nodes = new ArrayList<SelectorNode>();
            List<PackageNode> subs = getSubpackages(parent);
            List<ClassNode> classes = getClasses(parent);
            nodes.addAll(subs);
            nodes.addAll(classes);

            return nodes;
        }

        /**
         *
         * @param parent Parent package
         * @return Returns a list of class nodes belonging to the parent package
         */
        private List<ClassNode> getClasses(final PackageNode parent) {
            List<ClassNode> nodes = parent.getContainedClasses();

            Collections.sort(nodes, ClassNode.COMPARATOR);

            return nodes;
        }

        /**
         *
         * @param parent Parent package
         * @return Returns a list fo package nodes belonging to the parent package
         */
        private List<PackageNode> getSubpackages(final PackageNode parent) {
            List<PackageNode> nodes = parent.getContainedPackages();

            Collections.sort(nodes, COMPARATOR);

            return nodes;
        }
    }

    /**
     * Default package name
     */
    public static final String DEFAULT_NAME = "<default>"; // NOI18N
    /**
     * A {@linkplain Comparator} able to compare {@linkplain PackageNode} instances
     */
    public static final Comparator COMPARATOR = new Comparator<PackageNode>() {
        @Override
        public int compare(PackageNode o1, PackageNode o2) {
            if (o1.getNodeName().equals(PackageNode.DEFAULT_NAME)) {
                return -1;
            }

            return o1.toString().compareTo(o2.toString());
        }
    };

    protected static final Logger LOGGER = Logger.getLogger(PackageNode.class.getName());

    /** Creates a new instance of PackageNode */
    public PackageNode(SourcePackageInfo pkg, ContainerNode parent) {
        super(pkg.getSimpleName(), stripName(defaultizeName(pkg.getBinaryName())), Icons.getIcon(LanguageIcons.PACKAGE), parent);
        this.pkg = pkg;
        
        this.signature = new SourceCodeSelection(pkg.getBinaryName() + ".**", null, null); // NOI18N
    }

    @Override
    final protected SelectorChildren getChildren() {
        return new PackageChildren();
    }

    @Override
    final public SourceCodeSelection getSignature() {
        return signature;
    }

    private List<ClassNode> getContainedClasses() {
        final List<ClassNode> nodes = new ArrayList<ClassNode>();
        
        for(SourceClassInfo clz : pkg.getClasses()) {
            nodes.add(new ClassNode(clz, this));
        }
        return nodes;
    }

    private List<PackageNode> getContainedPackages() {
        final List<PackageNode> nodes = new ArrayList<PackageNode>();
        
        for(SourcePackageInfo p : pkg.getSubpackages()) {
            nodes.add(new PackageNode(p, this));
        }
        return nodes;
    }
    
    private static String defaultizeName(String name) {
        return ((name == null) || (name.length() == 0)) ? DEFAULT_NAME : name;
    }

    private static String stripName(String name) {
        int lastDot = name.lastIndexOf('.'); // NOI18N

        if (lastDot > -1) {
            return name.substring(lastDot + 1);
        }

        return name;
    }
}

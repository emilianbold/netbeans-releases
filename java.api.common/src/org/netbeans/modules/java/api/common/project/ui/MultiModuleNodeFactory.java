/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.java.api.common.project.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.impl.MultiModule;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public final class MultiModuleNodeFactory implements NodeFactory {
    private final MultiModule sourceModules;
    private final MultiModule testModules;

    private MultiModuleNodeFactory(
            @NonNull final MultiModule sourceModules,
            @NonNull final MultiModule testModules) {
        Parameters.notNull("sourceModules", sourceModules); //NOI18N
        Parameters.notNull("testModules", testModules);     //NOI18N
        this.sourceModules = sourceModules;
        this.testModules = testModules;
    }

    @Override
    public NodeList<?> createNodes(@NonNull final Project project) {
        return new Nodes(sourceModules, testModules);
    }

    private static final class Nodes implements NodeList<ModuleKey>, PropertyChangeListener {
        private final MultiModule sourceModules;
        private final MultiModule testModules;
        private final ChangeSupport listeners;

        Nodes(
                @NonNull final MultiModule sourceModules,
                @NonNull final MultiModule testModules) {
            this.sourceModules = sourceModules;
            this.testModules = testModules;
            this.listeners = new ChangeSupport(this);
        }

        @Override
        public List<ModuleKey> keys() {
            return Stream.concat(
                    this.sourceModules.getModuleNames().stream(),
                    this.testModules.getModuleNames().stream())
                .map((name) -> new ModuleKey(name, sourceModules, testModules))
                .collect(Collectors.toList());
        }

        @Override
        public void addChangeListener(@NonNull final ChangeListener l) {
            this.listeners.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            this.listeners.removeChangeListener(l);
        }

        @Override
        public Node node(@NonNull final ModuleKey key) {
            return new ModuleNode(key);
        }

        @Override
        public void addNotify() {
            this.sourceModules.addPropertyChangeListener(this);
            this.testModules.addPropertyChangeListener(this);
        }

        @Override
        public void removeNotify() {
            this.sourceModules.removePropertyChangeListener(this);
            this.testModules.removePropertyChangeListener(this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    private static final class ModuleKey {
        private final MultiModule sourceModules;
        private final MultiModule testModules;
        private final String moduleName;

        ModuleKey(
                @NonNull final String moduleName,
                @NonNull final MultiModule sourceModules,
                @NonNull final MultiModule testModules) {
            Parameters.notNull("moduleName", moduleName);       //NOI18N
            Parameters.notNull("sourceModules", sourceModules); //NOI18N
            Parameters.notNull("testModules", testModules);     //NOI18N
            this.moduleName = moduleName;
            this.sourceModules = sourceModules;
            this.testModules = testModules;
        }

        @NonNull
        String getModuleName() {
            return moduleName;
        }

        @NonNull
        MultiModule getSourceModules() {
            return sourceModules;
        }

        @NonNull
        MultiModule getTestModules() {
            return testModules;
        }

        @Override
        public int hashCode() {
            return moduleName.hashCode();
        }

        @Override
        public boolean equals(@NullAllowed final Object other) {
            if (other == this) {
                return true;
            }
            if (other.getClass() != ModuleKey.class) {
                return false;
            }
            return ((ModuleKey)other).moduleName.equals(this.moduleName);
        }
    }

    private static final class ModuleNode extends AbstractNode {
        @StaticResource
        private static final String ICON = "org/netbeans/modules/java/api/common/project/ui/resources/module.png";
        private final MultiModule modules;
        private final MultiModule testModules;
        private final String moduleName;

        ModuleNode(@NonNull final ModuleKey key) {
            super(Children.LEAF, Lookup.EMPTY);
            this.modules = key.getSourceModules();
            this.testModules = key.getTestModules();
            this.moduleName = key.getModuleName();
            setIconBaseWithExtension(ICON);
            setName(moduleName);
        }
    }


    @NonNull
    public static MultiModuleNodeFactory create(
            @NonNull final SourceRoots sourceModules,
            @NonNull final SourceRoots srcRoots,
            @NonNull final SourceRoots testModules,
            @NonNull final SourceRoots testRoots) {
        final MultiModule mods = MultiModule.getOrCreate(sourceModules, srcRoots);
        final MultiModule testMods = MultiModule.getOrCreate(testModules, testRoots);
        return new MultiModuleNodeFactory(mods, testMods);
    }
}

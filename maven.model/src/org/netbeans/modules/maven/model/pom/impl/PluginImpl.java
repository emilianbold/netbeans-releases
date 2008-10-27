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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.model.pom.impl;

import java.util.*;
import org.w3c.dom.Element;
import org.netbeans.modules.maven.model.pom.*;	
import org.netbeans.modules.maven.model.pom.POMComponentVisitor;	

/**
 *
 * @author mkleint
 */
public class PluginImpl extends VersionablePOMComponentImpl implements Plugin {

    public PluginImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public PluginImpl(POMModel model) {
        this(model, createElementNS(model, POMQName.PLUGIN));
    }

    // attributes

    // child elements
    public java.util.List<PluginExecution> getExecutions() {
        ModelList<PluginExecution> childs = getChild(PluginExecutionImpl.List.class);
        if (childs != null) {
            return childs.getListChildren();
        }
        return null;
    }

    public void addExecution(PluginExecution execution) {
        ModelList<PluginExecution> childs = getChild(PluginExecutionImpl.List.class);
        if (childs == null) {
            setChild(DependencyImpl.List.class,
                    POMQName.EXECUTIONS.getName(),
                    getModel().getFactory().create(this, POMQName.EXECUTIONS.getQName()),
                    Collections.EMPTY_LIST);
            childs = getChild(PluginExecutionImpl.List.class);
            assert childs != null;
        }
        childs.addListChild(execution);
    }

    public void removeExecution(PluginExecution execution) {
        ModelList<PluginExecution> childs = getChild(PluginExecutionImpl.List.class);
        if (childs != null) {
            childs.removeListChild(execution);
        }
    }

    public java.util.List<Dependency> getDependencies() {
        ModelList<Dependency> childs = getChild(DependencyImpl.List.class);
        if (childs != null) {
            return childs.getListChildren();
        }
        return null;
    }

    public void addDependency(Dependency dep) {
        ModelList<Dependency> childs = getChild(DependencyImpl.List.class);
        if (childs == null) {
            setChild(DependencyImpl.List.class,
                    POMQName.DEPENDENCIES.getName(),
                    getModel().getFactory().create(this, POMQName.DEPENDENCIES.getQName()),
                    Collections.EMPTY_LIST);
            childs = getChild(DependencyImpl.List.class);
            assert childs != null;
        }
        childs.addListChild(dep);
    }

    public void removeDependency(Dependency dep) {
        ModelList<Dependency> childs = getChild(DependencyImpl.List.class);
        if (childs != null) {
            childs.removeListChild(dep);
        }
    }


    public Boolean isExtensions() {
        String str = getChildElementText(POMQName.EXTENSIONS.getQName());
        if (str != null) {
            return Boolean.valueOf(str);
        }
        return null;
    }

    public void setExtensions(Boolean extensions) {
        setChildElementText(POMQName.EXTENSIONS.getName(),
                extensions == null ? null : extensions.toString(),
                POMQName.EXTENSIONS.getQName());
    }

    public Boolean isInherited() {
        String str = getChildElementText(POMQName.INHERITED.getQName());
        if (str != null) {
            return Boolean.valueOf(str);
        }
        return null;
    }

    public void setInherited(Boolean inherited) {
        setChildElementText(POMQName.INHERITED.getName(),
                inherited == null ? null : inherited.toString(),
                POMQName.INHERITED.getQName());
    }

    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }

    public Configuration getConfiguration() {
        return getChild(Configuration.class);
    }

    public void setConfiguration(Configuration config) {
        java.util.List<Class<? extends POMComponent>> empty = Collections.emptyList();
        setChild(Configuration.class, POMQName.CONFIGURATION.getName(), config, empty);
    }
    
    public static class List extends ListImpl<Plugin> {
        public List(POMModel model, Element element) {
            super(model, element, POMQName.PLUGIN, Plugin.class);
        }

        public List(POMModel model) {
            this(model, createElementNS(model, POMQName.PLUGINS));
        }
    }

}
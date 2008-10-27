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
public class BuildBaseImpl extends POMComponentImpl implements BuildBase {

    public BuildBaseImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public BuildBaseImpl(POMModel model) {
        this(model, createElementNS(model, POMQName.BUILD));
    }

    // attributes

    // child elements
    public List<Resource> getResources() {
        ModelList<Resource> childs = getChild(ResourceImpl.ResList.class);
        if (childs != null) {
            return childs.getListChildren();
        }
        return null;
    }

    public void addResource(Resource res) {
        ModelList<Resource> childs = getChild(ResourceImpl.ResList.class);
        if (childs == null) {
            setChild(ResourceImpl.ResList.class,
                    POMQName.RESOURCES.getName(),
                    getModel().getFactory().create(this, POMQName.RESOURCES.getQName()),
                    Collections.EMPTY_LIST);
            childs = getChild(ResourceImpl.ResList.class);
            assert childs != null;
        }
        childs.addListChild(res);
    }

    public void removeResource(Resource res) {
        ModelList<Resource> childs = getChild(ResourceImpl.ResList.class);
        if (childs != null) {
            childs.removeListChild(res);
        }
    }

    public List<Resource> getTestResources() {
        ModelList<Resource> childs = getChild(ResourceImpl.TestResList.class);
        if (childs != null) {
            return childs.getListChildren();
        }
        return null;
    }

    public void addTestResource(Resource res) {
        ModelList<Resource> childs = getChild(ResourceImpl.TestResList.class);
        if (childs == null) {
            setChild(ResourceImpl.TestResList.class,
                    POMQName.TESTRESOURCES.getName(),
                    getModel().getFactory().create(this, POMQName.TESTRESOURCES.getQName()),
                    Collections.EMPTY_LIST);
            childs = getChild(ResourceImpl.TestResList.class);
            assert childs != null;
        }
        childs.addListChild(res);
    }

    public void removeTestResource(Resource res) {
        ModelList<Resource> childs = getChild(ResourceImpl.TestResList.class);
        if (childs != null) {
            childs.removeListChild(res);
        }
    }

    public PluginManagement getPluginManagement() {
        return getChild(PluginManagement.class);
    }

    public void setPluginManagement(PluginManagement pluginManagement) {
        List<Class<? extends POMComponent>> empty = Collections.emptyList();
        setChild(PluginManagement.class, POMQName.PLUGINMANAGEMENT.getName(), pluginManagement, empty);
    }

    public List<Plugin> getPlugins() {
        ModelList<Plugin> childs = getChild(PluginImpl.List.class);
        if (childs != null) {
            return childs.getListChildren();
        }
        return null;
    }

    public void addPlugin(Plugin plugin) {
        ModelList<Plugin> childs = getChild(PluginImpl.List.class);
        if (childs == null) {
            setChild(PluginImpl.List.class,
                    POMQName.PLUGINS.getName(),
                    getModel().getFactory().create(this, POMQName.PLUGINS.getQName()),
                    Collections.EMPTY_LIST);
            childs = getChild(PluginImpl.List.class);
            assert childs != null;
        }
        childs.addListChild(plugin);
    }

    public void removePlugin(Plugin plugin) {
        ModelList<Plugin> childs = getChild(PluginImpl.List.class);
        if (childs != null) {
            childs.removeListChild(plugin);
        }
    }


    public String getDefaultGoal() {
        return getChildElementText(POMQName.DEFAULTGOAL.getQName());
    }

    public void setDefaultGoal(String goal) {
        setChildElementText(POMQName.DEFAULTGOAL.getName(), goal,
                POMQName.DEFAULTGOAL.getQName());
    }

    public String getDirectory() {
        return getChildElementText(POMQName.DIRECTORY.getQName());
    }

    public void setDirectory(String directory) {
        setChildElementText(POMQName.DIRECTORY.getName(), directory,
                POMQName.DIRECTORY.getQName());
    }


    public String getFinalName() {
        return getChildElementText(POMQName.FINALNAME.getQName());
    }

    public void setFinalName(String finalName) {
        setChildElementText(POMQName.FINALNAME.getName(), finalName,
                POMQName.FINALNAME.getQName());
    }

    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }

}
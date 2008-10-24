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
import org.netbeans.modules.maven.model.pom.visitor.POMComponentVisitor;	

/**
 *
 * @author mkleint
 */
public class BuildImpl extends BuildBaseImpl implements Build {

    public BuildImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public BuildImpl(POMModel model) {
        this(model, createElementNS(model, POMQName.BUILD));
    }

    // attributes

    // child elements
    public List<Extension> getExtensions() {
        return getChildren(Extension.class);
    }

    public void addExtension(Extension plugin) {
        appendChild(EXTENSION_PROPERTY, plugin);
    }

    public void removeExtension(Extension plugin) {
        removeChild(EXTENSION_PROPERTY, plugin);
    }

    public List<Resource> getResources() {
        return getChildren(Resource.class);
    }

    public void addResource(Resource plugin) {
        appendChild(RESOURCE_PROPERTY, plugin);
    }

    public void removeResource(Resource plugin) {
        removeChild(RESOURCE_PROPERTY, plugin);
    }

    public List<TestResource> getTestResources() {
        return getChildren(TestResource.class);
    }

    public void addTestResource(TestResource plugin) {
        appendChild(TESTRESOURCE_PROPERTY, plugin);
    }

    public void removeTestResource(TestResource plugin) {
        removeChild(TESTRESOURCE_PROPERTY, plugin);
    }

    public PluginManagement getPluginManagement() {
        return getChild(PluginManagement.class);
    }

    public void setPluginManagement(PluginManagement pluginManagement) {
        List<Class<? extends POMComponent>> empty = Collections.emptyList();
        setChild(PluginManagement.class, PLUGINMANAGEMENT_PROPERTY, pluginManagement, empty);
    }

    public List<Plugin> getPlugins() {
        return getChildren(Plugin.class);
    }

    public void addPlugin(Plugin pluginManagement) {
        appendChild(PLUGIN_PROPERTY, pluginManagement);
    }

    public void removePlugin(Plugin pluginManagement) {
        removeChild(PLUGIN_PROPERTY, pluginManagement);
    }

    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }

}
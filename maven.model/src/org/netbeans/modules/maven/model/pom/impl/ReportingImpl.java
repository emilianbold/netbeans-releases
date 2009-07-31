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
public class ReportingImpl extends POMComponentImpl implements Reporting {

    public ReportingImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public ReportingImpl(POMModel model) {
        this(model, createElementNS(model, model.getPOMQNames().REPORTING));
    }

    // attributes

    // child elements
    public List<ReportPlugin> getReportPlugins() {
        ModelList<ReportPlugin> childs = getChild(ReportPluginImpl.List.class);
        if (childs != null) {
            return childs.getListChildren();
        }
        return null;
    }

    public void addReportPlugin(ReportPlugin plugin) {
        ModelList<ReportPlugin> childs = getChild(ReportPluginImpl.List.class);
        if (childs == null) {
            setChild(ReportPluginImpl.List.class,
                    getModel().getPOMQNames().REPORTPLUGINS.getName(),
                    getModel().getFactory().create(this, getModel().getPOMQNames().REPORTPLUGINS.getQName()),
                    Collections.EMPTY_LIST);
            childs = getChild(ReportPluginImpl.List.class);
            assert childs != null;
        }
        childs.addListChild(plugin);
    }

    public void removeReportPlugin(ReportPlugin plugin) {
        ModelList<ReportPlugin> childs = getChild(ReportPluginImpl.List.class);
        if (childs != null) {
            childs.removeListChild(plugin);
        }
    }

    public ReportPlugin findReportPluginById(String groupId, String artifactId) {
        assert groupId != null;
        assert artifactId != null;
        java.util.List<ReportPlugin> plgs = getReportPlugins();
        if (plgs != null) {
            for (ReportPlugin p : plgs) {
                if (groupId.equals(p.getGroupId()) && artifactId.equals(p.getArtifactId())) {
                    return p;
                }
            }
        }
        return null;
    }

    public Boolean isExcludeDefaults() {
        String str = getChildElementText(getModel().getPOMQNames().EXCLUDEDEFAULTS.getQName());
        if (str != null) {
            return Boolean.valueOf(str);
        }
        return null;
    }

    public void setExcludeDefaults(Boolean exclude) {
        setChildElementText(getModel().getPOMQNames().EXCLUDEDEFAULTS.getName(),
                exclude == null ? null : exclude.toString(),
                getModel().getPOMQNames().EXCLUDEDEFAULTS.getQName());
    }

    public String getOutputDirectory() {
        return getChildElementText(getModel().getPOMQNames().OUTPUTDIRECTORY.getQName());
    }

    public void setOutputDirectory(String directory) {
        setChildElementText(getModel().getPOMQNames().OUTPUTDIRECTORY.getName(), directory,
                getModel().getPOMQNames().OUTPUTDIRECTORY.getQName());
    }

    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }
}
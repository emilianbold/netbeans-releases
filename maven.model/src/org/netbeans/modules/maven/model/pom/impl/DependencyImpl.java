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
public class DependencyImpl extends VersionablePOMComponentImpl implements Dependency {

    public DependencyImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public DependencyImpl(POMModel model) {
        this(model, createElementNS(model, model.getPOMQNames().DEPENDENCY));
    }

    // attributes

    // child elements
    public java.util.List<Exclusion> getExclusions() {
        ModelList<Exclusion> childs = getChild(ExclusionImpl.List.class);
        if (childs != null) {
            return childs.getListChildren();
        }
        return null;
    }

    public void addExclusion(Exclusion exclusion) {
        ModelList<Exclusion> childs = getChild(ExclusionImpl.List.class);
        if (childs == null) {
            setChild(ExclusionImpl.List.class,
                    getModel().getPOMQNames().EXCLUSIONS.getName(),
                    getModel().getFactory().create(this, getModel().getPOMQNames().EXCLUSIONS.getQName()),
                    Collections.EMPTY_LIST);
            childs = getChild(ExclusionImpl.List.class);
            assert childs != null;
        }
        childs.addListChild(exclusion);
    }

    public void removeExclusion(Exclusion exclusion) {
        ModelList<Exclusion> childs = getChild(ExclusionImpl.List.class);
        if (childs != null) {
            childs.removeListChild(exclusion);
        }
    }

    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }

    public String getType() {
        return getChildElementText(getModel().getPOMQNames().TYPE.getQName());
    }

    public void setType(String type) {
        setChildElementText(getModel().getPOMQNames().TYPE.getName(), type,
                getModel().getPOMQNames().TYPE.getQName());
    }

    public String getClassifier() {
        return getChildElementText(getModel().getPOMQNames().CLASSIFIER.getQName());
    }

    public void setClassifier(String classifier) {
        setChildElementText(getModel().getPOMQNames().CLASSIFIER.getName(), classifier,
                getModel().getPOMQNames().CLASSIFIER.getQName());
    }

    public String getScope() {
        return getChildElementText(getModel().getPOMQNames().SCOPE.getQName());
    }

    public void setScope(String scope) {
        setChildElementText(getModel().getPOMQNames().SCOPE.getName(), scope,
                getModel().getPOMQNames().SCOPE.getQName());
    }

    public String getSystemPath() {
        return getChildElementText(getModel().getPOMQNames().SYSTEMPATH.getQName());
    }

    public void setSystemPath(String systemPath) {
        setChildElementText(getModel().getPOMQNames().SYSTEMPATH.getName(), systemPath,
                getModel().getPOMQNames().SYSTEMPATH.getQName());
    }

    public Boolean isOptional() {
        String str = getChildElementText(getModel().getPOMQNames().OPTIONAL.getQName());
        if (str != null) {
            return Boolean.valueOf(str);
        }
        return null;
    }

    public void setOptional(Boolean optional) {
        setChildElementText(getModel().getPOMQNames().OPTIONAL.getName(),
                optional == null ? null : optional.toString(),
                getModel().getPOMQNames().OPTIONAL.getQName());
    }

    public Exclusion findExclusionById(String groupId, String artifactId) {
        assert groupId != null;
        assert artifactId != null;
        java.util.List<Exclusion> excs = getExclusions();
        if (excs != null) {
            for (Exclusion e : excs) {
                if (groupId.equals(e.getGroupId()) && artifactId.equals(e.getArtifactId())) {
                    return e;
                }
            }
        }
        return null;
    }

    public static class List extends ListImpl<Dependency> {
        public List(POMModel model, Element element) {
            super(model, element, model.getPOMQNames().DEPENDENCY, Dependency.class);
        }

        public List(POMModel model) {
            this(model, createElementNS(model, model.getPOMQNames().DEPENDENCIES));
        }
    }


}
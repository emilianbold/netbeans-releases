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
public class DependencyImpl extends VersionablePOMComponentImpl implements Dependency {

    public DependencyImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public DependencyImpl(POMModel model) {
        this(model, createElementNS(model, POMQName.DEPENDENCY));
    }

    // attributes

    // child elements
    public List<Exclusion> getExclusions() {
        return getChildren(Exclusion.class);
    }

    public void addExclusion(Exclusion exclusion) {
        appendChild(EXCLUSION_PROPERTY, exclusion);
    }

    public void removeExclusion(Exclusion exclusion) {
        removeChild(EXCLUSION_PROPERTY, exclusion);
    }

    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }

    public String getType() {
        return getChildElementText(POMQName.TYPE.getQName());
    }

    public void setType(String type) {
        setChildElementText(POMQName.TYPE.getQName().getLocalPart(), type,
                POMQName.TYPE.getQName());
    }

    public String getClassifier() {
        return getChildElementText(POMQName.CLASSIFIER.getQName());
    }

    public void setClassifier(String classifier) {
        setChildElementText(POMQName.CLASSIFIER.getQName().getLocalPart(), classifier,
                POMQName.CLASSIFIER.getQName());
    }

    public String getScope() {
        return getChildElementText(POMQName.SCOPE.getQName());
    }

    public void setScope(String scope) {
        setChildElementText(POMQName.SCOPE.getQName().getLocalPart(), scope,
                POMQName.SCOPE.getQName());
    }

    public String getSystemPath() {
        return getChildElementText(POMQName.SYSTEMPATH.getQName());
    }

    public void setSystemPath(String systemPath) {
        setChildElementText(POMQName.SYSTEMPATH.getQName().getLocalPart(), systemPath,
                POMQName.SYSTEMPATH.getQName());
    }

    public Boolean isOptional() {
        String str = getChildElementText(POMQName.OPTIONAL.getQName());
        if (str != null) {
            return Boolean.valueOf(str);
        }
        return Boolean.FALSE;
    }

    public void setOptional(Boolean optional) {
        setChildElementText(POMQName.OPTIONAL.getQName().getLocalPart(),
                optional == null ? null : optional.toString(),
                POMQName.OPTIONAL.getQName());
    }

}
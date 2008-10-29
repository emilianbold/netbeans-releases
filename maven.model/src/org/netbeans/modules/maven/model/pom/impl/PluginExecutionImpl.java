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

import java.util.Collections;
import org.w3c.dom.Element;
import org.netbeans.modules.maven.model.pom.*;	
import org.netbeans.modules.maven.model.pom.POMComponentVisitor;	
import org.w3c.dom.NodeList;

/**
 *
 * @author mkleint
 */
public class PluginExecutionImpl extends IdPOMComponentImpl implements PluginExecution {

    public PluginExecutionImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public PluginExecutionImpl(POMModel model) {
        this(model, createElementNS(model, POMQName.EXECUTION));
    }

    // attributes

    // child elements
    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }

    public String getPhase() {
        return getChildElementText(POMQName.PHASE.getQName());
    }

    public void setPhase(String phase) {
        setChildElementText(POMQName.PHASE.getName(), phase,
                POMQName.PHASE.getQName());
    }

    public Boolean isInherited() {
        String str = getChildElementText(POMQName.INHERITED.getQName());
        if (str != null) {
            return Boolean.valueOf(str);
        }
        return Boolean.TRUE;
    }

    public void setInherited(Boolean inherited) {
        setChildElementText(POMQName.INHERITED.getName(),
                inherited == null ? null : inherited.toString(),
                POMQName.INHERITED.getQName());
    }

    public Configuration getConfiguration() {
        return getChild(Configuration.class);
    }

    public void setConfiguration(Configuration config) {
        java.util.List<Class<? extends POMComponent>> empty = Collections.emptyList();
        setChild(Configuration.class, POMQName.CONFIGURATION.getName(), config, empty);
    }

    public java.util.List<String> getGoals() {
        java.util.List<StringList> lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (POMQName.GOALS.getName().equals(list.getPeer().getNodeName())) {
                return list.getListChildren();
            }
        }
        return null;
    }

    public void addGoal(String goal) {
        java.util.List<StringList> lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (POMQName.GOALS.getName().equals(list.getPeer().getNodeName())) {
                list.addListChild(goal);
                return;
            }
        }
        setChild(StringListImpl.class,
                 POMQName.GOALS.getName(),
                 getModel().getFactory().create(this, POMQName.GOALS.getQName()),
                 Collections.EMPTY_LIST);
        lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (POMQName.GOALS.getName().equals(list.getPeer().getNodeName())) {
                list.addListChild(goal);
                return;
            }
        }
    }

    public void removeGoal(String goal) {
        java.util.List<StringList> lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (POMQName.GOALS.getName().equals(list.getPeer().getNodeName())) {
                list.removeListChild(goal);
                return;
            }
        }
    }

    public static class List extends ListImpl<PluginExecution> {
        public List(POMModel model, Element element) {
            super(model, element, POMQName.EXECUTION, PluginExecution.class);
        }

        public List(POMModel model) {
            this(model, createElementNS(model, POMQName.EXECUTIONS));
        }
    }

}
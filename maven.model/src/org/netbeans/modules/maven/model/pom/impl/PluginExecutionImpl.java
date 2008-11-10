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

import org.w3c.dom.Element;
import org.netbeans.modules.maven.model.pom.*;	

/**
 *
 * @author mkleint
 */
public class PluginExecutionImpl extends IdPOMComponentImpl implements PluginExecution {

    private static final Class<? extends POMComponent>[] ORDER = new Class[] {
        StringList.class, //goals
        Configuration.class
    };

    public PluginExecutionImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public PluginExecutionImpl(POMModel model) {
        this(model, createElementNS(model, model.getPOMQNames().EXECUTION));
    }

    // attributes

    // child elements
    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }

    public String getPhase() {
        return getChildElementText(getModel().getPOMQNames().PHASE.getQName());
    }

    public void setPhase(String phase) {
        setChildElementText(getModel().getPOMQNames().PHASE.getName(), phase,
                getModel().getPOMQNames().PHASE.getQName());
    }

    public Boolean isInherited() {
        String str = getChildElementText(getModel().getPOMQNames().INHERITED.getQName());
        if (str != null) {
            return Boolean.valueOf(str);
        }
        return Boolean.TRUE;
    }

    public void setInherited(Boolean inherited) {
        setChildElementText(getModel().getPOMQNames().INHERITED.getName(),
                inherited == null ? null : inherited.toString(),
                getModel().getPOMQNames().INHERITED.getQName());
    }

    public Configuration getConfiguration() {
        return getChild(Configuration.class);
    }

    public void setConfiguration(Configuration config) {
        setChild(Configuration.class, getModel().getPOMQNames().CONFIGURATION.getName(), config,
                getClassesBefore(ORDER, Configuration.class));
    }

    public java.util.List<String> getGoals() {
        java.util.List<StringList> lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (getModel().getPOMQNames().GOALS.getName().equals(list.getPeer().getNodeName())) {
                return list.getListChildren();
            }
        }
        return null;
    }

    public void addGoal(String goal) {
        java.util.List<StringList> lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (getModel().getPOMQNames().GOALS.getName().equals(list.getPeer().getNodeName())) {
                list.addListChild(goal);
                return;
            }
        }
        setChild(StringListImpl.class,
                 getModel().getPOMQNames().GOALS.getName(),
                 getModel().getFactory().create(this, getModel().getPOMQNames().GOALS.getQName()),
                 getClassesBefore(ORDER, StringListImpl.class));
        lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (getModel().getPOMQNames().GOALS.getName().equals(list.getPeer().getNodeName())) {
                list.addListChild(goal);
                return;
            }
        }
    }

    public void removeGoal(String goal) {
        java.util.List<StringList> lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (getModel().getPOMQNames().GOALS.getName().equals(list.getPeer().getNodeName())) {
                list.removeListChild(goal);
                return;
            }
        }
    }

    public static class List extends ListImpl<PluginExecution> {
        public List(POMModel model, Element element) {
            super(model, element, model.getPOMQNames().EXECUTION, PluginExecution.class);
        }

        public List(POMModel model) {
            this(model, createElementNS(model, model.getPOMQNames().EXECUTIONS));
        }
    }

}
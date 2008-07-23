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
package org.netbeans.modules.ruby.rubyproject.rake;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Represent <em>task</em> or <em>namespace</em> element from Rakefile.
 * <p>
 * <strong>TODO</strong>: divide into <tt>RakeTask</tt> and <tt>Namespace</tt>
 * classes.
 */
public final class RakeTask implements Comparable<RakeTask> {

    private final String task;
    private final String description;
    private final String displayName;
    private final List<String> taskParameters = new ArrayList<String>();
    private final List<String> rakeParameters = new ArrayList<String>();

    private Set<RakeTask> children;

    public static RakeTask newNameSpace(final String displayName) {
        return new RakeTask(null, displayName, null);
    }

    public RakeTask(String task, String name, String description) {
        this.task = task;
        this.displayName = name;
        this.description = description;
    }

    boolean isNameSpace() {
        return task == null;
    }

    public void addRakeParameters(String... params) {
        for (String param : params) {
            rakeParameters.add(param);
        }
    }

    public void addTaskParameters(String... params) {
        for (String param : params) {
            taskParameters.add(param);
        }
    }

    List<String> getTaskParameters() {
        return taskParameters;
    }

    List<String> getRakeParameters() {
        return rakeParameters;
    }

    /**
     * Useful only for <em>task</em>, return <tt>null</tt> for
     * <em>namespace</em>.
     *
     * @return full name containing possible namespace(s), used for invoking a
     *         task, e.g. <tt>test:coverage</tt>, <tt>db:migrate</tt>
     */
    public String getTask() {
        return task;
    }

    public Set<RakeTask> getChildren() {
        return children;
    }

    /**
     * @return description represented by <tt>:desc</tt> in the Rakefile.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Useful only for <em>task</em>, return <tt>null</tt> for
     * <em>namespace</em>.
     *
     * @return task name without namespace
     */
    public String getDisplayName() {
        return displayName;
    }

    public void addChild(RakeTask child) {
        if (children == null) {
            children = new TreeSet<RakeTask>();
        }

        children.add(child);
    }

    public @Override boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RakeTask other = (RakeTask) obj;
        if (this.task != other.task && (this.task == null || !this.task.equals(other.task))) {
            return false;
        }
        return true;
    }

    public @Override int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.task != null ? this.task.hashCode() : 0);
        return hash;
    }

    public int compareTo(final RakeTask o) {
        // until this is devided into RakeTask and Namespace classes
        if (isNameSpace() && !o.isNameSpace()) {
            return -1;
        }
        if (!isNameSpace() && o.isNameSpace()) {
            return 1;
        }

        if (task == null || o.getTask() == null) {
            assert displayName != null : "displayName not null";
            assert o.getDisplayName() != null : "other displayName not null";
            return displayName.compareTo(o.getDisplayName());
        }
        return this.getTask().compareTo(o.getTask());
    }

    public @Override String toString() {
        return "RakeTask[task: " + getTask() + ", displayName: " + getDisplayName() + ", description: " + getDescription() + ']'; // NOI18N
    }

}

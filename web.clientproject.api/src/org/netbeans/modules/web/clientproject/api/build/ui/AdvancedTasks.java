/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.clientproject.api.build.ui;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.util.Parameters;

public final class AdvancedTasks {

    private static final Logger LOGGER = Logger.getLogger(AdvancedTasks.class.getName());

    private static final int TASKS_NUMBER = 3;
    private static final String TASK_KEY = "%s.task.%d"; // NOI18N
    private static final String NAMESPACE_TASK_KEY = "%s.%s.task.%d"; // NOI18N

    private final Project project;
    private final String ident;
    private final String namespace;


    AdvancedTasks(Project project, String ident, @NullAllowed String namespace) {
        Parameters.notNull("project", project); // NOI18N
        Parameters.notEmpty("ident", ident); // NOI18N
        this.project = project;
        this.ident = ident;
        this.namespace = namespace == null ? null : getHash(namespace);
    }

    List<String> getTasks() {
        Set<String> tasks = new LinkedHashSet<>();
        // public
        tasks.addAll(getTasks(getPreferences(true)));
        // private
        tasks.addAll(getTasks(getPreferences(false)));
        return new ArrayList<>(tasks);
    }

    void addTask(boolean shared, String task) {
        Preferences preferences = getPreferences(shared);
        // load
        List<String> tasks = getTasks(preferences);
        tasks.add(0, task);
        // unique
        tasks = new ArrayList<>(new LinkedHashSet<>(tasks));
        // shorten
        while (tasks.size() > TASKS_NUMBER) {
            tasks.remove(tasks.size() - 1);
        }
        // save
        for (int i = 0; i < tasks.size(); i++) {
            preferences.put(getKey(i + 1), tasks.get(i));
        }
    }

    private List<String> getTasks(Preferences preferences) {
        List<String> tasks = new ArrayList<>(TASKS_NUMBER);
        for (int i = 1; i <= TASKS_NUMBER; ++i) {
            String task = preferences.get(getKey(i), null);
            if (task != null) {
                tasks.add(task);
            }
        }
        return tasks;
    }

    private Preferences getPreferences(boolean shared) {
        return ProjectUtils.getPreferences(project, AdvancedTasks.class, shared);
    }

    private String getKey(int index) {
        if (namespace == null) {
            return String.format(TASK_KEY, ident, index);
        }
        return String.format(NAMESPACE_TASK_KEY, ident, namespace, index);
    }

    private String getHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5"); // NOI18N
            byte[] hash = md.digest(input.getBytes("UTF-8")); // NOI18N
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(Integer.toHexString((int) (b & 0xff)));
            }
            String result = sb.toString();
            LOGGER.log(Level.FINE, "Hashing \"{0}\" to \"{1}\"", new Object[] {input, result});
            return result;
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        LOGGER.log(Level.FINE, "No hashing for \"{0}\"", input);
        return input;
    }

}

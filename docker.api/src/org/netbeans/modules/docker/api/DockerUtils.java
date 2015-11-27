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
package org.netbeans.modules.docker.api;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.docker.api.remote.DockerEvent;

/**
 *
 * @author Petr Hejl
 */
public final class DockerUtils {

    public static final String DOCKER_FILE = "Dockerfile"; // NOI18N

    private DockerUtils() {
        super();
    }

    public static String getShortId(Identifiable identifiable) {
        return getShortId(identifiable.getId());
    }

    public static String getShortId(String id) {
        if (id.length() > 12) {
            return id.substring(0, 12);
        }
        return id;
    }

    public static String getImage(DockerTag tag) {
        String id = tag.getTag();
        if (id.equals("<none>:<none>")) { // NOI18N
            id = tag.getImage().getId();
        }
        return id;
    }

    public static String getTag(String repository, String tag) {
        if (repository == null) {
            return "<none>:<none>";
        }
        if (tag == null) {
            return repository + ":latest";
        }
        return repository + ":" + tag;
    }

    public static ContainerStatus getContainerStatus(String status) {
        if (status == null) {
            return ContainerStatus.STOPPED;
        }
        if (!status.startsWith("Up")) { // NOI18N
            return ContainerStatus.STOPPED;
        }
        if (!status.contains("Paused")) { // NOI18N
            return ContainerStatus.RUNNING;
        }
        return ContainerStatus.PAUSED;
    }

    @CheckForNull
    public static ContainerStatus getContainerStatus(DockerEvent event) {
        return getContainerStatus(event.getStatus());
    }

    @CheckForNull
    public static ContainerStatus getContainerStatus(DockerEvent.Status status) {
        switch (status) {
            case DIE:
                return ContainerStatus.STOPPED;
            case START:
                return ContainerStatus.RUNNING;
            case PAUSE:
                return ContainerStatus.PAUSED;
            case UNPAUSE:
                return ContainerStatus.RUNNING;
            default:
                return null;
        }
    }

    @NonNull
    public static String appendLatestTag(String image) {
        String ret = image;
        if (!image.contains(":") && !image.contains("@")) { // NOI18N
            ret += ":latest"; // NOI18N
        }
        return ret;
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.api;

import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.netbeans.modules.kenai.api.KenaiService.Type;

/**
 * Instant notification about project change on Kenai
 * @author Jan Becicka
 */
public final class KenaiNotification {
    private Date stamp;
    private KenaiService.Type type;
    private URI uri;
    private String author;
    private String service;

    private List<Modification> modifications;

    /**
     * Creates new instance of notification. You probably don't need to create your own.
     * Listen on KenaiProject#PROP_PROJECT_NOTIFICATION
     *
     * @param stamp time stamp of change
     * @param type type of change
     * @param uri uri of change
     * @param author author of change
     * @param service service name
     * @param modifications modifications in this change
     */
    public KenaiNotification(Date stamp, Type type, URI uri, String author, String service, List<Modification> modifications) {
        this.stamp = stamp;
        this.type = type;
        this.uri = uri;
        this.author = author;
        this.service = service;
        this.modifications = Collections.unmodifiableList(modifications);
    }

    /**
     * getter for modifications
     * @return unmodifiable list of modifications
     */
    public List<Modification> getModifications() {
        return modifications;
    }

    /**
     * getter for author of this change
     * @return
     */
    public String getAuthor() {
        return author;
    }

    /**
     * getter for time stamp of this change
     * @return
     */
    public Date getStamp() {
        return stamp;
    }

    /**
     * getter for service type
     * @return
     */
    public KenaiService.Type getType() {
        return type;
    }

    /**
     * getter for source uri of this change
     * @return
     */
    public URI getUri() {
        return uri;
    }

    /**
     * getter for name of the service
     * @return
     */
    public String getServiceName() {
        return service;
    }

    public static final class Modification {

        private String resource;
        private String id;
        private Type type;

        /**
         * Type of Modification
         */
        public static enum Type {
            ADD,
            CHANGE,
            REMOVE
        }

        public Modification(String resource, String id, Type type) {
            this.resource = resource;
            this.id = id;
            this.type = type;
        }
        /**
         * Get the value of resource
         *
         * @return the value of resource
         */
        public String getResource() {
            return resource;
        }

        /**
         * Get the value of type
         *
         * @return the value of type
         */
        public Type getType() {
            return type;
        }

        /**
         * Get the value of id
         *
         * @return the value of id
         */
        public String getId() {
            return id;
        }
    }
}

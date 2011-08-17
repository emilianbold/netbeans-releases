/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence.server;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.server.properties.InstanceProperties;

/**
 * Holds basic Coherence and Coherence plugin properties.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class CoherenceProperties {

    /**
     * Key for Coherence instance ID in {@link InstanceProperties}.
     */
    public static final String PROP_COHERENCE_ID = "base.coherence.id"; //NOI18N

    /**
     * Key for Coherence location path in {@link InstanceProperties}.
     */
    public static final String PROP_COHERENCE_LOCATION = "base.coherence.location"; //NOI18N

    /**
     * Key for Coherence instance display name in {@link InstanceProperties}.
     */
    public static final String PROP_DISPLAY_NAME = "base.displayName"; // NOI18N

    /**
     * Key for Coherence instance classpath in {@link InstanceProperties}. It includes
     * Coherence base JAR as well as additional Coherence or another JARs.
     */
    public static final String PROP_COHERENCE_CLASSPATH = "base.coherence.classpath"; //NOI18N

    /**
     * Key for Coherence instance java flags in {@link InstanceProperties}.
     */
    public static final String PROP_JAVA_FLAGS = "base.java.flags"; //NOI18N

    /**
     * Key for Coherence instance custom properties in {@link InstanceProperties}.
     */
    public static final String PROP_CUSTOM_PROPERTIES = "base.custom.properties"; //NOI18N

    /**
     * Display name of Coherence server.
     */
    public static final String DISPLAY_NAME_DEFAULT = "Oracle Coherence"; // NOI18N

    /**
     * Delimiter for serializing additional libraries from array to string and opposite.
     */
    public static final String CLASSPATH_SEPARATOR = "#@#"; //NOI18N

    /**
     * File name of base Coherence JAR file.
     */
    public static final String COHERENCE_JAR_NAME = "coherence.jar"; //NOI18N

    /**
     * Contains all Coherence server command line properties.
     */
    public static final List<CoherenceServerProperty> SERVER_PROPERTIES = new ArrayList<CoherenceServerProperty>();

    static {
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.cacheconfig", "Cache configuration descriptor filename", String.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.cluster", "Cluster name", String.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.clusteraddress", "Cluster (multicast) IP address", String.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.clusterport", "Cluster (multicast) IP port", Long.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.distributed.backup", "Data backup storage location", String.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.distributed.backupcount", "Number of data backups", Integer.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.distributed.localstorage", "Local partition management enabled", Boolean.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.distributed.threads", "Thread pool size", Integer.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.distributed.transfer", "Partition transfer threshold", Long.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.edition", "Product edition", String.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.invocation.threads", "Invocation service thread pool size", Integer.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.localhost", "Unicast IP address", String.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.localport", "Unicast IP port", Long.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.localport.adjust", "Unicast IP port auto assignment", Boolean.class, "true"));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.log", "Logging destination", String.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.log.level", "Logging level", String.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.log.limit", "Log output character limit", Long.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.machine", "Machine name", String.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.management", "JMX management mode", String.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.management.readonly", "JMX management read-only flag", Boolean.class, "false"));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.management.remote", "Remote JMX management enabled flag", Boolean.class, "false"));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.member", "Member name", String.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.mode", "Operational mode", String.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.override", "Deployment configuration override filename", String.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.priority", "Priority", Integer.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.process", "Process name", String.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.proxy.threads", "Coherence*Extend service thread pool size", Integer.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.rack", "Rack name", String.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.role", "Role name", String.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.security", "Cache access security enabled flag", Boolean.class, "false"));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.security.keystore", "Security access controller keystore file name", String.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.security.password", "Keystore or cluster encryption password", String.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.security.permissions", "Security access controller permissions file name", String.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.shutdownhook", "Shutdown listener action", String.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.site", "Site name", String.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.tcmp.enabled", "TCMP enabled flag", Boolean.class, "true"));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.tcpring", "TCP Ring enabled flag", Boolean.class, "false"));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.ttl", "Multicast packet time to live (TTL)", Long.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.wka", "Well known IP address", String.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.wka.port", "Well known IP port", Long.class));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.pof.enabled", "Enable POF Serialization", Boolean.class, "false"));
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.pof.config", "Configuration file containing POF Serialization class information", String.class));
    }

}

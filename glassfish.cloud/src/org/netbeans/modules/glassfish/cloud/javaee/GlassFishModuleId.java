/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.cloud.javaee;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import org.netbeans.modules.glassfish.cloud.data.GlassFishAccountInstance;

/**
 * Unique identifier for a deployed application module on GlassFish server.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class GlassFishModuleId implements TargetModuleID {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Empty deployed application module array. */
    private static final TargetModuleID[] EMPTY_ARRAY = new TargetModuleID[0];

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** URL of web module. */
    private URL url;

    /** ID assigned to represent the deployed module. */
    private String id;

    /** Name of the target server this module was deployed to. */
    private Target target;

    /** Identifier of the parent object of this deployed module. */
    private GlassFishModuleId parent;
    
    /** List of identifiers of the children of this deployed module. */
    private List<GlassFishModuleId> children;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Crates an instance of unique identifier for a deployed application module
     * on GlassFish server.
     * <p/>
     * @param url      URL of web module.
     * @param id       ID assigned to represent the deployed module.
     * @param target   Name of the target server this module was deployed to.
     * @param children List of identifiers of the children of this deployed
     *                 module.
     */
    public GlassFishModuleId(String url, String id, Target target,
            Collection<GlassFishModuleId> children) {
        try {
            this.url = url != null ? new URL(url) : null;
        } catch (MalformedURLException mue) {
            throw new IllegalArgumentException(mue);
        }
        this.id = id;
        this.target = target;
        this.parent = null;
        this.children = children != null
                ? new LinkedList<GlassFishModuleId>(children)
                : new LinkedList<GlassFishModuleId>();
    }
    
    /**
     * Convenient constructor of unique identifier for a deployed application module
     * on GlassFish server.
     * @param target Target server where the module is deployed.
     * @param module 
     */
    public GlassFishModuleId(GlassFishAccountInstance target, File module) {
        this.url = null;
        if (module.getName().endsWith(".war")) {
            try {
            this.url = constructUrl("http", target.getCloudEntity().getHost(), 
                    target.getCloudEntity().getPort(), null);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        }
        this.id = id;
        this.target = target;
        this.parent = null;
        this.children = null;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Implemented interface methods                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Retrieve name of the target server this module was deployed to. 
     * <p/>
     * @return Target an object representing a server target.
     */
    @Override
    public Target getTarget() {
        return target;
    }

    /**
     * Retrieve the ID assigned to represent the deployed module.
     * <p/>
     * @return ID assigned to represent the deployed module.
     */
    @Override
    public String getModuleID() {
        return id;
    }

    /**
     * If this TargetModulID represents web module retrieve the URL for it.
     * <p/>
     * @return URL of web module or <code>null</code> if the module is not
     *         web module.
     */
    @Override
    public String getWebURL() {
        return url.toString();
    }

    /**
     * Retrieve identifier of parent object of this deployed module.
     * <p/>
     * If there is no parent then this is the root object deployed. The root
     * could represent an EAR file or it could be a stand alone module that
     * was deployed.
     * <p/>
     * @return TargetModuleID of the parent of this object.
     *         <code>null</code> value means this module is the root object
     *         deployed.
     */
    @Override
    public TargetModuleID getParentTargetModuleID() {
        return parent;
    }

    /**
     * Retrieve List of identifiers of children of this deployed module.
     * <p/>
     * @return List of TargetModuleIDs identifying children of this object.
     *         <code>null</code> value means this module has no children.
     */
    @Override
    public TargetModuleID[] getChildTargetModuleID() {
        return children.isEmpty() ? children.toArray(EMPTY_ARRAY) : null;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Sets identifier of parent object of this deployed module.
     * <p/>
     * @param parent Identifier of parent object of this deployed module to
     *               be set.
     */
    public void setParentTargetModuleID(GlassFishModuleId parent) {
        this.parent = parent;
    }

    /**
     * Sets identifier of the children of this deployed module.
     * <p/>
     * Also sets parent identifier of child object to this instance.
     * <p/>
     * @param child Identifier of the children of this deployed module to
     *              be set.
     */
    public void addChildTargetModuleID(GlassFishModuleId child) {
        children.add(child);
        child.setParentTargetModuleID(this);
    }
    
    
     ////////////////////////////////////////////////////////////////////////////
    // Helper methods                                       //
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Constructs URL for given attributes.
     * 
     * @param protocol
     * @param host
     * @param port
     * @param contextRoot - has to begin with slash
     * @return 
     */
    private static URL constructUrl(String protocol, String host, Integer port, String contextRoot) throws MalformedURLException {
        StringBuilder builder = new StringBuilder(128);
        builder.append(protocol);
        builder.append("://"); // NOI18N
        builder.append(host);
        builder.append(":"); // NOI18N
        builder.append(port);
        if (contextRoot != null && contextRoot.length() > 0) {
            builder.append(contextRoot);
        }
        return new URL(builder.toString());
    }

}

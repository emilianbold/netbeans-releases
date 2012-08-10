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

import java.awt.Image;
import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.glassfish.tools.ide.data.GlassFishVersion;
import org.glassfish.tools.ide.server.config.LibraryBuilder;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.glassfish.cloud.data.GlassFishCloudInstanceProvider;
import org.netbeans.modules.glassfish.cloud.data.GlassFishInstance;
import org.netbeans.modules.glassfish.cloud.data.GlassFishUrl;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl2;
import org.netbeans.spi.project.libraries.LibraryImplementation;

/**
 * Common Java EE platform SPI interface implementation for Java EE platform
 * registered with GlassFish cloud.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public abstract class GlassFishPlatformImpl
        extends J2eePlatformImpl2 implements ChangeListener {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Library builder default configuration file. */
    private static final URL LIBRARY_BUILDER_CONFIG_DEFAULT
            = GlassFishPlatformImpl.class.getResource("gfLibsDefault.xml");

    /** Library builder configuration since GlassFish 4. */
    private static final LibraryBuilder.Config LIBRARY_BUILDER_CONFIG_4
            = new LibraryBuilder.Config(GlassFishVersion.GF_4,
            GlassFishPlatformImpl.class.getResource("gfLibs4.xml"));

    // Now there is only GlassFish 4 so we have single option to return.
    /** Set of Java platforms supported by GlassFish cloud. */
    static final Set<String> JAVA_PLATFORMS = new HashSet<String>();
    static {
        JAVA_PLATFORMS.add("1.6");
        JAVA_PLATFORMS.add("1.7");
    }

    ////////////////////////////////////////////////////////////////////////////
    // Library builder initialization.                                        //
    ////////////////////////////////////////////////////////////////////////////

    static {
        LibraryBuilder.init(LIBRARY_BUILDER_CONFIG_DEFAULT,
                LIBRARY_BUILDER_CONFIG_4);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish cloud URL. */
    final GlassFishUrl url;

    /** GlassFish cloud local instance. */
    GlassFishInstance instance;

    /** Library builder associated with current platform.
      * This attribute should be accessed only using {@see #getBuilder()} even
      * internally. */
    private volatile LibraryBuilder builder;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of common Java EE platform registered with GlassFish
     * cloud.
     * <p/>
     * Initializes common Java EE platform attributes.
     * <p/>
     * @param url GlassFish cloud URL.
     */
    @SuppressWarnings("LeakingThisInConstructor")
    GlassFishPlatformImpl(GlassFishUrl url) {
        this.url = url;
        instance = GlassFishCloudInstanceProvider
                .getCloudInstance(url.getName());        
        if (instance == null || instance.getLocalServer() == null) {
            throw new NullPointerException("GlassFish local server instance "
                    + url.getName() + "does not exist.");
        }
        instance.addChangeListener(this, new GlassFishInstance.Event[]{
            GlassFishInstance.Event.STORE, GlassFishInstance.Event.REMOVE});
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented Interface Methods                                          //
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Return a set of J2SE platform versions GlassFish cloud can run with.
     * <p/>
     * This method should be updated once there will be more options to return.
     * <p/>
     * @return Set of J2SE platform versions GlassFish cloud can run with.
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Set getSupportedJavaPlatformVersions() {
        // Now there is only GlassFish 4 so we have single option to return.
        return JAVA_PLATFORMS;
    }

    /**
     * Returns the GlassFish cloud local server installation directory
     * or <code>null</code> if not specified or unknown.
     * <p/>
     * @return The server installation directory or <code>null</code> if not
     *         specified or unknown
     */
    @Override
    public File getServerHome() {        
        return instance.getLocalServer().getServerHome() != null
                ? new File(instance.getLocalServer().getServerHome())
                : null;
    }

    /**
     * Returns the GlassFish cloud local domain directory or <code>null</code>
     * if not specified or unknown.
     * <p/>
     * There is no domain registered with local GlassFish server now.
     * Internal </code>donainsFolder</code> is always set to <code>null</code>.
     * <p/>
     * @return The domain directory or <code>null</code> if not specified
     *         or unknown.
     */    
    @Override
    public File getDomainHome() {
        return instance.getLocalServer().getDomainsFolder() != null
                ? new File(instance.getLocalServer().getDomainsFolder())
                : null;
    }

    /**
     * Returns the middleware directory or <code>null</code> if not specified
     * or unknown.
     * <p/>
     * Middleware directory is not recognized in GlassFish cloud local server
     * registration. this method always returns <code>null</code>.
     * <p/>
     * @return The middleware directory or <code>null</code> if not
     *         specified or unknown
     */
    @Override
    public File getMiddlewareHome() {
        return null;
    }

    @Override
    public LibraryImplementation[] getLibraries() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Return GlassFish cloud platform display name.
     * <p/>
     * @return GlassFish cloud platform display name.
     */
    @Override
    public String getDisplayName() {
        return instance.getServerDisplayName();
    }

    /**
     * Return an icon describing GlassFish cloud local server platform.
     * <p/>
     * @return An icon describing GlassFish cloud platform.
     */
    @Override
    public Image getIcon() {
        return instance.getBasicNode().getIcon(1);
    }

    /**
     * Return GlassFish cloud platform root directories.
     * <p/>
     * Returns <code>File[1]</code> array containing GlassFish cloud local
     * server home directory.
     * <p/>
     * @return GlassFish cloud platform root directories.
     */
    @Override
    public File[] getPlatformRoots() {
        return instance.getLocalServer().getServerHome() != null
                ? new File[] {
                    new File(instance.getLocalServer().getServerHome()) }
                : new File[0];
    }

    /**
     * Return GlassFish cloud J2SE platform.
     * <p/>
     * Now this method returns default J2SE platform set in NEtBeans. In the
     * future it may return <code>AS_JAVA</code> value set in
     * <code>asenv.conf</code> or <code>asenv.bat</code> files.
     * <p/>
     * @return Default J2SE platform set in NetBeans.
     */
    @Override
    public org.netbeans.api.java.platform.JavaPlatform getJavaPlatform() {
        return JavaPlatformManager.getDefault().getDefaultPlatform();
    }

    /**
     * Return class path for the specified tool.
     * <p/>
     * GlassFish cloud platform does not support tools at this moment. Will
     * always return zero length <code>File</code> array.
     * </p>
     * @param  toolName Tool name, for example
     *         {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform#TOOL_APP_CLIENT_RUNTIME}.
     * @return Class path for the specified tool.
     */
    @Override
    public File[] getToolClasspathEntries(String toolName) {
        return new File[0];
    }

    /**
     * Specifies whether a tool of the given name is supported by GlassFish
     * cloud.
     * <p/>
     * GlassFish cloud platform does not support tools at this moment. Will
     * always return <code>false</code>.
     * <p/>
     * @param toolName Tool name, for example
     *        {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform#TOOL_APP_CLIENT_RUNTIME}.
     * @return Always returns <code>false</code>. This method is not supported.
     * @deprecated
     */
    @Deprecated
    @Override    
    public boolean isToolSupported(String toolName) {
        return false;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Invoked when the target of the listener has changed its state.
     * <p/>
     * Handles common store event handling. Remove event should be handled
     * correctly in child class because it's not possible to access correct
     * factory in common code..
     * <p/>
     * @param e ChangeEvent object.
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        GlassFishInstance.Event event = (GlassFishInstance.Event) e.getSource();
        switch (event) {
            case STORE:
                builder = null;
                instance.removeChangeListener(this, new GlassFishInstance.Event[]{
                            GlassFishInstance.Event.STORE, GlassFishInstance.Event.REMOVE});
                instance = GlassFishCloudInstanceProvider.getCloudInstance(url.getName());
                if (instance == null || instance.getLocalServer() == null) {
                    throw new NullPointerException("GlassFish local server instance "
                            + url.getName() + "does not exist.");
                }
                instance.addChangeListener(this, new GlassFishInstance.Event[]{
                            GlassFishInstance.Event.STORE, GlassFishInstance.Event.REMOVE});
                break;
            case REMOVE:                
                GlassFishPlatformFactory.removeJ2eePlatformImpl(url);
                builder = null;
                break;
        }
    }
    
// TODO: Change listeners to reflect server instance changes.
    /**
     * Initialize library builder on demand.
     */
    LibraryBuilder getBuilder() {
        if (builder != null) {
            return builder;
        }
        synchronized(this) {
            if (builder == null) {
                builder = new LibraryBuilder(
                        instance.getLocalServer().getServerHome(), "B", "C");
            }
        }
        return builder;
    }
    
}

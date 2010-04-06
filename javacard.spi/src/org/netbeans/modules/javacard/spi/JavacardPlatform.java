/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.javacard.spi;

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.openide.modules.SpecificationVersion;

/**
 * Service Provider Interface for Java Card Platforms.  Java Card Platforms
 * are registered as Java Platforms in the system filesystem, just as
 * instances on disk of the JDK which a project may be run against are.
 * <p/>
 * The Java Card Platform instance is responsible for providing the Java Card
 * API library classpath, which is used by Java Card projects for code
 * completion, compilation and debugging.
 * <p/>
 * A Java Card Platform provides one or more Cards which a project may be
 * deployed to.
 * <p/>
 * While a Java Card platform can be registered any way you choose, the
 * Java Card project modules (and particularly the Ant scripts) expect a
 * Java Card platform to be represented by a Properties-format file on disk, and
 * that file is read into the Ant environment by the project build script.
 * Note the distinction <i>properties-format</i> versus <i>properties-file</i>.
 * It is not required, and in fact, is inadvisable that the file <i>extension</i>
 * be <code>.properties</code>.  Instead, you will register your own file
 * extension (meaning that you provide the DataObject/Node/UI for files with
 * this extension), and make sure that an instance of JavacardPlatform can
 * be found in the Lookup of the DataObject.  For very simple cases, you may
 * be able to get away with just reusing the Java Card RI's built-in support
 * for properties files with the extension <code>.jcplatform</code>, and possibly
 * a custom factory for actual card instances (also properties-format files -
 * you will probably want your own file extension/data object type for these,
 * since the customizer for virtual cards for the RI allows things to be set
 * which cannot be changed in an actual piece of hardware).
 * <p/>
 * A module that wants to register a Java Card platform has several options.
 * <ol>
 * <li>Use the fairly flexible support for the Java Card RI to simply register
 * a properties-format <code>.jcplatform</code> file for the platform;  register
 * a factory for Card objects by platform kind, in the correct directory of the
 * system filesystem.</li>
 * <li>Register your own file type, which is in properties-format (the
 * Properties Based DataObjects module can handle most of this), and provides
 * a <code>JavacardPlatform</code> from its Lookup</li>
 * <li><i>For cards that really don't care that a project can never be deployed
 * via command-line+Ant:</i> provide a JavacardPlatform with Card instances which
 * provide an AntTargetInterceptor capability, and use the AntTargetInterceptor
 * to hijack load/instantiate/debug/etc. targets on the project, compile the
 * project, find the target JAR/CAP/etc. file and push it onto the device
 * however you want</li>
 * </ol>
 *
 * @author Tim Boudreau
 */
public abstract class JavacardPlatform extends JavaPlatform {
    /**
     * Name which can be passed to <code>JavaPlatform.findTool()</code> to
     * get the binary for the debug proxy (in the Java Card RI,
     * <code>$PLATFORM_HOME/bin/debugproxy.bat</code>).
     */
    public static final String TOOL_DEBUG_PROXY = "debugProxy";
    /**
     * Name which can passed to <code>JavaPlatform.findTool()</code> to
     * get the binary for the emulator executable (if any).  In the
     * Java Card RI, this is <code>$PLATFORM_HOME/bin/cjcre.exe</code>.
     */
    public static final String TOOL_EMULATOR = "emulator";
    /**
     * Get the unique ID of this platform.  If installed via a properties file,
     * this should be the name of the DataObject representing that platform,
     * to guarantee uniqueness.
     * @return A system (non-human readable) unique id
     */
    public abstract String getSystemName();
    /**
     * Determine if this platform is usable, or if it is misconfigured or
     * otherwise unusable.
     * @return True unless the file representing this platform is incomplete
     * or missing
     */
    public abstract boolean isValid();
    /**
     * Get the version of the Java Card platform (as distinct from the
     * java version) which this platform implements.
     * @return A specification version
     */
    public abstract SpecificationVersion getJavacardVersion();
    /**
     * Determine if this platform can run the passed target version of
     * the java card specification.
     * @param javacardVersion
     * @return
     */
    public abstract boolean isVersionSupported(SpecificationVersion javacardVersion);    /**
     * Get the bootstrap libraries appropriate to this kind of project.
     * Classic applets and libraries may have a different boot class path
     * than JavaCard v3 ones
     * @param kind The kind of the project
     * @return A classpath
     */
    public abstract ClassPath getBootstrapLibraries(ProjectKind kind);

    /**
     * Get this platform's annotation processor classpath for the given
     * project kind.  The returned value may vary depending on
     * kind.isClassic().
     * @param kind The kind of project a classpath is needed for
     * @return A ClassPath
     */
    public abstract ClassPath getProcessorClasspath(ProjectKind kind);

    /**
     * Get a localized (or user-entered) display name for this particular
     * platform instance
     * @return A localized or user-entered display name for the platform
     */
    public abstract String getDisplayName();

    /**
     * Get an array of Lookup.Providers which (may) contain Card instances in their
     * lookups.  In the case of the RI, this might be a collection of
     * DataObjects for cards in some folder in the system filesystem.  Other
     * implementations may query a physical interface to interrorgate the cards
     * present, or similar.
     * @return An array of Lookup.Providers
     */
    public abstract Cards getCards();

    /**
     * Get the set of properties, as defined in <a href="JavacardPlatformKeyNames.html">JavacardPlatformKeyNames</a>,
     * which are used by Ant and parts of the IDE.  Note that this method is
     * distinct from <code>JavaPlatform.getProperties()</code>, which is expected
     * to return the runtime contents of <code>System.getProperties()</code> for
     * this platform (which may not even exist in the case of Classic-style
     * platforms).
     * @return A Properties object.
     */
    public abstract Properties toProperties();
    /**
     * Get the <i>kind</i> of this platform.  This is an ad-hoc string
     * used to decouple Cards from JavacardPlatforms, making it possible for
     * another module to implement its own Cards which somehow run against
     * this platform.
     * <p/>
     * The return value of this method is used to find registration directories
     * in the system filesystem, so it should be a valid file name that does
     * not contain file or path separator characters.
     * <p/>
     * The return value for the Java Card Reference Implementation is
     * <code>RI</code>.  All other platforms (unless they really are compatible
     * with the Reference Implementation - i.e. the user should be able to
     * define ad-hoc cards with ad-hoc memory settings on the fly) should
     * return some other value.
     * 
     * @return
     */
    public abstract String getPlatformKind();

    /**
     * Called when a file representing a Java Card platform is deleted.  Perform
     * any cleanup of eprom files, device definitions, etc. here.
     */
    public void onDelete() throws IOException {
        //do nothing
    }

    public abstract Set<ProjectKind> supportedProjectKinds();

    public static JavacardPlatform createBrokenJavacardPlatform (String name) {
        return new BrokenJavacardPlatform(name);
    }

    public static JavacardPlatform createBrokenJavacardPlatform(String name, Collection<String> cardNames) {
        return new BrokenJavacardPlatform(name, cardNames);
    }
}

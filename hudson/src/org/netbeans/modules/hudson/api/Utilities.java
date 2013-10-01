/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.hudson.api;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.netbeans.modules.hudson.Installer;
import static org.netbeans.modules.hudson.api.HudsonJobBuild.Result.FAILURE;
import static org.netbeans.modules.hudson.api.HudsonJobBuild.Result.SUCCESS;
import static org.netbeans.modules.hudson.api.HudsonJobBuild.Result.UNSTABLE;
import org.netbeans.modules.hudson.impl.HudsonInstanceImpl;
import org.netbeans.modules.hudson.spi.UIExtension;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.w3c.dom.Element;

/**
 * Helper utility class
 */
public class Utilities {

    private Utilities() {}
    
    public static boolean isSupportedVersion(HudsonVersion version) {
        // Check for null
        if (null == version)
            return false;
        
        // Version check
        if (version.compareTo(HudsonVersion.SUPPORTED_VERSION) < 0)
            return false;
        
        return true;
    }

    /**
     * Encode a path segment or longer path of a URI (such as a job name) suitably for a URL.
     * @param path a path segment, or several such segments separated by slashes, with possible initial and/or trailing slashes
     * @return the same with spaces and unsafe characters escaped
     * @throws IllegalArgumentException if there is some other URI problem
     */
    public static String uriEncode(String path) {
        Parameters.notNull("segment", path);
        try {
            return new URI(null, path, null).toASCIIString();
        } catch (URISyntaxException x) {
            throw new IllegalArgumentException(x);
        }
    }

    /**
     * Mostly inverse of {@link #uriEncode}, but only decodes a single path segment.
     */
    public static String uriDecode(String string) {
        String d = URI.create(string).getPath();
        if (d.contains("/")) { // NOI18N
            throw new IllegalArgumentException(d);
        }
        return d;
    }

        /**
         * Evaluate an XPath expression.
         * @param expr an XPath expression
         * @param xml a DOM context
         * @return the string value, or null
         */
        public static synchronized String xpath(String expr, Element xml) {
            try {
                return xpath.evaluate(expr, xml);
            } catch (XPathExpressionException x) {
                Logger.getLogger(Utilities.class.getName()).log(Level.FINE, "cannot evaluate '" + expr + "'", x);
                return null;
            }
        }
        private static final XPath xpath = XPathFactory.newInstance().newXPath();

    /**
     * Quickly check whether Hudson support is in use.
     *
     * @return True if Hudson support is in use, false otherwise.
     */
    public static boolean isHudsonSupportActive() {
        return Installer.active();
    }

    /**
     * Make an instance persistent.
     *
     * @param instance Hudson instance to persist.
     */
    public static void persistInstance(HudsonInstance instance) {
        if (instance instanceof HudsonInstanceImpl) {
            ((HudsonInstanceImpl) instance).makePersistent();
        }
    }

    /**
     * Get appropriate {@link HudsonJob.Color} instance for a build.
     *
     * @param build Hudson Job Build.
     * @return Appropriate color, depending on the result of the build.
     */
    public static HudsonJob.Color getColorForBuild(HudsonJobBuild build) {
        switch (build.getResult()) {
            case SUCCESS:
                return HudsonJob.Color.blue;
            case UNSTABLE:
                return HudsonJob.Color.yellow;
            case FAILURE:
                return HudsonJob.Color.red;
            default:
                return HudsonJob.Color.grey;
        }
    }

    /**
     * Return icon for a Hudson job.
     *
     * @param job Job to get icon for.
     * @return Icon representing current state of the job.
     */
    public static Icon getIcon(HudsonJob job) {
        return makeIcon(job.getColor().iconBase());
    }

    /**
     * Return icon for a Hudson Job build.
     *
     * @param build Build to get icon for.
     * @return Icon representing current state of the build.
     */
    public static Icon getIcon(HudsonJobBuild build) {
        return makeIcon(Utilities.getColorForBuild(build).iconBase());
    }

    /**
     * Make an icon for the provided iconBase.
     */
    private static Icon makeIcon(String iconBase) {
        return ImageUtilities.image2Icon(
                ImageUtilities.loadImageIcon(iconBase, false).getImage());
    }

    /**
     * Show a build in the UI.
     *
     * A UI extendsion is needed to perform this method. If no extension is
     * registered, this method does nothing. See {@link UIExtension}.
     *
     * @param build Hudson Build to show.
     */
    public static void showInUI(HudsonJobBuild build) {
        UIExtension ext = findUIExtension();
        if (ext != null) {
            ext.showInUI(build);
        }
    }

    /**
     * Show a job in the UI.
     *
     * A UI extendsion is needed to perform this method. If no extension is
     * registered, this method does nothing. See {@link UIExtension}.
     *
     * @param job Hudson Job to show.
     */
    public static void showInUI(HudsonJob job) {
        UIExtension ext = findUIExtension();
        if (ext != null) {
            ext.showInUI(job);
        }
    }

    /**
     * Find UI extension.
     *
     * @return The UI extension if registered, null otherwise.
     */
    private static UIExtension findUIExtension() {
        return Lookup.getDefault().lookup(UIExtension.class);
    }
}

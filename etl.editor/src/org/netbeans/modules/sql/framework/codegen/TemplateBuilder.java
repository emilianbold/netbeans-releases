/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.sql.framework.codegen;

import java.io.StringWriter;
import java.util.Properties;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import net.java.hulp.i18n.Logger;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class TemplateBuilder {

    /** LOG_CATEGORY is the name of this class. */
    private static final String LOG_CATEGORY = TemplateBuilder.class.getName();
    /** Default encoding for templates. */
    private static final String TEMPLATE_ENCODING = "ISO-8859-1";
    /** Prefix for global Velocity engine * */
    private static final String SQLFRAMEWORK_DB_PREFIX = "org/netbeans/modules/sql/framework/codegen/";
    private static AtomicReference<VelocityEngine> VELOCITY_ENGINE = null;
    private static transient final Logger mLogger = Logger.getLogger(TemplateBuilder.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    /**
     * Get VelocityEngine after initializing to use "autoload.Velocity" module's
     * resource loader.
     */
    private static VelocityEngine getVelocityEngine() {
        synchronized (TemplateBuilder.class) {
            if (VELOCITY_ENGINE == null) {
                // Velocity engine and resource loader class from "autoload.velocity" NB Module.
                VELOCITY_ENGINE = new AtomicReference(new VelocityEngine());
                Properties velProp = new Properties();
                velProp.put("resource.loader", "class");
                velProp.put("class.resource.loader.description", "Velocity Classpath Resource Loader");
                velProp.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
                try {
                    VELOCITY_ENGINE.get().init(velProp);
                } catch (Exception e) {
                    throw new RuntimeException("Could not initialize velocity engine: " + e);
                }
            }
        }
        return VELOCITY_ENGINE.get();
    }

    public static String generateSql(String templateFile, VelocityContext context) {
        try {
            // Instantiate and initialize Velocity engine.
            VelocityEngine ve = getVelocityEngine();
            // Lets render a template
            StringWriter sw = new StringWriter();
            ve.mergeTemplate(SQLFRAMEWORK_DB_PREFIX + templateFile, TEMPLATE_ENCODING, context, sw);
            return sw.toString();
        } catch (Exception ex) {
            mLogger.errorNoloc(mLoc.t("EDIT090: Problem in initializing/merging Velocity template:{0}", LOG_CATEGORY), ex);
            return null;
        }
    }
}

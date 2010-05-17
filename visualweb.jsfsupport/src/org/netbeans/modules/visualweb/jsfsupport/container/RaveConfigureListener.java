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
package org.netbeans.modules.visualweb.jsfsupport.container;

import com.sun.faces.config.ConfigureListener;
import com.sun.faces.util.Util;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.FactoryFinder;
import javax.faces.application.ApplicationFactory;
import javax.servlet.ServletContextEvent;


/**
 * <p>Preserve prior calling sequences, but the design time patches to
 * <code>com.sun.faces.util.Util</code> should have dealt with all the
 * cases where we had to specialize behavior in this class.  THe only
 * exception is for what was previously marked as ADDED FUNCTIONALITY.</p>
 *
 * @author Winston Prakash - Modifications to support JSF 1.2
 */


public class RaveConfigureListener extends ConfigureListener {


    /**
     * <p>During the standard <code>contextInitialized()</code> processing,
     * suppress warning messages from Digester (due to merging of JSF
     * declarations) that might alarm application developers, even though
     * they are harmless.</p>
     *
     * @param event <code>ServletContextEvent being processed
     */
    public void contextInitialized(ServletContextEvent event) {

        Logger logger = Logger.getLogger("org.apache.commons.digester.Digester");
        Level level = logger.getLevel();
        logger.setLevel(Level.SEVERE);

        Logger renderkitLogger = Util.getLogger(Util.FACES_LOGGER + Util.RENDERKIT_LOGGER);
        renderkitLogger.setLevel(Level.SEVERE);
        Logger taglibLogger = Util.getLogger(Util.FACES_LOGGER + Util.TAGLIB_LOGGER);
        taglibLogger.setLevel(Level.SEVERE);
        Logger applicationLogger = Util.getLogger(Util.FACES_LOGGER + Util.APPLICATION_LOGGER);
        applicationLogger.setLevel(Level.SEVERE);
        Logger contextLogger = Util.getLogger(Util.FACES_LOGGER + Util.CONTEXT_LOGGER);
        contextLogger.setLevel(Level.SEVERE);
        Logger configLogger = Util.getLogger(Util.FACES_LOGGER + Util.CONFIG_LOGGER);
        configLogger.setLevel(Level.SEVERE);
        Logger lifecycleLogger = Util.getLogger(Util.FACES_LOGGER + Util.LIFECYCLE_LOGGER);
        lifecycleLogger.setLevel(Level.SEVERE);

        try {
            super.contextInitialized(event);
        } finally {
            logger.setLevel(level); // Restore previous level
        }

    }


}

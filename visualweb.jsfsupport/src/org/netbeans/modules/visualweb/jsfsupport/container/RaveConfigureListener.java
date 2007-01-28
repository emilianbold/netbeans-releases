/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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

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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.uml.core.support;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thuy
 */
public class UMLLogger {
    public static final Logger umlLogger = Logger.getLogger("org.netbeans.modules.uml"); 
    
    /**
     * Log a given exception using the default logger named org.netbeans.modules.uml
     * @param ex An exception to be logged.   
     * @param messgLevel One of the message level identifiers (e.g Level.WARNING)
     */
    public static void logException (Throwable ex, Level messgLevel)
    {
        String mesg = ex.getMessage();
        umlLogger.log(messgLevel, mesg != null ? mesg : "", ex);
    }
    
    /**
     * Log a given exception using a logger with a given name.
     * @param loggerName  A name for the logger. This should be a dot-separated 
     * name and should represent the package name or class name.  (e.g. org.netbeans.module.uml.ui.drawingarea)
     * @param ex An exception to be logged.   
     * @param messgLevel One of the message level identifiers (e.g Level.WARNING)
     */
    public static void logException (String loggerName, Throwable ex, Level messgLevel)
    {
        String mesg = ex.getMessage();
        Logger.getLogger(loggerName).log(messgLevel, mesg != null ? mesg : "", ex);
    }
    
    /**
     * Log a given exception using a given logger
     * @param logger  A predefined logger
     * @param ex An exception to be logged.   
     * @param messgLevel One of the message level identifiers (e.g Level.WARNING)
     */
    public static void logException (Logger logger, Throwable ex, Level messgLevel)
    {
        String mesg = ex.getMessage();
        logger.log(messgLevel, mesg != null ? mesg : "", ex);
    }
    
    
   /**
     * Log a given message using the default logger named org.netbeans.modules.uml
     * @param message A message to be logged.   
     * @param messgLevel One of the message level identifiers (e.g Level.WARNING)
     */
    public  static void logMessage (String message, Level messgLevel)
    {
        umlLogger.log(messgLevel, message != null ? message : "");
    }
    
    /**
     * Log a given message using a logger with a given name.
     * @param loggerName  A name for the logger. This should be a dot-separated 
     * name and should represent the package name or class name.  (e.g. org.netbeans.module.uml.ui.drawingarea)
     * @param message A message to be logged.   
     * @param messgLevel One of the message level identifier (e.g Level.WARNING)
     */
    public  static void logMessage (String loggerName, String message, Level messgLevel)
    {
        Logger.getLogger(loggerName).log(messgLevel, message != null ? message : "");
    }
    
    /**
     * Log a given message using a given logger
     * @param logger  A predefined logger
     * @param message A message to be logged.   
     * @param messgLevel One of the message level identifier (e.g Level.WARNING)
     */
    public  static void logMessage (Logger logger, String message, Level messgLevel)
    {
        logger.log(messgLevel, message != null ? message : "");
    }

}

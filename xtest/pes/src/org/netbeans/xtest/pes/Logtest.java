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

/*
 * Logtest.java
 *
 * Created on June 13, 2002, 11:45 AM
 */

package org.netbeans.xtest.pes;
import java.util.logging.*;
import java.util.regex.*;

/**
 *
 * @author  mb115822
 */
public class Logtest {

    /** Creates a new instance of Logtest */
    public Logtest() {
    }

    public static void main(String[] args) {

        System.out.println("logger test");
        PESLogger.logger.info("wegwegweg");
        PESLogger.logger.setLevel(Level.ALL);
        PESLogger.setConsoleLoggingLevel(Level.ALL);
        //System.setProperty("java.util.logging.ConsoleHandler.level",Level.ALL.toString());
        //PESLogger.log(Level.INFO,"berberber");
        Exception e = new Exception("gwerg wergwerG");
        PESLogger.logger.log(Level.INFO,"EXception",e);
        PESLogger.logger.fine("Fine");
        PESLogger.logger.finer("Finer");
        PESLogger.logger.finest("Finest");
        PESLogger.logger.warning("Warning");
         
        String s1 = "020306";
        String s2 = "020307";
        String s3 = "020301";
        String s4 = "020301_1";
        
        System.out.println("C1:"+s1.compareToIgnoreCase(s2));
        System.out.println("C2:"+s1.compareToIgnoreCase(s3));
        System.out.println("C3:"+s1.compareToIgnoreCase(s4));
        
        /*
        System.out.println("regex test");
        String expr="Orion ME.*";
        String str = "Orion ME EA JDK1.3";
        
        Pattern pattern = Pattern.compile(expr,Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        
        System.out.println("result = "+matcher.matches());
        */
    }
    
}

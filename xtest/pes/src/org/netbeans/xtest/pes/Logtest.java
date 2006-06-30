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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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

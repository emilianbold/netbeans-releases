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


package org.netbeans.modules.iep.model.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;



/**
 * Description of the Class
 *
 * @author Bing Lu
 *
 * @since November 6, 2002
 */
public class RuntimeUtil {
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(RuntimeUtil.class.getName());

    /**
     * Description of the Field
     */
    public static final String P_SYSTEM_JAVA_HOME = "JAVA_HOME";

    /**
     * Description of the Field
     */
    public static final String SYSTEM_JAVA_HOME =
        System.getProperty(P_SYSTEM_JAVA_HOME);

    /**
     * Description of the Field
     */
    public static final String P_SYSTEM_JAVA_CLASS_PATH = "java.class.path";

    /**
     * Description of the Field
     */
    public static final String SYSTEM_JAVA_CLASS_PATH =
        System.getProperty(P_SYSTEM_JAVA_CLASS_PATH);

    /**
     * Description of the Field
     */
    public static final String JAVA_EXE = "java";

    /**
     * Description of the Field
     */
    public static final String JAVAC_EXE = "javac";

    /**
     * Description of the Field
     */
    public static final String JAR_EXE = "jar";

    /**
     * Description of the Field
     */
    public static final String WSDL2JAVA_CLASS =
        "org.apache.axis.wsdl.WSDL2Java";

    /**
     * Description of the Field
     */
    public static final String P_SYSTEM_JASPER_HOME = "JASPER_HOME";

    /**
     * Description of the Field
     */
    public static final String SYSTEM_JASPER_HOME =
        System.getProperty(P_SYSTEM_JASPER_HOME);


    /**
     * Description of the Method
     *
     * @param command Description of the Parameter
     *
     * @return Description of the Return Value
     *
     * @exception IOException Description of the Exception
     */
    public static String exec(String command)
        throws IOException {

        mLog.info("RuntimeUtil.exec " + command);

        Process process = Runtime.getRuntime().exec(command);
        InputStream input1 = process.getInputStream();
        InputStream input2 = process.getErrorStream();
        StringWriter writer1 = new StringWriter();
        StringWriter writer2 = new StringWriter();
        PrintWriter pwriter1 = new PrintWriter(writer1);
        PrintWriter pwriter2 = new PrintWriter(writer2);
        StreamReader reader1 = new StreamReader(input1, pwriter1);
        Thread thread1 = new Thread(reader1);
        StreamReader reader2 = new StreamReader(input2, pwriter2);
        Thread thread2 = new Thread(reader2);

        thread1.start();
        thread2.start();

        int retCode = 0;

        try {
            retCode = process.waitFor();
        } catch (InterruptedException e) {
            retCode = 1;
        }

        try {
            thread1.join();
        } catch (InterruptedException e) {
            retCode = 1;
        }

        try {
            thread2.join();
        } catch (InterruptedException e) {
            retCode = 1;
        }

        if (reader1.getException() != null) {
            throw reader1.getException();
        }

        if (reader2.getException() != null) {
            throw reader2.getException();
        } else {
            StringBuffer buf = new StringBuffer();

            buf.append(writer1.toString());
            buf.append('\n');
            buf.append(writer2.toString());

            if (retCode != 0) {
                throw new IOException(buf.toString());
            } else {
                return buf.toString();
            }

            // retCode == 0 ? "" : buf.toString();
        }
    }

    /**
     * Description of the Method
     *
     * @param jarName Description of the Parameter
     * @param changeDir Description of the Parameter
     * @param files Description of the Parameter
     *
     * @return Description of the Return Value
     *
     * @exception IOException Description of the Exception
     */
    public static String jarcvf0(String jarName, String changeDir, String files)
        throws IOException {

        StringBuffer buf = new StringBuffer();

        buf.append(JAR_EXE);
        buf.append(" -cvf0 ");
        buf.append(jarName);

        if (changeDir != null) {
            buf.append(" -C " + changeDir);
        }

        buf.append(" ");
        buf.append(files);

        return RuntimeUtil.exec(buf.toString());
    }

    /**
     * Description of the Method
     *
     * @param jarName Description of the Parameter
     * @param destDir Description of the Parameter
     *
     * @return Description of the Return Value
     *
     * @exception IOException Description of the Exception
     */
    public static String jarxvf(String jarName, String destDir)
        throws IOException {

        StringBuffer buf = new StringBuffer();

        buf.append(JAR_EXE);
        buf.append(" -xvf ");
        buf.append(jarName);
        buf.append(" ");
        buf.append(destDir);

        return RuntimeUtil.exec(buf.toString());
    }

    /**
     * Description of the Method
     *
     * @param classpath Description of the Parameter
     * @param args Description of the Parameter
     *
     * @return Description of the Return Value
     *
     * @exception IOException Description of the Exception
     */
    public static String java(String classpath, String args)
        throws IOException {

        StringBuffer buf = new StringBuffer();

        buf.append(JAVA_EXE);

        if (classpath == null) {
            buf.append(" -classpath " + SYSTEM_JAVA_CLASS_PATH);
        } else {
            buf.append(" -classpath " + classpath);
        }

        buf.append(" ");
        buf.append(args);

        return RuntimeUtil.exec(buf.toString());
    }

    /**
     * Description of the Method
     *
     * @param classpath Description of the Parameter
     * @param args Description of the Parameter
     *
     * @return Description of the Return Value
     *
     * @exception IOException Description of the Exception
     */
    public static String javac(String classpath, String args)
        throws IOException {

        StringBuffer buf = new StringBuffer();

        buf.append(JAVAC_EXE);

        if (classpath == null) {
            buf.append(" -classpath " + SYSTEM_JAVA_CLASS_PATH);
        } else {
            buf.append(" -classpath " + classpath);
        }

        buf.append(" ");
        buf.append(args);

        return RuntimeUtil.exec(buf.toString());
    }

    /**
     * Description of the Method
     *
     * @param classpath Description of the Parameter
     * @param destDir Description of the Parameter
     * @param args Description of the Parameter
     *
     * @return Description of the Return Value
     *
     * @exception IOException Description of the Exception
     */
    public static String javac(String classpath, String destDir, String args)
        throws IOException {

        StringBuffer buf = new StringBuffer();

        buf.append(JAVAC_EXE);

        if (classpath == null) {
            buf.append(" -classpath " + SYSTEM_JAVA_CLASS_PATH);
        } else {
            buf.append(" -classpath " + classpath);
        }

        if (destDir != null) {
            buf.append(" -d " + destDir);
        }

        buf.append(" ");
        buf.append(args);

        return RuntimeUtil.exec(buf.toString());
    }


    /**
     * The main program for the RuntimeUtil class
     *
     * @param args The command line arguments
     */
    public static void main(String[] args) {

        try {
            mLog.info(RuntimeUtil.SYSTEM_JAVA_CLASS_PATH);
            System.exit(1);
            mLog.info(RuntimeUtil.wsdl2Java(null, true, "babelfish",
                                                     args[0], args[1], false));
            mLog.info(RuntimeUtil.javac(null,
                                                 args[1]
                                                 + "\\babelfish\\*.java"));
            mLog.info(RuntimeUtil.jarcvf0("babelfish.jar", args[1],
                                                   "\\."));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Description of the Method
     *
     * @param classpath Description of the Parameter
     * @param genJUnit Description of the Parameter
     * @param thePackage Description of the Parameter
     * @param destDir Description of the Parameter
     * @param wsdlURI Description of the Parameter
     * @param noWrapped Description of the Parameter
     *
     * @return Description of the Return Value
     *
     * @exception IOException Description of the Exception
     */
    public static String wsdl2Java(String classpath, boolean genJUnit, String thePackage,
                                   String destDir, String wsdlURI, boolean noWrapped)
        throws IOException {

        StringBuffer buf = new StringBuffer();

        buf.append(WSDL2JAVA_CLASS);

        if (false) {

            // genHelper
            buf.append(" -H");
        }

        if (genJUnit) {
            buf.append(" -t");
        }

        if (thePackage != null) {
            buf.append(" -p " + thePackage);
        }

        if (destDir != null) {
            buf.append(" -o " + destDir);
        }

        if (noWrapped) {
            buf.append(" -noWrapped"); 
        }

        buf.append(" ");
        buf.append(wsdlURI);

        return java(classpath, buf.toString());
    }
}


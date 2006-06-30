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
package org.netbeans.tests.xml;

import java.io.PrintStream;
import java.io.PrintWriter;
import org.netbeans.jellytools.JellyTestCase;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObject;
import org.openide.util.Utilities;
import org.openide.util.io.NullOutputStream;

/**
 * Provides the basic support for XML API tests.
 * @author mschovanek
 */
public abstract class XTest extends JellyTestCase {
    public final String CATALOG_BUNDLE   = "org.netbeans.modules.xml.catalog.resources.Bundle";
    public final String CORE_BUNDLE      = "org.netbeans.modules.xml.core.resources.Bundle";
    public final String CSS_BUNDLE       = "org.netbeans.modules.css.resources.Bundle";
    public final String TAX_BUNDLE       = "org.netbeans.tax.resources.Bundle";
    public final String TEXT_BUNDLE      = "org.netbeans.modules.xml.text.resources.Bundle";
    public final String TOOLS_BUNDLE     = "org.netbeans.modules.xml.tools.resources.Bundle";
    public final String TREE_BUNDLE      = "org.netbeans.modules.xml.tree.resources.Bundle";
    
    protected String packageName;
    protected String absolutePath;
    protected String fsName;
    
    /** debug test output */
    protected PrintWriter dbg = new PrintWriter(new NullOutputStream());
    
    /** debug switch */
    protected static boolean DEBUG = false;
    private static boolean LOG_INTO_CONSOLE = false;
    
    public XTest(String testName) {
        super(testName);
    }
    
    
    protected void deleteData(String path) {
        try {
        DataObject dao = TestUtil.THIS.findData(path);
        if (dao != null) dao.delete();
        } catch (Exception ex) {}
    }

    /** @depricated use getPackegeName() */
    protected String packageName() {
        return  getPackageName();
    }

    /** Returns test's package name. */
    protected String getPackageName() {
        if (packageName == null) {
            packageName = this.getClass().getPackage().getName();
        }
        return packageName;
    }

    /** Returns test's package name delimited by 'separator'. */
    protected String getPackageName(String separator) {
        String name = getPackageName();
        name = org.openide.util.Utilities.replaceString(name, ".", separator);
        return name;
    }

    /** Returns data package name  delimited by 'separator'. */
    protected String getDataPackageName(String separator) {
        String name = getPackageName(separator);
        name += separator + "data";
        return name;
    }

    /** Returns absolute test folder path. **/
    protected String getAbsolutePath() {
        if (absolutePath == null) {
            String url = this.getClass().getResource("").toExternalForm();
            absolutePath = TestUtil.toAbsolutePath(TestUtil.findFileObject(url));
        }
        return absolutePath;
    }

    /** Returns test's filesystem display name */
    protected String getFilesystemName() throws FileStateInvalidException {
        if (fsName == null) {
            fsName = TestUtil.findFileObject(packageName(), Utilities.getShortClassName(this.getClass()), "class").getFileSystem().getDisplayName();
        }
        return fsName;
    }
    
    /** Returns default log. @see super#getLog() , @see #logIntoConsole(boolean) */
    public PrintStream getLog() {
        if (LOG_INTO_CONSOLE) {
            return System.out;
        } else {
            return super.getLog();
        }
    }
    
    /** Simple and easy to use method for printing a message to a default log
     * @param message meesage to log
     */    
    public void log(String message) {
        getLog().println(message);
    }
    
    /** Simple and easy to use method for printing a message to a default log
     * @param message meesage to log
     */    
    public void log(String message, Exception ex) {
        getLog().println(message);
        ex.printStackTrace(getLog());
    }
    
    /** Redirects log into console. @see #getLog() */
    public static void logIntoConsole(boolean console) {
        LOG_INTO_CONSOLE = console;
    }
    
    /** Causes the test to sleep for the specified nmuber of milliseconds.
     *  Does not throw any exception.
     */
    protected static void sleepTest(long millis) {
        try {
            Thread.currentThread().sleep(millis);
        } catch (Exception e) { /* do nothig */}
    }
}
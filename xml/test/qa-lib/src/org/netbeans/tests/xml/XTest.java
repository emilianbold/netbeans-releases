/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.tests.xml;

import java.io.PrintWriter;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.io.NullOutputStream;

/**
 * Provides the basic support for XML API tests.
 * @author mschovanek
 */
public abstract class XTest extends NbTestCase {
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
    protected PrintWriter dbg;
    protected PrintWriter out = new PrintWriter(System.out, true);
    
    /** debug switch */
    protected static boolean DEBUG = false;
    
    public XTest(String testName) {
        super(testName);
        if (DEBUG) {
            dbg = new PrintWriter(System.out, true);
        } else {
            dbg = new PrintWriter(new NullOutputStream());
        }
    }
    
    protected void println(String string) { //???
        if (out == null) {
            System.out.println(string);
        } else {
            out.println(string);
        }
    }
    
    protected String packageName() {
        if (packageName == null) {
            packageName = this.getClass().getPackage().getName();
        }
        return packageName;
    }
    
    
    protected String getAbsolutePath() {
        if (absolutePath == null) {
            String url = this.getClass().getResource("").toExternalForm();
            absolutePath = TestUtil.toAbsolutePath(TestUtil.findFileObject(url));
        }
        return absolutePath;
    }
    
    protected String getFilesystemName() throws FileStateInvalidException {
        if (fsName == null) {
            fsName = TestUtil.findFileObject(packageName(), null, null).getFileSystem().getDisplayName();
        }
        return fsName;
    }
}
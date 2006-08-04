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
 * JadTaskTest.java
 * JUnit based test
 *
 * Created on 09 November 2005, 16:13
 */
package org.netbeans.mobility.antext;

import junit.framework.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.text.MessageFormat;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author lukas
 */
public class JadTaskTest extends NbTestCase
{
    
    public JadTaskTest(String testName)
    {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(JadTaskTest.class);
        
        return suite;
    }

    /**
     * Test of execute method, of class org.netbeans.mobility.antext.JadTask.
     */
    public void testExecute() throws IOException
    {
        System.out.println("execute");
        
        //Prepare test
        File dir=getWorkDir();
        clearWorkDir();        
        File jadfile   =getGoldenFile("MobileApplication.jad");
        File jarfile   =getGoldenFile("MobileApplication.jar");
        File myfile    =getGoldenFile("MyApplication.jad");
        File output    =new File (dir+File.separator+"MobileApplication.jad");
        
        Project p=new Project();
        JadTask instance = new JadTask();
        instance.setProject(p);
        instance.setJadFile(jadfile);
        instance.setJarFile(jarfile);
        instance.setKeyStoreType("default");
        instance.setEncoding("UTF-8");
        instance.setUrl("MyApplication.jar");
        instance.setSign(true);
        instance.setKeyStore(getGoldenFile("keystore.ks"));
        instance.setKeyStorePassword("xxxxxx");
        instance.setAlias("test");
        instance.setAliasPassword("xxxxxx");
        instance.setOutput(output);
        instance.execute();
        this.assertFile(output,myfile);
        clearWorkDir();
    }
}

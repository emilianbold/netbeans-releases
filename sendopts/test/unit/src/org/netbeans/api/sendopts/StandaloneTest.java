/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.api.sendopts;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.sendopts.Arg;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class StandaloneTest extends NbTestCase {
    private URLClassLoader loader;
    private Class<?> classCommandLine;
    private Method methodProcess;
    private Class<?> classOptions;
    
    public StandaloneTest(String n) {
        super(n);
    }

    @Override
    protected void setUp() throws Exception {
        URL api = CommandLine.class.getProtectionDomain().getCodeSource().getLocation();
        URL options = Options.class.getProtectionDomain().getCodeSource().getLocation();
        
        loader = new URLClassLoader(
            new URL[] { api, options },
            CommandLine.class.getClassLoader().getParent()
        );
        Thread.currentThread().setContextClassLoader(loader);
        
        classCommandLine = loader.loadClass(CommandLine.class.getName());
        classOptions = loader.loadClass(Options.class.getName());
        methodProcess = classCommandLine.getMethod("process", String[].class);
    }
    
    
    
    public void testStandaloneUsage() throws Exception {
        Method factory = classCommandLine.getMethod("create", Class[].class);
        final Object arr = new Class[] { classOptions };
        Object cli = factory.invoke(null, arr);
        final Object param = new String[] { "--value=myvalue" };
        methodProcess.invoke(cli, param);
        
        assertEquals("myvalue", System.getProperty("key"));
    }
    
    public static final class Options implements Runnable {
        @Arg(longName="value")
        public String value;
        
        @Override
        public void run() {
            System.setProperty("key", value);
        }
        
    }
}

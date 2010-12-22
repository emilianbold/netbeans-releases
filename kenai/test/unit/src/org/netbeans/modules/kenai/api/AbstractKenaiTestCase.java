/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Before;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Stupka
 */
public abstract class AbstractKenaiTestCase extends NbTestCase {
    
    private Kenai kenai;
    
    private static String TEST_PROJECT = "nb-jnet-test";
    private static String uname = null;
    private static String passw = null;
        
    public AbstractKenaiTestCase(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();                
        
        BufferedReader br = null;
        final Logger logger = Logger.getLogger("TIMER.kenai");
        logger.setLevel(Level.FINE);
        if (uname == null) {
            uname = System.getProperty("kenai.user.login");
            passw = System.getProperty("kenai.user.password");
        }
        if (uname == null) {
            br = new BufferedReader(new FileReader(new File(System.getProperty("user.home"), ".test-kenai")));
            uname = br.readLine();
            passw = br.readLine();
            
            String proxy = br.readLine();
            String port = br.readLine();

            if(proxy != null) {
                System.setProperty("https.proxyHost", proxy);
                System.setProperty("https.proxyPort", port);
            }
            br.close();
        }              
    }
    
    protected String getTestProject() {
        return TEST_PROJECT;
    }
    
    protected String getUsername() {
        return uname;
    }
    
    protected String getPassword() {
        return passw;
    }
    
    protected Kenai getKenai() {
        if(kenai == null) {
            try {
                kenai = KenaiManager.getDefault().createKenai("testjava.net", "https://testjava.net");
                getKenai().login(uname, passw.toCharArray(), false);                
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);                    
                fail();
            } 
        }
        return kenai;
    }
    
}

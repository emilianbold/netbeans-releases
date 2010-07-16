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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.kenai.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

/**
 *
 * @author tester
 */
public class ServicesChecker {

    public class ServiceDescr {

        public String name = null,  type = null,  displayName = null,  description = null;
    }
    public LinkedList<ServiceDescr> serviceDescriptions = new LinkedList<ServiceDescr>();

    /**
     * Constructs the list of services and details of the services from the golden file
     * 
     * @param fileName - path to a golden file
     * @throws java.io.IOException
     */
    public ServicesChecker(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String S = br.readLine();
        ServiceDescr service = null;
        while (S != null) {
            if (S.trim().equals("")) { // skip empty lines
                S = br.readLine();
                continue;
            }
            if (S.trim().equals("{")) { // new service will be added in the list
                service = new ServiceDescr();
                S = br.readLine();
                continue;
            }
            if (S.trim().equals("}")) { // service was successfully read
                serviceDescriptions.add(service);
                S = br.readLine();
                continue;
            }
            // parse key:value pairs
            String[] keyValPair = S.split(":");
            if (keyValPair.length != 2) {
                br.close();
                throw new IOException("File doesn't have a propper structure, pair key:value wasn't read...");
            }
            try {
                if (keyValPair[0].equals("name")) {
                    service.name = keyValPair[1];
                } else if (keyValPair[0].equals("type")) {
                    service.type = keyValPair[1];
                } else if (keyValPair[0].equals("display_name")) {
                    service.displayName = keyValPair[1];
                } else if (keyValPair[0].equals("description")) {
                    service.description = keyValPair[1];
                }
            } catch (NullPointerException npe) {
                br.close();
                throw new IOException("Missing opening char '{'... ?");
            }
            S = br.readLine();
        }
        br.close();
    }

}

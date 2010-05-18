/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
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

package org.netbeans.modules.j2me.cdc.project.ricoh.dalp;

import java.io.File;
import java.util.Properties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.xml.sax.*;

public class DalpParserHandlerImpl implements DalpParserHandler {

    public static final boolean DEBUG = false;
    private Properties p;
    private FileObject projectRoot;
    
    public DalpParserHandlerImpl(FileObject projectRoot, Properties p){
        this.p = p;
        this.projectRoot = projectRoot;
    }
    
    public void handle_application_ver(final java.lang.String data, final Attributes meta) throws SAXException {
        if (DEBUG) System.err.println("handle_application_ver: " + data);
    }

    public void handle_jar(final Attributes meta) throws SAXException {
        if (DEBUG) System.err.println("handle_jar: " + meta);
    }

    public void handle_dsdk(final Attributes meta) throws SAXException {
        if (DEBUG) System.err.println("handle_dsdk: " + meta);
    }

    public void handle_vendor(final java.lang.String data, final Attributes meta) throws SAXException {
        if (DEBUG) System.err.println("handle_vendor: " + data);
        p.put("application.vendor", data);
    }

    public void handle_install(final Attributes meta) throws SAXException {
        if (DEBUG) System.err.println("handle_install: " + meta);
    }

    public void handle_encode_file(final java.lang.String data, final Attributes meta) throws SAXException {
        if (DEBUG) System.err.println("handle_encode_file: " + data);
    }

    public void start_dalp(final Attributes meta) throws SAXException {
        if (DEBUG) System.err.println("start_dalp: " + meta);
    }

    public void end_dalp() throws SAXException {
        if (DEBUG) System.err.println("end_dalp()");
    }

    public void handle_title(final java.lang.String data, final Attributes meta) throws SAXException {
        if (DEBUG) System.err.println("handle_title: " + data);
        p.put("application.name", data);
    }

    public void start_resources(final Attributes meta) throws SAXException {
        if (DEBUG) System.err.println("start_resources: " + meta);
    }

    public void end_resources() throws SAXException {
        if (DEBUG) System.err.println("end_resources()");
    }

    public void handle_telephone(final java.lang.String data, final Attributes meta) throws SAXException {
        if (DEBUG) System.err.println("handle_telephone: " + data);
        p.put("ricoh.application.telephone", data);
    }

    public void handle_offline_allowed(final Attributes meta) throws SAXException {
        if (DEBUG) System.err.println("handle_offline_allowed: " + meta);
    }

    public void handle_application_desc(final Attributes meta) throws SAXException {
        if (DEBUG) System.err.println("handle_application_desc: " + meta);
        if (meta.getValue("main-class") != null){
            p.put("main.class.class", "xlet");
            p.put("main.class", meta.getValue("main-class"));
        }
    }

    public void handle_fax(final java.lang.String data, final Attributes meta) throws SAXException {
        if (DEBUG) System.err.println("handle_fax: " + data);
        p.put("ricoh.application.fax", data);        
    }

    public void handle_all_permissions(final Attributes meta) throws SAXException {
        if (DEBUG) System.err.println("handle_all_permissions: " + meta);
    }

    public void handle_e_mail(final java.lang.String data, final Attributes meta) throws SAXException {
        if (DEBUG) System.err.println("handle_e_mail: " + data);
        p.put("ricoh.application.email", data);                
    }

    public void handle_product_id(final java.lang.String data, final Attributes meta) throws SAXException {
        if (DEBUG) System.err.println("handle_product_id: " + data);
    }

    public void start_information(final Attributes meta) throws SAXException {
        if (DEBUG) System.err.println("start_information: " + meta);
    }

    public void end_information() throws SAXException {
        if (DEBUG) System.err.println("end_information()");
    }

    public void handle_icon(final java.lang.String data, final Attributes meta) throws SAXException {
        if (DEBUG) System.err.println("handle_icon: " + data);
        if (data != null && data.length() != 0){
            File f = new File(FileUtil.toFile(projectRoot), "src/" + data); //NOI18N
            if (f.exists())
                p.put("ricoh.application.icon", f);                
        }
    }

    public void handle_description(final java.lang.String data, final Attributes meta) throws SAXException {
        if (DEBUG) System.err.println("handle_description: " + data);
        if ("detail".equalsIgnoreCase(meta.getValue("type"))){
            p.put("application.description.detail", data);
        } else {
            p.put("application.description", data);
        }
    }

    public void start_security(final Attributes meta) throws SAXException {
        if (DEBUG) System.err.println("start_security: " + meta);
    }

    public void end_security() throws SAXException {
        if (DEBUG) System.err.println("end_security()");
    }

    public void handle_argument(final String data, Attributes attrs) throws SAXException {
        String args = p.getProperty("application.args");
        p.put("application.args", args != null ? (args + " " + data) : data);
    }
}

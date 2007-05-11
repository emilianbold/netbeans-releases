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
            p.put("main.class.xlet", "true");
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

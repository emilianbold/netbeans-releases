/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.mac;

import org.netbeans.modules.reglib.NbServiceTagSupport;
import org.netbeans.modules.servicetag.ServiceTag;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Dmitry Lipin
 */
public class ServiceTagGenerator {

    public static final String ST_SOURCE =
            System.getProperty("servicetag.source");

    public static final String JAVA_VERSION =
            System.getProperty("java.version");

    public static String JAVA_HOME =
            System.getProperty("java.home");


    public static void main(String[] args) {
        System.out.println("ST_SOURCE = " + ST_SOURCE);
        System.out.println("JAVA_VERSION = " + JAVA_VERSION);
        System.out.println("JAVA_HOME = " + JAVA_HOME);


        int result = 0;
        if(args.length>0) {
            final String product = args[0];
            System.out.println("Trying to create ST for " + product);
            if(product.equals("javafx")) {
                try {
                     NbServiceTagSupport.createJavaFXServiceTag(ST_SOURCE, JAVA_VERSION);
                     NbServiceTagSupport.createJavaFXSdkServiceTag(ST_SOURCE, JAVA_VERSION);
                } catch (Exception e) {
                    e.printStackTrace();
                    result = 1;
                }
            } else if(product.equals("nb")) {
                try {
                    NbServiceTagSupport.createNbServiceTag(ST_SOURCE, JAVA_VERSION);
                } catch (Exception e) {
                    e.printStackTrace();
                    result = 1;
                }
            } else if(product.equals("nb-cnd")) {
                try {
                    NbServiceTagSupport.createCndServiceTag(ST_SOURCE, JAVA_VERSION);
                } catch (Exception e) {
                    e.printStackTrace();
                    result = 1;
                }
            } else if(product.startsWith("gf")) {
                try{
                    createSTGlassFish(product);
                } catch (Exception e) {
                    e.printStackTrace();
                    result = 1;
                }
            }
        }
        System.exit(result);
    }



    private static void createSTGlassFish(final String product) throws IOException {
        //Glassfish has product like gf-<version>, i.e. gf-v2, gf-v3, gf-v2.1
        final String version = product.substring(product.lastIndexOf('-')+1);
        final String gfHome = System.getProperty("location");
        System.out.println("version = " + version);
        System.out.println("gfHome = " + gfHome);
        // installation ST for glassfish is created in postflight/setup.xml
        // installation ST for sjsas exists in image
        // so we just have to register 
     
        ServiceTag gfST = null;
        if (System.getProperty("netbeans.home") != null) {
            gfST= NbServiceTagSupport.createGfServiceTag(
                    ST_SOURCE,
                    JAVA_HOME,
                    JAVA_VERSION,
                    gfHome,
                    version);
        }
        final String registry = "lib/registration/servicetag-registry.xml";
        final String relativeLocation = (version.contains("3") ? "glassfish/" : "") + registry;
        File gfReg = new File(gfHome, relativeLocation);

        if (gfReg.exists()) {
            Map<String, Object> map = new HashMap<String, Object>();
            if (gfST != null) {
                String urn = gfST.getInstanceURN();
                System.out.println("... GF instanceUrn : " + urn);
                if (urn != null && !urn.equals("")) {
                    map.put("<instance_urn/>", "<instance_urn>" + urn + "</instance_urn>");
                    map.put("<instance_urn></instance_urn>", "<instance_urn>" + urn + "</instance_urn>");
                }
                // specific to ST that is created by AppServer itself and stored in the installation image
                // platform_arch and product_defined_inst_id are not set in AS install image
                map.put("<platform_arch></platform_arch>",
                        "<platform_arch>" + gfST.getPlatformArch() + "</platform_arch>");
                map.put("<product_defined_inst_id></product_defined_inst_id>",
                        "<product_defined_inst_id>" + gfST.getProductDefinedInstanceID() + "</product_defined_inst_id>");

            } else {
                map.put("<platform_arch></platform_arch>",
                        "<platform_arch>" + System.getProperty("os.arch") + "</platform_arch>");
            }
            map.put("<source>Sun Java System Application Server Native Packages</source>",
                    "<source>" + ST_SOURCE + "</source>");
            map.put("<source>Sun GlassFish Enterprise Server Native Packages</source>",
                    "<source>" + ST_SOURCE + "</source>");
            map.put("<source>GlassFish V3</source>",
                    "<source>" + ST_SOURCE + "</source>");
            // AppServer installation image has this incorrect vendor
            map.put("Sun Micosystems Inc.",
                    "Sun Microsystems Inc.");
            modifyFile(gfReg, map);
        }
  

    }


    private static void modifyFile(final File file, Map<String, Object> map) throws IOException {
        // if the file is larger than 100 Kb - skip it
        if (file.length() > 1024*100) {
            return;
        }

        final String original = readFile(file);

        String modified = new String(original);
        for(String token : map.keySet()) {
            final Object object = map.get(token);

            final String replacement;
            if (object instanceof File) {
                replacement = ((File) object).getAbsolutePath();
            }  else {
                replacement = object.toString();
            }

            modified = modified.toString().replace(token, replacement);
        }

        if (!modified.equals(original)) {
            System.out.println("modifying file: " + file.getAbsolutePath());
            writeFile(file, modified);
        }
    }



   public static String readFile(
            final File file) throws IOException {
        final Reader reader = new BufferedReader(new FileReader(file));
        try {
            final char[] buffer = new char[BUFFER_SIZE];
            final StringBuilder stringBuilder = new StringBuilder();
            int readLength;
            while ((readLength = reader.read(buffer)) != -1) {
                stringBuilder.append(buffer, 0, readLength);
            }
            return stringBuilder.toString();
        } finally {
            try {
                reader.close();
            } catch(IOException ignord) {}
        }
    }

    private static File writeFile(
            final File file,
            final String data) throws IOException{
        if(!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream output = null;
        try{
            output = new FileOutputStream(file, false);
            output.write(data.getBytes());
            output.flush();
        } finally {
            if(output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        return file;
    }

    public static final int BUFFER_SIZE = 65536;

}

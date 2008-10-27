/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modeldiscovery.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 *
 * @author Alexander Simon
 */
public class PackageConfig {
    private HashMap<String, PackageConfiguration> configurations = new HashMap<String, PackageConfiguration>();

    public PackageConfig() {
        initPackages();
    }

    private void initPackages() {
        File file = new File("/usr/lib/pkgconfig/");
        if (file.exists() && file.isDirectory() && file.canRead()) {
            for (File fpc : file.listFiles()) {
                String name = fpc.getName();
                if (name.endsWith(".pc") && fpc.canRead() && fpc.isFile()) {
                    PackageConfiguration pc = new PackageConfiguration();
                    readConfig(fpc, pc);
                    configurations.put(name.substring(0, name.length()-3), pc);
                }
            }
        }
    }

    /*package-local*/ void trace(){
        List<String> sort = new ArrayList<String>(configurations.keySet());
        Collections.sort(sort);
        for(String pkg: sort){
            PackageConfiguration pc = getConfig(pkg);
            if (pc != null){
                System.out.println("Package:\t"+pkg);
                System.out.println("Macros:\t"+pc.macros);
                System.out.println("Paths:\t"+pc.paths);
            }
        }
    }

    /*package-local*/ void traceConfig(String pkg){
        PackageConfiguration pc = configurations.get(pkg);
        if (pc != null){
            System.out.println("Package:\t"+pkg);
            System.out.println("Requires:\t"+pc.requires);
            System.out.println("Macros:\t"+pc.macros);
            System.out.println("Paths:\t"+pc.paths);
        }
    }

    /*package-local*/ void traceRecursiveConfig(String pkg){
        PackageConfiguration pc = getConfig(pkg);
        if (pc != null){
            System.out.println("Package:\t"+pkg);
            System.out.println("Macros:\t"+pc.macros);
            System.out.println("Paths:\t"+pc.paths);
        }
    }

    /*package-local*/ PackageConfiguration getConfig(String pkg){
        PackageConfiguration master = new PackageConfiguration();
        getConfig(master, configurations.get(pkg));
        return master;
    }

    private void getConfig(PackageConfiguration master, PackageConfiguration pc){
        if (pc != null) {
            for(String m : pc.macros){
                if (!master.macros.contains(m)){
                    master.macros.add(m);
                }
            }
            for(String P : pc.paths){
                if (!master.paths.contains(P)){
                    master.paths.add(P);
                }
            }
            for(String require : pc.requires){
                getConfig(master, configurations.get(require));
            }
        }
    }


//prefix=/usr
//exec_prefix=${prefix}
//libdir=${exec_prefix}/lib
//includedir=${prefix}/include
//target=x11
//
//gtk_binary_version=2.4.0
//gtk_host=i386-pc-solaris2.10
//
//Name: GTK+
//Description: GIMP Tool Kit (${target} target)
//Version: 2.4.9
//Requires: gdk-${target}-2.0 atk
//0123456789
//Libs: -L${libdir} -lgtk-${target}-2.0
//Cflags: -I${includedir}/gtk-2.0

    private void readConfig(File file, PackageConfiguration pc) {
        try {
            Map<String, String> vars = new HashMap<String, String>();
            BufferedReader in = new BufferedReader(new FileReader(file));
            while (true) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                line = line.trim();
                if (line.startsWith("#")) { // NOI18N
                    continue;
                }
                if (line.indexOf("=")>0){
                    int i = line.indexOf("=");
                    String name = line.substring(0, i).trim();
                    String value = line.substring(i+1).trim();
                    vars.put(name, expandMacros(value, vars));
                } else if (line.startsWith("Requires:")){
                    String value = line.substring(9).trim();
                    value = expandMacros(value,vars);
                    StringTokenizer st = new StringTokenizer(value, " ");
                    while(st.hasMoreTokens()) {
                        pc.requires.add(st.nextToken());
                    }
                } else if (line.startsWith("Cflags:")){
                    String value = line.substring(5).trim();
                    value = expandMacros(value,vars);
                    StringTokenizer st = new StringTokenizer(value, " ");
                    while(st.hasMoreTokens()) {
                        String v = st.nextToken();
                        if (v.startsWith("-I")){
                            pc.paths.add(v.substring(3));
                        } else if (v.startsWith("-D")){
                            pc.macros.add(v.substring(3));
                        }
                    }
                }
            }
            in.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String expandMacros(String value, Map<String, String> vars){
        if (value.indexOf("${")>=0) {
            while(value.indexOf("${")>=0) {
                int i = value.indexOf("${");
                int j = value.indexOf("}");
                if (j < i) {
                    break;
                }
                String macro = value.substring(i+2, j);
                String v = vars.get(macro);
                if (v == null || v.indexOf("${")>=0) {
                    break;
                }
                value = value.substring(0,i)+v+value.substring(j+1);
            }
        }
        return value;
    }

    /*package-local*/ static class PackageConfiguration {
        List<String> requires = new ArrayList<String>();
        List<String> macros = new ArrayList<String>();
        List<String> paths = new ArrayList<String>();
        private PackageConfiguration(){
        }
    }
}

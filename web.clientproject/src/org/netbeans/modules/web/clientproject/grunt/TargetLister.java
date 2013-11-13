/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.clientproject.grunt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

public class TargetLister {
     
    private static final Map<String, Pair<Long, Collection<Target>>> cache = new HashMap<>();
    private static final RequestProcessor RP = new RequestProcessor(TargetLister.class);
    
    public static Collection<Target> getTargets(FileObject pr) throws IOException { 
        read(pr);
        Pair<Long, Collection<Target>> targetPair = cache.get(pr.getPath());
        if (targetPair != null && targetPair.first().equals(pr.lastModified().getTime())) {
            return targetPair.second();
        }
        
        Collection<Target> loading = new ArrayList<>();
        loading.add(new Target("default", pr));//NOI18N
        return loading; 
    }
    
    public static void read(final FileObject gruntFile) {
        RP.post(new Runnable() {

            @Override
            public void run() {
                try {
                    String data = null;

                    String work = gruntFile.getParent().getPath();
                    if (Utilities.isWindows()) {
                        data = ProcessUtilities.callProcess("cmd", work, true, 60 , work, "/C grunt -h --no-color");//NOI18N
                    } else if (Utilities.isMac()) {
                        data = ProcessUtilities.callProcess("/bin/bash", work, true, 60 , work, "-lc", "grunt -h --no-color");//NOI18N
                    } else {
                        data = ProcessUtilities.callProcess("grunt", work, true, 60 , work, "-h", "--no-color");//NOI18N
                    }

                    parse(data, gruntFile);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

        });
    }
    
    private static void parse(String output, FileObject gruntFile) throws IOException {
        BufferedReader r = new BufferedReader(new StringReader(output));

        while (r.ready()) {
            String line = r.readLine();
            if (line == null) {
                return;
            }
            if (line.trim().toLowerCase().startsWith("available tasks")) {//NOI18N
                break;
            }
        }
        
        Collection<Target> col = new ArrayList<>();
        while (r.ready()) {
            String line = r.readLine();
            if (line == null) {
                break;
            }
            if (line.trim().isEmpty()) {
                break;
            }
            if (line.trim().equals("(no tasks found)")) {//NOI18N
                return;
            }

            String l = line.trim();
            l = l.substring(0,l.indexOf(" "));
            col.add(new Target(l, gruntFile));
        }
        cache.clear();
        cache.put(gruntFile.getPath(), Pair.of(gruntFile.lastModified().getTime(), col));
    }


    public static class Target {
        private final String name;
        private final FileObject script;

        public Target(String name, FileObject script) {
            this.name = name;
            this.script = script;
                  
        }

        String getName() {
            return name;
        }
        
        FileObject getOriginatingScript() {
            return script;
        }

        boolean isOverridden() {
            return false;
        }

        boolean isInternal() {
            return false;
        }

        boolean isDefault() {
            return name.equals("default");//NOI18N
        }

        boolean isDescribed() {
            return true;
        }
    }
    
}

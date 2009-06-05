/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.execute.cmd;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.openide.util.Utilities;

/**
 *
 * @author mkleint
 */
public class DirectConstructor implements Constructor {
    private final ArtifactVersion version;
    private final File javaExec;
    private final File mavenHome;
    //#164234
    //if maven.bat file is in space containing path, we need to quote with simple quotes.
    String quote = "\"";
    // the command line parameters with space in them need to be quoted and escaped to arrive
    // correctly to the java runtime on windows
    String escaped = "\\" + quote;

    public DirectConstructor(ArtifactVersion version, File javaExec, File mavenHome) {
        this.version = version;
        this.javaExec = javaExec;
        this.mavenHome = mavenHome;
    }

    public List<String> construct() {
        List<String> toRet = new ArrayList<String>();
        toRet.add(getJavaCmd());
        toRet.addAll(getM2OPTS());
        toRet.addAll(getClassPath());
        toRet.add(getClassWorldsConf());
        toRet.add(getMavenHome());
        toRet.add(getClassWorldLauncher());
        return toRet;
    }

    protected String getJavaCmd() {
        //TODO we should better pass the java eec directly from the j2seplatform..
        File bin = new File(javaExec, "bin");
        File java = new File(bin, "java");
        return quoteSpaces(java.getAbsolutePath(), quote);
    }

    protected List<String> getM2OPTS() {
        //TODO - will need to split the content and quote it properly?
        return Collections.<String>emptyList();
    }

    protected List<String> getClassPath() {
        // in 2.0.5 it's core/boot.. do we care?
        File bootParent = new File(mavenHome, "boot");
        File[] boots = bootParent.listFiles();
        List<String> toRet = new ArrayList<String>();
        toRet.add("-classpath");
        if (boots != null && boots.length > 0) {
            StringBuffer path = new StringBuffer();
            for (File boot : boots) {
                path.append(boot.getAbsolutePath()).append(":");
            }
            path.setLength(path.length() - 1);
            toRet.add(path.toString());
            return toRet;
        }
        throw new IllegalArgumentException("Wrong maven.home=" + mavenHome);
    }

    protected String getClassWorldsConf() {
        //TODO quoting?
        return "-Dclassworlds.conf=" + mavenHome.getAbsolutePath() + File.separator + "bin" + File.separator + "m2.conf"; //NOI18N

    }

    protected String getMavenHome() {
        return "-Dmaven.home=" + mavenHome.getAbsolutePath();
    }

    protected String getClassWorldLauncher() {
        DefaultArtifactVersion dav = new DefaultArtifactVersion("3.0-alpha-2"); //NOI18N
        if (version.compareTo(dav) >= 0) { //in 3.0
            return "org.codehaus.plexus.classworlds.launcher.Launcher"; //NOI18N
        }
        return "org.codehaus.classworlds.Launcher"; //NOI18N
    }


    // we run the shell/bat script in the process, on windows we need to quote any spaces
    //once/if we get rid of shell/bat execution, we might need to remove this
    //#164234
    private static String quoteSpaces(String val, String quote) {
        if (Utilities.isWindows() && val.indexOf(' ') != -1) { //NOI18N
            return quote + val + quote; //NOI18N
        }
        return val;
    }

}

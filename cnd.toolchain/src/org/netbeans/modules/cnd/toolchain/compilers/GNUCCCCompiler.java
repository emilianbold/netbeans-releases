/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.toolchain.compilers;

import java.io.BufferedReader;
import java.io.IOException;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.ToolKind;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.CompilerDescriptor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * A common base class for GNU C and C++  compilers
 * @author vk155633
 */
/*package*/abstract class GNUCCCCompiler extends CCCCompiler {

    public GNUCCCCompiler(ExecutionEnvironment env, CompilerFlavor flavor, ToolKind kind, String name, String displayName, String path) {
        super(env, flavor, kind, name, displayName, path);
    }

    protected String getCompilerStderrCommand() {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null){
            return " " + compiler.getIncludeFlags(); // NOI18N
        }
        return ""; // NOI18N
    }

    protected String getCompilerStdoutCommand() {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null){
            return " " + compiler.getMacroFlags();  // NOI18N
        }
        return ""; // NOI18N
    }

    @Override
    protected Pair getFreshSystemIncludesAndDefines() {
        Pair res = new Pair();
        try {
            getSystemIncludesAndDefines(getCompilerStderrCommand(), false, res);
            getSystemIncludesAndDefines(getCompilerStdoutCommand(), true, res);
            // a workaround for gcc bug - see http://gcc.gnu.org/ml/gcc-bugs/2006-01/msg00767.html
            if (!containsMacro(res.systemPreprocessorSymbolsList, "__STDC__")) { // NOI18N
                res.systemPreprocessorSymbolsList.add("__STDC__=1"); // NOI18N
            }
        } catch (IOException ioe) {
            System.err.println("IOException " + ioe);
            String errormsg;
            if (getExecutionEnvironment().isLocal()) {
                errormsg = NbBundle.getMessage(getClass(), "CANTFINDCOMPILER", getPath()); // NOI18N
            }  else {
                errormsg = NbBundle.getMessage(getClass(), "CANT_FIND_REMOTE_COMPILER", getPath(), getExecutionEnvironment().getDisplayName()); // NOI18N
            }
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errormsg, NotifyDescriptor.ERROR_MESSAGE));
        }
        return res;
    }

    private boolean startsWithPath(String line) {
        line = line.trim();
        if( line.startsWith("/") ) {  // NOI18N
            return true;
        } else if ( line.length()>2 && Character.isLetter(line.charAt(0)) && line.charAt(1) == ':' ) {
            return true;
        }
        return false;
    }

    protected String cutIncludePrefix(String line) {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null && compiler.getRemoveIncludeOutputPrefix() != null) {
            String remove = compiler.getRemoveIncludeOutputPrefix();
            if (line.toLowerCase().startsWith(getIncludeFilePathPrefix().toLowerCase())) {
                line = line.substring(getIncludeFilePathPrefix().length());
            } else if (line.toLowerCase().startsWith(remove)) {
                line = line.substring(remove.length());
            }
        }
        return line;
    }

   @Override
   protected void parseCompilerOutput(BufferedReader reader, Pair pair) {

       try {
           String line;
           boolean startIncludes = false;
           while ((line = reader.readLine()) != null) {
               //System.out.println(line);
               line = line.trim();
               if (line.contains("#include <...>")) { // NOI18N
                   startIncludes = true;
                   continue;
               }
               if (startIncludes) {
                   if (line.startsWith("End of search") || ! startsWithPath(line)) { // NOI18N
                       startIncludes = false;
                       continue;
                   }
                   if (line.length()>2 && line.charAt(1)==':') {
                       pair.systemIncludeDirectoriesList.addUnique(normalizePath(line));
                   } else {
                       line = cutIncludePrefix(line);
                       if (line.endsWith(" (framework directory)")) { // NOI18N
                           line = line.substring(0, line.lastIndexOf('(')).trim();
                       }
                       pair.systemIncludeDirectoriesList.addUnique(applyPathPrefix(line));
                       if (getDescriptor().getRemoveIncludePathPrefix()!=null && line.startsWith("/usr/lib")) { // NOI18N
                           // TODO: if we are fixing cygwin's include location (C:\Cygwin\lib) it seems
                           // we shouldn't add original dir (fix later to avoid regression before release)
                           pair.systemIncludeDirectoriesList.addUnique(applyPathPrefix(line.substring(4)));
                       }
                   }
                   continue;
               }
               parseUserMacros(line, pair.systemPreprocessorSymbolsList);
               if (line.startsWith("#define ")) { // NOI18N
                   int i = line.indexOf(' ', 8);
                   if (i > 0) {
                       String token = line.substring(8, i) + "=" + line.substring(i+1); // NOI18N
                       pair.systemPreprocessorSymbolsList.addUnique(token);
                   }
               }
           }
           reader.close();
       } catch (IOException ioe) {
           ErrorManager.getDefault().notify(ErrorManager.WARNING, ioe); // FIXUP
       }
   }
}

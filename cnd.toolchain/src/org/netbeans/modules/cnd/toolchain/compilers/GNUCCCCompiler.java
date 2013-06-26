/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.toolchain.compilers;

import java.io.BufferedReader;
import java.io.IOException;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.ToolKind;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.CompilerDescriptor;
import static org.netbeans.modules.cnd.toolchain.compilers.CCCCompiler.addUnique;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
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
        } catch (IOException ioe) {
            System.err.println("IOException " + ioe);
            String errormsg;
            if (getExecutionEnvironment().isLocal()) {
                errormsg = NbBundle.getMessage(getClass(), "CANTFINDCOMPILER", getPath()); // NOI18N
            }  else {
                errormsg = NbBundle.getMessage(getClass(), "CANT_FIND_REMOTE_COMPILER", getPath(), getExecutionEnvironment().getDisplayName()); // NOI18N
            }
            if (CndUtils.isStandalone()) {
                System.err.println(errormsg);
            } else {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errormsg, NotifyDescriptor.ERROR_MESSAGE));
            }
        }
        completePredefinedMacros(res);
        checkModel(res, new MyCallable<Pair>() {

            @Override
            public Pair call(String p) {
                Pair tmp = new Pair();
                try {
                    getSystemIncludesAndDefines(getCompilerStdoutCommand()+" "+p, true, tmp); // NOI18N
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                return tmp;
            }
        });
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
            String lline = line.toLowerCase();
            if (lline.startsWith(getIncludeFilePathPrefix().toLowerCase())) {
                line = line.substring(getIncludeFilePathPrefix().length());
            } else {
                String wpath = WindowsSupport.getInstance().convertToShellPath(getIncludeFilePathPrefix().toLowerCase());
                if (wpath != null && lline.startsWith(wpath)) {
                    line = line.substring(wpath.length());
                } else if (lline.startsWith(remove)) {
                    line = line.substring(remove.length());
                }
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
                       addUnique(pair.systemIncludeDirectoriesList, normalizePath(line));
                   } else {
                       line = cutIncludePrefix(line);
                       if (line.endsWith(" (framework directory)")) { // NOI18N
                           line = line.substring(0, line.lastIndexOf('(')).trim();
                       }
                       addUnique(pair.systemIncludeDirectoriesList, applyPathPrefix(line));
                       
                       if (getDescriptor() != null && getDescriptor().getRemoveIncludePathPrefix()!=null && line.startsWith("/usr/lib")) { // NOI18N
                           // TODO: if we are fixing cygwin's include location (C:\Cygwin\lib) it seems
                           // we shouldn't add original dir (fix later to avoid regression before release)
                           addUnique(pair.systemIncludeDirectoriesList, applyPathPrefix(line.substring(4)));
                       }
                   }
                   continue;
               }
               if (line.startsWith("#define ")) { // NOI18N
                   String[] macro = CCCCompiler.getMacro(line.substring(8).trim());
                   if (CCCCompiler.isValidMacroName(macro[0])) {
                       String token;
                       if (macro[1] != null) {
                            token = macro[0] + "=" + macro[1]; // NOI18N
                       } else {
                           token = macro[0];
                       }
                       addUnique(pair.systemPreprocessorSymbolsList, token);
                   }
               } else {
                   parseUserMacros(line, pair.systemPreprocessorSymbolsList);
               }
           }
           reader.close();
       } catch (IOException ioe) {
           ErrorManager.getDefault().notify(ErrorManager.WARNING, ioe); // FIXUP
       }
   }
}

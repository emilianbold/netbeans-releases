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
package org.netbeans.modules.cnd.gizmo;

import org.netbeans.modules.cnd.gizmo.support.GizmoServiceInfo;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfEntry;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.TAG;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfLineInfoSection.LineNumber;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Alexey Vladykin
 */
@ServiceProvider(service = SourceFileInfoProvider.class, position = 5000)
public class DwarfSourceInfoProvider implements SourceFileInfoProvider {

    private WeakHashMap<String, Map<String, SourceFileInfo>> cache;

    public DwarfSourceInfoProvider() {
        cache = new WeakHashMap<String, Map<String, SourceFileInfo>>();
    }

    public SourceFileInfo fileName(String functionSignature, int lineNumber, long offset, Map<String, String> serviceInfo) {
        if (serviceInfo == null){
            return null;
        }
        String executable = serviceInfo.get(GizmoServiceInfo.GIZMO_PROJECT_EXECUTABLE);
        if (executable != null) {
            SourceFileInfo sourceInfo;
            String functionName = functionSignature;
            int parenIdx = functionSignature.indexOf('(');
            if (0 <= parenIdx) {
                functionName = functionSignature.substring(0, parenIdx);
            }
            if (offset > 0) {
                sourceInfo = findDwarf2Line(executable, functionName, offset, serviceInfo);
                if (sourceInfo != null) {
                    return sourceInfo;
                }
            }
            Map<String, SourceFileInfo> sourceInfoMap = getSourceInfo(executable, lineNumber, serviceInfo);
            sourceInfo = sourceInfoMap.get(functionSignature);
            if (sourceInfo != null) {
                return sourceInfo;
            }

            // try without parameters
            if (0 <= parenIdx) {
                sourceInfo = sourceInfoMap.get(functionName);
                if (sourceInfo != null) {
                    return sourceInfo;
                }
            }
        }
        return null;
    }

    private SourceFileInfo findDwarf2Line(String executable, String function, long shift, Map<String, String> serviceInfo){
        //Dwarf2NameFinder finder = new Dwarf2NameFinder(executable);
        //finder.lookup(offset);
        //String sourceFile = finder.getSourceFile();
        //int lineNumber = finder.getLineNumber();
        //if (sourceFile != null && 0 <= lineNumber) {
        //    return new SourceFileInfo(sourceFile, lineNumber, 0);
        //}
        try {
            Dwarf dwarf = new Dwarf(executable);
            try {
                for (CompilationUnit unit : dwarf.getCompilationUnits()){
                    for (DwarfEntry entry : unit.getDeclarations(false)){
                        if (entry.getKind()== TAG.DW_TAG_subprogram){
                            String name = entry.getName();
                            if (name.equals(function) || entry.getQualifiedName().equals(function)) {
                                if (entry.getLowAddress() == 0) {
                                    continue;
                                }
                                LineNumber number = unit.getLineNumber(entry.getLowAddress() + shift);
                                if (number != null) {
                                    return new SourceFileInfo(toAbsolutePath(serviceInfo, number.file), number.line, 0);
                                }
                            }
                        }
                    }
                }
            } finally {
                dwarf.dispose();
            }
        } catch (IOException ex) {
            DLightLogger.instance.log(Level.INFO, ex.getMessage());
        }
        return null;
    }

    private synchronized Map<String, SourceFileInfo> getSourceInfo(String executable, int lineNumber, Map<String, String> serviceInfo) {
        Map<String, SourceFileInfo> sourceInfoMap = cache.get(executable);
        if (sourceInfoMap == null) {
            sourceInfoMap = new HashMap<String, SourceFileInfo>();
            try {
                Dwarf dwarf = new Dwarf(executable);
                try {
                    for (CompilationUnit compilationUnit : dwarf.getCompilationUnits()) {
                        for (DwarfEntry entry : compilationUnit.getDeclarations(false)) {
                            if (entry.getKind().equals(TAG.DW_TAG_subprogram)) {
                                if (entry.getLowAddress() == 0) {
                                    continue;
                                }
                                if (entry.getDeclarationFilePath() != null) {
                                    SourceFileInfo sourceInfo = new SourceFileInfo(
                                            toAbsolutePath(serviceInfo, entry.getDeclarationFilePath()),
                                            lineNumber > 0 ? lineNumber : entry.getLine(), 0);
                                    sourceInfoMap.put(entry.getQualifiedName(), sourceInfo);
                                }
                            }
                        }
                    }
                } finally {
                    dwarf.dispose();
                }
            } catch (IOException ex) {
                DLightLogger.instance.log(Level.INFO, ex.getMessage());
            }
            cache.put(executable, sourceInfoMap.isEmpty()?
                Collections.<String, SourceFileInfo>emptyMap() : sourceInfoMap);
        }
        return sourceInfoMap;
    }

    private static String toAbsolutePath(Map<String, String> serviceInfo, String path) {
        String projectPath = serviceInfo.get(GizmoServiceInfo.GIZMO_PROJECT_FOLDER);
        if (projectPath != null) {
            path = IpeUtils.toAbsolutePath(projectPath, path);
        }
        return path;
    }
}

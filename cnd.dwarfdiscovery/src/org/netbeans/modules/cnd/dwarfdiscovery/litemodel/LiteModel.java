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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.dwarfdiscovery.litemodel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnitInterface;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;
import org.netbeans.modules.cnd.dwarfdump.Dwarf.CompilationUnitIterator;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.LANG;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;
import org.netbeans.modules.cnd.litemodel.api.Declaration;
import org.netbeans.modules.cnd.litemodel.api.Model;
import org.netbeans.modules.cnd.litemodel.api.ModelAccessor;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifactProvider;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;

/**
 *
 * @author Alexander Simon
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.litemodel.api.ModelAccessor.class)
public class LiteModel extends ModelAccessor {

    @Override
    public Model createModel(Project project, ModelKind kind) {
        MakeArtifactProvider artifactProvider = project.getLookup().lookup(MakeArtifactProvider.class);
        if (artifactProvider != null) {
            for(MakeArtifact artifact : artifactProvider.getBuildArtifacts()){
                String output = artifact.getOutput();
                if (output != null) {
                    if (!CndPathUtilitities.isPathAbsolute(output)) {
                        output = artifact.getWorkingDirectory() + "/" + output; // NOI18N
                    }
                    return createModel(output, kind);
                }
            }
        }
        return null;
    }

    private Model createModel(String binary, ModelKind kind) {
        DwarfRenderer renderer = LWM(binary, kind);
        final Map<String, Map<String, Declaration>> lwm = renderer.getLWM();
        return new Model() {
            @Override
            public Map<String, Declaration> getFile(String path) {
                return lwm.get(path);
            }
        };
    }

    private DwarfRenderer LWM(String objFileName, ModelKind kind){
        //long time = System.currentTimeMillis();
        DwarfRenderer dwarfRenderer;
        switch(kind) {
            case TOP_LEVEL_DECLARATIONS:
                dwarfRenderer = DwarfRenderer.createTopLevelDeclarationsRenderer();
                break;
            case TOP_LEVEL_DECLARATIONS_IN_COMPILATION_UNIT:
                dwarfRenderer = DwarfRenderer.createTopLevelDeclarationsCompilationUnitsRenderer();
                break;
            case FULL:
            default:
                dwarfRenderer = DwarfRenderer.createFullRenderer();
                break;
        }
        Dwarf dump = null;
        try {
            dump = new Dwarf(objFileName);
            CompilationUnitIterator iterator = dump.iteratorCompilationUnits();
            while (iterator.hasNext()) {
                CompilationUnitInterface cu = iterator.next();
                if (cu != null) {
                    if (cu.getRoot() == null || cu.getSourceFileName() == null) {
                        continue;
                    }
                    String lang = cu.getSourceLanguage();
                    if (lang == null) {
                        continue;
                    }
                    if (LANG.DW_LANG_C.toString().equals(lang)
                            || LANG.DW_LANG_C89.toString().equals(lang)
                            || LANG.DW_LANG_C99.toString().equals(lang)) {
                    } else if (LANG.DW_LANG_C_plus_plus.toString().equals(lang)) {
                    } else if (LANG.DW_LANG_Fortran77.toString().equals(lang) ||
                           LANG.DW_LANG_Fortran90.toString().equals(lang) ||
                           LANG.DW_LANG_Fortran95.toString().equals(lang)) {
                    } else {
                        continue;
                    }
                    dwarfRenderer.process(cu);
                }
            }
        } catch (FileNotFoundException ex) {
            // Skip Exception
        } catch (WrongFileFormatException ex) {
        } catch (IOException ex) {
        } catch (Exception ex) {
        } finally {
            if (dump != null) {
                dump.dispose();
            }
        }
        //System.out.println("Analyzing time "+(System.currentTimeMillis()-time));
        return dwarfRenderer;
    }
}

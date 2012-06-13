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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.profiler.nbimpl.providers;

import org.netbeans.modules.profiler.nbimpl.javac.JavacClassInfo;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import org.netbeans.modules.profiler.categorization.spi.CategoryDefinitionProcessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.lib.profiler.marker.ClassMarker;
import org.netbeans.lib.profiler.marker.CompositeMarker;
import org.netbeans.lib.profiler.marker.Marker;
import org.netbeans.lib.profiler.marker.MethodMarker;
import org.netbeans.lib.profiler.marker.PackageMarker;
import org.netbeans.lib.profiler.marker.Mark;
import org.netbeans.lib.profiler.results.cpu.marking.MarkMapping;
import org.netbeans.modules.profiler.api.java.ProfilerTypeUtils;
import org.netbeans.modules.profiler.api.java.SourceClassInfo;
import org.netbeans.modules.profiler.api.java.SourceMethodInfo;
import org.netbeans.modules.profiler.categorization.api.definitions.CustomCategoryDefinition;
import org.netbeans.modules.profiler.categorization.api.definitions.PackageCategoryDefinition;
import org.netbeans.modules.profiler.categorization.api.definitions.SingleTypeCategoryDefinition;
import org.netbeans.modules.profiler.categorization.api.definitions.SubtypeCategoryDefinition;
import org.netbeans.modules.profiler.nbimpl.javac.ElementUtilitiesEx;
import org.netbeans.spi.project.LookupProvider.Registration.ProjectType;
import org.netbeans.spi.project.ProjectServiceProvider;

/**
 *
 * @author Jaroslav Bachorik
 */
@ProjectServiceProvider(service=CategoryDefinitionProcessor.class, projectTypes={
    @ProjectType(id="org-netbeans-modules-java-j2seproject"),
    @ProjectType(id="org-netbeans-modules-ant-freeform", position=1202),
    @ProjectType(id="org-netbeans-modules-apisupport-project"),
    @ProjectType(id="org-netbeans-modules-apisupport-project-suite"),
    @ProjectType(id="org-netbeans-modules-j2ee-ejbjarproject"),
    @ProjectType(id="org-netbeans-modules-web-project"),
    @ProjectType(id="org-netbeans-modules-maven")
})
final public class MarkerProcessor extends CategoryDefinitionProcessor implements Marker {

    private final static Logger LOGGER = Logger.getLogger(MarkerProcessor.class.getName());
    private MethodMarker mMarker = new MethodMarker();
    private ClassMarker cMarker = new ClassMarker();
    private PackageMarker pMarker = new PackageMarker();
    private CompositeMarker cmMarker = new CompositeMarker();
    private Project pp;

    public MarkerProcessor(final Project prj) {
        pp = prj;
    }

    @Override
    public void process(SubtypeCategoryDefinition def) {
        if (def.getExcludes() == null && def.getIncludes() == null) {
            addInterfaceMarker(mMarker, def.getTypeName(), def.getAssignedMark());
        } else {
            if (def.getExcludes() != null) {
                addInterfaceMarker(mMarker, def.getTypeName(), def.getExcludes(), false, def.getAssignedMark());
            }
            if (def.getIncludes() != null) {
                addInterfaceMarker(mMarker, def.getTypeName(), def.getIncludes(), true, def.getAssignedMark());
            }
        }
    }

    @Override
    public void process(SingleTypeCategoryDefinition def) {
        if (def.getExcludes() == null && def.getIncludes() == null) {
            cMarker.addClassMark(def.getTypeName(), def.getAssignedMark());
        } else {
            if (def.getExcludes() != null) {
                addTypeMarker(mMarker, def.getTypeName(), def.getExcludes(), false, def.getAssignedMark());
            }
            if (def.getIncludes() != null) {
                addTypeMarker(mMarker, def.getTypeName(), def.getIncludes(), true, def.getAssignedMark());
            }
        }
    }

    @Override
    public void process(CustomCategoryDefinition def) {
        cmMarker.addMarker(def.getCustomMarker());
    }

    @Override
    public void process(PackageCategoryDefinition def) {
        pMarker.addPackageMark(def.getPackageName(), def.getAssignedMark(), def.isRecursive());
    }

    public MarkMapping[] getMappings() {
        List<MarkMapping> mappings = new ArrayList<MarkMapping>();
        mappings.addAll(Arrays.asList(mMarker.getMappings()));
        mappings.addAll(Arrays.asList(cMarker.getMappings()));
        mappings.addAll(Arrays.asList(pMarker.getMappings()));
        mappings.addAll(Arrays.asList(cmMarker.getMappings()));
        return mappings.toArray(new MarkMapping[mappings.size()]);
    }

    public Mark[] getMarks() {
        Set<Mark> marks = new HashSet<Mark>();
        marks.addAll(Arrays.asList(mMarker.getMarks()));
        marks.addAll(Arrays.asList(cMarker.getMarks()));
        marks.addAll(Arrays.asList(pMarker.getMarks()));
        marks.addAll(Arrays.asList(cmMarker.getMarks()));
        return marks.toArray(new Mark[marks.size()]);
    }

    private void addInterfaceMarker(MethodMarker marker, String interfaceName, Mark mark) {
        addInterfaceMarker(marker, interfaceName, null, false, mark);
    }

    private void addInterfaceMarker(final MethodMarker marker, final String interfaceName,
            final String[] methodNameRestriction, final boolean inclusive, final Mark mark) {
                
        final Set<String> restrictorSet = methodNameRestriction != null ? new HashSet<String>(Arrays.asList(methodNameRestriction)) : Collections.EMPTY_SET;
        
        SourceClassInfo ci = ProfilerTypeUtils.resolveClass(interfaceName, pp);
        
        if (ci == null) {
            LOGGER.log(Level.FINE, "Couldn''t resolve type: {0}", interfaceName);
            return;
        }
        
        Set<SourceMethodInfo> applicableMethods = addTypeMarker(marker, ci, restrictorSet, inclusive, mark);
        
        for (SourceClassInfo sci : ci.getSubclasses()) {
            for(SourceMethodInfo smi : applicableMethods) {
                marker.addMethodMark(sci.getQualifiedName(), smi.getName(), smi.getSignature(), mark);
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.log(Level.FINEST, "marking method: {0}#{1}{2}", new Object[]{sci.getQualifiedName(), smi.getName(), smi.getSignature()});
                }
            }
//            addTypeMarker(marker, sci, restrictorSet, inclusive, mark);
        }
    }

    private void addTypeMarker(final MethodMarker marker, final String type, final String[] methodNameRestriction,
            final boolean inclusive, final Mark mark) {
        final Set<String> restrictorSet = methodNameRestriction != null ? new HashSet<String>(Arrays.asList(methodNameRestriction)) : Collections.EMPTY_SET;
        JavaSource src = ElementUtilitiesEx.getSources(pp);
        if (src != null) {
            ElementHandle<TypeElement> eh = ElementUtilitiesEx.resolveClassByName(type, src.getClasspathInfo(), false);
            if (eh != null) {
                SourceClassInfo ci = new JavacClassInfo(eh, src.getClasspathInfo());
                addTypeMarker(marker, ci, restrictorSet, inclusive, mark);
            } else {
                LOGGER.log(Level.FINE, "Couldn''t resolve type: {0}", type);
            }
        }
    }
    
    private Set<SourceMethodInfo> addTypeMarker(final MethodMarker marker, final SourceClassInfo classType, final Set<String> restrictors,
            final boolean inclusive, final Mark mark) {
        final Set<SourceMethodInfo> applicableMethods = new HashSet<SourceMethodInfo>();
        for(SourceMethodInfo mi : classType.getMethods(true)) {
            if (!Modifier.isPrivate(mi.getModifiers())) {
                if ((inclusive && restrictors.contains(mi.getName())) || (!inclusive && !restrictors.contains(mi.getName()))) {
                    if (!Modifier.isFinal(mi.getModifiers())) {
                        applicableMethods.add(mi);
                    }
                    if (!Modifier.isAbstract(mi.getModifiers())) {
                        marker.addMethodMark(mi.getClassName(), mi.getName(), mi.getSignature(), mark);
                    }
                }
            }
        }
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "added type marker for {0}", classType.getQualifiedName());
        }
        return applicableMethods;
    }
}

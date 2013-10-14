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
package org.netbeans.modules.html.angular.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Pisl
 */
public class AngularJsIndex {
    
    private static final Logger LOGGER = Logger.getLogger(AngularJsIndex.class.getSimpleName());
    
    private static final Map<Project, AngularJsIndex> INDEXES = new WeakHashMap<>();
    private final QuerySupport querySupport;
    
    public static AngularJsIndex get(Project project) throws IOException {
        if(project == null) {
            return null;
        }
        synchronized (INDEXES) {
            AngularJsIndex index = INDEXES.get(project);
            if(index == null) {
                index = new AngularJsIndex(project);
                INDEXES.put(project, index);
            } 
            return index;
        }
    }
    
    private AngularJsIndex(Project project) throws IOException {
        Collection<FileObject>sourceRoots = QuerySupport.findRoots(project,
                null /* all source roots */,
                Collections.<String>emptyList(),
                Collections.<String>emptyList());
        this.querySupport = QuerySupport.forRoots(AngularJsIndexer.Factory.NAME, AngularJsIndexer.Factory.VERSION, sourceRoots.toArray(new FileObject[]{}));        
    }
    
    public Collection<AngularJsController> getControllers(final String name, final boolean exact) {
        Collection<? extends IndexResult> result = null;
        try {
            result = querySupport.query(AngularJsIndexer.FIELD_CONTROLLER, name, QuerySupport.Kind.PREFIX, AngularJsIndexer.FIELD_CONTROLLER);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (result != null && !result.isEmpty()) {
            Collection<AngularJsController> controllers = new ArrayList<>();
            for (IndexResult indexResult : result) {
                Collection<AngularJsController> possibleControllers = createControllers(indexResult);
                for(AngularJsController controller: possibleControllers) {
                    if (exact && (controller.getName().equals(name)) || (!exact && controller.getName().startsWith(name))) {
                        controllers.add(controller);
                    }
                }
            }
            return controllers;
        }
        return Collections.emptyList();
    }
    
    private Collection<AngularJsController> createControllers(final IndexResult indexResult) {
        String[] values = indexResult.getValues(AngularJsIndexer.FIELD_CONTROLLER);
        Collection<AngularJsController> result = new ArrayList<>(values.length);
        for (String value : values) {
            if (value != null && !value.isEmpty() && value.indexOf(':') > 0) {
                String[] split = value.split(":");
                result.add(new AngularJsController(split[0], split[1], indexResult.getFile().getPath()));
            }
        }
        return result;
    }
}

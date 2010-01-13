/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.editor;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.Element;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.beans.api.model.ModelUnit;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author marekfukala
 */
public class WebBeansModelSupport {

    //XXX error reporting ... the exceptions are just logged and an empty list is returned.
    public static List<Element> getNamedBeans(MetadataModel<WebBeansModel> webBeansModel) {
	try {
	    List<Element> filtered = new LinkedList<Element>(webBeansModel.runReadAction(new MetadataModelAction<WebBeansModel, List<Element>>() {

		public List<Element> run(WebBeansModel metadata) throws Exception {
		    return metadata.getNamedElements();
		}
	    }));

	    //filter out null elements:
	    //probably a WebBeansModel bug, happens under some circumstances when renaming/deleting beans
	    Iterator<Element> filtering = filtered.iterator();
	    while(filtering.hasNext()) {
		if(null == filtering.next()) {
		    filtering.remove();
		}
	    }

	    return filtered;
	} catch (MetadataModelException ex) {
	    Exceptions.printStackTrace(ex);
	} catch (IOException ex) {
	    Exceptions.printStackTrace(ex);
	}

	return Collections.emptyList();
    }

    //>>> copied from JsfModelFactory (web.jsf module), must be refactored out
    public static ModelUnit getModelUnit(WebModule module) {
	if (module == null) {
	    return null;
	}
	FileObject fileObject = getFileObject(module);
	Project project = FileOwnerQuery.getOwner(fileObject);
	if (project == null) {
	    return null;
	}
	ClassPath boot = getClassPath(project, ClassPath.BOOT);
	ClassPath compile = getClassPath(project, ClassPath.COMPILE);
	ClassPath src = getClassPath(project, ClassPath.SOURCE);
	return ModelUnit.create(boot, compile, src);
    }

    private static ClassPath getClassPath(Project project, String type) {
	ClassPathProvider provider = project.getLookup().lookup(
		ClassPathProvider.class);
	if (provider == null) {
	    return null;
	}
	Sources sources = project.getLookup().lookup(Sources.class);
	if (sources == null) {
	    return null;
	}
	SourceGroup[] sourceGroups = sources.getSourceGroups(
		JavaProjectConstants.SOURCES_TYPE_JAVA);
	ClassPath[] paths = new ClassPath[sourceGroups.length];
	int i = 0;
	for (SourceGroup sourceGroup : sourceGroups) {
	    FileObject rootFolder = sourceGroup.getRootFolder();
	    paths[i] = provider.findClassPath(rootFolder, type);
	}
	return ClassPathSupport.createProxyClassPath(paths);
    }

    private static FileObject getFileObject(WebModule module) {
	FileObject fileObject = module.getDocumentBase();
	if (fileObject != null) {
	    return fileObject;
	}
	fileObject = module.getDeploymentDescriptor();
	if (fileObject != null) {
	    return fileObject;
	}
	fileObject = module.getWebInf();
	if (fileObject != null) {
	    return fileObject;
	}

	FileObject[] facesConfigFiles = ConfigurationUtils.getFacesConfigFiles(module);
	if (facesConfigFiles != null && facesConfigFiles.length > 0) {
	    return facesConfigFiles[0];
	}

	FileObject[] fileObjects = module.getJavaSources();
	if (fileObjects != null) {
	    for (FileObject source : fileObjects) {
		if (source != null) {
		    return source;
		}
	    }
	}
	return null;
    }
    //<<< end of copied code
}

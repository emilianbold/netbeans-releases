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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.projectimport.j2seimport;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Radek Matous
 */
public final class AbstractProject implements ProjectModel {
    private final Collection libraries;
    private final Collection userLibraries;
    private final Collection sourceRoots;
    private final Set dependencies;
    private File jdkDirectory;
    private String jdkId;    
    private final FileObject projectDir;
    private final String name;
    private WarningContainer warnings;
    
    private boolean isAlreadyImported = false;
    
    public static final Logger logger =
            LoggerFactory.getDefault().createLogger(AbstractProject.class);
    
    
    /** Creates a new instance of AbstractProjectDefinition */
    public AbstractProject(String name, FileObject projectDir) {
        this.projectDir = projectDir;
        this.name = name;
        if (name.indexOf('/') != -1) {
            throw new IllegalArgumentException();
        }
        
        libraries = new LinkedHashSet();
        userLibraries = new LinkedHashSet();
        sourceRoots = new LinkedHashSet();
        dependencies = new LinkedHashSet();
        warnings = new WarningContainer();
        
        logger.finest("created project: " + "\""+ name + "\"  (" + //NOI18N
                projectDir.getPath() + ")");//NOI18N
        
    }
    
    public String getName() {
        return name;
    }
    
    public FileObject getProjectDir() {
        return projectDir;
    }
    
    
    public boolean isAlreadyImported() {
        return isAlreadyImported;
    }
    
    public void setAsImported() {
        isAlreadyImported = true;
    }
    
    public Collection/*<AbstractProjectDefinition.AbstractLibraryEntry>*/ getLibraries() {
        return libraries;
    }
    
    private void addWarning(final String warning, boolean userNotification) {
        StringBuffer sbuf = new StringBuffer(NbBundle.getMessage(AbstractProject.class, "MSG_ProjectDefinitionWarning"));//NOI18N
        sbuf.append(" ").append(warning);//NOI18N
        
        String warningPlusPrefix = sbuf.toString();
        warnings.add(warningPlusPrefix, userNotification);
        AbstractProject.logger.warning(warningPlusPrefix);
    }
    
    
    public boolean addLibrary(final AbstractProject.Library lEntry) {
        if (isAlreadyImported()) {
            throw new IllegalStateException("Unexpected usage: project was already imported");//NOI18N
        }
        
        if (!lEntry.isValid()) {
            addWarning(NbBundle.getMessage(AbstractProject.class, "MSG_InvalidLibrary",
                    lEntry.getArchiv().getAbsolutePath(),this.getName()), false);//NOI18N
        }
        
        boolean retVal = libraries.add(lEntry);
        if (retVal == false) {
            addWarning(NbBundle.getMessage(AbstractProject.class, "MSG_AlreadyExistsLibrary",
                    lEntry.getArchiv().getAbsolutePath(),this.getName()), false); //NOI18N
        }
        
        logger.finest("added library: " + "\"" + lEntry.getArchiv().getAbsolutePath() + "\""); //NOI18N
        
        return retVal;
    }
    
    
    public Collection/*<AbstractProjectDefinition.AbstractUserLibrary>*/ getUserLibraries() {
        return userLibraries;
    }
    
    public boolean addUserLibrary(final  AbstractProject.UserLibrary uEntry) {
        if (isAlreadyImported()) {
            throw new IllegalStateException("Unexpected usage: project was already imported");//NOI18N
        }
        
        if (!uEntry.isValid()) {
            addWarning(NbBundle.getMessage(AbstractProject.class, "MSG_InvalidUserLibrary",
                    uEntry.getName(),this.getName()), true); //NOI18N
        }
        
        checkUserLibrary(uEntry);
        boolean retVal = userLibraries.add(uEntry);
        
        if (retVal == false) {
            addWarning(NbBundle.getMessage(AbstractProject.class, "MSG_AlreadyExistsUserLibrary",
                    uEntry.getName(),this.getName()), false); //NOI18N
        }
        
        logger.finest("added user library: " + "\"" + uEntry.getName() + "\""); //NOI18N
        
        return retVal;
    }
    
    private void checkUserLibrary(AbstractProject.UserLibrary uLibrary) {
        for (Iterator it = uLibrary.getLibraries().iterator(); it.hasNext();) {
            AbstractProject.Library    lEntry = (AbstractProject.Library)it.next();
            if (getLibraries().contains(lEntry)) {
                addWarning(NbBundle.getMessage(AbstractProject.class, "MSG_AlreadyExistsLibrary",
                        lEntry.getArchiv().getAbsolutePath(),this.getName()), false); //NOI18N
            }
        }
    }
    
    public Collection/*<AbstractProjectDefinition.AbstractSourceRootEntry>*/ getSourceRoots() {
        return sourceRoots;
    }
    
    public boolean addSourceRoot(final AbstractProject.SourceRoot srcEntry) {
        if (isAlreadyImported()) {
            throw new IllegalStateException("Unexpected usage: project was already imported");//NOI18N
        }
        
        if (!srcEntry.isValid()) {
            addWarning(NbBundle.getMessage(AbstractProject.class, "MSG_InvalidSourceRoot",
                    srcEntry.getDirectory().getAbsolutePath(),this.getName()), false); //NOI18N
            return false;
        }
        
        boolean retVal = sourceRoots.add(srcEntry);
        if (retVal == false) {
            addWarning(NbBundle.getMessage(AbstractProject.class, "MSG_AlreadyExistsSourceRoot",
                    srcEntry.getDirectory().getAbsolutePath(),this.getName()), false); //NOI18N
        }
        
        logger.finest("added source root: " + "\"" + srcEntry.getLabel()+" ("+srcEntry.getDirectory().getAbsolutePath()+ ")\"" ); //NOI18N
        
        return retVal;
    }
    
    public java.util.Set/*<AbstractProjectDefinition>*/ getDependencies() {
        return dependencies;
    }
    
    public boolean addDependency(final AbstractProject projectDefinition) {
        if (isAlreadyImported()) {
            throw new IllegalStateException("Unexpected usage: project was already imported");//NOI18N
        }
        
        logger.finest("added dependency: " + "\""+ projectDefinition.getName() + "\"  (" + //NOI18N
                projectDefinition.getProjectDir().getPath() + ")");//NOI18N
        
        return dependencies.add(projectDefinition);
    }
    
    
    public File getJDKDirectory() {
        return jdkDirectory;
    }
    
    public void setJDKDirectory(File jdkDirectory) {
        this.jdkDirectory = jdkDirectory;
    }
    
    public void setInvalidJDK(String expectedJDKId) {
        addWarning(NbBundle.getMessage(AbstractProject.class, "MSG_JDKDoesnExistUseDefault",
                this.getName(), expectedJDKId),true); //NOI18N
    }
    
    
    public WarningContainer getWarnings() {
        return warnings;
    }
    
    public Collection/**<String>*/ getErrors() {
        Collection errors = new HashSet();
        DependencyValidator instance = DependencyValidator.checkProject(this);
        if (!instance.isValid()) {
            errors.add(instance.getErrorMessage());
        }
        if (getSourceRoots().size() == 0) {
            errors.add(NbBundle.getMessage(
                    AbstractProject.class,"ERR_NoSourceRoot",this.getName()));//NOI18N
            
        }
        return errors;
    }
    
    
    public static final class SourceRoot implements ProjectModel.SourceRoot {
        private final String label;
        private final File sourceFolder;
        
        public SourceRoot(String label, File sourceFolder) {
            this.label = label;
            this.sourceFolder = sourceFolder;
        }
        public final String getLabel() {
            return label;
        }
        
        public final File getDirectory() {
            return sourceFolder;
        }
        
        
        public final boolean  isValid() {
            return AbstractProject.isValid(getDirectory());
        }
        
        public boolean equals(Object obj) {
            return (obj instanceof SourceRoot) ?
                ((SourceRoot)obj).getDirectory().equals(getDirectory()) : false;
        }
        
        public int hashCode() {
            return getLabel().hashCode();
        }
        
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(NbBundle.getMessage(AbstractProject.class,"TXT_SourceRoot"));//NOI18N
            sb.append((isValid()) ? "" : "!");//NOI18N
            sb.append((sourceFolder != null) ? sourceFolder.getAbsolutePath() : "");//NOI18N
            
            return sb.toString();
        }
        
    }
    
    public static final class Library  implements ProjectModel.Library {
        private File archiv;
        public Library(File archiv) {
            this.archiv = archiv;
        }
        
        public java.io.File getArchiv() {
            return archiv;
        }
        
        public final boolean  isValid() {
            return AbstractProject.isValidArchiv(getArchiv());
        }
        
        public boolean equals(Object obj) {
            return (obj instanceof Library ) ?
                ((Library )obj).getArchiv().equals(getArchiv()) : false;
        }
        
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(NbBundle.getMessage(AbstractProject.class,"TXT_Library"));//NOI18N
            sb.append((isValid()) ? "" : "!");//NOI18N
            sb.append((archiv != null) ? archiv.getAbsolutePath() : "");//NOI18N
            
            return sb.toString();
        }
    }
    
    public static final class UserLibrary implements ProjectModel.UserLibrary {
        private final String name;
        private Collection/*<AbstractProject.Library>*/ libraries;
        private boolean fileNotFound = false;
        private Collection/*<AbstractProject.UserLibrary>*/ dependencies;
        

        public UserLibrary(String name, boolean fileNotFound) {
            this(name);
            this.fileNotFound = fileNotFound;
        }
        
        public UserLibrary(String name) {
            this.name = name;
            libraries = new HashSet();
            dependencies = new HashSet();
        }
        
        public UserLibrary(String name, Collection/*<AbstractLibraryEntry>*/ libraries) {
            this(name);
            libraries.addAll(libraries);
        }
        
        
        public boolean addLibrary(ProjectModel.Library lEntry) {
            logger.finest("added library: " + "\"" + lEntry.getArchiv().getAbsolutePath() + "\""); //NOI18N
            
            return libraries.add(lEntry);
        }
        
        public boolean addDependency(ProjectModel.UserLibrary uLibrary) {
            logger.finest("added library dependency: " + "\"" + uLibrary.getName() + "\""); //NOI18N
            
            return dependencies.add(uLibrary);
        }
        
        
        
        public String getName() {
            return name;
        }
        
        public Collection/*<AbstractProjectDefinition.AbstractLibraryEntry>*/ getLibraries() {
            return libraries;
        }

        public final boolean  fileNotFound() {
            return fileNotFound;
        }
        
        public final boolean  isValid() {
            boolean isValid = false;//no included library means invalid state
            for (Iterator it = getLibraries().iterator(); it.hasNext();) {
                AbstractProject.Library lEntry = (AbstractProject.Library)it.next();
                isValid = lEntry.isValid();
                if (!isValid) {break;}
                
            }
            return isValid;
        }
        
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(NbBundle.getMessage(AbstractProject.class,"TXT_UserLibrary"));//NOI18N
            sb.append((isValid()) ? "" : "!");//NOI18N
            sb.append(getName());
            
            return sb.toString();
        }

        public Collection getDependencies() {
            return dependencies;
        }
        
        
    }
    
    private static boolean isValid(File f) {
        File srcFolder = FileUtil.normalizeFile(f);
        return (srcFolder.isDirectory() && FileUtil.toFileObject(srcFolder) != null);
    }
    
    private static boolean isValidArchiv(File f) {
        File srcFolder = FileUtil.normalizeFile(f);
        FileObject srcFileObject = FileUtil.toFileObject(srcFolder);
        return (srcFolder.exists() && srcFileObject != null) && FileUtil.isArchiveFile(srcFileObject);
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        
        sb.append(NbBundle.getMessage(AbstractProject.class,"TXT_Project"));//NOI18N
        sb.append(this.getName()).append("\n");//NOI18N
        
        Collection all = new ArrayList();
        all.addAll(getSourceRoots());
        all.addAll(getLibraries());
        all.addAll(getUserLibraries());
        
        for (Iterator it = all.iterator(); it.hasNext();) {
            sb.append(it.next().toString()).append("\n");//NOI18N
        }
        
        for (Iterator it = getDependencies().iterator(); it.hasNext();) {
            sb.append(NbBundle.getMessage(AbstractProject.class,"TXT_Deps"));//NOI18N
            sb.append(((AbstractProject)it.next()).getProjectDir().getPath()).append("\n");
        }
        
        
        
        sb.append(NbBundle.getMessage(AbstractProject.class,"TXT_JDK"));//NOI18N
        sb.append((getJDKDirectory() != null) ? getJDKDirectory().getAbsolutePath() : "!");//NOI18N
        
        return sb.toString();
    }

    public String getJdkId() {
        return jdkId;
    }

    public void setJdkId(String jdkId) {
        this.jdkId = jdkId;
    }
    
}

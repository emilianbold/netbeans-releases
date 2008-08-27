/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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


package org.netbeans.modules.maven;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.spi.java.project.classpath.ProjectClassPathModifierImplementation;
import org.netbeans.spi.project.LookupMerger;
import org.openide.util.Lookup;

/**
 * TODO: The idea of having a LookupMerger for this class is not 100% semantically correct.
 * The original add/remove methods return values have different meaning than here.
 * if true is returned from impls means everything is done no further processing necessary, 
 * false means not relevant, try another impl or fallback to default.
 * a proper solution would be to create our own api that would reflect this difference.
 * @author mkleint
 */
public class CPModifierLookupMerger implements LookupMerger<ProjectClassPathModifierImplementation>{
    
    private CPExtender fallback;
    private Extender instance;
    
    /** Creates a new instance of CPExtenderLookupMerger */
    public CPModifierLookupMerger(CPExtender fallbck) {
        fallback = fallbck;
        assert fallback != null;
    }
    
    public Class<ProjectClassPathModifierImplementation> getMergeableClass() {
        return ProjectClassPathModifierImplementation.class;
    }

    public synchronized ProjectClassPathModifierImplementation merge(Lookup lookup) {
        if (instance == null) {
            instance =  new Extender();
        }
        instance.setLookup(lookup);
        return instance;
    }

    private class Extender extends ProjectClassPathModifierImplementation {
        
        private Lookup context;
        
        private Extender() {
            this.context = context;
        }
        private void setLookup(Lookup context) {
            this.context = context;
        }
    
        private Object retVal(String methodName, ProjectClassPathModifierImplementation impl, 
                              Class<?>[] paramTypes,
                              Object... params) throws IOException {
            try {
                Method meth = impl.getClass().getDeclaredMethod(methodName, paramTypes);
                meth.setAccessible(true);
                return meth.invoke(impl, params);
            } catch (InvocationTargetException x) {
                if (x.getCause() instanceof IOException) {
                    throw (IOException)x.getCause();
                }
                //JDK16 can replace with new IOException(x.getCause());
                IOException ex = new IOException(x.getCause().getMessage());
                ex.initCause(x.getCause());
                throw ex;
            } catch (Exception e) {
                e.printStackTrace();
                throw new AssertionError("Cannot use reflection on " + impl + " method:" + methodName);
            }
        }

        protected SourceGroup[] getExtensibleSourceGroups() {
            Collection<? extends ProjectClassPathModifierImplementation> list = context.lookupAll(ProjectClassPathModifierImplementation.class);
            Collection<SourceGroup> sg = new HashSet<SourceGroup>();
            for (ProjectClassPathModifierImplementation ext : list) {
                try {
                    SourceGroup[] sgs = (SourceGroup[])retVal("getExtensibleSourceGroups", ext, null);//NOI18N
                    sg.addAll(Arrays.asList(sgs));
                } catch (IOException e) {
                    //should not happen at all.
                }
            }
            sg.addAll(Arrays.asList(fallback.getExtensibleSourceGroups()));
            return sg.toArray(new SourceGroup[sg.size()]);
        }

        protected String[] getExtensibleClassPathTypes(SourceGroup arg0) {
            Collection<? extends ProjectClassPathModifierImplementation> list = context.lookupAll(ProjectClassPathModifierImplementation.class);
            Collection<String> retVal = new HashSet<String>();
            for (ProjectClassPathModifierImplementation ext : list) {
                try {
                    String[] ret = (String[])retVal("getExtensibleClassPathTypes", ext, //NOI18N
                            new Class<?>[] {SourceGroup.class}, arg0 );
                    retVal.addAll(Arrays.asList(ret));
                } catch (IOException e) {
                    //should not happen at all.
                }
            }
            retVal.addAll(Arrays.asList(fallback.getExtensibleClassPathTypes(arg0)));
            return retVal.toArray(new String[retVal.size()]);
        }

        protected boolean addLibraries(Library[] arg0, SourceGroup arg1,
                                       String arg2) throws IOException,
                                                           UnsupportedOperationException {
            Collection<? extends ProjectClassPathModifierImplementation> list = context.lookupAll(ProjectClassPathModifierImplementation.class);
            for (ProjectClassPathModifierImplementation ext : list) {
                Boolean ret = (Boolean)retVal("addLibraries", ext, //NOI18N
                        new Class<?>[] { new Library[0].getClass(), SourceGroup.class, String.class}, arg0, arg1, arg2);
                if (ret.booleanValue()) {
                    return ret.booleanValue();
                }
            }
            return fallback.addLibraries(arg0, arg1, arg2);
        }

        protected boolean removeLibraries(Library[] arg0, SourceGroup arg1,
                                          String arg2) throws IOException,
                                                              UnsupportedOperationException {
            Collection<? extends ProjectClassPathModifierImplementation> list = context.lookupAll(ProjectClassPathModifierImplementation.class);
            for (ProjectClassPathModifierImplementation ext : list) {
                Boolean ret = (Boolean)retVal("removeLibraries", ext, //NOI18N
                        new Class<?>[] { new Library[0].getClass(), SourceGroup.class, String.class}, arg0, arg1, arg2);
                if (ret.booleanValue()) {
                    return ret.booleanValue();
                }
            }
            return fallback.removeLibraries(arg0, arg1, arg2);
        }

        protected boolean addRoots(URL[] arg0, SourceGroup arg1, String arg2) throws IOException,
                                                                                     UnsupportedOperationException {
            Collection<? extends ProjectClassPathModifierImplementation> list = context.lookupAll(ProjectClassPathModifierImplementation.class);
            for (ProjectClassPathModifierImplementation ext : list) {
                Boolean ret = (Boolean)retVal("addRoots", ext, //NOI18N
                        new Class<?>[] { new URL[0].getClass(), SourceGroup.class, String.class}, arg0, arg1, arg2);
                if (ret.booleanValue()) {
                    return ret.booleanValue();
                }
            }
            return fallback.addRoots(arg0, arg1, arg2);
        }

        protected boolean removeRoots(URL[] arg0, SourceGroup arg1, String arg2) throws IOException,
                                                                                        UnsupportedOperationException {
            Collection<? extends ProjectClassPathModifierImplementation> list = context.lookupAll(ProjectClassPathModifierImplementation.class);
            for (ProjectClassPathModifierImplementation ext : list) {
                Boolean ret = (Boolean)retVal("removeRoots", ext, //NOI18N
                        new Class<?>[] { new URL[0].getClass(), SourceGroup.class, String.class}, arg0, arg1, arg2);
                if (ret.booleanValue()) {
                    return ret.booleanValue();
                }
            }
            return fallback.removeRoots(arg0, arg1, arg2);
        }

        protected boolean addAntArtifacts(AntArtifact[] arg0, URI[] arg1,
                                          SourceGroup arg2, String arg3) throws IOException,
                                                                                UnsupportedOperationException {
            Collection<? extends ProjectClassPathModifierImplementation> list = context.lookupAll(ProjectClassPathModifierImplementation.class);
            for (ProjectClassPathModifierImplementation ext : list) {
                Boolean ret = (Boolean)retVal("addAntArtifacts", ext, //NOI18N
                        new Class<?>[] { new AntArtifact[0].getClass(), new URI[0].getClass(), SourceGroup.class, String.class}, arg0, arg1, arg2, arg3);
                if (ret.booleanValue()) {
                    return ret.booleanValue();
                }
            }
            return fallback.addAntArtifacts(arg0, arg1, arg2, arg3);
        }

        protected boolean removeAntArtifacts(AntArtifact[] arg0, URI[] arg1,
                                             SourceGroup arg2, String arg3) throws IOException,
                                                                                   UnsupportedOperationException {
            Collection<? extends ProjectClassPathModifierImplementation> list = context.lookupAll(ProjectClassPathModifierImplementation.class);
            for (ProjectClassPathModifierImplementation ext : list) {
                Boolean ret = (Boolean)retVal("addAntArtifacts", ext, //NOI18N
                        new Class<?>[] { new AntArtifact[0].getClass(), new URI[0].getClass(), SourceGroup.class, String.class}, arg0, arg1, arg2, arg3);
                if (ret.booleanValue()) {
                    return ret.booleanValue();
                }
            }
            return fallback.addAntArtifacts(arg0, arg1, arg2, arg3);
        }

    }
    
}

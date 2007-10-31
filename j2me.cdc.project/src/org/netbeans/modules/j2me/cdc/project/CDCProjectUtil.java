/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.j2me.cdc.project;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.ClassPath.Entry;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.queries.SourceForBinaryQuery.Result;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.SearchKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.mobility.project.ui.customizer.ProjectProperties;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2me.cdc.platform.CDCDevice;
import org.netbeans.modules.j2me.cdc.platform.CDCPlatform;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Miscellaneous utilities for the cdcproject module.
 * @author  Jiri Rechtacek
 */
public class CDCProjectUtil {
    private CDCProjectUtil () {}
    
    /** Check if the given file object represents a source with the main method.
     * 
     * @param fo source
     * @return true if the source contains the main method
     */
    public static boolean hasMainMethod(FileObject fo) {
        // support for unit testing
        if (MainClassChooser.unitTestingSupport_hasMainMethodResult != null) {
            return MainClassChooser.unitTestingSupport_hasMainMethodResult.booleanValue ();
        }
        if (fo == null) {
            // ??? maybe better should be thrown IAE
            return false;
        }
        return !SourceUtils.getMainClasses(fo).isEmpty();
    }

    /** Returns list of FQN of classes contains the main method.
     * 
     * @param roots the classpath roots of source to start find
     * @return list of names of classes, e.g, [sample.project1.Hello, sample.project.app.MainApp]
     */
    public static List<String> getMainClasses (FileObject[] roots, Map<String,String> executionModes,String bootcp) {
        List<String> result = new ArrayList<String> ();
        for (FileObject fo : roots) {
            getMainClasses(fo, result, executionModes,bootcp);
        }
        return result;
    }
    
    /** Returns list of FQN of classes contains the main method.
     * 
     * @param root the root of source to start find
     * @param addInto list of names of classes, e.g, [sample.project1.Hello, sample.project.app.MainApp]
     */
    private static void getMainClasses (final FileObject root, final List<String> addInto, final Map<String,String> executionModes, final String bootcp) {       
        final String specialXletFqn = (executionModes != null) ? executionModes.get(CDCPlatform.PROP_EXEC_XLET)  : null;
        final String specialAppletFqn = (executionModes != null) ? executionModes.get(CDCPlatform.PROP_EXEC_APPLET)  : null;
        
        //We must get acuall (choosen in the customizer bootclasspath so we can't usee ClassPath.Boot
        ClassPath bcp=null;
        if (bootcp != null)
        {
            StringTokenizer tokens=new StringTokenizer(bootcp,File.pathSeparator);
            if (tokens.countTokens()>0)
            {
                FileObject bcpRoots[]=new FileObject[tokens.countTokens()];
                int i=0;
                for (;tokens.hasMoreTokens();i++)
                {
                    FileObject fo=FileUtil.toFileObject(new File(tokens.nextToken()));
                    if (FileUtil.isArchiveFile(fo))
                        bcpRoots[i]=FileUtil.getArchiveRoot(fo);
                    else
                        bcpRoots[i]=fo;
                }

                bcp=ClassPathSupport.createClassPath(bcpRoots);
            }
        }
        else
            bcp=ClassPath.getClassPath (root, ClassPath.BOOT);  //Single compilation unit
        
        final ClassPath boot = bcp;
        final ClassPath rtm2 = ClassPath.getClassPath (root, ClassPath.EXECUTE);  //Single compilation unit'
        final ClassPath rtm1 = ClassPath.getClassPath (root, ClassPath.COMPILE);
        final ClassPath rtm  = org.netbeans.spi.java.classpath.support.ClassPathSupport.createProxyClassPath(new ClassPath[] { rtm1, rtm2 } );
        
        
        
        /* Here is the trick to include not build dependent projects */
        ArrayList<ClassPath> srcRoots=new ArrayList<ClassPath>();
        final ArrayList<FileObject> srcRootsFO=new ArrayList<FileObject>();
        srcRoots.add(ClassPath.getClassPath (root, ClassPath.SOURCE));
        Library libs[]=LibraryManager.getDefault().getLibraries();
        HashSet<URL> libSet=new HashSet<URL>();
        for (Library lib : libs)
        {
            List<URL> url=lib.getContent("src");
            libSet.addAll(url);
        }
        HashSet<URL> entrySet=new HashSet<URL>();
        for (Entry e: rtm2.entries())
        {
            Result res=null;
            try {
                res=SourceForBinaryQuery.findSourceRoots(e.getURL());
            } catch(Exception ex) {}
            FileObject[] roots=res.getRoots();
            for ( FileObject r : roots)
            {
                ClassPath path=ClassPath.getClassPath(r,ClassPath.SOURCE);
                entrySet.clear();
                for (ClassPath.Entry entry : path.entries())
                {
                    entrySet.add(entry.getURL());
                }
                entrySet.removeAll(libSet);
                if (!srcRoots.contains(path) && entrySet.size()>0 )
                {
                    srcRoots.add(path);
                    for (ClassPath.Entry entry : path.entries())
                            srcRootsFO.add(entry.getRoot());
                }
            }
        }
        
        final ClassPath src = org.netbeans.spi.java.classpath.support.ClassPathSupport.createProxyClassPath(srcRoots.toArray(new ClassPath[srcRoots.size()]));                
         
        final ClasspathInfo cpInfo = ClasspathInfo.create(boot, rtm, src);
        
        JavaSource js = JavaSource.create(cpInfo);
        try {
            js.runUserActionTask(new CancellableTask<CompilationController>() {

                HashSet<SearchKind> sk=new HashSet<SearchKind>();
                HashSet<SearchScope> ss=new HashSet<SearchScope>();
                CompilationController control; 
                
                Collection<ElementHandle<TypeElement>> addChildren(ClassIndex index,Collection<ElementHandle<TypeElement>> elems)
                {
                    Collection<ElementHandle<TypeElement>> elcl=new ArrayList<ElementHandle<TypeElement>>();
                    for (ElementHandle<TypeElement> elem : elems )
                    {
                        Collection<ElementHandle<TypeElement>> newEl=index.getElements(elem, sk, ss);
                        if (newEl.size()!=0)
                        {                            
                            elcl.addAll(addChildren(index,newEl));
                        }
                        elcl.add(elem);
                    }
                    return elcl;
                }
                
                public void run(CompilationController control) throws Exception {
                    control.toPhase(Phase.RESOLVED);
                    TypeElement xlet = control.getElements().getTypeElement(specialXletFqn != null ? specialXletFqn : "javax.microedition.xlet.Xlet");
                    TypeElement applet = control.getElements().getTypeElement(specialAppletFqn != null ? specialAppletFqn : "java.applet.Applet");

                    sk.add(SearchKind.IMPLEMENTORS);
                    ss.add(SearchScope.SOURCE);
                    ss.add(SearchScope.DEPENDENCIES);
                    Collection<ElementHandle<TypeElement>> arr = new ArrayList<ElementHandle<TypeElement>>();
                    if (executionModes == null || (executionModes != null && executionModes.containsKey(CDCPlatform.PROP_EXEC_MAIN))){
                        arr = SourceUtils.getMainClasses(srcRootsFO.toArray(new FileObject[srcRootsFO.size()])); // NOI18N
                    }
                    
                    Collection<ElementHandle<TypeElement>> exec=new ArrayList<ElementHandle<TypeElement>>();
                    Types types=control.getTypes();                    
                    if (xlet !=null && (executionModes == null || executionModes.containsKey(CDCPlatform.PROP_EXEC_XLET)))
                        exec.add(ElementHandle.create(xlet));
                    
                    if (applet != null && (executionModes == null || executionModes.containsKey(CDCPlatform.PROP_EXEC_APPLET)))
                        exec.add(ElementHandle.create(applet));

                    if (arr == null && exec.size() == 0) {
                        // no main classes
                        return;
                    }
                    ClasspathInfo newInfo = ClasspathInfo.create(rtm1, rtm1, src);
                    ClassIndex index=newInfo.getClassIndex();
                    arr.addAll(addChildren(index,exec));
                    arr.removeAll(exec);
                    
                    for (ElementHandle<TypeElement> res : arr ){
                        TypeElement elem=res.resolve(control);
                        if (elem==null)
                        {
                            continue;
                        }
                        addInto.add(elem.getQualifiedName().toString());
                    }
                }
                
                
                public void cancel() {}
                
                },true);
            
        } catch (IOException ex) {ex.printStackTrace();};
    }
    
    /** Returns if the given class name exists under the sources root and
     * it's a main class.
     * 
     * @param className FQN of class
     * @param root roots of sources
     * @return true if the class name exists and it's a main class
     */
    public static boolean isMainClass(String className, FileObject root) {
        ClassPath boot = ClassPath.getClassPath (root, ClassPath.BOOT);  //Single compilation unit
        ClassPath rtm2  = ClassPath.getClassPath (root, ClassPath.EXECUTE);  //Single compilation unit'
        ClassPath rtm1 = ClassPath.getClassPath (root, ClassPath.COMPILE);
        ClassPath rtm  = org.netbeans.spi.java.classpath.support.ClassPathSupport.createProxyClassPath(new ClassPath[] { rtm1, rtm2 } );
        ClassPath clp = ClassPath.getClassPath (root, ClassPath.SOURCE);        
        
        ClasspathInfo cpInfo = ClasspathInfo.create(boot,rtm,clp);
        return SourceUtils.isMainClass (className, cpInfo);        
    }
    
    private static boolean isSubclass(final String className, final String baseClassName, final FileObject root)
    {
        if (className == null)
            return false;
        // support for unit testing
        if (MainClassChooser.unitTestingSupport_hasMainMethodResult != null) {
            return MainClassChooser.unitTestingSupport_hasMainMethodResult.booleanValue ();
        }
        
        ClassPath boot = ClassPath.getClassPath (root, ClassPath.BOOT);  //Single compilation unit
        ClassPath rtm2  = ClassPath.getClassPath (root, ClassPath.EXECUTE);  //Single compilation unit'
        ClassPath rtm1 = ClassPath.getClassPath (root, ClassPath.COMPILE);
        ClassPath rtm  = org.netbeans.spi.java.classpath.support.ClassPathSupport.createProxyClassPath(new ClassPath[] { rtm1, rtm2 } );
        ClassPath clp = ClassPath.getClassPath (root, ClassPath.SOURCE);        
        
        ClasspathInfo cpInfo = ClasspathInfo.create(boot, rtm, clp);
        JavaSource js = JavaSource.create(cpInfo);
        final boolean[] result = new boolean[]{false};
        try {
            
            js.runUserActionTask(new CancellableTask<CompilationController>() {

                public void run(CompilationController control) throws Exception {
                    TypeElement type = control.getElements().getTypeElement(baseClassName);
                    if (type == null) {
                        return;
                    }
                    
                    TypeElement xtype = control.getElements().getTypeElement(className);
                    if (xtype == null) {
                        return;
                    }
                    Types types=control.getTypes();
                    result[0]=types.isSubtype(types.erasure(xtype.asType()),types.erasure(type.asType()));                     
                }

                public void cancel() {}

            }, true);            
        } 
        catch (IOException ioe) {}
        return result[0];
    }

    /** Returns if the given class name exists under the sources root and
     * it's a Xlet class.
     * 
     * @param className FQN of class
     * @param root roots of sources
     * @return true if the class name exists and it's a Xlet class
     */
    public static boolean isXletClass (final String className, FileObject root, final String specialXletFqn) {        
        return isSubclass(className, specialXletFqn != null ? specialXletFqn : "javax.microedition.xlet.Xlet",root);
    }
    
    /** Returns if the given class name exists under the sources root and
     * it's a Applet class.
     * 
     * @param className FQN of class
     * @param roots roots of sources
     * @return true if the class name exists and it's a Xlet class
     */
    public static boolean isAppletClass (final String className, final FileObject root, final String specialAppletFqn) {
        return isSubclass(className, specialAppletFqn != null ? specialAppletFqn : "java.applet.Applet",root);
    }

    

    /**
     * Returns the active platform used by the project or null if the active
     * project platform is broken.
     * @param activePlatformId the name of platform used by Ant script or null
     * for default platform.
     * @return active {@link JavaPlatform} or null if the project's platform
     * is broken
     */
    public static CDCPlatform getActivePlatform (final String activePlatformId) {
        final JavaPlatformManager pm = JavaPlatformManager.getDefault();
        JavaPlatform[] installedPlatforms = pm.getPlatforms(null, new Specification (CDCPlatform.PLATFORM_CDC,null));   //NOI18N
        for (JavaPlatform platform : installedPlatforms ){
            if (platform.getDisplayName().equals(activePlatformId)) {
                return (CDCPlatform) platform;
            }
        }
        return null;
    }
    
    
    
    public static Map<String,String> getExecutionModes(ProjectProperties props){
        String activePlatformId = (String)props.get("platform.active");  //NOI18N
        String defaultDevice    = (String)props.get("platform.device");  //NOI18N
        CDCPlatform platform = getActivePlatform (activePlatformId);
        if (platform == null)
            return null;
        CDCDevice[] devices = platform.getDevices();
        Map<String,String> executionModes = null;
        for (int i = 0; i < devices.length && executionModes == null; i++) {
            if (devices[i].getName().equals(defaultDevice)){
                CDCDevice.CDCProfile[] profiles = devices[i].getProfiles();
                for (CDCDevice.CDCProfile profile : profiles ) {
                    if (profile.isDefault()){
                        executionModes = profile.getExecutionModes();
                        break;
                    }
                }
            }            
        }
        return executionModes;
    }    
}

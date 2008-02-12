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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.spring.beans.refactoring.plugins;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;

/**
 * @author John Baker
 */
public class SpringBeansRefactoringFindUsagesPlugin extends SpringBeansJavaRefactoringPlugin {
    private static final Logger LOGGER = Logger.getLogger(SpringBeansRefactoringFindUsagesPlugin.class.getName());
    private WhereUsedQuery springBeansWhereUsed;

    SpringBeansRefactoringFindUsagesPlugin(WhereUsedQuery query) {
        springBeansWhereUsed = query;
    }

    private Set<FileObject> getRelevantFiles(final TreePathHandle tph) throws IOException {
//        final ClasspathInfo cpInfo = getClasspathInfo(springBeansWhereUsed);
//        final ClassIndex idx = cpInfo.getClassIndex();
        final Set<FileObject> set = new HashSet<FileObject>();
//        final FileObject fo = tph.getFileObject();        
//        ClassPath classPath = ClassPath.getClassPath(fo, ClassPath.SOURCE);
//        final String fqn = classPath.getResourceName(fo, '.', false);                    
//        SpringConfigModel model = SpringConfigModel.forFileObject(fo);
//        final boolean includeGlobal = false;
        
        // XXX support for searching config files not yet working
        
        // See if any Spring configuration files contain the fqn matching the class to find usages for
//        model.runReadAction(new Action<SpringBeans>() {
//            
//            public void run(SpringBeans sb) {
//                List<SpringBean> beans = includeGlobal ? sb.getBeans() : sb.getBeans(FileUtil.toFile(fo));
//                Map<String, SpringBean> name2Bean = getName2Beans(beans, includeGlobal); // if local beans, then add only bean ids;
//                for (String beanName : name2Bean.keySet()) {
//                    SpringBean bean = name2Bean.get(beanName);
//                    
//                    if (bean.getClassName().equals(fqn)) {
//                        set.add(FileUtil.toFileObject(name2Bean.get(bean).getLocation().getFile()));
//                    }
//                }
//            }
//
//            private Map<String, SpringBean> getName2Beans(List<SpringBean> beans, boolean addNames) {
//                Map<String, SpringBean> name2Bean = new HashMap<String, SpringBean>();
//                for (SpringBean bean : beans) {
//                    String beanId = bean.getId();
//                    if (beanId != null) {
//                        name2Bean.put(beanId, bean);
//                    }
//                    if (addNames) {
//                        List<String> beanNames = bean.getNames();
//                        for (String beanName : beanNames) {
//                            name2Bean.put(beanName, bean);
//                        }
//                    }
//                }
//
//                return name2Bean;
//            }
//        });

//       
        return set;
    }
   

    @Override
    public Problem prepare(RefactoringElementsBag refactoringElementsBag) {
        Set<FileObject> relevantFiles = null;
        try {
            relevantFiles = getRelevantFiles(springBeansWhereUsed.getRefactoringSource().lookup(TreePathHandle.class));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return null;
    }

    @Override
    public Problem fastCheckParameters() {
        return null;
    }

    @Override
    public Problem checkParameters() {
        return null;
    }

    public static CloneableEditorSupport findCloneableEditorSupport(DataObject dob) {
        Object obj = dob.getCookie(org.openide.cookies.OpenCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport) obj;
        }
        obj = dob.getCookie(org.openide.cookies.EditorCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport) obj;
        }
        return null;
    }

    public void doRefactoring(List<RefactoringElementImplementation> elements)
            throws IOException {
    }

    @Override
    public void cancelRequest() {
        
    }
  
    private boolean isFindUsages() {
        return springBeansWhereUsed.getBooleanValue(WhereUsedQuery.FIND_REFERENCES);
    }     
}

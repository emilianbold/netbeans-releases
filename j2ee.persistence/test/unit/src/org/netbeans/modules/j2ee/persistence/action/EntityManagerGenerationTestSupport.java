/*
 * ContainerManagedJTANonInjectableInWebTest.java
 * JUnit based test
 *
 * Created on 1 November 2006, 16:00
 */

package org.netbeans.modules.j2ee.persistence.action;

import junit.framework.*;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import java.io.IOException;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.persistence.sourcetestsupport.SourceTestSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Erno Mononen
 */
public abstract class EntityManagerGenerationTestSupport  extends SourceTestSupport{
    
    public EntityManagerGenerationTestSupport(String testName){
        super(testName);
    }
    
    
    protected FileObject generate(FileObject targetFo, final GenerationOptions options) throws IOException{
        
        JavaSource targetSource = JavaSource.forFileObject(targetFo);
        
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            
            public void cancel() {
            }
            
            public void run(WorkingCopy workingCopy) throws Exception {
                
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                
                for (Tree typeDeclaration : cut.getTypeDecls()){
                    if (Tree.Kind.CLASS == typeDeclaration.getKind()){
                        ClassTree clazz = (ClassTree) typeDeclaration;
                        ClassTree modifiedClazz = getStrategy(workingCopy, make, clazz, options).generate();
                        workingCopy.rewrite(clazz, modifiedClazz);
                    }
                }
                
            }
        };
        targetSource.runModificationTask(task).commit();
        
        return targetFo;
        
    }

    protected abstract EntityManagerGenerationStrategy getStrategy(WorkingCopy workingCopy, TreeMaker make, ClassTree clazz, GenerationOptions options);

}


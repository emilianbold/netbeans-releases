/*
 * FoldHierarchyExecutionTest.java
 * JUnit based test
 *
 * Created on June 27, 2004, 1:03 AM
 */

package org.netbeans.modules.editor.fold;

import java.util.Collections;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import junit.framework.*;
import junit.framework.*;
import org.netbeans.junit.*;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldManagerFactory;
import org.netbeans.spi.editor.fold.FoldOperation;

/**
 *
 * @author mmetelka
 */
public class SimpleFoldManagerTest extends NbTestCase {
    
    private static final int MAX_FOLD_MEMORY_SIZE = 64;
    
    static final int FOLD_START_OFFSET_1 = 5;
    static final int FOLD_END_OFFSET_1 = 10;
    
    public SimpleFoldManagerTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(SimpleFoldManagerTest.class);
        return suite;
    }

    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    /**
     * Test the creation of several folds.
     */
    public void test() {
        FoldHierarchyTestEnv env = new FoldHierarchyTestEnv(new SimpleFoldManagerFactory());

        FoldHierarchy hierarchy = env.getHierarchy();
        AbstractDocument doc = env.getDocument();
        doc.readLock();
        try {
            hierarchy.lock();
            try {
                
                Fold rootFold = hierarchy.getRootFold();
                int foldCount = rootFold.getFoldCount();
                int expectedFoldCount = 1;
                assertTrue("Incorrect fold count " + foldCount, // NOI18N
                    (foldCount == expectedFoldCount)
                );
                
                Fold fold = rootFold.getFold(0);
                FoldType foldType = fold.getType();
                int foldStartOffset = fold.getStartOffset();
                int foldEndOffset = fold.getEndOffset();
                assertTrue("Incorrect fold type " + foldType, // NOI18N
                    (foldType == AbstractFoldManager.REGULAR_FOLD_TYPE));
                assertTrue("Incorrect fold start offset " + foldStartOffset, // NOI18N
                    (foldStartOffset == FOLD_START_OFFSET_1));
                assertTrue("Incorrect fold end offset " + foldEndOffset, // NOI18N
                    (foldEndOffset == FOLD_END_OFFSET_1));
                
                // Check fold size
                assertSize("Size of the fold " , Collections.singleton(fold), // NOI18N
                    MAX_FOLD_MEMORY_SIZE, new FoldMemoryFilter(fold));
                
            } finally {
                hierarchy.unlock();
            }
        } finally {
            doc.readUnlock();
        }
    }
    
    
    final class SimpleFoldManager extends AbstractFoldManager {
        
        public void initFolds(FoldHierarchyTransaction transaction) {
            try {
                getOperation().addToHierarchy(
                    REGULAR_FOLD_TYPE,
                    "...", // non-null to properly count fold's size (non-null desc gets set) // NOI18N
                    false,
                    FOLD_START_OFFSET_1, FOLD_END_OFFSET_1, 1, 1,
                    null,
                    transaction
                );
            } catch (BadLocationException e) {
                e.printStackTrace();
                fail();
            }
        }
        
    }

    public final class SimpleFoldManagerFactory implements FoldManagerFactory {
        
        public FoldManager createFoldManager() {
            return new SimpleFoldManager();
        }
        
    }

    private final class FoldMemoryFilter implements MemoryFilter {
        
        private Fold fold;
        
        FoldMemoryFilter(Fold fold) {
            this.fold = fold;
        }
        
        public boolean reject(Object o) {
            return (o == fold.getType())
                || (o == fold.getDescription()) // requires non-null description during construction
                || (o == fold.getParent())
                || (o instanceof org.netbeans.modules.editor.fold.FoldOperationImpl)
                || (o instanceof Position);
            
            // Will count possible FoldChildren and ExtraInfo
        }

    }

}

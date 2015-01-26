package org.netbeans.modules.cnd.api.codemodel.providers;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.api.codemodel.CMCursor;
import org.netbeans.modules.cnd.api.codemodel.CMSourceRange;
import org.netbeans.modules.cnd.api.codemodel.visit.CMVisitQuery;

/**
 * - *
 * -
 *
 * @author Vladimir Kvashin + * This test will take all source files from the
 * folder and visit all cursors
 */


public class CursorsVisitorTest extends ReferenceVisitorTestBase {

    public CursorsVisitorTest(String testName) {
        super(testName);
    }

    public void testCursorVisitor_1() throws Exception {

        File dir = getTestCaseDataDir();
        File[] ccFiles = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith("cc");
            }
        });
        List<String> sourceFiles = new ArrayList<>();
        for (int i = 0; i < ccFiles.length; i++) {
            final String name = ccFiles[i].getName();
            if (name.startsWith("fu")){
                continue;
            }
            sourceFiles.add(name);
        }
        
        
        printVisitedCursors(new CMVisitQuery.CursorAndRangeVisitor() {
            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean visit(CMCursor cur, CMSourceRange curRange) {
                //CMDataBase.getInstance("CursorsVisitorTest").addCursor(cur, curRange);
                return true;
            }
        }, "fu_simple.cc");
        //printVisitedCursors("test.cc");
        System.out.println("Will close DAtabase now");
        //CMH2Database.getInstance("CursorsVisitorTest").shutdown();
    }
}

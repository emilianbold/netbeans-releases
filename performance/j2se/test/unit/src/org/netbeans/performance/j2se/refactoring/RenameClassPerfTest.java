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
package org.netbeans.performance.j2se.refactoring;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.*;
import org.netbeans.junit.NbPerformanceTest;
import org.netbeans.junit.NbPerformanceTest.PerformanceData;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.Lookups;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Pavel Flaska
 */
public class RenameClassPerfTest extends RefactoringTestCase implements NbPerformanceTest {

    private final MyHandler handler;
    private List<PerformanceData> data = new ArrayList<PerformanceData>();
    private static int size=0;

    public RenameClassPerfTest(String name) {
        super(name);
        handler = new MyHandler();
        handler.setLevel(Level.FINE);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new RenameClassPerfTest("testRenameJEditClass"));
        return suite;
    }


    public void testRenameJEditClass() throws IOException, InterruptedException, ExecutionException {
        Logger timer = Logger.getLogger("TIMER.RefactoringSession");
        timer.setLevel(Level.FINE);
        timer.addHandler(handler);

        timer = Logger.getLogger("TIMER.RefactoringPrepare");
        timer.setLevel(Level.FINE);
        timer.addHandler(handler);

        FileObject test = getFileInProject("jEdit41", "/src/org/gjt/sp/jedit/jEdit.java");
        final RenameRefactoring renameRefactoring = new RenameRefactoring(Lookups.singleton(test));
        perform(renameRefactoring, new ParameterSetter() {

            public void setParameters() {
                renameRefactoring.setNewName("jEdit2");
            }
        });
        long prepare = handler.get("refactoring.prepare");
        long doIt = handler.get("refactoringSession.doRefactoring");
        NbPerformanceTest.PerformanceData d = new NbPerformanceTest.PerformanceData();
        d.name = "refactoring.prepare";
        d.value = prepare;
        d.unit = "ms";
        d.runOrder = 0;
        data.add(d);
        d.name = "refactoringSession.doRefactoring";
        d.value = doIt;
        d.unit = "ms";
        d.runOrder = 0;
        System.err.println("usages collection: " + prepare);
        System.err.println("do refactoring: " + doIt);

      File resGlobal=new File(this.getWorkDirPath()+File.separator+"../../allPerformance.xml");
      FileOutputStream fos=null;
      FileInputStream fis=null;
      if (!resGlobal.exists()) {
          try {

          fos = new FileOutputStream(resGlobal, true);
          fos.write("<TestResults>\n".getBytes());
          fos.write("   </Suite>\n".getBytes());
          fos.write("</TestResults>".getBytes());
          fos.close();

            } catch (IOException ex) {

            }
      }


        try {
            fis= new FileInputStream(resGlobal);
            size=(int)(resGlobal.length()-25);

            byte[] array=new byte[size];
            fis.read(array, 0, size);
            fis.close();

            fos= new FileOutputStream(resGlobal, false);

            fos.write(array);


            if (!new String(array).contains("<Suite suitename=\"J2SE Refactoring\" name=\"org.netbeans.performance.j2se.refactoring.RenameClassPerfTest\">")) {
                if (new String(array).contains("<Suite suitename=")) fos.write("   </Suite>\n".getBytes());
                fos.write(("   <Suite suitename=\"J2SE Refactoring\" name=\"org.netbeans.performance.j2se.refactoring.RenameClassPerfTest\">\n").getBytes());
            }
            
            fos.write(("      <Test name=\"Refactoring Rename: usages collection\" unit=\"ms\" results=\"passed\" threshold=\"0\" classname=\"org.netbeans.performance.j2se.refactoring.RenameClassPerfTest\">\n").getBytes());
            fos.write(("         <PerformanceData runOrder=\"1\" value=\""+prepare+"\"/>\n").getBytes());
            fos.write(("      </Test>\n").getBytes());

            fos.write(("      <Test name=\"Refactoring Rename: do refactoring\" unit=\"ms\" results=\"passed\" threshold=\"0\" classname=\"org.netbeans.performance.j2se.refactoring.RenameClassPerfTest\">\n").getBytes());
            fos.write(("         <PerformanceData runOrder=\"1\" value=\""+doIt+"\"/>\n").getBytes());
            fos.write(("      </Test>\n").getBytes());

            fos.write("   </Suite>\n".getBytes());
            fos.write("</TestResults>".getBytes());
            fos.close();

        } catch (IOException ex) {
            System.err.println("Exception:"+ex);
        }

    }

    @Override
    protected void tearDown() {
    }


    public PerformanceData[] getPerformanceData() {
        return data.toArray(new PerformanceData[0]);
    }

    private static class MyHandler extends Handler {

        private Map<String, Long> map = new HashMap<String, Long>();

        @Override
        public void publish(LogRecord record) {
            Long data;

            for (Object o : record.getParameters()) {
                if (o instanceof Long) {
                    data = (Long) o;
                    map.put(record.getMessage(), data);
                }
            }
        }

        public Long get(String key) {
            return map.get(key);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }

}

interface ParameterSetter {

       void setParameters();

}

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
package org.netbeans.test.web.core.syntax.performance;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.XMLFormatter;
import javax.swing.text.StyledDocument;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.EditorCookie.Observable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

/**
 *
 * @author Jindrich Sedek
 */
public class PerformanceTest extends NbTestCase {

    private static final int TIMEOUT = 10000;
    private TimerHandler timerHandler = new TimerHandler();
    private RequestProcessor.Task waiter;
    private Formatter longFormat = new XMLFormatter();
    private Formatter shortFormat = new SimpleFormatter();

    public PerformanceTest(String name) {
        super(name);
        waiter = RequestProcessor.getDefault().post(Task.EMPTY);
        Logger logger = Logger.getLogger("TIMER");
        logger.addHandler(timerHandler);
        logger.setLevel(Level.FINEST);
    }

    public static Test suite(){
        return NbModuleSuite.allModules(PerformanceTest.class);
    }    

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Thread.sleep(TIMEOUT);
        timerHandler.flush();
        waiter = RequestProcessor.getDefault().post(new Runnable() {

            public void run() {
            }
        }, TIMEOUT);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testOpenHTML() throws Exception {
        StyledDocument doc = prepare("performance.html");
        doc.insertString(0, "<table></table>", null);
        waiter.waitFinished();
        doc.insertString(doc.getEndPosition().getOffset() - 1, "<tab", null);
        waiter.waitFinished();
        for (LogRecord log : timerHandler.logs) {
            String message = log.getMessage();
            if (message.contains("Navigator Initialization")) {
                verify(log, 2000, 4000);
            } else if (message.contains("Parsing (text/html)")){
                verify(log, 500, 1000);
            }else {
                verify(log, 200, 500);
            }
        }
    }

    public void testOpenJSP() throws Exception {
        StyledDocument doc = prepare("performance.jsp");
        doc.insertString(0, "${\"hello\"}", null);
        waiter.waitFinished();
        doc.insertString(doc.getEndPosition().getOffset() - 1, "<%= \"hello\" %>", null);
        waiter.waitFinished();
        for (LogRecord log : timerHandler.logs) {
            if (log.getMessage().contains("Navigator Initialization")) {
                verify(log, 2000, 4000);   
            } else {
                verify(log, 200, 500);
            }
        }
    }

    public void testOpenCSS() throws Exception {
        StyledDocument doc = prepare("performance.css");
        doc.insertString(0, "selector{color:green}", null);
        waiter.waitFinished();
        doc.insertString(doc.getEndPosition().getOffset() - 1, "sx{c:red}", null);
        waiter.waitFinished();
        for (LogRecord log : timerHandler.logs) {
            verify(log, 200, 500);
        }
    }

    private StyledDocument prepare(String fileName) throws Exception {
        File testFile = new File(getDataDir(), fileName);
        FileObject testObject = FileUtil.createData(testFile);
        DataObject dataObj = DataObject.find(testObject);
        EditorCookie.Observable ed = dataObj.getCookie(Observable.class);
        ed.openDocument();
        ed.open();
        waiter.waitFinished();
        return ed.getDocument();
    }

    private void verify(LogRecord log, int expected, int boundary) {
        Object[] params = log.getParameters();
        if (params.length < 2){
            return;
        }
        if (!(params[1] instanceof Number)) {
            return;
        }
        Number nTime = (Number) params[1];
        Integer time = nTime.intValue();
        if (time > expected) {
            log(longFormat.format(log));
        }
        if (time > boundary) {
            fail(shortFormat.format(log) + " Reached:" + nTime);
        }

    }

    private class TimerHandler extends Handler {

        ArrayList<LogRecord> logs = new ArrayList<LogRecord>();

        @Override
        public void publish(LogRecord record) {
            waiter.schedule(TIMEOUT);
            logs.add(record);
        }

        @Override
        public void flush() {
            logs.clear();
        }

        @Override
        public void close() throws SecurityException {
        }
    }
}





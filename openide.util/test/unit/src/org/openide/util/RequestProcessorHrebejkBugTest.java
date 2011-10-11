/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.openide.util;

import org.junit.Test;



public class RequestProcessorHrebejkBugTest {
    
    @Test
    public void testBug() throws Exception {
        RequestProcessor rp = new RequestProcessor("TestProcessor", 3, true); 
        
        R1 r1 = new R1();
        R2 r2 = new R2(r1);
        
        r1.submit(rp);
        RequestProcessor.Task t = rp.post(r2);
        
        t.waitFinished(); 
        
        if (r1.count != 1) {
            throw r1.wrong;
        }
    }
    
    
    private static class R1 implements Runnable {
        private volatile Exception wrong;
        private volatile int count;
        private RequestProcessor.Task task;

        @Override
        public void run() {
            count ++;
            if (wrong == null) {
                wrong = new Exception("First call");
            } else {
                wrong = (Exception) wrong.initCause(new Exception("Next call " + count));
            }
            long until = System.currentTimeMillis() + 1000;
            for (;;) {
                long missing = until - System.currentTimeMillis();
                if (missing <= 0) {
                    break;
                }
                try {
                    Thread.sleep(missing);
                } catch (InterruptedException ex) {
                    // OK, will be interrupted likely
                }
            }
        }
        
        void submit(RequestProcessor rp) {
            task = rp.post(this);
        }
    
        void cancel() {
            task.cancel();
        }
                
        void check() {
            if ( !task.isFinished() ) {
                task.waitFinished();
            }
        }
            
        
    }
    
    
    private static class R2 implements Runnable {
        
        R1 r1;
        
        R2( R1 r1 ) {
            this.r1 = r1;
        }
        
        @Override
        public void run() {
            
            r1.cancel();
            r1.check();
            
        }
        
    }
    
}


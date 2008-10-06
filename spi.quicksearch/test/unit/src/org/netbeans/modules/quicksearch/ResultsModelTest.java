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

package org.netbeans.modules.quicksearch;

import java.util.Collections;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.junit.NbTest;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Dafe Simonek
 */
public class ResultsModelTest extends NbTestCase {
    
    private int changeCounter;
    
    public ResultsModelTest() {
        super("");
    }
    
    public ResultsModelTest(String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTest suite() {
        NbTestSuite suite = new NbTestSuite(ResultsModelTest.class);
        return suite;
    }
    
    /** Tests behavior of model when providers are filling it quickly - changes
     * firing for listeners should be coalesced.
     */
    public void testFireCoalescing () {
        System.out.println("Testing coalescing of change firing of ResultsModel...");
        
        changeCounter = 0;
        
        ResultsModel model = ResultsModel.getInstance();
        
        final CategoryResult cr = new CategoryResult(
                new ProviderModel.Category(null, "Testing Fire Coalescing", null),
                false);
        
        model.setContent(Collections.singletonList(cr));
        
        model.addListDataListener(new ListDataListener() {

            public void intervalAdded(ListDataEvent e) {
                changeCounter++;
            }

            public void intervalRemoved(ListDataEvent e) {
                changeCounter++;
            }

            public void contentsChanged(ListDataEvent e) {
                changeCounter++;
            }
            
            
        });

        // writer thread, imitates provider which fills result model
        RequestProcessor.Task writer = RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                for (int i = 0; i < CategoryResult.MAX_RESULTS; i++) {
                    cr.addItem(new ResultsModel.ItemResult(null, null, this, String.valueOf(i)));
                    try {
                        // wait longer each second iteration
                        int waitTime = i % 2 == 0 ? ResultsModel.COALESCE_TIME * 2 
                                : ResultsModel.COALESCE_TIME / 10;
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ex) {
                        return;
                    }
                }
            }
        });
        
        writer.waitFinished();
        
        assertTrue("Actual fire change count was " + changeCounter + 
                " times, but expected count was in range of 3-5 times", 
                Math.abs(changeCounter - 4) <= 1);
        
    }


}

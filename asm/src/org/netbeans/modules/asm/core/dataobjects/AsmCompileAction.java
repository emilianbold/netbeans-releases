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

package org.netbeans.modules.asm.core.dataobjects;


import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.openide.execution.ExecutionEngine;
import org.openide.execution.ExecutorTask;
import org.openide.execution.NbProcessDescriptor;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;



public final class AsmCompileAction extends CookieAction {

            

    public static class Executor implements Runnable, Node.Cookie {

       

        private String command;

        private String args;

        

        public Executor(String command, String args) {

            this.command = command;

            this.args = args;

        }

                        

        public void run() {           

            try {              

                NbProcessDescriptor desc = new NbProcessDescriptor(command, args);

                Process proc = desc.exec();

                InputStream stream = proc.getErrorStream();   

                

                Thread t1 = new Thread(new OutputPrinter(stream, System.out));

                t1.run();                                 

                

                proc.waitFor();                

                t1.join();

            } catch (IOException ex) {

                ex.printStackTrace();

            }

            catch (InterruptedException ex) {

                ex.printStackTrace();

            }

        } 

                

        static class OutputPrinter implements Runnable {

            private InputStream in;

            private PrintStream out;

                                    

            public OutputPrinter(InputStream in, PrintStream out) {

                this.in = in; 

                this.out = out; 

            }

            

            public void run() {

               try {

                  int lineSep = '\n';

                  int read;

                                    

                  StringBuilder buf = new StringBuilder();

                  

                   while ((read = in.read())  != (-1)) {

                      if (read == lineSep) {                          

                          out.print(buf.toString());

                          out.println();

                          buf.setLength(0); 

                      }

                      else {

                          buf.append((char) read);

                      }                                         

                   }

                   out.println(buf.toString()); 

                   

                } catch (IOException e) {

                

                }               

            }            

        }

    }

    

    protected void performAction(Node[] activatedNodes) {

        DataObject c = (DataObject) activatedNodes[0].getCookie(DataObject.class);   

        

        AsmDataNode node = (AsmDataNode) c.getNodeDelegate();

        Executor run = new Executor(node.getCommand(), node.getArgs());

       

      

        ExecutorTask task = ExecutionEngine.getDefault().execute("Asm Compile", run, null);             

    }

    

    protected int mode() {

        return CookieAction.MODE_EXACTLY_ONE;

    }

    

    public String getName() {

        return NbBundle.getMessage(AsmCompileAction.class, "CTL_AsmCompileAction");

    }

    

    protected Class[] cookieClasses() {

        return new Class[] {

            DataObject.class

        };

    }

    

    protected void initialize() {

        super.initialize();

        // see org.openide.util.actions.SystemAction.iconResource() javadoc for more details

        putValue("noIconInMenu", Boolean.TRUE);

    }

    

    public HelpCtx getHelpCtx() {

        return HelpCtx.DEFAULT_HELP;

    }

    

    protected boolean asynchronous() {

        return false;

    }

    

}




/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.lib.collab.tools;

import org.netbeans.lib.collab.*;
import java.io.*;
import java.util.*;



/**
 *
 * @author Jacques Belissent
 * @author Rahul Shah
 * @author Vijayakumar Palaniappan
 *
 */
class Instance extends Shell
{

    // Constructor takes input a command line interface to the server
    public Instance(BufferedReader br) throws Exception {
	super(br, true);
    }

    // The generated conference names are based on time.
    // there is an (extremely unlikely) event that two
    // instances can generate the same name
    // Should really add creator in the key
    protected String getCanonicalConferenceName(Conference conf) {
	// iIMCLIConference c = (iIMCLIConference) conf;
	// Conference confr = c.getConference();
	return (String)MultiShell.allConfnames.get(conf.getDestination());
    }

    protected void assignLocalName(String name, String confDest) {
	super.assignLocalName(name, confDest);
	MultiShell.allConfnames.put(confDest, name);
    }
}


class CmdLine implements Runnable {
    public PipedWriter pw;
    public BufferedReader br;
    public Shell instance;
    
    CmdLine() {
	pw = new PipedWriter();
	try {
	    br = new BufferedReader(new PipedReader(pw));
	    
	    instance = new Instance(br);

	    Thread t = new Thread(this);
	    t.start();
	} catch (Exception e) {
	    System.out.println("error creating reader");
	    return;
	}
    }
    
    public void run() {
	instance.start();            
    }        
}

public class MultiShell 
{

    // Is a mapping from the locally generated conference name in any
    // instance to a canonical fxed representation of the integer of the instance
    // and the local name used for the conference. 
    static Hashtable allConfnames = new Hashtable();
    
    private static void exitUsage() {
        System.out.println("usage: \n\tjava org.netbeans.lib.collab.tools.MultiShell [-factory <session-factory-class-name>]");
        System.exit(0);
    }

    public static void main(String args[]) 
    {
	// read arguments
        Shell.readArgs(args);

        Hashtable ht = new Hashtable();
	Hashtable conf = new Hashtable();
        BufferedReader br= new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                String line = br.readLine();
                if (line == null) break; //end of file
                if (line.startsWith("#") || line.length() < 1) {
		    //System.out.println(line);
                    continue;
                }
		if (line.equalsIgnoreCase("exit")) System.exit(0);
                int i = line.indexOf(",");
		if (i < 0) {
		    System.out.println("Syntax error in line: " + line);
		    System.out.println("Exiting");
		    break;
		}
                String instanceId = line.substring(0,i);
                String command = line.substring(i + 1);
                CmdLine cl = (CmdLine)ht.get(instanceId);
                if (cl == null) {
                    cl = new CmdLine();
		    cl.instance.setInstanceID(instanceId);
		    cl.instance.setConfName(conf);
                    ht.put(instanceId, cl);                    
                }                
                System.out.println("\n*******start command********");
                System.out.println(instanceId + ">" + command);                  
                cl.pw.write(command);
                cl.pw.write("\n");
                cl.pw.flush();
                Thread.sleep(1000);
                while (!cl.instance.isWaiting()) { 
		    Thread.yield();               
                    if (!cl.instance.isRunning()) break;
                }
                System.out.println("\n*******stop command**********");
            } catch (Exception e) {
                System.out.println("Exception " + e);
                e.printStackTrace();
                break;
            }
            
        }
    }
}


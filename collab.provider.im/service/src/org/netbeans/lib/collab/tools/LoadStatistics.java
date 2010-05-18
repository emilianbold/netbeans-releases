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
import org.netbeans.lib.collab.util.*;
import java.util.*;
import java.io.*;
import java.net.*;

import org.apache.log4j.*;


/**
 *
 * @author Jacques Belissent
 * @author Rahul Shah
 * @author Vijayakumar Palaniappan
 * @author Rebecca Ramer
 *
 */
class LoadResource {
    String name;
    long order = 0, cumul = 0;
    synchronized void adjustOrder(int n) {
	order += n;
	if (n > 0) cumul += n;
    }
    void print(PrintStream out) {
        out.println("STAT res:" + name + " order=" + order + " cumul=" + cumul);
    }
}

class LoadService {
    String name;
    long nTrans, tMax = 0, tMin = 0, tCumul = 0;
    HashSet _transactions = new HashSet();
    synchronized Object startTransaction() {
        LoadTransaction t = new LoadTransaction();
        t.service = this;
        _transactions.add(t);
        return t;
    } 
    synchronized void endTransaction(Object transaction)
    {
	if (transaction instanceof LoadTransaction) {
	    LoadTransaction tx = (LoadTransaction)transaction;
	    nTrans++;
	    long t = tx.endTime - tx.startTime;
	    tCumul += t;
	    if (t > tMax) tMax = t;
	    if (t < tMin || tMin == 0) tMin = t;
	}
        _transactions.remove(transaction);
    }
    void print(PrintStream out) {
	if (nTrans == 0) return;
        out.println("STAT svc:" + name + " order=" + nTrans + " avg.t=" + tCumul/nTrans + " max.t=" + tMax + " min.t=" + tMin);
    }
}

class LoadTransaction {
    LoadService service;
    long startTime = System.currentTimeMillis();
    long endTime;
    void end() { 
	endTime = System.currentTimeMillis();
	service.endTransaction(this); 
    }
}


class LoadStatistics 
{

    private static Hashtable _resources = new Hashtable();
    private static Hashtable _services = new Hashtable();


    static void createResource(String name) {
        LoadResource r = new LoadResource();
        r.name = name;
        _resources.put(name, r);
    }

    static void incrementResourceOrder(String name)
    {
        incrementResourceOrder(name, 1);
    }
    static void decrementResourceOrder(String name)
    {
        incrementResourceOrder(name, -1);
    }

    static void incrementResourceOrder(String name, int order)
    {
        LoadResource r = (LoadResource)_resources.get(name);
        if (r != null) r.adjustOrder(order);        
    }

    static void decrementResourceOrder(String name, int order)
    {
        incrementResourceOrder(name, -order);
    }

    static void createService(String name) 
    {
        LoadService s = new LoadService();
        s.name = name;
        _services.put(name, s);        
    }

    static Object startTransaction(String name)
    {
        LoadService s = (LoadService)_services.get(name);
        if (s != null) {
            return s.startTransaction();
        } else { 
	    return null;
	}
    }

    static void endTransaction(Object transaction) 
    {
        if (transaction instanceof LoadTransaction) {
            ((LoadTransaction)transaction).end();
        }
    }

    static void printService(String name, PrintStream out)
    {
        LoadService s = (LoadService)_services.get(name);
        if (s != null) {
            s.print(out);
        }
    }

    static void printResource(String name, PrintStream out)
    {
        LoadResource r = (LoadResource)_resources.get(name);
        if (r != null) {
            r.print(out);
        }
    }

    static void printServices(PrintStream out)
    {
	for (Iterator i = _services.keySet().iterator();
	     i.hasNext(); ) {
	    printService((String)i.next(), out);
        }
    }

    static void printResources(PrintStream out)
    {
	for (Iterator i = _resources.keySet().iterator();
	     i.hasNext(); ) {
	    printResource((String)i.next(), out);
        }
    }

    static void print(PrintStream out)
    {
	out.println("STAT");
	printResources(out);
	printServices(out);
	out.println("STAT");
    }

    static void startPrintLoop(final PrintStream out, final long period)
    {
	(new Thread(new Runnable() {
	    public void run() {
		for (;;) {
		    print(out);
		    try {
			Thread.sleep(period);
		    } catch (Exception e) {}
		}
	    }
	})).start();
    }

}

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
 * Generate Contact List with random buddies
 *
 * @author Jacques Belissent
 * @author Rahul Shah
 * @author Vijayakumar Palaniappan
 * @author Rebecca Ramer
 *
 */

public class Populate implements CollaborationSessionListener, PersonalStoreServiceListener
{
    
    static int START = 0;
    static int END = 0;
    static int[] MAXUSERS_START = new int[10];
    static int[] MAXUSERS_END = new int[10];
    static int NUMBUDDIES = 0;
    static int NUMDIGITS = 7;
    static String[] MUX = new String[10];
    static int line = 0;
    final static String GROUP_ID = "TestGroup";
    private Random rand;
    CollaborationSessionFactory _factory;
    
    public Populate(){
        Random r1 = new Random(System.currentTimeMillis());
        rand = new Random(r1.nextLong());
        try {
            _factory = new CollaborationSessionFactory();
        } catch(Exception e) {
            e.printStackTrace();
        } 
        for(int n = START; n < END + 1; n++) {
            create(createValidId(Integer.toString(n)));
        }
                
    }
    
    public void create(String user)  {
        System.out.println("Create Contact List for " + user);
        try {
            CollaborationSession _session = _factory.getSession(MUX[0], user, "iplanet", this);
            PersonalStoreService _personalStoreSession = _session.getPersonalStoreService();
            PresenceService _presenceService = _session.getPresenceService();
            _personalStoreSession.initialize(this);

            PersonalStoreFolder f = (PersonalStoreFolder)_personalStoreSession.getEntry(PersonalStoreEntry.CONTACT_FOLDER, GROUP_ID);
            if (f == null) {
                f = (PersonalStoreFolder)_personalStoreSession.createEntry(PersonalStoreEntry.CONTACT_FOLDER, GROUP_ID);
                f.save();
            }
            for(int n = 0; n < NUMBUDDIES; n++){
                String tmp = createRandomUser(_session.getPrincipal().getDomainName());
                PersonalContact entry = (PersonalContact)_personalStoreSession.getEntry(PersonalStoreEntry.CONTACT, tmp);
                if(entry != null) {            
                    n--;
                    continue;
                }
                entry = (PersonalContact)_personalStoreSession.createEntry(PersonalStoreEntry.CONTACT, tmp);
        
                entry.addToFolder(f);
                entry.addAddress(PersonalContact.IM, tmp, 0);
                entry.save();
                _presenceService.subscribe(tmp);
            }
            //if (_personalStoreSession != null) _personalStoreSession.logout();
            if (_session != null) _session.logout();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

    }     
   
    
    //Make sure uid is 7 characters long
    public String createValidId(String id)  {
        int size = id.length();
        if(size == NUMDIGITS) return id;
        
        StringBuffer buf = new StringBuffer();
        for(int n = size; n < NUMDIGITS; n++){
            buf.append("0");
        }
        
        id = buf.toString() + id;
        return id;
    }
    
    
    public String createRandomUser(String domainName){
        int line_no = rand.nextInt();
        if(line_no < 0) line_no = line_no * -1;
        line_no = line_no % line;
        int id = rand.nextInt();
        if(id < 0) id = id * -1;
        
        //Now make sure id is in our UID range here
        id = id % MAXUSERS_END[line_no];
        id = id + MAXUSERS_START[line_no];
        if (line_no == 0) {
            String user = Integer.toString(id) + "@" + domainName;
            return user;
        }
        return Integer.toString(id) + "@" + MUX[line_no];
    }
    
    public void onError(org.netbeans.lib.collab.CollaborationException e) {
        e.printStackTrace();
    }
    
    public static void usage() {
            System.out.println("***********************");
        System.out.println("Populate USAGE: ");
        System.out.println("Contactlist <contract-file>");
        System.out.println("The contarct-file should be like:");
        System.out.println("<start> <end> <mux> <start_user> <num_users> <buddies>");
        System.out.println("<domain> <start_user> <num_users>");
        System.out.println("....  where");
        System.out.println("<start>     Start ID");
        System.out.println("<end>       Last ID");
        System.out.println("<mux>       The multiplexor to connect");
        System.out.println("<start_user>  Start Number of user id's available");
        System.out.println("<num_users>  Number of user id's available from the <start_user>");
        System.out.println("<buddies>   Number of buddies to add to each Users contact list");
        System.out.println("<digits>       Number of digits per user id");
        System.out.println("<domain> The domain name of the other server from which the buddy should be added.");
                
        System.out.println("***********************");
        System.exit(0);
    }
    /**
     * The entry point for this application.
     * Sets the Look and Feel to the System Look and Feel.
     * Creates a new generator and makes it visible.
     */
    static public void main(String args[]) {
            
        try {
            if(args.length < 1) usage();
            String contractFile = args[0];
            // reaf contract file
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(contractFile)));
            for(;;) {
                String sContract = reader.readLine();
                if ((sContract == null) && (line == 0)) usage();
                if (sContract == null) break;
                if (sContract.startsWith("#")) continue;
                if (line == 0) {
                    StringTokenizer st = new StringTokenizer(sContract, " ");
                    if (st.hasMoreElements()) START = Integer.parseInt(st.nextToken());
                    else usage();
                    if (st.hasMoreElements()) END = Integer.parseInt(st.nextToken());
                    else usage();
                    if (st.hasMoreElements()) MUX[line] = st.nextToken();
                    else usage();
                    if (st.hasMoreElements()) MAXUSERS_START[line] = Integer.parseInt(st.nextToken());
                    else usage();
                    if (st.hasMoreElements()) MAXUSERS_END[line] = Integer.parseInt(st.nextToken());
                    else usage();
                    if (st.hasMoreElements()) NUMBUDDIES = Integer.parseInt(st.nextToken());
                    else usage();
                    if (st.hasMoreElements()) NUMDIGITS = Integer.parseInt(st.nextToken());
                    line++;
                }
                else {
                    StringTokenizer st = new StringTokenizer(sContract, " ");
                    if (st.hasMoreElements()) MUX[line] = st.nextToken();
                    else usage();
                    if (st.hasMoreElements()) MAXUSERS_START[line] = Integer.parseInt(st.nextToken());
                    else usage();
                    if (st.hasMoreElements()) MAXUSERS_END[line] = Integer.parseInt(st.nextToken());
                    else usage();
                    line++;
                }
            }
            new Populate();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

public void onEvent(PersonalStoreEvent event) { }

}



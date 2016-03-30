/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package jdk.internal.jshell.remote;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import org.netbeans.lib.jshell.agent.NbJShellAgent;

/**
 *
 * @author sdedic
 */
public class AgentWorker extends RemoteAgent implements Runnable, ClassFileTransformer {
    private final NbJShellAgent   agent;
    /**
     * The control socket
     */
    private final Socket socket;
    private final int   socketPort;
    
    private AgentWorker() {
        agent = null;
        loader = new NbRemoteLoader();
        socket = null;
        socketPort = -1;
    }

    @Override
    protected void prepareClassLoader() {
        super.prepareClassLoader();
    }
    
    public AgentWorker(NbJShellAgent agent, Socket controlSocket) {
        loader = new NbRemoteLoader();
        this.agent = agent;
        this.socket = controlSocket;
        this.socketPort = controlSocket.getLocalPort();
        if (agent.getClassName() != null) {
            if (agent.getMethod() == null && agent.getField() == null) {
                // hook onto class loading
                agent.getInstrumentation().addTransformer(this);
            }
        }
    }
    
    public static void main(String[] args) throws Exception {
        String loopBack = null;
        Socket socket = new Socket(loopBack, Integer.parseInt(args[0]));
        try {
            (new AgentWorker()).commandLoop(new ObjectInputStream(socket.getInputStream()),
                    new ObjectOutputStream(socket.getOutputStream()));
        } catch (EOFException ex) {
            // ignore, forcible close by the tool
        }
    }
    
    @Override
    public void run() {
        loader = new RemoteClassLoader();
        // reset the classloader
        try (
            // will block, but this is necessary so the IDE eventually sets the debuggerKey
            ObjectOutputStream osm = new ObjectOutputStream(socket.getOutputStream());
            // will read immediately
            ObjectInputStream ism = new ObjectInputStream(socket.getInputStream())) {
            commandLoop(ism, osm);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Collect and send information about the executing VM.
     */
    public static final int CMD_VM_INFO   = 100;
    
    /**
     * Find out reference identity of a class.
     */
    public static final int CMD_TYPE_ID   = 101;
    
    private Pattern EXCLUDE_CLASSPATH_ITEMS = Pattern.compile(
              "lib/tools.jar$|"
            + "modules/ext/nb-custom-jshell-probe.jar$"
    );
        

    private void returnVMInfo(ObjectOutputStream o) throws IOException {
        Map<String, String>  result = new HashMap<>();
        Properties props = System.getProperties();
        for (String s : props.stringPropertyNames()) {
            if (!s.startsWith("java")) { // NOI18N
                continue;
            }
            result.put(s, props.getProperty(s));
        }
        
        prepareClassLoader();
        StringBuilder cp = new StringBuilder();
        for (URL u: loader.getURLs()) {
            try {
                File f = new File(u.toURI());
                String s = f.getPath();
                if (EXCLUDE_CLASSPATH_ITEMS.matcher(s).find()) {
                    continue;
                }
                if (cp.length() > 0) {
                    cp.append(":"); // NOI18N
                }
                cp.append(f.getPath());
            } catch (URISyntaxException ex) {
                cp.append(u.toExternalForm());
            }
        }
        for (String s : props.getProperty("java.class.path").split(File.pathSeparator)) {  // NOI18N
            if (s.isEmpty()) {
                continue;
            }
            if (EXCLUDE_CLASSPATH_ITEMS.matcher(s).find()) {
                continue;
            }
            if (cp.length() > 0) {
                cp.append(":"); // NOI18N
            }
            cp.append(s);
        }
        
        result.put("nb.class.path", cp.toString()); // NOI18N
        
        o.writeInt(result.size());
        for (String s : result.keySet()) {
            o.writeUTF(s);
            o.writeUTF(result.get(s));
        }
        o.flush();
    }

    @Override
    protected void handleUnknownCommand(int cmd, ObjectInputStream i, ObjectOutputStream o) throws IOException {
        System.err.flush();
        try {
            switch (cmd) {
                case CMD_VM_INFO:
                    returnVMInfo(o);
                    break;
                default:
                    super.handleUnknownCommand(cmd, i, o);
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            System.err.flush();
        }
    }
    
    

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        return null;
    }
 }

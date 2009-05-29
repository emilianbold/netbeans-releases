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
package org.netbeans.modules.visualweb.errorhandler;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.SwingUtilities;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Annotatable;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.windows.WindowManager;

/*
 * ClientSocketThread.java
 * Created on January 6, 2004, 1:05 PM
 */

/**
 * @author  Winston Prakash
 */
public class ClientSocketConnectionThread extends Thread {
    BufferedReader in = null;
    PrintWriter out = null;

    StringBuffer message = new StringBuffer();

    boolean connected = true;

    Socket clientSocket = null;
    String threadName = null;

    DebugProtocol debugProtocol = new DebugProtocol();

    public ClientSocketConnectionThread(ThreadGroup threadGroup, String threadName, Socket clientSocket) {
        super(threadGroup, threadName);
        this.threadName = threadName;
        this.clientSocket = clientSocket;
    }

    public void run() {
        String line = null;
        try{
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException exc) {
            displayText("Could not get client socket I/O : " + exc.getLocalizedMessage());
        }

        while(connected){
            try{
                line = in.readLine();
                if(line != null){
                    processText(line,true);
                    message.append(line + "\n");
                } else{
                    //displayText("Client connection closed - " + threadName);
                    connected = false;
                    disconnect();
                }
            } catch (IOException exc) {
                displayText(exc.getLocalizedMessage() + " - " + threadName);
                connected = false;
                disconnect();
            }
        }
    }

    public void disconnect(){
        if(connected){
            try{
                out.close();
                in.close();
                clientSocket.close();
            }catch (IOException exc) {
                //displayText("Error occured while disconnecting socket" + exc.getLocalizedMessage());
            }
            out = null;
            in = null;
            clientSocket = null;
            connected = false;
        }
    }

    public void sendMessage(String text){
        if(out != null){
            out.println(text);
        }
    }

    public String getMessage(){
        return message.toString();
    }

    private void displayText(String line){
        processText(line, false);
    }

    private void processText(String line, boolean process){
        // Procees the Text

        if(process){
            final String processedOutput = debugProtocol.processInput(line);

            //DebugServerTestWindow.displayMessage(processedOutput);
            //System.out.println(processedOutput);

            // Disconnect if the client is not recognized
            if (debugProtocol.getState() == DebugProtocol.STATE_CLIENT_UNRCOGNIZED){
                //System.out.println(" disconnecting ..\n");
                disconnect();
                debugProtocol.setState(DebugProtocol.STATE_WAITING);
            }
            
            if (debugProtocol.getState() == DebugProtocol.STATE_DONE){
                disconnect();
                showErrorLine(debugProtocol.getErrorInfo());
                debugProtocol.setState(DebugProtocol.STATE_WAITING);
                //DebugServerTestWindow.displayMessage("\n");
                //System.out.println("\n");
            }
            
        }else{
            //DebugServerTestWindow.displayMessage(line);
            //System.out.println(line);
        }
    }

    private void showErrorLine(DebugProtocol.ErrorInfo errorInfo){
        String filePath = errorInfo.getFilePath();
        /*** filePath is something like "WebApplication1/Page2.java" (package/Page).
         *  also can get faces stuff, like com/sun/faces/el/MethodBindingImpl.java
         */
        int lineNumber  = errorInfo.getLineNumber();

        Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
        Project currentProject = null;
        String resourcePath = "src/" + filePath;
        FileObject srcFile = null;

        for (int i = 0; srcFile == null && openProjects != null && i < openProjects.length; i++) {
            Project nextProject = openProjects[i];
            try {
                FileObject errorFile = nextProject.getProjectDirectory().getFileObject(resourcePath);
                if (errorFile != null) {
                    srcFile = errorFile;
                    currentProject = nextProject;
                }
            }catch (Exception ex) {
            }
        }
        
        if ( currentProject == null ) { 
            return ;
        }
        
        try {
            DataObject dob = DataObject.find(srcFile);
            new ErrorLineMarker().markError(dob, lineNumber);
        }catch (DataObjectNotFoundException ex) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ex);
        }

    }
    
    private static final class ErrorLineMarker extends Annotation implements PropertyChangeListener{
        
        public void markError(DataObject dob, int lineNo){
            try {
                EditorCookie ed = (EditorCookie) dob.getCookie(EditorCookie.class);
                if (ed != null) {
                    //displayText("Got the editor cookie.");
                    if (lineNo == -1) {
                        // OK, just open it.
                        ed.open();
                    } else {
                        ed.openDocument(); // XXX getLineSet does not do it for you
                        final Line line = ed.getLineSet().getOriginal(lineNo-1);
                        if (! line.isDeleted()) {
                            SwingUtilities.invokeLater(new Runnable(){
                                public void run(){
                                    attachAsNeeded(line);
                                    line.show(ShowOpenType.OPEN, ShowVisibilityType.FOCUS);
                                    WindowManager.getDefault().getMainWindow().toFront();
                                }
                            });
                        }
                    }
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            } catch (DataObjectNotFoundException donfe) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, donfe);
            } catch (IndexOutOfBoundsException iobe) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, iobe);
            } catch (IOException ioe) {
                // XXX see above, should not be necessary to call openDocument at all
                ErrorManager.getDefault().notify(ErrorManager.WARNING, ioe);
            }
        }
        
        private synchronized void attachAsNeeded(Line line) {
            if (getAttachedAnnotatable() == null) {
                // Attach the whole line
                Annotatable ann = line;
                // System.out.println("Attaching to line " + line.getDisplayName() + " text=`" + line.getText() );
                attach(ann);
                ann.addPropertyChangeListener(this);
            }
        }
        
        private synchronized void doDetach() {
            Annotatable ann = getAttachedAnnotatable();
            if (ann != null) {
                ann.removePropertyChangeListener(this);
                detach();
            }
        }
    
        public void propertyChange(PropertyChangeEvent ev) {
            String prop = ev.getPropertyName();
            if (prop == null ||  prop.equals(Annotatable.PROP_TEXT) ||
                prop.equals(Annotatable.PROP_DELETED)) {
                // Affected line has changed.
                // Assume user has edited & corrected the error.
                doDetach();
            }
        }
        // Annotation:
        public String getAnnotationType() {
            return "com-sun-rave-errorhandler-error"; // NOI18N
        }
        public String getShortDescription() {
            return "Error Handler Marker"; // NOI18N
        }
    }
}

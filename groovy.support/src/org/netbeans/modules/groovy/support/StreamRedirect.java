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

package org.netbeans.modules.groovy.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import org.netbeans.modules.groovy.support.api.GroovyErrorOutputSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/** 
 * This class is a thread used for listening a redirecting
 * standard output/error streams from external application
 * to NetBeans InputOutput. 
 *
 * @author Petr Hamernik
 */
class StreamRedirect extends Thread {
    
    /** Input stream where to read */
    private InputStream is;
     
    /** OutputWriter of NetBeans' InputOutput - where to write
     */
    private OutputWriter ow;
    
    private FileObject fileObject;
    
    /** Create new stream redirector 
     * 
     * @param dataObject DataObject which is executed 
     * @param is Input stream where to read
     * @param ow OutputWriter of NetBeans' InputOutput - where to write
     */
    StreamRedirect(FileObject fileObject, InputStream is, OutputWriter ow) {
        this.fileObject = fileObject;
        this.is = is;
        this.ow = ow;
    }
    
    /** Starts this thread.
     */
    @Override
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null) {
                
                OutputListener ol = checkErrorLine( line );
                if ( ol != null ) {
                    ow.println( line, ol );
                }
                else {
                    ow.println(line);
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        ow.close();
    }

    public OutputListener checkErrorLine(String line) {
        GroovyErrorOutputSupport.HyperlinkData data = GroovyErrorOutputSupport.checkErrorLine(line, fileObject);
        if ( data != null ) {
            try {
                return new ScriptHyperlink(data.file.toURI().toURL(), data.message, data.line1, data.column1, data.line2, data.column2);
            } catch (MalformedURLException e) {
                assert false : e;
            }
        }
        return null;
    }
    
}
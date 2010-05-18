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
package org.netbeans.modules.mobility.end2end.output;

import java.io.IOException;
import org.netbeans.modules.mobility.end2end.codegenerator.ConnectionGenerator;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;


/**
 * @author ads
 *
 */
public class OutputLogger {

    private static final String WARNING = "TXT_Warning";   //NOI18N
    private static final String JAVON_TAB = "LBL_JavonTab";//NOI18N
    
    private OutputLogger(){
        myOutput = IOProvider.getDefault().getIO(
                NbBundle.getMessage( ConnectionGenerator.class, JAVON_TAB ),true );
        try {
            myOutput.getIn().close();
            //open();
        } catch (IOException ex) {
            ErrorManager.getDefault().notify( ex );
        }
        //open();
    }
    
    public static enum LogLevel {
        NOTICE,
        WARNING,
        ERROR
    }
    
    public void open(){
        myOutput.select();
    }
    
    public static OutputLogger getInstance(){
        return INSTANCE;
    }
    
    public void log( LogLevel level , String message , boolean appendNewLine){
        if ( appendNewLine ){
            getOutputWriter(level, false ).println(message);
        }
        else {
            getOutputWriter(level, false ).print(message);
        }
    }

    public void log( Exception e ){
        e.printStackTrace( getOutput().getErr());
    }
    
    public void logAppend( LogLevel level , String message , boolean appendNewLine){
        if ( appendNewLine ){
            getOutputWriter(level, true ).println(message);
        }
        else {
            getOutputWriter(level, true ).print(message);
        }
    }
    
    public void log( LogLevel level , String message ){
        log( level, message , true );
    }
    
    public void log( String message ){
        log( LogLevel.NOTICE, message , true );
    }

    private InputOutput getOutput(){
        return myOutput;
    }
    
    private OutputWriter getOutputWriter( LogLevel level, boolean append ){
        if ( level == LogLevel.ERROR ){
            return getOutput().getErr();
        }
        else if ( level == LogLevel.WARNING ){
            OutputWriter writer = getOutput().getOut();
            if ( !append ){
                String warning = NbBundle.getMessage( OutputLogger.class , WARNING );
                writer.append(warning);
                writer.append(" ");
            }
            return writer;
        }
        else {
            return getOutput().getOut();
        }
    }

    public void close(){
        //getOutput().closeInputOutput();
        getOutput().getOut().close();
        getOutput().getErr().close();
    }


    private InputOutput myOutput;
    private static final OutputLogger INSTANCE = new OutputLogger();
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.execution;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.execution.impl.ErrorAnnotation;
import org.netbeans.modules.cnd.execution.impl.OutputListenerImpl;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

@Deprecated
public class OutputWindowWriter extends Writer {

    private final OutputWriter delegate;
    private final StringBuffer buffer;
    private final boolean parseOutputForErrors;
    private CompilerLineConvertor convertor;
//    private FileObject relativeTo;
    
    private static final String LINE_SEPARATOR_QUOTED = System.getProperty("line.separator");  // NOI18N
    
    public OutputWindowWriter(Project project, ExecutionEnvironment execEnv, OutputWriter delegate, FileObject relativeTo, boolean parseOutputForErrors) {
	if (parseOutputForErrors) {
	    convertor = new CompilerLineConvertor(project, execEnv, relativeTo);
	}
        this.delegate = delegate;
//        this.relativeTo = relativeTo;
        this.parseOutputForErrors = parseOutputForErrors;
        this.buffer = new StringBuffer();
        
        ErrorAnnotation.getInstance().detach(null);
    }
    
    public void write(char[] cbuf, int off, int len) throws IOException {
        buffer.append(new String(cbuf, off, len).replaceAll(LINE_SEPARATOR_QUOTED, "\n")); // NOI18N
        
        int eolIndex;
        
        while ((eolIndex = buffer.indexOf("\n")) != (-1)) {  // NOI18N
            handleLine(buffer.substring(0, eolIndex));
            buffer.delete(0, eolIndex + "\n".length() + 1);  // NOI18N
        }
    }
    
    public void flush() throws IOException {
        //ignored.
    }
    
    public void close() throws IOException {
        delegate.close();
    }

    private void handleLine(String line) throws IOException {
	if (parseOutputForErrors) {
	    List<ConvertedLine> lines = convertor.convert(line);
	    if (lines != null) {
		for(ConvertedLine cl : lines) {
		    String t = cl.getText();
		    OutputListener l = cl.getListener();
		    if (l == null) {
			delegate.println(t);
		    } else {
			if (l instanceof OutputListenerImpl) {
			    if (((OutputListenerImpl)l).isError()){
				delegate.println(t, l, true);
			    } else {
				delegate.println(t, l, false);
			    }
			} else {
			    delegate.println(t, l);
			}
		    }
		}
	    } else {
		delegate.println(line);
	    }
	} else {
	    delegate.println(line);
	}
    }

}

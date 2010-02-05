/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.spi.toolchain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.windows.OutputListener;

/**
 *
 * @author Alexander Simon
 */
public abstract class ErrorParserProvider {
    public static final Result NO_RESULT = new NoResult();
    public static final Result REMOVE_LINE = new RemoveLine();

    private static final ErrorParserProvider DEFAULT = new DefaultErrorParserProvider();
    
    public static ErrorParserProvider getDefault() {
	return DEFAULT;
    }

    public abstract ErrorParser getErorParser(CompilerFlavor flavor, ExecutionEnvironment execEnv, FileObject relativeTo);
    public abstract String getID();

    public interface ErrorParser {
	Result handleLine(String line) throws IOException;
    }
    public interface Result {
        public abstract boolean result();
        public abstract List<ConvertedLine> converted();
    }

    public static final class Results implements Result {
        private List<ConvertedLine> result = new ArrayList<ConvertedLine>(1);
        public Results(){
        }
        public Results(String line, OutputListener listener){
            result.add(ConvertedLine.forText(line, listener));
        }
        public void add(String line, OutputListener listener) {
            result.add(ConvertedLine.forText(line, listener));
        }
        @Override
        public boolean result() {
            return true;
        }
        @Override
        public List<ConvertedLine> converted() {
            return result;
        }
    }

    private static final class NoResult implements Result {
        @Override
        public boolean result() {
            return false;
        }
        @Override
        public List<ConvertedLine> converted() {
            return Collections.<ConvertedLine>emptyList();
        }
    }

    private static final class RemoveLine implements Result {
        @Override
        public boolean result() {
            return true;
        }
        @Override
        public List<ConvertedLine> converted() {
            return Collections.<ConvertedLine>emptyList();
        }
    }

    private static final class DefaultErrorParserProvider extends ErrorParserProvider {
        private final Lookup.Result<ErrorParserProvider> res;
        DefaultErrorParserProvider() {
            res = Lookup.getDefault().lookupResult(ErrorParserProvider.class);
        }

        private ErrorParserProvider getService(String id){
	    for (ErrorParserProvider service : res.allInstances()) {
		if (service.getID().equals(id)) {
		    return service;
		}
	    }
            return null;
        }

	@Override
	public ErrorParser getErorParser(CompilerFlavor flavor, ExecutionEnvironment execEnv, FileObject relativeTo) {
	    ErrorParserProvider provider = getService(flavor.getToolchainDescriptor().getScanner().getID());
	    if (provider != null) {
		return provider.getErorParser(flavor, execEnv, relativeTo);
	    }
	    return null;
	}

	@Override
	public String getID() {
	    throw new UnsupportedOperationException();
	}
    }
}

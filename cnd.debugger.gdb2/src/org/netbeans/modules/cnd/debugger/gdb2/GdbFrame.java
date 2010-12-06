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

package org.netbeans.modules.cnd.debugger.gdb2;

import org.netbeans.modules.cnd.debugger.gdb2.mi.MIResult;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIValue;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MITList;
import org.netbeans.modules.cnd.debugger.common2.debugger.Frame;

public final class GdbFrame extends Frame {
  
    private MITList MIframe;
    private MITList args_list = null;
    private String fullname = null;

    public GdbFrame(GdbDebuggerImpl debugger, MIValue frame, MIResult frameargs) {
	super(debugger);
	if (frame == null)
	    return;

	MIframe = frame.asTuple();
	if (MIframe == null)
	    return;

	MIValue framenov = MIframe.valueOf("level"); // NOI18N
	if (framenov != null)
	    frameno = framenov.asConst().value();
	else
	    frameno = "";
	
	MIValue pcv = MIframe.valueOf("addr"); // NOI18N
	if (pcv != null)
	    pc = pcv.asConst().value();
	else
	    pc = "";
	
	MIValue funcv = MIframe.valueOf("func"); // NOI18N
	if (funcv != null)
	    func = funcv.asConst().value();
	else
	    func = "";
	
        args_list = (MITList) MIframe.valueOf("args"); // NOI18N
	if (args_list != null && frameargs != null)
	    System.out.println("GdbFrame Impossible "); // NOI18N

	MIValue linenov = MIframe.valueOf("line"); // NOI18N
	if (linenov != null)
	    lineno = linenov.asConst().value();
	else
	    lineno = "";
	
	MIValue sourcev = MIframe.valueOf("file"); // NOI18N
	if (sourcev != null)
	    source = sourcev.asConst().value();
	else {
	    MIValue fromv = MIframe.valueOf("from"); // NOI18N
	    if (fromv != null)
	        source = fromv.asConst().value();
	    else
	        source = "";
	}
        
        MIValue fn = MIframe.valueOf("fullname"); // NOI18N
        if (fn != null) {
            fullname = fn.asConst().value();
        }

	// handle args info
	if (frameargs != null) 
            args_list = (MITList) frameargs.value().asTuple().valueOf("args"); // NOI18N

	if (args_list != null) {
            String stringframes = args_list.toString();

	args = " ("; // NOI18N
	if (debugger.getVerboseStack()) {
        int args_count = args_list.size();
        // iterate through args list
        for (int vx=0; vx < args_count; vx++) {
            MIValue arg = (MIValue)args_list.get(vx);
            if (vx != 0)
                args += ", "; // NOI18N
            args += arg.asTuple().valueOf("name").asConst().value(); // NOI18N
            MIValue value = arg.asTuple().valueOf("value"); // NOI18N
            if (value != null) {
                args += "="; // NOI18N
                args += value.asConst().value();
            }
        }
        }
        args += ")"; // NOI18N
	}

	range_of_hidden = false;
	current = false;
	optimized = false;
	attr_user_call = false;
	attr_sig = 0;
	attr_signame = "";
    }

    public MITList getMIframe() {
	return MIframe;
    }

    public void setMIArgs(MITList arg) {
	args_list = arg;
    }

    public MITList getMIArgs() {
	return args_list;
    }
    
    @Override
    public String getFullPath() {
        return debugger.remoteToLocal("Gdb frame", debugger.fmap().engineToWorld(fullname)); //NOI18N
    }

}

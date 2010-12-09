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

import java.util.logging.Logger;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

import org.netbeans.modules.cnd.debugger.common2.values.Action;
import org.netbeans.modules.cnd.debugger.common2.values.FunctionSubEvent;

import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.Handler;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.HandlerCommand;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.HandlerExpert;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.NativeBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.NativeBreakpointType;

import org.netbeans.modules.cnd.debugger.common2.debugger.Address;

import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.FunctionBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.InstructionBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.LineBreakpoint;

import org.netbeans.modules.cnd.debugger.gdb2.mi.MIResult;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MITList;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIValue;

public class GdbHandlerExpert implements HandlerExpert {
    private static final boolean sendShortPaths = Boolean.getBoolean("gdb.breakpoints.shortpaths"); // NOI18N

    // "infinity" for gdb is largest signed 32bit number:
    static final Integer infinity = new Integer(0x7fffffff);

    private final GdbDebuggerImpl debugger;

    private static final Logger LOG = Logger.getLogger(GdbHandlerExpert.class.toString());

    public GdbHandlerExpert(GdbDebuggerImpl debugger) {
        this.debugger = debugger;
    }

    Handler newHandler(NativeBreakpoint template,
		       MIResult result,
		       NativeBreakpoint breakpoint) {

	assert result.variable().equals("bkpt");
	MIValue bkptValue = result.value();
	MITList props = bkptValue.asTuple();

	if (breakpoint == null) {
	    breakpoint = createBreakpoint(props, template);
	} else {
	    assert ! breakpoint.hasHandler();
	}
	update(template, breakpoint, props);
	Handler handler = new Handler(debugger, breakpoint);
	setGenericProperties(handler, props);
	return handler;
    }

    Handler replaceHandler(NativeBreakpoint template,
                           Handler originalHandler, MIResult result) {

	assert result.variable().equals("bkpt");
	MIValue bkptValue = result.value();
	MITList props = bkptValue.asTuple();

	NativeBreakpoint breakpoint = originalHandler.breakpoint();
	update(template, breakpoint, props);
	Handler handler = new Handler(debugger, breakpoint);
	setGenericProperties(handler, props);
	return handler;
    }

    public ReplacementPolicy replacementPolicy() {
	return ReplacementPolicy.EXPLICIT;
    }

    // interface HandlerExpert
    public Handler childHandler(NativeBreakpoint bpt) {
	NativeBreakpoint breakpoint;
	if (bpt.isToplevel()) {
	    breakpoint = bpt.makeSubBreakpointCopy();
	} else {
	    breakpoint = bpt;
	}
	Handler handler = new Handler(debugger, breakpoint);
	return handler;
    }


    // interface HandlerExpert
    public HandlerCommand commandFormNew(NativeBreakpoint breakpoint) {

	//
	// First, weed out options gdb doesn't support
        // but do not fail, see IZ 191537
        //
	if (breakpoint.getAction() != Action.STOP) {
            LOG.warning(Catalog.get("MSG_OnlyStopGdb")); // NOI18N
//	    return HandlerCommand.makeError(Catalog.get("MSG_OnlyStopGdb")); // NOI18N
        }

	if (!IpeUtils.isEmpty(breakpoint.getWhileIn())) {
            LOG.warning(Catalog.get("MSG_NoWhileGdb")); // NOI18N
//	    return HandlerCommand.makeError(Catalog.get("MSG_NoWhileGdb")); // NOI18N
        }

	if (!IpeUtils.isEmpty(breakpoint.getLwp())) {
            LOG.warning(Catalog.get("MSG_NoLwpGdb")); // NOI18N
//	    return HandlerCommand.makeError(Catalog.get("MSG_NoLwpGdb")); // NOI18N
        }


	//
	// Now we should breeze through
	//
	String cmd = "-break-insert";				// NOI18N

	if (breakpoint.getTemp())
	    cmd += " -t";					// NOI18N

	if (breakpoint.getCondition() != null)
	    cmd += " -c " + quote(breakpoint.getCondition());	// NOI18N

	if (breakpoint.hasCountLimit()) {
	    if (breakpoint.getCountLimit() == -1) {
		cmd += " -i " + infinity;			// NOI18N
	    } else {
		Long limit = new Long(breakpoint.getCountLimit() - 1);
		cmd += " -i " + limit;	// NOI18N
	    }
	}

	// -p doesn't seem to be documented
	if (breakpoint.getThread() != null)
	    cmd += " -p " + breakpoint.getThread();		// NOI18N

	// Only gdb 6.8 and newer understand -d
	/* LATER
	if (! breakpoint.isEnabled())
	    cmd += " -d";					// NOI18N
	*/


	Class<?> bClass = breakpoint.getClass(); // dynamic type

	if (bClass == LineBreakpoint.class) {
	    LineBreakpoint lb = (LineBreakpoint) breakpoint;

	    String file = lb.getFileName();
	    int line = lb.getLineNumber();

	    file = debugger.localToRemote("LineBreakpoint", file); // NOI18N
            
            // unify separators
            file = file.replace("\\","/"); //NOI18N

	    // gdb break command seems to not like full pathnames.
            if (sendShortPaths) {
                int pos = file.lastIndexOf('/');
		if (pos >= 0) {
		    file = file.substring(pos + 1);
		}
            }

	    String fileLine = null;
	    if (file != null && file.length() > 0) {
		fileLine = file + ":" + line;	// NOI18N
	    } else {
		fileLine = "" + line;		// NOI18N
	    }

	    cmd += " \"" + fileLine + "\""; // NOI18N

	} else if (bClass == InstructionBreakpoint.class) {
	    InstructionBreakpoint ib = (InstructionBreakpoint) breakpoint;
	    cmd += " *" + ib.getAddress(); // NOI18N

	} else if (bClass == FunctionBreakpoint.class) {
	    FunctionBreakpoint fb = (FunctionBreakpoint) breakpoint;
	    FunctionSubEvent se = fb.getSubEvent();

	    String function = null;
	    if (se.equals(FunctionSubEvent.IN)) {
		function = fb.getFunction();
	    } else if (se.equals(FunctionSubEvent.INFUNCTION)) {
		// Not supported
		return HandlerCommand.makeError(null);
	    } else if (se.equals(FunctionSubEvent.RETURNS)) {
		// Not supported
		return HandlerCommand.makeError(null);
	    }

	    // MI -break-insert doesn't like like spaces in function names.
	    // Surrounding teh whole function signature with quotes seems
	    // to help.
	    cmd += " \"" + function + "\""; // NOI18N

	} else {
	    return HandlerCommand.makeError(null);
	}


	return HandlerCommand.makeCommand(cmd);
    }

    // interface HandlerExpert
    public HandlerCommand commandFormCustomize(NativeBreakpoint clonedBreakpoint, 
		                       NativeBreakpoint repairedBreakpoint) {

	return commandFormNew(clonedBreakpoint);
    }

    private static NativeBreakpoint createBreakpoint(MITList results,
			   NativeBreakpoint template) {
	NativeBreakpointType type = null;

	type = (NativeBreakpointType) template.getBreakpointType();

	NativeBreakpoint newBreakpoint = null;
	if (type != null)
	    newBreakpoint = type.newInstance(NativeBreakpoint.SUBBREAKPOINT);

	return newBreakpoint;
    }

    private void update(NativeBreakpoint template,
			NativeBreakpoint breakpoint,
			MITList props) {
	breakpoint.removeAnnotations();
	setGenericProperties(breakpoint, props);
	setSpecificProperties(template, breakpoint, props);
    }

    private void setGenericProperties(Handler handler, MITList props) {
	// enabled
	MIValue enabledValue = props.valueOf("enabled");	// NOI18N
	String enabledString = enabledValue.asConst().value();

	if (IpeUtils.sameString(enabledString, "y"))		// NOI18N
	    handler.setEnabled(true);
	else if (IpeUtils.sameString(enabledString, "n"))	// NOI18N
	    handler.setEnabled(false);
	else
	    handler.setEnabled(false);

	// 'number'
	MIValue numberValue = props.valueOf("number");		// NOI18N
	String numberString = numberValue.asConst().value();
	int number = Integer.parseInt(numberString);
	handler.setId(number);
    }

    private void setGenericProperties(NativeBreakpoint breakpoint,
				      MITList props) {

	// temporary
	MIValue dispValue = props.valueOf("disp");		// NOI18N
	String dispString = dispValue.asConst().value();
	if ("keep".equals(dispString))				// NOI18N
	    breakpoint.setTemp(false);
	else if ("del".equals(dispString))			// NOI18N
	    breakpoint.setTemp(true);
	else
	    breakpoint.setTemp(false);

	// count
	MIValue ignoreValue = props.valueOf("ignore");		// NOI18N
	if (ignoreValue != null) {
	    String ignoreString = ignoreValue.asConst().value();
	    long ignore = Long.parseLong(ignoreString);
	    // our bpt view converts -1 to a literal "infinity".
	    if (ignore == infinity)
		ignore = -1;
	    else
		ignore++;
	    breakpoint.setCountLimit(ignore, true);
	} else {
	    breakpoint.setCountLimit(0, false);
	}

	// thread
	MIValue threadValue = props.valueOf("thread");		// NOI18N
	if (threadValue != null) {
	    String threadString = threadValue.asConst().value();
	    breakpoint.setThread(threadString);
	}

	// condition
	MIValue condValue = props.valueOf("cond");		// NOI18N
	if (condValue != null) {
	    String condString = condValue.asConst().value();
	    breakpoint.setCondition(condString);
	}

	// action
	Action action = Action.STOP;
	breakpoint.setAction(action);
    }

    private String getFileName(MITList props,
			       NativeBreakpoint originalBreakpoint) {
	String filename = null;
	// 'fullname' (try it first but it's not always available)
	MIValue fullnameValue = props.valueOf("fullname"); // NOI18N
	if (fullnameValue != null) {
	    String fullnameString = fullnameValue.asConst().value();

	    fullnameString = debugger.remoteToLocal("getFileName", fullnameString); // NOI18N

            // convert to world
            fullnameString = debugger.fmap().engineToWorld(fullnameString);
	    filename = fullnameString;
	} else {
	    // 'file'
	    MIValue fileValue = props.valueOf("file"); // NOI18N
	    if (fileValue == null)
		return null;
	    String fileString = fileValue.asConst().value();

	    // 'file' property is just a basename and rather useless ...
	    // Extract original full filename from command:
	    if (originalBreakpoint instanceof LineBreakpoint) {
		LineBreakpoint olb = (LineBreakpoint) originalBreakpoint;
		filename = olb.getFileName();
	    }
	}
	return filename;
    }

    private static int getLine(MITList props) {
	MIValue lineValue = props.valueOf("line"); // NOI18N
	if (lineValue == null)
	    return 0;
	String lineString = lineValue.asConst().value();
	int line = Integer.parseInt(lineString);
	return line;
    }

    private static long getAddr(MITList props) {
	MIValue addrValue = props.valueOf("addr"); // NOI18N
	if (addrValue == null)
	    return 0;
	String addrString = addrValue.asConst().value();
	long addr = Address.parseAddr(addrString);
	return addr;
    }

    private void setSpecificProperties(NativeBreakpoint template,
				       NativeBreakpoint breakpoint,
				       MITList props) {


	if (template instanceof LineBreakpoint) {
	    LineBreakpoint lb = (LineBreakpoint) breakpoint;

	    String filename = getFileName(props, template);
	    int line = getLine(props);

	    lb.setFileAndLine(filename, line);

	} else if (template instanceof FunctionBreakpoint) {
	    FunctionBreakpoint fb = (FunctionBreakpoint) breakpoint;

	    MIValue funcValue = props.valueOf("func"); // NOI18N
	    String funcString;
	    if (funcValue != null) {
		funcString = funcValue.asConst().value();
	    } else {
		// We'll get an 'at' instead of a 'func' if there's 
		// no src debugging information at the given function.
		MIValue atValue = props.valueOf("at"); // NOI18N
		if (atValue != null) {
		    funcString = atValue.asConst().value();

		    // usually of the form 
		    // "<strdup+4>"
		    // (but sometimes of the form "strdup@plt")

		    // clean out <
		    if (funcString.startsWith("<")) // NOI18N
			funcString = funcString.substring(1);

		    // clean out >
		    int gtx = funcString.indexOf('>');
		    if (gtx != -1)
			funcString = funcString.substring(0, gtx);

		    // clean out +4
		    int plx = funcString.indexOf('+');
		    if (plx != -1)
			funcString = funcString.substring(0, plx);

		} else {
		    funcString = "";
		}
	    }

	    fb.setFunction(funcString);

	} else if (template instanceof InstructionBreakpoint) {
	    InstructionBreakpoint ib = (InstructionBreakpoint) breakpoint;

	    long addr = getAddr(props);

	    ib.setAddress(Address.toHexString0x(addr, true));
	}
    }

    void addAnnotations(Handler handler,
			       NativeBreakpoint breakpoint,
			       NativeBreakpoint template,
			       MIResult result) {

	assert result.variable().equals("bkpt");
	MIValue bkptValue = result.value();
	MITList props = bkptValue.asTuple();

	int line = getLine(props);
	String fileName = getFileName(props, template);
	long addr = getAddr(props);
	// TMP if (line != 0 && fileName != null)
	{
	    if (fileName == null) { //|| !fileName.startsWith("/"))
		line = 0;
            }
	    handler.breakpoint().addAnnotation(fileName, line, addr);
	}
    }

    /**
     * Put quotes around a string and escape internal quotes.
     * <p>
     * Converts a string of the form
     *		strcmp(x, "hello")
     * to
     *		"strcmp(x, \"hello\")"
     */
    private static String quote(String in) {
	StringBuilder out = new StringBuilder();
	out.append('"');
	for (int sx = 0; sx < in.length(); sx++) {
	    char c = in.charAt(sx);
	    if (c == '"')
		out.append('\\');
	    out.append(c);
	}
	out.append('"');
	return out.toString();
    }

}

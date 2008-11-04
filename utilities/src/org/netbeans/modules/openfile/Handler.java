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

package org.netbeans.modules.openfile;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * Processor for command line options.
 * @author Jesse Glick, Jaroslav Tulach
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.sendopts.OptionProcessor.class)
public class Handler extends OptionProcessor {
    private Option open;
    private Option defaultOpen;

    public Handler() {
    }

    protected Set<Option> getOptions() {
        if (open == null) {
            defaultOpen = Option.defaultArguments();
            Option o = Option.additionalArguments(Option.NO_SHORT_NAME, "open"); // NOI18N
            String bundle = "org.netbeans.modules.openfile.Bundle"; // NOI18N
            o = Option.shortDescription(o, bundle, "MSG_OpenOptionDescription"); // NOI18N
            o = Option.displayName(o, bundle, "MSG_OpenOptionDisplayName"); // NOI18N            
            open = o;
            
            assert open != null;
            assert defaultOpen != null;
        }
        
        HashSet<Option> set = new HashSet<Option>();
        set.add(open);
        set.add(defaultOpen);
        
        return set;
    }

    protected void process(Env env, Map<Option, String[]> optionValues) throws CommandException {
        String[] argv = optionValues.get(open);
        if (argv == null) {
            argv = optionValues.get(defaultOpen);
        }
        if (argv == null || argv.length == 0) {
            throw new CommandException(2, NbBundle.getMessage(Handler.class, "EXC_MissingArgOpen")); 
        }
        
        File curDir = env.getCurrentDirectory ();

        StringBuffer failures = new StringBuffer();
        String sep = "";
        for (int i = 0; i < argv.length; i++) {
            String error = openFile (curDir, env, argv[i]);
            if (error != null) {
                failures.append(sep);
                failures.append(error);
                sep = "\n";
            }
        }
        if (failures.length() > 0) {
            DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(failures.toString()));
            throw new CommandException(1, failures.toString());
        }
    }

    private File findFile (File curDir, String name) {
        File f = new File(name);
        if (!f.isAbsolute()) {
            f = new File(curDir, name);
        }
        return f;
    }
    
    private String openFile (File curDir, Env args, String s) {
        int line = -1;
        File f = findFile (curDir, s);
        if (!f.exists()) {
            // Check if it is file:line syntax.
            int idx = s.lastIndexOf(':'); // NOI18N
            if (idx != -1) {
                try {
                    line = Integer.parseInt(s.substring(idx + 1)) - 1;
                    f = findFile (curDir, s.substring(0, idx));
                } catch (NumberFormatException e) {
                    // OK, leave as a filename
                }
            }
        }
        // Just make sure it was opened, then exit.
        return OpenFile.openFile(f, line);
    }
}

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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.shell;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.javacard.spi.CardState;
import org.openide.util.NbBundle;

/**
 *
 * @author Anki R Nelaturu
 */
class CommandManager {

    private final Map<String, Command> commands =
            new HashMap<String, Command>(13);

    CommandManager() {
        //XXX localize the command names?
        commands.put("help", new HelpCommand()); //NOI18N
        commands.put("history", new HistoryCommand()); //NOI18N
        commands.put("select", new SelectCommand()); //NOI18N
        commands.put("send", new SendCommand()); //NOI18N
        commands.put("get", new GetCommand()); //NOI18N
        commands.put("start", new StartCommand()); //NOI18N
        commands.put("stop", new StopCommand()); //NOI18N
        commands.put("restart", new RestartCommand()); //NOI18N
        commands.put("resume", new ResumeCommand()); //NOI18N
        commands.put("list", new ListCommand()); //NOI18N
        commands.put("clear", new ClearCommand()); //NOI18N
        commands.put("powerup", new PowerupCommand()); //NOI18N
        commands.put("powerdown", new PowerdownCommand()); //NOI18N
        commands.put("extended", new ExtendedCommand()); //NOI18N
        commands.put("contacted", new ContactedCommand()); //NOI18N
        commands.put("contactless", new ContactlessCommand()); //NOI18N
    }

    public Object[] allCommandNames() {
        Object[] names = commands.keySet().toArray();
        Arrays.sort(names);
        return names;
    }

    public Command getCommand(String str) {
        return commands.get(str);
    }

    public boolean isValidCommand(String str) {
        return commands.containsKey(str);
    }

    public String execute(ShellPanel shellPanel, String command) throws ShellException {
        CardState state = shellPanel.getCard().getState();
        if (state.isTransitionalState()) {
            return NbBundle.getMessage(ResumeCommand.class,
                    "ERR_TRANSITIONAL_STATE", shellPanel.getCard().getState()); //NOI18N
        }
        String[] tokens = command.split(" "); //NOI18N
        Command c = commands.get(tokens[0]);
        if (c != null) {
            return c.execute(shellPanel, tokens);
        }
        throw new ShellException(NbBundle.getMessage(CommandManager.class,
                "ERR_UNKNOWN_COMMAND", //NOI18N
                command));
    }
}

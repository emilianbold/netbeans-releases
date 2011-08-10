/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.project.ui.groups;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;
import org.openide.util.NbBundle.Messages;
import static org.netbeans.modules.project.ui.groups.Bundle.*;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=OptionProcessor.class)
@Messages({
    "GroupOptionProcessor.open.name=--open-group NAME",
    "GroupOptionProcessor.open.desc=open a project group by name",
    "GroupOptionProcessor.close.desc=close any open project group",
    "GroupOptionProcessor.list.desc=list available project groups"
})
public class GroupOptionProcessor extends OptionProcessor {

    private static final Option OPEN_OPTION =
            Option.shortDescription(
            Option.displayName(
            Option.requiredArgument(Option.NO_SHORT_NAME, "open-group"),
            Bundle.class.getName(), "GroupOptionProcessor.open.name"),
            Bundle.class.getName(), "GroupOptionProcessor.open.desc");
    private static final Option CLOSE_OPTION =
            Option.shortDescription(
            Option.withoutArgument(Option.NO_SHORT_NAME, "close-group"),
            Bundle.class.getName(), "GroupOptionProcessor.close.desc");
    private static final Option LIST_OPTION =
            Option.shortDescription(
            Option.withoutArgument(Option.NO_SHORT_NAME, "list-groups"),
            Bundle.class.getName(), "GroupOptionProcessor.list.desc");

    @Override protected Set<Option> getOptions() {
        return new LinkedHashSet<Option>(Arrays.asList(OPEN_OPTION, CLOSE_OPTION, LIST_OPTION));
    }

    @Messages({
        "# {0} - name of group", "GroupOptionProcessor.no_such_group=No such group: {0}",
        "GroupOptionProcessor.column_id=Shortened Name",
        "GroupOptionProcessor.column_name=Full Name"
    })
    @Override protected void process(Env env, Map<Option,String[]> optionValues) throws CommandException {
        String[] val = optionValues.get(OPEN_OPTION);
        if (val != null) {
            String name = val[0];
            for (Group g : Group.allGroups()) {
                if (g.id.equals(name) || g.getName().equals(name)) {
                    Group.setActiveGroup(g);
                    return;
                }
            }
            throw new CommandException(2, GroupOptionProcessor_no_such_group(name));
        } else if (optionValues.containsKey(CLOSE_OPTION)) {
            Group.setActiveGroup(null);
        } else if (optionValues.containsKey(LIST_OPTION)) {
            int max_size = GroupOptionProcessor_column_id().length();
            for (Group g : Group.allGroups()) {
                max_size = Math.max(max_size, g.id.length());
            }
            PrintStream ps = env.getOutputStream();
            ps.printf("%-" + max_size + "s  %s\n", GroupOptionProcessor_column_id(), GroupOptionProcessor_column_name());
            for (Group g : Group.allGroups()) {
                ps.printf("%-" + max_size + "s  %s\n", g.id, g.getName());
            }
        }
    }

}

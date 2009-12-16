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

package org.netbeans.modules.php.spi.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.php.api.util.StringUtils;

/**
 * <b>Warning:</b> Subclasses should not hold strong reference
 * to {@link org.netbeans.modules.php.api.phpmodule.PhpModule PHP module}.
 * @author Petr Hejl, Tomas Mysik
 */
public abstract class FrameworkCommand implements Comparable<FrameworkCommand> {

    private final List<String> commands;
    private final String description;
    private final String displayName;
    private volatile String help;

    protected FrameworkCommand(String command, String description, String displayName) {
        this(Arrays.asList(command), description, displayName);
    }

    /**
     * @since 1.24
     */
    protected FrameworkCommand(List<String> commands, String description, String displayName) {
        this.commands = Collections.unmodifiableList(commands);
        this.description = description;
        this.displayName = displayName;
    }

    /**
     * Get the help for the current command. This method is called just once,
     * the result is cached and used later each time user wants to see it.
     * @return the help message for the current command, it should not be <code>null</code>
     */
    protected abstract String getHelpInternal();

    /**
     * Get the full form of this command (e.g. suitable for preview).
     * @return the full form of this command.
     */
    public abstract String getPreview();

    public String getCommand() {
        return StringUtils.implode(commands, " "); // NOI18N
    }

    /**
     * @since 1.24
     */
    protected List<String> getCommands() {
        return commands;
    }

    public String getDescription() {
        return description;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * This method can be time consuming so it should be called in a background thread.
     */
    public String getHelp() {
        if (help == null) {
            help = getHelpInternal();
        }
        return help;
    }

    public boolean hasHelp() {
        return help != null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FrameworkCommand other = (FrameworkCommand) obj;
        if (!commands.equals(other.commands)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + commands.hashCode();
        return hash;
    }

    public int compareTo(FrameworkCommand o) {
        if (commands.isEmpty() || o.getCommands().isEmpty()) {
            assert displayName != null : "displayName not null";
            assert o.getDisplayName() != null : "other displayName not null";
            return displayName.compareTo(o.getDisplayName());
        }
        return getCommand().compareTo(o.getCommand());
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder(200);
        buffer.append(getClass().getName());
        buffer.append(" [displayName: ");
        buffer.append(displayName);
        buffer.append(", commands: ");
        buffer.append(commands);
        buffer.append(", description: ");
        buffer.append(description);
        buffer.append(", help: ");
        buffer.append(help);
        buffer.append("]");
        return buffer.toString();
    }
}

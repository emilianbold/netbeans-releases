/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ruby.rubyproject;

import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Option for enabling detailed logging.
 *
 * @author Erno Mononen
 */
public class RubyLoggingOption extends AdvancedOption {

    private static final Logger RUBY_LOGGER = Logger.getLogger("org.netbeans.modules.ruby"); //NOI18N
    private static final Logger YAML_LOGGER = Logger.getLogger("org.netbeans.modules.languages.yaml"); //NOI18N
    private static final Logger EXECUTION_LOGGER = Logger.getLogger("org.netbeans.api.extexecution"); //NOI18N
    private static final Logger DEBUGGER_LOGGER = Logger.getLogger("org.rubyforge.debugcommons"); //NOI18N
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(RubyLoggingOption.class, "RubyLoggingOption.displayName.text");
    }

    @Override
    public String getTooltip() {
        return getDisplayName();
    }

    @Override
    public OptionsPanelController create() {
        return new Controller();
    }

    static void initLoggers () {
        setIfHigher(RUBY_LOGGER, RubyLoggingSettings.getDefault().getLoggingLevel(RUBY_LOGGER));
        setIfHigher(YAML_LOGGER, RubyLoggingSettings.getDefault().getLoggingLevel(YAML_LOGGER));
        setIfHigher(EXECUTION_LOGGER, RubyLoggingSettings.getDefault().getLoggingLevel(EXECUTION_LOGGER));
        setIfHigher(DEBUGGER_LOGGER, RubyLoggingSettings.getDefault().getLoggingLevel(DEBUGGER_LOGGER));
    }

    private static void setIfHigher(Logger logger, Level level) {
        if (level == null) {
            return;
        }
        if (logger.getLevel() == null || level.intValue() < logger.getLevel().intValue()) {
            logger.setLevel(level);
        }
    }

    private static final class Controller extends OptionsPanelController {

        private final RubyLoggingOptionsPanel component = new RubyLoggingOptionsPanel();

        @Override
        public void update() {
            Level debuggerLevel = DEBUGGER_LOGGER.getLevel();
            Level rubyLevel = RUBY_LOGGER.getLevel();
            boolean toggle = debuggerLevel != null && debuggerLevel.intValue() <= Level.FINEST.intValue();
            component.setDebuggerLogging(toggle);
            toggle = rubyLevel != null && rubyLevel.intValue() <= Level.FINE.intValue();
            component.setStandardLogging(toggle);
        }

        @Override
        public void applyChanges() {
            setLevel(RUBY_LOGGER, Level.FINE, component.isStandardLoggingEnabled());
            setLevel(YAML_LOGGER, Level.FINE, component.isStandardLoggingEnabled());
            setLevel(EXECUTION_LOGGER, Level.FINE, component.isStandardLoggingEnabled());
            setLevel(DEBUGGER_LOGGER, Level.FINEST, component.isDebuggerLoggingEnabled());
        }

        @Override
        public void cancel() {
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public boolean isChanged() {
            //XXX
            return false;
        }

        @Override
        public JComponent getComponent(Lookup masterLookup) {
            return component;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(RubyLoggingOption.class);
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
        }

        private void setLevel(Logger logger, Level level, boolean selected) {
            RubyLoggingSettings loggingSettings = RubyLoggingSettings.getDefault();
            if (selected) {
                Level original = logger.getLevel();
                if (original == null || original.intValue() > level.intValue()) {
                    logger.setLevel(level);
                    loggingSettings.setLoggingLevel(logger, level);
                }
            } else {
                logger.setLevel(null);
                loggingSettings.setLoggingLevel(logger, null);
            }
        }
    }
}

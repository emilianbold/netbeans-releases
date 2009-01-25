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

package org.netbeans.spi.debugger.ui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import javax.swing.JComponent;

import org.netbeans.modules.debugger.ui.registry.DebuggerProcessor;
import org.netbeans.spi.debugger.ContextAwareService;
import org.netbeans.spi.debugger.ContextAwareSupport;
import org.netbeans.spi.debugger.ContextProvider;


/**
 * Support for "Attach ..." dialog. Represents one type of attaching.
 *
 * @author   Jan Jancura
 */
public abstract class AttachType {

    /**
     * TODO
     *
     * @return display name of this Attach Type
     */
    public String getTypeDisplayName () {
        return null;
    }

    /**
     * Returns visual customizer for this Attach Type. Customizer can
     * optionally implement {@link Controller} interface. In that case please
     * notice the clash of {@link Controller#isValid()} method with
     * {@link javax.swing.JComponent#isValid()} and consider extending
     * {@link #getController()} method in case you need to provide
     * false validity in some cases.
     *
     * @return visual customizer for this Attach Type
     */
    public abstract JComponent getCustomizer ();

    /**
     * Return the implementation of {@link Controller} interface.<br/>
     * In cases when it's not desired to implement {@link Controller} interface
     * by the JComponent returned from {@link #getCustomizer()} method, because
     * of the clash of {@link Controller#isValid()} method with
     * {@link javax.swing.JComponent#isValid()}, an explicit implementation
     * can be returned by overriding this method.
     * The default implementation returns <code>null</code>, in which case
     * the customizer component is cast to {@link Controller}.
     *
     * @return Controller implementation or <code>null</code>.
     * @since 2.14
     */
    public Controller getController() {
        return null;
    }

    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE})
    public @interface Registration {
        String displayName();
    }

    static class ContextAware extends AttachType implements ContextAwareService<AttachType> {

        private String serviceName;
        private String displayName;
        private ContextProvider context;
        private AttachType delegate;

        private ContextAware(String serviceName, String displayName) {
            this.serviceName = serviceName;
            this.displayName = displayName;
        }

        private ContextAware(String serviceName, String displayName, ContextProvider context) {
            this.serviceName = serviceName;
            this.displayName = displayName;
            this.context = context;
        }

        private synchronized AttachType getDelegate() {
            if (delegate == null) {
                delegate = (AttachType) ContextAwareSupport.createInstance(serviceName, context);
            }
            return delegate;
        }

        public AttachType forContext(ContextProvider context) {
            if (context == this.context) {
                return this;
            } else {
                return new AttachType.ContextAware(serviceName, displayName, context);
            }
        }

        @Override
        public String getTypeDisplayName() {
            if (displayName != null) {
                return displayName;
            }
            return getDelegate().getTypeDisplayName();
        }

        @Override
        public JComponent getCustomizer() {
            return getDelegate().getCustomizer();
        }

        @Override
        public Controller getController() {
            return getDelegate().getController();
        }

        /**
         * Creates instance of <code>ContextAwareService</code> based on layer.xml
         * attribute values
         *
         * @param attrs attributes loaded from layer.xml
         * @return new <code>ContextAwareService</code> instance
         */
        static ContextAwareService createService(Map attrs) throws ClassNotFoundException {
            String serviceName = (String) attrs.get(DebuggerProcessor.SERVICE_NAME);
            String displayName = (String) attrs.get("displayName");
            return new AttachType.ContextAware(serviceName, displayName);
        }
    }

}
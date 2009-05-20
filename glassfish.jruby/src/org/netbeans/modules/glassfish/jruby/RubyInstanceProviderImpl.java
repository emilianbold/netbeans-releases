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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.jruby;

import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.ruby.railsprojects.server.spi.RubyInstance;
import org.netbeans.modules.ruby.railsprojects.server.spi.RubyInstanceProvider;
import org.netbeans.modules.glassfish.spi.ServerUtilities;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.openide.util.ChangeSupport;


/**
 *
 * @author Peter Williams
 */
public final class RubyInstanceProviderImpl implements RubyInstanceProvider, ChangeListener {

    // XXX not real happy about the separation of providers for V3 vs V3 prelude
    // but that's what we have for now.
    private static final RubyInstanceProviderImpl v3FullSupport = createImpl(ServerUtilities.getEe6Utilities());
    private static final RubyInstanceProviderImpl v3PreludeSupport = createImpl(ServerUtilities.getPreludeUtilities());

    private static RubyInstanceProviderImpl createImpl(ServerUtilities serverUtilities) {
        return serverUtilities != null ? new RubyInstanceProviderImpl(serverUtilities) : null;
    }

    private final ChangeSupport support = new ChangeSupport(this);
    private ServerUtilities serverUtilities;
    
    private RubyInstanceProviderImpl(ServerUtilities serverUtilities) {
        this.serverUtilities = serverUtilities;
        ServerInstanceProvider provider = serverUtilities.getServerProvider();
        provider.addChangeListener(this);
    }

    public static RubyInstanceProvider getV3Provider() {
        return v3FullSupport;
    }

    public static RubyInstanceProvider getV3PreludeProvider() {
        return v3PreludeSupport;
    }

    // Additional interesting API's
    public boolean hasServer(String uri) {
        return getInstance(uri) != null;
    }
    
    // ------------------------------------------------------------------------
    // RubyInstanceProvider interface implementation
    // ------------------------------------------------------------------------
    public List<? extends RubyInstance> getInstances() {
        return serverUtilities.getInstancesByCapability(RubyInstance.class);
    }

    public RubyInstance getInstance(String uri) {
        return serverUtilities.getInstanceByCapability(uri, RubyInstance.class);
    }

    public void addChangeListener(ChangeListener listener) {
        support.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        support.removeChangeListener(listener);
    }

    // ------------------------------------------------------------------------
    // ChangeListener implementation
    // ------------------------------------------------------------------------
    public void stateChanged(ChangeEvent e) {
        // Forward change event, regardless of whether ruby instance list has 
        // changed or not.  It's not worth the effort to determine that.
        support.fireChange();
    }

}

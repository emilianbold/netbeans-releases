/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.spi.impl;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Icon;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.modules.refactoring.spi.ui.ScopeProvider;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

public class DelegatingScopeProvider extends ScopeProvider implements DelegatingScopeInformation {

    private final Map<?, ?> map;
    private final String id;
    private final String displayName;
    private final int position;
    private Icon icon;
    private ScopeProvider delegate;

    public static DelegatingScopeProvider create(Map<?, ?> map) {
        return new DelegatingScopeProvider(map);
    }

    public DelegatingScopeProvider(ScopeProvider delegate, String id, String displayName, int position, Icon image) {
        this.icon = image;
        this.id = id;
        this.displayName = displayName;
        this.position = position;
        this.delegate = delegate;
        map = null;
    }

    private DelegatingScopeProvider(Map<?, ?> map) {
        this.map = map;
        String path = (String) map.get("iconBase"); //NOI18N
        icon = path != null && !path.equals("") ? ImageUtilities.loadImageIcon(path, false) : null;
        id = (String) map.get("id"); //NOI18N
        displayName = (String) map.get("displayName"); //NOI18N
        position = (Integer) map.get("position"); //NOI18N
    }

    public ScopeProvider getDelegate() {
        if (delegate == null) {
            assert map != null;
            delegate = (ScopeProvider) map.get("delegate"); // NOI18N
        }
        return delegate;
    }
    
    @Override
    public boolean initialize(Lookup context, AtomicBoolean cancel) {
        ScopeProvider d = getDelegate();
        return d != null ? d.initialize(context, cancel) : null;
    }

    @Override
    public Scope getScope() {
        ScopeProvider d = getDelegate();
        return d != null ? d.getScope() : null;
    }

    @Override
    public Problem getProblem() {
        ScopeProvider d = getDelegate();
        return d != null ? d.getProblem(): null;
    }
    
    @Override
    public Icon getIcon() {
        Icon delegateIcon = null;
        ScopeProvider d = getDelegate();
        if(d != null) {
            delegateIcon = d.getIcon();
        }
        return delegateIcon == null? icon : delegateIcon;
    }

    @Override
    public String getDisplayName() {
        String detail = null;
        ScopeProvider d = getDelegate();
        if(d != null) {
            detail = d.getDetail();
        }
        return detail == null? displayName : displayName + " (" + detail + ")";
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public String getId() {
        return id;
    }
}

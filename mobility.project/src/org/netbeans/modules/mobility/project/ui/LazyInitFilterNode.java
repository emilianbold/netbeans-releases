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

package org.netbeans.modules.mobility.project.ui;

import java.awt.EventQueue;
import java.io.CharConversionException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;
import org.openide.util.Lookup;
import org.openide.xml.XMLUtil;
import static org.netbeans.modules.mobility.project.ui.DecoratedNode.BOLD;
import static org.netbeans.modules.mobility.project.ui.DecoratedNode.ERROR;
import static org.netbeans.modules.mobility.project.ui.DecoratedNode.GRAY;

/**
 * FilterNode subclass which does not need the node it is filtering
 * to exist until the first time it is really expanded.  Also handles
 * same properties as DecoratedNode - basically a lazy filternode version
 * of that.
 *
 * Use for things like PackageView, where what you get is nodes, but
 * you do not want to create them until they are really needed.
 *
 * @author Tim Boudreau
 */
abstract class LazyInitFilterNode extends FilterNode {
    public LazyInitFilterNode(Lookup lkp) {
        super (new AbstractNode(Children.LEAF), Children.LEAF, lkp);
        reinit();
    }

    protected final void reinit() {
        setChildren(createLazyChildren());
    }

    protected final FilterNode.Children createLazyChildren() {
        return new LazyChildren();
    }

    protected final boolean isLazyChildren() {
        return getChildren() instanceof LazyChildren;
    }

    @Override
    public final void setName(final String name) {
        if (name.equals(this.getName())) {
            fireDisplayNameChange(null, null);
        } else {
            super.setName(name);
        }
    }

    @Override
    public final void setValue(String attributeName, Object value) {
        super.setValue(attributeName, value);
        if (GRAY.equals(attributeName) || BOLD.equals(attributeName) || ERROR.equals (attributeName)) {
            fireDisplayNameChange (null, null);
        }
    }

    @Override
    public String getHtmlDisplayName() {
        String displayName = this.getDisplayName();
        try {
            displayName = XMLUtil.toElementContent(displayName);
        } catch (CharConversionException ex) {
            // OK, no annotation in this case
            return null;
        }
        StringBuilder sb = new StringBuilder();
        final Boolean bold = Boolean.TRUE.equals(getValue(BOLD));
        if (bold == Boolean.TRUE) {
            sb.append ("<B>");
        }
        final Boolean error = Boolean.TRUE.equals(getValue(ERROR));
        final Boolean gray = Boolean.TRUE.equals(getValue(GRAY));
        if (error == Boolean.TRUE) {
            sb.append ("<font color=\"!nb.errorForeground\">");
        } else if (gray) {
            sb.append ("<font color=\"!controlShadow\">");
        }
        sb.append (displayName);
        return sb.toString();
    }

    protected abstract Children createRealChildren();

    private class LazyChildren extends FilterNode.Children implements Runnable {

        LazyChildren() {
            super (new AbstractNode(Children.LEAF));
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            FilterNode.Children kids = createRealChildren();
            if (kids == null) {
                //we're being constructed still, do it later
                EventQueue.invokeLater (this);
            } else {
                setChildren (kids);
            }
        }

        @Override
        protected void removeNotify() {
            super.removeNotify();
            setChildren (new LazyChildren());
        }

        public void run() {
            setChildren (createRealChildren());
        }
    }
}

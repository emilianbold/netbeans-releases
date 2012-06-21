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
package org.netbeans.modules.web.browser.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JToggleButton;
import org.netbeans.modules.web.browser.api.ResizeOption;
import org.netbeans.modules.web.browser.spi.Resizable;
import org.openide.util.Lookup;

/**
 * Button to resize the browser window.
 * 
 * @author S. Aubrecht
 */
class BrowserResizeButton {

    private BrowserResizeButton() {
    }

    static AbstractButton create( ResizeOption resizeOption, Lookup context ) {
        return create( new DummyIcon(), resizeOption.getToolTip(), resizeOption.getWidth(),
                resizeOption.getHeigh(), context );
    }

    static AbstractButton create( String label, Lookup context ) {
        return create( new DummyIcon(), label, -1, -1, context );
    }

    private static AbstractButton create( Icon icon, String label, final int width, final int height, final Lookup context ) {
        JToggleButton res = new JToggleButton( icon );
        res.setToolTipText( label );
        res.setEnabled( null != context.lookup( Resizable.class ) );
        res.addItemListener( new ItemListener() {
            @Override
            public void itemStateChanged( ItemEvent e ) {
                if( e.getStateChange() != ItemEvent.SELECTED )
                    return;

                doResize( width, height, context );
            }
        });
        return res;
    }

    private static void doResize( final int width, final int height, final Lookup context ) {
        Resizable resizable = context.lookup( Resizable.class );
        if( null == resizable )
            return;

        if( width < 0 || height < 0 ) {
            resizable.autofit();
        } else {
            resizable.resize( width, height );
        }
    }

    private static class DummyIcon implements Icon {

        @Override
        public void paintIcon( Component c, Graphics g, int x, int y ) {
            g.setColor( Color.red );
            g.fillRect( x, y, getIconWidth(), getIconHeight() );
        }

        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }
    }
}

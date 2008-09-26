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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.visualweb.css2;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;

import org.openide.util.ImageUtilities;
import org.w3c.dom.Element;

import org.netbeans.modules.visualweb.designer.WebForm;
import org.netbeans.modules.visualweb.designer.html.HtmlAttribute;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;


/**
 * ObjectBox represents an &lt;object&gt; tag, or an
 * &lt;applet&gt; tag, in the jsp markup.
 *
 * @author Tor Norbye
 */
public class ObjectBox extends ContainerBox {
    private static final int IMGWIDTH = 48;
    private static final int IMGHEIGHT = 90;
    private static Image appletlogo;
    boolean isApplet;

    /** Use the "getObjectBox" factory method instead */
    private ObjectBox(WebForm webform, Element element, BoxType boxType, boolean inline,
        boolean replaced) {
        super(webform, element, boxType, inline, replaced);
    }

    /** Create a new framebox, or provide one from a cache */
    public static CssBox getObjectBox(WebForm webform, Element element, BoxType boxType, HtmlTag tag,
        boolean inline) {
        // Is this an image?
        // If so I should look up the type and look for mime types,
        // then delegate to the ImageBox factory method. However,
        // that's not yet common usage of <object> so worry about it later.
        //String type = element.getAttribute(HtmlAttribute.TYPE);
        ObjectBox box = new ObjectBox(webform, element, boxType, inline, tag.isReplacedTag());
        String codeType = element.getAttribute(HtmlAttribute.CODETYPE);
        String classId = element.getAttribute(HtmlAttribute.CLASSID);

        // For now we do no previews of any kind
        if ((tag == HtmlTag.APPLET) || "java".equals(codeType) || // NOI18N
                classId.endsWith(".class")) { // NOI18N
            box.isApplet = true;
        }

        return box;
    }

    /** What should the default intrinsic width be? Mozilla 1.6 seems
     * to use 300x150.
     */
    public int getIntrinsicWidth() {
        return 300;
    }

    /** What should the default intrinsic height be? Mozilla 1.6 seems
     * to use 300x150.
     */
    public int getIntrinsicHeight() {
        return 150;
    }

//    public String toString() {
//        return "ObjectBox[" + paramString() + "]";
//    }

    /*
    protected String paramString() {
        return super.paramString() + ", " + markup;
    }
    */
    protected void createChildren(CreateContext context) {
        // No valid children for objects
    }

    public void paint(Graphics g, int px, int py) {
        super.paint(g, px, py);

        if (hidden) {
            return;
        }

        px += leftMargin;
        py += effectiveTopMargin;

        if (isApplet) {
            int x = (px + getX() + (width / 2)) - (IMGWIDTH / 2);
            int y = (py + getY() + (height / 2)) - (IMGHEIGHT / 2);

            Image appletlogo = getAppletLogo();

            if ((appletlogo != null) && (g instanceof Graphics2D)) {
                Graphics2D g2d = (Graphics2D)g;
                AffineTransform t = new AffineTransform();

                //t.translate((float) getX(), (float) getY());
                t.translate((float)x, (float)y);
                g2d.drawImage(appletlogo, t, null);
            }
        }

        //paintFacesWatermark(g, px+getX()+leftMargin, py+getY()+effectiveTopMargin);
    }

    private static Image getAppletLogo() {
        if (appletlogo == null) {
            appletlogo = ImageUtilities.loadImage("org/netbeans/modules/visualweb/designer/resources/applet.gif");

            // NOI18N
        }

        return appletlogo;
    }
}

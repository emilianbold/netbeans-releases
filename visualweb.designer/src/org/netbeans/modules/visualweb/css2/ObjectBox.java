/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.css2;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;

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
            appletlogo = org.openide.util.Utilities.loadImage("org/netbeans/modules/visualweb/css2/applet.gif");

            // NOI18N
        }

        return appletlogo;
    }
}

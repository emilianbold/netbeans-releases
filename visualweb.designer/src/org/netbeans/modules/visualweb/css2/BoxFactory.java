/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

import org.w3c.dom.Element;

import org.netbeans.modules.visualweb.designer.WebForm;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;

/**
 * Creates a box for a given element.
 *
 * @author Tor Norbye
 */
public class BoxFactory {
    private BoxFactory() {
    }

    /**
     * Return a box representing the element, or null if no box should
     * be displayed (as in the case of an input hidden for example)
     * @throws NullPointerException when tag is <code>null</code>
     * @throws IllegalArgumentException when tag.isReplaceTag doesn't match replace parameter
     */
    public static CssBox create(CreateContext context, HtmlTag tag, WebForm webform,
        Element element, BoxType boxType, boolean inline, boolean replaced) {
//        assert tag != null;
//        assert tag.isReplacedTag() == replaced;
        // XXX Later there should be throwin exceptions, for now just following the pattern.
        if (tag == null) {
            throw new NullPointerException("Parameter tag may not be null!"); // NOI18N
        }
        if (tag.isReplacedTag() != replaced) {
            throw new IllegalArgumentException("Parameters are incorrect, tag.isReplaceTag=" + tag.isReplacedTag() // NOI18N
                                                    + ", and replaced=" + replaced + ", has to match!"); // NOI18N
        }

        // This is no longer true: user can override via CSS "display" property:
        //assert tag.isInlineTag() == inline;
        if (tag.isHiddenTag()) {
            // That was easy! Do nothing - don't even process its children
            return null;
        }

        CssBox box = createBox(context, tag, webform, element, boxType, inline, replaced);

        if (box != null) {
            box.tag = tag;
        }

        return box;
    }

    private static CssBox createBox(CreateContext context, HtmlTag tag, WebForm webform,
        Element element, BoxType boxType, boolean inline, boolean replaced) {
        char c = tag.getTagName().charAt(0);

        switch (c) {
        case 't':

            if (tag == HtmlTag.TABLE) {
                CssBox box = TableBox.getTableBox(webform, element, boxType, inline, replaced);

                return box;
            } else if (tag == HtmlTag.TEXTAREA) { // XXX is this a blocktag?

                CssBox box =
                    FormComponentBox.getBox(webform, element, tag, boxType, inline, replaced);

                return box;
            }

            break;

        case 'i':

            if (tag == HtmlTag.INPUT) {
                CssBox box =
                    FormComponentBox.getBox(webform, element, tag, boxType, inline, replaced);

                return box;
            } else if (tag == HtmlTag.IMG) {
                CssBox box =
                    ImageBox.getImageBox(webform, element, webform.getPane(), boxType, inline);

                return box;
            } else if (tag == HtmlTag.IFRAME) {
                return FrameBox.getFrameBox(context, webform, element, boxType, tag, inline);
            }

            break;

        case 'b':

            if (tag == HtmlTag.BR) {
                return new LineBreakBox(webform, element, tag);
            } else if (tag == HtmlTag.BUTTON) {
                CssBox box =
                    FormComponentBox.getBox(webform, element, tag, boxType, inline, replaced);

                return box;
            }

            break;

        case 'j':

            if (tag == HtmlTag.JSPINCLUDE || tag == HtmlTag.JSPINCLUDEX) {
                return JspIncludeBox.getJspIncludeBox(context, webform, element, boxType, tag,
                    inline);
            }

            break;

        case 'f':

            if (tag == HtmlTag.FIELDSET) {
                return FieldSetBox.getFieldSetBox(webform, element, boxType, tag, inline);

                /* <frame> is only allowed as a child of a <frameset> so construction
                   of frames are moved there; any attempts to create frames here should
                   be suppressed because FRAME is recorded as a hidden tag
                } else if (tag == HtmlTag.FRAME) {
                   // XXX Need separate class from iframe?
                   return FrameBox.getFrameBox(context, webform, element, boxType, tag, inline);
                */
                /*
                  <frameset> is only allowed as a body tag, or as a child
                  of the <frameset> tag
                } else if (tag == HtmlTag.FRAMESET) {
                    return FrameSetBox.getFrameSetBox(webform.getPane(),
                                                      webform, element, boxType, tag, inline);
                */
            }

            break;

        case 'a':

            if (tag == HtmlTag.APPLET) {
                return ObjectBox.getObjectBox(webform, element, boxType, tag, inline);
            }

            break;

        case 'o':

            if (tag == HtmlTag.OL) {
                ListBox box = new ListBox(webform, element, boxType, inline, replaced);

                return box;
            } else if (tag == HtmlTag.OBJECT) {
                return ObjectBox.getObjectBox(webform, element, boxType, tag, inline);
            }

            break;

        case 's':

            if (tag == HtmlTag.SELECT) {
                CssBox box =
                    FormComponentBox.getBox(webform, element, tag, boxType, inline, replaced);

                return box;
            }

            break;

        case 'u':

            if (tag == HtmlTag.UL) {
                ListBox box = new ListBox(webform, element, boxType, inline, replaced);

                return box;
            }

            break;

        case 'm':

            if (tag == HtmlTag.MENU) {
                ListBox box = new ListBox(webform, element, boxType, inline, replaced);

                return box;
            }

            break;

        case 'd':

            if (tag == HtmlTag.DIR) {
                ListBox box = new ListBox(webform, element, boxType, inline, replaced);

                return box;
            }

            break;
        }

        // Some tag which should just take on general box formatting
        CssBox box = new ContainerBox(webform, element, boxType, inline, replaced);

        return box;
    }
}

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
package org.netbeans.modules.css.model.impl.semantic.background;

import org.netbeans.modules.css.model.api.semantic.Attachment;
import org.netbeans.modules.css.model.api.semantic.Box;
import org.netbeans.modules.css.model.api.semantic.Color;
import org.netbeans.modules.css.model.api.semantic.Image;
import org.netbeans.modules.css.model.api.semantic.RepeatStyle;
import org.netbeans.modules.css.model.api.semantic.Size;
import org.netbeans.modules.css.model.api.semantic.background.Background;
import org.netbeans.modules.css.model.api.semantic.background.BackgroundPosition;

/**
 *
 * @author marekfukala
 */
public class BackgroundI implements Background {

    private Image image;
    private RepeatStyle repeatStyle;
    private Attachment attachment;
    private BackgroundPosition position;
    private Box clip;
    private Box origin;
    private Size size;
    private Color color;

    @Override
    public Image getImage() {
        return image;
    }

    @Override
    public void setImage(Image image) {
        this.image = image;
    }

    @Override
    public RepeatStyle getRepeatStyle() {
        return repeatStyle;
    }

    @Override
    public void setRepeatStyle(RepeatStyle repeatStyle) {
        this.repeatStyle = repeatStyle;
    }

    @Override
    public Attachment getAttachment() {
        return attachment;
    }

    @Override
    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

    @Override
    public BackgroundPosition getPosition() {
        return position;
    }

    @Override
    public void setPosition(BackgroundPosition position) {
        this.position = position;
    }

    @Override
    public Box getClip() {
        return clip;
    }

    @Override
    public void setClip(Box clip) {
        this.clip = clip;
    }

    @Override
    public Box getOrigin() {
        return origin;
    }

    @Override
    public void setOrigin(Box origin) {
        this.origin = origin;
    }

    @Override
    public Size getSize() {
        return size;
    }

    @Override
    public void setSize(Size size) {
        this.size = size;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }
    
    
    
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ide.ergonomics.ant;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.apache.tools.ant.types.Resource;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class IconResource extends Resource {
    private Resource r;
    private BufferedImage badgeIcon;

    public IconResource(Resource r, BufferedImage badgeIcon) {
        super(r.getName(), r.isExists(), r.getLastModified(), r.isDirectory(), r.getSize());
        this.r = r;
        this.badgeIcon = badgeIcon;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (getName().endsWith(".html")) {
            return r.getInputStream();
        }
        try {
            InputStream is = r.getInputStream();
            BufferedImage img = ImageIO.read(is);
            if (img == null) {
                return r.getInputStream();
            }
            Image merge = ImageUtilities.mergeImages(img, badgeIcon, 0, 0);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write((BufferedImage)merge, "png", os);
            return new ByteArrayInputStream(os.toByteArray());
        } catch (IOException ex) {
            return r.getInputStream();
        }
    }
}

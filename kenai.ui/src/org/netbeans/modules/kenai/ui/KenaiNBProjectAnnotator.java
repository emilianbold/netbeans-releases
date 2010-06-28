/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.ui;

import java.awt.Image;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectIconAnnotator;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Becicka
 */
@ServiceProvider(service=ProjectIconAnnotator.class)
public class KenaiNBProjectAnnotator implements ProjectIconAnnotator {

    private static final Image kenaiBadge = ImageUtilities.loadImage("org/netbeans/modules/kenai/ui/resources/kenai-badge.png");
    private static final String tooltip = "<img src=\""
            + KenaiNBProjectAnnotator.class.getResource("/org/netbeans/modules/kenai/ui/resources/kenai-badge.png")
            + "\">&nbsp;"
            + NbBundle.getMessage(KenaiNBProjectAnnotator.class, "MSG_TeamProject");

    @Override
    public Image annotateIcon(Project p, Image original, boolean openedNode) {
        String s = (String) p.getProjectDirectory().getAttribute("ProvidedExtensions.RemoteLocation"); //NOI18N
        if (s != null || KenaiProject.getNameForRepository(s) != null) {
            original = ImageUtilities.addToolTipToImage(original, tooltip);
            original = ImageUtilities.mergeImages(original, kenaiBadge, 16, 0);
            return original;
        }
        return original;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        //do nothing
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        //do nothing
    }

}

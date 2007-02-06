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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 */

package org.netbeans.modules.vmd.game.integration;

import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.codegen.CodeClassLevelPresenter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.codegen.CodeWriter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.game.integration.components.GameTypes;
import org.netbeans.modules.vmd.game.model.SequenceCD;

import javax.swing.text.StyledDocument;

/**
 * @author David Kaspar
 */
public class GameCodeSupport {

    public static Presenter createSequenceCodePresenter () {
        return new CodeClassLevelPresenter() {
            protected void generateFieldSectionCode (MultiGuardedSection section) {
                CodeWriter writer = section.getWriter ();
                DesignComponent component = getComponent ();

                String name = MidpTypes.getString (component.readProperty (SequenceCD.PROPERTY_NAME));
                int frameMillis = MidpTypes.getInteger (component.readProperty (SequenceCD.PROPERTY_FRAME_MS));
                DesignComponent imageResource = component.readProperty (SequenceCD.PROPERTY_IMAGE_RESOURCE).getComponent ();
                String imageResourceNameNoExt = "NOT_RESOLVED_YET"; // TODO - resolve the name-no-ext from imageResource

                writer.write ("public int sequence_" + name + "_" + imageResourceNameNoExt + "_delay = " + frameMillis + ";\n"); // NOI18N
                writer.write ("public int[] sequence_" + name + "_" + imageResourceNameNoExt + " = {\n"); // NOI18N

                int[] frames = GameTypes.getFrames (component.readProperty (SequenceCD.PROPERTY_FRAMES));
                for (int i = 0; i < frames.length; i ++) {
                    if (i > 0)
                        writer.write (", "); // NOI18N
                    writer.write (Integer.toString (frames[i])); // TODO - previously there was getIndex called on a frame
                }

                writer.write ("};\n");
            }

            protected void generateMethodSectionCode (MultiGuardedSection section) {
            }

            public void generateInitializeSectionCode (MultiGuardedSection section) {
            }

            protected void generateClassBodyCode (StyledDocument document) {
            }

        };
    }

}

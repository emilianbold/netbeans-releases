/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 
//
// Implementation of police driver class for "Freeway".
//

#include <math.h>
#include "vehicle_list.h"
#include "police.h"

const double DELTA_T = 0.0000555; // 1/5 sec expressed in hours
const double OPT_DT = 0.0001; // optimal buffer (in hrs) in front
const double CAR_LEN = 0.004; // car length (in miles) (roughly 16 ft)
const double BRAKE_DV = 6.0; // 30 mph / sec for 1/5 sec
const int CAR_LENGTH = 8;
const int CAR_WIDTH = 4;

Police::Police(int i, int l, double p, double v) {
    classID = CLASS_POLICE;
    name_int = i;
    lane_num = l;
    position = p;
    velocity = v;
    state = VSTATE_MAINTAIN;
    max_speed = 150;
    xlocation = 0;
    ylocation = 0;
    change_state = 0;
    restrict_change = 0;
    absent_mindedness = 0;
    flash_state = 0;
}

double
Police::vehicle_length() {
    return CAR_LEN;
}

void
Police::recalc_pos() {
    // Update position based on velocity
    position += velocity * DELTA_T;

    // Update state of flashing lights
    flash_state = 1 - flash_state;
}

void
Police::draw(GdkDrawable *pix, GdkGC *gc, int x, int y,
        int direction_right, int scale, int xorg, int yorg, int selected) {
    extern GdkColor *color_red, *color_blue;

    this->xloc(x);
    this->yloc(y);

    // If I am heading to the right, then I need to draw brick to the left of 
    // front of car.  If I am heading left, draw brick to the right.
    if (direction_right) {
        x -= (CAR_LENGTH - 1);
    }

    int l = x * scale + xorg;
    int t = y * scale + yorg;
    int w = CAR_LENGTH * scale;
    int h = CAR_WIDTH * scale;
    int w2 = w / 2;
    int h2 = h / 2;

    // Draw brick.
    if (flash_state) {
        gdk_gc_set_foreground(gc, color_red);
    } else {
        gdk_gc_set_foreground(gc, color_blue);
    }
    gdk_draw_rectangle(pix, gc, TRUE, l, t, w, h);

    // Draw flashing lights on top and bottom
    if (flash_state) {
        gdk_gc_set_foreground(gc, color_blue);
    } else {
        gdk_gc_set_foreground(gc, color_red);
    }
    gdk_draw_rectangle(pix, gc, TRUE, l, t, w2, h2);
    gdk_draw_rectangle(pix, gc, TRUE, l + w2, t + h2, w2, h2);

    // Put red box around "current vehicle"
    if (selected) {
        draw_selection(pix, gc, l, t, w, h, scale);
    }
}



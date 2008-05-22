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
// Implementation of truck class for "Freeway".
//

#include <math.h>
#include <gtk/gtk.h>
#include "vehicle.h"
#include "truck.h"


const double DELTA_T = 0.0000555; // 1/5 sec expressed in hours
const double OPT_DT = 0.001; // optimal buffer (in hrs) in front of me
const double CAR_LEN = 0.0091; // truck length (in mi) (roughly 48 feet)
const double BRAKE_DV = 2.0; // 10 mph / sec for 1/5 sec
const int CAR_LENGTH = 20;
const int CAB_LENGTH = 3;
const int CAR_WIDTH = 5;

Truck::Truck(int i, int l, double p, double v) {
    classID = CLASS_TRUCK;
    name_int = i;
    lane_num = l;
    position = p;
    velocity = v;
    state = VSTATE_MAINTAIN;
    max_speed = 70;
    xlocation = 0;
    ylocation = 0;
    change_state = 0;
    restrict_change = 0;
    absent_mindedness = 0;
}

double
Truck::vehicle_length() {
    return CAR_LEN;
}

void
Truck::draw(GdkDrawable *pix, GdkGC *gc, int x, int y, 
        int direction_right, int scale, int xorg, int yorg, int selected) {
    extern GdkColor *color_black;

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

    // Draw brick.
    gdk_draw_rectangle(pix, gc, TRUE, l, t, w, h);

    gdk_gc_set_foreground(gc, color_black);

    int x1 = l;
    int y1 = t;
    int notch_height = scale * ((CAR_WIDTH - 1) / 2);

    // Put notches on left or right, depending on vehicle direction
    if (direction_right) {
        x1 += scale * (CAR_LENGTH - CAB_LENGTH - 1);
    } else {
        x1 += scale * CAB_LENGTH;
    }

    // Put notches on the top and bottom, separating cab from trailer
    gdk_draw_rectangle(pix, gc, TRUE, x1, y1, scale, notch_height);
    y1 += scale * (CAR_WIDTH - 2);
    gdk_draw_rectangle(pix, gc, TRUE, x1, y1, scale, notch_height);

    // Put red box around "current vehicle"
    if (selected) {
        draw_selection(pix, gc, l, t, w, h, scale);
    }
}

double
Truck::optimal_dist(Vehicle *in_front) {
    // Calculate optimal following distance based on my velocity and the 
    // difference in velocity from the car in front.
    double dv = in_front->vel() - velocity;

    return (OPT_DT * velocity + (0.5 * dv * dv * DELTA_T / BRAKE_DV));
}

void
Truck::recalc_velocity() {
    // Update velocity based on state
    switch (state) {
        case VSTATE_COAST: velocity *= 0.98;
            break;
        case VSTATE_BRAKE: velocity -= BRAKE_DV;
            break;
        case VSTATE_ACCELERATE: velocity += 0.25;
            break;
        case VSTATE_MAINTAIN: break;
        case VSTATE_CRASH: velocity = 0.00;
            break;
        case VSTATE_MAX_SPEED: velocity = max_speed;
            break;
        case VSTATE_CHANGE_LANE: break;
        case VSTATE_CHANGE_LEFT: break;
        case VSTATE_CHANGE_RIGHT: break;
    }
    if (velocity < 0.0) {
        velocity = 0.0;
    }
}

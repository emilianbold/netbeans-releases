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
 
#ifndef VEHICLE_H
#define VEHICLE_H

using namespace std;

//
// Header file for vehicle class for "Freeway".
//

#include <iostream>
#include <fstream>
#include <gtk/gtk.h>

#define CLASS_VEHICLE 0

enum VState {
    VSTATE_MAINTAIN = 0, // I'm maintaining speed.
    VSTATE_ACCELERATE, // I'm accelerating.
    VSTATE_COAST, // I'm coasting down to a lower speed.
    VSTATE_BRAKE, // I'm braking.
    VSTATE_CRASH, // I have crashed into the car in front of me.
    VSTATE_MAX_SPEED, // I've topped out at maximum speed for this car.
    VSTATE_CHANGE_LANE, // I'm switching lanes.
    VSTATE_CHANGE_LEFT, // I'm switching lanes to the left.
    VSTATE_CHANGE_RIGHT // I'm switching lanes to the right.
};

class Vehicle {
protected:
    int classID;
    int name_int;
    double position;
    double velocity;
    int max_speed; // top speed of this vehicle
    VState state; // one of the VSTATE_ values above
    int xlocation; // location on screen relative to window (x)
    int ylocation; // location on screen relative to window (y)
    int lane_num; // number of the lane in which I'm driving
    int change_state; // how far through a lane change I've progressed
    int restrict_change; // counter restricts making lane changes too often
    int absent_mindedness; // how long it's been since I updated my state

public:
    Vehicle(int = 0, int = 0, double = 0.0, double = 0.0);

    int name() {
        return name_int;
    }

    VState vstate() {
        return state;
    }

    VState vstate(VState s) {
        return state = s;
    }

    int lane() {
        return lane_num;
    }

    int lane(int l) {
        return lane_num = l;
    }

    double pos() {
        return position;
    }

    double pos(double p) {
        return position = p;
    }

    double rear_pos() {
        return position - vehicle_length();
    }

    double vel() {
        return velocity;
    }

    double vel(double v) {
        return velocity = v;
    }

    int xloc() {
        return xlocation;
    }

    int xloc(int x) {
        return xlocation = x;
    }

    int yloc() {
        return ylocation;
    }

    int yloc(int y) {
        return ylocation = y;
    }

    int top_speed() {
        return max_speed;
    }

    int top_speed(int s) {
        return max_speed = s;
    }

    int lane_change() {
        return change_state;
    }

    int lane_change(int s) {
        return change_state = s;
    }

    //  virtual char         *classname()     { return "vehicle"; }

    virtual char *classname() {
        return (char *) "vehicle";
    }

    virtual int classnum() {
        return CLASS_VEHICLE;
    }
    virtual void recalc(Vehicle *in_front, const int limit,
            void *neighbors);
    virtual void recalc_pos();
    virtual void recalc_state(Vehicle *in_front, const int limit);
    virtual void check_lane_change(Vehicle *in_front, void *neighbors);
    virtual void recalc_velocity();
    virtual int limit_speed(int limit);
    virtual double vehicle_length();
    virtual double optimal_dist(Vehicle *in_front);
    virtual void draw(GdkDrawable *pix, GdkGC *gc,
            int x, int y, int direction_right, int scale,
            int xorg, int yorg, int selected);
    void draw_selection(GdkDrawable *pix, GdkGC *gc,
            int x, int y, int width,
            int height, int scale);
    friend ostream & operator<<(ostream&, const Vehicle&);
    friend istream & operator>>(istream&, Vehicle&);
};

#endif

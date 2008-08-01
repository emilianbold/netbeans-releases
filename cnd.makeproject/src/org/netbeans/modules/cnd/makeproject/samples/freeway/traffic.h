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
// Freeway traffic-simulation control functions
//

class Vehicle;
class FreewayWindow;

extern void traffic_init(int, char **);

extern void traffic_start();
extern void traffic_stop();
extern void traffic_reset();
extern void traffic_clear();
extern void traffic_popup_done();
extern void traffic_popup_display(int);
extern void traffic_popup(Vehicle *);
extern void traffic_gap(gdouble);
extern void traffic_time(gdouble);
extern void traffic_state(int);
extern void traffic_class(int);
extern void traffic_randomize(int);
extern void traffic_position(char *);
extern void traffic_velocity(int);
extern void traffic_max_speed(int);
extern void traffic_step();
extern void traffic_remove();
extern void traffic_speed(int zone, int limit);
extern void traffic_force_popup_done();
extern void traffic_set_popup_values(Vehicle *);
extern void traffic_popup_scroll(Vehicle *);
extern void traffic_cancel();
extern void traffic_do_load(char *);
extern void traffic_do_save(char *);
extern void traffic_file_close();

extern char *traffic_current_file;


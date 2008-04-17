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
 
#ifndef	FREEWAY_WINDOW_H
#define	FREEWAY_WINDOW_H

#include <gtk/gtk.h>
#include <stdio.h>
#include <stdlib.h>

const unsigned long WHITE = 0;
const unsigned long BLACK = 1;

class FreewayWindow;
class VinfoWindow;
class FileWindow;
class HelpWindow;
class FwyControlObjects;
class FwyZoneObjects;
class FwyMenuObjects;
class VinfoControlObjects;

extern FreewayWindow   window;

class FreewayWindow {
public:
    void objects_initialize(int, char **);
    GtkWindow *getShell() { return GTK_WINDOW(shell); }

    GtkWidget *shell;

    GtkWidget *topform;
    GtkWidget *controlform;
    FwyControlObjects *control_objects;

    GtkWidget *separator2;
};

class FwyControlObjects {
public:
    FwyControlObjects(GtkWidget *_shell) { shell = _shell; }
    void objects_initialize();
    GtkWidget *getShellAsWidget() { return shell; }
    GtkWidget *getDrawingArea() { return drawing; }
    GtkWidget *getStatusArea() { return statuslab; }
    GtkWidget *getGapSlider() { return gap_slide; }
    GtkWidget *getTimeSlider() { return time_slide; }
    GtkWidget *getYesToggle() { return yes_tog; }
    GtkWidget *getTimeValueWidget() { return time_val; }
    
    FwyMenuObjects *menu_objects;
    FwyZoneObjects *zone_objects;

protected:
    GtkWidget *shell;
    GtkWidget *gslid_lab;
    GtkWidget *gap_slide;
    GtkWidget *tslid_lab;
    GtkWidget *time_slide;
    GtkWidget *time_lab;
    GtkWidget *time_val;
    GtkWidget *rand_lab;
    GtkWidget *rantog;
    GtkWidget *no_tog;
    GtkWidget *yes_tog;
    GtkWidget *separator;
    GtkWidget *spform;
    GtkWidget *drawing;
    GtkWidget *statuslab;
    GtkWidget *quit;
};

const int NZONES = 3;

// This is the declaration of "array of speeds expressed as char *s"
gchar *const SPEED_STR[] = {
   (gchar *) "75",
   (gchar *) "65",
   (gchar *) "55",
   (gchar *) "45",
   (gchar *) "35",
   (gchar *) "25",
   (gchar *) "15",
   (gchar *) "5",
}; 
const int NSPEEDS = sizeof (SPEED_STR) / sizeof( gchar *);
const int MAX_SPEED = 75;
const int MIN_SPEED = 5;
const int DEFAULT_SPEED = 65;

class FwyZoneObjects {
public:
    FwyZoneObjects(GtkWidget *_vbox) { vbox = _vbox; }
    void objects_initialize();
    GtkWidget *getWidget() { return vbox; }

    GtkWidget *vbox;
    struct Zone {
        GtkWidget *spop;        // speed selection combo box
        GtkWidget *spzonel;     // lowest speed in zone
        GtkWidget *spzoneu;     // highest speed in zone
    } z[NZONES];
};

class FwyMenuObjects {
public:
    FwyMenuObjects(GtkWidget *_window) { window = _window; }
    void objects_initialize();
    GtkWidget *getWindow() { return window; }
    GtkWidget *getMenuBar() { return menubar; }

    GtkWidget *window;
    GtkWidget *menubar;
};


/*
	Call-back functions
*/
extern void file_load();
extern void file_save();
extern void file_saveas();
extern void file_close();
extern void fwy_start();
extern void fwy_stop();
extern void reset_clear(GtkWidget *, gpointer );
extern void reset_reset(GtkWidget *, gpointer );
extern void help_about();
extern void gap_change(GtkWidget *, gpointer ); // uses call_data
extern void time_change(GtkWidget *, gpointer ); // uses call_data
extern void randx(GtkWidget *, gpointer ); // uses client_data
extern void zone_speed(GtkWidget *, gpointer ); // uses client_data
extern void fw_quit();
extern void help_close(GtkWidget *, gpointer );
extern void popup_destroyed(GtkWidget *, gpointer );

#endif /* FREEWAY_WINDOW_H */

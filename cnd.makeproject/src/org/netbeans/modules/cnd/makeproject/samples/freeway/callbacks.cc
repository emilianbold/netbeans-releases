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
// callbacks.cc - Callback functions for UI events and actions
//

#include "FreewayWindow.h"
#include "traffic.h"
#include "about.h"

// Handler for "Load..." menu item
void 
file_load()
{
    GtkWidget *dialog = gtk_file_chooser_dialog_new("Freeway Load File",
            window.getShell(),
            GTK_FILE_CHOOSER_ACTION_OPEN,
            GTK_STOCK_OPEN, GTK_RESPONSE_ACCEPT,
            GTK_STOCK_CANCEL, GTK_RESPONSE_CANCEL,
            NULL);
    if (gtk_dialog_run(GTK_DIALOG(dialog)) == GTK_RESPONSE_ACCEPT) {
        char *filename = gtk_file_chooser_get_filename(GTK_FILE_CHOOSER(dialog));
        traffic_do_load(filename);
        g_free(filename);
    }
}

// Handler for "Save" menu item
void 
file_save()
{
    if (!traffic_current_file) {
        file_saveas();
    } else {
        traffic_do_save(traffic_current_file);
    }
}

// Handler for "Save As..." menu item
void 
file_saveas()
{
    GtkWidget *dialog = gtk_file_chooser_dialog_new("Freeway Save File",
            window.getShell(),
            GTK_FILE_CHOOSER_ACTION_OPEN,
            GTK_STOCK_SAVE, GTK_RESPONSE_ACCEPT,
            GTK_STOCK_CANCEL, GTK_RESPONSE_CANCEL,
            NULL);
    if (gtk_dialog_run(GTK_DIALOG(dialog)) == GTK_RESPONSE_ACCEPT) {
        char *filename = gtk_file_chooser_get_filename(GTK_FILE_CHOOSER(dialog));
        traffic_do_load(filename);
        g_free(filename);
    }
}

// Handler for "Close" menu item
void 
file_close()
{
    traffic_file_close();
}

// Handler for "About.." help-menu item
void 
help_about()
{
#if GTK_CHECK_VERSION(2, 12, 0)
    gtk_show_about_dialog(window.getShell(),
            "authors", "Gordon Prieur",
            "program-name", "GtkFreeway",
            "website", "http://developers.sun.com/sunstudio",
            "comments", "A GTK+ reimplementation of the Motif Freeway shipped with Sun Studio",
            NULL);
#else
    GtkWidget *dialog = gtk_dialog_new_with_buttons("About GtkFreeway", window.getShell(),
            GTK_DIALOG_DESTROY_WITH_PARENT, GTK_STOCK_OK, GTK_RESPONSE_NONE, NULL);
    GtkWidget *label = gtk_label_new("A GTK+ reimplementation of the Motif Freeway shipped with Sun Studio");
    g_signal_connect_swapped(dialog, "response", G_CALLBACK(gtk_widget_destroy), dialog);
    gtk_container_add(GTK_CONTAINER(GTK_DIALOG(dialog)->vbox), label);
    gtk_widget_show_all(dialog);
#endif
}

// Handler for `menu_reset (Reset)'
void 
reset_reset(GtkWidget *w, gpointer user_data )
{
    traffic_reset();
}

// Handler for `menu_reset (Clear Wrecks)
void 
reset_clear(GtkWidget *w, gpointer user_data )
{
    traffic_clear();
}

// Callback function for `gap'
void 
gap_change(GtkWidget *w, gpointer user_data )
{
    traffic_gap(gtk_range_get_value(GTK_RANGE(w)));
}

void 
time_change(GtkWidget *w, gpointer user_data )
{
    traffic_time(gtk_range_get_value(GTK_RANGE(w)));
}

void 
fwy_start()
{
    traffic_start();
}

// Callback function for `button_stop'
void 
fwy_stop()
{
    traffic_stop();
}

// Callback function for `choice_randomize'
void 
randx(GtkWidget *w, gpointer user_data)
{
    if (gtk_toggle_button_get_active(GTK_TOGGLE_BUTTON(w))) {
        traffic_randomize((int) user_data);
    }
}

// Callback function for setting speed zones.
void 
zone_speed(GtkWidget *w, gpointer user_data )
{
    int zone = ((int) user_data) - 1;
    int i = gtk_combo_box_get_active(GTK_COMBO_BOX(w));
    
    if (zone >= 0 && zone < NZONES && i >= 0 && i < NSPEEDS) {
        traffic_speed(zone, atoi(SPEED_STR[i]));
    }
}

// Callback for "Close" button on Help popup
void 
help_close (GtkWidget *w, gpointer user_data )
{
//	XtUnmapWidget(window.help_winp->shell);
}

// Callback for Freeway "Quit" button
void 
fw_quit()
{
    traffic_stop();
    gtk_main_quit();
}

// Callback for Freeway "Quit" button
void 
popup_destroyed(GtkWidget *w, gpointer user_data )
{
    // A user has used the window-system's popup-window menu
    // to dismiss the window. This cuases the window to be destroyed
    // since the XmdeleteResponse resource was sent to XmDESTROY
    // to avoid the strange behavior of different window managers.
    // Figure-out which window was destroyed and reinitialize it.

//	if (w == window.file_winp->shell) {
//		window.file_winp->objects_initialize(&window);
//	} else if (w == window.help_winp->shell) {
//		window.help_winp->objects_initialize(&window);
//	} else if (w == window.vinfo_winp->shell){
//		window.vinfo_winp->objects_initialize(&window);
//	}
}


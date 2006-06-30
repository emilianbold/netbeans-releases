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
 */

#import <Cocoa/Cocoa.h>

@interface NBPreferences : NSObject
{
    IBOutlet NSButton *debug;
    IBOutlet NSPopUpButton *fontSize;
    IBOutlet NSTextField *jdkHome;
    IBOutlet NSPopUpButton *lookFeel;
    IBOutlet NSButton *quitImm;
    IBOutlet NSButton *runImm;
    IBOutlet NSTextField *userDirectory;
    IBOutlet NSFormCell *vmSizeForm;
    IBOutlet NSFormCell *extraParamsForm;
}
- (IBAction)revertExpert:(id)sender;
- (IBAction)revertUser:(id)sender;
- (IBAction)setUserdir:(id)sender;
- (NSArray *)allArguments;
- (void)writeDefaults;
- (NSWindow *)window;
- (NSString *)getLogFile;

#define DEFAULT_NAME_DEBUG @"DEBUG"
#define DEFAULT_NAME_JDKHOME @"JDKHOME"
#define DEFAULT_NAME_FONTSIZE @"FONTSIZE"
#define DEFAULT_NAME_LOOKFEEL @"LOOKFEEL"
#define DEFAULT_NAME_USERDIR @"USERDIR"
#define DEFAULT_NAME_RUNIMM @"RUNIMMEDIATELY"
#define DEFAULT_NAME_QUITIMM @"QUITIMMEDIATELY"
#define DEFAULT_NAME_NBHOME @"NETBEANSHOME"
#define DEFAULT_NAME_VMSIZE @"VMSIZE"
#define DEFAULT_NAME_EXTRAPARAMS @"EXTRAPARAMS"
#define DEFAULT_NAME_SET @"DEFAULTS_SET"

@end

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
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

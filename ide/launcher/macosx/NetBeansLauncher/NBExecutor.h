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

#import "NBPreferences.h"

#define OPEN_LOG_TAG 11

@interface NBExecutor : NSObject
{
    IBOutlet NBPreferences *preferences;
    IBOutlet NSMenu *fileMenu;
	NSString *netbeansHome;
}
- (IBAction)execute:(id)sender;
- (IBAction)openLog:(id)sender;
@end

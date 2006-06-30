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

#import "NBExecutor.h"
#import "NBPreferences.h"

#define NB_SCRIPT @"bin/netbeans"

@implementation NBExecutor

- (IBAction)execute:(id)sender
{	NSTask *nbTask=[NSTask new];

	[preferences writeDefaults];
	[nbTask setCurrentDirectoryPath:netbeansHome];
	[nbTask setLaunchPath:[netbeansHome stringByAppendingPathComponent:NB_SCRIPT]];
	[nbTask setArguments:[preferences allArguments]];
	// NSLog([[nbTask arguments] description]);
	[nbTask launch];
	if ([[NSUserDefaults standardUserDefaults] integerForKey:DEFAULT_NAME_QUITIMM])
		[NSApp performSelector:@selector(terminate:) withObject:nil afterDelay:10];
	else
		[NSApp hide:nil];
	[nbTask release];
}

- (IBAction)openLog:(id)sender
{	NSString *logFile=[preferences getLogFile];

	if (logFile)
		[[NSWorkspace sharedWorkspace] openFile:logFile];
}

- (void)applicationWillTerminate:(NSNotification *)aNotification
{
	[self release];
}

- (BOOL)testNetbeansHome:(NSString *)home
{	if ([home length])
	{	NSString *script=[home stringByAppendingPathComponent:NB_SCRIPT];
		NSFileManager *fm=[NSFileManager defaultManager];
	
		return [fm isExecutableFileAtPath:script];
	}
	return NO;
}

- (NSString *)findNetbeans
{	NSString *defaultHome=[[NSBundle mainBundle] pathForResource:@"netbeans" ofType:@""];
	
	if (![self testNetbeansHome:defaultHome])
	{	NSUserDefaults *def=[NSUserDefaults standardUserDefaults];
		NSString *nbHome=[def stringForKey:DEFAULT_NAME_NBHOME];

		if  (![self testNetbeansHome:nbHome])
		{	NSString *newHome=nil;
		
			do
			{	int ret;
				NSOpenPanel *panel;
				NSString *directory;
				
				ret=NSRunAlertPanel(NSLocalizedString(@"NetBeans Launcher",@"Title of alert when NetBeans IDE root was not found"),
					NSLocalizedString(@"Cannot find NetBeans IDE root directory",@"Message indicating that IDE root was not found"),
					NSLocalizedString(@"Quit",@"Quit."),
					NSLocalizedString(@"Find...",@"Tile of button, which is used to display fileselector to locate IDE root"),
					nil);
				if (ret==NSAlertDefaultReturn)
					[NSApp terminate:nil];
				panel=[NSOpenPanel openPanel];
				[panel setCanChooseFiles:NO];
				[panel setCanChooseDirectories:YES];
				[panel runModal];
				directory=[panel filename];
				if (directory)
					newHome=directory;
			}while(![self testNetbeansHome:newHome]);
			[def setObject:newHome forKey:DEFAULT_NAME_NBHOME];
			return newHome;
		}
		return nbHome;
	}
	return defaultHome;
}

- (void)applicationDidFinishLaunching:(NSNotification *)aNotification
{	NSUserDefaults *def=[NSUserDefaults standardUserDefaults];

	netbeansHome=[[self findNetbeans] retain];
	if (![def integerForKey:DEFAULT_NAME_SET]) 
		[[preferences window] makeKeyAndOrderFront:nil];
	if ([def integerForKey:DEFAULT_NAME_RUNIMM])
		[self execute:nil];
}

- (BOOL)validateMenuItem:(NSMenuItem *)anItem {	
	if ([anItem tag]==OPEN_LOG_TAG)
	{	if (![preferences getLogFile])
			return NO;
	}
    return YES;
}


- (void)awakeFromNib
{	[fileMenu setAutoenablesItems:YES];
}

- (void)dealloc
{	[preferences release];
	[netbeansHome release];
	[super dealloc];
}

@end

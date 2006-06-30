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

#import "NBPreferences.h"
#import <JavaVM/JavaVM.h>

@implementation NBPreferences

#define DEFAULT_DEBUG 0
#define NODEFAULT_DEBUG @"-J-Xdebug -J-Xnoagent -J-Xrunjdwp:transport=dt_socket,server=y,suspend=n"

#define DEFAULT_JDKHOME @""
#define DEFAULT_JDKPATH @"/Library/Java/Home"
#define JDK_STRING @"-jdkhome"

#define DEFAULT_FONTSIZE 0
#define FONTSIZE_STRING @"--fontsize"

#define DEFAULT_LOOKFEEL 0
#define LOOKFEEL_STRING @"--laf"
NSString *look_and_feels[]={@"apple.laf.AquaLookAndFeel",@"javax.swing.plaf.metal.MetalLookAndFeel"};

#define DEFAULT_USERDIR @""
#define USERDIR_STRING @"--userdir"
#define LOG_FILE @"var/log/messages.log"

#define DEFAULT_RUNIMM 0

#define DEFAULT_QUITIMM 0

#define ICON_FORMAT @"-J-Xdock:icon=%@"
#define NAME_FORMAT @"-J-Xdock:name=NetBeans"

#define DEFAULT_VMSIZE @""
#define VMSIZE_STRING @"-J-Xmx%@m"

#define DEFAULT_EXTRAPARAMS @""

- (void)readDefaults
{	NSUserDefaults *def=[NSUserDefaults standardUserDefaults];

	if ([def integerForKey:DEFAULT_NAME_SET])
	 {	NSString *vmsize=[def stringForKey:DEFAULT_NAME_VMSIZE];
		NSString *extra_params=[def stringForKey:DEFAULT_NAME_EXTRAPARAMS];
	
		[debug setState:[def integerForKey:DEFAULT_NAME_DEBUG]];
		[jdkHome setStringValue:[def stringForKey:DEFAULT_NAME_JDKHOME]];
		[fontSize selectItemAtIndex:[def integerForKey:DEFAULT_NAME_FONTSIZE]];
		[lookFeel selectItemAtIndex:[def integerForKey:DEFAULT_NAME_LOOKFEEL]];
		[runImm setState:[def integerForKey:DEFAULT_NAME_RUNIMM]];
		[quitImm setState:[def integerForKey:DEFAULT_NAME_QUITIMM]];
		[userDirectory setStringValue:[def stringForKey:DEFAULT_NAME_USERDIR]];
		if (vmsize)
			[vmSizeForm setStringValue:vmsize];
		if (extra_params)
			[extraParamsForm setStringValue:extra_params];
	}
	else
	{	[self revertExpert:nil];
		[self revertUser:nil];
	}
}

- (void)awakeFromNib
{/*	NSJavaVirtualMachine *vm=[NSJavaVirtualMachine defaultVirtualMachine];
	Class uiManagerclass=NSClassFromString(@"javax.swing.UIManager");
	id obj,obj1;
	
	NSLog(@"XXX");
	NSLog([uiManagerclass description]);
	obj=[[uiManagerclass alloc] init];
	NSLog([obj description]);
	obj1=[obj getInstalledLookAndFeels];
	NSLog([obj1 description]);
*/	[self readDefaults];
	
}

- (void)writeDefaults
{	NSUserDefaults *def=[NSUserDefaults standardUserDefaults];

	[def setInteger:[debug state] forKey:DEFAULT_NAME_DEBUG];
	[def setObject:[jdkHome stringValue] forKey:DEFAULT_NAME_JDKHOME];
	[def setInteger:[fontSize indexOfSelectedItem] forKey:DEFAULT_NAME_FONTSIZE];
	[def setInteger:[lookFeel indexOfSelectedItem] forKey:DEFAULT_NAME_LOOKFEEL];
	[def setInteger:[runImm state] forKey:DEFAULT_NAME_RUNIMM];
	[def setInteger:[quitImm state] forKey:DEFAULT_NAME_QUITIMM];
	[def setObject:[userDirectory stringValue] forKey:DEFAULT_NAME_USERDIR]; 
	[def setObject:[vmSizeForm stringValue] forKey:DEFAULT_NAME_VMSIZE]; 
	[def setObject:[extraParamsForm stringValue] forKey:DEFAULT_NAME_EXTRAPARAMS]; 
	[def setBool:YES forKey:DEFAULT_NAME_SET];
}

- (IBAction)revertExpert:(id)sender
{	[debug setState:DEFAULT_DEBUG];
	[jdkHome setStringValue:DEFAULT_JDKHOME];
	[vmSizeForm setStringValue:DEFAULT_VMSIZE];
	[extraParamsForm setStringValue:DEFAULT_EXTRAPARAMS];
}

- (IBAction)revertUser:(id)sender
{	[fontSize selectItemAtIndex:DEFAULT_FONTSIZE];
	[lookFeel selectItemAtIndex:DEFAULT_LOOKFEEL];
	[runImm setState:DEFAULT_RUNIMM];
	[quitImm setState:DEFAULT_QUITIMM];
	[userDirectory setStringValue:DEFAULT_USERDIR]; 
}

- (NSArray *)allArguments
{	NSMutableArray *args=[NSMutableArray array];
	NSBundle *bundle=[NSBundle mainBundle];
	NSString *jdk;
	NSString *vm_size=[vmSizeForm stringValue];
	NSString *extra_params=[extraParamsForm stringValue];

	[args addObject:NAME_FORMAT];
	[args addObject:[NSString stringWithFormat:ICON_FORMAT,[bundle pathForResource:@"netbeans" ofType:@"icns"]]];
	if ([debug state]!=DEFAULT_DEBUG)
		[args addObjectsFromArray:[NODEFAULT_DEBUG componentsSeparatedByString:@" "]];
    jdk=[jdkHome stringValue];
	if ([jdk isEqualToString:DEFAULT_JDKHOME])
		jdk=DEFAULT_JDKPATH;
	[args addObject:JDK_STRING];
	[args addObject:jdk];
	if ([fontSize indexOfSelectedItem]!=DEFAULT_FONTSIZE) {
		[args addObject:FONTSIZE_STRING];
		[args addObject:[[fontSize selectedItem] title]];
	}
	if ([lookFeel indexOfSelectedItem]!=DEFAULT_LOOKFEEL) {
		[args addObject:LOOKFEEL_STRING];
		[args addObject:look_and_feels[[lookFeel indexOfSelectedItem]-1]];
	}
	if (![[userDirectory stringValue] isEqualToString:DEFAULT_USERDIR]) {
		[args addObject:USERDIR_STRING];
		[args addObject:[userDirectory stringValue]];
	}
	if (![vm_size isEqualToString:DEFAULT_VMSIZE])
		[args addObject:[NSString stringWithFormat:VMSIZE_STRING,vm_size]];
	if (![extra_params isEqualToString:DEFAULT_EXTRAPARAMS]) 
	{	NSArray *extra_arr=[extra_params componentsSeparatedByString:@" "];
	
		if ([extra_arr count]>0)
			[args addObjectsFromArray:extra_arr];
	}
	return args;
}

- (IBAction)setUserdir:(id)sender
{	NSOpenPanel *panel=[NSOpenPanel openPanel];
	NSString *directory;
	
	[panel setCanChooseFiles:NO];
	[panel setCanChooseDirectories:YES];
	[panel setDirectory:[userDirectory stringValue]];
	[panel runModal];
	directory=[panel filename];
	if (directory)
		[userDirectory setStringValue:directory];
}

- (NSWindow *)window
{
	return [fontSize window];
}

- (NSString *)getLogFile
{	NSString *nbUserHome=[userDirectory stringValue];
	NSString *logFile;

	if (!nbUserHome || ![nbUserHome length])
		return nil;
	logFile=[nbUserHome stringByAppendingPathComponent:LOG_FILE];
	if (![[NSFileManager defaultManager] isReadableFileAtPath:logFile])
		return nil;
	return logFile;
}

- (void)dealloc
{
	[self writeDefaults];
	[super dealloc];
}

@end

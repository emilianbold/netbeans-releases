grammar CommandLine;

options { 
   output=AST;
   ASTLabelType=CommonTree;
}

@header {

package org.netbeans.modules.java.j2seproject.ui.customizer.vmo.gen;

import org.netbeans.modules.java.j2seproject.ui.customizer.vmo.*;
import java.util.Collections;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

}

@lexer::header {
package org.netbeans.modules.java.j2seproject.ui.customizer.vmo.gen;

import java.util.LinkedList;
import java.util.Queue;
}

@lexer::members {
    private Queue<Token> safeguard = new LinkedList<Token>();

    @Override
    public void recover(RecognitionException re) {
	
        input.rewind();
        while (input.getCharPositionInLine() <= re.charPositionInLine) {
            try {
                state.token = null;
                state.channel = Token.DEFAULT_CHANNEL;
                state.tokenStartCharIndex = input.index();
                state.tokenStartCharPositionInLine = input.getCharPositionInLine();
                state.tokenStartLine = input.getLine();
                state.text = null;
                mLetter();
                safeguard.offer(emit());
            } catch (RecognitionException e) {
                input.consume();
            }
        }
        skip();

    }

    @Override
    public void reportError(RecognitionException e) {
        //System.out.println("ERROR: " + e);        // no reporting yet.
    }

    @Override
    public Token nextToken() {        
        safeguard.offer(super.nextToken());
        return safeguard.poll();
    }
}

@parser::members {
    public List<JavaVMOption<?>> parse() {
        Set<JavaVMOption<?>> result = new HashSet<JavaVMOption<?>>(); 
        try {
            vmOptions_return options_return = vmOptions();
            CommonTree root = options_return.tree;
            if (root instanceof JavaVMOption<?>) {
                result.add((JavaVMOption<?>) root);
            } else if (root != null) {
                result.addAll(root.getChildren());
            }                                       
        } catch (RecognitionException e) {
            e.printStackTrace();
        }
        result.addAll(getAllOptions());
        return new LinkedList<JavaVMOption<?>>(result); 
    }


    private static enum Kind {
        SWITCH, D, LOOSEPARAM, EQPARAM, COLUMNPARAM, FOLLOWED
    }


    private static class OptionDefinition {
        private OptionDefinition(String name, Kind kind) {
            this.kind = kind;
            this.name = name;
        }

        Kind kind;
        String name;
    }

    private static OptionDefinition[] optionsTemplates = {
            new OptionDefinition("client", Kind.SWITCH),
            new OptionDefinition("server", Kind.SWITCH),
            new OptionDefinition("esa", Kind.SWITCH),
            new OptionDefinition("dsa", Kind.SWITCH),
            new OptionDefinition("verbose", Kind.SWITCH),
            new OptionDefinition("verbose:class", Kind.SWITCH),
            new OptionDefinition("verbose:jni", Kind.SWITCH),
            new OptionDefinition("verbose:gc", Kind.SWITCH),
            new OptionDefinition("version", Kind.SWITCH),
            new OptionDefinition("version", Kind.COLUMNPARAM),
            new OptionDefinition("showversion", Kind.SWITCH),
            new OptionDefinition("Xint", Kind.SWITCH),
            new OptionDefinition("Xbatch", Kind.SWITCH),
            new OptionDefinition("Xcheck:jni", Kind.SWITCH),
            new OptionDefinition("Xfuture", Kind.SWITCH),
            new OptionDefinition("Xnoclassgc", Kind.SWITCH),
            new OptionDefinition("Xincgc", Kind.SWITCH),
            new OptionDefinition("Xprof", Kind.SWITCH),
            new OptionDefinition("Xrs", Kind.SWITCH),
            new OptionDefinition("Xshare:off", Kind.SWITCH),
            new OptionDefinition("Xshare:on", Kind.SWITCH),
            new OptionDefinition("Xshare:auto", Kind.SWITCH),
            new OptionDefinition("jre-restrict-search", Kind.SWITCH),
            new OptionDefinition("jre-no-restrict-search", Kind.SWITCH),
            new OptionDefinition("Xmx", Kind.FOLLOWED),
            new OptionDefinition("Xms", Kind.FOLLOWED),
            new OptionDefinition("Xss", Kind.FOLLOWED),
            new OptionDefinition("splash", Kind.COLUMNPARAM),
            new OptionDefinition("javaagent", Kind.COLUMNPARAM),
            new OptionDefinition("agentlib", Kind.COLUMNPARAM),
            new OptionDefinition("agentpath", Kind.COLUMNPARAM),
    };

    public static List<JavaVMOption<?>> getAllOptions() {
        List<JavaVMOption<?>> result = new LinkedList<JavaVMOption<?>>();
        for (OptionDefinition optionsTemplate : optionsTemplates) {
            result.add(createOption(optionsTemplate));
        }
        return result;
    }

    private static JavaVMOption<?> createOption(OptionDefinition definition) {
        switch (definition.kind) {
            case SWITCH:
                return new SwitchNode(definition.name);
            case D:
                return new UserPropertyNode();
            case FOLLOWED:
                return new ParametrizedNode(definition.name, "");
            case COLUMNPARAM:
                return new ParametrizedNode(definition.name, ":");
            case EQPARAM:
                return new ParametrizedNode(definition.name, "=");
            case LOOSEPARAM:
                return new ParametrizedNode(definition.name, " ");
            default:
                throw new IllegalArgumentException("Invalid definition.");
        }
    }



}

WS	:	(' '|'\r'|'\t'|'\u000C'|'\n') {$channel=HIDDEN;};

//switches
SERVER	:	'server';
CLIENT	:	'client';
ESA	:	'enablesystemassertions'|'esa';
DSA	:	'disablesystemassertions' | 'dsa';
VERBOSE	:	'verbose' (':' ('class'|'gc'|'jni'))?;
VERSION	:	'version' (':' Text)?;
SVERION	:	'showversion';
HELP	:	'help' | '?';
X	:	'X';
XINT	:	'Xint';
XBATCH	:	'Xbatch';
XCJNI	:	'Xcheck:jni';
XFUTURE	:	'Xfuture';
XNOCLSGC:	'Xnoclassgc';
XINCGC	:	'Xincgc';
XPROF	:	'Xprof';
XRS	:	'Xrs';
XSHARE	:	'Xshare:'('off'|'on'|'auto');
BOOTCP	:	'Xbootclasspath'('/a'|'/p')? ':' Text;
MEMS	:	'Xms' MEMSIZE;
MEMX	:	'Xmx' MEMSIZE;
SS	:	'Xss' MEMSIZE;
LOGGC	:	'Xloggc:' Text;
SPLASH	:	'splash:' Text;
JAGENT	:	'javaagent:' Text;
EA	:	'ea'| 'enableassertions';
DEA	:	'disableassertions'|'da';
AGENT	:	('agentlib' | 'agentpath') ':' Text;
JRE_SEARCH
	:	'jre-restrict-search';
JRE_NO_SEARCH 
	:	'jre-no-restrict-search';
CP	:	'cp' | 'classpath';

CPROP	:	'D' Text;

fragment
Letter
    :  '\u0021' |        
       '\u0023'..'\u0026' |       
       '\u002b' |
       '\u002e'..'\u0039' |
       '\u0041'..'\u005a' |       
       '\u005c' |
       '\u005f' |
       '\u0061'..'\u007a' |
       '\u007e' |
       '\u00c0'..'\u00d6' |
       '\u00d8'..'\u00f6' |
       '\u00f8'..'\u00ff' |
       '\u0100'..'\u1fff' |
       '\u3040'..'\u318f' |
       '\u3300'..'\u337f' |
       '\u3400'..'\u3d2d' |
       '\u4e00'..'\u9fff' |
       '\uf900'..'\ufaff'
    ;

fragment
MEMSIZE	:	('0'..'9')+('k'|'m'|'K'|'M');

Text	:	Letter (Letter|'-'|':')* ;

    

vmOptions
	:	 (option)*
	;

option	:	'-'! (switchDef|splash|continuous|version|loosedParameter|propertyDef|looseTextualNode[true]) | looseTextualNode[false]
	;



switchDef
	:	(SERVER|CLIENT|ESA|DSA|VERBOSE|SVERION|HELP|X|XINT|XBATCH|XCJNI|XFUTURE|XNOCLSGC|XINCGC|XPROF|XRS|XSHARE|JRE_SEARCH|JRE_NO_SEARCH)  
		-> {new SwitchNode($start)}
	;
	


propertyDef
@init {
	System.out.println("Parsing user property definition");
}
	:	CPROP ('=' Text)? -> {new UserPropertyNode($CPROP, $Text, $CPROP.pos)}
							       //^(CPROP<UserPropertyNode>[$CPROP, $pvalue, $n.pos])
	;    

/*
fragment
propertyValue
	:	('"' Text '"')| Text
	;	
*/

splash	
scope {
	String name;
	String value;
	int idx;
}
@init {
	$splash::name="";
	$splash::value="";
	$splash::idx = -1;
}
	:	(SPLASH|BOOTCP|LOGGC|JAGENT|AGENT)
		{
		$splash::idx = $start.getText().indexOf(':');
		$splash::name=$start.getText().substring(0, $splash::idx); 
		$splash::value=$start.getText().substring($splash::idx + 1);
		}
		-> ^({new ParametrizedNode($start, $splash::name, ":", $splash::value)})
	;
	
//columnSeparator	
//	:	(BOOTCP|LOGGC|JAGENT|AGENT) ':' Text ->  {new ParametrizedNode($start, ":", $Text)}
//	;
	
columnSeparatorOpt
	:	(EA|DEA) (':' Text)? -> {new ParametrizedNode($start, ":", $Text)}
	;	

continuous
	:	(MEMS|MEMX|SS) -> {new ParametrizedNode($start, 3)}
	;
	
version	
scope {
	boolean simple;
	String versionText;
}
:	VERSION 
	{
	$version::simple = !$VERSION.getText().contains(":");	
	}
	-> {$version::simple}? {new SwitchNode($VERSION)}
	-> {new ParametrizedNode($VERSION, "version", ":", $VERSION.getText().substring(8))} 
	;	

loosedParameter
	:	CP Text -> {new ParametrizedNode($CP, " ", $Text, false)}
	;

	
classpath 
	returns [String cls]
@init { 
	$cls = "";	
}
	: l=Letter {$cls += $l.text;} 
		(l=Letter {$cls += $l.text;}
			| '-' {$cls += "-";}
			| ':' {$cls += ":";}
		)*	
	;
	

looseTextualNode[boolean unresolved]
	:
		Text 
		-> {$unresolved}?  {new UnrecognizedOption($start)}
		-> {new UnknownOption($start)}
	;
	
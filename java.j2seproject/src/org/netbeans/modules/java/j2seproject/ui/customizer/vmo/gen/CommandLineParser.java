// $ANTLR 3.1.3 Mar 17, 2009 19:23:44 C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g 2010-10-21 13:38:38


package org.netbeans.modules.java.j2seproject.ui.customizer.vmo.gen;

import org.netbeans.modules.java.j2seproject.ui.customizer.vmo.*;
import java.util.Collections;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;



import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;


import org.antlr.runtime.tree.*;

public class CommandLineParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "WS", "SERVER", "CLIENT", "ESA", "DSA", "VERBOSE", "Text", "VERSION", "SVERION", "HELP", "X", "XINT", "XBATCH", "XCJNI", "XFUTURE", "XNOCLSGC", "XINCGC", "XPROF", "XRS", "XSHARE", "BOOTCP", "MEMSIZE", "MEMS", "MEMX", "SS", "LOGGC", "SPLASH", "JAGENT", "EA", "DEA", "AGENT", "JRE_SEARCH", "JRE_NO_SEARCH", "CP", "CPROP", "Letter", "'-'", "'='", "':'"
    };
    public static final int DEA=33;
    public static final int T__42=42;
    public static final int HELP=13;
    public static final int SVERION=12;
    public static final int T__40=40;
    public static final int XBATCH=16;
    public static final int T__41=41;
    public static final int EA=32;
    public static final int CLIENT=6;
    public static final int DSA=8;
    public static final int XNOCLSGC=19;
    public static final int BOOTCP=24;
    public static final int CPROP=38;
    public static final int MEMSIZE=25;
    public static final int ESA=7;
    public static final int SS=28;
    public static final int VERSION=11;
    public static final int MEMS=26;
    public static final int EOF=-1;
    public static final int JRE_NO_SEARCH=36;
    public static final int XFUTURE=18;
    public static final int X=14;
    public static final int SERVER=5;
    public static final int XSHARE=23;
    public static final int VERBOSE=9;
    public static final int WS=4;
    public static final int SPLASH=30;
    public static final int XPROF=21;
    public static final int XRS=22;
    public static final int MEMX=27;
    public static final int AGENT=34;
    public static final int Text=10;
    public static final int JAGENT=31;
    public static final int JRE_SEARCH=35;
    public static final int XINCGC=20;
    public static final int XINT=15;
    public static final int XCJNI=17;
    public static final int LOGGC=29;
    public static final int Letter=39;
    public static final int CP=37;

    // delegates
    // delegators


        public CommandLineParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public CommandLineParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        
    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() { return CommandLineParser.tokenNames; }
    public String getGrammarFileName() { return "C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g"; }


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





    public static class vmOptions_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "vmOptions"
    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:233:1: vmOptions : ( option )* ;
    public final CommandLineParser.vmOptions_return vmOptions() throws RecognitionException {
        CommandLineParser.vmOptions_return retval = new CommandLineParser.vmOptions_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        CommandLineParser.option_return option1 = null;



        try {
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:234:2: ( ( option )* )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:234:5: ( option )*
            {
            root_0 = (CommonTree)adaptor.nil();

            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:234:5: ( option )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==Text||LA1_0==40) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:234:6: option
            	    {
            	    pushFollow(FOLLOW_option_in_vmOptions700);
            	    option1=option();

            	    state._fsp--;

            	    adaptor.addChild(root_0, option1.getTree());

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "vmOptions"

    public static class option_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "option"
    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:237:1: option : ( '-' ( switchDef | splash | continuous | version | loosedParameter | propertyDef | looseTextualNode[true] ) | looseTextualNode[false] );
    public final CommandLineParser.option_return option() throws RecognitionException {
        CommandLineParser.option_return retval = new CommandLineParser.option_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal2=null;
        CommandLineParser.switchDef_return switchDef3 = null;

        CommandLineParser.splash_return splash4 = null;

        CommandLineParser.continuous_return continuous5 = null;

        CommandLineParser.version_return version6 = null;

        CommandLineParser.loosedParameter_return loosedParameter7 = null;

        CommandLineParser.propertyDef_return propertyDef8 = null;

        CommandLineParser.looseTextualNode_return looseTextualNode9 = null;

        CommandLineParser.looseTextualNode_return looseTextualNode10 = null;


        CommonTree char_literal2_tree=null;

        try {
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:237:8: ( '-' ( switchDef | splash | continuous | version | loosedParameter | propertyDef | looseTextualNode[true] ) | looseTextualNode[false] )
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==40) ) {
                alt3=1;
            }
            else if ( (LA3_0==Text) ) {
                alt3=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }
            switch (alt3) {
                case 1 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:237:10: '-' ( switchDef | splash | continuous | version | loosedParameter | propertyDef | looseTextualNode[true] )
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    char_literal2=(Token)match(input,40,FOLLOW_40_in_option712); 
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:237:15: ( switchDef | splash | continuous | version | loosedParameter | propertyDef | looseTextualNode[true] )
                    int alt2=7;
                    switch ( input.LA(1) ) {
                    case SERVER:
                    case CLIENT:
                    case ESA:
                    case DSA:
                    case VERBOSE:
                    case SVERION:
                    case HELP:
                    case X:
                    case XINT:
                    case XBATCH:
                    case XCJNI:
                    case XFUTURE:
                    case XNOCLSGC:
                    case XINCGC:
                    case XPROF:
                    case XRS:
                    case XSHARE:
                    case JRE_SEARCH:
                    case JRE_NO_SEARCH:
                        {
                        alt2=1;
                        }
                        break;
                    case BOOTCP:
                    case LOGGC:
                    case SPLASH:
                    case JAGENT:
                    case AGENT:
                        {
                        alt2=2;
                        }
                        break;
                    case MEMS:
                    case MEMX:
                    case SS:
                        {
                        alt2=3;
                        }
                        break;
                    case VERSION:
                        {
                        alt2=4;
                        }
                        break;
                    case CP:
                        {
                        alt2=5;
                        }
                        break;
                    case CPROP:
                        {
                        alt2=6;
                        }
                        break;
                    case Text:
                        {
                        alt2=7;
                        }
                        break;
                    default:
                        NoViableAltException nvae =
                            new NoViableAltException("", 2, 0, input);

                        throw nvae;
                    }

                    switch (alt2) {
                        case 1 :
                            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:237:16: switchDef
                            {
                            pushFollow(FOLLOW_switchDef_in_option716);
                            switchDef3=switchDef();

                            state._fsp--;

                            adaptor.addChild(root_0, switchDef3.getTree());

                            }
                            break;
                        case 2 :
                            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:237:26: splash
                            {
                            pushFollow(FOLLOW_splash_in_option718);
                            splash4=splash();

                            state._fsp--;

                            adaptor.addChild(root_0, splash4.getTree());

                            }
                            break;
                        case 3 :
                            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:237:33: continuous
                            {
                            pushFollow(FOLLOW_continuous_in_option720);
                            continuous5=continuous();

                            state._fsp--;

                            adaptor.addChild(root_0, continuous5.getTree());

                            }
                            break;
                        case 4 :
                            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:237:44: version
                            {
                            pushFollow(FOLLOW_version_in_option722);
                            version6=version();

                            state._fsp--;

                            adaptor.addChild(root_0, version6.getTree());

                            }
                            break;
                        case 5 :
                            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:237:52: loosedParameter
                            {
                            pushFollow(FOLLOW_loosedParameter_in_option724);
                            loosedParameter7=loosedParameter();

                            state._fsp--;

                            adaptor.addChild(root_0, loosedParameter7.getTree());

                            }
                            break;
                        case 6 :
                            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:237:68: propertyDef
                            {
                            pushFollow(FOLLOW_propertyDef_in_option726);
                            propertyDef8=propertyDef();

                            state._fsp--;

                            adaptor.addChild(root_0, propertyDef8.getTree());

                            }
                            break;
                        case 7 :
                            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:237:80: looseTextualNode[true]
                            {
                            pushFollow(FOLLOW_looseTextualNode_in_option728);
                            looseTextualNode9=looseTextualNode(true);

                            state._fsp--;

                            adaptor.addChild(root_0, looseTextualNode9.getTree());

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:237:106: looseTextualNode[false]
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_looseTextualNode_in_option734);
                    looseTextualNode10=looseTextualNode(false);

                    state._fsp--;

                    adaptor.addChild(root_0, looseTextualNode10.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "option"

    public static class switchDef_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "switchDef"
    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:242:1: switchDef : ( SERVER | CLIENT | ESA | DSA | VERBOSE | SVERION | HELP | X | XINT | XBATCH | XCJNI | XFUTURE | XNOCLSGC | XINCGC | XPROF | XRS | XSHARE | JRE_SEARCH | JRE_NO_SEARCH ) ->;
    public final CommandLineParser.switchDef_return switchDef() throws RecognitionException {
        CommandLineParser.switchDef_return retval = new CommandLineParser.switchDef_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SERVER11=null;
        Token CLIENT12=null;
        Token ESA13=null;
        Token DSA14=null;
        Token VERBOSE15=null;
        Token SVERION16=null;
        Token HELP17=null;
        Token X18=null;
        Token XINT19=null;
        Token XBATCH20=null;
        Token XCJNI21=null;
        Token XFUTURE22=null;
        Token XNOCLSGC23=null;
        Token XINCGC24=null;
        Token XPROF25=null;
        Token XRS26=null;
        Token XSHARE27=null;
        Token JRE_SEARCH28=null;
        Token JRE_NO_SEARCH29=null;

        CommonTree SERVER11_tree=null;
        CommonTree CLIENT12_tree=null;
        CommonTree ESA13_tree=null;
        CommonTree DSA14_tree=null;
        CommonTree VERBOSE15_tree=null;
        CommonTree SVERION16_tree=null;
        CommonTree HELP17_tree=null;
        CommonTree X18_tree=null;
        CommonTree XINT19_tree=null;
        CommonTree XBATCH20_tree=null;
        CommonTree XCJNI21_tree=null;
        CommonTree XFUTURE22_tree=null;
        CommonTree XNOCLSGC23_tree=null;
        CommonTree XINCGC24_tree=null;
        CommonTree XPROF25_tree=null;
        CommonTree XRS26_tree=null;
        CommonTree XSHARE27_tree=null;
        CommonTree JRE_SEARCH28_tree=null;
        CommonTree JRE_NO_SEARCH29_tree=null;
        RewriteRuleTokenStream stream_HELP=new RewriteRuleTokenStream(adaptor,"token HELP");
        RewriteRuleTokenStream stream_SVERION=new RewriteRuleTokenStream(adaptor,"token SVERION");
        RewriteRuleTokenStream stream_XBATCH=new RewriteRuleTokenStream(adaptor,"token XBATCH");
        RewriteRuleTokenStream stream_XSHARE=new RewriteRuleTokenStream(adaptor,"token XSHARE");
        RewriteRuleTokenStream stream_VERBOSE=new RewriteRuleTokenStream(adaptor,"token VERBOSE");
        RewriteRuleTokenStream stream_CLIENT=new RewriteRuleTokenStream(adaptor,"token CLIENT");
        RewriteRuleTokenStream stream_XPROF=new RewriteRuleTokenStream(adaptor,"token XPROF");
        RewriteRuleTokenStream stream_DSA=new RewriteRuleTokenStream(adaptor,"token DSA");
        RewriteRuleTokenStream stream_XNOCLSGC=new RewriteRuleTokenStream(adaptor,"token XNOCLSGC");
        RewriteRuleTokenStream stream_XRS=new RewriteRuleTokenStream(adaptor,"token XRS");
        RewriteRuleTokenStream stream_ESA=new RewriteRuleTokenStream(adaptor,"token ESA");
        RewriteRuleTokenStream stream_JRE_SEARCH=new RewriteRuleTokenStream(adaptor,"token JRE_SEARCH");
        RewriteRuleTokenStream stream_XINCGC=new RewriteRuleTokenStream(adaptor,"token XINCGC");
        RewriteRuleTokenStream stream_XINT=new RewriteRuleTokenStream(adaptor,"token XINT");
        RewriteRuleTokenStream stream_XCJNI=new RewriteRuleTokenStream(adaptor,"token XCJNI");
        RewriteRuleTokenStream stream_JRE_NO_SEARCH=new RewriteRuleTokenStream(adaptor,"token JRE_NO_SEARCH");
        RewriteRuleTokenStream stream_XFUTURE=new RewriteRuleTokenStream(adaptor,"token XFUTURE");
        RewriteRuleTokenStream stream_X=new RewriteRuleTokenStream(adaptor,"token X");
        RewriteRuleTokenStream stream_SERVER=new RewriteRuleTokenStream(adaptor,"token SERVER");

        try {
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:243:2: ( ( SERVER | CLIENT | ESA | DSA | VERBOSE | SVERION | HELP | X | XINT | XBATCH | XCJNI | XFUTURE | XNOCLSGC | XINCGC | XPROF | XRS | XSHARE | JRE_SEARCH | JRE_NO_SEARCH ) ->)
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:243:4: ( SERVER | CLIENT | ESA | DSA | VERBOSE | SVERION | HELP | X | XINT | XBATCH | XCJNI | XFUTURE | XNOCLSGC | XINCGC | XPROF | XRS | XSHARE | JRE_SEARCH | JRE_NO_SEARCH )
            {
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:243:4: ( SERVER | CLIENT | ESA | DSA | VERBOSE | SVERION | HELP | X | XINT | XBATCH | XCJNI | XFUTURE | XNOCLSGC | XINCGC | XPROF | XRS | XSHARE | JRE_SEARCH | JRE_NO_SEARCH )
            int alt4=19;
            switch ( input.LA(1) ) {
            case SERVER:
                {
                alt4=1;
                }
                break;
            case CLIENT:
                {
                alt4=2;
                }
                break;
            case ESA:
                {
                alt4=3;
                }
                break;
            case DSA:
                {
                alt4=4;
                }
                break;
            case VERBOSE:
                {
                alt4=5;
                }
                break;
            case SVERION:
                {
                alt4=6;
                }
                break;
            case HELP:
                {
                alt4=7;
                }
                break;
            case X:
                {
                alt4=8;
                }
                break;
            case XINT:
                {
                alt4=9;
                }
                break;
            case XBATCH:
                {
                alt4=10;
                }
                break;
            case XCJNI:
                {
                alt4=11;
                }
                break;
            case XFUTURE:
                {
                alt4=12;
                }
                break;
            case XNOCLSGC:
                {
                alt4=13;
                }
                break;
            case XINCGC:
                {
                alt4=14;
                }
                break;
            case XPROF:
                {
                alt4=15;
                }
                break;
            case XRS:
                {
                alt4=16;
                }
                break;
            case XSHARE:
                {
                alt4=17;
                }
                break;
            case JRE_SEARCH:
                {
                alt4=18;
                }
                break;
            case JRE_NO_SEARCH:
                {
                alt4=19;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }

            switch (alt4) {
                case 1 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:243:5: SERVER
                    {
                    SERVER11=(Token)match(input,SERVER,FOLLOW_SERVER_in_switchDef749);  
                    stream_SERVER.add(SERVER11);


                    }
                    break;
                case 2 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:243:12: CLIENT
                    {
                    CLIENT12=(Token)match(input,CLIENT,FOLLOW_CLIENT_in_switchDef751);  
                    stream_CLIENT.add(CLIENT12);


                    }
                    break;
                case 3 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:243:19: ESA
                    {
                    ESA13=(Token)match(input,ESA,FOLLOW_ESA_in_switchDef753);  
                    stream_ESA.add(ESA13);


                    }
                    break;
                case 4 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:243:23: DSA
                    {
                    DSA14=(Token)match(input,DSA,FOLLOW_DSA_in_switchDef755);  
                    stream_DSA.add(DSA14);


                    }
                    break;
                case 5 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:243:27: VERBOSE
                    {
                    VERBOSE15=(Token)match(input,VERBOSE,FOLLOW_VERBOSE_in_switchDef757);  
                    stream_VERBOSE.add(VERBOSE15);


                    }
                    break;
                case 6 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:243:35: SVERION
                    {
                    SVERION16=(Token)match(input,SVERION,FOLLOW_SVERION_in_switchDef759);  
                    stream_SVERION.add(SVERION16);


                    }
                    break;
                case 7 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:243:43: HELP
                    {
                    HELP17=(Token)match(input,HELP,FOLLOW_HELP_in_switchDef761);  
                    stream_HELP.add(HELP17);


                    }
                    break;
                case 8 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:243:48: X
                    {
                    X18=(Token)match(input,X,FOLLOW_X_in_switchDef763);  
                    stream_X.add(X18);


                    }
                    break;
                case 9 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:243:50: XINT
                    {
                    XINT19=(Token)match(input,XINT,FOLLOW_XINT_in_switchDef765);  
                    stream_XINT.add(XINT19);


                    }
                    break;
                case 10 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:243:55: XBATCH
                    {
                    XBATCH20=(Token)match(input,XBATCH,FOLLOW_XBATCH_in_switchDef767);  
                    stream_XBATCH.add(XBATCH20);


                    }
                    break;
                case 11 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:243:62: XCJNI
                    {
                    XCJNI21=(Token)match(input,XCJNI,FOLLOW_XCJNI_in_switchDef769);  
                    stream_XCJNI.add(XCJNI21);


                    }
                    break;
                case 12 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:243:68: XFUTURE
                    {
                    XFUTURE22=(Token)match(input,XFUTURE,FOLLOW_XFUTURE_in_switchDef771);  
                    stream_XFUTURE.add(XFUTURE22);


                    }
                    break;
                case 13 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:243:76: XNOCLSGC
                    {
                    XNOCLSGC23=(Token)match(input,XNOCLSGC,FOLLOW_XNOCLSGC_in_switchDef773);  
                    stream_XNOCLSGC.add(XNOCLSGC23);


                    }
                    break;
                case 14 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:243:85: XINCGC
                    {
                    XINCGC24=(Token)match(input,XINCGC,FOLLOW_XINCGC_in_switchDef775);  
                    stream_XINCGC.add(XINCGC24);


                    }
                    break;
                case 15 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:243:92: XPROF
                    {
                    XPROF25=(Token)match(input,XPROF,FOLLOW_XPROF_in_switchDef777);  
                    stream_XPROF.add(XPROF25);


                    }
                    break;
                case 16 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:243:98: XRS
                    {
                    XRS26=(Token)match(input,XRS,FOLLOW_XRS_in_switchDef779);  
                    stream_XRS.add(XRS26);


                    }
                    break;
                case 17 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:243:102: XSHARE
                    {
                    XSHARE27=(Token)match(input,XSHARE,FOLLOW_XSHARE_in_switchDef781);  
                    stream_XSHARE.add(XSHARE27);


                    }
                    break;
                case 18 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:243:109: JRE_SEARCH
                    {
                    JRE_SEARCH28=(Token)match(input,JRE_SEARCH,FOLLOW_JRE_SEARCH_in_switchDef783);  
                    stream_JRE_SEARCH.add(JRE_SEARCH28);


                    }
                    break;
                case 19 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:243:120: JRE_NO_SEARCH
                    {
                    JRE_NO_SEARCH29=(Token)match(input,JRE_NO_SEARCH,FOLLOW_JRE_NO_SEARCH_in_switchDef785);  
                    stream_JRE_NO_SEARCH.add(JRE_NO_SEARCH29);


                    }
                    break;

            }



            // AST REWRITE
            // elements: 
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 244:3: ->
            {
                adaptor.addChild(root_0, new SwitchNode(((Token)retval.start)));

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "switchDef"

    public static class propertyDef_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "propertyDef"
    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:249:1: propertyDef : CPROP ( '=' Text )? ->;
    public final CommandLineParser.propertyDef_return propertyDef() throws RecognitionException {
        CommandLineParser.propertyDef_return retval = new CommandLineParser.propertyDef_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token CPROP30=null;
        Token char_literal31=null;
        Token Text32=null;

        CommonTree CPROP30_tree=null;
        CommonTree char_literal31_tree=null;
        CommonTree Text32_tree=null;
        RewriteRuleTokenStream stream_Text=new RewriteRuleTokenStream(adaptor,"token Text");
        RewriteRuleTokenStream stream_41=new RewriteRuleTokenStream(adaptor,"token 41");
        RewriteRuleTokenStream stream_CPROP=new RewriteRuleTokenStream(adaptor,"token CPROP");


        	System.out.println("Parsing user property definition");

        try {
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:253:2: ( CPROP ( '=' Text )? ->)
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:253:4: CPROP ( '=' Text )?
            {
            CPROP30=(Token)match(input,CPROP,FOLLOW_CPROP_in_propertyDef813);  
            stream_CPROP.add(CPROP30);

            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:253:10: ( '=' Text )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==41) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:253:11: '=' Text
                    {
                    char_literal31=(Token)match(input,41,FOLLOW_41_in_propertyDef816);  
                    stream_41.add(char_literal31);

                    Text32=(Token)match(input,Text,FOLLOW_Text_in_propertyDef818);  
                    stream_Text.add(Text32);


                    }
                    break;

            }



            // AST REWRITE
            // elements: 
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 253:22: ->
            {
                adaptor.addChild(root_0, new UserPropertyNode(CPROP30, Text32, (CPROP30!=null?CPROP30.getCharPositionInLine():0)));

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "propertyDef"

    protected static class splash_scope {
        String name;
        String value;
        int idx;
    }
    protected Stack splash_stack = new Stack();

    public static class splash_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "splash"
    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:264:1: splash : ( SPLASH | BOOTCP | LOGGC | JAGENT | AGENT ) -> ^() ;
    public final CommandLineParser.splash_return splash() throws RecognitionException {
        splash_stack.push(new splash_scope());
        CommandLineParser.splash_return retval = new CommandLineParser.splash_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SPLASH33=null;
        Token BOOTCP34=null;
        Token LOGGC35=null;
        Token JAGENT36=null;
        Token AGENT37=null;

        CommonTree SPLASH33_tree=null;
        CommonTree BOOTCP34_tree=null;
        CommonTree LOGGC35_tree=null;
        CommonTree JAGENT36_tree=null;
        CommonTree AGENT37_tree=null;
        RewriteRuleTokenStream stream_AGENT=new RewriteRuleTokenStream(adaptor,"token AGENT");
        RewriteRuleTokenStream stream_JAGENT=new RewriteRuleTokenStream(adaptor,"token JAGENT");
        RewriteRuleTokenStream stream_SPLASH=new RewriteRuleTokenStream(adaptor,"token SPLASH");
        RewriteRuleTokenStream stream_LOGGC=new RewriteRuleTokenStream(adaptor,"token LOGGC");
        RewriteRuleTokenStream stream_BOOTCP=new RewriteRuleTokenStream(adaptor,"token BOOTCP");


        	((splash_scope)splash_stack.peek()).name ="";
        	((splash_scope)splash_stack.peek()).value ="";
        	((splash_scope)splash_stack.peek()).idx = -1;

        try {
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:275:2: ( ( SPLASH | BOOTCP | LOGGC | JAGENT | AGENT ) -> ^() )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:275:4: ( SPLASH | BOOTCP | LOGGC | JAGENT | AGENT )
            {
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:275:4: ( SPLASH | BOOTCP | LOGGC | JAGENT | AGENT )
            int alt6=5;
            switch ( input.LA(1) ) {
            case SPLASH:
                {
                alt6=1;
                }
                break;
            case BOOTCP:
                {
                alt6=2;
                }
                break;
            case LOGGC:
                {
                alt6=3;
                }
                break;
            case JAGENT:
                {
                alt6=4;
                }
                break;
            case AGENT:
                {
                alt6=5;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;
            }

            switch (alt6) {
                case 1 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:275:5: SPLASH
                    {
                    SPLASH33=(Token)match(input,SPLASH,FOLLOW_SPLASH_in_splash868);  
                    stream_SPLASH.add(SPLASH33);


                    }
                    break;
                case 2 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:275:12: BOOTCP
                    {
                    BOOTCP34=(Token)match(input,BOOTCP,FOLLOW_BOOTCP_in_splash870);  
                    stream_BOOTCP.add(BOOTCP34);


                    }
                    break;
                case 3 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:275:19: LOGGC
                    {
                    LOGGC35=(Token)match(input,LOGGC,FOLLOW_LOGGC_in_splash872);  
                    stream_LOGGC.add(LOGGC35);


                    }
                    break;
                case 4 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:275:25: JAGENT
                    {
                    JAGENT36=(Token)match(input,JAGENT,FOLLOW_JAGENT_in_splash874);  
                    stream_JAGENT.add(JAGENT36);


                    }
                    break;
                case 5 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:275:32: AGENT
                    {
                    AGENT37=(Token)match(input,AGENT,FOLLOW_AGENT_in_splash876);  
                    stream_AGENT.add(AGENT37);


                    }
                    break;

            }


            		((splash_scope)splash_stack.peek()).idx = ((Token)retval.start).getText().indexOf(':');
            		((splash_scope)splash_stack.peek()).name =((Token)retval.start).getText().substring(0, ((splash_scope)splash_stack.peek()).idx); 
            		((splash_scope)splash_stack.peek()).value =((Token)retval.start).getText().substring(((splash_scope)splash_stack.peek()).idx + 1);
            		


            // AST REWRITE
            // elements: 
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 281:3: -> ^()
            {
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:281:6: ^()
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(new ParametrizedNode(((Token)retval.start), ((splash_scope)splash_stack.peek()).name, ":", ((splash_scope)splash_stack.peek()).value), root_1);

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            splash_stack.pop();
        }
        return retval;
    }
    // $ANTLR end "splash"

    public static class columnSeparatorOpt_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "columnSeparatorOpt"
    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:288:1: columnSeparatorOpt : ( EA | DEA ) ( ':' Text )? ->;
    public final CommandLineParser.columnSeparatorOpt_return columnSeparatorOpt() throws RecognitionException {
        CommandLineParser.columnSeparatorOpt_return retval = new CommandLineParser.columnSeparatorOpt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EA38=null;
        Token DEA39=null;
        Token char_literal40=null;
        Token Text41=null;

        CommonTree EA38_tree=null;
        CommonTree DEA39_tree=null;
        CommonTree char_literal40_tree=null;
        CommonTree Text41_tree=null;
        RewriteRuleTokenStream stream_DEA=new RewriteRuleTokenStream(adaptor,"token DEA");
        RewriteRuleTokenStream stream_Text=new RewriteRuleTokenStream(adaptor,"token Text");
        RewriteRuleTokenStream stream_EA=new RewriteRuleTokenStream(adaptor,"token EA");
        RewriteRuleTokenStream stream_42=new RewriteRuleTokenStream(adaptor,"token 42");

        try {
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:289:2: ( ( EA | DEA ) ( ':' Text )? ->)
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:289:4: ( EA | DEA ) ( ':' Text )?
            {
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:289:4: ( EA | DEA )
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==EA) ) {
                alt7=1;
            }
            else if ( (LA7_0==DEA) ) {
                alt7=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;
            }
            switch (alt7) {
                case 1 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:289:5: EA
                    {
                    EA38=(Token)match(input,EA,FOLLOW_EA_in_columnSeparatorOpt907);  
                    stream_EA.add(EA38);


                    }
                    break;
                case 2 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:289:8: DEA
                    {
                    DEA39=(Token)match(input,DEA,FOLLOW_DEA_in_columnSeparatorOpt909);  
                    stream_DEA.add(DEA39);


                    }
                    break;

            }

            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:289:13: ( ':' Text )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==42) ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:289:14: ':' Text
                    {
                    char_literal40=(Token)match(input,42,FOLLOW_42_in_columnSeparatorOpt913);  
                    stream_42.add(char_literal40);

                    Text41=(Token)match(input,Text,FOLLOW_Text_in_columnSeparatorOpt915);  
                    stream_Text.add(Text41);


                    }
                    break;

            }



            // AST REWRITE
            // elements: 
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 289:25: ->
            {
                adaptor.addChild(root_0, new ParametrizedNode(((Token)retval.start), ":", Text41));

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "columnSeparatorOpt"

    public static class continuous_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "continuous"
    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:292:1: continuous : ( MEMS | MEMX | SS ) ->;
    public final CommandLineParser.continuous_return continuous() throws RecognitionException {
        CommandLineParser.continuous_return retval = new CommandLineParser.continuous_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token MEMS42=null;
        Token MEMX43=null;
        Token SS44=null;

        CommonTree MEMS42_tree=null;
        CommonTree MEMX43_tree=null;
        CommonTree SS44_tree=null;
        RewriteRuleTokenStream stream_SS=new RewriteRuleTokenStream(adaptor,"token SS");
        RewriteRuleTokenStream stream_MEMS=new RewriteRuleTokenStream(adaptor,"token MEMS");
        RewriteRuleTokenStream stream_MEMX=new RewriteRuleTokenStream(adaptor,"token MEMX");

        try {
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:293:2: ( ( MEMS | MEMX | SS ) ->)
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:293:4: ( MEMS | MEMX | SS )
            {
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:293:4: ( MEMS | MEMX | SS )
            int alt9=3;
            switch ( input.LA(1) ) {
            case MEMS:
                {
                alt9=1;
                }
                break;
            case MEMX:
                {
                alt9=2;
                }
                break;
            case SS:
                {
                alt9=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;
            }

            switch (alt9) {
                case 1 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:293:5: MEMS
                    {
                    MEMS42=(Token)match(input,MEMS,FOLLOW_MEMS_in_continuous934);  
                    stream_MEMS.add(MEMS42);


                    }
                    break;
                case 2 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:293:10: MEMX
                    {
                    MEMX43=(Token)match(input,MEMX,FOLLOW_MEMX_in_continuous936);  
                    stream_MEMX.add(MEMX43);


                    }
                    break;
                case 3 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:293:15: SS
                    {
                    SS44=(Token)match(input,SS,FOLLOW_SS_in_continuous938);  
                    stream_SS.add(SS44);


                    }
                    break;

            }



            // AST REWRITE
            // elements: 
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 293:19: ->
            {
                adaptor.addChild(root_0, new ParametrizedNode(((Token)retval.start), 3));

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "continuous"

    protected static class version_scope {
        boolean simple;
        String versionText;
    }
    protected Stack version_stack = new Stack();

    public static class version_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "version"
    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:296:1: version : VERSION -> {$version::simple}? ->;
    public final CommandLineParser.version_return version() throws RecognitionException {
        version_stack.push(new version_scope());
        CommandLineParser.version_return retval = new CommandLineParser.version_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token VERSION45=null;

        CommonTree VERSION45_tree=null;
        RewriteRuleTokenStream stream_VERSION=new RewriteRuleTokenStream(adaptor,"token VERSION");

        try {
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:301:1: ( VERSION -> {$version::simple}? ->)
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:301:3: VERSION
            {
            VERSION45=(Token)match(input,VERSION,FOLLOW_VERSION_in_version959);  
            stream_VERSION.add(VERSION45);


            	((version_scope)version_stack.peek()).simple = !VERSION45.getText().contains(":");	
            	


            // AST REWRITE
            // elements: 
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 305:2: -> {$version::simple}?
            if (((version_scope)version_stack.peek()).simple) {
                adaptor.addChild(root_0, new SwitchNode(VERSION45));

            }
            else // 306:2: ->
            {
                adaptor.addChild(root_0, new ParametrizedNode(VERSION45, "version", ":", VERSION45.getText().substring(8)));

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            version_stack.pop();
        }
        return retval;
    }
    // $ANTLR end "version"

    public static class loosedParameter_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "loosedParameter"
    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:309:1: loosedParameter : CP Text ->;
    public final CommandLineParser.loosedParameter_return loosedParameter() throws RecognitionException {
        CommandLineParser.loosedParameter_return retval = new CommandLineParser.loosedParameter_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token CP46=null;
        Token Text47=null;

        CommonTree CP46_tree=null;
        CommonTree Text47_tree=null;
        RewriteRuleTokenStream stream_Text=new RewriteRuleTokenStream(adaptor,"token Text");
        RewriteRuleTokenStream stream_CP=new RewriteRuleTokenStream(adaptor,"token CP");

        try {
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:310:2: ( CP Text ->)
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:310:4: CP Text
            {
            CP46=(Token)match(input,CP,FOLLOW_CP_in_loosedParameter988);  
            stream_CP.add(CP46);

            Text47=(Token)match(input,Text,FOLLOW_Text_in_loosedParameter990);  
            stream_Text.add(Text47);



            // AST REWRITE
            // elements: 
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 310:12: ->
            {
                adaptor.addChild(root_0, new ParametrizedNode(CP46, " ", Text47, false));

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "loosedParameter"

    public static class classpath_return extends ParserRuleReturnScope {
        public String cls;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "classpath"
    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:314:1: classpath returns [String cls] : l= Letter (l= Letter | '-' | ':' )* ;
    public final CommandLineParser.classpath_return classpath() throws RecognitionException {
        CommandLineParser.classpath_return retval = new CommandLineParser.classpath_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token l=null;
        Token char_literal48=null;
        Token char_literal49=null;

        CommonTree l_tree=null;
        CommonTree char_literal48_tree=null;
        CommonTree char_literal49_tree=null;

         
        	retval.cls = "";	

        try {
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:319:2: (l= Letter (l= Letter | '-' | ':' )* )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:319:4: l= Letter (l= Letter | '-' | ':' )*
            {
            root_0 = (CommonTree)adaptor.nil();

            l=(Token)match(input,Letter,FOLLOW_Letter_in_classpath1020); 
            l_tree = (CommonTree)adaptor.create(l);
            adaptor.addChild(root_0, l_tree);

            retval.cls += (l!=null?l.getText():null);
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:320:3: (l= Letter | '-' | ':' )*
            loop10:
            do {
                int alt10=4;
                switch ( input.LA(1) ) {
                case Letter:
                    {
                    alt10=1;
                    }
                    break;
                case 40:
                    {
                    alt10=2;
                    }
                    break;
                case 42:
                    {
                    alt10=3;
                    }
                    break;

                }

                switch (alt10) {
            	case 1 :
            	    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:320:4: l= Letter
            	    {
            	    l=(Token)match(input,Letter,FOLLOW_Letter_in_classpath1030); 
            	    l_tree = (CommonTree)adaptor.create(l);
            	    adaptor.addChild(root_0, l_tree);

            	    retval.cls += (l!=null?l.getText():null);

            	    }
            	    break;
            	case 2 :
            	    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:321:6: '-'
            	    {
            	    char_literal48=(Token)match(input,40,FOLLOW_40_in_classpath1039); 
            	    char_literal48_tree = (CommonTree)adaptor.create(char_literal48);
            	    adaptor.addChild(root_0, char_literal48_tree);

            	    retval.cls += "-";

            	    }
            	    break;
            	case 3 :
            	    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:322:6: ':'
            	    {
            	    char_literal49=(Token)match(input,42,FOLLOW_42_in_classpath1048); 
            	    char_literal49_tree = (CommonTree)adaptor.create(char_literal49);
            	    adaptor.addChild(root_0, char_literal49_tree);

            	    retval.cls += ":";

            	    }
            	    break;

            	default :
            	    break loop10;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "classpath"

    public static class looseTextualNode_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "looseTextualNode"
    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:327:1: looseTextualNode[boolean unresolved] : Text -> {$unresolved}? ->;
    public final CommandLineParser.looseTextualNode_return looseTextualNode(boolean unresolved) throws RecognitionException {
        CommandLineParser.looseTextualNode_return retval = new CommandLineParser.looseTextualNode_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token Text50=null;

        CommonTree Text50_tree=null;
        RewriteRuleTokenStream stream_Text=new RewriteRuleTokenStream(adaptor,"token Text");

        try {
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:328:2: ( Text -> {$unresolved}? ->)
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:329:3: Text
            {
            Text50=(Token)match(input,Text,FOLLOW_Text_in_looseTextualNode1072);  
            stream_Text.add(Text50);



            // AST REWRITE
            // elements: 
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 330:3: -> {$unresolved}?
            if (unresolved) {
                adaptor.addChild(root_0, new UnrecognizedOption(((Token)retval.start)));

            }
            else // 331:3: ->
            {
                adaptor.addChild(root_0, new UnknownOption(((Token)retval.start)));

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "looseTextualNode"

    // Delegated rules


 

    public static final BitSet FOLLOW_option_in_vmOptions700 = new BitSet(new long[]{0x0000010000000402L});
    public static final BitSet FOLLOW_40_in_option712 = new BitSet(new long[]{0x0000017CFDFFFFE0L});
    public static final BitSet FOLLOW_switchDef_in_option716 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_splash_in_option718 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_continuous_in_option720 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_version_in_option722 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_loosedParameter_in_option724 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_propertyDef_in_option726 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_looseTextualNode_in_option728 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_looseTextualNode_in_option734 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SERVER_in_switchDef749 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CLIENT_in_switchDef751 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ESA_in_switchDef753 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DSA_in_switchDef755 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VERBOSE_in_switchDef757 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SVERION_in_switchDef759 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HELP_in_switchDef761 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_X_in_switchDef763 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_XINT_in_switchDef765 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_XBATCH_in_switchDef767 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_XCJNI_in_switchDef769 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_XFUTURE_in_switchDef771 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_XNOCLSGC_in_switchDef773 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_XINCGC_in_switchDef775 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_XPROF_in_switchDef777 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_XRS_in_switchDef779 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_XSHARE_in_switchDef781 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_JRE_SEARCH_in_switchDef783 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_JRE_NO_SEARCH_in_switchDef785 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CPROP_in_propertyDef813 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_41_in_propertyDef816 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_Text_in_propertyDef818 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SPLASH_in_splash868 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BOOTCP_in_splash870 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LOGGC_in_splash872 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_JAGENT_in_splash874 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AGENT_in_splash876 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EA_in_columnSeparatorOpt907 = new BitSet(new long[]{0x0000040000000002L});
    public static final BitSet FOLLOW_DEA_in_columnSeparatorOpt909 = new BitSet(new long[]{0x0000040000000002L});
    public static final BitSet FOLLOW_42_in_columnSeparatorOpt913 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_Text_in_columnSeparatorOpt915 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MEMS_in_continuous934 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MEMX_in_continuous936 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SS_in_continuous938 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VERSION_in_version959 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CP_in_loosedParameter988 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_Text_in_loosedParameter990 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Letter_in_classpath1020 = new BitSet(new long[]{0x0000058000000002L});
    public static final BitSet FOLLOW_Letter_in_classpath1030 = new BitSet(new long[]{0x0000058000000002L});
    public static final BitSet FOLLOW_40_in_classpath1039 = new BitSet(new long[]{0x0000058000000002L});
    public static final BitSet FOLLOW_42_in_classpath1048 = new BitSet(new long[]{0x0000058000000002L});
    public static final BitSet FOLLOW_Text_in_looseTextualNode1072 = new BitSet(new long[]{0x0000000000000002L});

}
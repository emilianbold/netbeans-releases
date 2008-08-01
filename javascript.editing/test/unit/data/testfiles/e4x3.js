var x = <f={}/>;
        var breakpointSetResponse =
            <response command="breakpoint_set"
        state={ disabled ? "disabled" : "enabled"}
        id={href + ":" + line}
        transaction_id={transaction_id} />;

// ( From issue http://www.netbeans.org/issues/show_bug.cgi?id=138529 )


static void local_enumerators_foo() {
    enum Action { SEND, RECEIVE, DELETE } ;
    enum Action action = SEND; // SEND is absent in completion list
    action = RECEIVE; // no RECEIVE in completion
}
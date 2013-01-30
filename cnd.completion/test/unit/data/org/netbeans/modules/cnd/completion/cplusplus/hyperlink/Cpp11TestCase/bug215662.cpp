int bug215662() {
    vector<TypeDeclOwnerRef> typeDeclarations;
    std::for_each( typeDeclarations.begin() , typeDeclarations.end()
            , [&] (const TypeDeclOwnerRef& tyDecl)
            {
                //both structTypePtr and tyDecl show as unable to resolveidentifier
                auto structTypePtr34 = Compiler::SymbolResolver::resolveOrCreateModuleType( tyDecl.get() );
            } );
    return 0;
} 
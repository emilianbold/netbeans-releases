struct bug141302_A <%
    int i;
%>;

int bug141302_main(int argc, char** argv) <%
    bug141302_A a<:5:>;

    a[0].i;

    return 0;
%>

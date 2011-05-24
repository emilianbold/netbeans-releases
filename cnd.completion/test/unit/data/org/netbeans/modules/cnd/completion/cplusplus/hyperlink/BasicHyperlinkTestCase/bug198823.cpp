class bug198823_A {
public:
    int accept(int i) {
        return i;
    }
};

class bug198823_tcp
{
public:
  /// The type of a TCP endpoint.
  typedef basic_endpoint<bug198823_tcp> endpoint;
};

int bug198823_main(int argc, char** argv) {
    bug198823_A a(argc, bug198823_tcp::endpoint(tcp::v4(), port));
    a.accept(1);

    return 0;
}
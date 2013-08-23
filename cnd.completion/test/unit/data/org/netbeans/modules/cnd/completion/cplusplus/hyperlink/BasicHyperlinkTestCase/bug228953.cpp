namespace bug228953 {
  void bug228953_foo() {
      static void *bug228953_labels[2] = {
          &&bug228953__unknown_opcode
      };
  bug228953__unknown_opcode:
      goto *bug228953_labels[0];
  }
}
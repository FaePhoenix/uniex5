package fae.FSUGenBank;

public class IllegalGenBankFormat extends RuntimeException{

    public IllegalGenBankFormat() {
        super();
    }

    public IllegalGenBankFormat(String msg) {
        super(msg);
    }
}

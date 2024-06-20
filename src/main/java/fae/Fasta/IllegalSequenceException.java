package fae.Fasta;

public class IllegalSequenceException extends RuntimeException{
    
    public IllegalSequenceException() {
        super();
    }

    public IllegalSequenceException(String message) {
        super(message);
    }
}

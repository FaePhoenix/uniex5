package fae.Helper;

import java.util.HashMap;


public class Sequencer {

    private HashMap<String, String> translation;
    private HashMap<String, String> complementoryDNA;
    private HashMap<String, String> DNAtoRNA;
    
    public Sequencer(){
        this.translation = new HashMap<String, String>();

        //First character U
        this.translation.put("UUU", "F");
        this.translation.put("UUC", "F");
        this.translation.put("UUA", "L");
        this.translation.put("UUG", "L");

        this.translation.put("UCU", "S");
        this.translation.put("UCC", "S");
        this.translation.put("UCA", "S");
        this.translation.put("UCG", "S");

        this.translation.put("UAU", "Y");
        this.translation.put("UAC", "Y");
        this.translation.put("UAA", "Ochre"); //Stop-codon
        this.translation.put("UAG", "Amber"); //Stop-codon

        this.translation.put("UGU", "C");
        this.translation.put("UGC", "C");
        this.translation.put("UGA", "Opal"); //Stop-codon
        this.translation.put("UGG", "W");

        //First character C
        this.translation.put("CUU", "L");
        this.translation.put("CUC", "L");
        this.translation.put("CUA", "L");
        this.translation.put("CUG", "L");

        this.translation.put("CCU", "P");
        this.translation.put("CCC", "P");
        this.translation.put("CCA", "P");
        this.translation.put("CCG", "P");

        this.translation.put("CAU", "H");
        this.translation.put("CAC", "H");
        this.translation.put("CAA", "Q");
        this.translation.put("CAG", "Q");

        this.translation.put("CGU", "R");
        this.translation.put("CGC", "R");
        this.translation.put("CGA", "R");
        this.translation.put("CGG", "R");

        //First character A
        this.translation.put("AUU", "I");
        this.translation.put("AUC", "I");
        this.translation.put("AUA", "I");
        this.translation.put("AUG", "M"); //start-codon

        this.translation.put("ACU", "T");
        this.translation.put("ACC", "T");
        this.translation.put("ACA", "T");
        this.translation.put("ACG", "T");

        this.translation.put("AAU", "N");
        this.translation.put("AAC", "N");
        this.translation.put("AAA", "K");
        this.translation.put("AAG", "K");

        this.translation.put("AGU", "S");
        this.translation.put("AGC", "S");
        this.translation.put("AGA", "R");
        this.translation.put("AGG", "R");

        //First character G
        this.translation.put("GUU", "V");
        this.translation.put("GUC", "V");
        this.translation.put("GUA", "V");
        this.translation.put("GUG", "V");

        this.translation.put("GCu", "A");
        this.translation.put("GCC", "A");
        this.translation.put("GCA", "A");
        this.translation.put("GCG", "A");

        this.translation.put("GAU", "D");
        this.translation.put("GAC", "D");
        this.translation.put("GAA", "E");
        this.translation.put("GAG", "E");

        this.translation.put("GGU", "G");
        this.translation.put("GGC", "G");
        this.translation.put("GGA", "G");
        this.translation.put("GGG", "G");


        this.complementoryDNA = new HashMap<String, String>();

        this.complementoryDNA.put("T", "A");
        this.complementoryDNA.put("C", "G");
        this.complementoryDNA.put("A", "T");
        this.complementoryDNA.put("G", "C");

        this.DNAtoRNA = new HashMap<String, String>();

        this.DNAtoRNA.put("A", "U");
        this.DNAtoRNA.put("C", "G");
        this.DNAtoRNA.put("G", "C");
        this.DNAtoRNA.put("T", "A");
    }

    public String reverseSequence(String sequence){
        StringBuilder reverser = new StringBuilder(sequence);

        return reverser.reverse().toString();
    }

    public String buildComplementaryStrand(String sequence){
        StringBuilder complementoryStrand = new StringBuilder();

        for (char c : sequence.toCharArray()) {
            complementoryStrand.append(this.complementoryDNA.get(String.valueOf(c)));
        }

        return complementoryStrand.toString();
    }

    public String mutateDNA(String sequence) {
        String firstChar = String.valueOf(sequence.charAt(0));
        StringBuilder mutated = new StringBuilder(sequence);

        if (firstChar.equals("A")) {
            mutated.setCharAt(0, 'T');
            return mutated.toString();
        } else {
            mutated.setCharAt(0, 'A');
            return mutated.toString();
        }
    }

    public String transcribe(String DNA) {
        StringBuilder RNA = new StringBuilder();

        for (char c : DNA.toCharArray()) {
            RNA.append(this.DNAtoRNA.get(String.valueOf(c)));
        }

        return RNA.toString();
    }

    public String translate(String RNA) {
        StringBuilder aminoAcidChain = new StringBuilder();
        int peptideLength = (int) Math.floor(RNA.length()/3);

        for (int i = 0; i < peptideLength; i++ ) {
            String triplet = RNA.substring(i, i + 3);
            aminoAcidChain.append(this.translation.get(triplet));
        }

        return aminoAcidChain.toString();
    }

}

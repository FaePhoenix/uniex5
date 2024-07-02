package fae.Helper;

public class Levenshtein {
    private int insertionCost;
    private int deletionCost;
    private int missMatchCost;

    public Levenshtein() {
        this.insertionCost = 1;
        this.deletionCost = 1;
        this.missMatchCost = 2;
    }

    public int calcdistance(String seq1, String seq2){


        //create table
        int seq1size = seq1.length();
        int seq2size = seq2.length();
        int[][] table= new int[seq1size + 1][seq2size + 1];

        //set boundaries
        table[0][0] = 0;

        for (int idx = 1; idx < seq1size + 1; idx++) {
            table[idx][0] = idx;
        }

        for (int idx = 1; idx < seq2size + 1; idx++) {
            table[0][idx] = idx;
        }


        for (int idx1 = 1; idx1 < seq1size + 1; idx1++) {
            for (int idx2 = 1; idx2 < seq2size + 1; idx2++) {
                if (seq1.charAt(idx1 - 1) == seq2.charAt(idx2 - 1)) {
                    table[idx1][idx2] = table[idx1 - 1][idx2 - 1];
                } else {
                    int helper = Math.min(table[idx1 - 1][idx2] +this.deletionCost, table[idx1][idx2 - 1] + this.insertionCost);
                    table[idx1][idx2] =  Math.min(helper, table[idx1 - 1][idx2 - 1] + this.missMatchCost);
                }
            }
        }

        return table[seq1size][seq2size];
    }
}

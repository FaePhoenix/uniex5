package fae.Fasta;

import java.util.ArrayList;
import java.util.Arrays;
import org.json.JSONArray;
import org.json.JSONObject;

import fae.Helper.FileHelper;


public class Fasta {
    private String header;
    private ArrayList<String> comments;
    private String dnaSequence;


    public Fasta() {
        header = "";
        comments = new ArrayList<String>();
        dnaSequence = "";
    }

    public Fasta(JSONObject fasta) {
        this.header = fasta.getString("header");

        this.dnaSequence = fasta.getString("sequence");
        
        JSONArray commentsExtract = fasta.getJSONArray("comments");
        this.comments = new ArrayList<String>();
        for (int idx = 0; idx < commentsExtract.length(); idx ++){
            this.comments.add(commentsExtract.getString(idx));
        }

        

    }

    public Fasta(String description, ArrayList<String> commentCollection, String sequence) throws IllegalHeaderException, IllegalCommentException, IllegalSequenceException{
        this.setHeader(description);
        this.setComments(commentCollection);
        this.setDnaSequence(sequence);
    }


    public Fasta(String filename) {

        //Get content from file
        FileHelper helper = new FileHelper(filename);
        ArrayList<String> content = helper.getContent();
        
        //Get Header
        String readHeader = content.get(0);

        //Ende der Kommentarzeilen finden
        int commentsEnd = -1;
        for (int idx = content.size() - 1; idx >= 0; idx--) {
            if (content.get(idx).startsWith(";")) {
                commentsEnd = idx;
                break;
            }
        }

        //Kommentare in Komponente lesen
        ArrayList<String> readCommentCollection = new ArrayList<String>();
        if (commentsEnd != -1) {
            for (int idx = 1; idx <= commentsEnd; idx++){
                readCommentCollection.add(content.get(idx));
            }
        }
        
        //Restliche File ins Sequenz-Fragments-Array packen
        ArrayList<String> sequenceFragments = new ArrayList<String>(content.subList(commentsEnd + 1, content.size()));
    

        //Sequenz-Start finden und SequenceFragments slicen
        int sequenceStart = sequenceFragments.size() - 1;
        for (int idx = sequenceFragments.size() - 1; idx >= 0; idx--) {
            String line = sequenceFragments.get(idx);
            if (!line.matches("^[AGCT]*$")){
                break;
            }
            sequenceStart = idx;
        }
        sequenceFragments = new ArrayList<String>(sequenceFragments.subList(sequenceStart, sequenceFragments.size()));

        //Sequenz-Komponente joinen
        String readSequence = ""; 
        readSequence = String.join("", sequenceFragments);

        try {setHeader(readHeader);}
        catch(IllegalHeaderException exception) {
            System.out.println("Aborting setHeader because Header did not match expected format.\nSetting empty Header instead");
            this.header = "";
        }

        try {setComments(readCommentCollection);}
        catch (IllegalCommentException exception) {
            System.out.println("Aborting setComments because all comments did not match expected format.\nSetting empty comments instead");
            this.comments = new ArrayList<String>();
        }

        try {setDnaSequence(readSequence);}
        catch (IllegalSequenceException exception) {
            System.out.println("Aborting setHeader because Header did not match expected format.\nSetting empty sequence instead");
            this.dnaSequence = "";
        }
    }


    public Fasta(Fasta fastaToCopy) {
        this.header = fastaToCopy.getHeader();
        this.comments = fastaToCopy.getComments();
        this.dnaSequence = fastaToCopy.getDnaSequence();
    }


    public void setEmpty() {
        this.header = "";
        this.comments = new ArrayList<String>();
        this.dnaSequence = "";
    }


    public String getHeader() {return this.header;}


    public ArrayList<String> getComments() {return this.comments;}


    public String getDnaSequence() {return this.dnaSequence;}


    public void setHeader(String headerTry) throws IllegalHeaderException {
        if (headerTry.matches("^>.*")) {
            this.header = headerTry;

        } else {
            throw new IllegalHeaderException("Given Header does not conform to Fasta-Header format.");
        }
    }


    public void setComments(ArrayList<String> commentsTry) throws IllegalCommentException {
        ArrayList<String> acceptedComments = new ArrayList<String>();

        if (commentsTry.size() == 0) {
            return;
        }
        
        for (String comment : commentsTry) {

            if (comment.matches("^;.*")) {
                acceptedComments.add(comment);

            } else {
                System.out.println("Given comment: \n\"" + comment + "\"\ndoes not conform to Fasta-Comment format.");
            }
        }
        if (acceptedComments.size() > 0 ) {
            this.comments = acceptedComments;
        
        } else {
            throw new IllegalCommentException("No comment given conforms to Fasta-Comment format.");
        }

    }


    public void setDnaSequence(String sequenceTry) throws IllegalSequenceException {
        if (sequenceTry.matches("^[AGCT]*$") | sequenceTry.matches("^[AGCU]*$") ) {
            this.dnaSequence = sequenceTry;
        } else {
            System.out.println(sequenceTry);
            throw new IllegalSequenceException("Given Sequence does not conform to Fasta-Sequence format.");
        }
    }


    public int getSequenceSize() {return this.dnaSequence.length();}


    public void sequenceComparison (Fasta comparingSequence) {

        String seq1 = this.dnaSequence, seq2 = comparingSequence.getDnaSequence();
        int seq1Size = this.getSequenceSize(), seq2Size = comparingSequence.getSequenceSize();

        Boolean[][] comparisonMatrix = new Boolean[seq2Size][seq1Size];

        for (int i = 0; i < seq1Size; i++) {

            for (int j = 0; j < seq2Size; j++) {
                Boolean value = seq1.charAt(i) == seq2.charAt(j);
                //System.out.println("Comparing " + seq1.charAt(i) + " and " + seq2.charAt(j) + "\nFound: " + value);
                comparisonMatrix[j][i] = value;
            }
        }

        System.out.println(" " + seq1);
        for (int idx = 0; idx < seq2Size; idx++) {
            Boolean[] line = comparisonMatrix[idx];
            String convertedLine = String.valueOf(seq2.charAt(idx));
            
            for (Boolean entry : line) {
                String charToAdd = (entry) ? "*" : " ";
                convertedLine += charToAdd;
            }
            System.out.println(convertedLine);
        }

    }


    public Boolean saveToFile(String fileName) {
        //Prepare Content
        String content = this.toStr();
        ArrayList<String> toWrite = new ArrayList<String> (Arrays.asList(content.split("\n")));

        //Helper Class writes to File
        FileHelper helper = new FileHelper(toWrite);
        return helper.saveToFile(fileName);
    }


    public String toStr() {
        String repr = "";

        repr += this.getHeader() + "\n";

        if (this.getComments() != null) {
            for (String comment : this.getComments()) {
                repr += comment + "\n";
            }
        }

        int seqSize = this.getSequenceSize();
        for (int idx = 0; idx < seqSize; idx += 80) {
            if (idx + 80 < seqSize) {
                repr += this.getDnaSequence().substring(idx, idx + 80) + "\n";
            } else {
                repr += this.getDnaSequence().substring(idx, seqSize);
            }
        } 
        return repr; 
    }

    public JSONObject toJSON(){
        JSONObject rep = new JSONObject();

        rep.put("header", this.getHeader());
        rep.put("comments",this.getComments());
        rep.put("sequence",this.getDnaSequence());

        return rep;
    }
}
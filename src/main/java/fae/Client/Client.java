package fae.Client;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

import fae.FSUGenBank.FSUGenBank;
import fae.Fasta.Fasta;
import fae.Helper.FileHelper;
import fae.Helper.ObjectParser;
import fae.Helper.RequestBuilder;
import fae.Helper.Sequencer;



public class Client{

    private User user;
    private Socket socket;
    private BufferedReader userInput;
    private DataInputStream in;
    private DataOutputStream out;


    public Client() throws IOException{
        this.userInput = new BufferedReader(new InputStreamReader(System.in));
    }


    public Boolean connect(int port) throws IOException {

        //Build helpers
        RequestBuilder protocolBuilder = new RequestBuilder();
        ObjectParser inStreamHelper = new ObjectParser();

        //Build socket connection and data-streams
        this.socket = new Socket("localhost", port);
        this.in = new DataInputStream(this.socket.getInputStream());
        this.out = new DataOutputStream(this.socket.getOutputStream());

        //Get username from user and send server greeting
        String username = getMailFromUser();
        JSONObject greeting = protocolBuilder.buildFirstContactProtocol(username);
        out.writeUTF(greeting.toString());

        //Get password from user and send to server
        String password = getPasswordFromUser();
        JSONObject pwdConfirmation = protocolBuilder.buildPasswordConfirmationProtocol(password);
        out.writeUTF(pwdConfirmation.toString());

        //Check success of handshake
        JSONObject serverResponse = inStreamHelper.handleInput(this.in, "authenticate_response");
        Boolean success = serverResponse.getJSONObject("protocol_body").getBoolean("handshake_status");
        if(!success) {
            System.out.println("Pasword wrong. Terminating");
            return false;
        }

        //Setting password
        System.out.println("Client-Success for: " + this.user.getUsername());
        this.user.setPassword(password);
        return true;
    }

    private String getMailFromUser() {

        //Get input from user
        System.out.println("Please enter your email-adress");
        String username;
        try {
            username = this.userInput.readLine();
        } catch (Exception e) {
           System.out.println("Failed to read terminal input. Please try again:");
           username = getMailFromUser();
        }

        //validate input
        if (! username.matches("^((?!\\.)[\\w\\-_.]*[^.])(@\\w+)(\\.\\w+(\\.\\w+)?[^.\\W])$")){
            System.out.println("The email you entered was invalid. Please try again:");
            username = getMailFromUser();
        }

        //set username and return
        this.user = new User(username);
        return username;
    }

    private String getPasswordFromUser(){

        //Get input from user
        System.out.println("Please enter the your password.");
        System.out.println("If you don't have a password yet, you will recieve an email:");
        String password;
        try {
            password = this.userInput.readLine();
        } catch (IOException e) {
            System.out.println("Failed to read terminal input. Please try again:");
            password = getPasswordFromUser();
        }

        return password;
    }


    public void run() throws IOException{
        Boolean alive = true;
        while(alive){
            System.out.println("Please Input your desired action:");
            System.out.println("S (send data); R (request data); C (change password); D (dotplot); F (flip sequence); T (transcribe DNA to RNA);M (mutate Sequence); B (get complementary sequence); A (translate RNA to peptideChain); L (Levenshtein comparison); E (end connection)");
            String userAction = this.userInput.readLine();
            switch(userAction){
                case "S":
                    this.sendData();
                    System.out.println("Successfully send data to server");
                    break;

                case "R":
                    this.handleRequestData();
                    break;

                case "E":
                    this.endConnection();
                    System.out.println("Ended Socket-Connection");
                    alive = false;
                    break;

                case "C":
                    this.changePassword();
                    break;
                
                case "D":
                    this.handleDotPlot();
                    break;

                case "F": 
                    this.flipSequence();
                    break;

                case "T":
                    this.transcribeDNA();
                    break;
                
                case "M":
                    this.mutate(); 
                    break;

                case "B":
                    this.generateComplementarySequence();
                    break;

                case "A":
                    this.translateSequencetoAminoAcids();
                    break;

                case "L":
                    this.levenshteinComp();
                    break;

                default:
                    System.out.println("Could not interpret Input. Please select an available action (S;R;C;D;F;T;M;B;A;L;E)");
                    break;
            }

        }
    }


    private void levenshteinComp() throws IOException {

        // Build helpers
        RequestBuilder protocolBuilder = new RequestBuilder();

        //getFasta
        System.out.println("Please select a fasta File you want to server to compare to known sequences");
        String filename = getValidFilenameFromUser();
        Fasta extrFasta = new Fasta(filename);

        //send fasta to server
        JSONObject sendFastaProtocol = protocolBuilder.buildSendFastaProtocol(extrFasta.getDnaSequence());
        try {
            this.out.writeUTF(sendFastaProtocol.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //get server repsonses
        ArrayList<JSONObject> serverResponses = recieveLevResponse();
    
        if (serverResponses.size() == 0) {
            System.out.println("Server has no entries to compare sent sequence with. Aborting");
            return;
        }

        for (int idx = 0; idx < serverResponses.size(); idx++) {
            JSONObject response = serverResponses.get(idx);
            int distance = response.getInt("distance");
            String name = response.getString("name");
            String sequence = response.getString("sequence");

            System.out.println("Candidate " + idx+1 + ": " + name);
            System.out.println("Found distance of " + distance);
            System.out.println("Printing dotplot:");

            Fasta sent = new Fasta(name, new ArrayList<String>(), sequence);
            extrFasta.sequenceComparison(sent);

            System.out.println("\n\n");

        }

        System.out.println("finished printing dotplots");
    }

    private ArrayList<JSONObject> recieveLevResponse() throws IOException {

        //build helper
        ObjectParser inStreamHelper = new ObjectParser();

        //initialize
        ArrayList<JSONObject> responses = new ArrayList<JSONObject>();
        String latest_type = "";

        while (!latest_type.equals("send_fasta_response_done")) {
            JSONObject response = inStreamHelper.handleRequest(this.in);
            latest_type = response.getString("protocol_type");

            if(latest_type.equals("send_fasta_response")){
                responses.add(response);
            }
        }

        return responses;
    }


    private void mutate() {
        // build helper
        Sequencer seqHelper = new Sequencer();

        // get file from user
        System.out.println("Please select a fasta File you want to mutate");
        String fileName = getValidFilenameFromUser();
        Fasta givenFasta = new Fasta(fileName);

        String mutSeq = seqHelper.mutateDNA(givenFasta.getDnaSequence());
        Fasta mutFasta = new Fasta(givenFasta.getHeader() + "mutated", givenFasta.getComments(), mutSeq);

        String newFilename = fileName.replaceAll(".txt", "") + "_mutated.txt";
        mutFasta.saveToFile(newFilename);
        System.out.println("Successfully build mutated sequence and saved as: " + newFilename);
    }


    private void translateSequencetoAminoAcids() {
        
        // build helper
        Sequencer seqHelper = new Sequencer();
        
        // get file from user
        System.out.println("Please select a fasta File you want to translate into an peptide chain");
        System.out.println("Note that this only works with RNA sequences and not DNA");
        String fileName = getValidFilenameFromUser();
        Fasta givenFasta = new Fasta(fileName);

    
        //Check if given sequence is RNA
        String sequence = givenFasta.getDnaSequence();
        if (! sequence.matches("[ACGU]*")) {
            System.out.println("Did not get RNA sequence. Aborting");
            return;
        }

        String peptideChain = seqHelper.translate(sequence);
        String newFilename = "txtfiles/" + givenFasta.getHeader() + "_translated.txt";

        ArrayList<String> content = new ArrayList<String>();
        content.add(peptideChain);

        FileHelper helper = new FileHelper(content);
        helper.saveToFile(newFilename);
        System.out.println("Successfully translated RNA sequence to peptide Chain and saved to: " + newFilename);
    }


    private void generateComplementarySequence() {
        
        // build helper
        Sequencer seqHelper = new Sequencer();

        // get file from user
        System.out.println("Please select a fasta File you want to create the complementary sequence of");
        System.out.println("Note that this only works with DNA sequences and not RNA");
        String fileName = getValidFilenameFromUser();
        Fasta givenFasta = new Fasta(fileName);

        //Check if given sequence is DNA
        String sequence = givenFasta.getDnaSequence();
        if (! sequence.matches("[ACGT]*")) {
            System.out.println("Did not get DNA sequence. Aborting");
            return;
        }
        
        String complSeq = seqHelper.buildComplementaryStrand(sequence);
        Fasta complFasta = new Fasta(givenFasta.getHeader() + "complementary", givenFasta.getComments(), complSeq);
        
        String newFilename = fileName.replaceAll(".txt", "") + "_complementary.txt";
        complFasta.saveToFile(newFilename);
        System.out.println("Successfully build complementary sequence and saved as: " + newFilename);
    }


    private void transcribeDNA() {
 
        // build helper
        Sequencer seqHelper = new Sequencer();
 
        // get file from user
        System.out.println("Please select a fasta File you want to transcribe");
        System.out.println("Note that this only works with DNA sequences and not RNA");
        String fileName = getValidFilenameFromUser();
        Fasta givenFasta = new Fasta(fileName);
 
        //Check if given sequence is DNA
        String sequence = givenFasta.getDnaSequence();
        if (! sequence.matches("[ACGT]*")) {
            System.out.println("Did not get DNA sequence. Aborting");
            return;
        }

        String RNA = seqHelper.transcribe(sequence);
        Fasta transcFasta = new Fasta(givenFasta.getHeader() + "transcribed", givenFasta.getComments(), RNA);

        String newFilename = fileName.replaceAll(".txt", "") + "_transcribed.txt";
        transcFasta.saveToFile(newFilename);
        System.out.println("Successfully build complementary sequence and saved as: " + newFilename);
    }


    private void flipSequence() {

        // build helper
        Sequencer seqHelper = new Sequencer();

        // get file from user
        System.out.println("Please select a fasta File you want to reverse the sequence of");
        String fileName = getValidFilenameFromUser();
        Fasta givenFasta = new Fasta(fileName);

        // generate Fasta w/ reverse sequence
        String sequence = givenFasta.getDnaSequence();
        String revSeq = seqHelper.reverseSequence(sequence);
        Fasta revFasta = new Fasta(givenFasta.getHeader() + "reversed", givenFasta.getComments(), revSeq);

        // save results to file
        String newFilename = fileName.replaceAll(".txt", "") + "_reverse.txt";
        revFasta.saveToFile(newFilename);
        System.out.println("Successfully reversed sequence and saved as: " + newFilename);
    }


    private void changePassword() throws IOException {

        // Build helpers
        RequestBuilder protocolBuilder = new RequestBuilder();
        ObjectParser inStreamHelper = new ObjectParser();

        // Get new Password from user and send server the changing request
        String newPassword = getNewPasswordFromuser();
        JSONObject request = protocolBuilder.buildPasswordChangeProtocol(newPassword);
        out.writeUTF(request.toString());

        // Check success of password change
        JSONObject serverResponse = inStreamHelper.handleInput(this.in, "change_password_response");
        Boolean success = serverResponse.getJSONObject("protocol_body").getBoolean("change_status");
        if(!success){
            System.out.println("Password change was not successfull. Please try again");
            System.out.println("Changing password failed bc: " + serverResponse.getString("reason"));
            return;
        }

        //Set new password
        System.out.println("Successfully changed password");
        this.user.setPassword(newPassword);
        return;
    }

    private String getNewPasswordFromuser() {
        
        //Get input from user
        System.out.println("Please enter your new password");
        String newPassword;
        try {
            newPassword = this.userInput.readLine();
        } catch (IOException e) {
            System.out.println("Failed to read terminal input. Please try again:");
            newPassword = getPasswordFromUser();
        }

        return newPassword;
    }


    private void sendData() throws IOException{

        //Build helper
        RequestBuilder protocolBuilder = new RequestBuilder();

        //Get filename from user and send server the data
        String fileName = getValidFilenameFromUser();
        JSONObject request = protocolBuilder.buildDataSendProtocol(fileName);
        out.writeUTF(request.toString());
    }

    private String getValidFilenameFromUser() {

        //Get input from user
        System.out.println("Please enter the name of the file you want to select");
        String fileName;
        try {
            fileName = userInput.readLine();
        } catch (IOException e) {
            System.out.println("Failed to read terminal input. Please try again:");
            fileName = getValidFilenameFromUser();
        }

        //validate input
        FileHelper helper = new FileHelper();
        if (!helper.isValidFile(fileName)){
            System.out.println("The filename you entered was invalid. Please try again:");
            fileName = getValidFilenameFromUser();
        }

        return fileName;
    }


    private void handleRequestData() throws IOException{

        //Get available entries and end when not available
        ArrayList<String> entries = requestEntries();
        if (entries.size() == 0) {
            return;
        }
        
        //Get user selection from entries and request data from server
        String requestedEntryName = getValidEntryFromUser(entries);
        this.requestData(requestedEntryName);
    }

    private String getValidEntryFromUser(ArrayList<String> entries){

        //Give available options to user
        System.out.println("Please type one of the available entries you want to request:");
        for (String entry : entries){
            System.out.println(entry);
        }

        //Get input from user
        String requestedEntryName;
        try {
            requestedEntryName = this.userInput.readLine();
        } catch (IOException e) {
            System.out.println("Failed to read terminal input. Please try again:");
            requestedEntryName = getValidEntryFromUser(entries);
        }


        //validate input
        if (!entries.contains(requestedEntryName)){
            System.out.println("Got name: " + requestedEntryName);
            System.out.println("Given name is not in the presented available entries. Please try again");
            requestedEntryName = getValidEntryFromUser(entries);
        }


        return requestedEntryName;
    }


    private ArrayList<String> requestEntries() throws IOException{

        //Build helpers
        RequestBuilder protocolBuilder = new RequestBuilder();
        ObjectParser inStreamHelper = new ObjectParser();

        //Get available entries
        out.writeUTF(protocolBuilder.buildEntriesRequestProtocol().toString());
        JSONObject serverResponse = inStreamHelper.handleInput(this.in, "entries_list");

        //Extract protocol-body
        JSONObject responseBody = serverResponse.getJSONObject("protocol_body");
        int amount = responseBody.getInt("amount");

        //Check for empty entries list
        if (amount == 0) {
            System.out.println("Server has no saved entries. Can't request data yet");
            return new ArrayList<String>();
        }
        
        //Extract entries
        ArrayList<String> entries = new ArrayList<String>();
        JSONArray entryList = responseBody.getJSONArray("entries");
        for (int idx = 0; idx < amount; idx++){
            String entryName = entryList.getString(idx);
            entries.add(entryName);
        }
        
        return entries;
    }


    private void requestData(String entryName) throws IOException {

        //Build helpers
        RequestBuilder protocolBuilder = new RequestBuilder();
        ObjectParser inStreamHelper = new ObjectParser();

        //Request data from server and recieve
        JSONObject request = protocolBuilder.buildDataRequestProtocol(entryName);
        out.writeUTF(request.toString());
        JSONObject serverResponse = inStreamHelper.handleInput(this.in, "send_data");

        //Extract data from protocol
        JSONObject responseBody = serverResponse.getJSONObject("protocol_body");
        JSONObject dataBody = responseBody.getJSONObject("data_body");
        FSUGenBank entry = new FSUGenBank(dataBody);

        //Save recieved information to file
        String saveLocation = "txtfiles/" + entryName + ".txt";
        entry.saveToFile(saveLocation);
        System.out.println("Successfully saved requested data as: " + saveLocation);
    }

    private void handleDotPlot() throws IOException {

        //Build helpers
        RequestBuilder protocolBuilder = new RequestBuilder();
        ObjectParser interpreter = new ObjectParser();

        //Get available entries and end when not available
        ArrayList<String> entries = requestEntries();
        if (entries.size() == 0) {
            return;
        }

        //Get user selection from entries and request data from server
        String entry1 = getValidEntryFromUser(entries);
        String entry2 = getValidEntryFromUser(entries);

        JSONObject sequenceRequest = protocolBuilder.buildRequestSequenceProtocol(entry1, entry2);
        this.out.writeUTF(sequenceRequest.toString());

        System.out.println("sent protocol");

        JSONObject responses = interpreter.handleInput(this.in, "sequences_response");

        JSONObject protocolBody = responses.getJSONObject("protocol_body");
        String sequence1 = protocolBody.getString("sequence_1");
        String sequence2 = protocolBody.getString("sequence_2");

        System.out.println("extracted sequences");

        Fasta fasta1 = new Fasta(">", new ArrayList<String>(), sequence1);
        Fasta fasta2 = new Fasta(">", new ArrayList<String>(), sequence2);
        fasta1.sequenceComparison(fasta2);

    }


    public void endConnection() throws IOException{
        //Notify server of connection end
        RequestBuilder protocolBuilder = new RequestBuilder();
        JSONObject goodbyeMessage = protocolBuilder.buildEndConnectionProtocol();
        out.writeUTF(goodbyeMessage.toString());

        //Close socket connection
        if (this.socket != null){
            try{
                socket.close();
            } catch (IOException exception){
                exception.printStackTrace();
            }
        }
    }


    @Override
    public void finalize(){
        try{
            this.socket.close();
        } catch(IOException exception){}
        
    }
}
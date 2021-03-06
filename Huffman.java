import java.io.*;
import java.util.IllegalFormatException;
import java.util.PriorityQueue;
import java.util.Scanner;


public class Huffman {
    private static final int ALPH_SIZE = 100000; //Number of possible characters, overestimated so that it should work for all languages
    private static int paddingLength;

    /**
     * Method to create a frequency table of all characters given a string input
     *
     * @param text: string input for a frequency table to be produced
     * @return the frequency table generated
     */
    private static int[] createFrequencyTable(String text) { // Creates a frequency table indexed by the character value
        int[] frequencyTable = new int[ALPH_SIZE]; // Creates the frequency table to be the size of the alphabet in decimal
        try {
            for (char character : text.toCharArray()) { // For every character in the text
                frequencyTable[character]++; // Increment location indexed by the character in the array
            }
        } catch (IllegalFormatException e) {
            System.out.println("Error, enter a valid string!");
        } catch (IndexOutOfBoundsException ignored){}
        return frequencyTable;
    }

    /**
     * Recursively populates an array with codes representing each character
     * @param tree
     * @param code
     * @param codes
     */
    private static void getCodes(Node tree, String code, String[] codes){
        if(tree.isLeaf()){ //If the current node is a leaf
            codes[tree.getLetter()] = code; //Add the code to the array of codes
            return; //Return out of recursive call
        }

        //Recursive calls ensures that every path of the tree is traversed until a leaf node is reached
        getCodes(tree.left, code + '0', codes); //Recursive call to move left in the tree and add 0 to the code
        getCodes(tree.right, code + '1', codes); //Recursive call to move right in the tree and add 1 to the code
    }

    /**
     * Method to read an external text file into a string
     *
     * @param fileName name of the file to be read into a string
     * @return string containing the data from the text file
     */
    private static String readFile(String fileName) {
        StringBuilder text = new StringBuilder(); //Creates a new string builder object
        try {
            File textFile = new File(fileName); //Gets the text file
            Scanner reader = new Scanner(textFile); //Creates a new scanner object
            while (reader.hasNextLine()){ //While there are more lines in the text file
                //Append the new line to the scanner object
                text.append(reader.nextLine() + "\n"); // \n maintains new line and whitespace
            }
            reader.close(); //Close the scanner
        } catch (FileNotFoundException e){
            System.out.println("Critical error, file not found");
            System.out.println("Please enter path of the file and try again");
            System.exit(0);
        }
        return text.toString(); //Return the scanned data to string
    }

    private static void writeToFile(String text, String filename){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writer.write(text);
            writer.close();
            System.out.println("Decoded data written to file successfully and written to: " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param encodedText
     * @return
     */
    private static String padEncodedText(String encodedText){
        Huffman.paddingLength = (7 - (encodedText.length()) % 7) + 1;
        StringBuilder encodedTextBuilder = new StringBuilder(encodedText);
        for(int i = 0; i < Huffman.paddingLength; i++){
            encodedTextBuilder.append("0");
        }
        return encodedTextBuilder.toString();
    }

    /**
     * Method to remove the padding from an encoded text string
     * @param encodedText string of bits to remove padding from
     * @return a substring of the encoded text without the padding bits
     */
    private static String removePadding(String encodedText){
        //Returns a substring of the encoded text that excludes the padding added
        return encodedText.substring(0, (encodedText.length() - Huffman.paddingLength));
    }

    /**
     * Method to create a byte array given a string of binary
     * @param data string of binary data to convert to byte array
     * @return byte array equivalent of given data
     */
    private static byte[] createByteArray(String data){
        data = padEncodedText(data); //Pads the encoded data using the method pad encoded text
        int counter = 0; //Counter of number of bytes written to byte array
        byte[] byteArray = new byte[(data.length() / 7)];
        for (int i = 7; i < data.length(); i+=7){ //For every 7 string of bits in the entered string
            byte currentByte = Byte.parseByte(data.substring(i - 7, i), 2); //Parse the 7 bits into a byte (maximum number of bits for byte in java)
            byteArray[counter] = currentByte; //Appends the parsed byte into the byte array
            counter++; //Adds 1 to the counter of number of string bits converted to byte
        }
        return byteArray;
    }

    /**
     * Method to write a string of bits to a file as bytes
     * @param encodedData string of bits to write to file
     * @param filename name of file to write to
     */
    private static void writeBitsToFile(String encodedData, String filename){
        byte[] byteArray = createByteArray(encodedData); //Creates a new byte array using the method create byte array
        try(FileOutputStream out = new FileOutputStream(filename)){
            out.write(byteArray); //Writes byte array to the file
            System.out.println("File successfully compressed and written to: " + filename); //Confirmation message
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to read bytes from a file and return them as a String
     * @param filename name of the file to read bytes from
     * @return String of ones and zeroes that have been read from the file
     */
    private static String readBytesFromFile(String filename){
        StringBuilder byteString = new StringBuilder();
        try {
            File file = new File(filename);
            byte[] bytes = new byte[(int) file.length()]; //Creates a new byte array with the length of the number of characters in the file
            DataInputStream input = new DataInputStream(new FileInputStream(file));
            input.readFully(bytes); //Reads the full file into the byte array
            input.close(); //Closes the data input stream
            for(int i = 0; i < bytes.length; i++){ //For every byte in the byte array
                int indexedCharacter = bytes[i]; //Get the indexed byte
                //Converts the indexed byte to a binary string with the leading zeroes maintained
                String stringRepresentation = String.format("%7s", Integer.toBinaryString(indexedCharacter)).replace(' ', '0');
                byteString.append(stringRepresentation); //Appends the String binary representation of the indexed byte to a string
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String noPadding = removePadding(byteString.toString()); //Calls a method to remove padding from binary string
        return noPadding;
    }

    /**
     * Method to construct a Huffman Tree given a frequency table of characters
     * @param frequencyTable table of characters with frequency
     * @return Node that is the root of the tree
     */
    private static Node constructTree(int[] frequencyTable) {
        //Creates a priority queue of nodes, sorted from least frequent to most frequent using the defined comparator
        PriorityQueue<Node> nodes = new PriorityQueue<>();
        for(int i = 0; i < frequencyTable.length; i++){
            if(!(frequencyTable[i] == 0)){ //Checks if the indexed letter appears in the text document
                Node character = new Node(frequencyTable[i], (char) i); //If so then create a new node
                nodes.add(character); //Add node to priority queue
            }
        }
        while (nodes.size() > 1){ //While there are still nodes in the queue to be combined
            try {
                Node leftNode = nodes.poll(); //Gets left node by getting least frequent node in the queue and removing it
                Node rightNode = nodes.poll(); //Gets right node by getting least frequent node in the queue and removing it
                //Creates a parent node which has the frequency of its two child nodes combined. Character of this node is the null character
                Node combinedNode = new Node(leftNode.getFrequency() + rightNode.getFrequency(), '\u0000', leftNode, rightNode);
                nodes.add(combinedNode); //Parent node added into priority queue at the correct position for its frequency
            }catch (NullPointerException e){
                System.out.println("Ensure number of characters is greater than 0");
                System.exit(0);
            }
        }
        return nodes.poll(); //Returns the only node in the queue which is the root node
    }

    /**
     * Method to encode a given string using codes generated from the huffman tree
     * @param text data to encode
     * @param codes table of codes representing each character
     * @return the encoded String
     */
    private static String encode(String text, String[] codes){
        StringBuilder encodedText = new StringBuilder();
        for(char character : text.toCharArray()){
            try{
                encodedText.append(codes[character]); //Append the code representation of the character to the string builder
            }catch (IndexOutOfBoundsException e){
                continue;
            }
        }
        return encodedText.toString();
    }

    /**
     * Method to decode an encoded text using a given tree
     * @param encodedText text representation of encoded bits
     * @param root node of the root of the tree
     * @return decoded string of text
     */
    private static String decode(String encodedText, Node root){
        StringBuilder decodedText = new StringBuilder(); //Creates a new string builder object
        Node currentNode = root; //Gets the current node (starting at the root)
        for(int i = 0; i < encodedText.length(); i++){
            if(encodedText.charAt(i) == '0'){ //if the character at current indexed position is equal to 0
                currentNode = currentNode.getLeft(); //Go left in the huffman tree
            } else if(encodedText.charAt(i) == '1') { //Else if the character is 1
                currentNode = currentNode.getRight(); //Go right in the huffman tree
            } else {
                //If the character isn't either a 1 or a 0 then the entered data must not be encoded and therefore an error is thrown
                throw new IllegalArgumentException("Data must be in the form of 1s and 0s");
            }

            if (currentNode.isLeaf()){ //Checks if the current node is a leaf
                //If the current node is a leaf then it means that a letter has been reached
                decodedText.append(currentNode.getLetter()); //The letter of the current node is added to the string builder
                currentNode = root; //The current node is reset to the root so the tree can be traversed again with another string of bits
            }
        }
        System.out.println("Text decoded successfully");
        return decodedText.toString() + "\n"; //Returns the string builder converted to a string
    }

    public static class Node implements Comparable<Node>{ //Node class for huffman coding
        private int frequency; //Stores frequency of the letter
        private Node left; //Stores the left child of the node
        private Node right; //Stores the right child of the node
        private char letter; //Stores the letter associated with the node

        Node(int frequency, char letter) { //Node constructors
            this.frequency = frequency;
            this.letter = letter;
            this.left = null;
            this.right = null;
        }

        Node(int frequency, char letter, Node left, Node right) { //Constructor where the two child nodes are provided
            this.frequency = frequency;
            this.letter = letter;
            this.left = left;
            this.right = right;
        }

        //Setters and getters
        public void setRight(Node right) {
            this.right = right;
        }

        public void setLeft(Node left) {
            this.left = left;
        }

        public int getFrequency() {
            return frequency;
        }

        public Node getLeft() {
            return left;
        }

        public Node getRight() {
            return right;
        }

        public char getLetter() {
            return letter;
        }

        /**
         * Instance method which determines if the node is a leaf or not
         *
         * @return boolean indicating if the node is a leaf or not
         */
        public boolean isLeaf() {
            return (this.right == null && this.left == null); // Determines if a node is a leaf by checking if is has any children
        }

        @Override
        public int compareTo(Node otherNode) { //Comparator to compare the frequency of nodes in a priority queue
            int comparison = Integer.compare(this.frequency, otherNode.frequency); //Compare frequencies of 2 nodes
            if(comparison != 0){
                return comparison;
            }else{ //Else if the nodes have the same frequencies
                return Integer.compare(this.letter, otherNode.letter); //Compare based on alphabetical order
            }
        }
    }

    public static void main(String[] args) throws IOException {
        //FILE PATHS
        String filePath = ""; //File path for file to be compressed
        String compressedFilePath = ""; //File path for where the compressed file should be
        String decompressedFilePath = ""; ///File path for where the decompressed file should be

        System.out.println("Process may take a few seconds for large datasets");

        //CREATING HUFFMAN TREE AND CODES
        String[] codes = new String[ALPH_SIZE]; //Creates a new string array to store codes of each letter
        String fileData = readFile(filePath); //Reads the given text file
        int[] frequencyTable = createFrequencyTable(fileData); // Creates the frequency table for the data that has just been read from the text file
        Node tree = constructTree(frequencyTable); //Constructs a huffman tree using the frequency table
        getCodes(tree, "", codes); //Populates the codes array by traversing through the Huffman Tree

        //COMPRESSION
        String encodedData = encode(fileData, codes); //Encodes the given string of text using codes for each letter
        writeBitsToFile(encodedData, compressedFilePath); //Writes the encoded data into a file specified by compressedFilePath

        //DECOMPRESSION
        String bitsInCompressedFile = readBytesFromFile(compressedFilePath); //Reads the bytes from the compressed file and converts them into a string of bits
        String decodedData = decode(bitsInCompressedFile, tree); //Decodes the string of bits using the tree
        writeToFile(decodedData, decompressedFilePath); //Writes decoded string to the the file specified in decompressedFilePath

        System.out.println("Is decompressed data the same as the original data?: " + fileData.equals(decodedData));
    }
}


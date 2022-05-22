//Kevin Siraki
//COMP 424
//Professor Boctor
//Assignment 1
import java.io.*;
import java.util.*;

class Assignment1 {
    //main method, tests all the algorithms against given Cipher.
    public static void main(String args[]) throws IOException { //check for all permutations, print result with most words
        String cipher = "KUHPVIBQKVOSHWHXBPOFUXHRPVLLDDWVOSKWPREDDVVIDWQRBHBGLLBBPKQUNRVOHQEIRLWOKKRDD";
        ArrayList < String > list = new ArrayList < String> ();
        read(list); //populate new arraylist with dictionary of words
        Map freq = mostFrequent(cipher); //map each key to its frequency (string:int)
        ArrayList < Character > sortedCharacterss = frequencySort(freq); //sort by most frequent
        Set < Integer > shiftsPoss = findShifts(sortedCharacterss); //finds shifts on most frequent in list
        Set < String > stringsPoss = new HashSet < String > (); //hash set for shifts on each possible message
        //substitution shift called
        for (Integer shift: shiftsPoss)
            stringsPoss.add(subShift(shift, cipher)); //add to set
        List < String > keys = new ArrayList < String> ();
        //keyLen is narrowed down  7 for faster execution.
        for(int i = 0; i <= 7; i++)      
            for(String str : permutation(ALPHABET.substring(0, i))) //key-gen from alphabet[0]->alphabet[i]
                keys.add(str); //insert each permutation of given key
        for (String candidate: stringsPoss) //call columnar transposition
            for (String strKeys: keys) //narrowed it down to checking if the deciphered text has at least 5 english words to clean up output.
                if (segmentString(columnarTranspos(candidate, strKeys), list) != null && wordCount(segmentString(columnarTranspos(candidate, strKeys), list)) > 5)
                    System.out.println(segmentString(columnarTranspos(candidate, strKeys), list)); //prints best match
    }
      
    //our alphabet. it is a constant. in caps b/c cipher is upper case.
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    //simple helper methods
    private static void read(ArrayList < String > list) throws FileNotFoundException { //read file contents, store in array list
        //dictionary file source: https://gist.github.com/deekayen/4148741
        File listFile = new File("Dictionary.txt"); //text file of most common words
        if (!listFile.exists()) {
            System.out.println("Dictionary.txt not found.  Exiting.");
            System.exit(-1);
        }
        Scanner sc = new Scanner(listFile); //read in each word
        while (sc.hasNext())
            list.add(sc.nextLine().toUpperCase()); //cipher is upper case.
        sc.close();
    }
    
    private static boolean wordExists(ArrayList < String > list, String wordToLookup) { 
        boolean res = list.contains(wordToLookup) ? true : false; //see if a word exists in array list
        return res;
    }

    private static int wordCount(String str) {
        int count = 1; //count how many words a string has (split by spaces)
        for (int i = 0; i < str.length() - 1; i++)
            if ((str.charAt(i) == ' ') && (str.charAt(i + 1) != ' '))
                count++;
        return count;
    }

    //source: https://programmingcode4life.blogspot.com/2015/09/columnar-transposition-cipher.html
    //columnar transposition decryption algorithm
    /*
    Columnar Transposition involves writing the plaintext out in rows, and then reading the ciphertext off in columns one by one.
    In a columnar transposition, the message is written out in rows of a fixed length, 
    and then read out again column by column, and the columns are chosen in some scrambled order. 
    Both the width of the rows and the permutation of the columns are usually defined by a keyword.
    */
    private static String columnarTranspos(String str, String key) {
        int keyLen = key.length();
        int rem = str.length() % keyLen; //remainer 
        int flag = rem; //initial flag 
        int rowCt = (int) Math.ceil((double) str.length() / key.length()); //row counter
        int[] arrangedKeys = arrangeKey(key); //call arrangeKeys method
        char[][] grid = new char[rowCt][keyLen]; //2d grid of characters 
        String newStr = "";
        for (int col = 0; col < keyLen; col++) { //check each column against each key
            for (int intKeys: arrangedKeys)
                if (intKeys == col)
                    if (flag <= keyLen && flag != 0) //check if space is needed
                        for (int i = keyLen - 1; i > rem - 1; i--) //start at last column
                            if (arrangedKeys[i] == col) {
                                newStr = str.substring(0, (rowCt - 1));
                                newStr = newStr + 'X'; //X for pad
                                flag++; //increment flag
                                str = str.substring((rowCt - 1), str.length());
                            }
            if (newStr == "") { //if space isnt required
                newStr = str.substring(0, rowCt);
                str = str.substring(rowCt, str.length());
            }
            for (int row = 0; row < rowCt; row++)
                grid[row][col] = newStr.charAt(row); //populate grid with chars
            newStr = "";
        }
        String message = ""; //populate the message with the grid
        for (int r = 0; r < rowCt; r++)
            for (int intKeys: arrangedKeys)
                message += grid[r][intKeys];
        return message;
    }
    
    //source: https://stackoverflow.com/questions/19108737/java-how-to-implement-a-shift-cipher-caesar-cipher/31601568
    //shifting decryption algorithm, returns a string with specified number of shifts on the cipher.
    private static String subShift(int shift, String cipher) {
        String plainText = "";
        for (char ch: cipher.toCharArray()) { //where each char in cipher  is on the alphabet
            int alphaIn = ALPHABET.indexOf(ch);
            if ((alphaIn - shift) < 0) {
                int rem = Math.abs(alphaIn - shift) % 25; //length of alphabet is 26, REM needed
                plainText += ALPHABET.charAt(26 - rem); 
            } else
                plainText += ALPHABET.charAt(alphaIn - shift); //if no rem needed, just shift.
        }
        return plainText;
    }
    
    //source: https://www.geeksforgeeks.org/caesar-cipher-in-cryptography/
    private static Set findShifts(List < Character > sortedCharacterss) { //find number of shifts in the sorted list
        Set < Integer > shiftsPoss = new HashSet < > ();
        char[] commonChars = { //common letters
            'A',
            'E',
            'O',
            'S',
            'T'
        }; 
        char shiftedCharacter;
        for (Character sortedCharacters: sortedCharacterss) {
            int index = ALPHABET.indexOf(sortedCharacters);
            boolean finished = false;
            int shift = 1;
            while (!finished) { //subtracts shifts until finding common letter
                if ((index - shift) < 0) {
                    int rem = Math.abs(index - shift) % 25;
                    shiftedCharacter = ALPHABET.charAt(26 - rem);
                } else
                    shiftedCharacter = ALPHABET.charAt(index - shift);
                for (char ch: commonChars) //if found most common 
                    if (shiftedCharacter == ch) {
                        shiftsPoss.add(shift); //add shifted word to Set (avoid dup)
                        finished = true;
                    }
                shift++; //increment number of shifts
            }
        }
        return shiftsPoss;
    }
    
    //source: https://www.edureka.co/blog/java-string/
    //split the string with spaces between words found in dictionary
    private static String segmentString(String newString, ArrayList < String > list) {
        for (int i = 1; i < newString.length(); i++) {
            String prefix = newString.substring(0, i); //newString[0]
            if (wordExists(list, prefix)) { //if the dictionary contains the word at prefix, add space, return rest of string (recurse rest of string)
                String suffix = newString.substring(i, newString.length());
                String segSuffix = segmentString(suffix, list); //recursive call for rest of string (suffix)
                if (segSuffix != null)
                    return prefix + " " + segSuffix; //first letter with space then rest of word
                else
                    return suffix; //rest of word
            }
        }
        return null; //no words found
    }

    //source: https://stackoverflow.com/questions/21750365/how-to-find-the-most-frequently-occurring-character-in-a-string-with-java
    //map of charachter:frequency in a given cipher
    private static Map mostFrequent(String cipher) {
        Map freq = new HashMap();
        int count;
        for (char alphaChar = 'A'; alphaChar <= 'Z'; alphaChar++) {
            count = 0;
            for (int i = 0; i <= cipher.length() - 1; i++) //see how often the char is in the cipher
                if (alphaChar == cipher.charAt(i))
                    count++; //increment each time a alphabet char is found
            if (count > 3)
                freq.put(alphaChar, count); //avoids duplicates, HashMap of character:frquencyCount
        }
        return freq;
    }

    //source: https://www.geeksforgeeks.org/sort-elements-by-frequency-set-5-using-java-map/
    //return a map of the most to least common in a given map
    private static ArrayList frequencySort(Map freq) { //sort the keys in freq
        ArrayList keys = new ArrayList < Character > ();
        int maxValue = (int) Collections.max(freq.values());
        for (int max = maxValue; max >= 0; max--)
            for (Object newObj: freq.keySet())
                if (freq.get(newObj).equals(max))
                    keys.add(newObj);
        return keys;
    }
    
    //source: https://howtodoinjava.com/java/collections/arraylist/merge-arraylists/
    //source: https://www.tutorialspoint.com/print-all-permutation-of-a-string-using-arraylist-in-java
    private static ArrayList < String > merge(ArrayList < String > list, String c) { //merges a string / arraylist
        ArrayList < String > res = new ArrayList < String > ();
        for (String s: list)
            for (int i = 0; i <= s.length(); ++i) {
                String ps = new StringBuffer(s).insert(i, c).toString(); //insert last char in possible pos., add to list
                res.add(ps);
            }
        return res;
    }

    //source: https://www.tutorialspoint.com/print-all-permutation-of-a-string-using-arraylist-in-java
    private static ArrayList <String> permutation(String s) { //generate a permutation of an arraylist
        ArrayList < String > res = new ArrayList < String > ();
        if (s.length() == 1)
            res.add(s);
        else if (s.length() > 1) {
            int lastChar = s.length() - 1; //index of last character
            String last = s.substring(lastChar); //last char
            String rest = s.substring(0, lastChar); //rest of str 
            res = merge(permutation(rest), last); //perform permutation on rest of string, merge last char.
        }
        return res;
    }
    
    //source: https://programmingcode4life.blogspot.com/2015/09/columnar-transposition-cipher.html
    //arrange the stringlist based on keys, return a list of their positions.
    private static int[] arrangeKey(String key) {
        String[] keys = key.split(""); 
        Arrays.sort(keys); //built-in java sort on key array
        int[] num = new int[key.length()];
        for (int i = 0; i < keys.length; i++) //iterate over array
            for (int j = 0; j < key.length(); j++) //iterate over string
                if (keys[i].equals(key.charAt(j) + "")) { //if the index equals the char pos in the string
                    num[j] = i; //append positions to a list of keys
                    break;
                }
        return num;
    }
}
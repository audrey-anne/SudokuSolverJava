package com.start;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class Sudoku {
    //The objective of this class is to be able to solve sudokus. Right now, the program solves almost every sudoku that could
    //be manually solved using only logic. The program does not solve sudoku where probabilities need to be involved.
    //It solves almost every extreme sudoku

    //An object sudoku is an array of 81 elements representing a sudoku
    private int[] sudokuArray;

    //This first constructor takes an Array of 81 elements as input
    //Empty squares must be represented as 0 (value by default of an int element that has not been initialized)
    Sudoku(int[] sudoku){
        this.sudokuArray = new int[81];
        System.arraycopy(sudoku, 0, this.sudokuArray, 0, 81);
    }

    //The second constructor takes a file as input where each line of the sudoku is on a different line
    //and the numbers are separated by spaces
    Sudoku(String filename) {
        try{
            int[] sudoku = new int[81];
            File file= new File(filename);
            Scanner read = new Scanner(file);
            String line;
            int index =0;
            for (int i=0;i<9;i++){
                line = read.nextLine();
                String[] eachCharacter = line.split( " ");
                for (int j=0;j<9;j++){
                    sudoku[index]=Integer.parseInt(eachCharacter[j]);
                    index++;
                }
            }
            this.sudokuArray = sudoku;
        }catch (IOException e){
            System.out.println("The file could not be found");
        }
    }

    // this is the main method that solves the array of an object Sudoku
    void solveSudoku() {
        int[] sudoku = this.sudokuArray;
        //the element sudoku must have been initialized
        if(sudoku == null){
            System.out.println("Your sudoku is null, you need initialize your sudoku in order to solve it");
            return;
        }
        //the method iterates while the sudoku is still not solved
        while (isNotSolved(sudoku)) {
            //we create a copy of the sudoku at the start of the iteration to compare it at the end with isSame
            int[] sudokuCopy = new int[81];
            System.arraycopy(sudoku, 0, sudokuCopy, 0, 81);
            //the hints represent all the possible numbers an empty square can have, every empty square as an array of
            //possible numbers so it is a 2D array.
            int[][] hints = cleanUpHints(createHints(sudoku));
            helpToSolveSudoku(sudoku,hints);

            if(isNotSolved(sudoku)) {

                //we iterate through each hint line
                for(int i=1;i<=9;i++) {
                    int[][] lineHints = lineHints(hints,i);
                    for(int j=1;j<=9;j++) {

                        //if the line contains only one time a certain number we want to change it in the sudoku
                        // since that number can only be in that position
                        if(containsNumber(lineHints,j)==1) {
                            int index = getIndex(lineHints,j);
                            sudoku[index+9*(i-1)]=j;
                        }

                    }
                }
                //we do the same with the columns if the sudoku is not solved
                if (isNotSolved(sudoku)) {
                    hints = cleanUpHints(createHints(sudoku));
                    for(int i=1;i<=9;i++) {
                        int[][] columnHints = columnHints(hints,i);
                        for(int j=1;j<=9;j++) {

                            //if the column contains only one time a certain number we want to change it in the sudoku
                            if(containsNumber(columnHints,j)==1) {
                                int index = getIndex(columnHints,j);
                                sudoku[9*index+(i-1)]=j;
                            }

                        }
                    }
                    //we do the same with the squares if the sudoku is still not solved
                    if(isNotSolved(sudoku)) {
                        hints = cleanUpHints(createHints(sudoku));
                        for (int i=1;i<=9;i++) {
                            int[][] squareHints = squareHints(hints,i);
                            for (int j=1;j<=9;j++) {
                                //if the square contains only one time the number we change it in the sudoku
                                if(containsNumber(squareHints,j)==1) {
                                    int index = getIndex(squareHints,j);
                                    // we want to know the index of the first element of this square
                                    int squareIndex = getSquareIndex(i);
                                    int position;
                                    // if the index is 0 to 2 we just add index to the square Index to get the position
                                    if(index>=0&&index<=2) {
                                        position = squareIndex + index;
                                    }else if(index>=3&&index<=5) {
                                        //then its squareIndex +9 and + index -3
                                        position = squareIndex+9+ (index-3);
                                    }else {
                                        //then the index is in between 6 and 8
                                        position= squareIndex +18+ (index-6);
                                    }
                                    sudoku[position]= j;
                                }
                            }
                        }
                    }
                }

            }
            //if the sudoku does not change after a full iteration we don't want to be stuck in
            // an infinite loop so we have to check if it is stuck

            if (isSame(sudoku,sudokuCopy)) {
                break;
            }

        }// Once we are done, we print the sudoku and changes the array of the object sudoku
        printSudoku(sudoku);
        this.sudokuArray=sudoku;
    }

    //the method now takes the set of cleaned up hints that we give it. Where there is only one possible number,
    //it puts that number in the right square of the sudoku
    private static void helpToSolveSudoku(int[] sudoku, int[][] hints) {
        while (isNotSolved(sudoku)) {
            int[] sudokuCopy = new int[81];
            System.arraycopy(sudoku, 0, sudokuCopy, 0, 81);
            for (int i=0;i<81;i++) {
                if(hints[i]!= null) {
                    //when the length, then there is only one possible number at that position in the sudoku
                    if(hints[i].length==1) {
                        sudoku[i]=hints[i][0];
                    }
                }
            }
            //if the sudoku does not change at a certain point we don't want to be stuck in
            // an infinite loop so we have to check if it is unsolvable with this method
            if (isSame(sudokuCopy,sudoku)) {
                break;
            }
        }
    }
    //this method takes the hints and removes from the possible numbers the ones that are impossible in that position
    //once it has cleaned up the hints it returns them
    private static int[][] cleanUpHints(int[][] hints){
        // if isNumberAligned is true for a number in a square, then we want to remove this number from the hints of the line where align = true
        // in other words, if it is true then this number in that square must be in the line where it isAligned, which means it cannot appear in the rest of the line
        // first we want go through each square
        for (int i=1;i<=9;i++) {

            //then for a square we want to go through each number
            for (int number=1;number<=9;number++) {
                int[][] squareHints=squareHints(hints,i);
                //we will remove some elements only if the number is aligned in a square

                if(isNumberAligned(squareHints,number)) {
                    //if it is aligned we need to know in which direction

                    if(isAlignedVertically(squareHints,number)) {
                        //if it is vertically we want to remove all numbers that are in that line but not in the same square
                        int [] indexes = getNumbersArray(squareHints,number);

                        int columnOfSquare = (indexes[0])%3+1;

                        int column= initializeColumn(i,columnOfSquare);
                        int[] notTouching = new int[3];
                        if(i==1||i==2||i==3) {
                            // the element at 0 has already value 0
                            notTouching[1]=1;
                            notTouching[2]=2;
                        } else if(i==4||i==5||i==6) {
                            notTouching[0]=3;
                            notTouching[1]=4;
                            notTouching[2]=5;
                        }else {
                            notTouching[0]=6;
                            notTouching[1]=7;
                            notTouching[2]=8;
                        }
                        // we get the column hints
                        int[][] columnHints = columnHints(hints,column);
                        //we through each element
                        for (int j=0;j<9;j++) {
                            // we check if the index is not Touchable
                            if(!isElement(notTouching,j)) {
                                //if not we remove our number at the index
                                removeFromHints(columnHints,j,number);
                                for(int k=0;k<9;k++) {
                                    hints[9*k+column-1]=columnHints[k];

                                }

                            }
                        }

                    }
                }
            }
        }
        // we can also do the same thing horizontally
        // first we want go through each square
        for (int i=1;i<=9;i++) {

            //then for a square we want to go through each number
            for (int number=1;number<=9;number++) {
                int[][] squareHints=squareHints(hints,i);
                //we will remove some elements only if the number is aligned in a square

                if(isNumberAligned(squareHints,number)) {
                    //if it is aligned we need to know in which direction

                    if(isAlignedVertically(squareHints,number)) {
                        //if it is vertically we want to remove all numbers that are in that column but not in the same square
                        int [] indexes = getNumbersArray(squareHints,number);

                        int columnOfSquare = (indexes[0])%3+1;

                        int column= initializeColumn(i,columnOfSquare);
                        int[] notTouching = new int[3];
                        if(i==1||i==2||i==3) {
                            // value at position 0 has already value 0
                            notTouching[1]=1;
                            notTouching[2]=2;
                        } else if(i==4||i==5||i==6) {
                            notTouching[0]=3;
                            notTouching[1]=4;
                            notTouching[2]=5;
                        }else {
                            notTouching[0]=6;
                            notTouching[1]=7;
                            notTouching[2]=8;
                        }
                        // we get the column hints
                        int[][] columnHints = columnHints(hints,column);
                        //we through each element
                        for (int j=0;j<9;j++) {
                            // we check if the index is not Touchable
                            if(!isElement(notTouching,j)) {
                                //if not we remove our number at the index
                                removeFromHints(columnHints,j,number);
                                for(int k=0;k<9;k++) {
                                    hints[9*k+column-1]=columnHints[k];

                                }

                            }
                        }
                    }else {
                        //If it is not aligned vertically it is aligned horizontally
                        //if it is horizontally we want to remove all numbers that are in that line but not in the same square
                        // so we get the indexes of the elements ( we only need the first index though)
                        int[] indexes = getNumbersArray(squareHints, number);
                        // the lineOfSquare represents which line is aligned in the square (first, second or third)
                        int lineOfSquare = (indexes[0]) / 3 + 1;

                        int line = ((i - 1) / 3) * 3 + lineOfSquare;
                        int[] notTouching = new int[3];
                        if (i == 1 || i == 4 || i == 7) {
                            // value at position 0 is already 0
                            notTouching[1] = 1;
                            notTouching[2] = 2;
                        } else if (i == 2 || i == 5 || i == 8) {
                            notTouching[0] = 3;
                            notTouching[1] = 4;
                            notTouching[2] = 5;
                        } else {
                            notTouching[0] = 6;
                            notTouching[1] = 7;
                            notTouching[2] = 8;
                        }
                        // we get the line hints
                        int[][] lineHints = lineHints(hints, line);
                        //we through each element
                        for (int j = 0; j < 9; j++) {
                            // we check if the index is not Touchable
                            if (!isElement(notTouching, j)) {
                                //if not we remove our number at the index
                                removeFromHints(lineHints, j, number);
                                System.arraycopy(lineHints, 0, hints, 9 * (line - 1), 9);

                            }
                        }
                    }

                }
            }
        }

        // now that we cleaned up with the hints that are aligned, we can further clean up
        // if two hints in a same row / column / square have length 2 and the same 2 elements, then all
        // the other elements in the row / column / square cannot have these 2 numbers so we remove them from the hints
        // we go through each line and apply the cleanUpIdenticalElements method
        for (int i=1;i<=9;i++){
            int[][] lineHint = lineHints(hints,i);
            cleanUpIdenticalElements(lineHint);
            //we go through each element of hints on this line to change to the new value
            System.arraycopy(lineHint, 0, hints, 9 * (i - 1), 9);

        }
        // now we do the same thing for columns
        for (int i=1;i<=9;i++){
            int[][] columnHint = columnHints(hints,i);
            cleanUpIdenticalElements(columnHint);
            //we go through each element of hints on this column to change to the new value
            for(int j=0;j<9;j++){
                hints[9*j+(i-1)]=columnHint[j];
            }

        }
        // and for the squares
        for(int i=1;i<=9;i++){
            int[][] squareHint = squareHints(hints,i);
            cleanUpIdenticalElements(squareHint);
            int index= getSquareIndex(i);
            for (int j=0;j<9;j++){
                hints[index]= squareHint[j];
                if((j+1)%3 ==0){
                    index =index+ 7;
                }else{
                    index ++;
                }

            }
        }
        // if there is 2 numbers in a line / column / square that can only be in the same 2 positions, then all the other numbers
        // cannot be in these positions
        // we go through each line
        for(int i=1;i<=9;i++){
            //then we check for each number if they appear 2 times in the line at the same indexes
            int[][]lineHints= lineHints(hints,i);
            for(int number=1;number<=9;number++){
                if(containsNumber(lineHints,number)==2){
                    //if it does, we get the indexes where this number is
                    int[] indexes1 = getIndexes(lineHints,number);
                    //then we go through all of the remaining numbers to get the same thing and compare them
                    for(int number2=number+1;number2<=9;number2++){
                        if(containsNumber(lineHints,number2)==2){
                            int[] indexes2 = getIndexes(lineHints,number2);
                            // now that we have the indexes we compare them
                            if(Arrays.equals(indexes1,indexes2)){
                                //if they are the same we want to remove any other number at this indexes
                                //so we go through every number
                                for(int j=1;j<=9;j++){
                                    //we remove the number from the indexes unless its number or number2
                                    if(j==number || j==number2){
                                        continue;
                                    }
                                    removeFromHints(lineHints,indexes1[0],j);
                                    removeFromHints(lineHints,indexes1[1],j);
                                    // we copy it in the hints and we are done
                                    System.arraycopy(lineHints, 0, hints, 9 * (i - 1), 9);

                                }
                            }
                        }
                    }
                }
            }
        }
        // we repeat with columns
        for(int i=1;i<=9;i++){
            //then we check for each number if they appear 2 times in the column at the same indexes
            int[][]columnHints= columnHints(hints,i);
            for(int number=1;number<=9;number++){
                if(containsNumber(columnHints,number)==2){
                    //if it does, we get the indexes where this number is
                    int[] indexes1 = getIndexes(columnHints,number);
                    //then we go through all of the remaining numbers to get the same thing and compare them
                    for(int number2=number+1;number2<=9;number2++){
                        if(containsNumber(columnHints,number2)==2){
                            int[] indexes2 = getIndexes(columnHints,number2);
                            // now that we have the indexes we compare them
                            if(Arrays.equals(indexes1,indexes2)){
                                //if they are the same we want to remove any other number at this indexes
                                //so we go through every number
                                for(int j=1;j<=9;j++){
                                    //we remove the number from the indexes unless its number or number2
                                    if(j==number || j==number2){
                                        continue;
                                    }
                                    removeFromHints(columnHints,indexes1[0],j);
                                    removeFromHints(columnHints,indexes1[1],j);
                                    // we copy it in the hints and we are done
                                    for(int k=0;k<9;k++){
                                        hints[9*k+(i-1)]=columnHints[k];
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }




        return hints;
    }

    // a method that returns the index where is a number in an hint ( we assume there is only this number once in the whole array)
    private static int getIndex(int[][] hints, int number) {
        for (int i=0;i<hints.length;i++) {
            //we check if the element at i is null
            if (hints[i]!= null) {
                //if it is not null we check if it is an element
                if (isElement(hints[i],number)) {
                    // if it is we return the index
                    return i;

                }

            }
        }
        return -1;

    }
    // a method that returns the indexes of a number in an hint
    private static int[] getIndexes(int[][] hints, int number){
        int[] indexes = new int[containsNumber(hints,number)];
        int count =0;
        for (int i=0;i<hints.length;i++){
            //if that number is the subarray then we add its index the indexes
            if(isElement(hints[i],number)){
                indexes[count]=i;
                count++;
            }
        }
        return indexes;
    }
    // a method that check if in the hints, there is an array of length 2 that has the same two elements then another array of length 2
    private  static void cleanUpIdenticalElements(int[][] hints){
        // we want to go through each element and check if it has length 2
        for (int i=0;i<hints.length;i++){
            if(hints[i]!=null&&hints[i].length==2){
                //then we want to check if there is another array of length 2 with the same elements
                // so we go through the remaining elements
                for(int j=i+1;j<hints.length;j++){
                    if(hints[j]!=null&&hints[j].length==2){
                        //then we check if hints[i] and hints[j] have the same elements
                        if (Arrays.equals(hints[i], hints[j])){
                            //if they do have the same elements then we want to remove from the other hints these 2 elements
                            //we don't want to change the elements at i and j
                            //we also want to know what are these elements
                            int number1 = hints[i][0];
                            int number2 = hints[i][1];
                            //we go through all the hints and remove these 2 numbers, unless it's at i or j
                            for (int k=0;k<hints.length;k++){
                                if(k==i || k==j){
                                    continue;
                                }
                                removeFromHints(hints,k,number1);
                                removeFromHints(hints,k,number2);
                            }


                        }
                    }
                }
            }
        }
    }
    // a method that checks if a number is present in 2/3 hints aligned in the same line without being present in the rest of the square
    // we assume that the number is at least present 1 time
    private static boolean isNumberAligned(int[][] squareHints, int number) {
        //first, lets check how many times the number appears in the squareHint
        // if it more than 3 then it is automatically false
        int numberCount = containsNumber(squareHints,number);
        if(numberCount>3||numberCount<1) {
            return false;
        }
        //then we know there is either 1,2 or 3 times this number in the square
        //in the rare case where there is 1 element then it is true
        if(numberCount==1) {
            return true;
        }
        // now we must check if the 2/3 numbers are aligned vertically or horizontally
        else {
            //we separate the squareHints in 3 horizontal lines
            int[][] line1 = { squareHints[0],squareHints[1],squareHints[2]};
            int[][] line2 = { squareHints[3],squareHints[4],squareHints[5]};
            int[][] line3 = { squareHints[6],squareHints[7],squareHints[8]};
            // if the number are only present on one line then the 2 others must contain 0 times the number
            int countLine1 = containsNumber(line1,number);
            int countLine2 = containsNumber(line2,number);
            int countLine3 = containsNumber(line3,number);
            if((countLine1 ==0 && countLine2 ==0)||(countLine1==0&&countLine3==0)||(countLine2==0&&countLine3==0)) {
                return true;
            }
            //If they are not aligned horizontally then we want to check if the are aligned vertically
            int[][] column1 = { squareHints[0],squareHints[3],squareHints[6]};
            int[][] column2 = { squareHints[1],squareHints[4],squareHints[7]};
            int[][] column3 = { squareHints[2],squareHints[5],squareHints[8]};
            int countColumn1 = containsNumber(column1,number);
            int countColumn2 = containsNumber(column2,number);
            int countColumn3 = containsNumber(column3,number);
            return (countColumn1 == 0 && countColumn2 == 0) || (countColumn1 == 0 && countColumn3 == 0) || (countColumn2 == 0 && countColumn3 == 0);

        }
    }
    // a method that returns the index of the numbers in an array
    private static int[] getNumbersArray(int[][] squareHint, int number) {
        int[] indexes = new int[containsNumber(squareHint,number)];
        int count =0;
        for (int i=0;i<squareHint.length;i++) {
            if (isElement(squareHint[i],number)) {
                indexes[count]=i;
                count++;
            }
        }
        return indexes;
    }
    // a method that removes a number from the hints array at a the given index
    private static void removeFromHints(int[][] hints, int index, int number){
        if(!isElement(hints[index],number)) {
            return ;
        }
        int newSubarrayLength = hints[index].length-1;
        int[] newSubarray = new int[newSubarrayLength];
        int count =0;
        for (int i=0;i<hints[index].length;i++) {
            if (hints[index][i] != number) {
                newSubarray[count]=hints[index][i];
                count++;
            }
        }
        hints[index]=newSubarray;

    }
    // a method that checks if elements are aligned vertically given that they are aligned
    private static boolean isAlignedVertically(int[][] squareHints, int number) {
        int[][] column1 = { squareHints[0],squareHints[3],squareHints[6]};
        int[][] column2 = { squareHints[1],squareHints[4],squareHints[7]};
        int[][] column3 = { squareHints[2],squareHints[5],squareHints[8]};
        int countColumn1 = containsNumber(column1,number);
        int countColumn2 = containsNumber(column2,number);
        int countColumn3 = containsNumber(column3,number);
        return (countColumn1 == 0 && countColumn2 == 0) || (countColumn1 == 0 && countColumn3 == 0) || (countColumn2 == 0 && countColumn3 == 0);
    }
    // a method that returns the number of hints that contains a certain number on a array of 9 elements
    private static int containsNumber(int[][] hints, int number) {
        //we have the count of numbers at 0, each time we find an element in a hint that is equal to the number we increase the count by one
        int count =0;
        //we iterate through each group of hints
        for (int[] hint : hints) {
            //we check if the element at i is null
            if (hint != null) {
                //if it is not null we check if it is an element
                if (isElement(hint, number)) {
                    count++;
                }

            }
        }
        return count;
    }
    // a method that returns a line of Hints
    private static int[][] lineHints(int[][] hints, int line){
        int[][] lineHints= new int[9][];
        for(int i=0;i<9;i++) {
            if (hints[9*(line -1)+i] != null) {
                lineHints[i]= new int[hints[9*(line -1)+i].length];
                lineHints[i]=hints[9*(line -1)+i];
            }
        }return lineHints;
    }
    // a method that returns a column of Hints
    private static int[][] columnHints(int[][] hints, int column){
        int[][] columnHints= new int[9][];
        for(int i=0;i<9;i++) {
            if (hints[column-1 +9*i] != null) {
                columnHints[i]=hints[column-1 +9*i];
            }
        }return columnHints;
    }
    // a method that returns a square of hints
    private static int[][] squareHints(int[][] hints, int square){
        int[][] squareHints= new int[9][];
        int index = getSquareIndex(square);
        for(int i=0;i<9;i++) {
            if(hints[index] != null) {
                squareHints[i]=new int[hints[index].length];
                squareHints[i]=hints[index];
            }
            if(((i+1)%3)==0) {
                index = index+ 7;
            }else {
                index++;
            }
        }return squareHints;
    }
    // a method that compares the elements of two sudoku and returns true if the elements are all the same
    private static boolean isSame(int[] firstSudoku, int[] secondSudoku) {
        for(int i=0;i<81;i++) {
            if(firstSudoku[i] != secondSudoku[i]) {
                return false;
            }
        }return true;
    }
    // a method that checks if the sudoku is solved
    private static boolean isNotSolved(int[] sudoku) {
        boolean isNotSolved = false;
        // we go through the array, if we find a zero this means the sudoku is not
        for(int i=0; i<81; i++) {
            if(sudoku[i]==0) {
                isNotSolved = true;
                break;
            }
        } return isNotSolved;
    }
    // a method that creates an array representing the a line of the sudoku
    private static int[] getLine(int[] sudoku, int line) {
        int[] newLine = new int[9] ;
        //we iterate through each element to initialize
        System.arraycopy(sudoku, 9 * (line - 1) , newLine, 0, 9);
        return newLine;
    }
    // a method that creates an array representing a column of the sudoku
    private static int[] getColumn(int[] sudoku, int column) {
        int[] newColumn = new int[9];
        int count = 0;
        for (int i=0; i<9; i++) {
            newColumn[i] = sudoku[(column-1)+count];
            count = count +9;
        }
        return newColumn;
    }
    // a method that creates an array representing a square in the sudoku
    private static int[] getSquare(int[] sudoku, int square) {
        //The squares are denoted as
        /* 1 2 3
         * 4 5 6
         * 7 8 9
         */
        int[] newSquare = new int[9];
        int index = getSquareIndex(square);

        for (int i=0;i<9;i++) {
            newSquare[i]=sudoku[index];
            if (i == 2 || i == 5) {
                index += 7;
            }else {
                index +=1;
            }
        }
        return newSquare;
    }
    // a method that gets the index of the first element of a square
    private static int getSquareIndex(int square) {
        int[] index = { 0, 3, 6, 27, 30 , 33 , 54, 57, 60 };
        return index[square-1];
    }
    // a method that returns the number of zeros in a line
    private static int numberZeros(int[] line) {
        int count =0;
        for (int i=0; i<9;i++) {
            if (line[i]==0) {
                count ++;
            }
        }
        return count;
    }
    //we have an array with 81 elements
    // method that prints the sudoku
    private static void printSudoku(int[] sudoku) {

        for (int i=0; i<81;i++) {
            if (sudoku[i] == 0) {
                System.out.print("_ ");
            }else {
                System.out.print(sudoku[i]+" ");
            }
            if ((i+1)%9 ==0) {
                System.out.println();
            }
        }
    }

    //a method that creates the hints ( all possible values a 0 could take )
    private static int[][] createHints(int[] sudoku){
        int[][] hints = new int[81][];
        for(int i=0;i<81;i++) {
            if(sudoku[i]==0) {
                int[] placement = placement(i);
                int[] line = partialHint(getLine(sudoku,placement[0]));
                int[] column = partialHint(getColumn(sudoku,placement[1]));
                int[] square = partialHint(getSquare(sudoku,placement[2]));
                hints[i]= new int[subarrayLength(line,column,square)];
                int count=0;
                for (int j=1;j<=9;j++) {
                    if (isElement(line,j)&&isElement(column,j)&&isElement(square,j)) {
                        hints[i][count]= j;
                        count++;
                    }
                }
            }
        }
        return hints;
    }
    // a method that returns true if a number is element of an array
    private static boolean isElement(int[] array, int number) {
        if (array == null) {
            return false;
        }
        //we iterate through all the element and compare them with our number
        for (int i1 : array) {
            if (i1 == number) {
                return true;
            }
        }
        return false;
    }
    // a method that returns the possible values of an hint for an array( line, column or square)
    private static int[] partialHint(int[] array) {
        // the length of the array is the number of zeros on the line
        int[] partialHint = new int[numberZeros(array)];
        if (numberZeros(array)==0) {
            return null;
        }
        int count = 0;
        if (isArrayLegal(array)) {
            for(int i=1;i<=9;i++) {
                if(!isElement(array, i)) {
                    partialHint[count]= i;
                    count++;
                }
            }
        }return partialHint;
    }
    //a method that checks if an array (line, column, square) is okay
    private static boolean isArrayLegal(int[] array) {
        for (int i=1;i<=9;i++) {
            int count =0;
            for (int i1 : array) {
                if (i1 == i) {
                    count++;
                }
            }
            if (count>=2) {
                throw new IllegalArgumentException("The sudoku is not solvable, "+ i +" is in the array" +count+"times");
            }
        }
        return true;

    }
    //a method that returns the number of same elements of 3 arrays
    private static int subarrayLength(int[] lineHints, int[] columnHints, int[] squareHints) {
        int count=0;
        for (int i=1;i<=9;i++) {
            if(isElement(lineHints,i)&&isElement(columnHints,i)&&isElement(squareHints,i)) {
                count++;
            }
        }return count;
    }
    // a method that initializes the column
    private static int initializeColumn(int i, int columnSquare) {
        int column=0;
        if(i==1||i==4||i==7) {
            column=columnSquare;
        }
        if(i==2||i==5||i==8) {
            column=columnSquare+3;
        }if(i==3||i==6||i==9){
            column=columnSquare+6;
        }
        return column;

    }
    // a method that returns which column, line and square an element is in
    private static int[] placement(int index) {
        int[][] lines= {{0,1,2,3,4,5,6,7,8},
                {9,10,11,12,13,14,15,16,17},
                {18,19,20,21,22,23,24,25,26},
                {27,28,29,30,31,32,33,34,35},
                {36,37,38,39,40,41,42,43,44},
                {45,46,47,48,49,50,51,52,53},
                {54,55,56,57,58,59,60,61,62},
                {63,64,65,66,67,68,69,70,71},
                {72,73,74,75,76,77,78,79,80}};
        int[][] columns= {{0,9,18,27,36,45,54,63,72},
                {1,10,19,28,37,46,55,64,73},
                {2,11,20,29,38,47,56,65,74},
                {3,12,21,30,39,48,57,66,75},
                {4,13,22,31,40,49,58,67,76},
                {5,14,23,32,41,50,59,68,77},
                {6,15,24,33,42,51,60,69,78},
                {7,16,25,34,43,52,61,70,79},
                {8,17,26,35,44,53,62,71,80}};
        int[][] squares= {{0,1,2,9,10,11,18,19,20},
                {3,4,5,12,13,14,21,22,23},
                {6,7,8,15,16,17,24,25,26},
                {27,28,29,36,37,38,45,46,47},
                {30,31,32,39,40,41,48,49,50},
                {33,34,35,42,43,44,51,52,53},
                {54,55,56,63,64,65,72,73,74},
                {57,58,59,66,67,68,75,76,77},
                {60,61,62,69,70,71,78,79,80}};
        int[] placement = new int[3];
        for(int i=0;i<9;i++) {
            if(isElement(lines[i],index)) {
                placement[0]= i+1;
                break;
            }
        }
        for(int i=0;i<9;i++) {
            if(isElement(columns[i],index)) {
                placement[1]= i+1;
                break;
            }
        }
        for(int i=0;i<9;i++) {
            if(isElement(squares[i],index)) {
                placement[2]= i+1;
                break;
            }
        }
        return placement;
    }

    public static void main(String[] args) {

        //test to solve an extreme sudoku
        Sudoku extremeSudoku = new Sudoku("extremeSudoku");
        extremeSudoku.solveSudoku();
    }


}


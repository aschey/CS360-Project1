import java.io.*;
import java.util.*;

/**
 * Austin Schey
 * Project 1
 * 09/17/2015
 * CS 360
 */

enum SortData {
    SSN,
    BIRTH_DATE
}

enum SortType {
    QUICKSORT("Quicksort"),
    RADIX_SORT("Radix Sort");

    private final String text;

    SortType(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}

public class Project1 {
    public static void main(String[] args) {
        Sort sort = new Sort();
        // Parse command line args and read data file
        sort.readData(args);
        // Run quicksort and radix sort
        sort.runSorts();
    }
}

class Person {
    private static final int BIRTHDAY_LENGTH = 8;
    private static final int SSN_LENGTH = 9;

    private static SortData compareType = SortData.BIRTH_DATE;
    private static int compareLength = BIRTHDAY_LENGTH;

    private String lastName;
    private String birthDate;
    private String SSN;

    private int compareValue;

    public Person(String lastName, String birthDate, String SSN) {
        // Make sure strings are in the correct format
        if (!this.validateBirthday(birthDate)) {
            Utilities.exitWithError("Invalid birth date- " + birthDate);
        }
        if (!this.validateSSN(SSN)) {
            Utilities.exitWithError("Invalid SSN- " + SSN);
        }

        this.lastName = lastName;
        this.birthDate = birthDate;
        this.SSN = SSN;

        // Store the value to sort with as an int for easier access
        this.compareValue = (Person.compareType == SortData.BIRTH_DATE) ?
            Integer.parseInt(birthDate) : Integer.parseInt(SSN.replace("-", ""));
    }

    public static void setCompareType(SortData compare) {
        /**
         * Sets the type of data to compare as well as
         * the length of the value (8 for birthday, 9 for SSN)
         */
        Person.compareType = compare;
        Person.compareLength = (compare == SortData.BIRTH_DATE) ? BIRTHDAY_LENGTH : SSN_LENGTH;
    }

    public static int getCompareLength() {
        /**
         * Returns the length of the value used to make comparisons (SSN or birthday)
         */
        return compareLength;
    }

    public int getCompareValue(int start, int end) {
        /**
         * returns a "slice" of the compare value from start (inclusive) to end (exclusive)
         * Note: "start" and "end" values do not depend on input size (they must be between
         * 0 and 9), so this function has no effect on complexity
         */
        // Remove the portion from the beginning to "start"
        int intSlice = this.compareValue % (int)Math.pow(10, compareLength - start);
        // Remove the portion from "end" to the end
        intSlice /= (int)Math.pow(10, compareLength - end);
        return intSlice;
    }

    public int getCompareValue() {
        /**
         * Returns the value used to make comparisons
         */
        return this.compareValue;
    }

    @Override
    public String toString() {
        /**
         * Returns a string representation of the Person
         */
        return this.lastName + " " + this.SSN + " DOB: " + this.formatBirthday();
    }

    private String formatBirthday() {
        /**
         * Returns a slash-delimited representation of the birthday
         */
        String month = this.birthDate.substring(0, 2);
        String day = this.birthDate.substring(2, 4);
        String year = this.birthDate.substring(4);
        return String.join("/", month, day, year);
    }

    private boolean validateBirthday(String birthday) {
        /**
         * Makes sure the birthday is 8 digits long
         */
        String regex = "^\\d{8}$";
        return birthday.matches(regex);
    }

    private boolean validateSSN(String SSN) {
        /**
         * Makes sure the SSN is formatted as NNN-NN-NNNN
         */
        String regex = "^\\d{3}-\\d{2}-\\d{4}$";
        return SSN.matches(regex);
    }
}

class Sort {
    private int numRecords;
    private int numIterations;
    private Person[] unsortedData;
    private int numComparisons;
    private int numAssignments;

    public Sort() {
        this.numComparisons = 0;
        this.numAssignments = 0;
    }

    public void parseArgs(String[] args) {
        /**
         * Parses the command line arguments
         */
        int bIndex = Utilities.indexOf(args, "-B");
        int sIndex = Utilities.indexOf(args, "-S");

        if (bIndex > -1 && sIndex > -1) {
            Utilities.exitWithError("Cannot choose both -B and -S");
        }

        if (bIndex > -1) {
            Person.setCompareType(SortData.BIRTH_DATE);
        }
        else if (sIndex > -1) {
            Person.setCompareType(SortData.SSN);
        }
        else {
            Utilities.exitWithError("Sort type not specified");
        }

        int numIterationsIndex = Utilities.indexOf(args, "-n");

        if (numIterationsIndex < 0) {
            Utilities.exitWithError("Number of iterations flag missing");
        }

        // Value after -n must be missing if we're already at the end
        if (numIterationsIndex == (args.length - 1)) {
            Utilities.exitWithError("Number of iterations value missing");
        }

        int iterationsValueIndex = numIterationsIndex + 1;
        try {
            this.numIterations = Integer.parseInt(args[iterationsValueIndex]);
            if (this.numIterations < 1) {
                Utilities.exitWithError("Number of iterations must be a positive integer");
            }
        }
        catch (NumberFormatException ex) {
            Utilities.exitWithError("Number of iterations value invalid");
        }

        final int EXPECTED_ARGS_LENGTH = 3;
        // If there are more than 3 arguments, there must be an unnecessary one somewhere
        if (args.length > EXPECTED_ARGS_LENGTH) {
            for (int i = 0; i < args.length; i++) {
                if (i != bIndex && i != sIndex && i != numIterationsIndex && i != iterationsValueIndex) {
                    Utilities.exitWithError("Argument \"" + args[i] + "\" not recognized or is a duplicate");
                }
            }
        }
    }

    public void readData(String[] args) {
        /**
         * Reads data from the input file and stores it
         */
        this.parseArgs(args);

        try (Scanner scan = new Scanner(new File("personnel.csv"))) {
            // Don't tokenize on spaces
            scan.useDelimiter("\n");
            try {
                // The number of records should be the first line in the file
                this.numRecords = scan.nextInt();
            }
            catch (NumberFormatException ex) {
                Utilities.exitWithError("Number of records not specified");
            }
            this.unsortedData = new Person[this.numRecords];
            // Keep i accessible outside the loop for error handling
            int i = 0;
            try {
                for (i = 0; i < this.numRecords; i++) {
                    String[] line = scan.next().split(",");
                    String name = line[0];
                    String birthday = line[1];
                    String SSN = line[2];

                    unsortedData[i] = new Person(name, birthday, SSN);
                }
            }
            // Catch any errors thrown because the number of records parameter is incorrect
            // or the current line is missing a field
            catch (InputMismatchException | ArrayIndexOutOfBoundsException ex) {
                // Add 2 to the line number because of zero indexing and the line for input size
                final int OFFSET = 2;
                Utilities.exitWithError("Line " + (i + OFFSET) + " malformed");
            }
        }
        catch (FileNotFoundException ex) {
            Utilities.exitWithError("CSV file not found");
        }
    }

    public void runSorts() {
        /**
         * Runs both Quicksort and Radix Sort
         */
        this.runSort(SortType.QUICKSORT);
        this.runSort(SortType.RADIX_SORT);
    }

    private void runSort(SortType sortType) {
        /**
         * Runs the specified sort on the unsorted data
         */
        // How many records to sort per iteration
        int interval = (int)Math.ceil((double)this.numRecords / this.numIterations);
        Person[] sortArray = new Person[interval];
        int[] comparisons = new int[this.numIterations];
        int[] assignments = new int[this.numIterations];

        // Keep track of which iteration we're on to store comparisons and assignments
        int sortDataIndex = 0;
        for (int currentNumRecords = interval; currentNumRecords < (this.numRecords + interval); currentNumRecords += interval) {
            // Make sure we don't exceed the size of the array
            if (currentNumRecords > this.numRecords) {
                currentNumRecords = this.numRecords;
            }
            this.numComparisons = 0;
            this.numAssignments = 0;
            sortArray = this.deepCopy(this.unsortedData, 0, currentNumRecords);

            if (sortType == SortType.QUICKSORT) {
                this.quicksort(sortArray, currentNumRecords);
            }
            else {
                // Need to reassign the array for Radix Sort because the sorted array is stored in a different pointer
                sortArray = this.radixSort(sortArray, currentNumRecords);
            }
            comparisons[sortDataIndex] = this.numComparisons;
            assignments[sortDataIndex++] = this.numAssignments;
            if (!this.testCorrectness(sortArray)) {
                Utilities.exitWithError("Not sorted");
            }
        }
        this.printResults(sortArray, this.numRecords, assignments, comparisons, this.numIterations, sortType);
    }

    private void swap(Person[] array, int swapIndex1, int swapIndex2) {
        /**
         * Swaps array values at index1 and index2
         */
        Person temp = array[swapIndex1];
        array[swapIndex1] = array[swapIndex2];
        array[swapIndex2] = temp;
    }

    private Person[] deepCopy(Person[] copyArray, int start, int numRecords) {
        /**
         * Returns a deep copy of the array
         */
        Person[] newArray = new Person[numRecords];
        for (int i = 0; i < numRecords; i++) {
            newArray[i] = copyArray[i + start];
        }
        return newArray;
    }

    // TODO: remove this before turning in
    private boolean testCorrectness(Person[] sortedArray) {
        for (int i = 0; i < sortedArray.length - 1; i++) {
            if (sortedArray[i].getCompareValue() > sortedArray[i+1].getCompareValue()) {
                return false;
            }
        }
        return true;
    }

    private void printResults(Person[] sortedArray, int sortedLength, int[] assignments, int[] comparisons,
                              int dataLength, SortType sortType) {
        /**
         * Prints the contents of the sorted array as well as the assignments and comparisons
         */
        System.out.println("* " + sortType);
        System.out.println("- Sorted array");

        this.printPeople(sortedArray, sortedLength);

        System.out.println();

        System.out.println("- Assignments");
        this.printSortData(assignments, dataLength);

        System.out.println();

        System.out.println("- Comparisons");
        this.printSortData(comparisons, dataLength);
        System.out.println();
        System.out.println();
    }

    private void printSortData(int[] data, int dataLength) {
        /**
         * Prints the array all on one line
         */
        for (int i = 0; i < dataLength; i++)
            System.out.print(data[i] + " ");
    }

    private void printPeople(Person[] people, int peopleLength) {
        /**
         * Prints the array with one record per line
         */
        for (int i = 0; i < peopleLength; i++) {
            System.out.println(people[i]);
        }
    }

    private void quicksort(Person[] sortArray, int length) {
        /**
         * Wrapper for recursive Quicksort function
         */
        this.quicksortRec(sortArray, 0, length);
    }

    private void quicksortRec(Person[] sortArray, int low, int high) {
        /**
         * Performs quicksort on the array from index "low" (inclusive) to "high" (exclusive)
         */
        // Base case: low == high (array is size 0)
        if (low < (high - 1)) {
            // Get the index of the pivot
            int mid = this.partition(sortArray, low, high);
            // Sort the lower half
            this.quicksortRec(sortArray, low, mid);
            // Sort the upper half
            this.quicksortRec(sortArray, mid + 1, high);
        }
    }

    private int getRandomPivotIndex(int min, int max) {
        /**
         * Returns a random value from "min" (inclusive) to "max" (exclusive)
         */
        // Return an int from min to max - 1
        Random rand = new Random();
        int range = max - min;
        return rand.nextInt(range) + min;
    }

    private int partition(Person[] sortArray, int low, int high) {
        /**
         * Partitions the array so every value to the left of the "pivot" value is
         * less than the pivot and every value to the right is greater than it,
         * then returns the pivot index
         */
        // Use a random pivot to reduce the chance of a T(n-1) recurrence
        int pivotIndex = this.getRandomPivotIndex(low, high);
        this.numAssignments += 2;
        // Put the pivot value at the end of the array
        this.swap(sortArray, pivotIndex, high - 1);
        Person pivot = sortArray[high - 1];
        int i = low - 1;
        for (int j = low; j < (high - 1); j++) {
            this.numComparisons++;
            // Put the values less than the pivot to the left of it and
            // the values greater than it to the right
            if (sortArray[j].getCompareValue() < pivot.getCompareValue()) {
                i++;
                this.numAssignments += 2;
                this.swap(sortArray, i, j);
            }
        }
        i++;
        this.numAssignments += 2;
        // Put the pivot in place
        this.swap(sortArray, i, high - 1);
        return i;
    }

    private Person[] radixSort(Person[] sortArray, int arrayLength) {
        /**
         * Performs a Radix Sort on the array
         */
        // Choose how the length of each array to perform counting sort on
        int partitionLength = this.getPartitionLength(arrayLength);
        int firstStart =  Person.getCompareLength() - partitionLength;
        // Start at the end of the array and work backwards, sorting on subarrays of size partitionLength
        // We need to keep iterating past zero because the compare value's length may not be divisible by
        // the partition length
        for (int start = firstStart; start >= (-1 * partitionLength + 1); start -= partitionLength) {
            int checkedStart;
            int end = start + partitionLength;
            // If the compare value's length is not divisible by the partition length,
            // the final subarray will start at a negative index
            if (start < 0) {
                checkedStart = 0;
            }
            else {
                checkedStart = start;
            }
            // Use Counting Sort because it is a stable sort that runs in theta(n+k) time,
            // causing Radix Sort to run in an optimal theta(d(n+k)) time
            sortArray = this.countingSort(sortArray, arrayLength, checkedStart, end);
        }
        return sortArray;
    }

    private int getPartitionLength(int arraySize) {
        /**
         * Calculates the partition length as described in Lemma 8.4 in the book
         * Using lg(n) as the partition size yields the most optimal results as
         * it reduces the complexity of Radix Sort to theta(n/lg(n))
         *
         * If we needed to handle input sizes greater than 2^32 - 1,
         * which we do not because that is the maximum size for an integer,
         * we would choose 32 (number of bits in an integer) as the partition size
         * because the 2^r term begins to overshadow the b/r term in the complexity
         * of Radix Sort, which is theta((b/r)(n+2^r)).
         * Again, we can ignore this case because the input will never be that large.
         */
        // Number of bits in an integer
        final double NUM_BITS = 32.0;
        double partitionSize = Math.floor(Math.log(arraySize) / Math.log(2));
        // Choose the most efficient partition size based on log of the input size
        double numPasses = Math.ceil(NUM_BITS / partitionSize);

        double compareLength = (double)Person.getCompareLength();

        // Make sure the number of passes won't be less than 1 or greater than
        // the number of digits in the compare value
        if (numPasses < 1.0) {
            numPasses = 1.0;
        }
        else if (numPasses > compareLength) {
            numPasses = compareLength;
        }

        // Return the length of each subarray
        return (int)Math.ceil(compareLength / numPasses);
    }

    private Person[] countingSort(Person[] unsorted, int arrayLength, int start, int end) {
        /**
         * Performs counting sort on the array, sorting digits within the range from
         * "start" (inclusive) to "end" exclusive
         */
        // Get the max compare value in the relevant digits of the compare values
        int maxValue = this.getMaxPersonValue(unsorted, arrayLength, start, end);
        // The length of the helper is one more than "maxValue" because indexing starts at 0, not 1
        int helperLength = maxValue + 1;
        int[] helper = new int[helperLength];

        Person[] sorted = new Person[arrayLength];

        // Initialize helper values
        for (int i = 0; i <= maxValue; i++) {
            this.numAssignments++;
            helper[i] = 0;
        }

        // Count how many of each value there are
        for (int i = 0; i < arrayLength; i++) {
            this.numAssignments++;
            helper[unsorted[i].getCompareValue(start, end)]++;
        }

        // Accumulate values so that helper[i] contains how many values there are
        // from 0 to helper[i]
        for (int i = 1; i < helperLength; i++) {
            this.numAssignments++;
            helper[i] += helper[i - 1];
        }

        // Sort values and store them into the "sorted" array
        for (int i = arrayLength - 1; i >= 0; i--) {
            Person p = unsorted[i];
            int value = p.getCompareValue(start, end);
            this.numAssignments++;
            sorted[helper[value]-1] = p;
            this.numAssignments++;
            helper[value]--;
        }

        return sorted;
    }

    private int getMaxPersonValue(Person[] array, int arrayLength, int start, int end) {
        /**
         * Returns the max value in the array, consisting only of the digits from "start" to "end"
         */
        int maxValue = 0;
        for (int i = 0; i < arrayLength; i++) {
            int compareValue = array[i].getCompareValue(start, end);
            if (compareValue > maxValue) {
                maxValue = compareValue;
            }
        }
        return maxValue;
    }
}

class Utilities {
    static void exitWithError(String errorMessage) {
        /**
         * Exits the program completely, displaying the error message
         */
        System.out.println("Error: " + errorMessage);
        System.exit(0);
    }

    static int indexOf(String[] array, String value) {
        /**
         * Returns the index of the given value,
         * or -1 if it does not exist
         */
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) {
                return i;
            }
        }
        return -1;
    }
}
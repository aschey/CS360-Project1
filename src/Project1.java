import javax.rmi.CORBA.Util;
import java.io.*;
import java.util.*;

/**
 * Created by aschey on 9/2/15.
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
        if (birthDate.contains("-") || birthDate.length() != Person.BIRTHDAY_LENGTH) {
            Utilities.exitWithError("Data file contains invalid birth date");
        }

        if (!SSN.contains("-") || SSN.length() != Person.SSN_LENGTH) {
            Utilities.exitWithError("Data file contains invalid SSN");
        }
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.SSN = SSN;

        // Store the value to sort with as an int for easier access
        this.compareValue = (Person.compareType == SortData.BIRTH_DATE) ?
            Integer.parseInt(birthDate) : Integer.parseInt(SSN.replace("-", ""));
    }

    public static void setCompareType(SortData compare) {
        Person.compareType = compare;
        Person.compareLength = (compare == SortData.BIRTH_DATE) ? BIRTHDAY_LENGTH : SSN_LENGTH;
    }

    public static int getCompareLength() {
        return compareLength;
    }

    public int getCompareValue(int start, int end) {
        // Remove the portion from the beginning to "start"
        int intSlice = this.compareValue % (int)Math.pow(10, compareLength - start);
        // Remove the portion from "end" to the end
        intSlice /= (int)Math.pow(10, compareLength - end);
        return intSlice;
    }

    public int getCompareValue() {
        return this.compareValue;
    }

    @Override
    public String toString() {
        return this.lastName + " " + this.SSN + " DOB: " + this.formatBirthday();
    }

    private String formatBirthday() {
        String month = this.birthDate.substring(0, 2);
        String day = this.birthDate.substring(2, 4);
        String year = this.birthDate.substring(4);
        return String.join("/", month, day, year);
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

        // If there are more than 3 arguments, there must be an unnecessary one
        if (args.length > 3) {
            for (int i = 0; i < args.length; i++) {
                if (i != bIndex && i != sIndex && i != numIterationsIndex && i != iterationsValueIndex) {
                    Utilities.exitWithError("Argument \"" + args[i] + "\" not recognized");
                }
            }
        }
    }

    public void readData(String[] args) {
        this.parseArgs(args);
//        File f = new File("personnel.csv");
//        try {
//            BufferedReader br = new BufferedReader(new FileReader(f));
//            try {
//                br.readLine();
//                this.numRecords = 341516255;
//                this.unsortedData = new Person[this.numRecords];
//                String line;
//                int i = 0;
//                while ((line = br.readLine()) != null) {
//                    String[] l = line.split(",");
//                    this.unsortedData[i] = new Person(l[0], l[1], l[2]);
//                    i++;
//                }
//            }
//            catch (IOException ex) {
//                this.exitWithError("IOException");
//            }
//        }
//        catch (FileNotFoundException ex) {
//            this.exitWithError("File not found");
//        }


        try (Scanner scan = new Scanner(new File("personnel.csv"))) {
            //scan.useDelimiter(",|\n");
            try {
                this.numRecords = scan.nextInt();
            }
            catch (NumberFormatException ex) {
                Utilities.exitWithError("Number of records not specified");
            }
            this.unsortedData = new Person[this.numRecords];
            try {
                for (int i = 0; i < this.numRecords; i++) {
                    String[] line = scan.next().split(",");
                    String name = line[0];
                    String birthday = line[1];
                    String SSN = line[2];

                    unsortedData[i] = new Person(name, birthday, SSN);
                }
            }
            catch (InputMismatchException | ArrayIndexOutOfBoundsException ex) {
                Utilities.exitWithError("Malformed input file");
            }
        }
        catch (FileNotFoundException ex) {
            Utilities.exitWithError("CSV file not found");
        }
    }

    public void runSorts() {
        this.runSort(SortType.QUICKSORT);
        this.runSort(SortType.RADIX_SORT);
    }

    private void runSort(SortType sortType) {
        int interval = (int)Math.ceil((double)this.numRecords / this.numIterations);
        Person[] sortArray = new Person[interval];
        int[] comparisons = new int[this.numIterations];
        int[] assignments = new int[this.numIterations];

        int sortDataIndex = 0;
        for (int currentNumRecords = interval; currentNumRecords < (this.numRecords + interval); currentNumRecords += interval) {
            if (currentNumRecords > this.numRecords) {
                currentNumRecords = this.numRecords;
            }
            this.numComparisons = 0;
            this.numAssignments = 0;
            sortArray = this.deepCopy(this.unsortedData, 0, currentNumRecords);

            if (sortType == SortType.QUICKSORT) {
                this.quickSort(sortArray, currentNumRecords);
            }
            else {
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
        Person temp = array[swapIndex1];
        array[swapIndex1] = array[swapIndex2];
        array[swapIndex2] = temp;
    }

    private Person[] deepCopy(Person[] copyArray, int start, int numRecords) {
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

    private void printResults(Person[] sortedArray, int sortedLength, int[] assignments, int[] comparisons, int dataLength, SortType sortType) {
        System.out.println(sortType);
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
        for (int i = 0; i < dataLength; i++)
            System.out.print(data[i] + " ");
    }

    private void printPeople(Person[] people, int peopleLength) {
        for (int i = 0; i < peopleLength; i++) {
            System.out.println(people[i]);
        }
    }

    private void quickSort(Person[] sortArray, int length) {
        this.quickSortRec(sortArray, 0, length);
    }

    private void quickSortRec(Person[] sortArray, int low, int high) {
        if (low < (high - 1)) {
            int mid = this.partition(sortArray, low, high);
            this.quickSortRec(sortArray, low, mid);
            this.quickSortRec(sortArray, mid + 1, high);
        }
    }

    private int getRandomPivotIndex(int min, int max) {
        Random rand = new Random();
        int range = max - min;
        return rand.nextInt(range) + min;
    }

    private int partition(Person[] sortArray, int low, int high) {
        int pivotIndex = this.getRandomPivotIndex(low, high);
        this.swap(sortArray, pivotIndex, high - 1);
        Person pivot = sortArray[high - 1];
        int i = low - 1;
        for (int j = low; j < (high - 1); j++) {
            this.numComparisons++;
            if (sortArray[j].getCompareValue() < pivot.getCompareValue()) {
                i++;
                //System.out.println(i + " " + j);
                this.numAssignments += 2;
                this.swap(sortArray, i, j);
            }
        }
        i++;
        this.numAssignments += 2;
        this.swap(sortArray, i, high - 1);
        return i;
    }

    private Person[] radixSort(Person[] sortArray, int arrayLength) {
        int partitionLength = this.getPartitionLength(arrayLength);
        //System.out.println("partition length = " + partitionLength);
        int firstStart =  Person.getCompareLength() - partitionLength;
        for (int start = firstStart; start >= (-1 * partitionLength + 1); start -= partitionLength) {
            int checkedStart;
            int end = start + partitionLength;
            if (start < 0) {
                checkedStart = 0;
                //System.out.println(subarraySize);
            }
            else {
                checkedStart = start;
            }
            sortArray = this.countingSort(sortArray, arrayLength, checkedStart, end);
        }
        return sortArray;
    }

    private int getPartitionLength(int arraySize) {
        final double NUM_BITS = 32.0;
        double lgn = Math.floor(Math.log(arraySize) / Math.log(2));
        double partitionSize = (NUM_BITS < lgn) ? NUM_BITS : lgn;
        double numPasses = Math.ceil(NUM_BITS / partitionSize);

        double compareLength = (double)Person.getCompareLength();

        if (numPasses < 1.0) {
            numPasses = 1.0;
        }
        else if (numPasses > compareLength) {
            numPasses = compareLength;
        }
        return (int)Math.ceil(compareLength / numPasses);
    }

    private Person[] countingSort(Person[] unsorted, int arrayLength, int start, int end) {
        int maxValue = this.getMaxPersonValue(unsorted, arrayLength, start, end);
        int helperLength = maxValue + 1;
        int[] helper = new int[helperLength];

        Person[] sorted = new Person[arrayLength];

        for (int i = 0; i <= maxValue; i++) {
            this.numAssignments++;
            helper[i] = 0;
        }

        for (int i = 0; i < arrayLength; i++) {
            this.numAssignments++;
            helper[unsorted[i].getCompareValue(start, end)]++;
        }

        for (int i = 1; i < helperLength; i++) {
            this.numAssignments++;
            helper[i] += helper[i - 1];
        }

        for (int i = arrayLength - 1; i >= 0; i--) {
            Person p = unsorted[i];
            int value = p.getCompareValue(start, end);
            this.numAssignments++;
            //System.out.println(helper[value]);
            sorted[helper[value]-1] = p;
            this.numAssignments++;
            helper[value]--;
        }

        return sorted;
    }

    private int getMaxPersonValue(Person[] array, int arrayLength, int start, int end) {
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
        System.out.println("Error: " + errorMessage);
        System.exit(0);
    }

    static int indexOf(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) {
                return i;
            }
        }
        return -1;
    }
}
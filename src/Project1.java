import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by aschey on 9/2/15.
 */

enum SortData {
    SSN,
    BIRTH_DATE
}

enum SortType {
    QUICKSORT,
    RADIX_SORT
}

public class Project1 {

    public static void main(String[] args) {
        Sort sort = new Sort();
        sort.readData(args);
        sort.runSorts();
    }
}

class Person implements Comparator<Person>, Comparable<Person> {
    public static SortData compareType = SortData.BIRTH_DATE;
    public String lastName;
    public String SSN;
    public String birthDate;
    private int compareValue;

    public Person(String lastName, String SSN, String birthDate) {
        this.lastName = lastName;
        this.SSN = SSN;
        this.birthDate = birthDate;
        if (Person.compareType == SortData.BIRTH_DATE) {
            String reorderedBirthDate = birthDate.substring(4) + birthDate.substring(0,4);
            this.compareValue = Integer.parseInt(reorderedBirthDate);
        }
        else {
            this.compareValue = Integer.parseInt(SSN.replace("-", ""));
        }
    }

    public int getCompareValue() {
        return compareValue;
    }

    @Override
    public int compareTo(Person p) {
        return Integer.compare(this.compareValue, p.compareValue);
    }

    @Override
    public int compare(Person p1, Person p2) {
        return Integer.compare(p1.compareValue, p2.compareValue);
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

    private void exitWithError(String errorMessage) {
        System.out.println("Error: " + errorMessage);
        System.exit(0);
    }

    private void swap(Person[] array, int swapIndex1, int swapIndex2) {
        Person temp = array[swapIndex1];
        array[swapIndex1] = array[swapIndex2];
        array[swapIndex2] = temp;
    }

    private int indexOf(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) {
                return i;
            }
        }
        return -1;
    }

    public void parseArgs(String[] args) {
        if (this.indexOf(args, "-B") > -1) {
            Person.compareType = SortData.BIRTH_DATE;
        }
        else if (this.indexOf(args, "-S") > -1) {
            Person.compareType = SortData.SSN;
        }
        else {
            this.exitWithError("Sort type not specified");
        }

        int numIterationsIndex = this.indexOf(args, "-n");
        if (numIterationsIndex < 0) {
            this.exitWithError("Number of iterations flag missing");
        }
        if (numIterationsIndex == (args.length - 1)) {
            this.exitWithError("Number of iterations value missing");
        }

        try {
            this.numIterations = Integer.parseInt(args[numIterationsIndex + 1]);
        }
        catch (NumberFormatException ex) {
            this.exitWithError("Number of iterations value invalid");
        }
    }

    public void readData(String[] args) {
        this.parseArgs(args);
        try (Scanner scan = new Scanner(new File("personnel.csv"))) {
            scan.useDelimiter(",|\n");
            this.numRecords = scan.nextInt();
            this.unsortedData = new Person[this.numRecords];
            for (int i = 0; i < this.numRecords; i++) {
                String name = scan.next();
                String SSN = scan.next();
                String birthday = scan.next();

                unsortedData[i] = new Person(name, SSN, birthday);
            }
        }
        catch (FileNotFoundException ex) {
            this.exitWithError("CSV file not found");
        }
    }

    public void runSorts() {
        this.runSort(SortType.QUICKSORT);
        //this.runSort(SortType.RADIX_SORT);
    }

    private void runSort(SortType sortType) {
        int interval = this.numRecords / this.numIterations;
        Person[] sortArray = new Person[interval];
        int[] comparisons = new int[this.numIterations];
        int[] assignments = new int[this.numIterations];

        int sortDataIndex = 0;
        for (int currentNumRecords = interval; currentNumRecords <= this.numRecords; currentNumRecords += interval) {
            this.numComparisons = 0;
            this.numAssignments = 0;
            sortArray = this.deepCopy(this.unsortedData, currentNumRecords);
            //System.out.println(Arrays.deepToString(sortArray));
            if (sortType == SortType.QUICKSORT) {
                this.quickSort(sortArray);
            }
            else {
                this.radixSort();
            }
            comparisons[sortDataIndex] = this.numComparisons;
            assignments[sortDataIndex++] = this.numAssignments;
            if (!this.testCorrectness(sortArray)) {
                this.exitWithError("Not sorted");
            }
            //this.printResults(sortArray, assignments, comparisons, "Quicksort");
        }
        this.printResults(sortArray, assignments, comparisons, "Quicksort");
    }

    private Person[] deepCopy(Person[] copyArray, int numRecords) {
        Person[] newArray = new Person[numRecords];
        for (int i = 0; i < numRecords; i++) {
            newArray[i] = copyArray[i];
        }
        return newArray;
    }

    // TODO: remove this before turning in
    private boolean testCorrectness(Person[] sortedArray) {
        for (int i = 0; i < sortedArray.length - 1; i++) {
            if (sortedArray[i].compareTo(sortedArray[i+1]) > 0) {
                return false;
            }
        }
        return true;
    }

    private void printResults(Person[] sortedArray, int[] assignments, int[] comparisons, String sortTitle) {
        System.out.println(sortTitle);
        System.out.println("- Sorted array");

        this.printPeople(sortedArray);

        System.out.println();

        System.out.println("- Assignments");
        this.printSortData(assignments);

        System.out.println();

        System.out.println("- Comparisons");
        this.printSortData(comparisons);
    }

    private void printSortData(int[] data) {
        for (int i = 0; i < data.length; i++)
            System.out.print(data[i] + " ");
    }

    private void printPeople(Person[] people) {
        for (int i = 0; i < people.length; i++) {
            System.out.println(people[i]);
        }
    }

    public void quickSort(Person[] sortArray) {
        this.quickSortRec(sortArray, 0, sortArray.length);
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
            if (sortArray[j].compareTo(pivot) <= 0) {
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

    public void radixSort() {

    }

    private Person[] countingSort(Person[] unsorted) {
        int maxValue = this.getMaxPersonValue();
        int[] helper = new int[maxValue];
        Person[] sorted = new Person[unsorted.length];

        for (int i = 0; i <= maxValue; i++) {
            helper[i] = 0;
        }

        for (int i = 0; i < unsorted.length; i++) {
            helper[unsorted[i].getCompareValue()]++;
        }

        for (int i = 1; i <= maxValue; i++) {
            helper[i] += helper[i - 1];
        }

        for (int i = 0; i < unsorted.length; i++) {
            Person p = unsorted[i];
            int value = p.getCompareValue();
            sorted[helper[value]] = p;
            helper[value]--;
        }

        return sorted;
    }

    private int getMaxPersonValue() {
        //return Arrays.stream(this.unsortedData).max(Comparator.<Person>naturalOrder()).get().getCompareValue();
        int maxValue = 0;
        for (int i = 0; i < this.unsortedData.length; i++) {
            int compareValue = this.unsortedData[i].getCompareValue();
            if (compareValue > maxValue) {
                maxValue = compareValue;
            }
        }
        return maxValue;
    }
}
public class MyGenerics_Program {

    public static <T> void printArray(T[] args) {
        for (int i = 0; i < args.length; i++) {
            T t = args[i];
            System.out.print(t + " ");
        }
        System.out.println();
    }

    public static void main(String[] args) {

        Integer[] intArray = {10, 20, 30, 40};
        Double[] doubleArray = {1.1, 2.2, 3.3, 4.4};
        String[] stringArray = {"aa", "bb", "cc", "dd"};

        System.out.println("Integer Array");
        printArray(intArray);

        System.out.println("Double Array");
        printArray(doubleArray);

        System.out.println("String Array");
        printArray(stringArray);
    }
}
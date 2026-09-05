public class MyGeneric3 {
 
    static <T> boolean isContained(T[] arr, T element) {
            for (T curElement : arr) {
            if (curElement.equals(element)) {
            return true;
        }
}
            return false;
}
    
public static void main(String[] args) {
     Integer[] intArr = {1, 2, 3};
     System.out.println(isContained(intArr, 4));
     String[] stringArr = {"aa", "bb", "cc"};
System.out.println(isContained(stringArr, "bb"));
 
    }

}

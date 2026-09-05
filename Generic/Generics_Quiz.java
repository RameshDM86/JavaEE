public class Generics_Quiz {
	
	public static <T extends Comparable<T>> T max(T e1, T e2, T e3) {
	    T max = e1;

	    if (e2.compareTo(max)>0) {
	        max = e2;
	    }

	    if (e3.compareTo(max) > 0) {
	        max = e3;
	    }

	    return max;
	}

	public static void main(String[] args) {
	    System.out.println(max(3, 5, 2));                 // Integer
	    System.out.println(max(3.5, 2.1, 7.8));           // Double
	    System.out.println(max("Apple", "Orange", "Banana")); // String
	}

}

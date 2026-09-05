import java.util.ArrayList;
import java.util.List;

public class Generics_Quiz2 {
	
	static <T extends Number> double sum(List<T> list) {
		   double sum = 0;
		   for (T e : list) {
		      sum += e.doubleValue();
		   }
		   
		   return sum;
		}


	public static void main(String[] args) {
		   List<Integer> intList = new ArrayList<>();
		   intList.add(1);
		   intList.add(2);
		   intList.add(3);
		   System.out.println(sum(intList));
		   
		   List<Double> doubleList = new ArrayList<>();
		   doubleList.add(1.1);
		   doubleList.add(2.2);
		   doubleList.add(3.3);
		   System.out.println(sum(doubleList));


	}

}

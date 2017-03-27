
public class Main {

  public static void main(String[] args) {
    Integer i1 =100;            //use constant values
    Integer i2 =100;            //use small values

    if(i1 == i2)        //compare the references
      System.out.println("references are EQUAL !");
    else
      System.out.println("references are NOT equal !");

    i1 = 300;          //use bigger values
    i2 = 300;

    if(i1 == i2)
      System.out.println( "references are EQUAL !");
    else
      System.out.println("references are NOT equal !");
  }
}

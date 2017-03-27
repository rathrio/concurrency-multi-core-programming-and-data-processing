/**
 * Generic Printer Thread class  
 * Prints a string letter by letter with a delay between characters
 */
public class PrinterThread extends Thread {
	
	char[] text ;
	int delay ;
	
	/**
	 * @param toPrint - the string to be printed
	 * @param delay - the delay between printing the characters
	 */
	public PrinterThread (String toPrint, int delay) {
		text = toPrint.toCharArray();
		this.delay = delay;
	}
	
	public void run () {
		int i;
		
		try {
			for (i = 0; i < text.length ; i++) {
				System.out.print(text[i]+" ");
				Thread.sleep(delay);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}

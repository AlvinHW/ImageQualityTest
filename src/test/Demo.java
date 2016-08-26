package test;

public class Demo {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String filePath1 = "data/sample1.tif";
		String filePath2 = "data/sample2.tif";
		ImageQualityTest test = new ImageQualityTest();
		boolean quality1 = test.TestQuality(filePath1);
		boolean quality2 = test.TestQuality(filePath2);
		//if quality == true, the quality of this page is good 
		System.out.println(quality1);
		System.out.println(quality2);
	}

}

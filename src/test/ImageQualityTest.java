package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.POS;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;

public class ImageQualityTest {
	private static AbstractSequenceClassifier<CoreLabel> classifier;
	static {
		String serializedClassifier = "classifiers/english.muc.7class.distsim.crf.ser.gz";
		try {
			classifier = CRFClassifier.getClassifier(serializedClassifier);
		} catch (ClassCastException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ImageQualityTest() {
	}

	public boolean TestQuality(String filePath) {
		boolean good = false;
		File file = new File(filePath);
		String inputTiff = file.getAbsolutePath();
		String ocrPath = "data/OCRoutput";
		File ocroutputFolder = new File(ocrPath);
		if (!ocroutputFolder.exists()) {
			ocroutputFolder.mkdir();
		}
		String txtFile = ocrPath + "/" + file.getName().replace(".tif", "");
		String command = "tesseract/3.04.01_1/bin/tesseract " + inputTiff + " " + txtFile;
		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String txtOutput = "";
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(txtFile + ".txt"));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			txtOutput = sb.toString();
			br.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
		ArrayList<String> words = new ArrayList<String>();
		ArrayList<String> tagWords = new ArrayList<String>();
		ArrayList<String> dicWords = new ArrayList<String>();
		try {
			String[] example = txtOutput.split("\n");
			for (String str : example) {
				String temp_str1 = str.replace("<", "0");
				String temp_str2 = temp_str1.replace(">", "0");
				String xml = "<root>" + classifier.classifyToString(temp_str2, "xml", true) + "</root>";
				Document doc = DocumentHelper.parseText(xml);
				Element rootElt = doc.getRootElement();
				@SuppressWarnings("rawtypes")
				Iterator iter = rootElt.elementIterator("wi");
				while (iter.hasNext()) {
					Element recordEle = (Element) iter.next();
					words.add(recordEle.getText());

					if (!recordEle.attributeValue("entity").equals("O")) {
						tagWords.add(recordEle.getText());
					}
					if (check_for_word(recordEle.getText().toLowerCase())) {
						dicWords.add(recordEle.getText());
					}
				}
			}
		} catch (ClassCastException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		double ratio = ((double) tagWords.size() + (double) dicWords.size()) / words.size();
		// System.out.println(ratio);
		if (ratio > 0.6) {
			good = true;
		}
		return good;
	}

	public boolean check_for_word(String word) {
		boolean isWord = false;
		File sourcefile1 = new File("/usr/share/dict/web2");
		File sourcefile2 = new File("/usr/share/dict/american-english");
		if (sourcefile1.exists()) {
			try {
				BufferedReader in = new BufferedReader(new FileReader("/usr/share/dict/web2"));
				String str;
				while ((str = in.readLine()) != null) {
					if (str.indexOf(word) != -1) {
						return true;
					}
				}
				in.close();
			} catch (IOException e) {
			}

			return false;
		} else if (sourcefile2.exists()) {
			try {
				BufferedReader in = new BufferedReader(new FileReader("/usr/share/dict/american-english"));
				String str;
				while ((str = in.readLine()) != null) {
					if (str.indexOf(word) != -1) {
						return true;
					}
				}
				in.close();
			} catch (IOException e) {
			}
			return false;
		} else {
			URL url = null;
			try {
				url = new URL("file", null, "WordNet-3.0/dict");
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			IDictionary dict = new Dictionary(url);
			try {
				dict.open();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (!(dict.getIndexWord(word, POS.ADJECTIVE) == null)) {
				isWord = true;
			} else if (!(dict.getIndexWord(word, POS.ADVERB) == null)) {
				isWord = true;
			} else if (!(dict.getIndexWord(word, POS.NOUN) == null)) {
				isWord = true;
			} else if (!(dict.getIndexWord(word, POS.VERB) == null)) {
				isWord = true;
			} else {
				isWord = false;
			}
			return isWord;
		}

	}

}

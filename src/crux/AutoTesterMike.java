package crux;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

public class AutoTesterMike {
	// public static final String errorLogDir = ".";
	// public static final String resultFileName = "result.txt";

	public static final int PASS = 0;
	public static final int FAIL = 1;
	public static final int NOT_ACCESSABLE = 2;
	public static final int IO_ERROR = 3;
	public static final int SLEEP_ERROR = 4;

	public static int testPublic(int testNum) {
		String inputFilename = String.format("tests/test%02d.crx", testNum);
		String outputFilename = String.format("tests/test%02d.rea", testNum);
		String expectedFilename = String.format("tests/test%02d.out", testNum);

		Scanner s = null;
		try {
			s = new Scanner(new FileReader(inputFilename));
		} catch (IOException e) {
			e.printStackTrace();
			return NOT_ACCESSABLE;
			// System.err.println("Error accessing the source file: \"" +
			// inputFilename + "\"");
			// System.exit(2);
		}

		try {
			PrintStream outputStream = new PrintStream(outputFilename);
			Parser p = new Parser(s);
			p.parse();
			if (p.hasError()) {
				System.out.println(p.errorReport());
				outputStream.println("Error parsing file.");
				outputStream.println(p.errorReport());
				outputStream.close();
				// System.exit(-3);
			}
			outputStream.println(p.parseTreeReport());
			outputStream.close();

		} catch (IOException e) {
			System.err.println("Error opening output file: \"" + outputFilename + "\"");
			e.printStackTrace();
			return IO_ERROR;
		}

		BufferedReader bufferedexpected;
		BufferedReader bufferedoutput;

		String lineExpected;
		String lineOutput;

		try {
			bufferedexpected = new BufferedReader(new FileReader(expectedFilename));
			bufferedoutput = new BufferedReader(new FileReader(outputFilename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return IO_ERROR;
		}

		int result = PASS;

		try {
			while ((lineExpected = bufferedexpected.readLine()) != null) {
				lineOutput = bufferedoutput.readLine();
				if (lineOutput == null) {
					result = FAIL;
					break;
				}
				lineExpected = lineExpected.replaceAll("\\s+$", "");
				lineOutput = lineOutput.replaceAll("\\s+$", "");
				if (!lineExpected.equals(lineOutput)) {
					result = FAIL;
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			result = IO_ERROR;
		}

		try {
			bufferedoutput.close();
			bufferedexpected.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;

		// try {
		// Process diff = Runtime.getRuntime().exec(
		// "diff -w -B -b -i --strip-trailing-cr " + outputFilename
		// + " " + expectedFilename);
		// Thread.sleep(100);
		// BufferedReader diffout = new BufferedReader(new InputStreamReader(
		// diff.getInputStream()));
		// int count = 0;
		// int tag = 0;
		// while (diffout.ready()) {
		// if (tag == 0) {
		// System.err.println("Public Test " + testNum);
		// tag = 1;
		// }
		// System.err.print((char) diffout.read());
		// ++count;
		// }
		// if (count == 0) {
		// return PASS;
		// } else {
		// return FAIL;
		// }
		// } catch (IOException e) {
		// System.err.println("Error Reading Diff: \"" + expectedFilename
		// + "\"");
		// e.printStackTrace();
		// return IO_ERROR;
		// } catch (InterruptedException e) {
		// System.err.println("Error Sleeping: \"" + expectedFilename + "\"");
		// e.printStackTrace();
		// return SLEEP_ERROR;
		// }

	}

	public static void main(String args[]) throws IOException {

		// String studentName = Compiler.studentName;
		String studentID = Compiler.studentID;
		String uciNetID = Compiler.uciNetID;

		int publicTestcaseNum = 16;

		int publicPass = 0;
		for (int i = 1; i <= publicTestcaseNum; ++i) {
			if (testPublic(i) == PASS) {
				++publicPass;
			}
		}

		System.out.print(studentID);
		System.out.print("\t");
		System.out.print(uciNetID);
		System.out.print("\t");
		System.out.print(" Passed Public Cases: ");
		System.out.print(publicPass);
		System.out.print("/");
		System.out.println(publicTestcaseNum);
	}
}

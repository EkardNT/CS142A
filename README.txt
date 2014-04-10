TO COMPILE:
Because my program defines some classes in the sub-package crux.scanning, you need to tell the Java compiler to look for .java files in the scanning folder. This is the command line expression that I used to successfully compile the program on my Windows machine:

javac *.java scanning\*.java

TO RUN:
Once you have compiled the program, make sure to specify the current folder as the classpath when you attempt to run it. For example, on my Windows machine I had to use the following command line expression:

java -cp ../ crux.Compiler EXAMPLE_INPUT.txt

INPUT FILES:
I have provided a sample Crux program in the EXAMPLE_INPUT.txt file. To scan your own input file (it has to be a UTF-8 encoded text file, but does not need to have the ".txt" extension), substitute EXAMPLE_INPUT.txt with your file's name. My program also can scan multiple files at once, by providing more than one file name. For example, the following command line expression will output the results of scanning files "Input1.txt" and "Input2.txt":

java -cp ../ crux.Compiler Input1.txt Input2.txt
Homework 3. Multithreaded gzip compression filter

Background

You're working for a web server house that regularly needs to generate some fairly large data streams and compress them using gzip. The compression part of this is turning into a CPU bottleneck and the users are starting to complain. Your company can easily afford to throw hardware at the problem, so your boss asks you whether you can reprogram your servers to do the compression in parallel, taking advantage of the fact that your servers are multiprocessor.

You look into the matter, and discover that there's a C implementation of a program called pigz that does something along the lines that you want. (For convenience, a copy of this program is available on Seasnet in the /usr/local/cs/src/pigz directory, and an executable is installed in /usr/local/cs/bin/pigz.) The pigz program can be used as a filter that reads programs from standard input and writes to standard output. Unfortunately, it has a problem: it's written in C. Your company has standardized on Java, so that it can use just one version of each executable and run it on a wide variety of servers that you have: some are x86-64, some are x86, some Sparc, and some use secret hardware whose nature isn't disclosed even to your group.

You tell this to your boss, who responds, "OK, so do what pigz is doing, but do it in Java". Her suggestion is to use standard Java classes and see how well your substitute does, compared to standard pigz.

Now the gzip format has the property that you can partition an input stream, compress each partition separately, and concatenate the compressed versions of each partition; the resulting compressed stream can be decompressed by pigz, by gzip, or by any other gzip-format-understanding program. Unfortunately, this approach doesn't work for your application, because the data are generated dynamically in in the form of a stream, and you want the data to be compressed and delivered to its destination on the fly. You do not want to generate all the data into a huge file, partition the file, compress each partition separately, and send the concatenation of the compressed partitions.

What you want instead, is to do what pigz does: divide the input into fixed-size blocks (with block size equal to 128 KiB), and have P threads that are each busily compressing a block. That is, pigz starts by reading P blocks and starting a compression thread on each block. It then waits for the first thread to finish, outputs its result, and then can reuse that thread to compress the (P+1)st block.

For better compression, pigz does something else. Instead of compressing each block independently, it uses the last 32 KiB of the previous block to prime the compression dictionary for the next block. That way, each block other than the first is compressed better, in the typical case. You want to do that too.

You search around the net some more to see whether someone has done this, and find that there's a package by Cédrik Lime called MessAdmin that has a similar feature. It has a lot of code that you don't need, though, and doesn't have a simple standalone application to try out. You'd like a stripped down version that just does pigz-style multithreaded compression, so that you compare the two applications' performances.

Assignment

Write a Java program called Jpigz that behaves like the C pigz implementation, in the sense that it operates with multiple compression threads to improve wall-clock performance. Each compression thread acts on an input data block of size 128 KiB. Normally, each thread uses as its dictionary the last 32 KiB of the previous input data block, but if the -i option is used, it does not do this and instead compresses each data block independently. Compressed output blocks are generated in the same order that their uncompressed blocks were input. The number of compression threads defaults to the number of available processors, but this can be overridden. Your program may also use a small, fixed number of threads to control the compression threads or to do input/output.

Your implementation can be a simplification of pigz, in the following ways:

Jpigz need not decompress; you have to implement only the compression part.
Jpigz needs to support only the -i and -p processes options of pigz. The latter option must be spelled with the space between the option and the value: for example, -p3 (without a space) need not be recognized. Jpigz can report an error if any other option syntax is used.
Jpigz always reads from standard input and writes to standard output. It can report an error if you specify a file name.
Jpigz's behavior is not specified if the input or the output is a terminal. For example, it can unconditionally read standard input and write to standard output without checking whether these streams are connected to a terminal.
When an error occurs, Jpigz need not issue exactly the same diagnostic message as pigz, so long as it detects and reports the error to standard error, and exits with nonzero status.
Jpigz should behave like pigz in the following respects:

If you decompress the output, you get a copy of the input.
The output is compressed, about as well as pigz would compress it.
The output follows the GZIP file format standard, Internet RFC 1952.
The output contains just a single member, that is, it does not contain the concatenation of two or more members. For a definition of "member" please see RFC 1952 §2.3.
Ideally the output is byte-for-byte identical with the output of pigz. If this is not possible, the reason for any discrepancies must be documented.
Jpigz runs faster than gzip, when the number of processors is greater than 1. It is competitive in speed with pigz.
The default value for processes is the number of available processors; see the Java standard library's availableProcessors method.
Read errors and write errors are detected. For example, the command "pigz </dev/zero >/dev/full" reports a write error and exits with nonzero exit status, and the same should be true for "java Jpigz </dev/zero >/dev/full".
Out-of-range requests are detected. For example, on the Seasnet Linux servers "pigz -p 10000000 </dev/zero >/dev/null" by default reports an error and exits with nonzero status due to lack of virtual memory, and Jpigz should do likewise.
The input and output need not be a regular file; they may be pipes. For example, the command "cat /etc/passwd | java Jpigz | cat" should output the same thing as the command "java Jpigz </etc/passwd".
Measure the performance of the pigz, compared to your Jpigz, and compare both to /usr/local/cs/bin/gzip. For your measurement platform, use the Seasnet Linux servers. Use shell commands like the following to compare the performance of the three implementations:

input=/usr/local/cs/jdk1.7.0_09/jre/lib/rt.jar
time gzip <$input >gzip.gz
time pigz <$input >pigz.gz
time java Jpigz <$input >Jpigz.gz

# This checks Jpigz's output.
pigz -d <Jpigz.gz | cmp - $input
Try at least one other benchmark as well, of your own design. See what happens if -i is used, or if the number of processors is changed to values other than the default, trying this with both pigz and Jpigz. Run each trial at least three times, and report each instance of real time, user time, and system time; also, report the compression ratio of each command.

Assess your work by writing an after-action report that summarizes the performance that you observed. Focus in particular on any problems you foresee as the file size and the number of threads scales up, and which method you expect to work better in general. This report should be a simple ASCII text file that consumes at most five pages.

Your implementation may use ideas taken from MessAdmin, but all the code you submit must be your own. If you use ideas, give MessAdmin's author proper credit for them in your code's comments and in your after-action report.

Your implementation should operate correctly under Java Standard Edition 7. There is no need to run on older Java versions. Please keep your implementation as simple and short as possible, for the benefit of the reader. Your program should compile cleanly, without any warnings.

If your PATH is set correctly to a string starting with "/usr/local/cs/bin:", the command "java -version" should output the following text:

java version "1.7.0_09"
Java(TM) SE Runtime Environment (build 1.7.0_09-b05)
Java HotSpot(TM) Server VM (build 23.5-b02, mixed mode)
the command "pigz --version" should output "pigz 2.2.5", and the command "gzip --version" should output "gzip 1.5" followed by some licensing information.

Submit

To turn in your assignment, submit a single jar file hw3.jar that contains both your Java source code and a plain text file README.txt that holds your assessment. Do not submit class files. Also, please do not put your name, student ID, or other personally identifying information in your files. Before submitting hw3.jar you should test it using the following shell commands on SEASnet:

# Make a fresh directory and change into it.
mkdir testdir
cd testdir

# Extract your program.
jar xf ../hw3.jar

# Make sure you have a README.txt file.
ls -l README.txt

# Build your modified version.
javac `find . -name '*.java'`

# Check your modified version; the output should be empty.
cat ../hw3.jar | java Jpigz | pigz -d | cmp - ../hw3.jar
Hints

Here are some library references and tips that may help.

java.lang.Integer.parseInt
java.nio
java.util.zip
Lesson: Concurrency, The Java Tutorials (2011)
Cédrik Lime, MessAdmin: Notification system and Session administration for Java EE Web Applications
© 2010–2012 Paul Eggert. See copying rules.
$Id: hw3.html,v 1.60 2012/10/18 00:27:06 eggert Exp $
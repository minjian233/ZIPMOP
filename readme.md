ZIPMOP implements an algorithm that checks if a trace compressed by SLP(straight line program) compression satisfies a formula in LTL[F,G,X].<br />
To compress the a trace, we recommand to use sequitur in the repo which is forked from https://github.com/craignm/sequitur.<br />
One way the tool works in is that it assumes the input is written with numerical values separated by spaces like:

0 1 1 1 1

It generates SLP grammars such that all non terminal symbols are ordered numerical values with 0 as the initial symbol <br />
and all terminal symbols are encoded by [ and ]. The corresponding SLP grammar of the this example is:

0 -> [0] 1 1 <br />
1 -> [1] [1] <br />

To run the sequitur tool:
```
> cd /path/to/sequitur/c++
> make
>/path/to/ziptrack/sequitur/c++/sequitur -d -p -m 2000 < /path/to/base_folder/sub_folder/trace.txt > /path/to/basefolder/subfolder/slp.txt
```
This command creates the SLP in the file /path/to/basefolder/subfolder/slp.txt. <br />
ZIPMOP assumes the given SLP grammar in the same form so the usage of sequitur is highly recommanded. 

To compile the provided source code, simply run:
```
>cd /path/to/ZIPMOP
>javac *java
```
To run the algorithm with the compressed grammar and a formula ψ, simply run: 
```
>java -Xss4m ZIPMOP.Main path_to_compressed_file ψ

\\for example
>java -Xss4m LTL.Main path_to_compressed_file <F(a)->G(!b)> 
```
Currently, ZIPMOP does not support past LTL and it requires the following format:

All allowed operations are ! for negation, & for conjunction, | for disjunction, X for next, 
G for globally, F for eventually and -> for implication.

The X operator is pushed in as far as it can be pushed with |, &, and F. For example: X(a|b) is not considered as an valid input as X(a|b)=Xa|Xb. 
The algorithm may result in undefined behavior if inputs are not valid for now.

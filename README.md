# OTEDAMA - Preordering for Machine Translation

Welcome to OTEDAMA, a system for extracting preordering rules for machine translation. Otedama currently works for English and Chinese (using the Stanford Parser) as source languages and any target language for which word alignments can be extracted. External source-language parsers using the CoNLL output format are also supported. 

## Prerequisites 

OTEDAMA comes pre-packaged with the Stanford Parser. You just need a working installation of java (Version 8.0 or higher, including a compiler).

## Installation

Running

`./build.sh`

in the installation directory should create the files `parser.jar`, `learner.jar` and `reorder.jar` in the bin subdirectory.

## Preprocessing/Postprocessing

OTEDAMA cannot handle the unicode input chracters '{', '}', '[', ']', '&', '#', '=' and '>'. It will automatically replace them with the placeholders "-LCB-", "-RCB-", "-LSB-", "-RSB-", "-AMP-", "-NUM-", "-EQU-" and "-GRE-". You can undo this for the system's output by using the postprocess.sh script.

## Parsing

#### Parser Models:  
In order to use the Stanford parser, you need to download the models which are not included in this repository. 

1. Download the parser models from [here](http://www.cl.uni-heidelberg.de/statnlpgroup/otedama/stanford-parser-3.5.2-models.jar) (282 MB)
2. Copy stanford-parser-3.5.2-models.jar to the `lib/parser/` directory in this repo. 

You can also try using a different version of the Stanford parser and parser models (which will probably work just fine), but this has not been tested.

#### For training:

````
java -jar bin/parser.jar <english training corpus> <alignments english-foreign> <output file> <verbose_mode: v(erbose) or q(uiet)>
````

_Example_:  
`java -jar bin/parser.jar train.en train.en-fr.align train.en-fr.trees v` 
 
#### For testing (without alignments):  

Specifying "no_align" instead of an alignment file will create an unaligned treebank (such as for reordering test data).  
````
java -jar bin/parser.jar <english_training_corpus> no_align <output_file> <verbose_mode: v(erbose) or q(uiet)>
````
_Example_:  
`java -jar bin/parser.jar test.en no_align test.en.trees q`

#### Languages other than English:

* For Chinese, use `bin/zhparser.jar` instead of `bin/parser.jar`.
* For external dependency parsers that produce output in CoNLL format (http://ilk.uvt.nl/conll/#dataformat),  run `bin/conll2tree.jar` to convert labelled dependencies to trees in Otedama's own format.   
* It is planned to support other output from external parsers eventually.  


## Learning rules

````
java -jar bin/learner.jar <english treebank> <number of iterations> <size of training subsets> <size of evaluation treebank> <maximum crossing score> <number of trials> <minimum matching features> <parallel threads> <output file> <test treebank> <maximum overall reduction> <minimum reduction factor> <window size> <use feature subsets (y/n)>, <logging (v[erbose]/q[uiet])> <maximum waiting time (mins)>

````
_Example_:  
`java -jar bin/learner.jar train.en-fr.trees 50 20 10000 -200 100 10 4 en-fr.rules none 0 2 4 y v 30`

#### Recommended Parameter Ranges:

* `<number of iterations>`:[20-10000] Depends on target language and syntactic complexity of the corpus, and on whether you used feature subsets or fuzzy matching. 200 ist a good starting value for syntactically simple corpora. 
* `<size of training subsets>`:[20-1000]  A new random subset of the specified size is chosen as a learning treebank for extracting rule candidates in each iteration. Bigger training subsets lead to longer training times, but increase the probability of finding a near-optimal new rule in each iteration. Should be at leat 2N, where N is the number of CPUs available for adequate load balancing. 
* `<size of evaluation treebank>`:[1000-size of corpus] A new random subset of the specified size is chosen as an evaluation treebank in each iteration. Bigger evaluation treebanks lead to longer training times, but increase the probability of finding a near-optimal new rule in each iteration. This feature is currently disabled.
* `<maximum crossing score>`:[0.5-2]*((-1)*(<size of evaluation treebank>/100)) Minimum reduction in crossing score in evaluation treebank that each new rule must meet. This feature is currently disabled.
* `<number of trials>`: Upper bound on number of iterations that will be run. Iterations which do not yield a rule candidate that fullfils <maximum crossing score> do not result in a new rule. learner.jar will terminate either after <number of rules to be learned> rules have been discovered or <number of trials> have been run (successfully or unsuccessfully).
* `<minimum matching features>`:[1-12] 12 for exact matching, <12 for fuzzy matching. The given value is adjusted automatically for nodes that have few children and thus cannot fulfill the given matching criterion. Small values yield very poor results. 
* `<test treebank>`: [file name or 'none'] Treebank file for tracking alignment monotonicity. This feature is currently not supported.
* `<maximum overall reduction>`:[0-100] Bound on the effect on alignment monotonicity of the whole training corpus for candidate rules.
* `<minimum reduction factor>`:[0-10] Variance constraint for rules: Each new rule must reduce crossing score on n times as many sentences as number of sentences where it increases crossing score.
* `<window size>:[2-4]` Size of the sliding window during rule extraction.
* `<use feature subsets>`: If 'y', subsets of the matching context are also extracted as feature sets for candidate rules. 
* `<maximum waiting time>[10-60]` : Maximum time that may elapse without any new rules being learned before training is stopped. 



## Reordering

````
java -jar bin/reorder.jar <english treebank> <rule file> <english output treebank> <english surface output> <batch-size> <minimum matching features> <number of parallel threads>
````

_Example_:  
`java -jar bin/reorder.jar test.en.trees en-fr.rules test.en.trees.reordered test.en.reordered 1000 10 10`

Greater batch sizes can lead to higher memory consumption. It is recommended to use the same value for <minimum matching features> as in the learning step, but feel free to experiment with other values. The value of <minimum matching features> is adjusted automatically for nodes that have few children and thus cannot fulfill the given matching criterion.

Good luck and have fun!

##Example Script

For instructions on how to run an example script of the entire Otedama pipeline, see example/README.txt

##Computing Alignment Monotonicity

A python script for computing the monotonicity of a given word alignment can be found at scripts/crossing_score.py. For example, you can run

python crossing_score.py <alignment_file> none

to obtain the total number of alignment crossings in <alignment_file>. Optionally, a file with alignment transformations can be specified as the second parameter.

## References

#### Publications:

> D. Genzel. Automatically Learning Source-side Reordering Rules for Large Scale Machine Translation. Proceedings of the 43rd Annual Meeting on Association for Computational Linguistics ACL 2010, (August):376-384, 2010.

> M. Collins, et. al. Clause restructuring for statistical machine translation. Proceedings of the 43rd Annual Meeting on Association for Computational Linguistics ACL05, (June):531-540, 2005.

#### External Libraries:

stanford-parser.jar

## License

OTEDAMA was created 2013-2016 by Julian Hitschler, Laura Jehl, Sariya Karimova, Mayumi Ohta, Benjamin Körner and Stefan Riezler at the Institute for Computational Linguistics, University of Heidelberg, Germany (www.cl.uni-heidelberg.de). For question, please contact J.H.: hitschler@cl.uni-heidelberg.de  
OTEDAMA is licensed under the GNU General Public License (Version 3): http://www.gnu.org/licenses/gpl.html
OTEDAMA is named after a Japanese juggling game for children: https://en.wikipedia.org/wiki/Otedama

## Citing Otedama

If you use Otedama, please cite the following publication:

> Julian Hitschler, Laura Jehl, Sariya Karimova, Mayumi Ohta, Benjamin Körner, Stefan Riezler. Otedama: Fast Rule-Based Pre-Ordering for Machine Translation.  The Prague Bulletin of Mathematical Linguistics, vol. 106, pp. 159-168. Charles University, Prague, Czech Republic, 2016: http://ufal.mff.cuni.cz/pbml/106/art-hitschler-et-al.pdf
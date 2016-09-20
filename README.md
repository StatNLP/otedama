# OTEDAMA - Preordering for Machine Translation

Welcome to OTEDAMA, a system for extracting preordering rules for machine translation. Otedama currently works for English and Chinese (using the Stanford Parser) as source languages and any target language for which word alignments can be extracted. External source-language parsers using the CoNLL output format are also supported. 

## Prerequisites 

OTEDAMA comes pre-packaged with the Stanford Parser. You just need a working installation of java (Version 8.0 or higher, including a compiler).

## Installation

1. Download Otedama by running:
 `git clone https://github.com/StatNLP/otedama`
2. Download the Stanford Parser models by running:
 * `cd otedama/lib/parser/`
 * `wget http://www.cl.uni-heidelberg.de/statnlpgroup/otedama/stanford-parser-3.5.2-models.jar`
 * `cd ../..`
3. Running
 `./build.sh`
 in the installation directory should create the files `parser.jar`, `zhparser.jar`, `conll2tree.jar`, `learner.jar` and `reorder.jar` in the bin subdirectory.
4. If all went well, you should be able to run the example script (see example/README.txt for instructions).

## Preprocessing/Postprocessing

Note that because output sentences are reconstructed from parse trees, the parser's tokenization is preserved in the input (if your parser performs automatic tokenization). For meaningful comparison, it is recommended you use tokenized text for your experiments or apply de-tokenization after re-ordering.

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
* For external dependency parsers that produce output in CoNLL format (http://ilk.uvt.nl/conll/#dataformat),  run `bin/conll2tree.jar  <input file source in CONLL format>, <alignment file source-target>, <output file>` to convert labelled dependencies to trees in Otedama's own format.   
* It is planned to support other output from external parsers eventually.  


## Learning rules

````
java -jar bin/learner.jar <config file> 

````
_Example_:  
`java -jar bin/learner.jar example/example.config`

#### Training Parameters:

Parameters for the learner are specified in a config file. See example/example.config for a template. The parameters are as follows: 

* TRAINING_TREEBANK_FILE: Parsed training data, created by parser.jar or conll2tree.jar
* INITIAL_SUBSAMPLE_SIZE=10: A new random subset of the specified size is chosen as a learning treebank for extracting rule candidates in each iteration. Bigger training subsets lead to longer training times, but increase the probability of finding a near-optimal new rule in each iteration. Should be at leat 4N, where N is the number of CPUs available for adequate load balancing. 
* MAX_RULE_CROSSING_SCORE: Bound on the effect on alignment monotonicity of the whole training corpus for candidate rules. Should be 0 or a negative number.
* MIN_MATCHING_FEATURES: For window size 4: 12 for exact matching, <12 for fuzzy matching. For window size 3: 10 for exact matching, <10 for fuzzy matching. For window size 2: 8 for exact matcing, <8 for fuzzy matching  The given value is adjusted automatically for nodes that have few children and thus cannot fulfill the given matching criterion. Small values yield very poor results.
* PARALLEL_THREADS: Number of parallel threads for learning.
* RULE_OUTPUT_FILE: Output model file where learned rules are written.
* MIN_REDUCTION_FACTOR: Variance constraint for rules: Each new rule must reduce crossing score on n times as many sentences as number of sentences where it increases crossing score. For noisy data sets, a minimum reduction factor of 2.0 is recommended.
* WINDOW_SIZE: Size of the sliding window during rule extraction. Values between 2 and 4 are supported.
* USE_FEATURE_SUBSETS: If 'y', subsets of the matching context are also extracted as feature sets for candidate rules. 
* LOGGING: v[erbose] or q[uiet]
* MAX_WAITING_TIME_MINS: Maximum time in minutes that may elapse without any new rules being learned before training is stopped. Longer time limits will result in more rules being learned. For large datasets, a minimum of 10 minutes waiting time is recommended.

## Reordering

````
java -jar bin/reorder.jar <english treebank> <rule file> <config file> <english output treebank> <english surface output> <batch-size> <number of parallel threads>
````

_Example_:  
`java -jar bin/reorder.jar test.en.trees en-fr.rules en-fr.config test.en.trees.reordered test.en.reordered 1000 10 10`

Greater batch sizes can lead to higher memory consumption but higher throughput as well. It is highly recommended to use the same config file as in the learning step. 

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

## Support

For questions and comments, please contact Julian Hitschler: hitschler@cl.uni-heidelberg.de. We are happy to help with any issue you may encounter while using Otedama, no matter how big or small.

## License

OTEDAMA was created 2013-2016 by Julian Hitschler, Laura Jehl, Sariya Karimova, Mayumi Ohta, Benjamin Körner and Stefan Riezler at the Institute for Computational Linguistics, University of Heidelberg, Germany (www.cl.uni-heidelberg.de). 
OTEDAMA is licensed under the GNU General Public License (Version 3): http://www.gnu.org/licenses/gpl.html
OTEDAMA is named after a Japanese juggling game for children: https://en.wikipedia.org/wiki/Otedama

## Citing Otedama

If you use Otedama, please cite the following publication:

> Julian Hitschler, Laura Jehl, Sariya Karimova, Mayumi Ohta, Benjamin Körner, Stefan Riezler. Otedama: Fast Rule-Based Pre-Ordering for Machine Translation.  The Prague Bulletin of Mathematical Linguistics, vol. 106, pp. 159-168. Charles University, Prague, Czech Republic, 2016: http://ufal.mff.cuni.cz/pbml/106/art-hitschler-et-al.pdf
#Parsing training data:
java -jar ../bin/parser.jar train.en train.align train.trees v
#Parsing test data:
java -jar ../bin/parser.jar test.en no_align test.trees v
#Learning pre-ordering rules:
java -jar ../bin/learner.jar train.trees 1000 10 100 0 1000 10 4 preordering.rules none 0 2.0 3 y q 1
#Applying pre-ordering rules to test data:
java -jar ../bin/reorder.jar test.trees preordering.rules test-reordered.trees test-reordered.en 100 10 4




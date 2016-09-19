#Parsing training data:
java -jar ../bin/parser.jar train.en train.align train.trees v
#Parsing test data:
java -jar ../bin/parser.jar test.en no_align test.trees v
#Learning pre-ordering rules:
java -jar ../bin/learner.jar example.config
#Applying pre-ordering rules to test data:
java -jar ../bin/reorder.jar test.trees preordering.rules example.config test-reordered.trees test-reordered.en 100 4
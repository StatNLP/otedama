#Useage: "python crossing_score.py <alignments> <transformations>"
#Or: "python crossing_score.py <alignments> none" without transformations
import sys

if len(sys.argv) < 2:
  sys.stderr.write("USAGE: %s initial_alignments [transformations]\n"%sys.argv[0])
  exit(1)

initial_alignments_input = [x.split() for x in open(sys.argv[1], 'r').readlines()]
if sys.argv[2] != "none":
	transformations_input = [x.split() for x in open(sys.argv[2], 'r').readlines()]
#print initial_alignments_input
#print transformations_input
alignments = []
transformations = []
transformations_sequential = []
for y in initial_alignments_input:
	processed = []
	for y_i in y:
		processed.append([int(z) for z in y_i.split("-")])
	alignments.append(processed)

CS_total = 0

for x in alignments:
	sorted_alignments = sorted(x, key=lambda y: y[0])
	#print sorted_alignments 
	target_alignments = []
	for y in sorted_alignments:
		target_alignments.append(y[1])
	CS = 0
	#print target_alignments
	for i in range(len(target_alignments)):
		for j in range(len(target_alignments)):
			if j >= i:
				break
			if target_alignments[i] < target_alignments[j]:
				CS += 1
	#print CS
	CS_total += CS

print "Before reordering:",CS_total

if sys.argv[2] == "none":
	exit(0)

for x in transformations_input:
	processed = []
	for x_i in x:
		processed.append(int(x_i))
	transformations_sequential.append(processed)
#print alignments
#print transformations

#convert sequential transformations to n:m transformations

for x in transformations_sequential:
	pairs = []
	for x_i in range(len(x)):
		if x[x_i] != x_i and x[x_i] > x_i:
			pairs.append([x_i, x[x_i]])
	transformations.append(pairs)
		
#apply transformations:
l = 0
for x in alignments:
	y = transformations[l]
	for i in y:
		for j in x:
			if j[0] == i[0]:
				j[0] = i[1]
			elif j[0] == i[1]:
				j[0] = i[0]
	l += 1
			
#print alignments
#print transformations

#compute crossing scores

CS_total = 0

for x in alignments:
	sorted_alignments = sorted(x, key=lambda y: y[0])
	#print sorted_alignments 
	target_alignments = []
	for y in sorted_alignments:
		target_alignments.append(y[1])
	CS = 0
	#print target_alignments
	for i in range(len(target_alignments)):
		for j in range(len(target_alignments)):
			if j >= i:
				break
			if target_alignments[i] < target_alignments[j]:
				CS += 1
	#print CS
	CS_total += CS

print "After  reordering:", CS_total
			

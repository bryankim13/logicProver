# logicProver
 AI that proves logic based on inputs

**Doesn't Require any jars at all**
5 functions:
Teach: teaches rules to the AI. 
 Syntax: Teach S -> V
 
List: Lists all known rules and inferences
 Syntax: List
 
Learn: Uses Forward Chaining on existing rules to learn new rules
 Syntax: Learn
 
Query: Returns true if given expression is true
 Syntax: Query (S&V)

Why: Prints out entire logic process of Query
 Sytax: Why (S&V)
 
To use input files, run them as an argument
Example: java main.java testfile1.in

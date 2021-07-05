JC = javac
.SUFFIXES: .java .class

.java.class:
	$(JC) $*.java

target: 
run: 
		java main.java
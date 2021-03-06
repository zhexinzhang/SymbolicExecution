package org.tamier.symbolic;

import org.checkerframework.dataflow.analysis.Analysis;
import org.checkerframework.dataflow.cfg.DataflowTypeProcessor;

import java.util.Set;

import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;

/**
 * Class of entry with all the configurations. Invoke Javac compiler with
 * DataflowTypeProcessor, get the control flow graph, perform analysis, and
 * finally solve the constraints using solver
 *
 * @author tamier
 *
 */
public class SymbolicPlayground {
    /* Configuration: change as appropriate */
    // input file name and path
    static String inputFile = "//home/tamier/jsr308/SymbolicExecution/src/test/SymbolicTest3.java";
    public static String clazz = "test.SymbolicTest3"; // name of the class to analyze
    static String method = "foo"; // name of the method to symbolicly execute

    public static void main(String[] args) {

        Context context = new Context();
        JavaCompiler javac = new JavaCompiler(context);
        javac.attrParseOnly = true;
        JavacFileManager fileManager = (JavacFileManager) context.get(JavaFileManager.class);
        JavaFileObject fileObject = fileManager
                .getJavaFileObjectsFromStrings(List.of(inputFile)).iterator().next();

        // DataflowTypeProcessor to generate control flow graph
        DataflowTypeProcessor typeProcessor = new DataflowTypeProcessor() {};
        typeProcessor.setMethodNameToProcess(method);
        javac.compile(List.of(fileObject), List.of(clazz),
                List.of(typeProcessor));

        // Perform dataflow analysis using SymbolicTrasnfer
        SymbolicTransfer transfer = new SymbolicTransfer();
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Analysis analysis = new Analysis(null, transfer);
        analysis.performAnalysis(typeProcessor.getCFG());
        Set<Set<Constraint>> permutatedConstraintSet = transfer
                .getConstrainManager().getPermutatedSets();
        // Invoke SymbolicSolver to solve all the Constraints
        SymbolicSolver solver = new SymbolicSolver();
        solver.solvePermutatedConstraintSet(permutatedConstraintSet);

    }
}

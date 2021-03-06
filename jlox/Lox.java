package jlox;

import java.io.Console;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    private static final Interpreter interpreter = new Interpreter();

	static boolean hadError = false;
    static boolean hadRuntimeError = false;

	public static void main(String[] args) throws IOException {
		if (args.length > 1) {
			System.out.println("Usage: java Lox [script]");
			System.exit(64); 
		} else if (args.length == 1) {
			runFile(args[0]);
		} else {
			runPrompt();
		}
	}

	private static void runFile(String path) throws IOException {
	    byte[] bytes = Files.readAllBytes(Paths.get(path));
		run(new String(bytes, Charset.defaultCharset()));
		if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
	}

	private static void runPrompt() throws IOException {
        Console cons = System.console();

		for (;;) {
			System.out.print("> ");
			String line = cons.readLine();
			if (line == null) break;
			run(line);
			hadError = false;
		}

        System.out.println();
	}

	private static void run(String source) {
		Scanner scanner = new Scanner(source);
		List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

		if (hadError) return;

        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);

        if (hadError) return;

        //System.out.println(new AstPrinter().print(expression));
        interpreter.interpret(statements);
	}

	static void error(int line, String message) {
		report(line, "", message);
	}

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    static void warning(Token token, String message) {
        System.err.printf("[line %s] Warning at '%s': %s\n", token.line, token.lexeme, message);
    }

    static void runtimeError(RuntimeError error) {
        System.err.printf("%s\n[line %s]\n", error.getMessage(), error.token.line);
        hadRuntimeError = true;
    }

	private static void report(int line, String where, String message) {
		System.err.printf("[line %s] Error%s: %s\n", line, where, message);
		hadError = true;
	}
}


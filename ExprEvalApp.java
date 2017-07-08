// import ANTLR runtime libraries
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

// import Java Map Libs
import java.util.HashMap;
import java.util.Map;

// import Java Stack Libs
import java.util.Stack;

// import Java console IO
import java.io.Console;
import java.io.IOException;

// import Additions
import java.io.FileInputStream;
import java.lang.StringBuffer;
import java.util.*;
import java.util.EmptyStackException;
import java.lang.NumberFormatException;

class EvalListener extends ExprBaseListener {
   // hash-map for variables' integer value for assignment
   Map<String, Integer> vars = new HashMap<String, Integer>(); 

   // stack for expression tree evaluation
   Stack<Integer> evalStack = new Stack<Integer>();
   // stack for operators (+-*/) in shunting-yard algorithm 
   Stack<String> opStack = new Stack<String>();

	//Queue, Hashmap,Stack for ID,Cal
	Queue<String> shuntQueue = new LinkedList<String>();
	HashMap<String, Integer> forAssn = new HashMap<String, Integer>();
	Stack<String> id = new Stack<String>();
	int Cal;
	boolean exprState;

   @Override
   public void exitProg(ExprParser.ProgContext ctx) {
      //System.out.println("exitProg: "); 

   }
   
   @Override
   public void exitExpr(ExprParser.ExprContext ctx) {
      //System.out.println("exitExpr: ");
   }

   @Override
   public void enterAssn(ExprParser.AssnContext ctx) {
      //System.out.println("enterAssn: ");
      //not exprState
      exprState = false;
   }
   @Override
   public void exitAssn(ExprParser.AssnContext ctx) {
	exprState = false;
	//Set HashMap for ID = INT

	while( id.peek().contentEquals("=") == true || id.peek().contentEquals(";") == true )
		id.pop();

	String a = id.pop();
	Integer b = evalStack.pop();
	forAssn.put( a , b );

	while( shuntQueue.peek() != null )
		shuntQueue.poll();

	//System.out.println("exitAssn: ");
   }

   // Add more overrride methods if needed 
 	public void enterExpr(ExprParser.ExprContext ctx) {
		//System.out.println("enterExpr: ");
		exprState = true;
	}

   @Override 
   public void visitTerminal(TerminalNode node) {
      String s = node.getText();
	try{

      switch(s) {
      case "+": 
          //System.out.println("Terminal PLUS");

		if( opStack.empty() )
			opStack.push(s);
		else{
			while( !opStack.empty() && !opStack.peek().contentEquals("(") ) 
		 		shuntQueue.offer( opStack.pop() );
			opStack.push(s);
		}

          break;
      case "-": 
          //System.out.println("Terminal MINUS");

		if( opStack.empty() )
			opStack.push(s);
		else{
			while( !opStack.empty() && !opStack.peek().contentEquals("(") )
				shuntQueue.offer( opStack.pop() );
			opStack.push(s);
		}

          break;
      case "*": 
          //System.out.println("Terminal MULTIPLY");

		if( opStack.empty() )
			opStack.push(s);
		else if( opStack.peek().contentEquals("+") || opStack.peek().contentEquals("-") || opStack.peek().contentEquals("(") )
			opStack.push(s);
		else {
			shuntQueue.offer( opStack.pop() );
			opStack.push(s);
		}

          break;
      case "/": 
          //System.out.println("Terminal DIVIDE");

		if( opStack.empty() )
			opStack.push(s);
		else if( opStack.peek().contentEquals("+") || opStack.peek().contentEquals("-") || opStack.peek().contentEquals("(") )
			opStack.push(s);
		else {
			shuntQueue.offer( opStack.pop() );
			opStack.push(s);
		}
          break;
      case "(": 
          //System.out.println("Terminal LEFT_PAR");
		opStack.push("(");
          break;
      case ")": 
          //System.out.println("Terminal RIGHT_PAR");

		while( !opStack.peek().contentEquals("(")  ) 
			shuntQueue.offer( opStack.pop() );
		opStack.pop();
          break;
      default:
          if (s.matches("[0-9]+")) { // INT
             //System.out.println("Terminal-INT " + s);
             Integer i = new Integer(s);
             evalStack.push(i);
		shuntQueue.offer(s);
          } else { // ID
             //System.out.println("Terminal-ID " + s);
		
		if( s.contentEquals(";") && exprState == true ) {
			Cal = result(1);
			System.out.println( Cal );
		}
		id.push(s);
		shuntQueue.offer(s);
          } 
      }

	}
	catch( EmptyStackException e ) {
		System.out.println("EmptyStackException in Queue");
	}
   }
	//Calculating
	public int result(int start) {

		while( !opStack.empty() )
			shuntQueue.offer( opStack.pop() );

		Stack<Integer> temp = new Stack<Integer>();
		int a,b,c;

		try {
		while( shuntQueue.peek() != null ) {
			if( shuntQueue.peek().contentEquals(";") )
					shuntQueue.poll();

			String tempString = shuntQueue.poll() ;

			if( tempString.contentEquals("+") ) {
				b=temp.pop();
				a=temp.pop();
				c=a+b;
				temp.push(c);
			}
			else if( tempString.contentEquals("-") ) {
				b=temp.pop();
				a=temp.pop();
				c=a-b;
				temp.push(c);
			}
			else if( tempString.contentEquals("*") ) {
				b=temp.pop();
				a=temp.pop();
				c=a*b;
				temp.push(c);
			}
			else if( tempString.contentEquals("/") ) {
				b=temp.pop();
				a=temp.pop();
				c=a/b;
				temp.push(c);
			}
			else {
				try {
					Integer i = new Integer(tempString);
					temp.push( i ) ;
				}
				catch( NumberFormatException e ) {
					Integer i = forAssn.get( tempString );
					temp.push( i );
				}
			}
		}

		}
		catch( ArithmeticException e ) {
			System.out.println("ArithmeticException");
		}
		catch( EmptyStackException e ) {
			System.out.println("EmptyStackException in Cal");
		}

		return temp.pop();
	}
}

public class ExprEvalApp {
   public static void main(String[] args) throws IOException {
      //System.out.println("** Expression Eval w/ antlr-listener **");

      Console c = System.console();
      if (c == null) {
         System.err.println("No Console");
         System.exit(1);
      }

		//File Input
	      String input = c.readLine("Input(example : input.txt): ");

	FileInputStream stream = new FileInputStream( input );
	StringBuffer buffer = new StringBuffer();
	byte[] b = new byte[1024];

	for(int n ; (n=stream.read(b)) != -1 ; ) {
		buffer.append(new String(b,0,n));
	}
	String AntlrInput = buffer.toString();

	      // Get lexer
	      ExprLexer lexer = new ExprLexer(new ANTLRInputStream( AntlrInput ));
	      // Get a list of matched tokens
	      CommonTokenStream tokens = new CommonTokenStream(lexer);
	      // Pass tokens to parser
	      ExprParser parser = new ExprParser(tokens);
	      // Walk parse-tree and attach our listener
	      ParseTreeWalker walker = new ParseTreeWalker();
	      EvalListener listener = new EvalListener();
	      walker.walk(listener, parser.prog());	// walk from the root of parse tree
   }
} 

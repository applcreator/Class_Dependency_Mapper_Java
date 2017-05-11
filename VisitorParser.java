import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import java.util.Scanner;
import java.util.*;
import java.util.Enumeration;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.regex.Matcher;
import java.io.PrintWriter;


public class VisitorParser extends JavaBaseVisitor<Void> {

	/**
	* Main Method
	*/
	public static void main(String[] args) throws IOException {
	
	//handles the input arguments provided during runtime.
//	try{
		if(args.length==0){
			System.out.println("Please provide the path and filename to be parsed. Eg: java VisitorParser ./folder1/filename.java ");
			}
			else {
				System.out.println("Input Received"+args[0]);
				ANTLRInputStream input = new ANTLRInputStream(new FileInputStream(args[0]));
				JavaLexer lexer = new JavaLexer(input);
				CommonTokenStream tokens = new CommonTokenStream(lexer);
				JavaParser parser = new JavaParser(tokens);
				JavaParser.CompilationUnitContext tree = parser.compilationUnit(); 
		
				

				VisitorParser visitor = new VisitorParser(); 
				visitor.visit(tree);
				
				visitor.summary();
				System.out.println("Program Analysis Completed. \n");
				
				}
/*		}     
		catch(Exception e1){
			System.out.println("Caught IO Exception. Check the argument includes the correct path and the filename"+e1.getMessage());
			}
*/	}

	private String CurrentClassAttribute = null;
	private int classNameCounter = 0;
	Map packageLevelClassNames = new HashMap();
	
	
	private String currentMethodName = null;
	HashMap<String, String> classLevelMethodNames = new HashMap();
	
	private String currentClassConstructorName = null;

	Map importStatementsUsed = new HashMap();
	List<String> classLevelImports = new ArrayList<String>();



	Map lVariableMap = new HashMap();
	HashMap<HashMap<String, String>, String> classLevelLocalVariableMap = new HashMap();


	HashMap<String, HashMap<String, String>> methodLevelLocalVariableMap = new HashMap();
	List<String> eLevel = new ArrayList();
	
	Map methodParametersMap = new HashMap();
	HashMap<String, HashMap<String, String>> methodParamDetails = new HashMap();
	
	
	
	HashMap<String, HashMap<String, String>> mLevelExprDetails = new HashMap();



	/**
	 * The following method should display the package being accessed
	 */
    
	@Override
	public Void visitPackageDeclaration(JavaParser.PackageDeclarationContext ctx) {
		System.out.println("Package Declaration:" + ctx.getChild(1).getText()+"\n");
		return visitChildren(ctx); 
	 }
	
	/**
	 * The following method should collect the import statements used inside the class accessed
	 */
	@Override 
	public Void visitImportDeclaration(JavaParser.ImportDeclarationContext ctx) { 
		String importName = ctx.getChild(1).getText();
		
		classLevelImports.add(importName);
		importStatementsUsed.put(classLevelImports,CurrentClassAttribute);	//append the current class name to the import statements map.
		
		return super.visitImportDeclaration(ctx); 
	}

	

	/**
	 *The following method should display the name of the java class accessed
	 */
	@Override
	public Void visitClassName(JavaParser.ClassNameContext ctx) {
		
		classNameCounter = classNameCounter+1;
		CurrentClassAttribute = ctx.getText();
		packageLevelClassNames.put(classNameCounter,CurrentClassAttribute);
		classLevelLocalVariableMap.put(new HashMap(),CurrentClassAttribute);
		
		System.out.println("Class name:" + CurrentClassAttribute+"\n");
		System.out.println("Class Level Details Initial Entry:" + classLevelLocalVariableMap.entrySet()+"\n");
		System.out.println("Class Level Details Sub Entry:" + packageLevelClassNames.entrySet()+"\n");
	
		return super.visitClassName(ctx);
	}

	/**
	*The following method is used to identify constructors in the current class.
	**/
	@Override 
	public Void visitConstructorDeclaration(JavaParser.ConstructorDeclarationContext ctx) {
		//add class constructor counter. And some mechanism to list it on csv.******************
		currentClassConstructorName = ctx.getChild(0).getText();
		classLevelMethodNames.put(currentClassConstructorName, CurrentClassAttribute); //may need improvement.
		return super.visitConstructorDeclaration(ctx); }
		
			
	/**
	 *The following method should display the name of the java method inside the accessed class
	 */
	@Override
	public Void visitMyMethodName(JavaParser.MyMethodNameContext ctx) {
		String observedName = ctx.getText();
		
		currentMethodName = observedName; 
		System.out.println("Method name:" +currentMethodName);
		
		classLevelMethodNames.put(currentMethodName, CurrentClassAttribute);
		System.out.println("classLevelMethodNames : "+classLevelMethodNames.entrySet());
	
		return super.visitMyMethodName(ctx);
	}
	

	
	@Override 
	public Void visitFormalParameters(JavaParser.FormalParametersContext ctx) {
		System.out.println("listing:"+ctx.getChild(0).getText());
		
		return super.visitFormalParameters(ctx); }
		
		
	@Override 
	public Void visitFormalParameterList(JavaParser.FormalParameterListContext ctx) {
		System.out.println("Parameter #####$$$$$"+ctx.getText());
		for(int i=0; i<ctx.getChildCount(); i=i+2){
		String paramType = ctx.getChild(i).getChild(0).getText();
		String paramName = ctx.getChild(i).getChild(1).getText();
		//Map methodParametersMap1 = new HashMap();  //Creating a local map seems to avoid the issue of appending params from all methods into the same map.
		methodParametersMap.put(paramName,paramType);
		System.out.println("Parameter Level Map : "+methodParametersMap.entrySet());
		
		if(methodParamDetails.get(currentMethodName) != null){
			((HashMap<String, String>)methodParamDetails.get(currentMethodName)).put(paramName,paramType);
			System.out.println("Method Level Parameter Map : "+methodParamDetails.entrySet());
		}
		else{
			methodParamDetails.put(currentMethodName,new HashMap<String,String>());
			((HashMap<String, String>)methodParamDetails.get(currentMethodName)).put(paramName,paramType);
			
		//	methodParamDetails.put(currentMethodName,methodParametersMap);
			System.out.println("Method Level Parameter Map : "+methodParamDetails.entrySet());
			}	
		}
		return super.visitFormalParameterList(ctx); }
		
	
	@Override
	public Void visitFieldDeclaration(JavaParser.FieldDeclarationContext ctx) { 
		HashMap<String,String> lVariableMap1 = new HashMap<>();
		
		if(ctx.getChildCount()==2){
			String fieldDataTypeListed = ctx.getChild(0).getText();
			String fieldNameListed = ctx.getChild(1).getText();
			System.out.println("WHAAAATTTTT");
			
			lVariableMap1.put(fieldNameListed, fieldDataTypeListed);
			}
			else if(ctx.getChildCount()>2 && ctx.getText().contains("=")){
				System.out.println("WHADDDDDD");
				String fieldDataTypeListed = ctx.getChild(0).getText();
				String fieldNameListed = ctx.getChild(1).getText().substring(0,ctx.getChild(1).getText().indexOf("="));
				
				lVariableMap1.put(fieldNameListed, fieldDataTypeListed);
				}
				else if(ctx.getChildCount()>2 && !ctx.getText().contains("=")){	//NEED TO ADD EXTRA CHECKS TO CONSIDER MULTIPLE field declarations.
					System.out.println("WHACCCCCCC");
					String fieldDataTypeListed = ctx.getChild(0).getText();
					String fieldNameListed = ctx.getChild(1).getText();
					
					lVariableMap1.put(fieldNameListed, fieldDataTypeListed);
					System.out.println("DT : "+fieldDataTypeListed+"\t Name: "+fieldNameListed);
					}
					else{
						System.out.println("There are no local variables found.");
						}
		
		
		System.out.println("Local Variable Map 1: "+lVariableMap1);
		classLevelLocalVariableMap.put(lVariableMap1,CurrentClassAttribute);
		return visitChildren(ctx); }
		
		
	@Override
	public Void visitBlockStatement(JavaParser.BlockStatementContext ctx) {
		if(ctx.getChildCount() != 0){
			int children = ctx.getChildCount();
			System.out.println("-----****-----------");
			System.out.println("children"+children);
			for(int i=0;i<children;i++){
				System.out.println("-----**-----------");
				System.out.println("Block Statement "+i+":"+ctx.getChild(i).getText());
			}
		}
		
		return super.visitBlockStatement(ctx); }
	
		
	/**
	* This method is used to identify methods that belong to any object of a different class.
	*/
	@Override 
	public Void visitLocalVariableDeclaration(JavaParser.LocalVariableDeclarationContext ctx) {
		
		if(ctx.getChildCount() != 0){

		        String dataType = ctx.getChild(0).getText();
			
			String expression = ctx.getChild(1).getText();
			String variableName;
			
			if(expression.contains("=")){
				variableName = expression.substring(0,expression.indexOf('='));
			}else{
				variableName = expression.substring(0,expression.indexOf(';'));
			}

			System.out.println("variableName: " +variableName);
			System.out.println("datatype: " +dataType);
			
			if(variableName != null  &&  dataType != null){
			//Storing the details of local variables in a map.
			HashMap<String,String> lVariableMap2 = new HashMap<>();
			lVariableMap2.put(variableName,dataType);
			
			System.out.println("Local Variable Map 2 : "+lVariableMap2+"\n \n");
			
				//store the class & method level couplings
				
						classLevelLocalVariableMap.put(lVariableMap2, CurrentClassAttribute);
						System.out.println("Class level coupling info: \n"+classLevelLocalVariableMap.entrySet()+"\n \n");
						
						
						//Complex Map implementation.
						if(methodLevelLocalVariableMap.get(currentMethodName) != null){
						
					//	((HashMap<String, String>)methodLevelLocalVariableMap.get(currentMethodName)).putAll(lVariableMap2);
						
							((HashMap<String, String>)methodLevelLocalVariableMap.get(currentMethodName)).put(variableName,dataType);
						
					//	methodLevelLocalVariableMap.put(currentMethodName, (HashMap<String,String>)lVariableMap2);
							System.out.println("Method level coupling info ###########: \n"+methodLevelLocalVariableMap.entrySet()+"\n \n");
						}
						else{
							methodLevelLocalVariableMap.put(currentMethodName,new HashMap<String,String>());
							((HashMap<String, String>)methodLevelLocalVariableMap.get(currentMethodName)).put(variableName,dataType);
						//	methodLevelLocalVariableMap.put(currentMethodName, (HashMap<String,String>)lVariableMap2);
							System.out.println("Method level coupling info ###########: \n"+methodLevelLocalVariableMap.entrySet()+"\n \n");
							
							}
					
		        }
		        else{
		        	System.out.println("local variables do not exist.");
		        }
		        
		}
		return super.visitChildren(ctx);
	}
	
	
	
	
	/** 
	* This method is required to collect the method level coupling information 
	*/
	
	@Override 
	public Void visitExpression(JavaParser.ExpressionContext ctx) {
		if(ctx.getChildCount() == 0){
			return null;
			}
			else{
				int children = ctx.getChildCount();
			
				System.out.println("children:"+children);
				String instanceName;
				String associatedMethodOrVariable;
			
				for(int iter=0; iter<children; iter++){
			
					String expression = ctx.getChild(iter).getText();
					System.out.println("Expr Under Evaluation: "+expression);
			
					if(expression.contains(".") 
						&& !expression.equals(".")
						&& !expression.contains("\\(") 
						&& !expression.contains("\\)") 
						&& !expression.contains(",")
						&& !expression.contains("\"") 
						&& !expression.contains(" ") 
						&& !expression.contains("=")
						&& !expression.contains("==")
						&& !expression.contains("->")
						&& !expression.contains(">")
						&& !expression.contains("<")
						&& !expression.contains("||") 
						&& !expression.contains("&&") 
						&& !expression.contains("System")
						){
							int i = expression.indexOf(".");
							instanceName = expression.substring(0,i);
							System.out.println("instance name :"+instanceName);
							associatedMethodOrVariable = expression.substring(i+1,expression.length());
						
							System.out.println("associated method name :"+associatedMethodOrVariable);
						}
						else if(expression.contains(".")		//This need to be eliminated.
							&& expression.contains("\\(")
							&& expression.contains("\\)")
							&& (!expression.equals(".")
							|| !expression.equals("\\(")
							|| !expression.equals("\\)"))){
								int i = expression.indexOf(".");
								int j = expression.indexOf("(");
								instanceName = expression.substring(0, i);
								associatedMethodOrVariable = expression.substring(i+1, j);
								
						}
						else{
							instanceName = null;
							associatedMethodOrVariable = null;
							}
					
					if(currentMethodName == null && currentClassConstructorName != null){ //This will allow constructor name to be mapped to any couplings identified in the class even before encountering a method definition inside the current class, else it continues to use the method name.
						currentMethodName = currentClassConstructorName;
						System.out.println("Current METH :: "+currentMethodName);
						}
						else{
							//DO NOTHING
							}
					
					if(instanceName != null && associatedMethodOrVariable != null){
						eLevel.add(instanceName+"."+associatedMethodOrVariable);	//this will produce a list structure. This is not used anymore.

						HashMap<String,String> exprLevelMap1 = new HashMap<>();
						exprLevelMap1.put(associatedMethodOrVariable,instanceName);	//this will produce a map structure.
						
							
							if(mLevelExprDetails.get(currentMethodName) != null){
								System.out.println("mLevel Expr Eval IF Block"+mLevelExprDetails.entrySet()+"\n");
								((HashMap<String,String>)mLevelExprDetails.get(currentMethodName)).put(associatedMethodOrVariable,instanceName);
					
								System.out.println("Data from the Map : "+mLevelExprDetails.get(currentMethodName));
							
							
						
							//	mLevelExprDetails.put(currentMethodName,(HashMap<String,String>)exprLevelMap1);
								System.out.println("Method Level Expr Detail ###########"+mLevelExprDetails.entrySet()+"\n");
							}
							else{
								mLevelExprDetails.put(currentMethodName,new HashMap<String,String>());
								System.out.println("mLevel Expr Eval ELSE Block"+mLevelExprDetails.entrySet()+"\n");
								System.out.println("Currently Active Expr Map"+exprLevelMap1.entrySet());
							//	System.out.println("Currently Active Expr List"+eLevel.toArray());
							//	((HashMap<String,String>)mLevelExprDetails.get(currentMethodName)).putAll(exprLevelMap1);
								System.out.println("Previous Data from the Map : "+mLevelExprDetails.get(currentMethodName));
								((HashMap<String,String>)mLevelExprDetails.get(currentMethodName)).put(associatedMethodOrVariable,instanceName);
							//	mLevelExprDetails.put(currentMethodName,(HashMap<String,String>)exprLevelMap1);
								System.out.println("Method Level Expr Detail ###########"+mLevelExprDetails.entrySet()+"\n");
								}
						}	
						else{
							//DO NOTHING
							System.out.println("\n");
							}
					}
			}
		return visitChildren(ctx);			
	}

	
	
	
	
	
	/**
	*The following method is to summarize the parsed information succinctly in a CSV file.	
	*/
	
	
	public Void summary(){
		
		String cName1;
		String mName1;
		
			//All iterators are used to list the content of the various data structures.
		for(int j=0; j<classLevelImports.size(); j++){
		
			System.out.println("Import Statement Used :"+classLevelImports.get(j)+"\t\n");
			}
			
		for(Map.Entry<HashMap<String,String>, String> en : classLevelLocalVariableMap.entrySet()){
				System.out.println("Class Level Local Variables:\t"+en.getKey()+"|\t"+en.getValue());
				
			}
			
		Set<String> mkeys = methodLevelLocalVariableMap.keySet();
		Iterator<String> i = mkeys.iterator();
		while(i.hasNext()){
			String mkey = i.next();
			System.out.println("\t Method Level Local Variable Details: "+mkey+"|\t"+methodLevelLocalVariableMap.get(mkey)+"\n");
			}
			
		Set<String> paramkeys = methodParamDetails.keySet();
		Iterator<String> k = paramkeys.iterator();
		while(k.hasNext()){
			String paramkey = k.next();
			System.out.println("Method Parameters:\t"+paramkey+"|\t"+methodParamDetails.get(paramkey)+"\n");
			
			}
			
		

		
		//The following try catch block is used to create a CSV file with the desired output format.
		
		try{
			int count = 0;
			String csvOutputFileName = "class_Info_Summary.csv";
			boolean fileExists = true;
			PrintWriter printOutput = new PrintWriter(new File(csvOutputFileName));
			
			//The following statements create the header section of the CSV file.
			StringBuilder outputString = new StringBuilder();
			String outputColumnNames = "Current ClassName, MethodNames inside current Class, Coupling Instances inside methods, ClassName of the objects coupled to methods";
			outputString.append(outputColumnNames +"\n");
			
			
			//The following loop is designed to fetch specific information to be included in the CSV file.
			for(Map.Entry<String, HashMap<String, String>> ent : mLevelExprDetails.entrySet()){
				System.out.println("Expression Level Coupling Information:\t"+ent.getKey()+"|\t"+ent.getValue()+"\n");
			
				String s4 = ent.getKey(); 		//identifies the method name inside the coupled expression.
				System.out.println("Value of S4 : "+s4);

				String s5 = classLevelMethodNames.get(s4);   //identifies the class name linked to the method identified in earlier step.
				System.out.println("Value of S5 : "+s5);
				
				String s6 = "Imports Unavailable";	//Default String printed if a relevant import statement cannot be identified.
				
				Collection<String> exprLookUpSubSet = ent.getValue().keySet();
			//	System.out.println("Count Loop"+exprLookUpSubSet);
			
			
				for(Iterator<String> exprLookUpString = exprLookUpSubSet.iterator(); exprLookUpString.hasNext(); ){
			
					String s1 = exprLookUpString.next();		//fetches the method or variable names.
					String s2 = ent.getValue().get(s1);		//fetches the object names associated with the string s1.
				
					System.out.println("Values of S1 : "+s1);
					System.out.println("Values of S2 : "+s2);
				
					String s3 = null;
				
					for(Map.Entry<HashMap<String,String>, String> en : classLevelLocalVariableMap.entrySet()){
				
						if (en.getKey().get(s2) != null){	//fetches the class name associated with the object string s2's data type.					
							s3 = en.getKey().get(s2);
							System.out.println("Value of S3 : "+s3);
						}
						else{
							//DO NOTHING
						}
					}
					
					//	importStatementSearch logic
					String lookfor = null;
					if(s3!=null 
						&& s3.contains(".") 
						&& s3.contains("<")
						&& s3.contains(">") 
						&& s3.contains("(")){
							int x = s3.indexOf(".");
							int y = s3.indexOf("<");
							int y1 = s3.indexOf(">");
							int z = s3.indexOf("(");
							int location = Math.min(x, Math.min(y,z));
							if(location == y){
								lookfor = s3.substring(y,y1);
								}
								else{
									lookfor = s3.substring(0,location);
									}
							}
							else if(s3!=null 
								&& s3.contains(".") 
								&& s3.contains("<") 
								&& !s3.contains("(")){
									int x = s3.indexOf(".");
									int y = s3.indexOf("<");
									int y1 = s3.indexOf(">");
									int location = Math.min(x,y);
									if(location == y){
										lookfor = s3.substring(y,y1);
										}
										else{
											lookfor = s3.substring(0,location);
											}
									}
									else if(s3!=null 
										&& s3.contains(".") 
										&& s3.contains("(") 
										&& !s3.contains("<")){
											int x = s3.indexOf(".");
											int y = s3.indexOf("(");
											int location = Math.min(x,y);
											lookfor = s3.substring(0,location);
											}
											else if(s3!=null 
												&& s3.contains(".") 
												&& !s3.contains("<") 
												&& !s3.contains("(")){
													int location = s3.indexOf(".");
													lookfor = s3.substring(0,location);
												}
												else if(s3!=null
													&& !s3.contains(".")
													&& s3.contains("<")
													&& !s3.contains("(")){
													int y = s3.indexOf("<");
													int y1 = s3.indexOf(">");
													lookfor = s3.substring(y,y1);
												}
												else if(s3!=null
													&& !s3.contains(".")
													&& !s3.contains("(")
													&& !s3.contains("<")){
														lookfor = s3;
													}
													else{
														lookfor = s2;
														}
						System.out.println("looking for ____++++"+lookfor);
						
							for(int j=0; j<classLevelImports.size(); j++){
								if(classLevelImports.get(j).contains(lookfor)){
									s6 = classLevelImports.get(j);
									System.out.println("Value of S6 : "+s6);
								}
								else{
									//TO DO Error Handling if there is no entry.
									}								
							}
							
							//	importStatementSearch logic ends.
							
							if(s3!=null){
							outputString.append(s5+", "+s5+"."+s4+"(), "+s2+"."+s1+", "+s3+", "+s6 +"\n");
							}
							else{
							outputString.append(s5+", "+s5+"."+s4+"(), "+s2+"."+s1+", "+s2+", "+s6 + "\n");
							//Replacing s3.s1 with s2.s1 since there is no local variable within the current class that defines s2. This assumes that s2 is a possible generic Java Class Name.	
						}
					}
					
				}

			//The following statements complete writing to the CSV file and save it to the disk.
				printOutput.write(outputString.toString());
				printOutput.close();
			}
			catch (FileNotFoundException e){
				e.printStackTrace();
			}
			
		return null;
	}
}

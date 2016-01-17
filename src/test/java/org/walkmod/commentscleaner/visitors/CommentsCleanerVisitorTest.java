package org.walkmod.commentscleaner.visitors;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.javalang.ASTManager;
import org.walkmod.javalang.ParseException;
import org.walkmod.javalang.ast.CompilationUnit;


public class CommentsCleanerVisitorTest {

   @Test
   public void testBlockComment() throws ParseException {
      CompilationUnit cu = ASTManager.parse("public class Foo { /*for (int i = 0; i < value; i++) {\nmultiply(value);\n}*/}");
      CommentsCleanerVisitor<?> visitor = new CommentsCleanerVisitor<Object>();
      cu.accept(visitor, null);
      Assert.assertEquals(0, cu.getComments().size());
   }
   
   @Test
   public void testBlockCommentWithText() throws ParseException{
      CompilationUnit cu = ASTManager.parse("public class Foo { //TODO: Important!\n /*int a = 1;*/}");
      CommentsCleanerVisitor<?> visitor = new CommentsCleanerVisitor<Object>();
      cu.accept(visitor, null);
      Assert.assertEquals(1, cu.getComments().size());
   }
   
   @Test
   public void testToogleComment() throws ParseException{
      CompilationUnit cu = ASTManager.parse("public class Foo { \n//for (int i = 0; i < value; i++) {\n//multiply(value);\n//}\n }");
      CommentsCleanerVisitor<?> visitor = new CommentsCleanerVisitor<Object>();
      cu.accept(visitor, null);
      Assert.assertEquals(0, cu.getComments().size());
   }
   
}

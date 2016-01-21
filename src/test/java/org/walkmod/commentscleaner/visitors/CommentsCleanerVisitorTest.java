package org.walkmod.commentscleaner.visitors;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.walkmod.javalang.ASTManager;
import org.walkmod.javalang.ParseException;
import org.walkmod.javalang.actions.Action;
import org.walkmod.javalang.actions.ActionsApplier;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.walkers.ChangeLogVisitor;
import org.walkmod.walkers.VisitorContext;


public class CommentsCleanerVisitorTest {

   @Test
   public void testBlockComment() throws ParseException {
      CompilationUnit cu = ASTManager.parse("public class Foo { /*for (int i = 0; i < value; i++) {\nmultiply(value);\n}*/}");
      CommentsCleanerVisitor<?> visitor = new CommentsCleanerVisitor<Object>();
      cu.accept(visitor, null);
      assertEquals(0, cu.getComments().size());
   }
   
   @Test
   public void testBlockCommentWithText() throws ParseException{
      CompilationUnit cu = ASTManager.parse("public class Foo { //TODO: Important!\n /*int a = 1;*/}");
      CommentsCleanerVisitor<?> visitor = new CommentsCleanerVisitor<Object>();
      cu.accept(visitor, null);
      assertEquals(1, cu.getComments().size());
   }
   
   @Test
   public void testToogleComment() throws ParseException{
      CompilationUnit cu = ASTManager.parse("public class Foo { \n//for (int i = 0; i < value; i++) {\n//multiply(value);\n//}\n }");
      CommentsCleanerVisitor<?> visitor = new CommentsCleanerVisitor<Object>();
      cu.accept(visitor, null);
      assertEquals(0, cu.getComments().size());
   }


    @Test
    public void testLastCommentIsLineComment() throws Exception {
        String code = IOUtils.toString(new File("src/test/resources/Test1.txt").toURI());
        CompilationUnit cu = ASTManager.parse(code);

        CommentsCleanerVisitor<?> commentsCleanerVisitor = new CommentsCleanerVisitor<Object>();
        cu.accept(commentsCleanerVisitor, null);

        ChangeLogVisitor changeLogVisitor = new ChangeLogVisitor();
        VisitorContext ctx = new VisitorContext();
        ctx.put(ChangeLogVisitor.NODE_TO_COMPARE_KEY, ASTManager.parse(code));
        changeLogVisitor.visit(cu, ctx);
        List<Action> actions = changeLogVisitor.getActionsToApply();
        ActionsApplier applier = new ActionsApplier();
        applier.setActionList(actions);
        applier.setText(code);
        applier.execute();
        String result = applier.getModifiedText();
        assertEquals(code, result);
    }
   
}

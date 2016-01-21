package org.walkmod.commentscleaner.visitors;

import java.beans.Statement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;
import org.walkmod.javalang.ASTManager;
import org.walkmod.javalang.ParseException;
import org.walkmod.javalang.ast.BlockComment;
import org.walkmod.javalang.ast.Comment;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.LineComment;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;

public class CommentsCleanerVisitor<T> extends VoidVisitorAdapter<T> {
   private static final Logger log = Logger.getLogger(CommentsCleanerVisitor.class);

   @Override
   public void visit(CompilationUnit cu, T ctx) {
      List<Comment> comments = cu.getComments();
      if (comments != null) {
         List<Comment> finalComments = new LinkedList<Comment>();

         Stack<List<Comment>> toogleComments = new Stack<List<Comment>>();

         Comment lastLineComment = null;
         for (Comment comment : comments) {
            if (comment instanceof BlockComment) {
               if (!requiresToDelete(comment.getContent())) {
                  finalComments.add(comment);
               }

            } else if (comment instanceof LineComment) {
               if (lastLineComment == null || lastLineComment.getBeginLine() != comment.getBeginLine() - 1) {
                  LinkedList<Comment> list = new LinkedList<Comment>();
                  list.add(comment);
                  toogleComments.push(list);

               } else {
                  List<Comment> toogleComment = toogleComments.peek();
                  toogleComment.add(comment);

               }
               lastLineComment = comment;
            } else {
               finalComments.add(comment);
            }
         }

         for (List<Comment> toogle : toogleComments) {
            if (!requiresToDelete(toogle)) {
               if (!finalComments.isEmpty()) {
                  // merge toogle into finalComments according to their position
                  Comment firstComment = toogle.get(0);
                  boolean added = false;
                  List<Comment> accum = new LinkedList<Comment>();
                  for (Comment addedComment : finalComments) {
                     if (addedComment.isPreviousThan(firstComment)) {
                        accum.add(addedComment);
                     } else {
                        if (!added) {
                           accum.addAll(toogle);
                           added = true;
                        }
                        accum.add(addedComment);
                     }
                  }
                  if (!added) {
                     accum.addAll(toogle);
                  }
                  finalComments = accum;
               } else{
                  finalComments = toogle;
               }
            }
            
         }

         cu.setComments(finalComments);
      }
   }

   private boolean requiresToDelete(List<Comment> comments) {
      String code = "";
      for (Comment c : comments) {
         code +=  c.getContent();
      }
      return requiresToDelete(code);
   }

   private boolean requiresToDelete(String code) {
      try {
         if(!code.startsWith("{")){
            code = "{"+code+"}";
         }
         ASTManager.parse(Statement.class, code);
         log.debug("Deleting " + code);
         return true;
      } catch (ParseException e) {
      }
      return false;
   }

}
